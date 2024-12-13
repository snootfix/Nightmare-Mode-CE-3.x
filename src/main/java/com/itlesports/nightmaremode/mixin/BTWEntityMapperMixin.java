package com.itlesports.nightmaremode.mixin;

import btw.entity.util.BTWEntityMapper;
import com.itlesports.nightmaremode.NightmareModeEntityMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects the NM entity mappings into the BTW entity mapper
 */
@Mixin(BTWEntityMapper.class)
public class BTWEntityMapperMixin {
    @Inject(method = "createModEntityMappings", at = @At("TAIL"), remap = false)
    private static void mapCustomEntities(CallbackInfo ci){
        NightmareModeEntityMapper.createModEntityMappings();
    }
}
