package com.plusls.carpet.mixin.rule.autoTrade;

import com.plusls.carpet.util.rule.autoTrade.MyVillagerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 26.3 移除了 {@code updateMerchantTimer} / {@code increaseProfessionLevelOnUpdate} 字段
 * （升级逻辑改成 {@code shouldIncreaseLevel()} + {@code increaseMerchantCareer(ServerLevel)}），
 * 这里只保留“交易也给经验”的部分，用公开的 getter/setter 完成。
 */
@Mixin(Villager.class)
public abstract class MixinVillager extends AbstractVillager implements MyVillagerEntity {
    @Shadow
    public abstract int getVillagerXp();

    @Shadow
    public abstract void setVillagerXp(int villagerXp);

    public MixinVillager(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void pca$tradeWithoutPlayer(@NotNull MerchantOffer offer) {
        this.setVillagerXp(this.getVillagerXp() + offer.getXp());
    }
}
