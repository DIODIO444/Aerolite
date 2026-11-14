package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;

/**
 * Thin adapter so the ported Tenacity (newdropdown) UI can keep its
 * `Fonts.SF.SF_18.SF_18.drawString(...)` / `stringWidth(...)` / `getMiddleOfBox(...)` call style
 * while drawing through the client's {@link GameFontRenderer}.
 */
public final class NewFonts {

    public static final Face SF14 = new Face(Fonts.INSTANCE.getSF14());
    public static final Face SF16 = new Face(Fonts.INSTANCE.getSF16());
    public static final Face SF18 = new Face(Fonts.INSTANCE.getSF18());
    public static final Face SF20 = new Face(Fonts.INSTANCE.getSF20());
    public static final Face SFBOLD18 = new Face(Fonts.INSTANCE.getSFBOLD18());
    public static final Face SFBOLD26 = new Face(Fonts.INSTANCE.getSFBOLD26());
    public static final Face CheckFont20 = new Face(Fonts.INSTANCE.getCheckFont20());
    public static final Face ICONFONT20 = new Face(Fonts.INSTANCE.getICONFONT20());

    private NewFonts() {
    }

    public static final class Face {
        public final GameFontRenderer font;

        public Face(GameFontRenderer font) {
            this.font = font;
        }

        public void drawString(String text, float x, float y, int color) {
            font.drawString(text, x, y, color);
        }

        public void drawCenteredString(String text, float x, float y, int color) {
            font.drawCenteredString(text, x, y, color, false);
        }

        public int stringWidth(String text) {
            return font.getStringWidth(text);
        }

        public int getHeight() {
            return font.getFontHeight();
        }

        /** Vertical offset that centres a line of text inside a box of the given height. */
        public float getMiddleOfBox(float boxHeight) {
            return boxHeight / 2f - font.getFontHeight() / 2f;
        }
    }
}
