package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.novoline;

import com.google.common.collect.Lists;
import net.ccbluex.liquidbounce.config.Value;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleManager;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.Translate;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;

public class Window {
    public final Category category;
    public final ArrayList<Button> buttons = Lists.newArrayList();
    public boolean drag;
    public boolean extended;
    public int x;
    public int y;
    public float expand;
    public int dragX;
    public int dragY;
    public final int max;
    public int scroll;
    public int scrollTo;
    int allX;
    final Translate translate = new Translate(0F, 0F);

    public Window(Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
        max = 120;
        int y2 = y + 22;
        for (Module c : ModuleManager.INSTANCE.getModules()) {
            if (c.getCategory() != category)
                continue;
            buttons.add(new Button(c, x + 5, y2));
            y2 += 15;
        }
        for (Button b2 : buttons) {
            b2.setParent(this);
        }
    }

    int wheely;
    public float totalY;
    boolean buttonanim;

    private String label() {
        switch (category) {
            case RENDER:
                return "Visuals";
            default:
                return category.getDisplayName();
        }
    }

    public void render(int mouseX, int mouseY) {
        boolean isOnPanel;
        int current = 0;
        int iY = y + 22;
        totalY = 17;
        for (Button b3 : buttons) {
            b3.y = (int) (iY - translate.getY());
            iY += 15;
            totalY += 15;
            if (b3.expand) {
                for (ValueButton ignored : b3.buttons) {
                    current += 15;
                    totalY += 15;
                }
            }
            current += 15;
        }

        allX = 12;
        int height = 15 + current;
        switch (category) {
            case MISC:
                if (height > 250 - 143) height = 250 - 143;
                break;
            case PLAYER:
                if (height > 230 - 93) height = 230 - 93;
                break;
            case MOVEMENT:
                if (height > 316 - 44) height = 316 - 44;
                break;
            case COMBAT:
                if (height > 270 - 58) height = 270 - 58;
                break;
            default:
                if (height > 316) height = 316;
                break;
        }
        isOnPanel = mouseX > x - 2 && mouseX < x + 92 && mouseY > y - 2 && mouseY < y + expand;
        translate.interpolate(0, wheely, 0.15F);
        if (isOnPanel) {
            runWheel(height);
        }
        if (extended) {
            if (buttonanim) {
                expand = AnimationUtil.moveUD(expand, height, 0.2f, 0.15f);
            } else {
                if (category != Category.WORLD) {
                    expand = AnimationUtil.moveUD(expand, height, 0.5f, 0.5f);
                } else {
                    expand = AnimationUtil.moveUD(expand, height, 0.2f, 0.15f);
                }
            }
        } else {
            if (buttonanim) {
                expand = AnimationUtil.moveUD(expand, 0, 0.2f, 0.15f);
            } else {
                if (category != Category.WORLD) {
                    expand = AnimationUtil.moveUD(expand, 0, 0.5f, 0.5f);
                } else {
                    expand = AnimationUtil.moveUD(expand, 0, 0.2f, 0.15f);
                }
            }
        }
        Gui.drawRect(x - 1, y, x + 91 + allX, y + 17, new Color(29, 29, 29).getRGB());
        RenderUtils.drawBorderedRect(x - 0.5f, y - 0.5f, x + 91 + allX, y + expand, 0.05f, new Color(29, 29, 29).getRGB(), new Color(40, 40, 40).getRGB());

        Fonts.INSTANCE.getFont35().drawStringWithShadow(label(), x + 4, y + 5, -1);

        if (expand > 0) {
            buttons.forEach(b2 -> b2.render(mouseX, mouseY, x, y + 16, x + 90 + allX, (int) (y + expand)));
        }

        if (drag) {
            if (!Mouse.isButtonDown(0)) {
                drag = false;
            }
            x = mouseX - dragX;
            y = mouseY - dragY;
            if (!buttons.isEmpty()) {
                buttons.get(0).y = y + 22 - scroll;
            }
            for (Button b4 : buttons) {
                b4.x = x + 5;
            }
        }
    }

    protected void runWheel(int height) {
        if (Mouse.hasWheel()) {
            int wheel = Mouse.getDWheel();
            if (totalY - height <= 0) {
                return;
            }
            if (wheel < 0) {
                if (wheely < totalY - height) {
                    wheely += 20 + Mouse.getDWheel();
                    if (wheely < 0) {
                        wheely = 0;
                    }
                }
            } else if (wheel > 0) {
                wheely -= 20 + Mouse.getDWheel();
                if (wheely < 0) {
                    wheely = 0;
                }
            }
        }
    }

    public void key(char typedChar, int keyCode) {
        buttons.forEach(b2 -> b2.key(typedChar, keyCode));
    }

    public void mouseScroll(int mouseX, int mouseY, int amount) {
        if (mouseX > x - 2 && mouseX < x + 92 && mouseY > y - 2 && mouseY < y + 17 + expand) {
            scrollTo = (int) ((float) scrollTo - (amount / 120 * 28));
        }
    }

    public void click(int mouseX, int mouseY, int button) {
        if (mouseX > x - 2 && mouseX < x + 92 && mouseY > y - 2 && mouseY < y + 17) {
            if (button == 1) {
                extended = !extended;
            }
            if (button == 0) {
                drag = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            }
        }
        if (extended) {
            buttons.stream().filter(b2 -> b2.y < y + expand).forEach(b2 -> b2.click(mouseX, mouseY, button));
        }
    }
}
