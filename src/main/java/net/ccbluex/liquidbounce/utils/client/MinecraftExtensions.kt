/*
 * Aerolite — FDPClient framework port
 * Kotlin-visible accessor for Minecraft's private sendClickBlockToController,
 * routed through the IMixinMinecraft invoker mixin.
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.injection.implementations.IMixinMinecraft
import net.minecraft.client.Minecraft

fun Minecraft.sendClickBlockToController(clicked: Boolean) {
    (this as IMixinMinecraft).invokeSendClickBlockToController(clicked)
}
