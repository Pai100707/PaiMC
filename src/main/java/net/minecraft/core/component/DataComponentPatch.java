package net.minecraft.core.component;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import org.jspecify.annotations.Nullable;

public final class DataComponentPatch {
   public static final DataComponentPatch EMPTY = new DataComponentPatch(Reference2ObjectMaps.emptyMap());
   public static final Codec<DataComponentPatch> CODEC = Codec.dispatchedMap(DataComponentPatch.PatchKey.CODEC, DataComponentPatch.PatchKey::valueCodec)
      .xmap($$0 -> {
         if ($$0.isEmpty()) {
            return EMPTY;
         } else {
            Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$1 = new Reference2ObjectArrayMap($$0.size());

            for (Entry<DataComponentPatch.PatchKey, ?> $$2 : $$0.entrySet()) {
               DataComponentPatch.PatchKey $$3 = $$2.getKey();
               if ($$3.removed()) {
                  $$1.put($$3.type(), Optional.empty());
               } else {
                  $$1.put($$3.type(), Optional.of($$2.getValue()));
               }
            }

            return new DataComponentPatch($$1);
         }
      }, $$0 -> {
         Reference2ObjectMap<DataComponentPatch.PatchKey, Object> $$1 = new Reference2ObjectArrayMap($$0.map.size());
         ObjectIterator var2 = Reference2ObjectMaps.fastIterable($$0.map).iterator();

         while (var2.hasNext()) {
            Entry<DataComponentType<?>, Optional<?>> $$2 = (Entry<DataComponentType<?>, Optional<?>>)var2.next();
            DataComponentType<?> $$3 = $$2.getKey();
            if (!$$3.isTransient()) {
               Optional<?> $$4 = $$2.getValue();
               if ($$4.isPresent()) {
                  $$1.put(new DataComponentPatch.PatchKey($$3, false), $$4.get());
               } else {
                  $$1.put(new DataComponentPatch.PatchKey($$3, true), Unit.INSTANCE);
               }
            }
         }

         return $$1;
      });
   public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> STREAM_CODEC = createStreamCodec(new DataComponentPatch.CodecGetter() {
      @Override
      public <T> StreamCodec<RegistryFriendlyByteBuf, T> apply(DataComponentType<T> $$0) {
         return $$0.streamCodec().cast();
      }
   });
   public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> DELIMITED_STREAM_CODEC = createStreamCodec(
      new DataComponentPatch.CodecGetter() {
         @Override
         public <T> StreamCodec<RegistryFriendlyByteBuf, T> apply(DataComponentType<T> $$0) {
            StreamCodec<RegistryFriendlyByteBuf, T> $$1 = $$0.streamCodec().cast();
            return $$1.apply(ByteBufCodecs.registryFriendlyLengthPrefixed(Integer.MAX_VALUE));
         }
      }
   );
   private static final String REMOVED_PREFIX = "!";
   final Reference2ObjectMap<DataComponentType<?>, Optional<?>> map;

