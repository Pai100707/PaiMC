package net.minecraft.world.level;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;

public interface WorldGenLevel extends net.minecraft.world.level.ServerLevelAccessor {
   long getSeed();

   default boolean ensureCanWrite(BlockPos $$0) {
      return true;
   }

   default void setCurrentlyGenerating(Supplier<String> $$0) {
   }
}
