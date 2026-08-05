package net.minecraft.util.debug;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.level.ChunkPos;

public abstract class TrackingDebugSynchronizer<T> {
   protected final DebugSubscription<T> subscription;
   private final Set<UUID> subscribedPlayers = new ObjectOpenHashSet();

   public TrackingDebugSynchronizer(DebugSubscription<T> $$0) {
      this.subscription = $$0;
   }

   public final void tick(ServerLevel $$0) {
      for (ServerPlayer $$1 : $$0.players()) {
         boolean $$2 = this.subscribedPlayers.contains($$1.getUUID());
         boolean $$3 = $$1.debugSubscriptions().contains(this.subscription);
         if ($$3 != $$2) {
            if ($$3) {
               this.addSubscriber($$1);
            } else {
               this.subscribedPlayers.remove($$1.getUUID());
            }
         }
      }

      this.subscribedPlayers.removeIf($$1x -> $$0.getPlayerByUUID($$1x) == null);
      if (!this.subscribedPlayers.isEmpty()) {
         this.pollAndSendUpdates($$0);
      }
   }

   private void addSubscriber(ServerPlayer $$0) {
      this.subscribedPlayers.add($$0.getUUID());
      $$0.getChunkTrackingView().forEach($$1 -> {
         if (!$$0.connection.chunkSender.isPending($$1.toLong())) {
            this.startTrackingChunk($$0, $$1);
         }
      });
      $$0.level().getChunkSource().chunkMap.forEachEntityTrackedBy($$0, $$1 -> this.startTrackingEntity($$0, $$1));
   }

   protected final void sendToPlayersTrackingChunk(ServerLevel $$0, ChunkPos $$1, Packet<? super ClientGamePacketListener> $$2) {
      ChunkMap $$3 = $$0.getChunkSource().chunkMap;

      for (UUID $$4 : this.subscribedPlayers) {
         if ($$0.getPlayerByUUID($$4) instanceof ServerPlayer $$5 && $$3.isChunkTracked($$5, $$1.x, $$1.z)) {
            $$5.connection.send($$2);
         }
      }
   }

