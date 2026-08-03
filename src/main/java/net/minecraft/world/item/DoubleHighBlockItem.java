package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleHighBlockItem extends net.minecraft.world.item.BlockItem {
   public DoubleHighBlockItem(Block $$0, net.minecraft.world.item.Item.Properties $$1) {
      super($$0, $$1);
   }

   @Override
   protected boolean placeBlock(BlockPlaceContext $$0, BlockState $$1) {
      Level $$2 = $$0.getLevel();
      BlockPos $$3 = $$0.getClickedPos().above();
      BlockState $$4 = $$2.isWaterAt($$3) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
      $$2.setBlock($$3, $$4, 27);
      return super.placeBlock($$0, $$1);
   }
}
