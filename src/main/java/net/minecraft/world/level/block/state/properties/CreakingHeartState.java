package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum CreakingHeartState implements StringRepresentable {
   UPROOTED("uprooted"),
   DORMANT("dormant"),
   AWAKE("awake");

   private final String name;

   private CreakingHeartState(final String $$0) {
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
