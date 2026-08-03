package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LightEventListener {
   void checkBlock(BlockPos var1);

   boolean hasLightWork();

   int runLightUpdates();

   default void updateSectionStatus(BlockPos $$0, boolean $$1) {
      this.updateSectionStatus(SectionPos.of($$0), $$1);
   }

   void updateSectionStatus(SectionPos var1, boolean var2);

   void setLightEnabled(net.minecraft.world.level.ChunkPos var1, boolean var2);

   void propagateLightSources(net.minecraft.world.level.ChunkPos var1);
}
