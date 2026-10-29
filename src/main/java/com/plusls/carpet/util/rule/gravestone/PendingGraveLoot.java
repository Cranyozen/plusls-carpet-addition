package com.plusls.carpet.util.rule.gravestone;

import com.plusls.carpet.PluslsCarpetAdditionReference;

import java.util.ArrayList;
import java.util.List;

/**
 * 墓碑被爆炸摧毁时，如果当拍就把物品掉出来，掉落物会被同一次爆炸的伤害阶段直接炸掉。
 * 这里把结算推迟到下一个 tick（爆炸的伤害阶段已经结束），物品才能活下来。
 */
public final class PendingGraveLoot {
    private static final List<Runnable> TASKS = new ArrayList<>();

    public static void enqueue(Runnable task) {
        TASKS.add(task);
    }

    public static void tick() {
        if (TASKS.isEmpty()) {
            return;
        }

        List<Runnable> pending = new ArrayList<>(TASKS);
        TASKS.clear();

        for (Runnable task : pending) {
            try {
                task.run();
            } catch (Exception exception) {
                PluslsCarpetAdditionReference.getLogger().error("gravestone: delayed loot failed", exception);
            }
        }
    }

    private PendingGraveLoot() {
    }
}
