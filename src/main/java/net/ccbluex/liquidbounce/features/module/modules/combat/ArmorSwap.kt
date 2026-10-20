/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 *
 * NOTE: the full FDPClient ArmorSwap depends on its coroutine-based inventory scheduling
 * framework. Only the API used by InventoryManager is provided for now.
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "ArmorSwap", category = ModuleCategory.COMBAT)
object ArmorSwap : Module() {

    init {
        subCategory = Category.SubCategory.COMBAT_LEGIT
    }

    /**
     * Runs one pending manual armor swap, if any.
     *
     * @return `true` when a swap was performed and the inventory manager should pause for a tick.
     */
    fun runSwap(): Boolean = false
}
