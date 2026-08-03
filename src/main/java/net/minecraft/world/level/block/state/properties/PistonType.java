package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum PistonType implements StringRepresentable {
   DEFAULT("normal"),
   STICKY("sticky");

   private final String name;

   private PistonType(final String $$0) {
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
