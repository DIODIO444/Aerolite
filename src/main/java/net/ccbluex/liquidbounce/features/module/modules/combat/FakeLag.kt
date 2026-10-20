/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.*

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.int
import net.minecraft.network.Packet

/**
 * Delays outgoing packets for a configurable window, then flushes them all at once.
 * Exposes [isLagging] which [net.ccbluex.liquidbounce.utils.rotation.RotationUtils] reads to know
 * whether the player's real position is being held back.
 */
@ModuleInfo(name = "FakeLag", category = ModuleCategory.COMBAT)
class FakeLag : Module() {

    private val lagLength by int("LagLength", 400, 0..2000)
    private val lagDelay by int("LagDelay", 0, 0..1000)
    private val noLagBlock by boolean("NoLagBlock", false)
    private val noLagEntity by boolean("NoLagEntity", false)

    var isLagging = false
        private set

    private val lagTimer = MSTimer()
    private val queue = ArrayDeque<Packet<*>>()

    override fun onEnable() {
        isLagging = false
        queue.clear()
        lagTimer.reset()
    }

    override fun onDisable() {
        releaseAll()
        isLagging = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!isLagging && lagTimer.hasTimePassed(lagDelay.toLong())) {
            isLagging = true
            lagTimer.reset()
        }

        if (isLagging && lagTimer.hasTimePassed(lagLength.toLong())) {
            releaseAll()
            isLagging = false
            lagTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.type != PacketEvent.Type.SEND) return

        // Keep certain packets flowing so the world does not appear frozen.
        if (noLagBlock && event.packet is net.minecraft.network.play.client.C07PacketPlayerDigging) return
        if (noLagEntity && event.packet is net.minecraft.network.play.client.C0APacketAnimation) return

        if (!isLagging) return

        event.cancelEvent()
        queue.addLast(event.packet)
    }

    private fun releaseAll() {
        while (queue.isNotEmpty()) {
            sendPacket(queue.removeFirst())
        }
    }
}
