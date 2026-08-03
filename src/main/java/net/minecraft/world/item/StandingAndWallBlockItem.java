package net.minecraft.world.item;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public class StandingAndWallBlockItem extends net.minecraft.world.item.BlockItem {
   protected final Block wallBlock;
   private final Direction attachmentDirection;

   public StandingAndWallBlockItem(Block $$0, Block $$1, Direction $$2, net.minecraft.world.item.Item.Properties $$3) {
      super($$0, $$3);
      this.wallBlock = $$1;
      this.attachmentDirection = $$2;
   }

   protected boolean canPlace(LevelReader $$0, BlockState $$1, BlockPos $$2) {
      return $$1.canSurvive($$0, $$2);
   }

   @Nullable
   @Override
   protected BlockState getPlacementState(BlockPlaceContext $$0) {
      BlockState $$1 = this.wallBlock.getStateForPlacement($$0);
      BlockState $$2 = null;
      LevelReader $$3 = $$0.getLevel();
      BlockPos $$4 = $$0.getClickedPos();

      for (Direction $$5 : $$0.getNearestLookingDirections()) {
         if ($$5 != this.attachmentDirection.getOpposite()) {
            BlockState $$6 = $$5 == this.attachmentDirection ? this.getBlock().getStateForPlacement($$0) : $$1;
            if ($$6 != null && this.canPlace($$3, $$6, $$4)) {
               $$2 = $$6;
               break;
            }
         }
      }

      return $$2 != null && $$3.isUnobstructed($$2, $$4, CollisionContext.empty()) ? $$2 : null;
   }

   @Override
   public void registerBlocks(Map<Block, net.minecraft.world.item.Item> $$0, net.minecraft.world.item.Item $$1) {
      super.registerBlocks($$0, $$1);
      $$0.put(this.wallBlock, $$1);
   }
}
