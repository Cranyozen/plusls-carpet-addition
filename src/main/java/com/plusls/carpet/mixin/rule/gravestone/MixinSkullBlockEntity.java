package com.plusls.carpet.mixin.rule.gravestone;

import com.plusls.carpet.util.rule.gravestone.DeathInfo;
import com.plusls.carpet.util.rule.gravestone.GravesStoneSkullBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullBlockEntity.class)
public class MixinSkullBlockEntity implements GravesStoneSkullBlockEntity {
    @Unique
    private DeathInfo pca$deathInfo;

    @Unique
    private boolean pca$looted;

    @Unique
    private ServerPlayer pca$breakingPlayer;

    @Override
    public @Nullable DeathInfo pca$getDeathInfo() {
        return this.pca$deathInfo;
    }

    @Override
    public void pca$setDeathInfo(DeathInfo deathInfo) {
        this.pca$deathInfo = deathInfo;
    }

    @Override
    public boolean pca$isLooted() {
        return this.pca$looted;
    }

    @Override
    public void pca$markLooted() {
        this.pca$looted = true;
    }

    @Override
    public @Nullable ServerPlayer pca$getBreakingPlayer() {
        return this.pca$breakingPlayer;
    }

    @Override
    public void pca$setBreakingPlayer(@Nullable ServerPlayer player) {
        this.pca$breakingPlayer = player;
    }

    @Inject(
            method = "loadAdditional",
            at = @At(value = "RETURN")
    )
    private void postLoad(@NotNull ValueInput input, CallbackInfo ci) {
        // 只有真正写过死亡数据的头骨才算墓碑；普通玩家头不能凭空变成空墓碑
        // （否则重启后敲掉一个普通玩家头会什么都不掉，还会挡流体）。
        if (input.getLong("DeathTime").isPresent()) {
            this.pca$deathInfo = DeathInfo.read(input);
        }
    }

    @Inject(
            method = "saveAdditional",
            at = @At(value = "RETURN")
    )
    private void postSave(@NotNull ValueOutput output, CallbackInfo ci) {
        if (this.pca$deathInfo != null) {
            this.pca$deathInfo.store(output);
        }
    }
}
