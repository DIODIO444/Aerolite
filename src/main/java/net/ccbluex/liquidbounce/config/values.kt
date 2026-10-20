/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import net.ccbluex.liquidbounce.value.BlockValue
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.client.gui.FontRenderer
import java.awt.Color

/**
 * Multi-select value created from a fixed set of [options].
 */
fun Configurable.multiSelect(
    name: String,
    options: Array<String>,
    displayable: () -> Boolean = { true }
): MultiSelectValue = register(MultiSelectValue(name, options).displayable(displayable)) as MultiSelectValue

/**
 * Register [value] onto this configurable and return it so it can be used as a property delegate.
 */
private fun Configurable.register(value: Value<*>): Value<*> {
    registeredValues.add(value)
    return value
}

fun Configurable.float(
    name: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    suffix: String = "",
    displayable: () -> Boolean = { true }
): FloatValue = register(FloatValue(name, value, range.start, range.endInclusive).displayable(displayable)) as FloatValue

fun Configurable.boolean(
    name: String,
    value: Boolean,
    displayable: () -> Boolean = { true }
): BoolValue = register(BoolValue(name, value).displayable(displayable)) as BoolValue

fun Configurable.int(
    name: String,
    value: Int,
    range: IntRange,
    suffix: String = "",
    displayable: () -> Boolean = { true }
): IntegerValue = register(IntegerValue(name, value, range.first, range.last).displayable(displayable)) as IntegerValue

fun Configurable.choices(
    name: String,
    values: Array<String>,
    value: String,
    displayable: () -> Boolean = { true }
): ListValue = register(ListValue(name, values, value).displayable(displayable)) as ListValue

fun Configurable.text(
    name: String,
    value: String = "",
    displayable: () -> Boolean = { true }
): TextValue = register(TextValue(name, value).displayable(displayable)) as TextValue

fun Configurable.list(
    name: String,
    values: Array<String>,
    value: String,
    displayable: () -> Boolean = { true }
): ListValue = choices(name, values, value, displayable)


fun Configurable.block(
    name: String,
    value: Int,
    displayable: () -> Boolean = { true }
): BlockValue = register(BlockValue(name, value).displayable(displayable)) as BlockValue

fun Configurable.font(
    name: String,
    value: FontRenderer,
    displayable: () -> Boolean = { true }
): FontValue = register(FontValue(name, value).displayable(displayable)) as FontValue


fun Configurable.intRange(
    name: String,
    value: IntRange,
    range: IntRange = Int.MIN_VALUE..Int.MAX_VALUE,
    suffix: String = "",
    displayable: () -> Boolean = { true }
): IntegerRangeValue = register(
    IntegerRangeValue(name, value, range).displayable(displayable)
) as IntegerRangeValue

fun Configurable.floatRange(
    name: String,
    value: ClosedFloatingPointRange<Float>,
    range: ClosedFloatingPointRange<Float> = Float.MIN_VALUE..Float.MAX_VALUE,
    suffix: String = "",
    displayable: () -> Boolean = { true }
): FloatRangeValue = register(
    FloatRangeValue(name, value, range).displayable(displayable)
) as FloatRangeValue

/**
 * Color value created from a [java.awt.Color].
 */
fun Configurable.color(
    name: String,
    value: Color,
    rainbow: Boolean = false,
    displayable: () -> Boolean = { true }
): ColorValue = register(ColorValue(name, value.rgb).displayable(displayable)) as ColorValue

/**
 * Color value created from an RGB [Int].
 */
fun Configurable.color(
    name: String,
    value: Int,
    rainbow: Boolean = false,
    displayable: () -> Boolean = { true }
): ColorValue = register(ColorValue(name, value).displayable(displayable)) as ColorValue
