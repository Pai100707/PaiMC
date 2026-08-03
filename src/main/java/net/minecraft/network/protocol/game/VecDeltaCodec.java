package net.minecraft.network.protocol.game;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.world.phys.Vec3;

public class VecDeltaCodec {
   private static final double TRUNCATION_STEPS = 4096.0;
   private Vec3 base = Vec3.ZERO;

   @VisibleForTesting
   static long encode(double $$0) {
      return Math.round($$0 * 4096.0);
   }

   @VisibleForTesting
   static double decode(long $$0) {
      return $$0 / 4096.0;
   }

   public Vec3 decode(long $$0, long $$1, long $$2) {
      if ($$0 == 0L && $$1 == 0L && $$2 == 0L) {
         return this.base;
      } else {
         double $$3 = $$0 == 0L ? this.base.x : decode(encode(this.base.x) + $$0);
         double $$4 = $$1 == 0L ? this.base.y : decode(encode(this.base.y) + $$1);
         double $$5 = $$2 == 0L ? this.base.z : decode(encode(this.base.z) + $$2);
         return new Vec3($$3, $$4, $$5);
      }
   }

   public long encodeX(Vec3 $$0) {
      return encode($$0.x) - encode(this.base.x);
   }

   public long encodeY(Vec3 $$0) {
      return encode($$0.y) - encode(this.base.y);
   }

   public long encodeZ(Vec3 $$0) {
      return encode($$0.z) - encode(this.base.z);
   }

   public Vec3 delta(Vec3 $$0) {
      return $$0.subtract(this.base);
   }

   public void setBase(Vec3 $$0) {
      this.base = $$0;
   }

   public Vec3 getBase() {
      return this.base;
   }
}
