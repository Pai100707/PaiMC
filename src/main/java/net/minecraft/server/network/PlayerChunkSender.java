package net.minecraft.server.network;

import com.google.common.collect.Comparators;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.SharedConstants;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public class PlayerChunkSender {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final float MIN_CHUNKS_PER_TICK = 0.01F;
   public static final float MAX_CHUNKS_PER_TICK = 64.0F;
   private static final float START_CHUNKS_PER_TICK = 9.0F;
   private static final int MAX_UNACKNOWLEDGED_BATCHES = 10;
   private final LongSet pendingChunks = new LongOpenHashSet();
   private final boolean memoryConnection;
   private float desiredChunksPerTick = 9.0F;
   private float batchQuota;
   private int unacknowledgedBatches;
   private int maxUnacknowledgedBatches = 1;

   public PlayerChunkSender(boolean $$0) {
      this.memoryConnection = $$0;
   }

   public void markChunkPendingToSend(LevelChunk $$0) {
      this.pendingChunks.add($$0.getPos().toLong());
   }

   public void dropChunk(ServerPlayer $$0, ChunkPos $$1) {
      if (!this.pendingChunks.remove($$1.toLong()) && $$0.isAlive()) {
         $$0.connection.send(new ClientboundForgetLevelChunkPacket($$1));
      }
   }

   public void sendNextChunks(ServerPlayer $$0) {
      if (this.unacknowledgedBatches < this.maxUnacknowledgedBatches) {
         float $$1 = Math.max(1.0F, this.desiredChunksPerTick);
         this.batchQuota = Math.min(this.batchQuota + this.desiredChunksPerTick, $$1);
         if (!(this.batchQuota < 1.0F)) {
            if (!this.pendingChunks.isEmpty()) {
               ServerLevel $$2 = $$0.level();
               ChunkMap $$3 = $$2.getChunkSource().chunkMap;
               List<LevelChunk> $$4 = this.collectChunksToSend($$3, $$0.chunkPosition());
               if (!$$4.isEmpty()) {
                  ServerGamePacketListenerImpl $$5 = $$0.connection;
                  this.unacknowledgedBatches++;
                  $$5.send(ClientboundChunkBatchStartPacket.INSTANCE);

                  for (LevelChunk $$6 : $$4) {
                     sendChunk($$5, $$2, $$6);
                  }

                  $$5.send(new ClientboundChunkBatchFinishedPacket($$4.size()));
                  this.batchQuota = this.batchQuota - $$4.size();
               }
            }
         }
      }
   }

   private static void sendChunk(ServerGamePacketListenerImpl $$0, ServerLevel $$1, LevelChunk $$2) {
      $$0.send(new ClientboundLevelChunkWithLightPacket($$2, $$1.getLightEngine(), null, null));
      ChunkPos $$3 = $$2.getPos();
      if (SharedConstants.DEBUG_VERBOSE_SERVER_EVENTS) {
         LOGGER.debug("SEN {}", $$3);
      }

      $$1.debugSynchronizers().startTrackingChunk($$0.player, $$2.getPos());
   }

   private List<LevelChunk> collectChunksToSend(ChunkMap $$0, ChunkPos $$1) {
      int $$2 = Mth.floor(this.batchQuota);
      List<LevelChunk> $$4;
      if (!this.memoryConnection && this.pendingChunks.size() > $$2) {
         $$4 = this.pendingChunks
            .stream()
            .collect(Comparators.least($$2, Comparator.comparingInt($$1::distanceSquared)))
            .stream()
            .mapToLong(Long::longValue)
            .mapToObj($$0::getChunkToSend)
            .filter(Objects::nonNull)
            .toList();
      } else {
         $$4 = this.pendingChunks
            .longStream()
            .mapToObj($$0::getChunkToSend)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt($$1x -> $$1.distanceSquared($$1x.getPos())))
            .toList();
      }

      for (LevelChunk $$5 : $$4) {
         this.pendingChunks.remove($$5.getPos().toLong());
      }

      return $$4;
   }

   public void onChunkBatchReceivedByClient(float $$0) {
      this.unacknowledgedBatches--;
      this.desiredChunksPerTick = Double.isNaN($$0) ? 0.01F : Mth.clamp($$0, 0.01F, 64.0F);
      if (this.unacknowledgedBatches == 0) {
         this.batchQuota = 1.0F;
      }

      this.maxUnacknowledgedBatches = 10;
   }

   public boolean isPending(long $$0) {
      return this.pendingChunks.contains($$0);
   }
}
