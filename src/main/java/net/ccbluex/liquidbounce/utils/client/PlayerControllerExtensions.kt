/*
 * Aerolite — FDPClient framework port
 * Kotlin-visible accessor for PlayerControllerMP's private syncCurrentPlayItem,
 * routed through the IMixinPlayerControllerMP invoker mixin.
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.injection.implementations.IMixinPlayerControllerMP
import net.minecraft.client.multiplayer.PlayerControllerMP

fun PlayerControllerMP.syncCurrentPlayItem() {
    (this as IMixinPlayerControllerMP).invokeSyncCurrentPlayItem()
}
