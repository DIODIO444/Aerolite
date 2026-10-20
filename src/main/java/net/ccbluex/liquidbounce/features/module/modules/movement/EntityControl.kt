/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.config.float
import net.ccbluex.liquidbounce.config.multiSelect

/** Lets the local player steer rideable 1.8.9 entities without their normal equipment checks. */
object EntityControl : Module(
    "EntityControl",
    Category.MOVEMENT,
    Category.SubCategory.MOVEMENT_EXTRAS,
) {
    private val enforce by multiSelect(
        "Enforce",
        arrayOf("Saddled", "JumpStrength"),
    ).describe("Vanilla mount restrictions that should be overridden.")

    private val jumpStrength by float("JumpStrength", 1f, 0.1f..2f) { "JumpStrength" in enforce }
        .describe("Horse jump strength while the override is active.")

    @JvmStatic
    val enforceSaddled: Boolean
        get() = handleEvents() && "Saddled" in enforce

    @JvmStatic
    val enforceJumpStrength: Boolean
        get() = handleEvents() && "JumpStrength" in enforce

    @JvmStatic
    val controlledJumpStrength: Double
        get() = jumpStrength.toDouble()
}
