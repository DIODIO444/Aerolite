/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect


object AntiBlind : Module("AntiBlind", Category.RENDER) {
    val confusionEffectValue = boolean("Confusion", true)
    val pumpkinEffectValue = boolean("Pumpkin", true)
    val fireEffectValue = boolean("Fire", false)
    val fireEffectValue2 = float("FireAlpha", 0.2F, 0F..1F)
    private val brightValue = choices("Bright", arrayOf("None", "Gamma", "NightVision"), "Gamma")

    /* ---- Java (mixin) compatibility accessors ---- */
    val confusionEffect: Boolean get() = confusionEffectValue.get()
    val pumpkinEffect: Boolean get() = pumpkinEffectValue.get()
    val fireEffect: Float get() = fireEffectValue2.get()
    /** E dropped the achievements toggle; the overlay stays enabled. */
    val achievements: Boolean get() = true
    val bossHealth: Boolean get() = true

    private var prevGamma = -1f

    override fun onEnable() {
        prevGamma = mc.gameSettings.gammaSetting
    }

    override fun onDisable() {
        if (prevGamma == -1f) return
        mc.gameSettings.gammaSetting = prevGamma
        prevGamma = -1f
        if (mc.thePlayer != null) mc.thePlayer.removePotionEffectClient(Potion.nightVision.id)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (state || LiquidBounce.moduleManager[XRay::class.java]!!.state) {
            when (brightValue.get().lowercase()) {
                "gamma" -> if (mc.gameSettings.gammaSetting <= 100f) mc.gameSettings.gammaSetting++
                "nightvision" -> mc.thePlayer.addPotionEffect(PotionEffect(Potion.nightVision.id, 1337, 1))
            }
        } else if (prevGamma != -1f) {
            mc.gameSettings.gammaSetting = prevGamma
            prevGamma = -1f
        }
    }

    @EventTarget
    fun onShutdown(event: ClientShutdownEvent) {
        onDisable()
    }
}