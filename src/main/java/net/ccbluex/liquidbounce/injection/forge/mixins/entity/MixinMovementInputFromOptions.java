/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.MovementInputEvent;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires [MovementInputEvent] before the local player's movement input is calculated.
 * Modules such as Parkour and Scaffold use it to simulate the upcoming movement.
 */
@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions {

    @Inject(method = "updatePlayerMoveState", at = @At("HEAD"))
    private void onUpdatePlayerMoveState(CallbackInfo callbackInfo) {
        LiquidBounce.eventManager.callEvent(new MovementInputEvent((MovementInput) (Object) this));
    }
}
