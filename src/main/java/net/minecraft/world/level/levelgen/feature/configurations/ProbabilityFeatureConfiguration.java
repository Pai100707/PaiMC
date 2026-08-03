package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ProbabilityFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<ProbabilityFeatureConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter($$0x -> $$0x.probability))
         .apply($$0, ProbabilityFeatureConfiguration::new)
   );
   public final float probability;

   public ProbabilityFeatureConfiguration(float $$0) {
      this.probability = $$0;
   }
}
