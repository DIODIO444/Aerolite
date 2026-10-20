/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.Event
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.features.module.modules.client.Modules
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.Animation
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.Translate
import net.ccbluex.liquidbounce.value.*
import org.lwjgl.input.Keyboard
import java.util.IdentityHashMap

open class Module() :MinecraftInstance(), Listenable, Configurable {

    // Module information
    override var name: String = ""
    val tab = Translate(0f , 0f)
    var localizedName = ""
        get() = field.ifEmpty { name }
    var expanded: Boolean = false
    var description: String
    var category: ModuleCategory
    @JvmField
    var showSettings = false
    @JvmField
    var yPos1 = 30F
    val valueTranslate = Translate(0F, 0F)
    val moduleTranslate = Translate(0F, 0F)
    var Loading = false;
    var Save = false;
    var Start=false;
    var keyBind = Keyboard.CHAR_NONE
        set(keyBind) {
            field = keyBind

            if (!LiquidBounce.isStarting) {
                LiquidBounce.configManager.smartSave()
            }
        }
    var array = true
        set(array) {
            field = array

            if (!LiquidBounce.isStarting) {
                LiquidBounce.configManager.smartSave()
            }
        }
    var canEnable: Boolean
    var autoDisable: EnumAutoDisableType
    var triggerType: EnumTriggerType
    var moduleCommand: Boolean
    val moduleInfo: ModuleInfo? = javaClass.getAnnotation(ModuleInfo::class.java)
    var splicedName = ""

    // New DSL support: values registered via the float()/boolean()/choices() delegate DSL
    override val registeredValues = mutableListOf<Value<*>>()

    // Sub-category used by the new module DSL
    var subCategory: Category.SubCategory = Category.SubCategory.NONE

    // Whether this module should be shown in the "game detecting" HUD list
    var gameDetecting: Boolean = true

    /**
     * Human-readable spaced variant of [name] used by the HUD list (e.g. "KillAura" -> "Kill Aura").
     */
    var gameDetectingName: String = ""
        get() {
            if (field.isEmpty()) {
                val sb = StringBuilder()
                val arr = name.toCharArray()
                for (i in arr.indices) {
                    val char = arr[i]
                    if (i != 0 && !Character.isLowerCase(char) && Character.isLowerCase(arr[i - 1])) {
                        sb.append(' ')
                    }
                    sb.append(char)
                }
                field = sb.toString()
            }
            return field
        }

    init {
        // Defaults (also used by modules that don't carry @ModuleInfo)
        name = javaClass.simpleName
        description = "%module.$name.description%"
        category = ModuleCategory.MISC
        keyBind = Keyboard.CHAR_NONE
        array = true
        canEnable = true
        autoDisable = EnumAutoDisableType.NONE
        moduleCommand = true
        triggerType = EnumTriggerType.TOGGLE

        moduleInfo?.let {
            name = it.name
            description = "%module.${it.name}.description%"
            category = it.category
            keyBind = it.keyBind
            array = it.array
            canEnable = it.canEnable
            autoDisable = it.autoDisable
            moduleCommand = it.moduleCommand
            triggerType = it.triggerType
            subCategory = it.subCategory
        }
    }

    /**
     * New-style constructor used by the FDPClient-ported modules (no @ModuleInfo required).
     */
    constructor(
        name: String,
        category: ModuleCategory,
        subCategory: Category.SubCategory = Category.SubCategory.NONE,
        keyBind: Int = Keyboard.CHAR_NONE,
        gameDetecting: Boolean = true
    ) : this() {
        this.name = name
        this.category = category
        this.subCategory = subCategory
        this.keyBind = keyBind
        this.gameDetecting = gameDetecting
    }

    // ----- handler {} event DSL support (see net.ccbluex.liquidbounce.event.Listenable.handler) -----

    val numberValues: List<Value<*>>
        get() = values.filter { it is IntegerValue || it is FloatValue }

    val booleanValues: List<BoolValue>
        get() = values.filterIsInstance<BoolValue>()

    val listValues: List<ListValue>
        get() = values.filterIsInstance<ListValue>()

    open fun onLoad() {
        localizedName = name
    }

