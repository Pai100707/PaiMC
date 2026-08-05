package net.minecraft.core;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader.LoadResult;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public interface Registry<T> extends Keyable, net.minecraft.core.HolderLookup.RegistryLookup<T>, net.minecraft.core.IdMap<T> {
   @Override
   ResourceKey<? extends net.minecraft.core.Registry<T>> key();

   default Codec<T> byNameCodec() {
      return this.referenceHolderWithLifecycle()
         .flatComapMap(net.minecraft.core.Holder.Reference::value, $$0 -> this.safeCastToReference(this.wrapAsHolder((T)$$0)));
   }

   default Codec<net.minecraft.core.Holder<T>> holderByNameCodec() {
      return this.referenceHolderWithLifecycle().flatComapMap($$0 -> $$0, this::safeCastToReference);
   }

   private Codec<net.minecraft.core.Holder.Reference<T>> referenceHolderWithLifecycle() {
      Codec<net.minecraft.core.Holder.Reference<T>> $$0 = Identifier.CODEC
         .comapFlatMap(
            $$0x -> this.get($$0x)
               .<DataResult>map(DataResult::success)
               .orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + this.key() + ": " + $$0x)),
            $$0x -> $$0x.key().identifier()
         );
      return ExtraCodecs.overrideLifecycle(
         $$0, $$0x -> this.registrationInfo($$0x.key()).map(net.minecraft.core.RegistrationInfo::lifecycle).orElse(Lifecycle.experimental())
      );
   }

   private DataResult<net.minecraft.core.Holder.Reference<T>> safeCastToReference(net.minecraft.core.Holder<T> $$0) {
      return $$0 instanceof net.minecraft.core.Holder.Reference<T> $$1
         ? DataResult.success($$1)
         : DataResult.error(() -> "Unregistered holder in " + this.key() + ": " + $$0);
   }

   default <U> Stream<U> keys(DynamicOps<U> $$0) {
      return this.keySet().stream().map($$1 -> (U)$$0.createString($$1.toString()));
   }

   
   Identifier getKey(T var1);

   Optional<ResourceKey<T>> getResourceKey(T var1);

   @Override
   int getId(T var1);

   
   T getValue(ResourceKey<T> var1);

   
   T getValue(Identifier var1);

   Optional<net.minecraft.core.RegistrationInfo> registrationInfo(ResourceKey<T> var1);

   default Optional<T> getOptional(Identifier $$0) {
      return Optional.ofNullable(this.getValue($$0));
   }

   default Optional<T> getOptional(ResourceKey<T> $$0) {
      return Optional.ofNullable(this.getValue($$0));
   }

   Optional<net.minecraft.core.Holder.Reference<T>> getAny();

   default T getValueOrThrow(ResourceKey<T> $$0) {
      T $$1 = this.getValue($$0);
      if ($$1 == null) {
         throw new IllegalStateException("Missing key in " + this.key() + ": " + $$0);
      } else {
         return $$1;
      }
   }

   Set<Identifier> keySet();

   Set<Entry<ResourceKey<T>, T>> entrySet();

   Set<ResourceKey<T>> registryKeySet();

   Optional<net.minecraft.core.Holder.Reference<T>> getRandom(RandomSource var1);

   default Stream<T> stream() {
      return StreamSupport.stream(this.spliterator(), false);
   }

   boolean containsKey(Identifier var1);

   boolean containsKey(ResourceKey<T> var1);

   static <T> T register(net.minecraft.core.Registry<? super T> $$0, String $$1, T $$2) {
      return register($$0, Identifier.parse($$1), $$2);
   }

   static <V, T extends V> T register(net.minecraft.core.Registry<V> $$0, Identifier $$1, T $$2) {
      return register($$0, ResourceKey.create($$0.key(), $$1), $$2);
   }

   static <V, T extends V> T register(net.minecraft.core.Registry<V> $$0, ResourceKey<V> $$1, T $$2) {
      ((net.minecraft.core.WritableRegistry)$$0).register($$1, (V)$$2, net.minecraft.core.RegistrationInfo.BUILT_IN);
      return $$2;
   }

   static <R, T extends R> net.minecraft.core.Holder.Reference<T> registerForHolder(net.minecraft.core.Registry<R> $$0, ResourceKey<R> $$1, T $$2) {
      return ((net.minecraft.core.WritableRegistry)$$0).register($$1, (R)$$2, net.minecraft.core.RegistrationInfo.BUILT_IN);
   }

   static <R, T extends R> net.minecraft.core.Holder.Reference<T> registerForHolder(net.minecraft.core.Registry<R> $$0, Identifier $$1, T $$2) {
      return registerForHolder($$0, ResourceKey.create($$0.key(), $$1), $$2);
   }

   net.minecraft.core.Registry<T> freeze();

   net.minecraft.core.Holder.Reference<T> createIntrusiveHolder(T var1);

   Optional<net.minecraft.core.Holder.Reference<T>> get(int var1);

   Optional<net.minecraft.core.Holder.Reference<T>> get(Identifier var1);

   net.minecraft.core.Holder<T> wrapAsHolder(T var1);

   default Iterable<net.minecraft.core.Holder<T>> getTagOrEmpty(TagKey<T> $$0) {
      return (Iterable<net.minecraft.core.Holder<T>>)DataFixUtils.orElse(this.get($$0), List.of());
   }

   Stream<net.minecraft.core.HolderSet.Named<T>> getTags();

   default net.minecraft.core.IdMap<net.minecraft.core.Holder<T>> asHolderIdMap() {
      return new net.minecraft.core.IdMap<net.minecraft.core.Holder<T>>() {
         public int getId(net.minecraft.core.Holder<T> $$0) {
            return Registry.this.getId($$0.value());
         }

         
         public net.minecraft.core.Holder<T> byId(int $$0) {
            return (net.minecraft.core.Holder<T>)Registry.this.get($$0).orElse(null);
         }

         @Override
         public int size() {
            return Registry.this.size();
         }

         @Override
         public Iterator<net.minecraft.core.Holder<T>> iterator() {
            return Registry.this.listElements().map($$0 -> (net.minecraft.core.Holder<T>)$$0).iterator();
         }
      };
   }

   net.minecraft.core.Registry.PendingTags<T> prepareTagReload(LoadResult<T> var1);

   public interface PendingTags<T> {
      ResourceKey<? extends net.minecraft.core.Registry<? extends T>> key();

      net.minecraft.core.HolderLookup.RegistryLookup<T> lookup();

      void apply();

      int size();
   }
}
