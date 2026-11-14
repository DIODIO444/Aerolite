/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MovementInputEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity

/**
 * "Stuck in air" — freezes the player for [pulse] ticks, releases for one tick, and repeats.
 *
 * While frozen the motion is zeroed every tick and the movement input is cleared. On Hypixel the
 * plain position packet is swapped for a look packet, which the server accepts without moving the
 * player. A knockback or a position setback releases the freeze early and schedules a short delay
 * before the next pulse, so the client does not fight the server.
 */
object Stuck : Module("Stuck", Category.MOVEMENT) {

    private val pulse by int("Pulse", 65, 2..85)

    var frozen = false
        private set

    private var savedX = 0.0
    private var savedY = 0.0
    private var savedZ = 0.0
    private var frozenAt = 0
    private var delay = 0

    override fun onEnable() {
        delay = 2
    }

    override fun onDisable() {
        freeze(false)
        delay = 0
    }

    val onPreUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (delay > 0) {
            delay--
            if (delay == 0) {
                freeze(true)
            }
        }

        if (!frozen) {
            return@handler
        }

        player.motionX = 0.0
        player.motionY = 0.0
        player.motionZ = 0.0

        if (pulse > 2 && player.ticksExisted - frozenAt >= pulse) {
            freeze(false)
            delay = 1
        }
    }

    val onPostPlayerInput = handler<MovementInputEvent> {
        if (frozen || delay == 1) {
            it.originalInput.moveStrafe = 0F
            it.originalInput.moveForward = 0F
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        if (event.eventType == EventState.SEND) {
            if (!frozen) {
                return@handler
            }

            val packet = event.packet
            // C05 is a subclass of C03; only the plain position packets need replacing.
            if (packet !is C03PacketPlayer || packet is C03PacketPlayer.C05PacketPlayerLook) {
                return@handler
            }

            if (!isHypixel()) {
                return@handler
            }

            PacketUtils.sendPacket(
                C03PacketPlayer.C05PacketPlayerLook(player.rotationYaw, player.rotationPitch, player.onGround),
                false
            )
            event.cancelEvent()
            return@handler
        }

        when (val packet = event.packet) {
            is S12PacketEntityVelocity -> {
                if (packet.entityID == player.entityId && frozen) {
                    freeze(false)
                    delay = 4
                }
            }

            is S08PacketPlayerPosLook -> {
                if (frozen) {
                    freeze(false)
                    delay = 3
                }
            }
        }
    }

    private fun isHypixel(): Boolean =
        mc.currentServerData?.serverIP?.contains("hypixel", ignoreCase = true) == true

    fun freeze(state: Boolean) {
        val player = mc.thePlayer ?: return

        if (state) {
            savedX = player.motionX
            savedY = player.motionY
            savedZ = player.motionZ
            frozenAt = player.ticksExisted
        } else {
            player.motionX = savedX
            player.motionY = savedY
            player.motionZ = savedZ
        }

        frozen = state
    }
}
