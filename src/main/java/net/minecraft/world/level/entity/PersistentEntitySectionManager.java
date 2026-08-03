package net.minecraft.world.level.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity.RemovalReason;
import org.slf4j.Logger;

public class PersistentEntitySectionManager<T extends EntityAccess> implements AutoCloseable {
   static final Logger LOGGER = LogUtils.getLogger();
   final Set<UUID> knownUuids = Sets.newHashSet();
   final LevelCallback<T> callbacks;
   private final EntityPersistentStorage<T> permanentStorage;
   private final EntityLookup<T> visibleEntityStorage;
   final EntitySectionStorage<T> sectionStorage;
   private final LevelEntityGetter<T> entityGetter;
   private final Long2ObjectMap<Visibility> chunkVisibility = new Long2ObjectOpenHashMap();
   private final Long2ObjectMap<PersistentEntitySectionManager.ChunkLoadStatus> chunkLoadStatuses = new Long2ObjectOpenHashMap();
   private final LongSet chunksToUnload = new LongOpenHashSet();
   private final Queue<ChunkEntities<T>> loadingInbox = Queues.newConcurrentLinkedQueue();

   public PersistentEntitySectionManager(Class<T> $$0, LevelCallback<T> $$1, EntityPersistentStorage<T> $$2) {
      this.visibleEntityStorage = new EntityLookup<>();
      this.sectionStorage = new EntitySectionStorage<>($$0, this.chunkVisibility);
      this.chunkVisibility.defaultReturnValue(Visibility.HIDDEN);
      this.chunkLoadStatuses.defaultReturnValue(PersistentEntitySectionManager.ChunkLoadStatus.FRESH);
      this.callbacks = $$1;
      this.permanentStorage = $$2;
      this.entityGetter = new LevelEntityGetterAdapter<>(this.visibleEntityStorage, this.sectionStorage);
   }

   void removeSectionIfEmpty(long $$0, EntitySection<T> $$1) {
      if ($$1.isEmpty()) {
         this.sectionStorage.remove($$0);
      }
   }

   private boolean addEntityUuid(T $$0) {
      if (!this.knownUuids.add($$0.getUUID())) {
         LOGGER.warn("UUID of added entity already exists: {}", $$0);
         return false;
      } else {
         return true;
      }
   }

   public boolean addNewEntity(T $$0) {
      return this.addEntity($$0, false);
   }

   private boolean addEntity(T $$0, boolean $$1) {
      if (!this.addEntityUuid($$0)) {
         return false;
      } else {
         long $$2 = SectionPos.asLong($$0.blockPosition());
         EntitySection<T> $$3 = this.sectionStorage.getOrCreateSection($$2);
         $$3.add($$0);
         $$0.setLevelCallback(new PersistentEntitySectionManager.Callback($$0, $$2, $$3));
         if (!$$1) {
            this.callbacks.onCreated($$0);
         }

         Visibility $$4 = getEffectiveStatus($$0, $$3.getStatus());
         if ($$4.isAccessible()) {
            this.startTracking($$0);
         }

         if ($$4.isTicking()) {
            this.startTicking($$0);
         }

         return true;
      }
   }

   static <T extends EntityAccess> Visibility getEffectiveStatus(T $$0, Visibility $$1) {
      return $$0.isAlwaysTicking() ? Visibility.TICKING : $$1;
   }

   public boolean isTicking(net.minecraft.world.level.ChunkPos $$0) {
      return ((Visibility)this.chunkVisibility.get($$0.toLong())).isTicking();
   }

   public void addLegacyChunkEntities(Stream<T> $$0) {
      $$0.forEach($$0x -> this.addEntity((T)$$0x, true));
   }

   public void addWorldGenChunkEntities(Stream<T> $$0) {
      $$0.forEach($$0x -> this.addEntity((T)$$0x, false));
   }

   void startTicking(T $$0) {
      this.callbacks.onTickingStart($$0);
   }

   void stopTicking(T $$0) {
      this.callbacks.onTickingEnd($$0);
   }

   void startTracking(T $$0) {
      this.visibleEntityStorage.add($$0);
      this.callbacks.onTrackingStart($$0);
   }

