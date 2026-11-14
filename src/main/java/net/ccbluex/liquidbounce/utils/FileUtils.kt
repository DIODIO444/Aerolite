/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.utils.client.ClientUtils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Small file helpers, ported from the Aerolite code base.
 */
object FileUtils {

    /**
     * Copies a classpath resource to [file], creating it when missing.
     *
     * @param file destination on disk
     * @param name resource path, e.g. `assets/minecraft/liquidbounce/clicksound/enable.wav`
     */
    @JvmStatic
    fun unpackFile(file: File, name: String) {
        val stream = FileUtils::class.java.classLoader.getResourceAsStream(name)
            ?: FileUtils::class.java.getResourceAsStream("/$name")

        if (stream == null) {
            ClientUtils.LOGGER.warn("Unable to find resource '$name' to unpack.")
            return
        }

        stream.use { input ->
            file.parentFile?.mkdirs()
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
    }

    /** Reads an [InputStream] into a string, one line per line. */
    @JvmStatic
    fun readInputStream(inputStream: InputStream?): String? {
        if (inputStream == null) {
            return null
        }

        return inputStream.bufferedReader().use { it.readText() }
    }
}