   protected final void sendToPlayersTrackingEntity(ServerLevel $$0, Entity $$1, Packet<? super ClientGamePacketListener> $$2) {
      ChunkMap $$3 = $$0.getChunkSource().chunkMap;
      $$3.sendToTrackingPlayersFiltered($$1, $$2, $$0x -> this.subscribedPlayers.contains($$0x.getUUID()));
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

   public static class PoiSynchronizer extends TrackingDebugSynchronizer<DebugPoiInfo> {
      public PoiSynchronizer() {
         super(DebugSubscriptions.POIS);
      }

      @Override
      protected void sendInitialChunk(ServerPlayer $$0, ChunkPos $$1) {
         ServerLevel $$2 = $$0.level();
         PoiManager $$3 = $$2.getPoiManager();
         $$3.getInChunk($$0x -> true, $$1, Occupancy.ANY)
            .forEach($$1x -> $$0.connection.send(new ClientboundDebugBlockValuePacket($$1x.getPos(), this.subscription.packUpdate(new DebugPoiInfo($$1x)))));
      }

      public void onPoiAdded(ServerLevel $$0, PoiRecord $$1) {
         this.sendToPlayersTrackingChunk(
            $$0, new ChunkPos($$1.getPos()), new ClientboundDebugBlockValuePacket($$1.getPos(), this.subscription.packUpdate(new DebugPoiInfo($$1)))
         );
      }

      public void onPoiRemoved(ServerLevel $$0, BlockPos $$1) {
         this.sendToPlayersTrackingChunk($$0, new ChunkPos($$1), new ClientboundDebugBlockValuePacket($$1, this.subscription.emptyUpdate()));
      }

      public void onPoiTicketCountChanged(ServerLevel $$0, BlockPos $$1) {
         this.sendToPlayersTrackingChunk(
            $$0, new ChunkPos($$1), new ClientboundDebugBlockValuePacket($$1, this.subscription.packUpdate($$0.getPoiManager().getDebugPoiInfo($$1)))
         );
      }
   }

   public static class SourceSynchronizer<T> extends TrackingDebugSynchronizer<T> {
      private final Map<ChunkPos, TrackingDebugSynchronizer.ValueSource<T>> chunkSources = new HashMap<>();
      private final Map<BlockPos, TrackingDebugSynchronizer.ValueSource<T>> blockEntitySources = new HashMap<>();
      private final Map<UUID, TrackingDebugSynchronizer.ValueSource<T>> entitySources = new HashMap<>();

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
         for (Entry<ChunkPos, TrackingDebugSynchronizer.ValueSource<T>> $$1 : this.chunkSources.entrySet()) {
            DebugSubscription.Update<T> $$2 = $$1.getValue().pollUpdate(this.subscription);
            if ($$2 != null) {
               ChunkPos $$3 = $$1.getKey();
               this.sendToPlayersTrackingChunk($$0, $$3, new ClientboundDebugChunkValuePacket($$3, $$2));
            }
         }

         for (Entry<BlockPos, TrackingDebugSynchronizer.ValueSource<T>> $$4 : this.blockEntitySources.entrySet()) {
            DebugSubscription.Update<T> $$5 = $$4.getValue().pollUpdate(this.subscription);
            if ($$5 != null) {
               BlockPos $$6 = $$4.getKey();
               ChunkPos $$7 = new ChunkPos($$6);
               this.sendToPlayersTrackingChunk($$0, $$7, new ClientboundDebugBlockValuePacket($$6, $$5));
            }
         }

         for (Entry<UUID, TrackingDebugSynchronizer.ValueSource<T>> $$8 : this.entitySources.entrySet()) {
            DebugSubscription.Update<T> $$9 = $$8.getValue().pollUpdate(this.subscription);
            if ($$9 != null) {
               Entity $$10 = Objects.requireNonNull($$0.getEntity($$8.getKey()));
               this.sendToPlayersTrackingEntity($$0, $$10, new ClientboundDebugEntityValuePacket($$10.getId(), $$9));
            }
         }
      }

      public void registerChunk(ChunkPos $$0, DebugValueSource.ValueGetter<T> $$1) {
         this.chunkSources.put($$0, new TrackingDebugSynchronizer.ValueSource<>($$1));
      }

      public void registerBlockEntity(BlockPos $$0, DebugValueSource.ValueGetter<T> $$1) {
         this.blockEntitySources.put($$0, new TrackingDebugSynchronizer.ValueSource<>($$1));
      }

      public void registerEntity(UUID $$0, DebugValueSource.ValueGetter<T> $$1) {
         this.entitySources.put($$0, new TrackingDebugSynchronizer.ValueSource<>($$1));
      }

      public void dropChunk(ChunkPos $$0) {
         this.chunkSources.remove($$0);
         this.blockEntitySources.keySet().removeIf($$0::contains);
      }

      public void dropBlockEntity(ServerLevel $$0, BlockPos $$1) {
         TrackingDebugSynchronizer.ValueSource<T> $$2 = this.blockEntitySources.remove($$1);
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
         TrackingDebugSynchronizer.ValueSource<T> $$2 = this.chunkSources.get($$1);
         if ($$2 != null && $$2.lastSyncedValue != null) {
            $$0.connection.send(new ClientboundDebugChunkValuePacket($$1, this.subscription.packUpdate($$2.lastSyncedValue)));
         }

         for (Entry<BlockPos, TrackingDebugSynchronizer.ValueSource<T>> $$3 : this.blockEntitySources.entrySet()) {
            T $$4 = $$3.getValue().lastSyncedValue;
            if ($$4 != null) {
               BlockPos $$5 = $$3.getKey();
               if ($$1.contains($$5)) {
                  $$0.connection.send(new ClientboundDebugBlockValuePacket($$5, this.subscription.packUpdate($$4)));
               }
            }
         }
      }

      @Override
      protected void sendInitialEntity(ServerPlayer $$0, Entity $$1) {
         TrackingDebugSynchronizer.ValueSource<T> $$2 = this.entitySources.get($$1.getUUID());
         if ($$2 != null && $$2.lastSyncedValue != null) {
            $$0.connection.send(new ClientboundDebugEntityValuePacket($$1.getId(), this.subscription.packUpdate($$2.lastSyncedValue)));
         }
      }
   }

