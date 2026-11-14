/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.config.displayable
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import org.lwjgl.util.glu.Sphere
import java.awt.Color


object Breadcrumbs : Module("Breadcrumbs", Category.RENDER) {
    private val typeValue = choices("Type", arrayOf("Line", "Rect", "Sphere", "Rise"), "Line")
    val colorRedValue = int("R", 255, 0..255).displayable { !colorRainbowValue.get() }
    val colorGreenValue = int("G", 255, 0..255).displayable { !colorRainbowValue.get() }
    val colorBlueValue = int("B", 255, 0..255).displayable { !colorRainbowValue.get() }
    val colorAlphaValue = int("Alpha", 255, 0..255)
    val colorRainbowValue = boolean("Rainbow", false)
    private val fadeValue = boolean("Fade", true)
    private val drawThePlayerValue = boolean("DrawThePlayer", true)
    private val drawTargetsValue = boolean("DrawTargets", true)
    private val fadeTimeValue = int("FadeTime", 5, 1..20)
    private val precisionValue = int("Precision", 4, 1..20)
    private val lineWidthValue = int("LineWidth", 1, 1..10).displayable { typeValue.equals("Line") }
    private val sphereScaleValue = float("SphereScale", 0.6f, 0.1f..2f).displayable { typeValue.equals("Sphere") || typeValue.equals("Rise")}

    private val points = mutableMapOf<Int, MutableList<BreadcrumbPoint>>()

    val color: Color
        get() = if (colorRainbowValue.get()) rainbow() else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())

    private val sphereList = GL11.glGenLists(1)

    init {
        GL11.glNewList(sphereList, GL11.GL_COMPILE)

        val shaft = Sphere()
        shaft.drawStyle = GLU.GLU_FILL
        shaft.draw(0.3f, 25, 10)

        GL11.glEndList()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val fTime = fadeTimeValue.get() * 1000
        val fadeSec = System.currentTimeMillis() - fTime
        val colorAlpha = colorAlphaValue.get() / 255.0f

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        mc.entityRenderer.disableLightmap()
        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ
        points.forEach { (_, mutableList) ->
            var lastPosX = 114514.0
            var lastPosY = 114514.0
            var lastPosZ = 114514.0
            when(typeValue.get().lowercase()) {
                "line" -> {
                    GL11.glLineWidth(lineWidthValue.get().toFloat())
                    GL11.glEnable(GL11.GL_LINE_SMOOTH)
                    GL11.glBegin(GL11.GL_LINE_STRIP)
                }
                "rect" -> {
                    GL11.glDisable(GL11.GL_CULL_FACE)
                }
            }
            for (point in mutableList.reversed()) {
                val alpha = if (fadeValue.get()) {
                    val pct = (point.time - fadeSec).toFloat() / fTime
                    if (pct < 0 || pct > 1) {
                        mutableList.remove(point)
                        continue
                    }
                    pct
                } else { 1f } * colorAlpha
                if (!typeValue.equals("Rise")) {
                    RenderUtils.glColor(point.color, alpha)
                }
                when(typeValue.get().lowercase()) {
                    "line" -> GL11.glVertex3d(point.x - renderPosX, point.y - renderPosY, point.z - renderPosZ)
                    "rect" -> {
                        if(!(lastPosX==114514.0 && lastPosY==114514.0 && lastPosZ==114514.0)) {
                            GL11.glBegin(GL11.GL_QUADS)
                            GL11.glVertex3d(point.x - renderPosX, point.y - renderPosY, point.z - renderPosZ)
                            GL11.glVertex3d(lastPosX, lastPosY, lastPosZ)
                            GL11.glVertex3d(lastPosX, lastPosY + mc.thePlayer.height, lastPosZ)
                            GL11.glVertex3d(point.x - renderPosX, point.y - renderPosY + mc.thePlayer.height, point.z - renderPosZ)
                            GL11.glEnd()
                        }
                        lastPosX = point.x - renderPosX
                        lastPosY = point.y - renderPosY
                        lastPosZ = point.z - renderPosZ
                    }
                    "rise" -> {

                        val circleScale = sphereScaleValue.get()
                        RenderUtils.glColor(point.color, 20)
                        GL11.glPushMatrix()
                        GL11.glTranslated(point.x - renderPosX, point.y - renderPosY, point.z - renderPosZ)
                        GL11.glScalef(circleScale, circleScale, circleScale)
                        GL11.glCallList(sphereList)
                        GL11.glPopMatrix()

                        RenderUtils.glColor(point.color, 35)
                        GL11.glPushMatrix()
                        GL11.glTranslated(point.x - renderPosX, point.y - renderPosY, point.z - renderPosZ)
                        GL11.glScalef(circleScale * 0.7f, circleScale * 0.7f, circleScale * 0.7f)
                        GL11.glCallList(sphereList)
                        GL11.glPopMatrix()

                        RenderUtils.glColor(point.color, alpha)
                        GL11.glPushMatrix()
                        GL11.glTranslated(point.x - renderPosX, point.y - renderPosY, point.z - renderPosZ)
                        GL11.glScalef(circleScale * 0.4f, circleScale * 0.4f, circleScale * 0.4f)
                        GL11.glCallList(sphereList)
                        GL11.glPopMatrix()

                    }
                    "sphere" -> {
                        GL11.glPushMatrix()
                        GL11.glTranslated(point.x - renderPosX, point.y - renderPosY, point.z - renderPosZ)
                        GL11.glScalef(sphereScaleValue.get(), sphereScaleValue.get(), sphereScaleValue.get())
                        GL11.glCallList(sphereList)
                        GL11.glPopMatrix()
                    }
                }
            }
            when(typeValue.get().lowercase()) {
                "line" -> {
                    GL11.glEnd()
                    GL11.glDisable(GL11.GL_LINE_SMOOTH)
                }
                "rect" -> {
                    GL11.glEnable(GL11.GL_CULL_FACE)
                }
            }
        }
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glPopMatrix()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // Clear points for entities that no longer exist.
        // Removing while iterating threw ConcurrentModificationException every tick.
        val world = mc.theWorld
        if (world != null) {
            points.keys.filter { world.getEntityByID(it) == null }.forEach(points::remove)
        }
        // add new points
        if(mc.thePlayer.ticksExisted % precisionValue.get() != 0) {
            return // skip if not on tick
        }
        if(drawTargetsValue.get()) {
            mc.theWorld.loadedEntityList.forEach {
                if(EntityUtils.isSelected(it, true)) {
                    updatePoints(it as EntityLivingBase)
                }
            }
        }
        if(drawThePlayerValue.get()) {
            updatePoints(mc.thePlayer)
        }
    }

    private fun updatePoints(entity: EntityLivingBase) {
        (points[entity.entityId] ?: mutableListOf<BreadcrumbPoint>().also { points[entity.entityId] = it })
            .add(BreadcrumbPoint(entity.posX, entity.entityBoundingBox.minY, entity.posZ, System.currentTimeMillis(), color.rgb))
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        points.clear()
    }

    override fun onDisable() {
        points.clear()
    }

    class BreadcrumbPoint(val x: Double, val y: Double, val z: Double, val time: Long, val color: Int)
}