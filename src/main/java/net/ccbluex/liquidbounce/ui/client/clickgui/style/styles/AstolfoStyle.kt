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
 * Astolfo look: the Aerolite layout with the softer pastel category palette.
 */
@SideOnly(Side.CLIENT)
object AstolfoStyle : PaletteStyle() {
    private val categoryColors = linkedMapOf(
        "combat" to Color(255, 128, 166),
        "player" to Color(186, 148, 255),
        "movement" to Color(140, 235, 190),
        "render" to Color(148, 202, 255),
        "world" to Color(255, 232, 150),
        "misc" to Color(255, 186, 128),
        "exploit" to Color(178, 178, 190),
        "client" to Color(148, 202, 255),
        "fun" to Color(255, 160, 200)
    )

    override val panelBody = Color(17, 17, 17, 242).rgb
    override val popupBody = Color(23, 23, 23, 246).rgb
    override val panelSeparator = Color(38, 38, 38).rgb

    private fun categoryColor(panel: Panel): Color =
        categoryColors.entries.firstOrNull { panel.name.lowercase().contains(it.key) }?.value ?: Color(186, 148, 255)

    override fun headerColor(panel: Panel): Int = categoryColor(panel).rgb
}
