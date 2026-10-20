/*
 * Minimal compatibility holder for the combat-visual constants that render helpers import.
 * Deliberately NOT a Module so it is not auto-registered by ModuleManager's package scan.
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import java.awt.Color

object CombatVisuals {
    const val DOUBLE_PI = Math.PI * 2

    val colorPrimary: Color
        get() = Color(0, 90, 255)

    val colorSecondary: Color
        get() = Color(0, 90, 255)

    /**
     * Animation phase (degrees) advanced by render helpers each frame.
     */
    var start = 0.0
}
