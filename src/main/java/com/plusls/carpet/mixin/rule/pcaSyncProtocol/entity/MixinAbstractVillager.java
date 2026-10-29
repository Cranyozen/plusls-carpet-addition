package com.plusls.carpet.mixin.rule.pcaSyncProtocol.entity;

import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.impl.network.PcaSyncProtocol;
import com.plusls.carpet.mixin.accessor.AccessorAbstractVillager;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 移除了 {@code ContainerListener} / {@code SimpleContainer.addListener}，
 * 因此改为在 tick 末尾比对容器内容指纹，变化时才推送同步包。
 * 注意：{@code AbstractVillager} 自己不再声明 {@code tick}，这里锚在声明了 tick 的 {@link Villager} 上。
 */
@Mixin(Villager.class)
public abstract class MixinAbstractVillager extends AbstractVillager {
    @Unique
    private int pca$lastInventoryHash;

    protected MixinAbstractVillager(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "tick",
            at = @At(value = "RETURN")
    )
    private void pca$checkInventoryChanged(CallbackInfo ci) {
        if (!PluslsCarpetAdditionSettings.pcaSyncProtocol || this.level().isClientSide()) {
            return;
        }

        SimpleContainer inventory = ((AccessorAbstractVillager) this).pca$getInventory();
        int hash = 1;

        for (int slot = 0; slot < inventory.getContainerSize(); ++slot) {
            ItemStack stack = inventory.getItem(slot);
            hash = 31 * hash + (stack.isEmpty() ? 0
                    : stack.getItem().hashCode() * 31 + stack.getCount() * 17
                    + stack.getComponents().hashCode());
        }

        if (hash != this.pca$lastInventoryHash) {
            this.pca$lastInventoryHash = hash;

            if (PcaSyncProtocol.syncEntityToClient(this)) {
                PluslsCarpetAdditionReference.getLogger().debug("update villager inventory: onInventoryChanged.");
            }
        }
    }
}
