package com.plusls.carpet.mixin.rule.forceRestock;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.EntityCompat;


@Mixin(SpectralArrow.class)
public abstract class MixinSpectralArrow extends AbstractArrow {
    private MixinSpectralArrow(
            EntityType<? extends AbstractArrow> entityType,
            Level world
    ) {
        super(
                entityType,
                world
        );
    }

    @Inject(
            method = "doPostHurtEffects",
            at = @At(
                    value = "RETURN"
            )
    )
    private void forceRestock(LivingEntity target, CallbackInfo ci) {
        Level levelCompat = EntityCompat.of(target).getLevel();

        if (PluslsCarpetAdditionSettings.forceRestock && !levelCompat.isClientSide() && target instanceof AbstractVillager) {
            AbstractVillager villager = (AbstractVillager) target;

            for (MerchantOffer tradeOffer : villager.getOffers()) {
                tradeOffer.resetUses();
            }

            // make villager happy ~
            levelCompat.broadcastEntityEvent(villager, (byte) 14);
        }
    }
}
