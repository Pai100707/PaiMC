package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.jspecify.annotations.Nullable;

public interface SpawnPlacementType {
   boolean isSpawnPositionOk(LevelReader var1, BlockPos var2, @Nullable net.minecraft.world.entity.EntityType<?> var3);

   default BlockPos adjustSpawnPosition(LevelReader $$0, BlockPos $$1) {
      return $$1;
   }
}
