/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import kotlin.reflect.KProperty
import net.ccbluex.liquidbounce.value.Value

class FloatRangeValue(
    name: String,
    value: ClosedFloatingPointRange<Float>,
    val range: ClosedFloatingPointRange<Float> = Float.MIN_VALUE..Float.MAX_VALUE
) : Value<ClosedFloatingPointRange<Float>>(name, value) {
    override fun describe(text: String): FloatRangeValue = apply { description = text }

    val start: Float
        get() = value.start
    val endInclusive: Float
        get() = value.endInclusive

    /** FDPClient-compatible aliases for the range bounds. */
    val minimum: Float
        get() = range.start
    val maximum: Float
        get() = range.endInclusive
    val min: Float
        get() = range.start
    val max: Float
        get() = range.endInclusive

    override fun toJson() = JsonPrimitive("${value.start},${value.endInclusive}")

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            val parts = element.asString.split(",")
            if (parts.size == 2) {
                value = parts[0].toFloat()..parts[1].toFloat()
            }
        }
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): ClosedFloatingPointRange<Float> = value
}