   private static StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> createStreamCodec(final DataComponentPatch.CodecGetter $$0) {
      return new StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch>() {
         public DataComponentPatch decode(RegistryFriendlyByteBuf $$0x) {
            int $$1 = $$0.readVarInt();
            int $$2 = $$0.readVarInt();
            if ($$1 == 0 && $$2 == 0) {
               return DataComponentPatch.EMPTY;
            } else {
               int $$3 = $$1 + $$2;
               Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$4 = new Reference2ObjectArrayMap(Math.min($$3, 65536));

               for (int $$5 = 0; $$5 < $$1; $$5++) {
                  DataComponentType<?> $$6 = (DataComponentType<?>)DataComponentType.STREAM_CODEC.decode($$0);
                  Object $$7 = $$0.apply($$6).decode($$0);
                  $$4.put($$6, Optional.of($$7));
               }

               for (int $$8 = 0; $$8 < $$2; $$8++) {
                  DataComponentType<?> $$9 = (DataComponentType<?>)DataComponentType.STREAM_CODEC.decode($$0);
                  $$4.put($$9, Optional.empty());
               }

               return new DataComponentPatch($$4);
            }
         }

         public void encode(RegistryFriendlyByteBuf $$0x, DataComponentPatch $$1) {
            if ($$1.isEmpty()) {
               $$0.writeVarInt(0);
               $$0.writeVarInt(0);
            } else {
               int $$2 = 0;
               int $$3 = 0;
               ObjectIterator var5 = Reference2ObjectMaps.fastIterable($$1.map).iterator();

               while (var5.hasNext()) {
                  it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> $$4 = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>>)var5.next();
                  if (((Optional)$$4.getValue()).isPresent()) {
                     $$2++;
                  } else {
                     $$3++;
                  }
               }

               $$0.writeVarInt($$2);
               $$0.writeVarInt($$3);
               var5 = Reference2ObjectMaps.fastIterable($$1.map).iterator();

               while (var5.hasNext()) {
                  it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> $$5 = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>>)var5.next();
                  Optional<?> $$6 = (Optional<?>)$$5.getValue();
                  if ($$6.isPresent()) {
                     DataComponentType<?> $$7 = (DataComponentType<?>)$$5.getKey();
                     DataComponentType.STREAM_CODEC.encode($$0, $$7);
                     this.encodeComponent($$0, $$7, $$6.get());
                  }
               }

               var5 = Reference2ObjectMaps.fastIterable($$1.map).iterator();

               while (var5.hasNext()) {
                  it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> $$8 = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>>)var5.next();
                  if (((Optional)$$8.getValue()).isEmpty()) {
                     DataComponentType<?> $$9 = (DataComponentType<?>)$$8.getKey();
                     DataComponentType.STREAM_CODEC.encode($$0, $$9);
                  }
               }
            }
         }

         private <T> void encodeComponent(RegistryFriendlyByteBuf $$0x, DataComponentType<T> $$1, Object $$2) {
            $$0.apply($$1).encode($$0, $$2);
         }
      };
   }

   DataComponentPatch(Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$0) {
      this.map = $$0;
   }

   public static DataComponentPatch.Builder builder() {
      return new DataComponentPatch.Builder();
   }

   @Nullable
   public <T> Optional<? extends T> get(DataComponentType<? extends T> $$0) {
      return (Optional<? extends T>)this.map.get($$0);
   }

   public Set<Entry<DataComponentType<?>, Optional<?>>> entrySet() {
      return this.map.entrySet();
   }

   public int size() {
      return this.map.size();
   }

   public DataComponentPatch forget(Predicate<DataComponentType<?>> $$0) {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$1 = new Reference2ObjectArrayMap(this.map);
         $$1.keySet().removeIf($$0);
         return $$1.isEmpty() ? EMPTY : new DataComponentPatch($$1);
      }
   }

   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   public DataComponentPatch.SplitResult split() {
      if (this.isEmpty()) {
         return DataComponentPatch.SplitResult.EMPTY;
      } else {
         DataComponentMap.Builder $$0 = DataComponentMap.builder();
         Set<DataComponentType<?>> $$1 = Sets.newIdentityHashSet();
         this.map.forEach(($$2, $$3) -> {
            if ($$3.isPresent()) {
               $$0.setUnchecked($$2, $$3.get());
            } else {
               $$1.add($$2);
            }
         });
         return new DataComponentPatch.SplitResult($$0.build(), $$1);
      }
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof DataComponentPatch $$1 && this.map.equals($$1.map);
   }

   @Override
   public int hashCode() {
      return this.map.hashCode();
   }

   @Override
   public String toString() {
      return toString(this.map);
   }

   static String toString(Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$0) {
      StringBuilder $$1 = new StringBuilder();
      $$1.append('{');
      boolean $$2 = true;
      ObjectIterator var3 = Reference2ObjectMaps.fastIterable($$0).iterator();

      while (var3.hasNext()) {
         Entry<DataComponentType<?>, Optional<?>> $$3 = (Entry<DataComponentType<?>, Optional<?>>)var3.next();
         if ($$2) {
            $$2 = false;
         } else {
            $$1.append(", ");
         }

         Optional<?> $$4 = $$3.getValue();
         if ($$4.isPresent()) {
            $$1.append($$3.getKey());
            $$1.append("=>");
            $$1.append($$4.get());
         } else {
            $$1.append("!");
            $$1.append($$3.getKey());
         }
      }

      $$1.append('}');
      return $$1.toString();
   }

   public static class Builder {
      private final Reference2ObjectMap<DataComponentType<?>, Optional<?>> map = new Reference2ObjectArrayMap();

      Builder() {
      }

      public <T> DataComponentPatch.Builder set(DataComponentType<T> $$0, T $$1) {
         this.map.put($$0, Optional.of($$1));
         return this;
      }

      public <T> DataComponentPatch.Builder remove(DataComponentType<T> $$0) {
         this.map.put($$0, Optional.empty());
         return this;
      }

      public <T> DataComponentPatch.Builder set(TypedDataComponent<T> $$0) {
         return this.set($$0.type(), $$0.value());
      }

      public DataComponentPatch build() {
         return this.map.isEmpty() ? DataComponentPatch.EMPTY : new DataComponentPatch(this.map);
      }
   }

   @FunctionalInterface
   interface CodecGetter {
      <T> StreamCodec<? super RegistryFriendlyByteBuf, T> apply(DataComponentType<T> var1);
   }

   record PatchKey(DataComponentType<?> type, boolean removed) {
      public static final Codec<DataComponentPatch.PatchKey> CODEC = Codec.STRING
         .flatXmap(
            $$0 -> {
               boolean $$1 = $$0.startsWith("!");
               if ($$1) {
                  $$0 = $$0.substring("!".length());
               }

               Identifier $$2 = Identifier.tryParse($$0);
               DataComponentType<?> $$3 = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue($$2);
               if ($$3 == null) {
                  return DataResult.error(() -> "No component with type: '" + $$2 + "'");
               } else {
                  return $$3.isTransient()
                     ? DataResult.error(() -> "'" + $$2 + "' is not a persistent component")
                     : DataResult.success(new DataComponentPatch.PatchKey($$3, $$1));
               }
            },
            $$0 -> {
               DataComponentType<?> $$1 = $$0.type();
               Identifier $$2 = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey($$1);
               return $$2 == null ? DataResult.error(() -> "Unregistered component: " + $$1) : DataResult.success($$0.removed() ? "!" + $$2 : $$2.toString());
            }
         );

      public Codec<?> valueCodec() {
         return this.removed ? Codec.EMPTY.codec() : this.type.codecOrThrow();
      }
   }

   public record SplitResult(DataComponentMap added, Set<DataComponentType<?>> removed) {
      public static final DataComponentPatch.SplitResult EMPTY = new DataComponentPatch.SplitResult(DataComponentMap.EMPTY, Set.of());
   }
}