   void stopTracking(T $$0) {
      this.callbacks.onTrackingEnd($$0);
      this.visibleEntityStorage.remove($$0);
   }

   public void updateChunkStatus(net.minecraft.world.level.ChunkPos $$0, FullChunkStatus $$1) {
      Visibility $$2 = Visibility.fromFullChunkStatus($$1);
      this.updateChunkStatus($$0, $$2);
   }

   public void updateChunkStatus(net.minecraft.world.level.ChunkPos $$0, Visibility $$1) {
      long $$2 = $$0.toLong();
      if ($$1 == Visibility.HIDDEN) {
         this.chunkVisibility.remove($$2);
         this.chunksToUnload.add($$2);
      } else {
         this.chunkVisibility.put($$2, $$1);
         this.chunksToUnload.remove($$2);
         this.ensureChunkQueuedForLoad($$2);
      }

      this.sectionStorage.getExistingSectionsInChunk($$2).forEach($$1x -> {
         Visibility $$2x = $$1x.updateChunkStatus($$1);
         boolean $$3 = $$2x.isAccessible();
         boolean $$4 = $$1.isAccessible();
         boolean $$5 = $$2x.isTicking();
         boolean $$6 = $$1.isTicking();
         if ($$5 && !$$6) {
            $$1x.getEntities().filter($$0xx -> !$$0xx.isAlwaysTicking()).forEach(this::stopTicking);
         }

         if ($$3 && !$$4) {
            $$1x.getEntities().filter($$0xx -> !$$0xx.isAlwaysTicking()).forEach(this::stopTracking);
         } else if (!$$3 && $$4) {
            $$1x.getEntities().filter($$0xx -> !$$0xx.isAlwaysTicking()).forEach(this::startTracking);
         }

         if (!$$5 && $$6) {
            $$1x.getEntities().filter($$0xx -> !$$0xx.isAlwaysTicking()).forEach(this::startTicking);
         }
      });
   }

   private void ensureChunkQueuedForLoad(long $$0) {
      PersistentEntitySectionManager.ChunkLoadStatus $$1 = (PersistentEntitySectionManager.ChunkLoadStatus)this.chunkLoadStatuses.get($$0);
      if ($$1 == PersistentEntitySectionManager.ChunkLoadStatus.FRESH) {
         this.requestChunkLoad($$0);
      }
   }

   private boolean storeChunkSections(long $$0, Consumer<T> $$1) {
      PersistentEntitySectionManager.ChunkLoadStatus $$2 = (PersistentEntitySectionManager.ChunkLoadStatus)this.chunkLoadStatuses.get($$0);
      if ($$2 == PersistentEntitySectionManager.ChunkLoadStatus.PENDING) {
         return false;
      } else {
         List<T> $$3 = this.sectionStorage
            .getExistingSectionsInChunk($$0)
            .flatMap($$0x -> $$0x.getEntities().filter(EntityAccess::shouldBeSaved))
            .collect(Collectors.toList());
         if ($$3.isEmpty()) {
            if ($$2 == PersistentEntitySectionManager.ChunkLoadStatus.LOADED) {
               this.permanentStorage.storeEntities(new ChunkEntities<>(new net.minecraft.world.level.ChunkPos($$0), ImmutableList.of()));
            }

            return true;
         } else if ($$2 == PersistentEntitySectionManager.ChunkLoadStatus.FRESH) {
            this.requestChunkLoad($$0);
            return false;
         } else {
            this.permanentStorage.storeEntities(new ChunkEntities<>(new net.minecraft.world.level.ChunkPos($$0), $$3));
            $$3.forEach($$1);
            return true;
         }
      }
   }

   private void requestChunkLoad(long $$0) {
      this.chunkLoadStatuses.put($$0, PersistentEntitySectionManager.ChunkLoadStatus.PENDING);
      net.minecraft.world.level.ChunkPos $$1 = new net.minecraft.world.level.ChunkPos($$0);
      this.permanentStorage.loadEntities($$1).thenAccept(this.loadingInbox::add).exceptionally($$1x -> {
         LOGGER.error("Failed to read chunk {}", $$1, $$1x);
         return null;
      });
   }

