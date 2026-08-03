package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;

public final class BlockLightEngine extends LightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
   private final MutableBlockPos mutablePos = new MutableBlockPos();

   public BlockLightEngine(LightChunkGetter $$0) {
      this($$0, new BlockLightSectionStorage($$0));
   }

   @VisibleForTesting
   public BlockLightEngine(LightChunkGetter $$0, BlockLightSectionStorage $$1) {
      super($$0, $$1);
   }

   @Override
   protected void checkNode(long $$0) {
      long $$1 = SectionPos.blockToSection($$0);
      if (this.storage.storingLightForSection($$1)) {
         BlockState $$2 = this.getState(this.mutablePos.set($$0));
         int $$3 = this.getEmission($$0, $$2);
         int $$4 = this.storage.getStoredLevel($$0);
         if ($$3 < $$4) {
            this.storage.setStoredLevel($$0, 0);
            this.enqueueDecrease($$0, LightEngine.QueueEntry.decreaseAllDirections($$4));
         } else {
            this.enqueueDecrease($$0, PULL_LIGHT_IN_ENTRY);
         }

         if ($$3 > 0) {
            this.enqueueIncrease($$0, LightEngine.QueueEntry.increaseLightFromEmission($$3, isEmptyShape($$2)));
         }
      }
   }

   @Override
   protected void propagateIncrease(long $$0, long $$1, int $$2) {
      BlockState $$3 = null;

      for (Direction $$4 : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection($$1, $$4)) {
            long $$5 = BlockPos.offset($$0, $$4);
            if (this.storage.storingLightForSection(SectionPos.blockToSection($$5))) {
               int $$6 = this.storage.getStoredLevel($$5);
               int $$7 = $$2 - 1;
               if ($$7 > $$6) {
                  this.mutablePos.set($$5);
                  BlockState $$8 = this.getState(this.mutablePos);
                  int $$9 = $$2 - this.getOpacity($$8);
                  if ($$9 > $$6) {
                     if ($$3 == null) {
                        $$3 = LightEngine.QueueEntry.isFromEmptyShape($$1) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set($$0));
                     }

                     if (!this.shapeOccludes($$3, $$8, $$4)) {
                        this.storage.setStoredLevel($$5, $$9);
                        if ($$9 > 1) {
                           this.enqueueIncrease($$5, LightEngine.QueueEntry.increaseSkipOneDirection($$9, isEmptyShape($$8), $$4.getOpposite()));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   protected void propagateDecrease(long $$0, long $$1) {
      int $$2 = LightEngine.QueueEntry.getFromLevel($$1);

      for (Direction $$3 : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection($$1, $$3)) {
            long $$4 = BlockPos.offset($$0, $$3);
            if (this.storage.storingLightForSection(SectionPos.blockToSection($$4))) {
               int $$5 = this.storage.getStoredLevel($$4);
               if ($$5 != 0) {
                  if ($$5 <= $$2 - 1) {
                     BlockState $$6 = this.getState(this.mutablePos.set($$4));
                     int $$7 = this.getEmission($$4, $$6);
                     this.storage.setStoredLevel($$4, 0);
                     if ($$7 < $$5) {
                        this.enqueueDecrease($$4, LightEngine.QueueEntry.decreaseSkipOneDirection($$5, $$3.getOpposite()));
                     }

                     if ($$7 > 0) {
                        this.enqueueIncrease($$4, LightEngine.QueueEntry.increaseLightFromEmission($$7, isEmptyShape($$6)));
                     }
                  } else {
                     this.enqueueIncrease($$4, LightEngine.QueueEntry.increaseOnlyOneDirection($$5, false, $$3.getOpposite()));
                  }
               }
            }
         }
      }
   }

   private int getEmission(long $$0, BlockState $$1) {
      int $$2 = $$1.getLightEmission();
      return $$2 > 0 && this.storage.lightOnInSection(SectionPos.blockToSection($$0)) ? $$2 : 0;
   }

   @Override
   public void propagateLightSources(net.minecraft.world.level.ChunkPos $$0) {
      this.setLightEnabled($$0, true);
      LightChunk $$1 = this.chunkSource.getChunkForLighting($$0.x, $$0.z);
      if ($$1 != null) {
         $$1.findBlockLightSources(($$0x, $$1x) -> {
            int $$2 = $$1x.getLightEmission();
            this.enqueueIncrease($$0x.asLong(), LightEngine.QueueEntry.increaseLightFromEmission($$2, isEmptyShape($$1x)));
         });
      }
   }
}
