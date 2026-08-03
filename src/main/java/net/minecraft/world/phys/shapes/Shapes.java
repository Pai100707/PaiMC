package net.minecraft.world.phys.shapes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import com.mojang.math.OctahedralGroup;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.properties.AttachFace;

public final class Shapes {
   public static final double EPSILON = 1.0E-7;
   public static final double BIG_EPSILON = 1.0E-6;
   private static final VoxelShape BLOCK = (VoxelShape)Util.make(() -> {
      DiscreteVoxelShape $$0 = new BitSetDiscreteVoxelShape(1, 1, 1);
      $$0.fill(0, 0, 0);
      return new CubeVoxelShape($$0);
   });
   private static final net.minecraft.world.phys.Vec3 BLOCK_CENTER = new net.minecraft.world.phys.Vec3(0.5, 0.5, 0.5);
   public static final VoxelShape INFINITY = box(
      Double.NEGATIVE_INFINITY,
      Double.NEGATIVE_INFINITY,
      Double.NEGATIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
   );
   private static final VoxelShape EMPTY = new ArrayVoxelShape(
      new BitSetDiscreteVoxelShape(0, 0, 0),
      new DoubleArrayList(new double[]{0.0}),
      new DoubleArrayList(new double[]{0.0}),
      new DoubleArrayList(new double[]{0.0})
   );

   public static VoxelShape empty() {
      return EMPTY;
   }

   public static VoxelShape block() {
      return BLOCK;
   }

   public static VoxelShape box(double $$0, double $$1, double $$2, double $$3, double $$4, double $$5) {
      if (!($$0 > $$3) && !($$1 > $$4) && !($$2 > $$5)) {
         return create($$0, $$1, $$2, $$3, $$4, $$5);
      } else {
         throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
      }
   }

   public static VoxelShape create(double $$0, double $$1, double $$2, double $$3, double $$4, double $$5) {
      if (!($$3 - $$0 < 1.0E-7) && !($$4 - $$1 < 1.0E-7) && !($$5 - $$2 < 1.0E-7)) {
         int $$6 = findBits($$0, $$3);
         int $$7 = findBits($$1, $$4);
         int $$8 = findBits($$2, $$5);
         if ($$6 < 0 || $$7 < 0 || $$8 < 0) {
            return new ArrayVoxelShape(
               BLOCK.shape,
               DoubleArrayList.wrap(new double[]{$$0, $$3}),
               DoubleArrayList.wrap(new double[]{$$1, $$4}),
               DoubleArrayList.wrap(new double[]{$$2, $$5})
            );
         } else if ($$6 == 0 && $$7 == 0 && $$8 == 0) {
            return block();
         } else {
            int $$9 = 1 << $$6;
            int $$10 = 1 << $$7;
            int $$11 = 1 << $$8;
            BitSetDiscreteVoxelShape $$12 = BitSetDiscreteVoxelShape.withFilledBounds(
               $$9,
               $$10,
               $$11,
               (int)Math.round($$0 * $$9),
               (int)Math.round($$1 * $$10),
               (int)Math.round($$2 * $$11),
               (int)Math.round($$3 * $$9),
               (int)Math.round($$4 * $$10),
               (int)Math.round($$5 * $$11)
            );
            return new CubeVoxelShape($$12);
         }
      } else {
         return empty();
      }
   }

   public static VoxelShape create(net.minecraft.world.phys.AABB $$0) {
      return create($$0.minX, $$0.minY, $$0.minZ, $$0.maxX, $$0.maxY, $$0.maxZ);
   }

