package com.plusls.carpet.mixin.rule.avoidAnvilTooExpensive;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 26.3 的“过于昂贵”判定从 {@code Abilities.instabuild} 改成了
 * {@code cost >= 40 && !player.hasInfiniteMaterials()}，命中后把结果槽置空。
 * 这里把该判定（createResult 里的第 2 个调用，第 1 个属于附魔书逻辑）伪造为 true，
 * 结果槽保持有物品，即可继续取出。
 */
@Mixin(AnvilMenu.class)
public class MixinAnvilMenu {
    @Redirect(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z",
                    ordinal = 1
            )
    )
    private boolean pca$notTooExpensive(@NotNull Player player) {
        return player.hasInfiniteMaterials() || PluslsCarpetAdditionSettings.avoidAnvilTooExpensive;
    }
}
