package net.minecraft.util.valueproviders;

import java.util.Arrays;

public class MultipliedFloats implements SampledFloat {
   private final SampledFloat[] values;

   public MultipliedFloats(SampledFloat... $$0) {
      this.values = $$0;
   }

   @Override
   public float sample(net.minecraft.util.RandomSource $$0) {
      float $$1 = 1.0F;

      for (SampledFloat $$2 : this.values) {
         $$1 *= $$2.sample($$0);
      }

      return $$1;
   }

   @Override
   public String toString() {
      return "MultipliedFloats" + Arrays.toString((Object[])this.values);
   }
}
