/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo

/**
 * Locks the player's selected hotbar slot so other modules / server slot resets cannot move it.
 * Used by [net.ccbluex.liquidbounce.utils.inventory.InventoryUtils] together with [net.ccbluex.liquidbounce.event.async.TickScheduler].
 */
@ModuleInfo(name = "NoSlotSet", category = ModuleCategory.PLAYER, subCategory = Category.SubCategory.MISCELLANEOUS)
object NoSlotSet : Module() {

    override fun handleEvents() = state
}
