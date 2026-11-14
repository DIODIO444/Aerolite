/*
 * Aerolite rendering engine — UI shadow / glow panel renderer.
 *
 * Ported from the Aerolite code base. The original gated the drawing behind the
 * FDPClient HUDModule settings; here the decision belongs to the caller, so the
 * renderer only exposes the two panel styles.
 */
package net.ccbluex.liquidbounce.utils.render.shader

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*

object UIEffectRenderer {

    /** Panel shadow style used by the UI when it asks for a soft shadow. */
    enum class ShadowStyle { None, Shadow, Glow }

    /** Global style, changed by the ClickGUI / HUD settings. */
    @JvmField
    var shadowStyle: ShadowStyle = ShadowStyle.Shadow

    @JvmStatic
    fun drawGlowWithCustomAlpha(x: Float, y: Float, width: Float, height: Float, alpha: Float) {
        drawPanel(x, y, width, height, "glowpanel", alpha)
    }

    @JvmStatic
    fun drawShadowWithCustomAlpha(x: Float, y: Float, width: Float, height: Float, alpha: Float) {
        when (shadowStyle) {
            ShadowStyle.None -> return
            ShadowStyle.Shadow -> drawPanel(x, y, width, height, "panel", alpha)
            ShadowStyle.Glow -> drawPanel(x, y, width, height, "glowpanel", alpha)
        }
    }

    private fun drawPanel(x: Float, y: Float, width: Float, height: Float, prefix: String, alpha: Float) {
        drawTexturedRectWithCustomAlpha(x - 9, y - 9, 9f, 9f, "${prefix}topleft", alpha)
        drawTexturedRectWithCustomAlpha(x - 9, y + height, 9f, 9f, "${prefix}bottomleft", alpha)
        drawTexturedRectWithCustomAlpha(x + width, y + height, 9f, 9f, "${prefix}bottomright", alpha)
        drawTexturedRectWithCustomAlpha(x + width, y - 9, 9f, 9f, "${prefix}topright", alpha)
        drawTexturedRectWithCustomAlpha(x - 9, y, 9f, height, "${prefix}left", alpha)
        drawTexturedRectWithCustomAlpha(x + width, y, 9f, height, "${prefix}right", alpha)
        drawTexturedRectWithCustomAlpha(x, y - 9, width, 9f, "${prefix}top", alpha)
        drawTexturedRectWithCustomAlpha(x, y + height, width, 9f, "${prefix}bottom", alpha)
    }

    @JvmStatic
    fun drawTexturedRectWithCustomAlpha(x: Float, y: Float, width: Float, height: Float, image: String, alpha: Float) {
        glPushMatrix()
        val enableBlend = glIsEnabled(GL_BLEND)
        val disableAlpha = !glIsEnabled(GL_ALPHA_TEST)
        if (!enableBlend) glEnable(GL_BLEND)
        if (!disableAlpha) glDisable(GL_ALPHA_TEST)
        GlStateManager.color(1f, 1f, 1f, alpha)
        Minecraft.getMinecraft().textureManager.bindTexture(ResourceLocation("liquidbounce/effects/$image.png"))
        drawModalRectWithCustomSizedTexture(
            x,
            y,
            0F,
            0F,
            width,
            height,
            width,
            height
        )
        if (!enableBlend) glDisable(GL_BLEND)
        if (!disableAlpha) glEnable(GL_ALPHA_TEST)
        GlStateManager.resetColor()
        glPopMatrix()
    }

    @JvmStatic
    fun drawTexturedRect(x: Float, y: Float, width: Float, height: Float, image: String) {
        glPushMatrix()
        val enableBlend = glIsEnabled(GL_BLEND)
        val disableAlpha = !glIsEnabled(GL_ALPHA_TEST)
        if (!enableBlend) glEnable(GL_BLEND)
        if (!disableAlpha) glDisable(GL_ALPHA_TEST)
        Minecraft.getMinecraft().textureManager.bindTexture(ResourceLocation("liquidbounce/effects/$image.png"))
        GlStateManager.color(1f, 1f, 1f, 1f)
        drawModalRectWithCustomSizedTexture(
            x,
            y,
            0F,
            0F,
            width,
            height,
            width,
            height
        )
        if (!enableBlend) glDisable(GL_BLEND)
        if (!disableAlpha) glEnable(GL_ALPHA_TEST)
        glPopMatrix()
    }

    @JvmStatic
    fun drawModalRectWithCustomSizedTexture(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        width: Float,
        height: Float,
        textureWidth: Float,
        textureHeight: Float
    ) {
        val f = 1.0f / textureWidth
        val f1 = 1.0f / textureHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(
            (u * f).toDouble(),
            ((v + height) * f1).toDouble()
        ).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(
            ((u + width) * f).toDouble(),
            ((v + height) * f1).toDouble()
        ).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(
            ((u + width) * f).toDouble(),
            (v * f1).toDouble()
        ).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }
}
