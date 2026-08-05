/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Iterators
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  io.netty.buffer.ByteBuf
 *  org.jetbrains.annotations.Contract
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
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
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public enum Direction implements StringRepresentable
{
    DOWN(0, 1, -1, "down", AxisDirection.NEGATIVE, Axis.Y, new Vec3i(0, -1, 0)),
    UP(1, 0, -1, "up", AxisDirection.POSITIVE, Axis.Y, new Vec3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", AxisDirection.NEGATIVE, Axis.Z, new Vec3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", AxisDirection.POSITIVE, Axis.Z, new Vec3i(0, 0, 1)),
    WEST(4, 5, 1, "west", AxisDirection.NEGATIVE, Axis.X, new Vec3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", AxisDirection.POSITIVE, Axis.X, new Vec3i(1, 0, 0));

    public static final StringRepresentable.EnumCodec<Direction> CODEC;
    public static final Codec<Direction> VERTICAL_CODEC;
    public static final IntFunction<Direction> BY_ID;
    public static final StreamCodec<ByteBuf, Direction> STREAM_CODEC;
    @Deprecated
    public static final Codec<Direction> LEGACY_ID_CODEC;
    @Deprecated
    public static final Codec<Direction> LEGACY_ID_CODEC_2D;
    private static final ImmutableList<Axis> YXZ_AXIS_ORDER;
    private static final ImmutableList<Axis> YZX_AXIS_ORDER;
    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;
    private final String name;
    private final Axis axis;
    private final AxisDirection axisDirection;
    private final Vec3i normal;
    private final Vec3 normalVec3;
    private final Vector3fc normalVec3f;
    private static final Direction[] VALUES;
    private static final Direction[] BY_3D_DATA;
    private static final Direction[] BY_2D_DATA;

    private Direction(int $$0, int $$1, int $$2, String $$3, AxisDirection $$4, Axis $$5, Vec3i $$6) {
        this.data3d = $$0;
        this.data2d = $$2;
        this.oppositeIndex = $$1;
        this.name = $$3;
        this.axis = $$5;
        this.axisDirection = $$4;
        this.normal = $$6;
        this.normalVec3 = Vec3.atLowerCornerOf($$6);
        this.normalVec3f = new Vector3f((float)$$6.getX(), (float)$$6.getY(), (float)$$6.getZ());
    }

    public static Direction[] orderedByNearest(Entity $$0) {
        Direction $$17;
        float $$1 = $$0.getViewXRot(1.0f) * ((float)Math.PI / 180);
        float $$2 = -$$0.getViewYRot(1.0f) * ((float)Math.PI / 180);
        float $$3 = Mth.sin($$1);
        float $$4 = Mth.cos($$1);
        float $$5 = Mth.sin($$2);
        float $$6 = Mth.cos($$2);
        boolean $$7 = $$5 > 0.0f;
        boolean $$8 = $$3 < 0.0f;
        boolean $$9 = $$6 > 0.0f;
        float $$10 = $$7 ? $$5 : -$$5;
        float $$11 = $$8 ? -$$3 : $$3;
        float $$12 = $$9 ? $$6 : -$$6;
        float $$13 = $$10 * $$4;
        float $$14 = $$12 * $$4;
        Direction $$15 = $$7 ? EAST : WEST;
        Direction $$16 = $$8 ? UP : DOWN;
        Direction direction = $$17 = $$9 ? SOUTH : NORTH;
        if ($$10 > $$12) {
            if ($$11 > $$13) {
                return Direction.makeDirectionArray($$16, $$15, $$17);
            }
            if ($$14 > $$11) {
                return Direction.makeDirectionArray($$15, $$17, $$16);
            }
            return Direction.makeDirectionArray($$15, $$16, $$17);
        }
        if ($$11 > $$14) {
            return Direction.makeDirectionArray($$16, $$17, $$15);
        }
        if ($$13 > $$11) {
            return Direction.makeDirectionArray($$17, $$15, $$16);
        }
        return Direction.makeDirectionArray($$17, $$16, $$15);
    }

    private static Direction[] makeDirectionArray(Direction $$0, Direction $$1, Direction $$2) {
        return new Direction[]{$$0, $$1, $$2, $$2.getOpposite(), $$1.getOpposite(), $$0.getOpposite()};
    }

    public static Direction rotate(Matrix4fc $$0, Direction $$1) {
        Vector3f $$2 = $$0.transformDirection($$1.normalVec3f, new Vector3f());
        return Direction.getApproximateNearest($$2.x(), $$2.y(), $$2.z());
    }

    public static Collection<Direction> allShuffled(RandomSource $$0) {
        return Util.shuffledCopy(Direction.values(), $$0);
    }

    public static Stream<Direction> stream() {
        return Stream.of(VALUES);
    }

    public static float getYRot(Direction $$0) {
        return switch ($$0.ordinal()) {
            case 2 -> 180.0f;
            case 3 -> 0.0f;
            case 4 -> 90.0f;
            case 5 -> -90.0f;
            default -> throw new IllegalStateException("No y-Rot for vertical axis: " + String.valueOf($$0));
        };
    }

    public Quaternionf getRotation() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> new Quaternionf().rotationX((float)Math.PI);
            case 1 -> new Quaternionf();
            case 2 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, (float)Math.PI);
            case 3 -> new Quaternionf().rotationX(1.5707964f);
            case 4 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, 1.5707964f);
            case 5 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, -1.5707964f);
        };
    }

    public int get3DDataValue() {
        return this.data3d;
    }

    public int get2DDataValue() {
        return this.data2d;
    }

    public AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public static Direction getFacingAxis(Entity $$0, Axis $$1) {
        return switch ($$1.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (EAST.isFacingAngle($$0.getViewYRot(1.0f))) {
                    yield EAST;
                }
                yield WEST;
            }
            case 2 -> {
                if (SOUTH.isFacingAngle($$0.getViewYRot(1.0f))) {
                    yield SOUTH;
                }
                yield NORTH;
            }
            case 1 -> $$0.getViewXRot(1.0f) < 0.0f ? UP : DOWN;
        };
    }

    public Direction getOpposite() {
        return Direction.from3DDataValue(this.oppositeIndex);
    }

    public Direction getClockWise(Axis $$0) {
        return switch ($$0.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this == WEST || this == EAST) {
                    yield this;
                }
                yield this.getClockWiseX();
            }
            case 1 -> {
                if (this == UP || this == DOWN) {
                    yield this;
                }
                yield this.getClockWise();
            }
            case 2 -> this == NORTH || this == SOUTH ? this : this.getClockWiseZ();
        };
    }

    public Direction getCounterClockWise(Axis $$0) {
        return switch ($$0.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this == WEST || this == EAST) {
                    yield this;
                }
                yield this.getCounterClockWiseX();
            }
            case 1 -> {
                if (this == UP || this == DOWN) {
                    yield this;
                }
                yield this.getCounterClockWise();
            }
            case 2 -> this == NORTH || this == SOUTH ? this : this.getCounterClockWiseZ();
        };
    }

    public Direction getClockWise() {
        return switch (this.ordinal()) {
            case 2 -> EAST;
            case 5 -> SOUTH;
            case 3 -> WEST;
            case 4 -> NORTH;
            default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getClockWiseX() {
        return switch (this.ordinal()) {
            case 1 -> NORTH;
            case 2 -> DOWN;
            case 0 -> SOUTH;
            case 3 -> UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getCounterClockWiseX() {
        return switch (this.ordinal()) {
            case 1 -> SOUTH;
            case 3 -> DOWN;
            case 0 -> NORTH;
            case 2 -> UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getClockWiseZ() {
        return switch (this.ordinal()) {
            case 1 -> EAST;
            case 5 -> DOWN;
            case 0 -> WEST;
            case 4 -> UP;
            default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getCounterClockWiseZ() {
        return switch (this.ordinal()) {
            case 1 -> WEST;
            case 4 -> DOWN;
            case 0 -> EAST;
            case 5 -> UP;
            default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + String.valueOf(this));
        };
    }

    public Direction getCounterClockWise() {
        return switch (this.ordinal()) {
            case 2 -> WEST;
            case 5 -> NORTH;
            case 3 -> EAST;
            case 4 -> SOUTH;
            default -> throw new IllegalStateException("Unable to get CCW facing of " + String.valueOf(this));
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

    public Axis getAxis() {
        return this.axis;
    }

    public static @Nullable Direction byName(String $$0) {
        return CODEC.byName($$0);
    }

    public static Direction from3DDataValue(int $$0) {
        return BY_3D_DATA[Mth.abs($$0 % BY_3D_DATA.length)];
    }

    public static Direction from2DDataValue(int $$0) {
        return BY_2D_DATA[Mth.abs($$0 % BY_2D_DATA.length)];
    }

    public static Direction fromYRot(double $$0) {
        return Direction.from2DDataValue(Mth.floor($$0 / 90.0 + 0.5) & 3);
    }

    public static Direction fromAxisAndDirection(Axis $$0, AxisDirection $$1) {
        return switch ($$0.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if ($$1 == AxisDirection.POSITIVE) {
                    yield EAST;
                }
                yield WEST;
            }
            case 1 -> {
                if ($$1 == AxisDirection.POSITIVE) {
                    yield UP;
                }
                yield DOWN;
            }
            case 2 -> $$1 == AxisDirection.POSITIVE ? SOUTH : NORTH;
        };
    }

    public float toYRot() {
        return (this.data2d & 3) * 90;
    }

    public static Direction getRandom(RandomSource $$0) {
        return Util.getRandom(VALUES, $$0);
    }

    public static Direction getApproximateNearest(double $$0, double $$1, double $$2) {
        return Direction.getApproximateNearest((float)$$0, (float)$$1, (float)$$2);
    }

    public static Direction getApproximateNearest(float $$0, float $$1, float $$2) {
        Direction $$3 = NORTH;
        float $$4 = Float.MIN_VALUE;
        for (Direction $$5 : VALUES) {
            float $$6 = $$0 * (float)$$5.normal.getX() + $$1 * (float)$$5.normal.getY() + $$2 * (float)$$5.normal.getZ();
            if (!($$6 > $$4)) continue;
            $$4 = $$6;
            $$3 = $$5;
        }
        return $$3;
    }

    public static Direction getApproximateNearest(Vec3 $$0) {
        return Direction.getApproximateNearest($$0.x, $$0.y, $$0.z);
    }

    @Contract(value="_,_,_,!null->!null;_,_,_,_->_")
    public static @Nullable Direction getNearest(int $$0, int $$1, int $$2, @Nullable Direction $$3) {
        int $$4 = Math.abs($$0);
        int $$5 = Math.abs($$1);
        int $$6 = Math.abs($$2);
        if ($$4 > $$6 && $$4 > $$5) {
            return $$0 < 0 ? WEST : EAST;
        }
        if ($$6 > $$4 && $$6 > $$5) {
            return $$2 < 0 ? NORTH : SOUTH;
        }
        if ($$5 > $$4 && $$5 > $$6) {
            return $$1 < 0 ? DOWN : UP;
        }
        return $$3;
    }

    @Contract(value="_,!null->!null;_,_->_")
    public static @Nullable Direction getNearest(Vec3i $$0, @Nullable Direction $$1) {
        return Direction.getNearest($$0.getX(), $$0.getY(), $$0.getZ(), $$1);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    private static DataResult<Direction> verifyVertical(Direction $$0) {
        return $$0.getAxis().isVertical() ? DataResult.success((Object)$$0) : DataResult.error(() -> "Expected a vertical direction");
    }

    public static Direction get(AxisDirection $$0, Axis $$1) {
        for (Direction $$2 : VALUES) {
            if ($$2.getAxisDirection() != $$0 || $$2.getAxis() != $$1) continue;
            return $$2;
        }
        throw new IllegalArgumentException("No such direction: " + String.valueOf((Object)$$0) + " " + String.valueOf($$1));
    }

    public static ImmutableList<Axis> axisStepOrder(Vec3 $$0) {
        if (Math.abs($$0.x) < Math.abs($$0.z)) {
            return YZX_AXIS_ORDER;
        }
        return YXZ_AXIS_ORDER;
    }

    public Vec3i getUnitVec3i() {
        return this.normal;
    }

    public Vec3 getUnitVec3() {
        return this.normalVec3;
    }

    public Vector3fc getUnitVec3f() {
        return this.normalVec3f;
    }

    public boolean isFacingAngle(float $$0) {
        float $$1 = $$0 * ((float)Math.PI / 180);
        float $$2 = -Mth.sin($$1);
        float $$3 = Mth.cos($$1);
        return (float)this.normal.getX() * $$2 + (float)this.normal.getZ() * $$3 > 0.0f;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Direction::values);
        VERTICAL_CODEC = CODEC.validate(Direction::verifyVertical);
        BY_ID = ByIdMap.continuous(Direction::get3DDataValue, Direction.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Direction::get3DDataValue);
        LEGACY_ID_CODEC = Codec.BYTE.xmap(Direction::from3DDataValue, $$0 -> (byte)$$0.get3DDataValue());
        LEGACY_ID_CODEC_2D = Codec.BYTE.xmap(Direction::from2DDataValue, $$0 -> (byte)$$0.get2DDataValue());
        YXZ_AXIS_ORDER = ImmutableList.of((Object)Axis.Y, (Object)Axis.X, (Object)Axis.Z);
        YZX_AXIS_ORDER = ImmutableList.of((Object)Axis.Y, (Object)Axis.Z, (Object)Axis.X);
        VALUES = Direction.values();
        BY_3D_DATA = (Direction[])Arrays.stream(VALUES).sorted(Comparator.comparingInt($$0 -> $$0.data3d)).toArray(Direction[]::new);
        BY_2D_DATA = (Direction[])Arrays.stream(VALUES).filter($$0 -> $$0.getAxis().isHorizontal()).sorted(Comparator.comparingInt($$0 -> $$0.data2d)).toArray(Direction[]::new);
    }

    public static enum Axis implements StringRepresentable,
    Predicate<Direction>
    {
        X("x"){

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
            public Direction getPositive() {
                return EAST;
            }

            @Override
            public Direction getNegative() {
                return WEST;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        }
        ,
        Y("y"){

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
            public Direction getPositive() {
                return UP;
            }

            @Override
            public Direction getNegative() {
                return DOWN;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        }
        ,
        Z("z"){

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
            public Direction getPositive() {
                return SOUTH;
            }

            @Override
            public Direction getNegative() {
                return NORTH;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        };

        public static final Axis[] VALUES;
        public static final StringRepresentable.EnumCodec<Axis> CODEC;
        private final String name;

        Axis(String $$0) {
            this.name = $$0;
        }

        public static @Nullable Axis byName(String $$0) {
            return CODEC.byName($$0);
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

        public abstract Direction getPositive();

        public abstract Direction getNegative();

        public Direction[] getDirections() {
            return new Direction[]{this.getPositive(), this.getNegative()};
        }

        public String toString() {
            return this.name;
        }

        public static Axis getRandom(RandomSource $$0) {
            return Util.getRandom(VALUES, $$0);
        }

        @Override
        public boolean test(@Nullable Direction $$0) {
            return $$0 != null && $$0.getAxis() == this;
        }

        public Plane getPlane() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2 -> Plane.HORIZONTAL;
                case 1 -> Plane.VERTICAL;
            };
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public abstract int choose(int var1, int var2, int var3);

        public abstract double choose(double var1, double var3, double var5);

        public abstract boolean choose(boolean var1, boolean var2, boolean var3);

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Direction)object);
        }

        static {
            VALUES = Axis.values();
            CODEC = StringRepresentable.fromEnum(Axis::values);
        }
    }

    public static enum AxisDirection {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int step;
        private final String name;

        private AxisDirection(int $$0, String $$1) {
            this.step = $$0;
            this.name = $$1;
        }

        public int getStep() {
            return this.step;
        }

        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }

        public AxisDirection opposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }
    }

    public static enum Plane implements Iterable<Direction>,
    Predicate<Direction>
    {
        HORIZONTAL(new Direction[]{NORTH, EAST, SOUTH, WEST}, new Axis[]{Axis.X, Axis.Z}),
        VERTICAL(new Direction[]{UP, DOWN}, new Axis[]{Axis.Y});

        private final Direction[] faces;
        private final Axis[] axis;

        private Plane(Direction[] $$0, Axis[] $$1) {
            this.faces = $$0;
            this.axis = $$1;
        }

        public Direction getRandomDirection(RandomSource $$0) {
            return Util.getRandom(this.faces, $$0);
        }

        public Axis getRandomAxis(RandomSource $$0) {
            return Util.getRandom(this.axis, $$0);
        }

        @Override
        public boolean test(@Nullable Direction $$0) {
            return $$0 != null && $$0.getAxis().getPlane() == this;
        }

        @Override
        public Iterator<Direction> iterator() {
            return Iterators.forArray((Object[])this.faces);
        }

        public Stream<Direction> stream() {
            return Arrays.stream(this.faces);
        }

        public List<Direction> shuffledCopy(RandomSource $$0) {
            return Util.shuffledCopy(this.faces, $$0);
        }

        public int length() {
            return this.faces.length;
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Direction)object);
        }
    }
}

