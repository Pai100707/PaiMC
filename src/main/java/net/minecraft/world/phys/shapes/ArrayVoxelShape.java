package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Util;

public class ArrayVoxelShape extends VoxelShape {
   private final DoubleList xs;
   private final DoubleList ys;
   private final DoubleList zs;

   protected ArrayVoxelShape(DiscreteVoxelShape $$0, double[] $$1, double[] $$2, double[] $$3) {
      this(
         $$0,
         DoubleArrayList.wrap(Arrays.copyOf($$1, $$0.getXSize() + 1)),
         DoubleArrayList.wrap(Arrays.copyOf($$2, $$0.getYSize() + 1)),
         DoubleArrayList.wrap(Arrays.copyOf($$3, $$0.getZSize() + 1))
      );
   }

   ArrayVoxelShape(DiscreteVoxelShape $$0, DoubleList $$1, DoubleList $$2, DoubleList $$3) {
      super($$0);
      int $$4 = $$0.getXSize() + 1;
      int $$5 = $$0.getYSize() + 1;
      int $$6 = $$0.getZSize() + 1;
      if ($$4 == $$1.size() && $$5 == $$2.size() && $$6 == $$3.size()) {
         this.xs = $$1;
         this.ys = $$2;
         this.zs = $$3;
      } else {
         throw (IllegalArgumentException)Util.pauseInIde(
            new IllegalArgumentException("Lengths of point arrays must be consistent with the size of the VoxelShape.")
         );
      }
   }

   @Override
   public DoubleList getCoords(Axis $$0) {
      return switch ($$0) {
         case X -> this.xs;
         case Y -> this.ys;
         case Z -> this.zs;
         default -> throw new MatchException(null, null);
      };
   }
}
