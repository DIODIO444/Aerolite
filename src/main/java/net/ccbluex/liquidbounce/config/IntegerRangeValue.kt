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

class IntegerRangeValue(
    name: String,
    value: IntRange,
    val range: IntRange = Int.MIN_VALUE..Int.MAX_VALUE
) : Value<IntRange>(name, value) {
    override fun describe(text: String): IntegerRangeValue = apply { description = text }

    val start: Int
        get() = value.first
    val endInclusive: Int
        get() = value.last
    val min: Int
        get() = range.first
    val max: Int
        get() = range.last

    /** FDPClient-compatible aliases for the range bounds. */
    val minimum: Int
        get() = range.first
    val maximum: Int
        get() = range.last

    override fun toJson() = JsonPrimitive("${value.first},${value.last}")

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            val parts = element.asString.split(",")
            if (parts.size == 2) {
                value = parts[0].toInt()..parts[1].toInt()
            }
        }
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): IntRange = value
}
