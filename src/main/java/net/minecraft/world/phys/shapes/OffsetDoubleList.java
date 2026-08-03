package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class OffsetDoubleList extends AbstractDoubleList {
   private final DoubleList delegate;
   private final double offset;

   public OffsetDoubleList(DoubleList $$0, double $$1) {
      this.delegate = $$0;
      this.offset = $$1;
   }

   public double getDouble(int $$0) {
      return this.delegate.getDouble($$0) + this.offset;
   }

   public int size() {
      return this.delegate.size();
   }
}
