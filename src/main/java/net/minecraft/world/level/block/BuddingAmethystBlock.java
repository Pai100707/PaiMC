package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingAmethystBlock extends AmethystBlock {
   public static final MapCodec<BuddingAmethystBlock> CODEC = simpleCodec(BuddingAmethystBlock::new);
   public static final int GROWTH_CHANCE = 5;
   private static final Direction[] DIRECTIONS = Direction.values();

   @Override
   public MapCodec<BuddingAmethystBlock> codec() {
      return CODEC;
   }

   public BuddingAmethystBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$3.nextInt(5) == 0) {
         Direction $$4 = DIRECTIONS[$$3.nextInt(DIRECTIONS.length)];
         BlockPos $$5 = $$2.relative($$4);
         BlockState $$6 = $$1.getBlockState($$5);
         Block $$7 = null;
         if (canClusterGrowAtState($$6)) {
            $$7 = Blocks.SMALL_AMETHYST_BUD;
         } else if ($$6.is(Blocks.SMALL_AMETHYST_BUD) && $$6.getValue(AmethystClusterBlock.FACING) == $$4) {
            $$7 = Blocks.MEDIUM_AMETHYST_BUD;
         } else if ($$6.is(Blocks.MEDIUM_AMETHYST_BUD) && $$6.getValue(AmethystClusterBlock.FACING) == $$4) {
            $$7 = Blocks.LARGE_AMETHYST_BUD;
         } else if ($$6.is(Blocks.LARGE_AMETHYST_BUD) && $$6.getValue(AmethystClusterBlock.FACING) == $$4) {
            $$7 = Blocks.AMETHYST_CLUSTER;
         }

         if ($$7 != null) {
            BlockState $$8 = $$7.defaultBlockState()
               .setValue(AmethystClusterBlock.FACING, $$4)
               .setValue(AmethystClusterBlock.WATERLOGGED, $$6.getFluidState().getType() == Fluids.WATER);
            $$1.setBlockAndUpdate($$5, $$8);
         }
      }
   }

   public static boolean canClusterGrowAtState(BlockState $$0) {
      return $$0.isAir() || $$0.is(Blocks.WATER) && $$0.getFluidState().getAmount() == 8;
   }
}
