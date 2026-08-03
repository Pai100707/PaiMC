package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;

public class DripstoneClusterFeature extends Feature<DripstoneClusterConfiguration> {
   public DripstoneClusterFeature(Codec<DripstoneClusterConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<DripstoneClusterConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      BlockPos $$2 = $$0.origin();
      DripstoneClusterConfiguration $$3 = $$0.config();
      RandomSource $$4 = $$0.random();
      if (!DripstoneUtils.isEmptyOrWater($$1, $$2)) {
         return false;
      } else {
         int $$5 = $$3.height.sample($$4);
         float $$6 = $$3.wetness.sample($$4);
         float $$7 = $$3.density.sample($$4);
         int $$8 = $$3.radius.sample($$4);
         int $$9 = $$3.radius.sample($$4);

         for (int $$10 = -$$8; $$10 <= $$8; $$10++) {
            for (int $$11 = -$$9; $$11 <= $$9; $$11++) {
               double $$12 = this.getChanceOfStalagmiteOrStalactite($$8, $$9, $$10, $$11, $$3);
               BlockPos $$13 = $$2.offset($$10, 0, $$11);
               this.placeColumn($$1, $$4, $$13, $$10, $$11, $$6, $$12, $$5, $$7, $$3);
            }
         }

         return true;
      }
   }

   private void placeColumn(
      net.minecraft.world.level.WorldGenLevel $$0,
      RandomSource $$1,
      BlockPos $$2,
      int $$3,
      int $$4,
      float $$5,
      double $$6,
      int $$7,
      float $$8,
      DripstoneClusterConfiguration $$9
   ) {
      Optional<Column> $$10 = Column.scan($$0, $$2, $$9.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isNeitherEmptyNorWater);
      if (!$$10.isEmpty()) {
         OptionalInt $$11 = $$10.get().getCeiling();
         OptionalInt $$12 = $$10.get().getFloor();
         if (!$$11.isEmpty() || !$$12.isEmpty()) {
            boolean $$13 = $$1.nextFloat() < $$5;
            Column $$15;
            if ($$13 && $$12.isPresent() && this.canPlacePool($$0, $$2.atY($$12.getAsInt()))) {
               int $$14 = $$12.getAsInt();
               $$15 = $$10.get().withFloor(OptionalInt.of($$14 - 1));
               $$0.setBlock($$2.atY($$14), Blocks.WATER.defaultBlockState(), 2);
            } else {
               $$15 = $$10.get();
            }

            OptionalInt $$17 = $$15.getFloor();
            boolean $$18 = $$1.nextDouble() < $$6;
            int $$22;
            if ($$11.isPresent() && $$18 && !this.isLava($$0, $$2.atY($$11.getAsInt()))) {
               int $$19 = $$9.dripstoneBlockLayerThickness.sample($$1);
               this.replaceBlocksWithDripstoneBlocks($$0, $$2.atY($$11.getAsInt()), $$19, Direction.UP);
               int $$20;
               if ($$17.isPresent()) {
                  $$20 = Math.min($$7, $$11.getAsInt() - $$17.getAsInt());
               } else {
                  $$20 = $$7;
               }

               $$22 = this.getDripstoneHeight($$1, $$3, $$4, $$8, $$20, $$9);
            } else {
               $$22 = 0;
            }

            boolean $$24 = $$1.nextDouble() < $$6;
            int $$26;
            if ($$17.isPresent() && $$24 && !this.isLava($$0, $$2.atY($$17.getAsInt()))) {
               int $$25 = $$9.dripstoneBlockLayerThickness.sample($$1);
               this.replaceBlocksWithDripstoneBlocks($$0, $$2.atY($$17.getAsInt()), $$25, Direction.DOWN);
               if ($$11.isPresent()) {
                  $$26 = Math.max(0, $$22 + Mth.randomBetweenInclusive($$1, -$$9.maxStalagmiteStalactiteHeightDiff, $$9.maxStalagmiteStalactiteHeightDiff));
               } else {
                  $$26 = this.getDripstoneHeight($$1, $$3, $$4, $$8, $$7, $$9);
               }
            } else {
               $$26 = 0;
            }

            int $$36;
            int $$35;
            if ($$11.isPresent() && $$17.isPresent() && $$11.getAsInt() - $$22 <= $$17.getAsInt() + $$26) {
               int $$29 = $$17.getAsInt();
               int $$30 = $$11.getAsInt();
               int $$31 = Math.max($$30 - $$22, $$29 + 1);
               int $$32 = Math.min($$29 + $$26, $$30 - 1);
               int $$33 = Mth.randomBetweenInclusive($$1, $$31, $$32 + 1);
               int $$34 = $$33 - 1;
               $$35 = $$30 - $$33;
               $$36 = $$34 - $$29;
            } else {
               $$35 = $$22;
               $$36 = $$26;
            }

            boolean $$39 = $$1.nextBoolean() && $$35 > 0 && $$36 > 0 && $$15.getHeight().isPresent() && $$35 + $$36 == $$15.getHeight().getAsInt();
            if ($$11.isPresent()) {
               DripstoneUtils.growPointedDripstone($$0, $$2.atY($$11.getAsInt() - 1), Direction.DOWN, $$35, $$39);
            }

            if ($$17.isPresent()) {
               DripstoneUtils.growPointedDripstone($$0, $$2.atY($$17.getAsInt() + 1), Direction.UP, $$36, $$39);
            }
         }
      }
   }

