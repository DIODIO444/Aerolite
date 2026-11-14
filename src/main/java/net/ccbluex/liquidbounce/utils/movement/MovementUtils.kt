/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.*
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.Vec3
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object MovementUtils : MinecraftInstance, Listenable {

    /** Blocks per second of the local player's horizontal motion. */
    val bps: Double
        get() {
            val player = mc.thePlayer ?: return 0.0
            return Math.hypot(player.posX - player.prevPosX, player.posZ - player.prevPosZ) * 20.0
        }

    fun resetMotion(y: Boolean) {
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
        if (y) mc.thePlayer.motionY = 0.0
    }

    var affectSprintOnAttack: Boolean? = null

    var speed
        get() = mc.thePlayer?.run { sqrt(motionX * motionX + motionZ * motionZ).toFloat() } ?: .0f
        set(value) {
            strafe(value)
        }

    /** Whether the local player is pressing a movement key. */
    fun isMoving(): Boolean {
        val player = mc.thePlayer ?: return false
        return player.movementInput.moveForward != 0F || player.movementInput.moveStrafe != 0F
    }

    val hasMotion
        get() = mc.thePlayer?.run { motionX != .0 || motionY != .0 || motionZ != .0 } == true

    var airTicks = 0
    var groundTicks = 0

    @JvmOverloads
    fun strafe(
        speed: Float = MovementUtils.speed, stopWhenNoInput: Boolean = false, moveEvent: MoveEvent? = null,
        strength: Double = 1.0,
    ) =
        mc.thePlayer?.run {
            if (!mc.thePlayer.isMoving) {
                if (stopWhenNoInput) {
                    moveEvent?.zeroXZ()
                    stopXZ()
                }

                return@run
            }

            val prevX = motionX * (1.0 - strength)
            val prevZ = motionZ * (1.0 - strength)
            val useSpeed = speed * strength

            val yaw = direction
            val x = (-sin(yaw) * useSpeed) + prevX
            val z = (cos(yaw) * useSpeed) + prevZ

            if (moveEvent != null) {
                moveEvent.x = x
                moveEvent.z = z
            }

            motionX = x
            motionZ = z
        }

    fun Vec3.strafe(
        yaw: Float = direction.toDegreesF(), speed: Double = sqrt(xCoord * xCoord + zCoord * zCoord),
        strength: Double = 1.0,
        moveCheck: Boolean = false,
    ): Vec3 {
        if (moveCheck) {
            xCoord = 0.0
            zCoord = 0.0
            return this
        }

        val prevX = xCoord * (1.0 - strength)
        val prevZ = zCoord * (1.0 - strength)
        val useSpeed = speed * strength

        val angle = Math.toRadians(yaw.toDouble())
        xCoord = (-sin(angle) * useSpeed) + prevX
        zCoord = (cos(angle) * useSpeed) + prevZ
        return this
    }

    fun forward(distance: Double) =
        mc.thePlayer?.run {
            val yaw = rotationYaw.toRadiansD()
            setPosition(posX - sin(yaw) * distance, posY, posZ + cos(yaw) * distance)
        }

    val direction
        get() = mc.thePlayer?.run {
            var yaw = rotationYaw
            var forward = 1f

            if (movementInput.moveForward < 0f) {
                yaw += 180f
                forward = -0.5f
            } else if (movementInput.moveForward > 0f)
                forward = 0.5f

            if (movementInput.moveStrafe < 0f) yaw += 90f * forward
            else if (movementInput.moveStrafe > 0f) yaw -= 90f * forward

            yaw.toRadiansD()
        } ?: 0.0

    fun isOnGround(height: Double) =
        mc.theWorld != null && mc.thePlayer != null &&
            mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.entityBoundingBox.offset(Vec3_ZERO.withY(-height))
            ).isNotEmpty()

    var serverOnGround = false

    var serverX = .0
    var serverY = .0
    var serverZ = .0

    val onPacket = handler<PacketEvent> { event ->
        if (event.isCancelled)
            return@handler

        val packet = event.packet

        if (packet is C03PacketPlayer) {
            serverOnGround = packet.onGround

            if (packet.isMoving) {
                serverX = packet.x
                serverY = packet.y
                serverZ = packet.z
            }
        }
    }

    /* ------------------------------------------------------------------ *
     *  Helpers used by the ported TargetStrafe / NewTargetStrafe modules.  *
     * ------------------------------------------------------------------ */

    fun distance(srcX: Double, srcY: Double, srcZ: Double, dstX: Double, dstY: Double, dstZ: Double): Double {
        val xDist = dstX - srcX
        val yDist = dstY - srcY
        val zDist = dstZ - srcZ
        return sqrt(xDist * xDist + yDist * yDist + zDist * zDist)
    }

    fun distance(srcX: Double, srcZ: Double, dstX: Double, dstZ: Double): Double {
        val xDist = dstX - srcX
        val zDist = dstZ - srcZ
        return sqrt(xDist * xDist + zDist * zDist)
    }

    /** Horizontal speed carried by a [MoveEvent]. */
    fun getSpeed2(x: Double, z: Double): Double = sqrt(x * x + z * z)

    fun getMovementDirection(forward: Float, strafing: Float, yaw: Float): Float {
        var yaw = yaw
        if (forward == 0.0f && strafing == 0.0f) return yaw
        val reversed = forward < 0.0f
        val strafingYaw = 90.0f * if (forward > 0.0f) 0.5f else if (reversed) -0.5f else 1.0f
        if (reversed) yaw += 180.0f
        if (strafing > 0.0f) yaw -= strafingYaw else if (strafing < 0.0f) yaw += strafingYaw
        return yaw
    }

    fun setSpeed(moveEvent: MoveEvent, speed: Double, forward: Float, strafing: Float, yaw: Float) {
        if (forward == 0.0f && strafing == 0.0f) return
        val movementDirectionRads = Math.toRadians(getMovementDirection(forward, strafing, yaw).toDouble())
        moveEvent.x = -sin(movementDirectionRads) * speed
        moveEvent.z = cos(movementDirectionRads) * speed
    }

    /**
     * NewTargetStrafe's variant: the strafe direction is expressed as a side offset instead of a
     * forward/strafe pair.
     */
    fun setSpeed(moveEvent: MoveEvent, speed: Double, yaw: Float, side: Double, forward: Double) {
        val rad = Math.toRadians((yaw + side * 90.0).toDouble())
        val f = forward.coerceIn(-1.0, 1.0)
        moveEvent.x = (-sin(rad) * speed * f) + (cos(rad) * speed * side)
        moveEvent.z = (cos(rad) * speed * f) + (sin(rad) * speed * side)
    }

    /** Circles [target] at [radius], keeping the player's current speed. */
    fun doTargetStrafe(target: EntityLivingBase, direction: Float, radius: Float, moveEvent: MoveEvent, mode: Int = 0) {
        val player = mc.thePlayer ?: return
        if (!isMoving()) return

        val speed = getSpeed2(moveEvent.x, moveEvent.z)
        if (speed <= 0.0001) return

        val dir = when {
            direction > 0.001f -> 1.0
            direction < -0.001f -> -1.0
            else -> 0.0
        }

        val distance = when (mode) {
            1 -> player.getDistanceToEntity(target).toDouble()
            else -> sqrt((player.posX - target.posX).pow(2) + (player.posZ - target.posZ).pow(2))
        }

        val forward = when {
            distance < radius - speed -> -1.0
            distance > radius + speed -> 1.0
            else -> (distance - radius) / speed
        }

        var strafe = if (distance in (radius - speed * 2)..(radius + speed * 2)) 1.0 else 0.0
        strafe *= dir

        val norm = sqrt(forward.pow(2) + strafe.pow(2))
        if (norm <= 0.0) return
        val f = forward / norm
        val s = strafe / norm

        var angle = Math.toDegrees(asin(s))
        if (angle > 0) {
            if (f < 0) angle = 180 - angle
        } else {
            if (f < 0) angle = -180 - angle
        }

        val baseYaw = net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getRotationsEntity(target).yaw + angle
        val rad = Math.toRadians(baseYaw)

        moveEvent.x = -sin(rad) * speed
        moveEvent.z = cos(rad) * speed

        player.motionX = moveEvent.x
        player.motionZ = moveEvent.z
    }

}
