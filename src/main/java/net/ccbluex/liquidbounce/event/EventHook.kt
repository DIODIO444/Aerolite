/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

/**
 * Action-based event hook created by the [handler] DSL. Wraps a `(T) -> Unit` action with an owning
 * [Listenable], an [always] flag and a [priority].
 */
class EventHook<T : Event>(
    val owner: Listenable,
    val always: Boolean = false,
    val priority: Byte = 0,
    private val action: (T) -> Unit
) {
    val isActive: Boolean
        get() = this.owner.handleEvents() || this.always

    internal fun processEvent(event: T) {
        if (!this.isActive) {
            return
        }

        try {
            action(event)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
