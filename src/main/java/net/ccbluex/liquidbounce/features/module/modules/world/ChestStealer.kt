/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.config.*

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.float
import net.ccbluex.liquidbounce.config.int
import net.minecraft.inventory.ContainerChest

@ModuleInfo(name = "ChestStealer", category = ModuleCategory.WORLD, subCategory = Category.SubCategory.MISCELLANEOUS)
class ChestStealer : Module() {

    val silentValue by boolean("Silent", false)
    val silentTitleValue by boolean("SilentTitle", false)
    val chestTitleValue by boolean("ChestTitle", false)

    private val delay by int("Delay", 0, 0..500)
    private val takeAllValue by boolean("TakeAll", false)

    override fun handleEvents() = state

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // Stealing is driven by InventoryManager's loop; nothing to do here.
    }

    companion object {
        /**
         * Shift-clicks every non-empty slot of the currently opened chest into the player's
         * inventory. Called from [net.ccbluex.liquidbounce.utils.inventory.InventoryManager].
         */
        fun stealFromChest() {
            val module = LiquidBounce.moduleManager.getModule(ChestStealer::class.java) ?: return
            if (!module.state) return

            val player = net.minecraft.client.Minecraft.getMinecraft().thePlayer ?: return
            val container = player.openContainer as? ContainerChest ?: return
            val pc = net.minecraft.client.Minecraft.getMinecraft().playerController ?: return

            val slots = container.inventorySlots
            // Last 36 slots are the player's own inventory; chest contents precede them.
            val chestSlots = slots.size - 36
            if (chestSlots <= 0) return

            for (i in 0 until chestSlots) {
                val slot = slots[i]
                if (slot.hasStack) {
                    pc.windowClick(container.windowId, i, 0, 1, player)
                }
            }
        }
    }
}
