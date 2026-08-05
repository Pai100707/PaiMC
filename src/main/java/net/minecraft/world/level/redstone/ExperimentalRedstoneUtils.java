package net.minecraft.world.level.redstone;

import net.minecraft.core.Direction;
import net.minecraft.world.flag.FeatureFlags;

public class ExperimentalRedstoneUtils {
   
   public static Orientation initialOrientation(net.minecraft.world.level.Level $$0, Direction $$1, Direction $$2) {
      if ($$0.enabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS)) {
         Orientation $$3 = Orientation.random($$0.random).withSideBias(Orientation.SideBias.LEFT);
         if ($$2 != null) {
            $$3 = $$3.withUp($$2);
         }

         if ($$1 != null) {
            $$3 = $$3.withFront($$1);
         }

         return $$3;
      } else {
         return null;
      }
   }

   
   public static Orientation withFront(Orientation $$0, Direction $$1) {
      return $$0 == null ? null : $$0.withFront($$1);
   }
}
