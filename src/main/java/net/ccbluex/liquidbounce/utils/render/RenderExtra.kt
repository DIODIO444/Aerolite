/*
 * Aerolite rendering engine — extra 3D helpers and stencil clipping.
 *
 * These routines were part of LiquidBounce's RenderUtils object. They are kept as
 * top-level functions because RenderUtils is now the ported Aerolite engine class and
 * the callers only need the drawing behaviour, not the container.
 */
package net.ccbluex.liquidbounce.utils.render

import co.uk.hexeption.utils.OutlineUtils
import net.ccbluex.liquidbounce.utils.client.ClientUtils.disableFastRender
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.ccbluex.liquidbounce.utils.extensions.interpolatedPosition
import net.ccbluex.liquidbounce.utils.extensions.lerpWith
import net.ccbluex.liquidbounce.utils.extensions.plus
import net.ccbluex.liquidbounce.utils.extensions.prevPos
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val mc = net.minecraft.client.Minecraft.getMinecraft()

private const val CIRCLE_STEPS = 40

private val circlePoints = Array(CIRCLE_STEPS + 1) {
    val theta = 2 * PI * it / CIRCLE_STEPS
    Vec3(cos(theta), 0.0, sin(theta))
}

/**
 * Draws a hue-shifted circle around [position] in world space.
 */
fun drawHueCircle(position: Vec3, radius: Float, innerColor: Color, outerColor: Color) {
    val manager = mc.renderManager

    val renderX = manager.viewerPosX
    val renderY = manager.viewerPosY
    val renderZ = manager.viewerPosZ

    glPushAttrib(GL_ALL_ATTRIB_BITS)
    glPushMatrix()

    glDisable(GL_TEXTURE_2D)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glEnable(GL_LINE_SMOOTH)
    glDisable(GL_DEPTH_TEST)
    glDisable(GL_CULL_FACE)
    glEnable(GL_ALPHA_TEST)
    glAlphaFunc(GL_GREATER, 0.0f)
    mc.entityRenderer.disableLightmap()

    glBegin(GL_TRIANGLE_FAN)
    circlePoints.forEachIndexed { index, pos ->
        val innerHue = ColorUtils.shiftHue(innerColor, index / CIRCLE_STEPS)
        glColor4f(
            innerHue.red / 255f, innerHue.green / 255f, innerHue.blue / 255f, innerColor.alpha / 255f
        )
        glVertex3d(
            position.xCoord - renderX + pos.xCoord * radius,
            position.yCoord - renderY,
            position.zCoord - renderZ + pos.zCoord * radius
        )
    }
    glEnd()

    glBegin(GL_LINE_LOOP)
    circlePoints.forEachIndexed { index, pos ->
        val outerHue = ColorUtils.shiftHue(outerColor, index / CIRCLE_STEPS)
        glColor4f(
            outerHue.red / 255f, outerHue.green / 255f, outerHue.blue / 255f, outerColor.alpha / 255f
        )
        glVertex3d(
            position.xCoord - renderX + pos.xCoord * radius,
            position.yCoord - renderY,
            position.zCoord - renderZ + pos.zCoord * radius
        )
    }
    glEnd()

    glEnable(GL_CULL_FACE)
    glEnable(GL_DEPTH_TEST)
    glDisable(GL_ALPHA_TEST)
    glDisable(GL_LINE_SMOOTH)
    glDisable(GL_BLEND)
    glEnable(GL_TEXTURE_2D)
    glPopMatrix()
    glPopAttrib()
}

/**
 * Draws a dome around [pos]. Only [GL_LINES], [GL_TRIANGLES] and [GL_QUADS] are allowed.
 */
