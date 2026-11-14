/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.event

/**
 * Marks a method as an event listener.
 *
 * The ported Aerolite modules declare their listeners this way instead of the `handler<T>` DSL, so
 * [EventManager.registerAnnotated] scans for it on construction. The method must take exactly one
 * parameter, and that parameter must be a subclass of [Event].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class EventTarget(
    /** Lower number runs first, matching the `handler<T>` priority semantics. */
    val priority: Byte = 0
)
