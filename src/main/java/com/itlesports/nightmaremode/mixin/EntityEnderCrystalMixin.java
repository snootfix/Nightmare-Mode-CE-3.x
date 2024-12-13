package com.itlesports.nightmaremode.mixin;

import btw.entity.LightningBoltEntity;
import btw.world.util.difficulty.Difficulties;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Handles the ender crystal entity
 */
@Mixin(EntityEnderCrystal.class)
public abstract class EntityEnderCrystalMixin extends Entity{
    /**
     * Constructor
     *
     * @param {World} par1World - The world to create the ender crystal in
     *
     * @return {EntityEnderCrystalMixin} - The instance
     */
    public EntityEnderCrystalMixin(World par1World) {
        super(par1World);
    }

    /**
     * Strikes the player with lightning when the ender crystal is destroyed
     *
     * @param {DamageSource} par1DamageSource - The source of the damage
     * @param {float} par2 - TODO: ???
     * @param {CallbackInfoReturnable<Boolean>} cir - The cb for the main method
     *
     * @return {void}
     */
    @Inject(
            method = "attackEntityFrom",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityEnderCrystal;setDead()V")
    )
    private void avengeDestroyer(DamageSource par1DamageSource, float par2, CallbackInfoReturnable<Boolean> cir) {
        Entity destroyer = par1DamageSource.getSourceOfDamage();

        /**
         * If the destroyer is an arrow or a throwable, get the shooter/thrower
         */
        if (destroyer instanceof EntityArrow) {
            destroyer = ((EntityArrow) destroyer).shootingEntity;
        } else if (destroyer instanceof EntityThrowable) {
            destroyer = ((EntityThrowable) destroyer).getThrower();
        }

        /**
         * If in NM end, strike the destroyer with lightning
         */
        if (destroyer instanceof EntityPlayer && this.dimension != 0 && this.worldObj.getDifficulty() == Difficulties.HOSTILE) {
            Entity lightningbolt = new LightningBoltEntity(this.worldObj, destroyer.posX, destroyer.posY-0.5, destroyer.posZ);
            this.worldObj.addWeatherEffect(lightningbolt);
        }
    }

    /**
     * Spawns fire only in the end
     *
     * @param {World} instance - The world to spawn the fire in
     * @param {int} par1 - TODO: ???
     * @param {int} par2 - TODO: ???
     * @param {int} par3 - TODO: ???
     * @param {int} par4 - TODO: ???
     *
     * @return {false} - Stops further execution
     */
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;setBlock(IIII)Z"))
    private boolean spawnFireOnlyInEnd(World instance, int par1, int par2, int par3, int par4){
        if(this.dimension != 0){
            int var1 = MathHelper.floor_double(this.posX);
            int var2 = MathHelper.floor_double(this.posY);
            int var3 = MathHelper.floor_double(this.posZ);
            this.worldObj.setBlock(var1, var2, var3, Block.fire.blockID);
        }
        return false;
    }

    /**
     * Sets the ender crystal size
     *
     * @param {World} par1World - The world to create the ender crystal in
     * @param {CallbackInfo} ci - The cb for the main method
     *
     * @return {void}
     */
    @Inject(method = "<init>(Lnet/minecraft/src/World;)V", at = @At("TAIL"))
    private void updateEndCrystalSize(World par1World, CallbackInfo ci) {
        this.setSize(1.8f,3.2f);
    }

    /**
     * Despawns the ender crystal if the end is unloaded
     *
     * @param {CallbackInfo} ci - The cb for the main method
     *
     * @return {void}
     */
    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void manageDespawnIfDeloaded(CallbackInfo ci){
        if (this.dimension != 1 && this.ridingEntity == null && this.ticksExisted >= 8000){
            this.setDead();
        }
    }
}