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
 * The original Aerolite palette: neutral graphite body with a lighter grey header.
 */
@SideOnly(Side.CLIENT)
object AeroliteOldStyle : PaletteStyle() {
    override val panelBody = Color(32, 32, 34, 244).rgb
    override val popupBody = Color(32, 32, 34, 244).rgb
    override val panelSeparator = Color(78, 78, 84).rgb
    override val sliderTrack = Color(255, 255, 255, 36).rgb

    override fun headerColor(panel: Panel): Int = Color(52, 52, 56, 248).rgb

    override fun panelTextColor(): Int = Color(200, 200, 205).rgb
}
