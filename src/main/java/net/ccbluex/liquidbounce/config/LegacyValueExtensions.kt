/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.config

/**
 * Aerolite-compatible aliases for the [Value] chain API.
 *
 * The ported modules declare settings as
 * `val x = BoolValue("X", true).displayable { someCondition }`, so [displayable] has to exist on the
 * native [Value] type. It forwards to [Value.setSupport], which is what the ClickGUI already uses to
 * decide whether a setting is shown.
 */
fun <V : Value<*>> V.displayable(condition: () -> Boolean): V = apply {
    setSupport { condition() }
}

/**
 * No-op description setter kept for source compatibility with the ported modules.
 */
fun <V : Value<*>> V.describe(text: String): V = this
