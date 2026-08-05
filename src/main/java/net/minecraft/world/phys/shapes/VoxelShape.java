package net.minecraft.world.phys.shapes;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.apache.commons.lang3.mutable.MutableObject;

public abstract class VoxelShape {
   protected final DiscreteVoxelShape shape;
   
   private VoxelShape[] faces;

   protected VoxelShape(DiscreteVoxelShape $$0) {
      this.shape = $$0;
   }

   public double min(Axis $$0) {
      int $$1 = this.shape.firstFull($$0);
      return $$1 >= this.shape.getSize($$0) ? Double.POSITIVE_INFINITY : this.get($$0, $$1);
   }

   public double max(Axis $$0) {
      int $$1 = this.shape.lastFull($$0);
      return $$1 <= 0 ? Double.NEGATIVE_INFINITY : this.get($$0, $$1);
   }

   public net.minecraft.world.phys.AABB bounds() {
      if (this.isEmpty()) {
         throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("No bounds for empty shape."));
      } else {
         return new net.minecraft.world.phys.AABB(this.min(Axis.X), this.min(Axis.Y), this.min(Axis.Z), this.max(Axis.X), this.max(Axis.Y), this.max(Axis.Z));
      }
   }

   public VoxelShape singleEncompassing() {
      return this.isEmpty()
         ? Shapes.empty()
         : Shapes.box(this.min(Axis.X), this.min(Axis.Y), this.min(Axis.Z), this.max(Axis.X), this.max(Axis.Y), this.max(Axis.Z));
   }

   protected double get(Axis $$0, int $$1) {
      return this.getCoords($$0).getDouble($$1);
   }

   public abstract DoubleList getCoords(Axis var1);

   public boolean isEmpty() {
      return this.shape.isEmpty();
   }

   public VoxelShape move(net.minecraft.world.phys.Vec3 $$0) {
      return this.move($$0.x, $$0.y, $$0.z);
   }

   public VoxelShape move(Vec3i $$0) {
      return this.move($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public VoxelShape move(double $$0, double $$1, double $$2) {
      return (VoxelShape)(this.isEmpty()
         ? Shapes.empty()
         : new ArrayVoxelShape(
            this.shape,
            new OffsetDoubleList(this.getCoords(Axis.X), $$0),
            new OffsetDoubleList(this.getCoords(Axis.Y), $$1),
            new OffsetDoubleList(this.getCoords(Axis.Z), $$2)
         ));
   }

   public VoxelShape optimize() {
      VoxelShape[] $$0 = new VoxelShape[]{Shapes.empty()};
      this.forAllBoxes(($$1, $$2, $$3, $$4, $$5, $$6) -> $$0[0] = Shapes.joinUnoptimized($$0[0], Shapes.box($$1, $$2, $$3, $$4, $$5, $$6), BooleanOp.OR));
      return $$0[0];
   }

   public void forAllEdges(Shapes.DoubleLineConsumer $$0) {
      this.shape
         .forAllEdges(
            ($$1, $$2, $$3, $$4, $$5, $$6) -> $$0.consume(
               this.get(Axis.X, $$1), this.get(Axis.Y, $$2), this.get(Axis.Z, $$3), this.get(Axis.X, $$4), this.get(Axis.Y, $$5), this.get(Axis.Z, $$6)
            ),
            true
         );
   }

   public void forAllBoxes(Shapes.DoubleLineConsumer $$0) {
      DoubleList $$1 = this.getCoords(Axis.X);
      DoubleList $$2 = this.getCoords(Axis.Y);
      DoubleList $$3 = this.getCoords(Axis.Z);
      this.shape
         .forAllBoxes(
            ($$4, $$5, $$6, $$7, $$8, $$9) -> $$0.consume(
               $$1.getDouble($$4), $$2.getDouble($$5), $$3.getDouble($$6), $$1.getDouble($$7), $$2.getDouble($$8), $$3.getDouble($$9)
            ),
            true
         );
   }

   public List<net.minecraft.world.phys.AABB> toAabbs() {
      List<net.minecraft.world.phys.AABB> $$0 = Lists.newArrayList();
      this.forAllBoxes(($$1, $$2, $$3, $$4, $$5, $$6) -> $$0.add(new net.minecraft.world.phys.AABB($$1, $$2, $$3, $$4, $$5, $$6)));
      return $$0;
   }

   public double min(Axis $$0, double $$1, double $$2) {
      Axis $$3 = AxisCycle.FORWARD.cycle($$0);
      Axis $$4 = AxisCycle.BACKWARD.cycle($$0);
      int $$5 = this.findIndex($$3, $$1);
      int $$6 = this.findIndex($$4, $$2);
      int $$7 = this.shape.firstFull($$0, $$5, $$6);
      return $$7 >= this.shape.getSize($$0) ? Double.POSITIVE_INFINITY : this.get($$0, $$7);
   }

   public double max(Axis $$0, double $$1, double $$2) {
      Axis $$3 = AxisCycle.FORWARD.cycle($$0);
      Axis $$4 = AxisCycle.BACKWARD.cycle($$0);
      int $$5 = this.findIndex($$3, $$1);
      int $$6 = this.findIndex($$4, $$2);
      int $$7 = this.shape.lastFull($$0, $$5, $$6);
      return $$7 <= 0 ? Double.NEGATIVE_INFINITY : this.get($$0, $$7);
   }

   protected int findIndex(Axis $$0, double $$1) {
      return Mth.binarySearch(0, this.shape.getSize($$0) + 1, $$2 -> $$1 < this.get($$0, $$2)) - 1;
   }

   
   public net.minecraft.world.phys.BlockHitResult clip(net.minecraft.world.phys.Vec3 $$0, net.minecraft.world.phys.Vec3 $$1, BlockPos $$2) {
      if (this.isEmpty()) {
         return null;
      } else {
         net.minecraft.world.phys.Vec3 $$3 = $$1.subtract($$0);
         if ($$3.lengthSqr() < 1.0E-7) {
            return null;
         } else {
            net.minecraft.world.phys.Vec3 $$4 = $$0.add($$3.scale(0.001));
            return this.shape
                  .isFullWide(
                     this.findIndex(Axis.X, $$4.x - $$2.getX()), this.findIndex(Axis.Y, $$4.y - $$2.getY()), this.findIndex(Axis.Z, $$4.z - $$2.getZ())
                  )
               ? new net.minecraft.world.phys.BlockHitResult($$4, Direction.getApproximateNearest($$3.x, $$3.y, $$3.z).getOpposite(), $$2, true)
               : net.minecraft.world.phys.AABB.clip(this.toAabbs(), $$0, $$1, $$2);
         }
      }
   }

   public Optional<net.minecraft.world.phys.Vec3> closestPointTo(net.minecraft.world.phys.Vec3 $$0) {
      if (this.isEmpty()) {
         return Optional.empty();
      } else {
         MutableObject<net.minecraft.world.phys.Vec3> $$1 = new MutableObject();
         this.forAllBoxes(($$2, $$3, $$4, $$5, $$6, $$7) -> {
            double $$8 = Mth.clamp($$0.x(), $$2, $$5);
            double $$9 = Mth.clamp($$0.y(), $$3, $$6);
            double $$10 = Mth.clamp($$0.z(), $$4, $$7);
            net.minecraft.world.phys.Vec3 $$11 = (net.minecraft.world.phys.Vec3)$$1.get();
            if ($$11 == null || $$0.distanceToSqr($$8, $$9, $$10) < $$0.distanceToSqr($$11)) {
               $$1.setValue(new net.minecraft.world.phys.Vec3($$8, $$9, $$10));
            }
         });
         return Optional.of(Objects.requireNonNull((net.minecraft.world.phys.Vec3)$$1.get()));
      }
   }

   public VoxelShape getFaceShape(Direction $$0) {
      if (!this.isEmpty() && this != Shapes.block()) {
         if (this.faces != null) {
            VoxelShape $$1 = this.faces[$$0.ordinal()];
            if ($$1 != null) {
               return $$1;
            }
         } else {
            this.faces = new VoxelShape[6];
         }

         VoxelShape $$2 = this.calculateFace($$0);
         this.faces[$$0.ordinal()] = $$2;
         return $$2;
      } else {
         return this;
      }
   }

   private VoxelShape calculateFace(Direction $$0) {
      Axis $$1 = $$0.getAxis();
      if (this.isCubeLikeAlong($$1)) {
         return this;
      } else {
         AxisDirection $$2 = $$0.getAxisDirection();
         int $$3 = this.findIndex($$1, $$2 == AxisDirection.POSITIVE ? 0.9999999 : 1.0E-7);
         SliceShape $$4 = new SliceShape(this, $$1, $$3);
         if ($$4.isEmpty()) {
            return Shapes.empty();
         } else {
            return (VoxelShape)($$4.isCubeLike() ? Shapes.block() : $$4);
         }
      }
   }

   protected boolean isCubeLike() {
      for (Axis $$0 : Axis.VALUES) {
         if (!this.isCubeLikeAlong($$0)) {
            return false;
         }
      }

      return true;
   }

   private boolean isCubeLikeAlong(Axis $$0) {
      DoubleList $$1 = this.getCoords($$0);
      return $$1.size() == 2 && DoubleMath.fuzzyEquals($$1.getDouble(0), 0.0, 1.0E-7) && DoubleMath.fuzzyEquals($$1.getDouble(1), 1.0, 1.0E-7);
   }

   public double collide(Axis $$0, net.minecraft.world.phys.AABB $$1, double $$2) {
      return this.collideX(AxisCycle.between($$0, Axis.X), $$1, $$2);
   }

   protected double collideX(AxisCycle $$0, net.minecraft.world.phys.AABB $$1, double $$2) {
      if (this.isEmpty()) {
         return $$2;
      } else if (Math.abs($$2) < 1.0E-7) {
         return 0.0;
      } else {
         AxisCycle $$3 = $$0.inverse();
         Axis $$4 = $$3.cycle(Axis.X);
         Axis $$5 = $$3.cycle(Axis.Y);
         Axis $$6 = $$3.cycle(Axis.Z);
         double $$7 = $$1.max($$4);
         double $$8 = $$1.min($$4);
         int $$9 = this.findIndex($$4, $$8 + 1.0E-7);
         int $$10 = this.findIndex($$4, $$7 - 1.0E-7);
         int $$11 = Math.max(0, this.findIndex($$5, $$1.min($$5) + 1.0E-7));
         int $$12 = Math.min(this.shape.getSize($$5), this.findIndex($$5, $$1.max($$5) - 1.0E-7) + 1);
         int $$13 = Math.max(0, this.findIndex($$6, $$1.min($$6) + 1.0E-7));
         int $$14 = Math.min(this.shape.getSize($$6), this.findIndex($$6, $$1.max($$6) - 1.0E-7) + 1);
         int $$15 = this.shape.getSize($$4);
         if ($$2 > 0.0) {
            for (int $$16 = $$10 + 1; $$16 < $$15; $$16++) {
               for (int $$17 = $$11; $$17 < $$12; $$17++) {
                  for (int $$18 = $$13; $$18 < $$14; $$18++) {
                     if (this.shape.isFullWide($$3, $$16, $$17, $$18)) {
                        double $$19 = this.get($$4, $$16) - $$7;
                        if ($$19 >= -1.0E-7) {
                           $$2 = Math.min($$2, $$19);
                        }

                        return $$2;
                     }
                  }
               }
            }
         } else if ($$2 < 0.0) {
            for (int $$20 = $$9 - 1; $$20 >= 0; $$20--) {
               for (int $$21 = $$11; $$21 < $$12; $$21++) {
                  for (int $$22 = $$13; $$22 < $$14; $$22++) {
                     if (this.shape.isFullWide($$3, $$20, $$21, $$22)) {
                        double $$23 = this.get($$4, $$20 + 1) - $$8;
                        if ($$23 <= 1.0E-7) {
                           $$2 = Math.max($$2, $$23);
                        }

                        return $$2;
                     }
                  }
               }
            }
         }

         return $$2;
      }
   }

   @Override
   public boolean equals(Object $$0) {
      return super.equals($$0);
   }

   @Override
   public String toString() {
      return this.isEmpty() ? "EMPTY" : "VoxelShape[" + this.bounds() + "]";
   }
}
