/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.font

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_CLOUD
import net.ccbluex.liquidbounce.file.FileManager.fontsDir
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.io.*
import net.ccbluex.liquidbounce.utils.io.Downloader
import net.minecraft.client.gui.FontRenderer
import java.awt.Font
import java.io.File
import kotlin.system.measureTimeMillis

data class FontInfo(val name: String, val size: Int = -1, val isCustom: Boolean = false)

data class CustomFontInfo(val name: String, val fontFile: String, val fontSize: Int)

private val FONT_REGISTRY = LinkedHashMap<FontInfo, FontRenderer>()

object Fonts : MinecraftInstance {

    /**
     * Custom Fonts
     */
    private val configFile = File(fontsDir, "fonts.json")
    private var customFontInfoList: List<CustomFontInfo>
        get() = with(configFile) {
            if (exists()) {
                try {
                    // For old versions
                    readJson().asJsonArray.map {
                        it as JsonObject
                        val fontFile = it["fontFile"].asString
                        val fontSize = it["fontSize"].asInt
                        val name = if (it.has("name")) it["name"].asString else fontFile
                        CustomFontInfo(name, fontFile, fontSize)
                    }
                } catch (e: Exception) {
                    LOGGER.error("Failed to load fonts", e)
                    emptyList()
                }
            } else {
                createNewFile()
                writeText("[]") // empty list
                emptyList()
            }
        }
        set(value) = configFile.writeJson(value)

    val minecraftFontInfo = FontInfo(name = "Minecraft Font")
    val minecraftFont: FontRenderer by lazy {
        mc.fontRendererObj
    }

    lateinit var fontExtraBold35: GameFontRenderer
    lateinit var fontExtraBold40: GameFontRenderer
    lateinit var fontSemibold35: GameFontRenderer
    lateinit var fontSemibold40: GameFontRenderer
    lateinit var fontRegular40: GameFontRenderer
    lateinit var fontRegular45: GameFontRenderer
    lateinit var fontRegular35: GameFontRenderer
    lateinit var fontRegular30: GameFontRenderer
    lateinit var fontBold180: GameFontRenderer

    /* ------------------------------------------------------------------ *
     *  Aerolite / b100 font aliases                                       *
     *  The ported HUD elements reference these names directly.            *
     * ------------------------------------------------------------------ */

    val font35: GameFontRenderer get() = fontSemibold35
    val font40: GameFontRenderer get() = fontSemibold40
    val font32: GameFontRenderer get() = fontRegular30
    val font30: GameFontRenderer get() = fontRegular30
    val font24: GameFontRenderer get() = fontRegular30
    val font37: GameFontRenderer get() = fontRegular35
    val font52: GameFontRenderer get() = fontRegular45
    val font70: GameFontRenderer get() = fontBold180
    val font72: GameFontRenderer get() = fontBold180
    val font100: GameFontRenderer get() = fontBold180

    val tc35: GameFontRenderer get() = fontSemibold35

    /* Neverlose (NL*) faces used by Aerolite's AeroGui / HyperGui. */
    val NL16: GameFontRenderer by lazy { loadPackedFont("neverlose500.ttf", 26) }
    val NL18: GameFontRenderer by lazy { loadPackedFont("neverlose500.ttf", 30) }
    val NL20: GameFontRenderer by lazy { loadPackedFont("neverlose500.ttf", 33) }
    val NL24: GameFontRenderer by lazy { loadPackedFont("neverlose500.ttf", 40) }
    val NL35: GameFontRenderer by lazy { loadPackedFont("neverlose500.ttf", 58) }

