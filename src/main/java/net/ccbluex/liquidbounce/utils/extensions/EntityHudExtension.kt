/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.minecraft.client.Minecraft
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation

/**
 * Entity accessors used by the HUD elements (ported from the Aerolite EntityExtension).
 */

/** Hurt animation progress in the range 0..1 (vanilla renderHurtTime is 0..10). */
val EntityLivingBase.hurtPercent: Float
    get() = hurtTime / 10F

/** Player skin, falling back to the legacy default skin for mobs / unknown players. */
val EntityLivingBase.skin: ResourceLocation
    get() = (if (this is EntityPlayer) {
        Minecraft.getMinecraft().netHandler.getPlayerInfo(uniqueID)?.locationSkin
    } else {
        null
    }) ?: DefaultPlayerSkin.getDefaultSkinLegacy()

/** Latency in milliseconds, or -1 when unknown. */
val EntityLivingBase.ping: Int
    get() = (if (this is EntityPlayer) {
        Minecraft.getMinecraft().netHandler.getPlayerInfo(uniqueID)?.responseTime?.coerceAtLeast(0)
    } else {
        null
    }) ?: -1
