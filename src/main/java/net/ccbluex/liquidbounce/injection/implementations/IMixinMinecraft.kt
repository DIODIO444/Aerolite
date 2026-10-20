/*
 * Aerolite — FDPClient framework port
 * Interface injected into net.minecraft.client.Minecraft via MixinMinecraft, exposing
 * otherwise-private members used by migrated code.
 */
package net.ccbluex.liquidbounce.injection.implementations

interface IMixinMinecraft {
    fun invokeSendClickBlockToController(clicked: Boolean)
}
