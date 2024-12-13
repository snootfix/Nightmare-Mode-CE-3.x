package com.itlesports.nightmaremode.mixin;

import btw.client.render.BTWRenderMapper;
import com.itlesports.nightmaremode.*;
import net.minecraft.src.RenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects the NM entity render mappings into the BTW render mapper
 */
@Mixin(BTWRenderMapper.class)
public class BTWRenderMapperMixin {
    @Inject(method = "initEntityRenderers", at = @At("TAIL"),remap = false)
    private static void doNightmareEntityRenderMapping(CallbackInfo ci){
        RenderManager.addEntityRenderer(EntityFireCreeper.class, new RenderFireCreeper());
        RenderManager.addEntityRenderer(EntityShadowZombie.class, new RenderShadowZombie());
//        RenderManager.addEntityRenderer(SocksMobsEntityGoatPossessed.class, new SocksMobsRenderGoatPossessed(new SocksMobsModelGoatPossessed(),1));
//        RenderManager.addEntityRenderer(NightmareEntity.class, new RenderShadowZombie());
    }
}
