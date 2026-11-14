/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.config.FloatValue;
public class ItemPhysics extends Module
{
    public ItemPhysics() {
        super("ItemPhysics", Category.RENDER, org.lwjgl.input.Keyboard.KEY_NONE, true);
    }

    public final FloatValue itemWeight = new FloatValue("Weight", 0.5F, 0F, 1F, "x");


    /* ---- Java (mixin) compatibility accessors ---- */
    public boolean getRealistic() { return true; }

    public float getRotationSpeed() { return 1F; }

    public float getWeight() { return itemWeight.get(); }

    @Override
    public String getTag() {
        return itemWeight.get().toString();
    }
}
