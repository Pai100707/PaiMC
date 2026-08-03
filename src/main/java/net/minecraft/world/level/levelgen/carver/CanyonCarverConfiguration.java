package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CanyonCarverConfiguration extends CarverConfiguration {
   public static final Codec<CanyonCarverConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            CarverConfiguration.CODEC.forGetter($$0x -> $$0x),
            FloatProvider.CODEC.fieldOf("vertical_rotation").forGetter($$0x -> $$0x.verticalRotation),
            CanyonCarverConfiguration.CanyonShapeConfiguration.CODEC.fieldOf("shape").forGetter($$0x -> $$0x.shape)
         )
         .apply($$0, CanyonCarverConfiguration::new)
   );
   public final FloatProvider verticalRotation;
   public final CanyonCarverConfiguration.CanyonShapeConfiguration shape;

   public CanyonCarverConfiguration(
      float $$0,
      HeightProvider $$1,
      FloatProvider $$2,
      VerticalAnchor $$3,
      CarverDebugSettings $$4,
      HolderSet<Block> $$5,
      FloatProvider $$6,
      CanyonCarverConfiguration.CanyonShapeConfiguration $$7
   ) {
      super($$0, $$1, $$2, $$3, $$4, $$5);
      this.verticalRotation = $$6;
      this.shape = $$7;
   }

   public CanyonCarverConfiguration(CarverConfiguration $$0, FloatProvider $$1, CanyonCarverConfiguration.CanyonShapeConfiguration $$2) {
      this($$0.probability, $$0.y, $$0.yScale, $$0.lavaLevel, $$0.debugSettings, $$0.replaceable, $$1, $$2);
   }

   public static class CanyonShapeConfiguration {
      public static final Codec<CanyonCarverConfiguration.CanyonShapeConfiguration> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               FloatProvider.CODEC.fieldOf("distance_factor").forGetter($$0x -> $$0x.distanceFactor),
               FloatProvider.CODEC.fieldOf("thickness").forGetter($$0x -> $$0x.thickness),
               ExtraCodecs.POSITIVE_INT.fieldOf("width_smoothness").forGetter($$0x -> $$0x.widthSmoothness),
               FloatProvider.CODEC.fieldOf("horizontal_radius_factor").forGetter($$0x -> $$0x.horizontalRadiusFactor),
               Codec.FLOAT.fieldOf("vertical_radius_default_factor").forGetter($$0x -> $$0x.verticalRadiusDefaultFactor),
               Codec.FLOAT.fieldOf("vertical_radius_center_factor").forGetter($$0x -> $$0x.verticalRadiusCenterFactor)
            )
            .apply($$0, CanyonCarverConfiguration.CanyonShapeConfiguration::new)
      );
      public final FloatProvider distanceFactor;
      public final FloatProvider thickness;
      public final int widthSmoothness;
      public final FloatProvider horizontalRadiusFactor;
      public final float verticalRadiusDefaultFactor;
      public final float verticalRadiusCenterFactor;

      public CanyonShapeConfiguration(FloatProvider $$0, FloatProvider $$1, int $$2, FloatProvider $$3, float $$4, float $$5) {
         this.widthSmoothness = $$2;
         this.horizontalRadiusFactor = $$3;
         this.verticalRadiusDefaultFactor = $$4;
         this.verticalRadiusCenterFactor = $$5;
         this.distanceFactor = $$0;
         this.thickness = $$1;
      }
   }
}
