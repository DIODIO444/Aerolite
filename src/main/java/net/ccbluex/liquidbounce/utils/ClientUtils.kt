/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils

/**
 * Legacy facade for [net.ccbluex.liquidbounce.utils.client.ClientUtils].
 *
 * The ported Aerolite code (the `value` package and the HUD subsystem) imports the short
 * `utils.ClientUtils` path, so this object forwards everything to the real implementation.
 */
object ClientUtils {

    fun logInfo(message: String) = net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER.info(message)

    fun logWarn(message: String) = net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER.warn(message)

    fun logError(message: String, throwable: Throwable? = null) {
        if (throwable == null) {
            net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER.error(message)
        } else {
            net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER.error(message, throwable)
        }
    }

    @JvmStatic
    fun displayAlert(message: String) = net.ccbluex.liquidbounce.utils.client.ClientUtils.displayChatMessage(message)

    val LOGGER get() = net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
}
