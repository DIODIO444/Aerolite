/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.objects.Drag
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.render.Scroll
import net.minecraft.util.ResourceLocation

enum class Category(val displayName: String) {

    COMBAT("Combat"),
    PLAYER("Player"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    WORLD("World"),
    MISC("Misc"),
    EXPLOIT("Exploit"),
    FUN("Fun");

    val iconResourceLocation = ResourceLocation("liquidbounce/tabgui/${name.lowercase()}.png")

    /* ------------------------------------------------------------------ *
     *  Panel state for the ported Tenacity (newdropdown) click GUI.       *
     *  Aerolite stored this on its ModuleCategory enum as well.           *
     * ------------------------------------------------------------------ */

    private var guiDrag: Drag? = null
    private var guiScroll: Scroll? = null

    fun getScroll(): Scroll = guiScroll ?: Scroll().also { guiScroll = it }

    fun getDrag(): Drag = guiDrag ?: createDrag().also { guiDrag = it }

    fun getDragNew(category: Category): Drag = category.getDrag()

    private fun createDrag(): Drag {
        val x = when (this) {
            COMBAT -> 5f
            PLAYER -> 125f
            MOVEMENT -> 245f
            RENDER -> 365f
            WORLD -> 605f
            MISC -> 725f
            EXPLOIT -> 845f
            FUN -> 965f
        }
        return Drag(x, 20f)
    }

}
