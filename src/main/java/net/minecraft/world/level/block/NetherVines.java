package net.minecraft.world.level.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class NetherVines {
   private static final double BONEMEAL_GROW_PROBABILITY_DECREASE_RATE = 0.826;
   public static final double GROW_PER_TICK_PROBABILITY = 0.1;

   public static boolean isValidGrowthState(BlockState $$0) {
      return $$0.isAir();
   }

   public static int getBlocksToGrowWhenBonemealed(RandomSource $$0) {
      double $$1 = 1.0;

      int $$2;
      for ($$2 = 0; $$0.nextDouble() < $$1; $$2++) {
         $$1 *= 0.826;
      }

      return $$2;
   }
}
