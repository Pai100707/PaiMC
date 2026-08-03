package net.minecraft.world.level.block.entity.trialspawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public record TrialSpawnerConfig(
   int spawnRange,
   float totalMobs,
   float simultaneousMobs,
   float totalMobsAddedPerPlayer,
   float simultaneousMobsAddedPerPlayer,
   int ticksBetweenSpawn,
   WeightedList<net.minecraft.world.level.SpawnData> spawnPotentialsDefinition,
   WeightedList<ResourceKey<LootTable>> lootTablesToEject,
   ResourceKey<LootTable> itemsToDropWhenOminous
) {
   public static final TrialSpawnerConfig DEFAULT = builder().build();
   public static final Codec<TrialSpawnerConfig> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.intRange(1, 128).optionalFieldOf("spawn_range", DEFAULT.spawnRange).forGetter(TrialSpawnerConfig::spawnRange),
            Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("total_mobs", DEFAULT.totalMobs).forGetter(TrialSpawnerConfig::totalMobs),
            Codec.floatRange(0.0F, Float.MAX_VALUE)
               .optionalFieldOf("simultaneous_mobs", DEFAULT.simultaneousMobs)
               .forGetter(TrialSpawnerConfig::simultaneousMobs),
            Codec.floatRange(0.0F, Float.MAX_VALUE)
               .optionalFieldOf("total_mobs_added_per_player", DEFAULT.totalMobsAddedPerPlayer)
               .forGetter(TrialSpawnerConfig::totalMobsAddedPerPlayer),
            Codec.floatRange(0.0F, Float.MAX_VALUE)
               .optionalFieldOf("simultaneous_mobs_added_per_player", DEFAULT.simultaneousMobsAddedPerPlayer)
               .forGetter(TrialSpawnerConfig::simultaneousMobsAddedPerPlayer),
            Codec.intRange(0, Integer.MAX_VALUE)
               .optionalFieldOf("ticks_between_spawn", DEFAULT.ticksBetweenSpawn)
               .forGetter(TrialSpawnerConfig::ticksBetweenSpawn),
            net.minecraft.world.level.SpawnData.LIST_CODEC
               .optionalFieldOf("spawn_potentials", WeightedList.of())
               .forGetter(TrialSpawnerConfig::spawnPotentialsDefinition),
            WeightedList.codec(LootTable.KEY_CODEC)
               .optionalFieldOf("loot_tables_to_eject", DEFAULT.lootTablesToEject)
               .forGetter(TrialSpawnerConfig::lootTablesToEject),
            LootTable.KEY_CODEC
               .optionalFieldOf("items_to_drop_when_ominous", DEFAULT.itemsToDropWhenOminous)
               .forGetter(TrialSpawnerConfig::itemsToDropWhenOminous)
         )
         .apply($$0, TrialSpawnerConfig::new)
   );
   public static final Codec<Holder<TrialSpawnerConfig>> CODEC = RegistryFileCodec.create(Registries.TRIAL_SPAWNER_CONFIG, DIRECT_CODEC);

   public int calculateTargetTotalMobs(int $$0) {
      return (int)Math.floor(this.totalMobs + this.totalMobsAddedPerPlayer * $$0);
   }

   public int calculateTargetSimultaneousMobs(int $$0) {
      return (int)Math.floor(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * $$0);
   }

   public long ticksBetweenItemSpawners() {
      return 160L;
   }

   public static TrialSpawnerConfig.Builder builder() {
      return new TrialSpawnerConfig.Builder();
   }

   public TrialSpawnerConfig withSpawning(EntityType<?> $$0) {
      CompoundTag $$1 = new CompoundTag();
      $$1.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey($$0).toString());
      net.minecraft.world.level.SpawnData $$2 = new net.minecraft.world.level.SpawnData($$1, Optional.empty(), Optional.empty());
      return new TrialSpawnerConfig(
         this.spawnRange,
         this.totalMobs,
         this.simultaneousMobs,
         this.totalMobsAddedPerPlayer,
         this.simultaneousMobsAddedPerPlayer,
         this.ticksBetweenSpawn,
         WeightedList.of($$2),
         this.lootTablesToEject,
         this.itemsToDropWhenOminous
      );
   }

   public static class Builder {
      private int spawnRange = 4;
      private float totalMobs = 6.0F;
      private float simultaneousMobs = 2.0F;
      private float totalMobsAddedPerPlayer = 2.0F;
      private float simultaneousMobsAddedPerPlayer = 1.0F;
      private int ticksBetweenSpawn = 40;
      private WeightedList<net.minecraft.world.level.SpawnData> spawnPotentialsDefinition = WeightedList.of();
      private WeightedList<ResourceKey<LootTable>> lootTablesToEject = WeightedList.builder()
         .add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_CONSUMABLES)
         .add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_KEY)
         .build();
      private ResourceKey<LootTable> itemsToDropWhenOminous = BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS;

      public TrialSpawnerConfig.Builder spawnRange(int $$0) {
         this.spawnRange = $$0;
         return this;
      }

      public TrialSpawnerConfig.Builder totalMobs(float $$0) {
         this.totalMobs = $$0;
         return this;
      }

      public TrialSpawnerConfig.Builder simultaneousMobs(float $$0) {
         this.simultaneousMobs = $$0;
         return this;
      }

      public TrialSpawnerConfig.Builder totalMobsAddedPerPlayer(float $$0) {
         this.totalMobsAddedPerPlayer = $$0;
         return this;
      }

      public TrialSpawnerConfig.Builder simultaneousMobsAddedPerPlayer(float $$0) {
         this.simultaneousMobsAddedPerPlayer = $$0;
         return this;
      }

      public TrialSpawnerConfig.Builder ticksBetweenSpawn(int $$0) {
         this.ticksBetweenSpawn = $$0;
         return this;
      }

      public TrialSpawnerConfig.Builder spawnPotentialsDefinition(WeightedList<net.minecraft.world.level.SpawnData> $$0) {
         this.spawnPotentialsDefinition = $$0;
         return this;
      }

      public TrialSpawnerConfig.Builder lootTablesToEject(WeightedList<ResourceKey<LootTable>> $$0) {
         this.lootTablesToEject = $$0;
         return this;
      }

      public TrialSpawnerConfig.Builder itemsToDropWhenOminous(ResourceKey<LootTable> $$0) {
         this.itemsToDropWhenOminous = $$0;
         return this;
      }

      public TrialSpawnerConfig build() {
         return new TrialSpawnerConfig(
            this.spawnRange,
            this.totalMobs,
            this.simultaneousMobs,
            this.totalMobsAddedPerPlayer,
            this.simultaneousMobsAddedPerPlayer,
            this.ticksBetweenSpawn,
            this.spawnPotentialsDefinition,
            this.lootTablesToEject,
            this.itemsToDropWhenOminous
         );
      }
   }
}
