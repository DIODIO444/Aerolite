package net.ccbluex.liquidbounce.ui.client.gui.newVer.element.module.value.impl

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.newVer.element.module.value.ValueElement
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.Value

import java.awt.Color

/**
 * Collapsible header for a `Configurable` group (the `ConfigurableImpl` containers created by the
 * new value DSL, e.g. KillAura's `RotationSettings` / `AutoBlocking`).
 *
 * Only created when `ClickGUIModule.groupSettings` is enabled; the children are drawn below the
 * header with a small indent.
 */
class GroupElement(
    private val group: Configurable,
    private val children: List<ValueElement<*>>,
) : ValueElement<MutableList<Value<*>>>(group as Value<MutableList<Value<*>>>) {

    companion object {
        private const val HEADER_HEIGHT = 16F
        private const val INDENT = 8F
    }

    var expanded = true

    private val visibleChildren: List<ValueElement<*>>
        get() = children.filter { it.isDisplayable() }

    override val height: Float
        get() {
            if (!expanded) return HEADER_HEIGHT

            var total = HEADER_HEIGHT
            for (child in visibleChildren) total += child.height
            return total
        }

    override fun isDisplayable(): Boolean = true

    override fun drawElement(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, bgColor: Color, accentColor: Color): Float {
        val textY = y + HEADER_HEIGHT / 2F - Fonts.font40.FONT_HEIGHT / 2F + 2F

        Fonts.font40.drawString(if (expanded) "v" else ">", x + 10F, textY, accentColor.rgb)
        Fonts.font40.drawString(group.name, x + 22F, textY, accentColor.rgb)
        RenderUtils.newDrawRect(x + 10F, y + HEADER_HEIGHT - 1F, x + width - 10F, y + HEADER_HEIGHT - 0.5F, accentColor.rgb)

        if (expanded) {
            var childY = y + HEADER_HEIGHT
            for (child in visibleChildren) {
                childY += child.drawElement(mouseX, mouseY, x + INDENT, childY, width - INDENT, bgColor, accentColor)
            }
        }

        return height
    }

    override fun onClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {
        if (ClientUtils.mouseWithinBounds(mouseX, mouseY, x, y, x + width, y + HEADER_HEIGHT)) {
            expanded = !expanded
            return
        }

        if (!expanded) return

        var childY = y + HEADER_HEIGHT
        for (child in visibleChildren) {
            child.onClick(mouseX, mouseY, x + INDENT, childY, width - INDENT)
            childY += child.height
        }
    }

    override fun onRelease(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {
        if (!expanded) return

        var childY = y + HEADER_HEIGHT
        for (child in visibleChildren) {
            child.onRelease(mouseX, mouseY, x + INDENT, childY, width - INDENT)
            childY += child.height
        }
    }

    override fun onKeyPress(typed: Char, keyCode: Int): Boolean {
        if (!expanded) return false

        return visibleChildren.any { it.onKeyPress(typed, keyCode) }
    }
}
