package net.minecraft.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ScaffoldingBlockItem extends net.minecraft.world.item.BlockItem {
   public ScaffoldingBlockItem(Block $$0, net.minecraft.world.item.Item.Properties $$1) {
      super($$0, $$1);
   }

   
   @Override
   public BlockPlaceContext updatePlacementContext(BlockPlaceContext $$0) {
      BlockPos $$1 = $$0.getClickedPos();
      Level $$2 = $$0.getLevel();
      BlockState $$3 = $$2.getBlockState($$1);
      Block $$4 = this.getBlock();
      if (!$$3.is($$4)) {
         return ScaffoldingBlock.getDistance($$2, $$1) == 7 ? null : $$0;
      } else {
         Direction $$5;
         if ($$0.isSecondaryUseActive()) {
            $$5 = $$0.isInside() ? $$0.getClickedFace().getOpposite() : $$0.getClickedFace();
         } else {
            $$5 = $$0.getClickedFace() == Direction.UP ? $$0.getHorizontalDirection() : Direction.UP;
         }

         int $$7 = 0;
         MutableBlockPos $$8 = $$1.mutable().move($$5);

         while ($$7 < 7) {
            if (!$$2.isClientSide() && !$$2.isInWorldBounds($$8)) {
               Player $$9 = $$0.getPlayer();
               int $$10 = $$2.getMaxY();
               if ($$9 instanceof ServerPlayer && $$8.getY() > $$10) {
                  ((ServerPlayer)$$9).sendSystemMessage(Component.translatable("build.tooHigh", new Object[]{$$10}).withStyle(ChatFormatting.RED), true);
               }
               break;
            }

            $$3 = $$2.getBlockState($$8);
            if (!$$3.is(this.getBlock())) {
               if ($$3.canBeReplaced($$0)) {
                  return BlockPlaceContext.at($$0, $$8, $$5);
               }
               break;
            }

            $$8.move($$5);
            if ($$5.getAxis().isHorizontal()) {
               $$7++;
            }
         }

         return null;
      }
   }

   @Override
   protected boolean mustSurvive() {
      return false;
   }
}
