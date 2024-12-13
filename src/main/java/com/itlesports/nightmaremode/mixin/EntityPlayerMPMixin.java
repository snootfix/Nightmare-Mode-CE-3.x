package com.itlesports.nightmaremode.mixin;

import btw.block.BTWBlocks;
import btw.community.nightmaremode.NightmareMode;
import btw.entity.LightningBoltEntity;
import btw.item.BTWItems;
import btw.world.util.WorldUtils;
import btw.world.util.difficulty.Difficulties;
import com.itlesports.nightmaremode.NightmareUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side player mixin
 * This is for acting on the player entity on the server side (eg.: death messages, gloom level)
 */
@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer {
    @Unique boolean runAgain = true;
    @Unique long targetTime = 2147483647;
    @Unique boolean isTryingToEscapeBloodMoon = true;

    @Shadow public MinecraftServer mcServer;

    @Shadow public abstract void sendChatToPlayer(ChatMessageComponent par1ChatMessageComponent);

    @Unique int steelModifier;

    @Unique private boolean shouldSpawnWithSpecials = true;

    /**
     * Constructor
     *
     * @param {World} par1World - The world
     * @param {String} par2Str - The player name(?)
     *
     * @return {EntityPlayerMP} - The player entity
     */
    public EntityPlayerMPMixin(World par1World, String par2Str) {
        super(par1World, par2Str);
    }

    /**
     * Increments the gloom counter 6x faster if the player is in gloom
     *
     * @param {CallbackInfo} info - The main method cb
     *
     * @return {void}
     */
    @Inject(method="updateGloomState", at = @At("HEAD"))
    public void incrementInGloomCounter(CallbackInfo info) {
        if (this.getGloomLevel() > 0) {
            this.inGloomCounter += 5; // gloom goes up 6x faster
        }
    }

    /**
     * Manages gloom immunity during blood moon
     *
     * @param {EntityPlayerMP} player - The player
     * @param {Potion} potion - The potion to check
     *
     * @return {boolean} - Whether the player is immune to gloom
     */
    @Redirect(method = "isInGloom", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityPlayerMP;isPotionActive(Lnet/minecraft/src/Potion;)Z"))
    private boolean manageGloomDuringBloodMoon(EntityPlayerMP player, Potion potion){
        /**
         * No gloom during blood moon
         */
        if(NightmareUtils.getIsBloodMoon()){
            return false;
        }

        /**
         * Default behavior
         */
        return player.isPotionActive(potion);
    }

    /**
     * Manages gloom immunity when wearing ender spectacles
     *
     * @param {CallbackInfoReturnable<Boolean>} cir - The main method cb
     *
     * @return {void}
     */
    @Inject(method = "isInGloom", at = @At("HEAD"),cancellable = true)
    private void noGloomIfWearingEnderSpectacles(CallbackInfoReturnable<Boolean> cir){
        if(this.getCurrentItemOrArmor(4) != null && this.getCurrentItemOrArmor(4).itemID == BTWItems.enderSpectacles.itemID){
            cir.setReturnValue(false);
        }
    }

    /**
     * Manage trying to go to the neither:
     *  - Start the 3 day opening period on first travel
     *  - Prevent travel during blood moon
     *
     * @param {int} par1 - The dimension to travel to
     * @param {CallbackInfo} ci - The main method cb
     *
     * @return {void}
     */
    @Inject(method = "travelToDimension",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/src/EntityPlayerMP;triggerAchievement(Lnet/minecraft/src/StatBase;)V",
                    ordinal = 2),cancellable = true)
    private void initialiseNetherThreeDayPeriod(int par1, CallbackInfo ci){
        /**
         * Init the nether opening 3 day period
         */
        if (this.runAgain) {
            this.targetTime = this.worldObj.getWorldTime() + 72000;
            this.runAgain = false;
        }

        /**
         * If the nether is open and we're in the overworld,
         * prevent travel during blood moon
         */
        if (WorldUtils.gameProgressHasNetherBeenAccessedServerOnly() && this.dimension == 0) {
            int dayCount = ((int)Math.ceil((double) this.worldObj.getWorldTime() / 24000)) + (this.worldObj.getWorldTime() % 24000 >= 23459 ? 1 : 0);
            if(NightmareUtils.getIsBloodMoon() || (dayCount % 16 >= 8 && dayCount % 16 <= 9)){
                if(this.isTryingToEscapeBloodMoon){
                    ChatMessageComponent text1 = new ChatMessageComponent();
                    text1.addText("<???> Running to another realm? Pathetic. This is where your nightmare stays.");
                    text1.setColor(EnumChatFormatting.DARK_RED);
                    this.sendChatToPlayer(text1);
                    this.isTryingToEscapeBloodMoon = false;
                }
                ci.cancel();
            }
        }
    }

    /**
     * Does stuff that's convenient in the main game loop (eg.: spawning with specials, starting hardmode)
     *
     * @param {CallbackInfo} ci - The main method cb
     *
     * @return {void}
     */
    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void manageNetherThreeDayPeriod(CallbackInfo ci){
        if (this.shouldSpawnWithSpecials) {
            if (NightmareMode.furnaceStart) {
                this.inventory.addItemStackToInventory(new ItemStack(BTWBlocks.idleOven, 1));
                this.foodStats.setFoodLevel(30);
            }

            this.shouldSpawnWithSpecials = false;
        }

        /**
         * If the 3 day period is over and the nether hasn't been accessed yet,
         * start hardmode
         */
        if(this.worldObj.getWorldTime() > this.targetTime && !this.runAgain && !WorldUtils.gameProgressHasNetherBeenAccessedServerOnly()){
            ChatMessageComponent text2 = new ChatMessageComponent();
            text2.addText("<???> Hardmode has begun.");
            text2.setColor(EnumChatFormatting.DARK_RED);
            this.sendChatToPlayer(text2);
            this.playSound("mob.wither.death",1.0f,0.905f);
            WorldUtils.gameProgressSetNetherBeenAccessedServerOnly();
        }

        /**
         * Assume the player is trying to escape the blood moon at night
         * TODO: not sure why this is here
         */
        if(this.worldObj.getWorldTime() % 24000 == 0){
            this.isTryingToEscapeBloodMoon = true;
        }
    }

    /**
     * Handles dialogue when the player goes to the end
     *
     * @param {int} par1 - TODO: ???
     *
     * @return {void}
     */
    @Inject(method = "travelToDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityPlayerMP;triggerAchievement(Lnet/minecraft/src/StatBase;)V",ordinal = 1))
    private void manageEndDialogue(int par1, CallbackInfo ci){
        ChatMessageComponent text2 = new ChatMessageComponent();
        text2.addText("<The Twins> Your journey ends here.");
        text2.setColor(EnumChatFormatting.LIGHT_PURPLE);
        this.mcServer.getConfigurationManager().sendChatMsg(text2);
        // need to figure out how to make this not happen every time the player goes to the end
    }

    /**
     * Smite the player on death
     *
     * @param {DamageSource} par1DamageSource - The damage source
     * @param {CallbackInfo} ci - The main method cb
     *
     * @return {void}
     */
    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityPlayerMP;addStat(Lnet/minecraft/src/StatBase;I)V", shift = At.Shift.AFTER))
    private void smitePlayer(DamageSource par1DamageSource, CallbackInfo ci){
        if (this.worldObj.getDifficulty() == Difficulties.HOSTILE && !MinecraftServer.getIsServer()) {
            Entity lightningbolt = new LightningBoltEntity(this.getEntityWorld(), this.posX, this.posY-0.5, this.posZ);
            getEntityWorld().addWeatherEffect(lightningbolt);

            // SUMMONS EXPLOSION. explosion does tile and entity damage. effectively kills all dropped items.
            double par2 = this.posX;
            double par4 = this.posY;
            double par6 = this.posZ;
            float par8 = 3.0f;
            this.worldObj.createExplosion(null, par2, par4, par6, par8, true);
        }
    }

    /**
     * Taunt the player on death
     *
     * @param {DamageSource} par1DamageSource - The damage source
     * @param {CallbackInfo} ci - The main method cb
     *
     * @return {void}
     */
    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/ServerConfigurationManager;sendChatMsg(Lnet/minecraft/src/ChatMessageComponent;)V",shift = At.Shift.AFTER))
    private void manageTauntingChatMessage(DamageSource par1DamageSource, CallbackInfo ci){
        if (NightmareUtils.getWorldProgress(this.worldObj) != 3) {
            ChatMessageComponent text2 = new ChatMessageComponent();
            text2.addText(getDeathMessages().get(this.rand.nextInt(getDeathMessages().size())));
            text2.setColor(getDeathColors().get(this.rand.nextInt(getDeathColors().size())));
            this.mcServer.getConfigurationManager().sendChatMsg(text2);
        }
    }

    /**
     * Change the damage source of lightning to magic
     * Nerf dragon fight fire res strat
     *
     * @param {EntityPlayerMP} instance - The player instance
     * @param {int} i - The damage to deal
     *
     * @return {void}
     */
    @Redirect(method = "onStruckByLightning", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityPlayerMP;dealFireDamage(I)V"))
    private void dealMagicDamage(EntityPlayerMP instance, int i){
        this.attackEntityFrom(DamageSource.magic, 5f+this.rand.nextInt(3));
    }

    /**
     * Buff lightning strike debuffs
     *
     * @param {LightningBoltEntity} boltEntity - The lightning bolt entity
     * @param {CallbackInfo} ci - The main method cb
     *
     * @return {void}
     */
    @Inject(method = "onStruckByLightning",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityPlayerMP;addPotionEffect(Lnet/minecraft/src/PotionEffect;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void givePlayerSlowness(LightningBoltEntity boltEntity, CallbackInfo ci){
        EntityPlayerMP thisObj = (EntityPlayerMP)(Object)this;
        steelModifier = 0;
        if(isPlayerWearingItem(thisObj, BTWItems.plateBoots,1)){
            steelModifier += 1;
        }
        if(isPlayerWearingItem(thisObj, BTWItems.plateLeggings,2)){
            steelModifier += 3;
        }
        if(isPlayerWearingItem(thisObj, BTWItems.plateBreastplate,3)) {
            steelModifier += 5;
        }
        if(isPlayerWearingItem(thisObj, BTWItems.plateHelmet,4) || isPlayerWearingItem(thisObj, BTWItems.enderSpectacles,4)) {
            steelModifier += 1;
        }

        this.addPotionEffect(new PotionEffect(Potion.moveSlowdown.getId(),120 - steelModifier * 10,10 - steelModifier,true));
        this.addPotionEffect(new PotionEffect(Potion.digSlowdown.getId(),800 - steelModifier * 79,3,true));
        this.addPotionEffect(new PotionEffect(Potion.confusion.getId(),300 - steelModifier * 28,0,true));
        this.addPotionEffect(new PotionEffect(Potion.blindness.getId(),300 - steelModifier * 28,0,true));
        this.addPotionEffect(new PotionEffect(Potion.weakness.getId(),800 - steelModifier * 75,1,true));
    }

    /**
     * Check if the player is wearing a specific item
     *
     * @param {EntityPlayerMP} player - The player
     * @param {Item} itemToCheck - The item to check
     * @param {int} armorIndex - The armor slot (boots 1, legs 2, chest 3, helmet 4, held item 0)
     *
     * @return {boolean} - Whether the player is wearing the item
     */
    @Unique private boolean isPlayerWearingItem(EntityPlayerMP player, Item itemToCheck, int armorIndex){
        return player.getCurrentItemOrArmor(armorIndex) != null && player.getCurrentItemOrArmor(armorIndex).itemID == itemToCheck.itemID;
    }

    /**
     * Get death messages list
     *
     * @return {List<String>} - The death messages
     */
    @Unique
    private static @NotNull List<String> getDeathMessages() {
        List<String> messageList = new ArrayList<>();
        messageList.add("<???> Pathetic.");
        messageList.add("<???> Really?");
        messageList.add("<???> Have you tried not dying?");
        messageList.add("<???> Skill issue.");
        messageList.add("<???> Dead again?");
        messageList.add("<???> Nice one.");
        messageList.add("<???> Easy.");
        messageList.add("<???> Not even close.");
        messageList.add("<???> Don't bother trying.");
        messageList.add("<???> You weren't built to last.");
        messageList.add("<???> Did you think you were special? You're not even memorable.");
        messageList.add("<???> Such potential... wasted on someone so utterly incompetent.");
        messageList.add("<???> Your light fades, but I remain eternal.");
        return messageList;
    }

    /**
     * Get death colors list
     *
     * @return {List<EnumChatFormatting>} - The death colors
     */
    @Unique
    private static @NotNull List<EnumChatFormatting> getDeathColors() {
        List<EnumChatFormatting> colorList = new ArrayList<>();
        colorList.add(EnumChatFormatting.RED);
        colorList.add(EnumChatFormatting.BLUE);
        return colorList;
    }
}
