/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.nautilus;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.TemperatureVariants;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.biome.Biome;

public class ZombieNautilusVariants {
    public static final ResourceKey<ZombieNautilusVariant> TEMPERATE = ZombieNautilusVariants.createKey(TemperatureVariants.TEMPERATE);
    public static final ResourceKey<ZombieNautilusVariant> WARM = ZombieNautilusVariants.createKey(TemperatureVariants.WARM);
    public static final ResourceKey<ZombieNautilusVariant> DEFAULT = TEMPERATE;

    private static ResourceKey<ZombieNautilusVariant> createKey(Identifier $$0) {
        return ResourceKey.create(Registries.ZOMBIE_NAUTILUS_VARIANT, $$0);
    }

    public static void bootstrap(BootstrapContext<ZombieNautilusVariant> $$0) {
        ZombieNautilusVariants.register($$0, TEMPERATE, ZombieNautilusVariant.ModelType.NORMAL, "zombie_nautilus", SpawnPrioritySelectors.fallback(0));
        ZombieNautilusVariants.register($$0, WARM, ZombieNautilusVariant.ModelType.WARM, "zombie_nautilus_coral", BiomeTags.SPAWNS_CORAL_VARIANT_ZOMBIE_NAUTILUS);
    }

    private static void register(BootstrapContext<ZombieNautilusVariant> $$0, ResourceKey<ZombieNautilusVariant> $$1, ZombieNautilusVariant.ModelType $$2, String $$3, TagKey<Biome> $$4) {
        HolderSet.Named<Biome> $$5 = $$0.lookup(Registries.BIOME).getOrThrow($$4);
        ZombieNautilusVariants.register($$0, $$1, $$2, $$3, SpawnPrioritySelectors.single(new BiomeCheck($$5), 1));
    }

    private static void register(BootstrapContext<ZombieNautilusVariant> $$0, ResourceKey<ZombieNautilusVariant> $$1, ZombieNautilusVariant.ModelType $$2, String $$3, SpawnPrioritySelectors $$4) {
        Identifier $$5 = Identifier.withDefaultNamespace("entity/nautilus/" + $$3);
        $$0.register($$1, new ZombieNautilusVariant(new ModelAndTexture<ZombieNautilusVariant.ModelType>($$2, $$5), $$4));
    }
}

