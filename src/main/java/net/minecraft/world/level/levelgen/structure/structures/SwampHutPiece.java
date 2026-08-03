package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class SwampHutPiece extends ScatteredFeaturePiece {
   private boolean spawnedWitch;
   private boolean spawnedCat;

   public SwampHutPiece(RandomSource $$0, int $$1, int $$2) {
      super(StructurePieceType.SWAMPLAND_HUT, $$1, 64, $$2, 7, 7, 9, getRandomHorizontalDirection($$0));
   }

   public SwampHutPiece(CompoundTag $$0) {
      super(StructurePieceType.SWAMPLAND_HUT, $$0);
      this.spawnedWitch = $$0.getBooleanOr("Witch", false);
      this.spawnedCat = $$0.getBooleanOr("Cat", false);
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
      super.addAdditionalSaveData($$0, $$1);
      $$1.putBoolean("Witch", this.spawnedWitch);
      $$1.putBoolean("Cat", this.spawnedCat);
   }

   @Override
   public void postProcess(
      net.minecraft.world.level.WorldGenLevel $$0,
      net.minecraft.world.level.StructureManager $$1,
      ChunkGenerator $$2,
      RandomSource $$3,
      BoundingBox $$4,
      net.minecraft.world.level.ChunkPos $$5,
      BlockPos $$6
   ) {
      if (this.updateAverageGroundHeight($$0, $$4, 0)) {
         this.generateBox($$0, $$4, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.placeBlock($$0, Blocks.OAK_FENCE.defaultBlockState(), 2, 3, 2, $$4);
         this.placeBlock($$0, Blocks.OAK_FENCE.defaultBlockState(), 3, 3, 7, $$4);
         this.placeBlock($$0, Blocks.AIR.defaultBlockState(), 1, 3, 4, $$4);
         this.placeBlock($$0, Blocks.AIR.defaultBlockState(), 5, 3, 4, $$4);
         this.placeBlock($$0, Blocks.AIR.defaultBlockState(), 5, 3, 5, $$4);
         this.placeBlock($$0, Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 1, 3, 5, $$4);
         this.placeBlock($$0, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 2, 6, $$4);
         this.placeBlock($$0, Blocks.CAULDRON.defaultBlockState(), 4, 2, 6, $$4);
         this.placeBlock($$0, Blocks.OAK_FENCE.defaultBlockState(), 1, 2, 1, $$4);
         this.placeBlock($$0, Blocks.OAK_FENCE.defaultBlockState(), 5, 2, 1, $$4);
         BlockState $$7 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
         BlockState $$8 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
         BlockState $$9 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
         BlockState $$10 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
         this.generateBox($$0, $$4, 0, 4, 1, 6, 4, 1, $$7, $$7, false);
         this.generateBox($$0, $$4, 0, 4, 2, 0, 4, 7, $$8, $$8, false);
         this.generateBox($$0, $$4, 6, 4, 2, 6, 4, 7, $$9, $$9, false);
         this.generateBox($$0, $$4, 0, 4, 8, 6, 4, 8, $$10, $$10, false);
         this.placeBlock($$0, $$7.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 0, 4, 1, $$4);
         this.placeBlock($$0, $$7.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 6, 4, 1, $$4);
         this.placeBlock($$0, $$10.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 0, 4, 8, $$4);
         this.placeBlock($$0, $$10.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 6, 4, 8, $$4);

         for (int $$11 = 2; $$11 <= 7; $$11 += 5) {
            for (int $$12 = 1; $$12 <= 5; $$12 += 4) {
               this.fillColumnDown($$0, Blocks.OAK_LOG.defaultBlockState(), $$12, -1, $$11, $$4);
            }
         }

         if (!this.spawnedWitch) {
            BlockPos $$13 = this.getWorldPos(2, 2, 5);
            if ($$4.isInside($$13)) {
               this.spawnedWitch = true;
               Witch $$14 = (Witch)EntityType.WITCH.create($$0.getLevel(), EntitySpawnReason.STRUCTURE);
               if ($$14 != null) {
                  $$14.setPersistenceRequired();
                  $$14.snapTo($$13.getX() + 0.5, $$13.getY(), $$13.getZ() + 0.5, 0.0F, 0.0F);
                  $$14.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$13), EntitySpawnReason.STRUCTURE, null);
                  $$0.addFreshEntityWithPassengers($$14);
               }
            }
         }

         this.spawnCat($$0, $$4);
      }
   }

   private void spawnCat(net.minecraft.world.level.ServerLevelAccessor $$0, BoundingBox $$1) {
      if (!this.spawnedCat) {
         BlockPos $$2 = this.getWorldPos(2, 2, 5);
         if ($$1.isInside($$2)) {
            this.spawnedCat = true;
            Cat $$3 = (Cat)EntityType.CAT.create($$0.getLevel(), EntitySpawnReason.STRUCTURE);
            if ($$3 != null) {
               $$3.setPersistenceRequired();
               $$3.snapTo($$2.getX() + 0.5, $$2.getY(), $$2.getZ() + 0.5, 0.0F, 0.0F);
               $$3.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$2), EntitySpawnReason.STRUCTURE, null);
               $$0.addFreshEntityWithPassengers($$3);
            }
         }
      }
   }
}
