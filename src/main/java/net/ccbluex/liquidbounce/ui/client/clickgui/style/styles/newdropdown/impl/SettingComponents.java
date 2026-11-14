package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.impl;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.NewFonts;
import net.ccbluex.liquidbounce.ui.font.FontInfo;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;


import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.MathUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.animations.impl.EaseInOutQuad;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.normal.Main;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.objects.PasswordField;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.render.DrRenderUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.newdropdown.utils.render.GuiEvents;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.utils.render.RoundedUtil;
import net.ccbluex.liquidbounce.config.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

public class SettingComponents extends Component {
    public static float scale;
    private final Module module;
    public Animation settingHeightScissor;
    private final HashMap<Module, Animation[]> keySettingAnimMap = new HashMap<>();
    private final HashMap<IntValue, Float> sliderintMap = new HashMap<>();
    private final HashMap<IntValue, Animation[]> sliderintAnimMap = new HashMap<>();
    private final HashMap<FloatValue, Float> sliderfloatMap = new HashMap<>();
    private final HashMap<FloatValue, Animation[]> sliderfloatAnimMap = new HashMap<>();
    private final HashMap<Value, Float> sliderMap = new HashMap<>();
    private final HashMap<Value, Animation[]> sliderAnimMap = new HashMap<>();
    private final HashMap<BoolValue, Animation[]> toggleAnimation = new HashMap<>();
    private final HashMap<ListValue, Animation[]> modeSettingAnimMap = new HashMap<>();
    private final HashMap<ListValue, Boolean> modeSettingClick = new HashMap<>();
    private final HashMap<ListValue, HashMap<String, Animation>> modesHoverAnimation = new HashMap<>();
    public Module binding;
    public Value draggingNumber;
    public float x, y, width, rectHeight, panelLimitY;
    public int alphaAnimation;
    public double settingSize;
    private PasswordField selectedField;
    private TextValue selectedStringSetting;
    private boolean hueFlag;

    public SettingComponents(Module module) {
        this.module = module;
        keySettingAnimMap.put( module, new Animation[]{new EaseInOutQuad(250, 1, Direction.BACKWARDS),
                new DecelerateAnimation(225, 1, Direction.BACKWARDS)});

        for (Value setting : module.getValues()) {

            if (setting.get() instanceof Number && !(setting instanceof IntValue) && !(setting instanceof FloatValue)) {
                sliderMap.put((Value) setting, 0f);
                sliderAnimMap.put((Value) setting, new Animation[]{new DecelerateAnimation(250, 1, Direction.BACKWARDS), new DecelerateAnimation(200, 1, Direction.BACKWARDS)});
            }
            if (setting instanceof FloatValue) {
                sliderfloatMap.put((FloatValue) setting, 0f);
                sliderfloatAnimMap.put((FloatValue) setting, new Animation[]{new DecelerateAnimation(250, 1, Direction.BACKWARDS), new DecelerateAnimation(200, 1, Direction.BACKWARDS)});
            }
            if (setting instanceof IntValue) {
                sliderintMap.put((IntValue) setting, 0f);
                sliderintAnimMap.put((IntValue) setting, new Animation[]{new DecelerateAnimation(250, 1, Direction.BACKWARDS), new DecelerateAnimation(200, 1, Direction.BACKWARDS)});
            }
            if (setting instanceof BoolValue) {
                toggleAnimation.put((BoolValue) setting, new Animation[]{new DecelerateAnimation(225, 1, Direction.BACKWARDS),
                        new DecelerateAnimation(200, 1, Direction.BACKWARDS)});
            }
            if (setting instanceof ListValue) {
                ListValue modeSetting = (ListValue) setting;
                modeSettingClick.put(modeSetting, false);
                modeSettingAnimMap.put(modeSetting, new Animation[]{new DecelerateAnimation(225, 1, Direction.BACKWARDS),
                        new EaseInOutQuad(250, 1, Direction.BACKWARDS)});

                HashMap<String, Animation> modeMap = new HashMap<>();
                for (String mode : modeSetting.getValues()) {
                    modeMap.put(mode, new DecelerateAnimation(225, 1, Direction.BACKWARDS));
                }

                modesHoverAnimation.put(modeSetting, modeMap);
            }



        }
    }

