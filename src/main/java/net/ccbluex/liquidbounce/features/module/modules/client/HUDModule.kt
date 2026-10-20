/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.config.*

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.choices

/**
 * Configuration holder for HUD rendering effects (shadow / glow). Referenced as a singleton by
 * [net.ccbluex.liquidbounce.utils.render.shader.UIEffectRenderer].
 */
@ModuleInfo(name = "HUDModule", category = ModuleCategory.CLIENT)
object HUDModule : Module() {

    val uiEffectValue by boolean("UIEffect", false)

    val UiShadowValue by choices("UiShadow", arrayOf("Shadow", "Glow", "None"), "Shadow")
}
