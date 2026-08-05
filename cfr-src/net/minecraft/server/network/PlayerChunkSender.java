/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Comparators
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.collect.Comparators;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.lang.invoke.LambdaMetafactory;
import java.util.Comparator;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import net.minecraft.SharedConstants;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public class PlayerChunkSender {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final float MIN_CHUNKS_PER_TICK = 0.01f;
    public static final float MAX_CHUNKS_PER_TICK = 64.0f;
    private static final float START_CHUNKS_PER_TICK = 9.0f;
    private static final int MAX_UNACKNOWLEDGED_BATCHES = 10;
    private final LongSet pendingChunks = new LongOpenHashSet();
    private final boolean memoryConnection;
    private float desiredChunksPerTick = 9.0f;
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
        if (this.unacknowledgedBatches >= this.maxUnacknowledgedBatches) {
            return;
        }
        float $$1 = Math.max(1.0f, this.desiredChunksPerTick);
        this.batchQuota = Math.min(this.batchQuota + this.desiredChunksPerTick, $$1);
        if (this.batchQuota < 1.0f) {
            return;
        }
        if (this.pendingChunks.isEmpty()) {
            return;
        }
        ServerLevel $$2 = $$0.level();
        ChunkMap $$3 = $$2.getChunkSource().chunkMap;
        List<LevelChunk> $$4 = this.collectChunksToSend($$3, $$0.chunkPosition());
        if ($$4.isEmpty()) {
            return;
        }
        ServerGamePacketListenerImpl $$5 = $$0.connection;
        ++this.unacknowledgedBatches;
        $$5.send(ClientboundChunkBatchStartPacket.INSTANCE);
        for (LevelChunk $$6 : $$4) {
            PlayerChunkSender.sendChunk($$5, $$2, $$6);
        }
        $$5.send(new ClientboundChunkBatchFinishedPacket($$4.size()));
        this.batchQuota -= (float)$$4.size();
    }

    private static void sendChunk(ServerGamePacketListenerImpl $$0, ServerLevel $$1, LevelChunk $$2) {
        $$0.send(new ClientboundLevelChunkWithLightPacket($$2, $$1.getLightEngine(), null, null));
        ChunkPos $$3 = $$2.getPos();
        if (SharedConstants.DEBUG_VERBOSE_SERVER_EVENTS) {
            LOGGER.debug("SEN {}", (Object)$$3);
        }
        $$1.debugSynchronizers().startTrackingChunk($$0.player, $$2.getPos());
    }

    /*
     * Unable to fully structure code
     */
    private List<LevelChunk> collectChunksToSend(ChunkMap $$0, ChunkPos $$1) {
        $$2 = Mth.floor(this.batchQuota);
        if (this.memoryConnection) ** GOTO lbl7
        if (this.pendingChunks.size() <= $$2) {
lbl7:
            // 2 sources

            $$3 = this.pendingChunks.longStream().mapToObj((LongFunction<LevelChunk>)LambdaMetafactory.metafactory(null, null, null, (J)Ljava/lang/Object;, getChunkToSend(long ), (J)Lnet/minecraft/world/level/chunk/LevelChunk;)((ChunkMap)$$0)).filter((Predicate<LevelChunk>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, nonNull(java.lang.Object ), (Lnet/minecraft/world/level/chunk/LevelChunk;)Z)()).sorted(Comparator.comparingInt((ToIntFunction<LevelChunk>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)I, lambda$collectChunksToSend$0(net.minecraft.world.level.ChunkPos net.minecraft.world.level.chunk.LevelChunk ), (Lnet/minecraft/world/level/chunk/LevelChunk;)I)((ChunkPos)$$1))).toList();
        } else {
            $$4 = ((List)this.pendingChunks.stream().collect(Comparators.least((int)$$2, Comparator.comparingInt((ToIntFunction<Long>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)I, distanceSquared(long ), (Ljava/lang/Long;)I)((ChunkPos)$$1))))).stream().mapToLong((ToLongFunction<Long>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)J, longValue(), (Ljava/lang/Long;)J)()).mapToObj((LongFunction<LevelChunk>)LambdaMetafactory.metafactory(null, null, null, (J)Ljava/lang/Object;, getChunkToSend(long ), (J)Lnet/minecraft/world/level/chunk/LevelChunk;)((ChunkMap)$$0)).filter((Predicate<LevelChunk>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, nonNull(java.lang.Object ), (Lnet/minecraft/world/level/chunk/LevelChunk;)Z)()).toList();
        }
        for (LevelChunk $$5 : $$4) {
            this.pendingChunks.remove($$5.getPos().toLong());
        }
        return $$4;
    }

    public void onChunkBatchReceivedByClient(float $$0) {
        --this.unacknowledgedBatches;
        float f = this.desiredChunksPerTick = Double.isNaN($$0) ? 0.01f : Mth.clamp($$0, 0.01f, 64.0f);
        if (this.unacknowledgedBatches == 0) {
            this.batchQuota = 1.0f;
        }
        this.maxUnacknowledgedBatches = 10;
    }

    public boolean isPending(long $$0) {
        return this.pendingChunks.contains($$0);
    }

    private static /* synthetic */ int lambda$collectChunksToSend$0(ChunkPos $$0, LevelChunk $$1) {
        return $$0.distanceSquared($$1.getPos());
    }
}

