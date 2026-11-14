/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.LiquidBounce.hud
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Element.Companion.MAX_GRADIENT_COLORS
import net.ccbluex.liquidbounce.utils.render.ColorSettingsFloat
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.minecraft.client.gui.GuiChat
import net.minecraft.util.ResourceLocation

object HUD : Module("HUD", Category.RENDER, gameDetecting = false, defaultState = true, defaultHidden = true) {
    /* ---- Arraylist entry animation (ported from Aerolite-main1) ---- */

    val arraylistXAxisAnimType by choices(
        "ArraylistXAxisAnimType",
        EaseUtils.EnumEasingType.values().map { it.toString() }.toTypedArray(),
        "SINE"
    )
    val arraylistXAxisAnimOrder by choices(
        "ArraylistXAxisHotbarAnimOrder",
        EaseUtils.EnumEasingOrder.values().map { it.toString() }.toTypedArray(),
        "FAST_AT_START"
    )
    val arraylistXAxisAnimSpeed by int("ArraylistXAxisAnimSpeed", 10, 5..20)

    val arraylistYAxisAnimType by choices(
        "ArraylistYAxisAnimType",
        EaseUtils.EnumEasingType.values().map { it.toString() }.toTypedArray(),
        "SINE"
    )
    val arraylistYAxisAnimOrder by choices(
        "ArraylistYAxisHotbarAnimOrder",
        EaseUtils.EnumEasingOrder.values().map { it.toString() }.toTypedArray(),
        "FAST_AT_START"
    )
    val arraylistYAxisAnimSpeed by int("ArraylistYAxisAnimSpeed", 10, 5..20)

    /* ---- Global rainbow colours (ported from Aerolite-main1's HUD) ----
     *
     * Aerolite's ColorUtils.hslRainbow() reads these, so every rainbow in the client (main menu
     * title, arraylist accents, HUD elements …) shares one hue band instead of a full spectrum.
     */

    val rainbowStartValue by float("RainbowStart", 0.41f, 0f..1f)
    val rainbowStopValue by float("RainbowStop", 0.58f, 0f..1f)
    val rainbowSaturationValue by float("RainbowSaturation", 0.7f, 0f..1f)
    val rainbowBrightnessValue by float("RainbowBrightness", 1f, 0f..1f)
    val rainbowSpeedValue by int("RainbowSpeed", 1500, 500..7000)

    val customHotbar by boolean("CustomHotbar", true)

    /** "Aerolite" reproduces Aerolite's BetterHotbar (Rect / RoundRect / Rect2), "Modern" keeps the
     *  client's gradient/rainbow rounded hotbar. */
    val hotbarStyle by choices("Hotbar-Style", arrayOf("Aerolite", "Modern"), "Aerolite") { customHotbar }
    val betterHotbarMode by choices("BetterHotbar-Mode", arrayOf("Rect", "RoundRect", "Rect2"), "Rect") {
        customHotbar && hotbarStyle == "Aerolite"
    }
    val betterHotbarShadow by boolean("BetterHotbar-Shadow", false) {
        customHotbar && hotbarStyle == "Aerolite"
    }

    /** Blurs the strip behind the hotbar. Independent from the GUI background blur below. */
    val betterHotbarBlur by boolean("BetterHotbar-Blur", false) {
        customHotbar && hotbarStyle == "Aerolite"
    }

    val smoothHotbarSlot by boolean("SmoothHotbarSlot", true) { customHotbar }

    val roundedHotbarRadius by float("RoundedHotbar-Radius", 3F, 0F..5F) { customHotbar }

    val hotbarMode by choices("Hotbar-Color", arrayOf("Custom", "Rainbow", "Gradient"), "Custom") { customHotbar }
    val hbHighlightColors = ColorSettingsInteger(this, "Hotbar-Highlight-Colors", applyMax = true)
    { customHotbar }.with(a = 0)
    val hbBackgroundColors = ColorSettingsInteger(this, "Hotbar-Background-Colors")
    { customHotbar && hotbarMode == "Custom" }.with(a = 190)
    val gradientHotbarSpeed by float("Hotbar-Gradient-Speed", 1f, 0.5f..10f)
    { customHotbar && hotbarMode == "Gradient" }
    val maxHotbarGradientColors by int("Max-Hotbar-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { customHotbar && hotbarMode == "Gradient" }
    val bgGradColors = ColorSettingsFloat.create(this, "Hotbar-Gradient")
    { customHotbar && hotbarMode == "Gradient" && it <= maxHotbarGradientColors }
    val hbHighlightBorder by float("HotbarBorder-Highlight-Width", 2F, 0.5F..5F) { customHotbar }
    val hbHighlightBorderColors = ColorSettingsInteger(this, "HotbarBorder-Highlight-Colors")
    { customHotbar }.with(a = 255, g = 111, b = 255)
    val hbBackgroundBorder by float("HotbarBorder-Background-Width", 0.5F, 0.5F..5F) { customHotbar }
    val hbBackgroundBorderColors = ColorSettingsInteger(this, "HotbarBorder-Background-Colors")
    { customHotbar }.with(a = 0)

    val rainbowX by float("Rainbow-X", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Rainbow" }
    val rainbowY by float("Rainbow-Y", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Rainbow" }
    val gradientX by float("Gradient-X", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Gradient" }
    val gradientY by float("Gradient-Y", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Gradient" }

    val inventoryParticle by boolean("InventoryParticle", false)
    /** GUI background blur (screen shader while a screen is open). */
    private val blur by boolean("Blur", false)

    /** Read by MixinGuiInGame — hotbar strip blur, separate from [blur]. */
    val isHotbarBlur: Boolean get() = betterHotbarBlur
    private val fontChat by boolean("FontChat", false)

    val onRender2D = handler<Render2DEvent> { event ->
        if (mc.currentScreen is GuiHudDesigner)
            return@handler

        hud.render(false, event.partialTicks)
    }

    val onUpdate = handler<UpdateEvent> {
        hud.update()
    }

    val onKey = handler<KeyEvent> { event ->
        hud.handleKey('a', event.key)
    }

    val onScreen = handler<ScreenEvent>(always = true) { event ->
        if (mc.theWorld == null || mc.thePlayer == null) return@handler

        // Must match the path loaded below. It used to say "liquidbounce/blur.json", so the
        // stopUseShader() branch never matched and the full screen shader stayed active forever
        // after the first screen was opened.
        val blurShaderName = "${CLIENT_NAME.lowercase()}/blur.json"

        if (state && blur && !mc.entityRenderer.isShaderActive && event.guiScreen != null &&
            !(event.guiScreen is GuiChat || event.guiScreen is GuiHudDesigner)
        ) mc.entityRenderer.loadShader(
            ResourceLocation(blurShaderName)
        ) else if (mc.entityRenderer.shaderGroup != null &&
            blurShaderName in mc.entityRenderer.shaderGroup.shaderGroupName
        ) mc.entityRenderer.stopUseShader()
    }

    fun shouldModifyChatFont() = handleEvents() && fontChat
}