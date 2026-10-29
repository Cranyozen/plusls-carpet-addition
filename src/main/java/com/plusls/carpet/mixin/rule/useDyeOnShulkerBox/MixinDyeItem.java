package com.plusls.carpet.mixin.rule.useDyeOnShulkerBox;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.rule.useDyeOnShulkerBox.ShulkerBoxRecolor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.3 的 {@link DyeItem} 已经不再声明 {@code useOn}（染料行为改为组件 + {@code SignApplicator} 驱动），
 * 因此注入点下沉到 {@link Item#useOn}，再用 {@code instanceof DyeItem} 过滤。
 */
@Mixin(Item.class)
public abstract class MixinDyeItem {
    @Inject(
            method = "useOn",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void preUseOn(UseOnContext context, @NotNull CallbackInfoReturnable<InteractionResult> cir) {
        if (!PluslsCarpetAdditionSettings.useDyeOnShulkerBox) {
            return;
        }

        if (!((Object) this instanceof DyeItem)) {
            return;
        }

        ItemStack itemStack = context.getItemInHand();
        DyeColor dyeColor = itemStack.get(DataComponents.DYE);

        if (dyeColor == null) {
            return;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!(level.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }

        if (ShulkerBoxRecolor.recolor(level, pos, Blocks.DYED_SHULKER_BOX.pick(dyeColor))) {
            if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
                itemStack.shrink(1);
            }
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
