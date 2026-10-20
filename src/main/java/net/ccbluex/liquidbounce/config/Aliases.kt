/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 *
 * Type aliases that expose FDPClient's `config.*` value-class names on top of Aerolite's existing
 * `value.*` implementation. The new combat modules (and the util packages copied from FDPClient) import
 * `config.IntValue`, `config.ColorValue`, `config.Value`, etc.; Aerolite keeps the real classes in the
 * `value` package (used by ~100 older modules), so we alias rather than duplicate.
 */
package net.ccbluex.liquidbounce.config

import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ColorValue as FdpColorValue
import net.ccbluex.liquidbounce.value.FloatValue as FdpFloatValue
import net.ccbluex.liquidbounce.value.IntegerValue as FdpIntegerValue
import net.ccbluex.liquidbounce.value.ListValue as FdpListValue
import net.ccbluex.liquidbounce.value.TextValue as FdpTextValue
import net.ccbluex.liquidbounce.value.Value as FdpValue

typealias Value<T> = FdpValue<T>
typealias BooleanValue = BoolValue
typealias IntValue = FdpIntegerValue
typealias FloatValue = FdpFloatValue
typealias StringValue = FdpTextValue
typealias TextValue = FdpTextValue
typealias ListValue = FdpListValue
typealias ColorValue = FdpColorValue
// NOTE: `FloatRangeValue` is a real class in this same package — no typealias needed.
// `IntRangeValue` however maps to `IntegerRangeValue` (different class name in Aerolite).
typealias IntRangeValue = IntegerRangeValue
