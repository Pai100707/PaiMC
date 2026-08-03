package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class UnderwaterMagmaConfiguration implements FeatureConfiguration {
   public static final Codec<UnderwaterMagmaConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.intRange(0, 512).fieldOf("floor_search_range").forGetter($$0x -> $$0x.floorSearchRange),
            Codec.intRange(0, 64).fieldOf("placement_radius_around_floor").forGetter($$0x -> $$0x.placementRadiusAroundFloor),
            Codec.floatRange(0.0F, 1.0F).fieldOf("placement_probability_per_valid_position").forGetter($$0x -> $$0x.placementProbabilityPerValidPosition)
         )
         .apply($$0, UnderwaterMagmaConfiguration::new)
   );
   public final int floorSearchRange;
   public final int placementRadiusAroundFloor;
   public final float placementProbabilityPerValidPosition;

   public UnderwaterMagmaConfiguration(int $$0, int $$1, float $$2) {
      this.floorSearchRange = $$0;
      this.placementRadiusAroundFloor = $$1;
      this.placementProbabilityPerValidPosition = $$2;
   }
}
