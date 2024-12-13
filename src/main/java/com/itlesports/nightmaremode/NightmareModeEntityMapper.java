package com.itlesports.nightmaremode;
import net.minecraft.src.EntityList;

/**
 * Maps NM entities to the game
 */
public class NightmareModeEntityMapper {
    public NightmareModeEntityMapper(){
        // noop
    }

    public static void createModEntityMappings() {
        //TODO: the numbers mason what do they mean
        EntityList.addMapping(EntityFireCreeper.class, "NightmareFireCreeper", 2301, 5651506, 12422001);
        EntityList.addMapping(EntityShadowZombie.class, "NightmareShadowZombie", 2302, 0, 0);
//        EntityList.addMapping(NightmareFlameEntity.class, "NightmareFlame", 2303, 100000, 100000);
//        EntityList.addMapping(SocksMobsEntityGoatPossessed.class, "NightmareHellGoat", 2302, 0, 7208964);
//        EntityList.addMapping(NightmareEntity.class,"NightmareEntity",2304,0,0);
    }
}
