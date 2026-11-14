/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat

/**
 * Tracks combat statistics (kills / games played / wins) for the HUD.
 *
 * Ported from the Aerolite code base, but wired to this project's event system.
 */
class CombatManager : Listenable, MinecraftInstance() {

    /** Last entity attacked by the local player, used by the HUD target element. */
    var target: EntityLivingBase? = null
        private set

    private var killCounts = 0L
    private var totalPlayed = 0L
    private var win = 0L

    private val attackedEntityList = mutableListOf<EntityLivingBase>()

    private val winPatterns = listOf("won", "victory", "you win", "1st place", "winner")
    private val gameStartPatterns = listOf("starting in", "game starting", "the game starts", "游戏开始")

    val onAttack = handler<AttackEvent>(always = true) { event ->
        val target = event.targetEntity
        if (target is EntityLivingBase && target !in attackedEntityList) {
            attackedEntityList += target
        }
    }

    val onUpdate = handler<UpdateEvent>(always = true) {
        target = net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.target

        if (mc.theWorld == null) {
            attackedEntityList.clear()
            return@handler
        }

        val iterator = attackedEntityList.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            if (entity.isDead) {
                killCounts++
                iterator.remove()
            }
        }
    }

    val onPacket = handler<PacketEvent>(always = true) { event ->
        if (event.eventType != EventState.RECEIVE) {
            return@handler
        }

        val packet = event.packet
        if (packet !is S02PacketChat) {
            return@handler
        }

        val text = packet.chatComponent?.unformattedText?.lowercase() ?: return@handler

        if (gameStartPatterns.any { text.contains(it) }) {
            totalPlayed++
        }

        if (winPatterns.any { text.contains(it) }) {
            win++
        }
    }

    fun getKillCounts(): Long = killCounts

    fun getTotalPlayed(): Long = totalPlayed

    fun getWin(): Long = win

    fun getNearByEntity(radius: Float): EntityLivingBase? {
        val player = mc.thePlayer ?: return null
        return mc.theWorld?.loadedEntityList
            ?.filterIsInstance<EntityLivingBase>()
            ?.filter { it != player && !it.isDead && player.getDistanceToEntity(it) <= radius }
            ?.minByOrNull { player.getDistanceToEntity(it) }
    }

    fun isFocusEntity(entity: EntityPlayer): Boolean = entity in attackedEntityList

    override fun handleEvents() = true
}
