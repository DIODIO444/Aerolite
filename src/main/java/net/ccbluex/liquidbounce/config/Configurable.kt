/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonElement

/**
 * Something that can hold [Value]s. Both [net.ccbluex.liquidbounce.features.module.Module] and value
 * grouping objects implement this so the new `float()` / `boolean()` / `choices()` DSL can register
 * values onto the correct owner via the implicit receiver.
 *
 * Groups created via [Configurable] are themselves [Value]s (see [ConfigurableImpl]), so a group can be
 * added as a child of another configurable with `addValue(group)`.
 */
interface Configurable {
    val name: String
    val registeredValues: MutableList<Value<*>>
    val values: List<Value<*>>

    fun addValue(value: Value<*>): Value<*> {
        if (value !in registeredValues) registeredValues.add(value)
        return value
    }

    fun addValues(values: Collection<Value<*>>) = apply {
        values.forEach { addValue(it) }
    }

    /**
     * Move the values identified by [names] from this configurable into [group].
     */
    fun moveValues(group: Configurable, vararg names: String) {
        for (name in names) {
            val iterator = registeredValues.iterator()
            while (iterator.hasNext()) {
                val v = iterator.next()
                if ((v as? Value<*>)?.name.equals(name, true)) {
                    iterator.remove()
                    group.addValue(v)
                }
            }
        }
    }

    /**
     * Exposes a [value] owned by another container (e.g. a shared manager singleton) in this
     * container too, without stealing ownership: every sharing container renders and serializes it,
     * while the canonical owner keeps it. Mirrors FDPClient's `Configurable.shared`.
     */
    fun <T, V : Value<T>> shared(value: V): V {
        addValue(value)
        return value
    }

    /**
     * Creates a nested group named [name] and moves the values identified by [names] into it.
     * Mirrors FDPClient's `Configurable.group`.
     */
    fun group(name: String, vararg names: String): ConfigurableImpl {
        val group = ConfigurableImpl(name)
        moveValues(group, *names)
        addValue(group)
        return group
    }

    /**
     * Registers [this] value onto the configurable: `val x by +someValue`.
     * Mirrors FDPClient's `Configurable.unaryPlus`.
     */
    operator fun <T, V : Value<T>> V.unaryPlus(): V {
        addValue(this)
        return this
    }
}

/**
 * Concrete [Configurable] used to visually group values (`Configurable("General")`). It is itself a
 * [Value] so it can be nested inside another configurable.
 */
open class ConfigurableImpl(name: String) :
    Value<MutableList<Value<*>>>(name, mutableListOf()), Configurable {

    override val registeredValues: MutableList<Value<*>>
        get() = get()

    override val values: List<Value<*>>
        get() = get()

    override fun toJson(): JsonElement? = null
    override fun fromJson(element: JsonElement) {}
}

fun Configurable(name: String): ConfigurableImpl = ConfigurableImpl(name)
