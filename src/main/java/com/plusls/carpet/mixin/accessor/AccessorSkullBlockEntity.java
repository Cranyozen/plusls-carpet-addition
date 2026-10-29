package com.plusls.carpet.mixin.accessor;

import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 26.3 的 {@code SkullBlockEntity} 已经没有 {@code setOwner(...)}（只剩 {@code getOwnerProfile()}），
 * 这里直接改私有字段，避免依赖组件的隐式装载流程。
 */
@Mixin(net.minecraft.world.level.block.entity.SkullBlockEntity.class)
public interface AccessorSkullBlockEntity {
    @Accessor("owner")
    void pca$setOwnerProfile(ResolvableProfile profile);
}
