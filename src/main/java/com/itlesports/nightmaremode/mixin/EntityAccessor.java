package com.itlesports.nightmaremode.mixin;

import net.minecraft.src.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * NM entity accessors
 */
@Mixin(Entity.class)
public interface EntityAccessor {
    /**
     * Whether the entity is in a web
     *
     * @return {boolean} - Whether the entity is in a web
     */
    @Accessor("isInWeb")
    boolean getIsInWeb();
}
