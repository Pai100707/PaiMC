package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class BoundingBox {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<BoundingBox> CODEC = Codec.INT_STREAM
      .comapFlatMap(
         $$0 -> Util.fixedSize($$0, 6).map($$0x -> new BoundingBox($$0x[0], $$0x[1], $$0x[2], $$0x[3], $$0x[4], $$0x[5])),
         $$0 -> IntStream.of($$0.minX, $$0.minY, $$0.minZ, $$0.maxX, $$0.maxY, $$0.maxZ)
      )
      .stable();
   public static final StreamCodec<ByteBuf, BoundingBox> STREAM_CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      $$0 -> new BlockPos($$0.minX, $$0.minY, $$0.minZ),
      BlockPos.STREAM_CODEC,
      $$0 -> new BlockPos($$0.maxX, $$0.maxY, $$0.maxZ),
      ($$0, $$1) -> new BoundingBox($$0.getX(), $$0.getY(), $$0.getZ(), $$1.getX(), $$1.getY(), $$1.getZ())
   );
   private int minX;
   private int minY;
   private int minZ;
   private int maxX;
   private int maxY;
   private int maxZ;

   public BoundingBox(BlockPos $$0) {
      this($$0.getX(), $$0.getY(), $$0.getZ(), $$0.getX(), $$0.getY(), $$0.getZ());
   }

   public BoundingBox(int $$0, int $$1, int $$2, int $$3, int $$4, int $$5) {
      this.minX = $$0;
      this.minY = $$1;
      this.minZ = $$2;
      this.maxX = $$3;
      this.maxY = $$4;
      this.maxZ = $$5;
      if ($$3 < $$0 || $$4 < $$1 || $$5 < $$2) {
         Util.logAndPauseIfInIde("Invalid bounding box data, inverted bounds for: " + this);
         this.minX = Math.min($$0, $$3);
         this.minY = Math.min($$1, $$4);
         this.minZ = Math.min($$2, $$5);
         this.maxX = Math.max($$0, $$3);
         this.maxY = Math.max($$1, $$4);
         this.maxZ = Math.max($$2, $$5);
      }
   }

   public static BoundingBox fromCorners(Vec3i $$0, Vec3i $$1) {
      return new BoundingBox(
         Math.min($$0.getX(), $$1.getX()),
         Math.min($$0.getY(), $$1.getY()),
         Math.min($$0.getZ(), $$1.getZ()),
         Math.max($$0.getX(), $$1.getX()),
         Math.max($$0.getY(), $$1.getY()),
         Math.max($$0.getZ(), $$1.getZ())
      );
   }

   public static BoundingBox infinite() {
      return new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
   }

   public static BoundingBox orientBox(int $$0, int $$1, int $$2, int $$3, int $$4, int $$5, int $$6, int $$7, int $$8, Direction $$9) {
      switch ($$9) {
         case SOUTH:
         default:
            return new BoundingBox($$0 + $$3, $$1 + $$4, $$2 + $$5, $$0 + $$6 - 1 + $$3, $$1 + $$7 - 1 + $$4, $$2 + $$8 - 1 + $$5);
         case NORTH:
            return new BoundingBox($$0 + $$3, $$1 + $$4, $$2 - $$8 + 1 + $$5, $$0 + $$6 - 1 + $$3, $$1 + $$7 - 1 + $$4, $$2 + $$5);
         case WEST:
            return new BoundingBox($$0 - $$8 + 1 + $$5, $$1 + $$4, $$2 + $$3, $$0 + $$5, $$1 + $$7 - 1 + $$4, $$2 + $$6 - 1 + $$3);
         case EAST:
            return new BoundingBox($$0 + $$5, $$1 + $$4, $$2 + $$3, $$0 + $$8 - 1 + $$5, $$1 + $$7 - 1 + $$4, $$2 + $$6 - 1 + $$3);
      }
   }

   public Stream<net.minecraft.world.level.ChunkPos> intersectingChunks() {
      int $$0 = SectionPos.blockToSectionCoord(this.minX());
      int $$1 = SectionPos.blockToSectionCoord(this.minZ());
      int $$2 = SectionPos.blockToSectionCoord(this.maxX());
      int $$3 = SectionPos.blockToSectionCoord(this.maxZ());
      return net.minecraft.world.level.ChunkPos.rangeClosed(new net.minecraft.world.level.ChunkPos($$0, $$1), new net.minecraft.world.level.ChunkPos($$2, $$3));
   }

   public boolean intersects(BoundingBox $$0) {
      return this.maxX >= $$0.minX && this.minX <= $$0.maxX && this.maxZ >= $$0.minZ && this.minZ <= $$0.maxZ && this.maxY >= $$0.minY && this.minY <= $$0.maxY;
   }

   public boolean intersects(int $$0, int $$1, int $$2, int $$3) {
      return this.maxX >= $$0 && this.minX <= $$2 && this.maxZ >= $$1 && this.minZ <= $$3;
   }

   public static Optional<BoundingBox> encapsulatingPositions(Iterable<BlockPos> $$0) {
      Iterator<BlockPos> $$1 = $$0.iterator();
      if (!$$1.hasNext()) {
         return Optional.empty();
      } else {
         BoundingBox $$2 = new BoundingBox($$1.next());
         $$1.forEachRemaining($$2::encapsulate);
         return Optional.of($$2);
      }
   }

   public static Optional<BoundingBox> encapsulatingBoxes(Iterable<BoundingBox> $$0) {
      Iterator<BoundingBox> $$1 = $$0.iterator();
      if (!$$1.hasNext()) {
         return Optional.empty();
      } else {
         BoundingBox $$2 = $$1.next();
         BoundingBox $$3 = new BoundingBox($$2.minX, $$2.minY, $$2.minZ, $$2.maxX, $$2.maxY, $$2.maxZ);
         $$1.forEachRemaining($$3::encapsulate);
         return Optional.of($$3);
      }
   }

   @Deprecated
   public BoundingBox encapsulate(BoundingBox $$0) {
      this.minX = Math.min(this.minX, $$0.minX);
      this.minY = Math.min(this.minY, $$0.minY);
      this.minZ = Math.min(this.minZ, $$0.minZ);
      this.maxX = Math.max(this.maxX, $$0.maxX);
      this.maxY = Math.max(this.maxY, $$0.maxY);
      this.maxZ = Math.max(this.maxZ, $$0.maxZ);
      return this;
   }

   public static BoundingBox encapsulating(BoundingBox $$0, BoundingBox $$1) {
      return new BoundingBox(
         Math.min($$0.minX, $$1.minX),
         Math.min($$0.minY, $$1.minY),
         Math.min($$0.minZ, $$1.minZ),
         Math.max($$0.maxX, $$1.maxX),
         Math.max($$0.maxY, $$1.maxY),
         Math.max($$0.maxZ, $$1.maxZ)
      );
   }

   @Deprecated
   public BoundingBox encapsulate(BlockPos $$0) {
      this.minX = Math.min(this.minX, $$0.getX());
      this.minY = Math.min(this.minY, $$0.getY());
      this.minZ = Math.min(this.minZ, $$0.getZ());
      this.maxX = Math.max(this.maxX, $$0.getX());
      this.maxY = Math.max(this.maxY, $$0.getY());
      this.maxZ = Math.max(this.maxZ, $$0.getZ());
      return this;
   }

   @Deprecated
   public BoundingBox move(int $$0, int $$1, int $$2) {
      this.minX += $$0;
      this.minY += $$1;
      this.minZ += $$2;
      this.maxX += $$0;
      this.maxY += $$1;
      this.maxZ += $$2;
      return this;
   }

   @Deprecated
   public BoundingBox move(Vec3i $$0) {
      return this.move($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public BoundingBox moved(int $$0, int $$1, int $$2) {
      return new BoundingBox(this.minX + $$0, this.minY + $$1, this.minZ + $$2, this.maxX + $$0, this.maxY + $$1, this.maxZ + $$2);
   }

   public BoundingBox inflatedBy(int $$0) {
      return this.inflatedBy($$0, $$0, $$0);
   }

   public BoundingBox inflatedBy(int $$0, int $$1, int $$2) {
      return new BoundingBox(this.minX() - $$0, this.minY() - $$1, this.minZ() - $$2, this.maxX() + $$0, this.maxY() + $$1, this.maxZ() + $$2);
   }

   public boolean isInside(Vec3i $$0) {
      return this.isInside($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public boolean isInside(int $$0, int $$1, int $$2) {
      return $$0 >= this.minX && $$0 <= this.maxX && $$2 >= this.minZ && $$2 <= this.maxZ && $$1 >= this.minY && $$1 <= this.maxY;
   }

   public Vec3i getLength() {
      return new Vec3i(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ);
   }

   public int getXSpan() {
      return this.maxX - this.minX + 1;
   }

   public int getYSpan() {
      return this.maxY - this.minY + 1;
   }

   public int getZSpan() {
      return this.maxZ - this.minZ + 1;
   }

   public BlockPos getCenter() {
      return new BlockPos(this.minX + (this.maxX - this.minX + 1) / 2, this.minY + (this.maxY - this.minY + 1) / 2, this.minZ + (this.maxZ - this.minZ + 1) / 2);
   }

   public void forAllCorners(Consumer<BlockPos> $$0) {
      MutableBlockPos $$1 = new MutableBlockPos();
      $$0.accept($$1.set(this.maxX, this.maxY, this.maxZ));
      $$0.accept($$1.set(this.minX, this.maxY, this.maxZ));
      $$0.accept($$1.set(this.maxX, this.minY, this.maxZ));
      $$0.accept($$1.set(this.minX, this.minY, this.maxZ));
      $$0.accept($$1.set(this.maxX, this.maxY, this.minZ));
      $$0.accept($$1.set(this.minX, this.maxY, this.minZ));
      $$0.accept($$1.set(this.maxX, this.minY, this.minZ));
      $$0.accept($$1.set(this.minX, this.minY, this.minZ));
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this)
         .add("minX", this.minX)
         .add("minY", this.minY)
         .add("minZ", this.minZ)
         .add("maxX", this.maxX)
         .add("maxY", this.maxY)
         .add("maxZ", this.maxZ)
         .toString();
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return !($$0 instanceof BoundingBox $$1)
            ? false
            : this.minX == $$1.minX
               && this.minY == $$1.minY
               && this.minZ == $$1.minZ
               && this.maxX == $$1.maxX
               && this.maxY == $$1.maxY
               && this.maxZ == $$1.maxZ;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }

   public int minX() {
      return this.minX;
   }

   public int minY() {
      return this.minY;
   }

   public int minZ() {
      return this.minZ;
   }

   public int maxX() {
      return this.maxX;
   }

   public int maxY() {
      return this.maxY;
   }

   public int maxZ() {
      return this.maxZ;
   }
}