    /* Tenacity (newdropdown) faces. */
    val SF14: GameFontRenderer by lazy { loadPackedFont("SF.ttf", 23) }
    val SF16: GameFontRenderer by lazy { loadPackedFont("SF.ttf", 26) }
    val SF18: GameFontRenderer by lazy { loadPackedFont("SF.ttf", 30) }
    val SF20: GameFontRenderer by lazy { loadPackedFont("SF.ttf", 33) }
    val SFBOLD18: GameFontRenderer by lazy { loadPackedFont("SFBOLD.ttf", 30) }
    val SFBOLD26: GameFontRenderer by lazy { loadPackedFont("SFBOLD.ttf", 43) }
    val CheckFont20: GameFontRenderer by lazy { loadPackedFont("check.ttf", 33) }
    val ICONFONT20: GameFontRenderer by lazy { loadPackedFont("stylesicons.ttf", 33) }
    val tc45: GameFontRenderer get() = fontRegular45
    val tc50: GameFontRenderer get() = fontRegular45
    val font48: GameFontRenderer get() = fontRegular45
    /* Icon fonts: these map plain ASCII letters to glyphs, so they must not fall back to a text font.
     *
     * Sizes are ~1.65x the Aerolite value because GameFontRenderer renders glyphs at roughly 0.6x the
     * AWT point size. Loading them at the original value produced a half-resolution atlas, which
     * looked blurry once scaled. */
    val icon30: GameFontRenderer by lazy { loadPackedFont("Icon.ttf", 34) }
    val icon35: GameFontRenderer by lazy { loadPackedFont("Icon.ttf", 38) }
    val icon40: GameFontRenderer by lazy { loadPackedFont("Icon.ttf", 42) }
    val poppins16: GameFontRenderer get() = fontRegular30
    val poppinsBold20: GameFontRenderer get() = fontExtraBold35
    val gs35: GameFontRenderer get() = fontSemibold35
    val gs40: GameFontRenderer get() = fontSemibold40
    val gs15: GameFontRenderer by lazy { loadPackedFont("GoogleSans.ttf", 30) }
    val tc40: GameFontRenderer by lazy { loadPackedFont("Tenacity.ttf", 33) }
    val Nicon80: GameFontRenderer by lazy { loadPackedFont("novoline.ttf", 66) }
    val Nicon40: GameFontRenderer by lazy { loadPackedFont("novoline.ttf", 40) }
    val Nicon20: GameFontRenderer by lazy { loadPackedFont("novoline.ttf", 24) }
    val fontBangers: GameFontRenderer by lazy { loadPackedFont("Bangers.ttf", 38) }
    val fontJello: GameFontRenderer get() = fontSemibold35

    /** Main-menu icon font: G..M map to the single player / multiplayer / alt / mods / options / language / network / quit icons. */
    val mainmenu: GameFontRenderer by lazy { loadPackedFont("mainmenu.ttf", 76) }

    private fun <T : FontRenderer> register(fontInfo: FontInfo, fontRenderer: T): T {
        FONT_REGISTRY[fontInfo] = fontRenderer
        return fontRenderer
    }

    fun registerCustomAWTFont(customFontInfo: CustomFontInfo, save: Boolean = true): GameFontRenderer? {
        val font = getFontFromFileOrNull(customFontInfo.fontFile, customFontInfo.fontSize) ?: return null

        val result = register(
            FontInfo(customFontInfo.name, customFontInfo.fontSize, isCustom = true),
            font.asGameFontRenderer()
        )

        if (save) {
            customFontInfoList += customFontInfo
        }

        return result
    }

    fun loadFonts() {
        LOGGER.info("Start to load fonts.")
        val time = measureTimeMillis {
            downloadFonts()

            register(minecraftFontInfo, minecraftFont)

            // Regular text uses the bundled HarmonyOS Sans SC (Aerolite's "regular.ttf"): it covers
            // Latin *and* CJK, so Chinese no longer has to fall back to another typeface.
            fontRegular30 = register(
                FontInfo(name = "HarmonyOS Sans SC", size = 30),
                loadPackedFont("regular.ttf", 30)
            )

            fontSemibold35 = register(
                FontInfo(name = "Outfit Semibold", size = 35),
                getFontFromFile("Outfit-Semibold.ttf", 35).asGameFontRenderer()
            )

            fontRegular35 = register(
                FontInfo(name = "HarmonyOS Sans SC", size = 35),
                loadPackedFont("regular.ttf", 35)
            )

            fontRegular40 = register(
                FontInfo(name = "HarmonyOS Sans SC", size = 40),
                loadPackedFont("regular.ttf", 40)
            )

            fontSemibold40 = register(
                FontInfo(name = "Outfit Semibold", size = 40),
                getFontFromFile("Outfit-Semibold.ttf", 40).asGameFontRenderer()
            )

            fontRegular45 = register(
                FontInfo(name = "HarmonyOS Sans SC", size = 45),
                loadPackedFont("regular.ttf", 45)
            )

            fontSemibold40 = register(
                FontInfo(name = "Outfit Semibold", size = 40),
                getFontFromFile("Outfit-Semibold.ttf", 40).asGameFontRenderer()
            )

            fontExtraBold35 = register(
                FontInfo(name = "Outfit Extrabold", size = 35),
                getFontFromFile("Outfit-Extrabold.ttf", 35).asGameFontRenderer()
            )

            fontExtraBold40 = register(
                FontInfo(name = "Outfit Extrabold", size = 40),
                getFontFromFile("Outfit-Extrabold.ttf", 40).asGameFontRenderer()
            )

            fontBold180 = register(
                FontInfo(name = "Outfit Bold", size = 180),
                getFontFromFile("Outfit-Bold.ttf", 180).asGameFontRenderer()
            )

            loadCustomFonts()
        }
        LOGGER.info("Loaded ${FONT_REGISTRY.size} fonts in ${time}ms")
    }

