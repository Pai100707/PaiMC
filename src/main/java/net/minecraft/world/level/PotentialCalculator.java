package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;

public class PotentialCalculator {
   private final List<net.minecraft.world.level.PotentialCalculator.PointCharge> charges = Lists.newArrayList();

   public void addCharge(BlockPos $$0, double $$1) {
      if ($$1 != 0.0) {
         this.charges.add(new net.minecraft.world.level.PotentialCalculator.PointCharge($$0, $$1));
      }
   }

   public double getPotentialEnergyChange(BlockPos $$0, double $$1) {
      if ($$1 == 0.0) {
         return 0.0;
      } else {
         double $$2 = 0.0;

         for (net.minecraft.world.level.PotentialCalculator.PointCharge $$3 : this.charges) {
            $$2 += $$3.getPotentialChange($$0);
         }

         return $$2 * $$1;
      }
   }

   static class PointCharge {
      private final BlockPos pos;
      private final double charge;

      public PointCharge(BlockPos $$0, double $$1) {
         this.pos = $$0;
         this.charge = $$1;
      }

      public double getPotentialChange(BlockPos $$0) {
         double $$1 = this.pos.distSqr($$0);
         return $$1 == 0.0 ? Double.POSITIVE_INFINITY : this.charge / Math.sqrt($$1);
      }
   }
}
