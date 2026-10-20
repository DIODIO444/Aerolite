/*
 * Aerolite — FDPClient framework port
 * IMixinNetworkManager interface injected into net.minecraft.network.NetworkManager via MixinNetworkManager.
 * Exposes the otherwise-private outbound dispatch members so PacketUtils can perform silent sends.
 */
package net.ccbluex.liquidbounce.injection.implementations

interface IMixinNetworkManager {
    fun flushOutboundQueue()
}
