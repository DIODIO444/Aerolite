package net.ccbluex.liquidbounce.ui.cape

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.font.drawCenteredString
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import org.lwjgl.opengl.GL11
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

/**
 * Local cape selection screen, ported from Aerolite's `ui.cape.GuiCapeManager`.
 */
object GuiCapeManager : GuiScreen() {

    private val jsonFile = File(FileManager.capesDir, "cape.json")

    val FDP_CAPE_1 = loadCapeFromResource("半裸纱雾酱", "assets/minecraft/Capes/cape1.png")
    val FDP_CAPE_2 = loadCapeFromResource("半裸胡桃", "assets/minecraft/Capes/cape2.png")
    val FDP_CAPE_3 = loadCapeFromResource("半裸萝莉", "assets/minecraft/Capes/cape3.png")
    val FDP_CAPE_4 = loadCapeFromResource("粉毛", "assets/minecraft/Capes/cape4.png")
    val FDP_CAPE_5 = loadCapeFromResource("蓝毛", "assets/minecraft/Capes/cape5.png")
    val FDP_CAPE_6 = loadCapeFromResource("RICE Loser", "assets/minecraft/Capes/cape6.png")
    val FDP_CAPE_7 = loadCapeFromResource("侮华客户端 Cape1", "assets/minecraft/Capes/cape7.png")
    val FDP_CAPE_8 = loadCapeFromResource("侮华客户端 Cape2", "assets/minecraft/Capes/cape8.png")
    val FDP_CAPE_9 = loadCapeFromResource("NovoLine Cape", "assets/minecraft/Capes/cape9.png")
    val FDP_CAPE_10 = loadCapeFromResource("白毛死库水", "assets/minecraft/Capes/cape10.png")
    val FDP_CAPE_11 = loadCapeFromResource("白毛泳装", "assets/minecraft/Capes/cape11.png")
    val FDP_CAPE_12 = loadCapeFromResource("泳装-双倍", "assets/minecraft/Capes/cape12.png")

    var nowCape: Cape? = FDP_CAPE_1

    val capeList = mutableListOf<Cape>()

    private val defaultCapes
        get() = listOf(
            FDP_CAPE_1, FDP_CAPE_2, FDP_CAPE_3, FDP_CAPE_4, FDP_CAPE_5, FDP_CAPE_6,
            FDP_CAPE_7, FDP_CAPE_8, FDP_CAPE_9, FDP_CAPE_10, FDP_CAPE_11, FDP_CAPE_12
        )

    init {
        capeList.addAll(defaultCapes)
    }

    /**
     * Reloads the built in capes, the files dropped into `capes/` and the saved selection.
     */
    fun load() {
        capeList.clear()
        capeList.addAll(defaultCapes)

        FileManager.capesDir.listFiles()?.forEach { file ->
            if (file.isFile && !file.name.equals(jsonFile.name)) {
                try {
                    val name = file.name.substringBeforeLast(".")
                    capeList.add(loadCapeFromFile(name, file))
                } catch (throwable: Throwable) {
                    ClientUtils.LOGGER.error("Occurred an error while loading cape from file: ${file.name}", throwable)
                }
            }
        }

        if (!jsonFile.exists()) {
            return
        }

        try {
            val json = FileManager.PRETTY_GSON.fromJson(jsonFile.reader(Charsets.UTF_8), JsonObject::class.java)

            if (json.has("name")) {
                val name = json.get("name").asString

                if (name != "NONE") {
                    capeList.firstOrNull { it.name == name }?.let { nowCape = it }
                } else {
                    nowCape = null
                }
            }
        } catch (throwable: Throwable) {
            ClientUtils.LOGGER.error("Failed to load cape selection", throwable)
        }
    }

    fun save() {
        val json = JsonObject()

        json.addProperty("name", nowCape?.name ?: "NONE")

        jsonFile.writeText(FileManager.PRETTY_GSON.toJson(json), Charsets.UTF_8)
    }

    private fun loadCapeFromResource(name: String, loc: String): Cape {
        val image = runCatching {
            GuiCapeManager::class.java.classLoader.getResourceAsStream(loc)?.use { ImageIO.read(it) }
        }.getOrNull() ?: BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)

