package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class BendingTrunkPlacer extends TrunkPlacer {
   public static final MapCodec<BendingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> trunkPlacerParts($$0)
         .and(
            $$0.group(
               ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_height_for_leaves", 1).forGetter($$0x -> $$0x.minHeightForLeaves),
               IntProvider.codec(1, 64).fieldOf("bend_length").forGetter($$0x -> $$0x.bendLength)
            )
         )
         .apply($$0, BendingTrunkPlacer::new)
   );
   private final int minHeightForLeaves;
   private final IntProvider bendLength;

   public BendingTrunkPlacer(int $$0, int $$1, int $$2, int $$3, IntProvider $$4) {
      super($$0, $$1, $$2);
      this.minHeightForLeaves = $$3;
      this.bendLength = $$4;
   }

   @Override
   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.BENDING_TRUNK_PLACER;
   }

   @Override
   public List<FoliagePlacer.FoliageAttachment> placeTrunk(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, int $$3, BlockPos $$4, TreeConfiguration $$5
   ) {
      Direction $$6 = Plane.HORIZONTAL.getRandomDirection($$2);
      int $$7 = $$3 - 1;
      MutableBlockPos $$8 = $$4.mutable();
      BlockPos $$9 = $$8.below();
      setDirtAt($$0, $$1, $$2, $$9, $$5);
      List<FoliagePlacer.FoliageAttachment> $$10 = Lists.newArrayList();

      for (int $$11 = 0; $$11 <= $$7; $$11++) {
         if ($$11 + 1 >= $$7 + $$2.nextInt(2)) {
            $$8.move($$6);
         }

         if (TreeFeature.validTreePos($$0, $$8)) {
            this.placeLog($$0, $$1, $$2, $$8, $$5);
         }

         if ($$11 >= this.minHeightForLeaves) {
            $$10.add(new FoliagePlacer.FoliageAttachment($$8.immutable(), 0, false));
         }

         $$8.move(Direction.UP);
      }

      int $$12 = this.bendLength.sample($$2);

      for (int $$13 = 0; $$13 <= $$12; $$13++) {
         if (TreeFeature.validTreePos($$0, $$8)) {
            this.placeLog($$0, $$1, $$2, $$8, $$5);
         }

         $$10.add(new FoliagePlacer.FoliageAttachment($$8.immutable(), 0, false));
         $$8.move($$6);
      }

      return $$10;
   }
}
