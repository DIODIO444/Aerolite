/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the slot the mouse is currently hovering, used by BetterInventory to know which slot a
 * click would hit before it is processed.
 */
@Mixin(GuiContainer.class)
public interface MixinGuiContainerAccessor {

    @Accessor("theSlot")
    Slot getHoveredSlot();
}
