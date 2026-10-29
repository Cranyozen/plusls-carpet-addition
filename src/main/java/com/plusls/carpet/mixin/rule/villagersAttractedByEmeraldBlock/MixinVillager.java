package com.plusls.carpet.mixin.rule.villagersAttractedByEmeraldBlock;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class MixinVillager extends AbstractVillager {
    @Unique
    boolean pca$VillagersAttractedByEmeraldBlock;
    @Unique
    private TemptGoal pca$villagersAttractedByEmeraldBlockGoal;

    public MixinVillager(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private TemptGoal pca$createTemptGoal() {
        return new TemptGoal(this, 1.0D, stack -> stack.is(Items.EMERALD_BLOCK), false);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At(
                    value = "RETURN"
            )
    )
    private void init(EntityType<? extends Villager> entityType, Level level, CallbackInfo ci) {
        if (this.level().isClientSide()) {
            return;
        }

        this.pca$villagersAttractedByEmeraldBlockGoal = this.pca$createTemptGoal();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "RETURN"
            )
    )
    private void checkVillagersAttractedByEmeraldBlock(CallbackInfo ci) {
        if (this.level().isClientSide()) {
            return;
        }

        if (!this.pca$VillagersAttractedByEmeraldBlock && PluslsCarpetAdditionSettings.villagersAttractedByEmeraldBlock) {
            if (!this.pca$villagersAttractedByEmeraldBlockGoal.canUse()) {
                this.pca$villagersAttractedByEmeraldBlockGoal = this.pca$createTemptGoal();
            }

            this.goalSelector.addGoal(0, this.pca$villagersAttractedByEmeraldBlockGoal);
            this.pca$VillagersAttractedByEmeraldBlock = PluslsCarpetAdditionSettings.villagersAttractedByEmeraldBlock;
        } else if (this.pca$VillagersAttractedByEmeraldBlock && !PluslsCarpetAdditionSettings.villagersAttractedByEmeraldBlock) {
            this.goalSelector.removeGoal(this.pca$villagersAttractedByEmeraldBlockGoal);
            this.pca$VillagersAttractedByEmeraldBlock = PluslsCarpetAdditionSettings.villagersAttractedByEmeraldBlock;
        }
    }
}