fun drawDome(pos: Vec3, hRadius: Double, vRadius: Double, lineWidth: Float? = null, color: Color, renderMode: Int) {
    require(renderMode == GL_LINES || renderMode == GL_TRIANGLES || renderMode == GL_QUADS)

    val manager = mc.renderManager ?: return

    val renderX = manager.viewerPosX
    val renderY = manager.viewerPosY
    val renderZ = manager.viewerPosZ

    val vStep = Math.PI / (CIRCLE_STEPS / 2)
    val hStep = 2 * Math.PI / CIRCLE_STEPS

    glPushAttrib(GL_ALL_ATTRIB_BITS)
    glPushMatrix()

    glDisable(GL_TEXTURE_2D)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glEnable(GL_LINE_SMOOTH)
    lineWidth?.let { glLineWidth(it) }
    glDisable(GL_DEPTH_TEST)
    glDisable(GL_CULL_FACE)
    glEnable(GL_ALPHA_TEST)
    glAlphaFunc(GL_GREATER, 0.0f)
    glTranslated(-renderX, -renderY, -renderZ)
    glColor(color)

    drawWithTessellatorWorldRenderer {
        begin(renderMode, DefaultVertexFormats.POSITION)

        val min = if (renderMode != GL_TRIANGLES) 0 to 0 else -1 to 1

        for (i in min.first until CIRCLE_STEPS / 2) {
            val vAngle1 = i * vStep
            val vAngle2 = (i + 1) * vStep

            for (j in min.second until CIRCLE_STEPS) {
                val hAngle1 = j * hStep
                val hAngle2 = (j + 1) * hStep

                val p1 = calculateDomeVertex(pos, vAngle1, hAngle1, hRadius, vRadius)
                val p2 = calculateDomeVertex(pos, vAngle2, hAngle1, hRadius, vRadius)
                val p3 = calculateDomeVertex(pos, vAngle2, hAngle2, hRadius, vRadius)
                val p4 = calculateDomeVertex(pos, vAngle1, hAngle2, hRadius, vRadius)

                if (renderMode == GL_QUADS) {
                    pos(p1[0], p1[1], p1[2]).endVertex()
                    pos(p2[0], p2[1], p2[2]).endVertex()
                    pos(p3[0], p3[1], p3[2]).endVertex()
                    pos(p4[0], p4[1], p4[2]).endVertex()
                } else {
                    pos(p1[0], p1[1], p1[2]).endVertex()
                    pos(p2[0], p2[1], p2[2]).endVertex()

                    pos(p2[0], p2[1], p2[2]).endVertex()
                    pos(p3[0], p3[1], p3[2]).endVertex()

                    pos(p3[0], p3[1], p3[2]).endVertex()
                    pos(p4[0], p4[1], p4[2]).endVertex()

                    pos(p4[0], p4[1], p4[2]).endVertex()
                    pos(p1[0], p1[1], p1[2]).endVertex()
                }
            }
        }
    }

    glEnable(GL_CULL_FACE)
    glEnable(GL_DEPTH_TEST)
    glDisable(GL_ALPHA_TEST)
    glDisable(GL_LINE_SMOOTH)
    glDisable(GL_BLEND)
    glEnable(GL_TEXTURE_2D)

    glPopMatrix()
    glPopAttrib()
}

private fun calculateDomeVertex(
    pos: Vec3, theta: Double, phi: Double, horizontalRadius: Double, verticalRadius: Double
): DoubleArray {
    return doubleArrayOf(
        pos.xCoord + horizontalRadius * sin(theta) * cos(phi),
        pos.yCoord + verticalRadius * cos(theta),
        pos.zCoord + horizontalRadius * sin(theta) * sin(phi)
    )
}

/** Runs [f] with the GL state required to batch many cones in one pass. */
fun drawConesForEntities(f: () -> Unit) {
    pushAttrib()
    pushMatrix()

    disableTexture2D()
    disableCull()

    enableBlend()
    glEnable(GL_DEPTH_TEST)
    depthMask(false)
    blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    f()

    resetColor()

    enableTexture2D()
    depthMask(true)
    enableCull()

    disableBlend()
    glDisable(GL_DEPTH_TEST)

    popMatrix()
    popAttrib()
}

/** Draws a single cone at the current matrix origin. */
fun drawCone(width: Float, height: Float, useTexture: Boolean = false) {
    if (useTexture) {
        mc.textureManager.bindTexture(ResourceLocation("liquidbounce/textures/hat.png"))
        enableTexture2D()
        depthMask(true)
    }

    drawWithTessellatorWorldRenderer {
        begin(GL_TRIANGLE_FAN, if (useTexture) DefaultVertexFormats.POSITION_TEX else DefaultVertexFormats.POSITION)

        if (useTexture) {
            pos(0.0, height.toDouble(), 0.0).tex(0.5, 0.5).endVertex()
        } else {
            pos(0.0, height.toDouble(), 0.0).endVertex()
        }

        for (point in circlePoints) {
            if (useTexture) {
                pos(point.xCoord * width, 0.0, point.zCoord * width)
                    .tex(0.5 + 0.5 * point.xCoord, 0.5 + 0.5 * point.zCoord).endVertex()
            } else {
                pos(point.xCoord * width, 0.0, point.zCoord * width).endVertex()
            }
        }
    }
}

/**
 * Useful for clipping any top-layered rectangle that falls outside a bottom-layered rectangle.
 */
inline fun withClipping(main: () -> Unit, toClip: () -> Unit) {
    disableFastRender()
    OutlineUtils.checkSetupFBO()
    glPushMatrix()

    disableAlpha()

    glEnable(GL_STENCIL_TEST)
    glStencilFunc(GL_ALWAYS, 1, 1)
    glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
    glStencilMask(1)
    glClear(GL_STENCIL_BUFFER_BIT)

    main()

    glStencilFunc(GL_EQUAL, 1, 1)
    glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
    glStencilMask(0)

    toClip()

    glStencilMask(0xFF)
    glDisable(GL_STENCIL_TEST)

    enableAlpha()

    glPopMatrix()
}