    @Override
    public void initGui() {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        /*
        if (binding != null) {
            selectedField = null;
            selectedStringSetting = null;
            if (keyCode == Keyboard.KEY_SPACE || keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE)
                binding.setKeyBind(Keyboard.KEY_NONE);

                binding.setKeyBind(keyCode);
            binding = null;
            return;
        }

         */


        if (selectedField != null) {
            if (keyCode == 1) {
                selectedField = null;
                selectedStringSetting = null;
                return;
            }
            selectedField.textboxKeyTyped(typedChar, keyCode);
            selectedStringSetting.set(selectedField.getText());
        }
    }


    public void handle(int mouseX, int mouseY, int button, GuiEvents type) {
        //Setting up the colors
        Color textColor = new Color(255, 255, 255, alphaAnimation);
        Color darkRectColor = new Color(48, 50, 55, alphaAnimation);
        Color darkRectColorDisabled = new Color(52, 52, 52, alphaAnimation);
        Color darkRectHover = DrRenderUtils.brighter(darkRectColor, .8f);


        // HudMod hudMod = (HudMod) Tenacity.INSTANCE.getModuleCollection().get(HudMod.class);
        Color[] colors = new Color[2];
        boolean accent = true;

        Color color2 = new Color(new Color(ClickGUI.INSTANCE.getGuiColor(), true).getRGB());
        colors = new Color[]{color2,color2};

        Color accentedColor = DrRenderUtils.applyOpacity(colors[0], alphaAnimation / 255f);
        Color accentedColor2 = DrRenderUtils.applyOpacity(colors[1], alphaAnimation / 255f);


        double count = 0;



        //
        for (Value setting : module.getValues()) {
            if (!setting.shouldRender())
                continue;

            float settingY = (float) MathUtils.roundToHalf(y + (count * rectHeight));
            if (setting instanceof FloatValue) {
                FloatValue numberSetting = (FloatValue) setting;

                String value = Float.toString((float) MathUtils.round(numValue(numberSetting), 0.01));
                float regularFontWidth = (float) NewFonts.SF18.stringWidth(numberSetting.getName() + ": ");
                float valueFontWidth = (float) NewFonts.SF18.stringWidth(value);

                float titleX = x + width / 2f - (regularFontWidth + valueFontWidth) / 2f;
                float titleY = settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) -
                        NewFonts.SF18.getMiddleOfBox(rectHeight) / 2f + 1;
                GlStateManager.color(1, 1, 1, 1);

                NewFonts.SF18.drawString(numberSetting.getName() + ": ", titleX, titleY, textColor.getRGB());
                GlStateManager.color(1, 1, 1, 1);

                NewFonts.SFBOLD18.drawString(value, titleX + regularFontWidth, titleY, textColor.getRGB());


                Animation hoverAnimation = sliderfloatAnimMap.get(numberSetting)[0];
                Animation selectAnimtion = sliderfloatAnimMap.get(numberSetting)[1];

                float totalSliderWidth = width - 10;
                boolean hoveringSlider = isClickable(settingY + 17) && DrRenderUtils.isHovering(x + 5, settingY + 17, totalSliderWidth, 6, mouseX, mouseY);

                if (type == GuiEvents.RELEASE) {
                    draggingNumber = null;
                }
                hoverAnimation.setDirection(hoveringSlider || draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS);
                selectAnimtion.setDirection(draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS);
                if (type == GuiEvents.CLICK && hoveringSlider && button == 0) {
                    draggingNumber = numberSetting;
                }


                double currentValue = numValue(numberSetting);


                if (draggingNumber != null && draggingNumber == setting) {
                    float percent = Math.min(1, Math.max(0, (mouseX - (x + 5)) / totalSliderWidth));
                    double newValue = (percent * (numMax(numberSetting)
                            - numMin(numberSetting))) + numMin(numberSetting);
                    numSet(numberSetting, newValue);
                }

                float sliderMath = (float) (((currentValue) - numMin(numberSetting))
                        / (numMax(numberSetting) - numMin(numberSetting)));


                sliderfloatMap.put(numberSetting, (float) DrRenderUtils.animate(totalSliderWidth * sliderMath, sliderfloatMap.get(numberSetting), .1));


                float sliderY = (settingY + 18);
                RoundedUtil.drawRound(x + 5, sliderY, totalSliderWidth, 3, 1.5f, DrRenderUtils.applyOpacity(darkRectHover, (float) (.4f + (.2 * hoverAnimation.getOutput()))));
                RoundedUtil.drawRound(x + 5, sliderY, Math.max(4, sliderfloatMap.get(numberSetting)), 3, 1.5f, accent ? accentedColor2 : textColor);

                DrRenderUtils.setAlphaLimit(0);
                DrRenderUtils.fakeCircleGlow(x + 4 + Math.max(4, sliderfloatMap.get(numberSetting)), sliderY + 1.5f, 6,
                        Color.BLACK, .3f);

                DrRenderUtils.drawGoodCircle(x + 4 + Math.max(4, sliderfloatMap.get(numberSetting)), sliderY + 1.5f, 3.75f, accent ? accentedColor2.getRGB() : textColor.getRGB());
                count += .5f;
            }
            if (setting instanceof IntValue) {
                IntValue numberSetting = (IntValue) setting;

                String value = Float.toString((float) MathUtils.round(numValue(numberSetting), 1));
                float regularFontWidth = (float) NewFonts.SF18.stringWidth(numberSetting.getName() + ": ");
                float valueFontWidth = (float) NewFonts.SF18.stringWidth(value);

                float titleX = x + width / 2f - (regularFontWidth + valueFontWidth) / 2f;
                float titleY = settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) -
                        NewFonts.SF18.getMiddleOfBox(rectHeight) / 2f + 1;
                GlStateManager.color(1, 1, 1, 1);

                NewFonts.SF18.drawString(numberSetting.getName() + ": ", titleX, titleY, textColor.getRGB());
                GlStateManager.color(1, 1, 1, 1);

                NewFonts.SFBOLD18.drawString(value, titleX + regularFontWidth, titleY, textColor.getRGB());


                Animation hoverAnimation = sliderintAnimMap.get(numberSetting)[0];
                Animation selectAnimtion = sliderintAnimMap.get(numberSetting)[1];

                float totalSliderWidth = width - 10;
                boolean hoveringSlider = isClickable(settingY + 17) && DrRenderUtils.isHovering(x + 5, settingY + 17, totalSliderWidth, 6, mouseX, mouseY);

                if (type == GuiEvents.RELEASE) {
                    draggingNumber = null;
                }
                hoverAnimation.setDirection(hoveringSlider || draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS);
                selectAnimtion.setDirection(draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS);
                if (type == GuiEvents.CLICK && hoveringSlider && button == 0) {
                    draggingNumber = numberSetting;
                }


                double currentValue = numValue(numberSetting);


                if (draggingNumber != null && draggingNumber == setting) {
                    float percent = Math.min(1, Math.max(0, (mouseX - (x + 5)) / totalSliderWidth));
                    double newValue = (percent * (numMax(numberSetting)
                            - numMin(numberSetting))) + numMin(numberSetting);
                    numSet(numberSetting, newValue);
                }

                float sliderMath = (float) (((currentValue) - numMin(numberSetting))
                        / (numMax(numberSetting) - numMin(numberSetting)));


                sliderintMap.put(numberSetting, (float) DrRenderUtils.animate(totalSliderWidth * sliderMath, sliderintMap.get(numberSetting), .1));


                float sliderY = (settingY + 18);
                RoundedUtil.drawRound(x + 5, sliderY, totalSliderWidth, 3, 1.5f, DrRenderUtils.applyOpacity(darkRectHover, (float) (.4f + (.2 * hoverAnimation.getOutput()))));
                RoundedUtil.drawRound(x + 5, sliderY, Math.max(4, sliderintMap.get(numberSetting)), 3, 1.5f, accent ? accentedColor2 : textColor);

                DrRenderUtils.setAlphaLimit(0);
                DrRenderUtils.fakeCircleGlow(x + 4 + Math.max(4, sliderintMap.get(numberSetting)), sliderY + 1.5f, 6,
                        Color.BLACK, .3f);

                DrRenderUtils.drawGoodCircle(x + 4 + Math.max(4, sliderintMap.get(numberSetting)), sliderY + 1.5f, 3.75f, accent ? accentedColor2.getRGB() : textColor.getRGB());
                count += .5f;
            }
            if (setting.get() instanceof Number && !(setting instanceof IntValue) && !(setting instanceof FloatValue)) {
                Value numberSetting = (Value) setting;

                String value = Float.toString((float) MathUtils.round(numValue(numberSetting), numInc(numberSetting)));
                float regularFontWidth = (float) NewFonts.SF18.stringWidth(numberSetting.getName() + ": ");
                float valueFontWidth = (float) NewFonts.SF18.stringWidth(value);

                float titleX = x + width / 2f - (regularFontWidth + valueFontWidth) / 2f;
                float titleY = settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) -
                        NewFonts.SF18.getMiddleOfBox(rectHeight) / 2f + 1;
                GlStateManager.color(1, 1, 1, 1);

