/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category

/**
 * Cancels vanilla view bobbing.
 *
 * The old implementation only forced `distanceWalkedModified` to 0 every tick, which still left a
 * residual offset (`-abs(cameraYaw)` translate plus the `cameraPitch` rotation) and also silenced
 * footstep sounds. `MixinEntityRenderer#cancelViewBobbing` now skips `setupViewBobbing` outright —
 * that covers both the world camera and the first person hand (which is rendered from `renderHand`
 * under the same bobbed model-view matrix).
 */
object NoBob : Module("NoBob", Category.RENDER)
