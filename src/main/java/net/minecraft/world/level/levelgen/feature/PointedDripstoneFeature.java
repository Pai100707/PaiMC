package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;

public class PointedDripstoneFeature extends Feature<PointedDripstoneConfiguration> {
   public PointedDripstoneFeature(Codec<PointedDripstoneConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<PointedDripstoneConfiguration> $$0) {
      net.minecraft.world.level.LevelAccessor $$1 = $$0.level();
      BlockPos $$2 = $$0.origin();
      RandomSource $$3 = $$0.random();
      PointedDripstoneConfiguration $$4 = $$0.config();
      Optional<Direction> $$5 = getTipDirection($$1, $$2, $$3);
      if ($$5.isEmpty()) {
         return false;
      } else {
         BlockPos $$6 = $$2.relative($$5.get().getOpposite());
         createPatchOfDripstoneBlocks($$1, $$3, $$6, $$4);
         int $$7 = $$3.nextFloat() < $$4.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater($$1.getBlockState($$2.relative($$5.get()))) ? 2 : 1;
         DripstoneUtils.growPointedDripstone($$1, $$2, $$5.get(), $$7, false);
         return true;
      }
   }

   private static Optional<Direction> getTipDirection(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, RandomSource $$2) {
      boolean $$3 = DripstoneUtils.isDripstoneBase($$0.getBlockState($$1.above()));
      boolean $$4 = DripstoneUtils.isDripstoneBase($$0.getBlockState($$1.below()));
      if ($$3 && $$4) {
         return Optional.of($$2.nextBoolean() ? Direction.DOWN : Direction.UP);
      } else if ($$3) {
         return Optional.of(Direction.DOWN);
      } else {
         return $$4 ? Optional.of(Direction.UP) : Optional.empty();
      }
   }

   private static void createPatchOfDripstoneBlocks(
      net.minecraft.world.level.LevelAccessor $$0, RandomSource $$1, BlockPos $$2, PointedDripstoneConfiguration $$3
   ) {
      DripstoneUtils.placeDripstoneBlockIfPossible($$0, $$2);

      for (Direction $$4 : Plane.HORIZONTAL) {
         if (!($$1.nextFloat() > $$3.chanceOfDirectionalSpread)) {
            BlockPos $$5 = $$2.relative($$4);
            DripstoneUtils.placeDripstoneBlockIfPossible($$0, $$5);
            if (!($$1.nextFloat() > $$3.chanceOfSpreadRadius2)) {
               BlockPos $$6 = $$5.relative(Direction.getRandom($$1));
               DripstoneUtils.placeDripstoneBlockIfPossible($$0, $$6);
               if (!($$1.nextFloat() > $$3.chanceOfSpreadRadius3)) {
                  BlockPos $$7 = $$6.relative(Direction.getRandom($$1));
                  DripstoneUtils.placeDripstoneBlockIfPossible($$0, $$7);
               }
            }
         }
      }
   }
}
