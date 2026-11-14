package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newVer.element.module.value.impl

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newVer.element.components.Checkbox
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newVer.element.module.value.ValueElement
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.config.BoolValue

import java.awt.Color

class BooleanElement(value: BoolValue): ValueElement<Boolean>(value) {
    private val checkbox = Checkbox()

    override fun drawElement(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, bgColor: Color, accentColor: Color): Float {
        checkbox.state = value.get()
        checkbox.onDraw(x + 10F, y + 5F, 10F, 10F, bgColor, accentColor)
        // Centre the label on the checkbox centre (y + 10). The old "+ 2F" nudge came from a build
        // whose font40 was taller; with the current Outfit metrics it pushed the text 2px too low.
        Fonts.font40.drawString(value.name, x + 25F, y + 10F - Fonts.font40.FONT_HEIGHT / 2F, -1)
        return valueHeight
    }

    override fun onClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {
        if (isDisplayable() && ClientUtils.mouseWithinBounds(mouseX, mouseY, x, y, x + width, y + 20F))
            value.set(!value.get())
    }
}