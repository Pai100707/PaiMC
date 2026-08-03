package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignItem extends net.minecraft.world.item.SignItem {
   public HangingSignItem(Block $$0, Block $$1, net.minecraft.world.item.Item.Properties $$2) {
      super($$2, $$0, $$1, Direction.UP);
   }

   @Override
   protected boolean canPlace(LevelReader $$0, BlockState $$1, BlockPos $$2) {
      return $$1.getBlock() instanceof WallHangingSignBlock $$3 && !$$3.canPlace($$1, $$0, $$2) ? false : super.canPlace($$0, $$1, $$2);
   }
}
