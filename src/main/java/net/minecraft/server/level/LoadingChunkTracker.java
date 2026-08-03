package net.minecraft.server.level;

import net.minecraft.world.level.TicketStorage;

class LoadingChunkTracker extends ChunkTracker {
   private static final int MAX_LEVEL = ChunkLevel.MAX_LEVEL + 1;
   private final DistanceManager distanceManager;
   private final TicketStorage ticketStorage;

   public LoadingChunkTracker(DistanceManager $$0, TicketStorage $$1) {
      super(MAX_LEVEL + 1, 16, 256);
      this.distanceManager = $$0;
      this.ticketStorage = $$1;
      $$1.setLoadingChunkUpdatedListener(this::update);
   }

   @Override
   protected int getLevelFromSource(long $$0) {
      return this.ticketStorage.getTicketLevelAt($$0, false);
   }

   protected int getLevel(long $$0) {
      if (!this.distanceManager.isChunkToRemove($$0)) {
         ChunkHolder $$1 = this.distanceManager.getChunk($$0);
         if ($$1 != null) {
            return $$1.getTicketLevel();
         }
      }

      return MAX_LEVEL;
   }

   protected void setLevel(long $$0, int $$1) {
      ChunkHolder $$2 = this.distanceManager.getChunk($$0);
      int $$3 = $$2 == null ? MAX_LEVEL : $$2.getTicketLevel();
      if ($$3 != $$1) {
         $$2 = this.distanceManager.updateChunkScheduling($$0, $$1, $$2, $$3);
         if ($$2 != null) {
            this.distanceManager.chunksToUpdateFutures.add($$2);
         }
      }
   }

   public int runDistanceUpdates(int $$0) {
      return this.runUpdates($$0);
   }
}
