package com.itlesports.nightmaremode.mixin;

import btw.BTWMod;
import btw.block.BTWBlocks;
import btw.community.nightmaremode.NightmareMode;
import btw.item.BTWItems;
import btw.util.hardcorespawn.HardcoreSpawnUtils;
import btw.world.util.WorldUtils;
import btw.world.util.difficulty.Difficulties;
import com.itlesports.nightmaremode.NightmareUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

// code is sloppy, there's probably a better way to do this.

/**
 * Handles BTW hardcode spawning
 */
@Mixin(HardcoreSpawnUtils.class)
public abstract class HardcoreSpawnUtilsMixin{

    /**
     * Sets time to night upon death, and gives bonus items in some conditions (eg.: post-wither)
     *
     * @param {World} world - The world
     * @param {MinecraftServer} server - The server
     * @param {EntityPlayerMP} player - The player
     * @param {CallbackInfoReturnable<Boolean>} cir - The cb for the main method
     *
     * @return {void}
     */
    @Inject(method = "assignNewHardcoreSpawnLocation", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityPlayerMP;setTimeOfLastSpawnAssignment(J)V"))
    private static void nightSetterUponDeath(World world, MinecraftServer server, EntityPlayerMP player, CallbackInfoReturnable<Boolean> cir) {
        long overworldTime = WorldUtils.getOverworldTimeServerOnly();
        if ((BTWMod.isSinglePlayerNonLan() || MinecraftServer.getServer().getCurrentPlayerCount() == 0) && world.getDifficulty() == Difficulties.HOSTILE) {
            /**
             * Sets time to night upon death
             */
            overworldTime += 18000L;

            /**
             * Skip gloom night
             */
            if(world.getMoonPhase() == 4 && (!WorldUtils.gameProgressHasWitherBeenSummonedServerOnly() || WorldUtils.gameProgressHasEndDimensionBeenAccessedServerOnly())){
//                ItemStack var1 = new ItemStack(BTWBlocks.finiteBurningTorch,3);
//                ItemStack var2 = new ItemStack(ItemPotion.potion,1,16422);
//                player.inventory.addItemStackToInventory(var1);
//                player.inventory.addItemStackToInventory(var2);
                overworldTime += 24000L;
            }
            for(int i = 0; i < MinecraftServer.getServer().worldServers.length; ++i) {
                WorldServer tempServer = MinecraftServer.getServer().worldServers[i];
                tempServer.setWorldTime(overworldTime);
            }

            /**
             * Post-wither bonus items
             */
            if (NightmareUtils.getWorldProgress(player.worldObj) >= 2) {
                ItemStack var1 = new ItemStack(BTWBlocks.finiteBurningTorch,3);
                ItemStack var3 = new ItemStack(ItemPotion.potion,1,16422);
                player.inventory.addItemStackToInventory(var1);
                player.inventory.addItemStackToInventory(var3);
            }

            /**
             * Post-nether bonus items
             */
            if (NightmareUtils.getWorldProgress(player.worldObj) >= 1){
                ItemStack var3 = new ItemStack(Item.compass,1);
                ItemStack var2 = new ItemStack(BTWItems.corpseEye,1);
                player.inventory.addItemStackToInventory(var2);
                player.inventory.addItemStackToInventory(var3);
            }
        }
    }

    /**
     * Disallow junglr spawns in bloodmare
     *
     * @param {ArrayList} list - The list of biomes
     * @param {Object} o - The biome
     *
     * @return {boolean} - Whether the biome is in the list
     */
    @Redirect(method = "assignNewHardcoreSpawnLocation", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;contains(Ljava/lang/Object;)Z"))
    private static boolean excludeJungle(ArrayList list, Object o){
        if((o.equals(BiomeGenBase.jungle) || o.equals(BiomeGenBase.jungleHills)) && NightmareMode.bloodNightmare){
            return true;
        }

        return list.contains(o);
    }

    /**
     * TODO: ???
     *
     * @param {EntityPlayerMP} instance - The player
     * @param {ChatMessageComponent} par1ChatMessageComponent - The chat message
     *
     * @return {void}
     */
    @Redirect(method = "handleHardcoreSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityPlayerMP;sendChatToPlayer(Lnet/minecraft/src/ChatMessageComponent;)V",ordinal = 0))
    private static void doNothing(EntityPlayerMP instance, ChatMessageComponent par1ChatMessageComponent){
        // noop
    }
}