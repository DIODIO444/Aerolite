package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles

import net.ccbluex.liquidbounce.config.BoolValue
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.FontValue
import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.config.ListValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI
import net.ccbluex.liquidbounce.ui.client.clickgui.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ButtonElement
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement
import net.ccbluex.liquidbounce.ui.client.clickgui.style.ClickGuiValueHelper
import net.ccbluex.liquidbounce.ui.client.clickgui.style.Style
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import org.lwjgl.input.Mouse
import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Faithful port of Aerolite's `LiquidBounceStyle`.
 */
object AeroliteStyle : Style() {

    private var mouseDown = false
    private var rightMouseDown = false

    private fun guiColor(): Int = ClickGUI.guiColor

    private fun playClick() {
        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0F))
    }

    private fun roundValue(value: Float): BigDecimal =
        BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP)

    private fun categoryColor(name: String): Color = when (name.lowercase()) {
        "combat" -> Color(231, 75, 58, 175)
        "player" -> Color(142, 69, 174, 175)
        "movement" -> Color(46, 205, 111, 175)
        "render" -> Color(76, 143, 200, 175)
        "world" -> Color(233, 215, 100, 175)
        "misc" -> Color(244, 157, 19, 175)
        "exploit" -> Color(90, 90, 90, 175)
        "client" -> Color(190, 190, 190, 175)
        "fun" -> Color(211, 123, 77, 175)
        else -> Color(guiColor())
    }

    private fun hoverColor(color: Color, hover: Int): Color {
        val red = color.red - hover * 2
        val green = color.green - hover * 2
        val blue = color.blue - hover * 2

        return Color(red.coerceAtLeast(0), green.coerceAtLeast(0), blue.coerceAtLeast(0), color.alpha)
    }

    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel) {
        val color = categoryColor(panel.name)

        RenderUtils.drawRect(
            (panel.x - 3).toFloat(), (panel.y - 1).toFloat(),
            (panel.x + panel.width + 3).toFloat(), (panel.y + 22 + panel.fade).toFloat(),
            color.rgb
        )
        RenderUtils.drawRect(
            (panel.x - 2).toFloat(), panel.y.toFloat(),
            (panel.x + panel.width + 2).toFloat(), (panel.y + 21 + panel.fade).toFloat(),
            Color(17, 17, 17).rgb
        )
        RenderUtils.drawRect(
            (panel.x + 1).toFloat(), (panel.y + 19).toFloat(),
            (panel.x + panel.width - 1).toFloat(), (panel.y + 18 + panel.fade).toFloat(),
            Color(26, 26, 26).rgb
        )

        GlStateManager.resetColor()
        Fonts.gs40.drawString(panel.name, (panel.x + 2).toFloat(), (panel.y + 6).toFloat(), Int.MAX_VALUE)

        if (panel.scrollbar && panel.fade > 0) {
            RenderUtils.drawRect(
                (panel.x - 2).toFloat(), (panel.y + 21).toFloat(),
                panel.x.toFloat(), (panel.y + 16 + panel.fade).toFloat(),
                Int.MAX_VALUE
            )

            val maxElements = ClickGUI.maxElements
            val divisor = (panel.elements.size - maxElements).coerceAtLeast(1)
            val heightPer = (panel.fade - 24F) / divisor

            RenderUtils.drawRect(
                (panel.x - 2).toFloat(),
                panel.y + 30 + heightPer * panel.dragged - 10.0f,
                panel.x.toFloat(),
                panel.y + 40 + heightPer * panel.dragged,
                Color(26, 26, 26).rgb
            )
        }
    }

    override fun drawHoverText(mouseX: Int, mouseY: Int, text: String) {
        val textWidth = Fonts.gs35.getStringWidth("Description:" + text)

        RenderUtils.drawRect(
            (mouseX + 9).toFloat(), mouseY.toFloat(),
            (mouseX + textWidth + 14).toFloat(), (mouseY + Fonts.gs35.FONT_HEIGHT + 3).toFloat(),
            Color(26, 26, 26).rgb
        )

        GlStateManager.resetColor()
        Fonts.gs35.drawString(
            "Description:" + text,
            (mouseX + 12).toFloat(),
            (mouseY + Fonts.gs35.FONT_HEIGHT / 2).toFloat(),
            Int.MAX_VALUE
        )
    }

    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement) {
        val color = if (buttonElement.color != Int.MAX_VALUE) Color(guiColor()) else Color(26, 26, 26)

        RenderUtils.drawRect(
            (buttonElement.x - 1).toFloat(), (buttonElement.y + 1).toFloat(),
            (buttonElement.x + buttonElement.width + 1).toFloat(), (buttonElement.y + buttonElement.height + 2).toFloat(),
            hoverColor(color, buttonElement.hoverTime).rgb
        )

        GlStateManager.resetColor()
        Fonts.gs35.drawString(
            buttonElement.displayName,
            (buttonElement.x + 3).toFloat(),
            (buttonElement.y + 6).toFloat(),
            Color.WHITE.rgb
        )
    }

    override fun drawModuleElementAndClick(
        mouseX: Int,
        mouseY: Int,
        moduleElement: ModuleElement,
        mouseButton: Int?
    ): Boolean {
        val color = guiColor()
        val module = moduleElement.module
        val catColor = categoryColor(module.category.name)

        RenderUtils.drawRect(
            (moduleElement.x + 1).toFloat(), (moduleElement.y + 1).toFloat(),
            (moduleElement.x + moduleElement.width - 1).toFloat(), (moduleElement.y + moduleElement.height + 2).toFloat(),
            hoverColor(Color(26, 26, 26), moduleElement.hoverTime).rgb
        )
        RenderUtils.drawRect(
            (moduleElement.x + 1).toFloat(), (moduleElement.y + 1).toFloat(),
            (moduleElement.x + moduleElement.width - 1).toFloat(), (moduleElement.y + moduleElement.height + 2).toFloat(),
            hoverColor(
                Color(catColor.red, catColor.green, catColor.blue, moduleElement.slowlyFade),
                moduleElement.hoverTime
            ).rgb
        )

        GlStateManager.resetColor()
        Fonts.gs35.drawString(
            moduleElement.displayName,
            (moduleElement.x + 3).toFloat(),
            (moduleElement.y + 7).toFloat(),
            Int.MAX_VALUE
        )

        // Row interaction (toggle / expand) is still handled by the element itself.
        if (mouseButton != null && mouseElementHovered(moduleElement, mouseX, mouseY)) {
            moduleElement.mouseClicked(mouseX, mouseY, mouseButton)
        }

        val moduleValues = ClickGuiValueHelper.values(module)

        if (moduleValues.isEmpty()) {
            return false
        }

        Fonts.gs35.drawString(
            "+",
            (moduleElement.x + moduleElement.width - 8).toFloat(),
            (moduleElement.y + moduleElement.height / 2).toFloat(),
            Color.WHITE.rgb
        )

        if (!moduleElement.showSettings) {
            return false
        }

        var yPos = moduleElement.y + 4
        val baseX = moduleElement.x + moduleElement.width

        for (value in moduleValues) {
            if (value is Configurable) {
                Fonts.gs35.drawString(value.name, (baseX + 6).toFloat(), (yPos + 4).toFloat(), 0xffffa0)
                yPos += 12
                continue
            }

            if (!value.shouldRender()) {
                continue
            }

            when (value) {
                is BoolValue -> {
                    val text = value.name
                    val textWidth = Fonts.gs35.getStringWidth(text)

                    if (moduleElement.settingsWidth < textWidth + 8) {
                        moduleElement.settingsWidth = textWidth + 8
                    }

                    RenderUtils.drawRect(
                        (baseX + 4).toFloat(), (yPos + 2).toFloat(),
                        (baseX + moduleElement.settingsWidth).toFloat(), (yPos + 14).toFloat(),
                        Color(26, 26, 26).rgb
                    )

                    if (hovered(mouseX, mouseY, baseX + 4, yPos + 2, baseX + moduleElement.settingsWidth, yPos + 14)) {
                        if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                            value.toggle()
                            playClick()
                        }
                    }

                    GlStateManager.resetColor()
                    Fonts.gs35.drawString(text, (baseX + 6).toFloat(), (yPos + 4).toFloat(), if (value.get()) color else Int.MAX_VALUE)
                    yPos += 12
                }

                is ListValue -> {
                    val text = value.name
                    val textWidth = Fonts.gs35.getStringWidth(text)

                    if (moduleElement.settingsWidth < textWidth + 16) {
                        moduleElement.settingsWidth = textWidth + 16
                    }

                    RenderUtils.drawRect(
                        (baseX + 4).toFloat(), (yPos + 2).toFloat(),
                        (baseX + moduleElement.settingsWidth).toFloat(), (yPos + 14).toFloat(),
                        Color(26, 26, 26).rgb
                    )

                    GlStateManager.resetColor()
                    Fonts.gs35.drawString("§e$text", (baseX + 6).toFloat(), (yPos + 4).toFloat(), 0xffffff)
                    Fonts.gs35.drawString(
                        if (value.openList) "-" else "+",
                        (baseX + moduleElement.settingsWidth - if (value.openList) 5 else 6).toFloat(),
                        (yPos + 4).toFloat(),
                        0xffffff
                    )

                    if (hovered(mouseX, mouseY, baseX + 4, yPos + 2, baseX + moduleElement.settingsWidth, yPos + 14)) {
                        if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                            value.openList = !value.openList
                            playClick()
                        }
                    }

                    yPos += 12

                    for (option in value.values) {
                        val optionWidth = Fonts.gs35.getStringWidth(">$option")

                        if (moduleElement.settingsWidth < optionWidth + 8) {
                            moduleElement.settingsWidth = optionWidth + 8
                        }

                        if (!value.openList) {
                            continue
                        }

                        RenderUtils.drawRect(
                            (baseX + 4).toFloat(), (yPos + 2).toFloat(),
                            (baseX + moduleElement.settingsWidth).toFloat(), (yPos + 14).toFloat(),
                            Color(26, 26, 26).rgb
                        )

                        if (hovered(mouseX, mouseY, baseX + 4, yPos + 2, baseX + moduleElement.settingsWidth, yPos + 14)) {
                            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                                value.set(option)
                                playClick()
                            }
                        }

                        GlStateManager.resetColor()
                        Fonts.gs35.drawString(">", (baseX + 6).toFloat(), (yPos + 4).toFloat(), Int.MAX_VALUE)
                        Fonts.gs35.drawString(
                            option, (baseX + 14).toFloat(), (yPos + 4).toFloat(),
                            if (value.get().equals(option, ignoreCase = true)) color else Int.MAX_VALUE
                        )
                        yPos += 12
                    }
                }

                is FloatValue -> {
                    val text = value.name + "§f: §b" + roundValue(value.get())
                    val textWidth = Fonts.gs35.getStringWidth(text)

                    if (moduleElement.settingsWidth < textWidth + 8) {
                        moduleElement.settingsWidth = textWidth + 8
                    }

                    val min = value.range.start
                    val max = value.range.endInclusive
                    val span = (max - min).takeIf { it != 0f } ?: 1f

                    RenderUtils.drawRect(
                        (baseX + 4).toFloat(), (yPos + 2).toFloat(),
                        (baseX + moduleElement.settingsWidth).toFloat(), (yPos + 24).toFloat(),
                        Color(26, 26, 26).rgb
                    )
                    RenderUtils.drawRect(
                        (baseX + 8).toFloat(), (yPos + 18).toFloat(),
                        (baseX + moduleElement.settingsWidth - 4).toFloat(), (yPos + 19).toFloat(),
                        Int.MAX_VALUE
                    )

                    val sliderValue = baseX + ((moduleElement.settingsWidth - 12) * (value.get() - min).toFloat() / span)
                    RenderUtils.drawRect(8 + sliderValue, (yPos + 15).toFloat(), sliderValue + 11, (yPos + 21).toFloat(), color)

                    if (hovered(mouseX, mouseY, baseX + 4, yPos + 15, baseX + moduleElement.settingsWidth - 4, yPos + 21)) {
                        if (Mouse.isButtonDown(0)) {
                            val fraction = MathHelper.clamp_double(
                                (mouseX - baseX - 8).toDouble() / (moduleElement.settingsWidth - 12), 0.0, 1.0
                            )
                            value.set(roundValue(min + span * fraction.toFloat()).toFloat())
                        }
                    }

                    GlStateManager.resetColor()
                    Fonts.gs35.drawString(text, (baseX + 6).toFloat(), (yPos + 4).toFloat(), 0xffffff)
                    yPos += 22
                }

                is IntValue -> {
                    val text = value.name + "§f: §c" + value.get()
                    val textWidth = Fonts.gs35.getStringWidth(text)

                    if (moduleElement.settingsWidth < textWidth + 8) {
                        moduleElement.settingsWidth = textWidth + 8
                    }

                    val min = value.range.first
                    val max = value.range.last
                    val span = (max - min).takeIf { it != 0 } ?: 1

                    RenderUtils.drawRect(
                        (baseX + 4).toFloat(), (yPos + 2).toFloat(),
                        (baseX + moduleElement.settingsWidth).toFloat(), (yPos + 24).toFloat(),
                        Color(26, 26, 26).rgb
                    )
                    RenderUtils.drawRect(
                        (baseX + 8).toFloat(), (yPos + 18).toFloat(),
                        (baseX + moduleElement.settingsWidth - 4).toFloat(), (yPos + 19).toFloat(),
                        Int.MAX_VALUE
                    )

                    val sliderValue = baseX + ((moduleElement.settingsWidth - 12) * (value.get() - min).toFloat() / span)
                    RenderUtils.drawRect(8 + sliderValue, (yPos + 15).toFloat(), sliderValue + 11, (yPos + 21).toFloat(), color)

                    if (hovered(mouseX, mouseY, baseX + 4, yPos + 15, baseX + moduleElement.settingsWidth, yPos + 21)) {
                        if (Mouse.isButtonDown(0)) {
                            val fraction = MathHelper.clamp_double(
                                (mouseX - baseX - 8).toDouble() / (moduleElement.settingsWidth - 12), 0.0, 1.0
                            )
                            value.set((min + span * fraction).toInt())
                        }
                    }

                    GlStateManager.resetColor()
                    Fonts.gs35.drawString(text, (baseX + 6).toFloat(), (yPos + 4).toFloat(), 0xffffff)
                    yPos += 22
                }

                is FontValue -> {
                    RenderUtils.drawRect(
                        (baseX + 4).toFloat(), (yPos + 2).toFloat(),
                        (baseX + moduleElement.settingsWidth).toFloat(), (yPos + 14).toFloat(),
                        Color(26, 26, 26).rgb
                    )

                    val fontRenderer = value.get()
                    val displayString = when {
                        fontRenderer is net.ccbluex.liquidbounce.ui.font.GameFontRenderer ->
                            "Font: " + fontRenderer.defaultFont.font.name + " - " + fontRenderer.defaultFont.font.size
                        fontRenderer === Fonts.minecraftFont -> "Font: Minecraft"
                        else -> "Font: Unknown"
                    }

                    Fonts.gs35.drawString(displayString, (baseX + 6).toFloat(), (yPos + 4).toFloat(), Color.WHITE.rgb)

                    val stringWidth = Fonts.gs35.getStringWidth(displayString)
                    if (moduleElement.settingsWidth < stringWidth + 8) {
                        moduleElement.settingsWidth = stringWidth + 8
                    }

                    if ((Mouse.isButtonDown(0) && !mouseDown || Mouse.isButtonDown(1) && !rightMouseDown) &&
                        hovered(mouseX, mouseY, baseX + 4, yPos + 4, baseX + moduleElement.settingsWidth, yPos + 12)
                    ) {
                        val fonts = Fonts.getFonts()
                        val index = fonts.indexOf(fontRenderer)

                        if (index >= 0) {
                            val step = if (Mouse.isButtonDown(0)) 1 else -1
                            var next = index + step

                            if (next >= fonts.size) next = 0
                            if (next < 0) next = fonts.size - 1

                            value.set(fonts[next])
                        }
                    }

                    yPos += 11
                }

                else -> {
                    val text = value.name + "§f: §c" + value.get()
                    val textWidth = Fonts.gs35.getStringWidth(text)

                    if (moduleElement.settingsWidth < textWidth + 8) {
                        moduleElement.settingsWidth = textWidth + 8
                    }

                    RenderUtils.drawRect(
                        (baseX + 4).toFloat(), (yPos + 2).toFloat(),
                        (baseX + moduleElement.settingsWidth).toFloat(), (yPos + 14).toFloat(),
                        Color(26, 26, 26).rgb
                    )

                    GlStateManager.resetColor()
                    Fonts.gs35.drawString(text, (baseX + 6).toFloat(), (yPos + 4).toFloat(), 0xffffff)
                    yPos += 12
                }
            }
        }

        moduleElement.updatePressed()
        mouseDown = Mouse.isButtonDown(0)
        rightMouseDown = Mouse.isButtonDown(1)

        if (moduleElement.settingsWidth > 0 && yPos > moduleElement.y + 4) {
            RenderUtils.drawBorderedRect(
                (baseX + 4).toFloat(), (moduleElement.y + 6).toFloat(),
                (baseX + moduleElement.settingsWidth).toFloat(), (yPos + 2).toFloat(),
                1F, Color(26, 26, 26).rgb, 0
            )
        }

        return false
    }

    private fun mouseElementHovered(element: ModuleElement, mouseX: Int, mouseY: Int): Boolean =
        mouseX in element.x..element.x + element.width &&
            mouseY in element.y..element.y + element.height

    private fun hovered(mouseX: Int, mouseY: Int, x1: Int, y1: Int, x2: Int, y2: Int): Boolean =
        mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2
}
