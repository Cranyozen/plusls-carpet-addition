package com.plusls.carpet.mixin.accessor;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 26.3 的 {@code ArmorStand#setMarker} 是 private，用 invoker 替代原来的 {@code @Shadow}。
 */
@Mixin(ArmorStand.class)
public interface ArmorStandAccessor {
    @Invoker("setMarker")
    void pca$setMarker(boolean marker);
}
