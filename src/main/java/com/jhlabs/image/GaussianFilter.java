/*
 * Aerolite — local stand-in for com.jhlabs.image.GaussianFilter.
 *
 * The original FDPClient code depends on Jerry Huxtable's "Java Image Filters" library
 * (com.jhlabs.image.GaussianFilter), which is not bundled with this build. To keep the ported
 * RenderEffects shader pipeline compiling and functional without pulling in the external jar, this
 * class re-implements the same public surface (constructor with a float radius + a `filter` method
 * returning a blurred BufferedImage) using a separable box-blur approximation of a Gaussian kernel.
 */
package com.jhlabs.image;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class GaussianFilter {

    private final float radius;

    public GaussianFilter(float radius) {
        this.radius = radius;
    }

    public GaussianFilter() {
        this(1.0f);
    }

    public float getRadius() {
        return radius;
    }

    /**
     * Blurs [src]. When [dest] is null a new image of the same size/type is allocated.
     * A radius of 0 or less returns the source unchanged.
     */
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        if (radius <= 0.0f) {
            if (dest != null) {
                dest.getGraphics().drawImage(src, 0, 0, null);
                return dest;
            }
            return src;
        }

        int size = Math.max(1, Math.round(radius));
        float[] matrix = new float[size * 2 + 1];
        float sigma = size / 2.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sum = 0.0f;
        for (int i = -size; i <= size; i++) {
            float value = (float) Math.exp(-(i * i) / twoSigmaSquare);
            matrix[i + size] = value;
            sum += value;
        }
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] /= sum;
        }

        Kernel kernel = new Kernel(matrix.length, 1, matrix);
        ConvolveOp horizontal = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage temp = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        horizontal.filter(src, temp);

        Kernel vKernel = new Kernel(1, matrix.length, matrix);
        ConvolveOp vertical = new ConvolveOp(vKernel, ConvolveOp.EDGE_NO_OP, null);

        if (dest == null) {
            dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        }
        vertical.filter(temp, dest);
        return dest;
    }
}
