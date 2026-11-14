package net.ccbluex.liquidbounce.utils.render;

import net.minecraft.client.renderer.GlStateManager;

/**
 * Subset of Aerolite's `DrRenderUtils` used by the ported standalone click GUIs.
 */
public final class DrRenderUtils {

    private DrRenderUtils() {
    }

    public static void color(int color) {
        color(color, 1F);
    }

    public static void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, alpha);
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, (float) ((double) limit * 0.01D));
    }
}
