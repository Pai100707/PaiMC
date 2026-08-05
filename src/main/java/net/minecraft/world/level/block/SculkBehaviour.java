package net.minecraft.world.level.block;

import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public interface SculkBehaviour {
   SculkBehaviour DEFAULT = new SculkBehaviour() {
      @Override
      public boolean attemptSpreadVein(
         net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, Collection<Direction> $$3, boolean $$4
      ) {
         if ($$3 == null) {
            return ((SculkVeinBlock)Blocks.SCULK_VEIN).getSameSpaceSpreader().spreadAll($$0.getBlockState($$1), $$0, $$1, $$4) > 0L;
         } else if (!$$3.isEmpty()) {
            return !$$2.isAir() && !$$2.getFluidState().is(Fluids.WATER) ? false : SculkVeinBlock.regrow($$0, $$1, $$2, $$3);
         } else {
            return SculkBehaviour.super.attemptSpreadVein($$0, $$1, $$2, $$3, $$4);
         }
      }

      @Override
      public int attemptUseCharge(
         SculkSpreader.ChargeCursor $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, RandomSource $$3, SculkSpreader $$4, boolean $$5
      ) {
         return $$0.getDecayDelay() > 0 ? $$0.getCharge() : 0;
      }

      @Override
      public int updateDecayDelay(int $$0) {
         return Math.max($$0 - 1, 0);
      }
   };

   default byte getSculkSpreadDelay() {
      return 1;
   }

   default void onDischarged(net.minecraft.world.level.LevelAccessor $$0, BlockState $$1, BlockPos $$2, RandomSource $$3) {
   }

   default boolean depositCharge(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, RandomSource $$2) {
      return false;
   }

   default boolean attemptSpreadVein(
      net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, Collection<Direction> $$3, boolean $$4
   ) {
      return ((MultifaceSpreadeableBlock)Blocks.SCULK_VEIN).getSpreader().spreadAll($$2, $$0, $$1, $$4) > 0L;
   }

   default boolean canChangeBlockStateOnSpread() {
      return true;
   }

   default int updateDecayDelay(int $$0) {
      return 1;
   }

   int attemptUseCharge(
      SculkSpreader.ChargeCursor var1, net.minecraft.world.level.LevelAccessor var2, BlockPos var3, RandomSource var4, SculkSpreader var5, boolean var6
   );
}
