/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.util.ResourceLocation

/**
 * Minimal Aerolite port of FDPClient's APIConnectorUtils.
 *
 * The upstream client downloads wing/marker pictures from a remote API; Aerolite has no such
 * backend, so [callImage] resolves to the bundled local texture instead. The public surface
 * (cached [ResourceLocation] lookup by image name + category) is preserved so ported render
 * code keeps compiling and working.
 */
object APIConnectorUtils : MinecraftInstance {

    private val picturesCache = mutableMapOf<Pair<String, String>, ResourceLocation>()

    /**
     * Retrieves the [ResourceLocation] for a specific image and location.
     *
     * @param image The name of the image.
     * @param location The category/location of the image.
     * @return The corresponding [ResourceLocation], or a default one if not found.
     */
    fun callImage(image: String, location: String): ResourceLocation =
        picturesCache.getOrPut(Pair(image, location)) {
            ResourceLocation("aerolite/misc/wings.png")
        }
}
