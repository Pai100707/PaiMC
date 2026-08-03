package net.minecraft.util;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;

public interface RandomSource {
   @Deprecated
   double GAUSSIAN_SPREAD_FACTOR = 2.297;

   static net.minecraft.util.RandomSource create() {
      return create(RandomSupport.generateUniqueSeed());
   }

   @Deprecated
   static net.minecraft.util.RandomSource createThreadSafe() {
      return new ThreadSafeLegacyRandomSource(RandomSupport.generateUniqueSeed());
   }

   static net.minecraft.util.RandomSource create(long $$0) {
      return new LegacyRandomSource($$0);
   }

   static net.minecraft.util.RandomSource createNewThreadLocalInstance() {
      return new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong());
   }

   net.minecraft.util.RandomSource fork();

   PositionalRandomFactory forkPositional();

   void setSeed(long var1);

   int nextInt();

   int nextInt(int var1);

   default int nextIntBetweenInclusive(int $$0, int $$1) {
      return this.nextInt($$1 - $$0 + 1) + $$0;
   }

   long nextLong();

   boolean nextBoolean();

   float nextFloat();

   double nextDouble();

   double nextGaussian();

   default double triangle(double $$0, double $$1) {
      return $$0 + $$1 * (this.nextDouble() - this.nextDouble());
   }

   default float triangle(float $$0, float $$1) {
      return $$0 + $$1 * (this.nextFloat() - this.nextFloat());
   }

   default void consumeCount(int $$0) {
      for (int $$1 = 0; $$1 < $$0; $$1++) {
         this.nextInt();
      }
   }

   default int nextInt(int $$0, int $$1) {
      if ($$0 >= $$1) {
         throw new IllegalArgumentException("bound - origin is non positive");
      } else {
         return $$0 + this.nextInt($$1 - $$0);
      }
   }
}
