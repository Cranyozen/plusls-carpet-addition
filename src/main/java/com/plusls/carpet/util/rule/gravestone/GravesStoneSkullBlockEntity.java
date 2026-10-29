package com.plusls.carpet.util.rule.gravestone;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface GravesStoneSkullBlockEntity {
    @Nullable
    DeathInfo pca$getDeathInfo();

    void pca$setDeathInfo(DeathInfo deathInfo);

    /**
     * 墓碑里的东西是否已经结算过（被挖掉 / 炸掉 / 被活塞推掉 / 被换成别的方块……）。
     */
    boolean pca$isLooted();

    void pca$markLooted();

    /**
     * 正在挖这个墓碑的玩家，用来把经验直接进玩家背包而不是掉经验球。
     * 只在 {@code playerWillDestroy} → {@code preRemoveSideEffects} 之间有效。
     */
    @Nullable
    ServerPlayer pca$getBreakingPlayer();

    void pca$setBreakingPlayer(@Nullable ServerPlayer player);
}
