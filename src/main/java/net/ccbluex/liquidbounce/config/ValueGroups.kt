/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 *
 * Adapted from FDPClient's ValueGroups.kt for Aerolite's interface-based [Configurable] (FDPClient uses an
 * open-class Configurable with member DSL, but Aerolite's Module already extends MinecraftInstance, so the
 * interface + extension-DSL design is preserved). Group subclasses therefore extend [ConfigurableImpl]
 * and call the extension DSL functions (boolean/int/float/...) with a `displayable` lambda.
 */
package net.ccbluex.liquidbounce.config

import java.awt.Color

/**
 * A named group whose children are only visible/active while its master [enabledValue] boolean is on.
 */
open class ToggleableValueGroup(
    name: String,
    enabled: Boolean = false,
    private val isSupported: (() -> Boolean)? = null,
) : ConfigurableImpl(name) {

    val enabledValue = boolean("Enabled", enabled) { isSupported?.invoke() ?: true }

    var enabled by enabledValue

    /**
     * Whether the group is currently expanded: supported and switched on.
     */
    fun isExpanded() = enabledValue.isActive()

    private fun gate(extra: (() -> Boolean)?): () -> Boolean = {
        enabled && (isSupported?.invoke() ?: true) && (extra?.invoke() ?: true)
    }

    fun gatedInt(name: String, value: Int, range: IntRange, suffix: String = "", isSupported: (() -> Boolean)? = null) =
        int(name, value, range, suffix, displayable = gate(isSupported))

    fun gatedFloat(name: String, value: Float, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE, suffix: String = "", isSupported: (() -> Boolean)? = null) =
        float(name, value, range, suffix, displayable = gate(isSupported))

    fun gatedBoolean(name: String, value: Boolean, isSupported: (() -> Boolean)? = null) =
        boolean(name, value, displayable = gate(isSupported))

    fun gatedChoices(name: String, values: Array<String>, value: String, isSupported: (() -> Boolean)? = null) =
        choices(name, values, value, displayable = gate(isSupported))

    fun gatedText(name: String, value: String, isSupported: (() -> Boolean)? = null) =
        text(name, value, displayable = gate(isSupported))

    fun gatedIntRange(name: String, value: IntRange, range: IntRange, suffix: String = "", isSupported: (() -> Boolean)? = null) =
        intRange(name, value, range, suffix, displayable = gate(isSupported))

    fun gatedFloatRange(name: String, value: ClosedFloatingPointRange<Float>, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE, suffix: String = "", isSupported: (() -> Boolean)? = null) =
        floatRange(name, value, range, suffix, displayable = gate(isSupported))

    fun gatedColor(name: String, value: Color, rainbow: Boolean = false, isSupported: (() -> Boolean)? = null) =
        color(name, value, rainbow, displayable = gate(isSupported))

    fun gatedColor(name: String, value: Int, rainbow: Boolean = false, isSupported: (() -> Boolean)? = null) =
        color(name, value, rainbow, displayable = gate(isSupported))
}
