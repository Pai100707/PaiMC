package net.minecraft.world.level.levelgen.synth;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class PerlinSimplexNoise {
   private final SimplexNoise[] noiseLevels;
   private final double highestFreqValueFactor;
   private final double highestFreqInputFactor;

   public PerlinSimplexNoise(RandomSource $$0, List<Integer> $$1) {
      this($$0, new IntRBTreeSet($$1));
   }

   private PerlinSimplexNoise(RandomSource $$0, IntSortedSet $$1) {
      if ($$1.isEmpty()) {
         throw new IllegalArgumentException("Need some octaves!");
      } else {
         int $$2 = -$$1.firstInt();
         int $$3 = $$1.lastInt();
         int $$4 = $$2 + $$3 + 1;
         if ($$4 < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
         } else {
            SimplexNoise $$5 = new SimplexNoise($$0);
            int $$6 = $$3;
            this.noiseLevels = new SimplexNoise[$$4];
            if ($$3 >= 0 && $$3 < $$4 && $$1.contains(0)) {
               this.noiseLevels[$$3] = $$5;
            }

            for (int $$7 = $$3 + 1; $$7 < $$4; $$7++) {
               if ($$7 >= 0 && $$1.contains($$6 - $$7)) {
                  this.noiseLevels[$$7] = new SimplexNoise($$0);
               } else {
                  $$0.consumeCount(262);
               }
            }

            if ($$3 > 0) {
               long $$8 = (long)($$5.getValue($$5.xo, $$5.yo, $$5.zo) * 9.223372E18F);
               RandomSource $$9 = new WorldgenRandom(new LegacyRandomSource($$8));

               for (int $$10 = $$6 - 1; $$10 >= 0; $$10--) {
                  if ($$10 < $$4 && $$1.contains($$6 - $$10)) {
                     this.noiseLevels[$$10] = new SimplexNoise($$9);
                  } else {
                     $$9.consumeCount(262);
                  }
               }
            }

            this.highestFreqInputFactor = Math.pow(2.0, $$3);
            this.highestFreqValueFactor = 1.0 / (Math.pow(2.0, $$4) - 1.0);
         }
      }
   }

   public double getValue(double $$0, double $$1, boolean $$2) {
      double $$3 = 0.0;
      double $$4 = this.highestFreqInputFactor;
      double $$5 = this.highestFreqValueFactor;

      for (SimplexNoise $$6 : this.noiseLevels) {
         if ($$6 != null) {
            $$3 += $$6.getValue($$0 * $$4 + ($$2 ? $$6.xo : 0.0), $$1 * $$4 + ($$2 ? $$6.yo : 0.0)) * $$5;
         }

         $$4 /= 2.0;
         $$5 *= 2.0;
      }

      return $$3;
   }
}
