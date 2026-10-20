/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.floor

object BlockUtils : MinecraftInstance() {

    /**
     * Get block from [blockPos]
     */
    @JvmStatic
    fun getBlock(vec3: Vec3?): Block? = getBlock(BlockPos(vec3))

    @JvmStatic
    fun getBlock(blockPos: BlockPos?): Block? = mc.theWorld?.getBlockState(blockPos)?.block

    /**
     * Get material from [blockPos]
     */
    @JvmStatic
    fun getMaterial(blockPos: BlockPos?): Material? = getBlock(blockPos)?.material

    /**
     * Check [blockPos] is replaceable
     */
    @JvmStatic
    fun isReplaceable(blockPos: BlockPos?) = getMaterial(blockPos)?.isReplaceable ?: false

    /**
     * Get state from [blockPos]
     */
    @JvmStatic
    fun getState(blockPos: BlockPos?): IBlockState = mc.theWorld.getBlockState(blockPos)

    /**
     * Check if [blockPos] is clickable
     */
    @JvmStatic
    fun canBeClicked(blockPos: BlockPos?) = getBlock(blockPos)?.canCollideCheck(getState(blockPos), false) ?: false &&
            mc.theWorld.worldBorder.contains(blockPos)

    /**
     * Get block name by [id]
     */
    @JvmStatic
    fun getBlockName(id: Int): String = Block.getBlockById(id).localizedName

    /**
     * Check if block is full block
     */
    /**
     * Check if [block] is a full (1x1x1) cube.
     */
    @JvmStatic
    fun isFullBlock(block: Block?): Boolean = block != null && block.isFullCube

    @JvmStatic
    fun isFullBlock(blockPos: BlockPos?): Boolean {
        val axisAlignedBB = getBlock(blockPos)?.getCollisionBoundingBox(mc.theWorld, blockPos, getState(blockPos))
                ?: return false
        return axisAlignedBB.maxX - axisAlignedBB.minX == 1.0 && axisAlignedBB.maxY - axisAlignedBB.minY == 1.0 && axisAlignedBB.maxZ - axisAlignedBB.minZ == 1.0
    }

    /**
     * Get distance to center of [blockPos]
     */
    @JvmStatic
    fun getCenterDistance(blockPos: BlockPos) =
            mc.thePlayer.getDistance(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5)

    /**
     * Search blocks around the player in a specific [radius]
     */
    @JvmStatic
    fun searchBlocks(radius: Int): Map<BlockPos, Block> {
        val blocks = mutableMapOf<BlockPos, Block>()

        for (x in radius downTo -radius + 1) {
            for (y in radius downTo -radius + 1) {
                for (z in radius downTo -radius + 1) {
                    val blockPos = BlockPos(mc.thePlayer.posX.toInt() + x, mc.thePlayer.posY.toInt() + y,
                            mc.thePlayer.posZ.toInt() + z)
                    val block = getBlock(blockPos) ?: continue

                    blocks[blockPos] = block
                }
            }
        }

        return blocks
    }

    /**
     * FDPClient-compatible block search with optional filtering.
     *
     * @param targetBlocks only these blocks are collected (null = any block)
     * @param maxBlocksLimit stop after this many blocks were found
     * @param predicate extra per-block filter
     */
    fun searchBlocks(
        radius: Int,
        targetBlocks: Set<Block>? = null,
        maxBlocksLimit: Int? = null,
        predicate: (BlockPos, Block) -> Boolean = { _, _ -> true }
    ): MutableMap<BlockPos, Block> {
        val thePlayer = mc.thePlayer ?: return mutableMapOf()

        val blocks = mutableMapOf<BlockPos, Block>()

        val mutable = BlockPos.MutableBlockPos(0, 0, 0)
        for (x in radius downTo -radius + 1) {
            for (y in radius downTo -radius + 1) {
                for (z in radius downTo -radius + 1) {
                    if (maxBlocksLimit != null && blocks.size >= maxBlocksLimit) {
                        return blocks
                    }

                    mutable.set(thePlayer.posX.toInt() + x, thePlayer.posY.toInt() + y, thePlayer.posZ.toInt() + z)

                    val block = mutable.block ?: continue

                    if (targetBlocks == null || targetBlocks.contains(block)) {
                        val pos = BlockPos(mutable.x, mutable.y, mutable.z)
                        if (predicate(pos, block)) {
                            blocks[pos] = block
                        }
                    }
                }
            }
        }

        return blocks
    }

    /**
     * Check if [axisAlignedBB] has collidable blocks using custom [collide] check
     */
    @JvmStatic
    fun collideBlock(axisAlignedBB: AxisAlignedBB, collide: (Block?) -> Boolean): Boolean {
        for (x in MathHelper.floor_double(mc.thePlayer.entityBoundingBox.minX) until
                MathHelper.floor_double(mc.thePlayer.entityBoundingBox.maxX) + 1) {
            for (z in MathHelper.floor_double(mc.thePlayer.entityBoundingBox.minZ) until
                    MathHelper.floor_double(mc.thePlayer.entityBoundingBox.maxZ) + 1) {
                val block = getBlock(BlockPos(x.toDouble(), axisAlignedBB.minY, z.toDouble()))

                if (!collide(block)) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Check if [axisAlignedBB] has collidable blocks using custom [collide] check
     */
    @JvmStatic
    fun collideBlockIntersects(axisAlignedBB: AxisAlignedBB, collide: (Block?) -> Boolean): Boolean {
        for (x in MathHelper.floor_double(mc.thePlayer.entityBoundingBox.minX) until
                MathHelper.floor_double(mc.thePlayer.entityBoundingBox.maxX) + 1) {
            for (z in MathHelper.floor_double(mc.thePlayer.entityBoundingBox.minZ) until
                    MathHelper.floor_double(mc.thePlayer.entityBoundingBox.maxZ) + 1) {
                val blockPos = BlockPos(x.toDouble(), axisAlignedBB.minY, z.toDouble())
                val block = getBlock(blockPos)

                if (collide(block)) {
                    val boundingBox = block?.getCollisionBoundingBox(mc.theWorld, blockPos, getState(blockPos))
                            ?: continue

                    if (mc.thePlayer.entityBoundingBox.intersectsWith(boundingBox)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    @JvmStatic
    fun floorVec3(vec3: Vec3) = Vec3(floor(vec3.xCoord), floor(vec3.yCoord), floor(vec3.zCoord))

    /**
     * Check if the collision bounding box of [blockPos] is valid (full block or, optionally, a
     * partial block such as a slab / stair that can still be built upon).
     */
    fun isBlockBBValid(
        blockPos: BlockPos,
        blockState: IBlockState? = null,
        supportSlabs: Boolean = false,
        supportPartialBlocks: Boolean = false
    ): Boolean {
        val state = blockState ?: getBlock(blockPos)?.let { mc.theWorld?.getBlockState(blockPos) } ?: return false

        val box = state.block.getCollisionBoundingBox(mc.theWorld, blockPos, state) ?: return false

        // Support blocks like stairs, slab (1x), dragon-eggs, glass-panes, fences, etc
        if (supportPartialBlocks && (box.maxY - box.minY < 1.0 || box.maxX - box.minX < 1.0 || box.maxZ - box.minZ < 1.0)) {
            return true
        }

        // The slab will only return true if it's placed at a level that can be placed like any normal full block
        return box.maxX - box.minX == 1.0 && (box.maxY - box.minY == 1.0 || supportSlabs && box.maxY % 1.0 == 0.0) && box.maxZ - box.minZ == 1.0
    }
}