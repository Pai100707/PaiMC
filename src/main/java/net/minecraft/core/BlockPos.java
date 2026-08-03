package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.Immutable;
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
public class BlockPos extends net.minecraft.core.Vec3i {
   public static final Codec<net.minecraft.core.BlockPos> CODEC = Codec.INT_STREAM
      .comapFlatMap(
         $$0 -> Util.fixedSize($$0, 3).map($$0x -> new net.minecraft.core.BlockPos($$0x[0], $$0x[1], $$0x[2])),
         $$0 -> IntStream.of($$0.getX(), $$0.getY(), $$0.getZ())
      )
      .stable();
   public static final StreamCodec<ByteBuf, net.minecraft.core.BlockPos> STREAM_CODEC = new StreamCodec<ByteBuf, net.minecraft.core.BlockPos>() {
      public net.minecraft.core.BlockPos decode(ByteBuf $$0) {
         return FriendlyByteBuf.readBlockPos($$0);
      }

      public void encode(ByteBuf $$0, net.minecraft.core.BlockPos $$1) {
         FriendlyByteBuf.writeBlockPos($$0, $$1);
      }
   };
   public static final net.minecraft.core.BlockPos ZERO = new net.minecraft.core.BlockPos(0, 0, 0);
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

   public BlockPos(net.minecraft.core.Vec3i $$0) {
      this($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public static long offset(long $$0, net.minecraft.core.Direction $$1) {
      return offset($$0, $$1.getStepX(), $$1.getStepY(), $$1.getStepZ());
   }

   public static long offset(long $$0, int $$1, int $$2, int $$3) {
      return asLong(getX($$0) + $$1, getY($$0) + $$2, getZ($$0) + $$3);
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

   public static net.minecraft.core.BlockPos of(long $$0) {
      return new net.minecraft.core.BlockPos(getX($$0), getY($$0), getZ($$0));
   }

   public static net.minecraft.core.BlockPos containing(double $$0, double $$1, double $$2) {
      return new net.minecraft.core.BlockPos(Mth.floor($$0), Mth.floor($$1), Mth.floor($$2));
   }

   public static net.minecraft.core.BlockPos containing(net.minecraft.core.Position $$0) {
      return containing($$0.x(), $$0.y(), $$0.z());
   }

   public static net.minecraft.core.BlockPos min(net.minecraft.core.BlockPos $$0, net.minecraft.core.BlockPos $$1) {
      return new net.minecraft.core.BlockPos(Math.min($$0.getX(), $$1.getX()), Math.min($$0.getY(), $$1.getY()), Math.min($$0.getZ(), $$1.getZ()));
   }

   public static net.minecraft.core.BlockPos max(net.minecraft.core.BlockPos $$0, net.minecraft.core.BlockPos $$1) {
      return new net.minecraft.core.BlockPos(Math.max($$0.getX(), $$1.getX()), Math.max($$0.getY(), $$1.getY()), Math.max($$0.getZ(), $$1.getZ()));
   }

   public long asLong() {
      return asLong(this.getX(), this.getY(), this.getZ());
   }

   public static long asLong(int $$0, int $$1, int $$2) {
      long $$3 = 0L;
      $$3 |= ($$0 & PACKED_X_MASK) << X_OFFSET;
      $$3 |= ($$1 & PACKED_Y_MASK) << 0;
      return $$3 | ($$2 & PACKED_Z_MASK) << Z_OFFSET;
   }

   public static long getFlatIndex(long $$0) {
      return $$0 & -16L;
   }

   public net.minecraft.core.BlockPos offset(int $$0, int $$1, int $$2) {
      return $$0 == 0 && $$1 == 0 && $$2 == 0 ? this : new net.minecraft.core.BlockPos(this.getX() + $$0, this.getY() + $$1, this.getZ() + $$2);
   }

   public Vec3 getCenter() {
      return Vec3.atCenterOf(this);
   }

   public Vec3 getBottomCenter() {
      return Vec3.atBottomCenterOf(this);
   }

   public net.minecraft.core.BlockPos offset(net.minecraft.core.Vec3i $$0) {
      return this.offset($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public net.minecraft.core.BlockPos subtract(net.minecraft.core.Vec3i $$0) {
      return this.offset(-$$0.getX(), -$$0.getY(), -$$0.getZ());
   }

   public net.minecraft.core.BlockPos multiply(int $$0) {
      if ($$0 == 1) {
         return this;
      } else {
         return $$0 == 0 ? ZERO : new net.minecraft.core.BlockPos(this.getX() * $$0, this.getY() * $$0, this.getZ() * $$0);
      }
   }

   public net.minecraft.core.BlockPos above() {
      return this.relative(net.minecraft.core.Direction.UP);
   }

   public net.minecraft.core.BlockPos above(int $$0) {
      return this.relative(net.minecraft.core.Direction.UP, $$0);
   }

   public net.minecraft.core.BlockPos below() {
      return this.relative(net.minecraft.core.Direction.DOWN);
   }

   public net.minecraft.core.BlockPos below(int $$0) {
      return this.relative(net.minecraft.core.Direction.DOWN, $$0);
   }

   public net.minecraft.core.BlockPos north() {
      return this.relative(net.minecraft.core.Direction.NORTH);
   }

   public net.minecraft.core.BlockPos north(int $$0) {
      return this.relative(net.minecraft.core.Direction.NORTH, $$0);
   }

   public net.minecraft.core.BlockPos south() {
      return this.relative(net.minecraft.core.Direction.SOUTH);
   }

   public net.minecraft.core.BlockPos south(int $$0) {
      return this.relative(net.minecraft.core.Direction.SOUTH, $$0);
   }

   public net.minecraft.core.BlockPos west() {
      return this.relative(net.minecraft.core.Direction.WEST);
   }

   public net.minecraft.core.BlockPos west(int $$0) {
      return this.relative(net.minecraft.core.Direction.WEST, $$0);
   }

   public net.minecraft.core.BlockPos east() {
      return this.relative(net.minecraft.core.Direction.EAST);
   }

   public net.minecraft.core.BlockPos east(int $$0) {
      return this.relative(net.minecraft.core.Direction.EAST, $$0);
   }

   public net.minecraft.core.BlockPos relative(net.minecraft.core.Direction $$0) {
      return new net.minecraft.core.BlockPos(this.getX() + $$0.getStepX(), this.getY() + $$0.getStepY(), this.getZ() + $$0.getStepZ());
   }

   public net.minecraft.core.BlockPos relative(net.minecraft.core.Direction $$0, int $$1) {
      return $$1 == 0
         ? this
         : new net.minecraft.core.BlockPos(this.getX() + $$0.getStepX() * $$1, this.getY() + $$0.getStepY() * $$1, this.getZ() + $$0.getStepZ() * $$1);
   }

   public net.minecraft.core.BlockPos relative(net.minecraft.core.Direction.Axis $$0, int $$1) {
      if ($$1 == 0) {
         return this;
      } else {
         int $$2 = $$0 == net.minecraft.core.Direction.Axis.X ? $$1 : 0;
         int $$3 = $$0 == net.minecraft.core.Direction.Axis.Y ? $$1 : 0;
         int $$4 = $$0 == net.minecraft.core.Direction.Axis.Z ? $$1 : 0;
         return new net.minecraft.core.BlockPos(this.getX() + $$2, this.getY() + $$3, this.getZ() + $$4);
      }
   }

   public net.minecraft.core.BlockPos rotate(Rotation $$0) {
      return switch ($$0) {
         case CLOCKWISE_90 -> new net.minecraft.core.BlockPos(-this.getZ(), this.getY(), this.getX());
         case CLOCKWISE_180 -> new net.minecraft.core.BlockPos(-this.getX(), this.getY(), -this.getZ());
         case COUNTERCLOCKWISE_90 -> new net.minecraft.core.BlockPos(this.getZ(), this.getY(), -this.getX());
         case NONE -> this;
         default -> throw new MatchException(null, null);
      };
   }

   public net.minecraft.core.BlockPos cross(net.minecraft.core.Vec3i $$0) {
      return new net.minecraft.core.BlockPos(
         this.getY() * $$0.getZ() - this.getZ() * $$0.getY(),
         this.getZ() * $$0.getX() - this.getX() * $$0.getZ(),
         this.getX() * $$0.getY() - this.getY() * $$0.getX()
      );
   }

   public net.minecraft.core.BlockPos atY(int $$0) {
      return new net.minecraft.core.BlockPos(this.getX(), $$0, this.getZ());
   }

   public net.minecraft.core.BlockPos immutable() {
      return this;
   }

   public net.minecraft.core.BlockPos.MutableBlockPos mutable() {
      return new net.minecraft.core.BlockPos.MutableBlockPos(this.getX(), this.getY(), this.getZ());
   }

   public Vec3 clampLocationWithin(Vec3 $$0) {
      return new Vec3(
         Mth.clamp($$0.x, this.getX() + 1.0E-5F, this.getX() + 1.0 - 1.0E-5F),
         Mth.clamp($$0.y, this.getY() + 1.0E-5F, this.getY() + 1.0 - 1.0E-5F),
         Mth.clamp($$0.z, this.getZ() + 1.0E-5F, this.getZ() + 1.0 - 1.0E-5F)
      );
   }

   public static Iterable<net.minecraft.core.BlockPos> randomInCube(RandomSource $$0, int $$1, net.minecraft.core.BlockPos $$2, int $$3) {
      return randomBetweenClosed($$0, $$1, $$2.getX() - $$3, $$2.getY() - $$3, $$2.getZ() - $$3, $$2.getX() + $$3, $$2.getY() + $$3, $$2.getZ() + $$3);
   }

   @Deprecated
   public static Stream<net.minecraft.core.BlockPos> squareOutSouthEast(net.minecraft.core.BlockPos $$0) {
      return Stream.of($$0, $$0.south(), $$0.east(), $$0.south().east());
   }

   public static Iterable<net.minecraft.core.BlockPos> randomBetweenClosed(RandomSource $$0, int $$1, int $$2, int $$3, int $$4, int $$5, int $$6, int $$7) {
      int $$8 = $$5 - $$2 + 1;
      int $$9 = $$6 - $$3 + 1;
      int $$10 = $$7 - $$4 + 1;
      return () -> new AbstractIterator<net.minecraft.core.BlockPos>() {
         final net.minecraft.core.BlockPos.MutableBlockPos nextPos = new net.minecraft.core.BlockPos.MutableBlockPos();
         int counter = $$1;

         protected net.minecraft.core.BlockPos computeNext() {
            if (this.counter <= 0) {
               return (net.minecraft.core.BlockPos)this.endOfData();
            } else {
               net.minecraft.core.BlockPos $$0x = this.nextPos.set($$2 + $$0.nextInt($$8), $$3 + $$0.nextInt($$9), $$4 + $$0.nextInt($$10));
               this.counter--;
               return $$0x;
            }
         }
      };
   }

   public static Iterable<net.minecraft.core.BlockPos> withinManhattan(net.minecraft.core.BlockPos $$0, int $$1, int $$2, int $$3) {
      int $$4 = $$1 + $$2 + $$3;
      int $$5 = $$0.getX();
      int $$6 = $$0.getY();
      int $$7 = $$0.getZ();
      return () -> new AbstractIterator<net.minecraft.core.BlockPos>() {
         private final net.minecraft.core.BlockPos.MutableBlockPos cursor = new net.minecraft.core.BlockPos.MutableBlockPos();
         private int currentDepth;
         private int maxX;
         private int maxY;
         private int x;
         private int y;
         private boolean zMirror;

         protected net.minecraft.core.BlockPos computeNext() {
            if (this.zMirror) {
               this.zMirror = false;
               this.cursor.setZ($$7 - (this.cursor.getZ() - $$7));
               return this.cursor;
            } else {
               net.minecraft.core.BlockPos $$0x;
               for ($$0x = null; $$0x == null; this.y++) {
                  if (this.y > this.maxY) {
                     this.x++;
                     if (this.x > this.maxX) {
                        this.currentDepth++;
                        if (this.currentDepth > $$4) {
                           return (net.minecraft.core.BlockPos)this.endOfData();
                        }

                        this.maxX = Math.min($$1, this.currentDepth);
                        this.x = -this.maxX;
                     }

                     this.maxY = Math.min($$2, this.currentDepth - Math.abs(this.x));
                     this.y = -this.maxY;
                  }

                  int $$1x = this.x;
                  int $$2x = this.y;
                  int $$3x = this.currentDepth - Math.abs($$1x) - Math.abs($$2x);
                  if ($$3x <= $$3) {
                     this.zMirror = $$3x != 0;
                     $$0x = this.cursor.set($$5 + $$1x, $$6 + $$2x, $$7 + $$3x);
                  }
               }

               return $$0x;
            }
         }
      };
   }

   public static Optional<net.minecraft.core.BlockPos> findClosestMatch(
      net.minecraft.core.BlockPos $$0, int $$1, int $$2, Predicate<net.minecraft.core.BlockPos> $$3
   ) {
      for (net.minecraft.core.BlockPos $$4 : withinManhattan($$0, $$1, $$2, $$1)) {
         if ($$3.test($$4)) {
            return Optional.of($$4);
         }
      }

      return Optional.empty();
   }

   public static Stream<net.minecraft.core.BlockPos> withinManhattanStream(net.minecraft.core.BlockPos $$0, int $$1, int $$2, int $$3) {
      return StreamSupport.stream(withinManhattan($$0, $$1, $$2, $$3).spliterator(), false);
   }

   public static Iterable<net.minecraft.core.BlockPos> betweenClosed(AABB $$0) {
      net.minecraft.core.BlockPos $$1 = containing($$0.minX, $$0.minY, $$0.minZ);
      net.minecraft.core.BlockPos $$2 = containing($$0.maxX, $$0.maxY, $$0.maxZ);
      return betweenClosed($$1, $$2);
   }

   public static Iterable<net.minecraft.core.BlockPos> betweenClosed(net.minecraft.core.BlockPos $$0, net.minecraft.core.BlockPos $$1) {
      return betweenClosed(
         Math.min($$0.getX(), $$1.getX()),
         Math.min($$0.getY(), $$1.getY()),
         Math.min($$0.getZ(), $$1.getZ()),
         Math.max($$0.getX(), $$1.getX()),
         Math.max($$0.getY(), $$1.getY()),
         Math.max($$0.getZ(), $$1.getZ())
      );
   }

   public static Stream<net.minecraft.core.BlockPos> betweenClosedStream(net.minecraft.core.BlockPos $$0, net.minecraft.core.BlockPos $$1) {
      return StreamSupport.stream(betweenClosed($$0, $$1).spliterator(), false);
   }

   public static Stream<net.minecraft.core.BlockPos> betweenClosedStream(BoundingBox $$0) {
      return betweenClosedStream(
         Math.min($$0.minX(), $$0.maxX()),
         Math.min($$0.minY(), $$0.maxY()),
         Math.min($$0.minZ(), $$0.maxZ()),
         Math.max($$0.minX(), $$0.maxX()),
         Math.max($$0.minY(), $$0.maxY()),
         Math.max($$0.minZ(), $$0.maxZ())
      );
   }

   public static Stream<net.minecraft.core.BlockPos> betweenClosedStream(AABB $$0) {
      return betweenClosedStream(Mth.floor($$0.minX), Mth.floor($$0.minY), Mth.floor($$0.minZ), Mth.floor($$0.maxX), Mth.floor($$0.maxY), Mth.floor($$0.maxZ));
   }

   public static Stream<net.minecraft.core.BlockPos> betweenClosedStream(int $$0, int $$1, int $$2, int $$3, int $$4, int $$5) {
      return StreamSupport.stream(betweenClosed($$0, $$1, $$2, $$3, $$4, $$5).spliterator(), false);
   }

   public static Iterable<net.minecraft.core.BlockPos> betweenClosed(int $$0, int $$1, int $$2, int $$3, int $$4, int $$5) {
      int $$6 = $$3 - $$0 + 1;
      int $$7 = $$4 - $$1 + 1;
      int $$8 = $$5 - $$2 + 1;
      int $$9 = $$6 * $$7 * $$8;
      return () -> new AbstractIterator<net.minecraft.core.BlockPos>() {
         private final net.minecraft.core.BlockPos.MutableBlockPos cursor = new net.minecraft.core.BlockPos.MutableBlockPos();
         private int index;

         protected net.minecraft.core.BlockPos computeNext() {
            if (this.index == $$9) {
               return (net.minecraft.core.BlockPos)this.endOfData();
            } else {
               int $$0x = this.index % $$6;
               int $$1x = this.index / $$6;
               int $$2x = $$1x % $$7;
               int $$3x = $$1x / $$7;
               this.index++;
               return this.cursor.set($$0 + $$0x, $$1 + $$2x, $$2 + $$3x);
            }
         }
      };
   }

   public static Iterable<net.minecraft.core.BlockPos.MutableBlockPos> spiralAround(
      net.minecraft.core.BlockPos $$0, int $$1, net.minecraft.core.Direction $$2, net.minecraft.core.Direction $$3
   ) {
      Validate.validState($$2.getAxis() != $$3.getAxis(), "The two directions cannot be on the same axis", new Object[0]);
      return () -> new AbstractIterator<net.minecraft.core.BlockPos.MutableBlockPos>() {
         private final net.minecraft.core.Direction[] directions = new net.minecraft.core.Direction[]{$$2, $$3, $$2.getOpposite(), $$3.getOpposite()};
         private final net.minecraft.core.BlockPos.MutableBlockPos cursor = $$0.mutable().move($$3);
         private final int legs = 4 * $$1;
         private int leg = -1;
         private int legSize;
         private int legIndex;
         private int lastX = this.cursor.getX();
         private int lastY = this.cursor.getY();
         private int lastZ = this.cursor.getZ();

         protected net.minecraft.core.BlockPos.MutableBlockPos computeNext() {
            this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
            this.lastX = this.cursor.getX();
            this.lastY = this.cursor.getY();
            this.lastZ = this.cursor.getZ();
            if (this.legIndex >= this.legSize) {
               if (this.leg >= this.legs) {
                  return (net.minecraft.core.BlockPos.MutableBlockPos)this.endOfData();
               }

               this.leg++;
               this.legIndex = 0;
               this.legSize = this.leg / 2 + 1;
            }

            this.legIndex++;
            return this.cursor;
         }
      };
   }

   public static int breadthFirstTraversal(
      net.minecraft.core.BlockPos $$0,
      int $$1,
      int $$2,
      BiConsumer<net.minecraft.core.BlockPos, Consumer<net.minecraft.core.BlockPos>> $$3,
      Function<net.minecraft.core.BlockPos, net.minecraft.core.BlockPos.TraversalNodeStatus> $$4
   ) {
      Queue<Pair<net.minecraft.core.BlockPos, Integer>> $$5 = new ArrayDeque<>();
      LongSet $$6 = new LongOpenHashSet();
      $$5.add(Pair.of($$0, 0));
      int $$7 = 0;

      while (!$$5.isEmpty()) {
         Pair<net.minecraft.core.BlockPos, Integer> $$8 = $$5.poll();
         net.minecraft.core.BlockPos $$9 = (net.minecraft.core.BlockPos)$$8.getLeft();
         int $$10 = (Integer)$$8.getRight();
         long $$11 = $$9.asLong();
         if ($$6.add($$11)) {
            net.minecraft.core.BlockPos.TraversalNodeStatus $$12 = $$4.apply($$9);
            if ($$12 != net.minecraft.core.BlockPos.TraversalNodeStatus.SKIP) {
               if ($$12 == net.minecraft.core.BlockPos.TraversalNodeStatus.STOP) {
                  break;
               }

               if (++$$7 >= $$2) {
                  return $$7;
               }

               if ($$10 < $$1) {
                  $$3.accept($$9, $$2x -> $$5.add(Pair.of($$2x, $$10 + 1)));
               }
            }
         }
      }

      return $$7;
   }

   public static Iterable<net.minecraft.core.BlockPos> betweenCornersInDirection(AABB $$0, Vec3 $$1) {
      Vec3 $$2 = $$0.getMinPosition();
      int $$3 = Mth.floor($$2.x());
      int $$4 = Mth.floor($$2.y());
      int $$5 = Mth.floor($$2.z());
      Vec3 $$6 = $$0.getMaxPosition();
      int $$7 = Mth.floor($$6.x());
      int $$8 = Mth.floor($$6.y());
      int $$9 = Mth.floor($$6.z());
      return betweenCornersInDirection($$3, $$4, $$5, $$7, $$8, $$9, $$1);
   }

   public static Iterable<net.minecraft.core.BlockPos> betweenCornersInDirection(net.minecraft.core.BlockPos $$0, net.minecraft.core.BlockPos $$1, Vec3 $$2) {
      return betweenCornersInDirection($$0.getX(), $$0.getY(), $$0.getZ(), $$1.getX(), $$1.getY(), $$1.getZ(), $$2);
   }

   public static Iterable<net.minecraft.core.BlockPos> betweenCornersInDirection(int $$0, int $$1, int $$2, int $$3, int $$4, int $$5, Vec3 $$6) {
      int $$7 = Math.min($$0, $$3);
      int $$8 = Math.min($$1, $$4);
      int $$9 = Math.min($$2, $$5);
      int $$10 = Math.max($$0, $$3);
      int $$11 = Math.max($$1, $$4);
      int $$12 = Math.max($$2, $$5);
      int $$13 = $$10 - $$7;
      int $$14 = $$11 - $$8;
      int $$15 = $$12 - $$9;
      int $$16 = $$6.x >= 0.0 ? $$7 : $$10;
      int $$17 = $$6.y >= 0.0 ? $$8 : $$11;
      int $$18 = $$6.z >= 0.0 ? $$9 : $$12;
      List<net.minecraft.core.Direction.Axis> $$19 = net.minecraft.core.Direction.axisStepOrder($$6);
      net.minecraft.core.Direction.Axis $$20 = $$19.get(0);
      net.minecraft.core.Direction.Axis $$21 = $$19.get(1);
      net.minecraft.core.Direction.Axis $$22 = $$19.get(2);
      net.minecraft.core.Direction $$23 = $$6.get($$20) >= 0.0 ? $$20.getPositive() : $$20.getNegative();
      net.minecraft.core.Direction $$24 = $$6.get($$21) >= 0.0 ? $$21.getPositive() : $$21.getNegative();
      net.minecraft.core.Direction $$25 = $$6.get($$22) >= 0.0 ? $$22.getPositive() : $$22.getNegative();
      int $$26 = $$20.choose($$13, $$14, $$15);
      int $$27 = $$21.choose($$13, $$14, $$15);
      int $$28 = $$22.choose($$13, $$14, $$15);
      return () -> new AbstractIterator<net.minecraft.core.BlockPos>() {
         private final net.minecraft.core.BlockPos.MutableBlockPos cursor = new net.minecraft.core.BlockPos.MutableBlockPos();
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

         protected net.minecraft.core.BlockPos computeNext() {
            if (this.end) {
               return (net.minecraft.core.BlockPos)this.endOfData();
            } else {
               this.cursor
                  .set(
                     $$16 + this.firstDirX * this.firstIndex + this.secondDirX * this.secondIndex + this.thirdDirX * this.thirdIndex,
                     $$17 + this.firstDirY * this.firstIndex + this.secondDirY * this.secondIndex + this.thirdDirY * this.thirdIndex,
                     $$18 + this.firstDirZ * this.firstIndex + this.secondDirZ * this.secondIndex + this.thirdDirZ * this.thirdIndex
                  );
               if (this.thirdIndex < $$28) {
                  this.thirdIndex++;
               } else if (this.secondIndex < $$27) {
                  this.secondIndex++;
                  this.thirdIndex = 0;
               } else if (this.firstIndex < $$26) {
                  this.firstIndex++;
                  this.thirdIndex = 0;
                  this.secondIndex = 0;
               } else {
                  this.end = true;
               }

               return this.cursor;
            }
         }
      };
   }

   public static class MutableBlockPos extends net.minecraft.core.BlockPos {
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
      public net.minecraft.core.BlockPos offset(int $$0, int $$1, int $$2) {
         return super.offset($$0, $$1, $$2).immutable();
      }

      @Override
      public net.minecraft.core.BlockPos multiply(int $$0) {
         return super.multiply($$0).immutable();
      }

      @Override
      public net.minecraft.core.BlockPos relative(net.minecraft.core.Direction $$0, int $$1) {
         return super.relative($$0, $$1).immutable();
      }

      @Override
      public net.minecraft.core.BlockPos relative(net.minecraft.core.Direction.Axis $$0, int $$1) {
         return super.relative($$0, $$1).immutable();
      }

      @Override
      public net.minecraft.core.BlockPos rotate(Rotation $$0) {
         return super.rotate($$0).immutable();
      }

      public net.minecraft.core.BlockPos.MutableBlockPos set(int $$0, int $$1, int $$2) {
         this.setX($$0);
         this.setY($$1);
         this.setZ($$2);
         return this;
      }

      public net.minecraft.core.BlockPos.MutableBlockPos set(double $$0, double $$1, double $$2) {
         return this.set(Mth.floor($$0), Mth.floor($$1), Mth.floor($$2));
      }

      public net.minecraft.core.BlockPos.MutableBlockPos set(net.minecraft.core.Vec3i $$0) {
         return this.set($$0.getX(), $$0.getY(), $$0.getZ());
      }

      public net.minecraft.core.BlockPos.MutableBlockPos set(long $$0) {
         return this.set(getX($$0), getY($$0), getZ($$0));
      }

      public net.minecraft.core.BlockPos.MutableBlockPos set(net.minecraft.core.AxisCycle $$0, int $$1, int $$2, int $$3) {
         return this.set(
            $$0.cycle($$1, $$2, $$3, net.minecraft.core.Direction.Axis.X),
            $$0.cycle($$1, $$2, $$3, net.minecraft.core.Direction.Axis.Y),
            $$0.cycle($$1, $$2, $$3, net.minecraft.core.Direction.Axis.Z)
         );
      }

      public net.minecraft.core.BlockPos.MutableBlockPos setWithOffset(net.minecraft.core.Vec3i $$0, net.minecraft.core.Direction $$1) {
         return this.set($$0.getX() + $$1.getStepX(), $$0.getY() + $$1.getStepY(), $$0.getZ() + $$1.getStepZ());
      }

      public net.minecraft.core.BlockPos.MutableBlockPos setWithOffset(net.minecraft.core.Vec3i $$0, int $$1, int $$2, int $$3) {
         return this.set($$0.getX() + $$1, $$0.getY() + $$2, $$0.getZ() + $$3);
      }

      public net.minecraft.core.BlockPos.MutableBlockPos setWithOffset(net.minecraft.core.Vec3i $$0, net.minecraft.core.Vec3i $$1) {
         return this.set($$0.getX() + $$1.getX(), $$0.getY() + $$1.getY(), $$0.getZ() + $$1.getZ());
      }

      public net.minecraft.core.BlockPos.MutableBlockPos move(net.minecraft.core.Direction $$0) {
         return this.move($$0, 1);
      }

      public net.minecraft.core.BlockPos.MutableBlockPos move(net.minecraft.core.Direction $$0, int $$1) {
         return this.set(this.getX() + $$0.getStepX() * $$1, this.getY() + $$0.getStepY() * $$1, this.getZ() + $$0.getStepZ() * $$1);
      }

      public net.minecraft.core.BlockPos.MutableBlockPos move(int $$0, int $$1, int $$2) {
         return this.set(this.getX() + $$0, this.getY() + $$1, this.getZ() + $$2);
      }

      public net.minecraft.core.BlockPos.MutableBlockPos move(net.minecraft.core.Vec3i $$0) {
         return this.set(this.getX() + $$0.getX(), this.getY() + $$0.getY(), this.getZ() + $$0.getZ());
      }

      public net.minecraft.core.BlockPos.MutableBlockPos clamp(net.minecraft.core.Direction.Axis $$0, int $$1, int $$2) {
         return switch ($$0) {
            case X -> this.set(Mth.clamp(this.getX(), $$1, $$2), this.getY(), this.getZ());
            case Y -> this.set(this.getX(), Mth.clamp(this.getY(), $$1, $$2), this.getZ());
            case Z -> this.set(this.getX(), this.getY(), Mth.clamp(this.getZ(), $$1, $$2));
         };
      }

      public net.minecraft.core.BlockPos.MutableBlockPos setX(int $$0) {
         super.setX($$0);
         return this;
      }

      public net.minecraft.core.BlockPos.MutableBlockPos setY(int $$0) {
         super.setY($$0);
         return this;
      }

      public net.minecraft.core.BlockPos.MutableBlockPos setZ(int $$0) {
         super.setZ($$0);
         return this;
      }

      @Override
      public net.minecraft.core.BlockPos immutable() {
         return new net.minecraft.core.BlockPos(this);
      }
   }

   public static enum TraversalNodeStatus {
      ACCEPT,
      SKIP,
      STOP;
   }
}
