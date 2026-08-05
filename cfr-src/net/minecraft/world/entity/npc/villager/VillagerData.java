/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.npc.villager;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;

public record VillagerData(Holder<VillagerType> type, Holder<VillagerProfession> profession, int level) {
    public static final int MIN_VILLAGER_LEVEL = 1;
    public static final int MAX_VILLAGER_LEVEL = 5;
    private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
    public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)BuiltInRegistries.VILLAGER_TYPE.holderByNameCodec().fieldOf("type").orElseGet(() -> BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS)).forGetter($$0 -> $$0.type), (App)BuiltInRegistries.VILLAGER_PROFESSION.holderByNameCodec().fieldOf("profession").orElseGet(() -> BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE)).forGetter($$0 -> $$0.profession), (App)Codec.INT.fieldOf("level").orElse((Object)1).forGetter($$0 -> $$0.level)).apply((Applicative)$$02, VillagerData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, VillagerData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.VILLAGER_TYPE), VillagerData::type, ByteBufCodecs.holderRegistry(Registries.VILLAGER_PROFESSION), VillagerData::profession, ByteBufCodecs.VAR_INT, VillagerData::level, VillagerData::new);

    public VillagerData {
        $$2 = Math.max(1, $$2);
    }

    public VillagerData withType(Holder<VillagerType> $$0) {
        return new VillagerData($$0, this.profession, this.level);
    }

    public VillagerData withType(HolderGetter.Provider $$0, ResourceKey<VillagerType> $$1) {
        return this.withType($$0.getOrThrow($$1));
    }

    public VillagerData withProfession(Holder<VillagerProfession> $$0) {
        return new VillagerData(this.type, $$0, this.level);
    }

    public VillagerData withProfession(HolderGetter.Provider $$0, ResourceKey<VillagerProfession> $$1) {
        return this.withProfession($$0.getOrThrow($$1));
    }

    public VillagerData withLevel(int $$0) {
        return new VillagerData(this.type, this.profession, $$0);
    }

    public static int getMinXpPerLevel(int $$0) {
        return VillagerData.canLevelUp($$0) ? NEXT_LEVEL_XP_THRESHOLDS[$$0 - 1] : 0;
    }

    public static int getMaxXpPerLevel(int $$0) {
        return VillagerData.canLevelUp($$0) ? NEXT_LEVEL_XP_THRESHOLDS[$$0] : 0;
    }

    public static boolean canLevelUp(int $$0) {
        return $$0 >= 1 && $$0 < 5;
    }
}

