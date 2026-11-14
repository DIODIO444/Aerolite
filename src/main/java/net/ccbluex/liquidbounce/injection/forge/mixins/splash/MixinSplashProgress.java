package net.ccbluex.liquidbounce.injection.forge.mixins.splash;

import net.minecraftforge.fml.client.SplashProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SplashProgress.class, remap = false)
public abstract class MixinSplashProgress {

    @Shadow(aliases = "SplashProgress", remap = false)
    private static boolean enabled;

    @Inject(method = "start", at = @At(value = "FIELD", target = "Lnet/minecraftforge/fml/client/SplashProgress;enabled:Z", opcode = 178, remap = false, ordinal = 0), remap = false, require = 1, allow = 1)
    private static void start(CallbackInfo callbackInfo) {
        enabled = true;
    }

    // The custom splash runnable never creates Forge's fontTexture/logoTexture/forgeTexture, so
    // SplashProgress.finish() would NPE while deleting them and disable the splash screen. done is
    // already set to true before this point (Forge 1.8.9, SplashProgress#finish line 572) and the
    // render thread has been joined, so cancelling after makeCurrent() is safe.
    @Inject(method = "finish", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Drawable;makeCurrent()V", shift = At.Shift.AFTER, remap = false, ordinal = 0), remap = false, cancellable = true, require = 1, allow = 1)
    private static void finish(CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }
}
