package net.minecraft.commands;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface CommandBuildContext extends Provider {
   static net.minecraft.commands.CommandBuildContext simple(final Provider $$0, final FeatureFlagSet $$1) {
      return new net.minecraft.commands.CommandBuildContext() {
         public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
            return $$0.listRegistryKeys();
         }

         public <T> Optional<RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$0x) {
            return $$0.lookup($$0).map($$1xx -> $$1xx.filterFeatures($$1));
         }

         @Override
         public FeatureFlagSet enabledFeatures() {
            return $$1;
         }
      };
   }

   FeatureFlagSet enabledFeatures();
}
