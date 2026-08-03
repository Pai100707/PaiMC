package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface RegistryAccess extends net.minecraft.core.HolderLookup.Provider {
   Logger LOGGER = LogUtils.getLogger();
   net.minecraft.core.RegistryAccess.Frozen EMPTY = new net.minecraft.core.RegistryAccess.ImmutableRegistryAccess(Map.of()).freeze();

   @Override
   <E> Optional<net.minecraft.core.Registry<E>> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends E>> var1);

   default <E> net.minecraft.core.Registry<E> lookupOrThrow(ResourceKey<? extends net.minecraft.core.Registry<? extends E>> $$0) {
      return this.<E>lookup($$0).orElseThrow(() -> new IllegalStateException("Missing registry: " + $$0));
   }

   Stream<net.minecraft.core.RegistryAccess.RegistryEntry<?>> registries();

   @Override
   default Stream<ResourceKey<? extends net.minecraft.core.Registry<?>>> listRegistryKeys() {
      return this.registries().map($$0 -> $$0.key);
   }

   static net.minecraft.core.RegistryAccess.Frozen fromRegistryOfRegistries(final net.minecraft.core.Registry<? extends net.minecraft.core.Registry<?>> $$0) {
      return new net.minecraft.core.RegistryAccess.Frozen() {
         @Override
         public <T> Optional<net.minecraft.core.Registry<T>> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$0x) {
            net.minecraft.core.Registry<net.minecraft.core.Registry<T>> $$1 = (net.minecraft.core.Registry<net.minecraft.core.Registry<T>>)$$0;
            return $$1.getOptional((ResourceKey<net.minecraft.core.Registry<T>>)$$0);
         }

         @Override
         public Stream<net.minecraft.core.RegistryAccess.RegistryEntry<?>> registries() {
            return $$0.entrySet().stream().map(net.minecraft.core.RegistryAccess.RegistryEntry::fromMapEntry);
         }

         @Override
         public net.minecraft.core.RegistryAccess.Frozen freeze() {
            return this;
         }
      };
   }

   default net.minecraft.core.RegistryAccess.Frozen freeze() {
      class FrozenAccess extends net.minecraft.core.RegistryAccess.ImmutableRegistryAccess implements net.minecraft.core.RegistryAccess.Frozen {
         protected FrozenAccess(final Stream<net.minecraft.core.RegistryAccess.RegistryEntry<?>> $$1) {
            super($$1);
         }
      }

      return new FrozenAccess(this.registries().map(net.minecraft.core.RegistryAccess.RegistryEntry::freeze));
   }

   public interface Frozen extends net.minecraft.core.RegistryAccess {
   }

   public static class ImmutableRegistryAccess implements net.minecraft.core.RegistryAccess {
      private final Map<? extends ResourceKey<? extends net.minecraft.core.Registry<?>>, ? extends net.minecraft.core.Registry<?>> registries;

      public ImmutableRegistryAccess(List<? extends net.minecraft.core.Registry<?>> $$0) {
         this.registries = $$0.stream().collect(Collectors.toUnmodifiableMap(net.minecraft.core.Registry::key, $$0x -> $$0x));
      }

      public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends net.minecraft.core.Registry<?>>, ? extends net.minecraft.core.Registry<?>> $$0) {
         this.registries = Map.copyOf($$0);
      }

      public ImmutableRegistryAccess(Stream<net.minecraft.core.RegistryAccess.RegistryEntry<?>> $$0) {
         this.registries = $$0.collect(
            ImmutableMap.toImmutableMap(net.minecraft.core.RegistryAccess.RegistryEntry::key, net.minecraft.core.RegistryAccess.RegistryEntry::value)
         );
      }

      @Override
      public <E> Optional<net.minecraft.core.Registry<E>> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends E>> $$0) {
         return Optional.ofNullable(this.registries.get($$0)).map($$0x -> $$0x);
      }

      @Override
      public Stream<net.minecraft.core.RegistryAccess.RegistryEntry<?>> registries() {
         return this.registries.entrySet().stream().map(net.minecraft.core.RegistryAccess.RegistryEntry::fromMapEntry);
      }
   }

   public record RegistryEntry<T>(ResourceKey<? extends net.minecraft.core.Registry<T>> key, net.minecraft.core.Registry<T> value) {

      private static <T, R extends net.minecraft.core.Registry<? extends T>> net.minecraft.core.RegistryAccess.RegistryEntry<T> fromMapEntry(
         Entry<? extends ResourceKey<? extends net.minecraft.core.Registry<?>>, R> $$0
      ) {
         return fromUntyped((ResourceKey<? extends net.minecraft.core.Registry<?>>)$$0.getKey(), $$0.getValue());
      }

      private static <T> net.minecraft.core.RegistryAccess.RegistryEntry<T> fromUntyped(
         ResourceKey<? extends net.minecraft.core.Registry<?>> $$0, net.minecraft.core.Registry<?> $$1
      ) {
         return new net.minecraft.core.RegistryAccess.RegistryEntry<>(
            (ResourceKey<? extends net.minecraft.core.Registry<T>>)$$0, (net.minecraft.core.Registry<T>)$$1
         );
      }

      private net.minecraft.core.RegistryAccess.RegistryEntry<T> freeze() {
         return new net.minecraft.core.RegistryAccess.RegistryEntry<>(this.key, this.value.freeze());
      }
   }
}
