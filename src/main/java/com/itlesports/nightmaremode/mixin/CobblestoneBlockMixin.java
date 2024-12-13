package com.itlesports.nightmaremode.mixin;

import btw.block.blocks.CobblestoneBlock;
import btw.item.BTWItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Handle cobblestone block stuff
 */
@Mixin(CobblestoneBlock.class)
public class CobblestoneBlockMixin {
    /**
     * Drops (one) stone on break
     * Nerf to day1 village strats
     *
     * @param {CallbackInfoReturnable<Integer>} cir - The cb for the main method
     *
     * @return {void}
     */
    @Inject(method = "idDropped", at = @At("RETURN"), cancellable = true)
    private void dropStoneOnBreak(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(BTWItems.stone.itemID);
    }
}
