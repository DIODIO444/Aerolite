/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.Aerolite
import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.minecraft.client.settings.GameSettings
import org.lwjgl.opengl.Display
import net.minecraft.network.NetworkManager
import net.minecraft.network.login.client.C01PacketEncryptionResponse
import net.minecraft.network.login.server.S01PacketEncryptionRequest
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Field
import java.security.PublicKey
import javax.crypto.SecretKey

@SideOnly(Side.CLIENT)
object ClientUtils : MinecraftInstance {
    private var fastRenderField: Field? = runCatching {
        GameSettings::class.java.getDeclaredField("ofFastRender")
    }.getOrNull()

    var runTimeTicks = 0

    var profilerName = ""

    val LOGGER: Logger = LogManager.getLogger("LiquidBounce")

    /** Window title shown while Forge is still injecting the core (Aerolite splash). */
    fun setLoadingTitle() {
        Display.setTitle("${CLIENT_NAME}正在注入核心,请稍后!")
    }

    /** Final window title: "Aerolite 6.0.0 | <random suffix>" like the reference client. */
    fun setClientTitle() {
        Display.setTitle(Aerolite.title + Aerolite.titles.random())
    }

    fun disableFastRender() {
        fastRenderField?.let {
            if (!it.isAccessible)
                it.isAccessible = true

            it.setBoolean(mc.gameSettings, false)
        }
    }

    fun sendEncryption(
        networkManager: NetworkManager,
        secretKey: SecretKey?,
        publicKey: PublicKey?,
        encryptionRequest: S01PacketEncryptionRequest
    ) {
        networkManager.sendPacket(C01PacketEncryptionResponse(secretKey, publicKey, encryptionRequest.verifyToken),
            { networkManager.enableEncryption(secretKey) }
        )
    }

    /** Prefixed alert message (Aerolite compatibility). */
    @JvmStatic
    fun displayAlert(message: String) = displayChatMessage(message)

    fun displayChatMessage(message: String) {
        mc.thePlayer?.addChatMessage(ChatComponentText("§8[§9§l$CLIENT_NAME§8]§r $message"))
            ?: LOGGER.info("(MCChat) $message")
    }

    /** Hit test helper used by the ported Aerolite click GUIs. */
    @JvmStatic
    fun mouseWithinBounds(mouseX: Int, mouseY: Int, x: Float, y: Float, x2: Float, y2: Float) =
        mouseX >= x && mouseX < x2 && mouseY >= y && mouseY < y2
}

fun chat(message: String) = ClientUtils.displayChatMessage(message)