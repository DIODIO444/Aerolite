package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.minecraft.util.ResourceLocation

object MotionBlur : Module("MotionBlur", Category.RENDER) {
    private val modeValue = choices("Mode", arrayOf("Little", "Normal", "Large"), "Little")

    /** Mode whose shader is currently loaded, `null` when no shader is active. */
    private var loadedMode: String? = null
    private var pausedForGui = false

    override fun onEnable() {
        loadedMode = null
        pausedForGui = false
        applyShader()
    }

    override fun onDisable() {
        stopShader()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // EntityRenderer.loadShader() allocates a new ShaderGroup and framebuffer every call, so
        // reloading it once per tick made the whole screen flicker. Only react to real changes.
        if (mc.currentScreen is ClickGui) {
            if (!pausedForGui) {
                stopShader()
                pausedForGui = true
            }
            return
        }

        pausedForGui = false
        applyShader()
    }

    private fun applyShader() {
        val mode = modeValue.get()
        if (mode == loadedMode) {
            return
        }

        val path = when (mode) {
            "Normal" -> "aerolite/shader/blur/normal.json"
            "Large" -> "aerolite/shader/blur/large.json"
            else -> "aerolite/shader/blur/little.json"
        }

        mc.entityRenderer.loadShader(ResourceLocation(path))
        loadedMode = mode
    }

    private fun stopShader() {
        if (loadedMode != null) {
            mc.entityRenderer.stopUseShader()
            loadedMode = null
        }
    }
}
