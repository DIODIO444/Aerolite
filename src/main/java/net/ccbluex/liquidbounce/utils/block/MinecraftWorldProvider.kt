/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.minecraft.block.material.Material
import net.minecraft.util.BlockPos
import net.minecraft.world.World

/**
 * Adapts the Minecraft [World] to the pathfinding API used by
 * [net.ccbluex.liquidbounce.utils.pathfinding.PathUtils].
 */
class MinecraftWorldProvider(val world: World) {

    /**
     * Returns `true` when the block at the given coordinates would obstruct movement (i.e. is a
     * solid, non-passable block).
     */
    fun isBlocked(x: Int, y: Int, z: Int): Boolean {
        val block = world.getBlockState(BlockPos(x, y, z)).block
        return block.material !== Material.air && block.material.blocksMovement()
    }
}
