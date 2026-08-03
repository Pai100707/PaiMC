package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class BlockLightSectionStorage extends LayerLightSectionStorage<BlockLightSectionStorage.BlockDataLayerStorageMap> {
   protected BlockLightSectionStorage(LightChunkGetter $$0) {
      super(net.minecraft.world.level.LightLayer.BLOCK, $$0, new BlockLightSectionStorage.BlockDataLayerStorageMap(new Long2ObjectOpenHashMap()));
   }

   @Override
   protected int getLightValue(long $$0) {
      long $$1 = SectionPos.blockToSection($$0);
      DataLayer $$2 = this.getDataLayer($$1, false);
      return $$2 == null
         ? 0
         : $$2.get(
            SectionPos.sectionRelative(BlockPos.getX($$0)), SectionPos.sectionRelative(BlockPos.getY($$0)), SectionPos.sectionRelative(BlockPos.getZ($$0))
         );
   }

   protected static final class BlockDataLayerStorageMap extends DataLayerStorageMap<BlockLightSectionStorage.BlockDataLayerStorageMap> {
      public BlockDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> $$0) {
         super($$0);
      }

      public BlockLightSectionStorage.BlockDataLayerStorageMap copy() {
         return new BlockLightSectionStorage.BlockDataLayerStorageMap(this.map.clone());
      }
   }
}
