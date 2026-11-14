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
 * Near-black body with an acid green header, matching the Zeroday ClickGUI look.
 */
@SideOnly(Side.CLIENT)
object ZerodayStyle : PaletteStyle() {
    override val panelBody = Color(8, 14, 10, 246).rgb
    override val popupBody = Color(8, 14, 10, 246).rgb
    override val panelSeparator = Color(46, 118, 58).rgb
    override val sliderTrack = Color(120, 255, 140, 42).rgb

    override fun headerColor(panel: Panel): Int = Color(24, 72, 36, 248).rgb

    override fun panelTextColor(): Int = Color(120, 255, 140).rgb
}
