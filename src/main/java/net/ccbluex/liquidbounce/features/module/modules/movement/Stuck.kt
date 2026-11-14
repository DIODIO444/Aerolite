/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.MovementInputEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.init.Items
import net.minecraft.item.ItemBow
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import java.util.ArrayDeque

/**
 * "Stuck in air" — freezes the player in place by swallowing outgoing movement packets and
 * replaying the captured use-item packet once the position is locked.
 *
 * Ported from LyasimNextgen's `today.lyasim.module.impl.movement.Stuck`, which is written against
 * 1.20 mappings, and re-implemented on 1.8.9 packets:
 *
 * | Lyasim (1.20)                         | here (1.8.9)                              |
 * |---------------------------------------|-------------------------------------------|
 * | PlayerMoveC2SPacket                   | C03PacketPlayer                           |
 * | CommonPongC2SPacket                   | C00PacketKeepAlive                        |
 * | PlayerInteractItemC2SPacket           | C08PacketPlayerBlockPlacement             |
 * | PlayerActionC2SPacket (release item)  | C07PacketPlayerDigging RELEASE_USE_ITEM   |
 * | PlayerPositionLookS2CPacket           | S08PacketPlayerPosLook                    |
 * | ClientCommandC2SPacket (fall flying)  | dropped — elytra does not exist in 1.8.9  |
 */
object Stuck : Module("Stuck", Category.MOVEMENT) {

    private val mode by choices("Mode", arrayOf("Delay", "Packet"), "Delay")

    /** 0 = idle, 1 = captured a use packet, 2 = released, 3 = finished. */
    private var stuckState = 0
    private var capturedPacket: Packet<*>? = null
    private var savedYaw = 0F
    private var savedPitch = 0F
    private var pendingDisable = false
    private val pongQueue = ArrayDeque<Packet<*>>()

    override fun onEnable() {
        stuckState = 0
        capturedPacket = null
        savedYaw = mc.thePlayer?.rotationYaw ?: 0F
        savedPitch = mc.thePlayer?.rotationPitch ?: 0F
        pendingDisable = false
        pongQueue.clear()
    }

    override fun onDisable() {
        flushPongs()
        pongQueue.clear()
        capturedPacket = null
        stuckState = 0
        pendingDisable = false
    }

    /** Packet mode: Scaffold keeps re-placing and fights the frozen position. */
    val onTick = handler<GameTickEvent> {
        if (mode != "Packet") return@handler

        if (Scaffold.handleEvents()) {
            Scaffold.state = false
            return@handler
        }
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (stuckState == 1) {
            stuckState = 2

            val currentYaw = player.rotationYaw
            val currentPitch = player.rotationPitch

            if (shouldSendCapturedPacket() && (savedYaw != currentYaw || savedPitch != currentPitch)) {
                sendPacket(
                    C03PacketPlayer.C05PacketPlayerLook(currentYaw, currentPitch, player.onGround),
                    false
                )
                flushPongs()
                savedYaw = currentYaw
                savedPitch = currentPitch
            }

            capturedPacket?.let { sendPacket(it, false) }
            capturedPacket = null
        } else if (mode == "Packet" && player.ticksExisted % 10 == 0) {
            flushPongs()
        }

        if (pendingDisable) {
            if (mode == "Delay") {
                sendPacket(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        player.posX + 1337.0, player.posY, player.posZ + 1337.0, player.onGround
                    ),
                    false
                )
            }

            flushPongs()
            stuckState = 3
            pendingDisable = false
        }
    }

    val onMovementInput = handler<MovementInputEvent> { event ->
        event.originalInput.moveForward = 0F
        event.originalInput.moveStrafe = 0F
    }

    val onPacket = handler<PacketEvent> { event ->
        if (mc.thePlayer == null) return@handler

        when (val packet = event.packet) {
            is C03PacketPlayer -> {
                // Freeze: no position update may reach the server.
                event.cancelEvent()
            }

            is C00PacketKeepAlive -> {
                pongQueue.addLast(packet)
                event.cancelEvent()
            }

            is C08PacketPlayerBlockPlacement -> {
                capturedPacket = packet
                stuckState = 1
                event.cancelEvent()
            }

            is C07PacketPlayerDigging -> {
                if (packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                    capturedPacket = packet
                    stuckState = 1
                    event.cancelEvent()
                }
            }

            is S08PacketPlayerPosLook -> {
                if (mode == "Delay") {
                    flushPongs()
                    stuckState = 3
                    state = false
                }
            }
        }
    }

    val onWorld = handler<WorldEvent> { event ->
        if (event.worldClient == null) return@handler

        stuckState = 3
        capturedPacket = null
        state = false
    }

    private fun flushPongs() {
        while (pongQueue.isNotEmpty()) {
            sendPacket(pongQueue.pollFirst(), false)
        }
    }

    /** Same rule as Lyasim: never release the use packet while eating or drawing a bow. */
    private fun shouldSendCapturedPacket(): Boolean {
        val packet = capturedPacket ?: return false
        val player = mc.thePlayer ?: return false

        return when (packet) {
            is C08PacketPlayerBlockPlacement -> {
                val item = player.heldItem?.item
                // beetroot soup does not exist in 1.8.9, so only the two stews are checked here.
                item != null && item !is ItemBow &&
                        item != Items.mushroom_stew &&
                        item != Items.rabbit_stew
            }

            is C07PacketPlayerDigging ->
                packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM &&
                        player.heldItem?.item is ItemBow

            else -> false
        }
    }
}
