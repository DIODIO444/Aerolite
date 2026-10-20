/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce

/**
 * Compatibility adapter exposing the [LiquidBounce] client instance under the FDPClient name used by
 * the ported modules.
 */
object FDPClient {
    const val CLIENT_NAME = "Aerolite"

    val hud get() = LiquidBounce.hud

    val moduleManager get() = LiquidBounce.moduleManager
}
