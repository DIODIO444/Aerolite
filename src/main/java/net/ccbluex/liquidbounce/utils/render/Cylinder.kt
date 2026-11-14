/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin

/**
 * Small cone helper used by the "Sims" target mark.
 *
 * Draws a cone whose base ring (radius [radius], [sides] segments) lies in the current XY plane and
 * whose apex sits at height [radius] along +Z. The mark stacks two of these — mirrored and rotated —
 * to build the diamond that spins above the target.
 *
 * The call signature matches the original FDP implementation so the mark code could be ported as is.
 */
class Cylinder {

    /**
     * Kept for compatibility with the original mark code; the ported mark only draws filled cones.
     */
    var drawStyle: Int = 0

    /**
     * @param x      base ring centre on the X axis
     * @param y      base ring centre on the Y axis
     * @param radius base ring radius, also used as the cone height
     * @param sides  number of segments of the base ring
     * @param type   1 = filled cone, 2 = outline of the base ring
     */
    fun draw(x: Float, y: Float, radius: Float, sides: Int, type: Int) {
        val segments = sides.coerceAtLeast(3)

        if (type == 2) {
            GL11.glBegin(GL11.GL_LINE_LOOP)

            for (i in 0 until segments) {
                val angle = 2.0 * Math.PI * i / segments

                GL11.glVertex3d(x + cos(angle) * radius, y + sin(angle) * radius, 0.0)
            }

            GL11.glEnd()
            return
        }

        val height = radius.toDouble()

        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        GL11.glVertex3d(x.toDouble(), y.toDouble(), height)

        for (i in 0..segments) {
            val angle = 2.0 * Math.PI * i / segments

            GL11.glVertex3d(x + cos(angle) * radius, y + sin(angle) * radius, 0.0)
        }

        GL11.glEnd()
    }
}
