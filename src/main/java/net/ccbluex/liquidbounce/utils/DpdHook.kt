/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler

/**
 * Feeds [DpdUtils] with fresh heap statistics once per tick.
 */
object DpdHook : Listenable {

    @Suppress("unused")
    val onUpdate = handler<UpdateEvent>(always = true) { DpdUtils.updateMemory() }
}
