/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.entity.npc.villager;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class VillagerType {
    public static final ResourceKey<VillagerType> DESERT = VillagerType.createKey("desert");
    public static final ResourceKey<VillagerType> JUNGLE = VillagerType.createKey("jungle");
    public static final ResourceKey<VillagerType> PLAINS = VillagerType.createKey("plains");
    public static final ResourceKey<VillagerType> SAVANNA = VillagerType.createKey("savanna");
    public static final ResourceKey<VillagerType> SNOW = VillagerType.createKey("snow");
    public static final ResourceKey<VillagerType> SWAMP = VillagerType.createKey("swamp");
    public static final ResourceKey<VillagerType> TAIGA = VillagerType.createKey("taiga");
    public static final Codec<Holder<VillagerType>> CODEC = RegistryFixedCodec.create(Registries.VILLAGER_TYPE);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<VillagerType>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.VILLAGER_TYPE);
    private static final Map<ResourceKey<Biome>, ResourceKey<VillagerType>> BY_BIOME = Util.make(Maps.newHashMap(), $$0 -> {
        $$0.put(Biomes.BADLANDS, DESERT);
        $$0.put(Biomes.DESERT, DESERT);
        $$0.put(Biomes.ERODED_BADLANDS, DESERT);
        $$0.put(Biomes.WOODED_BADLANDS, DESERT);
        $$0.put(Biomes.BAMBOO_JUNGLE, JUNGLE);
        $$0.put(Biomes.JUNGLE, JUNGLE);
        $$0.put(Biomes.SPARSE_JUNGLE, JUNGLE);
        $$0.put(Biomes.SAVANNA_PLATEAU, SAVANNA);
        $$0.put(Biomes.SAVANNA, SAVANNA);
        $$0.put(Biomes.WINDSWEPT_SAVANNA, SAVANNA);
        $$0.put(Biomes.DEEP_FROZEN_OCEAN, SNOW);
        $$0.put(Biomes.FROZEN_OCEAN, SNOW);
        $$0.put(Biomes.FROZEN_RIVER, SNOW);
        $$0.put(Biomes.ICE_SPIKES, SNOW);
        $$0.put(Biomes.SNOWY_BEACH, SNOW);
        $$0.put(Biomes.SNOWY_TAIGA, SNOW);
        $$0.put(Biomes.SNOWY_PLAINS, SNOW);
        $$0.put(Biomes.GROVE, SNOW);
        $$0.put(Biomes.SNOWY_SLOPES, SNOW);
        $$0.put(Biomes.FROZEN_PEAKS, SNOW);
        $$0.put(Biomes.JAGGED_PEAKS, SNOW);
        $$0.put(Biomes.SWAMP, SWAMP);
        $$0.put(Biomes.MANGROVE_SWAMP, SWAMP);
        $$0.put(Biomes.OLD_GROWTH_SPRUCE_TAIGA, TAIGA);
        $$0.put(Biomes.OLD_GROWTH_PINE_TAIGA, TAIGA);
        $$0.put(Biomes.WINDSWEPT_GRAVELLY_HILLS, TAIGA);
        $$0.put(Biomes.WINDSWEPT_HILLS, TAIGA);
        $$0.put(Biomes.TAIGA, TAIGA);
        $$0.put(Biomes.WINDSWEPT_FOREST, TAIGA);
    });

    private static ResourceKey<VillagerType> createKey(String $$0) {
        return ResourceKey.create(Registries.VILLAGER_TYPE, Identifier.withDefaultNamespace($$0));
    }

    private static VillagerType register(Registry<VillagerType> $$0, ResourceKey<VillagerType> $$1) {
        return Registry.register($$0, $$1, new VillagerType());
    }

    public static VillagerType bootstrap(Registry<VillagerType> $$0) {
        VillagerType.register($$0, DESERT);
        VillagerType.register($$0, JUNGLE);
        VillagerType.register($$0, PLAINS);
        VillagerType.register($$0, SAVANNA);
        VillagerType.register($$0, SNOW);
        VillagerType.register($$0, SWAMP);
        return VillagerType.register($$0, TAIGA);
    }

    public static ResourceKey<VillagerType> byBiome(Holder<Biome> $$0) {
        return $$0.unwrapKey().map(BY_BIOME::get).orElse(PLAINS);
    }
}

