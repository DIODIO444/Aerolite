/*
 * Aerolite — FDPClient framework port
 * IMixinEntity interface injected into net.minecraft.entity.Entity via MixinEntity.
 */
package net.ccbluex.liquidbounce.injection.implementations

interface IMixinEntity {
    var lerpX: Double
    var lerpY: Double
    var lerpZ: Double
    var trueX: Double
    var trueY: Double
    var trueZ: Double
    var truePos: Boolean
}
