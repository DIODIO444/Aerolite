/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.EaseUtils

/**
 * Animations module
 *
 * Carries every setting of the Aerolite animations feature set. The rendering itself lives in
 * [net.ccbluex.liquidbounce.injection.forge.mixins.item.MixinItemRenderer] (first person / blocking)
 * and in the Gui mixins (inventory + tab overlay open animations).
 *
 * The module is intentionally not toggleable: like in the original client the settings are always
 * applied, so the module is just a settings container.
 */
object Animations : Module("Animations", Category.RENDER, canBeEnabled = false), MinecraftInstance {

    /* ------------------------------ blocking ------------------------------ */

    val blockingMode by choices(
        "BlockingMode",
        arrayOf(
            "Akrien", "Avatar", "ETB", "Exhibition", "Push", "Reverse",
            "Shield", "SigmaNew", "SigmaOld", "Slide", "SlideDown", "HSlide", "Swong", "VisionFX",
            "Swank", "Jello", "Rotate", "Liquid", "Yeet", "Yeet2",
            "None"
        ),
        "SlideDown"
    )

    /* ----------------------------- inventory ----------------------------- */

    val invMode by choices("InvMode", arrayOf("None", "Slide", "Zoom"), "Slide")

    val invEaseMode by choices(
        "InvEase",
        EaseUtils.EnumEasingType.entries.map { it.toString() }.toTypedArray(),
        EaseUtils.EnumEasingType.SINE.toString()
    )

    val invEaseOrder by choices(
        "InvEaseOrder",
        EaseUtils.EnumEasingOrder.entries.map { it.toString() }.toTypedArray(),
        EaseUtils.EnumEasingOrder.FAST_AT_START.toString()
    )

    val invTime by int("InvTime", 500, 0..1000, isSupported = { invMode != "None" })

    /* -------------------------------- tab -------------------------------- */

    val tabShowPlayerSkin by boolean("TabShowPlayerSkin", false)

    val tabMode by choices("TabMode", arrayOf("None", "UpSlide", "DownSlide", "Zoom"), "Zoom")

    val tabEaseMode by choices(
        "TabEase",
        EaseUtils.EnumEasingType.entries.map { it.toString() }.toTypedArray(),
        EaseUtils.EnumEasingType.SINE.toString()
    )

    val tabEaseOrder by choices(
        "TabEaseOrder",
        EaseUtils.EnumEasingOrder.entries.map { it.toString() }.toTypedArray(),
        EaseUtils.EnumEasingOrder.FAST_AT_START.toString()
    )

    val tabTime by int("TabTime", 500, 0..1000, isSupported = { tabMode != "None" })

    /* --------------------------- item transform --------------------------- */

    val translateX by float("TranslateX", 0F, 0F..1.5F)
    val translateY by float("TranslateY", 0F, 0F..0.5F)
    val translateZ by float("TranslateZ", 0F, -2F..0F)

    val rotateX by float("RotateX", 0F, -180F..180F)
    val rotateY by float("RotateY", 0F, -180F..180F)
    val rotateZ by float("RotateZ", 0F, -180F..180F)

    val itemPosX by float("ItemPosX", 0.56F, -1F..1F)
    val itemPosY by float("ItemPosY", -0.52F, -1F..1F)
    val itemPosZ by float("ItemPosZ", -0.71999997F, -1F..1F)
    val itemScale by float("ItemScale", 0.4F, 0F..2F)

    val swingAnim by boolean("SwingAnim", false)
    val swingSpeed by float("SwingSpeed", 1F, 0.5F..5F)
    val anythingBlock by boolean("AnythingBlock", false)

    /* --------------------------- item rotation --------------------------- */

    val rotateMode by choices("RotateMode", arrayOf("RotateY", "RotateXY", "Custom", "None"), "RotateY")

    val rotateItems by boolean("RotateItems", false)
    val rotateSpeed by float("RotateSpeed", 1F, 0F..10F, isSupported = { rotateItems })

    val customRotateX by float(
        "RotateXAxis", 0F, -180F..180F,
        isSupported = { rotateItems && rotateMode.equals("Custom", ignoreCase = true) }
    )
    val customRotateY by float(
        "RotateYAxis", 0F, -180F..180F,
        isSupported = { rotateItems && rotateMode.equals("Custom", ignoreCase = true) }
    )
    val customRotateZ by float(
        "RotateZAxis", 0F, -180F..180F,
        isSupported = { rotateItems && rotateMode.equals("Custom", ignoreCase = true) }
    )

    val viewBobbingYaw by float("ViewBobbingYaw", 0.1F, 0F..0.5F)

    /* ------------------------------ tab state ------------------------------ */

    var flagRenderTabOverlay = false
        get() = field && tabShowPlayerSkin

    var tabPercent = 0F
    var tabHopePercent = 0F
    var lastTabSync = 0L

    /**
     * View-bobbing sway amount.
     *
     * NOTE: in the reference client this handler could never run — [Animations] is a settings
     * container (`canBeEnabled = false`), so `state` stays false and the `@EventTarget`
     * dispatcher skipped it. It must stay state-driven here as well.
     *
     * Writing [net.minecraft.client.entity.EntityPlayerSP.cameraYaw] from the *start* of the tick
     * is not harmless: `EntityLivingBase.onLivingUpdate` then copies it to `prevCameraYaw` and
     * smooths `cameraYaw` towards the player's motion. `EntityRenderer.setupViewBobbing`
     * interpolates both values with `partialTicks`, so resetting them every tick makes the
     * bobbing amplitude jump back at each tick boundary — a 20 Hz wobble of the whole camera that
     * is visible as screen jitter (stronger while walking/sprinting).
     */
    val onUpdate = handler<UpdateEvent> {
        mc.thePlayer?.let {
            it.cameraYaw = viewBobbingYaw
            it.prevCameraYaw = viewBobbingYaw
        }
    }
}
