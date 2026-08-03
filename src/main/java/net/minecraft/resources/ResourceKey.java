package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;

public class ResourceKey<T> {
   private static final ConcurrentMap<net.minecraft.resources.ResourceKey.InternKey, net.minecraft.resources.ResourceKey<?>> VALUES = new MapMaker()
      .weakValues()
      .makeMap();
   private final net.minecraft.resources.Identifier registryName;
   private final net.minecraft.resources.Identifier identifier;

   public static <T> Codec<net.minecraft.resources.ResourceKey<T>> codec(net.minecraft.resources.ResourceKey<? extends Registry<T>> $$0) {
      return net.minecraft.resources.Identifier.CODEC.xmap($$1 -> create($$0, $$1), net.minecraft.resources.ResourceKey::identifier);
   }

   public static <T> StreamCodec<ByteBuf, net.minecraft.resources.ResourceKey<T>> streamCodec(net.minecraft.resources.ResourceKey<? extends Registry<T>> $$0) {
      return net.minecraft.resources.Identifier.STREAM_CODEC.map($$1 -> create($$0, $$1), net.minecraft.resources.ResourceKey::identifier);
   }

   public static <T> net.minecraft.resources.ResourceKey<T> create(
      net.minecraft.resources.ResourceKey<? extends Registry<T>> $$0, net.minecraft.resources.Identifier $$1
   ) {
      return create($$0.identifier, $$1);
   }

   public static <T> net.minecraft.resources.ResourceKey<Registry<T>> createRegistryKey(net.minecraft.resources.Identifier $$0) {
      return create(Registries.ROOT_REGISTRY_NAME, $$0);
   }

   private static <T> net.minecraft.resources.ResourceKey<T> create(net.minecraft.resources.Identifier $$0, net.minecraft.resources.Identifier $$1) {
      return (net.minecraft.resources.ResourceKey<T>)VALUES.computeIfAbsent(
         new net.minecraft.resources.ResourceKey.InternKey($$0, $$1), $$0x -> new net.minecraft.resources.ResourceKey($$0x.registry, $$0x.identifier)
      );
   }

   private ResourceKey(net.minecraft.resources.Identifier $$0, net.minecraft.resources.Identifier $$1) {
      this.registryName = $$0;
      this.identifier = $$1;
   }

   @Override
   public String toString() {
      return "ResourceKey[" + this.registryName + " / " + this.identifier + "]";
   }

   public boolean isFor(net.minecraft.resources.ResourceKey<? extends Registry<?>> $$0) {
      return this.registryName.equals($$0.identifier());
   }

   public <E> Optional<net.minecraft.resources.ResourceKey<E>> cast(net.minecraft.resources.ResourceKey<? extends Registry<E>> $$0) {
      return this.isFor($$0) ? Optional.of((net.minecraft.resources.ResourceKey<E>)this) : Optional.empty();
   }

   public net.minecraft.resources.Identifier identifier() {
      return this.identifier;
   }

   public net.minecraft.resources.Identifier registry() {
      return this.registryName;
   }

   public net.minecraft.resources.ResourceKey<Registry<T>> registryKey() {
      return createRegistryKey(this.registryName);
   }

   record InternKey(net.minecraft.resources.Identifier registry, net.minecraft.resources.Identifier identifier) {
   }
}
