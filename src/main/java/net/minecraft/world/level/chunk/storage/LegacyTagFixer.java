package net.minecraft.world.level.chunk.storage;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;

@FunctionalInterface
public interface LegacyTagFixer {
   Supplier<LegacyTagFixer> EMPTY = () -> $$0 -> $$0;

   CompoundTag applyFix(CompoundTag var1);

   default void markChunkDone(net.minecraft.world.level.ChunkPos $$0) {
   }

   default int targetDataVersion() {
      return -1;
   }
}
