package com.itlesports.nightmaremode.mixin;

import btw.world.util.difficulty.Difficulties;
import btw.world.util.difficulty.Difficulty;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Handles playtime world info stuff (eg.: when to start night, spawning mobs)
 */
@Mixin(WorldInfo.class)
public abstract class WorldInfoMixin {
    @Shadow private long worldTime;
    @Shadow private GameRules theGameRules;
    @Shadow private long totalTime;
    @Shadow public abstract Difficulty getDifficulty();

    @Unique private boolean botherChecking = true;
    @Unique private int gracePeriodTicks = 90 * 20; //90 seconds at 20 tick speed
    @Unique private int worldTimeOffset = 18000;

    @Inject(method = "getWorldTime()J", at = @At("HEAD"))
    private void nightSetter(CallbackInfoReturnable<Long> cir) {
        /**
         * If the world is on nightmare mode, and it's the first time we're checking
         */
        if (botherChecking && this.getDifficulty() == Difficulties.HOSTILE) {
            if (this.totalTime == 0L) {
                /**
                 * If the world just started, set to night and start the grace period
                 */
                worldTime = worldTimeOffset;
                theGameRules.addGameRule("doMobSpawning", "false");
            } else if(worldTime >= (worldTimeOffset + gracePeriodTicks) && !theGameRules.getGameRuleBooleanValue("doMobSpawning")){
                /**
                 * If the world is past the grace period, start spawning mobs
                 */
                theGameRules.addGameRule("doMobSpawning", "true");
                botherChecking = false;
            } // 1:30 grace period
        }
    }
}
