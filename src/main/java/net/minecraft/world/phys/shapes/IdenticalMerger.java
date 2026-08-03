package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public class IdenticalMerger implements IndexMerger {
   private final DoubleList coords;

   public IdenticalMerger(DoubleList $$0) {
      this.coords = $$0;
   }

   @Override
   public boolean forMergedIndexes(IndexMerger.IndexConsumer $$0) {
      int $$1 = this.coords.size() - 1;

      for (int $$2 = 0; $$2 < $$1; $$2++) {
         if (!$$0.merge($$2, $$2, $$2)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int size() {
      return this.coords.size();
   }

   @Override
   public DoubleList getList() {
      return this.coords;
   }
}
