package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;

public class GeodeConfiguration implements FeatureConfiguration {
   public static final Codec<Double> CHANCE_RANGE = Codec.doubleRange(0.0, 1.0);
   public static final Codec<GeodeConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            GeodeBlockSettings.CODEC.fieldOf("blocks").forGetter($$0x -> $$0x.geodeBlockSettings),
            GeodeLayerSettings.CODEC.fieldOf("layers").forGetter($$0x -> $$0x.geodeLayerSettings),
            GeodeCrackSettings.CODEC.fieldOf("crack").forGetter($$0x -> $$0x.geodeCrackSettings),
            CHANCE_RANGE.fieldOf("use_potential_placements_chance").orElse(0.35).forGetter($$0x -> $$0x.usePotentialPlacementsChance),
            CHANCE_RANGE.fieldOf("use_alternate_layer0_chance").orElse(0.0).forGetter($$0x -> $$0x.useAlternateLayer0Chance),
            Codec.BOOL.fieldOf("placements_require_layer0_alternate").orElse(true).forGetter($$0x -> $$0x.placementsRequireLayer0Alternate),
            IntProvider.codec(1, 20).fieldOf("outer_wall_distance").orElse(UniformInt.of(4, 5)).forGetter($$0x -> $$0x.outerWallDistance),
            IntProvider.codec(1, 20).fieldOf("distribution_points").orElse(UniformInt.of(3, 4)).forGetter($$0x -> $$0x.distributionPoints),
            IntProvider.codec(0, 10).fieldOf("point_offset").orElse(UniformInt.of(1, 2)).forGetter($$0x -> $$0x.pointOffset),
            Codec.INT.fieldOf("min_gen_offset").orElse(-16).forGetter($$0x -> $$0x.minGenOffset),
            Codec.INT.fieldOf("max_gen_offset").orElse(16).forGetter($$0x -> $$0x.maxGenOffset),
            CHANCE_RANGE.fieldOf("noise_multiplier").orElse(0.05).forGetter($$0x -> $$0x.noiseMultiplier),
            Codec.INT.fieldOf("invalid_blocks_threshold").forGetter($$0x -> $$0x.invalidBlocksThreshold)
         )
         .apply($$0, GeodeConfiguration::new)
   );
   public final GeodeBlockSettings geodeBlockSettings;
   public final GeodeLayerSettings geodeLayerSettings;
   public final GeodeCrackSettings geodeCrackSettings;
   public final double usePotentialPlacementsChance;
   public final double useAlternateLayer0Chance;
   public final boolean placementsRequireLayer0Alternate;
   public final IntProvider outerWallDistance;
   public final IntProvider distributionPoints;
   public final IntProvider pointOffset;
   public final int minGenOffset;
   public final int maxGenOffset;
   public final double noiseMultiplier;
   public final int invalidBlocksThreshold;

   public GeodeConfiguration(
      GeodeBlockSettings $$0,
      GeodeLayerSettings $$1,
      GeodeCrackSettings $$2,
      double $$3,
      double $$4,
      boolean $$5,
      IntProvider $$6,
      IntProvider $$7,
      IntProvider $$8,
      int $$9,
      int $$10,
      double $$11,
      int $$12
   ) {
      this.geodeBlockSettings = $$0;
      this.geodeLayerSettings = $$1;
      this.geodeCrackSettings = $$2;
      this.usePotentialPlacementsChance = $$3;
      this.useAlternateLayer0Chance = $$4;
      this.placementsRequireLayer0Alternate = $$5;
      this.outerWallDistance = $$6;
      this.distributionPoints = $$7;
      this.pointOffset = $$8;
      this.minGenOffset = $$9;
      this.maxGenOffset = $$10;
      this.noiseMultiplier = $$11;
      this.invalidBlocksThreshold = $$12;
   }
}
