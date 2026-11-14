//lb+
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Ported from Aerolite (`movement/NewTargetStrafe.kt`).
 */
object NewTargetStrafe : Module("TargetStrafe2", Category.MOVEMENT) {

    val radius by float("Radius", 2.0f, 0.1f..4.0f)
    private val render by boolean("Render", true)
    private val alwaysRender by boolean("Always-Render", true)
    private val modeValue by choices("KeyMode", arrayOf("Jump", "None"), "None")
    private val safewalk by boolean("SafeWalk", true)
    val thirdPerson by boolean("ThirdPerson", true)
    private val colorType by choices("Color", arrayOf("Custom", "Dynamic", "Rainbow", "Rainbow2", "Sky", "Fade"), "Custom")
    private val redValue by int("Red", 255, 0..255)
    private val greenValue by int("Green", 255, 0..255)
    private val blueValue by int("Blue", 255, 0..255)
    private val saturationValue by float("Saturation", 0.7F, 0F..1F)
    private val brightnessValue by float("Brightness", 1F, 0F..1F)
    private val accuracyValue by int("Accuracy", 0, 0..59)
    private val thicknessValue by float("Thickness", 1F, 0.1F..5F)
    private val outLine by boolean("Outline", true)
    private val expMode by boolean("ExperimentalSpeed", false)

    var direction: Int = 1
    var lastView: Int = 0
    var hasChangedThirdPerson: Boolean = true

    override fun onEnable() {
        hasChangedThirdPerson = true
        lastView = mc.gameSettings.thirdPersonView
    }

    private val onMotion = handler<MotionEvent> { event ->
        if (thirdPerson) { // smart change back lol
            if (canStrafe) {
                if (hasChangedThirdPerson) lastView = mc.gameSettings.thirdPersonView
                mc.gameSettings.thirdPersonView = 1
                hasChangedThirdPerson = false
            } else if (!hasChangedThirdPerson) {
                mc.gameSettings.thirdPersonView = lastView
                hasChangedThirdPerson = true
            }
        }

        if (event.eventState == EventState.PRE) {
            if (mc.thePlayer.isCollidedHorizontally) this.direction = -this.direction

            if (mc.gameSettings.keyBindLeft.isPressed) this.direction = 1
            if (mc.gameSettings.keyBindRight.isPressed) this.direction = -1
        }
    }

    private val onMove = handler<MoveEvent> { event ->
        if (canStrafe) {
            strafe(event, MovementUtils.getSpeed2(event.x, event.z))

            if (safewalk && checkVoid()) event.isSafeWalk = true
        }
    }

    fun strafe(event: MoveEvent, moveSpeed: Double) {
        val target = KillAura.target ?: return
        val rotYaw = RotationUtils.getRotationsEntity(target).yaw

        val forward = if (mc.thePlayer.getDistanceToEntity(target) <= radius) 0.0 else 1.0
        val modifySpeed = if (expMode) maximizeSpeed(target, moveSpeed) else moveSpeed

        MovementUtils.setSpeed(event, modifySpeed, rotYaw, direction.toDouble(), forward)
    }

    private fun maximizeSpeed(ent: EntityLivingBase, speed: Double): Double = speed.coerceIn(0.0, radius.toDouble())

    val keyMode: Boolean
        get() = when (modeValue.lowercase()) {
            "jump" -> mc.gameSettings.keyBindJump.isKeyDown
            "none" -> mc.thePlayer.movementInput.moveStrafe != 0f || mc.thePlayer.movementInput.moveForward != 0f
            else -> false
        }

    val canStrafe: Boolean
        get() = state && (Speed.state || Fly.state) && KillAura.state && KillAura.target != null &&
                !mc.thePlayer.isSneaking && keyMode

    private fun checkVoid(): Boolean {
        for (x in -1..0) {
            for (z in -1..0) {
                if (isVoid(x, z)) return true
            }
        }
        return false
    }

    private fun isVoid(x: Int, z: Int): Boolean {
        if (mc.thePlayer.posY < 0.0) return true

        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb: AxisAlignedBB = mc.thePlayer.entityBoundingBox.offset(x.toDouble(), (-off).toDouble(), z.toDouble())
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer as Entity, bb).isEmpty()) {
                off += 2
                continue
            }
            return false
        }
        return true
    }

    private val onRender3D = handler<Render3DEvent> { _ ->
        val target = KillAura.target
        if ((canStrafe || alwaysRender) && render) {
            target ?: return@handler
            GL11.glPushMatrix()
            GL11.glTranslated(
                target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosX,
                target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosY,
                target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glRotatef(90F, 1F, 0F, 0F)

            if (outLine) {
                GL11.glLineWidth(thicknessValue + 1.25F)
                GL11.glColor3f(0F, 0F, 0F)
                GL11.glBegin(GL11.GL_LINE_LOOP)

                for (i in 0..360 step 60 - accuracyValue) {
                    GL11.glVertex2f(
                        Math.cos(i * Math.PI / 180.0).toFloat() * radius,
                        Math.sin(i * Math.PI / 180.0).toFloat() * radius
                    )
                }

                GL11.glEnd()
            }

            val rainbow2 = ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue, brightnessValue)
            val sky = ColorUtils.skyRainbow(0, brightnessValue, saturationValue, 1.0)
            val fade = ColorUtils.fade(Color(redValue, greenValue, blueValue), 0, 100)

            GL11.glLineWidth(thicknessValue)
            GL11.glBegin(GL11.GL_LINE_LOOP)

            for (i in 0..360 step 60 - accuracyValue) {
                when (colorType) {
                    "Custom" -> GL11.glColor3f(redValue / 255.0f, greenValue / 255.0f, blueValue / 255.0f)
                    "Dynamic" -> if (canStrafe) GL11.glColor4f(0.25f, 1f, 0.25f, 1f) else GL11.glColor4f(1f, 1f, 1f, 1f)
                    "Rainbow" -> {
                        val rainbow = Color(ColorUtils.getRainbowOpaque(1, saturationValue, brightnessValue, i))
                        GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                    }
                    "Rainbow2" -> GL11.glColor3f(rainbow2.red / 255.0f, rainbow2.green / 255.0f, rainbow2.blue / 255.0f)
                    "Sky" -> GL11.glColor3f(sky.red / 255.0f, sky.green / 255.0f, sky.blue / 255.0f)
                    else -> GL11.glColor3f(fade.red / 255.0f, fade.green / 255.0f, fade.blue / 255.0f)
                }
                GL11.glVertex2f(
                    Math.cos(i * Math.PI / 180.0).toFloat() * radius,
                    Math.sin(i * Math.PI / 180.0).toFloat() * radius
                )
            }

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()

            GlStateManager.resetColor()
            GL11.glColor4f(1F, 1F, 1F, 1F)
        }
    }
}
