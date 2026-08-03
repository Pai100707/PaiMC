package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BuriedTreasurePieces {
   public static class BuriedTreasurePiece extends StructurePiece {
      public BuriedTreasurePiece(BlockPos $$0) {
         super(StructurePieceType.BURIED_TREASURE_PIECE, 0, new BoundingBox($$0));
      }

      public BuriedTreasurePiece(CompoundTag $$0) {
         super(StructurePieceType.BURIED_TREASURE_PIECE, $$0);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
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
         int $$7 = $$0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.boundingBox.minX(), this.boundingBox.minZ());
         MutableBlockPos $$8 = new MutableBlockPos(this.boundingBox.minX(), $$7, this.boundingBox.minZ());

         while ($$8.getY() > $$0.getMinY()) {
            BlockState $$9 = $$0.getBlockState($$8);
            BlockState $$10 = $$0.getBlockState($$8.below());
            if ($$10 == Blocks.SANDSTONE.defaultBlockState()
               || $$10 == Blocks.STONE.defaultBlockState()
               || $$10 == Blocks.ANDESITE.defaultBlockState()
               || $$10 == Blocks.GRANITE.defaultBlockState()
               || $$10 == Blocks.DIORITE.defaultBlockState()) {
               BlockState $$11 = !$$9.isAir() && !this.isLiquid($$9) ? $$9 : Blocks.SAND.defaultBlockState();

               for (Direction $$12 : Direction.values()) {
                  BlockPos $$13 = $$8.relative($$12);
                  BlockState $$14 = $$0.getBlockState($$13);
                  if ($$14.isAir() || this.isLiquid($$14)) {
                     BlockPos $$15 = $$13.below();
                     BlockState $$16 = $$0.getBlockState($$15);
                     if (($$16.isAir() || this.isLiquid($$16)) && $$12 != Direction.UP) {
                        $$0.setBlock($$13, $$10, 3);
                     } else {
                        $$0.setBlock($$13, $$11, 3);
                     }
                  }
               }

               this.boundingBox = new BoundingBox($$8);
               this.createChest($$0, $$4, $$3, $$8, BuiltInLootTables.BURIED_TREASURE, null);
               return;
            }

            $$8.move(0, -1, 0);
         }
      }

      private boolean isLiquid(BlockState $$0) {
         return $$0 == Blocks.WATER.defaultBlockState() || $$0 == Blocks.LAVA.defaultBlockState();
      }
   }
}
