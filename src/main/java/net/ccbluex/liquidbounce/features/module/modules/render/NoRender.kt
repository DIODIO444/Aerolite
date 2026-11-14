/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer


object NoRender : Module("NoRender", Category.RENDER) {

    private val itemsValue = boolean("Items", true)
    private val playersValue = boolean("Players", true)
    private val mobsValue = boolean("Mobs", true)
    private val animalsValue = boolean("Animals", true)
    val armorStandValue = boolean("ArmorStand", true)
    val allValue = boolean("All", true)
    private val autoResetValue = boolean("AutoReset", true)
    private val maxRenderRange = float("MaxRenderRange", 4F, 0F..16F)

    @EventTarget
    fun onMotion(event: MotionEvent) {
        for (en in mc.theWorld.loadedEntityList) {
            val entity = en!! as Entity
            if (shouldStopRender(entity))
                entity.renderDistanceWeight = 0.0
            else if (autoResetValue.get())
                entity.renderDistanceWeight = 1.0
        }
    }

    fun shouldStopRender(entity: Entity): Boolean {
        return (allValue.get()
                ||(itemsValue.get() && entity is EntityItem)
                || (playersValue.get() && entity is EntityPlayer)
                || (mobsValue.get() && EntityUtils.isMob(entity))
                || (animalsValue.get() && EntityUtils.isAnimal(entity))
                || (armorStandValue.get() && entity is EntityArmorStand))
                && entity != mc.thePlayer!!
                && (mc.thePlayer!!.getDistanceToEntityBox(entity).toFloat() > maxRenderRange.get())
    }

    override fun onDisable() {
        for (en in mc.theWorld.loadedEntityList) {
            val entity = en!! as Entity
            if (entity != mc.thePlayer!! && entity.renderDistanceWeight <= 0.0)
                entity.renderDistanceWeight = 1.0
        }
    }

}