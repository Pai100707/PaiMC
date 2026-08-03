package net.minecraft.world.level.levelgen;

import net.minecraft.util.RandomSource;

public interface BitRandomSource extends RandomSource {
   float FLOAT_MULTIPLIER = 5.9604645E-8F;
   double DOUBLE_MULTIPLIER = 1.110223E-16F;

   int next(int var1);

   default int nextInt() {
      return this.next(32);
   }

   default int nextInt(int $$0) {
      if ($$0 <= 0) {
         throw new IllegalArgumentException("Bound must be positive");
      } else if (($$0 & $$0 - 1) == 0) {
         return (int)((long)$$0 * this.next(31) >> 31);
      } else {
         int $$1;
         int $$2;
         do {
            $$1 = this.next(31);
            $$2 = $$1 % $$0;
         } while ($$1 - $$2 + ($$0 - 1) < 0);

         return $$2;
      }
   }

   default long nextLong() {
      int $$0 = this.next(32);
      int $$1 = this.next(32);
      long $$2 = (long)$$0 << 32;
      return $$2 + $$1;
   }

   default boolean nextBoolean() {
      return this.next(1) != 0;
   }

   default float nextFloat() {
      return this.next(24) * 5.9604645E-8F;
   }

   default double nextDouble() {
      int $$0 = this.next(26);
      int $$1 = this.next(27);
      long $$2 = ((long)$$0 << 27) + $$1;
      return $$2 * 1.110223E-16F;
   }
}
