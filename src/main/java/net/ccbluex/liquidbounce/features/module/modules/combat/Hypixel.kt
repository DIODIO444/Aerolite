/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.rotation
import net.ccbluex.liquidbounce.utils.rotation.RaycastUtils.runWithModifiedRaycastResult
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.hypot
import kotlin.math.roundToInt

object Hypixel : MinecraftInstance {
    private var reduceTicks = 0

    var hasPendingJumpReset = false
        private set

    private var slot = false
    private var attack = false
    private var swing = false
    private var block = false
    private var inventory = false
    private var dig = false

    val isReducing
        get() = reduceTicks > 0

    fun onPacket(event: PacketEvent) {
        if (event.eventType == EventState.SEND) {
            val packet = event.packet

            if (packet is C09PacketHeldItemChange) {
                slot = true
            } else if (packet is C0APacketAnimation) {
                swing = true
            } else if (packet is C02PacketUseEntity) {
                if (packet.action == ATTACK) attack = true
            } else if (packet is C08PacketPlayerBlockPlacement) {
                block = true
            } else if (packet is C07PacketPlayerDigging) {
                block = true
                dig = true
            } else if (packet is C0DPacketCloseWindow || packet is C0EPacketClickWindow) {
                inventory = true
            } else if (packet is C16PacketClientStatus) {
                if (packet.status == OPEN_INVENTORY_ACHIEVEMENT) inventory = true
            } else if (packet is C03PacketPlayer) {
                resetBadPackets()
            }
        }
    }

    private fun badPackets() = slot || attack || swing || block || inventory || dig

    private fun resetBadPackets() {
        slot = false
        attack = false
        swing = false
        block = false
        inventory = false
        dig = false
    }

    fun onVelocity(event: PacketEvent, player: EntityPlayerSP) {
        val packet = event.packet as? S12PacketEntityVelocity ?: return

        if (packet.entityID == player.entityId) {
            if (Velocity.attackReduce) {
                hasPendingJumpReset = Velocity.attackReduceJumpReset

                val knockback = hypot(packet.motionX.toDouble(), packet.motionZ.toDouble())

                reduceTicks = if (!Velocity.attackReduceTickExact) {
                    (knockback * 6.43153527E-4 + 2.9419087136).roundToInt().coerceIn(1, 10)
                } else when {
                    knockback <= 500.0 -> Velocity.attackReduceTick500
                    knockback <= 1000.0 -> Velocity.attackReduceTick1000
                    knockback <= 2000.0 -> Velocity.attackReduceTick2000
                    knockback <= 3000.0 -> Velocity.attackReduceTick3000
                    knockback <= 4000.0 -> Velocity.attackReduceTick4000
                    knockback <= 5000.0 -> Velocity.attackReduceTick5000
                    knockback <= 6000.0 -> Velocity.attackReduceTick6000
                    knockback <= 7000.0 -> Velocity.attackReduceTick7000
                    knockback <= 8000.0 -> Velocity.attackReduceTick8000
                    knockback <= 9000.0 -> Velocity.attackReduceTick9000
                    else -> Velocity.attackReduceTick10000
                }
            }
        }
    }

    fun onTick(player: EntityPlayerSP) {
        if (reduceTicks > 0) {
            reduceTicks--

            if (reduceTicks <= 0) {
                resetAfterWindow(player)
            } else if (!player.isInWeb && player.isSprinting && player.isMoving && !badPackets()) {
                val target = if (Velocity.attackReduce) findReduceTarget(player) else null
                if (target != null) {
                    attackAndReduce(player, target)
                }
            }
        }
    }

    private fun findReduceTarget(player: EntityPlayerSP): EntityLivingBase? {
        var target: EntityLivingBase? = null

        runWithModifiedRaycastResult(
            currentRotation ?: player.rotation,
            Velocity.attackReduceRange.toDouble(),
            0.0
        ) {
            val entity = mc.objectMouseOver?.entityHit
            if (entity is EntityLivingBase && entity != player && isSelected(entity, true)
                && isWithinReduceRange(player, entity)
            ) {
                target = entity
            }
        }

        return target
    }

    private fun isWithinReduceRange(player: EntityPlayerSP, target: EntityLivingBase): Boolean {
        val range = Velocity.attackReduceRange
        return player.getDistanceToEntityBox(target) <= range && player.getDistanceToEntity(target) <= range
    }

    private fun attackAndReduce(player: EntityPlayerSP, target: EntityLivingBase) {
        EventManager.call(AttackEvent(target))
        sendPacket(C0APacketAnimation())
        sendPacket(C02PacketUseEntity(target, ATTACK))

        player.motionX *= 0.6
        player.motionZ *= 0.6
        player.setSprinting(false)
    }

    private fun resetAfterWindow(player: EntityPlayerSP) {
        player.setSprinting(player.isMoving)
        hasPendingJumpReset = false
        resetBadPackets()
    }

    fun reset() {
        reduceTicks = 0
        hasPendingJumpReset = false
        resetBadPackets()
    }

    fun consumeJumpReset() {
        hasPendingJumpReset = false
    }
}
