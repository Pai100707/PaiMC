package net.minecraft.world.level.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity.RemovalReason;
import org.slf4j.Logger;

public class TransientEntitySectionManager<T extends EntityAccess> {
   static final Logger LOGGER = LogUtils.getLogger();
   final LevelCallback<T> callbacks;
   final EntityLookup<T> entityStorage;
   final EntitySectionStorage<T> sectionStorage;
   private final LongSet tickingChunks = new LongOpenHashSet();
   private final LevelEntityGetter<T> entityGetter;

   public TransientEntitySectionManager(Class<T> $$0, LevelCallback<T> $$1) {
      this.entityStorage = new EntityLookup<>();
      this.sectionStorage = new EntitySectionStorage<>($$0, $$0x -> this.tickingChunks.contains($$0x) ? Visibility.TICKING : Visibility.TRACKED);
      this.callbacks = $$1;
      this.entityGetter = new LevelEntityGetterAdapter<>(this.entityStorage, this.sectionStorage);
   }

   public void startTicking(net.minecraft.world.level.ChunkPos $$0) {
      long $$1 = $$0.toLong();
      this.tickingChunks.add($$1);
      this.sectionStorage.getExistingSectionsInChunk($$1).forEach($$0x -> {
         Visibility $$1x = $$0x.updateChunkStatus(Visibility.TICKING);
         if (!$$1x.isTicking()) {
            $$0x.getEntities().filter($$0xx -> !$$0xx.isAlwaysTicking()).forEach(this.callbacks::onTickingStart);
         }
      });
   }

   public void stopTicking(net.minecraft.world.level.ChunkPos $$0) {
      long $$1 = $$0.toLong();
      this.tickingChunks.remove($$1);
      this.sectionStorage.getExistingSectionsInChunk($$1).forEach($$0x -> {
         Visibility $$1x = $$0x.updateChunkStatus(Visibility.TRACKED);
         if ($$1x.isTicking()) {
            $$0x.getEntities().filter($$0xx -> !$$0xx.isAlwaysTicking()).forEach(this.callbacks::onTickingEnd);
         }
      });
   }

   public LevelEntityGetter<T> getEntityGetter() {
      return this.entityGetter;
   }

   public void addEntity(T $$0) {
      this.entityStorage.add($$0);
      long $$1 = SectionPos.asLong($$0.blockPosition());
      EntitySection<T> $$2 = this.sectionStorage.getOrCreateSection($$1);
      $$2.add($$0);
      $$0.setLevelCallback(new TransientEntitySectionManager.Callback($$0, $$1, $$2));
      this.callbacks.onCreated($$0);
      this.callbacks.onTrackingStart($$0);
      if ($$0.isAlwaysTicking() || $$2.getStatus().isTicking()) {
         this.callbacks.onTickingStart($$0);
      }
   }

   @VisibleForDebug
   public int count() {
      return this.entityStorage.count();
   }

   void removeSectionIfEmpty(long $$0, EntitySection<T> $$1) {
      if ($$1.isEmpty()) {
         this.sectionStorage.remove($$0);
      }
   }

   @VisibleForDebug
   public String gatherStats() {
      return this.entityStorage.count() + "," + this.sectionStorage.count() + "," + this.tickingChunks.size();
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
               TransientEntitySectionManager.LOGGER
                  .warn("Entity {} wasn't found in section {} (moving to {})", new Object[]{this.entity, SectionPos.of(this.currentSectionKey), $$1});
            }

            TransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
            EntitySection<T> $$3 = TransientEntitySectionManager.this.sectionStorage.getOrCreateSection($$1);
            $$3.add(this.entity);
            this.currentSection = $$3;
            this.currentSectionKey = $$1;
            TransientEntitySectionManager.this.callbacks.onSectionChange(this.entity);
            if (!this.entity.isAlwaysTicking()) {
               boolean $$4 = $$2.isTicking();
               boolean $$5 = $$3.getStatus().isTicking();
               if ($$4 && !$$5) {
                  TransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
               } else if (!$$4 && $$5) {
                  TransientEntitySectionManager.this.callbacks.onTickingStart(this.entity);
               }
            }
         }
      }

      @Override
      public void onRemove(RemovalReason $$0) {
         if (!this.currentSection.remove(this.entity)) {
            TransientEntitySectionManager.LOGGER
               .warn("Entity {} wasn't found in section {} (destroying due to {})", new Object[]{this.entity, SectionPos.of(this.currentSectionKey), $$0});
         }

         Visibility $$1 = this.currentSection.getStatus();
         if ($$1.isTicking() || this.entity.isAlwaysTicking()) {
            TransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
         }

         TransientEntitySectionManager.this.callbacks.onTrackingEnd(this.entity);
         TransientEntitySectionManager.this.callbacks.onDestroyed(this.entity);
         TransientEntitySectionManager.this.entityStorage.remove(this.entity);
         this.entity.setLevelCallback(NULL);
         TransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
      }
   }
}
