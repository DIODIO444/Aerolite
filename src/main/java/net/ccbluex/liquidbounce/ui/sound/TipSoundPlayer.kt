/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.sound

import net.ccbluex.liquidbounce.utils.client.ClientUtils
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

/**
 * Plays a single WAV file on a background thread.
 *
 * Ported from the Aerolite code base. The clip is released as soon as playback stops so repeated
 * toggles do not leak native audio handles.
 */
class TipSoundPlayer(private val file: File) {

    /** Plays the sound without blocking the caller. */
    fun asyncPlay() {
        Thread({ playSound() }, "ClickSound-${file.nameWithoutExtension}").apply {
            isDaemon = true
            start()
        }
    }

    /** Plays the sound on the calling thread. */
    fun playSound() {
        try {
            if (!file.isFile) {
                return
            }

            val stream = AudioSystem.getAudioInputStream(file)
            val clip = AudioSystem.getClip()

            clip.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    clip.close()
                }
            }

            clip.open(stream)
            clip.start()
        } catch (ex: Exception) {
            ClientUtils.LOGGER.warn("Unable to play click sound '${file.name}'.", ex)
        }
    }

    companion object {
        /** Kept so callers can silence the clip type warning on older JDKs. */
        @Suppress("unused")
        private val clipType: Class<Clip> = Clip::class.java
    }
}
