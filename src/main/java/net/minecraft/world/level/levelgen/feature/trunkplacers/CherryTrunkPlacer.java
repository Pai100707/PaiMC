package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class CherryTrunkPlacer extends TrunkPlacer {
   private static final Codec<UniformInt> BRANCH_START_CODEC = UniformInt.CODEC
      .codec()
      .validate(
         $$0 -> $$0.getMaxValue() - $$0.getMinValue() < 1
            ? DataResult.error(() -> "Need at least 2 blocks variation for the branch starts to fit both branches")
            : DataResult.success($$0)
      );
   public static final MapCodec<CherryTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> trunkPlacerParts($$0)
         .and(
            $$0.group(
               IntProvider.codec(1, 3).fieldOf("branch_count").forGetter($$0x -> $$0x.branchCount),
               IntProvider.codec(2, 16).fieldOf("branch_horizontal_length").forGetter($$0x -> $$0x.branchHorizontalLength),
               IntProvider.validateCodec(-16, 0, BRANCH_START_CODEC).fieldOf("branch_start_offset_from_top").forGetter($$0x -> $$0x.branchStartOffsetFromTop),
               IntProvider.codec(-16, 16).fieldOf("branch_end_offset_from_top").forGetter($$0x -> $$0x.branchEndOffsetFromTop)
            )
         )
         .apply($$0, CherryTrunkPlacer::new)
   );
   private final IntProvider branchCount;
   private final IntProvider branchHorizontalLength;
   private final UniformInt branchStartOffsetFromTop;
   private final UniformInt secondBranchStartOffsetFromTop;
   private final IntProvider branchEndOffsetFromTop;

   public CherryTrunkPlacer(int $$0, int $$1, int $$2, IntProvider $$3, IntProvider $$4, UniformInt $$5, IntProvider $$6) {
      super($$0, $$1, $$2);
      this.branchCount = $$3;
      this.branchHorizontalLength = $$4;
      this.branchStartOffsetFromTop = $$5;
      this.secondBranchStartOffsetFromTop = UniformInt.of($$5.getMinValue(), $$5.getMaxValue() - 1);
      this.branchEndOffsetFromTop = $$6;
   }

   @Override
   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.CHERRY_TRUNK_PLACER;
   }

   @Override
   public List<FoliagePlacer.FoliageAttachment> placeTrunk(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, int $$3, BlockPos $$4, TreeConfiguration $$5
   ) {
      setDirtAt($$0, $$1, $$2, $$4.below(), $$5);
      int $$6 = Math.max(0, $$3 - 1 + this.branchStartOffsetFromTop.sample($$2));
      int $$7 = Math.max(0, $$3 - 1 + this.secondBranchStartOffsetFromTop.sample($$2));
      if ($$7 >= $$6) {
         $$7++;
      }

      int $$8 = this.branchCount.sample($$2);
      boolean $$9 = $$8 == 3;
      boolean $$10 = $$8 >= 2;
      int $$11;
      if ($$9) {
         $$11 = $$3;
      } else if ($$10) {
         $$11 = Math.max($$6, $$7) + 1;
      } else {
         $$11 = $$6 + 1;
      }

      for (int $$14 = 0; $$14 < $$11; $$14++) {
         this.placeLog($$0, $$1, $$2, $$4.above($$14), $$5);
      }

      List<FoliagePlacer.FoliageAttachment> $$15 = new ArrayList<>();
      if ($$9) {
         $$15.add(new FoliagePlacer.FoliageAttachment($$4.above($$11), 0, false));
      }

      MutableBlockPos $$16 = new MutableBlockPos();
      Direction $$17 = Plane.HORIZONTAL.getRandomDirection($$2);
      Function<BlockState, BlockState> $$18 = $$1x -> $$1x.trySetValue(RotatedPillarBlock.AXIS, $$17.getAxis());
      $$15.add(this.generateBranch($$0, $$1, $$2, $$3, $$4, $$5, $$18, $$17, $$6, $$6 < $$11 - 1, $$16));
      if ($$10) {
         $$15.add(this.generateBranch($$0, $$1, $$2, $$3, $$4, $$5, $$18, $$17.getOpposite(), $$7, $$7 < $$11 - 1, $$16));
      }

      return $$15;
   }

   private FoliagePlacer.FoliageAttachment generateBranch(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      BiConsumer<BlockPos, BlockState> $$1,
      RandomSource $$2,
      int $$3,
      BlockPos $$4,
      TreeConfiguration $$5,
      Function<BlockState, BlockState> $$6,
      Direction $$7,
      int $$8,
      boolean $$9,
      MutableBlockPos $$10
   ) {
      $$10.set($$4).move(Direction.UP, $$8);
      int $$11 = $$3 - 1 + this.branchEndOffsetFromTop.sample($$2);
      boolean $$12 = $$9 || $$11 < $$8;
      int $$13 = this.branchHorizontalLength.sample($$2) + ($$12 ? 1 : 0);
      BlockPos $$14 = $$4.relative($$7, $$13).above($$11);
      int $$15 = $$12 ? 2 : 1;

      for (int $$16 = 0; $$16 < $$15; $$16++) {
         this.placeLog($$0, $$1, $$2, $$10.move($$7), $$5, $$6);
      }

      Direction $$17 = $$14.getY() > $$10.getY() ? Direction.UP : Direction.DOWN;

      while (true) {
         int $$18 = $$10.distManhattan($$14);
         if ($$18 == 0) {
            return new FoliagePlacer.FoliageAttachment($$14.above(), 0, false);
         }

         float $$19 = (float)Math.abs($$14.getY() - $$10.getY()) / $$18;
         boolean $$20 = $$2.nextFloat() < $$19;
         $$10.move($$20 ? $$17 : $$7);
         this.placeLog($$0, $$1, $$2, $$10, $$5, $$20 ? Function.identity() : $$6);
      }
   }
}
