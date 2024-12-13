package com.itlesports.nightmaremode.mixin;

import btw.world.util.difficulty.Difficulties;
import com.itlesports.nightmaremode.NightmareUtils;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Common behaviour of living entities (not only the player)
 * (actually all entities)
 */
@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity implements EntityAccessor {
    @Shadow public abstract boolean isEntityAlive();

    @Shadow public abstract void addPotionEffect(PotionEffect par1PotionEffect);

    /**
     * Constructor
     *
     * @param {World} par1World - The world
     *
     * @return {EntityLivingBaseMixin}
     */
    public EntityLivingBaseMixin(World par1World) {
        super(par1World);
    }

    /**
     * Make wither skeletons 2 blocks tall(?)
     *
     * @param {float} constant - The constant to modify
     *
     * @return {float} - The modified constant
     */
    @ModifyConstant(method = "getEyeHeight", constant = @Constant(floatValue = 0.85f))
    private float modifyWitherSkeletonSight(float constant){
        EntityLivingBase thisObj = (EntityLivingBase)(Object)this;
        if(thisObj.worldObj.getDifficulty() != Difficulties.HOSTILE){
            return constant;
        }

        if(thisObj instanceof EntitySkeleton skeleton && skeleton.getSkeletonType()==1){
            return 0.6f;
        } else{
            return 0.85f;
        }
    }

    /**
     * Allow players to see in the dark during blood moons(?)
     *
     * @param {Potion} par1Potion - The potion to check
     * @param {CallbackInfoReturnable<Boolean>} cir - The cb for the main method
     *
     * @return {void}
     */
    @Inject(method = "isPotionActive(Lnet/minecraft/src/Potion;)Z", at = @At("HEAD"), cancellable = true)
    private void playerNightVisionBypassDuringBloodMoon(Potion par1Potion, CallbackInfoReturnable<Boolean> cir){
        EntityLivingBase thisObj = (EntityLivingBase)(Object)this;

        if(thisObj instanceof EntityPlayer && par1Potion.id == Potion.nightVision.id && NightmareUtils.getIsBloodMoon()){
            cir.setReturnValue(true);
        }
    }

    /**
     * Give blood armor wearers a chance to get regen and damage boost on killing something
     *
     * @param {DamageSource} source - The source of the damage
     * @param {CallbackInfo} ci - The cb for the main method
     *
     * @return {void}
     */
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void manageBloodMoonDeaths(DamageSource source, CallbackInfo ci){
        if(source.getEntity() instanceof EntityPlayer player){
            boolean bIsBloodMoon = NightmareUtils.getIsBloodMoon();

            if(NightmareUtils.isWearingFullBloodArmor(player)){
                if(this.rand.nextInt(3) == 0 || bIsBloodMoon){
                    if(player.isPotionActive(Potion.damageBoost) && (player.getActivePotionEffect(Potion.damageBoost)).getAmplifier() == 0){

                        if(rand.nextInt(5) == 0){this.addPotion(player,Potion.damageBoost.id, 1);}

                        this.addPotion(player,Potion.regeneration.id, 0);
                    } else {
                        this.addPotion(player, Potion.damageBoost.id, 0);
                    }
                }
                player.heal(2f);
            }
        }
    }

    /**
     * Add a potion effect to an entity
     *
     * @param {EntityLivingBase} entity - The entity to add the potion effect to
     * @param {int} potionID - The ID of the potion effect
     * @param {int} amp - The amplifier of the potion effect
     *
     * @return {void}
     */
    @Unique private void addPotion(EntityLivingBase entity, int potionID, int amp){
        entity.addPotionEffect(new PotionEffect(potionID, 60, amp));
    }
}