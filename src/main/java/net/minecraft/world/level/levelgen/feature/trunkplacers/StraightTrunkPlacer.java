package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class StraightTrunkPlacer extends TrunkPlacer {
   public static final MapCodec<StraightTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec($$0 -> trunkPlacerParts($$0).apply($$0, StraightTrunkPlacer::new));

   public StraightTrunkPlacer(int $$0, int $$1, int $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.STRAIGHT_TRUNK_PLACER;
   }

   @Override
   public List<FoliagePlacer.FoliageAttachment> placeTrunk(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, int $$3, BlockPos $$4, TreeConfiguration $$5
   ) {
      setDirtAt($$0, $$1, $$2, $$4.below(), $$5);

      for (int $$6 = 0; $$6 < $$3; $$6++) {
         this.placeLog($$0, $$1, $$2, $$4.above($$6), $$5);
      }

      return ImmutableList.of(new FoliagePlacer.FoliageAttachment($$4.above($$3), 0, false));
   }
}
