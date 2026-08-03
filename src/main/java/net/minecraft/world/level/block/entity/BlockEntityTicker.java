package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BlockEntityTicker<T extends BlockEntity> {
   void tick(net.minecraft.world.level.Level var1, BlockPos var2, BlockState var3, T var4);
}
