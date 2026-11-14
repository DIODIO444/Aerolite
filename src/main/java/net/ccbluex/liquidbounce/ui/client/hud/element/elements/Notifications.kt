package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager

import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal

/** Centre X / diameter of the status circle drawn on the left of a notification. */
private const val NOTIFICATION_ICON_X = 14F
private const val NOTIFICATION_ICON_RADIUS = 8.5F
// The icon glyphs are circular badges with the check / cross cut out, so the ink is the badge
// itself. Matching the circle diameter makes the two overlap exactly: the cut out symbol then
// shows the translucent circle behind it. Use 1.7F * radius instead for a visible rim.
private const val NOTIFICATION_ICON_SIZE = NOTIFICATION_ICON_RADIUS * 2F

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications")
class Notifications(x: Double = 0.0, y: Double = 0.0, scale: Float = 1F,
                    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {
    /**
     * Example notification for CustomHUD designer
     */
    companion object {
        val notificationStyle = ListValue("Style", arrayOf("Novoline", "FDPOld"), "Novoline")
    }
    private val exampleNotification = Notification("Notification", "This is an example notification.", NotifyType.INFO)

    /**
     * Draw element
     */
    override fun drawElement(partialTicks: Float): Border? {
        val notifications = mutableListOf<Notification>()
        //FUCK YOU java.util.ConcurrentModificationException
        for ((index, notify) in LiquidBounce.hud.notifications.withIndex()) {
            GL11.glPushMatrix()

            if (notify.drawNotification(index)) {
                notifications.add(notify)
            }

            GL11.glPopMatrix()
        }
        for (notify in notifications) {
            LiquidBounce.hud.notifications.remove(notify)
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!LiquidBounce.hud.notifications.contains(exampleNotification))
                LiquidBounce.hud.addNotification(exampleNotification)

            exampleNotification.fadeState = FadeState.STAY
            exampleNotification.displayTime = System.currentTimeMillis()
//            exampleNotification.x = exampleNotification.textLength + 8F

            if (notificationStyle.get().equals("Novoline")) return Border(-exampleNotification.width.toFloat() + 85, -exampleNotification.height.toFloat()-24.5f, 85F, -24.5F)
            else return Border(-exampleNotification.width.toFloat() + 120, -exampleNotification.height.toFloat()-24.5f, 40F, -24.5F)
        }

        return null
    }

    /**
     * Legacy severity levels used by the b100 style notification call sites.
     *
     * [barColor] lets the icon and the progress bar disagree: "module disabled" should keep the
     * success check mark but draw a red bar, which the old `NotifyType`-only mapping could not
     * express (both icon and colour were derived from the same value).
     */
    enum class SeverityType(val notifyType: NotifyType, val barColor: Color? = null) {
        SUCCESS(NotifyType.SUCCESS),
        RED_SUCCESS(NotifyType.SUCCESS, Color.RED),
        ERROR(NotifyType.ERROR),
        WARNING(NotifyType.WARNING),
        INFO(NotifyType.INFO)
    }
}

