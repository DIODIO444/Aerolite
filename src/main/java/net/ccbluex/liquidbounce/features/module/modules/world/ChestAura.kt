/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.config.*

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.float
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockPos

@ModuleInfo(name = "ChestAura", category = ModuleCategory.WORLD, subCategory = Category.SubCategory.MISCELLANEOUS)
class ChestAura : Module() {

    private val range by float("Range", 5f, 1f..10f)
    private val delay by float("Delay", 100f, 0f..1000f)
    private val throughWalls by boolean("ThroughWalls", true)
    private val chestValue by boolean("Chest", true)
    private val enderChestValue by boolean("EnderChest", false)

    companion object {
        /**
         * The chest tile entity currently being targeted by the aura, or `null` when inactive.
         * Written by [net.ccbluex.liquidbounce.utils.inventory.InventoryUtils].
         */
        var tileTarget: TileEntity? = null

        /**
         * Set of chest positions that have already been opened/clicked, used by StorageESP to skip
         * recolouring them.
         */
        var clickedBlocks: MutableSet<BlockPos> = mutableSetOf()
    }

    override fun handleEvents() = state
}
