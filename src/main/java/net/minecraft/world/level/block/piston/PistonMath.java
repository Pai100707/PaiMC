package net.minecraft.world.level.block.piston;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class PistonMath {
   public static AABB getMovementArea(AABB $$0, Direction $$1, double $$2) {
      double $$3 = $$2 * $$1.getAxisDirection().getStep();
      double $$4 = Math.min($$3, 0.0);
      double $$5 = Math.max($$3, 0.0);
      switch ($$1) {
         case WEST:
            return new AABB($$0.minX + $$4, $$0.minY, $$0.minZ, $$0.minX + $$5, $$0.maxY, $$0.maxZ);
         case EAST:
            return new AABB($$0.maxX + $$4, $$0.minY, $$0.minZ, $$0.maxX + $$5, $$0.maxY, $$0.maxZ);
         case DOWN:
            return new AABB($$0.minX, $$0.minY + $$4, $$0.minZ, $$0.maxX, $$0.minY + $$5, $$0.maxZ);
         case UP:
         default:
            return new AABB($$0.minX, $$0.maxY + $$4, $$0.minZ, $$0.maxX, $$0.maxY + $$5, $$0.maxZ);
         case NORTH:
            return new AABB($$0.minX, $$0.minY, $$0.minZ + $$4, $$0.maxX, $$0.maxY, $$0.minZ + $$5);
         case SOUTH:
            return new AABB($$0.minX, $$0.minY, $$0.maxZ + $$4, $$0.maxX, $$0.maxY, $$0.maxZ + $$5);
      }
   }
}
