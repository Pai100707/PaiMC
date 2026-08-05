/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DynamicOps
 *  io.netty.buffer.ByteBuf
 */
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
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.repository.KnownPack;

public class RegistrySynchronization {
    private static final Set<ResourceKey<? extends Registry<?>>> NETWORKABLE_REGISTRIES = RegistryDataLoader.SYNCHRONIZED_REGISTRIES.stream().map(RegistryDataLoader.RegistryData::key).collect(Collectors.toUnmodifiableSet());

    public static void packRegistries(DynamicOps<Tag> $$0, RegistryAccess $$1, Set<KnownPack> $$2, BiConsumer<ResourceKey<? extends Registry<?>>, List<PackedRegistryEntry>> $$3) {
        RegistryDataLoader.SYNCHRONIZED_REGISTRIES.forEach($$4 -> RegistrySynchronization.packRegistry($$0, $$4, $$1, $$2, $$3));
    }

    private static <T> void packRegistry(DynamicOps<Tag> $$0, RegistryDataLoader.RegistryData<T> $$1, RegistryAccess $$2, Set<KnownPack> $$3, BiConsumer<ResourceKey<? extends Registry<?>>, List<PackedRegistryEntry>> $$42) {
        $$2.lookup($$1.key()).ifPresent($$4 -> {
            ArrayList $$52 = new ArrayList($$4.size());
            $$4.listElements().forEach($$5 -> {
                Optional<Tag> $$9;
                boolean $$6 = $$4.registrationInfo($$5.key()).flatMap(RegistrationInfo::knownPackInfo).filter($$3::contains).isPresent();
                if ($$6) {
                    Optional $$7 = Optional.empty();
                } else {
                    Tag $$8 = (Tag)$$1.elementCodec().encodeStart($$0, $$5.value()).getOrThrow($$1 -> new IllegalArgumentException("Failed to serialize " + String.valueOf($$5.key()) + ": " + $$1));
                    $$9 = Optional.of($$8);
                }
                $$52.add(new PackedRegistryEntry($$5.key().identifier(), $$9));
            });
            $$42.accept($$4.key(), $$52);
        });
    }

    private static Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries(RegistryAccess $$02) {
        return $$02.registries().filter($$0 -> RegistrySynchronization.isNetworkable($$0.key()));
    }

    public static Stream<RegistryAccess.RegistryEntry<?>> networkedRegistries(LayeredRegistryAccess<RegistryLayer> $$0) {
        return RegistrySynchronization.ownedNetworkableRegistries($$0.getAccessFrom(RegistryLayer.WORLDGEN));
    }

    public static Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries(LayeredRegistryAccess<RegistryLayer> $$0) {
        Stream<RegistryAccess.RegistryEntry<?>> $$1 = $$0.getLayer(RegistryLayer.STATIC).registries();
        Stream<RegistryAccess.RegistryEntry<?>> $$2 = RegistrySynchronization.networkedRegistries($$0);
        return Stream.concat($$2, $$1);
    }

    public static boolean isNetworkable(ResourceKey<? extends Registry<?>> $$0) {
        return NETWORKABLE_REGISTRIES.contains($$0);
    }

    public record PackedRegistryEntry(Identifier id, Optional<Tag> data) {
        public static final StreamCodec<ByteBuf, PackedRegistryEntry> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, PackedRegistryEntry::id, ByteBufCodecs.TAG.apply(ByteBufCodecs::optional), PackedRegistryEntry::data, PackedRegistryEntry::new);
    }
}

