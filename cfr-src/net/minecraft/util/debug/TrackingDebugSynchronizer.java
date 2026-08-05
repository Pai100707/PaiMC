/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.debug;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundDebugBlockValuePacket;
import net.minecraft.network.protocol.game.ClientboundDebugChunkValuePacket;
import net.minecraft.network.protocol.game.ClientboundDebugEntityValuePacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.util.debug.DebugPoiInfo;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public abstract class TrackingDebugSynchronizer<T> {
    protected final DebugSubscription<T> subscription;
    private final Set<UUID> subscribedPlayers = new ObjectOpenHashSet();

    public TrackingDebugSynchronizer(DebugSubscription<T> $$0) {
        this.subscription = $$0;
    }

    public final void tick(ServerLevel $$0) {
        for (ServerPlayer $$12 : $$0.players()) {
            boolean $$2 = this.subscribedPlayers.contains($$12.getUUID());
            boolean $$3 = $$12.debugSubscriptions().contains(this.subscription);
            if ($$3 == $$2) continue;
            if ($$3) {
                this.addSubscriber($$12);
                continue;
            }
            this.subscribedPlayers.remove($$12.getUUID());
        }
        this.subscribedPlayers.removeIf($$1 -> $$0.getPlayerByUUID((UUID)$$1) == null);
        if (!this.subscribedPlayers.isEmpty()) {
            this.pollAndSendUpdates($$0);
        }
    }

    private void addSubscriber(ServerPlayer $$0) {
        this.subscribedPlayers.add($$0.getUUID());
        $$0.getChunkTrackingView().forEach($$1 -> {
            if (!$$0.connection.chunkSender.isPending($$1.toLong())) {
                this.startTrackingChunk($$0, (ChunkPos)$$1);
            }
        });
        $$0.level().getChunkSource().chunkMap.forEachEntityTrackedBy($$0, $$1 -> this.startTrackingEntity($$0, (Entity)$$1));
    }

    protected final void sendToPlayersTrackingChunk(ServerLevel $$0, ChunkPos $$1, Packet<? super ClientGamePacketListener> $$2) {
        ChunkMap $$3 = $$0.getChunkSource().chunkMap;
        for (UUID $$4 : this.subscribedPlayers) {
            ServerPlayer $$5;
            Player player = $$0.getPlayerByUUID($$4);
            if (!(player instanceof ServerPlayer) || !$$3.isChunkTracked($$5 = (ServerPlayer)player, $$1.x, $$1.z)) continue;
            $$5.connection.send($$2);
        }
    }

    protected final void sendToPlayersTrackingEntity(ServerLevel $$02, Entity $$1, Packet<? super ClientGamePacketListener> $$2) {
        ChunkMap $$3 = $$02.getChunkSource().chunkMap;
        $$3.sendToTrackingPlayersFiltered($$1, $$2, $$0 -> this.subscribedPlayers.contains($$0.getUUID()));
    }

    public final void startTrackingChunk(ServerPlayer $$0, ChunkPos $$1) {
        if (this.subscribedPlayers.contains($$0.getUUID())) {
            this.sendInitialChunk($$0, $$1);
        }
    }

    public final void startTrackingEntity(ServerPlayer $$0, Entity $$1) {
        if (this.subscribedPlayers.contains($$0.getUUID())) {
            this.sendInitialEntity($$0, $$1);
        }
    }

    protected void clear() {
    }

    protected void pollAndSendUpdates(ServerLevel $$0) {
    }

    protected void sendInitialChunk(ServerPlayer $$0, ChunkPos $$1) {
    }

    protected void sendInitialEntity(ServerPlayer $$0, Entity $$1) {
    }

    public static class VillageSectionSynchronizer
    extends TrackingDebugSynchronizer<Unit> {
        public VillageSectionSynchronizer() {
            super(DebugSubscriptions.VILLAGE_SECTIONS);
        }

        @Override
        protected void sendInitialChunk(ServerPlayer $$02, ChunkPos $$1) {
            ServerLevel $$2 = $$02.level();
            PoiManager $$3 = $$2.getPoiManager();
            $$3.getInChunk($$0 -> true, $$1, PoiManager.Occupancy.ANY).forEach($$22 -> {
                SectionPos $$3 = SectionPos.of($$22.getPos());
                VillageSectionSynchronizer.forEachVillageSectionUpdate($$2, $$3, ($$1, $$2) -> {
                    BlockPos $$3 = $$1.center();
                    $$0.connection.send(new ClientboundDebugBlockValuePacket($$3, this.subscription.packUpdate($$2 != false ? Unit.INSTANCE : null)));
                });
            });
        }

        public void onPoiAdded(ServerLevel $$0, PoiRecord $$1) {
            this.sendVillageSectionsPacket($$0, $$1.getPos());
        }

        public void onPoiRemoved(ServerLevel $$0, BlockPos $$1) {
            this.sendVillageSectionsPacket($$0, $$1);
        }

        private void sendVillageSectionsPacket(ServerLevel $$0, BlockPos $$12) {
            VillageSectionSynchronizer.forEachVillageSectionUpdate($$0, SectionPos.of($$12), ($$1, $$2) -> {
                BlockPos $$3 = $$1.center();
                if ($$2.booleanValue()) {
                    this.sendToPlayersTrackingChunk($$0, new ChunkPos($$3), new ClientboundDebugBlockValuePacket($$3, this.subscription.packUpdate(Unit.INSTANCE)));
                } else {
                    this.sendToPlayersTrackingChunk($$0, new ChunkPos($$3), new ClientboundDebugBlockValuePacket($$3, this.subscription.emptyUpdate()));
                }
            });
        }

        private static void forEachVillageSectionUpdate(ServerLevel $$0, SectionPos $$1, BiConsumer<SectionPos, Boolean> $$2) {
            for (int $$3 = -1; $$3 <= 1; ++$$3) {
                for (int $$4 = -1; $$4 <= 1; ++$$4) {
                    for (int $$5 = -1; $$5 <= 1; ++$$5) {
                        SectionPos $$6 = $$1.offset($$4, $$5, $$3);
                        if ($$0.isVillage($$6.center())) {
                            $$2.accept($$6, true);
                            continue;
                        }
                        $$2.accept($$6, false);
                    }
                }
            }
        }
    }

    public static class PoiSynchronizer
    extends TrackingDebugSynchronizer<DebugPoiInfo> {
        public PoiSynchronizer() {
            super(DebugSubscriptions.POIS);
        }

        @Override
        protected void sendInitialChunk(ServerPlayer $$02, ChunkPos $$12) {
            ServerLevel $$2 = $$02.level();
            PoiManager $$3 = $$2.getPoiManager();
            $$3.getInChunk($$0 -> true, $$12, PoiManager.Occupancy.ANY).forEach($$1 -> $$0.connection.send(new ClientboundDebugBlockValuePacket($$1.getPos(), this.subscription.packUpdate(new DebugPoiInfo((PoiRecord)$$1)))));
        }

        public void onPoiAdded(ServerLevel $$0, PoiRecord $$1) {
            this.sendToPlayersTrackingChunk($$0, new ChunkPos($$1.getPos()), new ClientboundDebugBlockValuePacket($$1.getPos(), this.subscription.packUpdate(new DebugPoiInfo($$1))));
        }

        public void onPoiRemoved(ServerLevel $$0, BlockPos $$1) {
            this.sendToPlayersTrackingChunk($$0, new ChunkPos($$1), new ClientboundDebugBlockValuePacket($$1, this.subscription.emptyUpdate()));
        }

        public void onPoiTicketCountChanged(ServerLevel $$0, BlockPos $$1) {
            this.sendToPlayersTrackingChunk($$0, new ChunkPos($$1), new ClientboundDebugBlockValuePacket($$1, this.subscription.packUpdate($$0.getPoiManager().getDebugPoiInfo($$1))));
        }
    }

    static class ValueSource<T> {
        private final DebugValueSource.ValueGetter<T> getter;
        @Nullable T lastSyncedValue;

        ValueSource(DebugValueSource.ValueGetter<T> $$0) {
            this.getter = $$0;
        }

        public @Nullable DebugSubscription.Update<T> pollUpdate(DebugSubscription<T> $$0) {
            T $$1 = this.getter.get();
            if (!Objects.equals($$1, this.lastSyncedValue)) {
                this.lastSyncedValue = $$1;
                return $$0.packUpdate($$1);
            }
            return null;
        }
    }

    public static class SourceSynchronizer<T>
    extends TrackingDebugSynchronizer<T> {
        private final Map<ChunkPos, ValueSource<T>> chunkSources = new HashMap<ChunkPos, ValueSource<T>>();
        private final Map<BlockPos, ValueSource<T>> blockEntitySources = new HashMap<BlockPos, ValueSource<T>>();
        private final Map<UUID, ValueSource<T>> entitySources = new HashMap<UUID, ValueSource<T>>();

        public SourceSynchronizer(DebugSubscription<T> $$0) {
            super($$0);
        }

        @Override
        protected void clear() {
            this.chunkSources.clear();
            this.blockEntitySources.clear();
            this.entitySources.clear();
        }

        @Override
        protected void pollAndSendUpdates(ServerLevel $$0) {
            for (Map.Entry<ChunkPos, ValueSource<T>> entry : this.chunkSources.entrySet()) {
                DebugSubscription.Update<T> $$2 = entry.getValue().pollUpdate(this.subscription);
                if ($$2 == null) continue;
                ChunkPos $$3 = entry.getKey();
                this.sendToPlayersTrackingChunk($$0, $$3, new ClientboundDebugChunkValuePacket($$3, $$2));
            }
            for (Map.Entry<Object, ValueSource<T>> entry : this.blockEntitySources.entrySet()) {
                DebugSubscription.Update<T> $$5 = entry.getValue().pollUpdate(this.subscription);
                if ($$5 == null) continue;
                BlockPos $$6 = (BlockPos)entry.getKey();
                ChunkPos $$7 = new ChunkPos($$6);
                this.sendToPlayersTrackingChunk($$0, $$7, new ClientboundDebugBlockValuePacket($$6, $$5));
            }
            for (Map.Entry<Object, ValueSource<T>> entry : this.entitySources.entrySet()) {
                DebugSubscription.Update<T> $$9 = entry.getValue().pollUpdate(this.subscription);
                if ($$9 == null) continue;
                Entity $$10 = Objects.requireNonNull($$0.getEntity((UUID)entry.getKey()));
                this.sendToPlayersTrackingEntity($$0, $$10, new ClientboundDebugEntityValuePacket($$10.getId(), $$9));
            }
        }

        public void registerChunk(ChunkPos $$0, DebugValueSource.ValueGetter<T> $$1) {
            this.chunkSources.put($$0, new ValueSource<T>($$1));
        }

        public void registerBlockEntity(BlockPos $$0, DebugValueSource.ValueGetter<T> $$1) {
            this.blockEntitySources.put($$0, new ValueSource<T>($$1));
        }

        public void registerEntity(UUID $$0, DebugValueSource.ValueGetter<T> $$1) {
            this.entitySources.put($$0, new ValueSource<T>($$1));
        }

        public void dropChunk(ChunkPos $$0) {
            this.chunkSources.remove($$0);
            this.blockEntitySources.keySet().removeIf($$0::contains);
        }

        public void dropBlockEntity(ServerLevel $$0, BlockPos $$1) {
            ValueSource<T> $$2 = this.blockEntitySources.remove($$1);
            if ($$2 != null) {
                ChunkPos $$3 = new ChunkPos($$1);
                this.sendToPlayersTrackingChunk($$0, $$3, new ClientboundDebugBlockValuePacket($$1, this.subscription.emptyUpdate()));
            }
        }

        public void dropEntity(Entity $$0) {
            this.entitySources.remove($$0.getUUID());
        }

        @Override
        protected void sendInitialChunk(ServerPlayer $$0, ChunkPos $$1) {
            ValueSource<T> $$2 = this.chunkSources.get($$1);
            if ($$2 != null && $$2.lastSyncedValue != null) {
                $$0.connection.send(new ClientboundDebugChunkValuePacket($$1, this.subscription.packUpdate($$2.lastSyncedValue)));
            }
            for (Map.Entry<BlockPos, ValueSource<T>> $$3 : this.blockEntitySources.entrySet()) {
                BlockPos $$5;
                Object $$4 = $$3.getValue().lastSyncedValue;
                if ($$4 == null || !$$1.contains($$5 = $$3.getKey())) continue;
                $$0.connection.send(new ClientboundDebugBlockValuePacket($$5, this.subscription.packUpdate($$4)));
            }
        }

        @Override
        protected void sendInitialEntity(ServerPlayer $$0, Entity $$1) {
            ValueSource<T> $$2 = this.entitySources.get($$1.getUUID());
            if ($$2 != null && $$2.lastSyncedValue != null) {
                $$0.connection.send(new ClientboundDebugEntityValuePacket($$1.getId(), this.subscription.packUpdate($$2.lastSyncedValue)));
            }
        }
    }
}

