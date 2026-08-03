package net.minecraft.world.level.levelgen;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.RandomSource;

@Deprecated
public class ThreadSafeLegacyRandomSource implements BitRandomSource {
   private static final int MODULUS_BITS = 48;
   private static final long MODULUS_MASK = 281474976710655L;
   private static final long MULTIPLIER = 25214903917L;
   private static final long INCREMENT = 11L;
   private final AtomicLong seed = new AtomicLong();
   private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

   public ThreadSafeLegacyRandomSource(long $$0) {
      this.setSeed($$0);
   }

   public RandomSource fork() {
      return new ThreadSafeLegacyRandomSource(this.nextLong());
   }

   public PositionalRandomFactory forkPositional() {
      return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
   }

   public void setSeed(long $$0) {
      this.seed.set(($$0 ^ 25214903917L) & 281474976710655L);
   }

   @Override
   public int next(int $$0) {
      long $$1;
      long $$2;
      do {
         $$1 = this.seed.get();
         $$2 = $$1 * 25214903917L + 11L & 281474976710655L;
      } while (!this.seed.compareAndSet($$1, $$2));

      return (int)($$2 >>> 48 - $$0);
   }

   public double nextGaussian() {
      return this.gaussianSource.nextGaussian();
   }
}
