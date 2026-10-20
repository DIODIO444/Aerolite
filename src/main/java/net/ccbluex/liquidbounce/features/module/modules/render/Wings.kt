package net.ccbluex.liquidbounce.features.module.modules.render

import me.stars.utils.WingUtils
import net.ccbluex.liquidbounce.config.choices
import net.ccbluex.liquidbounce.config.color
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import java.awt.Color

@ModuleInfo(name = "Wings", category = ModuleCategory.RENDER)
class Wings : Module() {

    /**
     * Wing model/texture selection, read by [net.ccbluex.liquidbounce.utils.render.RenderWings].
     */
    val wingStyle by choices("WingStyle", arrayOf("Dragon", "Simple"), "Dragon")

    /**
     * Colour source used when rendering the wings.
     */
    val colorType by choices("ColorType", arrayOf("Theme", "Custom"), "Theme")

    private val wingColor by color("Color", Color(255, 255, 255))

    /**
     * Custom wing colour, used when [colorType] is `Custom`.
     */
    val color: Color
        get() = Color(wingColor, true)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val renderWings = WingUtils()
        renderWings.renderWings(event.partialTicks)
    }
}