    private fun loadCustomFonts() {
        FONT_REGISTRY.keys.removeIf { it.isCustom }

        customFontInfoList.forEach {
            registerCustomAWTFont(it, save = false)
        }
    }

    fun downloadFonts() {
        fontsDir.mkdirs()
        val outputFile = File(fontsDir, "outfit.zip")
        if (!outputFile.exists()) {
            LOGGER.info("Downloading fonts...")
            Downloader.downloadWholeFile("$CLIENT_CLOUD/fonts/Outfit.zip", outputFile)
            LOGGER.info("Extracting fonts...")
            outputFile.extractZipTo(fontsDir)
        }
    }

    fun getFontRenderer(name: String, size: Int): FontRenderer {
        return FONT_REGISTRY.entries.firstOrNull { (fontInfo, _) ->
            fontInfo.size == size && fontInfo.name.equals(name, true)
        }?.value ?: minecraftFont
    }

    fun getFontDetails(fontRenderer: FontRenderer): FontInfo? {
        return FONT_REGISTRY.keys.firstOrNull { FONT_REGISTRY[it] == fontRenderer }
    }

    /** Human readable name for the font pickers, e.g. "HarmonyOS Sans SC 35". */
    fun labelOf(fontRenderer: FontRenderer): String {
        if (fontRenderer === minecraftFont)
            return "Minecraft"

        val info = getFontDetails(fontRenderer) ?: return "Unknown"
        return if (info.size > 0) "${info.name} ${info.size}" else info.name
    }

    /** Next registered font after [current], wrapping around. */
    fun nextFont(current: FontRenderer): FontRenderer {
        val list = fonts
        if (list.isEmpty())
            return current

        val index = list.indexOfFirst { it === current }
        return list[(index + 1) % list.size]
    }

    /** Previous registered font before [current], wrapping around. */
    fun previousFont(current: FontRenderer): FontRenderer {
        val list = fonts
        if (list.isEmpty())
            return current

        val index = list.indexOfFirst { it === current }
        return list[if (index <= 0) list.size - 1 else index - 1]
    }

    val fonts: List<FontRenderer>
        get() = FONT_REGISTRY.values.toList()

    /** b100 / Aerolite accessor (JVM name differs from [fonts] to avoid a signature clash). */
    @JvmName("getFontsList")
    fun getFonts(): List<FontRenderer> = fonts

    val customFonts: Map<FontInfo, FontRenderer>
        get() = FONT_REGISTRY.filterKeys { it.isCustom }

    fun removeCustomFont(fontInfo: FontInfo): CustomFontInfo? {
        if (!fontInfo.isCustom) {
            return null
        }

        FONT_REGISTRY.remove(fontInfo)
        return customFontInfoList.firstOrNull {
            it.name == fontInfo.name && it.fontSize == fontInfo.size
        }?.also {
            customFontInfoList -= it
        }
    }

    private fun getFontFromFileOrNull(file: String, size: Int): Font? = try {
        File(fontsDir, file).inputStream().use { inputStream ->
            Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(Font.PLAIN, size.toFloat())
        }
    } catch (e: Exception) {
        LOGGER.warn("Exception during loading font[name=${file}, size=${size}]", e)
        null
    }

    private fun getFontFromFile(file: String, size: Int): Font =
        getFontFromFileOrNull(file, size) ?: Font("default", Font.PLAIN, size)

    /**
     * Loads a TTF bundled in the jar (`assets/minecraft/Fonts/<file>`).
     *
     * The ported HUD elements use icon fonts whose ASCII letters map to glyphs — falling back to a
     * normal text font renders the raw letters instead (a "C" instead of an info icon).
     */
    private fun loadPackedFont(file: String, size: Int): GameFontRenderer {
        val font = runCatching {
            javaClass.classLoader.getResourceAsStream("assets/minecraft/Fonts/$file")?.use { stream ->
                Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(Font.PLAIN, size.toFloat())
            }
        }.onFailure { LOGGER.warn("Failed to load packed font $file", it) }.getOrNull()
            ?: runCatching { getFontFromFileOrNull(file, size) }.getOrNull()
            ?: Font("default", Font.PLAIN, size)

        return font.asGameFontRenderer()
    }

    private fun Font.asGameFontRenderer(): GameFontRenderer {
        return GameFontRenderer(this@asGameFontRenderer)
    }

}
