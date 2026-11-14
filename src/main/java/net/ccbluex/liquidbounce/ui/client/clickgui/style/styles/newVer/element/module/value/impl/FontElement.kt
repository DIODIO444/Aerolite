package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newVer.element.module.value.impl

import net.ccbluex.liquidbounce.config.FontValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newVer.ColorManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newVer.element.module.value.ValueElement
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager

import java.awt.Color

/**
 * Font picker for the newVer style.
 *
 * <p>The style previously had no `FontValue` branch at all, so a module's "Font" setting simply
 * never showed up. Clicking the pill cycles through every registered font — the same interaction
 * the LiquidBounce style uses.</p>
 */
class FontElement(private val fontValue: FontValue) : ValueElement<FontRenderer>(fontValue) {

    companion object {
        /** Width of the pill that shows the current font name. */
        private const val PILL_WIDTH = 78F

        /** Room for the text inside the pill (pill width minus padding). */
        private const val PILL_TEXT_WIDTH = 66
    }

    private val availableFonts: List<FontRenderer>
        get() = Fonts.fonts

    private fun displayName(font: FontRenderer): String {
        if (font === Fonts.minecraftFont)
            return "Minecraft"

        val info = Fonts.getFontDetails(font) ?: return "Unknown"
        return if (info.size > 0) "${info.name} ${info.size}" else info.name
    }

    override fun drawElement(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, bgColor: Color, accentColor: Color): Float {
        val current = value.get()
        // Long family names would spill out of the module column, so trim to the pill.
        val label = Fonts.font40.trimStringToWidth(displayName(current), PILL_TEXT_WIDTH)

        Fonts.font40.drawString(value.name, x + 10F, y + 10F - Fonts.font40.FONT_HEIGHT / 2F, -1)

        RenderUtils.originalRoundedRect(
            x + width - 18F - PILL_WIDTH, y + 2F,
            x + width - 10F, y + 18F,
            4F, ColorManager.button.rgb
        )
        GlStateManager.resetColor()

        Fonts.font40.drawString(
            label,
            x + width - 14F - PILL_WIDTH, y + 10F - Fonts.font40.FONT_HEIGHT / 2F,
            -1
        )

        valueHeight = 20F
        return valueHeight
    }

    override fun onClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {
        if (!isDisplayable())
            return

        if (!ClientUtils.mouseWithinBounds(mouseX, mouseY, x, y + 2F, x + width, y + 18F))
            return

        val fonts = availableFonts
        if (fonts.isEmpty())
            return

        val current = value.get()
        val index = fonts.indexOfFirst { it === current }
        value.set(fonts[(index + 1) % fonts.size])
    }
}
