package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;

public class BiomeGenerationSettings {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(HolderSet.direct(new Holder[0]), List.of());
   public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            ConfiguredWorldCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", LOGGER::error)).fieldOf("carvers").forGetter($$0x -> $$0x.carvers),
            PlacedFeature.LIST_OF_LISTS_CODEC.promotePartial(Util.prefix("Features: ", LOGGER::error)).fieldOf("features").forGetter($$0x -> $$0x.features)
         )
         .apply($$0, BiomeGenerationSettings::new)
   );
   private final HolderSet<ConfiguredWorldCarver<?>> carvers;
   private final List<HolderSet<PlacedFeature>> features;
   private final Supplier<List<ConfiguredFeature<?, ?>>> flowerFeatures;
   private final Supplier<Set<PlacedFeature>> featureSet;

   BiomeGenerationSettings(HolderSet<ConfiguredWorldCarver<?>> $$0, List<HolderSet<PlacedFeature>> $$1) {
      this.carvers = $$0;
      this.features = $$1;
      this.flowerFeatures = Suppliers.memoize(
         () -> $$1.stream()
            .flatMap(HolderSet::stream)
            .map(Holder::value)
            .flatMap(PlacedFeature::getFeatures)
            .filter($$0xx -> $$0xx.feature() == Feature.FLOWER)
            .collect(ImmutableList.toImmutableList())
      );
      this.featureSet = Suppliers.memoize(() -> $$1.stream().flatMap(HolderSet::stream).map(Holder::value).collect(Collectors.toSet()));
   }

   public Iterable<Holder<ConfiguredWorldCarver<?>>> getCarvers() {
      return this.carvers;
   }

   public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
      return this.flowerFeatures.get();
   }

   public List<HolderSet<PlacedFeature>> features() {
      return this.features;
   }

   public boolean hasFeature(PlacedFeature $$0) {
      return this.featureSet.get().contains($$0);
   }

   public static class Builder extends BiomeGenerationSettings.PlainBuilder {
      private final HolderGetter<PlacedFeature> placedFeatures;
      private final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers;

      public Builder(HolderGetter<PlacedFeature> $$0, HolderGetter<ConfiguredWorldCarver<?>> $$1) {
         this.placedFeatures = $$0;
         this.worldCarvers = $$1;
      }

      public BiomeGenerationSettings.Builder addFeature(GenerationStep.Decoration $$0, ResourceKey<PlacedFeature> $$1) {
         this.addFeature($$0.ordinal(), this.placedFeatures.getOrThrow($$1));
         return this;
      }

      public BiomeGenerationSettings.Builder addCarver(ResourceKey<ConfiguredWorldCarver<?>> $$0) {
         this.addCarver(this.worldCarvers.getOrThrow($$0));
         return this;
      }
   }

   public static class PlainBuilder {
      private final List<Holder<ConfiguredWorldCarver<?>>> carvers = new ArrayList<>();
      private final List<List<Holder<PlacedFeature>>> features = new ArrayList<>();

      public BiomeGenerationSettings.PlainBuilder addFeature(GenerationStep.Decoration $$0, Holder<PlacedFeature> $$1) {
         return this.addFeature($$0.ordinal(), $$1);
      }

      public BiomeGenerationSettings.PlainBuilder addFeature(int $$0, Holder<PlacedFeature> $$1) {
         this.addFeatureStepsUpTo($$0);
         this.features.get($$0).add($$1);
         return this;
      }

      public BiomeGenerationSettings.PlainBuilder addCarver(Holder<ConfiguredWorldCarver<?>> $$0) {
         this.carvers.add($$0);
         return this;
      }

      private void addFeatureStepsUpTo(int $$0) {
         while (this.features.size() <= $$0) {
            this.features.add(Lists.newArrayList());
         }
      }

      public BiomeGenerationSettings build() {
         return new BiomeGenerationSettings(
            HolderSet.direct(this.carvers), this.features.stream().map(HolderSet::direct).collect(ImmutableList.toImmutableList())
         );
      }
   }
}
