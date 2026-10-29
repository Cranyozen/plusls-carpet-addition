package com.plusls.carpet.mixin.rule.avoidAnvilTooExpensive;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 26.3 里 {@code renderLabels} 已改成 {@code extractLabels(GuiGraphicsExtractor,int,int)}，
 * 并且“过于昂贵”的判断变成 {@code cost >= 40 && !player.hasInfiniteMaterials()}。
 * 这里伪造该判定，避免客户端显示“过于昂贵”红字；成本数字本身保持不变。
 *
 * <p>客户端侧属于纯显示优化，用 {@code require = 0} 保证未来版本改动时不会崩游戏。</p>
 */
@Environment(EnvType.CLIENT)
@Mixin(AnvilScreen.class)
public class MixinAnvilScreen {
    @Redirect(
            method = "extractLabels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"
            ),
            require = 0
    )
    private boolean pca$notTooExpensive(@NotNull LocalPlayer player) {
        return player.hasInfiniteMaterials() || PluslsCarpetAdditionSettings.avoidAnvilTooExpensive;
    }
}
