package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.injection.access.StaticStorage
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation

object BlurUtils : MinecraftInstance() {
    private val blurShader: ShaderGroup = ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, ResourceLocation("shaders/post/blurArea.json"))
    private lateinit var buffer: Framebuffer
    private var lastScale = 0
    private var lastScaleWidth = 0
    private var lastScaleHeight = 0

    private fun reinitShader() {
        blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
        buffer = Framebuffer(mc.displayWidth, mc.displayHeight, true)
        buffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f)
    }

    fun draw(x: Float, y: Float, width: Float, height: Float, radius: Float) {
        val scale = StaticStorage.scaledResolution
        val factor = scale.scaleFactor
        val factor2 = scale.scaledWidth
        val factor3 = scale.scaledHeight
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            reinitShader()
        }
        lastScale = factor
        lastScaleWidth = factor2
        lastScaleHeight = factor3
        blurShader.listShaders[0].shaderManager.getShaderUniform("BlurXY")[x] = factor3 - y - height
        blurShader.listShaders[1].shaderManager.getShaderUniform("BlurXY")[x] = factor3 - y - height
        blurShader.listShaders[0].shaderManager.getShaderUniform("BlurCoord")[width] = height
        blurShader.listShaders[1].shaderManager.getShaderUniform("BlurCoord")[width] = height
        blurShader.listShaders[0].shaderManager.getShaderUniform("Radius").set(radius)
        blurShader.listShaders[1].shaderManager.getShaderUniform("Radius").set(radius)
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)
    }

    /* ------------------------------------------------------------------ *
     *  Region blur (Aerolite `me.stars.utils.BlurUtils` port)              *
     *  Used by the HUD elements and TabGUI to blur only the area they      *
     *  cover instead of the whole screen.                                  *
     * ------------------------------------------------------------------ */

    private val shaderGroup = ShaderGroup(
        mc.textureManager, mc.resourceManager, mc.framebuffer, ResourceLocation("shaders/post/blurArea.json")
    )
    private val resultBuffer: Framebuffer = shaderGroup.getFramebufferRaw("result")

    private var lastFactor = 0
    private var lastWidth = 0
    private var lastHeight = 0

    private var lastX = 0F
    private var lastY = 0F
    private var lastW = 0F
    private var lastH = 0F
    private var lastStrength = 5F

    private fun setupFramebuffers() {
        try {
            shaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
        } catch (e: Exception) {
            ClientUtils.LOGGER.error("Exception caught while setting up the blur shader group", e)
        }
    }

    private fun setValues(strength: Float, x: Float, y: Float, w: Float, h: Float,
                          width: Float, height: Float, force: Boolean = false) {
        if (!force && strength == lastStrength && lastX == x && lastY == y && lastW == w && lastH == h) {
            return
        }

        lastStrength = strength
        lastX = x
        lastY = y
        lastW = w
        lastH = h

        for (i in 0..1) {
            shaderGroup.listShaders[i].shaderManager.getShaderUniform("Radius").set(strength)
            shaderGroup.listShaders[i].shaderManager.getShaderUniform("BlurXY")[x] = height - y - h
            shaderGroup.listShaders[i].shaderManager.getShaderUniform("BlurCoord")[w] = h
        }
    }

    fun sizeHasChanged(scaleFactor: Int, width: Int, height: Int): Boolean =
        lastFactor != scaleFactor || lastWidth != width || lastHeight != height

    /**
     * Blurs the given rectangle and re-draws it, clipped by [triggerMethod].
     */
    fun blur(posX: Float, posY: Float, posXEnd: Float, posYEnd: Float, blurStrength: Float,
             displayClipMask: Boolean, triggerMethod: () -> Unit) {
        if (!OpenGlHelper.isFramebufferEnabled()) {
            return
        }

        val x = minOf(posX, posXEnd)
        val y = minOf(posY, posYEnd)
        val x2 = maxOf(posX, posXEnd)
        val y2 = maxOf(posY, posYEnd)

        // blurArea.fsh divides by Radius (1.0 / radius). With radius == 0 that is Infinity, the
        // SCurve weight becomes NaN and the whole region is written as NaN -> pure black.
        // Anything <= 0 therefore used to render as an opaque black mask instead of a blur.
        val radius = if (blurStrength <= 0F) 8F else blurStrength.coerceIn(1F, 30F)

        val sc = ScaledResolution(mc)
        val scaleFactor = sc.scaleFactor
        val width = sc.scaledWidth
        val height = sc.scaledHeight

        if (sizeHasChanged(scaleFactor, width, height)) {
            setupFramebuffers()
            setValues(radius, x, y, x2 - x, y2 - y, width.toFloat(), height.toFloat(), true)
        }

        lastFactor = scaleFactor
        lastWidth = width
        lastHeight = height

        setValues(radius, x, y, x2 - x, y2 - y, width.toFloat(), height.toFloat())

        shaderGroup.mainFramebuffer.bindFramebuffer(true)
        shaderGroup.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)

        Stencil.write(displayClipMask)
        triggerMethod()

        Stencil.erase(true)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)
        GlStateManager.pushMatrix()
        GlStateManager.colorMask(true, true, true, false)
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
        GlStateManager.enableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
        resultBuffer.bindFramebufferTexture()
        GlStateManager.color(1F, 1F, 1F, 1F)

        val f2 = resultBuffer.framebufferWidth.toDouble() / resultBuffer.framebufferTextureWidth.toDouble()
        val f3 = resultBuffer.framebufferHeight.toDouble() / resultBuffer.framebufferTextureHeight.toDouble()

        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRenderer.pos(0.0, height.toDouble(), 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex()
        worldRenderer.pos(width.toDouble(), height.toDouble(), 0.0).tex(f2, 0.0).color(255, 255, 255, 255).endVertex()
        worldRenderer.pos(width.toDouble(), 0.0, 0.0).tex(f2, f3).color(255, 255, 255, 255).endVertex()
        worldRenderer.pos(0.0, 0.0, 0.0).tex(0.0, f3).color(255, 255, 255, 255).endVertex()
        tessellator.draw()

        resultBuffer.unbindFramebufferTexture()
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.popMatrix()
        GlStateManager.disableBlend()

        Stencil.dispose()
        GlStateManager.enableAlpha()
    }

    /** Blurs a rectangular area (square corners). */
    fun blurArea(x: Float, y: Float, x2: Float, y2: Float, blurStrength: Float) =
        blur(x, y, x2, y2, blurStrength, false) {
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            RenderUtils.quickDrawRect(x, y, x2, y2)
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
        }

    /** Blurs a rounded rectangular area. */
    fun blurAreaRounded(x: Float, y: Float, x2: Float, y2: Float, rad: Float, blurStrength: Float) =
        blur(x, y, x2, y2, blurStrength, false) {
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            RenderUtils.fastRoundedRect(x, y, x2, y2, rad)
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
        }
}