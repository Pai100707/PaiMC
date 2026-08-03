package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;

public class TheEndPortalBlockEntity extends BlockEntity {
   protected TheEndPortalBlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2) {
      super($$0, $$1, $$2);
   }

   public TheEndPortalBlockEntity(BlockPos $$0, BlockState $$1) {
      this(BlockEntityType.END_PORTAL, $$0, $$1);
   }

   public boolean shouldRenderFace(Direction $$0) {
      return $$0.getAxis() == Axis.Y;
   }
}
