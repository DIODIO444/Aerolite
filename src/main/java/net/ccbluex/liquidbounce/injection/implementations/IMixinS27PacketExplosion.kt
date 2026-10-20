/*
 * Aerolite — FDPClient framework port
 * IMixinS27PacketExplosion interface injected into net.minecraft.network.play.server.S27PacketExplosion
 * via MixinS27PacketExplosion, exposing the otherwise-private motion fields.
 */
package net.ccbluex.liquidbounce.injection.implementations

interface IMixinS27PacketExplosion {
    // 1.8.9 stores these motion components as floats.
    var field_149152_f: Float
    var field_149153_g: Float
    var field_149159_h: Float
}
