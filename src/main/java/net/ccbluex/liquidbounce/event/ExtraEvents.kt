/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

/**
 * Event classes referenced by the ported FDPClient modules but not present in this client's core.
 * They are defined here so the `handler { }` references compile; some are fired by the rest of the
 * codebase, others are currently inert.
 */

class GameLoopEvent : Event()

class GameTickEvent : Event()

/**
 * Fired around `EntityPlayer.onUpdate` — [state] is [EventState.PRE] before the tick and
 * [EventState.POST] after it. Cancelling a PRE tick skips the player tick.
 */
class PlayerTickEvent(val state: EventState = EventState.PRE) : CancellableEvent()

class RotationUpdateEvent : Event()

class PostSprintUpdateEvent : Event()

/**
 * Fired right after a successful step-up, once the player's bounding box has been raised.
 * Used by the Step module to apply its timer/jump adjustments.
 */
class StepConfirmEvent : Event()

class DelayedPacketProcessEvent : CancellableEvent()

