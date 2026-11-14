/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import net.ccbluex.liquidbounce.utils.render.BlurUtils
import me.stars.utils.BlurUtilsOld
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.render.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.util.EnumChatFormatting
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * CustomHUD scoreboard
 *
 * Allows to move and customize minecraft scoreboard
 */
@ElementInfo(name = "Scoreboard")
class ScoreboardElement(
    x: Double = 5.0,
    y: Double = 0.0,
    scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.MIDDLE)
) : Element(x, y, scale, side) {

    private val textRedValue = IntegerValue("Text-R", 255, 0, 255)
    private val textGreenValue = IntegerValue("Text-G", 255, 0, 255)
    private val textBlueValue = IntegerValue("Text-B", 255, 0, 255)

    private val backgroundColorRedValue = IntegerValue("Background-R", 0, 0, 255)
    private val backgroundColorGreenValue = IntegerValue("Background-G", 0, 0, 255)
    private val backgroundColorBlueValue = IntegerValue("Background-B", 0, 0, 255)
    private val backgroundColorAlphaValue = IntegerValue("Background-Alpha", 95, 0, 255)

    private val shadowShaderValue = BoolValue("Shadow", false)

    /** See Arraylist: disabled until the full-screen shadow pass is fixed. */
    private val shaderShadowEnabled = true
    private val shadowStrength = FloatValue("Shadow-Strength", 0F, 0F, 30F).displayable{ shadowShaderValue.get() }
    private val blurValue2 = BoolValue("Blur", true)
    private val blurStrength = FloatValue("Blur-Strength", 0F, 0F, 30F).displayable { blurValue2.get() }


    private val rectValue = BoolValue("Rect", false)
    private val bgRoundedValue = BoolValue("BgRounded", true)
    private val roundStrength = FloatValue("RoundStrength", 5F, 0F, 30F).displayable { bgRoundedValue.get() }
    private val rectColorModeValue = ListValue("Rect-Color", arrayOf("Custom", "Rainbow"), "Custom")
    private val rectColorRedValue = IntegerValue("Rect-R", 0, 0, 255)
    private val rectColorGreenValue = IntegerValue("Rect-G", 111, 0, 255)
    private val rectColorBlueValue = IntegerValue("Rect-B", 255, 0, 255)
    private val rectColorBlueAlpha = IntegerValue("Rect-Alpha", 255, 0, 255)



    private val rectBlurSet = BoolValue("RectExpandBlur", true)
    private val rainbowBarValue = BoolValue("RainbowBar", false)
    private val shadowValue = BoolValue("Shadow", false)
    private val serverValue = ListValue("ServerIp", arrayOf("None", "ClientName", "Website", "Funny"), "Website")
    private val noPointValue = BoolValue("NoPoints", false)
    private val fontValue = FontValue("Font", Fonts.minecraftFont)

    private val allowedDomains = arrayOf(".ac", ".academy", ".accountant", ".accountants", ".actor", ".adult", ".ag", ".agency", ".ai", ".airforce", ".am", ".amsterdam", ".apartments", ".app", ".archi", ".army", ".art", ".asia", ".associates", ".at", ".attorney", ".au", ".auction", ".auto", ".autos", ".baby", ".band", ".bar", ".barcelona", ".bargains", ".bayern", ".be", ".beauty", ".beer", ".berlin", ".best", ".bet", ".bid", ".bike", ".bingo", ".bio", ".biz", ".biz.pl", ".black", ".blog", ".blue", ".boats", ".boston", ".boutique", ".build", ".builders", ".business", ".buzz", ".bz", ".ca", ".cab", ".cafe", ".camera", ".camp", ".capital", ".car", ".cards", ".care", ".careers", ".cars", ".casa", ".cash", ".casino", ".catering", ".cc", ".center", ".ceo", ".ch", ".charity", ".chat", ".cheap", ".church", ".city", ".cl", ".claims", ".cleaning", ".clinic", ".clothing", ".cloud", ".club", ".cn", ".co", ".co.in", ".co.jp", ".co.kr", ".co.nz", ".co.uk", ".co.za", ".coach", ".codes", ".coffee", ".college", ".com", ".com.ag", ".com.au", ".com.br", ".com.bz", ".com.cn", ".com.co", ".com.es", ".com.mx", ".com.pe", ".com.ph", ".com.pl", ".com.ru", ".com.tw", ".community", ".company", ".computer", ".condos", ".construction", ".consulting", ".contact", ".contractors", ".cooking", ".cool", ".country", ".coupons", ".courses", ".credit", ".creditcard", ".cricket", ".cruises", ".cymru", ".cz", ".dance", ".date", ".dating", ".de", ".deals", ".degree", ".delivery", ".democrat", ".dental", ".dentist", ".design", ".dev", ".diamonds", ".digital", ".direct", ".directory", ".discount", ".dk", ".doctor", ".dog", ".domains", ".download", ".earth", ".education", ".email", ".energy", ".engineer", ".engineering", ".enterprises", ".equipment", ".es", ".estate", ".eu", ".events", ".exchange", ".expert", ".exposed", ".express", ".fail", ".faith", ".family", ".fan", ".fans", ".farm", ".fashion", ".film", ".finance", ".financial", ".firm.in", ".fish", ".fishing", ".fit", ".fitness", ".flights", ".florist", ".fm", ".football", ".forsale", ".foundation", ".fr", ".fun", ".fund", ".furniture", ".futbol", ".fyi", ".gallery", ".games", ".garden", ".gay", ".gen.in", ".gg", ".gifts", ".gives", ".glass", ".global", ".gmbh", ".gold", ".golf", ".graphics", ".gratis", ".green", ".gripe", ".group", ".gs", ".guide", ".guru", ".hair", ".haus", ".health", ".healthcare", ".hockey", ".holdings", ".holiday", ".homes", ".horse", ".hospital", ".host", ".house", ".idv.tw", ".immo", ".immobilien", ".in", ".inc", ".ind.in", ".industries", ".info", ".info.pl", ".ink", ".institute", ".insure", ".international", ".investments", ".io", ".irish", ".ist", ".istanbul", ".it", ".jetzt", ".jewelry", ".jobs", ".jp", ".kaufen", ".kim", ".kitchen", ".kiwi", ".kr", ".la", ".land", ".law", ".lawyer", ".lease", ".legal", ".lgbt", ".life", ".lighting", ".limited", ".limo", ".live", ".llc", ".loan", ".loans", ".london", ".love", ".ltd", ".ltda", ".luxury", ".maison", ".makeup", ".management", ".market", ".marketing", ".mba", ".me", ".me.uk", ".media", ".melbourne", ".memorial", ".men", ".menu", ".miami", ".mobi", ".moda", ".moe", ".money", ".monster", ".mortgage", ".motorcycles", ".movie", ".ms", ".mx", ".nagoya", ".name", ".navy", ".ne.kr", ".net", ".net.ag", ".net.au", ".net.br", ".net.bz", ".net.cn", ".net.co", ".net.in", ".net.nz", ".net.pe", ".net.ph", ".net.pl", ".net.ru", ".network", ".news", ".ninja", ".nl", ".no", ".nom.co", ".nom.es", ".nom.pe", ".nrw", ".nyc", ".okinawa", ".one", ".onl", ".online", ".org", ".org.ag", ".org.au", ".org.cn", ".org.es", ".org.in", ".org.nz", ".org.pe", ".org.ph", ".org.pl", ".org.ru", ".org.uk", ".page", ".paris", ".partners", ".parts", ".party", ".pe", ".pet", ".ph", ".photography", ".photos", ".pictures", ".pink", ".pizza", ".pl", ".place", ".plumbing", ".plus", ".poker", ".porn", ".press", ".pro", ".productions", ".promo", ".properties", ".protection", ".pub", ".pw", ".quebec", ".quest", ".racing", ".re.kr", ".realestate", ".recipes", ".red", ".rehab", ".reise", ".reisen", ".rent", ".rentals", ".repair", ".report", ".republican", ".rest", ".restaurant", ".review", ".reviews", ".rich", ".rip", ".rocks", ".rodeo", ".ru", ".run", ".ryukyu", ".sale", ".salon", ".sarl", ".school", ".schule", ".science", ".se", ".security", ".services", ".sex", ".sg", ".sh", ".shiksha", ".shoes", ".shop", ".shopping", ".show", ".singles", ".site", ".ski", ".skin", ".soccer", ".social", ".software", ".solar", ".solutions", ".space", ".storage", ".store", ".stream", ".studio", ".study", ".style", ".supplies", ".supply", ".support", ".surf", ".surgery", ".sydney", ".systems", ".tax", ".taxi", ".team", ".tech", ".technology", ".tel", ".tennis", ".theater", ".theatre", ".tienda", ".tips", ".tires", ".today", ".tokyo", ".tools", ".tours", ".town", ".toys", ".top", ".trade", ".training", ".travel", ".tube", ".tv", ".tw", ".uk", ".university", ".uno", ".us", ".vacations", ".vegas", ".ventures", ".vet", ".viajes", ".video", ".villas", ".vin", ".vip", ".vision", ".vodka", ".vote", ".voto", ".voyage", ".wales", ".watch", ".webcam", ".website", ".wedding", ".wiki", ".win", ".wine", ".work", ".works", ".world", ".ws", ".wtf", ".xxx", ".xyz", ".yachts", ".yoga", ".yokohama", ".zone")

    /**
     * Draw element
     */
    override fun drawElement(partialTicks: Float): Border? {
        if (!HUD.state) return null
        val fontRenderer = fontValue.get()
        val textColor = textColor().rgb
        val backColor = backgroundColor().rgb

        val rectColorMode = rectColorModeValue.get()
        val rectCustomColor = Color(rectColorRedValue.get(), rectColorGreenValue.get(), rectColorBlueValue.get(),
            rectColorBlueAlpha.get()).rgb

        val worldScoreboard: Scoreboard = mc.theWorld.scoreboard
        var currObjective: ScoreObjective? = null
        val playerTeam = worldScoreboard.getPlayersTeam(mc.thePlayer.name)

        if (playerTeam != null) {
            val colorIndex = playerTeam.chatFormat.colorIndex

            if (colorIndex >= 0) {
                currObjective = worldScoreboard.getObjectiveInDisplaySlot(3 + colorIndex)
            }
        }

        val objective = currObjective ?: worldScoreboard.getObjectiveInDisplaySlot(1) ?: return null

        val scoreboard: Scoreboard = objective.scoreboard
        var scoreCollection = scoreboard.getSortedScores(objective)
        val scores = Lists.newArrayList(Iterables.filter(scoreCollection) { input ->
            input?.playerName != null && !input.playerName.startsWith("#")
        })

        scoreCollection = if (scores.size > 15) {
            Lists.newArrayList(Iterables.skip(scores, scoreCollection.size - 15))
        } else {
            scores
        }

        if (scoreCollection.size > 0) {

            var maxWidth = fontRenderer.getStringWidth(objective.displayName)

            for (score in scoreCollection) {
                val scorePlayerTeam = scoreboard.getPlayersTeam(score.playerName)
                val width = "${
                    ScorePlayerTeam.formatPlayerName(
                        scorePlayerTeam,
                        score.playerName
                    )
                }: ${EnumChatFormatting.RED}${score.scorePoints}"
                maxWidth = maxWidth.coerceAtLeast(fontRenderer.getStringWidth(width))
            }

            val maxHeight = scoreCollection.size * fontRenderer.FONT_HEIGHT
            val l1 = -maxWidth - 3 - if (rectValue.get()) 3 else 0

            // One single set of bounds for shadow / blur / background, so the three passes can
            // never drift apart (the blur used to be 10px narrower on LEFT side and the panel
            // edge did not line up with the score column).
            val bgLeft = l1.toFloat() - 2F
            val bgRight = 5F
            val bgTop = if (rectValue.get()) -3F else -2F
            val bgBottom = (maxHeight + fontRenderer.FONT_HEIGHT).toFloat()

            if (rainbowBarValue.get()) {
                Gui.drawRect(l1 - 2, -3, 5, -2, ColorUtils.rainbow().rgb)
            }
            if (shaderShadowEnabled) {
                GL11.glPushMatrix()
                GL11.glTranslated(-renderX, -renderY, 0.0)
                ShadowUtils.shadow(shadowStrength.get(), {
                    GL11.glPushMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                    GL11.glScalef(scale, scale, scale)
                    if (bgRoundedValue.get())
                        RenderUtils.originalRoundedRect(
                            bgLeft, bgTop, bgRight, bgBottom,
                            roundStrength.get(), ColorUtils.rainbow().rgb
                        )
                    else
                        RenderUtils.newDrawRect(
                            bgLeft, bgTop, bgRight, bgBottom, ColorUtils.rainbow().rgb
                        )
                    GL11.glPopMatrix()
                }, {
                    GL11.glPushMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                    GL11.glScalef(scale, scale, scale)
                    GlStateManager.enableBlend()
                    GlStateManager.disableTexture2D()
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    if (bgRoundedValue.get())
                        RenderUtils.fastRoundedRect(bgLeft, bgTop, bgRight, bgBottom, roundStrength.get())
                    else
                        RenderUtils.quickDrawRect(bgLeft, bgTop, bgRight, bgBottom)
                    GlStateManager.enableTexture2D()
                    GlStateManager.disableBlend()
                    GL11.glPopMatrix()
                })
                GL11.glPopMatrix()
            }
            if (blurValue2.get()) {
                GL11.glPushMatrix()
                GL11.glTranslated(-renderX, -renderY, 0.0)
                if (bgRoundedValue.get())
                    BlurUtils.blurAreaRounded(
                        (renderX + bgLeft * scale).toFloat(), (renderY + bgTop * scale).toFloat(),
                        (renderX + bgRight * scale).toFloat(), (renderY + bgBottom * scale).toFloat(),
                        roundStrength.get(), blurStrength.get()
                    )
                else
                    BlurUtils.blurArea(
                        (renderX + bgLeft * scale).toFloat(), (renderY + bgTop * scale).toFloat(),
                        (renderX + bgRight * scale).toFloat(), (renderY + bgBottom * scale).toFloat(),
                        blurStrength.get()
                    )
                GL11.glPopMatrix()
            }
            if (bgRoundedValue.get()) {
                Stencil.write(false)
                GlStateManager.enableBlend()
                GlStateManager.disableTexture2D()
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                RenderUtils.fastRoundedRect(bgLeft, bgTop, bgRight, bgBottom, roundStrength.get())
                GlStateManager.enableTexture2D()
                GlStateManager.disableBlend()
                Stencil.erase(true)
            }
            Gui.drawRect(bgLeft.toInt(), bgTop.toInt(), bgRight.toInt(), bgBottom.toInt(), backColor)
                //    RenderUtils.drawShadow(l1 - 2f, -2f, 5f - (l1 - 2f), maxHeight + fontRenderer.FONT_HEIGHT.toFloat() + 2f)

            scoreCollection.forEachIndexed { index, score ->
                val team = scoreboard.getPlayersTeam(score.playerName)

                var name = ScorePlayerTeam.formatPlayerName(team, score.playerName)
                val scorePoints = "${EnumChatFormatting.RED}${score.scorePoints}"

                val width = 5 - if (rectValue.get()) 4 else 0
                val height = maxHeight - index * fontRenderer.FONT_HEIGHT

                GlStateManager.resetColor()

                var listColor = textColor
                if (!serverValue.equals("none") && !serverValue.get().equals("Funny")) {
                    for (domain in allowedDomains) {
                        if (name.contains(domain, true)) {
                            name = when (serverValue.get().lowercase()) {
                                "clientname" -> LiquidBounce.COLORED_NAME
                                "website" -> LiquidBounce.CLIENT_WEBSITE
                                else -> "null"
                            }
                            listColor = ColorUtils.rainbow().rgb
                            break
                        }
                    }
                }
                if (serverValue.get().equals("Funny")) {
                    if (name.contains("BlocksMC.com", true)) name = "StaffMC.net"
                    else if (name.contains("mc.hypixel.net", true)) name = "mc.lagpixel.net"
                    listColor = ColorUtils.rainbow().rgb
                }

                fontRenderer.drawString(name, l1.toFloat(), height.toFloat(), listColor, shadowValue.get())
                if (!noPointValue.get()) {
                    fontRenderer.drawString(
                        scorePoints,
                        (width - fontRenderer.getStringWidth(scorePoints)).toFloat(),
                        height.toFloat(),
                        textColor,
                        shadowValue.get()
                    )
                }

                if (index == scoreCollection.size - 1) {
                    val displayName = objective.displayName

                    GlStateManager.resetColor()

                    fontRenderer.drawString(
                        displayName,
                        (l1 + maxWidth / 2 - fontRenderer.getStringWidth(displayName) / 2).toFloat(),
                        (height -
                                fontRenderer.FONT_HEIGHT).toFloat(),
                        textColor,
                        shadowValue.get()
                    )
                }

                if (rectValue.get()) {
                    val rectColor = when {
                        rectColorMode.equals("Rainbow", ignoreCase = true) -> ColorUtils.rainbow(index).rgb
                        else -> rectCustomColor
                    }
                    RenderUtils.drawRect(
                        2F,
                        if (index == scoreCollection.size - 1) -2F else height.toFloat(),
                        5F,
                        if (index == 0) fontRenderer.FONT_HEIGHT.toFloat() else height.toFloat() + fontRenderer.FONT_HEIGHT * 2F,
                        rectColor
                    )
                    if (rectBlurSet.equals(true)) {
                        RenderUtils.drawShadow(
                            2F,
                            if (index == scoreCollection.size - 1) -2F else height.toFloat(),
                            3F,
                            if (index == 0) fontRenderer.FONT_HEIGHT.toFloat() else height.toFloat() + fontRenderer.FONT_HEIGHT * 2F - if (index == scoreCollection.size - 1) -2F else height.toFloat()
                        )
                    }

                }
            }

            if (bgRoundedValue.get())
                Stencil.dispose()

            return Border(
                -maxWidth.toFloat() - 5 - if (rectValue.get()) 3 else 0,
                -2F,
                5F,
                maxHeight.toFloat() + fontRenderer.FONT_HEIGHT
            )
        }
        else return Border(0f,0f,0f,0f)
    }

    private fun backgroundColor() = Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(),
        backgroundColorBlueValue.get(), backgroundColorAlphaValue.get())

    private fun textColor() = Color(textRedValue.get(), textGreenValue.get(),
        textBlueValue.get())
}