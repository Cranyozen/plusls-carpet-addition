package com.plusls.carpet.mixin.rule.pcaSyncProtocol.block;

import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.impl.network.PcaSyncProtocol;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 里只有 {@code BlockEntity} 自己声明 {@code setChanged()}，熔炉/箱子/木桶/酿造台/比较器/发射器/潜影盒
 * 等子类都不再覆写它，因此把 9 个 per-class mixin 合并成这一个锚点。
 */
@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity {
    @Inject(
            method = "setChanged",
            at = @At(value = "RETURN")
    )
    private void postSetChanged(CallbackInfo ci) {
        if (!PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            return;
        }

        BlockEntity self = (BlockEntity) (Object) this;

        if (PcaSyncProtocol.syncBlockEntityToClient(self)) {
            PluslsCarpetAdditionReference.getLogger().debug("update blockEntity: {}", self.getBlockPos());
        }
    }
}
