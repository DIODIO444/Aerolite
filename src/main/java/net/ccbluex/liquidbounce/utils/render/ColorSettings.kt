/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render
import net.ccbluex.liquidbounce.config.*

import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.ConfigurableImpl
import net.ccbluex.liquidbounce.ui.client.hud.element.Element.Companion.MAX_GRADIENT_COLORS
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import java.awt.Color

class ColorSettingsFloat(owner: Configurable, name: String, val index: Int? = null, generalApply: () -> Boolean = { true }) : ConfigurableImpl(name) {
    private val colors = color(
        "$name${index ?: "Color"}",
        Color(
            if ((index ?: 0) % 3 == 1) 255 else 0,
            if ((index ?: 0) % 3 == 2) 255 else 0,
            if ((index ?: 0) % 3 == 0) 255 else 0
        )
    ) { generalApply() }.subjective() as ColorValue

    val color: Color
        get() = colors.selectedColor()

    init {
        owner.addValue(colors)
    }

    companion object {
        inline fun create(
            owner: Configurable, name: String, colors: Int = MAX_GRADIENT_COLORS, crossinline generalApply: (Int) -> Boolean = { true },
        ): List<ColorSettingsFloat> {
            return (1..colors).map { ColorSettingsFloat(owner, name, it) { generalApply(it) } }
        }
    }
}

class ColorSettingsInteger(
    owner: Configurable, name: String? = null,
    val index: Int? = null,
    applyMax: Boolean = false,
    generalApply: () -> Boolean = { true }
) : ConfigurableImpl(name ?: "Color") {
    private val max = if (applyMax) 255 else 0

    private val colors = color("${this.name}${index ?: ""}", Color(max, max, max, 255)) { generalApply() }.subjective() as ColorValue

    fun color(a: Int = colors.selectedColor().alpha) = color().withAlpha(a)

    fun color() = colors.selectedColor()

    fun with(
        r: Int = color().red, g: Int = color().green, b: Int = color().blue, a: Int = color().alpha
    ): ColorSettingsInteger {
        Color(r, g, b, a).let {
            colors.setAndUpdateDefault(it)
            colors.setupSliders(it)
        }
        return this
    }

    fun with(color: Color) = with(color.red, color.green, color.blue, color.alpha)

    init {
        owner.addValue(colors)
    }

    companion object {
        inline fun create(
            owner: Configurable, name: String, colors: Int, applyMax: Boolean = false, crossinline generalApply: (Int) -> Boolean = { true }
        ): List<ColorSettingsInteger> {
            return (1..colors).map {
                ColorSettingsInteger(owner, name, it, applyMax) { generalApply(it) }
            }
        }
    }
}

fun List<ColorSettingsFloat>.toColorArray(max: Int) = (0 until max).map {
    val colors = this[it].color

    floatArrayOf(
        colors.red.toFloat() / 255f,
        colors.green.toFloat() / 255f,
        colors.blue.toFloat() / 255f,
        1f
    )
}