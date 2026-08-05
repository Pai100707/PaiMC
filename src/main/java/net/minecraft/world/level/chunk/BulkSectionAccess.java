package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BulkSectionAccess implements AutoCloseable {
   private final net.minecraft.world.level.LevelAccessor level;
   private final Long2ObjectMap<LevelChunkSection> acquiredSections = new Long2ObjectOpenHashMap();
   
   private LevelChunkSection lastSection;
   private long lastSectionKey;

   public BulkSectionAccess(net.minecraft.world.level.LevelAccessor $$0) {
      this.level = $$0;
   }

   
   public LevelChunkSection getSection(BlockPos $$0) {
      int $$1 = this.level.getSectionIndex($$0.getY());
      if ($$1 >= 0 && $$1 < this.level.getSectionsCount()) {
         long $$2 = SectionPos.asLong($$0);
         if (this.lastSection == null || this.lastSectionKey != $$2) {
            this.lastSection = (LevelChunkSection)this.acquiredSections.computeIfAbsent($$2, $$2x -> {
               ChunkAccess $$3 = this.level.getChunk(SectionPos.blockToSectionCoord($$0.getX()), SectionPos.blockToSectionCoord($$0.getZ()));
               LevelChunkSection $$4 = $$3.getSection($$1);
               $$4.acquire();
               return $$4;
            });
            this.lastSectionKey = $$2;
         }

         return this.lastSection;
      } else {
         return null;
      }
   }

   public BlockState getBlockState(BlockPos $$0) {
      LevelChunkSection $$1 = this.getSection($$0);
      if ($$1 == null) {
         return Blocks.AIR.defaultBlockState();
      } else {
         int $$2 = SectionPos.sectionRelative($$0.getX());
         int $$3 = SectionPos.sectionRelative($$0.getY());
         int $$4 = SectionPos.sectionRelative($$0.getZ());
         return $$1.getBlockState($$2, $$3, $$4);
      }
   }

   @Override
   public void close() {
      ObjectIterator var1 = this.acquiredSections.values().iterator();

      while (var1.hasNext()) {
         LevelChunkSection $$0 = (LevelChunkSection)var1.next();
         $$0.release();
      }
   }
}
