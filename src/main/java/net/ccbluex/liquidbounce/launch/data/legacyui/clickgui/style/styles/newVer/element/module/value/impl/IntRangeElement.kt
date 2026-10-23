package net.ccbluex.liquidbounce.ui.client.gui.newVer.element.module.value.impl

import net.ccbluex.liquidbounce.config.IntegerRangeValue
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.newVer.ColorManager
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.newVer.element.module.value.ValueElement
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

/**
 * Dual handle slider for [IntegerRangeValue] (for example KillAura's `CPS` 5..8).
 *
 * The regular [IntElement] only understands single values, so range settings used to be dropped
 * from the ClickGUI entirely.
 */
class IntRangeElement(val savedValue: IntegerRangeValue) : ValueElement<IntRange>(savedValue) {

    private var draggingStart = false
    private var draggingEnd = false

    private val minimum: Int get() = savedValue.minimum
    private val maximum: Int get() = savedValue.maximum

    private fun sliderStart(x: Float, width: Float): Float {
        val nameLength = Fonts.font40.getStringWidth(value.name)
        val maxLength = Fonts.font40.getStringWidth("$maximum")
        val minLength = Fonts.font40.getStringWidth("$minimum")
        val valueDisplay = 30F + maxLength
        val sliderWidth = (width - 50F - nameLength - maxLength - minLength - valueDisplay).coerceAtLeast(20F)

        return x + width - 20F - sliderWidth - maxLength - valueDisplay
    }

    private fun sliderWidth(x: Float, width: Float): Float {
        val nameLength = Fonts.font40.getStringWidth(value.name)
        val maxLength = Fonts.font40.getStringWidth("$maximum")
        val minLength = Fonts.font40.getStringWidth("$minimum")
        val valueDisplay = 30F + maxLength

        return (width - 50F - nameLength - maxLength - minLength - valueDisplay).coerceAtLeast(20F)
    }

    private fun valueToX(value: Int, x: Float, width: Float): Float {
        val span = (maximum - minimum).coerceAtLeast(1)
        return sliderStart(x, width) + sliderWidth(x, width) * ((value - minimum).toFloat() / span)
    }

    private fun xToValue(mouseX: Int, x: Float, width: Float): Int {
        val span = (maximum - minimum).coerceAtLeast(1)
        val ratio = ((mouseX - sliderStart(x, width)) / sliderWidth(x, width)).coerceIn(0f, 1f)

        return (minimum + span * ratio).toInt().coerceIn(minimum, maximum)
    }

    override fun drawElement(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, bgColor: Color, accentColor: Color): Float {
        if (draggingStart) {
            val newStart = xToValue(mouseX, x, width).coerceAtMost(savedValue.endInclusive)
            savedValue.set(newStart..savedValue.endInclusive)
        }
        if (draggingEnd) {
            val newEnd = xToValue(mouseX, x, width).coerceAtLeast(savedValue.start)
            savedValue.set(savedValue.start..newEnd)
        }

        val maxLength = Fonts.font40.getStringWidth("$maximum")
        val minLength = Fonts.font40.getStringWidth("$minimum")
        val valueDisplay = 30F + maxLength
        val start = sliderStart(x, width)
        val trackWidth = sliderWidth(x, width)

        Fonts.font40.drawString(value.name, x + 10F, y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)
        Fonts.font40.drawString("$maximum", x + width - 10F - maxLength - valueDisplay, y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)
        Fonts.font40.drawString("$minimum", x + width - 30F - trackWidth - maxLength - minLength - valueDisplay, y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)

        // Track
        RenderUtils.drawRect(start, y + 9F, start + trackWidth, y + 11F, Color(60, 60, 60, 200).rgb)

        val startX = valueToX(savedValue.start, x, width)
        val endX = valueToX(savedValue.endInclusive, x, width)

        // Selected span + handles
        RenderUtils.drawRect(startX, y + 9F, endX, y + 11F, accentColor.rgb)
        RenderUtils.drawRect(startX - 1.5F, y + 6F, startX + 1.5F, y + 14F, Color.WHITE.rgb)
        RenderUtils.drawRect(endX - 1.5F, y + 6F, endX + 1.5F, y + 14F, Color.WHITE.rgb)

        // Current range
        val label = "${savedValue.start}-${savedValue.endInclusive}"
        Fonts.font40.drawString(
            label,
            x + width - 5F - valueDisplay + (valueDisplay - Fonts.font40.getStringWidth(label)) / 2F,
            y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F,
            -1
        )

        return valueHeight
    }

    override fun onClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {
        if (mouseY < y || mouseY > y + valueHeight) return

        val startX = valueToX(savedValue.start, x, width)
        val endX = valueToX(savedValue.endInclusive, x, width)

        // Pick the closer handle when both are under the cursor.
        if (ClientUtils.mouseWithinBounds(mouseX, mouseY, startX - 6F, y, startX + 6F, y + valueHeight)) {
            draggingStart = true
        } else if (ClientUtils.mouseWithinBounds(mouseX, mouseY, endX - 6F, y, endX + 6F, y + valueHeight)) {
            draggingEnd = true
        }
    }

    override fun onRelease(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {
        draggingStart = false
        draggingEnd = false
    }
}
