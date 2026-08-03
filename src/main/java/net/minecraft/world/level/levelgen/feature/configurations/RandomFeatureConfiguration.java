package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<RandomFeatureConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.apply2(
         RandomFeatureConfiguration::new,
         WeightedPlacedFeature.CODEC.listOf().fieldOf("features").forGetter($$0x -> $$0x.features),
         PlacedFeature.CODEC.fieldOf("default").forGetter($$0x -> $$0x.defaultFeature)
      )
   );
   public final List<WeightedPlacedFeature> features;
   public final Holder<PlacedFeature> defaultFeature;

   public RandomFeatureConfiguration(List<WeightedPlacedFeature> $$0, Holder<PlacedFeature> $$1) {
      this.features = $$0;
      this.defaultFeature = $$1;
   }

   @Override
   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return Stream.concat(
         this.features.stream().flatMap($$0 -> ((PlacedFeature)$$0.feature.value()).getFeatures()), ((PlacedFeature)this.defaultFeature.value()).getFeatures()
      );
   }
}
