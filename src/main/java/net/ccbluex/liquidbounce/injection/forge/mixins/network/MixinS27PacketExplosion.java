/*
 * Aerolite — FDPClient framework port
 * Accessor mixin exposing the private motion fields of S27PacketExplosion.
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import net.ccbluex.liquidbounce.injection.implementations.IMixinS27PacketExplosion;
import net.minecraft.network.play.server.S27PacketExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(S27PacketExplosion.class)
public abstract class MixinS27PacketExplosion implements IMixinS27PacketExplosion {

    @Accessor("field_149152_f")
    public abstract float getField_149152_f();

    @Accessor("field_149152_f")
    public abstract void setField_149152_f(float value);

    @Accessor("field_149153_g")
    public abstract float getField_149153_g();

    @Accessor("field_149153_g")
    public abstract void setField_149153_g(float value);

    @Accessor("field_149159_h")
    public abstract float getField_149159_h();

    @Accessor("field_149159_h")
    public abstract void setField_149159_h(float value);
}
