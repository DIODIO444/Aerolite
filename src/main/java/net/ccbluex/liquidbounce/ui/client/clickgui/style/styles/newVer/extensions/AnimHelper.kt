package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newVer.extensions

import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
  

fun Float.animSmooth(target: Float, speed: Float) = if (ClickGUI.fastRender) target else AnimationUtils.animate(target, this, speed * RenderUtils.deltaTime * 0.025F)
fun Float.animLinear(speed: Float, min: Float, max: Float) = if (ClickGUI.fastRender) { if (speed < 0F) min else max } else (this + speed).coerceIn(min, max)