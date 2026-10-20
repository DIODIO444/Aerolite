/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.*

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*

@ModuleInfo(name = "ForwardTrack", category = ModuleCategory.COMBAT)
object ForwardTrack : Module() {

    init {
        subCategory = Category.SubCategory.COMBAT_RAGE
    }

    private val espMode by choices("ESP-Mode", arrayOf("Box", "Model", "Wireframe"), "Model").subjective()
        .describe("How tracked positions are rendered.")
    private val wireframeWidth by float("WireFrame-Width", 1f, 0.5f..5f) { espMode == "WireFrame" }
        .describe("Line width of the wireframe ESP.")

    private val espColor = ColorSettingsInteger(this, "ESPColor") { espMode != "Model" }.with(0, 255, 0)

    val color
        get() = espColor.color()

    /**
     * Any good anti-cheat will easily detect this module.
     */
    fun includeEntityTruePos(entity: Entity, action: () -> Unit) {
        if (!handleEvents() || !isSelected(entity, true))
            return

        // Would be more fun if we simulated instead.
        Backtrack.runWithSimulatedPosition(entity, usePosition(entity)) {
            action()
        }
    }

    private fun usePosition(entity: Entity): Vec3 {
        return entity.positionVector
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val renderManager = mc.renderManager
        val world = mc.theWorld ?: return
        val partialTicks = event.partialTicks

        for (target in world.loadedEntityList) {
            if (!isSelected(target, true)) {
                continue
            }

            val vec = usePosition(target)

            val x = vec.xCoord - renderManager.renderPosX
            val y = vec.yCoord - renderManager.renderPosY
            val z = vec.zCoord - renderManager.renderPosZ

            val yaw = target.prevRotationYaw + (target.rotationYaw - target.prevRotationYaw) * partialTicks

            when (espMode.lowercase()) {
                "model" -> {
                    glPushMatrix()
                    glPushAttrib(GL_ALL_ATTRIB_BITS)

                    color(0.6f, 0.6f, 0.6f, 1f)
                    renderManager.doRenderEntity(target, x, y, z, yaw, partialTicks, true)

                    glPopAttrib()
                    glPopMatrix()
                }

                // TODO: Box / Wireframe rendering depends on drawBacktrackBox / glColor helpers.
                else -> {
                    glPushMatrix()
                    glPushAttrib(GL_ALL_ATTRIB_BITS)
                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                    glDisable(GL_TEXTURE_2D)
                    glDisable(GL_LIGHTING)
                    glDisable(GL_DEPTH_TEST)
                    glEnable(GL_LINE_SMOOTH)
                    glEnable(GL_BLEND)
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                    glLineWidth(wireframeWidth)
                    glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                    renderManager.doRenderEntity(target, x, y, z, yaw, partialTicks, true)
                    glPopAttrib()
                    glPopMatrix()
                }
            }
        }
    }
}
