/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.KeepSprintEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.attackEntityWithModifiedSprint
import net.minecraft.entity.Entity

object KeepSprint : Module("KeepSprint", Category.COMBAT) {
    private val mode by choices("Mode", arrayOf("Normal", "NewUniversal"), "Normal")
    private val debug by boolean("Debug", false)

    val motionAfterAttackOnGround by float("MotionAfterAttackOnGround", 0.6f, 0.0f..1f)
    val motionAfterAttackInAir by float("MotionAfterAttackInAir", 0.6f, 0.0f..1f)

    private val onlyKillAura by boolean("OnlyKillAura", true) { isNewUniversal }

    var sprintCancelled = false
        private set

    var attackPending = false
        private set

    private var pendingTicks = 0
    private var forceAttackNext = false
    private var killAuraAttack = false

    @JvmStatic
    var oldAttackInProgress = false

    val isNewUniversal
        get() = mode == "NewUniversal"

    val isVelocityReducing
        get() = Velocity.handleEvents() && Velocity.isHypixelMode() && Hypixel.isReducing

    fun shouldSkipKeepSprint() = (mc.thePlayer?.ticksExisted ?: 0) < 8

    fun stopSprinting(): Boolean {
        val player = mc.thePlayer ?: return false

        if (isVelocityReducing)
            return false

        if (attackPending) {
            player.setSprinting(false)
            return false
        }

        if (!player.isSprinting)
            return false

        player.setSprinting(false)
        mc.gameSettings.keyBindSprint.pressed = false
        sprintCancelled = true
        attackPending = true
        pendingTicks = 3
        forceAttackNext = true

        return true
    }

    fun consumeForcedAttack(): Boolean {
        if (!forceAttackNext)
            return false

        forceAttackNext = false
        return true
    }

    fun onAttackPerformed() {
        if (!attackPending)
            return

        attackPending = false
        pendingTicks = 0
        forceAttackNext = false
    }

    fun cancelPending() {
        attackPending = false
        pendingTicks = 0
        sprintCancelled = false
        forceAttackNext = false
    }

    val motionAfterAttack
        get() = if (mc.thePlayer.onGround) motionAfterAttackOnGround else motionAfterAttackInAir

    fun attackNewUniversal(entity: Entity, swing: Boolean = true): Boolean {
        val player = mc.thePlayer ?: return false

        if (!isNewUniversal || mc.playerController == null || !entity.isEntityAlive)
            return false

        if (isVelocityReducing)
            return false

        if (player.isSprinting) {
            player.setSprinting(false)
            mc.gameSettings.keyBindSprint.pressed = false
        }

        oldAttackInProgress = true
        killAuraAttack = true

        try {
            player.attackEntityWithModifiedSprint(entity, null) {
                if (swing) player.swingItem()
            }
        } finally {
            oldAttackInProgress = false
            killAuraAttack = false
        }

        if (debug)
            chat("NewUniversal attack target=${entity.entityId} sprint=${player.isSprinting}/${player.serverSprintState}")

        return true
    }

    val onKeepSprintEvent = handler<KeepSprintEvent> { event ->
        if (!isNewUniversal)
            return@handler

        if (isVelocityReducing)
            return@handler

        if (onlyKillAura && !killAuraAttack)
            return@handler

        if (event.eventState == EventState.PRE) {
            event.keepSprint = false
            event.cancelEvent()

            mc.thePlayer?.setSprinting(false)
            mc.thePlayer?.serverSprintState = false
        }
    }

    val onTick = handler<GameTickEvent> {
        val player = mc.thePlayer ?: return@handler

        if (player.isSprinting || KillAura.target == null || !isNewUniversal)
            sprintCancelled = false

        if (attackPending) {
            if (player.isSprinting) {
                player.setSprinting(false)
                mc.gameSettings.keyBindSprint.pressed = false
            }

            pendingTicks--
            if (pendingTicks <= 0)
                cancelPending()
        }
    }

    val onJump = handler<JumpEvent> {
        if (isNewUniversal && sprintCancelled) {
            if (mc.thePlayer?.isSprinting != true)
                it.cancelEvent()
        }
    }
}
