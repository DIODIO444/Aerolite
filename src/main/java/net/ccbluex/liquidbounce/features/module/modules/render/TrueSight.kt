/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
object TrueSight : Module("TrueSight", Category.RENDER) {
    val barriersValue = boolean("Barriers", true)
    val entitiesValue = boolean("Entities", true)    /* ---- Java (mixin) compatibility accessor ---- */
    val barriers: Boolean get() = barriersValue.get()
    val entities: Boolean get() = entitiesValue.get()

}