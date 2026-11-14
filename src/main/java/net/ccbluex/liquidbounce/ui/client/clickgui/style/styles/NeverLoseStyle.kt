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
 * Black body with a crimson header, matching the NeverLose ClickGUI look.
 */
@SideOnly(Side.CLIENT)
object NeverLoseStyle : PaletteStyle() {
    override val panelBody = Color(10, 10, 12, 246).rgb
    override val popupBody = Color(10, 10, 12, 246).rgb
    override val panelSeparator = Color(140, 34, 42).rgb
    override val sliderTrack = Color(255, 90, 90, 42).rgb

    override fun headerColor(panel: Panel): Int = Color(96, 16, 24, 248).rgb

    override fun panelTextColor(): Int = Color(255, 90, 90).rgb
}
