/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 *
 * 1.8.9 exposes PlayerControllerMP#syncCurrentPlayItem as a private method, while the ported
 * FDPClient code expects it to be callable from item/block interaction helpers. The mixin
 * MixinPlayerControllerMP exposes it through IMixinPlayerControllerMP#invokeSyncCurrentPlayItem,
 * so this extension simply bridges to that invoker.
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.injection.implementations.IMixinPlayerControllerMP
import net.minecraft.client.multiplayer.PlayerControllerMP

/**
 * Syncs the currently held item with the server, mirroring `PlayerControllerMP#syncCurrentPlayItem`.
 */
fun PlayerControllerMP.syncCurrentPlayItem() {
    (this as? IMixinPlayerControllerMP)?.invokeSyncCurrentPlayItem()
}