    // Current state of module
    var state = false
        set(value) {
            if (field == value) return

            // Call toggle
            onToggle(value)

            // Play sound and add notification
            if (!LiquidBounce.isStarting && Modules.showNotification.get()) {
                if (value) {
                    Modules.playSound(true)
                    LiquidBounce.hud.addNotification(Notification(if (Modules.notificationLongMessage.get())"%notify.module.longTitle%" else "%notify.module.title%",
                        if (Modules.notificationLongMessage.get()) LanguageManager.getAndFormat("notify.module.longEnable", localizedName) else LanguageManager.getAndFormat("notify.module.enable", localizedName), NotifyType.SUCCESS))
                } else {
                    Modules.playSound(false)
                    LiquidBounce.hud.addNotification(Notification(if (Modules.notificationLongMessage.get())"%notify.module.longTitle%" else "%notify.module.title%",
                        if (Modules.notificationLongMessage.get()) LanguageManager.getAndFormat("notify.module.longDisable", localizedName) else LanguageManager.getAndFormat("notify.module.disable", localizedName), NotifyType.ERROR))
                }
            }

            // Call on enabled or disabled
            try {
                if (value) {
                    onEnable()

                    if (canEnable) {
                        field = true
                    }
                } else {
                    onDisable()

                    field = false
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            // Save module state
            LiquidBounce.configManager.smartSave()
        }

    // HUD
    val hue = Math.random().toFloat()
    var slideAnimation: Animation? = null
    var slide = 0f
        get() {
            if (slideAnimation != null) {
                field = slideAnimation!!.value.toFloat()
                if (slideAnimation!!.state == Animation.EnumAnimationState.STOPPED) {
                    slideAnimation = null
                }
            }
            return field
        }
        set(value) {
            if (slideAnimation == null || (slideAnimation != null && slideAnimation!!.to != value.toDouble())) {
                slideAnimation = Animation(EaseUtils.EnumEasingType.valueOf(HUD.arraylistXAxisAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(HUD.arraylistXAxisAnimOrderValue.get()), field.toDouble(), value.toDouble(), HUD.arraylistXAxisAnimSpeedValue.get() * 30L).start()
            }
        }
    var yPosAnimation: Animation? = null
    var yPos = 0f
        get() {
            if (yPosAnimation != null) {
                field = yPosAnimation!!.value.toFloat()
                if (yPosAnimation!!.state == Animation.EnumAnimationState.STOPPED) {
                    yPosAnimation = null
                }
            }
            return field
        }
        set(value) {
            if (yPosAnimation == null || (yPosAnimation != null && yPosAnimation!!.to != value.toDouble())) {
                yPosAnimation = Animation(EaseUtils.EnumEasingType.valueOf(HUD.arraylistYAxisAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(HUD.arraylistYAxisAnimOrderValue.get()), field.toDouble(), value.toDouble(), HUD.arraylistYAxisAnimSpeedValue.get() * 30L).start()
            }
        }

    // Tag
    open val tag: String?
        get() = null

    val tagName: String
        get() = "$name${if (tag == null) "" else " §7$tag"}"

    val colorlessTagName: String
        get() = "$name${if (tag == null) "" else " " + stripColor(tag!!)}"

    open var width = 10

    /**
     * Toggle module
     */
    fun toggle() {
        state = !state
    }

    /**
     * Print [msg] to chat as alert
     */
    protected fun alert(msg: String) = ClientUtils.displayAlert(msg)

    /**
     * Print [msg] to chat as plain text
     */
    protected fun chat(msg: String) = ClientUtils.displayChatMessage(msg)

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
     * Called when module initialized
     */
    open fun onInitialize() {}

    /**
     * Get all values of module (reflected @ModuleInfo-style fields + DSL-registered values)
     *
     * Group containers (the `Configurable`/`ConfigurableImpl` values created by the new DSL) are
     * filtered out: the ClickGUI implementations can only render leaf values, and printing a group
     * would dump its raw child list (`[BoolValue@1a2b3c, ...]`) into the GUI. The children of a
     * group are declared as fields on the module (or moved into the group by name), so they are
     * still listed here and stay editable.
     */
    override val values: List<Value<*>>
        get() {
            val reflected = ClassUtils.getValues(javaClass, this)
            val result = ArrayList<Value<*>>(reflected)
            for (v in registeredValues) if (result.none { it.name == v.name }) result.add(v)
            return result.filter { it !is Configurable }
        }

    /**
     * Value tree with the group containers kept in place.
     *
     * Only used by the ClickGUI when `ClickGUIModule.groupSettings` is enabled: it lets the GUI draw
     * a header for every `Configurable` group and list that group's children below it.
     * Leaves that belong to a group are not repeated at the top level.
     */
    val valueTree: List<Value<*>>
        get() {
            val top = ArrayList<Value<*>>()

            fun addTop(value: Value<*>) {
                if (top.none { it === value }) top.add(value)
            }

            ClassUtils.getValues(javaClass, this).forEach(::addTop)
            registeredValues.forEach(::addTop)

            // Identity based, because Value#equals compares the wrapped values, not the instances.
            val grouped = IdentityHashMap<Value<*>, Boolean>()

            fun markGroup(value: Value<*>) {
                if (value !is Configurable) return
                value.values.forEach {
                    grouped[it] = true
                    markGroup(it)
                }
            }

            top.forEach(::markGroup)
            val rows = top.filter { it is Configurable || !grouped.containsKey(it) }

            // Modules such as Flight/Speed/LongJump expose extra values by overriding [values]
            // (mode specific settings). Keep those visible in the tree as well.
            val dynamic = values.filter { candidate ->
                rows.none { it === candidate } && !grouped.containsKey(candidate)
            }

            return rows + dynamic
        }

    /**
     * Get module by [valueName]
     */
    open fun getValue(valueName: String) = values.find { it.name.equals(valueName, ignoreCase = true) }

    /**
     * Events should be handled when module is enabled
     */
    override fun handleEvents() = state

}