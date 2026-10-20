/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs.models

/**
 * Client-wide configuration values consumed by utility code such as
 * [net.ccbluex.liquidbounce.utils.kotlin.RandomUtils] when generating throwaway alt accounts.
 */
object ClientConfiguration {
    var altsPrefix: String = "FDPClient"
    var altsLength: Int = 8
    var stylisedAlts: Boolean = false
    var unformattedAlts: Boolean = false
}
