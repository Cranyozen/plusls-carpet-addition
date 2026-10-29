package com.plusls.carpet.mixin.rule.spawnBiome;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.3 里 {@code NaturalSpawner.mobsAt} 不再接收 Biome，返回 {@code WeightedList}，
 * 而且群系的刷怪表变成了环境属性 {@code EnvironmentAttributes.NATURAL_MOB_SPAWNS}。
 * 这里在 {@code mobsAt} 返回处按“全局刷怪群系”重新计算一次刷怪表。
 */
@Mixin(NaturalSpawner.class)
public class MixinNaturalSpawner {
    @Inject(
            method = "mobsAt",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private static void pca$replaceSpawnBiome(ServerLevel level,
                                              StructureManager structureManager,
                                              ChunkGenerator generator,
                                              MobCategory category,
                                              BlockPos pos,
                                              @NotNull CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>> cir) {
        ResourceKey<Biome> targetBiome = pca$getTargetBiome();

        if (targetBiome == null) {
            return;
        }

        Holder<Biome> biome = level.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getOrThrow(targetBiome);
        MobSpawnSettings spawnSettings = biome.value().getAttributes()
                .applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY);
        cir.setReturnValue(spawnSettings.getMobsInCategory(category));
    }

    private static @Nullable ResourceKey<Biome> pca$getTargetBiome() {
        return switch (PluslsCarpetAdditionSettings.spawnBiome) {
            case DESERT -> Biomes.DESERT;
            case PLAINS -> Biomes.PLAINS;
            case THE_END -> Biomes.THE_END;
            case NETHER_WASTES -> Biomes.NETHER_WASTES;
            default -> null;
        };
    }
}
