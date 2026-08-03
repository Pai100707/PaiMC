package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryDataLoader.RegistryData;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.repository.KnownPack;

public class RegistrySynchronization {
   private static final Set<ResourceKey<? extends net.minecraft.core.Registry<?>>> NETWORKABLE_REGISTRIES = RegistryDataLoader.SYNCHRONIZED_REGISTRIES
      .stream()
      .<ResourceKey<? extends net.minecraft.core.Registry<?>>>map(RegistryData::key)
      .collect(Collectors.toUnmodifiableSet());

   public static void packRegistries(
      DynamicOps<Tag> $$0,
      net.minecraft.core.RegistryAccess $$1,
      Set<KnownPack> $$2,
      BiConsumer<ResourceKey<? extends net.minecraft.core.Registry<?>>, List<net.minecraft.core.RegistrySynchronization.PackedRegistryEntry>> $$3
   ) {
      RegistryDataLoader.SYNCHRONIZED_REGISTRIES.forEach($$4 -> packRegistry($$0, $$4, $$1, $$2, $$3));
   }

   private static <T> void packRegistry(
      DynamicOps<Tag> $$0,
      RegistryData<T> $$1,
      net.minecraft.core.RegistryAccess $$2,
      Set<KnownPack> $$3,
      BiConsumer<ResourceKey<? extends net.minecraft.core.Registry<?>>, List<net.minecraft.core.RegistrySynchronization.PackedRegistryEntry>> $$4
   ) {
      $$2.lookup($$1.key())
         .ifPresent(
            $$4x -> {
               List<net.minecraft.core.RegistrySynchronization.PackedRegistryEntry> $$5 = new ArrayList<>($$4x.size());
               $$4x.listElements()
                  .forEach(
                     $$5x -> {
                        boolean $$6 = $$4x.registrationInfo($$5x.key())
                           .flatMap(net.minecraft.core.RegistrationInfo::knownPackInfo)
                           .filter($$3::contains)
                           .isPresent();
                        Optional<Tag> $$7;
                        if ($$6) {
                           $$7 = Optional.empty();
                        } else {
                           Tag $$8 = (Tag)$$1.elementCodec()
                              .encodeStart($$0, $$5x.value())
                              .getOrThrow($$1xxx -> new IllegalArgumentException("Failed to serialize " + $$5x.key() + ": " + $$1xxx));
                           $$7 = Optional.of($$8);
                        }

                        $$5.add(new net.minecraft.core.RegistrySynchronization.PackedRegistryEntry($$5x.key().identifier(), $$7));
                     }
                  );
               $$4.accept($$4x.key(), $$5);
            }
         );
   }

   private static Stream<net.minecraft.core.RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries(net.minecraft.core.RegistryAccess $$0) {
      return $$0.registries().filter($$0x -> isNetworkable($$0x.key()));
   }

   public static Stream<net.minecraft.core.RegistryAccess.RegistryEntry<?>> networkedRegistries(net.minecraft.core.LayeredRegistryAccess<RegistryLayer> $$0) {
      return ownedNetworkableRegistries($$0.getAccessFrom(RegistryLayer.WORLDGEN));
   }

   public static Stream<net.minecraft.core.RegistryAccess.RegistryEntry<?>> networkSafeRegistries(net.minecraft.core.LayeredRegistryAccess<RegistryLayer> $$0) {
      Stream<net.minecraft.core.RegistryAccess.RegistryEntry<?>> $$1 = $$0.getLayer(RegistryLayer.STATIC).registries();
      Stream<net.minecraft.core.RegistryAccess.RegistryEntry<?>> $$2 = networkedRegistries($$0);
      return Stream.concat($$2, $$1);
   }

   public static boolean isNetworkable(ResourceKey<? extends net.minecraft.core.Registry<?>> $$0) {
      return NETWORKABLE_REGISTRIES.contains($$0);
   }

   public record PackedRegistryEntry(Identifier id, Optional<Tag> data) {
      public static final StreamCodec<ByteBuf, net.minecraft.core.RegistrySynchronization.PackedRegistryEntry> STREAM_CODEC = StreamCodec.composite(
         Identifier.STREAM_CODEC,
         net.minecraft.core.RegistrySynchronization.PackedRegistryEntry::id,
         ByteBufCodecs.TAG.apply(ByteBufCodecs::optional),
         net.minecraft.core.RegistrySynchronization.PackedRegistryEntry::data,
         net.minecraft.core.RegistrySynchronization.PackedRegistryEntry::new
      );
   }
}
