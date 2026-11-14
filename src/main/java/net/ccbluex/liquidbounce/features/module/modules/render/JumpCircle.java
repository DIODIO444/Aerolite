package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.config.BoolValue;
import net.ccbluex.liquidbounce.config.FloatValue;
import net.ccbluex.liquidbounce.config.IntValue;
;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.CopyOnWriteArrayList;



public class JumpCircle extends Module {
    public JumpCircle() {
        super("JumpCircle", Category.RENDER, org.lwjgl.input.Keyboard.KEY_NONE, true);
    }

    private final IntValue redValue = new IntValue("Red", 255, 0, 255);
    private final IntValue greenValue = new IntValue("Green", 255, 0, 255);
    private final IntValue blueValue = new IntValue("Blue", 255, 0, 255);
    private final BoolValue rainbow = new BoolValue("Rainbow", false);
    private final IntValue radiusValue = new IntValue("Radius", 3, 1, 5);
    private final FloatValue widthValue = new FloatValue("Width", 0.5F, 0.1F, 50F);
    private final FloatValue strengthValue = new FloatValue("Strength", 0.02F, 0.01F, 0.2F);
    private final CopyOnWriteArrayList<Circle> circles = new CopyOnWriteArrayList<>();
    private boolean lastOnGround;

    @Override
    public void onEnable(){
        lastOnGround = true;
    }

    @EventTarget
    public void onUpdate(UpdateEvent ignored) {
        if (getMc().thePlayer.onGround && !lastOnGround) {
            circles.add(new Circle(getMc().thePlayer.posX, getMc().thePlayer.posY, getMc().thePlayer.posZ, getMc().thePlayer.lastTickPosX, getMc().thePlayer.lastTickPosY, getMc().thePlayer.lastTickPosZ, widthValue.get()));
        }
        lastOnGround = getMc().thePlayer.onGround;
    }

    @EventTarget
    public void onRender3D(Render3DEvent ignored) {
        if (!circles.isEmpty()) {
            for (Circle circle : circles) {
                if (circle.add(strengthValue.get()) > radiusValue.get()) {
                    circles.remove(circle);
                    continue;
                }
                GL11.glPushMatrix();
                GL11.glTranslated(
                        circle.posX - getMc().getRenderManager().renderPosX,
                        circle.posY - getMc().getRenderManager().renderPosY,
                        circle.posZ - getMc().getRenderManager().renderPosZ
                );
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                GL11.glLineWidth(circle.width);
                if (rainbow.get()) GL11.glColor4f(ColorUtils.INSTANCE.rainbow().getRed() / 255.0F,ColorUtils.INSTANCE.rainbow().getGreen() / 255.0F,ColorUtils.INSTANCE.rainbow().getBlue() / 255.0F,(radiusValue.get() - circle.radius) / radiusValue.get());
                else GL11.glColor4f(redValue.get() / 255.0F, greenValue.get() / 255.0F, blueValue.get() / 255.0F, (radiusValue.get() - circle.radius) / radiusValue.get());
                GL11.glRotatef(90F, 1F, 0F, 0F);

                GL11.glBegin(GL11.GL_LINE_STRIP);

                for (int i = 0; i <= 360; i += 5) { // You can change circle accuracy  (60 - accuracy)
                    GL11.glVertex2f(
                            (float) (Math.cos(i * Math.PI / 180.0) * circle.radius),
                            (float) (Math.sin(i * Math.PI / 180.0) * circle.radius)
                    );
                }

                GL11.glEnd();

                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glPopMatrix();
            }
        }
    }

    static class Circle {
        public double posX, posY, posZ, lastTickPosX, lastTickPosY, lastTickPosZ;
        public float radius, width;
        Circle(double posX, double posY, double posZ, double lastTickPosX, double lastTickPosY, double lastTickPosZ, float width) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.lastTickPosX = lastTickPosX;
            this.lastTickPosY = lastTickPosY;
            this.lastTickPosZ = lastTickPosZ;
            this.width = width;
        }
        public double add(double radius) {
            this.radius += radius;
            return this.radius;
        }
    }
}