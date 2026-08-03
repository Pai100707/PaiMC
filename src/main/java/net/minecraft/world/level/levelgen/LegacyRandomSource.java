package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ThreadingDetector;

public class LegacyRandomSource implements BitRandomSource {
   private static final int MODULUS_BITS = 48;
   private static final long MODULUS_MASK = 281474976710655L;
   private static final long MULTIPLIER = 25214903917L;
   private static final long INCREMENT = 11L;
   private final AtomicLong seed = new AtomicLong();
   private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

   public LegacyRandomSource(long $$0) {
      this.setSeed($$0);
   }

   public RandomSource fork() {
      return new LegacyRandomSource(this.nextLong());
   }

   public PositionalRandomFactory forkPositional() {
      return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
   }

   public void setSeed(long $$0) {
      if (!this.seed.compareAndSet(this.seed.get(), ($$0 ^ 25214903917L) & 281474976710655L)) {
         throw ThreadingDetector.makeThreadingException("LegacyRandomSource", null);
      } else {
         this.gaussianSource.reset();
      }
   }

   @Override
   public int next(int $$0) {
      long $$1 = this.seed.get();
      long $$2 = $$1 * 25214903917L + 11L & 281474976710655L;
      if (!this.seed.compareAndSet($$1, $$2)) {
         throw ThreadingDetector.makeThreadingException("LegacyRandomSource", null);
      } else {
         return (int)($$2 >> 48 - $$0);
      }
   }

   public double nextGaussian() {
      return this.gaussianSource.nextGaussian();
   }

   public static class LegacyPositionalRandomFactory implements PositionalRandomFactory {
      private final long seed;

      public LegacyPositionalRandomFactory(long $$0) {
         this.seed = $$0;
      }

      @Override
      public RandomSource at(int $$0, int $$1, int $$2) {
         long $$3 = Mth.getSeed($$0, $$1, $$2);
         long $$4 = $$3 ^ this.seed;
         return new LegacyRandomSource($$4);
      }

      @Override
      public RandomSource fromHashOf(String $$0) {
         int $$1 = $$0.hashCode();
         return new LegacyRandomSource($$1 ^ this.seed);
      }

      @Override
      public RandomSource fromSeed(long $$0) {
         return new LegacyRandomSource($$0);
      }

      @VisibleForTesting
      @Override
      public void parityConfigString(StringBuilder $$0) {
         $$0.append("LegacyPositionalRandomFactory{").append(this.seed).append("}");
      }
   }
}
