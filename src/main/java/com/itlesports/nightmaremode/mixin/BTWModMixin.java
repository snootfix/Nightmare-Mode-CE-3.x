package com.itlesports.nightmaremode.mixin;

import btw.BTWMod;
import btw.world.util.difficulty.Difficulties;
import btw.world.util.difficulty.Difficulty;
import com.itlesports.nightmaremode.item.NMItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles BTWMod specific difficulty stuff (eg.: blood pickaxe speed)
 */
@Mixin(BTWMod.class)
public class BTWModMixin {
    /**
     * Increases blood pickaxe speed on hostile/nightmare difficulty
     *
     * @param {Difficulty} difficulty - The difficulty to check
     * @param {CallbackInfo} ci - The cb for the main method
     *
     * @return {void}
     */
    @Inject(method = "initializeDifficultyCommon", at = @At("HEAD"),remap = false)
    private void increaseBloodPickaxeSpeed(Difficulty difficulty, CallbackInfo ci){
        if(difficulty == Difficulties.HOSTILE){
            float multiplier = 1.5f;
            NMItems.bloodPickaxe.addCustomEfficiencyMultiplier(multiplier);
            NMItems.bloodAxe.addCustomEfficiencyMultiplier(multiplier);
            NMItems.bloodHoe.addCustomEfficiencyMultiplier(multiplier);
            NMItems.bloodShovel.addCustomEfficiencyMultiplier(multiplier);
        }
    }
}
