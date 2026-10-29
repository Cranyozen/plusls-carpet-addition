package com.plusls.carpet.mixin.rule.quickLeafDecay;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
@Mixin(LeavesBlock.class)
public abstract class MixinLeavesBlock extends Block {
    public MixinLeavesBlock(AmbientLeavesBlockSoundPlayer soundPlayer, Properties settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    @Inject(
            method = "tick",
            at = @At(
                    "RETURN"
            )
    )
    private void postScheduledTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {        if (PluslsCarpetAdditionSettings.quickLeafDecay) {
            this.randomTick(state, level, pos, random);
        }
    }
}