   private boolean processChunkUnload(long $$0) {
      boolean $$1 = this.storeChunkSections($$0, $$0x -> $$0x.getPassengersAndSelf().forEach(this::unloadEntity));
      if (!$$1) {
         return false;
      } else {
         this.chunkLoadStatuses.remove($$0);
         return true;
      }
   }

   private void unloadEntity(EntityAccess $$0) {
      $$0.setRemoved(RemovalReason.UNLOADED_TO_CHUNK);
      $$0.setLevelCallback(EntityInLevelCallback.NULL);
   }

   private void processUnloads() {
      this.chunksToUnload.removeIf($$0 -> this.chunkVisibility.get($$0) != Visibility.HIDDEN ? true : this.processChunkUnload($$0));
   }

   public void processPendingLoads() {
      ChunkEntities<T> $$0;
      while (($$0 = this.loadingInbox.poll()) != null) {
         $$0.getEntities().forEach($$0x -> this.addEntity((T)$$0x, true));
         this.chunkLoadStatuses.put($$0.getPos().toLong(), PersistentEntitySectionManager.ChunkLoadStatus.LOADED);
      }
   }

   public void tick() {
      this.processPendingLoads();
      this.processUnloads();
   }

   private LongSet getAllChunksToSave() {
      LongSet $$0 = this.sectionStorage.getAllChunksWithExistingSections();
      ObjectIterator var2 = Long2ObjectMaps.fastIterable(this.chunkLoadStatuses).iterator();

      while (var2.hasNext()) {
         Entry<PersistentEntitySectionManager.ChunkLoadStatus> $$1 = (Entry<PersistentEntitySectionManager.ChunkLoadStatus>)var2.next();
         if ($$1.getValue() == PersistentEntitySectionManager.ChunkLoadStatus.LOADED) {
            $$0.add($$1.getLongKey());
         }
      }

      return $$0;
   }

   public void autoSave() {
      this.getAllChunksToSave().forEach($$0 -> {
         boolean $$1 = this.chunkVisibility.get($$0) == Visibility.HIDDEN;
         if ($$1) {
            this.processChunkUnload($$0);
         } else {
            this.storeChunkSections($$0, $$0x -> {});
         }
      });
   }

   public void saveAll() {
      LongSet $$0 = this.getAllChunksToSave();

      while (!$$0.isEmpty()) {
         this.permanentStorage.flush(false);
         this.processPendingLoads();
         $$0.removeIf($$0x -> {
            boolean $$1 = this.chunkVisibility.get($$0x) == Visibility.HIDDEN;
            return $$1 ? this.processChunkUnload($$0x) : this.storeChunkSections($$0x, $$0xx -> {});
         });
      }

      this.permanentStorage.flush(true);
   }

   @Override
   public void close() throws IOException {
      this.saveAll();
      this.permanentStorage.close();
   }

   public boolean isLoaded(UUID $$0) {
      return this.knownUuids.contains($$0);
   }

   public LevelEntityGetter<T> getEntityGetter() {
      return this.entityGetter;
   }

   public boolean canPositionTick(BlockPos $$0) {
      return ((Visibility)this.chunkVisibility.get(net.minecraft.world.level.ChunkPos.asLong($$0))).isTicking();
   }

   public boolean canPositionTick(net.minecraft.world.level.ChunkPos $$0) {
      return ((Visibility)this.chunkVisibility.get($$0.toLong())).isTicking();
   }

   public boolean areEntitiesLoaded(long $$0) {
      return this.chunkLoadStatuses.get($$0) == PersistentEntitySectionManager.ChunkLoadStatus.LOADED;
   }

   public void dumpSections(Writer $$0) throws IOException {
      CsvOutput $$1 = CsvOutput.builder()
         .addColumn("x")
         .addColumn("y")
         .addColumn("z")
         .addColumn("visibility")
         .addColumn("load_status")
         .addColumn("entity_count")
         .build($$0);
      this.sectionStorage.getAllChunksWithExistingSections().forEach($$1x -> {
         PersistentEntitySectionManager.ChunkLoadStatus $$2 = (PersistentEntitySectionManager.ChunkLoadStatus)this.chunkLoadStatuses.get($$1x);
         this.sectionStorage.getExistingSectionPositionsInChunk($$1x).forEach($$2x -> {
            EntitySection<T> $$3 = this.sectionStorage.getSection($$2x);
            if ($$3 != null) {
               try {
                  $$1.writeRow(new Object[]{SectionPos.x($$2x), SectionPos.y($$2x), SectionPos.z($$2x), $$3.getStatus(), $$2, $$3.size()});
               } catch (IOException var7) {
                  throw new UncheckedIOException(var7);
               }
            }
         });
      });
   }

