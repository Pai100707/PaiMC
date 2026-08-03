package net.minecraft.core;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Vector3i;

@Immutable
public class Vec3i implements Comparable<net.minecraft.core.Vec3i> {
   public static final Codec<net.minecraft.core.Vec3i> CODEC = Codec.INT_STREAM
      .comapFlatMap(
         $$0 -> Util.fixedSize($$0, 3).map($$0x -> new net.minecraft.core.Vec3i($$0x[0], $$0x[1], $$0x[2])),
         $$0 -> IntStream.of($$0.getX(), $$0.getY(), $$0.getZ())
      );
   public static final StreamCodec<ByteBuf, net.minecraft.core.Vec3i> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      net.minecraft.core.Vec3i::getX,
      ByteBufCodecs.VAR_INT,
      net.minecraft.core.Vec3i::getY,
      ByteBufCodecs.VAR_INT,
      net.minecraft.core.Vec3i::getZ,
      net.minecraft.core.Vec3i::new
   );
   public static final net.minecraft.core.Vec3i ZERO = new net.minecraft.core.Vec3i(0, 0, 0);
   private int x;
   private int y;
   private int z;

   public static Codec<net.minecraft.core.Vec3i> offsetCodec(int $$0) {
      return CODEC.validate(
         $$1 -> Math.abs($$1.getX()) < $$0 && Math.abs($$1.getY()) < $$0 && Math.abs($$1.getZ()) < $$0
            ? DataResult.success($$1)
            : DataResult.error(() -> "Position out of range, expected at most " + $$0 + ": " + $$1)
      );
   }

   public Vec3i(int $$0, int $$1, int $$2) {
      this.x = $$0;
      this.y = $$1;
      this.z = $$2;
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return !($$0 instanceof net.minecraft.core.Vec3i $$1) ? false : this.getX() == $$1.getX() && this.getY() == $$1.getY() && this.getZ() == $$1.getZ();
      }
   }

   @Override
   public int hashCode() {
      return (this.getY() + this.getZ() * 31) * 31 + this.getX();
   }

   public int compareTo(net.minecraft.core.Vec3i $$0) {
      if (this.getY() == $$0.getY()) {
         return this.getZ() == $$0.getZ() ? this.getX() - $$0.getX() : this.getZ() - $$0.getZ();
      } else {
         return this.getY() - $$0.getY();
      }
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   protected net.minecraft.core.Vec3i setX(int $$0) {
      this.x = $$0;
      return this;
   }

   protected net.minecraft.core.Vec3i setY(int $$0) {
      this.y = $$0;
      return this;
   }

   protected net.minecraft.core.Vec3i setZ(int $$0) {
      this.z = $$0;
      return this;
   }

   public net.minecraft.core.Vec3i offset(int $$0, int $$1, int $$2) {
      return $$0 == 0 && $$1 == 0 && $$2 == 0 ? this : new net.minecraft.core.Vec3i(this.getX() + $$0, this.getY() + $$1, this.getZ() + $$2);
   }

   public net.minecraft.core.Vec3i offset(net.minecraft.core.Vec3i $$0) {
      return this.offset($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public net.minecraft.core.Vec3i subtract(net.minecraft.core.Vec3i $$0) {
      return this.offset(-$$0.getX(), -$$0.getY(), -$$0.getZ());
   }

   public net.minecraft.core.Vec3i multiply(int $$0) {
      if ($$0 == 1) {
         return this;
      } else {
         return $$0 == 0 ? ZERO : new net.minecraft.core.Vec3i(this.getX() * $$0, this.getY() * $$0, this.getZ() * $$0);
      }
   }

   public net.minecraft.core.Vec3i multiply(int $$0, int $$1, int $$2) {
      return new net.minecraft.core.Vec3i(this.getX() * $$0, this.getY() * $$1, this.getZ() * $$2);
   }

   public net.minecraft.core.Vec3i above() {
      return this.above(1);
   }

   public net.minecraft.core.Vec3i above(int $$0) {
      return this.relative(net.minecraft.core.Direction.UP, $$0);
   }

   public net.minecraft.core.Vec3i below() {
      return this.below(1);
   }

   public net.minecraft.core.Vec3i below(int $$0) {
      return this.relative(net.minecraft.core.Direction.DOWN, $$0);
   }

   public net.minecraft.core.Vec3i north() {
      return this.north(1);
   }

   public net.minecraft.core.Vec3i north(int $$0) {
      return this.relative(net.minecraft.core.Direction.NORTH, $$0);
   }

   public net.minecraft.core.Vec3i south() {
      return this.south(1);
   }

   public net.minecraft.core.Vec3i south(int $$0) {
      return this.relative(net.minecraft.core.Direction.SOUTH, $$0);
   }

   public net.minecraft.core.Vec3i west() {
      return this.west(1);
   }

   public net.minecraft.core.Vec3i west(int $$0) {
      return this.relative(net.minecraft.core.Direction.WEST, $$0);
   }

   public net.minecraft.core.Vec3i east() {
      return this.east(1);
   }

   public net.minecraft.core.Vec3i east(int $$0) {
      return this.relative(net.minecraft.core.Direction.EAST, $$0);
   }

   public net.minecraft.core.Vec3i relative(net.minecraft.core.Direction $$0) {
      return this.relative($$0, 1);
   }

   public net.minecraft.core.Vec3i relative(net.minecraft.core.Direction $$0, int $$1) {
      return $$1 == 0
         ? this
         : new net.minecraft.core.Vec3i(this.getX() + $$0.getStepX() * $$1, this.getY() + $$0.getStepY() * $$1, this.getZ() + $$0.getStepZ() * $$1);
   }

   public net.minecraft.core.Vec3i relative(net.minecraft.core.Direction.Axis $$0, int $$1) {
      if ($$1 == 0) {
         return this;
      } else {
         int $$2 = $$0 == net.minecraft.core.Direction.Axis.X ? $$1 : 0;
         int $$3 = $$0 == net.minecraft.core.Direction.Axis.Y ? $$1 : 0;
         int $$4 = $$0 == net.minecraft.core.Direction.Axis.Z ? $$1 : 0;
         return new net.minecraft.core.Vec3i(this.getX() + $$2, this.getY() + $$3, this.getZ() + $$4);
      }
   }

   public net.minecraft.core.Vec3i cross(net.minecraft.core.Vec3i $$0) {
      return new net.minecraft.core.Vec3i(
         this.getY() * $$0.getZ() - this.getZ() * $$0.getY(),
         this.getZ() * $$0.getX() - this.getX() * $$0.getZ(),
         this.getX() * $$0.getY() - this.getY() * $$0.getX()
      );
   }

   public boolean closerThan(net.minecraft.core.Vec3i $$0, double $$1) {
      return this.distSqr($$0) < Mth.square($$1);
   }

   public boolean closerToCenterThan(net.minecraft.core.Position $$0, double $$1) {
      return this.distToCenterSqr($$0) < Mth.square($$1);
   }

   public double distSqr(net.minecraft.core.Vec3i $$0) {
      return this.distToLowCornerSqr($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public double distToCenterSqr(net.minecraft.core.Position $$0) {
      return this.distToCenterSqr($$0.x(), $$0.y(), $$0.z());
   }

   public double distToCenterSqr(double $$0, double $$1, double $$2) {
      double $$3 = this.getX() + 0.5 - $$0;
      double $$4 = this.getY() + 0.5 - $$1;
      double $$5 = this.getZ() + 0.5 - $$2;
      return $$3 * $$3 + $$4 * $$4 + $$5 * $$5;
   }

   public double distToLowCornerSqr(double $$0, double $$1, double $$2) {
      double $$3 = this.getX() - $$0;
      double $$4 = this.getY() - $$1;
      double $$5 = this.getZ() - $$2;
      return $$3 * $$3 + $$4 * $$4 + $$5 * $$5;
   }

   public int distManhattan(net.minecraft.core.Vec3i $$0) {
      float $$1 = Math.abs($$0.getX() - this.getX());
      float $$2 = Math.abs($$0.getY() - this.getY());
      float $$3 = Math.abs($$0.getZ() - this.getZ());
      return (int)($$1 + $$2 + $$3);
   }

   public int distChessboard(net.minecraft.core.Vec3i $$0) {
      int $$1 = Math.abs(this.getX() - $$0.getX());
      int $$2 = Math.abs(this.getY() - $$0.getY());
      int $$3 = Math.abs(this.getZ() - $$0.getZ());
      return Math.max(Math.max($$1, $$2), $$3);
   }

   public int get(net.minecraft.core.Direction.Axis $$0) {
      return $$0.choose(this.x, this.y, this.z);
   }

   public Vector3i toMutable() {
      return new Vector3i(this.x, this.y, this.z);
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
   }

   public String toShortString() {
      return this.getX() + ", " + this.getY() + ", " + this.getZ();
   }
}
