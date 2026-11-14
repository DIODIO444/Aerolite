/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.modules.render.Animations;
import net.ccbluex.liquidbounce.features.module.modules.render.AntiBlind;
import net.ccbluex.liquidbounce.features.module.modules.render.HUD;
import net.ccbluex.liquidbounce.features.module.modules.render.SilentHotbarModule;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.client.ClassUtils;
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar;
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils;
import net.ccbluex.liquidbounce.utils.render.ColorSettingsKt;
import net.ccbluex.liquidbounce.utils.render.BlurUtils;
import net.ccbluex.liquidbounce.utils.render.DimOverlay;
import net.ccbluex.liquidbounce.utils.render.GuiAnimationState;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GradientShader;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.List;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.*;

@Mixin(GuiIngame.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiInGame extends Gui {

    @Shadow
    protected abstract void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer player);

    @Shadow
    @Final
    protected Minecraft mc;

    @Shadow
    protected GuiPlayerTabOverlay overlayPlayerList;

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void renderScoreboard(CallbackInfo callbackInfo) {
        if (HUD.INSTANCE.handleEvents())
            callbackInfo.cancel();
    }

    /**
     * Fades the dim overlay out after a container was closed.
     *
     * <p>Drawn at the very end of the overlay pass so it sits above the HUD, exactly like the
     * vanilla dim layer does while the screen is still open.</p>
     */
    @Inject(method = "renderGameOverlay", at = @At("RETURN"))
    private void liquidBounce$drawCloseFade(float partialTicks, CallbackInfo callbackInfo) {
        final float alpha = GuiAnimationState.closeFadeAlpha();
        if (alpha <= 0F)
            return;

        final ScaledResolution resolution = new ScaledResolution(mc);
        DimOverlay.draw(resolution.getScaledWidth(), resolution.getScaledHeight(), alpha);
    }

    @Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
    private ItemStack hookSilentHotbarHighlightedName(InventoryPlayer instance) {
        SilentHotbarModule module = SilentHotbarModule.INSTANCE;

        int slot = SilentHotbar.INSTANCE.renderSlot(module.handleEvents() && module.getKeepHighlightedName());

        return instance.getStackInSlot(slot);
    }

    /**
     * Keeps rendering the player list while it animates out after the tab key was released.
     */
    @Inject(method = "renderTooltip", at = @At("HEAD"))
    private void renderTabOverlayClosing(ScaledResolution resolution, float delta, CallbackInfo ci) {
        final Animations animations = Animations.INSTANCE;

        if (animations.getTabPercent() <= 0F || mc.gameSettings.keyBindPlayerList.isKeyDown()) {
            return;
        }

        if (mc.theWorld == null || mc.theWorld.getScoreboard() == null) {
            return;
        }

        overlayPlayerList.renderPlayerlist(
                resolution.getScaledWidth(),
                mc.theWorld.getScoreboard(),
                mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(0)
        );
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void injectCustomHotbar(ScaledResolution resolution, float delta, CallbackInfo ci) {
        final HUD hud = HUD.INSTANCE;
        final RenderUtils render = RenderUtils.INSTANCE;

        if (mc.getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) mc.getRenderViewEntity();
            float slot = entityPlayer.inventory.currentItem;

            if (hud.handleEvents() && hud.getCustomHotbar()) {
                if (hud.getSmoothHotbarSlot()) {
                    slot = InventoryUtils.INSTANCE.getLerpedSlot();
                }

                // Aerolite's BetterHotbar (Rect / RoundRect / Rect2) — see HUD.hotbarStyle.
                //
                // liquidBounce$injectRender2DEvent() is invoked at the TAIL of this method, so this
                // early return has to fire it by hand. Returning without it skips Render2DEvent
                // entirely and the whole HUD (watermark / arraylist / scoreboard ...) disappears.
                if ("Aerolite".equals(hud.getHotbarStyle())) {
                    try {
                        drawAeroliteHotbar(resolution, delta, entityPlayer, slot);
                    } catch (Throwable throwable) {
                        ClientUtils.INSTANCE.logError("Unable to render the Aerolite hotbar.", throwable);
                    }

                    liquidBounce$injectRender2DEvent(delta);
                    ci.cancel();
                    return;
                }

                int middleScreen = resolution.getScaledWidth() / 2;
                int height = resolution.getScaledHeight() - 1;

                float gradientOffset = (System.currentTimeMillis() % 10000) / 10000f;

                float gradientX = (hud.getGradientX() == 0f) ? 0f : 1f / hud.getGradientX();
                float gradientY = (hud.getGradientY() == 0f) ? 0f : 1f / hud.getGradientY();

                float rainbowOffset = (System.currentTimeMillis() % 10000) / 10000f;
                float rainbowX = (hud.getRainbowX() == 0f) ? 0f : 1f / hud.getRainbowX();
                float rainbowY = (hud.getRainbowY() == 0f) ? 0f : 1f / hud.getRainbowY();

                List<float[]> gradientColors = ColorSettingsKt.toColorArray(hud.getBgGradColors(), hud.getMaxHotbarGradientColors());

                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_DEPTH_TEST);

                boolean isGradient = hud.getHotbarMode().equals("Gradient");
                boolean isRainbow = hud.getHotbarMode().equals("Rainbow");

                AWTFontRenderer.Companion.setAssumeNonVolatile(true);

                if (isGradient) {
                    GradientShader.begin(
                            true,
                            gradientX,
                            gradientY,
                            gradientColors,
                            hud.getGradientHotbarSpeed(),
                            gradientOffset
                    );
                }

                if (isRainbow) {
                    RainbowShader.begin(true, rainbowX, rainbowY, rainbowOffset);
                }

                // Inner - Background
                render.drawRoundedRectInt(
                        middleScreen - 91, height - 22,
                        middleScreen + 91, height,
                        hud.getHbBackgroundColors().color().getRGB(),
                        hud.getRoundedHotbarRadius(),
                        RenderUtils.RoundedCorners.ALL
                );

                if (isRainbow) {
                    RainbowShader.INSTANCE.stopShader();
                }
                if (isGradient) {
                    GradientShader.INSTANCE.stopShader();
                }

                // Inner - Highlight
                render.drawRoundedRect(
                        middleScreen - 91 - 1 + slot * 20 + 1, height - 22,
                        middleScreen - 91 - 1 + slot * 20 + 23, height - 23 - 1 + 24,
                        hud.getHbHighlightColors().color().getRGB(),
                        hud.getRoundedHotbarRadius(),
                        RenderUtils.RoundedCorners.ALL
                );

                // Border - Background
                render.drawRoundedBorder(
                        middleScreen - 91, height - 21.55F,
                        middleScreen + 91 + 0.1F, height - 0.5F,
                        hud.getHbBackgroundBorder(),
                        hud.getHbBackgroundBorderColors().color().getRGB(),
                        hud.getRoundedHotbarRadius()
                );

                // Border - Highlight
                render.drawRoundedBorder(
                        middleScreen - 91 - 1 + slot * 20 + 1, height - 21.5F,
                        middleScreen - 91 - 1 + slot * 20 + 23.15F, height - 23 - 1 + 23.5F,
                        hud.getHbHighlightBorder(),
                        hud.getHbHighlightBorderColors().color().getRGB(),
                        hud.getRoundedHotbarRadius()
                );

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glPopMatrix();

                enableRescaleNormal();
                glEnable(GL_BLEND);
                tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();

                for (int j = 0; j < 9; ++j) {
                    int l = height - 16 - 3;
                    int k = middleScreen - 90 + j * 20 + 2;
                    renderHotbarItem(j, k, l, delta, entityPlayer);
                }

                RenderHelper.disableStandardItemLighting();
                disableRescaleNormal();
                disableBlend();

                AWTFontRenderer.Companion.setAssumeNonVolatile(false);

                ci.cancel();
            }
        }

        liquidBounce$injectRender2DEvent(delta);
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    private void renderPumpkinOverlay(final CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = AntiBlind.INSTANCE;

        if (antiBlind.handleEvents() && antiBlind.getPumpkinEffect())
            callbackInfo.cancel();
    }

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void renderBossHealth(CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = AntiBlind.INSTANCE;

        if (antiBlind.handleEvents() && antiBlind.getBossHealth())
            callbackInfo.cancel();
    }

    @Unique
    private void liquidBounce$injectRender2DEvent(float delta) {
        if (!ClassUtils.INSTANCE.hasClass("net.labymod.api.LabyModAPI")) {
            EventManager.INSTANCE.call(new Render2DEvent(delta));
        }
    }

    /**
     * Faithful port of Aerolite's `BetterHotbar` drawing (MixinGuiInGame#renderTooltip).
     */
    private void drawAeroliteHotbar(ScaledResolution sr, float delta, EntityPlayer player, float slot) {
        final HUD hud = HUD.INSTANCE;
        final RenderUtils render = RenderUtils.INSTANCE;

        int middle = sr.getScaledWidth() / 2;
        int bottom = sr.getScaledHeight();
        int itemX = (int) (middle - 91 + slot * 20);

        color(1F, 1F, 1F, 1F);
        enableRescaleNormal();
        enableBlend();
        tryBlendFuncSeparate(770, 771, 1, 0);

        if (hud.isHotbarBlur()) {
            // BlurUtils.draw() only runs the shader passes into its own "result" framebuffer and
            // never composites them back, so nothing was ever visible. blurArea() is the complete
            // implementation (stencil mask + composite) that the scoreboard and TabGUI already use.
            BlurUtils.INSTANCE.blurArea(middle - 91F, bottom - 22F, middle + 91F, (float) bottom, 8F);

            // Stencil.dispose() turns blending off again; restore what the hotbar expects.
            color(1F, 1F, 1F, 1F);
            enableBlend();
            tryBlendFuncSeparate(770, 771, 1, 0);
        }

        switch (hud.getBetterHotbarMode()) {
            case "RoundRect":
                render.drawRoundedCornerRect(middle - 91F, bottom - 22F, middle + 91F, bottom, 1F,
                        new Color(20, 20, 20, 100).getRGB());
                render.drawRoundedCornerRect(itemX, bottom - 22F, itemX + 22F, bottom, 3F,
                        new Color(150, 150, 150, 155).getRGB());
                break;

            case "Rect2":
                render.drawBorderedRect(middle - 91F, bottom - 22F, middle + 91F, (float) bottom, 0.2F,
                        new Color(100, 100, 100, 100).getRGB(), new Color(50, 50, 50, 200).getRGB());
                render.drawBorderedRect((float) itemX, bottom - 22F, itemX + 22F, (float) bottom, 0.4F,
                        new Color(150, 150, 150, 155).getRGB(), new Color(0, 200, 255, 220).getRGB());
                break;

            case "Rect":
            default:
                render.drawRect(middle - 91, bottom - 22, middle + 91, bottom,
                        new Color(20, 20, 20, 100).getRGB());
                render.drawRect(itemX, bottom - 22, itemX + 22, bottom,
                        new Color(150, 150, 150, 155).getRGB());
                break;
        }

        if (hud.getBetterHotbarShadow()) {
            // drawShadow takes (x, y, width, height) — the old call passed the screen centre and
            // the screen bottom as width/height, which painted a huge panel across the screen.
            RenderUtils.drawShadow(middle - 91F, bottom - 22F, 182F, 22F);
        }

        RenderHelper.enableGUIStandardItemLighting();

        for (int j = 0; j < 9; ++j) {
            int k = middle - 90 + j * 20 + 2;
            int l = bottom - 16 - 3;
            renderHotbarItem(j, k, l, delta, player);
        }

        RenderHelper.disableStandardItemLighting();
        disableRescaleNormal();
        disableBlend();
    }

}
