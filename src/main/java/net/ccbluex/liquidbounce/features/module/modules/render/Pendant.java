package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.utils.render.AnimationUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.config.BoolValue;
import net.ccbluex.liquidbounce.config.FloatValue;
import net.ccbluex.liquidbounce.config.ListValue;
import net.minecraft.util.ResourceLocation;


public class Pendant extends Module {
    public Pendant() {
        super("Pendant", Category.RENDER, org.lwjgl.input.Keyboard.KEY_NONE, true);
    }


    public final ListValue Fubukistyle = new ListValue("Fubukistyle", new String[] { "GIF", "Static"}, "GIF");
    public static BoolValue Taco = new BoolValue("Taco", false);
    public static BoolValue Fubuki = new BoolValue("Fubuki", false);
    public final FloatValue positionY = new FloatValue("PositionY", 5.0F, 130.0F, 1000.0F);
    public final FloatValue positionX = new FloatValue("PositionX", 5.0F, 130.0F, 1000.0F);
    public final FloatValue size = new FloatValue("size", 50.0F, 10.0F, 1000.0F);
    float posX = 0.0F;

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (Pendant.Taco.get()) {
            this.Taco();
        }

        if (Pendant.Fubuki.get()) {
            this.Fubuki();
        }

    }

    public void Fubuki() {
        if (this.Fubukistyle.get().contains("GIF")) {
            int state = Pendant.mc.thePlayer.ticksExisted % 16 + 1;

            RenderUtils.drawImage3(new ResourceLocation("aerolite/fubuki/" + state + ".png"), (float) ((Float) this.positionX.get()).intValue(), (float) (RenderUtils.height() - this.positionY.get().intValue()), (float) this.size.get().intValue(), (float) ((Float) this.size.get()).intValue());
        }

        if (this.Fubukistyle.get().contains("Static")) {
            RenderUtils.drawImage3(new ResourceLocation("aerolite/fubuki/Static.png"), (float) ((Float) this.positionX.get()).intValue(), (float) (RenderUtils.height() - this.positionY.get().intValue()), 77.0F, 250.0F);
        }

    }

    public void Taco() {
        if (this.posX < (float) RenderUtils.width()) {
            this.posX = (float) ((double) this.posX + AnimationUtils.INSTANCE.getDelta() * 0.1D);
        } else {
            this.posX = 0.0F;
        }

        int state = Pendant.mc.thePlayer.ticksExisted % 12 + 1;

        RenderUtils.drawImage3(new ResourceLocation("aerolite/taco/" + state + ".png"), this.posX, (float) (RenderUtils.height() - 80), 42.0F, 27.0F);
    }
}

