/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.config.displayable
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11


object Chams : Module("Chams", Category.RENDER) {
    val targetsValue = boolean("Targets", true)
    val chestsValue = boolean("Chests", true)
    val itemsValue = boolean("Items", true)

    /* ---- Java (mixin) compatibility accessors ---- */
    val targets: Boolean get() = targetsValue.get()
    val chests: Boolean get() = chestsValue.get()
    val items: Boolean get() = itemsValue.get()

    private val colorRainbowValue = boolean("Rainbow", false)
    private val colorRedValue = int("Red", 255, 0..255).displayable { !colorRainbowValue.get() }
    private val colorGreenValue = int("Green", 255, 0..255).displayable { !colorRainbowValue.get() }
    private val colorBlueValue = int("Blue", 255, 0..255).displayable { !colorRainbowValue.get() }
    private val colorAlphaValue = int("Alpha", 200, 0..255)

    fun setColor() {
        if (colorRainbowValue.get()) {
            RenderUtils.glColor(ColorUtils.rainbowWithAlpha(colorAlphaValue.get()))
        } else {
            GL11.glColor4f(colorRedValue.get() / 255f, colorGreenValue.get() / 255f, colorBlueValue.get() / 255f, colorAlphaValue.get() / 255f)
        }
    }
}
