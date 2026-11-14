package net.ccbluex.liquidbounce.features.module.modules.render;

import java.awt.Color;
import java.util.ArrayList;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.features.module.modules.render.HUD;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.VisualBase;
import net.ccbluex.liquidbounce.config.FloatValue;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.ScaledResolution;


public class Keybinds extends Module {
    public Keybinds() {
        super("Keybinds", Category.RENDER, org.lwjgl.input.Keyboard.KEY_NONE, true);
    }

    public final FloatValue IndicatorX = new FloatValue("PositionX", 360, 0, 500);
    public final FloatValue IndicatorY = new FloatValue("PositionY", 150, 0, 170);

    @EventTarget
    public void biba(Render2DEvent event) {

        ScaledResolution sr = new ScaledResolution(mc);
        final float scaledWidth = sr.getScaledWidth();
        final float scaledHeight = sr.getScaledHeight();

        float x = IndicatorX.get();
        float y = IndicatorY.get();

        VisualBase.drawGradientRect(x, y, x + 95, y + 10, new Color(11, 11, 11, 255).getRGB(),
                new Color(11, 11, 11, 255).getRGB());
        VisualBase.drawGradientSideways(x, y, x + 95, y + 1, ColorUtils.INSTANCE.rainbow().getRGB(),
                new Color(255, 255, 255, 220).getRGB());
        Fonts.INSTANCE.getFont35().drawString("binds", x + 40, (float) (y + 4), -1, true);
        double offsetY = y + 3;

        for (final Module f : LiquidBounce.moduleManager.getModules()) {
            if (f.getState() && f.getKeyBind() != 0) {
                Fonts.INSTANCE.getFont35().drawString(f.getName().toLowerCase(), x + 2, (float) (offsetY + 10), -1, true);
                Fonts.INSTANCE.getFont35().drawString("toggled",
                        x + 92 - Fonts.INSTANCE.getFont35().getStringWidth("toggled"), (float) (offsetY + 10), -1, true);
                offsetY += 8;
            }
        }

    }
}