                NewFonts.SF18.drawString(numberSetting.getName() + ": ", titleX, titleY, textColor.getRGB());
                GlStateManager.color(1, 1, 1, 1);

                NewFonts.SFBOLD18.drawString(value, titleX + regularFontWidth, titleY, textColor.getRGB());


                Animation hoverAnimation = sliderAnimMap.get(numberSetting)[0];
                Animation selectAnimtion = sliderAnimMap.get(numberSetting)[1];

                float totalSliderWidth = width - 10;
                boolean hoveringSlider = isClickable(settingY + 17) && DrRenderUtils.isHovering(x + 5, settingY + 17, totalSliderWidth, 6, mouseX, mouseY);

                if (type == GuiEvents.RELEASE) {
                    draggingNumber = null;
                }
                hoverAnimation.setDirection(hoveringSlider || draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS);
                selectAnimtion.setDirection(draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS);
                if (type == GuiEvents.CLICK && hoveringSlider && button == 0) {
                    draggingNumber = numberSetting;
                }


                double currentValue = numValue(numberSetting);


                if (draggingNumber != null && draggingNumber == setting) {
                    float percent = Math.min(1, Math.max(0, (mouseX - (x + 5)) / totalSliderWidth));
                    double newValue = (percent * (numMax(numberSetting)
                            - numMin(numberSetting))) + numMin(numberSetting);
                    numSet(numberSetting, newValue);
                }

