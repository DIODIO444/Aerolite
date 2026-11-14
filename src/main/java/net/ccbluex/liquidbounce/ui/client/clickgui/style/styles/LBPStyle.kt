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
 * LiquidBounce+ look: deep purple body with a violet header.
 */
@SideOnly(Side.CLIENT)
object LBPStyle : PaletteStyle() {
    override val panelBody = Color(18, 12, 28, 244).rgb
    override val popupBody = Color(18, 12, 28, 244).rgb
    override val panelSeparator = Color(96, 62, 150).rgb
    override val sliderTrack = Color(180, 140, 255, 42).rgb

    override fun headerColor(panel: Panel): Int = Color(58, 32, 96, 248).rgb

    override fun panelTextColor(): Int = Color(180, 140, 255).rgb
}