class Notification(
    val title: String,
    val content: String,
    val type: NotifyType = NotifyType.INFO,
    val time: Int = 2000,
    val animeTime: Int = 500,
    /** Overrides the colour derived from [type] (used by [Notifications.SeverityType.RED_SUCCESS]). */
    val barColor: Color? = null
) {

    /** Legacy constructor kept for the b100 style `severityType = ...` call sites. */
    constructor(title: String, content: String, severityType: Notifications.SeverityType, time: Int = 2000) :
            this(title, content, severityType.notifyType, time, barColor = severityType.barColor)

    companion object {
        fun informative(source: Any?, message: String, time: Long = 2000L) =
            Notification(sourceName(source), message, NotifyType.INFO, time.toInt())

        fun warning(source: Any?, message: String, time: Long = 2000L) =
            Notification(sourceName(source), message, NotifyType.WARNING, time.toInt())

        fun error(source: Any?, message: String, time: Long = 2000L) =
            Notification(sourceName(source), message, NotifyType.ERROR, time.toInt())

        private fun sourceName(source: Any?): String = when (source) {
            null -> "Notification"
            is net.ccbluex.liquidbounce.features.module.Module -> source.spacedName
            else -> "Notification"
        }
    }

    /**
     * Height follows the real font metrics: two text lines plus padding.
     * The old fixed value of 30 was smaller than `font37.height + font32.height`, so the second
     * line always spilled over the progress bar.
     */
    val height: Int
        get() = (Fonts.font37.fontHeight + Fonts.font32.fontHeight + 8).coerceAtLeast(30)

    var fadeState = FadeState.IN
    var nowY = -34
    private val classicHeight = 30
    var string = ""
    var displayTime = System.currentTimeMillis()
    var animeXTime = System.currentTimeMillis()
    var animeYTime = System.currentTimeMillis()
    // Reserve room for the icon column (27px), the " (0.0s)" suffix and a little padding.
    val width = Fonts.font32.getStringWidth("$content (00.0s)") + 42

    /**
     * Draw notification
     */
    fun drawNotification(index: Int): Boolean {
        var color = Color(-1)
        val realY = -(index + 1) * (height + 10)
        var transY = nowY.toDouble() - 10.0
        val nowTime = System.currentTimeMillis()
        //Y-Axis Animation
        if (nowY != realY) {
            var pct = (nowTime - animeYTime) / animeTime.toDouble()
            if (pct > 1) {
                nowY = realY
                pct = 1.0
            } else {
                pct = EaseUtils.easeOutExpo(pct)
            }
            GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
        } else {
            animeYTime = nowTime
        }
        GL11.glTranslated(0.0, nowY.toDouble(), 0.0)

        //X-Axis Animation
        var pct = (nowTime - animeXTime) / animeTime.toDouble()
        when (fadeState) {
            FadeState.IN -> {
                if (pct > 1) {
                    fadeState = FadeState.STAY
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = EaseUtils.easeOutExpo(pct)
            }

            FadeState.STAY -> {
                pct = 1.0
                if ((nowTime - animeXTime) > time) {
                    fadeState = FadeState.OUT
                    animeXTime = nowTime
                }
            }

            FadeState.OUT -> {
                if (pct > 1) {
                    fadeState = FadeState.END
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = 1 - EaseUtils.easeOutExpo(pct)
            }

            FadeState.END -> {
                return true
            }
        }
        if (type.toString() == "SUCCESS") {
            string = "a"
        }
        if (type.toString() == "ERROR") {
            string = "B"
        }
        if (type.toString() == "WARNING") {
            string = "D"
        }
        if (type.toString() == "INFO") {
            string = "C"
        }
        color = barColor ?: when (type.toString()) {
            "SUCCESS" -> Color.GREEN
            "ERROR" -> Color.RED
            "WARNING" -> Color.YELLOW
            "INFO" -> Color.GRAY
            else -> Color.WHITE
        }
        val transX = width - (width * pct) - width
        when (Notifications.Companion.notificationStyle.get()) {
            "Novoline" -> {
                GL11.glScaled(pct, pct, pct)
                val displayingTime =
                    BigDecimal(((time - time * ((nowTime - displayTime) / (animeTime * 2F + time))) / 1000).toDouble()).setScale(
                        1,
                        BigDecimal.ROUND_HALF_UP
                    )
                GL11.glTranslatef(-width.toFloat() / 2, -height.toFloat() / 2, 0F)
                RenderUtils.drawShadow(0F, 0F, width.toFloat(), height.toFloat())
                RenderUtils.drawRect(0F, 0F, width.toFloat(), height.toFloat(), Color(63, 63, 63, 100))
                RenderUtils.drawRect(
                    0.0,
                    height - 1.7,
                    (width * ((nowTime - displayTime) / (animeTime * 2F + time))).toDouble(),
                    height.toDouble(),
                    color.rgb
                )
                val titleFont = Fonts.font37
                val contentFont = Fonts.font32

                // Lay the two lines out from the font metrics so nothing spills over the bar.
                val titleY = 4F
                val contentY = titleY + titleFont.fontHeight + 1F
                val iconCenterY = height / 2F

                titleFont.drawStringWithShadow(title, 27F, titleY, Color.WHITE.rgb)
                contentFont.drawStringWithShadow(
                    content + " (" + displayingTime.toString() + "s)",
                    27F,
                    contentY,
                    Color.WHITE.rgb
                )
                RenderUtils.drawFilledCircle(
                    NOTIFICATION_ICON_X,
                    iconCenterY,
                    NOTIFICATION_ICON_RADIUS,
                    Color(0, 0, 0, 70)
                )

                // Centre the icon by its visible ink instead of the padded glyph quad, so the
                // check / cross / info glyph is exactly concentric with the circle behind it.
                val iconChar = string.firstOrNull()
                if (iconChar != null) {
                    Fonts.Nicon80.defaultFont.drawCenteredGlyph(
                        iconChar,
                        NOTIFICATION_ICON_X,
                        iconCenterY,
                        NOTIFICATION_ICON_SIZE,
                        Color.WHITE.rgb
                    )
                }
            }
            "FDPOld" -> {
                GL11.glScaled(pct, pct, pct)
                GL11.glTranslatef(-width.toFloat() / 2, -height.toFloat() / 2, 0F)
                RenderUtils.drawRect(0F, 0F, width.toFloat(), classicHeight.toFloat(), Color(0, 0, 0, 100))
                RenderUtils.drawRect(0F, classicHeight - 2F, Math.max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F), classicHeight.toFloat(), color)
                Fonts.font35.drawString(title, 4F, 4F, Color(255, 255, 255).rgb, false)
                Fonts.font35.drawString(content, 4F, 17F,Color(255, 255, 255).rgb, false)
            }
        }

        GlStateManager.resetColor()
        return false
    }
}

/**
 * FDPClient-compatible alias: ported code references `Type.SUCCESS` / `Type.INFO` etc.
 */
typealias Type = NotifyType

enum class NotifyType(var icon: String) {
    SUCCESS("check-circle"),
    ERROR("close-circle"),
    WARNING("warning"),
    INFO("information");
}


enum class FadeState { IN, STAY, OUT, END }