   @VisibleForTesting
   protected static int findBits(double $$0, double $$1) {
      if (!($$0 < -1.0E-7) && !($$1 > 1.0000001)) {
         for (int $$2 = 0; $$2 <= 3; $$2++) {
            int $$3 = 1 << $$2;
            double $$4 = $$0 * $$3;
            double $$5 = $$1 * $$3;
            boolean $$6 = Math.abs($$4 - Math.round($$4)) < 1.0E-7 * $$3;
            boolean $$7 = Math.abs($$5 - Math.round($$5)) < 1.0E-7 * $$3;
            if ($$6 && $$7) {
               return $$2;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   protected static long lcm(int $$0, int $$1) {
      return (long)$$0 * ($$1 / IntMath.gcd($$0, $$1));
   }

   public static VoxelShape or(VoxelShape $$0, VoxelShape $$1) {
      return join($$0, $$1, BooleanOp.OR);
   }

   public static VoxelShape or(VoxelShape $$0, VoxelShape... $$1) {
      return Arrays.stream($$1).reduce($$0, Shapes::or);
   }

   public static VoxelShape join(VoxelShape $$0, VoxelShape $$1, BooleanOp $$2) {
      return joinUnoptimized($$0, $$1, $$2).optimize();
   }

   public static VoxelShape joinUnoptimized(VoxelShape $$0, VoxelShape $$1, BooleanOp $$2) {
      if ($$2.apply(false, false)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
      } else if ($$0 == $$1) {
         return $$2.apply(true, true) ? $$0 : empty();
      } else {
         boolean $$3 = $$2.apply(true, false);
         boolean $$4 = $$2.apply(false, true);
         if ($$0.isEmpty()) {
            return $$4 ? $$1 : empty();
         } else if ($$1.isEmpty()) {
            return $$3 ? $$0 : empty();
         } else {
            IndexMerger $$5 = createIndexMerger(1, $$0.getCoords(Axis.X), $$1.getCoords(Axis.X), $$3, $$4);
            IndexMerger $$6 = createIndexMerger($$5.size() - 1, $$0.getCoords(Axis.Y), $$1.getCoords(Axis.Y), $$3, $$4);
            IndexMerger $$7 = createIndexMerger(($$5.size() - 1) * ($$6.size() - 1), $$0.getCoords(Axis.Z), $$1.getCoords(Axis.Z), $$3, $$4);
            BitSetDiscreteVoxelShape $$8 = BitSetDiscreteVoxelShape.join($$0.shape, $$1.shape, $$5, $$6, $$7, $$2);
            return (VoxelShape)($$5 instanceof DiscreteCubeMerger && $$6 instanceof DiscreteCubeMerger && $$7 instanceof DiscreteCubeMerger
               ? new CubeVoxelShape($$8)
               : new ArrayVoxelShape($$8, $$5.getList(), $$6.getList(), $$7.getList()));
         }
      }
   }

   public static boolean joinIsNotEmpty(VoxelShape $$0, VoxelShape $$1, BooleanOp $$2) {
      if ($$2.apply(false, false)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
      } else {
         boolean $$3 = $$0.isEmpty();
         boolean $$4 = $$1.isEmpty();
         if (!$$3 && !$$4) {
            if ($$0 == $$1) {
               return $$2.apply(true, true);
            } else {
               boolean $$5 = $$2.apply(true, false);
               boolean $$6 = $$2.apply(false, true);

               for (Axis $$7 : AxisCycle.AXIS_VALUES) {
                  if ($$0.max($$7) < $$1.min($$7) - 1.0E-7) {
                     return $$5 || $$6;
                  }

                  if ($$1.max($$7) < $$0.min($$7) - 1.0E-7) {
                     return $$5 || $$6;
                  }
               }

               IndexMerger $$8 = createIndexMerger(1, $$0.getCoords(Axis.X), $$1.getCoords(Axis.X), $$5, $$6);
               IndexMerger $$9 = createIndexMerger($$8.size() - 1, $$0.getCoords(Axis.Y), $$1.getCoords(Axis.Y), $$5, $$6);
               IndexMerger $$10 = createIndexMerger(($$8.size() - 1) * ($$9.size() - 1), $$0.getCoords(Axis.Z), $$1.getCoords(Axis.Z), $$5, $$6);
               return joinIsNotEmpty($$8, $$9, $$10, $$0.shape, $$1.shape, $$2);
            }
         } else {
            return $$2.apply(!$$3, !$$4);
         }
      }
   }

   private static boolean joinIsNotEmpty(IndexMerger $$0, IndexMerger $$1, IndexMerger $$2, DiscreteVoxelShape $$3, DiscreteVoxelShape $$4, BooleanOp $$5) {
      return !$$0.forMergedIndexes(
         ($$5x, $$6, $$7) -> $$1.forMergedIndexes(
            ($$6x, $$7x, $$8) -> $$2.forMergedIndexes(($$7xx, $$8x, $$9) -> !$$5.apply($$3.isFullWide($$5x, $$6x, $$7xx), $$4.isFullWide($$6, $$7x, $$8x)))
         )
      );
   }

   public static double collide(Axis $$0, net.minecraft.world.phys.AABB $$1, Iterable<VoxelShape> $$2, double $$3) {
      for (VoxelShape $$4 : $$2) {
         if (Math.abs($$3) < 1.0E-7) {
            return 0.0;
         }

         $$3 = $$4.collide($$0, $$1, $$3);
      }

      return $$3;
   }

   public static boolean blockOccludes(VoxelShape $$0, VoxelShape $$1, Direction $$2) {
      if ($$0 == block() && $$1 == block()) {
         return true;
      } else if ($$1.isEmpty()) {
         return false;
      } else {
         Axis $$3 = $$2.getAxis();
         AxisDirection $$4 = $$2.getAxisDirection();
         VoxelShape $$5 = $$4 == AxisDirection.POSITIVE ? $$0 : $$1;
         VoxelShape $$6 = $$4 == AxisDirection.POSITIVE ? $$1 : $$0;
         BooleanOp $$7 = $$4 == AxisDirection.POSITIVE ? BooleanOp.ONLY_FIRST : BooleanOp.ONLY_SECOND;
         return DoubleMath.fuzzyEquals($$5.max($$3), 1.0, 1.0E-7)
            && DoubleMath.fuzzyEquals($$6.min($$3), 0.0, 1.0E-7)
            && !joinIsNotEmpty(new SliceShape($$5, $$3, $$5.shape.getSize($$3) - 1), new SliceShape($$6, $$3, 0), $$7);
      }
   }

   public static boolean mergedFaceOccludes(VoxelShape $$0, VoxelShape $$1, Direction $$2) {
      if ($$0 != block() && $$1 != block()) {
         Axis $$3 = $$2.getAxis();
         AxisDirection $$4 = $$2.getAxisDirection();
         VoxelShape $$5 = $$4 == AxisDirection.POSITIVE ? $$0 : $$1;
         VoxelShape $$6 = $$4 == AxisDirection.POSITIVE ? $$1 : $$0;
         if (!DoubleMath.fuzzyEquals($$5.max($$3), 1.0, 1.0E-7)) {
            $$5 = empty();
         }

         if (!DoubleMath.fuzzyEquals($$6.min($$3), 0.0, 1.0E-7)) {
            $$6 = empty();
         }

         return !joinIsNotEmpty(
            block(), joinUnoptimized(new SliceShape($$5, $$3, $$5.shape.getSize($$3) - 1), new SliceShape($$6, $$3, 0), BooleanOp.OR), BooleanOp.ONLY_FIRST
         );
      } else {
         return true;
      }
   }

   public static boolean faceShapeOccludes(VoxelShape $$0, VoxelShape $$1) {
      if ($$0 == block() || $$1 == block()) {
         return true;
      } else {
         return $$0.isEmpty() && $$1.isEmpty() ? false : !joinIsNotEmpty(block(), joinUnoptimized($$0, $$1, BooleanOp.OR), BooleanOp.ONLY_FIRST);
      }
   }

   @VisibleForTesting
   protected static IndexMerger createIndexMerger(int $$0, DoubleList $$1, DoubleList $$2, boolean $$3, boolean $$4) {
      int $$5 = $$1.size() - 1;
      int $$6 = $$2.size() - 1;
      if ($$1 instanceof CubePointRange && $$2 instanceof CubePointRange) {
         long $$7 = lcm($$5, $$6);
         if ($$0 * $$7 <= 256L) {
            return new DiscreteCubeMerger($$5, $$6);
         }
      }

      if ($$1.getDouble($$5) < $$2.getDouble(0) - 1.0E-7) {
         return new NonOverlappingMerger($$1, $$2, false);
      } else if ($$2.getDouble($$6) < $$1.getDouble(0) - 1.0E-7) {
         return new NonOverlappingMerger($$2, $$1, true);
      } else {
         return (IndexMerger)($$5 == $$6 && Objects.equals($$1, $$2) ? new IdenticalMerger($$1) : new IndirectMerger($$1, $$2, $$3, $$4));
      }
   }

   public static VoxelShape rotate(VoxelShape $$0, OctahedralGroup $$1) {
      return rotate($$0, $$1, BLOCK_CENTER);
   }

   public static VoxelShape rotate(VoxelShape $$0, OctahedralGroup $$1, net.minecraft.world.phys.Vec3 $$2) {
      if ($$1 == OctahedralGroup.IDENTITY) {
         return $$0;
      } else {
         DiscreteVoxelShape $$3 = $$0.shape.rotate($$1);
         if ($$0 instanceof CubeVoxelShape && BLOCK_CENTER.equals($$2)) {
            return new CubeVoxelShape($$3);
         } else {
            Axis $$4 = $$1.permutation().permuteAxis(Axis.X);
            Axis $$5 = $$1.permutation().permuteAxis(Axis.Y);
            Axis $$6 = $$1.permutation().permuteAxis(Axis.Z);
            DoubleList $$7 = $$0.getCoords($$4);
            DoubleList $$8 = $$0.getCoords($$5);
            DoubleList $$9 = $$0.getCoords($$6);
            boolean $$10 = $$1.inverts(Axis.X);
            boolean $$11 = $$1.inverts(Axis.Y);
            boolean $$12 = $$1.inverts(Axis.Z);
            return new ArrayVoxelShape(
               $$3,
               flipAxisIfNeeded($$7, $$10, $$2.get($$4), $$2.x),
               flipAxisIfNeeded($$8, $$11, $$2.get($$5), $$2.y),
               flipAxisIfNeeded($$9, $$12, $$2.get($$6), $$2.z)
            );
         }
      }
   }

   @VisibleForTesting
   static DoubleList flipAxisIfNeeded(DoubleList $$0, boolean $$1, double $$2, double $$3) {
      if (!$$1 && $$2 == $$3) {
         return $$0;
      } else {
         int $$4 = $$0.size();
         DoubleList $$5 = new DoubleArrayList($$4);
         if ($$1) {
            for (int $$6 = $$4 - 1; $$6 >= 0; $$6--) {
               $$5.add(-($$0.getDouble($$6) - $$2) + $$3);
            }
         } else {
            for (int $$7 = 0; $$7 >= 0 && $$7 < $$4; $$7++) {
               $$5.add($$0.getDouble($$7) - $$2 + $$3);
            }
         }

         return $$5;
      }
   }

   public static boolean equal(VoxelShape $$0, VoxelShape $$1) {
      return !joinIsNotEmpty($$0, $$1, BooleanOp.NOT_SAME);
   }

   public static Map<Axis, VoxelShape> rotateHorizontalAxis(VoxelShape $$0) {
      return rotateHorizontalAxis($$0, BLOCK_CENTER);
   }

   public static Map<Axis, VoxelShape> rotateHorizontalAxis(VoxelShape $$0, net.minecraft.world.phys.Vec3 $$1) {
      return Maps.newEnumMap(Map.of(Axis.Z, $$0, Axis.X, rotate($$0, OctahedralGroup.BLOCK_ROT_Y_90, $$1)));
   }

   public static Map<Axis, VoxelShape> rotateAllAxis(VoxelShape $$0) {
      return rotateAllAxis($$0, BLOCK_CENTER);
   }

   public static Map<Axis, VoxelShape> rotateAllAxis(VoxelShape $$0, net.minecraft.world.phys.Vec3 $$1) {
      return Maps.newEnumMap(
         Map.of(Axis.Z, $$0, Axis.X, rotate($$0, OctahedralGroup.BLOCK_ROT_Y_90, $$1), Axis.Y, rotate($$0, OctahedralGroup.BLOCK_ROT_X_90, $$1))
      );
   }

   public static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape $$0) {
      return rotateHorizontal($$0, OctahedralGroup.IDENTITY, BLOCK_CENTER);
   }

   public static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape $$0, OctahedralGroup $$1) {
      return rotateHorizontal($$0, $$1, BLOCK_CENTER);
   }

   public static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape $$0, OctahedralGroup $$1, net.minecraft.world.phys.Vec3 $$2) {
      return Maps.newEnumMap(
         Map.of(
            Direction.NORTH,
            rotate($$0, $$1),
            Direction.EAST,
            rotate($$0, OctahedralGroup.BLOCK_ROT_Y_90.compose($$1), $$2),
            Direction.SOUTH,
            rotate($$0, OctahedralGroup.BLOCK_ROT_Y_180.compose($$1), $$2),
            Direction.WEST,
            rotate($$0, OctahedralGroup.BLOCK_ROT_Y_270.compose($$1), $$2)
         )
      );
   }

   public static Map<Direction, VoxelShape> rotateAll(VoxelShape $$0) {
      return rotateAll($$0, OctahedralGroup.IDENTITY, BLOCK_CENTER);
   }

   public static Map<Direction, VoxelShape> rotateAll(VoxelShape $$0, net.minecraft.world.phys.Vec3 $$1) {
      return rotateAll($$0, OctahedralGroup.IDENTITY, $$1);
   }

   public static Map<Direction, VoxelShape> rotateAll(VoxelShape $$0, OctahedralGroup $$1, net.minecraft.world.phys.Vec3 $$2) {
      return Maps.newEnumMap(
         Map.of(
            Direction.NORTH,
            rotate($$0, $$1),
            Direction.EAST,
            rotate($$0, OctahedralGroup.BLOCK_ROT_Y_90.compose($$1), $$2),
            Direction.SOUTH,
            rotate($$0, OctahedralGroup.BLOCK_ROT_Y_180.compose($$1), $$2),
            Direction.WEST,
            rotate($$0, OctahedralGroup.BLOCK_ROT_Y_270.compose($$1), $$2),
            Direction.UP,
            rotate($$0, OctahedralGroup.BLOCK_ROT_X_270.compose($$1), $$2),
            Direction.DOWN,
            rotate($$0, OctahedralGroup.BLOCK_ROT_X_90.compose($$1), $$2)
         )
      );
   }

   public static Map<AttachFace, Map<Direction, VoxelShape>> rotateAttachFace(VoxelShape $$0) {
      return rotateAttachFace($$0, OctahedralGroup.IDENTITY);
   }

   public static Map<AttachFace, Map<Direction, VoxelShape>> rotateAttachFace(VoxelShape $$0, OctahedralGroup $$1) {
      return Map.of(
         AttachFace.WALL,
         rotateHorizontal($$0, $$1),
         AttachFace.FLOOR,
         rotateHorizontal($$0, OctahedralGroup.BLOCK_ROT_X_270.compose($$1)),
         AttachFace.CEILING,
         rotateHorizontal($$0, OctahedralGroup.BLOCK_ROT_Y_180.compose(OctahedralGroup.BLOCK_ROT_X_90).compose($$1))
      );
   }

   public interface DoubleLineConsumer {
      void consume(double var1, double var3, double var5, double var7, double var9, double var11);
   }
}
