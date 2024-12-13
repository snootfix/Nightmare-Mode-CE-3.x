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

/**
 * Handles player specific stuff
 * This is to set rules about the player entity (eg.: can't jump if you have slowness, hunger management, etc.)
 */
@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase implements EntityAccessor {
    @Shadow public abstract ItemStack getHeldItem();

    @Shadow protected abstract boolean isPlayer();

    @Shadow public PlayerCapabilities capabilities;

    @Shadow protected abstract boolean isInBed();

    @Shadow public FoodStats foodStats;

    @Shadow public InventoryPlayer inventory;

    /**
     * Constructor
     *
     * @param {World} par1World - The world
     *
     * @return {EntityPlayerMixin} - The player entity
     */
    public EntityPlayerMixin(World par1World) {
        super(par1World);
    }

    /**
     * Cannot jump if the player has slowness
     *
     * @param {CallbackInfoReturnable<Boolean>} cir - The cb for the main method
     *
     * @return {void}
     */
    @Inject(method = "canJump", at = @At("RETURN"), cancellable = true)
    private void cantJumpIfSlowness(CallbackInfoReturnable<Boolean> cir){
        if(this.isPotionActive(Potion.moveSlowdown) && this.worldObj.getDifficulty() == Difficulties.HOSTILE){
            cir.setReturnValue(false);
        }
    }

    /**
     * Manages life steal and food gain from the blood sword
     *
     * @param {Entity} entity - The entity to attack
     * @param {CallbackInfo} ci - The cb for the main method
     *
     * @return {void}
     */
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

    /**
     * Increases the durability of the armor randomly
     *
     * @param {EntityLivingBase} player - The player entity
     *
     * @return {void}
     */
    @Unique private void increaseArmorDurabilityRandomly(EntityLivingBase player){
        int j = rand.nextInt(3);
        for (int a = 0; a < 2; a++) {
            int i = rand.nextInt(5);
            player.getCurrentItemOrArmor(i).setItemDamage(Math.max(player.getCurrentItemOrArmor(i).getItemDamage() - j,0));
        }
    }

    /**
     * Adds a potion effect to the player
     *
     * @param {EntityPlayer} player - The player entity
     * @param {int} potionID - The potion effect ID
     *
     * @return {void}
     */
    @Unique private void addPlayerPotionEffect(EntityPlayer player, int potionID){
        if(!player.isPotionActive(potionID) || potionID == Potion.blindness.id){
            player.addPotionEffect(new PotionEffect(potionID,81,0));
        }
    }

    /**
     * Reduces jump hunger cost
     *
     * @param {float} constant - The default multiplier(?)
     *
     * @return {float} - The new multiplier
     */
    @ModifyConstant(method = "addExhaustionForJump", constant = @Constant(floatValue = 0.2f))
    private float reduceExhaustion(float constant){
        /**
         * If bloodmare is active, reduce the exhaustion
         */
        if(NightmareMode.bloodNightmare){
            return 0.15f;
        }

        return 0.17f; // jump
    }

    /**
     * Reduces sprint jump hunger cost
     *
     * @param {float} constant - The default multiplier(?)
     *
     * @return {float} - The new multiplier
     */
    @ModifyConstant(method = "addExhaustionForJump", constant = @Constant(floatValue = 1.0f))
    private float reduceExhaustion1(float constant){
        /**
         * If bloodmare is active, reduce the exhaustion
         */
        if(NightmareMode.bloodNightmare){
            return 0.5f;
        }

        return 0.75f; // sprint jump
    }

    /**
     * Reduces punch hunger cost
     *
     * @param {float} constant - The default multiplier(?)
     *
     * @return {float} - The new multiplier
     */
    @ModifyConstant(method = "attackTargetEntityWithCurrentItem", constant = @Constant(floatValue = 0.3f))
    private float reduceExhaustion2(float constant){
        /**
         * If bloodmare is active, reduce the exhaustion
         */
        if(NightmareMode.bloodNightmare){
            return 0.15f;
        }
        return 0.2f; // punch
    }

    /**
     * Drinking water removes negative effects:
     *  - Burning
     *  - Confusion
     *  - Blindness
     *  - Weakness
     *  - Slowness
     *
     * @param {CallbackInfo} ci - The cb for the main method
     *
     * @return {void}
     */
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

    /**
     * Handles movement in blight
     *
     * @param {CallbackInfo} ci - The cb for the main method
     *
     * @return {void}
     */
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

    /**
     * Animals run away from the player
     *
     * @param {CallbackInfo} ci - The cb for the main method
     *
     * @return {void}
     */
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

    /**
     * Slows the player if they are in a web, adds debuffs
     *
     * @param {CallbackInfo} ci - The cb for the main method
     *
     * @return {void}
     */
    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void slowIfInWeb(CallbackInfo ci){
        if(this.isInWeb) {
            this.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 10, 3));
            this.addPotionEffect(new PotionEffect(Potion.weakness.id, 10, 1));
        }
    }

    /**
     * Allows daytime sleeping in beds
     *
     * @param {World} instance - The world
     *
     * @return {boolean} - Whether to allow sleeping
     */
    @Redirect(method = "sleepInBedAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;isDaytime()Z"))
    private boolean doNotCareIfDay(World instance){
        if (!(Block.blocksList[this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))] instanceof BedrollBlock)) {
            return false;
        } else {
            return this.worldObj.skylightSubtracted < 4;
        }
    }

    /**
     * Allows sleeping on day one(?)
     *
     * @param {World} instance - The world
     *
     * @return {boolean} - Whether to allow sleeping
     */
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;isDaytime()Z"))
    private boolean doNotCareIfDay1(World instance) {
        if (!(Block.blocksList[this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))] instanceof BedrollBlock)) {
            return false;
        } else {
            return this.worldObj.skylightSubtracted < 4;
        }
    }

    /**
     * Allows sleeping in the nether
     *
     * @param {WorldProvider} instance - The world provider
     *
     * @return {boolean} - Whether to allow sleeping
     */
    @Redirect(method = "sleepInBedAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/WorldProvider;isSurfaceWorld()Z"))
    private boolean canSleepInNether(WorldProvider instance){
        return true;
    }

    /**
     * Makes the player invisible while sleeping
     *
     * @param {CallbackInfo} ci - The cb for the main method
     *
     * @return {void}
     */
    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityPlayer;setTimerSpeedModifier(F)V"))
    private void manageInvisibilityWhileSleeping(CallbackInfo ci){
        if (this.isInBed()) {
            this.addPotionEffect(new PotionEffect(Potion.invisibility.id, 10,0));
        }
    }

    /**
     * Speeds up the player in a boat if they are holding a windmill
     *
     * @param {double} constant - The default speed
     *
     * @return {double} - The new speed
     */
    @ModifyConstant(method = "movementModifierWhenRidingBoat", constant = @Constant(doubleValue = 0.35))
    private double windmillSpeedBoat(double constant){
        EntityPlayer thisObj = (EntityPlayer)(Object)this;
        if(isPlayerHoldingWindmill(thisObj)){
            return 5.0;
        }

        return constant;
    }

    /**
     * Checks if the player is holding a windmill
     *
     * @param {EntityPlayer} player - The player entity
     *
     * @return {boolean} - Whether the player is holding a windmill
     */
    @Unique private boolean isPlayerHoldingWindmill(EntityPlayer player) {
        ItemStack currentItemStack = player.inventory.mainInventory[player.inventory.currentItem];
        if (currentItemStack != null) {
            return currentItemStack.itemID == BTWItems.windMill.itemID;
        }
        return false;
    }

    /**
     * Checks if the has a given id (and is not null)
     * 2,11,19,20,23,27,30,267,276,22580
     *
     * @param {ItemStack} par2ItemStack - The item stack
     *
     * @return {boolean} - Whether the item stack is not null and has the given id
     */
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
