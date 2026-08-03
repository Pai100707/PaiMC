package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum DripstoneThickness implements StringRepresentable {
   TIP_MERGE("tip_merge"),
   TIP("tip"),
   FRUSTUM("frustum"),
   MIDDLE("middle"),
   BASE("base");

   private final String name;

   private DripstoneThickness(final String $$0) {
      this.name = $$0;
   }

   @Override
   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }
}
