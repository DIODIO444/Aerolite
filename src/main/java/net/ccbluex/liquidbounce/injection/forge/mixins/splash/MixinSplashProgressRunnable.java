package net.ccbluex.liquidbounce.injection.forge.mixins.splash;

import net.ccbluex.liquidbounce.utils.render.AnimatedValue;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.ProgressManager;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

@Mixin(targets = "net.minecraftforge.fml.client.SplashProgress$3", remap = false)
public abstract class MixinSplashProgressRunnable {

    @Shadow(remap = false)
    protected abstract void setGL();

    @Shadow(remap = false)
    protected abstract void clearGL();

    // SplashProgress.done / mutex / pause are private in Forge 1.8.9, so they are read reflectively.
    private static final Field DONE = field("done");
    private static final Field MUTEX = field("mutex");
    private static final Field PAUSE = field("pause");

    private static Field field(String name) {
        try {
            Field field = SplashProgress.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Throwable throwable) {
            return null;
        }
    }

    private static boolean isDone() {
        try {
            return DONE == null || DONE.getBoolean(null);
        } catch (Throwable throwable) {
            return true;
        }
    }

    private static boolean isPaused() {
        try {
            return PAUSE != null && PAUSE.getBoolean(null);
        } catch (Throwable throwable) {
            return false;
        }
    }

    private static Semaphore mutex() {
        try {
            return MUTEX == null ? null : (Semaphore) MUTEX.get(null);
        } catch (Throwable throwable) {
            return null;
        }
    }

    @Inject(method = "run()V", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void run(CallbackInfo callbackInfo) {
        callbackInfo.cancel();

        this.setGL();
        GL11.glClearColor(1f, 1f, 1f, 1f);

        System.out.println("[Splash] Loading Texture...");
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        int tex;
        try {
            tex = RenderUtils.loadGlTexture(ImageIO.read(this.getClass().getResourceAsStream("/assets/minecraft/aerolite/misc/splash.png")));
        } catch (IOException e) {
            tex = 0;
        }
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        AnimatedValue animatedValue = new AnimatedValue();
        animatedValue.setType(EaseUtils.EnumEasingType.CIRC);
        animatedValue.setDuration(600L);

        System.out.println("[Splash] Starting Render Thread...");
        while (!isDone()) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            int width = Display.getWidth();
            int height = Display.getHeight();
            GL11.glViewport(0, 0, width, height);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0, width, height, 0.0, -1.0, 1.0);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            GL11.glColor4f(1f, 1f, 1f, 1f);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0.0f, 0.0f);
            GL11.glVertex2f(0.0f, 0.0f);
            GL11.glTexCoord2f(1.0f, 0.0f);
            GL11.glVertex2f(width, 0.0f);
            GL11.glTexCoord2f(1.0f, 1.0f);
            GL11.glVertex2f(width, height);
            GL11.glTexCoord2f(0.0f, 1.0f);
            GL11.glVertex2f(0.0f, height);
            GL11.glEnd();
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            float rectX = width * 0.2f;
            float rectX2 = width * 0.8f;
            float rectY = height * 0.75f;
            float rectY2 = height * 0.755f;
            float rectRadius = 0;
            float progress = (float) animatedValue.sync(getProgress());

            if (progress != 1f) {
                GL11.glColor4f(0f, 0f, 0f, 0.3f);
                RenderUtils.drawRoundedCornerRect(rectX, rectY, rectX2, rectY2, rectRadius);
            }

            if (progress != 0f) {
                GL11.glColor4f(1f, 1f, 1f, 1f);
                RenderUtils.drawRoundedCornerRect(rectX, rectY, rectX + (width * 0.6f * progress), rectY2, rectRadius);
            }

            final Semaphore mutex = mutex();

            if (mutex != null) {
                mutex.acquireUninterruptibly();
            }
            Display.update();
            if (mutex != null) {
                mutex.release();
            }
            if (isPaused()) {
                this.clearGL();
                this.setGL();
            }
            Display.sync(60);
        }

        GL11.glDeleteTextures(tex);
        this.clearGL();
    }

    private static float getProgress() {
        float progress = 0;
        Iterator<ProgressManager.ProgressBar> it = ProgressManager.barIterator();
        if (it.hasNext()) {
            ProgressManager.ProgressBar bar = it.next();
            progress = bar.getStep() / (float) bar.getSteps();
        }

        return progress;
    }
}
