/*
 * Aerolite — compatibility shim.
 *
 * The ported rendering engine (utils/render) originates from a code base where
 * MinecraftInstance was a plain Java class exposing a static `mc`. This project keeps
 * net.ccbluex.liquidbounce.utils.client.MinecraftInstance as the canonical Kotlin
 * interface, so this class only exists to keep the ported sources compiling unchanged.
 */
package net.ccbluex.liquidbounce.utils;

import net.minecraft.client.Minecraft;

public class MinecraftInstance {
    protected static final Minecraft mc = Minecraft.getMinecraft();
}
