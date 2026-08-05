/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.sheep;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.biome.Biome;

public class SheepColorSpawnRules {
    private static final SheepColorSpawnConfiguration TEMPERATE_SPAWN_CONFIGURATION = new SheepColorSpawnConfiguration(SheepColorSpawnRules.weighted(SheepColorSpawnRules.builder().add(SheepColorSpawnRules.single(DyeColor.BLACK), 5).add(SheepColorSpawnRules.single(DyeColor.GRAY), 5).add(SheepColorSpawnRules.single(DyeColor.LIGHT_GRAY), 5).add(SheepColorSpawnRules.single(DyeColor.BROWN), 3).add(SheepColorSpawnRules.commonColors(DyeColor.WHITE), 82).build()));
    private static final SheepColorSpawnConfiguration WARM_SPAWN_CONFIGURATION = new SheepColorSpawnConfiguration(SheepColorSpawnRules.weighted(SheepColorSpawnRules.builder().add(SheepColorSpawnRules.single(DyeColor.GRAY), 5).add(SheepColorSpawnRules.single(DyeColor.LIGHT_GRAY), 5).add(SheepColorSpawnRules.single(DyeColor.WHITE), 5).add(SheepColorSpawnRules.single(DyeColor.BLACK), 3).add(SheepColorSpawnRules.commonColors(DyeColor.BROWN), 82).build()));
    private static final SheepColorSpawnConfiguration COLD_SPAWN_CONFIGURATION = new SheepColorSpawnConfiguration(SheepColorSpawnRules.weighted(SheepColorSpawnRules.builder().add(SheepColorSpawnRules.single(DyeColor.LIGHT_GRAY), 5).add(SheepColorSpawnRules.single(DyeColor.GRAY), 5).add(SheepColorSpawnRules.single(DyeColor.WHITE), 5).add(SheepColorSpawnRules.single(DyeColor.BROWN), 3).add(SheepColorSpawnRules.commonColors(DyeColor.BLACK), 82).build()));

    private static SheepColorProvider commonColors(DyeColor $$0) {
        return SheepColorSpawnRules.weighted(SheepColorSpawnRules.builder().add(SheepColorSpawnRules.single($$0), 499).add(SheepColorSpawnRules.single(DyeColor.PINK), 1).build());
    }

    public static DyeColor getSheepColor(Holder<Biome> $$0, RandomSource $$1) {
        SheepColorSpawnConfiguration $$2 = SheepColorSpawnRules.getSheepColorConfiguration($$0);
        return $$2.colors().get($$1);
    }

    private static SheepColorSpawnConfiguration getSheepColorConfiguration(Holder<Biome> $$0) {
        if ($$0.is(BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS)) {
            return WARM_SPAWN_CONFIGURATION;
        }
        if ($$0.is(BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS)) {
            return COLD_SPAWN_CONFIGURATION;
        }
        return TEMPERATE_SPAWN_CONFIGURATION;
    }

    private static SheepColorProvider weighted(WeightedList<SheepColorProvider> $$0) {
        if ($$0.isEmpty()) {
            throw new IllegalArgumentException("List must be non-empty");
        }
        return $$1 -> ((SheepColorProvider)$$0.getRandomOrThrow($$1)).get($$1);
    }

    private static SheepColorProvider single(DyeColor $$0) {
        return $$1 -> $$0;
    }

    private static WeightedList.Builder<SheepColorProvider> builder() {
        return WeightedList.builder();
    }

    @FunctionalInterface
    static interface SheepColorProvider {
        public DyeColor get(RandomSource var1);
    }

    record SheepColorSpawnConfiguration(SheepColorProvider colors) {
    }
}

