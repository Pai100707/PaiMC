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

public final class RegistryFixedCodec<E> implements Codec<Holder<E>> {
   private final net.minecraft.resources.ResourceKey<? extends Registry<E>> registryKey;

   public static <E> net.minecraft.resources.RegistryFixedCodec<E> create(net.minecraft.resources.ResourceKey<? extends Registry<E>> $$0) {
      return new net.minecraft.resources.RegistryFixedCodec<>($$0);
   }

   private RegistryFixedCodec(net.minecraft.resources.ResourceKey<? extends Registry<E>> $$0) {
      this.registryKey = $$0;
   }

   public <T> DataResult<T> encode(Holder<E> $$0, DynamicOps<T> $$1, T $$2) {
      if ($$1 instanceof net.minecraft.resources.RegistryOps<?> $$3) {
         Optional<HolderOwner<E>> $$4 = $$3.owner(this.registryKey);
         if ($$4.isPresent()) {
            if (!$$0.canSerializeIn($$4.get())) {
               return DataResult.error(() -> "Element " + $$0 + " is not valid in current registry set");
            }

            return (DataResult<T>)$$0.unwrap()
               .map(
                  $$2x -> net.minecraft.resources.Identifier.CODEC.encode($$2x.identifier(), $$1, $$2),
                  $$0x -> DataResult.error(() -> "Elements from registry " + this.registryKey + " can't be serialized to a value")
               );
         }
      }

      return DataResult.error(() -> "Can't access registry " + this.registryKey);
   }

   public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> $$0, T $$1) {
      if ($$0 instanceof net.minecraft.resources.RegistryOps<?> $$2) {
         Optional<HolderGetter<E>> $$3 = $$2.getter(this.registryKey);
         if ($$3.isPresent()) {
            return net.minecraft.resources.Identifier.CODEC
               .decode($$0, $$1)
               .flatMap(
                  $$1x -> {
                     net.minecraft.resources.Identifier $$2x = (net.minecraft.resources.Identifier)$$1x.getFirst();
                     return $$3.get()
                        .get(net.minecraft.resources.ResourceKey.create(this.registryKey, $$2x))
                        .<DataResult>map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Failed to get element " + $$2x))
                        .map($$1xx -> Pair.of($$1xx, $$1x.getSecond()))
                        .setLifecycle(Lifecycle.stable());
                  }
               );
         }
      }

      return DataResult.error(() -> "Can't access registry " + this.registryKey);
   }

   @Override
   public String toString() {
      return "RegistryFixedCodec[" + this.registryKey + "]";
   }
}
