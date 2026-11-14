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
 * Dark teal glass with a saturated cyan header, matching the Tenacity ClickGUI look.
 */
@SideOnly(Side.CLIENT)
object TenacityStyle : PaletteStyle() {
    override val panelBody = Color(10, 24, 26, 244).rgb
    override val popupBody = Color(10, 24, 26, 244).rgb
    override val panelSeparator = Color(40, 110, 116).rgb
    override val sliderTrack = Color(90, 230, 240, 42).rgb

    override fun headerColor(panel: Panel): Int = Color(18, 64, 68, 248).rgb

    override fun panelTextColor(): Int = Color(90, 230, 240).rgb
}
