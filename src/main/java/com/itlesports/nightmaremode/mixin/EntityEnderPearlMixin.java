package com.itlesports.nightmaremode.mixin;

import net.minecraft.src.EntityEnderPearl;
import net.minecraft.src.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NM ender pearl behavior manager
 */
@Mixin(EntityEnderPearl.class)
public class EntityEnderPearlMixin {
    /**
     * Teleports the thrower to the pearl's position,
     * without taking damage(?)
     *
     * @param {MovingObjectPosition} movingObjectPosition - The position the pearl hit
     * @param {CallbackInfo} ci - Main method callback info
     *
     * @return {void}
     */
    @Inject(method = "onImpact", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityEnderPearl;setDead()V"))
    private void teleportEntity(MovingObjectPosition movingObjectPosition, CallbackInfo ci){
        EntityEnderPearl thisObj = (EntityEnderPearl) (Object)this;
        if(thisObj.getThrower() != null){
            thisObj.getThrower().setPositionAndUpdate(thisObj.posX, thisObj.posY, thisObj.posZ);
        }
    }
}
