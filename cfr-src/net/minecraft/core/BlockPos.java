/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  javax.annotation.concurrent.Immutable
 *  org.apache.commons.lang3.Validate
 *  org.apache.commons.lang3.tuple.Pair
 */
package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.Immutable;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

@Immutable
public class BlockPos
extends Vec3i {
    public static final Codec<BlockPos> CODEC = Codec.INT_STREAM.comapFlatMap($$02 -> Util.fixedSize($$02, 3).map($$0 -> new BlockPos($$0[0], $$0[1], $$0[2])), $$0 -> IntStream.of($$0.getX(), $$0.getY(), $$0.getZ())).stable();
    public static final StreamCodec<ByteBuf, BlockPos> STREAM_CODEC = new StreamCodec<ByteBuf, BlockPos>(){

        @Override
        public BlockPos decode(ByteBuf $$0) {
            return FriendlyByteBuf.readBlockPos($$0);
        }

        @Override
        public void encode(ByteBuf $$0, BlockPos $$1) {
            FriendlyByteBuf.writeBlockPos($$0, $$1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (BlockPos)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    public static final int PACKED_HORIZONTAL_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
    public static final int PACKED_Y_LENGTH = 64 - 2 * PACKED_HORIZONTAL_LENGTH;
    private static final long PACKED_X_MASK = (1L << PACKED_HORIZONTAL_LENGTH) - 1L;
    private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
    private static final long PACKED_Z_MASK = (1L << PACKED_HORIZONTAL_LENGTH) - 1L;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = PACKED_Y_LENGTH;
    private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_HORIZONTAL_LENGTH;
    public static final int MAX_HORIZONTAL_COORDINATE = (1 << PACKED_HORIZONTAL_LENGTH) / 2 - 1;

    public BlockPos(int $$0, int $$1, int $$2) {
        super($$0, $$1, $$2);
    }

    public BlockPos(Vec3i $$0) {
        this($$0.getX(), $$0.getY(), $$0.getZ());
    }

    public static long offset(long $$0, Direction $$1) {
        return BlockPos.offset($$0, $$1.getStepX(), $$1.getStepY(), $$1.getStepZ());
    }

    public static long offset(long $$0, int $$1, int $$2, int $$3) {
        return BlockPos.asLong(BlockPos.getX($$0) + $$1, BlockPos.getY($$0) + $$2, BlockPos.getZ($$0) + $$3);
    }

    public static int getX(long $$0) {
        return (int)($$0 << 64 - X_OFFSET - PACKED_HORIZONTAL_LENGTH >> 64 - PACKED_HORIZONTAL_LENGTH);
    }

    public static int getY(long $$0) {
        return (int)($$0 << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
    }

    public static int getZ(long $$0) {
        return (int)($$0 << 64 - Z_OFFSET - PACKED_HORIZONTAL_LENGTH >> 64 - PACKED_HORIZONTAL_LENGTH);
    }

    public static BlockPos of(long $$0) {
        return new BlockPos(BlockPos.getX($$0), BlockPos.getY($$0), BlockPos.getZ($$0));
    }

    public static BlockPos containing(double $$0, double $$1, double $$2) {
        return new BlockPos(Mth.floor($$0), Mth.floor($$1), Mth.floor($$2));
    }

    public static BlockPos containing(Position $$0) {
        return BlockPos.containing($$0.x(), $$0.y(), $$0.z());
    }

    public static BlockPos min(BlockPos $$0, BlockPos $$1) {
        return new BlockPos(Math.min($$0.getX(), $$1.getX()), Math.min($$0.getY(), $$1.getY()), Math.min($$0.getZ(), $$1.getZ()));
    }

    public static BlockPos max(BlockPos $$0, BlockPos $$1) {
        return new BlockPos(Math.max($$0.getX(), $$1.getX()), Math.max($$0.getY(), $$1.getY()), Math.max($$0.getZ(), $$1.getZ()));
    }

    public long asLong() {
        return BlockPos.asLong(this.getX(), this.getY(), this.getZ());
    }

    public static long asLong(int $$0, int $$1, int $$2) {
        long $$3 = 0L;
        $$3 |= ((long)$$0 & PACKED_X_MASK) << X_OFFSET;
        $$3 |= ((long)$$1 & PACKED_Y_MASK) << 0;
        return $$3 |= ((long)$$2 & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long $$0) {
        return $$0 & 0xFFFFFFFFFFFFFFF0L;
    }

    @Override
    public BlockPos offset(int $$0, int $$1, int $$2) {
        if ($$0 == 0 && $$1 == 0 && $$2 == 0) {
            return this;
        }
        return new BlockPos(this.getX() + $$0, this.getY() + $$1, this.getZ() + $$2);
    }

    public Vec3 getCenter() {
        return Vec3.atCenterOf(this);
    }

    public Vec3 getBottomCenter() {
        return Vec3.atBottomCenterOf(this);
    }

    @Override
    public BlockPos offset(Vec3i $$0) {
        return this.offset($$0.getX(), $$0.getY(), $$0.getZ());
    }

    @Override
    public BlockPos subtract(Vec3i $$0) {
        return this.offset(-$$0.getX(), -$$0.getY(), -$$0.getZ());
    }

    @Override
    public BlockPos multiply(int $$0) {
        if ($$0 == 1) {
            return this;
        }
        if ($$0 == 0) {
            return ZERO;
        }
        return new BlockPos(this.getX() * $$0, this.getY() * $$0, this.getZ() * $$0);
    }

    @Override
    public BlockPos above() {
        return this.relative(Direction.UP);
    }

    @Override
    public BlockPos above(int $$0) {
        return this.relative(Direction.UP, $$0);
    }

    @Override
    public BlockPos below() {
        return this.relative(Direction.DOWN);
    }

    @Override
    public BlockPos below(int $$0) {
        return this.relative(Direction.DOWN, $$0);
    }

    @Override
    public BlockPos north() {
        return this.relative(Direction.NORTH);
    }

    @Override
    public BlockPos north(int $$0) {
        return this.relative(Direction.NORTH, $$0);
    }

    @Override
    public BlockPos south() {
        return this.relative(Direction.SOUTH);
    }

    @Override
    public BlockPos south(int $$0) {
        return this.relative(Direction.SOUTH, $$0);
    }

    @Override
    public BlockPos west() {
        return this.relative(Direction.WEST);
    }

    @Override
    public BlockPos west(int $$0) {
        return this.relative(Direction.WEST, $$0);
    }

    @Override
    public BlockPos east() {
        return this.relative(Direction.EAST);
    }

    @Override
    public BlockPos east(int $$0) {
        return this.relative(Direction.EAST, $$0);
    }

    @Override
    public BlockPos relative(Direction $$0) {
        return new BlockPos(this.getX() + $$0.getStepX(), this.getY() + $$0.getStepY(), this.getZ() + $$0.getStepZ());
    }

    @Override
    public BlockPos relative(Direction $$0, int $$1) {
        if ($$1 == 0) {
            return this;
        }
        return new BlockPos(this.getX() + $$0.getStepX() * $$1, this.getY() + $$0.getStepY() * $$1, this.getZ() + $$0.getStepZ() * $$1);
    }

    @Override
    public BlockPos relative(Direction.Axis $$0, int $$1) {
        if ($$1 == 0) {
            return this;
        }
        int $$2 = $$0 == Direction.Axis.X ? $$1 : 0;
        int $$3 = $$0 == Direction.Axis.Y ? $$1 : 0;
        int $$4 = $$0 == Direction.Axis.Z ? $$1 : 0;
        return new BlockPos(this.getX() + $$2, this.getY() + $$3, this.getZ() + $$4);
    }

    public BlockPos rotate(Rotation $$0) {
        return switch ($$0) {
            default -> throw new MatchException(null, null);
            case Rotation.CLOCKWISE_90 -> new BlockPos(-this.getZ(), this.getY(), this.getX());
            case Rotation.CLOCKWISE_180 -> new BlockPos(-this.getX(), this.getY(), -this.getZ());
            case Rotation.COUNTERCLOCKWISE_90 -> new BlockPos(this.getZ(), this.getY(), -this.getX());
            case Rotation.NONE -> this;
        };
    }

    @Override
    public BlockPos cross(Vec3i $$0) {
        return new BlockPos(this.getY() * $$0.getZ() - this.getZ() * $$0.getY(), this.getZ() * $$0.getX() - this.getX() * $$0.getZ(), this.getX() * $$0.getY() - this.getY() * $$0.getX());
    }

    public BlockPos atY(int $$0) {
        return new BlockPos(this.getX(), $$0, this.getZ());
    }

    public BlockPos immutable() {
        return this;
    }

    public MutableBlockPos mutable() {
        return new MutableBlockPos(this.getX(), this.getY(), this.getZ());
    }

    public Vec3 clampLocationWithin(Vec3 $$0) {
        return new Vec3(Mth.clamp($$0.x, (double)((float)this.getX() + 1.0E-5f), (double)this.getX() + 1.0 - (double)1.0E-5f), Mth.clamp($$0.y, (double)((float)this.getY() + 1.0E-5f), (double)this.getY() + 1.0 - (double)1.0E-5f), Mth.clamp($$0.z, (double)((float)this.getZ() + 1.0E-5f), (double)this.getZ() + 1.0 - (double)1.0E-5f));
    }

    public static Iterable<BlockPos> randomInCube(RandomSource $$0, int $$1, BlockPos $$2, int $$3) {
        return BlockPos.randomBetweenClosed($$0, $$1, $$2.getX() - $$3, $$2.getY() - $$3, $$2.getZ() - $$3, $$2.getX() + $$3, $$2.getY() + $$3, $$2.getZ() + $$3);
    }

    @Deprecated
    public static Stream<BlockPos> squareOutSouthEast(BlockPos $$0) {
        return Stream.of($$0, $$0.south(), $$0.east(), $$0.south().east());
    }

    public static Iterable<BlockPos> randomBetweenClosed(final RandomSource $$0, final int $$1, final int $$2, final int $$3, final int $$4, int $$5, int $$6, int $$7) {
        final int $$8 = $$5 - $$2 + 1;
        final int $$9 = $$6 - $$3 + 1;
        final int $$10 = $$7 - $$4 + 1;
        return () -> new AbstractIterator<BlockPos>(){
            final MutableBlockPos nextPos = new MutableBlockPos();
            int counter = $$1;

            protected BlockPos computeNext() {
                if (this.counter <= 0) {
                    return (BlockPos)this.endOfData();
                }
                MutableBlockPos $$02 = this.nextPos.set($$2 + $$0.nextInt($$8), $$3 + $$0.nextInt($$9), $$4 + $$0.nextInt($$10));
                --this.counter;
                return $$02;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<BlockPos> withinManhattan(BlockPos $$0, final int $$1, final int $$2, final int $$3) {
        final int $$4 = $$1 + $$2 + $$3;
        final int $$5 = $$0.getX();
        final int $$6 = $$0.getY();
        final int $$7 = $$0.getZ();
        return () -> new AbstractIterator<BlockPos>(){
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int currentDepth;
            private int maxX;
            private int maxY;
            private int x;
            private int y;
            private boolean zMirror;

            protected BlockPos computeNext() {
                if (this.zMirror) {
                    this.zMirror = false;
                    this.cursor.setZ($$7 - (this.cursor.getZ() - $$7));
                    return this.cursor;
                }
                MutableBlockPos $$0 = null;
                while ($$0 == null) {
                    if (this.y > this.maxY) {
                        ++this.x;
                        if (this.x > this.maxX) {
                            ++this.currentDepth;
                            if (this.currentDepth > $$4) {
                                return (BlockPos)this.endOfData();
                            }
                            this.maxX = Math.min($$1, this.currentDepth);
                            this.x = -this.maxX;
                        }
                        this.maxY = Math.min($$2, this.currentDepth - Math.abs(this.x));
                        this.y = -this.maxY;
                    }
                    int $$12 = this.x;
                    int $$22 = this.y;
                    int $$32 = this.currentDepth - Math.abs($$12) - Math.abs($$22);
                    if ($$32 <= $$3) {
                        this.zMirror = $$32 != 0;
                        $$0 = this.cursor.set($$5 + $$12, $$6 + $$22, $$7 + $$32);
                    }
                    ++this.y;
                }
                return $$0;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Optional<BlockPos> findClosestMatch(BlockPos $$0, int $$1, int $$2, Predicate<BlockPos> $$3) {
        for (BlockPos $$4 : BlockPos.withinManhattan($$0, $$1, $$2, $$1)) {
            if (!$$3.test($$4)) continue;
            return Optional.of($$4);
        }
        return Optional.empty();
    }

    public static Stream<BlockPos> withinManhattanStream(BlockPos $$0, int $$1, int $$2, int $$3) {
        return StreamSupport.stream(BlockPos.withinManhattan($$0, $$1, $$2, $$3).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(AABB $$0) {
        BlockPos $$1 = BlockPos.containing($$0.minX, $$0.minY, $$0.minZ);
        BlockPos $$2 = BlockPos.containing($$0.maxX, $$0.maxY, $$0.maxZ);
        return BlockPos.betweenClosed($$1, $$2);
    }

    public static Iterable<BlockPos> betweenClosed(BlockPos $$0, BlockPos $$1) {
        return BlockPos.betweenClosed(Math.min($$0.getX(), $$1.getX()), Math.min($$0.getY(), $$1.getY()), Math.min($$0.getZ(), $$1.getZ()), Math.max($$0.getX(), $$1.getX()), Math.max($$0.getY(), $$1.getY()), Math.max($$0.getZ(), $$1.getZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(BlockPos $$0, BlockPos $$1) {
        return StreamSupport.stream(BlockPos.betweenClosed($$0, $$1).spliterator(), false);
    }

    public static Stream<BlockPos> betweenClosedStream(BoundingBox $$0) {
        return BlockPos.betweenClosedStream(Math.min($$0.minX(), $$0.maxX()), Math.min($$0.minY(), $$0.maxY()), Math.min($$0.minZ(), $$0.maxZ()), Math.max($$0.minX(), $$0.maxX()), Math.max($$0.minY(), $$0.maxY()), Math.max($$0.minZ(), $$0.maxZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(AABB $$0) {
        return BlockPos.betweenClosedStream(Mth.floor($$0.minX), Mth.floor($$0.minY), Mth.floor($$0.minZ), Mth.floor($$0.maxX), Mth.floor($$0.maxY), Mth.floor($$0.maxZ));
    }

    public static Stream<BlockPos> betweenClosedStream(int $$0, int $$1, int $$2, int $$3, int $$4, int $$5) {
        return StreamSupport.stream(BlockPos.betweenClosed($$0, $$1, $$2, $$3, $$4, $$5).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(final int $$0, final int $$1, final int $$2, int $$3, int $$4, int $$5) {
        final int $$6 = $$3 - $$0 + 1;
        final int $$7 = $$4 - $$1 + 1;
        int $$8 = $$5 - $$2 + 1;
        final int $$9 = $$6 * $$7 * $$8;
        return () -> new AbstractIterator<BlockPos>(){
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int index;

            protected BlockPos computeNext() {
                if (this.index == $$9) {
                    return (BlockPos)this.endOfData();
                }
                int $$02 = this.index % $$6;
                int $$12 = this.index / $$6;
                int $$22 = $$12 % $$7;
                int $$3 = $$12 / $$7;
                ++this.index;
                return this.cursor.set($$0 + $$02, $$1 + $$22, $$2 + $$3);
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<MutableBlockPos> spiralAround(final BlockPos $$0, final int $$1, final Direction $$2, final Direction $$3) {
        Validate.validState(($$2.getAxis() != $$3.getAxis() ? 1 : 0) != 0, (String)"The two directions cannot be on the same axis", (Object[])new Object[0]);
        return () -> new AbstractIterator<MutableBlockPos>(){
            private final Direction[] directions;
            private final MutableBlockPos cursor;
            private final int legs;
            private int leg;
            private int legSize;
            private int legIndex;
            private int lastX;
            private int lastY;
            private int lastZ;
            {
                this.directions = new Direction[]{$$2, $$3, $$2.getOpposite(), $$3.getOpposite()};
                this.cursor = $$0.mutable().move($$3);
                this.legs = 4 * $$1;
                this.leg = -1;
                this.lastX = this.cursor.getX();
                this.lastY = this.cursor.getY();
                this.lastZ = this.cursor.getZ();
            }

            protected MutableBlockPos computeNext() {
                this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
                this.lastX = this.cursor.getX();
                this.lastY = this.cursor.getY();
                this.lastZ = this.cursor.getZ();
                if (this.legIndex >= this.legSize) {
                    if (this.leg >= this.legs) {
                        return (MutableBlockPos)this.endOfData();
                    }
                    ++this.leg;
                    this.legIndex = 0;
                    this.legSize = this.leg / 2 + 1;
                }
                ++this.legIndex;
                return this.cursor;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static int breadthFirstTraversal(BlockPos $$0, int $$1, int $$22, BiConsumer<BlockPos, Consumer<BlockPos>> $$3, Function<BlockPos, TraversalNodeStatus> $$4) {
        ArrayDeque<Pair> $$5 = new ArrayDeque<Pair>();
        LongOpenHashSet $$6 = new LongOpenHashSet();
        $$5.add(Pair.of((Object)$$0, (Object)0));
        int $$7 = 0;
        while (!$$5.isEmpty()) {
            TraversalNodeStatus $$12;
            Pair $$8 = (Pair)$$5.poll();
            BlockPos $$9 = (BlockPos)$$8.getLeft();
            int $$10 = (Integer)$$8.getRight();
            long $$11 = $$9.asLong();
            if (!$$6.add($$11) || ($$12 = $$4.apply($$9)) == TraversalNodeStatus.SKIP) continue;
            if ($$12 == TraversalNodeStatus.STOP) break;
            if (++$$7 >= $$22) {
                return $$7;
            }
            if ($$10 >= $$1) continue;
            $$3.accept($$9, $$2 -> $$5.add(Pair.of((Object)$$2, (Object)($$10 + 1))));
        }
        return $$7;
    }

    public static Iterable<BlockPos> betweenCornersInDirection(AABB $$0, Vec3 $$1) {
        Vec3 $$2 = $$0.getMinPosition();
        int $$3 = Mth.floor($$2.x());
        int $$4 = Mth.floor($$2.y());
        int $$5 = Mth.floor($$2.z());
        Vec3 $$6 = $$0.getMaxPosition();
        int $$7 = Mth.floor($$6.x());
        int $$8 = Mth.floor($$6.y());
        int $$9 = Mth.floor($$6.z());
        return BlockPos.betweenCornersInDirection($$3, $$4, $$5, $$7, $$8, $$9, $$1);
    }

    public static Iterable<BlockPos> betweenCornersInDirection(BlockPos $$0, BlockPos $$1, Vec3 $$2) {
        return BlockPos.betweenCornersInDirection($$0.getX(), $$0.getY(), $$0.getZ(), $$1.getX(), $$1.getY(), $$1.getZ(), $$2);
    }

    public static Iterable<BlockPos> betweenCornersInDirection(int $$0, int $$1, int $$2, int $$3, int $$4, int $$5, Vec3 $$6) {
        int $$7 = Math.min($$0, $$3);
        int $$8 = Math.min($$1, $$4);
        int $$9 = Math.min($$2, $$5);
        int $$10 = Math.max($$0, $$3);
        int $$11 = Math.max($$1, $$4);
        int $$12 = Math.max($$2, $$5);
        int $$13 = $$10 - $$7;
        int $$14 = $$11 - $$8;
        int $$15 = $$12 - $$9;
        final int $$16 = $$6.x >= 0.0 ? $$7 : $$10;
        final int $$17 = $$6.y >= 0.0 ? $$8 : $$11;
        final int $$18 = $$6.z >= 0.0 ? $$9 : $$12;
        ImmutableList<Direction.Axis> $$19 = Direction.axisStepOrder($$6);
        Direction.Axis $$20 = (Direction.Axis)$$19.get(0);
        Direction.Axis $$21 = (Direction.Axis)$$19.get(1);
        Direction.Axis $$22 = (Direction.Axis)$$19.get(2);
        final Direction $$23 = $$6.get($$20) >= 0.0 ? $$20.getPositive() : $$20.getNegative();
        final Direction $$24 = $$6.get($$21) >= 0.0 ? $$21.getPositive() : $$21.getNegative();
        final Direction $$25 = $$6.get($$22) >= 0.0 ? $$22.getPositive() : $$22.getNegative();
        final int $$26 = $$20.choose($$13, $$14, $$15);
        final int $$27 = $$21.choose($$13, $$14, $$15);
        final int $$28 = $$22.choose($$13, $$14, $$15);
        return () -> new AbstractIterator<BlockPos>(){
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int firstIndex;
            private int secondIndex;
            private int thirdIndex;
            private boolean end;
            private final int firstDirX = $$23.getStepX();
            private final int firstDirY = $$23.getStepY();
            private final int firstDirZ = $$23.getStepZ();
            private final int secondDirX = $$24.getStepX();
            private final int secondDirY = $$24.getStepY();
            private final int secondDirZ = $$24.getStepZ();
            private final int thirdDirX = $$25.getStepX();
            private final int thirdDirY = $$25.getStepY();
            private final int thirdDirZ = $$25.getStepZ();

            protected BlockPos computeNext() {
                if (this.end) {
                    return (BlockPos)this.endOfData();
                }
                this.cursor.set($$16 + this.firstDirX * this.firstIndex + this.secondDirX * this.secondIndex + this.thirdDirX * this.thirdIndex, $$17 + this.firstDirY * this.firstIndex + this.secondDirY * this.secondIndex + this.thirdDirY * this.thirdIndex, $$18 + this.firstDirZ * this.firstIndex + this.secondDirZ * this.secondIndex + this.thirdDirZ * this.thirdIndex);
                if (this.thirdIndex < $$28) {
                    ++this.thirdIndex;
                } else if (this.secondIndex < $$27) {
                    ++this.secondIndex;
                    this.thirdIndex = 0;
                } else if (this.firstIndex < $$26) {
                    ++this.firstIndex;
                    this.thirdIndex = 0;
                    this.secondIndex = 0;
                } else {
                    this.end = true;
                }
                return this.cursor;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    @Override
    public /* synthetic */ Vec3i cross(Vec3i vec3i) {
        return this.cross(vec3i);
    }

    @Override
    public /* synthetic */ Vec3i relative(Direction.Axis axis, int n) {
        return this.relative(axis, n);
    }

    @Override
    public /* synthetic */ Vec3i relative(Direction direction, int n) {
        return this.relative(direction, n);
    }

    @Override
    public /* synthetic */ Vec3i relative(Direction direction) {
        return this.relative(direction);
    }

    @Override
    public /* synthetic */ Vec3i east(int n) {
        return this.east(n);
    }

    @Override
    public /* synthetic */ Vec3i east() {
        return this.east();
    }

    @Override
    public /* synthetic */ Vec3i west(int n) {
        return this.west(n);
    }

    @Override
    public /* synthetic */ Vec3i west() {
        return this.west();
    }

    @Override
    public /* synthetic */ Vec3i south(int n) {
        return this.south(n);
    }

    @Override
    public /* synthetic */ Vec3i south() {
        return this.south();
    }

    @Override
    public /* synthetic */ Vec3i north(int n) {
        return this.north(n);
    }

    @Override
    public /* synthetic */ Vec3i north() {
        return this.north();
    }

    @Override
    public /* synthetic */ Vec3i below(int n) {
        return this.below(n);
    }

    @Override
    public /* synthetic */ Vec3i below() {
        return this.below();
    }

    @Override
    public /* synthetic */ Vec3i above(int n) {
        return this.above(n);
    }

    @Override
    public /* synthetic */ Vec3i above() {
        return this.above();
    }

    @Override
    public /* synthetic */ Vec3i multiply(int n) {
        return this.multiply(n);
    }

    @Override
    public /* synthetic */ Vec3i subtract(Vec3i vec3i) {
        return this.subtract(vec3i);
    }

    @Override
    public /* synthetic */ Vec3i offset(Vec3i vec3i) {
        return this.offset(vec3i);
    }

    @Override
    public /* synthetic */ Vec3i offset(int n, int n2, int n3) {
        return this.offset(n, n2, n3);
    }

    public static class MutableBlockPos
    extends BlockPos {
        public MutableBlockPos() {
            this(0, 0, 0);
        }

        public MutableBlockPos(int $$0, int $$1, int $$2) {
            super($$0, $$1, $$2);
        }

        public MutableBlockPos(double $$0, double $$1, double $$2) {
            this(Mth.floor($$0), Mth.floor($$1), Mth.floor($$2));
        }

        @Override
        public BlockPos offset(int $$0, int $$1, int $$2) {
            return super.offset($$0, $$1, $$2).immutable();
        }

        @Override
        public BlockPos multiply(int $$0) {
            return super.multiply($$0).immutable();
        }

        @Override
        public BlockPos relative(Direction $$0, int $$1) {
            return super.relative($$0, $$1).immutable();
        }

        @Override
        public BlockPos relative(Direction.Axis $$0, int $$1) {
            return super.relative($$0, $$1).immutable();
        }

        @Override
        public BlockPos rotate(Rotation $$0) {
            return super.rotate($$0).immutable();
        }

        public MutableBlockPos set(int $$0, int $$1, int $$2) {
            this.setX($$0);
            this.setY($$1);
            this.setZ($$2);
            return this;
        }

        public MutableBlockPos set(double $$0, double $$1, double $$2) {
            return this.set(Mth.floor($$0), Mth.floor($$1), Mth.floor($$2));
        }

        public MutableBlockPos set(Vec3i $$0) {
            return this.set($$0.getX(), $$0.getY(), $$0.getZ());
        }

        public MutableBlockPos set(long $$0) {
            return this.set(MutableBlockPos.getX($$0), MutableBlockPos.getY($$0), MutableBlockPos.getZ($$0));
        }

        public MutableBlockPos set(AxisCycle $$0, int $$1, int $$2, int $$3) {
            return this.set($$0.cycle($$1, $$2, $$3, Direction.Axis.X), $$0.cycle($$1, $$2, $$3, Direction.Axis.Y), $$0.cycle($$1, $$2, $$3, Direction.Axis.Z));
        }

        public MutableBlockPos setWithOffset(Vec3i $$0, Direction $$1) {
            return this.set($$0.getX() + $$1.getStepX(), $$0.getY() + $$1.getStepY(), $$0.getZ() + $$1.getStepZ());
        }

        public MutableBlockPos setWithOffset(Vec3i $$0, int $$1, int $$2, int $$3) {
            return this.set($$0.getX() + $$1, $$0.getY() + $$2, $$0.getZ() + $$3);
        }

        public MutableBlockPos setWithOffset(Vec3i $$0, Vec3i $$1) {
            return this.set($$0.getX() + $$1.getX(), $$0.getY() + $$1.getY(), $$0.getZ() + $$1.getZ());
        }

        public MutableBlockPos move(Direction $$0) {
            return this.move($$0, 1);
        }

        public MutableBlockPos move(Direction $$0, int $$1) {
            return this.set(this.getX() + $$0.getStepX() * $$1, this.getY() + $$0.getStepY() * $$1, this.getZ() + $$0.getStepZ() * $$1);
        }

        public MutableBlockPos move(int $$0, int $$1, int $$2) {
            return this.set(this.getX() + $$0, this.getY() + $$1, this.getZ() + $$2);
        }

        public MutableBlockPos move(Vec3i $$0) {
            return this.set(this.getX() + $$0.getX(), this.getY() + $$0.getY(), this.getZ() + $$0.getZ());
        }

        public MutableBlockPos clamp(Direction.Axis $$0, int $$1, int $$2) {
            return switch ($$0) {
                default -> throw new MatchException(null, null);
                case Direction.Axis.X -> this.set(Mth.clamp(this.getX(), $$1, $$2), this.getY(), this.getZ());
                case Direction.Axis.Y -> this.set(this.getX(), Mth.clamp(this.getY(), $$1, $$2), this.getZ());
                case Direction.Axis.Z -> this.set(this.getX(), this.getY(), Mth.clamp(this.getZ(), $$1, $$2));
            };
        }

        @Override
        public MutableBlockPos setX(int $$0) {
            super.setX($$0);
            return this;
        }

        @Override
        public MutableBlockPos setY(int $$0) {
            super.setY($$0);
            return this;
        }

        @Override
        public MutableBlockPos setZ(int $$0) {
            super.setZ($$0);
            return this;
        }

        @Override
        public BlockPos immutable() {
            return new BlockPos(this);
        }

        @Override
        public /* synthetic */ Vec3i cross(Vec3i vec3i) {
            return super.cross(vec3i);
        }

        @Override
        public /* synthetic */ Vec3i relative(Direction.Axis axis, int n) {
            return this.relative(axis, n);
        }

        @Override
        public /* synthetic */ Vec3i relative(Direction direction, int n) {
            return this.relative(direction, n);
        }

        @Override
        public /* synthetic */ Vec3i relative(Direction direction) {
            return super.relative(direction);
        }

        @Override
        public /* synthetic */ Vec3i east(int n) {
            return super.east(n);
        }

        @Override
        public /* synthetic */ Vec3i east() {
            return super.east();
        }

        @Override
        public /* synthetic */ Vec3i west(int n) {
            return super.west(n);
        }

        @Override
        public /* synthetic */ Vec3i west() {
            return super.west();
        }

        @Override
        public /* synthetic */ Vec3i south(int n) {
            return super.south(n);
        }

        @Override
        public /* synthetic */ Vec3i south() {
            return super.south();
        }

        @Override
        public /* synthetic */ Vec3i north(int n) {
            return super.north(n);
        }

        @Override
        public /* synthetic */ Vec3i north() {
            return super.north();
        }

        @Override
        public /* synthetic */ Vec3i below(int n) {
            return super.below(n);
        }

        @Override
        public /* synthetic */ Vec3i below() {
            return super.below();
        }

        @Override
        public /* synthetic */ Vec3i above(int n) {
            return super.above(n);
        }

        @Override
        public /* synthetic */ Vec3i above() {
            return super.above();
        }

        @Override
        public /* synthetic */ Vec3i multiply(int n) {
            return this.multiply(n);
        }

        @Override
        public /* synthetic */ Vec3i subtract(Vec3i vec3i) {
            return super.subtract(vec3i);
        }

        @Override
        public /* synthetic */ Vec3i offset(Vec3i vec3i) {
            return super.offset(vec3i);
        }

        @Override
        public /* synthetic */ Vec3i offset(int n, int n2, int n3) {
            return this.offset(n, n2, n3);
        }

        @Override
        public /* synthetic */ Vec3i setZ(int n) {
            return this.setZ(n);
        }

        @Override
        public /* synthetic */ Vec3i setY(int n) {
            return this.setY(n);
        }

        @Override
        public /* synthetic */ Vec3i setX(int n) {
            return this.setX(n);
        }
    }

    public static enum TraversalNodeStatus {
        ACCEPT,
        SKIP,
        STOP;

    }
}

