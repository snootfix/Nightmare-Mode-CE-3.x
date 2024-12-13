package com.itlesports.nightmaremode;

import btw.world.util.WorldUtils;
import com.itlesports.nightmaremode.item.NMItems;
import net.minecraft.src.EntityLivingBase;
import net.minecraft.src.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Utils for NM specific mechanics (eg.: world progress, bloodmoons)
 */
public class NightmareUtils {
    private static final List<Integer> bloodArmor = new ArrayList<>(
            Arrays.asList(
                    NMItems.bloodSword.itemID,
                    NMItems.bloodBoots.itemID,
                    NMItems.bloodLeggings.itemID,
                    NMItems.bloodChestplate.itemID,
                    NMItems.bloodHelmet.itemID
            )
    );

    /**
     * Gets the world progress/difficulty
     *
     * @param {World} world - The world to check
     *
     * @return {0 | 1 | 2 | 3} - The world progress
     */
    public static int getWorldProgress(World world) {
        /**
         * 0: No progress (HC spawn range is the default)
         */
        if (!world.worldInfo.getDifficulty().shouldHCSRangeIncrease()) {
            return 0;
        }

        /**
         * 3: End portal lit
         */
        if (WorldUtils.gameProgressHasEndDimensionBeenAccessedServerOnly()) {
            return 3;
        }

        /**
         * 2: Wither summoned
         */
        if (WorldUtils.gameProgressHasWitherBeenSummonedServerOnly()) {
            return 2;
        }

        /**
         * 1: Nether accessed
         */
        if (WorldUtils.gameProgressHasNetherBeenAccessedServerOnly()) {
            return 1;
        }

        /**
         * Fallback: No progress
         */
        return 0;
    }

    /**
     * Gets whether it's a bloodmoon (from the addon instance)
     *
     * @return {boolean} - Whether it's a bloodmoon
     */
    public static boolean getIsBloodMoon(){
        if (NightmareMode.getInstance() == null) {
            return false;
        }

        return Objects.requireNonNullElse(btw.community.nightmaremode.NightmareMode.getInstance().isBloodMoon, false);
    }

    /**
     * Checks if an entity is wearing full blood armor,
     * INCLUDING the sword
     *
     * @param {EntityLivingBase} entity - The entity to check
     *
     * @return {boolean} - Whether the entity is wearing full blood armor
     */
    public static boolean isWearingFullBloodArmor(EntityLivingBase entity){
        boolean hasFullArmor = NightmareUtils.getBloodArmorWornCount(entity) >= 4;
        boolean isHoldingSword = NightmareUtils.isHoldingBloodSword(entity);
        return hasFullArmor && isHoldingSword;
    }

    /**
     * Checks if an entity is wearing any blood armor,
     * EXCLUDING the sword
     *
     * @param {EntityLivingBase} entity - The entity to check
     *
     * @return {boolean} - Whether the entity is wearing any blood armor
     */
    public static boolean isWearingAnyBloodArmor(EntityLivingBase entity){
        return NightmareUtils.getBloodArmorWornCount(entity) > 0;
    }

    /**
     * Gets the amount of blood armor worn by an entity
     * Up to 4 pieces of blood armor can be worn (sword does not count)
     *
     * @param {EntityLivingBase} entity - The entity to check
     *
     * @return {int} - The amount of blood armor worn
     */
    public static int getBloodArmorWornCount(EntityLivingBase entity){
        int value = 0;

        for(int i = 1; i < 5; i++){
            if(entity.getCurrentItemOrArmor(i) == null) continue;
            if(entity.getCurrentItemOrArmor(i).itemID == bloodArmor.get(i)){value += 1;}
        }

        return value;
    }

    /**
     * Checks if an entity is holding the blood sword
     *
     * @param {EntityLivingBase} entity - The entity to check
     *
     * @return {boolean} - Whether the entity is holding the blood sword
     */
    public static boolean isHoldingBloodSword(EntityLivingBase entity){
        if(entity.getCurrentItemOrArmor(0) == null){return false;}
        return entity.getCurrentItemOrArmor(0).itemID == bloodArmor.get(0);
    }
}
