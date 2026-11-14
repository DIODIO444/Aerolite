/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.config

import net.minecraft.client.gui.FontRenderer
import java.awt.Color

open class ToggleableValueGroup(
    name: String,
    enabled: Boolean = false,
    private val isSupported: (() -> Boolean)? = null,
) : Configurable(name) {

    val enabledValue = boolean("Enabled", enabled) { isSupported?.invoke() ?: true }

    var enabled by enabledValue

    /**
     * Whether the group is currently expanded: supported and switched on.
     */
    fun isExpanded() = enabledValue.isActive()

    private fun gate(extra: (() -> Boolean)?): () -> Boolean = {
        enabled && (isSupported?.invoke() ?: true) && (extra?.invoke() ?: true)
    }

    fun gatedInt(name: String, value: Int, range: IntRange, suffix: String? = null, isSupported: (() -> Boolean)? = null) =
        super.int(name, value, range, suffix, gate(isSupported))

    fun gatedFloat(name: String, value: Float, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE, suffix: String? = null, isSupported: (() -> Boolean)? = null) =
        super.float(name, value, range, suffix, gate(isSupported))

    fun gatedBoolean(name: String, value: Boolean, isSupported: (() -> Boolean)? = null) =
        super.boolean(name, value, gate(isSupported))

    fun gatedChoices(name: String, values: Array<String>, value: String, isSupported: (() -> Boolean)? = null) =
        super.choices(name, values, value, gate(isSupported))

    fun gatedText(name: String, value: String, isSupported: (() -> Boolean)? = null) =
        super.text(name, value, gate(isSupported))

    fun gatedBlock(name: String, value: Int, isSupported: (() -> Boolean)? = null) =
        super.block(name, value, gate(isSupported))

    fun gatedFont(name: String, value: FontRenderer, isSupported: (() -> Boolean)? = null) =
        super.font(name, value, gate(isSupported))

    fun gatedIntRange(name: String, value: IntRange, range: IntRange, suffix: String? = null, isSupported: (() -> Boolean)? = null) =
        super.intRange(name, value, range, suffix, gate(isSupported))

    fun gatedFloatRange(name: String, value: ClosedFloatingPointRange<Float>, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE, suffix: String? = null, isSupported: (() -> Boolean)? = null) =
        super.floatRange(name, value, range, suffix, gate(isSupported))

    fun gatedColor(name: String, value: Color, rainbow: Boolean = false, isSupported: (() -> Boolean)? = null) =
        super.color(name, value, rainbow, gate(isSupported))

    fun gatedColor(name: String, value: Int, rainbow: Boolean = false, isSupported: (() -> Boolean)? = null) =
        super.color(name, value, rainbow, gate(isSupported))
}
