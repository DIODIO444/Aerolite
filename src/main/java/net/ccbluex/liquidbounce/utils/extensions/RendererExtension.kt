/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.minecraft.client.gui.FontRenderer

/**
 * Centered text drawing used by the HUD elements.
 */
fun FontRenderer.drawCenteredString(s: String, x: Float, y: Float, color: Int, shadow: Boolean) =
    drawString(s, x - getStringWidth(s) / 2F, y, color, shadow)

fun FontRenderer.drawCenteredString(s: String, x: Float, y: Float, color: Int) =
    drawString(s, x - getStringWidth(s) / 2F, y, color, false)
