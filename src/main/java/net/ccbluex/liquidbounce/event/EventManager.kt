/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 *
 * Unified event bus.
 *  - Keeps the legacy reflection-based dispatch (registerListener / @EventTarget / MethodHook) used by the
 *    existing modules.
 *  - Adds the action-based dispatch (registerEventHook / EventHook) used by the new handler { } DSL ported
 *    from newer FDPClient.
 *
 * callEvent(event) dispatches to BOTH systems, so both old and new modules receive every fired event
 * without any change to the existing game-side call sites (LiquidBounce.eventManager.callEvent(event)).
 */
package net.ccbluex.liquidbounce.event

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class EventManager : MinecraftInstance() {

    // ---- Legacy reflection registry ----
    private val registry = HashMap<Class<out Event>, MutableList<MethodHook>>()

    fun registerListener(listener: Listenable) {
        for (method in listener.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(EventTarget::class.java) && method.parameterTypes.size == 1) {
                try {
                    if (!method.isAccessible) {
                        method.isAccessible = true
                    }

                    val eventClass = method.parameterTypes[0] as Class<out Event>
                    val eventTarget = method.getAnnotation(EventTarget::class.java)

                    val invokableEventTargets = registry.getOrDefault(eventClass, ArrayList())
                    invokableEventTargets.add(MethodHook(listener, method, eventTarget))
                    registry[eventClass] = invokableEventTargets
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }

    fun unregisterListener(listenable: Listenable) {
        for ((key, targets) in registry) {
            targets.removeIf { it.eventClass == listenable }

            registry[key] = targets
        }
    }

    /**
     * Call event to listeners (legacy reflection) AND new action hooks.
     */
    fun callEvent(event: Event) {
        // 1. Legacy reflection dispatch
        val targets = registry[event.javaClass]?.toMutableList() ?: mutableListOf()
        for (invokableEventTarget in targets) {
            try {
                if (!invokableEventTarget.eventClass.handleEvents() && !invokableEventTarget.isIgnoreCondition) {
                    continue
                }

                invokableEventTarget.method.invoke(invokableEventTarget.eventClass, event)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }

        // 2. New action-hook dispatch (handler { } DSL)
        EventManager.dispatchHooks(event)
    }

    /**
     * FDPClient-compatible instance entry point: dispatches [event] to the action hooks and returns it.
     */
    fun call(event: Event): Event = EventManager.call(event)

    /**
     * FDPClient-compatible instance entry point: dispatches [event] only to [listener]'s hooks.
     */
    fun <T : Event> call(event: T, listener: Listenable): T = EventManager.call(event, listener)

    // ---- New action-hook registry (global singleton shared with the single instance) ----
    companion object {
        private val hooks = HashMap<Class<out Event>, MutableList<EventHook<in Event>>>()

        @Synchronized
        fun <T : Event> registerEventHook(eventClass: Class<out T>, eventHook: EventHook<T>): EventHook<T> {
            val container = hooks.getOrPut(eventClass) { CopyOnWriteArrayList() }

            @Suppress("UNCHECKED_CAST")
            val hook = eventHook as EventHook<in Event>

            if (hook !in container) {
                container.add(hook)
            }

            return eventHook
        }

        @Synchronized
        fun <T : Event> unregisterEventHook(eventClass: Class<out T>, eventHook: EventHook<T>): Boolean =
            hooks[eventClass]?.remove(eventHook as EventHook<in Event>) ?: false

        internal fun dispatchHooks(event: Event) {
            hooks[event.javaClass]?.forEach { it.processEvent(event) }
        }

        /**
         * FDPClient compatible entry point: dispatches [event] to all registered hooks and returns it,
         * so callers can inspect `.isCancelled` (e.g. `EventManager.call(DelayedPacketProcessEvent()).isCancelled`).
         */
        fun call(event: Event): Event {
            dispatchHooks(event)
            return event
        }

        /**
         * FDPClient compatible: dispatches [event] only to the hooks owned by [listener]
         * (used by PacketUtils to route a delayed packet through a single module such as FakeLag/Velocity).
         */
        fun <T : Event> call(event: T, listener: Listenable): T {
            hooks[event.javaClass]?.forEach {
                if (it.owner === listener) {
                    it.processEvent(event)
                }
            }
            return event
        }

        // ---- Coroutine scope (FDPClient: EventManager itself is a CoroutineScope) ----
        private val eventScope = CoroutineScope(SupervisorJob())

        /**
         * FDPClient compatible coroutine builder used by `event.async` extensions
         * (`launchSequence` / `loopSequence`).
         */
        fun launch(
            dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
            block: suspend CoroutineScope.() -> Unit
        ): Job = eventScope.launch(dispatcher) { block() }
    }
}
