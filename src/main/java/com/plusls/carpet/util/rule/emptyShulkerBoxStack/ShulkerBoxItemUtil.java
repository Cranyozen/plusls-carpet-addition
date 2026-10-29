package com.plusls.carpet.util.rule.emptyShulkerBoxStack;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.component.DataComponents;import net.minecraft.world.item.component.ItemContainerContents;
public class ShulkerBoxItemUtil {
    public static final int SHULKERBOX_MAX_STACK_AMOUNT = 64;

    public static boolean isEmptyShulkerBoxItem(@NotNull ItemStack itemStack) {
        ItemContainerContents countContainer = itemStack.getComponents().get(DataComponents.CONTAINER);        return countContainer == null || !countContainer.nonEmptyItems().iterator().hasNext();    }

    public static int getMaxCount(ItemStack itemStack) {
        if (PluslsCarpetAdditionSettings.emptyShulkerBoxStack && ShulkerBoxItemUtil.isEmptyShulkerBoxItem(itemStack)) {
            return ShulkerBoxItemUtil.SHULKERBOX_MAX_STACK_AMOUNT;
        } else {
            return itemStack.getMaxStackSize();
        }
    }

    public static boolean isStackable(ItemStack itemStack) {
        return getMaxCount(itemStack) > 1 && (!itemStack.isDamageableItem() || !itemStack.isDamaged());
    }
}