/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.PushOutEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.config.multiSelect

object NoPush : Module("NoPush", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS, gameDetecting = false) {

    private val sourcesValue = multiSelect(
        "Sources",
        PushSource.values().map(PushSource::displayName).toTypedArray()
    ).describe("Choose which push sources are suppressed.")

    @JvmStatic
    fun canPush(source: PushSource): Boolean = !handleEvents() || !sourcesValue.isSelected(source.displayName)

    val onBlockPush = handler<PushOutEvent> { event ->
        if (!canPush(PushSource.BLOCKS)) {
            event.cancelEvent()
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (canPush(PushSource.SINKING)) {
            return@handler
        }

        val player = mc.thePlayer ?: return@handler

        if (mc.gameSettings.keyBindJump.isKeyDown || mc.gameSettings.keyBindSneak.isKeyDown) {
            return@handler
        }

        if ((player.isInWater || player.isInLava) && player.motionY < 0.0) {
            player.motionY = 0.0
        }
    }
}

enum class PushSource(val displayName: String) {
    ENTITIES("Entities"),
    BLOCKS("Blocks"),
    LIQUIDS("Liquids"),
    SINKING("Sinking")
}
