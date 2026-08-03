package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Direct;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec<E> implements Codec<HolderSet<E>> {
   private final net.minecraft.resources.ResourceKey<? extends Registry<E>> registryKey;
   private final Codec<Holder<E>> elementCodec;
   private final Codec<List<Holder<E>>> homogenousListCodec;
   private final Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;

   private static <E> Codec<List<Holder<E>>> homogenousList(Codec<Holder<E>> $$0, boolean $$1) {
      Codec<List<Holder<E>>> $$2 = $$0.listOf().validate(ExtraCodecs.ensureHomogenous(Holder::kind));
      return $$1 ? $$2 : ExtraCodecs.compactListCodec($$0, $$2);
   }

   public static <E> Codec<HolderSet<E>> create(net.minecraft.resources.ResourceKey<? extends Registry<E>> $$0, Codec<Holder<E>> $$1, boolean $$2) {
      return new net.minecraft.resources.HolderSetCodec<>($$0, $$1, $$2);
   }

   private HolderSetCodec(net.minecraft.resources.ResourceKey<? extends Registry<E>> $$0, Codec<Holder<E>> $$1, boolean $$2) {
      this.registryKey = $$0;
      this.elementCodec = $$1;
      this.homogenousListCodec = homogenousList($$1, $$2);
      this.registryAwareCodec = Codec.either(TagKey.hashedCodec($$0), this.homogenousListCodec);
   }

   public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> $$0, T $$1) {
      if ($$0 instanceof net.minecraft.resources.RegistryOps<T> $$2) {
         Optional<HolderGetter<E>> $$3 = $$2.getter(this.registryKey);
         if ($$3.isPresent()) {
            HolderGetter<E> $$4 = $$3.get();
            return this.registryAwareCodec
               .decode($$0, $$1)
               .flatMap(
                  $$1x -> {
                     DataResult<HolderSet<E>> $$2x = (DataResult<HolderSet<E>>)((Either)$$1x.getFirst())
                        .map($$1xx -> lookupTag($$4, $$1xx), $$0xx -> DataResult.success(HolderSet.direct($$0xx)));
                     return $$2x.map($$1xx -> Pair.of($$1xx, $$1x.getSecond()));
                  }
               );
         }
      }

      return this.decodeWithoutRegistry($$0, $$1);
   }

   private static <E> DataResult<HolderSet<E>> lookupTag(HolderGetter<E> $$0, TagKey<E> $$1) {
      return $$0.get($$1)
         .<DataResult<HolderSet<E>>>map(DataResult::success)
         .orElseGet(() -> DataResult.error(() -> "Missing tag: '" + $$1.location() + "' in '" + $$1.registry().identifier() + "'"));
   }

   public <T> DataResult<T> encode(HolderSet<E> $$0, DynamicOps<T> $$1, T $$2) {
      if ($$1 instanceof net.minecraft.resources.RegistryOps<T> $$3) {
         Optional<HolderOwner<E>> $$4 = $$3.owner(this.registryKey);
         if ($$4.isPresent()) {
            if (!$$0.canSerializeIn($$4.get())) {
               return DataResult.error(() -> "HolderSet " + $$0 + " is not valid in current registry set");
            }

            return this.registryAwareCodec.encode($$0.unwrap().mapRight(List::copyOf), $$1, $$2);
         }
      }

      return this.encodeWithoutRegistry($$0, $$1, $$2);
   }

   private <T> DataResult<Pair<HolderSet<E>, T>> decodeWithoutRegistry(DynamicOps<T> $$0, T $$1) {
      return this.elementCodec.listOf().decode($$0, $$1).flatMap($$0x -> {
         List<Direct<E>> $$1x = new ArrayList<>();

         for (Holder<E> $$2 : (List)$$0x.getFirst()) {
            if (!($$2 instanceof Direct<E> $$3)) {
               return DataResult.error(() -> "Can't decode element " + $$2 + " without registry");
            }

            $$1x.add($$3);
         }

         return DataResult.success(new Pair(HolderSet.direct($$1x), $$0x.getSecond()));
      });
   }

   private <T> DataResult<T> encodeWithoutRegistry(HolderSet<E> $$0, DynamicOps<T> $$1, T $$2) {
      return this.homogenousListCodec.encode($$0.stream().toList(), $$1, $$2);
   }
}
