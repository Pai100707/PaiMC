package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;

public interface EntityBlock {
   
   BlockEntity newBlockEntity(BlockPos var1, BlockState var2);

   
   default <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return null;
   }

   
   default <T extends BlockEntity> GameEventListener getListener(ServerLevel $$0, T $$1) {
      return $$1 instanceof GameEventListener.Provider<?> $$2 ? $$2.getListener() : null;
   }
}
