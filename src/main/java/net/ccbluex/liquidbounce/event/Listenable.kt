/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 *
 * Unified Listenable: supports BOTH the legacy @EventTarget reflection system and the new
 * handler { } DSL (action-based EventHook). The legacy reflection hook is kept under the name
 * MethodHook to avoid colliding with the action-based EventHook class.
 */
package net.ccbluex.liquidbounce.event

import kotlin.reflect.KProperty

interface Listenable {
    fun handleEvents(): Boolean = parent?.handleEvents() ?: true

    val subListeners: Array<Listenable>
        get() = emptyArray()

    val parent: Listenable?
        get() = null
}

/**
 * Registers a [handler] for the event type [T] on this [Listenable]. The hook is stored in the global
 * [EventManager] registry and is dispatched by [EventManager.callEvent], gated by [Listenable.handleEvents]
 * unless [always] is true.
 */
inline fun <reified T : Event> Listenable.handler(
    always: Boolean = false,
    priority: Byte = 0,
    noinline action: (T) -> Unit
) {
    EventManager.registerEventHook(T::class.java, EventHook(this, always, priority, action))
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class EventTarget(val ignoreCondition: Boolean = false, val priority: Int = 0)

/**
 * Legacy reflection-based event hook (an @EventTarget method on a [Listenable]). Kept separate from the
 * action-based [EventHook] to avoid a name clash in this package.
 */
internal class MethodHook(val eventClass: Listenable, val method: java.lang.reflect.Method, val priority: Int, eventTarget: EventTarget) {
    val isIgnoreCondition = eventTarget.ignoreCondition

    constructor(eventClass: Listenable, method: java.lang.reflect.Method, eventTarget: EventTarget) : this(
        eventClass,
        method,
        0,
        eventTarget
    )
}
