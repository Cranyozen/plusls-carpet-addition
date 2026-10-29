package com.plusls.carpet.mixin.rule.gravestone;

import com.plusls.carpet.util.rule.gravestone.DeathInfo;
import com.plusls.carpet.util.rule.gravestone.GravesStoneSkullBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 流体不能流进墓碑（玩家头），避免刷掉死亡信息。
 * <ul>
 *     <li>{@code canHoldFluid}：26.3 是 static，处理器也要 static；</li>
 *     <li>{@code spreadTo}：真正落方块的地方。水只靠 canHoldFluid 就能挡住，
 *     但 {@code LavaFluid.spreadTo} 会走自己的分支，实测岩浆仍能覆盖墓碑，所以这里再兜一层。</li>
 * </ul>
 */
@Mixin(FlowingFluid.class)
public abstract class MixinFlowingFluid extends Fluid {
    @Inject(
            method = "canHoldFluid",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true
    )
    private static void checkRail(BlockGetter blockGetter, BlockPos pos, BlockState state, Fluid fluid,
                                 @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !(state.getBlock() instanceof PlayerHeadBlock)) {
            return;
        }

        if (blockGetter.getBlockEntity(pos) instanceof GravesStoneSkullBlockEntity graveEntity &&
                graveEntity.pca$getDeathInfo() != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "spreadTo",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void pca$blockGraveFlood(LevelAccessor level, BlockPos pos, BlockState state, Direction direction,
                                     FluidState fluidState, @NotNull CallbackInfo ci) {
        if (state.getBlock() instanceof PlayerHeadBlock &&
                level.getBlockEntity(pos) instanceof GravesStoneSkullBlockEntity graveEntity &&
                graveEntity.pca$getDeathInfo() != null) {
            ci.cancel();
        }
    }
}
