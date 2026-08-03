package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PointedDripstoneConfiguration implements FeatureConfiguration {
   public static final Codec<PointedDripstoneConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_taller_dripstone").orElse(0.2F).forGetter($$0x -> $$0x.chanceOfTallerDripstone),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_directional_spread").orElse(0.7F).forGetter($$0x -> $$0x.chanceOfDirectionalSpread),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spread_radius2").orElse(0.5F).forGetter($$0x -> $$0x.chanceOfSpreadRadius2),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spread_radius3").orElse(0.5F).forGetter($$0x -> $$0x.chanceOfSpreadRadius3)
         )
         .apply($$0, PointedDripstoneConfiguration::new)
   );
   public final float chanceOfTallerDripstone;
   public final float chanceOfDirectionalSpread;
   public final float chanceOfSpreadRadius2;
   public final float chanceOfSpreadRadius3;

   public PointedDripstoneConfiguration(float $$0, float $$1, float $$2, float $$3) {
      this.chanceOfTallerDripstone = $$0;
      this.chanceOfDirectionalSpread = $$1;
      this.chanceOfSpreadRadius2 = $$2;
      this.chanceOfSpreadRadius3 = $$3;
   }
}
