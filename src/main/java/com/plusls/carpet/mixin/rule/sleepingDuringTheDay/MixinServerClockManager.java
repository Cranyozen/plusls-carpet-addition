package com.plusls.carpet.mixin.rule.sleepingDuringTheDay;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.3 删除了 {@code ServerLevel#setDayTime}，昼/夜由数据驱动的时钟（{@code world.clock.*}）管理，
 * 睡觉“跳到天亮”是 {@code ServerClockManager.moveToTimeMarker(clock, WAKE_UP_FROM_SLEEP)}。
 * 因此在白天睡觉时把目标标记换成 {@code NIGHT}，即可复现“白天睡觉直接入夜”的原始行为。
 */
@Mixin(ServerClockManager.class)
public abstract class MixinServerClockManager {
    @Inject(
            method = "moveToTimeMarker",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void pca$sleepToNight(Holder<WorldClock> clock,
                                  ResourceKey<ClockTimeMarker> marker,
                                  CallbackInfoReturnable<ServerClockManager.MoveResult> cir) {
        if (!PluslsCarpetAdditionSettings.sleepingDuringTheDay) {
            return;
        }

        if (!ClockTimeMarkers.WAKE_UP_FROM_SLEEP.equals(marker)) {
            return;
        }

        ServerClockManager self = (ServerClockManager) (Object) this;

        // 白天（0 ~ 13000 tick）睡觉时直接跳到夜晚；夜里睡觉保持原版逻辑
        if (self.getInstance(clock).totalTicks() % 24000L >= 13000L) {
            return;
        }

        cir.setReturnValue(self.moveToTimeMarker(clock, ClockTimeMarkers.NIGHT));
    }
}
