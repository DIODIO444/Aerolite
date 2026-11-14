package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.cape.GuiCapeManager

/**
 * Aerolite's CapeManager entry: a "button" module that opens the local cape selection screen.
 */
object CapeManager : Module("CapeManager", Category.MISC, canBeEnabled = false) {

    override fun onEnable() {
        mc.displayGuiScreen(GuiCapeManager)
    }
}
