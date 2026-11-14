/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.command.CommandManager;
import net.ccbluex.liquidbounce.features.module.modules.misc.ComponentOnHover;
import net.ccbluex.liquidbounce.features.module.modules.render.HUD;
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration;
import net.ccbluex.liquidbounce.utils.render.shader.Background;
import net.ccbluex.liquidbounce.utils.render.DimOverlay;
import net.ccbluex.liquidbounce.utils.render.GuiAnimationState;
import net.ccbluex.liquidbounce.utils.render.ParticleUtils;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.BackgroundShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

import static net.minecraft.client.renderer.GlStateManager.disableFog;
import static net.minecraft.client.renderer.GlStateManager.disableLighting;

@Mixin(GuiScreen.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiScreen {

    @Unique
    private static final ResourceLocation AEROLITE_BACKGROUND = new ResourceLocation("aerolite/main/game.png");

    /**
     * The background overlay is drawn inside the container open animation's matrix, which made it
     * move together with the panel (slide) or shrink together with it (zoom). Every background
     * draw undoes that transform so the dimming stays pinned to the screen.
     *
     * <p>The animation post-multiplies {@code S(scale) * T(offset)}, so the inverse is
     * {@code T(-offset)} followed by {@code S(1 / scale)} — applied in that order, because OpenGL
     * post-multiplies every call onto the current matrix.</p>
     *
     * <p>The state itself lives in {@link GuiAnimationState} because Mixin forbids non-private
     * static fields on a mixin class.</p>
     */
    @Unique
    private static void liquidBounce$pushBackgroundOffset() {
        GL11.glPushMatrix();

        if (GuiAnimationState.isIdentity())
            return;

        GL11.glTranslated(-GuiAnimationState.backgroundOffsetX, -GuiAnimationState.backgroundOffsetY, 0.0);

        final float scale = GuiAnimationState.backgroundScale;
        if (scale != 1F && scale > 0F)
            GL11.glScaled(1.0 / scale, 1.0 / scale, 1.0);
    }

    @Unique
    private static void liquidBounce$popBackgroundOffset() {
        GL11.glPopMatrix();
    }

    @Shadow
    public Minecraft mc;

    @Shadow
    protected List<GuiButton> buttonList;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected FontRenderer fontRendererObj;

    @Shadow
    public void updateScreen() {
    }

    @Shadow
    public abstract void handleComponentHover(IChatComponent component, int x, int y);

    @Shadow
    protected abstract void drawHoveringText(List<String> textLines, int x, int y);

    @Inject(method = "drawWorldBackground", at = @At("HEAD"), cancellable = true)
    private void drawWorldBackground(final CallbackInfo callbackInfo) {
        liquidBounce$pushBackgroundOffset();

        final HUD hud = HUD.INSTANCE;

        if (hud.getInventoryParticle() && mc.thePlayer != null) {
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            final int width = scaledResolution.getScaledWidth();
            final int height = scaledResolution.getScaledHeight();
            ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
        }

        // Fade the dim overlay in together with the panel. The vanilla path is only taken when the
        // world exists, so cancelling here can never swallow the main-menu background image.
        final float alpha = GuiAnimationState.backgroundAlpha;
        if (mc.theWorld != null && alpha < 1F) {
            DimOverlay.draw(width, height, alpha);

            liquidBounce$popBackgroundOffset();
            callbackInfo.cancel();
        }
    }

    /**
     * @author CCBlueX
     */
    @Inject(method = "drawWorldBackground", at = @At("RETURN"))
    private void restoreBackgroundOffset(final CallbackInfo callbackInfo) {
        liquidBounce$popBackgroundOffset();
    }

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void drawClientBackground(final CallbackInfo callbackInfo) {
        liquidBounce$pushBackgroundOffset();

        disableLighting();
        disableFog();

        final Background background = ClientConfiguration.INSTANCE.getCustomBackground()
                ? LiquidBounce.INSTANCE.getBackground()
                : null;

        if (background != null) {
            // User supplied background image
            background.drawBackground(width, height);
        } else {
            // Aerolite look: the bundled artwork is the background of every screen.
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.enableTexture2D();
            GlStateManager.color(1F, 1F, 1F, 1F);
            mc.getTextureManager().bindTexture(AEROLITE_BACKGROUND);
            Gui.drawScaledCustomSizeModalRect(0, 0, 0.0F, 0.0F, width, height, width, height, width, height);
            GlStateManager.resetColor();
        }

        if (ClientConfiguration.INSTANCE.getParticles()) {
            ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
        }

        liquidBounce$popBackgroundOffset();
        callbackInfo.cancel();
    }

    @Inject(method = "drawBackground", at = @At("RETURN"))
    private void drawParticles(final CallbackInfo callbackInfo) {
        if (ClientConfiguration.INSTANCE.getParticles())
            ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
    }

    @Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void messageSend(String msg, boolean addToChat, final CallbackInfo callbackInfo) {
        if (msg.startsWith(String.valueOf(CommandManager.INSTANCE.getPrefix())) && addToChat) {
            mc.ingameGUI.getChatGUI().addToSentMessages(msg);

            CommandManager.INSTANCE.executeCommands(msg);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "handleComponentHover", at = @At("HEAD"))
    private void handleHoverOverComponent(IChatComponent component, int x, int y, final CallbackInfo callbackInfo) {
        if (component == null || component.getChatStyle().getChatClickEvent() == null || !ComponentOnHover.INSTANCE.handleEvents())
            return;

        final ChatStyle chatStyle = component.getChatStyle();

        final ClickEvent clickEvent = chatStyle.getChatClickEvent();
        final HoverEvent hoverEvent = chatStyle.getChatHoverEvent();

        drawHoveringText(Collections.singletonList("§c§l" + clickEvent.getAction().getCanonicalName().toUpperCase() + ": §a" + clickEvent.getValue()), x, y - (hoverEvent != null ? 17 : 0));
    }

    /**
     * @author CCBlueX (superblaubeere27)
     * @reason Making it possible for other mixins to receive actions
     */
    @Overwrite
    protected void actionPerformed(GuiButton button) {
        injectedActionPerformed(button);
    }

    protected void injectedActionPerformed(GuiButton button) {

    }
}