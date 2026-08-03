package net.minecraft.core;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongConsumer;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.entity.EntityAccess;

public class SectionPos extends net.minecraft.core.Vec3i {
   public static final int SECTION_BITS = 4;
   public static final int SECTION_SIZE = 16;
   public static final int SECTION_MASK = 15;
   public static final int SECTION_HALF_SIZE = 8;
   public static final int SECTION_MAX_INDEX = 15;
   private static final int PACKED_X_LENGTH = 22;
   private static final int PACKED_Y_LENGTH = 20;
   private static final int PACKED_Z_LENGTH = 22;
   private static final long PACKED_X_MASK = 4194303L;
   private static final long PACKED_Y_MASK = 1048575L;
   private static final long PACKED_Z_MASK = 4194303L;
   private static final int Y_OFFSET = 0;
   private static final int Z_OFFSET = 20;
   private static final int X_OFFSET = 42;
   private static final int RELATIVE_X_SHIFT = 8;
   private static final int RELATIVE_Y_SHIFT = 0;
   private static final int RELATIVE_Z_SHIFT = 4;
   public static final StreamCodec<ByteBuf, net.minecraft.core.SectionPos> STREAM_CODEC = ByteBufCodecs.LONG
      .map(net.minecraft.core.SectionPos::of, net.minecraft.core.SectionPos::asLong);

   SectionPos(int $$0, int $$1, int $$2) {
      super($$0, $$1, $$2);
   }

   public static net.minecraft.core.SectionPos of(int $$0, int $$1, int $$2) {
      return new net.minecraft.core.SectionPos($$0, $$1, $$2);
   }

   public static net.minecraft.core.SectionPos of(net.minecraft.core.BlockPos $$0) {
      return new net.minecraft.core.SectionPos(blockToSectionCoord($$0.getX()), blockToSectionCoord($$0.getY()), blockToSectionCoord($$0.getZ()));
   }

   public static net.minecraft.core.SectionPos of(ChunkPos $$0, int $$1) {
      return new net.minecraft.core.SectionPos($$0.x, $$1, $$0.z);
   }

   public static net.minecraft.core.SectionPos of(EntityAccess $$0) {
      return of($$0.blockPosition());
   }

   public static net.minecraft.core.SectionPos of(net.minecraft.core.Position $$0) {
      return new net.minecraft.core.SectionPos(blockToSectionCoord($$0.x()), blockToSectionCoord($$0.y()), blockToSectionCoord($$0.z()));
   }

   public static net.minecraft.core.SectionPos of(long $$0) {
      return new net.minecraft.core.SectionPos(x($$0), y($$0), z($$0));
   }

   public static net.minecraft.core.SectionPos bottomOf(ChunkAccess $$0) {
      return of($$0.getPos(), $$0.getMinSectionY());
   }

   public static long offset(long $$0, net.minecraft.core.Direction $$1) {
      return offset($$0, $$1.getStepX(), $$1.getStepY(), $$1.getStepZ());
   }

   public static long offset(long $$0, int $$1, int $$2, int $$3) {
      return asLong(x($$0) + $$1, y($$0) + $$2, z($$0) + $$3);
   }

   public static int posToSectionCoord(double $$0) {
      return blockToSectionCoord(Mth.floor($$0));
   }

   public static int blockToSectionCoord(int $$0) {
      return $$0 >> 4;
   }

   public static int blockToSectionCoord(double $$0) {
      return Mth.floor($$0) >> 4;
   }

   public static int sectionRelative(int $$0) {
      return $$0 & 15;
   }

   public static short sectionRelativePos(net.minecraft.core.BlockPos $$0) {
      int $$1 = sectionRelative($$0.getX());
      int $$2 = sectionRelative($$0.getY());
      int $$3 = sectionRelative($$0.getZ());
      return (short)($$1 << 8 | $$3 << 4 | $$2 << 0);
   }

   public static int sectionRelativeX(short $$0) {
      return $$0 >>> 8 & 15;
   }

   public static int sectionRelativeY(short $$0) {
      return $$0 >>> 0 & 15;
   }

   public static int sectionRelativeZ(short $$0) {
      return $$0 >>> 4 & 15;
   }

   public int relativeToBlockX(short $$0) {
      return this.minBlockX() + sectionRelativeX($$0);
   }

   public int relativeToBlockY(short $$0) {
      return this.minBlockY() + sectionRelativeY($$0);
   }

   public int relativeToBlockZ(short $$0) {
      return this.minBlockZ() + sectionRelativeZ($$0);
   }

   public net.minecraft.core.BlockPos relativeToBlockPos(short $$0) {
      return new net.minecraft.core.BlockPos(this.relativeToBlockX($$0), this.relativeToBlockY($$0), this.relativeToBlockZ($$0));
   }

   public static int sectionToBlockCoord(int $$0) {
      return $$0 << 4;
   }

   public static int sectionToBlockCoord(int $$0, int $$1) {
      return sectionToBlockCoord($$0) + $$1;
   }

   public static int x(long $$0) {
      return (int)($$0 << 0 >> 42);
   }

   public static int y(long $$0) {
      return (int)($$0 << 44 >> 44);
   }

   public static int z(long $$0) {
      return (int)($$0 << 22 >> 42);
   }

   public int x() {
      return this.getX();
   }

   public int y() {
      return this.getY();
   }

   public int z() {
      return this.getZ();
   }

   public int minBlockX() {
      return sectionToBlockCoord(this.x());
   }

   public int minBlockY() {
      return sectionToBlockCoord(this.y());
   }

   public int minBlockZ() {
      return sectionToBlockCoord(this.z());
   }

