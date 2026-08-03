package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RootSystemFeature extends Feature<RootSystemConfiguration> {
   public RootSystemFeature(Codec<RootSystemConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<RootSystemConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      BlockPos $$2 = $$0.origin();
      if (!$$1.getBlockState($$2).isAir()) {
         return false;
      } else {
         RandomSource $$3 = $$0.random();
         BlockPos $$4 = $$0.origin();
         RootSystemConfiguration $$5 = $$0.config();
         MutableBlockPos $$6 = $$4.mutable();
         if (placeDirtAndTree($$1, $$0.chunkGenerator(), $$5, $$3, $$6, $$4)) {
            placeRoots($$1, $$5, $$3, $$4, $$6);
         }

         return true;
      }
   }

   private static boolean spaceForTree(net.minecraft.world.level.WorldGenLevel $$0, RootSystemConfiguration $$1, BlockPos $$2) {
      MutableBlockPos $$3 = $$2.mutable();

      for (int $$4 = 1; $$4 <= $$1.requiredVerticalSpaceForTree; $$4++) {
         $$3.move(Direction.UP);
         BlockState $$5 = $$0.getBlockState($$3);
         if (!isAllowedTreeSpace($$5, $$4, $$1.allowedVerticalWaterForTree)) {
            return false;
         }
      }

      return true;
   }

   private static boolean isAllowedTreeSpace(BlockState $$0, int $$1, int $$2) {
      if ($$0.isAir()) {
         return true;
      } else {
         int $$3 = $$1 + 1;
         return $$3 <= $$2 && $$0.getFluidState().is(FluidTags.WATER);
      }
   }

   private static boolean placeDirtAndTree(
      net.minecraft.world.level.WorldGenLevel $$0, ChunkGenerator $$1, RootSystemConfiguration $$2, RandomSource $$3, MutableBlockPos $$4, BlockPos $$5
   ) {
      for (int $$6 = 0; $$6 < $$2.rootColumnMaxHeight; $$6++) {
         $$4.move(Direction.UP);
         if ($$2.allowedTreePosition.test($$0, $$4) && spaceForTree($$0, $$2, $$4)) {
            BlockPos $$7 = $$4.below();
            if ($$0.getFluidState($$7).is(FluidTags.LAVA) || !$$0.getBlockState($$7).isSolid()) {
               return false;
            }

            if (((PlacedFeature)$$2.treeFeature.value()).place($$0, $$1, $$3, $$4)) {
               placeDirt($$5, $$5.getY() + $$6, $$0, $$2, $$3);
               return true;
            }
         }
      }

      return false;
   }

   private static void placeDirt(BlockPos $$0, int $$1, net.minecraft.world.level.WorldGenLevel $$2, RootSystemConfiguration $$3, RandomSource $$4) {
      int $$5 = $$0.getX();
      int $$6 = $$0.getZ();
      MutableBlockPos $$7 = $$0.mutable();

      for (int $$8 = $$0.getY(); $$8 < $$1; $$8++) {
         placeRootedDirt($$2, $$3, $$4, $$5, $$6, $$7.set($$5, $$8, $$6));
      }
   }

   private static void placeRootedDirt(
      net.minecraft.world.level.WorldGenLevel $$0, RootSystemConfiguration $$1, RandomSource $$2, int $$3, int $$4, MutableBlockPos $$5
   ) {
      int $$6 = $$1.rootRadius;
      Predicate<BlockState> $$7 = $$1x -> $$1x.is($$1.rootReplaceable);

      for (int $$8 = 0; $$8 < $$1.rootPlacementAttempts; $$8++) {
         $$5.setWithOffset($$5, $$2.nextInt($$6) - $$2.nextInt($$6), 0, $$2.nextInt($$6) - $$2.nextInt($$6));
         if ($$7.test($$0.getBlockState($$5))) {
            $$0.setBlock($$5, $$1.rootStateProvider.getState($$2, $$5), 2);
         }

         $$5.setX($$3);
         $$5.setZ($$4);
      }
   }

   private static void placeRoots(net.minecraft.world.level.WorldGenLevel $$0, RootSystemConfiguration $$1, RandomSource $$2, BlockPos $$3, MutableBlockPos $$4) {
      int $$5 = $$1.hangingRootRadius;
      int $$6 = $$1.hangingRootsVerticalSpan;

      for (int $$7 = 0; $$7 < $$1.hangingRootPlacementAttempts; $$7++) {
         $$4.setWithOffset($$3, $$2.nextInt($$5) - $$2.nextInt($$5), $$2.nextInt($$6) - $$2.nextInt($$6), $$2.nextInt($$5) - $$2.nextInt($$5));
         if ($$0.isEmptyBlock($$4)) {
            BlockState $$8 = $$1.hangingRootStateProvider.getState($$2, $$4);
            if ($$8.canSurvive($$0, $$4) && $$0.getBlockState($$4.above()).isFaceSturdy($$0, $$4, Direction.DOWN)) {
               $$0.setBlock($$4, $$8, 2);
            }
         }
      }
   }
}
