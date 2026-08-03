package net.minecraft.server.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkLoadCounter {
   private final List<ChunkHolder> pendingChunks = new ArrayList<>();
   private int totalChunks;

   public void track(ServerLevel $$0, Runnable $$1) {
      ServerChunkCache $$2 = $$0.getChunkSource();
      LongSet $$3 = new LongOpenHashSet();
      $$2.runDistanceManagerUpdates();
      $$2.chunkMap.allChunksWithAtLeastStatus(ChunkStatus.FULL).forEach($$1x -> $$3.add($$1x.getPos().toLong()));
      $$1.run();
      $$2.runDistanceManagerUpdates();
      $$2.chunkMap.allChunksWithAtLeastStatus(ChunkStatus.FULL).forEach($$1x -> {
         if (!$$3.contains($$1x.getPos().toLong())) {
            this.pendingChunks.add($$1x);
            this.totalChunks++;
         }
      });
   }

   public int readyChunks() {
      return this.totalChunks - this.pendingChunks();
   }

   public int pendingChunks() {
      this.pendingChunks.removeIf($$0 -> $$0.getLatestStatus() == ChunkStatus.FULL);
      return this.pendingChunks.size();
   }

   public int totalChunks() {
      return this.totalChunks;
   }
}
