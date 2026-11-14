/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce.clickGui
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.AeroliteOldStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.AeroliteStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.AstolfoStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.BlackStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.LBPStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.LiquidBounceStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.NeonStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.NeverLoseStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.NovolineStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.NullStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.SlowlyStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.TenacityStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.WhiteStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.ZerodayStyle
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard
import java.awt.Color

object ClickGUI : Module("ClickGUI", Category.RENDER, Keyboard.KEY_RSHIFT, canBeEnabled = false) {
    private val style by choices(
        "Style",
        arrayOf("Novoline", "LiquidBounce", "Aerolite", "AeroliteOld", "Astolfo", "Neon", "Tenacity", "LBP", "NeverLose", "Zeroday", "White", "Null", "Slowly", "Black"),
        "LiquidBounce"
    ).onChanged {
        updateStyle()
    }
    /** Module toggle sound: `None`, `Click` (vanilla) or `Custom` (files in the ClickSound folder). */
    val toggleSound by choices("ToggleSound", arrayOf("None", "Click", "Custom"), "Click")

    val backgroundBlur by boolean("BackgroundBlur", true)
    val blurRadius by float("BlurRadius", 8f, 1f..30f) { backgroundBlur }
    var scale by float("Scale", 0.8f, 0.5f..1.5f)
    val maxElements by int("MaxElements", 15, 1..30)
    /** Show settings grouped by their Configurable parent (used by the ported Aerolite UIs). */
    val groupSettings by boolean("GroupSettings", false)
    /** Skip per-frame animations in the ported Aerolite UIs. */
    val fastRender by boolean("FastRender", true)
    val fadeSpeed by float("FadeSpeed", 1f, 0.5f..4f)
    val scrolls by boolean("Scrolls", true)
    val spacedModules by boolean("SpacedModules", false)
    val panelsForcedInBoundaries by boolean("PanelsForcedInBoundaries", false)

    private val color by color("Color", Color(0, 160, 255)) { style !in arrayOf("Slowly", "Black") }

    val guiColor
        get() = color.rgb

    override fun onEnable() {
        // Aerolite's standalone UIs are full GuiScreen replacements, not Style implementations.
        when (style) {
            "LBP" -> {
                mc.displayGuiScreen(
                    net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newVer.NewUi()
                )
                state = false
                return
            }

            "Zeroday" -> {
                mc.displayGuiScreen(
                    net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.zeroday.ClickUI()
                )
                state = false
                return
            }

            "Novoline" -> {
                mc.displayGuiScreen(
                    net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.novoline.ClickyUI()
                )
                state = false
                return
            }

            "Aerolite" -> {
                mc.displayGuiScreen(
                    net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.aerolite.AeroGui()
                )
                state = false
                return
            }

            "NeverLose" -> {
                mc.displayGuiScreen(
                    net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.HyperGui()
                )
                state = false
                return
            }

            "Tenacity" -> {
                mc.displayGuiScreen(
                    net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.DropdownClickGui()
                )
                state = false
                return
            }
        }

        updateStyle()
        mc.displayGuiScreen(clickGui)
        Keyboard.enableRepeatEvents(true)
    }

    private fun updateStyle() {
        clickGui.style = when (style) {
            "Novoline" -> NovolineStyle
            "LiquidBounce" -> LiquidBounceStyle
            "Aerolite" -> AeroliteStyle
            "AeroliteOld" -> AeroliteOldStyle
            "Astolfo" -> AstolfoStyle
            "Neon" -> NeonStyle
            "Tenacity" -> TenacityStyle
            "LBP" -> LBPStyle
            "NeverLose" -> NeverLoseStyle
            "Zeroday" -> ZerodayStyle
            "White" -> WhiteStyle
            "Null" -> NullStyle
            "Slowly" -> SlowlyStyle
            "Black" -> BlackStyle
            else -> return
        }
    }

    val onPacket = handler<PacketEvent>(always = true) { event ->
        if (event.packet is S2EPacketCloseWindow && mc.currentScreen is ClickGui) {
            event.cancelEvent()
        }
    }
}
