/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.sound

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.asResourceLocation
import net.ccbluex.liquidbounce.utils.client.playSound

/**
 * Plays the module toggle sound, ported from the Aerolite code base.
 *
 * The mode is configured through `ClickGUI -> ToggleSound`:
 *
 * - `None`   – silent
 * - `Click`  – vanilla `random.click`
 * - `Custom` – the WAV files in the `ClickSound` folder
 */
object ClickSound : MinecraftInstance {

    /**
     * @param enable `true` when a module was enabled, `false` when disabled. Only the custom
     *               mode distinguishes between the two.
     */
    fun play(enable: Boolean) {
        when (ClickGUI.toggleSound.lowercase()) {
            "click" -> mc.playSound("random.click".asResourceLocation())

            "custom" -> {
                val manager = LiquidBounce.tipSoundManager
                if (enable) manager.enableSound.asyncPlay() else manager.disableSound.asyncPlay()
            }

            else -> Unit
        }
    }
}
