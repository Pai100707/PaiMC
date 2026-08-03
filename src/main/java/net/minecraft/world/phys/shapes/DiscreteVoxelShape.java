package net.minecraft.world.phys.shapes;

import com.mojang.math.OctahedralGroup;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import org.joml.Vector3i;

public abstract class DiscreteVoxelShape {
   private static final Axis[] AXIS_VALUES = Axis.values();
   protected final int xSize;
   protected final int ySize;
   protected final int zSize;

   protected DiscreteVoxelShape(int $$0, int $$1, int $$2) {
      if ($$0 >= 0 && $$1 >= 0 && $$2 >= 0) {
         this.xSize = $$0;
         this.ySize = $$1;
         this.zSize = $$2;
      } else {
         throw new IllegalArgumentException("Need all positive sizes: x: " + $$0 + ", y: " + $$1 + ", z: " + $$2);
      }
   }

   public DiscreteVoxelShape rotate(OctahedralGroup $$0) {
      if ($$0 == OctahedralGroup.IDENTITY) {
         return this;
      } else {
         Vector3i $$1 = $$0.rotate(new Vector3i(this.xSize, this.ySize, this.zSize));
         int $$2 = fixupCoordinate($$1, 0);
         int $$3 = fixupCoordinate($$1, 1);
         int $$4 = fixupCoordinate($$1, 2);
         DiscreteVoxelShape $$5 = new BitSetDiscreteVoxelShape($$1.x, $$1.y, $$1.z);

         for (int $$6 = 0; $$6 < this.xSize; $$6++) {
            for (int $$7 = 0; $$7 < this.ySize; $$7++) {
               for (int $$8 = 0; $$8 < this.zSize; $$8++) {
                  if (this.isFull($$6, $$7, $$8)) {
                     Vector3i $$9 = $$0.rotate($$1.set($$6, $$7, $$8));
                     int $$10 = $$2 + $$9.x;
                     int $$11 = $$3 + $$9.y;
                     int $$12 = $$4 + $$9.z;
                     $$5.fill($$10, $$11, $$12);
                  }
               }
            }
         }

         return $$5;
      }
   }

   private static int fixupCoordinate(Vector3i $$0, int $$1) {
      int $$2 = $$0.get($$1);
      if ($$2 < 0) {
         $$0.setComponent($$1, -$$2);
         return -$$2 - 1;
      } else {
         return 0;
      }
   }

   public boolean isFullWide(AxisCycle $$0, int $$1, int $$2, int $$3) {
      return this.isFullWide($$0.cycle($$1, $$2, $$3, Axis.X), $$0.cycle($$1, $$2, $$3, Axis.Y), $$0.cycle($$1, $$2, $$3, Axis.Z));
   }

   public boolean isFullWide(int $$0, int $$1, int $$2) {
      if ($$0 < 0 || $$1 < 0 || $$2 < 0) {
         return false;
      } else {
         return $$0 < this.xSize && $$1 < this.ySize && $$2 < this.zSize ? this.isFull($$0, $$1, $$2) : false;
      }
   }

   public boolean isFull(AxisCycle $$0, int $$1, int $$2, int $$3) {
      return this.isFull($$0.cycle($$1, $$2, $$3, Axis.X), $$0.cycle($$1, $$2, $$3, Axis.Y), $$0.cycle($$1, $$2, $$3, Axis.Z));
   }

   public abstract boolean isFull(int var1, int var2, int var3);

   public abstract void fill(int var1, int var2, int var3);

   public boolean isEmpty() {
      for (Axis $$0 : AXIS_VALUES) {
         if (this.firstFull($$0) >= this.lastFull($$0)) {
            return true;
         }
      }

      return false;
   }

   public abstract int firstFull(Axis var1);

   public abstract int lastFull(Axis var1);

   public int firstFull(Axis $$0, int $$1, int $$2) {
      int $$3 = this.getSize($$0);
      if ($$1 >= 0 && $$2 >= 0) {
         Axis $$4 = AxisCycle.FORWARD.cycle($$0);
         Axis $$5 = AxisCycle.BACKWARD.cycle($$0);
         if ($$1 < this.getSize($$4) && $$2 < this.getSize($$5)) {
            AxisCycle $$6 = AxisCycle.between(Axis.X, $$0);

            for (int $$7 = 0; $$7 < $$3; $$7++) {
               if (this.isFull($$6, $$7, $$1, $$2)) {
                  return $$7;
               }
            }

            return $$3;
         } else {
            return $$3;
         }
      } else {
         return $$3;
      }
   }

