package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface HolderLookup<T> extends net.minecraft.core.HolderGetter<T> {
   Stream<net.minecraft.core.Holder.Reference<T>> listElements();

   default Stream<ResourceKey<T>> listElementIds() {
      return this.listElements().map(net.minecraft.core.Holder.Reference::key);
   }

   Stream<net.minecraft.core.HolderSet.Named<T>> listTags();

   default Stream<TagKey<T>> listTagIds() {
      return this.listTags().map(net.minecraft.core.HolderSet.Named::key);
   }

   public interface Provider extends net.minecraft.core.HolderGetter.Provider {
      Stream<ResourceKey<? extends net.minecraft.core.Registry<?>>> listRegistryKeys();

      default Stream<net.minecraft.core.HolderLookup.RegistryLookup<?>> listRegistries() {
         return this.listRegistryKeys().map(this::lookupOrThrow);
      }

      @Override
      <T> Optional<? extends net.minecraft.core.HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> var1);

      default <T> net.minecraft.core.HolderLookup.RegistryLookup<T> lookupOrThrow(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$0) {
         return (net.minecraft.core.HolderLookup.RegistryLookup<?>)this.lookup($$0)
            .orElseThrow(() -> new IllegalStateException("Registry " + $$0.identifier() + " not found"));
      }

      default <V> RegistryOps<V> createSerializationContext(DynamicOps<V> $$0) {
         return RegistryOps.create($$0, this);
      }

      static net.minecraft.core.HolderLookup.Provider create(Stream<net.minecraft.core.HolderLookup.RegistryLookup<?>> $$0) {
         final Map<ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.core.HolderLookup.RegistryLookup<?>> $$1 = $$0.collect(
            Collectors.toUnmodifiableMap(net.minecraft.core.HolderLookup.RegistryLookup::key, $$0x -> $$0x)
         );
         return new net.minecraft.core.HolderLookup.Provider() {
            @Override
            public Stream<ResourceKey<? extends net.minecraft.core.Registry<?>>> listRegistryKeys() {
               return $$1.keySet().stream();
            }

            @Override
            public <T> Optional<net.minecraft.core.HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$0) {
               return Optional.ofNullable((net.minecraft.core.HolderLookup.RegistryLookup<T>)$$1.get($$0));
            }
         };
      }

      default Lifecycle allRegistriesLifecycle() {
         return this.listRegistries().map(net.minecraft.core.HolderLookup.RegistryLookup::registryLifecycle).reduce(Lifecycle.stable(), Lifecycle::add);
      }
   }

   public interface RegistryLookup<T> extends net.minecraft.core.HolderLookup<T>, net.minecraft.core.HolderOwner<T> {
      ResourceKey<? extends net.minecraft.core.Registry<? extends T>> key();

      Lifecycle registryLifecycle();

      default net.minecraft.core.HolderLookup.RegistryLookup<T> filterFeatures(FeatureFlagSet $$0) {
         return FeatureElement.FILTERED_REGISTRIES.contains(this.key()) ? this.filterElements($$1 -> ((FeatureElement)$$1).isEnabled($$0)) : this;
      }

      default net.minecraft.core.HolderLookup.RegistryLookup<T> filterElements(final Predicate<T> $$0) {
         return new net.minecraft.core.HolderLookup.RegistryLookup.Delegate<T>() {
            @Override
            public net.minecraft.core.HolderLookup.RegistryLookup<T> parent() {
               return RegistryLookup.this;
            }

            @Override
            public Optional<net.minecraft.core.Holder.Reference<T>> get(ResourceKey<T> $$0x) {
               return this.parent().get($$0).filter($$1 -> $$0.test($$1.value()));
            }

            @Override
            public Stream<net.minecraft.core.Holder.Reference<T>> listElements() {
               return this.parent().listElements().filter($$1 -> $$0.test($$1.value()));
            }
         };
      }

      public interface Delegate<T> extends net.minecraft.core.HolderLookup.RegistryLookup<T> {
         net.minecraft.core.HolderLookup.RegistryLookup<T> parent();

         @Override
         default ResourceKey<? extends net.minecraft.core.Registry<? extends T>> key() {
            return this.parent().key();
         }

         @Override
         default Lifecycle registryLifecycle() {
            return this.parent().registryLifecycle();
         }

         @Override
         default Optional<net.minecraft.core.Holder.Reference<T>> get(ResourceKey<T> $$0) {
            return this.parent().get($$0);
         }

         @Override
         default Stream<net.minecraft.core.Holder.Reference<T>> listElements() {
            return this.parent().listElements();
         }

         @Override
         default Optional<net.minecraft.core.HolderSet.Named<T>> get(TagKey<T> $$0) {
            return this.parent().get($$0);
         }

         @Override
         default Stream<net.minecraft.core.HolderSet.Named<T>> listTags() {
            return this.parent().listTags();
         }
      }
   }
}
