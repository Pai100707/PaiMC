package net.minecraft.core;

import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;

public enum FrontAndTop implements StringRepresentable {
   DOWN_EAST("down_east", net.minecraft.core.Direction.DOWN, net.minecraft.core.Direction.EAST),
   DOWN_NORTH("down_north", net.minecraft.core.Direction.DOWN, net.minecraft.core.Direction.NORTH),
   DOWN_SOUTH("down_south", net.minecraft.core.Direction.DOWN, net.minecraft.core.Direction.SOUTH),
   DOWN_WEST("down_west", net.minecraft.core.Direction.DOWN, net.minecraft.core.Direction.WEST),
   UP_EAST("up_east", net.minecraft.core.Direction.UP, net.minecraft.core.Direction.EAST),
   UP_NORTH("up_north", net.minecraft.core.Direction.UP, net.minecraft.core.Direction.NORTH),
   UP_SOUTH("up_south", net.minecraft.core.Direction.UP, net.minecraft.core.Direction.SOUTH),
   UP_WEST("up_west", net.minecraft.core.Direction.UP, net.minecraft.core.Direction.WEST),
   WEST_UP("west_up", net.minecraft.core.Direction.WEST, net.minecraft.core.Direction.UP),
   EAST_UP("east_up", net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.UP),
   NORTH_UP("north_up", net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.UP),
   SOUTH_UP("south_up", net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.UP);

   private static final int NUM_DIRECTIONS = net.minecraft.core.Direction.values().length;
   private static final net.minecraft.core.FrontAndTop[] BY_TOP_FRONT = (net.minecraft.core.FrontAndTop[])Util.make(
      new net.minecraft.core.FrontAndTop[NUM_DIRECTIONS * NUM_DIRECTIONS], $$0 -> {
         for (net.minecraft.core.FrontAndTop $$1 : values()) {
            $$0[lookupKey($$1.front, $$1.top)] = $$1;
         }
      }
   );
   private final String name;
   private final net.minecraft.core.Direction top;
   private final net.minecraft.core.Direction front;

   private static int lookupKey(net.minecraft.core.Direction $$0, net.minecraft.core.Direction $$1) {
      return $$0.ordinal() * NUM_DIRECTIONS + $$1.ordinal();
   }

   private FrontAndTop(final String $$0, final net.minecraft.core.Direction $$1, final net.minecraft.core.Direction $$2) {
      this.name = $$0;
      this.front = $$1;
      this.top = $$2;
   }

   public String getSerializedName() {
      return this.name;
   }

   public static net.minecraft.core.FrontAndTop fromFrontAndTop(net.minecraft.core.Direction $$0, net.minecraft.core.Direction $$1) {
      return BY_TOP_FRONT[lookupKey($$0, $$1)];
   }

   public net.minecraft.core.Direction front() {
      return this.front;
   }

   public net.minecraft.core.Direction top() {
      return this.top;
   }
}
