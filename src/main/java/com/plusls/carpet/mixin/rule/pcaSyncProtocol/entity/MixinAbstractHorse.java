package com.plusls.carpet.mixin.rule.pcaSyncProtocol.entity;

import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.impl.network.PcaSyncProtocol;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.3 把 {@code containerChanged} 换成了 {@code hasInventoryChanged(Container)}，
 * 且 {@code ContainerListener} 机制不再可用，这里直接钩住新的判定方法。
 */
@Mixin(AbstractHorse.class)
public abstract class MixinAbstractHorse extends Animal {
    protected MixinAbstractHorse(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "hasInventoryChanged",
            at = @At(value = "RETURN")
    )
    private void updateEntity(Container container, CallbackInfoReturnable<Boolean> cir) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncEntityToClient(this)) {
            PluslsCarpetAdditionReference.getLogger().debug("update HorseBaseEntity inventory: onInventoryChanged.");
        }
    }
}
