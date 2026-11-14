package net.ccbluex.liquidbounce.utils.render;

/**
 * Shared state between the GuiScreen and GuiContainer mixins.
 *
 * <p>Mixin forbids non-private static fields inside a mixin class, so the inventory open
 * animation state lives here instead of on {@code MixinGuiScreen}.</p>
 *
 * <p>The container animation applies {@code C0 * S(scale) * T(offset)} to the model-view matrix
 * ({@code slide} only translates, {@code zoom} scales around the container centre). Background
 * overlays are drawn inside that matrix, so they used to move with the panel. The screen mixin
 * undoes the transform while drawing a background and restores it afterwards.</p>
 */
public final class GuiAnimationState {

    /** Horizontal translation of the animation, in GUI pixels (before scaling). */
    public static float backgroundOffsetX = 0F;

    /** Vertical translation of the animation, in GUI pixels (before scaling). */
    public static float backgroundOffsetY = 0F;

    /** Uniform scale of the animation; 1 when the animation only translates. */
    public static float backgroundScale = 1F;

    /**
     * Opacity of the dim overlay, 0 (fully transparent) to 1 (vanilla look).
     * Driven by the same progress as the panel animation so the dimming fades in with it.
     */
    public static float backgroundAlpha = 1F;

    /** Opacity the dim overlay had on the last completed frame — the starting point of a fade-out. */
    public static float lastDrawnAlpha = 1F;

    /** {@link System#currentTimeMillis()} when the close fade-out started, 0 when inactive. */
    public static long closeFadeStart = 0L;

    /** Length of the close fade-out in milliseconds. */
    public static int closeFadeDuration = 0;

    /** Starts fading the dim overlay out (used when a container is closed). */
    public static void startCloseFade(int durationMs) {
        closeFadeStart = System.currentTimeMillis();
        closeFadeDuration = Math.max(1, durationMs);
    }

    /** Aborts a running fade-out, e.g. when another screen opens right after closing one. */
    public static void cancelCloseFade() {
        closeFadeStart = 0L;
    }

    /** Current opacity of the fade-out overlay; 0 when no fade is running. */
    public static float closeFadeAlpha() {
        if (closeFadeStart <= 0L)
            return 0F;

        final long elapsed = System.currentTimeMillis() - closeFadeStart;
        if (elapsed >= closeFadeDuration) {
            closeFadeStart = 0L;
            return 0F;
        }

        return lastDrawnAlpha * (1F - (float) elapsed / closeFadeDuration);
    }

    /** Restores the identity state — must be called on every entry / exit of {@code drawScreen}. */
    public static void reset() {
        backgroundOffsetX = 0F;
        backgroundOffsetY = 0F;
        backgroundScale = 1F;
        backgroundAlpha = 1F;
    }

    /** True when the container animation is not transforming anything this frame. */
    public static boolean isIdentity() {
        return backgroundOffsetX == 0F && backgroundOffsetY == 0F && backgroundScale == 1F;
    }

    private GuiAnimationState() {
    }
}
