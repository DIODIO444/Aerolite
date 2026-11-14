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
 * Deep navy body with an indigo header, matching the Novoline ClickGUI look.
 */
@SideOnly(Side.CLIENT)
object NovolineStyle : PaletteStyle() {
    override val panelBody = Color(12, 14, 22, 244).rgb
    override val popupBody = Color(12, 14, 22, 244).rgb
    override val panelSeparator = Color(58, 74, 130).rgb
    override val sliderTrack = Color(120, 160, 255, 40).rgb

    override fun headerColor(panel: Panel): Int = Color(28, 42, 90, 248).rgb

    override fun panelTextColor(): Int = Color(120, 160, 255).rgb
}