        return Cape(name, image)
    }

    private fun loadCapeFromFile(name: String, file: File) = Cape(name, ImageIO.read(file))

    @Suppress("unused")
    private fun loadCapeFromURL(name: String, url: URL) = Cape(name, ImageIO.read(url))

    override fun onGuiClosed() {
        save()
    }

    override fun initGui() {
        buttonList.clear()
        buttonList.add(GuiButton(0, 0, 0, mc.fontRendererObj.getStringWidth("< QUIT") + 10, 20, "< QUIT"))
        buttonList.add(
            GuiButton(1, (width * 0.3).toInt(), (height * 0.5).toInt(), mc.fontRendererObj.getStringWidth("<-") + 10, 20, "<-")
        )
        buttonList.add(
            GuiButton(2, (width * 0.7).toInt(), (height * 0.5).toInt(), mc.fontRendererObj.getStringWidth("->") + 10, 20, "->")
        )
    }

    override fun actionPerformed(button: GuiButton) {
        fun next(index: Int) {
            var chooseIndex = index

            if (chooseIndex >= capeList.size) {
                chooseIndex = -1
            }

            if (chooseIndex < -1) {
                chooseIndex = capeList.size - 1
            }

            nowCape = if (chooseIndex != -1) capeList[chooseIndex] else null
        }

        when (button.id) {
            0 -> mc.displayGuiScreen(null)
            1 -> next(capeList.indexOf(nowCape) - 1)
            2 -> next(capeList.indexOf(nowCape) + 1)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        GL11.glPushMatrix()
        mc.fontRendererObj.drawCenteredString(
            if (nowCape == null) "§cNONE" else "§a${nowCape!!.name}",
            width * 0.50f, height * 0.23f, -1, false
        )
        GL11.glScalef(2f, 2f, 2f)
        mc.fontRendererObj.drawCenteredString("Cape Manager", width * 0.25f, height * 0.03f, -1, false)
        GL11.glPopMatrix()

        super.drawScreen(mouseX, mouseY, partialTicks)

        val player = mc.thePlayer ?: return

        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)
        GlStateManager.enableColorMaterial()
        GlStateManager.pushMatrix()
        GL11.glTranslatef(width * 0.5f - 60, height * 0.3f, 0f)
        GL11.glScalef(2f, 2f, 2f)
        GL11.glTranslatef(30f, 100f, 0f)
        GlStateManager.translate(0F, 0F, 50F)
        GlStateManager.scale(-50F, 50F, 50F)
        GlStateManager.rotate(180F, 0F, 0F, 1F)

        val renderYawOffset = player.renderYawOffset
        val rotationYaw = player.rotationYaw
        val rotationPitch = player.rotationPitch
        val prevRotationYawHead = player.prevRotationYawHead
        val rotationYawHead = player.rotationYawHead
        val armor0 = player.inventory.armorInventory[0]
        val armor1 = player.inventory.armorInventory[1]
        val armor2 = player.inventory.armorInventory[2]
        val armor3 = player.inventory.armorInventory[3]
        val current = player.inventory.mainInventory[player.inventory.currentItem]

        GlStateManager.rotate(135F, 0F, 1F, 0F)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135F, 0F, 1F, 0F)
        GlStateManager.rotate(0f, 1F, 0F, 0F)

        player.renderYawOffset = 180f
        player.rotationYaw = 180f
        player.rotationPitch = 0f
        player.rotationYawHead = player.rotationYaw
        player.prevRotationYawHead = player.rotationYaw
        player.inventory.armorInventory[0] = null
        player.inventory.armorInventory[1] = null
        player.inventory.armorInventory[2] = null
        player.inventory.armorInventory[3] = null
        player.inventory.mainInventory[player.inventory.currentItem] = null

        GlStateManager.translate(0F, 0F, 0F)

        val renderManager = mc.renderManager
        renderManager.setPlayerViewY(180F)
        renderManager.isRenderShadow = false
        renderManager.renderEntityWithPosYaw(player, 0.0, 0.0, 0.0, 0F, 1F)
        renderManager.isRenderShadow = true

        player.renderYawOffset = renderYawOffset
        player.rotationYaw = rotationYaw
        player.rotationPitch = rotationPitch
        player.prevRotationYawHead = prevRotationYawHead
        player.rotationYawHead = rotationYawHead
        player.inventory.armorInventory[0] = armor0
        player.inventory.armorInventory[1] = armor1
        player.inventory.armorInventory[2] = armor2
        player.inventory.armorInventory[3] = armor3
        player.inventory.mainInventory[player.inventory.currentItem] = current

        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
        GlStateManager.resetColor()
    }

    override fun doesGuiPauseGame() = false
}
