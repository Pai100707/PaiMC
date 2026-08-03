package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;

public final class RegistryFileCodec<E> implements Codec<Holder<E>> {
   private final net.minecraft.resources.ResourceKey<? extends Registry<E>> registryKey;
   private final Codec<E> elementCodec;
   private final boolean allowInline;

   public static <E> net.minecraft.resources.RegistryFileCodec<E> create(net.minecraft.resources.ResourceKey<? extends Registry<E>> $$0, Codec<E> $$1) {
      return create($$0, $$1, true);
   }

   public static <E> net.minecraft.resources.RegistryFileCodec<E> create(
      net.minecraft.resources.ResourceKey<? extends Registry<E>> $$0, Codec<E> $$1, boolean $$2
   ) {
      return new net.minecraft.resources.RegistryFileCodec<>($$0, $$1, $$2);
   }

   private RegistryFileCodec(net.minecraft.resources.ResourceKey<? extends Registry<E>> $$0, Codec<E> $$1, boolean $$2) {
      this.registryKey = $$0;
      this.elementCodec = $$1;
      this.allowInline = $$2;
   }

   public <T> DataResult<T> encode(Holder<E> $$0, DynamicOps<T> $$1, T $$2) {
      if ($$1 instanceof net.minecraft.resources.RegistryOps<?> $$3) {
         Optional<HolderOwner<E>> $$4 = $$3.owner(this.registryKey);
         if ($$4.isPresent()) {
            if (!$$0.canSerializeIn($$4.get())) {
               return DataResult.error(() -> "Element " + $$0 + " is not valid in current registry set");
            }

            return (DataResult<T>)$$0.unwrap()
               .map($$2x -> net.minecraft.resources.Identifier.CODEC.encode($$2x.identifier(), $$1, $$2), $$2x -> this.elementCodec.encode($$2x, $$1, $$2));
         }
      }

      return this.elementCodec.encode($$0.value(), $$1, $$2);
   }

   public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> $$0, T $$1) {
      if ($$0 instanceof net.minecraft.resources.RegistryOps<?> $$2) {
         Optional<HolderGetter<E>> $$3 = $$2.getter(this.registryKey);
         if ($$3.isEmpty()) {
            return DataResult.error(() -> "Registry does not exist: " + this.registryKey);
         } else {
            HolderGetter<E> $$4 = $$3.get();
            DataResult<Pair<net.minecraft.resources.Identifier, T>> $$5 = net.minecraft.resources.Identifier.CODEC.decode($$0, $$1);
            if ($$5.result().isEmpty()) {
               return !this.allowInline
                  ? DataResult.error(() -> "Inline definitions not allowed here")
                  : this.elementCodec.decode($$0, $$1).map($$0x -> $$0x.mapFirst(Holder::direct));
            } else {
               Pair<net.minecraft.resources.Identifier, T> $$6 = (Pair<net.minecraft.resources.Identifier, T>)$$5.result().get();
               net.minecraft.resources.ResourceKey<E> $$7 = net.minecraft.resources.ResourceKey.create(
                  this.registryKey, (net.minecraft.resources.Identifier)$$6.getFirst()
               );
               return $$4.get($$7)
                  .<DataResult>map(DataResult::success)
                  .orElseGet(() -> DataResult.error(() -> "Failed to get element " + $$7))
                  .map($$1x -> Pair.of($$1x, $$6.getSecond()))
                  .setLifecycle(Lifecycle.stable());
            }
         }
      } else {
         return this.elementCodec.decode($$0, $$1).map($$0x -> $$0x.mapFirst(Holder::direct));
      }
   }

   @Override
   public String toString() {
      return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
   }
}