   public int lastFull(Axis $$0, int $$1, int $$2) {
      if ($$1 >= 0 && $$2 >= 0) {
         Axis $$3 = AxisCycle.FORWARD.cycle($$0);
         Axis $$4 = AxisCycle.BACKWARD.cycle($$0);
         if ($$1 < this.getSize($$3) && $$2 < this.getSize($$4)) {
            int $$5 = this.getSize($$0);
            AxisCycle $$6 = AxisCycle.between(Axis.X, $$0);

            for (int $$7 = $$5 - 1; $$7 >= 0; $$7--) {
               if (this.isFull($$6, $$7, $$1, $$2)) {
                  return $$7 + 1;
               }
            }

            return 0;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public int getSize(Axis $$0) {
      return $$0.choose(this.xSize, this.ySize, this.zSize);
   }

   public int getXSize() {
      return this.getSize(Axis.X);
   }

   public int getYSize() {
      return this.getSize(Axis.Y);
   }

   public int getZSize() {
      return this.getSize(Axis.Z);
   }

   public void forAllEdges(DiscreteVoxelShape.IntLineConsumer $$0, boolean $$1) {
      this.forAllAxisEdges($$0, AxisCycle.NONE, $$1);
      this.forAllAxisEdges($$0, AxisCycle.FORWARD, $$1);
      this.forAllAxisEdges($$0, AxisCycle.BACKWARD, $$1);
   }

   private void forAllAxisEdges(DiscreteVoxelShape.IntLineConsumer $$0, AxisCycle $$1, boolean $$2) {
      AxisCycle $$3 = $$1.inverse();
      int $$4 = this.getSize($$3.cycle(Axis.X));
      int $$5 = this.getSize($$3.cycle(Axis.Y));
      int $$6 = this.getSize($$3.cycle(Axis.Z));

      for (int $$7 = 0; $$7 <= $$4; $$7++) {
         for (int $$8 = 0; $$8 <= $$5; $$8++) {
            int $$9 = -1;

            for (int $$10 = 0; $$10 <= $$6; $$10++) {
               int $$11 = 0;
               int $$12 = 0;

               for (int $$13 = 0; $$13 <= 1; $$13++) {
                  for (int $$14 = 0; $$14 <= 1; $$14++) {
                     if (this.isFullWide($$3, $$7 + $$13 - 1, $$8 + $$14 - 1, $$10)) {
                        $$11++;
                        $$12 ^= $$13 ^ $$14;
                     }
                  }
               }

               if ($$11 == 1 || $$11 == 3 || $$11 == 2 && ($$12 & 1) == 0) {
                  if ($$2) {
                     if ($$9 == -1) {
                        $$9 = $$10;
                     }
                  } else {
                     $$0.consume(
                        $$3.cycle($$7, $$8, $$10, Axis.X),
                        $$3.cycle($$7, $$8, $$10, Axis.Y),
                        $$3.cycle($$7, $$8, $$10, Axis.Z),
                        $$3.cycle($$7, $$8, $$10 + 1, Axis.X),
                        $$3.cycle($$7, $$8, $$10 + 1, Axis.Y),
                        $$3.cycle($$7, $$8, $$10 + 1, Axis.Z)
                     );
                  }
               } else if ($$9 != -1) {
                  $$0.consume(
                     $$3.cycle($$7, $$8, $$9, Axis.X),
                     $$3.cycle($$7, $$8, $$9, Axis.Y),
                     $$3.cycle($$7, $$8, $$9, Axis.Z),
                     $$3.cycle($$7, $$8, $$10, Axis.X),
                     $$3.cycle($$7, $$8, $$10, Axis.Y),
                     $$3.cycle($$7, $$8, $$10, Axis.Z)
                  );
                  $$9 = -1;
               }
            }
         }
      }
   }

   public void forAllBoxes(DiscreteVoxelShape.IntLineConsumer $$0, boolean $$1) {
      BitSetDiscreteVoxelShape.forAllBoxes(this, $$0, $$1);
   }

   public void forAllFaces(DiscreteVoxelShape.IntFaceConsumer $$0) {
      this.forAllAxisFaces($$0, AxisCycle.NONE);
      this.forAllAxisFaces($$0, AxisCycle.FORWARD);
      this.forAllAxisFaces($$0, AxisCycle.BACKWARD);
   }

   private void forAllAxisFaces(DiscreteVoxelShape.IntFaceConsumer $$0, AxisCycle $$1) {
      AxisCycle $$2 = $$1.inverse();
      Axis $$3 = $$2.cycle(Axis.Z);
      int $$4 = this.getSize($$2.cycle(Axis.X));
      int $$5 = this.getSize($$2.cycle(Axis.Y));
      int $$6 = this.getSize($$3);
      Direction $$7 = Direction.fromAxisAndDirection($$3, AxisDirection.NEGATIVE);
      Direction $$8 = Direction.fromAxisAndDirection($$3, AxisDirection.POSITIVE);

      for (int $$9 = 0; $$9 < $$4; $$9++) {
         for (int $$10 = 0; $$10 < $$5; $$10++) {
            boolean $$11 = false;

            for (int $$12 = 0; $$12 <= $$6; $$12++) {
               boolean $$13 = $$12 != $$6 && this.isFull($$2, $$9, $$10, $$12);
               if (!$$11 && $$13) {
                  $$0.consume($$7, $$2.cycle($$9, $$10, $$12, Axis.X), $$2.cycle($$9, $$10, $$12, Axis.Y), $$2.cycle($$9, $$10, $$12, Axis.Z));
               }

               if ($$11 && !$$13) {
                  $$0.consume($$8, $$2.cycle($$9, $$10, $$12 - 1, Axis.X), $$2.cycle($$9, $$10, $$12 - 1, Axis.Y), $$2.cycle($$9, $$10, $$12 - 1, Axis.Z));
               }

               $$11 = $$13;
            }
         }
      }
   }

   public interface IntFaceConsumer {
      void consume(Direction var1, int var2, int var3, int var4);
   }

   public interface IntLineConsumer {
      void consume(int var1, int var2, int var3, int var4, int var5, int var6);
   }
}
