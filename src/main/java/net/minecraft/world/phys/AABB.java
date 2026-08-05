package net.minecraft.world.phys;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class AABB {
   private static final double EPSILON = 1.0E-7;
   public final double minX;
   public final double minY;
   public final double minZ;
   public final double maxX;
   public final double maxY;
   public final double maxZ;

   public AABB(double $$0, double $$1, double $$2, double $$3, double $$4, double $$5) {
      this.minX = Math.min($$0, $$3);
      this.minY = Math.min($$1, $$4);
      this.minZ = Math.min($$2, $$5);
      this.maxX = Math.max($$0, $$3);
      this.maxY = Math.max($$1, $$4);
      this.maxZ = Math.max($$2, $$5);
   }

   public AABB(BlockPos $$0) {
      this($$0.getX(), $$0.getY(), $$0.getZ(), $$0.getX() + 1, $$0.getY() + 1, $$0.getZ() + 1);
   }

   public AABB(net.minecraft.world.phys.Vec3 $$0, net.minecraft.world.phys.Vec3 $$1) {
      this($$0.x, $$0.y, $$0.z, $$1.x, $$1.y, $$1.z);
   }

   public static net.minecraft.world.phys.AABB of(BoundingBox $$0) {
      return new net.minecraft.world.phys.AABB($$0.minX(), $$0.minY(), $$0.minZ(), $$0.maxX() + 1, $$0.maxY() + 1, $$0.maxZ() + 1);
   }

   public static net.minecraft.world.phys.AABB unitCubeFromLowerCorner(net.minecraft.world.phys.Vec3 $$0) {
      return new net.minecraft.world.phys.AABB($$0.x, $$0.y, $$0.z, $$0.x + 1.0, $$0.y + 1.0, $$0.z + 1.0);
   }

   public static net.minecraft.world.phys.AABB encapsulatingFullBlocks(BlockPos $$0, BlockPos $$1) {
      return new net.minecraft.world.phys.AABB(
         Math.min($$0.getX(), $$1.getX()),
         Math.min($$0.getY(), $$1.getY()),
         Math.min($$0.getZ(), $$1.getZ()),
         Math.max($$0.getX(), $$1.getX()) + 1,
         Math.max($$0.getY(), $$1.getY()) + 1,
         Math.max($$0.getZ(), $$1.getZ()) + 1
      );
   }

   public net.minecraft.world.phys.AABB setMinX(double $$0) {
      return new net.minecraft.world.phys.AABB($$0, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }

   public net.minecraft.world.phys.AABB setMinY(double $$0) {
      return new net.minecraft.world.phys.AABB(this.minX, $$0, this.minZ, this.maxX, this.maxY, this.maxZ);
   }

   public net.minecraft.world.phys.AABB setMinZ(double $$0) {
      return new net.minecraft.world.phys.AABB(this.minX, this.minY, $$0, this.maxX, this.maxY, this.maxZ);
   }

   public net.minecraft.world.phys.AABB setMaxX(double $$0) {
      return new net.minecraft.world.phys.AABB(this.minX, this.minY, this.minZ, $$0, this.maxY, this.maxZ);
   }

   public net.minecraft.world.phys.AABB setMaxY(double $$0) {
      return new net.minecraft.world.phys.AABB(this.minX, this.minY, this.minZ, this.maxX, $$0, this.maxZ);
   }

   public net.minecraft.world.phys.AABB setMaxZ(double $$0) {
      return new net.minecraft.world.phys.AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, $$0);
   }

   public double min(Axis $$0) {
      return $$0.choose(this.minX, this.minY, this.minZ);
   }

   public double max(Axis $$0) {
      return $$0.choose(this.maxX, this.maxY, this.maxZ);
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else if (!($$0 instanceof net.minecraft.world.phys.AABB $$1)) {
         return false;
      } else if (Double.compare($$1.minX, this.minX) != 0) {
         return false;
      } else if (Double.compare($$1.minY, this.minY) != 0) {
         return false;
      } else if (Double.compare($$1.minZ, this.minZ) != 0) {
         return false;
      } else if (Double.compare($$1.maxX, this.maxX) != 0) {
         return false;
      } else {
         return Double.compare($$1.maxY, this.maxY) != 0 ? false : Double.compare($$1.maxZ, this.maxZ) == 0;
      }
   }

   @Override
   public int hashCode() {
      long $$0 = Double.doubleToLongBits(this.minX);
      int $$1 = (int)($$0 ^ $$0 >>> 32);
      $$0 = Double.doubleToLongBits(this.minY);
      $$1 = 31 * $$1 + (int)($$0 ^ $$0 >>> 32);
      $$0 = Double.doubleToLongBits(this.minZ);
      $$1 = 31 * $$1 + (int)($$0 ^ $$0 >>> 32);
      $$0 = Double.doubleToLongBits(this.maxX);
      $$1 = 31 * $$1 + (int)($$0 ^ $$0 >>> 32);
      $$0 = Double.doubleToLongBits(this.maxY);
      $$1 = 31 * $$1 + (int)($$0 ^ $$0 >>> 32);
      $$0 = Double.doubleToLongBits(this.maxZ);
      return 31 * $$1 + (int)($$0 ^ $$0 >>> 32);
   }

   public net.minecraft.world.phys.AABB contract(double $$0, double $$1, double $$2) {
      double $$3 = this.minX;
      double $$4 = this.minY;
      double $$5 = this.minZ;
      double $$6 = this.maxX;
      double $$7 = this.maxY;
      double $$8 = this.maxZ;
      if ($$0 < 0.0) {
         $$3 -= $$0;
      } else if ($$0 > 0.0) {
         $$6 -= $$0;
      }

      if ($$1 < 0.0) {
         $$4 -= $$1;
      } else if ($$1 > 0.0) {
         $$7 -= $$1;
      }

      if ($$2 < 0.0) {
         $$5 -= $$2;
      } else if ($$2 > 0.0) {
         $$8 -= $$2;
      }

      return new net.minecraft.world.phys.AABB($$3, $$4, $$5, $$6, $$7, $$8);
   }

   public net.minecraft.world.phys.AABB expandTowards(net.minecraft.world.phys.Vec3 $$0) {
      return this.expandTowards($$0.x, $$0.y, $$0.z);
   }

   public net.minecraft.world.phys.AABB expandTowards(double $$0, double $$1, double $$2) {
      double $$3 = this.minX;
      double $$4 = this.minY;
      double $$5 = this.minZ;
      double $$6 = this.maxX;
      double $$7 = this.maxY;
      double $$8 = this.maxZ;
      if ($$0 < 0.0) {
         $$3 += $$0;
      } else if ($$0 > 0.0) {
         $$6 += $$0;
      }

      if ($$1 < 0.0) {
         $$4 += $$1;
      } else if ($$1 > 0.0) {
         $$7 += $$1;
      }

      if ($$2 < 0.0) {
         $$5 += $$2;
      } else if ($$2 > 0.0) {
         $$8 += $$2;
      }

      return new net.minecraft.world.phys.AABB($$3, $$4, $$5, $$6, $$7, $$8);
   }

   public net.minecraft.world.phys.AABB inflate(double $$0, double $$1, double $$2) {
      double $$3 = this.minX - $$0;
      double $$4 = this.minY - $$1;
      double $$5 = this.minZ - $$2;
      double $$6 = this.maxX + $$0;
      double $$7 = this.maxY + $$1;
      double $$8 = this.maxZ + $$2;
      return new net.minecraft.world.phys.AABB($$3, $$4, $$5, $$6, $$7, $$8);
   }

   public net.minecraft.world.phys.AABB inflate(double $$0) {
      return this.inflate($$0, $$0, $$0);
   }

   public net.minecraft.world.phys.AABB intersect(net.minecraft.world.phys.AABB $$0) {
      double $$1 = Math.max(this.minX, $$0.minX);
      double $$2 = Math.max(this.minY, $$0.minY);
      double $$3 = Math.max(this.minZ, $$0.minZ);
      double $$4 = Math.min(this.maxX, $$0.maxX);
      double $$5 = Math.min(this.maxY, $$0.maxY);
      double $$6 = Math.min(this.maxZ, $$0.maxZ);
      return new net.minecraft.world.phys.AABB($$1, $$2, $$3, $$4, $$5, $$6);
   }

   public net.minecraft.world.phys.AABB minmax(net.minecraft.world.phys.AABB $$0) {
      double $$1 = Math.min(this.minX, $$0.minX);
      double $$2 = Math.min(this.minY, $$0.minY);
      double $$3 = Math.min(this.minZ, $$0.minZ);
      double $$4 = Math.max(this.maxX, $$0.maxX);
      double $$5 = Math.max(this.maxY, $$0.maxY);
      double $$6 = Math.max(this.maxZ, $$0.maxZ);
      return new net.minecraft.world.phys.AABB($$1, $$2, $$3, $$4, $$5, $$6);
   }

   public net.minecraft.world.phys.AABB move(double $$0, double $$1, double $$2) {
      return new net.minecraft.world.phys.AABB(this.minX + $$0, this.minY + $$1, this.minZ + $$2, this.maxX + $$0, this.maxY + $$1, this.maxZ + $$2);
   }

   public net.minecraft.world.phys.AABB move(BlockPos $$0) {
      return new net.minecraft.world.phys.AABB(
         this.minX + $$0.getX(), this.minY + $$0.getY(), this.minZ + $$0.getZ(), this.maxX + $$0.getX(), this.maxY + $$0.getY(), this.maxZ + $$0.getZ()
      );
   }

   public net.minecraft.world.phys.AABB move(net.minecraft.world.phys.Vec3 $$0) {
      return this.move($$0.x, $$0.y, $$0.z);
   }

   public net.minecraft.world.phys.AABB move(Vector3f $$0) {
      return this.move($$0.x, $$0.y, $$0.z);
   }

   public boolean intersects(net.minecraft.world.phys.AABB $$0) {
      return this.intersects($$0.minX, $$0.minY, $$0.minZ, $$0.maxX, $$0.maxY, $$0.maxZ);
   }

   public boolean intersects(double $$0, double $$1, double $$2, double $$3, double $$4, double $$5) {
      return this.minX < $$3 && this.maxX > $$0 && this.minY < $$4 && this.maxY > $$1 && this.minZ < $$5 && this.maxZ > $$2;
   }

   public boolean intersects(net.minecraft.world.phys.Vec3 $$0, net.minecraft.world.phys.Vec3 $$1) {
      return this.intersects(
         Math.min($$0.x, $$1.x), Math.min($$0.y, $$1.y), Math.min($$0.z, $$1.z), Math.max($$0.x, $$1.x), Math.max($$0.y, $$1.y), Math.max($$0.z, $$1.z)
      );
   }

   public boolean intersects(BlockPos $$0) {
      return this.intersects($$0.getX(), $$0.getY(), $$0.getZ(), $$0.getX() + 1, $$0.getY() + 1, $$0.getZ() + 1);
   }

   public boolean contains(net.minecraft.world.phys.Vec3 $$0) {
      return this.contains($$0.x, $$0.y, $$0.z);
   }

   public boolean contains(double $$0, double $$1, double $$2) {
      return $$0 >= this.minX && $$0 < this.maxX && $$1 >= this.minY && $$1 < this.maxY && $$2 >= this.minZ && $$2 < this.maxZ;
   }

   public double getSize() {
      double $$0 = this.getXsize();
      double $$1 = this.getYsize();
      double $$2 = this.getZsize();
      return ($$0 + $$1 + $$2) / 3.0;
   }

   public double getXsize() {
      return this.maxX - this.minX;
   }

   public double getYsize() {
      return this.maxY - this.minY;
   }

   public double getZsize() {
      return this.maxZ - this.minZ;
   }

   public net.minecraft.world.phys.AABB deflate(double $$0, double $$1, double $$2) {
      return this.inflate(-$$0, -$$1, -$$2);
   }

   public net.minecraft.world.phys.AABB deflate(double $$0) {
      return this.inflate(-$$0);
   }

   public Optional<net.minecraft.world.phys.Vec3> clip(net.minecraft.world.phys.Vec3 $$0, net.minecraft.world.phys.Vec3 $$1) {
      return clip(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, $$0, $$1);
   }

   public static Optional<net.minecraft.world.phys.Vec3> clip(
      double $$0, double $$1, double $$2, double $$3, double $$4, double $$5, net.minecraft.world.phys.Vec3 $$6, net.minecraft.world.phys.Vec3 $$7
   ) {
      double[] $$8 = new double[]{1.0};
      double $$9 = $$7.x - $$6.x;
      double $$10 = $$7.y - $$6.y;
      double $$11 = $$7.z - $$6.z;
      Direction $$12 = getDirection($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$8, null, $$9, $$10, $$11);
      if ($$12 == null) {
         return Optional.empty();
      } else {
         double $$13 = $$8[0];
         return Optional.of($$6.add($$13 * $$9, $$13 * $$10, $$13 * $$11));
      }
   }

   
   public static net.minecraft.world.phys.BlockHitResult clip(
      Iterable<net.minecraft.world.phys.AABB> $$0, net.minecraft.world.phys.Vec3 $$1, net.minecraft.world.phys.Vec3 $$2, BlockPos $$3
   ) {
      double[] $$4 = new double[]{1.0};
      Direction $$5 = null;
      double $$6 = $$2.x - $$1.x;
      double $$7 = $$2.y - $$1.y;
      double $$8 = $$2.z - $$1.z;

      for (net.minecraft.world.phys.AABB $$9 : $$0) {
         $$5 = getDirection($$9.move($$3), $$1, $$4, $$5, $$6, $$7, $$8);
      }

      if ($$5 == null) {
         return null;
      } else {
         double $$10 = $$4[0];
         return new net.minecraft.world.phys.BlockHitResult($$1.add($$10 * $$6, $$10 * $$7, $$10 * $$8), $$5, $$3, false);
      }
   }

   
   private static Direction getDirection(
      net.minecraft.world.phys.AABB $$0, net.minecraft.world.phys.Vec3 $$1, double[] $$2, Direction $$3, double $$4, double $$5, double $$6
   ) {
      return getDirection($$0.minX, $$0.minY, $$0.minZ, $$0.maxX, $$0.maxY, $$0.maxZ, $$1, $$2, $$3, $$4, $$5, $$6);
   }

   
   private static Direction getDirection(
      double $$0,
      double $$1,
      double $$2,
      double $$3,
      double $$4,
      double $$5,
      net.minecraft.world.phys.Vec3 $$6,
      double[] $$7,
      Direction $$8,
      double $$9,
      double $$10,
      double $$11
   ) {
      if ($$9 > 1.0E-7) {
         $$8 = clipPoint($$7, $$8, $$9, $$10, $$11, $$0, $$1, $$4, $$2, $$5, Direction.WEST, $$6.x, $$6.y, $$6.z);
      } else if ($$9 < -1.0E-7) {
         $$8 = clipPoint($$7, $$8, $$9, $$10, $$11, $$3, $$1, $$4, $$2, $$5, Direction.EAST, $$6.x, $$6.y, $$6.z);
      }

      if ($$10 > 1.0E-7) {
         $$8 = clipPoint($$7, $$8, $$10, $$11, $$9, $$1, $$2, $$5, $$0, $$3, Direction.DOWN, $$6.y, $$6.z, $$6.x);
      } else if ($$10 < -1.0E-7) {
         $$8 = clipPoint($$7, $$8, $$10, $$11, $$9, $$4, $$2, $$5, $$0, $$3, Direction.UP, $$6.y, $$6.z, $$6.x);
      }

      if ($$11 > 1.0E-7) {
         $$8 = clipPoint($$7, $$8, $$11, $$9, $$10, $$2, $$0, $$3, $$1, $$4, Direction.NORTH, $$6.z, $$6.x, $$6.y);
      } else if ($$11 < -1.0E-7) {
         $$8 = clipPoint($$7, $$8, $$11, $$9, $$10, $$5, $$0, $$3, $$1, $$4, Direction.SOUTH, $$6.z, $$6.x, $$6.y);
      }

      return $$8;
   }

   
   private static Direction clipPoint(
      double[] $$0,
      Direction $$1,
      double $$2,
      double $$3,
      double $$4,
      double $$5,
      double $$6,
      double $$7,
      double $$8,
      double $$9,
      Direction $$10,
      double $$11,
      double $$12,
      double $$13
   ) {
      double $$14 = ($$5 - $$11) / $$2;
      double $$15 = $$12 + $$14 * $$3;
      double $$16 = $$13 + $$14 * $$4;
      if (0.0 < $$14 && $$14 < $$0[0] && $$6 - 1.0E-7 < $$15 && $$15 < $$7 + 1.0E-7 && $$8 - 1.0E-7 < $$16 && $$16 < $$9 + 1.0E-7) {
         $$0[0] = $$14;
         return $$10;
      } else {
         return $$1;
      }
   }

   public boolean collidedAlongVector(net.minecraft.world.phys.Vec3 $$0, List<net.minecraft.world.phys.AABB> $$1) {
      net.minecraft.world.phys.Vec3 $$2 = this.getCenter();
      net.minecraft.world.phys.Vec3 $$3 = $$2.add($$0);

      for (net.minecraft.world.phys.AABB $$4 : $$1) {
         net.minecraft.world.phys.AABB $$5 = $$4.inflate(this.getXsize() * 0.5 - 1.0E-7, this.getYsize() * 0.5 - 1.0E-7, this.getZsize() * 0.5 - 1.0E-7);
         if ($$5.contains($$3) || $$5.contains($$2)) {
            return true;
         }

         if ($$5.clip($$2, $$3).isPresent()) {
            return true;
         }
      }

      return false;
   }

   public double distanceToSqr(net.minecraft.world.phys.Vec3 $$0) {
      double $$1 = Math.max(Math.max(this.minX - $$0.x, $$0.x - this.maxX), 0.0);
      double $$2 = Math.max(Math.max(this.minY - $$0.y, $$0.y - this.maxY), 0.0);
      double $$3 = Math.max(Math.max(this.minZ - $$0.z, $$0.z - this.maxZ), 0.0);
      return Mth.lengthSquared($$1, $$2, $$3);
   }

   public double distanceToSqr(net.minecraft.world.phys.AABB $$0) {
      double $$1 = Math.max(Math.max(this.minX - $$0.maxX, $$0.minX - this.maxX), 0.0);
      double $$2 = Math.max(Math.max(this.minY - $$0.maxY, $$0.minY - this.maxY), 0.0);
      double $$3 = Math.max(Math.max(this.minZ - $$0.maxZ, $$0.minZ - this.maxZ), 0.0);
      return Mth.lengthSquared($$1, $$2, $$3);
   }

   @Override
   public String toString() {
      return "AABB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
   }

   public boolean hasNaN() {
      return Double.isNaN(this.minX)
         || Double.isNaN(this.minY)
         || Double.isNaN(this.minZ)
         || Double.isNaN(this.maxX)
         || Double.isNaN(this.maxY)
         || Double.isNaN(this.maxZ);
   }

   public net.minecraft.world.phys.Vec3 getCenter() {
      return new net.minecraft.world.phys.Vec3(Mth.lerp(0.5, this.minX, this.maxX), Mth.lerp(0.5, this.minY, this.maxY), Mth.lerp(0.5, this.minZ, this.maxZ));
   }

   public net.minecraft.world.phys.Vec3 getBottomCenter() {
      return new net.minecraft.world.phys.Vec3(Mth.lerp(0.5, this.minX, this.maxX), this.minY, Mth.lerp(0.5, this.minZ, this.maxZ));
   }

   public net.minecraft.world.phys.Vec3 getMinPosition() {
      return new net.minecraft.world.phys.Vec3(this.minX, this.minY, this.minZ);
   }

   public net.minecraft.world.phys.Vec3 getMaxPosition() {
      return new net.minecraft.world.phys.Vec3(this.maxX, this.maxY, this.maxZ);
   }

   public static net.minecraft.world.phys.AABB ofSize(net.minecraft.world.phys.Vec3 $$0, double $$1, double $$2, double $$3) {
      return new net.minecraft.world.phys.AABB($$0.x - $$1 / 2.0, $$0.y - $$2 / 2.0, $$0.z - $$3 / 2.0, $$0.x + $$1 / 2.0, $$0.y + $$2 / 2.0, $$0.z + $$3 / 2.0);
   }

   public static class Builder {
      private float minX = Float.POSITIVE_INFINITY;
      private float minY = Float.POSITIVE_INFINITY;
      private float minZ = Float.POSITIVE_INFINITY;
      private float maxX = Float.NEGATIVE_INFINITY;
      private float maxY = Float.NEGATIVE_INFINITY;
      private float maxZ = Float.NEGATIVE_INFINITY;

      public void include(Vector3fc $$0) {
         this.minX = Math.min(this.minX, $$0.x());
         this.minY = Math.min(this.minY, $$0.y());
         this.minZ = Math.min(this.minZ, $$0.z());
         this.maxX = Math.max(this.maxX, $$0.x());
         this.maxY = Math.max(this.maxY, $$0.y());
         this.maxZ = Math.max(this.maxZ, $$0.z());
      }

      public net.minecraft.world.phys.AABB build() {
         return new net.minecraft.world.phys.AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
      }
   }
}
