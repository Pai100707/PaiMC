package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MobSpawnSettings {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float DEFAULT_CREATURE_SPAWN_PROBABILITY = 0.1F;
   public static final WeightedList<MobSpawnSettings.SpawnerData> EMPTY_MOB_LIST = WeightedList.of();
   public static final MobSpawnSettings EMPTY = new MobSpawnSettings.Builder().build();
   public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.floatRange(0.0F, 0.9999999F).optionalFieldOf("creature_spawn_probability", 0.1F).forGetter($$0x -> $$0x.creatureGenerationProbability),
            Codec.simpleMap(
                  MobCategory.CODEC,
                  WeightedList.codec(MobSpawnSettings.SpawnerData.CODEC).promotePartial(Util.prefix("Spawn data: ", LOGGER::error)),
                  StringRepresentable.keys(MobCategory.values())
               )
               .fieldOf("spawners")
               .forGetter($$0x -> $$0x.spawners),
            Codec.simpleMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), MobSpawnSettings.MobSpawnCost.CODEC, BuiltInRegistries.ENTITY_TYPE)
               .fieldOf("spawn_costs")
               .forGetter($$0x -> $$0x.mobSpawnCosts)
         )
         .apply($$0, MobSpawnSettings::new)
   );
   private final float creatureGenerationProbability;
   private final Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners;
   private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts;

   MobSpawnSettings(float $$0, Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> $$1, Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> $$2) {
      this.creatureGenerationProbability = $$0;
      this.spawners = ImmutableMap.copyOf($$1);
      this.mobSpawnCosts = ImmutableMap.copyOf($$2);
   }

   public WeightedList<MobSpawnSettings.SpawnerData> getMobs(MobCategory $$0) {
      return this.spawners.getOrDefault($$0, EMPTY_MOB_LIST);
   }

   @Nullable
   public MobSpawnSettings.MobSpawnCost getMobSpawnCost(EntityType<?> $$0) {
      return this.mobSpawnCosts.get($$0);
   }

   public float getCreatureProbability() {
      return this.creatureGenerationProbability;
   }

   public static class Builder {
      private final Map<MobCategory, net.minecraft.util.random.WeightedList.Builder<MobSpawnSettings.SpawnerData>> spawners = Util.makeEnumMap(
         MobCategory.class, $$0 -> WeightedList.builder()
      );
      private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts = Maps.newLinkedHashMap();
      private float creatureGenerationProbability = 0.1F;

      public MobSpawnSettings.Builder addSpawn(MobCategory $$0, int $$1, MobSpawnSettings.SpawnerData $$2) {
         this.spawners.get($$0).add($$2, $$1);
         return this;
      }

      public MobSpawnSettings.Builder addMobCharge(EntityType<?> $$0, double $$1, double $$2) {
         this.mobSpawnCosts.put($$0, new MobSpawnSettings.MobSpawnCost($$2, $$1));
         return this;
      }

      public MobSpawnSettings.Builder creatureGenerationProbability(float $$0) {
         this.creatureGenerationProbability = $$0;
         return this;
      }

      public MobSpawnSettings build() {
         return new MobSpawnSettings(
            this.creatureGenerationProbability,
            this.spawners
               .entrySet()
               .stream()
               .collect(ImmutableMap.toImmutableMap(Entry::getKey, $$0 -> ((net.minecraft.util.random.WeightedList.Builder)$$0.getValue()).build())),
            ImmutableMap.copyOf(this.mobSpawnCosts)
         );
      }
   }

   public record MobSpawnCost(double energyBudget, double charge) {
      public static final Codec<MobSpawnSettings.MobSpawnCost> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.DOUBLE.fieldOf("energy_budget").forGetter($$0x -> $$0x.energyBudget), Codec.DOUBLE.fieldOf("charge").forGetter($$0x -> $$0x.charge)
            )
            .apply($$0, MobSpawnSettings.MobSpawnCost::new)
      );
   }

   public record SpawnerData(EntityType<?> type, int minCount, int maxCount) {
      public static final MapCodec<MobSpawnSettings.SpawnerData> CODEC = RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                  BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter($$0x -> $$0x.type),
                  ExtraCodecs.POSITIVE_INT.fieldOf("minCount").forGetter($$0x -> $$0x.minCount),
                  ExtraCodecs.POSITIVE_INT.fieldOf("maxCount").forGetter($$0x -> $$0x.maxCount)
               )
               .apply($$0, MobSpawnSettings.SpawnerData::new)
         )
         .validate($$0 -> $$0.minCount > $$0.maxCount ? DataResult.error(() -> "minCount needs to be smaller or equal to maxCount") : DataResult.success($$0));

      public SpawnerData(EntityType<?> type, int minCount, int maxCount) {
         type = type.getCategory() == MobCategory.MISC ? EntityType.PIG : type;
         this.type = type;
         this.minCount = minCount;
         this.maxCount = maxCount;
      }

      @Override
      public String toString() {
         return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + ")";
      }
   }
}
