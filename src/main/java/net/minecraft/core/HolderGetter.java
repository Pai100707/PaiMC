package net.minecraft.core;

import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

public interface HolderGetter<T> {
   Optional<net.minecraft.core.Holder.Reference<T>> get(ResourceKey<T> var1);

   default net.minecraft.core.Holder.Reference<T> getOrThrow(ResourceKey<T> $$0) {
      return this.get($$0).orElseThrow(() -> new IllegalStateException("Missing element " + $$0));
   }

   Optional<net.minecraft.core.HolderSet.Named<T>> get(TagKey<T> var1);

   default net.minecraft.core.HolderSet.Named<T> getOrThrow(TagKey<T> $$0) {
      return this.get($$0).orElseThrow(() -> new IllegalStateException("Missing tag " + $$0));
   }

   default Optional<net.minecraft.core.Holder<T>> getRandomElementOf(TagKey<T> $$0, RandomSource $$1) {
      return this.get($$0).flatMap($$1x -> $$1x.getRandomElement($$1));
   }

   public interface Provider {
      <T> Optional<? extends net.minecraft.core.HolderGetter<T>> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> var1);

      default <T> net.minecraft.core.HolderGetter<T> lookupOrThrow(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$0) {
         return (net.minecraft.core.HolderGetter<T>)this.<T>lookup($$0)
            .orElseThrow(() -> new IllegalStateException("Registry " + $$0.identifier() + " not found"));
      }

      default <T> Optional<net.minecraft.core.Holder.Reference<T>> get(ResourceKey<T> $$0) {
         return this.lookup($$0.registryKey()).flatMap($$1 -> $$1.get($$0));
      }

      default <T> net.minecraft.core.Holder.Reference<T> getOrThrow(ResourceKey<T> $$0) {
         return this.lookup($$0.registryKey()).flatMap($$1 -> $$1.get($$0)).orElseThrow(() -> new IllegalStateException("Missing element " + $$0));
      }
   }
}
