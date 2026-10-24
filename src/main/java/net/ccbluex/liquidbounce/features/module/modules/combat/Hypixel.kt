package net.ccbluex.liquidbounce.features.module.modules.combat

import kotlin.math.roundToInt
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.PacketUtils
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.client.C02PacketUseEntity.Action
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState
import net.minecraft.network.play.server.S12PacketEntityVelocity

object Hypixel : MinecraftInstance {
    var reduceTicks: Int = 0
    var hasPendingJumpReset: Boolean = false
        private set
    var slot: Boolean = false
    var attack: Boolean = false
    var swing: Boolean = false
    var block: Boolean = false
    var inventory: Boolean = false
    var dig: Boolean = false

    fun onPacket(event: PacketEvent) {
        if (event.eventType === EventState.SEND) {
            val packet = event.packet
            when (packet) {
                is C09PacketHeldItemChange -> slot = true
                is C0APacketAnimation -> swing = true
                is C02PacketUseEntity -> if (packet.getAction() === Action.ATTACK) attack = true
                is C08PacketPlayerBlockPlacement -> block = true
                is C07PacketPlayerDigging -> {
                    block = true
                    dig = true
                }
                is C0DPacketCloseWindow, is C0EPacketClickWindow -> inventory = true
                is C16PacketClientStatus -> if (packet.getStatus() === EnumState.OPEN_INVENTORY_ACHIEVEMENT) inventory = true
                is C03PacketPlayer -> this.resetBadPackets()
            }
        }
    }

    fun badPackets(): Boolean {
        return slot || attack || swing || block || inventory || dig
    }

    fun resetBadPackets() {
        slot = false
        attack = false
        swing = false
        block = false
        inventory = false
        dig = false
    }

    fun onVelocity(event: PacketEvent, player: EntityPlayerSP) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity && packet.getEntityID() == player.getEntityId()) {
            if (Velocity.attackReduce) {
                hasPendingJumpReset = Velocity.attackReduceJumpReset
                val kb = Math.hypot(packet.motionX.toDouble(), packet.motionZ.toDouble())
                reduceTicks = if (!Velocity.attackReduceTickExact) {
                    (kb * 6.43153527E-4 + 2.9419087136).roundToInt().coerceIn(1, 10)
                } else {
                    when {
                        kb <= 500.0 -> Velocity.attackReduceTick500
                        kb <= 1000.0 -> Velocity.attackReduceTick1000
                        kb <= 2000.0 -> Velocity.attackReduceTick2000
                        kb <= 3000.0 -> Velocity.attackReduceTick3000
                        kb <= 4000.0 -> Velocity.attackReduceTick4000
                        kb <= 5000.0 -> Velocity.attackReduceTick5000
                        kb <= 6000.0 -> Velocity.attackReduceTick6000
                        kb <= 7000.0 -> Velocity.attackReduceTick7000
                        kb <= 8000.0 -> Velocity.attackReduceTick8000
                        kb <= 9000.0 -> Velocity.attackReduceTick9000
                        else -> Velocity.attackReduceTick10000
                    }
                }
            }
        }
    }

    fun onTick(player: EntityPlayerSP) {
        if (reduceTicks > 0) {
            reduceTicks -= 1
            if (reduceTicks <= 0) {
                this.resetAfterWindow(player)
            } else if (!player.isInWeb && player.isSprinting && player.isMoving && !this.badPackets()) {
                val target = if (Velocity.attackReduce) {
                    val ka = KillAura.target
                    if (ka != null && ka !== player && player.getDistanceToEntity(ka).toDouble() <= Velocity.attackReduceRange.toDouble()) ka else null
                } else {
                    null
                }

                if (target != null) {
                    this.attackAndReduce(player, target)
                }
            }
        }
    }

    fun attackAndReduce(player: EntityPlayerSP, target: EntityLivingBase) {
        EventManager.call(AttackEvent(target))
        PacketUtils.sendPacket(C0APacketAnimation(), triggerEvent = false)
        PacketUtils.sendPacket(C02PacketUseEntity(target, Action.ATTACK), triggerEvent = false)
        player.motionX *= 0.6
        player.motionZ *= 0.6
        player.setSprinting(false)
    }

    fun resetAfterWindow(player: EntityPlayerSP) {
        player.setSprinting(player.isMoving)
        hasPendingJumpReset = false
        this.resetBadPackets()
    }

    fun reset() {
        reduceTicks = 0
        hasPendingJumpReset = false
        this.resetBadPackets()
    }

    fun consumeJumpReset() {
        hasPendingJumpReset = false
    }
}
