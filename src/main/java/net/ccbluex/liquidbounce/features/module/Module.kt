/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.utils.render.Animation
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.LiquidBounce.isStarting
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.misc.GameDetector
import net.ccbluex.liquidbounce.file.FileManager.modulesConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.file.FileManager.valuesConfig
import net.ccbluex.liquidbounce.lang.translation
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.sound.ClickSound
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Arraylist
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.toLowerCamelCase
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.timing.TickedActions.clearTicked
import org.lwjgl.input.Keyboard

private val SPLIT_REGEX = "(?<=[a-z])(?=[A-Z])".toRegex()

open class Module @JvmOverloads constructor(
    name: String,
    val category: Category,
    defaultKeyBind: Int = Keyboard.KEY_NONE,
    private val canBeEnabled: Boolean = true,
    private val forcedDescription: String? = null,

    // Adds spaces between lowercase and uppercase letters (KillAura -> Kill Aura)
    val spacedName: String = name.splitToSequence(SPLIT_REGEX).joinToString(separator = " "),
    subjective: Boolean = category == Category.RENDER,
    val gameDetecting: Boolean = canBeEnabled,
    defaultState: Boolean = false,
    defaultHidden: Boolean = false,
) : Configurable(name), MinecraftInstance, Listenable {

    init {
        if (subjective) {
            subjective()
        }

        // Support the ported Aerolite modules, which declare listeners with @EventTarget.
        EventManager.registerAnnotated(this)
    }

    /**
     * Registers settings that were declared as plain fields instead of through the `by` DSL.
     *
     * The ported Aerolite modules (and the Java ones) create their settings as
     * `val x = BoolValue("X", true)` fields, so the owning module has to pick them up after
     * construction — the field initialisers have not run yet when [Module.init] executes.
     */
    fun scanDeclaredValues() {
        var type: Class<*>? = javaClass

        while (type != null && type != Module::class.java) {
            for (field in type.declaredFields) {
                // Kotlin `object`s carry a static `INSTANCE` field. Since Module itself is a Value
                // (via Configurable), that field would pass the type check below and register the
                // module as its own setting — which makes Value.isSupported() recurse forever.
                if (java.lang.reflect.Modifier.isStatic(field.modifiers)) {
                    continue
                }

                if (!Value::class.java.isAssignableFrom(field.type)) {
                    continue
                }

                // Never register a Configurable (module / nested config) as a value.
                if (Configurable::class.java.isAssignableFrom(field.type)) {
                    continue
                }

                field.isAccessible = true
                val value = field.get(this) as? Value<*> ?: continue

                if (value === this || value.owner != null) {
                    continue
                }

                addValue(value)
            }

            type = type.superclass
        }
    }

    // Value that determines whether the module should depend on GameDetector
    private val onlyInGameValue = boolean("OnlyInGame", true) {
        GameDetector.state
    }.subjective().excludeWhen(!gameDetecting)

    // Module information

    /** Pushes a notification to the HUD (Aerolite compatibility). */
    fun addNotification(notification: net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification) {
        net.ccbluex.liquidbounce.LiquidBounce.hud.addNotification(notification)
    }

    /** Spaced name, Aerolite naming. */
    val splicedName: String get() = spacedName

    /** Plain name, Aerolite naming. */
    val localizedName: String get() = name

    // Get normal or spaced name
    fun getName(spaced: Boolean = false) = if (spaced) spacedName else name

    var keyBind = defaultKeyBind
        set(keyBind) {
            field = keyBind

            saveConfig(modulesConfig)
        }

    var isHidden: Boolean by boolean("Hide", defaultHidden).subjective().onChanged {
        saveConfig(modulesConfig)
    }

    private val resetValue = boolean("Reset", false).subjective().onChange { _, _ ->
        try {
            values.forEach { if (it !== this) it.resetValue() else return@forEach }
        } catch (any: Exception) {
            LOGGER.error("Failed to reset all values", any)
            chat("Failed to reset all values: ${any.message}")
        } finally {
            addNotification(Notification(this.spacedName, "Successfully reset settings"))
            saveConfig(valuesConfig)
        }
        return@onChange false
    }

    /** Flattened settings tree used by the ported Aerolite click GUIs. */
    val valueTree: List<net.ccbluex.liquidbounce.config.Value<*>>
        get() {
            val result = ArrayList<net.ccbluex.liquidbounce.config.Value<*>>()

            fun walk(list: List<net.ccbluex.liquidbounce.config.Value<*>>) {
                for (value in list) {
                    result += value

                    if (value is net.ccbluex.liquidbounce.config.Configurable) {
                        walk(value.values)
                    }
                }
            }

            walk(values)
            return result
        }

    val description
        get() = forcedDescription
            ?: name.takeIf { it.isNotEmpty() }?.let { translation("module.${it.toLowerCamelCase()}.description") }
            ?: ""

    /** Per-module translate animation used by the ported tab GUIs. */
    val tab = net.ccbluex.liquidbounce.utils.render.Translate(0F, 0F)

    /** Prints a prefixed chat message, Aerolite style. */
    protected fun alert(message: String) =
        net.ccbluex.liquidbounce.utils.client.ClientUtils.displayChatMessage(message)

    var slideStep = 0F

    /** Whether this module's settings are expanded in the ported Tenacity dropdown GUI. */
    var expanded = false

    // Current state of module
    var state = defaultState
        set(value) {
            if (field == value) return

            // Call toggle
            onToggle(value)

            // Clear ticked actions
            clearTicked()

            // Play sound and add notification
            if (!isStarting) {
                try {
                    val state = if (value) {
                        "Enabled" to Notifications.SeverityType.SUCCESS
                    } else {
                        // Red bar, but still the success check mark — disabling is not an error.
                        "Disabled" to Notifications.SeverityType.RED_SUCCESS
                    }

                    val texts = translation("notification.module${state.first}", getName()).split(" ")
                    val (title, description) = texts[0] to texts[1]

                    ClickSound.play(value)
                    LiquidBounce.hud.addNotification(
                        Notification(title, description, severityType = state.second)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Call on enabled or disabled
            if (value) {
                onEnable()

                if (canBeEnabled) field = true
            } else {
                onDisable()
                field = false
            }

            // Save module state
            saveConfig(modulesConfig)
        }

    // HUD
    val hue = nextFloat()

    /**
     * Horizontal slide of this module's arraylist entry.
     *
     * Ported from Aerolite-main1: the setter starts an [Animation] instead of assigning the value
     * directly, so toggling a module eases the entry in/out instead of snapping.
     */
    var slideAnimation: Animation? = null
    var slide = 0F
        get() {
            slideAnimation?.let {
                field = it.value.toFloat()
                if (it.state == Animation.EnumAnimationState.STOPPED) {
                    slideAnimation = null
                }
            }
            return field
        }
        set(value) {
            if (slideAnimation == null || slideAnimation!!.to != value.toDouble()) {
                slideAnimation = Animation(
                    EaseUtils.EnumEasingType.valueOf(net.ccbluex.liquidbounce.features.module.modules.render.HUD.arraylistXAxisAnimType),
                    EaseUtils.EnumEasingOrder.valueOf(net.ccbluex.liquidbounce.features.module.modules.render.HUD.arraylistXAxisAnimOrder),
                    field.toDouble(), value.toDouble(), net.ccbluex.liquidbounce.features.module.modules.render.HUD.arraylistXAxisAnimSpeed * 30L
                ).start()
            }
        }

    /** Vertical position of this module's arraylist entry, animated the same way as [slide]. */
    var yPosAnimation: Animation? = null
    var yPos = 0F
        get() {
            yPosAnimation?.let {
                field = it.value.toFloat()
                if (it.state == Animation.EnumAnimationState.STOPPED) {
                    yPosAnimation = null
                }
            }
            return field
        }
        set(value) {
            if (yPosAnimation == null || yPosAnimation!!.to != value.toDouble()) {
                yPosAnimation = Animation(
                    EaseUtils.EnumEasingType.valueOf(net.ccbluex.liquidbounce.features.module.modules.render.HUD.arraylistYAxisAnimType),
                    EaseUtils.EnumEasingOrder.valueOf(net.ccbluex.liquidbounce.features.module.modules.render.HUD.arraylistYAxisAnimOrder),
                    field.toDouble(), value.toDouble(), net.ccbluex.liquidbounce.features.module.modules.render.HUD.arraylistYAxisAnimSpeed * 30L
                ).start()
            }
        }

    /** Whether this module is listed by the HUD arraylist. */
    var array = true

    /** Cached arraylist entry width (set by the Arraylist element each frame). */
    var width = 0

    var yAnim = 0f

    // Tag
    open val tag: String?
        get() = null

    /**
     * Toggle module
     */
    fun toggle() {
        state = !state
    }

    /**
     * Called when module toggled
     */
    open fun onToggle(state: Boolean) {}

    /**
     * Called when module enabled
     */
    open fun onEnable() {}

    /**
     * Called when module disabled
     */
    open fun onDisable() {}

    /**
     * Called when module unregistered (for scripts)
     */
    open fun onUnregister() {}

    /**
     * Get value by [valueName]
     */
    fun getValue(valueName: String) = findValue(valueName)

    /**
     * Get value via `module[valueName]`
     */
    operator fun get(valueName: String) = getValue(valueName)

    val isActive
        get() = !gameDetecting || !onlyInGameValue.get() || GameDetector.isInGame()

    /**
     * Events should be handled when module is enabled
     */
    override fun handleEvents() = state && isActive
}
