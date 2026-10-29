package com.plusls.carpet.util.rule.useDyeOnShulkerBox;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 26.3 里 {@code ShulkerBoxBlock.getBlockByColor} 已被删除（彩色潜影盒现在是独立的 Block 集合），
 * {@code BlockEntity} 的持久化也换成了 {@code ValueInput/ValueOutput}，所以这里集中处理“换色并保留内容”。
 */
public final class ShulkerBoxRecolor {
    private ShulkerBoxRecolor() {
    }

    public static boolean recolor(@NotNull Level level, @NotNull BlockPos pos, @Nullable Block newBlock) {
        if (level.isClientSide() || newBlock == null) {
            return false;
        }

        if (!(level.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity oldBlockEntity)) {
            return false;
        }

        BlockState oldState = level.getBlockState(pos);

        if (!(oldState.getBlock() instanceof ShulkerBoxBlock)) {
            return false;
        }

        var savedTag = oldBlockEntity.saveWithoutMetadata(level.registryAccess());
        var savedComponents = oldBlockEntity.components();
        BlockState newState = newBlock.defaultBlockState()
                .setValue(ShulkerBoxBlock.FACING, oldState.getValue(ShulkerBoxBlock.FACING));

        if (!level.setBlockAndUpdate(pos, newState)) {
            return false;
        }

        if (level.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity newBlockEntity) {
            newBlockEntity.loadFromTag(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), savedTag));
            newBlockEntity.setComponents(savedComponents);
            newBlockEntity.setChanged();
            return true;
        }

        return false;
    }
}
