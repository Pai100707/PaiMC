package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LevelHeightAccessor {
   int getHeight();

   int getMinY();

   default int getMaxY() {
      return this.getMinY() + this.getHeight() - 1;
   }

   default int getSectionsCount() {
      return this.getMaxSectionY() - this.getMinSectionY() + 1;
   }

   default int getMinSectionY() {
      return SectionPos.blockToSectionCoord(this.getMinY());
   }

   default int getMaxSectionY() {
      return SectionPos.blockToSectionCoord(this.getMaxY());
   }

   default boolean isInsideBuildHeight(int $$0) {
      return $$0 >= this.getMinY() && $$0 <= this.getMaxY();
   }

   default boolean isOutsideBuildHeight(BlockPos $$0) {
      return this.isOutsideBuildHeight($$0.getY());
   }

   default boolean isOutsideBuildHeight(int $$0) {
      return $$0 < this.getMinY() || $$0 > this.getMaxY();
   }

   default int getSectionIndex(int $$0) {
      return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord($$0));
   }

   default int getSectionIndexFromSectionY(int $$0) {
      return $$0 - this.getMinSectionY();
   }

   default int getSectionYFromSectionIndex(int $$0) {
      return $$0 + this.getMinSectionY();
   }

   static net.minecraft.world.level.LevelHeightAccessor create(final int $$0, final int $$1) {
      return new net.minecraft.world.level.LevelHeightAccessor() {
         @Override
         public int getHeight() {
            return $$1;
         }

         @Override
         public int getMinY() {
            return $$0;
         }
      };
   }
}
