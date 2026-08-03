package net.minecraft.world.level;

public interface ColorMapColorUtil {
   static int get(double $$0, double $$1, int[] $$2, int $$3) {
      $$1 *= $$0;
      int $$4 = (int)((1.0 - $$0) * 255.0);
      int $$5 = (int)((1.0 - $$1) * 255.0);
      int $$6 = $$5 << 8 | $$4;
      return $$6 >= $$2.length ? $$3 : $$2[$$6];
   }
}
