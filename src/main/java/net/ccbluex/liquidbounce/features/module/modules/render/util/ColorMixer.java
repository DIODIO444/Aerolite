/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 *
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.render.util;

import java.awt.Color;

/**
 * Colour mixer used by the HUD "Mixer" colour modes.
 *
 * The original implementation was a b100 module backed by {@code ColorElement}s. The HUD only needs
 * the generated gradient, so this version keeps the same static API while computing the palette
 * directly.
 */
public final class ColorMixer {

    private static final Color[] PALETTE = new Color[]{
            new Color(255, 0, 0),
            new Color(255, 255, 0),
            new Color(0, 255, 0),
            new Color(0, 255, 255),
            new Color(0, 0, 255),
            new Color(255, 0, 255),
            new Color(255, 0, 0)
    };

    private ColorMixer() {
    }

    /**
     * Samples the mixer gradient.
     *
     * @param index   offset in milliseconds
     * @param seconds cycle length in seconds
     */
    public static Color getMixedColor(int index, int seconds) {
        if (seconds <= 0) {
            return Color.WHITE;
        }

        final long cycle = Math.max(1L, seconds * 1000L);
        final float progress = (float) (((System.currentTimeMillis() + index) % cycle + cycle) % cycle) / cycle;

        final float scaled = progress * (PALETTE.length - 1);
        final int from = Math.min((int) scaled, PALETTE.length - 2);
        final float t = scaled - from;

        final Color a = PALETTE[from];
        final Color b = PALETTE[from + 1];

        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t)
        );
    }

    public static Color getMixedColor(int r, int g, int b, int r2, int g2, int b2, int index, int seconds) {
        return getMixedColor(index, seconds);
    }
}
