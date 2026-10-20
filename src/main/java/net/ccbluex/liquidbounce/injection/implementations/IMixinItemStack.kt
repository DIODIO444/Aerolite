/*
 * Aerolite — FDPClient framework port
 * IMixinItemStack interface injected into net.minecraft.item.ItemStack via MixinItemStack.
 */
package net.ccbluex.liquidbounce.injection.implementations

interface IMixinItemStack {
    val itemDelay: Long
}
