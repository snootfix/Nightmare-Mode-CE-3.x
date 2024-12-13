package com.itlesports.nightmaremode.mixin;

import btw.community.nightmaremode.NightmareMode;
import com.itlesports.nightmaremode.NightmareUtils;
import com.itlesports.nightmaremode.item.NMItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Random;

/**
 * Handles playtime world stuff (eg.: night brightness, item entities),
 * and add world utils (eg.: is night?)
 */
@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public abstract long getTotalWorldTime();

    @Shadow public Random rand;

    @Shadow public WorldInfo worldInfo;

    /**
     * Protects some items/entities from burning in fire
     *
     * @param {Entity} entity - The entity to check
     * @param {CallbackInfoReturnable<Boolean>} cir - The cb for the main method
     *
     * @return {boolean} - Whether the entity is burning/burnable
     */
    @Inject(method = "isBoundingBoxBurning", at = @At("RETURN"),cancellable = true)
    private void manageBurningItemImmunity(Entity entity, CallbackInfoReturnable<Boolean> cir){
        if(entity instanceof EntityItem item
                && (
                Objects.equals(item.getEntityItem().itemID, Item.magmaCream.itemID)
                        || Objects.equals(item.getEntityItem().itemID, Item.blazeRod.itemID)
                        || Objects.equals(item.getEntityItem().itemID, NMItems.bloodOrb.itemID)
        )
        ){
            cir.setReturnValue(false);
        }
    }

    /**
     * Manages gloom nights post wither
     *
     * @param {CallbackInfoReturnable<Float>} cir - The cb for the main method
     */
    @Inject(method = "computeOverworldSunBrightnessWithMoonPhases", at = @At("RETURN"),remap = false, cancellable = true)
    private void manageGloomPostWither(CallbackInfoReturnable<Float> cir){
        World thisObj = (World)(Object)this;
        if (NightmareUtils.getWorldProgress(thisObj) == 2 && !thisObj.isDaytime()) {
            cir.setReturnValue(0f);
        }
    }

    /**
     * Manages NM specific weather events:
     * - Stops rain nthe first week
     * - Sets blood moon if it's the right day(cycle)
     *
     * @param {CallbackInfo} ci - The cb for the main method
     */
    @Inject(method = "updateWeather", at = @At("TAIL"))
    private void manageRainAndBloodMoon(CallbackInfo ci){
        World thisObj = (World)(Object)this;

        /**
         * No rain in the first week
         */
        if (this.getIsFirstWeek(thisObj)) {
            this.worldInfo.setRaining(false);
        }

        /**
         * Blood moon management
         */
        if (!MinecraftServer.getIsServer()) {
            int dawnOffset = this.isDawnOrDusk(thisObj.getWorldTime());

            if(!NightmareMode.bloodNightmare){
                /**
                 * If bloodmare is not active, each nth night is a blood moon
                 */
                if (this.getIsBloodMoon(thisObj,((int)Math.ceil((double) thisObj.getWorldTime() / 24000)) + dawnOffset)) {
                    NightmareMode.setBloodMoonTrue();
                } else {
                    NightmareMode.setBloodMoonFalse();
                }
            /**
             * If bloodmare is active, every night is a blood moon
             */
            } else{
                if(this.getIsNight(thisObj)){
                    NightmareMode.setBloodMoonTrue();
                } else{
                    NightmareMode.setBloodMoonFalse();
                }
            }
        }
    }

    /**
     * Checks if it's dawn or dusk
     *
     * @param {long} time - The time to check
     *
     * @return {int} - 1 if it's dawn or dusk, 0 otherwise
     */
    @Unique
    private int isDawnOrDusk(long time){
        if(time % 24000 >= 23459) {
            return 1;
        }

        return 0;
    }

    /**
     * Checks if it's a blood moon
     *
     * @param {World} world - The world to check
     * @param {int} dayCount - The day count
     *
     * @return {boolean} - True if it's a blood moon, false otherwise
     */
    @Unique private boolean getIsBloodMoon(World world, int dayCount){
        if(NightmareUtils.getWorldProgress(world) == 0){
            return false;
        }

        return this.getIsNight(world) && (world.getMoonPhase() == 0  && (dayCount % 16 == 9)) || NightmareMode.bloodNightmare;
    }

    /**
     * Checks if it's night
     *
     * @param {World} world - The world to check
     *
     * @return {boolean} - True if it's night, false otherwise
     */
    @Unique private boolean getIsNight(World world){
        return world.getWorldTime() % 24000 >= 12541 && world.getWorldTime() % 24000 <= 23459;
    }

    /**
     * Checks if it's the first week
     *
     * @param {World} world - The world to check
     *
     * @return {boolean} - True if it's the first week, false otherwise
     */
    @Unique private boolean getIsFirstWeek(World world){
        return world.getTotalWorldTime() < 120000;
    }
}
