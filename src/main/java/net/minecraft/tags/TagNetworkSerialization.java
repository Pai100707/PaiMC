package net.minecraft.tags;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.Holder.Kind;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;

public class TagNetworkSerialization {
   public static Map<ResourceKey<? extends Registry<?>>, net.minecraft.tags.TagNetworkSerialization.NetworkPayload> serializeTagsToNetwork(
      LayeredRegistryAccess<RegistryLayer> $$0
   ) {
      return RegistrySynchronization.networkSafeRegistries($$0)
         .map($$0x -> Pair.of($$0x.key(), serializeToNetwork($$0x.value())))
         .filter($$0x -> !((net.minecraft.tags.TagNetworkSerialization.NetworkPayload)$$0x.getSecond()).isEmpty())
         .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
   }

   private static <T> net.minecraft.tags.TagNetworkSerialization.NetworkPayload serializeToNetwork(Registry<T> $$0) {
      Map<Identifier, IntList> $$1 = new HashMap<>();
      $$0.getTags().forEach($$2 -> {
         IntList $$3 = new IntArrayList($$2.size());

         for (Holder<T> $$4 : $$2) {
            if ($$4.kind() != Kind.REFERENCE) {
               throw new IllegalStateException("Can't serialize unregistered value " + $$4);
            }

            $$3.add($$0.getId($$4.value()));
         }

         $$1.put($$2.key().location(), $$3);
      });
      return new net.minecraft.tags.TagNetworkSerialization.NetworkPayload($$1);
   }

   static <T> net.minecraft.tags.TagLoader.LoadResult<T> deserializeTagsFromNetwork(
      Registry<T> $$0, net.minecraft.tags.TagNetworkSerialization.NetworkPayload $$1
   ) {
      ResourceKey<? extends Registry<T>> $$2 = $$0.key();
      Map<net.minecraft.tags.TagKey<T>, List<Holder<T>>> $$3 = new HashMap<>();
      $$1.tags.forEach(($$3x, $$4) -> {
         net.minecraft.tags.TagKey<T> $$5 = net.minecraft.tags.TagKey.create($$2, $$3x);
         List<Holder<T>> $$6 = $$4.intStream().mapToObj($$0::get).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
         $$3.put($$5, $$6);
      });
      return new net.minecraft.tags.TagLoader.LoadResult<>($$2, $$3);
   }

   public static final class NetworkPayload {
      public static final net.minecraft.tags.TagNetworkSerialization.NetworkPayload EMPTY = new net.minecraft.tags.TagNetworkSerialization.NetworkPayload(
         Map.of()
      );
      final Map<Identifier, IntList> tags;

      NetworkPayload(Map<Identifier, IntList> $$0) {
         this.tags = $$0;
      }

      public void write(FriendlyByteBuf $$0) {
         $$0.writeMap(this.tags, FriendlyByteBuf::writeIdentifier, FriendlyByteBuf::writeIntIdList);
      }

      public static net.minecraft.tags.TagNetworkSerialization.NetworkPayload read(FriendlyByteBuf $$0) {
         return new net.minecraft.tags.TagNetworkSerialization.NetworkPayload($$0.readMap(FriendlyByteBuf::readIdentifier, FriendlyByteBuf::readIntIdList));
      }

      public boolean isEmpty() {
         return this.tags.isEmpty();
      }

      public int size() {
         return this.tags.size();
      }

      public <T> net.minecraft.tags.TagLoader.LoadResult<T> resolve(Registry<T> $$0) {
         return net.minecraft.tags.TagNetworkSerialization.deserializeTagsFromNetwork($$0, this);
      }
   }
}
