package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class GiantTrunkPlacer extends TrunkPlacer {
   public static final MapCodec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec($$0 -> trunkPlacerParts($$0).apply($$0, GiantTrunkPlacer::new));

   public GiantTrunkPlacer(int $$0, int $$1, int $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.GIANT_TRUNK_PLACER;
   }

   @Override
   public List<FoliagePlacer.FoliageAttachment> placeTrunk(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, int $$3, BlockPos $$4, TreeConfiguration $$5
   ) {
      BlockPos $$6 = $$4.below();
      setDirtAt($$0, $$1, $$2, $$6, $$5);
      setDirtAt($$0, $$1, $$2, $$6.east(), $$5);
      setDirtAt($$0, $$1, $$2, $$6.south(), $$5);
      setDirtAt($$0, $$1, $$2, $$6.south().east(), $$5);
      MutableBlockPos $$7 = new MutableBlockPos();

      for (int $$8 = 0; $$8 < $$3; $$8++) {
         this.placeLogIfFreeWithOffset($$0, $$1, $$2, $$7, $$5, $$4, 0, $$8, 0);
         if ($$8 < $$3 - 1) {
            this.placeLogIfFreeWithOffset($$0, $$1, $$2, $$7, $$5, $$4, 1, $$8, 0);
            this.placeLogIfFreeWithOffset($$0, $$1, $$2, $$7, $$5, $$4, 1, $$8, 1);
            this.placeLogIfFreeWithOffset($$0, $$1, $$2, $$7, $$5, $$4, 0, $$8, 1);
         }
      }

      return ImmutableList.of(new FoliagePlacer.FoliageAttachment($$4.above($$3), 0, true));
   }

   private void placeLogIfFreeWithOffset(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      BiConsumer<BlockPos, BlockState> $$1,
      RandomSource $$2,
      MutableBlockPos $$3,
      TreeConfiguration $$4,
      BlockPos $$5,
      int $$6,
      int $$7,
      int $$8
   ) {
      $$3.setWithOffset($$5, $$6, $$7, $$8);
      this.placeLogIfFree($$0, $$1, $$2, $$3, $$4);
   }
}
