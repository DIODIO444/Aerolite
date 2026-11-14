/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */

package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.awt.Color;

import static net.minecraft.client.renderer.GlStateManager.resetColor;

/**
 * Same Aerolite skin as {@link MixinGuiButton}, for Forge's config-UI buttons and sliders.
 */
@Mixin(GuiButtonExt.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiButtonExt extends GuiButton {

    @Unique
    private static final int AEROLITE_BACKGROUND = new Color(31, 31, 31, 150).getRGB();

    @Unique
    private static final int AEROLITE_BACKGROUND_HOVER = new Color(60, 60, 60, 150).getRGB();

    @Unique
    private static final int AEROLITE_PROGRESS = new Color(0, 255, 255, 45).getRGB();

    @Unique
    private static final int AEROLITE_ACCENT = new Color(0, 255, 255).getRGB();

    public MixinGuiButtonExt(int p_i1020_1_, int p_i1020_2_, int p_i1020_3_, String p_i1020_4_) {
        super(p_i1020_1_, p_i1020_2_, p_i1020_3_, p_i1020_4_);
    }

    public MixinGuiButtonExt(int p_i46323_1_, int p_i46323_2_, int p_i46323_3_, int p_i46323_4_, int p_i46323_5_, String p_i46323_6_) {
        super(p_i46323_1_, p_i46323_2_, p_i46323_3_, p_i46323_4_, p_i46323_5_, p_i46323_6_);
    }

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

        if ((Object) this instanceof GuiSlider) {
            sliderProgress = (float) ((GuiSlider) (Object) this).sliderValue;
        }

        RenderUtils.drawRect(xPosition, yPosition, xPosition + width, yPosition + height,
                hovered ? AEROLITE_BACKGROUND_HOVER : AEROLITE_BACKGROUND);

        if (sliderProgress >= 0F) {
            RenderUtils.drawRect(xPosition, yPosition, xPosition + width * sliderProgress, yPosition + height,
                    AEROLITE_PROGRESS);
        }

        RenderUtils.drawRect(xPosition, yPosition + height, xPosition + width, yPosition + height + 2,
                AEROLITE_ACCENT);

        mc.getTextureManager().bindTexture(buttonTextures);
        mouseDragged(mc, mouseX, mouseY);

        final FontRenderer fontRenderer = mc.getLanguageManager().isCurrentLocaleUnicode() ? mc.fontRendererObj : Fonts.fontRegular35;
        fontRenderer.drawStringWithShadow(displayString,
                xPosition + width / 2F - fontRenderer.getStringWidth(displayString) / 2F,
                yPosition + (height - 8) / 2F,
                enabled ? 0xFFFFFF : 0xA0A0A0);

        resetColor();
    }
}
