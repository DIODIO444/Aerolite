package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.features.module.utils.CastHelper;
import net.ccbluex.liquidbounce.features.module.utils.Circle;
import net.ccbluex.liquidbounce.features.module.utils.WexSideUtils;
import net.ccbluex.liquidbounce.config.BoolValue;
import net.ccbluex.liquidbounce.config.FloatValue;
import net.ccbluex.liquidbounce.config.IntValue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;


public class JumpCircle2 extends Module {
    public JumpCircle2() {
        super("JumpCircle2", Category.RENDER, org.lwjgl.input.Keyboard.KEY_NONE, true);
    }

    private final BoolValue rainbow = new BoolValue("Rainbow", false);
    private final IntValue redValue = new IntValue("Red", 255, 0, 255);
    private final IntValue greenValue = new IntValue("Green", 255, 0, 255);
    private final IntValue blueValue = new IntValue("Blue", 255, 0, 255);
    private final IntValue alphaValue = new IntValue("Alpha", 255, 0, 255);
    private final FloatValue widthValue = new FloatValue("Width", 1f, 0.2f, 3f);
    private final BoolValue fade = new BoolValue("Fade", true);
    public List<Circle> circles = new ArrayList();
    public boolean raycastGround;
    @EventTarget
    public void onRender3D(Render3DEvent event) {
        getMc().entityRenderer.setupCameraTransform(event.getPartialTicks(), 2);
        for (EntityPlayer player : getMc().theWorld.playerEntities) {
            CastHelper castHelper = new CastHelper();
            castHelper.apply(CastHelper.EntityType.PLAYERS);
            castHelper.apply(CastHelper.EntityType.FRIENDS);
            castHelper.apply(CastHelper.EntityType.SELF);
            CastHelper.EntityType type;
            if ((type = CastHelper.isInstanceof(player, castHelper.build())) != null) {
                if (this.getState()) { // Why this?
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glDisable(GL11.GL_ALPHA_TEST);
                    GL11.glDisable(GL11.GL_CULL_FACE);
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glShadeModel(GL11.GL_SMOOTH);
                    GL11.glLineWidth(widthValue.get());
                    if (fade.get()) GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    for (Circle circle : circles) {
                        GL11.glBegin(GL11.GL_QUAD_STRIP);
                        for (int i = 0; i <= 360; i += 5) {
                            float red = redValue.get() / 255f, green = greenValue.get() / 255f, blue = blueValue.get() / 255f;
                            if (rainbow.get()) {
                                int rainbow = WexSideUtils.rainbow(i / 360F);
                                red = ((rainbow >> 16) & 255) / 255F;
                                green = ((rainbow >> 8) & 255) / 255F;
                                blue = (rainbow & 255) / 255F;
                            }
                            Vec3 pos = circle.pos();
                            double x = Math.cos(Math.toRadians(i)) * WexSideUtils.createAnimation((1 - circle.getAnimation(event.getPartialTicks()))) * 0.6;
                            double z = Math.sin(Math.toRadians(i)) * WexSideUtils.createAnimation((1 - circle.getAnimation(event.getPartialTicks()))) * 0.6;
                            GL11.glColor4d(red, green, blue, (alphaValue.get() / 255f) * circle.getAnimation(event.getPartialTicks()));
                            GL11.glVertex3d(pos.xCoord + x, pos.yCoord + 0.2f, pos.zCoord + z);
                            GL11.glColor4d(red, green, blue, (alphaValue.get() / 255f) * 0.2 * circle.getAnimation(event.getPartialTicks()));
                            GL11.glVertex3d(pos.xCoord + x * 1.4, pos.yCoord + 0.2f, pos.zCoord + z * 1.4);
                        }
                        GL11.glEnd();
                    }
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    GL11.glEnable(GL11.GL_ALPHA_TEST);
                    GL11.glShadeModel(GL11.GL_FLAT);
                    GL11.glEnable(GL11.GL_CULL_FACE);
                    GL11.glPopMatrix();
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        for (EntityPlayer player : getMc().theWorld.playerEntities) {
            circles.removeIf(Circle::update);
        }
        if (getMc().thePlayer.onGround && !raycastGround) { // Bad Detect
            circles.add(new Circle(getMc().thePlayer.getPositionVector()));
        }
        raycastGround = getMc().thePlayer.onGround;
    }

}
