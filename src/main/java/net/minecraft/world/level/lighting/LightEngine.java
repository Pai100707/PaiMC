package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class LightEngine<M extends DataLayerStorageMap<M>, S extends LayerLightSectionStorage<M>> implements LayerLightEventListener {
   public static final int MAX_LEVEL = 15;
   protected static final int MIN_OPACITY = 1;
   protected static final long PULL_LIGHT_IN_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(1);
   private static final int MIN_QUEUE_SIZE = 512;
   protected static final Direction[] PROPAGATION_DIRECTIONS = Direction.values();
   protected final LightChunkGetter chunkSource;
   protected final S storage;
   private final LongOpenHashSet blockNodesToCheck = new LongOpenHashSet(512, 0.5F);
   private final LongArrayFIFOQueue decreaseQueue = new LongArrayFIFOQueue();
   private final LongArrayFIFOQueue increaseQueue = new LongArrayFIFOQueue();
   private static final int CACHE_SIZE = 2;
   private final long[] lastChunkPos = new long[2];
   private final LightChunk[] lastChunk = new LightChunk[2];

   protected LightEngine(LightChunkGetter $$0, S $$1) {
      this.chunkSource = $$0;
      this.storage = $$1;
      this.clearChunkCache();
   }

   public static boolean hasDifferentLightProperties(BlockState $$0, BlockState $$1) {
      return $$1 == $$0
         ? false
         : $$1.getLightBlock() != $$0.getLightBlock()
            || $$1.getLightEmission() != $$0.getLightEmission()
            || $$1.useShapeForLightOcclusion()
            || $$0.useShapeForLightOcclusion();
   }

   public static int getLightBlockInto(BlockState $$0, BlockState $$1, Direction $$2, int $$3) {
      boolean $$4 = isEmptyShape($$0);
      boolean $$5 = isEmptyShape($$1);
      if ($$4 && $$5) {
         return $$3;
      } else {
         VoxelShape $$6 = $$4 ? Shapes.empty() : $$0.getOcclusionShape();
         VoxelShape $$7 = $$5 ? Shapes.empty() : $$1.getOcclusionShape();
         return Shapes.mergedFaceOccludes($$6, $$7, $$2) ? 16 : $$3;
      }
   }

   public static VoxelShape getOcclusionShape(BlockState $$0, Direction $$1) {
      return isEmptyShape($$0) ? Shapes.empty() : $$0.getFaceOcclusionShape($$1);
   }

   protected static boolean isEmptyShape(BlockState $$0) {
      return !$$0.canOcclude() || !$$0.useShapeForLightOcclusion();
   }

   protected BlockState getState(BlockPos $$0) {
      int $$1 = SectionPos.blockToSectionCoord($$0.getX());
      int $$2 = SectionPos.blockToSectionCoord($$0.getZ());
      LightChunk $$3 = this.getChunk($$1, $$2);
      return $$3 == null ? Blocks.BEDROCK.defaultBlockState() : $$3.getBlockState($$0);
   }

   protected int getOpacity(BlockState $$0) {
      return Math.max(1, $$0.getLightBlock());
   }

   protected boolean shapeOccludes(BlockState $$0, BlockState $$1, Direction $$2) {
      VoxelShape $$3 = getOcclusionShape($$0, $$2);
      VoxelShape $$4 = getOcclusionShape($$1, $$2.getOpposite());
      return Shapes.faceShapeOccludes($$3, $$4);
   }

   
   protected LightChunk getChunk(int $$0, int $$1) {
      long $$2 = net.minecraft.world.level.ChunkPos.asLong($$0, $$1);

      for (int $$3 = 0; $$3 < 2; $$3++) {
         if ($$2 == this.lastChunkPos[$$3]) {
            return this.lastChunk[$$3];
         }
      }

      LightChunk $$4 = this.chunkSource.getChunkForLighting($$0, $$1);

      for (int $$5 = 1; $$5 > 0; $$5--) {
         this.lastChunkPos[$$5] = this.lastChunkPos[$$5 - 1];
         this.lastChunk[$$5] = this.lastChunk[$$5 - 1];
      }

      this.lastChunkPos[0] = $$2;
      this.lastChunk[0] = $$4;
      return $$4;
   }

   private void clearChunkCache() {
      Arrays.fill(this.lastChunkPos, net.minecraft.world.level.ChunkPos.INVALID_CHUNK_POS);
      Arrays.fill(this.lastChunk, null);
   }

   @Override
   public void checkBlock(BlockPos $$0) {
      this.blockNodesToCheck.add($$0.asLong());
   }

   public void queueSectionData(long $$0, DataLayer $$1) {
      this.storage.queueSectionData($$0, $$1);
   }

   public void retainData(net.minecraft.world.level.ChunkPos $$0, boolean $$1) {
      this.storage.retainData(SectionPos.getZeroNode($$0.x, $$0.z), $$1);
   }

   @Override
   public void updateSectionStatus(SectionPos $$0, boolean $$1) {
      this.storage.updateSectionStatus($$0.asLong(), $$1);
   }

   @Override
   public void setLightEnabled(net.minecraft.world.level.ChunkPos $$0, boolean $$1) {
      this.storage.setLightEnabled(SectionPos.getZeroNode($$0.x, $$0.z), $$1);
   }

   @Override
   public int runLightUpdates() {
      LongIterator $$0 = this.blockNodesToCheck.iterator();

      while ($$0.hasNext()) {
         this.checkNode($$0.nextLong());
      }

      this.blockNodesToCheck.clear();
      this.blockNodesToCheck.trim(512);
      int $$1 = 0;
      $$1 += this.propagateDecreases();
      $$1 += this.propagateIncreases();
      this.clearChunkCache();
      this.storage.markNewInconsistencies(this);
      this.storage.swapSectionMap();
      return $$1;
   }

   private int propagateIncreases() {
      int $$0;
      for ($$0 = 0; !this.increaseQueue.isEmpty(); $$0++) {
         long $$1 = this.increaseQueue.dequeueLong();
         long $$2 = this.increaseQueue.dequeueLong();
         int $$3 = this.storage.getStoredLevel($$1);
         int $$4 = LightEngine.QueueEntry.getFromLevel($$2);
         if (LightEngine.QueueEntry.isIncreaseFromEmission($$2) && $$3 < $$4) {
            this.storage.setStoredLevel($$1, $$4);
            $$3 = $$4;
         }

         if ($$3 == $$4) {
            this.propagateIncrease($$1, $$2, $$3);
         }
      }

      return $$0;
   }

   private int propagateDecreases() {
      int $$0;
      for ($$0 = 0; !this.decreaseQueue.isEmpty(); $$0++) {
         long $$1 = this.decreaseQueue.dequeueLong();
         long $$2 = this.decreaseQueue.dequeueLong();
         this.propagateDecrease($$1, $$2);
      }

      return $$0;
   }

   protected void enqueueDecrease(long $$0, long $$1) {
      this.decreaseQueue.enqueue($$0);
      this.decreaseQueue.enqueue($$1);
   }

   protected void enqueueIncrease(long $$0, long $$1) {
      this.increaseQueue.enqueue($$0);
      this.increaseQueue.enqueue($$1);
   }

   @Override
   public boolean hasLightWork() {
      return this.storage.hasInconsistencies() || !this.blockNodesToCheck.isEmpty() || !this.decreaseQueue.isEmpty() || !this.increaseQueue.isEmpty();
   }

   
   @Override
   public DataLayer getDataLayerData(SectionPos $$0) {
      return this.storage.getDataLayerData($$0.asLong());
   }

   @Override
   public int getLightValue(BlockPos $$0) {
      return this.storage.getLightValue($$0.asLong());
   }

   public String getDebugData(long $$0) {
      return this.getDebugSectionType($$0).display();
   }

   public LayerLightSectionStorage.SectionType getDebugSectionType(long $$0) {
      return this.storage.getDebugSectionType($$0);
   }

   protected abstract void checkNode(long var1);

   protected abstract void propagateIncrease(long var1, long var3, int var5);

   protected abstract void propagateDecrease(long var1, long var3);

   public static class QueueEntry {
      private static final int FROM_LEVEL_BITS = 4;
      private static final int DIRECTION_BITS = 6;
      private static final long LEVEL_MASK = 15L;
      private static final long DIRECTIONS_MASK = 1008L;
      private static final long FLAG_FROM_EMPTY_SHAPE = 1024L;
      private static final long FLAG_INCREASE_FROM_EMISSION = 2048L;

      public static long decreaseSkipOneDirection(int $$0, Direction $$1) {
         long $$2 = withoutDirection(1008L, $$1);
         return withLevel($$2, $$0);
      }

      public static long decreaseAllDirections(int $$0) {
         return withLevel(1008L, $$0);
      }

      public static long increaseLightFromEmission(int $$0, boolean $$1) {
         long $$2 = 1008L;
         $$2 |= 2048L;
         if ($$1) {
            $$2 |= 1024L;
         }

         return withLevel($$2, $$0);
      }

      public static long increaseSkipOneDirection(int $$0, boolean $$1, Direction $$2) {
         long $$3 = withoutDirection(1008L, $$2);
         if ($$1) {
            $$3 |= 1024L;
         }

         return withLevel($$3, $$0);
      }

      public static long increaseOnlyOneDirection(int $$0, boolean $$1, Direction $$2) {
         long $$3 = 0L;
         if ($$1) {
            $$3 |= 1024L;
         }

         $$3 = withDirection($$3, $$2);
         return withLevel($$3, $$0);
      }

      public static long increaseSkySourceInDirections(boolean $$0, boolean $$1, boolean $$2, boolean $$3, boolean $$4) {
         long $$5 = withLevel(0L, 15);
         if ($$0) {
            $$5 = withDirection($$5, Direction.DOWN);
         }

         if ($$1) {
            $$5 = withDirection($$5, Direction.NORTH);
         }

         if ($$2) {
            $$5 = withDirection($$5, Direction.SOUTH);
         }

         if ($$3) {
            $$5 = withDirection($$5, Direction.WEST);
         }

         if ($$4) {
            $$5 = withDirection($$5, Direction.EAST);
         }

         return $$5;
      }

      public static int getFromLevel(long $$0) {
         return (int)($$0 & 15L);
      }

      public static boolean isFromEmptyShape(long $$0) {
         return ($$0 & 1024L) != 0L;
      }

      public static boolean isIncreaseFromEmission(long $$0) {
         return ($$0 & 2048L) != 0L;
      }

      public static boolean shouldPropagateInDirection(long $$0, Direction $$1) {
         return ($$0 & 1L << $$1.ordinal() + 4) != 0L;
      }

      private static long withLevel(long $$0, int $$1) {
         return $$0 & -16L | $$1 & 15L;
      }

      private static long withDirection(long $$0, Direction $$1) {
         return $$0 | 1L << $$1.ordinal() + 4;
      }

      private static long withoutDirection(long $$0, Direction $$1) {
         return $$0 & ~(1L << $$1.ordinal() + 4);
      }
   }
}
