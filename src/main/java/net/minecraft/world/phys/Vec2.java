package net.minecraft.world.phys;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class Vec2 {
   public static final net.minecraft.world.phys.Vec2 ZERO = new net.minecraft.world.phys.Vec2(0.0F, 0.0F);
   public static final net.minecraft.world.phys.Vec2 ONE = new net.minecraft.world.phys.Vec2(1.0F, 1.0F);
   public static final net.minecraft.world.phys.Vec2 UNIT_X = new net.minecraft.world.phys.Vec2(1.0F, 0.0F);
   public static final net.minecraft.world.phys.Vec2 NEG_UNIT_X = new net.minecraft.world.phys.Vec2(-1.0F, 0.0F);
   public static final net.minecraft.world.phys.Vec2 UNIT_Y = new net.minecraft.world.phys.Vec2(0.0F, 1.0F);
   public static final net.minecraft.world.phys.Vec2 NEG_UNIT_Y = new net.minecraft.world.phys.Vec2(0.0F, -1.0F);
   public static final net.minecraft.world.phys.Vec2 MAX = new net.minecraft.world.phys.Vec2(Float.MAX_VALUE, Float.MAX_VALUE);
   public static final net.minecraft.world.phys.Vec2 MIN = new net.minecraft.world.phys.Vec2(Float.MIN_VALUE, Float.MIN_VALUE);
   public static final Codec<net.minecraft.world.phys.Vec2> CODEC = Codec.FLOAT
      .listOf()
      .comapFlatMap(
         $$0 -> Util.fixedSize($$0, 2).map($$0x -> new net.minecraft.world.phys.Vec2((Float)$$0x.get(0), (Float)$$0x.get(1))), $$0 -> List.of($$0.x, $$0.y)
      );
   public final float x;
   public final float y;

   public Vec2(float $$0, float $$1) {
      this.x = $$0;
      this.y = $$1;
   }

   public net.minecraft.world.phys.Vec2 scale(float $$0) {
      return new net.minecraft.world.phys.Vec2(this.x * $$0, this.y * $$0);
   }

   public float dot(net.minecraft.world.phys.Vec2 $$0) {
      return this.x * $$0.x + this.y * $$0.y;
   }

   public net.minecraft.world.phys.Vec2 add(net.minecraft.world.phys.Vec2 $$0) {
      return new net.minecraft.world.phys.Vec2(this.x + $$0.x, this.y + $$0.y);
   }

   public net.minecraft.world.phys.Vec2 add(float $$0) {
      return new net.minecraft.world.phys.Vec2(this.x + $$0, this.y + $$0);
   }

   public boolean equals(net.minecraft.world.phys.Vec2 $$0) {
      return this.x == $$0.x && this.y == $$0.y;
   }

   public net.minecraft.world.phys.Vec2 normalized() {
      float $$0 = Mth.sqrt(this.x * this.x + this.y * this.y);
      return $$0 < 1.0E-4F ? ZERO : new net.minecraft.world.phys.Vec2(this.x / $$0, this.y / $$0);
   }

   public float length() {
      return Mth.sqrt(this.x * this.x + this.y * this.y);
   }

   public float lengthSquared() {
      return this.x * this.x + this.y * this.y;
   }

   public float distanceToSqr(net.minecraft.world.phys.Vec2 $$0) {
      float $$1 = $$0.x - this.x;
      float $$2 = $$0.y - this.y;
      return $$1 * $$1 + $$2 * $$2;
   }

   public net.minecraft.world.phys.Vec2 negated() {
      return new net.minecraft.world.phys.Vec2(-this.x, -this.y);
   }
}
