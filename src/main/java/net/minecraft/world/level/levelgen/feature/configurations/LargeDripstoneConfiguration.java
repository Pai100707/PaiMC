package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;

public class LargeDripstoneConfiguration implements FeatureConfiguration {
   public static final Codec<LargeDripstoneConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range").orElse(30).forGetter($$0x -> $$0x.floorToCeilingSearchRange),
            IntProvider.codec(1, 60).fieldOf("column_radius").forGetter($$0x -> $$0x.columnRadius),
            FloatProvider.codec(0.0F, 20.0F).fieldOf("height_scale").forGetter($$0x -> $$0x.heightScale),
            Codec.floatRange(0.1F, 1.0F).fieldOf("max_column_radius_to_cave_height_ratio").forGetter($$0x -> $$0x.maxColumnRadiusToCaveHeightRatio),
            FloatProvider.codec(0.1F, 10.0F).fieldOf("stalactite_bluntness").forGetter($$0x -> $$0x.stalactiteBluntness),
            FloatProvider.codec(0.1F, 10.0F).fieldOf("stalagmite_bluntness").forGetter($$0x -> $$0x.stalagmiteBluntness),
            FloatProvider.codec(0.0F, 2.0F).fieldOf("wind_speed").forGetter($$0x -> $$0x.windSpeed),
            Codec.intRange(0, 100).fieldOf("min_radius_for_wind").forGetter($$0x -> $$0x.minRadiusForWind),
            Codec.floatRange(0.0F, 5.0F).fieldOf("min_bluntness_for_wind").forGetter($$0x -> $$0x.minBluntnessForWind)
         )
         .apply($$0, LargeDripstoneConfiguration::new)
   );
   public final int floorToCeilingSearchRange;
   public final IntProvider columnRadius;
   public final FloatProvider heightScale;
   public final float maxColumnRadiusToCaveHeightRatio;
   public final FloatProvider stalactiteBluntness;
   public final FloatProvider stalagmiteBluntness;
   public final FloatProvider windSpeed;
   public final int minRadiusForWind;
   public final float minBluntnessForWind;

   public LargeDripstoneConfiguration(
      int $$0, IntProvider $$1, FloatProvider $$2, float $$3, FloatProvider $$4, FloatProvider $$5, FloatProvider $$6, int $$7, float $$8
   ) {
      this.floorToCeilingSearchRange = $$0;
      this.columnRadius = $$1;
      this.heightScale = $$2;
      this.maxColumnRadiusToCaveHeightRatio = $$3;
      this.stalactiteBluntness = $$4;
      this.stalagmiteBluntness = $$5;
      this.windSpeed = $$6;
      this.minRadiusForWind = $$7;
      this.minBluntnessForWind = $$8;
   }
}
