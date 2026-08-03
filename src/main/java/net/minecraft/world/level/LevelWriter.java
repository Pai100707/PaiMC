package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface LevelWriter {
   boolean setBlock(BlockPos var1, BlockState var2, @Block.UpdateFlags int var3, int var4);

   default boolean setBlock(BlockPos $$0, BlockState $$1, @Block.UpdateFlags int $$2) {
      return this.setBlock($$0, $$1, $$2, 512);
   }

   boolean removeBlock(BlockPos var1, boolean var2);

   default boolean destroyBlock(BlockPos $$0, boolean $$1) {
      return this.destroyBlock($$0, $$1, null);
   }

   default boolean destroyBlock(BlockPos $$0, boolean $$1, @Nullable Entity $$2) {
      return this.destroyBlock($$0, $$1, $$2, 512);
   }

   boolean destroyBlock(BlockPos var1, boolean var2, @Nullable Entity var3, int var4);

   default boolean addFreshEntity(Entity $$0) {
      return false;
   }
}
