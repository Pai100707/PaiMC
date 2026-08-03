package net.minecraft.world.entity.npc.villager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record VillagerData(Holder<VillagerType> type, Holder<VillagerProfession> profession, int level) {
   public static final int MIN_VILLAGER_LEVEL = 1;
   public static final int MAX_VILLAGER_LEVEL = 5;
   private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
   public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BuiltInRegistries.VILLAGER_TYPE
               .holderByNameCodec()
               .fieldOf("type")
               .orElseGet(() -> BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS))
               .forGetter($$0x -> $$0x.type),
            BuiltInRegistries.VILLAGER_PROFESSION
               .holderByNameCodec()
               .fieldOf("profession")
               .orElseGet(() -> BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE))
               .forGetter($$0x -> $$0x.profession),
            Codec.INT.fieldOf("level").orElse(1).forGetter($$0x -> $$0x.level)
         )
         .apply($$0, VillagerData::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, VillagerData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.holderRegistry(Registries.VILLAGER_TYPE),
      VillagerData::type,
      ByteBufCodecs.holderRegistry(Registries.VILLAGER_PROFESSION),
      VillagerData::profession,
      ByteBufCodecs.VAR_INT,
      VillagerData::level,
      VillagerData::new
   );

   public VillagerData(Holder<VillagerType> type, Holder<VillagerProfession> profession, int level) {
      level = Math.max(1, level);
      this.type = type;
      this.profession = profession;
      this.level = level;
   }

   public VillagerData withType(Holder<VillagerType> $$0) {
      return new VillagerData($$0, this.profession, this.level);
   }

   public VillagerData withType(Provider $$0, ResourceKey<VillagerType> $$1) {
      return this.withType($$0.getOrThrow($$1));
   }

   public VillagerData withProfession(Holder<VillagerProfession> $$0) {
      return new VillagerData(this.type, $$0, this.level);
   }

   public VillagerData withProfession(Provider $$0, ResourceKey<VillagerProfession> $$1) {
      return this.withProfession($$0.getOrThrow($$1));
   }

   public VillagerData withLevel(int $$0) {
      return new VillagerData(this.type, this.profession, $$0);
   }

   public static int getMinXpPerLevel(int $$0) {
      return canLevelUp($$0) ? NEXT_LEVEL_XP_THRESHOLDS[$$0 - 1] : 0;
   }

   public static int getMaxXpPerLevel(int $$0) {
      return canLevelUp($$0) ? NEXT_LEVEL_XP_THRESHOLDS[$$0] : 0;
   }

   public static boolean canLevelUp(int $$0) {
      return $$0 >= 1 && $$0 < 5;
   }
}
