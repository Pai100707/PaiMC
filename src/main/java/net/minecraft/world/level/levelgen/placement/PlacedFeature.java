package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.FeatureCountTracker;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record PlacedFeature(Holder<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placement) {
   public static final Codec<PlacedFeature> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ConfiguredFeature.CODEC.fieldOf("feature").forGetter($$0x -> $$0x.feature),
            PlacementModifier.CODEC.listOf().fieldOf("placement").forGetter($$0x -> $$0x.placement)
         )
         .apply($$0, PlacedFeature::new)
   );
   public static final Codec<Holder<PlacedFeature>> CODEC = RegistryFileCodec.create(Registries.PLACED_FEATURE, DIRECT_CODEC);
   public static final Codec<HolderSet<PlacedFeature>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE, DIRECT_CODEC);
   public static final Codec<List<HolderSet<PlacedFeature>>> LIST_OF_LISTS_CODEC = RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE, DIRECT_CODEC, true)
      .listOf();

   public boolean place(net.minecraft.world.level.WorldGenLevel $$0, ChunkGenerator $$1, RandomSource $$2, BlockPos $$3) {
      return this.placeWithContext(new PlacementContext($$0, $$1, Optional.empty()), $$2, $$3);
   }

   public boolean placeWithBiomeCheck(net.minecraft.world.level.WorldGenLevel $$0, ChunkGenerator $$1, RandomSource $$2, BlockPos $$3) {
      return this.placeWithContext(new PlacementContext($$0, $$1, Optional.of(this)), $$2, $$3);
   }

   private boolean placeWithContext(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      Stream<BlockPos> $$3 = Stream.of($$2);

      for (PlacementModifier $$4 : this.placement) {
         $$3 = $$3.flatMap($$3x -> $$4.getPositions($$0, $$1, $$3x));
      }

      ConfiguredFeature<?, ?> $$5 = (ConfiguredFeature<?, ?>)this.feature.value();
      MutableBoolean $$6 = new MutableBoolean();
      $$3.forEach($$4 -> {
         if ($$5.place($$0.getLevel(), $$0.generator(), $$1, $$4)) {
            $$6.setTrue();
            if (SharedConstants.DEBUG_FEATURE_COUNT) {
               FeatureCountTracker.featurePlaced($$0.getLevel().getLevel(), $$5, $$0.topFeature());
            }
         }
      });
      return $$6.isTrue();
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return ((ConfiguredFeature)this.feature.value()).getFeatures();
   }

   @Override
   public String toString() {
      return "Placed " + this.feature;
   }
}
