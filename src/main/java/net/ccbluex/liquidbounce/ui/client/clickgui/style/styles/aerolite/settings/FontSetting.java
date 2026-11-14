//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.aerolite.settings;

import net.ccbluex.liquidbounce.config.FontValue;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.aerolite.AeroGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.aerolite.Downward;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.aerolite.ModuleRender;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.aerolite.RenderUtil;
import net.ccbluex.liquidbounce.ui.font.Fonts;

import java.awt.*;

/**
 * Font picker for the Aerolite / NeverLose style.
 *
 * <p>This style never had a {@code FontValue} branch, so a module's "Font" setting was invisible.
 * The panel is only 160px wide, so the value gets its own line and the row is 30px tall.</p>
 */
public class FontSetting extends Downward<FontValue> {
    public int mainx;
    public int mainy;
    public int y;

    public FontSetting(FontValue s, float x, float y, int width, int height, ModuleRender moduleRender) {
        super(s, x, y, width, height, moduleRender);
    }

    public void draw(int mouseX, int mouseY) {
        this.mainx = AeroGui.mainx;
        this.mainy = AeroGui.mainy;
        this.y = (int) (this.pos.y + (float) this.getScrollY());

        Fonts.INSTANCE.getNL18().drawString(this.setting.getName(),
                (float) (this.mainx + 17) + this.pos.x, (float) (this.mainy + 38 + this.y),
                new Color(130, 140, 150).getRGB());

        String label = Fonts.INSTANCE.labelOf(this.setting.get());
        Fonts.INSTANCE.getNL18().drawString(label,
                (float) (this.mainx + 17) + this.pos.x, (float) (this.mainy + 49 + this.y),
                new Color(90, 200, 255).getRGB());
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0 && mouseButton != 1)
            return;

        if (!RenderUtil.isHovering((float) (this.mainx + 13) + this.pos.x, (float) (this.mainy + 34 + this.y),
                154.0F, 26.0F, mouseX, mouseY))
            return;

        FontValue fontValue = this.setting;
        fontValue.set(mouseButton == 0
                ? Fonts.INSTANCE.nextFont(fontValue.get())
                : Fonts.INSTANCE.previousFont(fontValue.get()));
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
    }
}
