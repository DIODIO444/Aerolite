/*
 * Aerolite rendering engine — theme bridge.
 *
 * The ported render helpers used to pull their accent colours and animation clock from
 * FDPClient's CombatVisuals module and ClientThemesUtils. This project has no such module,
 * so the values live here and can be driven by any UI/theme code that wants to.
 */
package net.ccbluex.liquidbounce.utils.render

import java.awt.Color

object RenderTheme {

    const val DOUBLE_PI = Math.PI * 2

    /** Main accent colour used by the visual effects. */
    @JvmField
    var colorPrimary: Color = Color(0x3F, 0x9C, 0xFF)

    /** Secondary accent used for gradients. */
    @JvmField
    var colorSecondary: Color = Color(0xFF, 0x6B, 0xC4)

    /** Global animation clock (degrees) shared by the rotating effects. */
    @JvmField
    var start: Double = 0.0

    /** Theme palette accessor, index 0 = primary, 1 = secondary, then alternating. */
    @JvmStatic
    fun getColor(index: Int = 1): Color = if (index % 2 == 0) colorSecondary else colorPrimary
}

/** Normalised ease-in-out quad, kept for the ported entity effects. */
fun easeInOutQuadX(x: Double): Double {
    return if (x < 0.5) 2 * x * x else 1 - Math.pow(-2 * x + 2, 2.0) / 2
}
