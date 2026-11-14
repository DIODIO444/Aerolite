/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.*;

import java.awt.Color;

import static net.minecraft.client.renderer.GlStateManager.resetColor;

/**
 * Aerolite button skin: a translucent dark plate with a cyan accent line right underneath it.
 */
@Mixin(GuiButton.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiButton extends Gui {

    @Shadow
    public boolean visible;

    @Shadow
    public int xPosition;

    @Shadow
    public int yPosition;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected boolean hovered;

    @Shadow
    public boolean enabled;

    @Shadow
    protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

    @Shadow
    public String displayString;

    @Shadow
    @Final
    protected static ResourceLocation buttonTextures;

    @Shadow
    public int id;

    @Unique
    private static final int AEROLITE_BACKGROUND = new Color(31, 31, 31, 150).getRGB();

    @Unique
    private static final int AEROLITE_BACKGROUND_HOVER = new Color(60, 60, 60, 150).getRGB();

    @Unique
    private static final int AEROLITE_PROGRESS = new Color(0, 255, 255, 45).getRGB();

    @Unique
    private static final int AEROLITE_ACCENT = new Color(0, 255, 255).getRGB();

    /**
     * @author CCBlueX
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }

        hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

        float sliderProgress = -1F;

        if ((Object) this instanceof GuiOptionSlider) {
            sliderProgress = ((GuiOptionSlider) (Object) this).sliderValue;
        } else if ((Object) this instanceof GuiScreenOptionsSounds.Button) {
            sliderProgress = ((GuiScreenOptionsSounds.Button) (Object) this).field_146156_o;
        }

        RenderUtils.drawRect(xPosition, yPosition, xPosition + width, yPosition + height,
                hovered ? AEROLITE_BACKGROUND_HOVER : AEROLITE_BACKGROUND);

        if (sliderProgress >= 0F) {
            RenderUtils.drawRect(xPosition, yPosition, xPosition + width * sliderProgress, yPosition + height,
                    AEROLITE_PROGRESS);
        }

        // Accent line underneath the plate
        RenderUtils.drawRect(xPosition, yPosition + height, xPosition + width, yPosition + height + 2,
                AEROLITE_ACCENT);

        mc.getTextureManager().bindTexture(buttonTextures);
        mouseDragged(mc, mouseX, mouseY);

        final FontRenderer fontRenderer = Fonts.fontRegular35;
        fontRenderer.drawStringWithShadow(displayString,
                xPosition + width / 2F - fontRenderer.getStringWidth(displayString) / 2F,
                yPosition + (height - 8) / 2F,
                enabled ? 0xFFFFFF : 0xA0A0A0);

        resetColor();
    }
}
