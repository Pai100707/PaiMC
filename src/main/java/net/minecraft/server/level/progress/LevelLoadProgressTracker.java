package net.minecraft.server.level.progress;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class LevelLoadProgressTracker implements LevelLoadListener {
   private static final int PREPARE_SERVER_WEIGHT = 10;
   private static final int EXPECTED_PLAYER_CHUNKS = Mth.square(7);
   private final boolean includePlayerChunks;
   private int totalWeight;
   private int finalizedWeight;
   private int segmentWeight;
   private float segmentFraction;
   private volatile float progress;

   public LevelLoadProgressTracker(boolean $$0) {
      this.includePlayerChunks = $$0;
   }

   @Override
   public void start(LevelLoadListener.Stage $$0, int $$1) {
      if (this.tracksStage($$0)) {
         switch ($$0) {
            case LOAD_INITIAL_CHUNKS:
               int $$2 = this.includePlayerChunks ? EXPECTED_PLAYER_CHUNKS : 0;
               this.totalWeight = 10 + $$1 + $$2;
               this.beginSegment(10);
               this.finishSegment();
               this.beginSegment($$1);
               break;
            case LOAD_PLAYER_CHUNKS:
               this.beginSegment(EXPECTED_PLAYER_CHUNKS);
         }
      }
   }

   private void beginSegment(int $$0) {
      this.segmentWeight = $$0;
      this.segmentFraction = 0.0F;
      this.updateProgress();
   }

   @Override
   public void update(LevelLoadListener.Stage $$0, int $$1, int $$2) {
      if (this.tracksStage($$0)) {
         this.segmentFraction = $$2 == 0 ? 0.0F : (float)$$1 / $$2;
         this.updateProgress();
      }
   }

   @Override
   public void finish(LevelLoadListener.Stage $$0) {
      if (this.tracksStage($$0)) {
         this.finishSegment();
      }
   }

   private void finishSegment() {
      this.finalizedWeight = this.finalizedWeight + this.segmentWeight;
      this.segmentWeight = 0;
      this.updateProgress();
   }

   private boolean tracksStage(LevelLoadListener.Stage $$0) {
      return switch ($$0) {
         case LOAD_INITIAL_CHUNKS -> true;
         case LOAD_PLAYER_CHUNKS -> this.includePlayerChunks;
         default -> false;
      };
   }

   private void updateProgress() {
      if (this.totalWeight == 0) {
         this.progress = 0.0F;
      } else {
         float $$0 = this.finalizedWeight + this.segmentFraction * this.segmentWeight;
         this.progress = $$0 / this.totalWeight;
      }
   }

   public float get() {
      return this.progress;
   }

   @Override
   public void updateFocus(ResourceKey<Level> $$0, ChunkPos $$1) {
   }
}
