package net.minecraft.world.phys;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Vec3 implements Position {
   public static final Codec<net.minecraft.world.phys.Vec3> CODEC = Codec.DOUBLE
      .listOf()
      .comapFlatMap(
         $$0 -> Util.fixedSize($$0, 3).map($$0x -> new net.minecraft.world.phys.Vec3((Double)$$0x.get(0), (Double)$$0x.get(1), (Double)$$0x.get(2))),
         $$0 -> List.of($$0.x(), $$0.y(), $$0.z())
      );
   public static final StreamCodec<ByteBuf, net.minecraft.world.phys.Vec3> STREAM_CODEC = new StreamCodec<ByteBuf, net.minecraft.world.phys.Vec3>() {
      public net.minecraft.world.phys.Vec3 decode(ByteBuf $$0) {
         return FriendlyByteBuf.readVec3($$0);
      }

      public void encode(ByteBuf $$0, net.minecraft.world.phys.Vec3 $$1) {
         FriendlyByteBuf.writeVec3($$0, $$1);
      }
   };
   public static final net.minecraft.world.phys.Vec3 ZERO = new net.minecraft.world.phys.Vec3(0.0, 0.0, 0.0);
   public static final net.minecraft.world.phys.Vec3 X_AXIS = new net.minecraft.world.phys.Vec3(1.0, 0.0, 0.0);
   public static final net.minecraft.world.phys.Vec3 Y_AXIS = new net.minecraft.world.phys.Vec3(0.0, 1.0, 0.0);
   public static final net.minecraft.world.phys.Vec3 Z_AXIS = new net.minecraft.world.phys.Vec3(0.0, 0.0, 1.0);
   public final double x;
   public final double y;
   public final double z;

   public static net.minecraft.world.phys.Vec3 atLowerCornerOf(Vec3i $$0) {
      return new net.minecraft.world.phys.Vec3($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public static net.minecraft.world.phys.Vec3 atLowerCornerWithOffset(Vec3i $$0, double $$1, double $$2, double $$3) {
      return new net.minecraft.world.phys.Vec3($$0.getX() + $$1, $$0.getY() + $$2, $$0.getZ() + $$3);
   }

   public static net.minecraft.world.phys.Vec3 atCenterOf(Vec3i $$0) {
      return atLowerCornerWithOffset($$0, 0.5, 0.5, 0.5);
   }

   public static net.minecraft.world.phys.Vec3 atBottomCenterOf(Vec3i $$0) {
      return atLowerCornerWithOffset($$0, 0.5, 0.0, 0.5);
   }

   public static net.minecraft.world.phys.Vec3 upFromBottomCenterOf(Vec3i $$0, double $$1) {
      return atLowerCornerWithOffset($$0, 0.5, $$1, 0.5);
   }

   public Vec3(double $$0, double $$1, double $$2) {
      this.x = $$0;
      this.y = $$1;
      this.z = $$2;
   }

   public Vec3(Vector3fc $$0) {
      this($$0.x(), $$0.y(), $$0.z());
   }

   public Vec3(Vec3i $$0) {
      this($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public net.minecraft.world.phys.Vec3 vectorTo(net.minecraft.world.phys.Vec3 $$0) {
      return new net.minecraft.world.phys.Vec3($$0.x - this.x, $$0.y - this.y, $$0.z - this.z);
   }

   public net.minecraft.world.phys.Vec3 normalize() {
      double $$0 = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
      return $$0 < 1.0E-5F ? ZERO : new net.minecraft.world.phys.Vec3(this.x / $$0, this.y / $$0, this.z / $$0);
   }

   public double dot(net.minecraft.world.phys.Vec3 $$0) {
      return this.x * $$0.x + this.y * $$0.y + this.z * $$0.z;
   }

   public net.minecraft.world.phys.Vec3 cross(net.minecraft.world.phys.Vec3 $$0) {
      return new net.minecraft.world.phys.Vec3(this.y * $$0.z - this.z * $$0.y, this.z * $$0.x - this.x * $$0.z, this.x * $$0.y - this.y * $$0.x);
   }

   public net.minecraft.world.phys.Vec3 subtract(net.minecraft.world.phys.Vec3 $$0) {
      return this.subtract($$0.x, $$0.y, $$0.z);
   }

   public net.minecraft.world.phys.Vec3 subtract(double $$0) {
      return this.subtract($$0, $$0, $$0);
   }

   public net.minecraft.world.phys.Vec3 subtract(double $$0, double $$1, double $$2) {
      return this.add(-$$0, -$$1, -$$2);
   }

   public net.minecraft.world.phys.Vec3 add(double $$0) {
      return this.add($$0, $$0, $$0);
   }

   public net.minecraft.world.phys.Vec3 add(net.minecraft.world.phys.Vec3 $$0) {
      return this.add($$0.x, $$0.y, $$0.z);
   }

   public net.minecraft.world.phys.Vec3 add(double $$0, double $$1, double $$2) {
      return new net.minecraft.world.phys.Vec3(this.x + $$0, this.y + $$1, this.z + $$2);
   }

   public boolean closerThan(Position $$0, double $$1) {
      return this.distanceToSqr($$0.x(), $$0.y(), $$0.z()) < $$1 * $$1;
   }

   public double distanceTo(net.minecraft.world.phys.Vec3 $$0) {
      double $$1 = $$0.x - this.x;
      double $$2 = $$0.y - this.y;
      double $$3 = $$0.z - this.z;
      return Math.sqrt($$1 * $$1 + $$2 * $$2 + $$3 * $$3);
   }

   public double distanceToSqr(net.minecraft.world.phys.Vec3 $$0) {
      double $$1 = $$0.x - this.x;
      double $$2 = $$0.y - this.y;
      double $$3 = $$0.z - this.z;
      return $$1 * $$1 + $$2 * $$2 + $$3 * $$3;
   }

   public double distanceToSqr(double $$0, double $$1, double $$2) {
      double $$3 = $$0 - this.x;
      double $$4 = $$1 - this.y;
      double $$5 = $$2 - this.z;
      return $$3 * $$3 + $$4 * $$4 + $$5 * $$5;
   }

   public boolean closerThan(net.minecraft.world.phys.Vec3 $$0, double $$1, double $$2) {
      double $$3 = $$0.x() - this.x;
      double $$4 = $$0.y() - this.y;
      double $$5 = $$0.z() - this.z;
      return Mth.lengthSquared($$3, $$5) < Mth.square($$1) && Math.abs($$4) < $$2;
   }

   public net.minecraft.world.phys.Vec3 scale(double $$0) {
      return this.multiply($$0, $$0, $$0);
   }

   public net.minecraft.world.phys.Vec3 reverse() {
      return this.scale(-1.0);
   }

   public net.minecraft.world.phys.Vec3 multiply(net.minecraft.world.phys.Vec3 $$0) {
      return this.multiply($$0.x, $$0.y, $$0.z);
   }

   public net.minecraft.world.phys.Vec3 multiply(double $$0, double $$1, double $$2) {
      return new net.minecraft.world.phys.Vec3(this.x * $$0, this.y * $$1, this.z * $$2);
   }

   public net.minecraft.world.phys.Vec3 horizontal() {
      return new net.minecraft.world.phys.Vec3(this.x, 0.0, this.z);
   }

   public net.minecraft.world.phys.Vec3 offsetRandom(RandomSource $$0, float $$1) {
      return this.add(($$0.nextFloat() - 0.5F) * $$1, ($$0.nextFloat() - 0.5F) * $$1, ($$0.nextFloat() - 0.5F) * $$1);
   }

   public net.minecraft.world.phys.Vec3 offsetRandomXZ(RandomSource $$0, float $$1) {
      return this.add(($$0.nextFloat() - 0.5F) * $$1, 0.0, ($$0.nextFloat() - 0.5F) * $$1);
   }

   public double length() {
      return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public double lengthSqr() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public double horizontalDistance() {
      return Math.sqrt(this.x * this.x + this.z * this.z);
   }

   public double horizontalDistanceSqr() {
      return this.x * this.x + this.z * this.z;
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else if (!($$0 instanceof net.minecraft.world.phys.Vec3 $$1)) {
         return false;
      } else if (Double.compare($$1.x, this.x) != 0) {
         return false;
      } else {
         return Double.compare($$1.y, this.y) != 0 ? false : Double.compare($$1.z, this.z) == 0;
      }
   }

   @Override
   public int hashCode() {
      long $$0 = Double.doubleToLongBits(this.x);
      int $$1 = (int)($$0 ^ $$0 >>> 32);
      $$0 = Double.doubleToLongBits(this.y);
      $$1 = 31 * $$1 + (int)($$0 ^ $$0 >>> 32);
      $$0 = Double.doubleToLongBits(this.z);
      return 31 * $$1 + (int)($$0 ^ $$0 >>> 32);
   }

   @Override
   public String toString() {
      return "(" + this.x + ", " + this.y + ", " + this.z + ")";
   }

   public net.minecraft.world.phys.Vec3 lerp(net.minecraft.world.phys.Vec3 $$0, double $$1) {
      return new net.minecraft.world.phys.Vec3(Mth.lerp($$1, this.x, $$0.x), Mth.lerp($$1, this.y, $$0.y), Mth.lerp($$1, this.z, $$0.z));
   }

   public net.minecraft.world.phys.Vec3 xRot(float $$0) {
      float $$1 = Mth.cos($$0);
      float $$2 = Mth.sin($$0);
      double $$3 = this.x;
      double $$4 = this.y * $$1 + this.z * $$2;
      double $$5 = this.z * $$1 - this.y * $$2;
      return new net.minecraft.world.phys.Vec3($$3, $$4, $$5);
   }

   public net.minecraft.world.phys.Vec3 yRot(float $$0) {
      float $$1 = Mth.cos($$0);
      float $$2 = Mth.sin($$0);
      double $$3 = this.x * $$1 + this.z * $$2;
      double $$4 = this.y;
      double $$5 = this.z * $$1 - this.x * $$2;
      return new net.minecraft.world.phys.Vec3($$3, $$4, $$5);
   }

   public net.minecraft.world.phys.Vec3 zRot(float $$0) {
      float $$1 = Mth.cos($$0);
      float $$2 = Mth.sin($$0);
      double $$3 = this.x * $$1 + this.y * $$2;
      double $$4 = this.y * $$1 - this.x * $$2;
      double $$5 = this.z;
      return new net.minecraft.world.phys.Vec3($$3, $$4, $$5);
   }

   public net.minecraft.world.phys.Vec3 rotateClockwise90() {
      return new net.minecraft.world.phys.Vec3(-this.z, this.y, this.x);
   }

   public static net.minecraft.world.phys.Vec3 directionFromRotation(net.minecraft.world.phys.Vec2 $$0) {
      return directionFromRotation($$0.x, $$0.y);
   }

   public static net.minecraft.world.phys.Vec3 directionFromRotation(float $$0, float $$1) {
      float $$2 = Mth.cos(-$$1 * (float) (Math.PI / 180.0) - (float) Math.PI);
      float $$3 = Mth.sin(-$$1 * (float) (Math.PI / 180.0) - (float) Math.PI);
      float $$4 = -Mth.cos(-$$0 * (float) (Math.PI / 180.0));
      float $$5 = Mth.sin(-$$0 * (float) (Math.PI / 180.0));
      return new net.minecraft.world.phys.Vec3($$3 * $$4, $$5, $$2 * $$4);
   }

   public net.minecraft.world.phys.Vec2 rotation() {
      float $$0 = (float)Math.atan2(-this.x, this.z) * (180.0F / (float)Math.PI);
      float $$1 = (float)Math.asin(-this.y / Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z)) * (180.0F / (float)Math.PI);
      return new net.minecraft.world.phys.Vec2($$1, $$0);
   }

   public net.minecraft.world.phys.Vec3 align(EnumSet<Axis> $$0) {
      double $$1 = $$0.contains(Axis.X) ? Mth.floor(this.x) : this.x;
      double $$2 = $$0.contains(Axis.Y) ? Mth.floor(this.y) : this.y;
      double $$3 = $$0.contains(Axis.Z) ? Mth.floor(this.z) : this.z;
      return new net.minecraft.world.phys.Vec3($$1, $$2, $$3);
   }

   public double get(Axis $$0) {
      return $$0.choose(this.x, this.y, this.z);
   }

   public net.minecraft.world.phys.Vec3 with(Axis $$0, double $$1) {
      double $$2 = $$0 == Axis.X ? $$1 : this.x;
      double $$3 = $$0 == Axis.Y ? $$1 : this.y;
      double $$4 = $$0 == Axis.Z ? $$1 : this.z;
      return new net.minecraft.world.phys.Vec3($$2, $$3, $$4);
   }

   public net.minecraft.world.phys.Vec3 relative(Direction $$0, double $$1) {
      Vec3i $$2 = $$0.getUnitVec3i();
      return new net.minecraft.world.phys.Vec3(this.x + $$1 * $$2.getX(), this.y + $$1 * $$2.getY(), this.z + $$1 * $$2.getZ());
   }

   public final double x() {
      return this.x;
   }

   public final double y() {
      return this.y;
   }

   public final double z() {
      return this.z;
   }

   public Vector3f toVector3f() {
      return new Vector3f((float)this.x, (float)this.y, (float)this.z);
   }

   public net.minecraft.world.phys.Vec3 projectedOn(net.minecraft.world.phys.Vec3 $$0) {
      return $$0.lengthSqr() == 0.0 ? $$0 : $$0.scale(this.dot($$0)).scale(1.0 / $$0.lengthSqr());
   }

   public static net.minecraft.world.phys.Vec3 applyLocalCoordinatesToRotation(net.minecraft.world.phys.Vec2 $$0, net.minecraft.world.phys.Vec3 $$1) {
      float $$2 = Mth.cos(($$0.y + 90.0F) * (float) (Math.PI / 180.0));
      float $$3 = Mth.sin(($$0.y + 90.0F) * (float) (Math.PI / 180.0));
      float $$4 = Mth.cos(-$$0.x * (float) (Math.PI / 180.0));
      float $$5 = Mth.sin(-$$0.x * (float) (Math.PI / 180.0));
      float $$6 = Mth.cos((-$$0.x + 90.0F) * (float) (Math.PI / 180.0));
      float $$7 = Mth.sin((-$$0.x + 90.0F) * (float) (Math.PI / 180.0));
      net.minecraft.world.phys.Vec3 $$8 = new net.minecraft.world.phys.Vec3($$2 * $$4, $$5, $$3 * $$4);
      net.minecraft.world.phys.Vec3 $$9 = new net.minecraft.world.phys.Vec3($$2 * $$6, $$7, $$3 * $$6);
      net.minecraft.world.phys.Vec3 $$10 = $$8.cross($$9).scale(-1.0);
      double $$11 = $$8.x * $$1.z + $$9.x * $$1.y + $$10.x * $$1.x;
      double $$12 = $$8.y * $$1.z + $$9.y * $$1.y + $$10.y * $$1.x;
      double $$13 = $$8.z * $$1.z + $$9.z * $$1.y + $$10.z * $$1.x;
      return new net.minecraft.world.phys.Vec3($$11, $$12, $$13);
   }

   public net.minecraft.world.phys.Vec3 addLocalCoordinates(net.minecraft.world.phys.Vec3 $$0) {
      return applyLocalCoordinatesToRotation(this.rotation(), $$0);
   }

   public boolean isFinite() {
      return Double.isFinite(this.x) && Double.isFinite(this.y) && Double.isFinite(this.z);
   }
}
