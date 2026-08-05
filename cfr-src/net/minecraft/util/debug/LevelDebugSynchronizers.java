/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundDebugBlockValuePacket;
import net.minecraft.network.protocol.game.ClientboundDebugEntityValuePacket;
import net.minecraft.network.protocol.game.ClientboundDebugEventPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.util.debug.ServerDebugSubscribers;
import net.minecraft.util.debug.TrackingDebugSynchronizer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class LevelDebugSynchronizers {
    private final ServerLevel level;
    private final List<TrackingDebugSynchronizer<?>> allSynchronizers = new ArrayList();
    private final Map<DebugSubscription<?>, TrackingDebugSynchronizer.SourceSynchronizer<?>> sourceSynchronizers = new HashMap();
    private final TrackingDebugSynchronizer.PoiSynchronizer poiSynchronizer = new TrackingDebugSynchronizer.PoiSynchronizer();
    private final TrackingDebugSynchronizer.VillageSectionSynchronizer villageSectionSynchronizer = new TrackingDebugSynchronizer.VillageSectionSynchronizer();
    private boolean sleeping = true;
    private Set<DebugSubscription<?>> enabledSubscriptions = Set.of();

    public LevelDebugSynchronizers(ServerLevel $$0) {
        this.level = $$0;
        for (DebugSubscription debugSubscription : BuiltInRegistries.DEBUG_SUBSCRIPTION) {
            if (debugSubscription.valueStreamCodec() == null) continue;
            this.sourceSynchronizers.put(debugSubscription, new TrackingDebugSynchronizer.SourceSynchronizer(debugSubscription));
        }
        this.allSynchronizers.addAll(this.sourceSynchronizers.values());
        this.allSynchronizers.add(this.poiSynchronizer);
        this.allSynchronizers.add(this.villageSectionSynchronizer);
    }

    public void tick(ServerDebugSubscribers $$0) {
        this.enabledSubscriptions = $$0.enabledSubscriptions();
        boolean $$1 = this.enabledSubscriptions.isEmpty();
        if (this.sleeping != $$1) {
            this.sleeping = $$1;
            if ($$1) {
                for (TrackingDebugSynchronizer<?> $$2 : this.allSynchronizers) {
                    $$2.clear();
                }
            } else {
                this.wakeUp();
            }
        }
        if (!this.sleeping) {
            for (TrackingDebugSynchronizer<?> $$3 : this.allSynchronizers) {
                $$3.tick(this.level);
            }
        }
    }

    private void wakeUp() {
        ChunkMap $$0 = this.level.getChunkSource().chunkMap;
        $$0.forEachReadyToSendChunk(this::registerChunk);
        for (Entity $$1 : this.level.getAllEntities()) {
            if (!$$0.isTrackedByAnyPlayer($$1)) continue;
            this.registerEntity($$1);
        }
    }

    <T> TrackingDebugSynchronizer.SourceSynchronizer<T> getSourceSynchronizer(DebugSubscription<T> $$0) {
        return this.sourceSynchronizers.get($$0);
    }

    public void registerChunk(final LevelChunk $$0) {
        if (this.sleeping) {
            return;
        }
        $$0.registerDebugValues(this.level, new DebugValueSource.Registration(){

            @Override
            public <T> void register(DebugSubscription<T> $$02, DebugValueSource.ValueGetter<T> $$1) {
                LevelDebugSynchronizers.this.getSourceSynchronizer($$02).registerChunk($$0.getPos(), $$1);
            }
        });
        $$0.getBlockEntities().values().forEach(this::registerBlockEntity);
    }

    public void dropChunk(ChunkPos $$0) {
        if (this.sleeping) {
            return;
        }
        for (TrackingDebugSynchronizer.SourceSynchronizer<?> $$1 : this.sourceSynchronizers.values()) {
            $$1.dropChunk($$0);
        }
    }

    public void registerBlockEntity(final BlockEntity $$0) {
        if (this.sleeping) {
            return;
        }
        $$0.registerDebugValues(this.level, new DebugValueSource.Registration(){

            @Override
            public <T> void register(DebugSubscription<T> $$02, DebugValueSource.ValueGetter<T> $$1) {
                LevelDebugSynchronizers.this.getSourceSynchronizer($$02).registerBlockEntity($$0.getBlockPos(), $$1);
            }
        });
    }

    public void dropBlockEntity(BlockPos $$0) {
        if (this.sleeping) {
            return;
        }
        for (TrackingDebugSynchronizer.SourceSynchronizer<?> $$1 : this.sourceSynchronizers.values()) {
            $$1.dropBlockEntity(this.level, $$0);
        }
    }

    public void registerEntity(final Entity $$0) {
        if (this.sleeping) {
            return;
        }
        $$0.registerDebugValues(this.level, new DebugValueSource.Registration(){

            @Override
            public <T> void register(DebugSubscription<T> $$02, DebugValueSource.ValueGetter<T> $$1) {
                LevelDebugSynchronizers.this.getSourceSynchronizer($$02).registerEntity($$0.getUUID(), $$1);
            }
        });
    }

    public void dropEntity(Entity $$0) {
        if (this.sleeping) {
            return;
        }
        for (TrackingDebugSynchronizer.SourceSynchronizer<?> $$1 : this.sourceSynchronizers.values()) {
            $$1.dropEntity($$0);
        }
    }

    public void startTrackingChunk(ServerPlayer $$0, ChunkPos $$1) {
        if (this.sleeping) {
            return;
        }
        for (TrackingDebugSynchronizer<?> $$2 : this.allSynchronizers) {
            $$2.startTrackingChunk($$0, $$1);
        }
    }

    public void startTrackingEntity(ServerPlayer $$0, Entity $$1) {
        if (this.sleeping) {
            return;
        }
        for (TrackingDebugSynchronizer<?> $$2 : this.allSynchronizers) {
            $$2.startTrackingEntity($$0, $$1);
        }
    }

    public void registerPoi(PoiRecord $$0) {
        if (this.sleeping) {
            return;
        }
        this.poiSynchronizer.onPoiAdded(this.level, $$0);
        this.villageSectionSynchronizer.onPoiAdded(this.level, $$0);
    }

    public void updatePoi(BlockPos $$0) {
        if (this.sleeping) {
            return;
        }
        this.poiSynchronizer.onPoiTicketCountChanged(this.level, $$0);
    }

    public void dropPoi(BlockPos $$0) {
        if (this.sleeping) {
            return;
        }
        this.poiSynchronizer.onPoiRemoved(this.level, $$0);
        this.villageSectionSynchronizer.onPoiRemoved(this.level, $$0);
    }

    public boolean hasAnySubscriberFor(DebugSubscription<?> $$0) {
        return this.enabledSubscriptions.contains($$0);
    }

    public <T> void sendBlockValue(BlockPos $$0, DebugSubscription<T> $$1, T $$2) {
        if (this.hasAnySubscriberFor($$1)) {
            this.broadcastToTracking(new ChunkPos($$0), $$1, (Packet<? super ClientGamePacketListener>)new ClientboundDebugBlockValuePacket($$0, $$1.packUpdate($$2)));
        }
    }

    public <T> void clearBlockValue(BlockPos $$0, DebugSubscription<T> $$1) {
        if (this.hasAnySubscriberFor($$1)) {
            this.broadcastToTracking(new ChunkPos($$0), $$1, (Packet<? super ClientGamePacketListener>)new ClientboundDebugBlockValuePacket($$0, $$1.emptyUpdate()));
        }
    }

    public <T> void sendEntityValue(Entity $$0, DebugSubscription<T> $$1, T $$2) {
        if (this.hasAnySubscriberFor($$1)) {
            this.broadcastToTracking($$0, $$1, (Packet<? super ClientGamePacketListener>)new ClientboundDebugEntityValuePacket($$0.getId(), $$1.packUpdate($$2)));
        }
    }

    public <T> void clearEntityValue(Entity $$0, DebugSubscription<T> $$1) {
        if (this.hasAnySubscriberFor($$1)) {
            this.broadcastToTracking($$0, $$1, (Packet<? super ClientGamePacketListener>)new ClientboundDebugEntityValuePacket($$0.getId(), $$1.emptyUpdate()));
        }
    }

    public <T> void broadcastEventToTracking(BlockPos $$0, DebugSubscription<T> $$1, T $$2) {
        if (this.hasAnySubscriberFor($$1)) {
            this.broadcastToTracking(new ChunkPos($$0), $$1, (Packet<? super ClientGamePacketListener>)new ClientboundDebugEventPacket($$1.packEvent($$2)));
        }
    }

    private void broadcastToTracking(ChunkPos $$0, DebugSubscription<?> $$1, Packet<? super ClientGamePacketListener> $$2) {
        ChunkMap $$3 = this.level.getChunkSource().chunkMap;
        for (ServerPlayer $$4 : $$3.getPlayers($$0, false)) {
            if (!$$4.debugSubscriptions().contains($$1)) continue;
            $$4.connection.send($$2);
        }
    }

    private void broadcastToTracking(Entity $$0, DebugSubscription<?> $$12, Packet<? super ClientGamePacketListener> $$2) {
        ChunkMap $$3 = this.level.getChunkSource().chunkMap;
        $$3.sendToTrackingPlayersFiltered($$0, $$2, $$1 -> $$1.debugSubscriptions().contains($$12));
    }
}

