/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.features.module.modules.render.Animations;
import net.ccbluex.liquidbounce.injection.access.StaticStorage;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives the Aerolite player-list open/close animation (TabMode / TabTime / TabEase).
 *
 * The progress value is advanced here instead of in the update loop so the animation stays
 * frame-synchronised with the overlay itself.
 */
@Mixin(GuiPlayerTabOverlay.class)
@SideOnly(Side.CLIENT)
public class MixinGuiPlayerTabOverlay {

    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "renderPlayerlist", at = @At("HEAD"))
    public void renderPlayerListPre(int width, Scoreboard scoreboardIn, ScoreObjective objectiveIn, CallbackInfo ci) {
        final Animations animations = Animations.INSTANCE;

        // Follow the tab key, once per frame.
        float tabHope = mc.gameSettings.keyBindPlayerList.isKeyDown() ? 1F : 0F;
        if (animations.getTabHopePercent() != tabHope) {
            animations.setLastTabSync(System.currentTimeMillis());
            animations.setTabHopePercent(tabHope);
        }

        if (animations.getTabPercent() != animations.getTabHopePercent()) {
            int time = Math.max(animations.getTabTime(), 1);
            float change = (System.currentTimeMillis() - animations.getLastTabSync()) / (float) time;

            if (Math.abs(animations.getTabHopePercent() - animations.getTabPercent()) < change) {
                animations.setTabPercent(animations.getTabHopePercent());
            } else if (animations.getTabHopePercent() > animations.getTabPercent()) {
                animations.setTabPercent(animations.getTabPercent() + change);
            } else {
                animations.setTabPercent(animations.getTabPercent() - change);
            }
        }

        animations.setFlagRenderTabOverlay(true);

        GL11.glPushMatrix();

        final float pct = (float) EaseUtils.INSTANCE.apply(
                EaseUtils.EnumEasingType.valueOf(animations.getTabEaseMode()),
                EaseUtils.EnumEasingOrder.valueOf(animations.getTabEaseOrder()),
                animations.getTabPercent()
        );

        final int scaledHeight = StaticStorage.scaledResolution.getScaledHeight();

        switch (animations.getTabMode().toLowerCase()) {
            case "upslide":
                GL11.glTranslatef(0F, scaledHeight * (pct - 1F), 0F);
                break;
            case "downslide":
                GL11.glTranslatef(0F, scaledHeight * (1F - pct), 0F);
                break;
            case "zoom":
                GL11.glScalef(1F, pct, 1F);
                break;
            default:
                break;
        }
    }

    @Inject(method = "renderPlayerlist", at = @At("RETURN"))
    public void renderPlayerListPost(int width, Scoreboard scoreboardIn, ScoreObjective objectiveIn, CallbackInfo ci) {
        Animations.INSTANCE.setFlagRenderTabOverlay(false);
        GL11.glPopMatrix();
    }
}
