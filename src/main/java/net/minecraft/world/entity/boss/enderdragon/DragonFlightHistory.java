package net.minecraft.world.entity.boss.enderdragon;

import java.util.Arrays;
import net.minecraft.util.Mth;

public class DragonFlightHistory {
   public static final int LENGTH = 64;
   private static final int MASK = 63;
   private final DragonFlightHistory.Sample[] samples = new DragonFlightHistory.Sample[64];
   private int head = -1;

   public DragonFlightHistory() {
      Arrays.fill(this.samples, new DragonFlightHistory.Sample(0.0, 0.0F));
   }

   public void copyFrom(DragonFlightHistory $$0) {
      System.arraycopy($$0.samples, 0, this.samples, 0, 64);
      this.head = $$0.head;
   }

   public void record(double $$0, float $$1) {
      DragonFlightHistory.Sample $$2 = new DragonFlightHistory.Sample($$0, $$1);
      if (this.head < 0) {
         Arrays.fill(this.samples, $$2);
      }

      if (++this.head == 64) {
         this.head = 0;
      }

      this.samples[this.head] = $$2;
   }

   public DragonFlightHistory.Sample get(int $$0) {
      return this.samples[this.head - $$0 & 63];
   }

   public DragonFlightHistory.Sample get(int $$0, float $$1) {
      DragonFlightHistory.Sample $$2 = this.get($$0);
      DragonFlightHistory.Sample $$3 = this.get($$0 + 1);
      return new DragonFlightHistory.Sample(Mth.lerp($$1, $$3.y, $$2.y), Mth.rotLerp($$1, $$3.yRot, $$2.yRot));
   }

   public record Sample(double y, float yRot) {
   }
}