inline fun withOutline(main: () -> Unit, toOutline: () -> Unit) {
    disableFastRender()
    OutlineUtils.checkSetupFBO()
    glPushMatrix()

    disableAlpha()

    glEnable(GL_STENCIL_TEST)
    glClear(GL_STENCIL_BUFFER_BIT)

    glStencilFunc(GL_ALWAYS, 1, 1)
    glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
    glStencilMask(1)

    main()

    glStencilFunc(GL_EQUAL, 0, 1)
    glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
    glStencilMask(0)

    toOutline()

    glStencilMask(0xFF)
    glDisable(GL_STENCIL_TEST)

    enableAlpha()

    glPopMatrix()
}

/**
 * Draws the KillAura "circle" mark around [entity] in world space.
 *
 * Ported from the b100 RenderUtils so the KillAura module keeps working with the
 * Aerolite render engine.
 */
fun drawCircle(
    entity: EntityLivingBase,
    speed: Float,
    height: ClosedFloatingPointRange<Float>,
    size: Float,
    filled: Boolean,
    withHeight: Boolean,
    circleY: ClosedFloatingPointRange<Float>? = null,
    startColor: Int,
    endColor: Int
) {
    val manager = mc.renderManager
    val positions = mutableListOf<DoubleArray>()

    val renderX = manager.viewerPosX
    val renderY = manager.viewerPosY
    val renderZ = manager.viewerPosZ

    glPushAttrib(GL_ALL_ATTRIB_BITS)
    glPushMatrix()

    glDisable(GL_TEXTURE_2D)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glDisable(GL_CULL_FACE)
    glEnable(GL_ALPHA_TEST)
    glDepthMask(false)
    glAlphaFunc(GL_GREATER, 0.0f)
    mc.entityRenderer.disableLightmap()

    shadeModel(GL_SMOOTH)

    val a1 = (startColor shr 24 and 255) / 255f
    val r1 = (startColor shr 16 and 255) / 255f
    val g1 = (startColor shr 8 and 255) / 255f
    val b1 = (startColor and 255) / 255f
    val a2 = (endColor shr 24 and 255) / 255f
    val r2 = (endColor shr 16 and 255) / 255f
    val g2 = (endColor shr 8 and 255) / 255f
    val b2 = (endColor and 255) / 255f

    val breathingT = AnimationUtil.breathe(speed)
    val entityHeight = (entity.hitBox.maxY - entity.hitBox.minY).toFloat()

    val width = (mc.renderManager.getEntityRenderObject<Entity>(entity)?.shadowSize ?: 0.5F) + size
    val animatedHeight = (0F..entityHeight).lerpWith((height.endInclusive..height.start).lerpWith(breathingT))
    val animatedCircleY = (0F..entityHeight).lerpWith(circleY?.lerpWith(breathingT) ?: 0F)

    val tessellator = Tessellator.getInstance()
    val buffer = tessellator.worldRenderer

    if (filled) {
        buffer.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR)
    }

    entity.interpolatedPosition(entity.prevPos).let { pos ->
        circlePoints.forEach { point ->
            val p = pos + Vec3(point.xCoord * width, point.yCoord + animatedCircleY, point.zCoord * width)
            positions += doubleArrayOf(p.xCoord, p.yCoord, p.zCoord)

            if (filled) {
                buffer.pos(p.xCoord - renderX, p.yCoord - renderY, p.zCoord - renderZ)
                    .color(r1, g1, b1, a1)
                    .endVertex()
            }
        }
    }

    if (filled) {
        tessellator.draw()
    }

    if (withHeight) {
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)

        positions.forEachIndexed { index, pos ->
            val endPos = positions.getOrNull(index + 1) ?: return@forEachIndexed

            buffer.pos(pos[0] - renderX, pos[1] - renderY, pos[2] - renderZ).color(r1, g1, b1, a1).endVertex()
            buffer.pos(endPos[0] - renderX, endPos[1] - renderY, endPos[2] - renderZ).color(r1, g1, b1, a1)
                .endVertex()
            buffer.pos(endPos[0] - renderX, endPos[1] - renderY + animatedHeight, endPos[2] - renderZ)
                .color(r2, g2, b2, a2).endVertex()
            buffer.pos(pos[0] - renderX, pos[1] - renderY + animatedHeight, pos[2] - renderZ).color(r2, g2, b2, a2)
                .endVertex()
        }
        tessellator.draw()
    }

    shadeModel(GL_FLAT)
    glDisable(GL_ALPHA_TEST)
    glDisable(GL_BLEND)
    glEnable(GL_CULL_FACE)
    glDepthMask(true)
    glEnable(GL_TEXTURE_2D)
    glPopMatrix()
    glPopAttrib()
}
