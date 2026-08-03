package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps.RegistryInfo;
import net.minecraft.resources.RegistryOps.RegistryInfoLookup;
import net.minecraft.tags.TagKey;

public class PlaceholderLookupProvider implements Provider {
   final net.minecraft.core.HolderLookup.Provider context;
   final net.minecraft.util.PlaceholderLookupProvider.UniversalLookup lookup = new net.minecraft.util.PlaceholderLookupProvider.UniversalLookup();
   final Map<ResourceKey<Object>, Reference<Object>> holders = new HashMap<>();
   final Map<TagKey<Object>, Named<Object>> holderSets = new HashMap<>();

   public PlaceholderLookupProvider(net.minecraft.core.HolderLookup.Provider $$0) {
      this.context = $$0;
   }

   public <T> Optional<? extends HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$0) {
      return Optional.of(this.lookup.castAsLookup());
   }

   public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> $$0) {
      return RegistryOps.create(
         $$0,
         new RegistryInfoLookup() {
            public <T> Optional<RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$0) {
               return PlaceholderLookupProvider.this.context
                  .lookup($$0)
                  .<RegistryInfo<T>>map(RegistryInfo::fromRegistryLookup)
                  .or(
                     () -> Optional.of(
                        new RegistryInfo(
                           PlaceholderLookupProvider.this.lookup.castAsOwner(), PlaceholderLookupProvider.this.lookup.castAsLookup(), Lifecycle.experimental()
                        )
                     )
                  );
            }
         }
      );
   }

   public net.minecraft.util.RegistryContextSwapper createSwapper() {
      return new net.minecraft.util.RegistryContextSwapper() {
         @Override
         public <T> DataResult<T> swapTo(Codec<T> $$0, T $$1, net.minecraft.core.HolderLookup.Provider $$2) {
            return $$0.encodeStart(PlaceholderLookupProvider.this.createSerializationContext(JavaOps.INSTANCE), $$1)
               .flatMap($$2x -> $$0.parse($$2.createSerializationContext(JavaOps.INSTANCE), $$2x));
         }
      };
   }

   public boolean hasRegisteredPlaceholders() {
      return !this.holders.isEmpty() || !this.holderSets.isEmpty();
   }

   class UniversalLookup implements HolderGetter<Object>, HolderOwner<Object> {
      public Optional<Reference<Object>> get(ResourceKey<Object> $$0) {
         return Optional.of(this.getOrCreate($$0));
      }

      public Reference<Object> getOrThrow(ResourceKey<Object> $$0) {
         return this.getOrCreate($$0);
      }

      private Reference<Object> getOrCreate(ResourceKey<Object> $$0) {
         return PlaceholderLookupProvider.this.holders.computeIfAbsent($$0, $$0x -> Reference.createStandAlone(this, $$0x));
      }

      public Optional<Named<Object>> get(TagKey<Object> $$0) {
         return Optional.of(this.getOrCreate($$0));
      }

      public Named<Object> getOrThrow(TagKey<Object> $$0) {
         return this.getOrCreate($$0);
      }

      private Named<Object> getOrCreate(TagKey<Object> $$0) {
         return PlaceholderLookupProvider.this.holderSets.computeIfAbsent($$0, $$0x -> HolderSet.emptyNamed(this, $$0x));
      }

      public <T> HolderGetter<T> castAsLookup() {
         return this;
      }

      public <T> HolderOwner<T> castAsOwner() {
         return this;
      }
   }
}