                float sliderMath = (float) (((currentValue) - numMin(numberSetting))
                        / (numMax(numberSetting) - numMin(numberSetting)));


                sliderMap.put(numberSetting, (float) DrRenderUtils.animate(totalSliderWidth * sliderMath, sliderMap.get(numberSetting), .1));


                float sliderY = (settingY + 18);
                RoundedUtil.drawRound(x + 5, sliderY, totalSliderWidth, 3, 1.5f, DrRenderUtils.applyOpacity(darkRectHover, (float) (.4f + (.2 * hoverAnimation.getOutput()))));
                RoundedUtil.drawRound(x + 5, sliderY, Math.max(4, sliderMap.get(numberSetting)), 3, 1.5f, accent ? accentedColor2 : textColor);

                DrRenderUtils.setAlphaLimit(0);
                DrRenderUtils.fakeCircleGlow(x + 4 + Math.max(4, sliderMap.get(numberSetting)), sliderY + 1.5f, 6,
                        Color.BLACK, .3f);

                DrRenderUtils.drawGoodCircle(x + 4 + Math.max(4, sliderMap.get(numberSetting)), sliderY + 1.5f, 3.75f, accent ? accentedColor2.getRGB() : textColor.getRGB());
                count += .5f;
            }
            if (setting instanceof BoolValue) {
                BoolValue booleanSetting = (BoolValue) setting;

                Animation toggleAnimation = this.toggleAnimation.get(booleanSetting)[0];
                Animation hoverAnimation = this.toggleAnimation.get(booleanSetting)[1];

                DrRenderUtils.resetColor();
                OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
                GlStateManager.enableBlend();

                NewFonts.SF18.drawString(booleanSetting.getName(), (int) MathUtils.roundToHalf(x + 4), settingY +
                        5, textColor.getRGB());

                float switchWidth = 16;

                boolean hoveringSwitch = isClickable(settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) - 1) &&
                        DrRenderUtils.isHovering(x + width - (switchWidth + 6), settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) - 1,
                                switchWidth, 8, mouseX, mouseY);

                hoverAnimation.setDirection(hoveringSwitch ? Direction.FORWARDS : Direction.BACKWARDS);

                if (type == GuiEvents.CLICK && hoveringSwitch && button == 0) {
                    booleanSetting.toggle();
                }

                toggleAnimation.setDirection(booleanSetting.get() ? Direction.FORWARDS : Direction.BACKWARDS);
                DrRenderUtils.resetColor();
                Color accentCircle = accent ? DrRenderUtils.applyOpacity(accentedColor, .8f) : DrRenderUtils.darker(textColor, .8f);
                RoundedUtil.drawRound(x + width - (switchWidth + 5.5f), settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) + 2, switchWidth, 4.5f,
                        2, DrRenderUtils.interpolateColorC(DrRenderUtils.applyOpacity(darkRectHover, .5f), accentCircle, (float) toggleAnimation.getOutput()));

                DrRenderUtils.fakeCircleGlow((float) ((x + width - (switchWidth + 3)) + ((switchWidth - 5) * toggleAnimation.getOutput())),
                        settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) + 4, 6, Color.BLACK, .3f);

                DrRenderUtils.resetColor();

                RoundedUtil.drawRound((float) (x + width - (switchWidth + 6) + ((switchWidth - 5) * toggleAnimation.getOutput())),
                        settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) + 1, 6.5f,
                        6.5f, 3, textColor);


            }
            if (setting instanceof ListValue) {
                ListValue modeSetting = (ListValue) setting;
                Animation hoverAnimation = modeSettingAnimMap.get(modeSetting)[0];
                Animation openAnimation = modeSettingAnimMap.get(modeSetting)[1];

                boolean hoveringModeSettingRect = isClickable(settingY + 5) &&
                        DrRenderUtils.isHovering(x + 5, settingY + 5, width - 10, rectHeight + 7, mouseX, mouseY);

                if (type == GuiEvents.CLICK && hoveringModeSettingRect && button == 1) {
                    modeSettingClick.put(modeSetting, !modeSettingClick.get(modeSetting));
                }

                hoverAnimation.setDirection(hoveringModeSettingRect ? Direction.FORWARDS : Direction.BACKWARDS);
                openAnimation.setDirection(modeSettingClick.get(modeSetting) ? Direction.FORWARDS : Direction.BACKWARDS);

                float math = (modeSetting.getValues().length - 1) * rectHeight;
                RoundedUtil.drawRound(x + 5, (float) (settingY + rectHeight + 2 + (12 * openAnimation.getOutput())),
                        width - 10, (float) (math * openAnimation.getOutput()), 3, DrRenderUtils.applyOpacity(darkRectHover, (float) (.35f * openAnimation.getOutput())));

                if (!openAnimation.isDone() && type == GuiEvents.DRAW) {
                    GL11.glEnable(GL11.GL_SCISSOR_TEST);
                    DrRenderUtils.scissor(x + 5, (float) (settingY + 7 + rectHeight + (3 * openAnimation.getOutput())),
                            width - 10, (float) (math * openAnimation.getOutput()));
                }
                float modeCount = 0;
                for (String mode : modeSetting.getValues()) {
                    if (mode.equalsIgnoreCase(modeSetting.get())) continue;
                    float modeY = (float) (settingY + rectHeight + 11 + ((8 + (modeCount * rectHeight)) * openAnimation.getOutput()));
                    DrRenderUtils.resetColor();

                    //指针所指的String
                    boolean hoveringMode = isClickable(modeY - 5) && openAnimation.getDirection().equals(Direction.FORWARDS) &&
                            DrRenderUtils.isHovering(x + 5, modeY - 5, width - 10, rectHeight, mouseX, mouseY);
                    Animation modeHoverAnimation = modesHoverAnimation.get(modeSetting).get(mode);




                    modeHoverAnimation.setDirection(hoveringMode ? Direction.FORWARDS : Direction.BACKWARDS);


                    if (modeHoverAnimation.finished(Direction.FORWARDS) || !modeHoverAnimation.isDone()) {
                        RoundedUtil.drawRound(x + 5, modeY - 5, width - 10, rectHeight, 3,
                                DrRenderUtils.applyOpacity(textColor, (float) (.2f * modeHoverAnimation.getOutput())));
                    }
                    if (type == GuiEvents.CLICK && button == 0 && hoveringMode) {
                        modeSettingClick.put(modeSetting, !modeSettingClick.get(modeSetting));
                        modeSetting.set(mode);
                    }
                    if (openAnimation.isDone() && openAnimation.getDirection().equals(Direction.FORWARDS) || !openAnimation.isDone()) {
                        //      RoundedUtil.drawRound(x + 5 + ((width - 10) / 2f - selectRectWidth / 2f), settingY + rectHeight + 10.5f,
                        //    Math.max(2, selectRectWidth), 1.5f, .5f, accent ? accentedColor2 : textColor);
                        NewFonts.SF18.drawString(mode, x + 13, modeY,DrRenderUtils.applyOpacity(textColor, (float) openAnimation.getOutput()).getRGB());
                    }
                    //   NewFonts.SF18.drawString(mode, x + 13, modeY,DrRenderUtils.applyOpacity(textColor, (float) openAnimation.getOutput()).getRGB());
                    modeCount++;
                }
                if (!openAnimation.isDone() && type == GuiEvents.DRAW) {
                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
                }
                if (settingHeightScissor.isDone() && openAnimation.isDone() && GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) {
                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
                }

                RoundedUtil.drawRound(x + 5, settingY + 5, width - 10, rectHeight + 7, 3, DrRenderUtils.applyOpacity(darkRectHover, .45f));

                if(!hoverAnimation.isDone() || hoverAnimation.finished(Direction.FORWARDS)) {
                    RoundedUtil.drawRound(x + 5, settingY + 5, width - 10, rectHeight + 7, 3, DrRenderUtils.applyOpacity(textColor, (float) (.2f * hoverAnimation.getOutput())));
                }


                float selectRectWidth = (float) ((width - 10) * openAnimation.getOutput());
                // DrRenderUtils.renderRoundedRect(x + 5, settingY + rectHeight + 7, width - 10, 2, .5f, disabledTextColor.getRGB());
                if (openAnimation.isDone() && openAnimation.getDirection().equals(Direction.FORWARDS) || !openAnimation.isDone()) {
                    RoundedUtil.drawRound(x + 5 + ((width - 10) / 2f - selectRectWidth / 2f), settingY + rectHeight + 10.5f,
                            Math.max(2, selectRectWidth), 1.5f, .5f, accent ? accentedColor2 : textColor);
                    //   NewFonts.SF18.drawString(mode, x + 13, modeY,DrRenderUtils.applyOpacity(textColor, (float) openAnimation.getOutput()).getRGB());
                }

                //Mode的名字
                NewFonts.SF14.drawString(modeSetting.getName(), x + 13, settingY + 9, textColor.getRGB());

                DrRenderUtils.resetColor();
                //当先选择的mode
                NewFonts.SFBOLD18.drawString(modeSetting.get(), x + 13, (float) (settingY + 17.5), textColor.getRGB());

                DrRenderUtils.resetColor();
                DrRenderUtils.drawClickGuiArrow(x + width - 15, settingY + 17, 5, openAnimation, textColor.getRGB());


                count += 1 + ((math / rectHeight) * openAnimation.getOutput());
            }
            if (setting instanceof TextValue) {
                TextValue stringSetting = (TextValue) setting;

                DrRenderUtils.resetColor();
                NewFonts.SF16.drawString(stringSetting.getName(), x + 5, settingY + 2, textColor.getRGB());

                PasswordField stringSettingField = new PasswordField("Type Here...", 0,
                        (int) (x + 5), (int) (settingY + 15), (int) (width - 10), 10, NewFonts.SF18.font);

                stringSettingField.setText(stringSetting.get());
                stringSettingField.setFocused(selectedStringSetting == stringSetting);
                stringSettingField.bottomBarColor = textColor.getRGB();
                stringSettingField.textColor = textColor.getRGB();
                stringSettingField.placeHolderTextX = x + 30;
                if (type == GuiEvents.CLICK) stringSettingField.mouseClicked(mouseX, mouseY, button);
                if (stringSettingField.isFocused()) {
                    selectedField = stringSettingField;
                    selectedStringSetting = stringSetting;

                } else if (selectedStringSetting == stringSetting) {
                    selectedStringSetting = null;
                    selectedField = null;
                }

                stringSettingField.drawTextBox();
                //Writes to StringSetting
                stringSetting.set(stringSettingField.getText());

                count++;
            }
            if (setting instanceof FontValue) {
                FontValue fontSetting = (FontValue) setting;

                DrRenderUtils.resetColor();
                NewFonts.SF18.drawString(fontSetting.getName(), x + 4,
                        settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) + 1, textColor.getRGB());

                String fontName = fontLabel(fontSetting.get());
                float pillWidth = NewFonts.SFBOLD18.stringWidth(fontName) + 10;
                float pillX = x + width - pillWidth - 5;
                float pillY = settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) - 2;

                boolean hoveringFont = isClickable(pillY)
                        && DrRenderUtils.isHovering(pillX, pillY, pillWidth, 12, mouseX, mouseY);

                RoundedUtil.drawRound(pillX, pillY, pillWidth, 12, 3,
                        DrRenderUtils.applyOpacity(darkRectHover, hoveringFont ? .85f : .45f));

                NewFonts.SFBOLD18.drawString(fontName, pillX + 5, pillY + 3.5f, textColor.getRGB());

                if (type == GuiEvents.CLICK && hoveringFont && button == 0) {
                    cycleFont(fontSetting);
                }
            }
            /*
                String bind = Keyboard.getKeyName(module.getKeyBind());

                boolean hoveringBindRect = isClickable(settingY + NewFonts.SFBOLD18.getMiddleOfBox(rectHeight) - 1)
                        && DrRenderUtils.isHovering((float) (x + width - (NewFonts.SFBOLD18.stringWidth(bind) + 7)),
                        settingY + NewFonts.SFBOLD18.getMiddleOfBox(rectHeight) - 1, (float) (NewFonts.SFBOLD18.stringWidth(bind) + 4),
                        NewFonts.SFBOLD18.getHeight() + 4, mouseX, mouseY);

                if (type == GuiEvents.CLICK && hoveringBindRect && button == 0) {
                    binding = module;
                    return;
                }
                Animation[] animations = keySettingAnimMap.get(module);

                animations[1].setDirection(binding == module ? Direction.FORWARDS : Direction.BACKWARDS);

                NewFonts.SF18.drawString("Bind", x + 5,
                        settingY + NewFonts.SF18.getMiddleOfBox(rectHeight) + 1,
                        textColor.getRGB());


                animations[0].setDirection(hoveringBindRect ? Direction.FORWARDS : Direction.BACKWARDS);


                RoundedUtil.drawRound((float) (x + width - (NewFonts.SFBOLD18.stringWidth(bind) + 9)),
                        settingY + NewFonts.SFBOLD18.getMiddleOfBox(rectHeight) - 1, (float) (NewFonts.SFBOLD18.stringWidth(bind) + 5),
                        NewFonts.SFBOLD18.getHeight() + 3.5f, 5, DrRenderUtils.applyOpacity(darkRectHover, (float) (.4 + (.2 * animations[0].getOutput()))));

                NewFonts.SFBOLD18.drawString(bind, x + width - (NewFonts.SFBOLD18.stringWidth(bind) + 6),
                        settingY + NewFonts.SFBOLD18.getMiddleOfBox(rectHeight) + 1,
                        DrRenderUtils.interpolateColor(textColor.getRGB(), accentedColor2.getRGB(), (float) animations[1].getOutput()));


             */
            count++;
        }
        settingSize = count;
        // settingSize = count;
    }


    @Override
    public void drawScreen(int mouseX, int mouseY) {
        handle(mouseX, mouseY, -1, GuiEvents.DRAW);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        handle(mouseX, mouseY, button, GuiEvents.CLICK);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        handle(mouseX, mouseY, state, GuiEvents.RELEASE);
    }


    public boolean isClickable(float y) {
        return y > panelLimitY && y < panelLimitY + 17 + Main.getAllowedClickGUIHeight();
    }

    /* ---- numeric setting helpers (local IntValue/FloatValue share no common base) ---- */

    public static double numValue(Value v) {
        Object o = v.get();
        return o instanceof Number ? ((Number) o).doubleValue() : 0.0;
    }

    public static double numMin(Value v) {
        if (v instanceof IntValue) return ((IntValue) v).getMinimum();
        if (v instanceof FloatValue) return ((FloatValue) v).getMinimum();
        return 0.0;
    }

    public static double numMax(Value v) {
        if (v instanceof IntValue) return ((IntValue) v).getMaximum();
        if (v instanceof FloatValue) return ((FloatValue) v).getMaximum();
        return 1.0;
    }

    public static double numInc(Value v) {
        if (v instanceof IntValue) return 1.0;
        if (v instanceof FloatValue) return 0.1;
        return 0.01;
    }

    public static void numSet(Value v, double value) {
        if (v instanceof IntValue) {
            ((IntValue) v).set((int) Math.round(value));
        } else if (v instanceof FloatValue) {
            ((FloatValue) v).set((float) value);
        }
    }

    /* ---- font setting helpers (FontValue has no dedicated component in this style) ---- */

    private static String fontLabel(net.minecraft.client.gui.FontRenderer font) {
        if (font == Fonts.INSTANCE.getMinecraftFont())
            return "Minecraft";

        FontInfo info = Fonts.INSTANCE.getFontDetails(font);
        if (info == null)
            return "Unknown";

        return info.getSize() > 0 ? info.getName() + " " + info.getSize() : info.getName();
    }

    private static void cycleFont(FontValue value) {
        java.util.List<net.minecraft.client.gui.FontRenderer> fonts = Fonts.INSTANCE.getFonts();
        if (fonts.isEmpty())
            return;

        int index = -1;
        for (int i = 0; i < fonts.size(); i++) {
            if (fonts.get(i) == value.get()) {
                index = i;
                break;
            }
        }

        value.set(fonts.get((index + 1) % fonts.size()));
    }
}
