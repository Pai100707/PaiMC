package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

public final class CubeVoxelShape extends VoxelShape {
   protected CubeVoxelShape(DiscreteVoxelShape $$0) {
      super($$0);
   }

   @Override
   public DoubleList getCoords(Axis $$0) {
      return new CubePointRange(this.shape.getSize($$0));
   }

   @Override
   protected int findIndex(Axis $$0, double $$1) {
      int $$2 = this.shape.getSize($$0);
      return Mth.floor(Mth.clamp($$1 * $$2, -1.0, $$2));
   }
}
