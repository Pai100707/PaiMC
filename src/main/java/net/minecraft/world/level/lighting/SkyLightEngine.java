package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.jspecify.annotations.Nullable;

public final class SkyLightEngine extends LightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
   private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(15);
   private static final long REMOVE_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseSkipOneDirection(15, Direction.UP);
   private static final long ADD_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.increaseSkipOneDirection(15, false, Direction.UP);
   private final MutableBlockPos mutablePos = new MutableBlockPos();
   private final ChunkSkyLightSources emptyChunkSources;

   public SkyLightEngine(LightChunkGetter $$0) {
      this($$0, new SkyLightSectionStorage($$0));
   }

   @VisibleForTesting
   protected SkyLightEngine(LightChunkGetter $$0, SkyLightSectionStorage $$1) {
      super($$0, $$1);
      this.emptyChunkSources = new ChunkSkyLightSources($$0.getLevel());
   }

   private static boolean isSourceLevel(int $$0) {
      return $$0 == 15;
   }

   private int getLowestSourceY(int $$0, int $$1, int $$2) {
      ChunkSkyLightSources $$3 = this.getChunkSources(SectionPos.blockToSectionCoord($$0), SectionPos.blockToSectionCoord($$1));
      return $$3 == null ? $$2 : $$3.getLowestSourceY(SectionPos.sectionRelative($$0), SectionPos.sectionRelative($$1));
   }

   @Nullable
   private ChunkSkyLightSources getChunkSources(int $$0, int $$1) {
      LightChunk $$2 = this.chunkSource.getChunkForLighting($$0, $$1);
      return $$2 != null ? $$2.getSkyLightSources() : null;
   }

   @Override
   protected void checkNode(long $$0) {
      int $$1 = BlockPos.getX($$0);
      int $$2 = BlockPos.getY($$0);
      int $$3 = BlockPos.getZ($$0);
      long $$4 = SectionPos.blockToSection($$0);
      int $$5 = this.storage.lightOnInSection($$4) ? this.getLowestSourceY($$1, $$3, Integer.MAX_VALUE) : Integer.MAX_VALUE;
      if ($$5 != Integer.MAX_VALUE) {
         this.updateSourcesInColumn($$1, $$3, $$5);
      }

      if (this.storage.storingLightForSection($$4)) {
         boolean $$6 = $$2 >= $$5;
         if ($$6) {
            this.enqueueDecrease($$0, REMOVE_SKY_SOURCE_ENTRY);
            this.enqueueIncrease($$0, ADD_SKY_SOURCE_ENTRY);
         } else {
            int $$7 = this.storage.getStoredLevel($$0);
            if ($$7 > 0) {
               this.storage.setStoredLevel($$0, 0);
               this.enqueueDecrease($$0, LightEngine.QueueEntry.decreaseAllDirections($$7));
            } else {
               this.enqueueDecrease($$0, PULL_LIGHT_IN_ENTRY);
            }
         }
      }
   }

   private void updateSourcesInColumn(int $$0, int $$1, int $$2) {
      int $$3 = SectionPos.sectionToBlockCoord(this.storage.getBottomSectionY());
      this.removeSourcesBelow($$0, $$1, $$2, $$3);
      this.addSourcesAbove($$0, $$1, $$2, $$3);
   }

   private void removeSourcesBelow(int $$0, int $$1, int $$2, int $$3) {
      if ($$2 > $$3) {
         int $$4 = SectionPos.blockToSectionCoord($$0);
         int $$5 = SectionPos.blockToSectionCoord($$1);
         int $$6 = $$2 - 1;

         for (int $$7 = SectionPos.blockToSectionCoord($$6); this.storage.hasLightDataAtOrBelow($$7); $$7--) {
            if (this.storage.storingLightForSection(SectionPos.asLong($$4, $$7, $$5))) {
               int $$8 = SectionPos.sectionToBlockCoord($$7);
               int $$9 = $$8 + 15;

               for (int $$10 = Math.min($$9, $$6); $$10 >= $$8; $$10--) {
                  long $$11 = BlockPos.asLong($$0, $$10, $$1);
                  if (!isSourceLevel(this.storage.getStoredLevel($$11))) {
                     return;
                  }

                  this.storage.setStoredLevel($$11, 0);
                  this.enqueueDecrease($$11, $$10 == $$2 - 1 ? REMOVE_TOP_SKY_SOURCE_ENTRY : REMOVE_SKY_SOURCE_ENTRY);
               }
            }
         }
      }
   }

   private void addSourcesAbove(int $$0, int $$1, int $$2, int $$3) {
      int $$4 = SectionPos.blockToSectionCoord($$0);
      int $$5 = SectionPos.blockToSectionCoord($$1);
      int $$6 = Math.max(
         Math.max(this.getLowestSourceY($$0 - 1, $$1, Integer.MIN_VALUE), this.getLowestSourceY($$0 + 1, $$1, Integer.MIN_VALUE)),
         Math.max(this.getLowestSourceY($$0, $$1 - 1, Integer.MIN_VALUE), this.getLowestSourceY($$0, $$1 + 1, Integer.MIN_VALUE))
      );
      int $$7 = Math.max($$2, $$3);

      for (long $$8 = SectionPos.asLong($$4, SectionPos.blockToSectionCoord($$7), $$5);
         !this.storage.isAboveData($$8);
         $$8 = SectionPos.offset($$8, Direction.UP)
      ) {
         if (this.storage.storingLightForSection($$8)) {
            int $$9 = SectionPos.sectionToBlockCoord(SectionPos.y($$8));
            int $$10 = $$9 + 15;

            for (int $$11 = Math.max($$9, $$7); $$11 <= $$10; $$11++) {
               long $$12 = BlockPos.asLong($$0, $$11, $$1);
               if (isSourceLevel(this.storage.getStoredLevel($$12))) {
                  return;
               }

               this.storage.setStoredLevel($$12, 15);
               if ($$11 < $$6 || $$11 == $$2) {
                  this.enqueueIncrease($$12, ADD_SKY_SOURCE_ENTRY);
               }
            }
         }
      }
   }

   @Override
   protected void propagateIncrease(long $$0, long $$1, int $$2) {
      BlockState $$3 = null;
      int $$4 = this.countEmptySectionsBelowIfAtBorder($$0);

      for (Direction $$5 : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection($$1, $$5)) {
            long $$6 = BlockPos.offset($$0, $$5);
            if (this.storage.storingLightForSection(SectionPos.blockToSection($$6))) {
               int $$7 = this.storage.getStoredLevel($$6);
               int $$8 = $$2 - 1;
               if ($$8 > $$7) {
                  this.mutablePos.set($$6);
                  BlockState $$9 = this.getState(this.mutablePos);
                  int $$10 = $$2 - this.getOpacity($$9);
                  if ($$10 > $$7) {
                     if ($$3 == null) {
                        $$3 = LightEngine.QueueEntry.isFromEmptyShape($$1) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set($$0));
                     }

                     if (!this.shapeOccludes($$3, $$9, $$5)) {
                        this.storage.setStoredLevel($$6, $$10);
                        if ($$10 > 1) {
                           this.enqueueIncrease($$6, LightEngine.QueueEntry.increaseSkipOneDirection($$10, isEmptyShape($$9), $$5.getOpposite()));
                        }

                        this.propagateFromEmptySections($$6, $$5, $$10, true, $$4);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   protected void propagateDecrease(long $$0, long $$1) {
      int $$2 = this.countEmptySectionsBelowIfAtBorder($$0);
      int $$3 = LightEngine.QueueEntry.getFromLevel($$1);

      for (Direction $$4 : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection($$1, $$4)) {
            long $$5 = BlockPos.offset($$0, $$4);
            if (this.storage.storingLightForSection(SectionPos.blockToSection($$5))) {
               int $$6 = this.storage.getStoredLevel($$5);
               if ($$6 != 0) {
                  if ($$6 <= $$3 - 1) {
                     this.storage.setStoredLevel($$5, 0);
                     this.enqueueDecrease($$5, LightEngine.QueueEntry.decreaseSkipOneDirection($$6, $$4.getOpposite()));
                     this.propagateFromEmptySections($$5, $$4, $$6, false, $$2);
                  } else {
                     this.enqueueIncrease($$5, LightEngine.QueueEntry.increaseOnlyOneDirection($$6, false, $$4.getOpposite()));
                  }
               }
            }
         }
      }
   }

   private int countEmptySectionsBelowIfAtBorder(long $$0) {
      int $$1 = BlockPos.getY($$0);
      int $$2 = SectionPos.sectionRelative($$1);
      if ($$2 != 0) {
         return 0;
      } else {
         int $$3 = BlockPos.getX($$0);
         int $$4 = BlockPos.getZ($$0);
         int $$5 = SectionPos.sectionRelative($$3);
         int $$6 = SectionPos.sectionRelative($$4);
         if ($$5 != 0 && $$5 != 15 && $$6 != 0 && $$6 != 15) {
            return 0;
         } else {
            int $$7 = SectionPos.blockToSectionCoord($$3);
            int $$8 = SectionPos.blockToSectionCoord($$1);
            int $$9 = SectionPos.blockToSectionCoord($$4);
            int $$10 = 0;

            while (!this.storage.storingLightForSection(SectionPos.asLong($$7, $$8 - $$10 - 1, $$9)) && this.storage.hasLightDataAtOrBelow($$8 - $$10 - 1)) {
               $$10++;
            }

            return $$10;
         }
      }
   }

   private void propagateFromEmptySections(long $$0, Direction $$1, int $$2, boolean $$3, int $$4) {
      if ($$4 != 0) {
         int $$5 = BlockPos.getX($$0);
         int $$6 = BlockPos.getZ($$0);
         if (crossedSectionEdge($$1, SectionPos.sectionRelative($$5), SectionPos.sectionRelative($$6))) {
            int $$7 = BlockPos.getY($$0);
            int $$8 = SectionPos.blockToSectionCoord($$5);
            int $$9 = SectionPos.blockToSectionCoord($$6);
            int $$10 = SectionPos.blockToSectionCoord($$7) - 1;
            int $$11 = $$10 - $$4 + 1;

            while ($$10 >= $$11) {
               if (!this.storage.storingLightForSection(SectionPos.asLong($$8, $$10, $$9))) {
                  $$10--;
               } else {
                  int $$12 = SectionPos.sectionToBlockCoord($$10);

                  for (int $$13 = 15; $$13 >= 0; $$13--) {
                     long $$14 = BlockPos.asLong($$5, $$12 + $$13, $$6);
                     if ($$3) {
                        this.storage.setStoredLevel($$14, $$2);
                        if ($$2 > 1) {
                           this.enqueueIncrease($$14, LightEngine.QueueEntry.increaseSkipOneDirection($$2, true, $$1.getOpposite()));
                        }
                     } else {
                        this.storage.setStoredLevel($$14, 0);
                        this.enqueueDecrease($$14, LightEngine.QueueEntry.decreaseSkipOneDirection($$2, $$1.getOpposite()));
                     }
                  }

                  $$10--;
               }
            }
         }
      }
   }

   private static boolean crossedSectionEdge(Direction $$0, int $$1, int $$2) {
      return switch ($$0) {
         case NORTH -> $$2 == 15;
         case SOUTH -> $$2 == 0;
         case WEST -> $$1 == 15;
         case EAST -> $$1 == 0;
         default -> false;
      };
   }

   @Override
   public void setLightEnabled(net.minecraft.world.level.ChunkPos $$0, boolean $$1) {
      super.setLightEnabled($$0, $$1);
      if ($$1) {
         ChunkSkyLightSources $$2 = Objects.requireNonNullElse(this.getChunkSources($$0.x, $$0.z), this.emptyChunkSources);
         int $$3 = $$2.getHighestLowestSourceY() - 1;
         int $$4 = SectionPos.blockToSectionCoord($$3) + 1;
         long $$5 = SectionPos.getZeroNode($$0.x, $$0.z);
         int $$6 = this.storage.getTopSectionY($$5);
         int $$7 = Math.max(this.storage.getBottomSectionY(), $$4);

         for (int $$8 = $$6 - 1; $$8 >= $$7; $$8--) {
            DataLayer $$9 = this.storage.getDataLayerToWrite(SectionPos.asLong($$0.x, $$8, $$0.z));
            if ($$9 != null && $$9.isEmpty()) {
               $$9.fill(15);
            }
         }
      }
   }

   @Override
   public void propagateLightSources(net.minecraft.world.level.ChunkPos $$0) {
      long $$1 = SectionPos.getZeroNode($$0.x, $$0.z);
      this.storage.setLightEnabled($$1, true);
      ChunkSkyLightSources $$2 = Objects.requireNonNullElse(this.getChunkSources($$0.x, $$0.z), this.emptyChunkSources);
      ChunkSkyLightSources $$3 = Objects.requireNonNullElse(this.getChunkSources($$0.x, $$0.z - 1), this.emptyChunkSources);
      ChunkSkyLightSources $$4 = Objects.requireNonNullElse(this.getChunkSources($$0.x, $$0.z + 1), this.emptyChunkSources);
      ChunkSkyLightSources $$5 = Objects.requireNonNullElse(this.getChunkSources($$0.x - 1, $$0.z), this.emptyChunkSources);
      ChunkSkyLightSources $$6 = Objects.requireNonNullElse(this.getChunkSources($$0.x + 1, $$0.z), this.emptyChunkSources);
      int $$7 = this.storage.getTopSectionY($$1);
      int $$8 = this.storage.getBottomSectionY();
      int $$9 = SectionPos.sectionToBlockCoord($$0.x);
      int $$10 = SectionPos.sectionToBlockCoord($$0.z);

      for (int $$11 = $$7 - 1; $$11 >= $$8; $$11--) {
         long $$12 = SectionPos.asLong($$0.x, $$11, $$0.z);
         DataLayer $$13 = this.storage.getDataLayerToWrite($$12);
         if ($$13 != null) {
            int $$14 = SectionPos.sectionToBlockCoord($$11);
            int $$15 = $$14 + 15;
            boolean $$16 = false;

            for (int $$17 = 0; $$17 < 16; $$17++) {
               for (int $$18 = 0; $$18 < 16; $$18++) {
                  int $$19 = $$2.getLowestSourceY($$18, $$17);
                  if ($$19 <= $$15) {
                     int $$20 = $$17 == 0 ? $$3.getLowestSourceY($$18, 15) : $$2.getLowestSourceY($$18, $$17 - 1);
                     int $$21 = $$17 == 15 ? $$4.getLowestSourceY($$18, 0) : $$2.getLowestSourceY($$18, $$17 + 1);
                     int $$22 = $$18 == 0 ? $$5.getLowestSourceY(15, $$17) : $$2.getLowestSourceY($$18 - 1, $$17);
                     int $$23 = $$18 == 15 ? $$6.getLowestSourceY(0, $$17) : $$2.getLowestSourceY($$18 + 1, $$17);
                     int $$24 = Math.max(Math.max($$20, $$21), Math.max($$22, $$23));

                     for (int $$25 = $$15; $$25 >= Math.max($$14, $$19); $$25--) {
                        $$13.set($$18, SectionPos.sectionRelative($$25), $$17, 15);
                        if ($$25 == $$19 || $$25 < $$24) {
                           long $$26 = BlockPos.asLong($$9 + $$18, $$25, $$10 + $$17);
                           this.enqueueIncrease(
                              $$26, LightEngine.QueueEntry.increaseSkySourceInDirections($$25 == $$19, $$25 < $$20, $$25 < $$21, $$25 < $$22, $$25 < $$23)
                           );
                        }
                     }

                     if ($$19 < $$14) {
                        $$16 = true;
                     }
                  }
               }
            }

            if (!$$16) {
               break;
            }
         }
      }
   }
}
