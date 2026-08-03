package net.minecraft.world.level.levelgen.feature;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

public class DripstoneUtils {
   protected static double getDripstoneHeight(double $$0, double $$1, double $$2, double $$3) {
      if ($$0 < $$3) {
         $$0 = $$3;
      }

      double $$4 = 0.384;
      double $$5 = $$0 / $$1 * 0.384;
      double $$6 = 0.75 * Math.pow($$5, 1.3333333333333333);
      double $$7 = Math.pow($$5, 0.6666666666666666);
      double $$8 = 0.3333333333333333 * Math.log($$5);
      double $$9 = $$2 * ($$6 - $$7 - $$8);
      $$9 = Math.max($$9, 0.0);
      return $$9 / 0.384 * $$1;
   }

   protected static boolean isCircleMostlyEmbeddedInStone(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1, int $$2) {
      if (isEmptyOrWaterOrLava($$0, $$1)) {
         return false;
      } else {
         float $$3 = 6.0F;
         float $$4 = 6.0F / $$2;

         for (float $$5 = 0.0F; $$5 < (float) (Math.PI * 2); $$5 += $$4) {
            int $$6 = (int)(Mth.cos($$5) * $$2);
            int $$7 = (int)(Mth.sin($$5) * $$2);
            if (isEmptyOrWaterOrLava($$0, $$1.offset($$6, 0, $$7))) {
               return false;
            }
         }

         return true;
      }
   }

   protected static boolean isEmptyOrWater(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      return $$0.isStateAtPosition($$1, DripstoneUtils::isEmptyOrWater);
   }

   protected static boolean isEmptyOrWaterOrLava(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      return $$0.isStateAtPosition($$1, DripstoneUtils::isEmptyOrWaterOrLava);
   }

   protected static void buildBaseToTipColumn(Direction $$0, int $$1, boolean $$2, Consumer<BlockState> $$3) {
      if ($$1 >= 3) {
         $$3.accept(createPointedDripstone($$0, DripstoneThickness.BASE));

         for (int $$4 = 0; $$4 < $$1 - 3; $$4++) {
            $$3.accept(createPointedDripstone($$0, DripstoneThickness.MIDDLE));
         }
      }

      if ($$1 >= 2) {
         $$3.accept(createPointedDripstone($$0, DripstoneThickness.FRUSTUM));
      }

      if ($$1 >= 1) {
         $$3.accept(createPointedDripstone($$0, $$2 ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP));
      }
   }

   protected static void growPointedDripstone(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, Direction $$2, int $$3, boolean $$4) {
      if (isDripstoneBase($$0.getBlockState($$1.relative($$2.getOpposite())))) {
         MutableBlockPos $$5 = $$1.mutable();
         buildBaseToTipColumn($$2, $$3, $$4, $$3x -> {
            if ($$3x.is(Blocks.POINTED_DRIPSTONE)) {
               $$3x = $$3x.setValue(PointedDripstoneBlock.WATERLOGGED, $$0.isWaterAt($$5));
            }

            $$0.setBlock($$5, $$3x, 2);
            $$5.move($$2);
         });
      }
   }

   protected static boolean placeDripstoneBlockIfPossible(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      if ($$2.is(BlockTags.DRIPSTONE_REPLACEABLE)) {
         $$0.setBlock($$1, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
         return true;
      } else {
         return false;
      }
   }

   private static BlockState createPointedDripstone(Direction $$0, DripstoneThickness $$1) {
      return Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, $$0).setValue(PointedDripstoneBlock.THICKNESS, $$1);
   }

   public static boolean isDripstoneBaseOrLava(BlockState $$0) {
      return isDripstoneBase($$0) || $$0.is(Blocks.LAVA);
   }

   public static boolean isDripstoneBase(BlockState $$0) {
      return $$0.is(Blocks.DRIPSTONE_BLOCK) || $$0.is(BlockTags.DRIPSTONE_REPLACEABLE);
   }

   public static boolean isEmptyOrWater(BlockState $$0) {
      return $$0.isAir() || $$0.is(Blocks.WATER);
   }

   public static boolean isNeitherEmptyNorWater(BlockState $$0) {
      return !$$0.isAir() && !$$0.is(Blocks.WATER);
   }

   public static boolean isEmptyOrWaterOrLava(BlockState $$0) {
      return $$0.isAir() || $$0.is(Blocks.WATER) || $$0.is(Blocks.LAVA);
   }
}
