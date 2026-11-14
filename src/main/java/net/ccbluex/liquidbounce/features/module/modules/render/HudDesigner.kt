/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner

/**
 * Opens the HUD designer screen. Ported from the Aerolite code base, where it lived in
 * `features/module/modules/client`.
 */
object HudDesigner : Module("HudDesigner", Category.RENDER, canBeEnabled = false) {

    override fun onEnable() {
        mc.displayGuiScreen(GuiHudDesigner())
    }
}
