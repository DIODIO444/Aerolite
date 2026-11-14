package net.ccbluex.liquidbounce.ui.client

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.fisproxy.FisProxyClient
import net.ccbluex.liquidbounce.utils.fisproxy.string
import net.ccbluex.liquidbounce.utils.fisproxy.stringOrNull
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import java.awt.Color
import java.io.File
import java.util.Collections

class GuiFisProxy(private val prevGui: GuiScreen) : AbstractScreen() {
    private lateinit var tokenField: GuiTextField
    private val lines = Collections.synchronizedList(ArrayList<String>())
    private var lastAddress: String? = null
    private var busy = false

    override fun initGui() {
        tokenField = GuiTextField(0, mc.fontRendererObj, width / 2 - 150, height / 4 + 8, 300, 20)
        tokenField.maxStringLength = 512
        tokenField.text = loadToken() ?: ""

        val x = width / 2 - 150
        val y = height / 4 + 36

        +GuiButton(0, x, height / 4 + 36, 96, 20, "获取资料")
        +GuiButton(1, x + 102, y, 96, 20, "状态")
        +GuiButton(2, x + 204, y, 96, 20, "开始加速")
        +GuiButton(3, x, y + 24, 96, 20, "换 IP")
        +GuiButton(4, x + 102, y + 24, 96, 20, "停止")
        +GuiButton(5, x + 204, y + 24, 96, 20, "入口列表")
        +GuiButton(6, x, y + 48, 96, 20, "复制地址")
        +GuiButton(7, x + 204, y + 48, 96, 20, "返回")
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        AWTFontRenderer.assumeNonVolatile {
            drawBackground(0)
            Fonts.fontBold180.drawCenteredString(
                "FisProxy", width / 2.0F, height / 8.0F + 5.0F, 4673984, true
            )
            Fonts.fontSemibold40.drawString(
                "API Token", width / 2.0F - 150.0F, height / 4.0F - 6.0F, Color.GRAY.rgb
            )

            tokenField.drawTextBox()

            RenderUtils.drawRect(
                width / 2.0F - 152.0F,
                height / 4.0F + 74.0F,
                width / 2.0F + 152.0F,
                height - 30.0F,
                Int.MIN_VALUE
            )

            var y = height / 4.0F + 80.0F

            val snapshot = synchronized(lines) { lines.toList() }

            for (line in snapshot.takeLast(16)) {
                Fonts.fontSemibold40.drawString(line, width / 2.0F - 146.0F, y, Color.WHITE.rgb)
                y += Fonts.fontSemibold40.fontHeight.toFloat()
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> submit("资料") { client ->
                val user = client.me().getAsJsonObject("user")
                "user=${user.stringOrNull("username") ?: "?"} balances=${user.get("balances")} session=${user.get("session")?.toString() ?: "-"}"
            }

            1 -> submit("状态") { client ->
                val status = client.status()
                val address = status.stringOrNull("address") ?: status.stringOrNull("entrance")

                if (address != null) {
                    lastAddress = address
                }

                "running=${status.get("running")} entrance=${status.stringOrNull("entrance") ?: "-"} address=$address"
            }

            2 -> submit("开始加速") { client ->
                val done = client.waitOperation(client.start().string("id"))
                val entrances = done.getAsJsonObject("result")?.get("entrances")
                "status=${done.string("status")} result=${done.get("result")} entrances=$entrances"
            }

            3 -> submit("换 IP") { client ->
                val done = client.waitOperation(client.changeIp().string("id"))
                "status=${done.string("status")} result=${done.get("result")}"
            }

            4 -> submit("停止") { client ->
                val result = client.stop()
                "stopped ${result.get("duration") ?: ""} ${result.get("charge") ?: ""} raw=$result"
            }

            5 -> submit("入口列表") { client ->
                client.entrances().get("entrances")?.toString() ?: "none"
            }

            6 -> {
                val address = lastAddress

                if (address != null) {
                    GuiScreen.setClipboardString(address)
                    addLine("§a已复制: §f$address")
                } else {
                    addLine("§c还没有地址，先执行“状态”或“开始加速”")
                }
            }

            7 -> mc.displayGuiScreen(prevGui)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == 1) {
            saveToken(tokenField.text.trim())
            mc.displayGuiScreen(prevGui)
        } else {
            tokenField.textboxKeyTyped(typedChar, keyCode)
            super.keyTyped(typedChar, keyCode)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        tokenField.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun onGuiClosed() {
        saveToken(tokenField.text.trim())
        super.onGuiClosed()
    }

    private fun submit(label: String, block: (FisProxyClient) -> String) {
        if (busy) {
            addLine("§e上一个请求还在进行中…")
            return
        }

        val token = tokenField.text.trim()

        if (token.isEmpty()) {
            addLine("§c请先填写 API 令牌（在用户中心 API 页面创建）")
            return
        }

        saveToken(token)
        busy = true
        addLine("§7> $label …")

        Thread {
            try {
                addLine("§a$label: §f${block(FisProxyClient(token))}")
            } catch (e: Exception) {
                addLine("§c$label 失败: §f${e.message ?: e.javaClass.simpleName}")
            } finally {
                busy = false
            }
        }.apply { isDaemon = true }.start()
    }

    private fun addLine(line: String) {
        synchronized(lines) {
            lines.add(line)

            while (lines.size > 40) {
                lines.removeAt(0)
            }
        }
    }

    private fun tokenFile(): File = File(FileManager.dir, "fisproxy.json")

    private fun loadToken(): String? = runCatching {
        JsonParser().parse(tokenFile().readText()).asJsonObject.stringOrNull("token")
    }.getOrNull()

    private fun saveToken(token: String) {
        runCatching {
            val json = JsonObject()
            json.addProperty("token", token)
            tokenFile().writeText(json.toString())
        }
    }
}
