package com.plusls.carpet.mixin.rule.gravestone;

import com.plusls.carpet.util.rule.gravestone.DeathInfo;
import com.plusls.carpet.util.rule.gravestone.GravesStoneSkullBlockEntity;
import com.plusls.carpet.util.rule.gravestone.GravestoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.BiConsumer;

@Mixin(PlayerHeadBlock.class)
public abstract class MixinPlayerHeadBlock extends SkullBlock {
    protected MixinPlayerHeadBlock(Type skullType, Properties properties) {
        super(skullType, properties);
    }

    /**
     * 包住原版头骨的 ticker（皮肤 / 动画那套），额外给墓碑跑流体倒计时。
     */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        BlockEntityTicker<T> vanillaTicker = super.getTicker(level, state, type);

        return (tickLevel, pos, tickState, blockEntity) -> {
            if (vanillaTicker != null) {
                vanillaTicker.tick(tickLevel, pos, tickState, blockEntity);
            }

            if (tickLevel instanceof ServerLevel serverLevel &&
                    blockEntity instanceof GravesStoneSkullBlockEntity gravestone) {
                GravestoneUtil.tickGrave(serverLevel, pos, gravestone);
            }
        };
    }

    /**
     * 在方块被换掉之前记下是谁挖的，{@code preRemoveSideEffects} 里好把经验直接给这个人。
     */
    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos,
                                                 @NotNull BlockState state, @NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer &&
                level.getBlockEntity(pos) instanceof GravesStoneSkullBlockEntity gravestone &&
                gravestone.pca$getDeathInfo() != null) {
            gravestone.pca$setBreakingPlayer(serverPlayer);
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * 玩家挖掘：正常结算（内容物通常已经在 preRemoveSideEffects 里掉出来了）。
     */
    @Override
    public void playerDestroy(@NotNull ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack stack) {
        if (blockEntity instanceof GravesStoneSkullBlockEntity gravestone &&
                (gravestone.pca$getDeathInfo() != null || gravestone.pca$isLooted())) {
            DeathInfo deathInfo = gravestone.pca$getDeathInfo();

            if (deathInfo != null) {
                gravestone.pca$setDeathInfo(null);
                gravestone.pca$markLooted();
                GravestoneUtil.lootGrave(level, pos, deathInfo, player);
            }

            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            return;
        }

        super.playerDestroy(level, player, pos, state, blockEntity, stack);
    }

    /**
     * TNT / 苦力怕 / 末地水晶等爆炸：先看墓碑保护次数，挡得下就整格跳过；
     * 挡不下就自己结算内容并移除方块（不掉玩家头物品），也不把位置交给原版爆炸。
     */
    @Override
    public void onExplosionHit(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos,
                               @NotNull Explosion explosion,
                               @NotNull BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (level.getBlockEntity(pos) instanceof GravesStoneSkullBlockEntity gravestone &&
                gravestone.pca$getDeathInfo() != null) {
            if (!GravestoneUtil.protectGrave(level, pos, gravestone)) {
                GravestoneUtil.destroyGrave(level, pos, gravestone);
            }

            return;
        }

        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }
}