   @VisibleForDebug
   public String gatherStats() {
      return this.knownUuids.size()
         + ","
         + this.visibleEntityStorage.count()
         + ","
         + this.sectionStorage.count()
         + ","
         + this.chunkLoadStatuses.size()
         + ","
         + this.chunkVisibility.size()
         + ","
         + this.loadingInbox.size()
         + ","
         + this.chunksToUnload.size();
   }

   @VisibleForDebug
   public int count() {
      return this.visibleEntityStorage.count();
   }

   class Callback implements EntityInLevelCallback {
      private final T entity;
      private long currentSectionKey;
      private EntitySection<T> currentSection;

      Callback(final T $$0, final long $$1, final EntitySection<T> $$2) {
         this.entity = $$0;
         this.currentSectionKey = $$1;
         this.currentSection = $$2;
      }

      @Override
      public void onMove() {
         BlockPos $$0 = this.entity.blockPosition();
         long $$1 = SectionPos.asLong($$0);
         if ($$1 != this.currentSectionKey) {
            Visibility $$2 = this.currentSection.getStatus();
            if (!this.currentSection.remove(this.entity)) {
               PersistentEntitySectionManager.LOGGER
                  .warn("Entity {} wasn't found in section {} (moving to {})", new Object[]{this.entity, SectionPos.of(this.currentSectionKey), $$1});
            }

            PersistentEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
            EntitySection<T> $$3 = PersistentEntitySectionManager.this.sectionStorage.getOrCreateSection($$1);
            $$3.add(this.entity);
            this.currentSection = $$3;
            this.currentSectionKey = $$1;
            this.updateStatus($$2, $$3.getStatus());
         }
      }

      private void updateStatus(Visibility $$0, Visibility $$1) {
         Visibility $$2 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, $$0);
         Visibility $$3 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, $$1);
         if ($$2 == $$3) {
            if ($$3.isAccessible()) {
               PersistentEntitySectionManager.this.callbacks.onSectionChange(this.entity);
            }
         } else {
            boolean $$4 = $$2.isAccessible();
            boolean $$5 = $$3.isAccessible();
            if ($$4 && !$$5) {
               PersistentEntitySectionManager.this.stopTracking(this.entity);
            } else if (!$$4 && $$5) {
               PersistentEntitySectionManager.this.startTracking(this.entity);
            }

            boolean $$6 = $$2.isTicking();
            boolean $$7 = $$3.isTicking();
            if ($$6 && !$$7) {
               PersistentEntitySectionManager.this.stopTicking(this.entity);
            } else if (!$$6 && $$7) {
               PersistentEntitySectionManager.this.startTicking(this.entity);
            }

            if ($$5) {
               PersistentEntitySectionManager.this.callbacks.onSectionChange(this.entity);
            }
         }
      }

      @Override
      public void onRemove(RemovalReason $$0) {
         if (!this.currentSection.remove(this.entity)) {
            PersistentEntitySectionManager.LOGGER
               .warn("Entity {} wasn't found in section {} (destroying due to {})", new Object[]{this.entity, SectionPos.of(this.currentSectionKey), $$0});
         }

         Visibility $$1 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, this.currentSection.getStatus());
         if ($$1.isTicking()) {
            PersistentEntitySectionManager.this.stopTicking(this.entity);
         }

         if ($$1.isAccessible()) {
            PersistentEntitySectionManager.this.stopTracking(this.entity);
         }

         if ($$0.shouldDestroy()) {
            PersistentEntitySectionManager.this.callbacks.onDestroyed(this.entity);
         }

         PersistentEntitySectionManager.this.knownUuids.remove(this.entity.getUUID());
         this.entity.setLevelCallback(NULL);
         PersistentEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
      }
   }

   static enum ChunkLoadStatus {
      FRESH,
      PENDING,
      LOADED;
   }
}
