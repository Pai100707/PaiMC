package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction.Axis;

public class SliceShape extends VoxelShape {
   private final VoxelShape delegate;
   private final Axis axis;
   private static final DoubleList SLICE_COORDS = new CubePointRange(1);

   public SliceShape(VoxelShape $$0, Axis $$1, int $$2) {
      super(makeSlice($$0.shape, $$1, $$2));
      this.delegate = $$0;
      this.axis = $$1;
   }

   private static DiscreteVoxelShape makeSlice(DiscreteVoxelShape $$0, Axis $$1, int $$2) {
      return new SubShape(
         $$0,
         $$1.choose($$2, 0, 0),
         $$1.choose(0, $$2, 0),
         $$1.choose(0, 0, $$2),
         $$1.choose($$2 + 1, $$0.xSize, $$0.xSize),
         $$1.choose($$0.ySize, $$2 + 1, $$0.ySize),
         $$1.choose($$0.zSize, $$0.zSize, $$2 + 1)
      );
   }

   @Override
   public DoubleList getCoords(Axis $$0) {
      return $$0 == this.axis ? SLICE_COORDS : this.delegate.getCoords($$0);
   }
}
