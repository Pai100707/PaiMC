package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class MineshaftPieces {
   private static final int DEFAULT_SHAFT_WIDTH = 3;
   private static final int DEFAULT_SHAFT_HEIGHT = 3;
   private static final int DEFAULT_SHAFT_LENGTH = 5;
   private static final int MAX_PILLAR_HEIGHT = 20;
   private static final int MAX_CHAIN_HEIGHT = 50;
   private static final int MAX_DEPTH = 8;
   public static final int MAGIC_START_Y = 50;

   
   private static MineshaftPieces.MineShaftPiece createRandomShaftPiece(
      StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6, MineshaftStructure.Type $$7
   ) {
      int $$8 = $$1.nextInt(100);
      if ($$8 >= 80) {
         BoundingBox $$9 = MineshaftPieces.MineShaftCrossing.findCrossing($$0, $$1, $$2, $$3, $$4, $$5);
         if ($$9 != null) {
            return new MineshaftPieces.MineShaftCrossing($$6, $$9, $$5, $$7);
         }
      } else if ($$8 >= 70) {
         BoundingBox $$10 = MineshaftPieces.MineShaftStairs.findStairs($$0, $$1, $$2, $$3, $$4, $$5);
         if ($$10 != null) {
            return new MineshaftPieces.MineShaftStairs($$6, $$10, $$5, $$7);
         }
      } else {
         BoundingBox $$11 = MineshaftPieces.MineShaftCorridor.findCorridorSize($$0, $$1, $$2, $$3, $$4, $$5);
         if ($$11 != null) {
            return new MineshaftPieces.MineShaftCorridor($$6, $$1, $$11, $$5, $$7);
         }
      }

      return null;
   }

   
   static MineshaftPieces.MineShaftPiece generateAndAddPiece(
      StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4, int $$5, Direction $$6, int $$7
   ) {
      if ($$7 > 8) {
         return null;
      } else if (Math.abs($$3 - $$0.getBoundingBox().minX()) <= 80 && Math.abs($$5 - $$0.getBoundingBox().minZ()) <= 80) {
         MineshaftStructure.Type $$8 = ((MineshaftPieces.MineShaftPiece)$$0).type;
         MineshaftPieces.MineShaftPiece $$9 = createRandomShaftPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7 + 1, $$8);
         if ($$9 != null) {
            $$1.addPiece($$9);
            $$9.addChildren($$0, $$1, $$2);
         }

         return $$9;
      } else {
         return null;
      }
   }

   public static class MineShaftCorridor extends MineshaftPieces.MineShaftPiece {
      private final boolean hasRails;
      private final boolean spiderCorridor;
      private boolean hasPlacedSpider;
      private final int numSections;

      public MineShaftCorridor(CompoundTag $$0) {
         super(StructurePieceType.MINE_SHAFT_CORRIDOR, $$0);
         this.hasRails = $$0.getBooleanOr("hr", false);
         this.spiderCorridor = $$0.getBooleanOr("sc", false);
         this.hasPlacedSpider = $$0.getBooleanOr("hps", false);
         this.numSections = $$0.getIntOr("Num", 0);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("hr", this.hasRails);
         $$1.putBoolean("sc", this.spiderCorridor);
         $$1.putBoolean("hps", this.hasPlacedSpider);
         $$1.putInt("Num", this.numSections);
      }

      public MineShaftCorridor(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3, MineshaftStructure.Type $$4) {
         super(StructurePieceType.MINE_SHAFT_CORRIDOR, $$0, $$4, $$2);
         this.setOrientation($$3);
         this.hasRails = $$1.nextInt(3) == 0;
         this.spiderCorridor = !this.hasRails && $$1.nextInt(23) == 0;
         if (this.getOrientation().getAxis() == Axis.Z) {
            this.numSections = $$2.getZSpan() / 5;
         } else {
            this.numSections = $$2.getXSpan() / 5;
         }
      }

      
      public static BoundingBox findCorridorSize(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5) {
         for (int $$6 = $$1.nextInt(3) + 2; $$6 > 0; $$6--) {
            int $$7 = $$6 * 5;

            BoundingBox $$11 = switch ($$5) {
               default -> new BoundingBox(0, 0, -($$7 - 1), 2, 2, 0);
               case SOUTH -> new BoundingBox(0, 0, 0, 2, 2, $$7 - 1);
               case WEST -> new BoundingBox(-($$7 - 1), 0, 0, 0, 2, 2);
               case EAST -> new BoundingBox(0, 0, 0, $$7 - 1, 2, 2);
            };
            $$11.move($$2, $$3, $$4);
            if ($$0.findCollisionPiece($$11) == null) {
               return $$11;
            }
         }

         return null;
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         int $$3 = this.getGenDepth();
         int $$4 = $$2.nextInt(4);
         Direction $$5 = this.getOrientation();
         if ($$5 != null) {
            switch ($$5) {
               case NORTH:
               default:
                  if ($$4 <= 1) {
                     MineshaftPieces.generateAndAddPiece(
                        $$0, $$1, $$2, this.boundingBox.minX(), this.boundingBox.minY() - 1 + $$2.nextInt(3), this.boundingBox.minZ() - 1, $$5, $$3
                     );
                  } else if ($$4 == 2) {
                     MineshaftPieces.generateAndAddPiece(
                        $$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + $$2.nextInt(3), this.boundingBox.minZ(), Direction.WEST, $$3
                     );
                  } else {
                     MineshaftPieces.generateAndAddPiece(
                        $$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + $$2.nextInt(3), this.boundingBox.minZ(), Direction.EAST, $$3
                     );
                  }
                  break;
               case SOUTH:
                  if ($$4 <= 1) {
                     MineshaftPieces.generateAndAddPiece(
                        $$0, $$1, $$2, this.boundingBox.minX(), this.boundingBox.minY() - 1 + $$2.nextInt(3), this.boundingBox.maxZ() + 1, $$5, $$3
                     );
                  } else if ($$4 == 2) {
                     MineshaftPieces.generateAndAddPiece(
                        $$0,
                        $$1,
                        $$2,
                        this.boundingBox.minX() - 1,
                        this.boundingBox.minY() - 1 + $$2.nextInt(3),
                        this.boundingBox.maxZ() - 3,
                        Direction.WEST,
                        $$3
                     );
                  } else {
                     MineshaftPieces.generateAndAddPiece(
                        $$0,
                        $$1,
                        $$2,
                        this.boundingBox.maxX() + 1,
                        this.boundingBox.minY() - 1 + $$2.nextInt(3),
                        this.boundingBox.maxZ() - 3,
                        Direction.EAST,
                        $$3
                     );
                  }
                  break;
               case WEST:
                  if ($$4 <= 1) {
                     MineshaftPieces.generateAndAddPiece(
                        $$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + $$2.nextInt(3), this.boundingBox.minZ(), $$5, $$3
                     );
                  } else if ($$4 == 2) {
                     MineshaftPieces.generateAndAddPiece(
                        $$0, $$1, $$2, this.boundingBox.minX(), this.boundingBox.minY() - 1 + $$2.nextInt(3), this.boundingBox.minZ() - 1, Direction.NORTH, $$3
                     );
                  } else {
                     MineshaftPieces.generateAndAddPiece(
                        $$0, $$1, $$2, this.boundingBox.minX(), this.boundingBox.minY() - 1 + $$2.nextInt(3), this.boundingBox.maxZ() + 1, Direction.SOUTH, $$3
                     );
                  }
                  break;
               case EAST:
                  if ($$4 <= 1) {
                     MineshaftPieces.generateAndAddPiece(
                        $$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + $$2.nextInt(3), this.boundingBox.minZ(), $$5, $$3
                     );
                  } else if ($$4 == 2) {
                     MineshaftPieces.generateAndAddPiece(
                        $$0,
                        $$1,
                        $$2,
                        this.boundingBox.maxX() - 3,
                        this.boundingBox.minY() - 1 + $$2.nextInt(3),
                        this.boundingBox.minZ() - 1,
                        Direction.NORTH,
                        $$3
                     );
                  } else {
                     MineshaftPieces.generateAndAddPiece(
                        $$0,
                        $$1,
                        $$2,
                        this.boundingBox.maxX() - 3,
                        this.boundingBox.minY() - 1 + $$2.nextInt(3),
                        this.boundingBox.maxZ() + 1,
                        Direction.SOUTH,
                        $$3
                     );
                  }
            }
         }

         if ($$3 < 8) {
            if ($$5 != Direction.NORTH && $$5 != Direction.SOUTH) {
               for (int $$8 = this.boundingBox.minX() + 3; $$8 + 3 <= this.boundingBox.maxX(); $$8 += 5) {
                  int $$9 = $$2.nextInt(5);
                  if ($$9 == 0) {
                     MineshaftPieces.generateAndAddPiece($$0, $$1, $$2, $$8, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, $$3 + 1);
                  } else if ($$9 == 1) {
                     MineshaftPieces.generateAndAddPiece($$0, $$1, $$2, $$8, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, $$3 + 1);
                  }
               }
            } else {
               for (int $$6 = this.boundingBox.minZ() + 3; $$6 + 3 <= this.boundingBox.maxZ(); $$6 += 5) {
                  int $$7 = $$2.nextInt(5);
                  if ($$7 == 0) {
                     MineshaftPieces.generateAndAddPiece($$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY(), $$6, Direction.WEST, $$3 + 1);
                  } else if ($$7 == 1) {
                     MineshaftPieces.generateAndAddPiece($$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY(), $$6, Direction.EAST, $$3 + 1);
                  }
               }
            }
         }
      }

      @Override
      protected boolean createChest(
         net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, RandomSource $$2, int $$3, int $$4, int $$5, ResourceKey<LootTable> $$6
      ) {
         BlockPos $$7 = this.getWorldPos($$3, $$4, $$5);
         if ($$1.isInside($$7) && $$0.getBlockState($$7).isAir() && !$$0.getBlockState($$7.below()).isAir()) {
            BlockState $$8 = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, $$2.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
            this.placeBlock($$0, $$8, $$3, $$4, $$5, $$1);
            MinecartChest $$9 = (MinecartChest)EntityType.CHEST_MINECART.create($$0.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
            if ($$9 != null) {
               $$9.setInitialPos($$7.getX() + 0.5, $$7.getY() + 0.5, $$7.getZ() + 0.5);
               $$9.setLootTable($$6, $$2.nextLong());
               $$0.addFreshEntity($$9);
            }

            return true;
         } else {
            return false;
         }
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
         if (!this.isInInvalidLocation($$0, $$4)) {
            int $$7 = 0;
            int $$8 = 2;
            int $$9 = 0;
            int $$10 = 2;
            int $$11 = this.numSections * 5 - 1;
            BlockState $$12 = this.type.getPlanksState();
            this.generateBox($$0, $$4, 0, 0, 0, 2, 1, $$11, CAVE_AIR, CAVE_AIR, false);
            this.generateMaybeBox($$0, $$4, $$3, 0.8F, 0, 2, 0, 2, 2, $$11, CAVE_AIR, CAVE_AIR, false, false);
            if (this.spiderCorridor) {
               this.generateMaybeBox($$0, $$4, $$3, 0.6F, 0, 0, 0, 2, 1, $$11, Blocks.COBWEB.defaultBlockState(), CAVE_AIR, false, true);
            }

            for (int $$13 = 0; $$13 < this.numSections; $$13++) {
               int $$14 = 2 + $$13 * 5;
               this.placeSupport($$0, $$4, 0, 0, $$14, 2, 2, $$3);
               this.maybePlaceCobWeb($$0, $$4, $$3, 0.1F, 0, 2, $$14 - 1);
               this.maybePlaceCobWeb($$0, $$4, $$3, 0.1F, 2, 2, $$14 - 1);
               this.maybePlaceCobWeb($$0, $$4, $$3, 0.1F, 0, 2, $$14 + 1);
               this.maybePlaceCobWeb($$0, $$4, $$3, 0.1F, 2, 2, $$14 + 1);
               this.maybePlaceCobWeb($$0, $$4, $$3, 0.05F, 0, 2, $$14 - 2);
               this.maybePlaceCobWeb($$0, $$4, $$3, 0.05F, 2, 2, $$14 - 2);
               this.maybePlaceCobWeb($$0, $$4, $$3, 0.05F, 0, 2, $$14 + 2);
               this.maybePlaceCobWeb($$0, $$4, $$3, 0.05F, 2, 2, $$14 + 2);
               if ($$3.nextInt(100) == 0) {
                  this.createChest($$0, $$4, $$3, 2, 0, $$14 - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
               }

               if ($$3.nextInt(100) == 0) {
                  this.createChest($$0, $$4, $$3, 0, 0, $$14 + 1, BuiltInLootTables.ABANDONED_MINESHAFT);
               }

               if (this.spiderCorridor && !this.hasPlacedSpider) {
                  int $$15 = 1;
                  int $$16 = $$14 - 1 + $$3.nextInt(3);
                  BlockPos $$17 = this.getWorldPos(1, 0, $$16);
                  if ($$4.isInside($$17) && this.isInterior($$0, 1, 0, $$16, $$4)) {
                     this.hasPlacedSpider = true;
                     $$0.setBlock($$17, Blocks.SPAWNER.defaultBlockState(), 2);
                     if ($$0.getBlockEntity($$17) instanceof SpawnerBlockEntity $$19) {
                        $$19.setEntityId(EntityType.CAVE_SPIDER, $$3);
                     }
                  }
               }
            }

            for (int $$20 = 0; $$20 <= 2; $$20++) {
               for (int $$21 = 0; $$21 <= $$11; $$21++) {
                  this.setPlanksBlock($$0, $$4, $$12, $$20, -1, $$21);
               }
            }

            int $$22 = 2;
            this.placeDoubleLowerOrUpperSupport($$0, $$4, 0, -1, 2);
            if (this.numSections > 1) {
               int $$23 = $$11 - 2;
               this.placeDoubleLowerOrUpperSupport($$0, $$4, 0, -1, $$23);
            }

            if (this.hasRails) {
               BlockState $$24 = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH);

               for (int $$25 = 0; $$25 <= $$11; $$25++) {
                  BlockState $$26 = this.getBlock($$0, 1, -1, $$25, $$4);
                  if (!$$26.isAir() && $$26.isSolidRender()) {
                     float $$27 = this.isInterior($$0, 1, 0, $$25, $$4) ? 0.7F : 0.9F;
                     this.maybeGenerateBlock($$0, $$4, $$3, $$27, 1, 0, $$25, $$24);
                  }
               }
            }
         }
      }

      private void placeDoubleLowerOrUpperSupport(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, int $$2, int $$3, int $$4) {
         BlockState $$5 = this.type.getWoodState();
         BlockState $$6 = this.type.getPlanksState();
         if (this.getBlock($$0, $$2, $$3, $$4, $$1).is($$6.getBlock())) {
            this.fillPillarDownOrChainUp($$0, $$5, $$2, $$3, $$4, $$1);
         }

         if (this.getBlock($$0, $$2 + 2, $$3, $$4, $$1).is($$6.getBlock())) {
            this.fillPillarDownOrChainUp($$0, $$5, $$2 + 2, $$3, $$4, $$1);
         }
      }

      @Override
      protected void fillColumnDown(net.minecraft.world.level.WorldGenLevel $$0, BlockState $$1, int $$2, int $$3, int $$4, BoundingBox $$5) {
         MutableBlockPos $$6 = this.getWorldPos($$2, $$3, $$4);
         if ($$5.isInside($$6)) {
            int $$7 = $$6.getY();

            while (this.isReplaceableByStructures($$0.getBlockState($$6)) && $$6.getY() > $$0.getMinY() + 1) {
               $$6.move(Direction.DOWN);
            }

            if (this.canPlaceColumnOnTopOf($$0, $$6, $$0.getBlockState($$6))) {
               while ($$6.getY() < $$7) {
                  $$6.move(Direction.UP);
                  $$0.setBlock($$6, $$1, 2);
               }
            }
         }
      }

      protected void fillPillarDownOrChainUp(net.minecraft.world.level.WorldGenLevel $$0, BlockState $$1, int $$2, int $$3, int $$4, BoundingBox $$5) {
         MutableBlockPos $$6 = this.getWorldPos($$2, $$3, $$4);
         if ($$5.isInside($$6)) {
            int $$7 = $$6.getY();
            int $$8 = 1;
            boolean $$9 = true;

            for (boolean $$10 = true; $$9 || $$10; $$8++) {
               if ($$9) {
                  $$6.setY($$7 - $$8);
                  BlockState $$11 = $$0.getBlockState($$6);
                  boolean $$12 = this.isReplaceableByStructures($$11) && !$$11.is(Blocks.LAVA);
                  if (!$$12 && this.canPlaceColumnOnTopOf($$0, $$6, $$11)) {
                     fillColumnBetween($$0, $$1, $$6, $$7 - $$8 + 1, $$7);
                     return;
                  }

                  $$9 = $$8 <= 20 && $$12 && $$6.getY() > $$0.getMinY() + 1;
               }

               if ($$10) {
                  $$6.setY($$7 + $$8);
                  BlockState $$13 = $$0.getBlockState($$6);
                  boolean $$14 = this.isReplaceableByStructures($$13);
                  if (!$$14 && this.canHangChainBelow($$0, $$6, $$13)) {
                     $$0.setBlock($$6.setY($$7 + 1), this.type.getFenceState(), 2);
                     fillColumnBetween($$0, Blocks.IRON_CHAIN.defaultBlockState(), $$6, $$7 + 2, $$7 + $$8);
                     return;
                  }

                  $$10 = $$8 <= 50 && $$14 && $$6.getY() < $$0.getMaxY();
               }
            }
         }
      }

      private static void fillColumnBetween(net.minecraft.world.level.WorldGenLevel $$0, BlockState $$1, MutableBlockPos $$2, int $$3, int $$4) {
         for (int $$5 = $$3; $$5 < $$4; $$5++) {
            $$0.setBlock($$2.setY($$5), $$1, 2);
         }
      }

      private boolean canPlaceColumnOnTopOf(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
         return $$2.isFaceSturdy($$0, $$1, Direction.UP);
      }

      private boolean canHangChainBelow(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
         return Block.canSupportCenter($$0, $$1, Direction.DOWN) && !($$2.getBlock() instanceof FallingBlock);
      }

      private void placeSupport(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, int $$2, int $$3, int $$4, int $$5, int $$6, RandomSource $$7) {
         if (this.isSupportingBox($$0, $$1, $$2, $$6, $$5, $$4)) {
            BlockState $$8 = this.type.getPlanksState();
            BlockState $$9 = this.type.getFenceState();
            this.generateBox($$0, $$1, $$2, $$3, $$4, $$2, $$5 - 1, $$4, $$9.setValue(FenceBlock.WEST, true), CAVE_AIR, false);
            this.generateBox($$0, $$1, $$6, $$3, $$4, $$6, $$5 - 1, $$4, $$9.setValue(FenceBlock.EAST, true), CAVE_AIR, false);
            if ($$7.nextInt(4) == 0) {
               this.generateBox($$0, $$1, $$2, $$5, $$4, $$2, $$5, $$4, $$8, CAVE_AIR, false);
               this.generateBox($$0, $$1, $$6, $$5, $$4, $$6, $$5, $$4, $$8, CAVE_AIR, false);
            } else {
               this.generateBox($$0, $$1, $$2, $$5, $$4, $$6, $$5, $$4, $$8, CAVE_AIR, false);
               this.maybeGenerateBlock(
                  $$0, $$1, $$7, 0.05F, $$2 + 1, $$5, $$4 - 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH)
               );
               this.maybeGenerateBlock(
                  $$0, $$1, $$7, 0.05F, $$2 + 1, $$5, $$4 + 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH)
               );
            }
         }
      }

      private void maybePlaceCobWeb(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, RandomSource $$2, float $$3, int $$4, int $$5, int $$6) {
         if (this.isInterior($$0, $$4, $$5, $$6, $$1) && $$2.nextFloat() < $$3 && this.hasSturdyNeighbours($$0, $$1, $$4, $$5, $$6, 2)) {
            this.placeBlock($$0, Blocks.COBWEB.defaultBlockState(), $$4, $$5, $$6, $$1);
         }
      }

      private boolean hasSturdyNeighbours(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, int $$2, int $$3, int $$4, int $$5) {
         MutableBlockPos $$6 = this.getWorldPos($$2, $$3, $$4);
         int $$7 = 0;

         for (Direction $$8 : Direction.values()) {
            $$6.move($$8);
            if ($$1.isInside($$6) && $$0.getBlockState($$6).isFaceSturdy($$0, $$6, $$8.getOpposite())) {
               if (++$$7 >= $$5) {
                  return true;
               }
            }

            $$6.move($$8.getOpposite());
         }

         return false;
      }
   }

   public static class MineShaftCrossing extends MineshaftPieces.MineShaftPiece {
      private final Direction direction;
      private final boolean isTwoFloored;

      public MineShaftCrossing(CompoundTag $$0) {
         super(StructurePieceType.MINE_SHAFT_CROSSING, $$0);
         this.isTwoFloored = $$0.getBooleanOr("tf", false);
         this.direction = $$0.read("D", Direction.LEGACY_ID_CODEC_2D).orElse(Direction.SOUTH);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("tf", this.isTwoFloored);
         $$1.store("D", Direction.LEGACY_ID_CODEC_2D, this.direction);
      }

      public MineShaftCrossing(int $$0, BoundingBox $$1, Direction $$2, MineshaftStructure.Type $$3) {
         super(StructurePieceType.MINE_SHAFT_CROSSING, $$0, $$3, $$1);
         this.direction = $$2;
         this.isTwoFloored = $$1.getYSpan() > 3;
      }

      
      public static BoundingBox findCrossing(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5) {
         int $$6;
         if ($$1.nextInt(4) == 0) {
            $$6 = 6;
         } else {
            $$6 = 2;
         }
         BoundingBox $$11 = switch ($$5) {
            default -> new BoundingBox(-1, 0, -4, 3, $$6, 0);
            case SOUTH -> new BoundingBox(-1, 0, 0, 3, $$6, 4);
            case WEST -> new BoundingBox(-4, 0, -1, 0, $$6, 3);
            case EAST -> new BoundingBox(0, 0, -1, 4, $$6, 3);
         };
         $$11.move($$2, $$3, $$4);
         return $$0.findCollisionPiece($$11) != null ? null : $$11;
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         int $$3 = this.getGenDepth();
         switch (this.direction) {
            case NORTH:
            default:
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, $$3
               );
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, $$3
               );
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, $$3
               );
               break;
            case SOUTH:
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, $$3
               );
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, $$3
               );
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, $$3
               );
               break;
            case WEST:
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, $$3
               );
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, $$3
               );
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, $$3
               );
               break;
            case EAST:
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, $$3
               );
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, $$3
               );
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, $$3
               );
         }

         if (this.isTwoFloored) {
            if ($$2.nextBoolean()) {
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() - 1, Direction.NORTH, $$3
               );
            }

            if ($$2.nextBoolean()) {
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.WEST, $$3
               );
            }

            if ($$2.nextBoolean()) {
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.EAST, $$3
               );
            }

            if ($$2.nextBoolean()) {
               MineshaftPieces.generateAndAddPiece(
                  $$0, $$1, $$2, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.maxZ() + 1, Direction.SOUTH, $$3
               );
            }
         }
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
         if (!this.isInInvalidLocation($$0, $$4)) {
            BlockState $$7 = this.type.getPlanksState();
            if (this.isTwoFloored) {
               this.generateBox(
                  $$0,
                  $$4,
                  this.boundingBox.minX() + 1,
                  this.boundingBox.minY(),
                  this.boundingBox.minZ(),
                  this.boundingBox.maxX() - 1,
                  this.boundingBox.minY() + 3 - 1,
                  this.boundingBox.maxZ(),
                  CAVE_AIR,
                  CAVE_AIR,
                  false
               );
               this.generateBox(
                  $$0,
                  $$4,
                  this.boundingBox.minX(),
                  this.boundingBox.minY(),
                  this.boundingBox.minZ() + 1,
                  this.boundingBox.maxX(),
                  this.boundingBox.minY() + 3 - 1,
                  this.boundingBox.maxZ() - 1,
                  CAVE_AIR,
                  CAVE_AIR,
                  false
               );
               this.generateBox(
                  $$0,
                  $$4,
                  this.boundingBox.minX() + 1,
                  this.boundingBox.maxY() - 2,
                  this.boundingBox.minZ(),
                  this.boundingBox.maxX() - 1,
                  this.boundingBox.maxY(),
                  this.boundingBox.maxZ(),
                  CAVE_AIR,
                  CAVE_AIR,
                  false
               );
               this.generateBox(
                  $$0,
                  $$4,
                  this.boundingBox.minX(),
                  this.boundingBox.maxY() - 2,
                  this.boundingBox.minZ() + 1,
                  this.boundingBox.maxX(),
                  this.boundingBox.maxY(),
                  this.boundingBox.maxZ() - 1,
                  CAVE_AIR,
                  CAVE_AIR,
                  false
               );
               this.generateBox(
                  $$0,
                  $$4,
                  this.boundingBox.minX() + 1,
                  this.boundingBox.minY() + 3,
                  this.boundingBox.minZ() + 1,
                  this.boundingBox.maxX() - 1,
                  this.boundingBox.minY() + 3,
                  this.boundingBox.maxZ() - 1,
                  CAVE_AIR,
                  CAVE_AIR,
                  false
               );
            } else {
               this.generateBox(
                  $$0,
                  $$4,
                  this.boundingBox.minX() + 1,
                  this.boundingBox.minY(),
                  this.boundingBox.minZ(),
                  this.boundingBox.maxX() - 1,
                  this.boundingBox.maxY(),
                  this.boundingBox.maxZ(),
                  CAVE_AIR,
                  CAVE_AIR,
                  false
               );
               this.generateBox(
                  $$0,
                  $$4,
                  this.boundingBox.minX(),
                  this.boundingBox.minY(),
                  this.boundingBox.minZ() + 1,
                  this.boundingBox.maxX(),
                  this.boundingBox.maxY(),
                  this.boundingBox.maxZ() - 1,
                  CAVE_AIR,
                  CAVE_AIR,
                  false
               );
            }

            this.placeSupportPillar($$0, $$4, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY());
            this.placeSupportPillar($$0, $$4, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY());
            this.placeSupportPillar($$0, $$4, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY());
            this.placeSupportPillar($$0, $$4, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY());
            int $$8 = this.boundingBox.minY() - 1;

            for (int $$9 = this.boundingBox.minX(); $$9 <= this.boundingBox.maxX(); $$9++) {
               for (int $$10 = this.boundingBox.minZ(); $$10 <= this.boundingBox.maxZ(); $$10++) {
                  this.setPlanksBlock($$0, $$4, $$7, $$9, $$8, $$10);
               }
            }
         }
      }

      private void placeSupportPillar(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, int $$2, int $$3, int $$4, int $$5) {
         if (!this.getBlock($$0, $$2, $$5 + 1, $$4, $$1).isAir()) {
            this.generateBox($$0, $$1, $$2, $$3, $$4, $$2, $$5, $$4, this.type.getPlanksState(), CAVE_AIR, false);
         }
      }
   }

   abstract static class MineShaftPiece extends StructurePiece {
      protected MineshaftStructure.Type type;

      public MineShaftPiece(StructurePieceType $$0, int $$1, MineshaftStructure.Type $$2, BoundingBox $$3) {
         super($$0, $$1, $$3);
         this.type = $$2;
      }

      public MineShaftPiece(StructurePieceType $$0, CompoundTag $$1) {
         super($$0, $$1);
         this.type = MineshaftStructure.Type.byId($$1.getIntOr("MST", 0));
      }

      @Override
      protected boolean canBeReplaced(net.minecraft.world.level.LevelReader $$0, int $$1, int $$2, int $$3, BoundingBox $$4) {
         BlockState $$5 = this.getBlock($$0, $$1, $$2, $$3, $$4);
         return !$$5.is(this.type.getPlanksState().getBlock())
            && !$$5.is(this.type.getWoodState().getBlock())
            && !$$5.is(this.type.getFenceState().getBlock())
            && !$$5.is(Blocks.IRON_CHAIN);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         $$1.putInt("MST", this.type.ordinal());
      }

      protected boolean isSupportingBox(net.minecraft.world.level.BlockGetter $$0, BoundingBox $$1, int $$2, int $$3, int $$4, int $$5) {
         for (int $$6 = $$2; $$6 <= $$3; $$6++) {
            if (this.getBlock($$0, $$6, $$4 + 1, $$5, $$1).isAir()) {
               return false;
            }
         }

         return true;
      }

      protected boolean isInInvalidLocation(net.minecraft.world.level.LevelAccessor $$0, BoundingBox $$1) {
         int $$2 = Math.max(this.boundingBox.minX() - 1, $$1.minX());
         int $$3 = Math.max(this.boundingBox.minY() - 1, $$1.minY());
         int $$4 = Math.max(this.boundingBox.minZ() - 1, $$1.minZ());
         int $$5 = Math.min(this.boundingBox.maxX() + 1, $$1.maxX());
         int $$6 = Math.min(this.boundingBox.maxY() + 1, $$1.maxY());
         int $$7 = Math.min(this.boundingBox.maxZ() + 1, $$1.maxZ());
         MutableBlockPos $$8 = new MutableBlockPos(($$2 + $$5) / 2, ($$3 + $$6) / 2, ($$4 + $$7) / 2);
         if ($$0.getBiome($$8).is(BiomeTags.MINESHAFT_BLOCKING)) {
            return true;
         } else {
            for (int $$9 = $$2; $$9 <= $$5; $$9++) {
               for (int $$10 = $$4; $$10 <= $$7; $$10++) {
                  if ($$0.getBlockState($$8.set($$9, $$3, $$10)).liquid()) {
                     return true;
                  }

                  if ($$0.getBlockState($$8.set($$9, $$6, $$10)).liquid()) {
                     return true;
                  }
               }
            }

            for (int $$11 = $$2; $$11 <= $$5; $$11++) {
               for (int $$12 = $$3; $$12 <= $$6; $$12++) {
                  if ($$0.getBlockState($$8.set($$11, $$12, $$4)).liquid()) {
                     return true;
                  }

                  if ($$0.getBlockState($$8.set($$11, $$12, $$7)).liquid()) {
                     return true;
                  }
               }
            }

            for (int $$13 = $$4; $$13 <= $$7; $$13++) {
               for (int $$14 = $$3; $$14 <= $$6; $$14++) {
                  if ($$0.getBlockState($$8.set($$2, $$14, $$13)).liquid()) {
                     return true;
                  }

                  if ($$0.getBlockState($$8.set($$5, $$14, $$13)).liquid()) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      protected void setPlanksBlock(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, BlockState $$2, int $$3, int $$4, int $$5) {
         if (this.isInterior($$0, $$3, $$4, $$5, $$1)) {
            BlockPos $$6 = this.getWorldPos($$3, $$4, $$5);
            BlockState $$7 = $$0.getBlockState($$6);
            if (!$$7.isFaceSturdy($$0, $$6, Direction.UP)) {
               $$0.setBlock($$6, $$2, 2);
            }
         }
      }
   }

   public static class MineShaftRoom extends MineshaftPieces.MineShaftPiece {
      private final List<BoundingBox> childEntranceBoxes = Lists.newLinkedList();

      public MineShaftRoom(int $$0, RandomSource $$1, int $$2, int $$3, MineshaftStructure.Type $$4) {
         super(
            StructurePieceType.MINE_SHAFT_ROOM,
            $$0,
            $$4,
            new BoundingBox($$2, 50, $$3, $$2 + 7 + $$1.nextInt(6), 54 + $$1.nextInt(6), $$3 + 7 + $$1.nextInt(6))
         );
         this.type = $$4;
      }

      public MineShaftRoom(CompoundTag $$0) {
         super(StructurePieceType.MINE_SHAFT_ROOM, $$0);
         this.childEntranceBoxes.addAll($$0.read("Entrances", BoundingBox.CODEC.listOf()).orElse(List.of()));
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         int $$3 = this.getGenDepth();
         int $$4 = this.boundingBox.getYSpan() - 3 - 1;
         if ($$4 <= 0) {
            $$4 = 1;
         }

         int $$5 = 0;

         while ($$5 < this.boundingBox.getXSpan()) {
            $$5 += $$2.nextInt(this.boundingBox.getXSpan());
            if ($$5 + 3 > this.boundingBox.getXSpan()) {
               break;
            }

            MineshaftPieces.MineShaftPiece $$6 = MineshaftPieces.generateAndAddPiece(
               $$0, $$1, $$2, this.boundingBox.minX() + $$5, this.boundingBox.minY() + $$2.nextInt($$4) + 1, this.boundingBox.minZ() - 1, Direction.NORTH, $$3
            );
            if ($$6 != null) {
               BoundingBox $$7 = $$6.getBoundingBox();
               this.childEntranceBoxes
                  .add(new BoundingBox($$7.minX(), $$7.minY(), this.boundingBox.minZ(), $$7.maxX(), $$7.maxY(), this.boundingBox.minZ() + 1));
            }

            $$5 += 4;
         }

         $$5 = 0;

         while ($$5 < this.boundingBox.getXSpan()) {
            $$5 += $$2.nextInt(this.boundingBox.getXSpan());
            if ($$5 + 3 > this.boundingBox.getXSpan()) {
               break;
            }

            MineshaftPieces.MineShaftPiece $$8 = MineshaftPieces.generateAndAddPiece(
               $$0, $$1, $$2, this.boundingBox.minX() + $$5, this.boundingBox.minY() + $$2.nextInt($$4) + 1, this.boundingBox.maxZ() + 1, Direction.SOUTH, $$3
            );
            if ($$8 != null) {
               BoundingBox $$9 = $$8.getBoundingBox();
               this.childEntranceBoxes
                  .add(new BoundingBox($$9.minX(), $$9.minY(), this.boundingBox.maxZ() - 1, $$9.maxX(), $$9.maxY(), this.boundingBox.maxZ()));
            }

            $$5 += 4;
         }

         $$5 = 0;

         while ($$5 < this.boundingBox.getZSpan()) {
            $$5 += $$2.nextInt(this.boundingBox.getZSpan());
            if ($$5 + 3 > this.boundingBox.getZSpan()) {
               break;
            }

            MineshaftPieces.MineShaftPiece $$10 = MineshaftPieces.generateAndAddPiece(
               $$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY() + $$2.nextInt($$4) + 1, this.boundingBox.minZ() + $$5, Direction.WEST, $$3
            );
            if ($$10 != null) {
               BoundingBox $$11 = $$10.getBoundingBox();
               this.childEntranceBoxes
                  .add(new BoundingBox(this.boundingBox.minX(), $$11.minY(), $$11.minZ(), this.boundingBox.minX() + 1, $$11.maxY(), $$11.maxZ()));
            }

            $$5 += 4;
         }

         $$5 = 0;

         while ($$5 < this.boundingBox.getZSpan()) {
            $$5 += $$2.nextInt(this.boundingBox.getZSpan());
            if ($$5 + 3 > this.boundingBox.getZSpan()) {
               break;
            }

            StructurePiece $$12 = MineshaftPieces.generateAndAddPiece(
               $$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY() + $$2.nextInt($$4) + 1, this.boundingBox.minZ() + $$5, Direction.EAST, $$3
            );
            if ($$12 != null) {
               BoundingBox $$13 = $$12.getBoundingBox();
               this.childEntranceBoxes
                  .add(new BoundingBox(this.boundingBox.maxX() - 1, $$13.minY(), $$13.minZ(), this.boundingBox.maxX(), $$13.maxY(), $$13.maxZ()));
            }

            $$5 += 4;
         }
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
         if (!this.isInInvalidLocation($$0, $$4)) {
            this.generateBox(
               $$0,
               $$4,
               this.boundingBox.minX(),
               this.boundingBox.minY() + 1,
               this.boundingBox.minZ(),
               this.boundingBox.maxX(),
               Math.min(this.boundingBox.minY() + 3, this.boundingBox.maxY()),
               this.boundingBox.maxZ(),
               CAVE_AIR,
               CAVE_AIR,
               false
            );

            for (BoundingBox $$7 : this.childEntranceBoxes) {
               this.generateBox($$0, $$4, $$7.minX(), $$7.maxY() - 2, $$7.minZ(), $$7.maxX(), $$7.maxY(), $$7.maxZ(), CAVE_AIR, CAVE_AIR, false);
            }

            this.generateUpperHalfSphere(
               $$0,
               $$4,
               this.boundingBox.minX(),
               this.boundingBox.minY() + 4,
               this.boundingBox.minZ(),
               this.boundingBox.maxX(),
               this.boundingBox.maxY(),
               this.boundingBox.maxZ(),
               CAVE_AIR,
               false
            );
         }
      }

      @Override
      public void move(int $$0, int $$1, int $$2) {
         super.move($$0, $$1, $$2);

         for (BoundingBox $$3 : this.childEntranceBoxes) {
            $$3.move($$0, $$1, $$2);
         }
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.store("Entrances", BoundingBox.CODEC.listOf(), this.childEntranceBoxes);
      }
   }

   public static class MineShaftStairs extends MineshaftPieces.MineShaftPiece {
      public MineShaftStairs(int $$0, BoundingBox $$1, Direction $$2, MineshaftStructure.Type $$3) {
         super(StructurePieceType.MINE_SHAFT_STAIRS, $$0, $$3, $$1);
         this.setOrientation($$2);
      }

      public MineShaftStairs(CompoundTag $$0) {
         super(StructurePieceType.MINE_SHAFT_STAIRS, $$0);
      }

      
      public static BoundingBox findStairs(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5) {
         BoundingBox $$9 = switch ($$5) {
            default -> new BoundingBox(0, -5, -8, 2, 2, 0);
            case SOUTH -> new BoundingBox(0, -5, 0, 2, 2, 8);
            case WEST -> new BoundingBox(-8, -5, 0, 0, 2, 2);
            case EAST -> new BoundingBox(0, -5, 0, 8, 2, 2);
         };
         $$9.move($$2, $$3, $$4);
         return $$0.findCollisionPiece($$9) != null ? null : $$9;
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         int $$3 = this.getGenDepth();
         Direction $$4 = this.getOrientation();
         if ($$4 != null) {
            switch ($$4) {
               case NORTH:
               default:
                  MineshaftPieces.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, $$3
                  );
                  break;
               case SOUTH:
                  MineshaftPieces.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, $$3
                  );
                  break;
               case WEST:
                  MineshaftPieces.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.WEST, $$3
                  );
                  break;
               case EAST:
                  MineshaftPieces.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.EAST, $$3
                  );
            }
         }
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
         if (!this.isInInvalidLocation($$0, $$4)) {
            this.generateBox($$0, $$4, 0, 5, 0, 2, 7, 1, CAVE_AIR, CAVE_AIR, false);
            this.generateBox($$0, $$4, 0, 0, 7, 2, 2, 8, CAVE_AIR, CAVE_AIR, false);

            for (int $$7 = 0; $$7 < 5; $$7++) {
               this.generateBox($$0, $$4, 0, 5 - $$7 - ($$7 < 4 ? 1 : 0), 2 + $$7, 2, 7 - $$7, 2 + $$7, CAVE_AIR, CAVE_AIR, false);
            }
         }
      }
   }
}
