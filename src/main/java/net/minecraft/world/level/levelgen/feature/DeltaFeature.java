package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;

public class DeltaFeature extends Feature<DeltaFeatureConfiguration> {
   private static final ImmutableList<Block> CANNOT_REPLACE = ImmutableList.of(
      Blocks.BEDROCK, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER
   );
   private static final Direction[] DIRECTIONS = Direction.values();
   private static final double RIM_SPAWN_CHANCE = 0.9;

   public DeltaFeature(Codec<DeltaFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<DeltaFeatureConfiguration> $$0) {
      boolean $$1 = false;
      RandomSource $$2 = $$0.random();
      net.minecraft.world.level.WorldGenLevel $$3 = $$0.level();
      DeltaFeatureConfiguration $$4 = $$0.config();
      BlockPos $$5 = $$0.origin();
      boolean $$6 = $$2.nextDouble() < 0.9;
      int $$7 = $$6 ? $$4.rimSize().sample($$2) : 0;
      int $$8 = $$6 ? $$4.rimSize().sample($$2) : 0;
      boolean $$9 = $$6 && $$7 != 0 && $$8 != 0;
      int $$10 = $$4.size().sample($$2);
      int $$11 = $$4.size().sample($$2);
      int $$12 = Math.max($$10, $$11);

      for (BlockPos $$13 : BlockPos.withinManhattan($$5, $$10, 0, $$11)) {
         if ($$13.distManhattan($$5) > $$12) {
            break;
         }

         if (isClear($$3, $$13, $$4)) {
            if ($$9) {
               $$1 = true;
               this.setBlock($$3, $$13, $$4.rim());
            }

            BlockPos $$14 = $$13.offset($$7, 0, $$8);
            if (isClear($$3, $$14, $$4)) {
               $$1 = true;
               this.setBlock($$3, $$14, $$4.contents());
            }
         }
      }

      return $$1;
   }

   private static boolean isClear(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, DeltaFeatureConfiguration $$2) {
      BlockState $$3 = $$0.getBlockState($$1);
      if ($$3.is($$2.contents().getBlock())) {
         return false;
      } else if (CANNOT_REPLACE.contains($$3.getBlock())) {
         return false;
      } else {
         for (Direction $$4 : DIRECTIONS) {
            boolean $$5 = $$0.getBlockState($$1.relative($$4)).isAir();
            if ($$5 && $$4 != Direction.UP || !$$5 && $$4 == Direction.UP) {
               return false;
            }
         }

         return true;
      }
   }
}
