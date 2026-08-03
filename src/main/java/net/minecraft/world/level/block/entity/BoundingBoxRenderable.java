package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public interface BoundingBoxRenderable {
   BoundingBoxRenderable.Mode renderMode();

   BoundingBoxRenderable.RenderableBox getRenderableBox();

   public static enum Mode {
      NONE,
      BOX,
      BOX_AND_INVISIBLE_BLOCKS;
   }

   public record RenderableBox(BlockPos localPos, Vec3i size) {
      public static BoundingBoxRenderable.RenderableBox fromCorners(int $$0, int $$1, int $$2, int $$3, int $$4, int $$5) {
         int $$6 = Math.min($$0, $$3);
         int $$7 = Math.min($$1, $$4);
         int $$8 = Math.min($$2, $$5);
         return new BoundingBoxRenderable.RenderableBox(
            new BlockPos($$6, $$7, $$8), new Vec3i(Math.max($$0, $$3) - $$6, Math.max($$1, $$4) - $$7, Math.max($$2, $$5) - $$8)
         );
      }
   }
}
