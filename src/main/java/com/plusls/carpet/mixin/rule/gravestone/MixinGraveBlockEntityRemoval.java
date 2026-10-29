package com.plusls.carpet.mixin.rule.gravestone;

import com.plusls.carpet.util.rule.gravestone.DeathInfo;
import com.plusls.carpet.util.rule.gravestone.GravesStoneSkullBlockEntity;
import com.plusls.carpet.util.rule.gravestone.GravestoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 里任何「方块被换掉」都会走 {@code BlockEntity#preRemoveSideEffects}（{@code LevelChunk#setBlockState} 里调），
 * 但不走区块卸载。把结算点放在这里，爆炸、活塞、创造模式挖掘、{@code /setblock}、其它模组全都能兜住，
 * 不会再出现「墓碑没了、里面的东西也没了」。
 */
@Mixin(BlockEntity.class)
public abstract class MixinGraveBlockEntityRemoval {
    @Inject(
            method = "preRemoveSideEffects",
            at = @At(value = "HEAD")
    )
    private void pca$lootGraveOnRemove(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!((Object) this instanceof GravesStoneSkullBlockEntity gravestone)) {
            return;
        }

        DeathInfo deathInfo = gravestone.pca$getDeathInfo();

        if (deathInfo == null) {
            return;
        }

        if (!(((BlockEntity) (Object) this).getLevel() instanceof ServerLevel level)) {
            return;
        }

        gravestone.pca$setDeathInfo(null);
        gravestone.pca$markLooted();

        // 玩家自己挖的情况（playerWillDestroy 里记下来的），经验直接进玩家身上，不掉经验球
        ServerPlayer breakingPlayer = gravestone.pca$getBreakingPlayer();
        gravestone.pca$setBreakingPlayer(null);

        GravestoneUtil.lootGrave(level, pos, deathInfo, breakingPlayer);
    }
}
