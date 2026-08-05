/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface CommandBuildContext
extends HolderLookup.Provider {
    public static CommandBuildContext simple(final HolderLookup.Provider $$0, final FeatureFlagSet $$1) {
        return new CommandBuildContext(){

            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                return $$0.listRegistryKeys();
            }

            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$02) {
                return $$0.lookup($$02).map($$1 -> $$1.filterFeatures($$1));
            }

            @Override
            public FeatureFlagSet enabledFeatures() {
                return $$1;
            }
        };
    }

    public FeatureFlagSet enabledFeatures();
}

