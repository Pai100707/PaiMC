package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import org.jspecify.annotations.Nullable;

public interface LayerLightEventListener extends LightEventListener {
   @Nullable
   DataLayer getDataLayerData(SectionPos var1);

   int getLightValue(BlockPos var1);

   public static enum DummyLightLayerEventListener implements LayerLightEventListener {
      INSTANCE;

      @Nullable
      @Override
      public DataLayer getDataLayerData(SectionPos $$0) {
         return null;
      }

      @Override
      public int getLightValue(BlockPos $$0) {
         return 0;
      }

      @Override
      public void checkBlock(BlockPos $$0) {
      }

      @Override
      public boolean hasLightWork() {
         return false;
      }

      @Override
      public int runLightUpdates() {
         return 0;
      }

      @Override
      public void updateSectionStatus(SectionPos $$0, boolean $$1) {
      }

      @Override
      public void setLightEnabled(net.minecraft.world.level.ChunkPos $$0, boolean $$1) {
      }

      @Override
      public void propagateLightSources(net.minecraft.world.level.ChunkPos $$0) {
      }
   }
}
