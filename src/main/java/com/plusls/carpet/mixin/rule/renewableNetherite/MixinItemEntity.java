package com.plusls.carpet.mixin.rule.renewableNetherite;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.ItemUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.3 里 {@code Entity#hurt} 变成 final，真正的逻辑在 {@code hurtServer(ServerLevel, DamageSource, float)}；
 * 同时 {@code ArmorItem}/{@code TieredItem}/{@code Tiers} 已被物品组件体系取代，
 * 因此“是不是钻石装备”改由 {@link ItemUtil#upGradeToNetheriteLike} 的映射表判定（等价且更稳）。
 */
@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {
    private MixinItemEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    public abstract ItemStack getItem();

    @Inject(
            method = "hurtServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V"
            )
    )
    private void checkDiamondEquip(ServerLevel level, DamageSource source, float amount,
                                   CallbackInfoReturnable<Boolean> cir) {
        if (!PluslsCarpetAdditionSettings.renewableNetheriteEquip) {
            return;
        }

        if (source != level.damageSources().lava() && level.dimension() != Level.NETHER) {
            return;
        }

        ItemStack stack = this.getItem();

        if (stack.isEmpty() || stack.getMaxDamage() - stack.getDamageValue() != 1) {
            return;
        }

        ItemStack newItemStack = ItemUtil.upGradeToNetheriteLike(stack);

        if (newItemStack != null) {
            level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY(), this.getZ(), newItemStack));
        }
    }
}
