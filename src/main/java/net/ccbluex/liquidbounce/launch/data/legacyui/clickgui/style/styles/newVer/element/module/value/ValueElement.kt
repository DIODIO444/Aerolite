package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.newVer.element.module.value

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.value.Value

import java.awt.Color

abstract class ValueElement<T>(val value: Value<T>) : MinecraftInstance() {

    var valueHeight = 20F

    /**
     * Height this element occupies. Group headers override this to include their expanded children.
     */
    open val height: Float
        get() = valueHeight

    abstract fun drawElement(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, bgColor: Color, accentColor: Color): Float
    abstract fun onClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float)
    open fun onRelease(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {}

    open fun onKeyPress(typed: Char, keyCode: Int): Boolean = false

    open fun isDisplayable(): Boolean = value.displayable
}
