package net.minecraft.world.level.levelgen.synth;

import java.util.Locale;

public class NoiseUtils {
   public static double biasTowardsExtreme(double $$0, double $$1) {
      return $$0 + Math.sin(Math.PI * $$0) * $$1 / Math.PI;
   }

   public static void parityNoiseOctaveConfigString(StringBuilder $$0, double $$1, double $$2, double $$3, byte[] $$4) {
      $$0.append(String.format(Locale.ROOT, "xo=%.3f, yo=%.3f, zo=%.3f, p0=%d, p255=%d", (float)$$1, (float)$$2, (float)$$3, $$4[0], $$4[255]));
   }

   public static void parityNoiseOctaveConfigString(StringBuilder $$0, double $$1, double $$2, double $$3, int[] $$4) {
      $$0.append(String.format(Locale.ROOT, "xo=%.3f, yo=%.3f, zo=%.3f, p0=%d, p255=%d", (float)$$1, (float)$$2, (float)$$3, $$4[0], $$4[255]));
   }
}
