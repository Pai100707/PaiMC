package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkPos {
   public static final Codec<net.minecraft.world.level.ChunkPos> CODEC = Codec.INT_STREAM
      .comapFlatMap($$0 -> Util.fixedSize($$0, 2).map($$0x -> new net.minecraft.world.level.ChunkPos($$0x[0], $$0x[1])), $$0 -> IntStream.of($$0.x, $$0.z))
      .stable();
   public static final StreamCodec<ByteBuf, net.minecraft.world.level.ChunkPos> STREAM_CODEC = new StreamCodec<ByteBuf, net.minecraft.world.level.ChunkPos>() {
      public net.minecraft.world.level.ChunkPos decode(ByteBuf $$0) {
         return FriendlyByteBuf.readChunkPos($$0);
      }

      public void encode(ByteBuf $$0, net.minecraft.world.level.ChunkPos $$1) {
         FriendlyByteBuf.writeChunkPos($$0, $$1);
      }
   };
   private static final int SAFETY_MARGIN = 1056;
   public static final long INVALID_CHUNK_POS = asLong(1875066, 1875066);
   private static final int SAFETY_MARGIN_CHUNKS = (32 + ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FULL).accumulatedDependencies().size() + 1) * 2;
   public static final int MAX_COORDINATE_VALUE = SectionPos.blockToSectionCoord(BlockPos.MAX_HORIZONTAL_COORDINATE) - SAFETY_MARGIN_CHUNKS;
   public static final net.minecraft.world.level.ChunkPos ZERO = new net.minecraft.world.level.ChunkPos(0, 0);
   private static final long COORD_BITS = 32L;
   private static final long COORD_MASK = 4294967295L;
   private static final int REGION_BITS = 5;
   public static final int REGION_SIZE = 32;
   private static final int REGION_MASK = 31;
   public static final int REGION_MAX_INDEX = 31;
   public final int x;
   public final int z;
   private static final int HASH_A = 1664525;
   private static final int HASH_C = 1013904223;
   private static final int HASH_Z_XOR = -559038737;

   public ChunkPos(int $$0, int $$1) {
      this.x = $$0;
      this.z = $$1;
   }

   public ChunkPos(BlockPos $$0) {
      this.x = SectionPos.blockToSectionCoord($$0.getX());
      this.z = SectionPos.blockToSectionCoord($$0.getZ());
   }

   public ChunkPos(long $$0) {
      this.x = (int)$$0;
      this.z = (int)($$0 >> 32);
   }

   public static net.minecraft.world.level.ChunkPos minFromRegion(int $$0, int $$1) {
      return new net.minecraft.world.level.ChunkPos($$0 << 5, $$1 << 5);
   }

   public static net.minecraft.world.level.ChunkPos maxFromRegion(int $$0, int $$1) {
      return new net.minecraft.world.level.ChunkPos(($$0 << 5) + 31, ($$1 << 5) + 31);
   }

   public boolean isValid() {
      return isValid(this.x, this.z);
   }

   public static boolean isValid(int $$0, int $$1) {
      return Mth.absMax($$0, $$1) <= MAX_COORDINATE_VALUE;
   }

   public long toLong() {
      return asLong(this.x, this.z);
   }

   public static long asLong(int $$0, int $$1) {
      return $$0 & 4294967295L | ($$1 & 4294967295L) << 32;
   }

   public static long asLong(BlockPos $$0) {
      return asLong(SectionPos.blockToSectionCoord($$0.getX()), SectionPos.blockToSectionCoord($$0.getZ()));
   }

   public static int getX(long $$0) {
      return (int)($$0 & 4294967295L);
   }

   public static int getZ(long $$0) {
      return (int)($$0 >>> 32 & 4294967295L);
   }

   @Override
   public int hashCode() {
      return hash(this.x, this.z);
   }

   public static int hash(int $$0, int $$1) {
      int $$2 = 1664525 * $$0 + 1013904223;
      int $$3 = 1664525 * ($$1 ^ -559038737) + 1013904223;
      return $$2 ^ $$3;
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return !($$0 instanceof net.minecraft.world.level.ChunkPos $$1) ? false : this.x == $$1.x && this.z == $$1.z;
      }
   }

   public int getMiddleBlockX() {
      return this.getBlockX(8);
   }

   public int getMiddleBlockZ() {
      return this.getBlockZ(8);
   }

   public int getMinBlockX() {
      return SectionPos.sectionToBlockCoord(this.x);
   }

   public int getMinBlockZ() {
      return SectionPos.sectionToBlockCoord(this.z);
   }

   public int getMaxBlockX() {
      return this.getBlockX(15);
   }

   public int getMaxBlockZ() {
      return this.getBlockZ(15);
   }

   public int getRegionX() {
      return this.x >> 5;
   }

   public int getRegionZ() {
      return this.z >> 5;
   }

   public int getRegionLocalX() {
      return this.x & 31;
   }

   public int getRegionLocalZ() {
      return this.z & 31;
   }

   public BlockPos getBlockAt(int $$0, int $$1, int $$2) {
      return new BlockPos(this.getBlockX($$0), $$1, this.getBlockZ($$2));
   }

   public int getBlockX(int $$0) {
      return SectionPos.sectionToBlockCoord(this.x, $$0);
   }

   public int getBlockZ(int $$0) {
      return SectionPos.sectionToBlockCoord(this.z, $$0);
   }

   public BlockPos getMiddleBlockPosition(int $$0) {
      return new BlockPos(this.getMiddleBlockX(), $$0, this.getMiddleBlockZ());
   }

   public boolean contains(BlockPos $$0) {
      return $$0.getX() >= this.getMinBlockX() && $$0.getZ() >= this.getMinBlockZ() && $$0.getX() <= this.getMaxBlockX() && $$0.getZ() <= this.getMaxBlockZ();
   }

   @Override
   public String toString() {
      return "[" + this.x + ", " + this.z + "]";
   }

   public BlockPos getWorldPosition() {
      return new BlockPos(this.getMinBlockX(), 0, this.getMinBlockZ());
   }

   public int getChessboardDistance(net.minecraft.world.level.ChunkPos $$0) {
      return this.getChessboardDistance($$0.x, $$0.z);
   }

   public int getChessboardDistance(int $$0, int $$1) {
      return Mth.chessboardDistance($$0, $$1, this.x, this.z);
   }

   public int distanceSquared(net.minecraft.world.level.ChunkPos $$0) {
      return this.distanceSquared($$0.x, $$0.z);
   }

   public int distanceSquared(long $$0) {
      return this.distanceSquared(getX($$0), getZ($$0));
   }

   private int distanceSquared(int $$0, int $$1) {
      int $$2 = $$0 - this.x;
      int $$3 = $$1 - this.z;
      return $$2 * $$2 + $$3 * $$3;
   }

   public static Stream<net.minecraft.world.level.ChunkPos> rangeClosed(net.minecraft.world.level.ChunkPos $$0, int $$1) {
      return rangeClosed(new net.minecraft.world.level.ChunkPos($$0.x - $$1, $$0.z - $$1), new net.minecraft.world.level.ChunkPos($$0.x + $$1, $$0.z + $$1));
   }

   public static Stream<net.minecraft.world.level.ChunkPos> rangeClosed(
      final net.minecraft.world.level.ChunkPos $$0, final net.minecraft.world.level.ChunkPos $$1
   ) {
      int $$2 = Math.abs($$0.x - $$1.x) + 1;
      int $$3 = Math.abs($$0.z - $$1.z) + 1;
      final int $$4 = $$0.x < $$1.x ? 1 : -1;
      final int $$5 = $$0.z < $$1.z ? 1 : -1;
      return StreamSupport.stream(new AbstractSpliterator<net.minecraft.world.level.ChunkPos>($$2 * $$3, 64) {
         
         private net.minecraft.world.level.ChunkPos pos;

         @Override
         public boolean tryAdvance(Consumer<? super net.minecraft.world.level.ChunkPos> $$0x) {
            if (this.pos == null) {
               this.pos = $$0;
            } else {
               int $$1x = this.pos.x;
               int $$2x = this.pos.z;
               if ($$1x == $$1.x) {
                  if ($$2x == $$1.z) {
                     return false;
                  }

                  this.pos = new net.minecraft.world.level.ChunkPos($$0.x, $$2x + $$5);
               } else {
                  this.pos = new net.minecraft.world.level.ChunkPos($$1x + $$4, $$2x);
               }
            }

            $$0.accept(this.pos);
            return true;
         }
      }, false);
   }
}
