package com.itlesports.nightmaremode.mixin;

import btw.community.nightmaremode.NightmareMode;
import btw.world.util.difficulty.Difficulty;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiCreateWorld;
import net.minecraft.src.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiCreateWorld.class)
public abstract class GuiCreateWorldMixin extends GuiScreen {
    @Shadow private boolean lockDifficulty;
    @Shadow private int difficultyID;
    @Unique boolean onlyOnce = true;

    @Unique private static final int DIFFICULTY_NM_BAD_DREAM = 0;
    @Unique private static final int DIFFICULTY_BTW_STANDARD = 1;
    @Unique private static final int DIFFICULTY_NM_NIGHTMARE = 2;
    @Unique private static final int DIFFICULTY_BTW_HOSTILE = 3;

    @Inject(method = "updateButtonText", at = @At("HEAD"))
    private void manageDifficulty(CallbackInfo ci){
        if(this.difficultyID == DIFFICULTY_NM_BAD_DREAM && onlyOnce){
            this.difficultyID = DIFFICULTY_NM_NIGHTMARE;
            onlyOnce = false;
        }
    }

    @Inject(method = "actionPerformed", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/GuiCreateWorld;updateButtonText()V", ordinal = 8))
    private void manageDifficulty2(GuiButton par1GuiButton, CallbackInfo ci){
        if(this.difficultyID == DIFFICULTY_BTW_HOSTILE){
            this.difficultyID = DIFFICULTY_NM_BAD_DREAM;
        } else if (this.difficultyID == DIFFICULTY_BTW_STANDARD){
            this.difficultyID = DIFFICULTY_NM_NIGHTMARE;
        }

        if(NightmareMode.bloodNightmare){
            this.difficultyID = DIFFICULTY_NM_NIGHTMARE;
        }
    }
    @Inject(method = "actionPerformed", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/GuiCreateWorld;updateButtonText()V", ordinal = 9))
    private void alwaysLockedDifficulty(CallbackInfo ci){
        this.lockDifficulty = true;
    }

    @Redirect(method = "updateButtonText", at = @At(value = "INVOKE", target = "Lbtw/world/util/difficulty/Difficulty;getLocalizedName()Ljava/lang/String;"))
    private String customDifficultyName(Difficulty difficulty){
        if(difficulty.ID == DIFFICULTY_NM_NIGHTMARE){
            if(NightmareMode.bloodNightmare){
                return "Bloodmare";
            }
            return "Nightmare";
        } else if (difficulty.ID == DIFFICULTY_NM_BAD_DREAM){
            return "Bad Dream";
        }
        return difficulty.NAME;
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/I18n;getString(Ljava/lang/String;)Ljava/lang/String;",ordinal = 10))
    private String customText(String string){
        if (this.difficultyID == DIFFICULTY_NM_NIGHTMARE) {
            if(NightmareMode.bloodNightmare){
                return "";
            }
            return "The ultimate challenge.";
        }
        return "A more relaxed experience. Makes";
    }
    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/I18n;getString(Ljava/lang/String;)Ljava/lang/String;",ordinal = 11))
    private String customText1(String string){
        if (this.difficultyID == DIFFICULTY_NM_NIGHTMARE) {
            return "";
        }
        return "many aspects of Nightmare Mode";
    }
    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/I18n;getString(Ljava/lang/String;)Ljava/lang/String;",ordinal = 12))
    private String customText2(String string){
        if (this.difficultyID == DIFFICULTY_NM_NIGHTMARE) {
            return "";
        }
        return "easier and more forgiving.";
    }
}
