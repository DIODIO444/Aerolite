/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.font

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.kotlin.LruCache
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.bindTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A memory-optimized bitmap-based font renderer using AWT [Font].
 *
 * @author opZywl
 */
@SideOnly(Side.CLIENT)
class AWTFontRenderer(
    val font: Font,
    startChar: Int = 0,
    stopChar: Int = 255,
    private val loadingScreen: Boolean = false
) : MinecraftInstance {

    companion object : Listenable {
        var assumeNonVolatile: Boolean = false

        /** All active font renderers (for GC tasks). */
        private val activeFontRenderers = mutableListOf<AWTFontRenderer>()

        /**
         * Runs a block with [assumeNonVolatile] = true, then restores the previous value.
         *
         * Restoring (instead of unconditionally setting false) keeps nesting safe — HUD rendering
         * wraps the whole element loop, and individual elements used to reset the flag to false
         * half way through, which disabled display lists for everything drawn after them.
         */
        inline fun assumeNonVolatile(block: () -> Unit) {
            val previous = assumeNonVolatile
            assumeNonVolatile = true
            try {
                block()
            } finally {
                assumeNonVolatile = previous
            }
        }

        // Garbage collection constants
        private const val GC_TICKS = 600                    // Do GC every 600 frames
        private const val CACHED_FONT_REMOVAL_TIME = 30000L // 30s time-based eviction
        private const val MAX_CACHED_STRINGS = 255          // LRU cache size limit

        /** Upper bound for the per character CJK textures, oldest ones are dropped first. */
        private const val MAX_CJK_GLYPHS = 2048

        /**
         * Fonts that ship CJK glyphs on the common platforms, in order of preference. Aerolite
         * renders Chinese with these instead of the vanilla unicode pages, which look blurry.
         */
        private val CJK_FONT_NAMES = arrayOf(
            "Microsoft YaHei", "微软雅黑", "SimSun", "宋体", "Noto Sans CJK SC",
            "Source Han Sans SC", "PingFang SC", "WenQuanYi Micro Hei", Font.SANS_SERIF
        )

        /**
         * Picks a font that can actually display Chinese characters at [size].
         */
        private fun resolveCjkFont(size: Int): Font {
            for (name in CJK_FONT_NAMES) {
                val candidate = Font(name, Font.PLAIN, size)

                if (candidate.canDisplay('中') && candidate.canDisplay('国')) {
                    return candidate
                }
            }

            // Logical font => the JVM performs glyph fallback, so it always covers CJK.
            return Font(Font.SANS_SERIF, Font.PLAIN, size)
        }

        private fun metricsOf(targetFont: Font): FontMetrics =
            BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().let {
                it.font = targetFont
                it.fontMetrics
            }

        private var gcTicks = 0

        /**
         * Should be called each frame or so. Every 600 frames, we run garbage collection
         * on every active font renderer.
         */
        private val onRender2D = handler<Render2DEvent>(priority = Byte.MIN_VALUE) {
            if (++gcTicks > GC_TICKS) {
                activeFontRenderers.forEach { it.collectGarbage() }
                gcTicks = 0
            }
        }
    }

    /**
     * Info about each character's location in the texture.
     */
    private data class CharLocation(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    /**
     * Stores a compiled display list for a specific string, plus a "last usage" timestamp.
     */
    private data class CachedFont(
        val displayList: Int,
        var lastUsage: Long,
        var deleted: Boolean = false
    )

    private val charLocations = arrayOfNulls<CharLocation>(stopChar)

    /**
     * Visible (non transparent) pixel bounds of every baked glyph, as [minX, minY, maxX, maxY]
     * inside its character image. Needed to centre icons exactly: the image the glyph is baked
     * into carries padding (3px left, 1px top, 4px right, ...), so centring the whole image would
     * leave the visible icon off-centre inside a circle.
     */
    private val charInkBounds = HashMap<Int, IntArray>()

    /**
     * We store strings in an LRU-like map with time-based eviction:
     * - If the size exceeds [MAX_CACHED_STRINGS], we remove the eldest entry.
     * - If an entry hasn't been used for 30s, we remove it.
     */
    private val cachedStringFonts = LruCache<String, CachedFont>(MAX_CACHED_STRINGS)

    /**
     * We don't do GC for the cached widths because they are constant for each string
     */
    private val cachedStringWidths = LruCache<String, Int>(MAX_CACHED_STRINGS)

    /**
     * Strings already drawn at least once.
     *
     * Display lists are only compiled from the second occurrence on: values that change every
     * frame (scoreboard lines, name tags with a live health readout, ...) would otherwise create a
     * brand new GL display list each frame and immediately evict another one, which is slower than
     * just drawing the quads.
     */
    private val seenStrings = HashSet<String>()

    /** True once [text] has been drawn before, i.e. it is worth compiling into a display list. */
    private fun seenBefore(text: String): Boolean {
        if (seenStrings.size > 4096) {
            seenStrings.clear()
        }

        return !seenStrings.add(text)
    }

    private var textureID: Int = -1
    private var textureWidth: Int = 0
    private var textureHeight: Int = 0
    private var fontHeight: Int = -1

    /**
     * Typical "font height" to use in layout, derived from [fontHeight].
     * Adjust as needed.
     */
    val height: Int
        get() = (fontHeight - 8) / 2

    // ---- CJK support --------------------------------------------------------
    //
    // The baked atlas only covers chars 0..255. Everything else (i.e. all Chinese text) is
    // rasterised on demand with a CJK capable font at the same resolution and cached as a small
    // texture, instead of falling back to Minecraft's 16x16 unicode pages.

    private val mainMetrics: FontMetrics = metricsOf(font)

    /** Either [font] itself (when it ships CJK glyphs) or a system CJK font of the same size. */
    private val cjkFont: Font = if (font.canDisplay('中')) font else resolveCjkFont(font.size)

    private val cjkMetrics: FontMetrics = metricsOf(cjkFont)

    private data class CjkGlyph(
        val textureId: Int,
        val width: Int,
        val height: Int,
        /** Horizontal advance in atlas pixels (0.25 screen pixels each). */
        val advance: Float,
        /** Vertical offset that keeps the baseline aligned with the baked ASCII glyphs. */
        val yOffset: Float
    )

    private val cjkGlyphs = HashMap<Char, CjkGlyph>()
    private val cjkGlyphOrder = ArrayDeque<Char>()

    init {
        // Generate the large bitmap with all glyphs
        renderBitmap(startChar, stopChar)

        // Register for GC tasks
        activeFontRenderers += this
    }

    /**
     * Rasterises [char] with [cjkFont] and uploads it to a dedicated texture, caching the result.
     * Returns null when no font can display the character (the caller then falls back to vanilla).
     */
    private fun getCjkGlyph(char: Char): CjkGlyph? {
        cjkGlyphs[char]?.let { return it }

        if (!cjkFont.canDisplay(char)) {
            return null
        }

        val advance = cjkMetrics.charWidth(char).coerceAtLeast(1)
        val ascent = cjkMetrics.ascent

        val image = BufferedImage(advance + 2, cjkMetrics.height + 2, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.font = cjkFont
        graphics.color = Color.WHITE
        graphics.drawString(char.toString(), 1, 1 + ascent)
        graphics.dispose()

        val textureId = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), image, true, true)

        val glyph = CjkGlyph(
            textureId = textureId,
            width = image.width,
            height = image.height,
            advance = advance.toFloat(),
            yOffset = (mainMetrics.ascent - ascent).toFloat()
        )

        if (cjkGlyphOrder.size >= MAX_CJK_GLYPHS) {
            val oldest = cjkGlyphOrder.removeFirst()
            cjkGlyphs.remove(oldest)?.let { glDeleteTextures(it.textureId) }
        }

        cjkGlyphs[char] = glyph
        cjkGlyphOrder.addLast(char)

        return glyph
    }

    /** Emits the textured quad of a CJK glyph, in the 0.25 scaled drawing space. */
    private fun drawCjkQuad(glyph: CjkGlyph, x: Float, y: Float) {
        val width = glyph.width.toFloat()
        val height = glyph.height.toFloat()

        glTexCoord2f(0F, 0F); glVertex2f(x, y)
        glTexCoord2f(0F, 1F); glVertex2f(x, y + height)
        glTexCoord2f(1F, 1F); glVertex2f(x + width, y + height)
        glTexCoord2f(1F, 0F); glVertex2f(x + width, y)
    }

    /**
     * Draw [text] at ([x], [y]) with [color]. Scales by 0.25 => typical UI text size.
     */
    fun drawString(text: String, x: Double, y: Double, color: Int) {
        // Scale down => 0.25 => then everything is "2 * x" in real coords
        glPushMatrix()
        glScaled(0.25, 0.25, 0.25)

        // Shift to final position
        glTranslated(x * 2.0, y * 2.0 - 2.0, 0.0)

        if (loadingScreen) {
            glBindTexture(GL_TEXTURE_2D, textureID)
        } else {
            bindTexture(textureID)
        }

        // Extract ARGB
        val (alpha, red, green, blue) = ColorUtils.unpackARGBFloatValue(color)
        glColor4f(red, green, blue, alpha)

        // 1) If we've cached this text, just call the display list
        val cached = cachedStringFonts[text]
        if (cached != null) {
            glCallList(cached.displayList)
            cached.lastUsage = System.currentTimeMillis()
            glPopMatrix()
            return
        }

        // 2) Not cached => build it now. Strings that need CJK glyphs are not compiled into a
        // display list: those glyph textures can be evicted later, which would leave the list
        // pointing at a deleted texture.
        val cacheable = text.all { it.code < 0x100 }

        var listID = -1
        if (assumeNonVolatile && cacheable && seenBefore(text)) {
            listID = glGenLists(1)
            glNewList(listID, GL_COMPILE_AND_EXECUTE)
        }

        // Rasterise and upload every missing (CJK) glyph *before* the batch starts: texture
        // uploads inside glBegin()/glEnd() are illegal and silently leave an empty texture,
        // which used to show up as colourful noise instead of Chinese characters.
        for (char in text) {
            if (charLocations.getOrNull(char.code) == null) {
                getCjkGlyph(char)
            }
        }

        glBegin(GL_QUADS)

        var currX = 0f
        var fallbackWidth = 0f // fallback for MC glyphs

        for (char in text) {
            val loc = charLocations.getOrNull(char.code)

            if (loc != null) {
                drawChar(loc, currX + (fallbackWidth * 4f), 0f)
                currX += (loc.width - 8f)
                continue
            }

            // Characters outside the baked atlas (CJK) live in their own small textures.
            // Texture creation/upload is illegal between glBegin()/glEnd(), so close the batch
            // before asking for the glyph; they are normally pre-warmed already.
            glEnd()

            val glyph = getCjkGlyph(char)

            if (glyph != null) {
                bindTexture(glyph.textureId)
                glBegin(GL_QUADS)
                drawCjkQuad(glyph, currX, glyph.yOffset)
                glEnd()

                bindTexture(textureID)
                glBegin(GL_QUADS)

                currX += glyph.advance
                continue
            }

            // Last resort: vanilla unicode font
            GlStateManager.resetColor()

            glPushMatrix()

            // Because we scaled by 0.25 => revert
            val rev = 4.0f
            glScalef(rev, rev, rev)

            // Then scale by (font.size / 32.0)
            val scale = font.size / 32.0f
            glScalef(scale, scale, 1.0f)

            mc.fontRendererObj.posY = 1.0f
            mc.fontRendererObj.posX = (currX / rev) + fallbackWidth

            val fallbackW = mc.fontRendererObj.renderUnicodeChar(char, false).coerceAtLeast(0f)
            fallbackWidth += fallbackW

            if (loadingScreen) {
                glBindTexture(GL_TEXTURE_2D, textureID)
            } else {
                bindTexture(textureID)
            }
            glPopMatrix()
            glBegin(GL_QUADS)
        }

        glEnd()

        if (assumeNonVolatile && listID >= 0) {
            // Insert into our LRU + time-based map
            cachedStringFonts[text] = CachedFont(listID, System.currentTimeMillis())
            glEndList()
        }

        glPopMatrix()
    }

    /**
     * Draws a single glyph so that the centre of its **visible ink** lands exactly on
     * ([centerX], [centerY]) and the ink fits a [size] x [size] box.
     *
     * [drawString] positions the whole baked character image, which is padded (3px on the left,
     * 1px on top, 4px on the right, ...) — that is why icons used to sit visibly off-centre inside
     * the notification circles. This method compensates for the padding.
     *
     * @return true when the glyph exists, false otherwise.
     */
    fun drawCenteredGlyph(char: Char, centerX: Float, centerY: Float, size: Float, color: Int): Boolean {
        val loc = charLocations.getOrNull(char.code) ?: return false
        val ink = charInkBounds[char.code] ?: intArrayOf(0, 0, loc.width - 1, loc.height - 1)

        val inkWidth = (ink[2] - ink[0] + 1).toFloat()
        val inkHeight = (ink[3] - ink[1] + 1).toFloat()

        if (inkWidth <= 0f || inkHeight <= 0f) {
            return false
        }

        // Glyph quads are drawn at 0.25 scale, so one atlas pixel is 0.25 screen pixels.
        val scale = size / (max(inkWidth, inkHeight) * 0.25f)

        // drawString() puts the quad's top-left corner at (x / 2, y / 2 - 0.5).
        // Shift the quad so the ink centre ends up on the current origin.
        val inkCenterX = ink[0] + inkWidth / 2f
        val inkCenterY = ink[1] + inkHeight / 2f
        val drawX = (-inkCenterX * 0.5f).toDouble()
        val drawY = (1f - inkCenterY * 0.5f).toDouble()

        glPushMatrix()
        glTranslatef(centerX, centerY, 0f)
        glScalef(scale, scale, 1f)
        drawString(char.toString(), drawX, drawY, color)
        glPopMatrix()

        return true
    }

    /**
     * Returns the pixel-width of [text]. If a character is not in [charLocations],
     * we fallback to MC's font (approx).
     */
    fun getStringWidth(text: String): Int = cachedStringWidths.getOrPut(text) {
        var myWidth = 0
        var fallbackWidth = 0f
        val fallbackScale = font.size / 32f

        for (char in text) {
            val loc = charLocations.getOrNull(char.code)
            if (loc == null) {
                if (cjkFont.canDisplay(char)) {
                    // CJK glyphs advance charWidth atlas pixels => charWidth / 2 in this unit.
                    fallbackWidth += cjkMetrics.charWidth(char) / 2f
                } else {
                    val w = mc.fontRendererObj.getCharWidth(char)
                    fallbackWidth += ((w + 8) * fallbackScale).coerceAtLeast(0f)
                }
            } else {
                myWidth += (loc.width - 8)
            }
        }

        (myWidth / 2) + fallbackWidth.roundToInt()
    }

    /**
     * Disposes of this font, removing it from the list and freeing texture memory.
     */
    fun dispose() {
        if (textureID != -1) {
            glDeleteTextures(textureID)
            textureID = -1
        }

        cjkGlyphs.values.forEach { glDeleteTextures(it.textureId) }
        cjkGlyphs.clear()
        cjkGlyphOrder.clear()

        activeFontRenderers.remove(this)
    }

    /** If the user forgets to call [dispose], still free resources. */
    protected fun finalize() {
        dispose()
    }

    private fun drawChar(loc: CharLocation, x: Float, y: Float) {
        val w = loc.width.toFloat()
        val h = loc.height.toFloat()

        val u = loc.x.toFloat() / textureWidth
        val v = loc.y.toFloat() / textureHeight
        val uw = w / textureWidth
        val vh = h / textureHeight

        // 4 corners
        glTexCoord2f(u, v)
        glVertex2f(x, y)

        glTexCoord2f(u, v + vh)
        glVertex2f(x, y + h)

        glTexCoord2f(u + uw, v + vh)
        glVertex2f(x + w, y + h)

        glTexCoord2f(u + uw, v)
        glVertex2f(x + w, y)
    }

    /**
     * Builds the single large texture with [startChar] to [stopChar] glyphs.
     */
    private fun renderBitmap(startChar: Int, stopChar: Int) {
        val fontImages = arrayOfNulls<BufferedImage>(stopChar)

        var rowHeight = 0
        var charX = 0
        var charY = 0

        for (charCode in startChar until stopChar) {
            val charImg = drawCharToImage(charCode.toChar())
            val cw = charImg.width
            val ch = charImg.height

            if (ch > fontHeight) {
                fontHeight = ch
            }

            val loc = CharLocation(charX, charY, cw, ch)
            charLocations[charCode] = loc
            fontImages[charCode] = charImg

            charX += cw
            if (cw > 0 && ch > rowHeight) {
                rowHeight = ch
            }
            // If exceeding ~2k width, break line
            if (charX > 2048) {
                if (charX > textureWidth)
                    textureWidth = charX
                charX = 0
                charY += rowHeight
                rowHeight = 0
            }
        }
        // finalize
        textureWidth = max(textureWidth, charX)
        textureHeight = charY + rowHeight

        // Big final image
        val bigImage = BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB)
        val g = bigImage.createGraphics()
        g.font = font
        g.color = Color(255, 255, 255, 0) // transparent
        g.fillRect(0, 0, textureWidth, textureHeight)
        g.color = Color.WHITE

        // Draw each char subimage
        for (charCode in startChar until stopChar) {
            val subImg = fontImages[charCode] ?: continue
            val loc = charLocations[charCode] ?: continue
            g.drawImage(subImg, loc.x, loc.y, null)
        }

        // Upload to GPU
        textureID = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), bigImage, true, true)
    }

    /**
     * Draws a single char [c] into a small [BufferedImage].
     */
    private fun drawCharToImage(c: Char): BufferedImage {
        // measure to get width/height
        val measureImg = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        val measureG = measureImg.createGraphics()
        measureG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        measureG.font = font

        val fm = measureG.fontMetrics
        var w = fm.charWidth(c) + 8
        if (w <= 0) w = 7
        var h = fm.height + 3
        if (h <= 0) h = font.size

        // real
        val charImg = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g2d = charImg.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.font = font
        g2d.color = Color.WHITE
        g2d.drawString(c.toString(), 3, 1 + fm.ascent)
        g2d.dispose()
        measureG.dispose()

        // Remember where the actual ink sits so icons can be centred by their visible pixels.
        registerInkBounds(c, charImg)

        return charImg
    }

    /**
     * Scans [image] for non transparent pixels and stores their bounding box for [c].
     */
    private fun registerInkBounds(c: Char, image: BufferedImage) {
        var minX = image.width
        var minY = image.height
        var maxX = -1
        var maxY = -1

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                if (image.getRGB(x, y) ushr 24 == 0) continue

                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
        }

        if (maxX >= 0 && maxY >= 0) {
            charInkBounds[c.code] = intArrayOf(minX, minY, maxX, maxY)
        }
    }

    /**
     * Removes unused / old strings from the cache. Called by global GC every so often.
     */
    private fun collectGarbage() {
        val now = System.currentTimeMillis()

        with(cachedStringFonts.entries.iterator()) {
            while (hasNext()) {
                val cached = next().value
                if (!cached.deleted && (now - cached.lastUsage) > CACHED_FONT_REMOVAL_TIME) {
                    glDeleteLists(cached.displayList, 1)
                    cached.deleted = true
                    remove()
                }
            }
        }
    }
}
