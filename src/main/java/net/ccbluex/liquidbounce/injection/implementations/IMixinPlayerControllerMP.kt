/*
 * Aerolite — FDPClient framework port
 * Interface injected into net.minecraft.client.multiplayer.PlayerControllerMP via
 * MixinPlayerControllerMP, exposing otherwise-private members used by migrated code.
 */
package net.ccbluex.liquidbounce.injection.implementations

interface IMixinPlayerControllerMP {
    fun invokeSyncCurrentPlayItem()
}
