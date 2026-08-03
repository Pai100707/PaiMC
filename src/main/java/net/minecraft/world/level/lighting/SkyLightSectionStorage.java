package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class SkyLightSectionStorage extends LayerLightSectionStorage<SkyLightSectionStorage.SkyDataLayerStorageMap> {
   protected SkyLightSectionStorage(LightChunkGetter $$0) {
      super(
         net.minecraft.world.level.LightLayer.SKY,
         $$0,
         new SkyLightSectionStorage.SkyDataLayerStorageMap(new Long2ObjectOpenHashMap(), new Long2IntOpenHashMap(), Integer.MAX_VALUE)
      );
   }

   @Override
   protected int getLightValue(long $$0) {
      return this.getLightValue($$0, false);
   }

   protected int getLightValue(long $$0, boolean $$1) {
      long $$2 = SectionPos.blockToSection($$0);
      int $$3 = SectionPos.y($$2);
      SkyLightSectionStorage.SkyDataLayerStorageMap $$4 = $$1 ? this.updatingSectionData : this.visibleSectionData;
      int $$5 = $$4.topSections.get(SectionPos.getZeroNode($$2));
      if ($$5 != $$4.currentLowestY && $$3 < $$5) {
         DataLayer $$6 = this.getDataLayer($$4, $$2);
         if ($$6 == null) {
            for ($$0 = BlockPos.getFlatIndex($$0); $$6 == null; $$6 = this.getDataLayer($$4, $$2)) {
               if (++$$3 >= $$5) {
                  return 15;
               }

               $$2 = SectionPos.offset($$2, Direction.UP);
            }
         }

         return $$6.get(
            SectionPos.sectionRelative(BlockPos.getX($$0)), SectionPos.sectionRelative(BlockPos.getY($$0)), SectionPos.sectionRelative(BlockPos.getZ($$0))
         );
      } else {
         return $$1 && !this.lightOnInSection($$2) ? 0 : 15;
      }
   }

   @Override
   protected void onNodeAdded(long $$0) {
      int $$1 = SectionPos.y($$0);
      if (this.updatingSectionData.currentLowestY > $$1) {
         this.updatingSectionData.currentLowestY = $$1;
         this.updatingSectionData.topSections.defaultReturnValue(this.updatingSectionData.currentLowestY);
      }

      long $$2 = SectionPos.getZeroNode($$0);
      int $$3 = this.updatingSectionData.topSections.get($$2);
      if ($$3 < $$1 + 1) {
         this.updatingSectionData.topSections.put($$2, $$1 + 1);
      }
   }

   @Override
   protected void onNodeRemoved(long $$0) {
      long $$1 = SectionPos.getZeroNode($$0);
      int $$2 = SectionPos.y($$0);
      if (this.updatingSectionData.topSections.get($$1) == $$2 + 1) {
         long $$3;
         for ($$3 = $$0; !this.storingLightForSection($$3) && this.hasLightDataAtOrBelow($$2); $$3 = SectionPos.offset($$3, Direction.DOWN)) {
            $$2--;
         }

         if (this.storingLightForSection($$3)) {
            this.updatingSectionData.topSections.put($$1, $$2 + 1);
         } else {
            this.updatingSectionData.topSections.remove($$1);
         }
      }
   }

   @Override
   protected DataLayer createDataLayer(long $$0) {
      DataLayer $$1 = (DataLayer)this.queuedSections.get($$0);
      if ($$1 != null) {
         return $$1;
      } else {
         int $$2 = this.updatingSectionData.topSections.get(SectionPos.getZeroNode($$0));
         if ($$2 != this.updatingSectionData.currentLowestY && SectionPos.y($$0) < $$2) {
            long $$3 = SectionPos.offset($$0, Direction.UP);

            DataLayer $$4;
            while (($$4 = this.getDataLayer($$3, true)) == null) {
               $$3 = SectionPos.offset($$3, Direction.UP);
            }

            return repeatFirstLayer($$4);
         } else {
            return this.lightOnInSection($$0) ? new DataLayer(15) : new DataLayer();
         }
      }
   }

   private static DataLayer repeatFirstLayer(DataLayer $$0) {
      if ($$0.isDefinitelyHomogenous()) {
         return $$0.copy();
      } else {
         byte[] $$1 = $$0.getData();
         byte[] $$2 = new byte[2048];

         for (int $$3 = 0; $$3 < 16; $$3++) {
            System.arraycopy($$1, 0, $$2, $$3 * 128, 128);
         }

         return new DataLayer($$2);
      }
   }

   protected boolean hasLightDataAtOrBelow(int $$0) {
      return $$0 >= this.updatingSectionData.currentLowestY;
   }

   protected boolean isAboveData(long $$0) {
      long $$1 = SectionPos.getZeroNode($$0);
      int $$2 = this.updatingSectionData.topSections.get($$1);
      return $$2 == this.updatingSectionData.currentLowestY || SectionPos.y($$0) >= $$2;
   }

   protected int getTopSectionY(long $$0) {
      return this.updatingSectionData.topSections.get($$0);
   }

   protected int getBottomSectionY() {
      return this.updatingSectionData.currentLowestY;
   }

   protected static final class SkyDataLayerStorageMap extends DataLayerStorageMap<SkyLightSectionStorage.SkyDataLayerStorageMap> {
      int currentLowestY;
      final Long2IntOpenHashMap topSections;

      public SkyDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> $$0, Long2IntOpenHashMap $$1, int $$2) {
         super($$0);
         this.topSections = $$1;
         $$1.defaultReturnValue($$2);
         this.currentLowestY = $$2;
      }

      public SkyLightSectionStorage.SkyDataLayerStorageMap copy() {
         return new SkyLightSectionStorage.SkyDataLayerStorageMap(this.map.clone(), this.topSections.clone(), this.currentLowestY);
      }
   }
}
