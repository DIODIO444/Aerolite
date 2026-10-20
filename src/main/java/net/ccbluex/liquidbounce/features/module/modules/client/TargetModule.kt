/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.config.*

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "Target", category = ModuleCategory.CLIENT, canEnable = false)
object TargetModule : Module() {

    init {
        subCategory = Category.SubCategory.CLIENT_GENERAL
    }

    var playerValue by boolean("Player", true)
        .describe("Allow players to be targeted.")
    var friendValue by boolean("Friend", false)
        .describe("Allow friends to be targeted, even when Player is disabled.")
    var animalValue by boolean("Animal", true)
        .describe("Allow animals to be targeted.")
    var mobValue by boolean("Mob", true)
        .describe("Allow hostile mobs to be targeted.")
    var invisibleValue by boolean("Invisible", false)
        .describe("Allow invisible entities to be targeted.")
    var deadValue by boolean("Dead", false)
        .describe("Allow dead entities to be targeted.")

    override fun handleEvents() = true
}
