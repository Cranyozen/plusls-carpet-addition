package com.plusls.carpet.util.rule.gravestone;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

/**
 * 26.3 的方块实体持久化换成了 {@code ValueInput/ValueOutput}，
 * {@code SimpleContainer} 也只剩 {@code fromItemList/storeAsItemList}，因此这里直接走新的值 API。
 */
public class DeathInfo {
    public final long deathTime;
    public final int xp;
    public final SimpleContainer inventory;
    /**
     * 还能扛几次非玩家破坏（爆炸 / 活塞）；0 表示下一次就直接结算掉。
     */
    private int protection;
    /**
     * 上一次成功挡下破坏的时间戳，用来做「同一 tick 只扣一次」的去重（不持久化）。
     */
    private long protectedTick = Long.MIN_VALUE;

    public DeathInfo(long deathTime, int xp, SimpleContainer inventory) {
        this(deathTime, xp, inventory, 0);
    }

    public DeathInfo(long deathTime, int xp, SimpleContainer inventory, int protection) {
        this.deathTime = deathTime;
        this.xp = xp;
        this.inventory = inventory;
        this.protection = Math.max(0, protection);
    }

    public int getProtection() {
        return this.protection;
    }

    /**
     * 被水 / 岩浆「浸泡」累计的 tick 数，到 `gravestoneFluidGraceSeconds * 20` 就销毁。
     */
    private int fluidGraceAge;


    /**
     * 尝试挡下一次破坏。返回 true 表示这次破坏被保护挡下（方块应当保持原样）。
     * 同一 gameTime 内的重复调用不重复扣减，避免一次活塞/爆炸动作把次数吃光。
     */
    public boolean tryBlockDamage(long gameTime) {
        if (this.protectedTick == gameTime) {
            return true;
        }

        if (this.protection > 0) {
            this.protection--;
            this.protectedTick = gameTime;
            return true;
        }

        return false;
    }

    public int getFluidGraceAge() {
        return this.fluidGraceAge;
    }

    public void addFluidGraceAge() {
        this.fluidGraceAge++;
    }

    public void resetFluidGraceAge() {
        this.fluidGraceAge = 0;
    }


    public static @NotNull DeathInfo read(@NotNull ValueInput input) {
        long deathTime = input.getLongOr("DeathTime", 0L);
        int xp = input.getIntOr("XP", 0);
        SimpleContainer inventory = new SimpleContainer(GravestoneUtil.PLAYER_INVENTORY_SIZE);
        inventory.fromItemList(input.listOrEmpty("Items", ItemStack.OPTIONAL_CODEC));
        DeathInfo deathInfo = new DeathInfo(deathTime, xp, inventory, input.getIntOr("Protection", 0));
        deathInfo.fluidGraceAge = input.getIntOr("FluidGraceAge", 0);
        return deathInfo;
    }

    public void store(@NotNull ValueOutput output) {
        output.putLong("DeathTime", this.deathTime);
        output.putInt("XP", this.xp);
        this.inventory.storeAsItemList(output.list("Items", ItemStack.OPTIONAL_CODEC));
        output.putInt("Protection", this.protection);
        output.putInt("FluidGraceAge", this.fluidGraceAge);
    }
}
