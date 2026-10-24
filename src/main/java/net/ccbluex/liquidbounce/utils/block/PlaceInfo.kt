/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3

class PlaceInfo(
    val blockPos: BlockPos,
    val enumFacing: EnumFacing,
    var vec3: Vec3 = Vec3(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5)
) {

    companion object {

        /**
         * Allows you to find a specific place info for your [pos]
         *
         * Same as FDPClient: walk the facings in vanilla order, never place on top of the block above,
         * and hand back the neighbouring block together with the face that points back at [pos].
         */
        fun get(pos: BlockPos): PlaceInfo? = EnumFacing.values().find {
            it != EnumFacing.UP && pos.offset(it).canBeClicked()
        }?.let { side -> PlaceInfo(pos.offset(side), side.opposite) }
    }
}