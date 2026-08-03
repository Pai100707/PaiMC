package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;

public class DripstoneClusterConfiguration implements FeatureConfiguration {
   public static final Codec<DripstoneClusterConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range").forGetter($$0x -> $$0x.floorToCeilingSearchRange),
            IntProvider.codec(1, 128).fieldOf("height").forGetter($$0x -> $$0x.height),
            IntProvider.codec(1, 128).fieldOf("radius").forGetter($$0x -> $$0x.radius),
            Codec.intRange(0, 64).fieldOf("max_stalagmite_stalactite_height_diff").forGetter($$0x -> $$0x.maxStalagmiteStalactiteHeightDiff),
            Codec.intRange(1, 64).fieldOf("height_deviation").forGetter($$0x -> $$0x.heightDeviation),
            IntProvider.codec(0, 128).fieldOf("dripstone_block_layer_thickness").forGetter($$0x -> $$0x.dripstoneBlockLayerThickness),
            FloatProvider.codec(0.0F, 2.0F).fieldOf("density").forGetter($$0x -> $$0x.density),
            FloatProvider.codec(0.0F, 2.0F).fieldOf("wetness").forGetter($$0x -> $$0x.wetness),
            Codec.floatRange(0.0F, 1.0F)
               .fieldOf("chance_of_dripstone_column_at_max_distance_from_center")
               .forGetter($$0x -> $$0x.chanceOfDripstoneColumnAtMaxDistanceFromCenter),
            Codec.intRange(1, 64)
               .fieldOf("max_distance_from_edge_affecting_chance_of_dripstone_column")
               .forGetter($$0x -> $$0x.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_height_bias").forGetter($$0x -> $$0x.maxDistanceFromCenterAffectingHeightBias)
         )
         .apply($$0, DripstoneClusterConfiguration::new)
   );
   public final int floorToCeilingSearchRange;
   public final IntProvider height;
   public final IntProvider radius;
   public final int maxStalagmiteStalactiteHeightDiff;
   public final int heightDeviation;
   public final IntProvider dripstoneBlockLayerThickness;
   public final FloatProvider density;
   public final FloatProvider wetness;
   public final float chanceOfDripstoneColumnAtMaxDistanceFromCenter;
   public final int maxDistanceFromEdgeAffectingChanceOfDripstoneColumn;
   public final int maxDistanceFromCenterAffectingHeightBias;

   public DripstoneClusterConfiguration(
      int $$0, IntProvider $$1, IntProvider $$2, int $$3, int $$4, IntProvider $$5, FloatProvider $$6, FloatProvider $$7, float $$8, int $$9, int $$10
   ) {
      this.floorToCeilingSearchRange = $$0;
      this.height = $$1;
      this.radius = $$2;
      this.maxStalagmiteStalactiteHeightDiff = $$3;
      this.heightDeviation = $$4;
      this.dripstoneBlockLayerThickness = $$5;
      this.density = $$6;
      this.wetness = $$7;
      this.chanceOfDripstoneColumnAtMaxDistanceFromCenter = $$8;
      this.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn = $$9;
      this.maxDistanceFromCenterAffectingHeightBias = $$10;
   }
}
