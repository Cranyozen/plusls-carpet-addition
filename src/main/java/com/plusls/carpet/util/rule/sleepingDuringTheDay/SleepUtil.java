package com.plusls.carpet.util.rule.sleepingDuringTheDay;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;

/**
 * 26.3 的 Fabric 事件由 {@code ALLOW_SLEEP_TIME} 改成了 {@code ALLOW_SLEEPING}
 * （返回 {@code BedSleepingProblem} 表示拒绝，返回 {@code null} 表示放行）。
 */
public class SleepUtil {
    public static void init() {
        EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
            if (PluslsCarpetAdditionSettings.sleepingDuringTheDay) {
                // 允许白天睡觉；真正的“跳到夜晚”由 MixinServerClockManager 处理
                return null;
            }

            return null;
        });
    }
}
