package net.minecraft.core.component;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface DataComponentMap extends Iterable<TypedDataComponent<?>>, DataComponentGetter {
   DataComponentMap EMPTY = new DataComponentMap() {
      
      @Override
      public <T> T get(DataComponentType<? extends T> $$0) {
         return null;
      }

      @Override
      public Set<DataComponentType<?>> keySet() {
         return Set.of();
      }

      @Override
      public Iterator<TypedDataComponent<?>> iterator() {
         return Collections.emptyIterator();
      }
   };
   Codec<DataComponentMap> CODEC = makeCodecFromMap(DataComponentType.VALUE_MAP_CODEC);

   static Codec<DataComponentMap> makeCodec(Codec<DataComponentType<?>> $$0) {
      return makeCodecFromMap(Codec.dispatchedMap($$0, DataComponentType::codecOrThrow));
   }

   static Codec<DataComponentMap> makeCodecFromMap(Codec<Map<DataComponentType<?>, Object>> $$0) {
      return $$0.flatComapMap(DataComponentMap.Builder::buildFromMapTrusted, $$0x -> {
         int $$1 = $$0x.size();
         if ($$1 == 0) {
            return DataResult.success(Reference2ObjectMaps.emptyMap());
         } else {
            Reference2ObjectMap<DataComponentType<?>, Object> $$2 = new Reference2ObjectArrayMap($$1);

            for (TypedDataComponent<?> $$3 : $$0x) {
               if (!$$3.type().isTransient()) {
                  $$2.put($$3.type(), $$3.value());
               }
            }

            return DataResult.success($$2);
         }
      });
   }

   static DataComponentMap composite(final DataComponentMap $$0, final DataComponentMap $$1) {
      return new DataComponentMap() {
         
         @Override
         public <T> T get(DataComponentType<? extends T> $$0x) {
            T $$1x = $$1.get($$0);
            return $$1x != null ? $$1x : $$0.get($$0);
         }

         @Override
         public Set<DataComponentType<?>> keySet() {
            return Sets.union($$0.keySet(), $$1.keySet());
         }
      };
   }

   static DataComponentMap.Builder builder() {
      return new DataComponentMap.Builder();
   }

   Set<DataComponentType<?>> keySet();

   default boolean has(DataComponentType<?> $$0) {
      return this.get($$0) != null;
   }

   @Override
   default Iterator<TypedDataComponent<?>> iterator() {
      return Iterators.transform(this.keySet().iterator(), $$0 -> Objects.requireNonNull(this.getTyped($$0)));
   }

   default Stream<TypedDataComponent<?>> stream() {
      return StreamSupport.stream(Spliterators.spliterator(this.iterator(), (long)this.size(), 1345), false);
   }

   default int size() {
      return this.keySet().size();
   }

   default boolean isEmpty() {
      return this.size() == 0;
   }

   default DataComponentMap filter(final Predicate<DataComponentType<?>> $$0) {
      return new DataComponentMap() {
         
         @Override
         public <T> T get(DataComponentType<? extends T> $$0x) {
            return $$0.test($$0) ? DataComponentMap.this.get($$0) : null;
         }

         @Override
         public Set<DataComponentType<?>> keySet() {
            return Sets.filter(DataComponentMap.this.keySet(), $$0::test);
         }
      };
   }

   public static class Builder {
      private final Reference2ObjectMap<DataComponentType<?>, Object> map = new Reference2ObjectArrayMap();

      Builder() {
      }

      public <T> DataComponentMap.Builder set(DataComponentType<T> $$0, T $$1) {
         this.setUnchecked($$0, $$1);
         return this;
      }

      <T> void setUnchecked(DataComponentType<T> $$0, Object $$1) {
         if ($$1 != null) {
            this.map.put($$0, $$1);
         } else {
            this.map.remove($$0);
         }
      }

      public DataComponentMap.Builder addAll(DataComponentMap $$0) {
         for (TypedDataComponent<?> $$1 : $$0) {
            this.map.put($$1.type(), $$1.value());
         }

         return this;
      }

      public DataComponentMap build() {
         return buildFromMapTrusted(this.map);
      }

      private static DataComponentMap buildFromMapTrusted(Map<DataComponentType<?>, Object> $$0) {
         if ($$0.isEmpty()) {
            return DataComponentMap.EMPTY;
         } else {
            return $$0.size() < 8
               ? new DataComponentMap.Builder.SimpleMap(new Reference2ObjectArrayMap($$0))
               : new DataComponentMap.Builder.SimpleMap(new Reference2ObjectOpenHashMap($$0));
         }
      }

      record SimpleMap(Reference2ObjectMap<DataComponentType<?>, Object> map) implements DataComponentMap {
         
         @Override
         public <T> T get(DataComponentType<? extends T> $$0) {
            return (T)this.map.get($$0);
         }

         @Override
         public boolean has(DataComponentType<?> $$0) {
            return this.map.containsKey($$0);
         }

         @Override
         public Set<DataComponentType<?>> keySet() {
            return this.map.keySet();
         }

         @Override
         public Iterator<TypedDataComponent<?>> iterator() {
            return Iterators.transform(Reference2ObjectMaps.fastIterator(this.map), TypedDataComponent::fromEntryUnchecked);
         }

         @Override
         public int size() {
            return this.map.size();
         }

         @Override
         public String toString() {
            return this.map.toString();
         }
      }
   }
}
