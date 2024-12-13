package com.itlesports.nightmaremode.mixin;

import com.itlesports.nightmaremode.NightmareUtils;
import net.minecraft.src.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Common behaviour of living entities (not only the player)
 */
@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin extends EntityLivingBase {
    /**
     * Constructor
     *
     * @param {World} par1World - The world
     *
     * @return {EntityLivingMixin}
     */
    public EntityLivingMixin(World par1World) {
        super(par1World);
    }

    @Shadow public abstract ItemStack getCurrentItemOrArmor(int par1);

    @Shadow public abstract void setCurrentItemOrArmor(int par1, ItemStack par2ItemStack);

    @Shadow public abstract ItemStack getHeldItem();

    @Shadow protected abstract boolean canDespawn();

    @Shadow private boolean persistenceRequired;

    @Shadow protected float[] equipmentDropChances;

    /**
     * Gets Leather armor ItemStacks
     *
     * @return {List<ItemStack>} - The list of leather armor ItemStacks
     */
    @Unique
    private static @NotNull List<ItemStack> getItemStacks() {
        ItemStack boots = new ItemStack(Item.bootsLeather);
        ItemStack pants = new ItemStack(Item.legsLeather);
        ItemStack chest = new ItemStack(Item.plateLeather);
        ItemStack helmet = new ItemStack(Item.helmetLeather);

        List<ItemStack> leatherArmorList = new ArrayList<>(4);
        leatherArmorList.add(boots);
        leatherArmorList.add(pants);
        leatherArmorList.add(chest);
        leatherArmorList.add(helmet);
        return leatherArmorList;
    }

    /**
     * Make it more likely for mobs to spawn with random armor(?)
     *
     * @param {CallbackInfo} ci - Main method callback info
     *
     * @return {void}
     */
    @Redirect(method = "entityLivingAddRandomArmor", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextFloat()F", ordinal = 0))
    private float returnRandomFloatButLower(Random rand){
        return (rand.nextFloat()-0.008F);
        // rand.nextFloat()   <  0.0033F ( original number: 0.0025F), from 1/400 to 1/303 chance
    }

    /**
     * Allow mobs to spawn with leather armor
     * By default this armor will not drop when the mob dies
     *
     * @param {CallbackInfo} ci - Main method callback info
     *
     * @return {void}
     */
    @Inject(method = "entityLivingAddRandomArmor", at = @At("TAIL"))
    private void chanceToSpawnWithLeatherArmor(CallbackInfo ci) {
        if (this.worldObj != null) {
            float streakModifier = 0.0f;
            for (int i = 1; i <= 4; i++) {
                if(this.getCurrentItemOrArmor(i) == null){ // starts at index 1, index 0 is held item
                    if(rand.nextFloat() < (0.04f + NightmareUtils.getWorldProgress(this.worldObj)*0.02) + streakModifier){
                        // 0.04f -> 0.06f -> 0.08f -> 0.10f
                        streakModifier += 0.05f;
                        List<ItemStack> leatherArmorList = getItemStacks();
                        this.setCurrentItemOrArmor(i, leatherArmorList.get(i-1));
                        this.equipmentDropChances[i] = 0f;
                    }
                }
            }
        }
    }

    /**
     * Make mobs despawn more during blood moons
     *
     * @param {CallbackInfo} ci - Main method callback info
     *
     * @return {void}
     */
    @Inject(method = "despawnEntity", at = @At(value = "TAIL"))
    private void manageDespawnDuringBloodMoon(CallbackInfo ci){
        if (this.canDespawn() && !this.persistenceRequired && this.ticksExisted % 300 == 299 && NightmareUtils.getIsBloodMoon()) {
            EntityPlayer nearestPlayer = this.worldObj.getClosestVulnerablePlayer(this.posX, this.posY, this.posZ, 128);

            if (nearestPlayer != null) {
                double verticalDistance = Math.abs(nearestPlayer.posY - this.posY);

                if (verticalDistance > 20) {
                    boolean isOnNonWoodMaterial = this.worldObj.getBlockMaterial(
                            (int) this.posX,
                            (int) (this.posY - 1),
                            (int) this.posZ
                    ) != Material.wood;

                    if (rand.nextInt(3) == 0 && isOnNonWoodMaterial) {
                        this.setDead();
                    }
                }
            } else {
                this.setDead();
            }
        }
    }
}
