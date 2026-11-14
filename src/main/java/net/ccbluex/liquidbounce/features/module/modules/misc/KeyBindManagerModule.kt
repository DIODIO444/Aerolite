/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import org.lwjgl.input.Keyboard

/**
 * Opens the on-screen keyboard editor (ported from Aerolite-main1's `ui.client.keybind` package).
 * The module itself never stays enabled — enabling it just opens the screen.
 */
object KeyBindManagerModule : Module("KeyBindManager", Category.MISC, Keyboard.KEY_RMENU) {

    override fun onEnable() {
        mc.displayGuiScreen(LiquidBounce.keyBindManager)
        state = false
    }
}
