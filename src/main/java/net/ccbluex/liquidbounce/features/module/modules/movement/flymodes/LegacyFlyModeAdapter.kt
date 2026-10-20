/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 *
 * Bridges the legacy `movement.flys` fly modes (old Aerolite framework) into the new
 * `movement.flymodes` framework, so a single "Fly" module can offer both mode sets.
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.event.TeleportEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.value.Value
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode as LegacyFlyMode

/**
 * Exposes a legacy [LegacyFlyMode] through the new [FlyMode] interface.
 *
 * The legacy modes also have their own settings ([values]) and react to strafe/teleport events,
 * which the merged "Fly" module forwards explicitly.
 */
class LegacyFlyModeAdapter(val legacy: LegacyFlyMode) : FlyMode(legacy.modeName) {

    /** Settings declared by the legacy mode, exposed to the module's value list. */
    val values: List<Value<*>>
        get() = legacy.values

    override fun onEnable() = legacy.onEnable()

    override fun onDisable() = legacy.onDisable()

    override fun onUpdate() = legacy.onUpdate(UpdateEvent())

    override fun onMove(event: MoveEvent) = legacy.onMove(event)

    override fun onPacket(event: PacketEvent) = legacy.onPacket(event)

    override fun onBB(event: BlockBBEvent) = legacy.onBlockBB(event)

    override fun onJump(event: JumpEvent) = legacy.onJump(event)

    override fun onStep(event: StepEvent) = legacy.onStep(event)

    override fun onMotion(event: MotionEvent) = legacy.onMotion(event)

    /** Not part of the new [FlyMode] API — dispatched by the module's StrafeEvent handler. */
    fun dispatchStrafe(event: StrafeEvent) = legacy.onStrafe(event)

    /** Not part of the new [FlyMode] API — dispatched by the module's TeleportEvent handler. */
    fun dispatchTeleport(event: TeleportEvent) = legacy.onTeleport(event)
}
