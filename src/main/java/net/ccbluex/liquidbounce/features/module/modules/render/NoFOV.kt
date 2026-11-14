/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
object NoFOV : Module("NoFOV", Category.RENDER) {
    val fovValue = float("FOV", 1f, 0f..1.5f)    /* ---- Java (mixin) compatibility accessor ---- */
    val fov: Float get() = fovValue.get()

}
