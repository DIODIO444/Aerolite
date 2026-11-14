/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.sound

import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.FileUtils
import java.io.File

/**
 * Loads the custom click sounds from the `ClickSound` folder inside the client directory.
 *
 * The WAV files are unpacked from the jar on first use, so users can drop their own
 * `enable.wav` / `disable.wav` in there to override them.
 */
class TipSoundManager {

    val enableSound: TipSoundPlayer
    val disableSound: TipSoundPlayer

    init {
        val enableSoundFile = File(FileManager.soundsDir, "enable.wav")
        val disableSoundFile = File(FileManager.soundsDir, "disable.wav")

        if (!enableSoundFile.exists()) {
            FileUtils.unpackFile(enableSoundFile, "assets/minecraft/liquidbounce/clicksound/enable.wav")
        }

        if (!disableSoundFile.exists()) {
            FileUtils.unpackFile(disableSoundFile, "assets/minecraft/liquidbounce/clicksound/disable.wav")
        }

        enableSound = TipSoundPlayer(enableSoundFile)
        disableSound = TipSoundPlayer(disableSoundFile)
    }
}
