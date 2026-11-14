package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.render;

import net.minecraft.client.renderer.GlStateManager;

import static org.lwjgl.opengl.GL11.*;

public class GLUtil {


    public static void render(int mode, Runnable render){
        glBegin(mode);
        render.run();
        glEnd();
    }

    /**
     * Draws 2D primitives with texturing turned off.
     *
     * <p>This used to call raw {@code glDisable/glEnable(GL_TEXTURE_2D)} while the rest of the
     * client (fonts, rects, gradients) goes through {@link GlStateManager}. Mixing the two leaves
     * GlStateManager's cache out of sync with the real GL state: the raw {@code glEnable} at the
     * end makes GL believe texturing is on while the cache says it is off, so the next
     * {@code GlStateManager.disableTexture2D()} becomes a no-op and every following quad is
     * textured with whatever atlas happened to be bound — which produced the magenta "missing
     * texture" blocks and the purple panels.
     */
    public static void setup2DRendering(Runnable f) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        f.run();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void rotate(float x, float y, float rotate, Runnable f) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.rotate(rotate, 0, 0, -1);
        GlStateManager.translate(-x, -y, 0);
        f.run();
        GlStateManager.popMatrix();
    }


}
