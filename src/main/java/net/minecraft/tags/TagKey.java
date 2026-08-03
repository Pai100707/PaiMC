package net.minecraft.tags;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record TagKey<T>(ResourceKey<? extends Registry<T>> registry, Identifier location) {
   private static final Interner<net.minecraft.tags.TagKey<?>> VALUES = Interners.newWeakInterner();

   @Deprecated
   public TagKey(ResourceKey<? extends Registry<T>> registry, Identifier location) {
      this.registry = registry;
      this.location = location;
   }

   public static <T> Codec<net.minecraft.tags.TagKey<T>> codec(ResourceKey<? extends Registry<T>> $$0) {
      return Identifier.CODEC.xmap($$1 -> create($$0, $$1), net.minecraft.tags.TagKey::location);
   }

   public static <T> Codec<net.minecraft.tags.TagKey<T>> hashedCodec(ResourceKey<? extends Registry<T>> $$0) {
      return Codec.STRING
         .comapFlatMap(
            $$1 -> $$1.startsWith("#") ? Identifier.read($$1.substring(1)).map($$1x -> create($$0, $$1x)) : DataResult.error(() -> "Not a tag id"),
            $$0x -> "#" + $$0x.location
         );
   }

   public static <T> StreamCodec<ByteBuf, net.minecraft.tags.TagKey<T>> streamCodec(ResourceKey<? extends Registry<T>> $$0) {
      return Identifier.STREAM_CODEC.map($$1 -> create($$0, $$1), net.minecraft.tags.TagKey::location);
   }

   public static <T> net.minecraft.tags.TagKey<T> create(ResourceKey<? extends Registry<T>> $$0, Identifier $$1) {
      return (net.minecraft.tags.TagKey<T>)VALUES.intern(new net.minecraft.tags.TagKey($$0, $$1));
   }

   public boolean isFor(ResourceKey<? extends Registry<?>> $$0) {
      return this.registry == $$0;
   }

   public <E> Optional<net.minecraft.tags.TagKey<E>> cast(ResourceKey<? extends Registry<E>> $$0) {
      return this.isFor($$0) ? Optional.of((net.minecraft.tags.TagKey<E>)this) : Optional.empty();
   }

   @Override
   public String toString() {
      return "TagKey[" + this.registry.identifier() + " / " + this.location + "]";
   }
}
