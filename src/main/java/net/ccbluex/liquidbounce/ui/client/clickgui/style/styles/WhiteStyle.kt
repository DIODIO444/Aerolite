/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles

import net.ccbluex.liquidbounce.ui.client.clickgui.Panel
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.awt.Color

/**
 * White look: the light theme from the Aerolite ClickGUI — slate panels with a blue accent.
 */
@SideOnly(Side.CLIENT)
object WhiteStyle : PaletteStyle() {
    private val accent = Color(66, 134, 245)

    override val panelRadius = 8F
    override val panelBody = Color(49, 52, 57, 248).rgb
    override val popupBody = Color(64, 68, 75, 250).rgb
    override val panelSeparator = Color(90, 95, 104).rgb
    override val scrollTrack = Color(255, 255, 255, 28).rgb
    override val sliderTrack = Color(255, 255, 255, 46).rgb
    override val shadowAlpha = 0.5F

    override fun headerColor(panel: Panel): Int = Color(64, 68, 75).rgb

    override fun panelTextColor(): Int = Color.WHITE.rgb

    /** Kept public so callers can match the accent when drawing custom chrome. */
    fun accentColor(): Int = accent.rgb
}
