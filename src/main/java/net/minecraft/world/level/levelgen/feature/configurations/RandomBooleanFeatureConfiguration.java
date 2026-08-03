package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            PlacedFeature.CODEC.fieldOf("feature_true").forGetter($$0x -> $$0x.featureTrue),
            PlacedFeature.CODEC.fieldOf("feature_false").forGetter($$0x -> $$0x.featureFalse)
         )
         .apply($$0, RandomBooleanFeatureConfiguration::new)
   );
   public final Holder<PlacedFeature> featureTrue;
   public final Holder<PlacedFeature> featureFalse;

   public RandomBooleanFeatureConfiguration(Holder<PlacedFeature> $$0, Holder<PlacedFeature> $$1) {
      this.featureTrue = $$0;
      this.featureFalse = $$1;
   }

   @Override
   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return Stream.concat(((PlacedFeature)this.featureTrue.value()).getFeatures(), ((PlacedFeature)this.featureFalse.value()).getFeatures());
   }
}
