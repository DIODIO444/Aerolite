/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.value.Value

/**
 * A value that lets the user pick multiple options out of a fixed [options] set (FDPClient port).
 */
open class MultiSelectValue(
    name: String,
    val options: Array<String>,
    value: Set<String> = options.toSet()
) : Value<Set<String>>(name, value) {

    override fun describe(text: String): MultiSelectValue = apply { description = text }

    fun isSelected(tag: String): Boolean = value.contains(tag)

    fun select(tag: String) {
        value = value + tag
    }

    fun deselect(tag: String) {
        value = value - tag
    }

    override fun toJson(): JsonElement {
        val array = JsonArray()
        value.forEach { array.add(JsonPrimitive(it)) }
        return array
    }

    override fun fromJson(element: JsonElement) {
        if (element is JsonArray) {
            value = element.mapNotNull { it.asString }.toSet()
        }
    }
}
