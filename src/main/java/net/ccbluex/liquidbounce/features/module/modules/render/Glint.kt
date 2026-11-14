package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.config.displayable
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import java.awt.Color


object Glint : Module("Glint", Category.RENDER) {

    private val modeValue = choices("Mode", arrayOf("Rainbow", "AnotherRainbow", "Custom"), "Custom")
    private val redValue = int("Red", 255, 0..255).displayable { modeValue.equals("Custom") }
    private val greenValue = int("Green", 0, 0..255).displayable { modeValue.equals("Custom") }
    private val blueValue = int("Blue", 0, 0..255).displayable { modeValue.equals("Custom") }

    fun getColor(): Color {
        return when (modeValue.get().lowercase()) {
            "rainbow" -> ColorUtils.rainbow()
            "anotherrainbow" -> ColorUtils.skyRainbow(10, 0.9F, 1F, 1.0)
            else -> Color(redValue.get(), greenValue.get(), blueValue.get())
        }
    }
}