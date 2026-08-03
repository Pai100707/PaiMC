package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class GameMasterBlockItem extends net.minecraft.world.item.BlockItem {
   public GameMasterBlockItem(Block $$0, net.minecraft.world.item.Item.Properties $$1) {
      super($$0, $$1);
   }

   @Nullable
   @Override
   protected BlockState getPlacementState(BlockPlaceContext $$0) {
      Player $$1 = $$0.getPlayer();
      return $$1 != null && !$$1.canUseGameMasterBlocks() ? null : super.getPlacementState($$0);
   }
}
