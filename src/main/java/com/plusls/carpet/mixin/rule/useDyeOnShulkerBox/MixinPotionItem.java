package com.plusls.carpet.mixin.rule.useDyeOnShulkerBox;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.rule.useDyeOnShulkerBox.ShulkerBoxRecolor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public abstract class MixinPotionItem extends Item {
    public MixinPotionItem(Properties properties) {
        super(properties);
    }

    @Inject(
            method = "useOn",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void preUseOn(@NotNull UseOnContext useOnContext, @NotNull CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = useOnContext.getItemInHand();
        Player player = useOnContext.getPlayer();

        if (!PluslsCarpetAdditionSettings.useDyeOnShulkerBox ||
                player == null ||
                itemStack.getItem() != Items.POTION ||
                !itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
            return;
        }

        Level level = useOnContext.getLevel();
        BlockPos pos = useOnContext.getClickedPos();

        if (!(level.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }

        if (ShulkerBoxRecolor.recolor(level, pos, Blocks.SHULKER_BOX)) {
            if (!player.isCreative()) {
                itemStack.shrink(1);
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
