package net.minecraft.world.flag;

public class FeatureFlag {
   final net.minecraft.world.flag.FeatureFlagUniverse universe;
   final long mask;

   FeatureFlag(net.minecraft.world.flag.FeatureFlagUniverse $$0, int $$1) {
      this.universe = $$0;
      this.mask = 1L << $$1;
   }
}
