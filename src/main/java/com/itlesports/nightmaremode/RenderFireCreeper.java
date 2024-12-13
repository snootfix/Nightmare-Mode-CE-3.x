package com.itlesports.nightmaremode;

import btw.entity.mob.JungleSpiderEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * Fire Creeper renderer
 */
public class RenderFireCreeper extends RenderCreeper {
    private final ModelBase creeperModel = new ModelCreeper(2.0f);
    private static final ResourceLocation armoredCreeperTextures = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    public static final ResourceLocation FIRE_CREEPER_TEXTURE = new ResourceLocation("textures/entity/firecreeper.png");

    /**
     * Constructor
     *
     * @return {RenderFireCreeper}
     */
    public RenderFireCreeper() {
        super();
    }

    /**
     * Overrides the creeper texture if it's a fire creeper
     *
     * @param {EntityCreeper} par1EntityCreeper - The creeper entity
     *
     * @return {ResourceLocation} - The texture
     */
    @Override
    protected ResourceLocation getCreeperTextures(EntityCreeper par1EntityCreeper) {
        if (par1EntityCreeper instanceof EntityFireCreeper) {
            return FIRE_CREEPER_TEXTURE;
        }

        return super.getEntityTexture(par1EntityCreeper);
    }

    /**
     * Overrides the entity texture if it's a fire creeper
     *
     * @param {Entity} par1Entity - The entity
     */
    @Override
    protected ResourceLocation getEntityTexture(Entity par1Entity) {
        if (par1Entity instanceof EntityFireCreeper) {
            return FIRE_CREEPER_TEXTURE;
        }

        return super.getEntityTexture(par1Entity);
    }

    /**
     * Overrides the render pass for the fire creeper
     *
     * @param {EntityLivingBase} par1EntityLivingBase - The entity
     * @param {int} par2 - TODO: ???
     * @param {float} par3 - TODO: ???
     *
     * @return {int} - The render pass
     */
    @Override
    protected int shouldRenderPass(EntityLivingBase par1EntityLivingBase, int par2, float par3) {
        return this.renderCreeperPassModel((EntityFireCreeper)par1EntityLivingBase, par2, par3);
    }

    /**
     * Creates the render pass for the fire creeper
     *
     * @param {EntityFireCreeper} par1EntityCreeper - The fire creeper entity
     * @param {int} par2 - TODO: ???
     * @param {float} par3 - TODO: ???
     *
     * @return {int} - The render pass
     */
    protected int renderCreeperPassModel(EntityFireCreeper par1EntityCreeper, int par2, float par3) {
        if (par1EntityCreeper.getPowered()) {
            if (par1EntityCreeper.isInvisible()) {
                GL11.glDepthMask(false);
            } else {
                GL11.glDepthMask(true);
            }
            if (par2 == 1) {
                float var4 = (float)par1EntityCreeper.ticksExisted + par3;
                this.bindTexture(armoredCreeperTextures);
                GL11.glMatrixMode(5890);
                GL11.glLoadIdentity();
                float var5 = var4 * 0.01f;
                float var6 = var4 * 0.01f;
                GL11.glTranslatef(var5, var6, 0.0f);
                this.setRenderPassModel(this.creeperModel);
                GL11.glMatrixMode(5888);
                GL11.glEnable(3042);
                float var7 = 0.5f;
                GL11.glColor4f(var7, var7, var7, 1.0f);
                GL11.glDisable(2896);
                GL11.glBlendFunc(1, 1);
                return 1;
            }
            if (par2 == 2) {
                GL11.glMatrixMode(5890);
                GL11.glLoadIdentity();
                GL11.glMatrixMode(5888);
                GL11.glEnable(2896);
                GL11.glDisable(3042);
            }
        }

        return -1;
    }
}
