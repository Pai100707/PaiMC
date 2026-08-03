package net.minecraft.world.level.levelgen;

import net.minecraft.util.RandomSource;

public class SingleThreadedRandomSource implements BitRandomSource {
   private static final int MODULUS_BITS = 48;
   private static final long MODULUS_MASK = 281474976710655L;
   private static final long MULTIPLIER = 25214903917L;
   private static final long INCREMENT = 11L;
   private long seed;
   private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

   public SingleThreadedRandomSource(long $$0) {
      this.setSeed($$0);
   }

   public RandomSource fork() {
      return new SingleThreadedRandomSource(this.nextLong());
   }

   public PositionalRandomFactory forkPositional() {
      return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
   }

   public void setSeed(long $$0) {
      this.seed = ($$0 ^ 25214903917L) & 281474976710655L;
      this.gaussianSource.reset();
   }

   @Override
   public int next(int $$0) {
      long $$1 = this.seed * 25214903917L + 11L & 281474976710655L;
      this.seed = $$1;
      return (int)($$1 >> 48 - $$0);
   }

   public double nextGaussian() {
      return this.gaussianSource.nextGaussian();
   }
}
