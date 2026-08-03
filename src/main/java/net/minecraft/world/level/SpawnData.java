package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EquipmentTable;

public record SpawnData(
   CompoundTag entityToSpawn, Optional<net.minecraft.world.level.SpawnData.CustomSpawnRules> customSpawnRules, Optional<EquipmentTable> equipment
) {
   public static final String ENTITY_TAG = "entity";
   public static final Codec<net.minecraft.world.level.SpawnData> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            CompoundTag.CODEC.fieldOf("entity").forGetter($$0x -> $$0x.entityToSpawn),
            net.minecraft.world.level.SpawnData.CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter($$0x -> $$0x.customSpawnRules),
            EquipmentTable.CODEC.optionalFieldOf("equipment").forGetter($$0x -> $$0x.equipment)
         )
         .apply($$0, net.minecraft.world.level.SpawnData::new)
   );
   public static final Codec<WeightedList<net.minecraft.world.level.SpawnData>> LIST_CODEC = WeightedList.codec(CODEC);

   public SpawnData() {
      this(new CompoundTag(), Optional.empty(), Optional.empty());
   }

   public SpawnData(
      CompoundTag entityToSpawn, Optional<net.minecraft.world.level.SpawnData.CustomSpawnRules> customSpawnRules, Optional<EquipmentTable> equipment
   ) {
      Optional<Identifier> $$3 = entityToSpawn.read("id", Identifier.CODEC);
      if ($$3.isPresent()) {
         entityToSpawn.store("id", Identifier.CODEC, $$3.get());
      } else {
         entityToSpawn.remove("id");
      }

      this.entityToSpawn = entityToSpawn;
      this.customSpawnRules = customSpawnRules;
      this.equipment = equipment;
   }

   public CompoundTag getEntityToSpawn() {
      return this.entityToSpawn;
   }

   public Optional<net.minecraft.world.level.SpawnData.CustomSpawnRules> getCustomSpawnRules() {
      return this.customSpawnRules;
   }

   public Optional<EquipmentTable> getEquipment() {
      return this.equipment;
   }

   public record CustomSpawnRules(InclusiveRange<Integer> blockLightLimit, InclusiveRange<Integer> skyLightLimit) {
      private static final InclusiveRange<Integer> LIGHT_RANGE = new InclusiveRange(0, 15);
      public static final Codec<net.minecraft.world.level.SpawnData.CustomSpawnRules> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               lightLimit("block_light_limit").forGetter($$0x -> $$0x.blockLightLimit), lightLimit("sky_light_limit").forGetter($$0x -> $$0x.skyLightLimit)
            )
            .apply($$0, net.minecraft.world.level.SpawnData.CustomSpawnRules::new)
      );

      private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> $$0) {
         return !LIGHT_RANGE.contains($$0) ? DataResult.error(() -> "Light values must be withing range " + LIGHT_RANGE) : DataResult.success($$0);
      }

      private static MapCodec<InclusiveRange<Integer>> lightLimit(String $$0) {
         return InclusiveRange.INT
            .lenientOptionalFieldOf($$0, LIGHT_RANGE)
            .validate(net.minecraft.world.level.SpawnData.CustomSpawnRules::checkLightBoundaries);
      }

      public boolean isValidPosition(BlockPos $$0, ServerLevel $$1) {
         return this.blockLightLimit.isValueInRange($$1.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, $$0))
            && this.skyLightLimit.isValueInRange($$1.getBrightness(net.minecraft.world.level.LightLayer.SKY, $$0));
      }
   }
}
