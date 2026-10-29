package com.plusls.carpet.mixin.rule.pcaSyncProtocol.block;

import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.impl.network.PcaSyncProtocol;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(BeehiveBlockEntity.class)
public abstract class MixinBeehiveBlockEntity extends BlockEntity {
    private MixinBeehiveBlockEntity(
            BlockEntityType<?> blockEntityType,
            BlockPos blockPos,
            BlockState blockState
    ) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(
            method = "tickOccupants",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Iterator;remove()V",
                    shift = At.Shift.AFTER
            )
    )
    private static void postTickOccupants(Level level, BlockPos blockPos, BlockState blockState,
                                          List<BeehiveBlockEntity.BeeData> bees, BlockPos flowerPos, CallbackInfo ci) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncBlockEntityToClient(
                Objects.requireNonNull(level.getBlockEntity(blockPos)))) {
            PluslsCarpetAdditionReference.getLogger().debug("update BeehiveBlockEntity: {}", blockPos);
        }
    }

    @Inject(
            method = "releaseAllOccupants",
            at = @At(value = "RETURN")
    )
    private void postReleaseAllOccupants(BlockState blockState,
                                         BeehiveBlockEntity.BeeReleaseStatus releaseStatus,
                                         @NotNull CallbackInfoReturnable<List<Entity>> cir) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncBlockEntityToClient(this)) {
            PluslsCarpetAdditionReference.getLogger().debug("update BeehiveBlockEntity: {}", this.worldPosition);
        }
    }

    @Inject(
            method = "loadAdditional",
            at = @At(value = "RETURN")
    )
    private void postLoad(@NotNull ValueInput input, CallbackInfo ci) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncBlockEntityToClient(this)) {
            PluslsCarpetAdditionReference.getLogger().debug("update BeehiveBlockEntity: {}", this.worldPosition);
        }
    }

    @Inject(
            method = "addOccupant",
            at = @At(value = "RETURN")
    )
    private void postAddOccupant(Bee bee, CallbackInfo ci) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncBlockEntityToClient(this)) {
            PluslsCarpetAdditionReference.getLogger().debug("update BeehiveBlockEntity: {}", this.worldPosition);
        }
    }
}
