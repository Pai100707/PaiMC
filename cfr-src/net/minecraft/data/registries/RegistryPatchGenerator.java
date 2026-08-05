/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 */
package net.minecraft.data.registries;

import com.mojang.datafixers.DataFixUtils;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Cloner;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RegistryPatchGenerator {
    public static CompletableFuture<RegistrySetBuilder.PatchedRegistries> createLookup(CompletableFuture<HolderLookup.Provider> $$0, RegistrySetBuilder $$1) {
        return $$0.thenApply($$12 -> {
            RegistryAccess.Frozen $$2 = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
            Cloner.Factory $$3 = new Cloner.Factory();
            RegistryDataLoader.WORLDGEN_REGISTRIES.forEach($$1 -> $$1.runWithArguments($$3::addCodec));
            RegistrySetBuilder.PatchedRegistries $$4 = $$1.buildPatch($$2, (HolderLookup.Provider)$$12, $$3);
            HolderLookup.Provider $$5 = $$4.full();
            Optional<HolderLookup.RegistryLookup<Biome>> $$6 = $$5.lookup(Registries.BIOME);
            Optional<HolderLookup.RegistryLookup<PlacedFeature>> $$7 = $$5.lookup(Registries.PLACED_FEATURE);
            if ($$6.isPresent() || $$7.isPresent()) {
                VanillaRegistries.validateThatAllBiomeFeaturesHaveBiomeFilter((HolderGetter)DataFixUtils.orElseGet($$7, () -> $$12.lookupOrThrow(Registries.PLACED_FEATURE)), (HolderLookup)DataFixUtils.orElseGet($$6, () -> $$12.lookupOrThrow(Registries.BIOME)));
            }
            return $$4;
        });
    }
}