   private boolean isLava(net.minecraft.world.level.LevelReader $$0, BlockPos $$1) {
      return $$0.getBlockState($$1).is(Blocks.LAVA);
   }

   private int getDripstoneHeight(RandomSource $$0, int $$1, int $$2, float $$3, int $$4, DripstoneClusterConfiguration $$5) {
      if ($$0.nextFloat() > $$3) {
         return 0;
      } else {
         int $$6 = Math.abs($$1) + Math.abs($$2);
         float $$7 = (float)Mth.clampedMap($$6, 0.0, $$5.maxDistanceFromCenterAffectingHeightBias, $$4 / 2.0, 0.0);
         return (int)randomBetweenBiased($$0, 0.0F, $$4, $$7, $$5.heightDeviation);
      }
   }

   private boolean canPlacePool(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      if (!$$2.is(Blocks.WATER) && !$$2.is(Blocks.DRIPSTONE_BLOCK) && !$$2.is(Blocks.POINTED_DRIPSTONE)) {
         if ($$0.getBlockState($$1.above()).getFluidState().is(FluidTags.WATER)) {
            return false;
         } else {
            for (Direction $$3 : Plane.HORIZONTAL) {
               if (!this.canBeAdjacentToWater($$0, $$1.relative($$3))) {
                  return false;
               }
            }

            return this.canBeAdjacentToWater($$0, $$1.below());
         }
      } else {
         return false;
      }
   }

   private boolean canBeAdjacentToWater(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      return $$2.is(BlockTags.BASE_STONE_OVERWORLD) || $$2.getFluidState().is(FluidTags.WATER);
   }

   private void replaceBlocksWithDripstoneBlocks(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1, int $$2, Direction $$3) {
      MutableBlockPos $$4 = $$1.mutable();

      for (int $$5 = 0; $$5 < $$2; $$5++) {
         if (!DripstoneUtils.placeDripstoneBlockIfPossible($$0, $$4)) {
            return;
         }

         $$4.move($$3);
      }
   }

   private double getChanceOfStalagmiteOrStalactite(int $$0, int $$1, int $$2, int $$3, DripstoneClusterConfiguration $$4) {
      int $$5 = $$0 - Math.abs($$2);
      int $$6 = $$1 - Math.abs($$3);
      int $$7 = Math.min($$5, $$6);
      return Mth.clampedMap($$7, 0.0F, $$4.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn, $$4.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0F);
   }

   private static float randomBetweenBiased(RandomSource $$0, float $$1, float $$2, float $$3, float $$4) {
      return ClampedNormalFloat.sample($$0, $$3, $$4, $$1, $$2);
   }
}
