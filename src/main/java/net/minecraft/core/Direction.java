package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.StringRepresentable.EnumCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public enum Direction implements StringRepresentable {
   DOWN(0, 1, -1, "down", net.minecraft.core.Direction.AxisDirection.NEGATIVE, net.minecraft.core.Direction.Axis.Y, new net.minecraft.core.Vec3i(0, -1, 0)),
   UP(1, 0, -1, "up", net.minecraft.core.Direction.AxisDirection.POSITIVE, net.minecraft.core.Direction.Axis.Y, new net.minecraft.core.Vec3i(0, 1, 0)),
   NORTH(2, 3, 2, "north", net.minecraft.core.Direction.AxisDirection.NEGATIVE, net.minecraft.core.Direction.Axis.Z, new net.minecraft.core.Vec3i(0, 0, -1)),
   SOUTH(3, 2, 0, "south", net.minecraft.core.Direction.AxisDirection.POSITIVE, net.minecraft.core.Direction.Axis.Z, new net.minecraft.core.Vec3i(0, 0, 1)),
   WEST(4, 5, 1, "west", net.minecraft.core.Direction.AxisDirection.NEGATIVE, net.minecraft.core.Direction.Axis.X, new net.minecraft.core.Vec3i(-1, 0, 0)),
   EAST(5, 4, 3, "east", net.minecraft.core.Direction.AxisDirection.POSITIVE, net.minecraft.core.Direction.Axis.X, new net.minecraft.core.Vec3i(1, 0, 0));

   public static final EnumCodec<net.minecraft.core.Direction> CODEC = StringRepresentable.fromEnum(net.minecraft.core.Direction::values);
   public static final Codec<net.minecraft.core.Direction> VERTICAL_CODEC = CODEC.validate(net.minecraft.core.Direction::verifyVertical);
   public static final IntFunction<net.minecraft.core.Direction> BY_ID = ByIdMap.continuous(
      net.minecraft.core.Direction::get3DDataValue, values(), OutOfBoundsStrategy.WRAP
   );
   public static final StreamCodec<ByteBuf, net.minecraft.core.Direction> STREAM_CODEC = ByteBufCodecs.idMapper(
      BY_ID, net.minecraft.core.Direction::get3DDataValue
   );
   @Deprecated
   public static final Codec<net.minecraft.core.Direction> LEGACY_ID_CODEC = Codec.BYTE
      .xmap(net.minecraft.core.Direction::from3DDataValue, $$0 -> (byte)$$0.get3DDataValue());
   @Deprecated
   public static final Codec<net.minecraft.core.Direction> LEGACY_ID_CODEC_2D = Codec.BYTE
      .xmap(net.minecraft.core.Direction::from2DDataValue, $$0 -> (byte)$$0.get2DDataValue());
   private static final ImmutableList<net.minecraft.core.Direction.Axis> YXZ_AXIS_ORDER = ImmutableList.of(
      net.minecraft.core.Direction.Axis.Y, net.minecraft.core.Direction.Axis.X, net.minecraft.core.Direction.Axis.Z
   );
   private static final ImmutableList<net.minecraft.core.Direction.Axis> YZX_AXIS_ORDER = ImmutableList.of(
      net.minecraft.core.Direction.Axis.Y, net.minecraft.core.Direction.Axis.Z, net.minecraft.core.Direction.Axis.X
   );
   private final int data3d;
   private final int oppositeIndex;
   private final int data2d;
   private final String name;
   private final net.minecraft.core.Direction.Axis axis;
   private final net.minecraft.core.Direction.AxisDirection axisDirection;
   private final net.minecraft.core.Vec3i normal;
   private final Vec3 normalVec3;
   private final Vector3fc normalVec3f;
   private static final net.minecraft.core.Direction[] VALUES = values();
   private static final net.minecraft.core.Direction[] BY_3D_DATA = Arrays.stream(VALUES)
      .sorted(Comparator.comparingInt($$0 -> $$0.data3d))
      .toArray(net.minecraft.core.Direction[]::new);
   private static final net.minecraft.core.Direction[] BY_2D_DATA = Arrays.stream(VALUES)
      .filter($$0 -> $$0.getAxis().isHorizontal())
      .sorted(Comparator.comparingInt($$0 -> $$0.data2d))
      .toArray(net.minecraft.core.Direction[]::new);

   private Direction(
      final int $$0,
      final int $$1,
      final int $$2,
      final String $$3,
      final net.minecraft.core.Direction.AxisDirection $$4,
      final net.minecraft.core.Direction.Axis $$5,
      final net.minecraft.core.Vec3i $$6
   ) {
      this.data3d = $$0;
      this.data2d = $$2;
      this.oppositeIndex = $$1;
      this.name = $$3;
      this.axis = $$5;
      this.axisDirection = $$4;
      this.normal = $$6;
      this.normalVec3 = Vec3.atLowerCornerOf($$6);
      this.normalVec3f = new Vector3f($$6.getX(), $$6.getY(), $$6.getZ());
   }

   public static net.minecraft.core.Direction[] orderedByNearest(Entity $$0) {
      float $$1 = $$0.getViewXRot(1.0F) * (float) (Math.PI / 180.0);
      float $$2 = -$$0.getViewYRot(1.0F) * (float) (Math.PI / 180.0);
      float $$3 = Mth.sin($$1);
      float $$4 = Mth.cos($$1);
      float $$5 = Mth.sin($$2);
      float $$6 = Mth.cos($$2);
      boolean $$7 = $$5 > 0.0F;
      boolean $$8 = $$3 < 0.0F;
      boolean $$9 = $$6 > 0.0F;
      float $$10 = $$7 ? $$5 : -$$5;
      float $$11 = $$8 ? -$$3 : $$3;
      float $$12 = $$9 ? $$6 : -$$6;
      float $$13 = $$10 * $$4;
      float $$14 = $$12 * $$4;
      net.minecraft.core.Direction $$15 = $$7 ? EAST : WEST;
      net.minecraft.core.Direction $$16 = $$8 ? UP : DOWN;
      net.minecraft.core.Direction $$17 = $$9 ? SOUTH : NORTH;
      if ($$10 > $$12) {
         if ($$11 > $$13) {
            return makeDirectionArray($$16, $$15, $$17);
         } else {
            return $$14 > $$11 ? makeDirectionArray($$15, $$17, $$16) : makeDirectionArray($$15, $$16, $$17);
         }
      } else if ($$11 > $$14) {
         return makeDirectionArray($$16, $$17, $$15);
      } else {
         return $$13 > $$11 ? makeDirectionArray($$17, $$15, $$16) : makeDirectionArray($$17, $$16, $$15);
      }
   }

   private static net.minecraft.core.Direction[] makeDirectionArray(
      net.minecraft.core.Direction $$0, net.minecraft.core.Direction $$1, net.minecraft.core.Direction $$2
   ) {
      return new net.minecraft.core.Direction[]{$$0, $$1, $$2, $$2.getOpposite(), $$1.getOpposite(), $$0.getOpposite()};
   }

   public static net.minecraft.core.Direction rotate(Matrix4fc $$0, net.minecraft.core.Direction $$1) {
      Vector3f $$2 = $$0.transformDirection($$1.normalVec3f, new Vector3f());
      return getApproximateNearest($$2.x(), $$2.y(), $$2.z());
   }

   public static Collection<net.minecraft.core.Direction> allShuffled(RandomSource $$0) {
      return Util.shuffledCopy(values(), $$0);
   }

   public static Stream<net.minecraft.core.Direction> stream() {
      return Stream.of(VALUES);
   }

   public static float getYRot(net.minecraft.core.Direction $$0) {
      return switch ($$0) {
         case NORTH -> 180.0F;
         case SOUTH -> 0.0F;
         case WEST -> 90.0F;
         case EAST -> -90.0F;
         default -> throw new IllegalStateException("No y-Rot for vertical axis: " + $$0);
      };
   }

   public Quaternionf getRotation() {
      return switch (this) {
         case DOWN -> new Quaternionf().rotationX((float) Math.PI);
         case UP -> new Quaternionf();
         case NORTH -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) Math.PI);
         case SOUTH -> new Quaternionf().rotationX((float) (Math.PI / 2));
         case WEST -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) (Math.PI / 2));
         case EAST -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) (-Math.PI / 2));
      };
   }

   public int get3DDataValue() {
      return this.data3d;
   }

   public int get2DDataValue() {
      return this.data2d;
   }

   public net.minecraft.core.Direction.AxisDirection getAxisDirection() {
      return this.axisDirection;
   }

   public static net.minecraft.core.Direction getFacingAxis(Entity $$0, net.minecraft.core.Direction.Axis $$1) {
      return switch ($$1) {
         case X -> EAST.isFacingAngle($$0.getViewYRot(1.0F)) ? EAST : WEST;
         case Y -> $$0.getViewXRot(1.0F) < 0.0F ? UP : DOWN;
         case Z -> SOUTH.isFacingAngle($$0.getViewYRot(1.0F)) ? SOUTH : NORTH;
      };
   }

   public net.minecraft.core.Direction getOpposite() {
      return from3DDataValue(this.oppositeIndex);
   }

   public net.minecraft.core.Direction getClockWise(net.minecraft.core.Direction.Axis $$0) {
      return switch ($$0) {
         case X -> this != WEST && this != EAST ? this.getClockWiseX() : this;
         case Y -> this != UP && this != DOWN ? this.getClockWise() : this;
         case Z -> this != NORTH && this != SOUTH ? this.getClockWiseZ() : this;
      };
   }

   public net.minecraft.core.Direction getCounterClockWise(net.minecraft.core.Direction.Axis $$0) {
      return switch ($$0) {
         case X -> this != WEST && this != EAST ? this.getCounterClockWiseX() : this;
         case Y -> this != UP && this != DOWN ? this.getCounterClockWise() : this;
         case Z -> this != NORTH && this != SOUTH ? this.getCounterClockWiseZ() : this;
      };
   }

   public net.minecraft.core.Direction getClockWise() {
      return switch (this) {
         case NORTH -> EAST;
         case SOUTH -> WEST;
         case WEST -> NORTH;
         case EAST -> SOUTH;
         default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
      };
   }

   private net.minecraft.core.Direction getClockWiseX() {
      return switch (this) {
         case DOWN -> SOUTH;
         case UP -> NORTH;
         case NORTH -> DOWN;
         case SOUTH -> UP;
         default -> throw new IllegalStateException("Unable to get X-rotated facing of " + this);
      };
   }

   private net.minecraft.core.Direction getCounterClockWiseX() {
      return switch (this) {
         case DOWN -> NORTH;
         case UP -> SOUTH;
         case NORTH -> UP;
         case SOUTH -> DOWN;
         default -> throw new IllegalStateException("Unable to get X-rotated facing of " + this);
      };
   }

   private net.minecraft.core.Direction getClockWiseZ() {
      return switch (this) {
         case DOWN -> WEST;
         case UP -> EAST;
         default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
         case WEST -> UP;
         case EAST -> DOWN;
      };
   }

   private net.minecraft.core.Direction getCounterClockWiseZ() {
      return switch (this) {
         case DOWN -> EAST;
         case UP -> WEST;
         default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
         case WEST -> DOWN;
         case EAST -> UP;
      };
   }

   public net.minecraft.core.Direction getCounterClockWise() {
      return switch (this) {
         case NORTH -> WEST;
         case SOUTH -> EAST;
         case WEST -> SOUTH;
         case EAST -> NORTH;
         default -> throw new IllegalStateException("Unable to get CCW facing of " + this);
      };
   }

   public int getStepX() {
      return this.normal.getX();
   }

   public int getStepY() {
      return this.normal.getY();
   }

   public int getStepZ() {
      return this.normal.getZ();
   }

   public Vector3f step() {
      return new Vector3f(this.normalVec3f);
   }

   public String getName() {
      return this.name;
   }

   public net.minecraft.core.Direction.Axis getAxis() {
      return this.axis;
   }

   
   public static net.minecraft.core.Direction byName(String $$0) {
      return (net.minecraft.core.Direction)CODEC.byName($$0);
   }

   public static net.minecraft.core.Direction from3DDataValue(int $$0) {
      return BY_3D_DATA[Mth.abs($$0 % BY_3D_DATA.length)];
   }

   public static net.minecraft.core.Direction from2DDataValue(int $$0) {
      return BY_2D_DATA[Mth.abs($$0 % BY_2D_DATA.length)];
   }

   public static net.minecraft.core.Direction fromYRot(double $$0) {
      return from2DDataValue(Mth.floor($$0 / 90.0 + 0.5) & 3);
   }

   public static net.minecraft.core.Direction fromAxisAndDirection(net.minecraft.core.Direction.Axis $$0, net.minecraft.core.Direction.AxisDirection $$1) {
      return switch ($$0) {
         case X -> $$1 == net.minecraft.core.Direction.AxisDirection.POSITIVE ? EAST : WEST;
         case Y -> $$1 == net.minecraft.core.Direction.AxisDirection.POSITIVE ? UP : DOWN;
         case Z -> $$1 == net.minecraft.core.Direction.AxisDirection.POSITIVE ? SOUTH : NORTH;
      };
   }

   public float toYRot() {
      return (this.data2d & 3) * 90;
   }

   public static net.minecraft.core.Direction getRandom(RandomSource $$0) {
      return (net.minecraft.core.Direction)Util.getRandom(VALUES, $$0);
   }

   public static net.minecraft.core.Direction getApproximateNearest(double $$0, double $$1, double $$2) {
      return getApproximateNearest((float)$$0, (float)$$1, (float)$$2);
   }

   public static net.minecraft.core.Direction getApproximateNearest(float $$0, float $$1, float $$2) {
      net.minecraft.core.Direction $$3 = NORTH;
      float $$4 = Float.MIN_VALUE;

      for (net.minecraft.core.Direction $$5 : VALUES) {
         float $$6 = $$0 * $$5.normal.getX() + $$1 * $$5.normal.getY() + $$2 * $$5.normal.getZ();
         if ($$6 > $$4) {
            $$4 = $$6;
            $$3 = $$5;
         }
      }

      return $$3;
   }

   public static net.minecraft.core.Direction getApproximateNearest(Vec3 $$0) {
      return getApproximateNearest($$0.x, $$0.y, $$0.z);
   }

   @Contract("_,_,_,!null->!null;_,_,_,_->_")
   
   public static net.minecraft.core.Direction getNearest(int $$0, int $$1, int $$2, net.minecraft.core.Direction $$3) {
      int $$4 = Math.abs($$0);
      int $$5 = Math.abs($$1);
      int $$6 = Math.abs($$2);
      if ($$4 > $$6 && $$4 > $$5) {
         return $$0 < 0 ? WEST : EAST;
      } else if ($$6 > $$4 && $$6 > $$5) {
         return $$2 < 0 ? NORTH : SOUTH;
      } else if ($$5 > $$4 && $$5 > $$6) {
         return $$1 < 0 ? DOWN : UP;
      } else {
         return $$3;
      }
   }

   @Contract("_,!null->!null;_,_->_")
   
   public static net.minecraft.core.Direction getNearest(net.minecraft.core.Vec3i $$0, net.minecraft.core.Direction $$1) {
      return getNearest($$0.getX(), $$0.getY(), $$0.getZ(), $$1);
   }

   @Override
   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }

   private static DataResult<net.minecraft.core.Direction> verifyVertical(net.minecraft.core.Direction $$0) {
      return $$0.getAxis().isVertical() ? DataResult.success($$0) : DataResult.error(() -> "Expected a vertical direction");
   }

   public static net.minecraft.core.Direction get(net.minecraft.core.Direction.AxisDirection $$0, net.minecraft.core.Direction.Axis $$1) {
      for (net.minecraft.core.Direction $$2 : VALUES) {
         if ($$2.getAxisDirection() == $$0 && $$2.getAxis() == $$1) {
            return $$2;
         }
      }

      throw new IllegalArgumentException("No such direction: " + $$0 + " " + $$1);
   }

   public static ImmutableList<net.minecraft.core.Direction.Axis> axisStepOrder(Vec3 $$0) {
      return Math.abs($$0.x) < Math.abs($$0.z) ? YZX_AXIS_ORDER : YXZ_AXIS_ORDER;
   }

   public net.minecraft.core.Vec3i getUnitVec3i() {
      return this.normal;
   }

   public Vec3 getUnitVec3() {
      return this.normalVec3;
   }

   public Vector3fc getUnitVec3f() {
      return this.normalVec3f;
   }

   public boolean isFacingAngle(float $$0) {
      float $$1 = $$0 * (float) (Math.PI / 180.0);
      float $$2 = -Mth.sin($$1);
      float $$3 = Mth.cos($$1);
      return this.normal.getX() * $$2 + this.normal.getZ() * $$3 > 0.0F;
   }

   public static enum Axis implements StringRepresentable, Predicate<net.minecraft.core.Direction> {
      X("x") {
         @Override
         public int choose(int $$0, int $$1, int $$2) {
            return $$0;
         }

         @Override
         public boolean choose(boolean $$0, boolean $$1, boolean $$2) {
            return $$0;
         }

         @Override
         public double choose(double $$0, double $$1, double $$2) {
            return $$0;
         }

         @Override
         public net.minecraft.core.Direction getPositive() {
            return net.minecraft.core.Direction.EAST;
         }

         @Override
         public net.minecraft.core.Direction getNegative() {
            return net.minecraft.core.Direction.WEST;
         }
      },
      Y("y") {
         @Override
         public int choose(int $$0, int $$1, int $$2) {
            return $$1;
         }

         @Override
         public double choose(double $$0, double $$1, double $$2) {
            return $$1;
         }

         @Override
         public boolean choose(boolean $$0, boolean $$1, boolean $$2) {
            return $$1;
         }

         @Override
         public net.minecraft.core.Direction getPositive() {
            return net.minecraft.core.Direction.UP;
         }

         @Override
         public net.minecraft.core.Direction getNegative() {
            return net.minecraft.core.Direction.DOWN;
         }
      },
      Z("z") {
         @Override
         public int choose(int $$0, int $$1, int $$2) {
            return $$2;
         }

         @Override
         public double choose(double $$0, double $$1, double $$2) {
            return $$2;
         }

         @Override
         public boolean choose(boolean $$0, boolean $$1, boolean $$2) {
            return $$2;
         }

         @Override
         public net.minecraft.core.Direction getPositive() {
            return net.minecraft.core.Direction.SOUTH;
         }

         @Override
         public net.minecraft.core.Direction getNegative() {
            return net.minecraft.core.Direction.NORTH;
         }
      };

      public static final net.minecraft.core.Direction.Axis[] VALUES = values();
      public static final EnumCodec<net.minecraft.core.Direction.Axis> CODEC = StringRepresentable.fromEnum(net.minecraft.core.Direction.Axis::values);
      private final String name;

      Axis(final String $$0) {
         this.name = $$0;
      }

      
      public static net.minecraft.core.Direction.Axis byName(String $$0) {
         return (net.minecraft.core.Direction.Axis)CODEC.byName($$0);
      }

      public String getName() {
         return this.name;
      }

      public boolean isVertical() {
         return this == Y;
      }

      public boolean isHorizontal() {
         return this == X || this == Z;
      }

      public abstract net.minecraft.core.Direction getPositive();

      public abstract net.minecraft.core.Direction getNegative();

      public net.minecraft.core.Direction[] getDirections() {
         return new net.minecraft.core.Direction[]{this.getPositive(), this.getNegative()};
      }

      @Override
      public String toString() {
         return this.name;
      }

      public static net.minecraft.core.Direction.Axis getRandom(RandomSource $$0) {
         return (net.minecraft.core.Direction.Axis)Util.getRandom(VALUES, $$0);
      }

      public boolean test(net.minecraft.core.Direction $$0) {
         return $$0 != null && $$0.getAxis() == this;
      }

      public net.minecraft.core.Direction.Plane getPlane() {
         return switch (this) {
            case X, Z -> net.minecraft.core.Direction.Plane.HORIZONTAL;
            case Y -> net.minecraft.core.Direction.Plane.VERTICAL;
         };
      }

      public String getSerializedName() {
         return this.name;
      }

      public abstract int choose(int var1, int var2, int var3);

      public abstract double choose(double var1, double var3, double var5);

      public abstract boolean choose(boolean var1, boolean var2, boolean var3);
   }

   public static enum AxisDirection {
      POSITIVE(1, "Towards positive"),
      NEGATIVE(-1, "Towards negative");

      private final int step;
      private final String name;

      private AxisDirection(final int $$0, final String $$1) {
         this.step = $$0;
         this.name = $$1;
      }

      public int getStep() {
         return this.step;
      }

      public String getName() {
         return this.name;
      }

      @Override
      public String toString() {
         return this.name;
      }

      public net.minecraft.core.Direction.AxisDirection opposite() {
         return this == POSITIVE ? NEGATIVE : POSITIVE;
      }
   }

   public static enum Plane implements Iterable<net.minecraft.core.Direction>, Predicate<net.minecraft.core.Direction> {
      HORIZONTAL(
         new net.minecraft.core.Direction[]{
            net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.WEST
         },
         new net.minecraft.core.Direction.Axis[]{net.minecraft.core.Direction.Axis.X, net.minecraft.core.Direction.Axis.Z}
      ),
      VERTICAL(
         new net.minecraft.core.Direction[]{net.minecraft.core.Direction.UP, net.minecraft.core.Direction.DOWN},
         new net.minecraft.core.Direction.Axis[]{net.minecraft.core.Direction.Axis.Y}
      );

      private final net.minecraft.core.Direction[] faces;
      private final net.minecraft.core.Direction.Axis[] axis;

      private Plane(final net.minecraft.core.Direction[] $$0, final net.minecraft.core.Direction.Axis[] $$1) {
         this.faces = $$0;
         this.axis = $$1;
      }

      public net.minecraft.core.Direction getRandomDirection(RandomSource $$0) {
         return (net.minecraft.core.Direction)Util.getRandom(this.faces, $$0);
      }

      public net.minecraft.core.Direction.Axis getRandomAxis(RandomSource $$0) {
         return (net.minecraft.core.Direction.Axis)Util.getRandom(this.axis, $$0);
      }

      public boolean test(net.minecraft.core.Direction $$0) {
         return $$0 != null && $$0.getAxis().getPlane() == this;
      }

      @Override
      public Iterator<net.minecraft.core.Direction> iterator() {
         return Iterators.forArray(this.faces);
      }

      public Stream<net.minecraft.core.Direction> stream() {
         return Arrays.stream(this.faces);
      }

      public List<net.minecraft.core.Direction> shuffledCopy(RandomSource $$0) {
         return Util.shuffledCopy(this.faces, $$0);
      }

      public int length() {
         return this.faces.length;
      }
   }
}
