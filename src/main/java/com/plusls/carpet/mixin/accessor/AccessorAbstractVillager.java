package com.plusls.carpet.mixin.accessor;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractVillager.class)
public interface AccessorAbstractVillager {
    @Accessor("inventory")
    SimpleContainer pca$getInventory();
}
