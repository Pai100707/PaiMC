package net.minecraft.core;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;

public enum Direction8 {
   NORTH(net.minecraft.core.Direction.NORTH),
   NORTH_EAST(net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.EAST),
   EAST(net.minecraft.core.Direction.EAST),
   SOUTH_EAST(net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.EAST),
   SOUTH(net.minecraft.core.Direction.SOUTH),
   SOUTH_WEST(net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.WEST),
   WEST(net.minecraft.core.Direction.WEST),
   NORTH_WEST(net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.WEST);

   private final Set<net.minecraft.core.Direction> directions;
   private final net.minecraft.core.Vec3i step;

   private Direction8(final net.minecraft.core.Direction... $$0) {
      this.directions = Sets.immutableEnumSet(Arrays.asList($$0));
      this.step = new net.minecraft.core.Vec3i(0, 0, 0);

      for (net.minecraft.core.Direction $$1 : $$0) {
         this.step.setX(this.step.getX() + $$1.getStepX()).setY(this.step.getY() + $$1.getStepY()).setZ(this.step.getZ() + $$1.getStepZ());
      }
   }

   public Set<net.minecraft.core.Direction> getDirections() {
      return this.directions;
   }

   public int getStepX() {
      return this.step.getX();
   }

   public int getStepZ() {
      return this.step.getZ();
   }
}
