package net.minecraft.world.level;

public class GrassColor {
   private static int[] pixels = new int[65536];

   public static void init(int[] $$0) {
      pixels = $$0;
   }

   public static int get(double $$0, double $$1) {
      return net.minecraft.world.level.ColorMapColorUtil.get($$0, $$1, pixels, -65281);
   }

   public static int getDefaultColor() {
      return get(0.5, 1.0);
   }
}
