package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.chunk.ChunkGenerator;

public class WorldGenerationContext {
   private final int minY;
   private final int height;

   public WorldGenerationContext(ChunkGenerator $$0, net.minecraft.world.level.LevelHeightAccessor $$1) {
      this.minY = Math.max($$1.getMinY(), $$0.getMinY());
      this.height = Math.min($$1.getHeight(), $$0.getGenDepth());
   }

   public int getMinGenY() {
      return this.minY;
   }

   public int getGenDepth() {
      return this.height;
   }
}
