package net.ccbluex.liquidbounce.utils.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/**
 * The vanilla dim gradient that {@code GuiScreen#drawWorldBackground} paints behind every screen,
 * with a controllable opacity.
 *
 * <p>Colours are copied verbatim from vanilla ({@code 0xC0101010} → {@code 0xD0101010}) so the
 * overlay is indistinguishable from the original when {@code alpha == 1}.</p>
 */
public final class DimOverlay {

    private static final int START_COLOR = 0xC0101010;
    private static final int END_COLOR = 0xD0101010;

    /** Draws the full-screen dim overlay at the given opacity (0 = invisible, 1 = vanilla). */
    public static void draw(int width, int height, float alpha) {
        if (alpha <= 0F)
            return;

        final float clamped = Math.min(alpha, 1F);
        drawGradient(0, 0, width, height,
                scaleAlpha(START_COLOR, clamped),
                scaleAlpha(END_COLOR, clamped));
    }

    private static int scaleAlpha(int color, float factor) {
        final int alpha = (int) (((color >>> 24) & 0xFF) * factor);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    /** Verbatim copy of {@code Gui.drawGradientRect}. */
    private static void drawGradient(int left, int top, int right, int bottom, int startColor, int endColor) {
        final float startA = (float) (startColor >> 24 & 255) / 255.0F;
        final float startR = (float) (startColor >> 16 & 255) / 255.0F;
        final float startG = (float) (startColor >> 8 & 255) / 255.0F;
        final float startB = (float) (startColor & 255) / 255.0F;
        final float endA = (float) (endColor >> 24 & 255) / 255.0F;
        final float endR = (float) (endColor >> 16 & 255) / 255.0F;
        final float endG = (float) (endColor >> 8 & 255) / 255.0F;
        final float endB = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);

        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(right, top, 0.0D).color(startR, startG, startB, startA).endVertex();
        renderer.pos(left, top, 0.0D).color(startR, startG, startB, startA).endVertex();
        renderer.pos(left, bottom, 0.0D).color(endR, endG, endB, endA).endVertex();
        renderer.pos(right, bottom, 0.0D).color(endR, endG, endB, endA).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private DimOverlay() {
    }
}
