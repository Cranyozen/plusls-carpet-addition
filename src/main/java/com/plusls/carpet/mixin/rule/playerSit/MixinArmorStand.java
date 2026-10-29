package com.plusls.carpet.mixin.rule.playerSit;

import com.plusls.carpet.util.rule.playerSit.SitEntity;
import com.plusls.carpet.mixin.accessor.ArmorStandAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStand.class)
public abstract class MixinArmorStand extends LivingEntity implements SitEntity {
    @Unique
    private boolean pca$sitEntity = false;

    protected MixinArmorStand(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean pca$isSitEntity() {
        return this.pca$sitEntity;
    }

    @Override
    public void pca$setSitEntity(boolean isSitEntity) {
        this.pca$sitEntity = isSitEntity;
        ((ArmorStandAccessor) this).pca$setMarker(isSitEntity);
        this.setInvisible(isSitEntity);
    }

    @Override
    @Intrinsic
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
    }

    @Inject(
            method = "removePassenger",
            at = @At("HEAD")
    )
    private void preRemovePassenger(Entity passenger, CallbackInfo ci) {
        if (this.pca$isSitEntity()) {
            this.setPos(this.getX(), this.getY() + 0.16, this.getZ());

            if (this.level() instanceof ServerLevel serverLevel) {
                this.kill(serverLevel);
            }
        }
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("RETURN")
    )
    private void postAddAdditionalSaveData(@NotNull ValueOutput output, CallbackInfo ci) {
        if (this.pca$sitEntity) {
            output.putBoolean("SitEntity", true);
        }
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("RETURN")
    )
    private void postReadAdditionalSaveData(@NotNull ValueInput input, CallbackInfo ci) {
        this.pca$sitEntity = input.getBooleanOr("SitEntity", false);
    }
}
