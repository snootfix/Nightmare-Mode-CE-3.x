package com.itlesports.nightmaremode.mixin;

import btw.block.BTWBlocks;
import btw.block.blocks.BedrollBlock;
import btw.community.nightmaremode.NightmareMode;
import btw.item.BTWItems;
import btw.world.util.difficulty.Difficulties;
import com.itlesports.nightmaremode.NightmareUtils;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase implements EntityAccessor {
    @Shadow public abstract ItemStack getHeldItem();

    @Shadow protected abstract boolean isPlayer();

    @Shadow public PlayerCapabilities capabilities;

    @Shadow protected abstract boolean isInBed();

    @Shadow public FoodStats foodStats;

    public EntityPlayerMixin(World par1World) {
        super(par1World);
    }

                    // can't jump if you have slowness
    @Inject(method = "canJump", at = @At("RETURN"), cancellable = true)
    private void cantJumpIfSlowness(CallbackInfoReturnable<Boolean> cir){
        if(this.isPotionActive(Potion.moveSlowdown) && this.worldObj.getDifficulty() == Difficulties.HOSTILE){
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"))
    private void manageLifeSteal(Entity entity, CallbackInfo ci){
        if(entity instanceof EntityLiving && NightmareUtils.isHoldingBloodSword(this) && entity.hurtResistantTime == 0 && !this.isPotionActive(Potion.weakness) && !(entity instanceof EntityWither)){
            int chance = 15 - NightmareUtils.getBloodArmorWornCount(this) * 3;
            // 15, 12, 9, 6, 3
            if(NightmareUtils.getIsBloodMoon()){
                chance -= 1;
            }

            if(rand.nextInt(chance) == 0){
                this.heal(rand.nextInt(chance) == 0 ? 2 : 1);
            }

            if(rand.nextInt((int) (chance / 1.5)) == 0 && this.foodStats.getFoodLevel() < 60){
                this.foodStats.setFoodLevel(this.foodStats.getFoodLevel() + 1);
            }

            if(NightmareUtils.isWearingFullBloodArmor(this)){
                this.increaseArmorDurabilityRandomly(this);
                if((this.rand.nextInt(3) == 0 || NightmareUtils.getIsBloodMoon()) && this.fallDistance > 0.0F){
                    this.heal(1f);
                }
            }
        }
    }

    @Unique private void increaseArmorDurabilityRandomly(EntityLivingBase player){
        int j = rand.nextInt(3);
        for (int a = 0; a < 2; a++) {
            int i = rand.nextInt(5);
            player.getCurrentItemOrArmor(i).setItemDamage(Math.max(player.getCurrentItemOrArmor(i).getItemDamage() - j,0));
        }
    }

    @ModifyConstant(method = "addExhaustionForJump", constant = @Constant(floatValue = 0.2f))
    private float reduceExhaustion(float constant){
        if(NightmareMode.bloodNightmare){
            return 0.15f;
        }
        return 0.17f; // jump
    }
    @ModifyConstant(method = "addExhaustionForJump", constant = @Constant(floatValue = 1.0f))
    private float reduceExhaustion1(float constant){
        if(NightmareMode.bloodNightmare){
            return 0.5f;
        }
        return 0.75f; // sprint jump
    }
    @ModifyConstant(method = "attackTargetEntityWithCurrentItem", constant = @Constant(floatValue = 0.3f))
    private float reduceExhaustion2(float constant){
        if(NightmareMode.bloodNightmare){
            return 0.15f;
        }
        return 0.2f; // punch
    }

    @Inject(method = "onItemUseFinish", at = @At("HEAD"))
    private void manageWaterDrinking(CallbackInfo ci){
        EntityPlayer thisObj = (EntityPlayer)(Object)this;
        if(thisObj.getItemInUse() != null && (thisObj.getItemInUse().itemID == Item.potion.itemID && thisObj.getItemInUse().getItemDamage() == 0)){
            if (this.isBurning()) {
                this.extinguish();
            }
            if(this.isPotionActive(Potion.confusion.id)){
                this.removePotionEffect(Potion.confusion.id);
            }
            if(this.isPotionActive(Potion.blindness.id)){
                this.removePotionEffect(Potion.blindness.id);
            }
            if(this.isPotionActive(Potion.weakness.id)){
                this.removePotionEffect(Potion.weakness.id);
            }
            if(this.isPotionActive(Potion.moveSlowdown.id)){
                this.removePotionEffect(Potion.moveSlowdown.id);
            }
        }
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void manageBlightMovement(CallbackInfo ci){
        if (this.worldObj.getBlockId(MathHelper.floor_double(this.posX),MathHelper.floor_double(this.posY-1),MathHelper.floor_double(this.posZ)) == BTWBlocks.aestheticEarth.blockID && !this.capabilities.isCreativeMode){
            EntityPlayer thisObj = (EntityPlayer)(Object)this;

            int i = MathHelper.floor_double(this.posX);
            int j = MathHelper.floor_double(this.posY-1);
            int k = MathHelper.floor_double(this.posZ);

            if(this.worldObj.getBlockMetadata(i,j,k) == 0){
                this.addPlayerPotionEffect(thisObj,Potion.weakness.id);
            } else if (this.worldObj.getBlockMetadata(i,j,k) == 1){
                this.addPlayerPotionEffect(thisObj,Potion.poison.id);
            } else if (this.worldObj.getBlockMetadata(i,j,k) == 2){
                this.addPlayerPotionEffect(thisObj,Potion.wither.id);
                this.addPlayerPotionEffect(thisObj,Potion.moveSlowdown.id);
            } else if (this.worldObj.getBlockMetadata(i,j,k) == 4){
                this.addPlayerPotionEffect(thisObj,Potion.wither.id);
                this.addPlayerPotionEffect(thisObj,Potion.moveSlowdown.id);
                this.addPlayerPotionEffect(thisObj,Potion.blindness.id);
                this.addPlayerPotionEffect(thisObj,Potion.weakness.id);
            }
        }
    }

    @Unique private void addPlayerPotionEffect(EntityPlayer player, int potionID){
        if(!player.isPotionActive(potionID) || potionID == Potion.blindness.id){
            player.addPotionEffect(new PotionEffect(potionID,81,0));
        }
    }

    @Inject(method = "onLivingUpdate", at = @At("TAIL"))
    private void manageRunningFromPlayer(CallbackInfo ci){
        EntityPlayer thisObj = (EntityPlayer)(Object)this;
        if (thisObj.worldObj.getDifficulty() == Difficulties.HOSTILE) {
            List list = thisObj.worldObj.getEntitiesWithinAABBExcludingEntity(thisObj, thisObj.boundingBox.expand(5.0, 5.0, 5.0));
            for (Object tempEntity : list) {
                if(tempEntity instanceof EntityEnderCrystal && !this.capabilities.isCreativeMode && this.dimension != 1 && ((EntityEnderCrystal) tempEntity).ridingEntity == null){((EntityEnderCrystal) tempEntity).setDead();}
                if (!(tempEntity instanceof EntityAnimal tempAnimal)) continue;
                if (tempAnimal instanceof EntityWolf) continue;
                if(!((!thisObj.isSneaking() || checkNullAndCompareID(thisObj.getHeldItem())) && !tempAnimal.getLeashed())) continue;
                ((EntityAnimalInvoker) tempAnimal).invokeOnNearbyPlayerStartles(thisObj);
                break;
            }
        }
    }



    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void slowIfInWeb(CallbackInfo ci){
        if(this.isInWeb) {
            this.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 10, 3));
            this.addPotionEffect(new PotionEffect(Potion.weakness.id, 10, 1));
        }
    }

    // removes the check for daytime and kicking the player out of the bed if it turns day. this enables infinite sleeping
    @Redirect(method = "sleepInBedAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;isDaytime()Z"))
    private boolean doNotCareIfDay(World instance){
        if (!(Block.blocksList[this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))] instanceof BedrollBlock)) {
            return false;
        } else {
            return this.worldObj.skylightSubtracted < 4;
        }
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;isDaytime()Z"))
    private boolean doNotCareIfDay1(World instance) {
        if (!(Block.blocksList[this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))] instanceof BedrollBlock)) {
            return false;
        } else {
            return this.worldObj.skylightSubtracted < 4;
        }
    }

    @Redirect(method = "sleepInBedAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/WorldProvider;isSurfaceWorld()Z"))
    private boolean canSleepInNether(WorldProvider instance){
        return true;
    }
    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityPlayer;setTimerSpeedModifier(F)V"))
    private void manageInvisibilityWhileSleeping(CallbackInfo ci){
        if (this.isInBed()) {
            this.addPotionEffect(new PotionEffect(Potion.invisibility.id, 10,0));
        }
    }

    @ModifyConstant(method = "movementModifierWhenRidingBoat", constant = @Constant(doubleValue = 0.35))
    private double windmillSpeedBoat(double constant){
        EntityPlayer thisObj = (EntityPlayer)(Object)this;
        if(isPlayerHoldingWindmill(thisObj)){
            return 5.0;
        }
        return constant;
    }

    @Unique private boolean isPlayerHoldingWindmill(EntityPlayer player) {
        ItemStack currentItemStack = player.inventory.mainInventory[player.inventory.currentItem];
        if (currentItemStack != null) {
            return currentItemStack.itemID == BTWItems.windMill.itemID;
        }
        return false;
    }

    @Unique
    public boolean checkNullAndCompareID(ItemStack par2ItemStack){
        if(par2ItemStack != null){
            switch(par2ItemStack.itemID){
                case 2,11,19,20,23,27,30,267,276,22580:
                    return true;
            }
        }
        return false;
    }
}
