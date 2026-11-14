package net.ccbluex.liquidbounce.ui.client.clickgui.style

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.features.module.Module

/**
 * Flattens a module's settings for the ported Aerolite styles.
 *
 * Nested [Configurable] groups are included so the style can draw their headers, followed by the
 * values they contain.
 */
object ClickGuiValueHelper {

    @JvmStatic
    fun values(module: Module): List<Value<*>> {
        val result = ArrayList<Value<*>>()

        fun walk(list: List<Value<*>>) {
            for (value in list) {
                result += value

                if (value is Configurable) {
                    walk(value.values)
                }
            }
        }

        walk(module.values)
        return result
    }
}