   public int maxBlockX() {
      return sectionToBlockCoord(this.x(), 15);
   }

   public int maxBlockY() {
      return sectionToBlockCoord(this.y(), 15);
   }

   public int maxBlockZ() {
      return sectionToBlockCoord(this.z(), 15);
   }

   public static long blockToSection(long $$0) {
      return asLong(
         blockToSectionCoord(net.minecraft.core.BlockPos.getX($$0)),
         blockToSectionCoord(net.minecraft.core.BlockPos.getY($$0)),
         blockToSectionCoord(net.minecraft.core.BlockPos.getZ($$0))
      );
   }

   public static long getZeroNode(int $$0, int $$1) {
      return getZeroNode(asLong($$0, 0, $$1));
   }

   public static long getZeroNode(long $$0) {
      return $$0 & -1048576L;
   }

   public static long sectionToChunk(long $$0) {
      return ChunkPos.asLong(x($$0), z($$0));
   }

   public net.minecraft.core.BlockPos origin() {
      return new net.minecraft.core.BlockPos(sectionToBlockCoord(this.x()), sectionToBlockCoord(this.y()), sectionToBlockCoord(this.z()));
   }

   public net.minecraft.core.BlockPos center() {
      int $$0 = 8;
      return this.origin().offset(8, 8, 8);
   }

   public ChunkPos chunk() {
      return new ChunkPos(this.x(), this.z());
   }

   public static long asLong(net.minecraft.core.BlockPos $$0) {
      return asLong(blockToSectionCoord($$0.getX()), blockToSectionCoord($$0.getY()), blockToSectionCoord($$0.getZ()));
   }

   public static long asLong(int $$0, int $$1, int $$2) {
      long $$3 = 0L;
      $$3 |= ($$0 & 4194303L) << 42;
      $$3 |= ($$1 & 1048575L) << 0;
      return $$3 | ($$2 & 4194303L) << 20;
   }

   public long asLong() {
      return asLong(this.x(), this.y(), this.z());
   }

   public net.minecraft.core.SectionPos offset(int $$0, int $$1, int $$2) {
      return $$0 == 0 && $$1 == 0 && $$2 == 0 ? this : new net.minecraft.core.SectionPos(this.x() + $$0, this.y() + $$1, this.z() + $$2);
   }

   public Stream<net.minecraft.core.BlockPos> blocksInside() {
      return net.minecraft.core.BlockPos.betweenClosedStream(
         this.minBlockX(), this.minBlockY(), this.minBlockZ(), this.maxBlockX(), this.maxBlockY(), this.maxBlockZ()
      );
   }

   public static Stream<net.minecraft.core.SectionPos> cube(net.minecraft.core.SectionPos $$0, int $$1) {
      int $$2 = $$0.x();
      int $$3 = $$0.y();
      int $$4 = $$0.z();
      return betweenClosedStream($$2 - $$1, $$3 - $$1, $$4 - $$1, $$2 + $$1, $$3 + $$1, $$4 + $$1);
   }

   public static Stream<net.minecraft.core.SectionPos> aroundChunk(ChunkPos $$0, int $$1, int $$2, int $$3) {
      int $$4 = $$0.x;
      int $$5 = $$0.z;
      return betweenClosedStream($$4 - $$1, $$2, $$5 - $$1, $$4 + $$1, $$3, $$5 + $$1);
   }

   public static Stream<net.minecraft.core.SectionPos> betweenClosedStream(
      final int $$0, final int $$1, final int $$2, final int $$3, final int $$4, final int $$5
   ) {
      return StreamSupport.stream(new AbstractSpliterator<net.minecraft.core.SectionPos>(($$3 - $$0 + 1) * ($$4 - $$1 + 1) * ($$5 - $$2 + 1), 64) {
         final net.minecraft.core.Cursor3D cursor = new net.minecraft.core.Cursor3D($$0, $$1, $$2, $$3, $$4, $$5);

         @Override
         public boolean tryAdvance(Consumer<? super net.minecraft.core.SectionPos> $$0x) {
            if (this.cursor.advance()) {
               $$0.accept(new net.minecraft.core.SectionPos(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
               return true;
            } else {
               return false;
            }
         }
      }, false);
   }

   public static void aroundAndAtBlockPos(net.minecraft.core.BlockPos $$0, LongConsumer $$1) {
      aroundAndAtBlockPos($$0.getX(), $$0.getY(), $$0.getZ(), $$1);
   }

   public static void aroundAndAtBlockPos(long $$0, LongConsumer $$1) {
      aroundAndAtBlockPos(net.minecraft.core.BlockPos.getX($$0), net.minecraft.core.BlockPos.getY($$0), net.minecraft.core.BlockPos.getZ($$0), $$1);
   }

   public static void aroundAndAtBlockPos(int $$0, int $$1, int $$2, LongConsumer $$3) {
      int $$4 = blockToSectionCoord($$0 - 1);
      int $$5 = blockToSectionCoord($$0 + 1);
      int $$6 = blockToSectionCoord($$1 - 1);
      int $$7 = blockToSectionCoord($$1 + 1);
      int $$8 = blockToSectionCoord($$2 - 1);
      int $$9 = blockToSectionCoord($$2 + 1);
      if ($$4 == $$5 && $$6 == $$7 && $$8 == $$9) {
         $$3.accept(asLong($$4, $$6, $$8));
      } else {
         for (int $$10 = $$4; $$10 <= $$5; $$10++) {
            for (int $$11 = $$6; $$11 <= $$7; $$11++) {
               for (int $$12 = $$8; $$12 <= $$9; $$12++) {
                  $$3.accept(asLong($$10, $$11, $$12));
               }
            }
         }
      }
   }
}
