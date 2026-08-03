package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.Spliterators;
import java.util.PrimitiveIterator.OfLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class EntitySectionStorage<T extends EntityAccess> {
   public static final int CHONKY_ENTITY_SEARCH_GRACE = 2;
   public static final int MAX_NON_CHONKY_ENTITY_SIZE = 4;
   private final Class<T> entityClass;
   private final Long2ObjectFunction<Visibility> intialSectionVisibility;
   private final Long2ObjectMap<EntitySection<T>> sections = new Long2ObjectOpenHashMap();
   private final LongSortedSet sectionIds = new LongAVLTreeSet();

   public EntitySectionStorage(Class<T> $$0, Long2ObjectFunction<Visibility> $$1) {
      this.entityClass = $$0;
      this.intialSectionVisibility = $$1;
   }

   public void forEachAccessibleNonEmptySection(AABB $$0, AbortableIterationConsumer<EntitySection<T>> $$1) {
      int $$2 = SectionPos.posToSectionCoord($$0.minX - 2.0);
      int $$3 = SectionPos.posToSectionCoord($$0.minY - 4.0);
      int $$4 = SectionPos.posToSectionCoord($$0.minZ - 2.0);
      int $$5 = SectionPos.posToSectionCoord($$0.maxX + 2.0);
      int $$6 = SectionPos.posToSectionCoord($$0.maxY + 0.0);
      int $$7 = SectionPos.posToSectionCoord($$0.maxZ + 2.0);

      for (int $$8 = $$2; $$8 <= $$5; $$8++) {
         long $$9 = SectionPos.asLong($$8, 0, 0);
         long $$10 = SectionPos.asLong($$8, -1, -1);
         LongIterator $$11 = this.sectionIds.subSet($$9, $$10 + 1L).iterator();

         while ($$11.hasNext()) {
            long $$12 = $$11.nextLong();
            int $$13 = SectionPos.y($$12);
            int $$14 = SectionPos.z($$12);
            if ($$13 >= $$3 && $$13 <= $$6 && $$14 >= $$4 && $$14 <= $$7) {
               EntitySection<T> $$15 = (EntitySection<T>)this.sections.get($$12);
               if ($$15 != null && !$$15.isEmpty() && $$15.getStatus().isAccessible() && $$1.accept($$15).shouldAbort()) {
                  return;
               }
            }
         }
      }
   }

   public LongStream getExistingSectionPositionsInChunk(long $$0) {
      int $$1 = net.minecraft.world.level.ChunkPos.getX($$0);
      int $$2 = net.minecraft.world.level.ChunkPos.getZ($$0);
      LongSortedSet $$3 = this.getChunkSections($$1, $$2);
      if ($$3.isEmpty()) {
         return LongStream.empty();
      } else {
         OfLong $$4 = $$3.iterator();
         return StreamSupport.longStream(Spliterators.spliteratorUnknownSize($$4, 1301), false);
      }
   }

   private LongSortedSet getChunkSections(int $$0, int $$1) {
      long $$2 = SectionPos.asLong($$0, 0, $$1);
      long $$3 = SectionPos.asLong($$0, -1, $$1);
      return this.sectionIds.subSet($$2, $$3 + 1L);
   }

   public Stream<EntitySection<T>> getExistingSectionsInChunk(long $$0) {
      return this.getExistingSectionPositionsInChunk($$0).<EntitySection<T>>mapToObj(this.sections::get).filter(Objects::nonNull);
   }

   private static long getChunkKeyFromSectionKey(long $$0) {
      return net.minecraft.world.level.ChunkPos.asLong(SectionPos.x($$0), SectionPos.z($$0));
   }

   public EntitySection<T> getOrCreateSection(long $$0) {
      return (EntitySection<T>)this.sections.computeIfAbsent($$0, this::createSection);
   }

   @Nullable
   public EntitySection<T> getSection(long $$0) {
      return (EntitySection<T>)this.sections.get($$0);
   }

   private EntitySection<T> createSection(long $$0) {
      long $$1 = getChunkKeyFromSectionKey($$0);
      Visibility $$2 = (Visibility)this.intialSectionVisibility.get($$1);
      this.sectionIds.add($$0);
      return new EntitySection<>(this.entityClass, $$2);
   }

   public LongSet getAllChunksWithExistingSections() {
      LongSet $$0 = new LongOpenHashSet();
      this.sections.keySet().forEach($$1 -> $$0.add(getChunkKeyFromSectionKey($$1)));
      return $$0;
   }

   public void getEntities(AABB $$0, AbortableIterationConsumer<T> $$1) {
      this.forEachAccessibleNonEmptySection($$0, $$2 -> $$2.getEntities($$0, $$1));
   }

   public <U extends T> void getEntities(EntityTypeTest<T, U> $$0, AABB $$1, AbortableIterationConsumer<U> $$2) {
      this.forEachAccessibleNonEmptySection($$1, $$3 -> $$3.getEntities($$0, $$1, $$2));
   }

   public void remove(long $$0) {
      this.sections.remove($$0);
      this.sectionIds.remove($$0);
   }

   @VisibleForDebug
   public int count() {
      return this.sectionIds.size();
   }
}
