/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer

/**
 * Adapter exposing the real [net.ccbluex.liquidbounce.features.special.CombatManager] instance (lazily,
 * since it is created during [LiquidBounce] startup) under the FDPClient-style object API used by the
 * ported modules.
 */
object CombatManager {
    val target: EntityLivingBase?
        get() = LiquidBounce.combatManager.target

    val inCombatState: Boolean
        get() = LiquidBounce.combatManager.target != null

    /**
     * FDPClient-compatible focus check accepting any [EntityLivingBase] (the real implementation
     * only operates on [EntityPlayer]).
     */
    fun isFocusEntity(entity: EntityLivingBase): Boolean =
        LiquidBounce.combatManager.isFocusEntity(entity as EntityPlayer)
}
