package net.minecraft.world.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record EitherHolder<T>(Either<Holder<T>, ResourceKey<T>> contents) {
   public EitherHolder(Holder<T> $$0) {
      this(Either.left($$0));
   }

   public EitherHolder(ResourceKey<T> $$0) {
      this(Either.right($$0));
   }

   public static <T> Codec<net.minecraft.world.item.EitherHolder<T>> codec(ResourceKey<Registry<T>> $$0, Codec<Holder<T>> $$1) {
      return Codec.either($$1, ResourceKey.codec($$0).comapFlatMap($$0x -> DataResult.error(() -> "Cannot parse as key without registry"), Function.identity()))
         .xmap(net.minecraft.world.item.EitherHolder::new, net.minecraft.world.item.EitherHolder::contents);
   }

   public static <T> StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.EitherHolder<T>> streamCodec(
      ResourceKey<Registry<T>> $$0, StreamCodec<RegistryFriendlyByteBuf, Holder<T>> $$1
   ) {
      return StreamCodec.composite(
         ByteBufCodecs.either($$1, ResourceKey.streamCodec($$0)), net.minecraft.world.item.EitherHolder::contents, net.minecraft.world.item.EitherHolder::new
      );
   }

   public Optional<T> unwrap(Registry<T> $$0) {
      return (Optional<T>)this.contents.map($$0x -> Optional.of($$0x.value()), $$0::getOptional);
   }

   public Optional<Holder<T>> unwrap(Provider $$0) {
      return (Optional<Holder<T>>)this.contents.map(Optional::of, $$1 -> $$0.get($$1).map($$0xx -> $$0xx));
   }

   public Optional<ResourceKey<T>> key() {
      return (Optional<ResourceKey<T>>)this.contents.map(Holder::unwrapKey, Optional::of);
   }
}
