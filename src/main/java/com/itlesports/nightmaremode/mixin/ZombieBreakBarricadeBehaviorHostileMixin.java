package com.itlesports.nightmaremode.mixin;

import btw.entity.mob.behavior.ZombieBreakBarricadeBehavior;
import btw.entity.mob.behavior.ZombieBreakBarricadeBehaviorHostile;
import btw.item.BTWItems;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ZombieBreakBarricadeBehaviorHostile.class)
public class ZombieBreakBarricadeBehaviorHostileMixin extends ZombieBreakBarricadeBehavior{
    public ZombieBreakBarricadeBehaviorHostileMixin(EntityLiving par1EntityLiving) {
        super(par1EntityLiving);
    }

    @Inject(method = "updateTask", at = @At("HEAD"))
    private void tooLZombieBreaksFaster(CallbackInfo ci){
        if (this.associatedEntity.getHeldItem() != null) {
            ItemStack heldItem = this.associatedEntity.getHeldItem();

            if(getFastItems().contains(heldItem.itemID)){
                this.breakingTime += 1;
                if(this.targetBlock.blockMaterial == Material.ground && heldItem.getDisplayName().contains("Shovel")){
                    this.breakingTime += 3;
                }
            }
            if(this.breakingTime > 240){
                this.breakingTime = 240;
            }
        }
    }

    @Inject(method = "continueExecuting", at = @At("HEAD"),cancellable = true)
    private void manageWhenToStop(CallbackInfoReturnable<Boolean> cir){
        if(this.associatedEntity.isAirBorne){
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static @NotNull List<Integer> getFastItems() {
        List<Integer> fastItemList = new ArrayList<>(14);
        fastItemList.add(Item.swordStone.itemID);
        fastItemList.add(Item.swordIron.itemID);
        fastItemList.add(Item.swordGold.itemID);
        fastItemList.add(Item.axeStone.itemID);
        fastItemList.add(Item.axeDiamond.itemID);
        fastItemList.add(Item.axeIron.itemID);
        fastItemList.add(Item.shovelIron.itemID);
        fastItemList.add(Item.shovelStone.itemID);
        fastItemList.add(Item.shovelGold.itemID);
        fastItemList.add(Item.shovelDiamond.itemID);

        fastItemList.add(BTWItems.boneClub.itemID);
        fastItemList.add(Item.swordWood.itemID);
        fastItemList.add(Item.swordDiamond.itemID);
        fastItemList.add(Item.axeGold.itemID);
        return fastItemList;
    }
}