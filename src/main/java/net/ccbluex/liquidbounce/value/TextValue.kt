package net.ccbluex.liquidbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import kotlin.reflect.KProperty

/**
 * Text value represents a value with a string
 */
open class TextValue(name: String, value: String) : Value<String>(name, value) {
    override fun describe(text: String): TextValue = apply { description = text }
    override fun toJson() = JsonPrimitive(value)

    var TextHovered = false

    fun append(o: Any): TextValue {
        set(get() + o)
        return this
    }
    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asString
        }
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): String = value
}