   static class ValueSource<T> {
      private final DebugValueSource.ValueGetter<T> getter;
      
      T lastSyncedValue;

      ValueSource(DebugValueSource.ValueGetter<T> $$0) {
         this.getter = $$0;
      }

      
      public DebugSubscription.Update<T> pollUpdate(DebugSubscription<T> $$0) {
         T $$1 = this.getter.get();
         if (!Objects.equals($$1, this.lastSyncedValue)) {
            this.lastSyncedValue = $$1;
            return $$0.packUpdate($$1);
         } else {
            return null;
         }
      }
   }

   public static class VillageSectionSynchronizer extends TrackingDebugSynchronizer<net.minecraft.util.Unit> {
      public VillageSectionSynchronizer() {
         super(DebugSubscriptions.VILLAGE_SECTIONS);
      }

      @Override
      protected void sendInitialChunk(ServerPlayer $$0, ChunkPos $$1) {
         ServerLevel $$2 = $$0.level();
         PoiManager $$3 = $$2.getPoiManager();
         $$3.getInChunk($$0x -> true, $$1, Occupancy.ANY).forEach($$2x -> {
            SectionPos $$3x = SectionPos.of($$2x.getPos());
            forEachVillageSectionUpdate($$2, $$3x, ($$1xx, $$2xx) -> {
               BlockPos $$3xx = $$1xx.center();
               $$0.connection.send(new ClientboundDebugBlockValuePacket($$3xx, this.subscription.packUpdate($$2xx ? net.minecraft.util.Unit.INSTANCE : null)));
            });
         });
      }

      public void onPoiAdded(ServerLevel $$0, PoiRecord $$1) {
         this.sendVillageSectionsPacket($$0, $$1.getPos());
      }

      public void onPoiRemoved(ServerLevel $$0, BlockPos $$1) {
         this.sendVillageSectionsPacket($$0, $$1);
      }

      private void sendVillageSectionsPacket(ServerLevel $$0, BlockPos $$1) {
         forEachVillageSectionUpdate(
            $$0,
            SectionPos.of($$1),
            ($$1x, $$2) -> {
               BlockPos $$3 = $$1x.center();
               if ($$2) {
                  this.sendToPlayersTrackingChunk(
                     $$0, new ChunkPos($$3), new ClientboundDebugBlockValuePacket($$3, this.subscription.packUpdate(net.minecraft.util.Unit.INSTANCE))
                  );
               } else {
                  this.sendToPlayersTrackingChunk($$0, new ChunkPos($$3), new ClientboundDebugBlockValuePacket($$3, this.subscription.emptyUpdate()));
               }
            }
         );
      }

      private static void forEachVillageSectionUpdate(ServerLevel $$0, SectionPos $$1, BiConsumer<SectionPos, Boolean> $$2) {
         for (int $$3 = -1; $$3 <= 1; $$3++) {
            for (int $$4 = -1; $$4 <= 1; $$4++) {
               for (int $$5 = -1; $$5 <= 1; $$5++) {
                  SectionPos $$6 = $$1.offset($$4, $$5, $$3);
                  if ($$0.isVillage($$6.center())) {
                     $$2.accept($$6, true);
                  } else {
                     $$2.accept($$6, false);
                  }
               }
            }
         }
      }
   }
}
