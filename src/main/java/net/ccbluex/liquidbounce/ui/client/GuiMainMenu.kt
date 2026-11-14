/*
 * Aerolite main menu — icon row layout, ported from
 * launch/data/legacyui/GuiMainMenuNew.java + Button.java of the Aerolite sources.
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_REAL_VERSION
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.FileManager.valuesConfig
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.fontmanager.GuiFontManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.drawCenteredString
import net.ccbluex.liquidbounce.utils.client.JavaVersion
import net.ccbluex.liquidbounce.utils.client.javaVersion
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.gui.GuiLanguage
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import org.lwjgl.input.Mouse
import java.awt.Color
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

class GuiMainMenu : AbstractScreen() {

    private var popup: PopupScreen? = null

    override fun initGui() {
        buttonList.clear()

        // Corner shortcuts, laid out like the previous main menu.
        +GuiButton(110, width - 110, 10, 100, 20, "FisProxy")
        +GuiButton(109, 10, 10, 110, 20, "Font Manager")
        +GuiButton(101, 10, 32, 110, 20, "Server Status")
        +GuiButton(108, 10, 54, 110, 20, "Contributors")
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            101 -> mc.displayGuiScreen(GuiServerStatus(this))
            108 -> mc.displayGuiScreen(GuiContributors(this))
            109 -> mc.displayGuiScreen(GuiFontManager(this))
            110 -> mc.displayGuiScreen(GuiFisProxy(this))
        }
    }

    /**
     * One round icon button of the main menu, mirroring Aerolite's `Button` class.
     */
    private inner class IconButton(
        private val icon: String,
        private val label: String,
        private val action: () -> Unit
    ) {
        private var x = 0F
        private var y = 0F
        private var hover = 0F

        fun render(x: Float, y: Float, mouseX: Int, mouseY: Int) {
            this.x = x
            this.y = y

            val isHovered = mouseX >= x - BUTTON_HALF && mouseX <= x + BUTTON_HALF &&
                    mouseY >= y - BUTTON_HALF && mouseY <= y + BUTTON_HALF

            // Ease towards the hover target (Aerolite used RenderUtils.smoothAnimation).
            hover += ((if (isHovered) MAX_LIFT else 0F) - hover) * SMOOTHING

            val lift = hover

            RenderUtils.drawRoundedRect(
                x - BUTTON_HALF, y - BUTTON_HALF, x + BUTTON_HALF, y + BUTTON_HALF,
                Color(0, 0, 0, 40).rgb, BUTTON_RADIUS
            )
            RenderUtils.drawRoundedBorder(
                x - BUTTON_HALF, y - BUTTON_HALF, x + BUTTON_HALF, y + BUTTON_HALF,
                1F, Color(255, 255, 255, 60).rgb, BUTTON_RADIUS
            )

            // Centre the glyph by its visible ink: the advance width of an icon glyph includes
            // side bearings, so centring by getStringWidth() visibly drifts to one side.
            Fonts.mainmenu.defaultFont.drawCenteredGlyph(
                icon[0],
                x,
                y - 2F - lift,
                ICON_SIZE,
                Color.WHITE.rgb
            )

            if (hover >= 0.11F) {
                val alpha = ((hover / MAX_LIFT) * 254F).toInt().coerceIn(0, 255)
                Fonts.gs15.drawString(
                    label,
                    x - Fonts.gs15.getStringWidth(label) / 2F,
                    y + 12F - lift,
                    Color(255, 255, 255, alpha).rgb,
                    false
                )
            }
        }

        fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
            if (mouseButton == 0 &&
                mouseX >= x - BUTTON_HALF && mouseX <= x + BUTTON_HALF &&
                mouseY >= y - BUTTON_HALF && mouseY <= y + BUTTON_HALF
            ) {
                action()
            }
        }
    }

    private val iconButtons = listOf(
        IconButton("G", "Single Player") { mc.displayGuiScreen(GuiSelectWorld(this@GuiMainMenu)) },
        IconButton("H", "Multiplayer") { mc.displayGuiScreen(GuiMultiplayer(this@GuiMainMenu)) },
        IconButton("I", "Alt Manager") { mc.displayGuiScreen(GuiAltManager(this@GuiMainMenu)) },
        IconButton("J", "Mods") { mc.displayGuiScreen(GuiModsMenu(this@GuiMainMenu)) },
        IconButton("K", "Settings") { mc.displayGuiScreen(GuiOptions(this@GuiMainMenu, mc.gameSettings)) },
        IconButton("L", "Language") {
            mc.displayGuiScreen(GuiLanguage(this@GuiMainMenu, mc.gameSettings, mc.languageManager))
        },
        IconButton("E", "Client Settings") { mc.displayGuiScreen(GuiClientConfiguration(this@GuiMainMenu)) },
        IconButton("M", "Quit") { mc.shutdown() }
    )

    companion object {
        private const val BUTTON_HALF = 30F
        private const val BUTTON_RADIUS = 15F
        private const val ICON_SIZE = 24F
        private const val PARALLAX_STRENGTH = 7F
        private const val MAX_LIFT = 4F
        private const val SMOOTHING = 0.25F

        private var popupOnce = false
        /** Kept for config compatibility (ValuesConfig still serialises it). */
        var lastWarningTime: Long? = null

    }

    init {
        if (!popupOnce) {
            javaVersion?.let {
                when {
                    it.major == 1 && it.minor == 8 && it.update < 100 -> showOutdatedJava8Warning()
                    it.major > 8 -> showJava11Warning()
                }
            }
            if (FileManager.firstStart) {
                showWelcomePopup()
            }
            popupOnce = true
        }
    }

    /** Mouse parallax offsets, mirroring Aerolite's moveMouseEffect(mouseX, mouseY, 7F). */
    private fun parallaxX(mouseX: Int) = (mouseX - width / 2F) / (width / 2F) * PARALLAX_STRENGTH
    private fun parallaxY(mouseY: Int) = (mouseY - height / 2F) / (height / 2F) * PARALLAX_STRENGTH

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        // The menu content (not the background) drifts a few pixels with the cursor.
        val offsetX = parallaxX(mouseX)
        val offsetY = parallaxY(mouseY)

        GlStateManager.pushMatrix()
        GlStateManager.translate(offsetX, offsetY, 0F)

        val centerX = width / 2F
        val centerY = height / 2F
        val halfPanelWidth = 80F * (iconButtons.size / 2F) + 3F

        // Panel behind the title / icon row / version, as in Aerolite.
        RenderUtils.drawRoundedCornerRect(
            centerX - halfPanelWidth, centerY - 103F,
            centerX + halfPanelWidth, centerY + 103F,
            10F, Color(0, 0, 0, 60).rgb
        )

        // The Aerolite title is noticeably larger than the body font; the AWT renderers already
        // draw at a quarter of their point size, so scale it up here instead of adding a new font.
        //
        // Aerolite-main1's GuiMainMenuNew draws it with ColorUtils.rainbow(), which resolves
        // through the HUD's RainbowStart / RainbowStop / RainbowSaturation / RainbowSpeed settings.
        GlStateManager.pushMatrix()
        // Same anchor as Aerolite-main1: the text top sits at height / 2 - 70.
        GlStateManager.translate(centerX, centerY - 70F, 0F)
        GlStateManager.scale(1.8F, 1.8F, 1F)
        Fonts.fontRegular45.drawCenteredString(CLIENT_NAME, 0F, 0F, ColorUtils.rainbow().rgb, true)
        GlStateManager.popMatrix()

        Fonts.font35.drawCenteredString(
            "You are using $CLIENT_REAL_VERSION version! You can check 578251834 for updates.",
            centerX,
            centerY + 70F,
            Color.WHITE.rgb
        )

        val localMouseX = (mouseX - offsetX).toInt()
        val localMouseY = (mouseY - offsetY).toInt()

        var iconX = centerX - 64.5F * (iconButtons.size / 2F)
        for (button in iconButtons) {
            button.render(iconX, centerY + 20F, localMouseX, localMouseY)
            iconX += 75F
        }

        Fonts.font35.drawCenteredString(
            "Someone stops your foolish behavior because everybody knows you are skidding aerolite ^^",
            centerX,
            height - 24F,
            Color.WHITE.rgb
        )
        Fonts.font35.drawCenteredString(
            "Made with <3 by Stars, Kilo, Packet, Crazy",
            centerX,
            height - 12F,
            Color.WHITE.rgb
        )

        GlStateManager.popMatrix()

        super.drawScreen(mouseX, mouseY, partialTicks)

        popup?.drawScreen(width, height, mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (popup != null) {
            popup!!.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        val localX = (mouseX - parallaxX(mouseX)).toInt()
        val localY = (mouseY - parallaxY(mouseY)).toInt()

        for (button in iconButtons) {
            button.mouseClicked(localX, localY, mouseButton)
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun handleMouseInput() {
        if (popup != null) {
            val eventDWheel = Mouse.getEventDWheel()
            if (eventDWheel != 0) {
                popup!!.handleMouseWheel(eventDWheel)
            }
        }

        super.handleMouseInput()
    }

    private fun showWelcomePopup() {
        popup = PopupScreen {
            title("§a§lWelcome!")
            message("""
                §eThank you for downloading and installing §bLiquidBounce§e!
        
                §6Here is some information you might find useful:§r
                §a- §fClickGUI:§r Press §7[RightShift]§f to open ClickGUI.
                §a- §fRight-click modules with a '+' to edit.
                §a- §fHover over a module to see its description.
        
                §6Important Commands:§r
                §a- §f.bind <module> <key> / .bind <module> none
                §a- §f.config load <name> / .config list
        
                §bNeed help? Contact us!§r
                - §fYouTube: §9https://youtube.com/ccbluex
                - §fTwitter: §9https://twitter.com/CCBlueX
                - §fForum: §9https://forums.ccbluex.net/
            """.trimIndent())
            button("§aOK")
            onClose { popup = null }
        }
    }

    private fun showOutdatedJava8Warning() {
        popup = PopupScreen {
            title("§c§lOutdated Java Runtime Environment")
            message("""
                §6§lYou are using an outdated version of Java 8 (${javaVersion!!.raw}).§r
                
                §fThis might cause unexpected §c§lBUGS§f.
                Please update it to 8u101+, or get a new one from the Internet.
            """.trimIndent())
            button("§aDownload Java") { MiscUtils.showURL(JavaVersion.DOWNLOAD_PAGE) }
            button("§eI realized")
            onClose { popup = null }
        }
    }

    private fun showJava11Warning() {
        popup = PopupScreen {
            title("§c§lInappropriate Java Runtime Environment")
            message("""
                §6§lThis version of $CLIENT_NAME is designed for Java 8 environment.§r
                
                §fHigher versions of Java might cause bug or crash.
                You can get JRE 8 from the Internet.
            """.trimIndent())
            button("§aDownload Java") { MiscUtils.showURL(JavaVersion.DOWNLOAD_PAGE) }
            button("§eI realized")
            onClose { popup = null }
        }
    }
}

