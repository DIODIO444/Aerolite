/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient
 */
package net.ccbluex.liquidbounce.value

import com.google.gson.JsonElement
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.Translate
import kotlin.reflect.KProperty

abstract class Value<T>(val name: String, open var value: T) {
    val default = value
    val ValueTranslate = Translate(0F, 0F)

    var description: String = ""

    /**
     * Alternative names this value can be addressed by (FDPClient compatibility).
     */
    var aliases: List<String> = emptyList()

    /**
     * Register alternative names for this value.
     */
    fun aliases(vararg names: String) = apply { aliases = names.toList() }

    private var displayableFunc: () -> Boolean = { true }

    fun displayable(func: () -> Boolean): Value<T> {
        displayableFunc = func
        return this
    }
    private var displayN: () -> String = { "" }
    fun displayN(func: () -> String): Value<T> {
        displayN = func
        return this
    }
    val displayName: String
        get() = name

    val displayable: Boolean
        get() = displayableFunc()

    // --- New DSL support (ported from FDPClient) ---
    var subjective: Boolean = false
        private set
    var hidden: Boolean = false
        private set
    var immutable: Boolean = false
        private set
    var excluded: Boolean = false
        private set

    private val onChangeInterceptors = mutableListOf<(old: T, new: T) -> T>()
    private var supportCondition: () -> Boolean = { true }

    fun subjective(): Value<T> { subjective = true; return this }
    fun hide(): Value<T> { hidden = true; return this }

    /**
     * Excludes this value from serialization, fixing [state] as its current value
     * (FDPClient compatibility).
     */
    fun excludeWithState(state: T = value) {
        value = state
        excluded = true
    }

    /**
     * Hides this value, fixing [state] as its current value (FDPClient compatibility).
     */
    fun hideWithState(state: T = value) {
        value = state
        hidden = true
    }
    fun immutable(): Value<T> { immutable = true; return this }
    fun exclude(): Value<T> { excluded = true; return this }

    /**
     * Register a value transformer invoked before the value is committed. The returned value replaces [new].
     */
    fun onChange(handler: (old: T, new: T) -> T): Value<T> {
        onChangeInterceptors.add(handler)
        return this
    }

    fun isActive(): Boolean = displayable && supportCondition()

    /**
     * Whether this value is currently supported (FDPClient compatibility).
     */
    fun isSupported(): Boolean = supportCondition()
    fun shouldRender(): Boolean = isActive() && !hidden

    /**
     * Compose an additional condition that decides whether this value is active/supported.
     * Mirrors FDPClient's `Value.setSupport`.
     */
    fun setSupport(condition: (Boolean) -> Boolean) = apply {
        val oldCondition = supportCondition
        supportCondition = { condition(oldCondition.invoke()) }
    }

    // Public change listener used by the new DSL (value.onChanged { ... })
    private var changeCallback: ((T) -> Unit)? = null

    /**
     * Attach a description shown in the GUI.
     */
    open fun describe(text: String): Value<T> {
        description = text
        return this
    }

    /**
     * Register a callback invoked with the new value after it changes.
     */
    fun onChanged(callback: (T) -> Unit): Value<T> {
        changeCallback = callback
        return this
    }

    fun set(newValue: T) {
        if (newValue == value) return

        val oldValue = get()

        try {
            var handled = newValue
            onChangeInterceptors.forEach { handled = it(oldValue, handled) }
            onChange(oldValue, handled)
            changeValue(handled)
            onChanged(oldValue, handled)
            changeCallback?.invoke(handled)
            LiquidBounce.configManager.smartSave()
        } catch (e: Exception) {
            ClientUtils.logError("[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]")
        }
    }

    fun get() = value

    /**
     * Kotlin property delegation support: `val x by someValue`.
     */
    open operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

    open operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        set(newValue)
    }

    fun setDefault() {
        value = default
    }

    open fun changeValue(value: T) {
        this.value = value
    }

    abstract fun toJson(): JsonElement?
    abstract fun fromJson(element: JsonElement)

    protected open fun onChange(oldValue: T, newValue: T) {}
    protected open fun onChanged(oldValue: T, newValue: T) {}

    // this is better api for ListValue and TextValue

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (value is String && other is String) {
            return (value as String).equals(other, true)
        }
        return value?.equals(other) ?: false
    }

    fun contains(text: String/*, ignoreCase: Boolean*/): Boolean {
        return if (value is String) {
            (value as String).contains(text, true)
        } else {
            false
        }
    }

    /**
     * Readable representation of the current value.
     *
     * Without this, any code path that prints a [Value] instead of calling [get] (GUIs, commands,
     * logs) leaks `net.ccbluex.liquidbounce.value.BoolValue@1a2b3c` into the output.
     */
    override fun toString(): String = value?.toString() ?: "null"
}
