package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public class StrongholdPieces {
   private static final int SMALL_DOOR_WIDTH = 3;
   private static final int SMALL_DOOR_HEIGHT = 3;
   private static final int MAX_DEPTH = 50;
   private static final int LOWEST_Y_POSITION = 10;
   private static final boolean CHECK_AIR = true;
   public static final int MAGIC_START_Y = 64;
   private static final StrongholdPieces.PieceWeight[] STRONGHOLD_PIECE_WEIGHTS = new StrongholdPieces.PieceWeight[]{
      new StrongholdPieces.PieceWeight(StrongholdPieces.Straight.class, 40, 0),
      new StrongholdPieces.PieceWeight(StrongholdPieces.PrisonHall.class, 5, 5),
      new StrongholdPieces.PieceWeight(StrongholdPieces.LeftTurn.class, 20, 0),
      new StrongholdPieces.PieceWeight(StrongholdPieces.RightTurn.class, 20, 0),
      new StrongholdPieces.PieceWeight(StrongholdPieces.RoomCrossing.class, 10, 6),
      new StrongholdPieces.PieceWeight(StrongholdPieces.StraightStairsDown.class, 5, 5),
      new StrongholdPieces.PieceWeight(StrongholdPieces.StairsDown.class, 5, 5),
      new StrongholdPieces.PieceWeight(StrongholdPieces.FiveCrossing.class, 5, 4),
      new StrongholdPieces.PieceWeight(StrongholdPieces.ChestCorridor.class, 5, 4),
      new StrongholdPieces.PieceWeight(StrongholdPieces.Library.class, 10, 2) {
         @Override
         public boolean doPlace(int $$0) {
            return super.doPlace($$0) && $$0 > 4;
         }
      },
      new StrongholdPieces.PieceWeight(StrongholdPieces.PortalRoom.class, 20, 1) {
         @Override
         public boolean doPlace(int $$0) {
            return super.doPlace($$0) && $$0 > 5;
         }
      }
   };
   private static List<StrongholdPieces.PieceWeight> currentPieces;
   @Nullable
   static Class<? extends StrongholdPieces.StrongholdPiece> imposedPiece;
   private static int totalWeight;
   static final StrongholdPieces.SmoothStoneSelector SMOOTH_STONE_SELECTOR = new StrongholdPieces.SmoothStoneSelector();

   public static void resetPieces() {
      currentPieces = Lists.newArrayList();

      for (StrongholdPieces.PieceWeight $$0 : STRONGHOLD_PIECE_WEIGHTS) {
         $$0.placeCount = 0;
         currentPieces.add($$0);
      }

      imposedPiece = null;
   }

   private static boolean updatePieceWeight() {
      boolean $$0 = false;
      totalWeight = 0;

      for (StrongholdPieces.PieceWeight $$1 : currentPieces) {
         if ($$1.maxPlaceCount > 0 && $$1.placeCount < $$1.maxPlaceCount) {
            $$0 = true;
         }

         totalWeight = totalWeight + $$1.weight;
      }

      return $$0;
   }

   @Nullable
   private static StrongholdPieces.StrongholdPiece findAndCreatePieceFactory(
      Class<? extends StrongholdPieces.StrongholdPiece> $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4, int $$5, Direction $$6, int $$7
   ) {
      StrongholdPieces.StrongholdPiece $$8 = null;
      if ($$0 == StrongholdPieces.Straight.class) {
         $$8 = StrongholdPieces.Straight.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$0 == StrongholdPieces.PrisonHall.class) {
         $$8 = StrongholdPieces.PrisonHall.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$0 == StrongholdPieces.LeftTurn.class) {
         $$8 = StrongholdPieces.LeftTurn.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$0 == StrongholdPieces.RightTurn.class) {
         $$8 = StrongholdPieces.RightTurn.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$0 == StrongholdPieces.RoomCrossing.class) {
         $$8 = StrongholdPieces.RoomCrossing.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$0 == StrongholdPieces.StraightStairsDown.class) {
         $$8 = StrongholdPieces.StraightStairsDown.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$0 == StrongholdPieces.StairsDown.class) {
         $$8 = StrongholdPieces.StairsDown.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$0 == StrongholdPieces.FiveCrossing.class) {
         $$8 = StrongholdPieces.FiveCrossing.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$0 == StrongholdPieces.ChestCorridor.class) {
         $$8 = StrongholdPieces.ChestCorridor.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$0 == StrongholdPieces.Library.class) {
         $$8 = StrongholdPieces.Library.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$0 == StrongholdPieces.PortalRoom.class) {
         $$8 = StrongholdPieces.PortalRoom.createPiece($$1, $$3, $$4, $$5, $$6, $$7);
      }

      return $$8;
   }

   @Nullable
   private static StrongholdPieces.StrongholdPiece generatePieceFromSmallDoor(
      StrongholdPieces.StartPiece $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4, int $$5, Direction $$6, int $$7
   ) {
      if (!updatePieceWeight()) {
         return null;
      } else {
         if (imposedPiece != null) {
            StrongholdPieces.StrongholdPiece $$8 = findAndCreatePieceFactory(imposedPiece, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
            imposedPiece = null;
            if ($$8 != null) {
               return $$8;
            }
         }

         int $$9 = 0;

         while ($$9 < 5) {
            $$9++;
            int $$10 = $$2.nextInt(totalWeight);

            for (StrongholdPieces.PieceWeight $$11 : currentPieces) {
               $$10 -= $$11.weight;
               if ($$10 < 0) {
                  if (!$$11.doPlace($$7) || $$11 == $$0.previousPiece) {
                     break;
                  }

                  StrongholdPieces.StrongholdPiece $$12 = findAndCreatePieceFactory($$11.pieceClass, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
                  if ($$12 != null) {
                     $$11.placeCount++;
                     $$0.previousPiece = $$11;
                     if (!$$11.isValid()) {
                        currentPieces.remove($$11);
                     }

                     return $$12;
                  }
               }
            }
         }

         BoundingBox $$13 = StrongholdPieces.FillerCorridor.findPieceBox($$1, $$2, $$3, $$4, $$5, $$6);
         return $$13 != null && $$13.minY() > 1 ? new StrongholdPieces.FillerCorridor($$7, $$13, $$6) : null;
      }
   }

   @Nullable
   static StructurePiece generateAndAddPiece(
      StrongholdPieces.StartPiece $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4, int $$5, Direction $$6, int $$7
   ) {
      if ($$7 > 50) {
         return null;
      } else if (Math.abs($$3 - $$0.getBoundingBox().minX()) <= 112 && Math.abs($$5 - $$0.getBoundingBox().minZ()) <= 112) {
         StructurePiece $$8 = generatePieceFromSmallDoor($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7 + 1);
         if ($$8 != null) {
            $$1.addPiece($$8);
            $$0.pendingChildren.add($$8);
         }

         return $$8;
      } else {
         return null;
      }
   }

   public static class ChestCorridor extends StrongholdPieces.StrongholdPiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 5;
      private static final int DEPTH = 7;
      private boolean hasPlacedChest;

      public ChestCorridor(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, $$0, $$2);
         this.setOrientation($$3);
         this.entryDoor = this.randomSmallDoor($$1);
      }

      public ChestCorridor(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, $$0);
         this.hasPlacedChest = $$0.getBooleanOr("Chest", false);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("Chest", this.hasPlacedChest);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 1);
      }

      @Nullable
      public static StrongholdPieces.ChestCorridor createPiece(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -1, 0, 5, 5, 7, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new StrongholdPieces.ChestCorridor($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 4, 4, 6, true, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, this.entryDoor, 1, 1, 0);
         this.generateSmallDoor($$0, $$3, $$4, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 6);
         this.generateBox($$0, $$4, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(), false);
         this.placeBlock($$0, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 1, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 5, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 2, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 4, $$4);

         for (int $$7 = 2; $$7 <= 4; $$7++) {
            this.placeBlock($$0, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2, 1, $$7, $$4);
         }

         if (!this.hasPlacedChest && $$4.isInside(this.getWorldPos(3, 2, 3))) {
            this.hasPlacedChest = true;
            this.createChest($$0, $$4, $$3, 3, 2, 3, BuiltInLootTables.STRONGHOLD_CORRIDOR);
         }
      }
   }

   public static class FillerCorridor extends StrongholdPieces.StrongholdPiece {
      private final int steps;

      public FillerCorridor(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR, $$0, $$1);
         this.setOrientation($$2);
         this.steps = $$2 != Direction.NORTH && $$2 != Direction.SOUTH ? $$1.getXSpan() : $$1.getZSpan();
      }

      public FillerCorridor(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR, $$0);
         this.steps = $$0.getIntOr("Steps", 0);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putInt("Steps", this.steps);
      }

      @Nullable
      public static BoundingBox findPieceBox(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5) {
         int $$6 = 3;
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -1, 0, 5, 5, 4, $$5);
         StructurePiece $$8 = $$0.findCollisionPiece($$7);
         if ($$8 == null) {
            return null;
         } else {
            if ($$8.getBoundingBox().minY() == $$7.minY()) {
               for (int $$9 = 2; $$9 >= 1; $$9--) {
                  $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -1, 0, 5, 5, $$9, $$5);
                  if (!$$8.getBoundingBox().intersects($$7)) {
                     return BoundingBox.orientBox($$2, $$3, $$4, -1, -1, 0, 5, 5, $$9 + 1, $$5);
                  }
               }
            }

            return null;
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
         for (int $$7 = 0; $$7 < this.steps; $$7++) {
            this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 0, 0, $$7, $$4);
            this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 1, 0, $$7, $$4);
            this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 2, 0, $$7, $$4);
            this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 3, 0, $$7, $$4);
            this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 4, 0, $$7, $$4);

            for (int $$8 = 1; $$8 <= 3; $$8++) {
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 0, $$8, $$7, $$4);
               this.placeBlock($$0, Blocks.CAVE_AIR.defaultBlockState(), 1, $$8, $$7, $$4);
               this.placeBlock($$0, Blocks.CAVE_AIR.defaultBlockState(), 2, $$8, $$7, $$4);
               this.placeBlock($$0, Blocks.CAVE_AIR.defaultBlockState(), 3, $$8, $$7, $$4);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 4, $$8, $$7, $$4);
            }

            this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 0, 4, $$7, $$4);
            this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, $$7, $$4);
            this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, $$7, $$4);
            this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 3, 4, $$7, $$4);
            this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 4, 4, $$7, $$4);
         }
      }
   }

   public static class FiveCrossing extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 10;
      protected static final int HEIGHT = 9;
      protected static final int DEPTH = 11;
      private final boolean leftLow;
      private final boolean leftHigh;
      private final boolean rightLow;
      private final boolean rightHigh;

      public FiveCrossing(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.STRONGHOLD_FIVE_CROSSING, $$0, $$2);
         this.setOrientation($$3);
         this.entryDoor = this.randomSmallDoor($$1);
         this.leftLow = $$1.nextBoolean();
         this.leftHigh = $$1.nextBoolean();
         this.rightLow = $$1.nextBoolean();
         this.rightHigh = $$1.nextInt(3) > 0;
      }

      public FiveCrossing(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_FIVE_CROSSING, $$0);
         this.leftLow = $$0.getBooleanOr("leftLow", false);
         this.leftHigh = $$0.getBooleanOr("leftHigh", false);
         this.rightLow = $$0.getBooleanOr("rightLow", false);
         this.rightHigh = $$0.getBooleanOr("rightHigh", false);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("leftLow", this.leftLow);
         $$1.putBoolean("leftHigh", this.leftHigh);
         $$1.putBoolean("rightLow", this.rightLow);
         $$1.putBoolean("rightHigh", this.rightHigh);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         int $$3 = 3;
         int $$4 = 5;
         Direction $$5 = this.getOrientation();
         if ($$5 == Direction.WEST || $$5 == Direction.NORTH) {
            $$3 = 8 - $$3;
            $$4 = 8 - $$4;
         }

         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)$$0, $$1, $$2, 5, 1);
         if (this.leftLow) {
            this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)$$0, $$1, $$2, $$3, 1);
         }

         if (this.leftHigh) {
            this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)$$0, $$1, $$2, $$4, 7);
         }

         if (this.rightLow) {
            this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)$$0, $$1, $$2, $$3, 1);
         }

         if (this.rightHigh) {
            this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)$$0, $$1, $$2, $$4, 7);
         }
      }

      @Nullable
      public static StrongholdPieces.FiveCrossing createPiece(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -4, -3, 0, 10, 9, 11, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new StrongholdPieces.FiveCrossing($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 9, 8, 10, true, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, this.entryDoor, 4, 3, 0);
         if (this.leftLow) {
            this.generateBox($$0, $$4, 0, 3, 1, 0, 5, 3, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.rightLow) {
            this.generateBox($$0, $$4, 9, 3, 1, 9, 5, 3, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.leftHigh) {
            this.generateBox($$0, $$4, 0, 5, 7, 0, 7, 9, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.rightHigh) {
            this.generateBox($$0, $$4, 9, 5, 7, 9, 7, 9, CAVE_AIR, CAVE_AIR, false);
         }

         this.generateBox($$0, $$4, 5, 1, 10, 7, 3, 10, CAVE_AIR, CAVE_AIR, false);
         this.generateBox($$0, $$4, 1, 2, 1, 8, 2, 6, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 4, 1, 5, 4, 4, 9, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 8, 1, 5, 8, 4, 9, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 1, 4, 7, 3, 4, 9, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 1, 3, 5, 3, 3, 6, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 1, 7, 7, 1, 8, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox($$0, $$4, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(
            $$0,
            $$4,
            5,
            5,
            7,
            7,
            5,
            9,
            Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE),
            Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE),
            false
         );
         this.placeBlock($$0, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, $$4);
      }
   }

   public static class LeftTurn extends StrongholdPieces.Turn {
      public LeftTurn(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.STRONGHOLD_LEFT_TURN, $$0, $$2);
         this.setOrientation($$3);
         this.entryDoor = this.randomSmallDoor($$1);
      }

      public LeftTurn(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_LEFT_TURN, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         Direction $$3 = this.getOrientation();
         if ($$3 != Direction.NORTH && $$3 != Direction.EAST) {
            this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 1);
         } else {
            this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 1);
         }
      }

      @Nullable
      public static StrongholdPieces.LeftTurn createPiece(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -1, 0, 5, 5, 5, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new StrongholdPieces.LeftTurn($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 4, 4, 4, true, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, this.entryDoor, 1, 1, 0);
         Direction $$7 = this.getOrientation();
         if ($$7 != Direction.NORTH && $$7 != Direction.EAST) {
            this.generateBox($$0, $$4, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
         } else {
            this.generateBox($$0, $$4, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
         }
      }
   }

   public static class Library extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 14;
      protected static final int HEIGHT = 6;
      protected static final int TALL_HEIGHT = 11;
      protected static final int DEPTH = 15;
      private final boolean isTall;

      public Library(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.STRONGHOLD_LIBRARY, $$0, $$2);
         this.setOrientation($$3);
         this.entryDoor = this.randomSmallDoor($$1);
         this.isTall = $$2.getYSpan() > 6;
      }

      public Library(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_LIBRARY, $$0);
         this.isTall = $$0.getBooleanOr("Tall", false);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("Tall", this.isTall);
      }

      @Nullable
      public static StrongholdPieces.Library createPiece(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -4, -1, 0, 14, 11, 15, $$5);
         if (!isOkBox($$7) || $$0.findCollisionPiece($$7) != null) {
            $$7 = BoundingBox.orientBox($$2, $$3, $$4, -4, -1, 0, 14, 6, 15, $$5);
            if (!isOkBox($$7) || $$0.findCollisionPiece($$7) != null) {
               return null;
            }
         }

         return new StrongholdPieces.Library($$6, $$1, $$7, $$5);
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
         int $$7 = 11;
         if (!this.isTall) {
            $$7 = 6;
         }

         this.generateBox($$0, $$4, 0, 0, 0, 13, $$7 - 1, 14, true, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, this.entryDoor, 4, 1, 0);
         this.generateMaybeBox($$0, $$4, $$3, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.defaultBlockState(), Blocks.COBWEB.defaultBlockState(), false, false);
         int $$8 = 1;
         int $$9 = 12;

         for (int $$10 = 1; $$10 <= 13; $$10++) {
            if (($$10 - 1) % 4 == 0) {
               this.generateBox($$0, $$4, 1, 1, $$10, 1, 4, $$10, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               this.generateBox($$0, $$4, 12, 1, $$10, 12, 4, $$10, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               this.placeBlock($$0, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 2, 3, $$10, $$4);
               this.placeBlock($$0, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 11, 3, $$10, $$4);
               if (this.isTall) {
                  this.generateBox($$0, $$4, 1, 6, $$10, 1, 9, $$10, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                  this.generateBox($$0, $$4, 12, 6, $$10, 12, 9, $$10, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               }
            } else {
               this.generateBox($$0, $$4, 1, 1, $$10, 1, 4, $$10, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
               this.generateBox($$0, $$4, 12, 1, $$10, 12, 4, $$10, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
               if (this.isTall) {
                  this.generateBox($$0, $$4, 1, 6, $$10, 1, 9, $$10, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                  this.generateBox($$0, $$4, 12, 6, $$10, 12, 9, $$10, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
               }
            }
         }

         for (int $$11 = 3; $$11 < 12; $$11 += 2) {
            this.generateBox($$0, $$4, 3, 1, $$11, 4, 3, $$11, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
            this.generateBox($$0, $$4, 6, 1, $$11, 7, 3, $$11, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
            this.generateBox($$0, $$4, 9, 1, $$11, 10, 3, $$11, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
         }

         if (this.isTall) {
            this.generateBox($$0, $$4, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.generateBox($$0, $$4, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.generateBox($$0, $$4, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.generateBox($$0, $$4, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.placeBlock($$0, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 11, $$4);
            this.placeBlock($$0, Blocks.OAK_PLANKS.defaultBlockState(), 8, 5, 11, $$4);
            this.placeBlock($$0, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 10, $$4);
            BlockState $$12 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
            BlockState $$13 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
            this.generateBox($$0, $$4, 3, 6, 3, 3, 6, 11, $$13, $$13, false);
            this.generateBox($$0, $$4, 10, 6, 3, 10, 6, 9, $$13, $$13, false);
            this.generateBox($$0, $$4, 4, 6, 2, 9, 6, 2, $$12, $$12, false);
            this.generateBox($$0, $$4, 4, 6, 12, 7, 6, 12, $$12, $$12, false);
            this.placeBlock($$0, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.EAST, true), 3, 6, 2, $$4);
            this.placeBlock($$0, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true).setValue(FenceBlock.EAST, true), 3, 6, 12, $$4);
            this.placeBlock($$0, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.WEST, true), 10, 6, 2, $$4);

            for (int $$14 = 0; $$14 <= 2; $$14++) {
               this.placeBlock(
                  $$0, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true).setValue(FenceBlock.WEST, true), 8 + $$14, 6, 12 - $$14, $$4
               );
               if ($$14 != 2) {
                  this.placeBlock(
                     $$0, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.EAST, true), 8 + $$14, 6, 11 - $$14, $$4
                  );
               }
            }

            BlockState $$15 = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
            this.placeBlock($$0, $$15, 10, 1, 13, $$4);
            this.placeBlock($$0, $$15, 10, 2, 13, $$4);
            this.placeBlock($$0, $$15, 10, 3, 13, $$4);
            this.placeBlock($$0, $$15, 10, 4, 13, $$4);
            this.placeBlock($$0, $$15, 10, 5, 13, $$4);
            this.placeBlock($$0, $$15, 10, 6, 13, $$4);
            this.placeBlock($$0, $$15, 10, 7, 13, $$4);
            int $$16 = 7;
            int $$17 = 7;
            BlockState $$18 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true);
            this.placeBlock($$0, $$18, 6, 9, 7, $$4);
            BlockState $$19 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true);
            this.placeBlock($$0, $$19, 7, 9, 7, $$4);
            this.placeBlock($$0, $$18, 6, 8, 7, $$4);
            this.placeBlock($$0, $$19, 7, 8, 7, $$4);
            BlockState $$20 = $$13.setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
            this.placeBlock($$0, $$20, 6, 7, 7, $$4);
            this.placeBlock($$0, $$20, 7, 7, 7, $$4);
            this.placeBlock($$0, $$18, 5, 7, 7, $$4);
            this.placeBlock($$0, $$19, 8, 7, 7, $$4);
            this.placeBlock($$0, $$18.setValue(FenceBlock.NORTH, true), 6, 7, 6, $$4);
            this.placeBlock($$0, $$18.setValue(FenceBlock.SOUTH, true), 6, 7, 8, $$4);
            this.placeBlock($$0, $$19.setValue(FenceBlock.NORTH, true), 7, 7, 6, $$4);
            this.placeBlock($$0, $$19.setValue(FenceBlock.SOUTH, true), 7, 7, 8, $$4);
            BlockState $$21 = Blocks.TORCH.defaultBlockState();
            this.placeBlock($$0, $$21, 5, 8, 7, $$4);
            this.placeBlock($$0, $$21, 8, 8, 7, $$4);
            this.placeBlock($$0, $$21, 6, 8, 6, $$4);
            this.placeBlock($$0, $$21, 6, 8, 8, $$4);
            this.placeBlock($$0, $$21, 7, 8, 6, $$4);
            this.placeBlock($$0, $$21, 7, 8, 8, $$4);
         }

         this.createChest($$0, $$4, $$3, 3, 3, 5, BuiltInLootTables.STRONGHOLD_LIBRARY);
         if (this.isTall) {
            this.placeBlock($$0, CAVE_AIR, 12, 9, 1, $$4);
            this.createChest($$0, $$4, $$3, 12, 8, 1, BuiltInLootTables.STRONGHOLD_LIBRARY);
         }
      }
   }

   static class PieceWeight {
      public final Class<? extends StrongholdPieces.StrongholdPiece> pieceClass;
      public final int weight;
      public int placeCount;
      public final int maxPlaceCount;

      public PieceWeight(Class<? extends StrongholdPieces.StrongholdPiece> $$0, int $$1, int $$2) {
         this.pieceClass = $$0;
         this.weight = $$1;
         this.maxPlaceCount = $$2;
      }

      public boolean doPlace(int $$0) {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }

      public boolean isValid() {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }
   }

   public static class PortalRoom extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 11;
      protected static final int HEIGHT = 8;
      protected static final int DEPTH = 16;
      private boolean hasPlacedSpawner;

      public PortalRoom(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, $$0, $$1);
         this.setOrientation($$2);
      }

      public PortalRoom(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, $$0);
         this.hasPlacedSpawner = $$0.getBooleanOr("Mob", false);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("Mob", this.hasPlacedSpawner);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         if ($$0 != null) {
            ((StrongholdPieces.StartPiece)$$0).portalRoomPiece = this;
         }
      }

      @Nullable
      public static StrongholdPieces.PortalRoom createPiece(StructurePieceAccessor $$0, int $$1, int $$2, int $$3, Direction $$4, int $$5) {
         BoundingBox $$6 = BoundingBox.orientBox($$1, $$2, $$3, -4, -1, 0, 11, 8, 16, $$4);
         return isOkBox($$6) && $$0.findCollisionPiece($$6) == null ? new StrongholdPieces.PortalRoom($$5, $$6, $$4) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 10, 7, 15, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, StrongholdPieces.StrongholdPiece.SmallDoorType.GRATES, 4, 1, 0);
         int $$7 = 6;
         this.generateBox($$0, $$4, 1, 6, 1, 1, 6, 14, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 9, 6, 1, 9, 6, 14, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 2, 6, 1, 8, 6, 2, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 2, 6, 14, 8, 6, 14, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 1, 1, 1, 2, 1, 4, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 8, 1, 1, 9, 1, 4, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 1, 1, 1, 1, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         this.generateBox($$0, $$4, 9, 1, 1, 9, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         this.generateBox($$0, $$4, 3, 1, 8, 7, 1, 12, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 4, 1, 9, 6, 1, 11, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         BlockState $$8 = Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true);
         BlockState $$9 = Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true).setValue(IronBarsBlock.EAST, true);

         for (int $$10 = 3; $$10 < 14; $$10 += 2) {
            this.generateBox($$0, $$4, 0, 3, $$10, 0, 4, $$10, $$8, $$8, false);
            this.generateBox($$0, $$4, 10, 3, $$10, 10, 4, $$10, $$8, $$8, false);
         }

         for (int $$11 = 2; $$11 < 9; $$11 += 2) {
            this.generateBox($$0, $$4, $$11, 3, 15, $$11, 4, 15, $$9, $$9, false);
         }

         BlockState $$12 = Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
         this.generateBox($$0, $$4, 4, 1, 5, 6, 1, 7, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 4, 2, 6, 6, 2, 7, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 4, 3, 7, 6, 3, 7, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);

         for (int $$13 = 4; $$13 <= 6; $$13++) {
            this.placeBlock($$0, $$12, $$13, 1, 4, $$4);
            this.placeBlock($$0, $$12, $$13, 2, 5, $$4);
            this.placeBlock($$0, $$12, $$13, 3, 6, $$4);
         }

         BlockState $$14 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.NORTH);
         BlockState $$15 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
         BlockState $$16 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.EAST);
         BlockState $$17 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.WEST);
         boolean $$18 = true;
         boolean[] $$19 = new boolean[12];

         for (int $$20 = 0; $$20 < $$19.length; $$20++) {
            $$19[$$20] = $$3.nextFloat() > 0.9F;
            $$18 &= $$19[$$20];
         }

         this.placeBlock($$0, $$14.setValue(EndPortalFrameBlock.HAS_EYE, $$19[0]), 4, 3, 8, $$4);
         this.placeBlock($$0, $$14.setValue(EndPortalFrameBlock.HAS_EYE, $$19[1]), 5, 3, 8, $$4);
         this.placeBlock($$0, $$14.setValue(EndPortalFrameBlock.HAS_EYE, $$19[2]), 6, 3, 8, $$4);
         this.placeBlock($$0, $$15.setValue(EndPortalFrameBlock.HAS_EYE, $$19[3]), 4, 3, 12, $$4);
         this.placeBlock($$0, $$15.setValue(EndPortalFrameBlock.HAS_EYE, $$19[4]), 5, 3, 12, $$4);
         this.placeBlock($$0, $$15.setValue(EndPortalFrameBlock.HAS_EYE, $$19[5]), 6, 3, 12, $$4);
         this.placeBlock($$0, $$16.setValue(EndPortalFrameBlock.HAS_EYE, $$19[6]), 3, 3, 9, $$4);
         this.placeBlock($$0, $$16.setValue(EndPortalFrameBlock.HAS_EYE, $$19[7]), 3, 3, 10, $$4);
         this.placeBlock($$0, $$16.setValue(EndPortalFrameBlock.HAS_EYE, $$19[8]), 3, 3, 11, $$4);
         this.placeBlock($$0, $$17.setValue(EndPortalFrameBlock.HAS_EYE, $$19[9]), 7, 3, 9, $$4);
         this.placeBlock($$0, $$17.setValue(EndPortalFrameBlock.HAS_EYE, $$19[10]), 7, 3, 10, $$4);
         this.placeBlock($$0, $$17.setValue(EndPortalFrameBlock.HAS_EYE, $$19[11]), 7, 3, 11, $$4);
         if ($$18) {
            BlockState $$21 = Blocks.END_PORTAL.defaultBlockState();
            this.placeBlock($$0, $$21, 4, 3, 9, $$4);
            this.placeBlock($$0, $$21, 5, 3, 9, $$4);
            this.placeBlock($$0, $$21, 6, 3, 9, $$4);
            this.placeBlock($$0, $$21, 4, 3, 10, $$4);
            this.placeBlock($$0, $$21, 5, 3, 10, $$4);
            this.placeBlock($$0, $$21, 6, 3, 10, $$4);
            this.placeBlock($$0, $$21, 4, 3, 11, $$4);
            this.placeBlock($$0, $$21, 5, 3, 11, $$4);
            this.placeBlock($$0, $$21, 6, 3, 11, $$4);
         }

         if (!this.hasPlacedSpawner) {
            BlockPos $$22 = this.getWorldPos(5, 3, 6);
            if ($$4.isInside($$22)) {
               this.hasPlacedSpawner = true;
               $$0.setBlock($$22, Blocks.SPAWNER.defaultBlockState(), 2);
               if ($$0.getBlockEntity($$22) instanceof SpawnerBlockEntity $$24) {
                  $$24.setEntityId(EntityType.SILVERFISH, $$3);
               }
            }
         }
      }
   }

   public static class PrisonHall extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 9;
      protected static final int HEIGHT = 5;
      protected static final int DEPTH = 11;

      public PrisonHall(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.STRONGHOLD_PRISON_HALL, $$0, $$2);
         this.setOrientation($$3);
         this.entryDoor = this.randomSmallDoor($$1);
      }

      public PrisonHall(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_PRISON_HALL, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 1);
      }

      @Nullable
      public static StrongholdPieces.PrisonHall createPiece(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -1, 0, 9, 5, 11, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new StrongholdPieces.PrisonHall($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 8, 4, 10, true, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, this.entryDoor, 1, 1, 0);
         this.generateBox($$0, $$4, 1, 1, 10, 3, 3, 10, CAVE_AIR, CAVE_AIR, false);
         this.generateBox($$0, $$4, 4, 1, 1, 4, 3, 1, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 4, 1, 3, 4, 3, 3, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 4, 1, 7, 4, 3, 7, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox($$0, $$4, 4, 1, 9, 4, 3, 9, false, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);

         for (int $$7 = 1; $$7 <= 3; $$7++) {
            this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true), 4, $$7, 4, $$4);
            this.placeBlock(
               $$0,
               Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true).setValue(IronBarsBlock.EAST, true),
               4,
               $$7,
               5,
               $$4
            );
            this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true), 4, $$7, 6, $$4);
            this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true).setValue(IronBarsBlock.EAST, true), 5, $$7, 5, $$4);
            this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true).setValue(IronBarsBlock.EAST, true), 6, $$7, 5, $$4);
            this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true).setValue(IronBarsBlock.EAST, true), 7, $$7, 5, $$4);
         }

         this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true), 4, 3, 2, $$4);
         this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true), 4, 3, 8, $$4);
         BlockState $$8 = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
         BlockState $$9 = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
         this.placeBlock($$0, $$8, 4, 1, 2, $$4);
         this.placeBlock($$0, $$9, 4, 2, 2, $$4);
         this.placeBlock($$0, $$8, 4, 1, 8, $$4);
         this.placeBlock($$0, $$9, 4, 2, 8, $$4);
      }
   }

   public static class RightTurn extends StrongholdPieces.Turn {
      public RightTurn(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.STRONGHOLD_RIGHT_TURN, $$0, $$2);
         this.setOrientation($$3);
         this.entryDoor = this.randomSmallDoor($$1);
      }

      public RightTurn(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_RIGHT_TURN, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         Direction $$3 = this.getOrientation();
         if ($$3 != Direction.NORTH && $$3 != Direction.EAST) {
            this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 1);
         } else {
            this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 1);
         }
      }

      @Nullable
      public static StrongholdPieces.RightTurn createPiece(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -1, 0, 5, 5, 5, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new StrongholdPieces.RightTurn($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 4, 4, 4, true, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, this.entryDoor, 1, 1, 0);
         Direction $$7 = this.getOrientation();
         if ($$7 != Direction.NORTH && $$7 != Direction.EAST) {
            this.generateBox($$0, $$4, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
         } else {
            this.generateBox($$0, $$4, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
         }
      }
   }

   public static class RoomCrossing extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 11;
      protected static final int HEIGHT = 7;
      protected static final int DEPTH = 11;
      protected final int type;

      public RoomCrossing(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.STRONGHOLD_ROOM_CROSSING, $$0, $$2);
         this.setOrientation($$3);
         this.entryDoor = this.randomSmallDoor($$1);
         this.type = $$1.nextInt(5);
      }

      public RoomCrossing(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_ROOM_CROSSING, $$0);
         this.type = $$0.getIntOr("Type", 0);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putInt("Type", this.type);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)$$0, $$1, $$2, 4, 1);
         this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 4);
         this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 4);
      }

      @Nullable
      public static StrongholdPieces.RoomCrossing createPiece(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -4, -1, 0, 11, 7, 11, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new StrongholdPieces.RoomCrossing($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 10, 6, 10, true, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, this.entryDoor, 4, 1, 0);
         this.generateBox($$0, $$4, 4, 1, 10, 6, 3, 10, CAVE_AIR, CAVE_AIR, false);
         this.generateBox($$0, $$4, 0, 1, 4, 0, 3, 6, CAVE_AIR, CAVE_AIR, false);
         this.generateBox($$0, $$4, 10, 1, 4, 10, 3, 6, CAVE_AIR, CAVE_AIR, false);
         switch (this.type) {
            case 0:
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, $$4);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, $$4);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, $$4);
               this.placeBlock($$0, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, $$4);
               this.placeBlock($$0, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, $$4);
               this.placeBlock($$0, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, $$4);
               this.placeBlock($$0, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, $$4);
               this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 4, $$4);
               this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 5, $$4);
               this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 6, $$4);
               this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 4, $$4);
               this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 5, $$4);
               this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 6, $$4);
               this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 4, $$4);
               this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 6, $$4);
               break;
            case 1:
               for (int $$7 = 0; $$7 < 5; $$7++) {
                  this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 3, 1, 3 + $$7, $$4);
                  this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 7, 1, 3 + $$7, $$4);
                  this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 3 + $$7, 1, 3, $$4);
                  this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 3 + $$7, 1, 7, $$4);
               }

               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, $$4);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, $$4);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, $$4);
               this.placeBlock($$0, Blocks.WATER.defaultBlockState(), 5, 4, 5, $$4);
               break;
            case 2:
               for (int $$8 = 1; $$8 <= 9; $$8++) {
                  this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 1, 3, $$8, $$4);
                  this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 9, 3, $$8, $$4);
               }

               for (int $$9 = 1; $$9 <= 9; $$9++) {
                  this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), $$9, 3, 1, $$4);
                  this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), $$9, 3, 9, $$4);
               }

               this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 4, $$4);
               this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 6, $$4);
               this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 4, $$4);
               this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 6, $$4);
               this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 4, 1, 5, $$4);
               this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 6, 1, 5, $$4);
               this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 4, 3, 5, $$4);
               this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 6, 3, 5, $$4);

               for (int $$10 = 1; $$10 <= 3; $$10++) {
                  this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 4, $$10, 4, $$4);
                  this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 6, $$10, 4, $$4);
                  this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 4, $$10, 6, $$4);
                  this.placeBlock($$0, Blocks.COBBLESTONE.defaultBlockState(), 6, $$10, 6, $$4);
               }

               this.placeBlock($$0, Blocks.WALL_TORCH.defaultBlockState(), 5, 3, 5, $$4);

               for (int $$11 = 2; $$11 <= 8; $$11++) {
                  this.placeBlock($$0, Blocks.OAK_PLANKS.defaultBlockState(), 2, 3, $$11, $$4);
                  this.placeBlock($$0, Blocks.OAK_PLANKS.defaultBlockState(), 3, 3, $$11, $$4);
                  if ($$11 <= 3 || $$11 >= 7) {
                     this.placeBlock($$0, Blocks.OAK_PLANKS.defaultBlockState(), 4, 3, $$11, $$4);
                     this.placeBlock($$0, Blocks.OAK_PLANKS.defaultBlockState(), 5, 3, $$11, $$4);
                     this.placeBlock($$0, Blocks.OAK_PLANKS.defaultBlockState(), 6, 3, $$11, $$4);
                  }

                  this.placeBlock($$0, Blocks.OAK_PLANKS.defaultBlockState(), 7, 3, $$11, $$4);
                  this.placeBlock($$0, Blocks.OAK_PLANKS.defaultBlockState(), 8, 3, $$11, $$4);
               }

               BlockState $$12 = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST);
               this.placeBlock($$0, $$12, 9, 1, 3, $$4);
               this.placeBlock($$0, $$12, 9, 2, 3, $$4);
               this.placeBlock($$0, $$12, 9, 3, 3, $$4);
               this.createChest($$0, $$4, $$3, 3, 4, 8, BuiltInLootTables.STRONGHOLD_CROSSING);
         }
      }
   }

   static class SmoothStoneSelector extends StructurePiece.BlockSelector {
      @Override
      public void next(RandomSource $$0, int $$1, int $$2, int $$3, boolean $$4) {
         if ($$4) {
            float $$5 = $$0.nextFloat();
            if ($$5 < 0.2F) {
               this.next = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            } else if ($$5 < 0.5F) {
               this.next = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            } else if ($$5 < 0.55F) {
               this.next = Blocks.INFESTED_STONE_BRICKS.defaultBlockState();
            } else {
               this.next = Blocks.STONE_BRICKS.defaultBlockState();
            }
         } else {
            this.next = Blocks.CAVE_AIR.defaultBlockState();
         }
      }
   }

   public static class StairsDown extends StrongholdPieces.StrongholdPiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 11;
      private static final int DEPTH = 5;
      private final boolean isSource;

      public StairsDown(StructurePieceType $$0, int $$1, int $$2, int $$3, Direction $$4) {
         super($$0, $$1, makeBoundingBox($$2, 64, $$3, $$4, 5, 11, 5));
         this.isSource = true;
         this.setOrientation($$4);
         this.entryDoor = StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING;
      }

      public StairsDown(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.STRONGHOLD_STAIRS_DOWN, $$0, $$2);
         this.isSource = false;
         this.setOrientation($$3);
         this.entryDoor = this.randomSmallDoor($$1);
      }

      public StairsDown(StructurePieceType $$0, CompoundTag $$1) {
         super($$0, $$1);
         this.isSource = $$1.getBooleanOr("Source", false);
      }

      public StairsDown(CompoundTag $$0) {
         this(StructurePieceType.STRONGHOLD_STAIRS_DOWN, $$0);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("Source", this.isSource);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         if (this.isSource) {
            StrongholdPieces.imposedPiece = StrongholdPieces.FiveCrossing.class;
         }

         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 1);
      }

      @Nullable
      public static StrongholdPieces.StairsDown createPiece(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -7, 0, 5, 11, 5, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new StrongholdPieces.StairsDown($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 4, 10, 4, true, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, this.entryDoor, 1, 7, 0);
         this.generateSmallDoor($$0, $$3, $$4, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 2, 6, 1, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 1, $$4);
         this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 6, 1, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 2, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, 3, $$4);
         this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 5, 3, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, 3, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 3, $$4);
         this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 4, 3, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 2, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 3, 2, 1, $$4);
         this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 3, 1, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 2, 2, 1, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 1, $$4);
         this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 2, 1, $$4);
         this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 2, $$4);
         this.placeBlock($$0, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 1, 3, $$4);
      }
   }

   public static class StartPiece extends StrongholdPieces.StairsDown {
      @Nullable
      public StrongholdPieces.PieceWeight previousPiece;
      @Nullable
      public StrongholdPieces.PortalRoom portalRoomPiece;
      public final List<StructurePiece> pendingChildren = Lists.newArrayList();

      public StartPiece(RandomSource $$0, int $$1, int $$2) {
         super(StructurePieceType.STRONGHOLD_START, 0, $$1, $$2, getRandomHorizontalDirection($$0));
      }

      public StartPiece(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_START, $$0);
      }

      @Override
      public BlockPos getLocatorPosition() {
         return this.portalRoomPiece != null ? this.portalRoomPiece.getLocatorPosition() : super.getLocatorPosition();
      }
   }

   public static class Straight extends StrongholdPieces.StrongholdPiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 5;
      private static final int DEPTH = 7;
      private final boolean leftChild;
      private final boolean rightChild;

      public Straight(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.STRONGHOLD_STRAIGHT, $$0, $$2);
         this.setOrientation($$3);
         this.entryDoor = this.randomSmallDoor($$1);
         this.leftChild = $$1.nextInt(2) == 0;
         this.rightChild = $$1.nextInt(2) == 0;
      }

      public Straight(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_STRAIGHT, $$0);
         this.leftChild = $$0.getBooleanOr("Left", false);
         this.rightChild = $$0.getBooleanOr("Right", false);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("Left", this.leftChild);
         $$1.putBoolean("Right", this.rightChild);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 1);
         if (this.leftChild) {
            this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 2);
         }

         if (this.rightChild) {
            this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 2);
         }
      }

      @Nullable
      public static StrongholdPieces.Straight createPiece(StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -1, 0, 5, 5, 7, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new StrongholdPieces.Straight($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 4, 4, 6, true, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, this.entryDoor, 1, 1, 0);
         this.generateSmallDoor($$0, $$3, $$4, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 6);
         BlockState $$7 = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST);
         BlockState $$8 = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST);
         this.maybeGenerateBlock($$0, $$4, $$3, 0.1F, 1, 2, 1, $$7);
         this.maybeGenerateBlock($$0, $$4, $$3, 0.1F, 3, 2, 1, $$8);
         this.maybeGenerateBlock($$0, $$4, $$3, 0.1F, 1, 2, 5, $$7);
         this.maybeGenerateBlock($$0, $$4, $$3, 0.1F, 3, 2, 5, $$8);
         if (this.leftChild) {
            this.generateBox($$0, $$4, 0, 1, 2, 0, 3, 4, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.rightChild) {
            this.generateBox($$0, $$4, 4, 1, 2, 4, 3, 4, CAVE_AIR, CAVE_AIR, false);
         }
      }
   }

   public static class StraightStairsDown extends StrongholdPieces.StrongholdPiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 11;
      private static final int DEPTH = 8;

      public StraightStairsDown(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, $$0, $$2);
         this.setOrientation($$3);
         this.entryDoor = this.randomSmallDoor($$1);
      }

      public StraightStairsDown(CompoundTag $$0) {
         super(StructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)$$0, $$1, $$2, 1, 1);
      }

      @Nullable
      public static StrongholdPieces.StraightStairsDown createPiece(
         StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6
      ) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -7, 0, 5, 11, 8, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new StrongholdPieces.StraightStairsDown($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 4, 10, 7, true, $$3, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor($$0, $$3, $$4, this.entryDoor, 1, 7, 0);
         this.generateSmallDoor($$0, $$3, $$4, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 7);
         BlockState $$7 = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);

         for (int $$8 = 0; $$8 < 6; $$8++) {
            this.placeBlock($$0, $$7, 1, 6 - $$8, 1 + $$8, $$4);
            this.placeBlock($$0, $$7, 2, 6 - $$8, 1 + $$8, $$4);
            this.placeBlock($$0, $$7, 3, 6 - $$8, 1 + $$8, $$4);
            if ($$8 < 5) {
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5 - $$8, 1 + $$8, $$4);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 2, 5 - $$8, 1 + $$8, $$4);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), 3, 5 - $$8, 1 + $$8, $$4);
            }
         }
      }
   }

   abstract static class StrongholdPiece extends StructurePiece {
      protected StrongholdPieces.StrongholdPiece.SmallDoorType entryDoor = StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING;

      protected StrongholdPiece(StructurePieceType $$0, int $$1, BoundingBox $$2) {
         super($$0, $$1, $$2);
      }

      public StrongholdPiece(StructurePieceType $$0, CompoundTag $$1) {
         super($$0, $$1);
         this.entryDoor = (StrongholdPieces.StrongholdPiece.SmallDoorType)$$1.read("EntryDoor", StrongholdPieces.StrongholdPiece.SmallDoorType.LEGACY_CODEC)
            .orElseThrow();
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         $$1.store("EntryDoor", StrongholdPieces.StrongholdPiece.SmallDoorType.LEGACY_CODEC, this.entryDoor);
      }

      protected void generateSmallDoor(
         net.minecraft.world.level.WorldGenLevel $$0,
         RandomSource $$1,
         BoundingBox $$2,
         StrongholdPieces.StrongholdPiece.SmallDoorType $$3,
         int $$4,
         int $$5,
         int $$6
      ) {
         switch ($$3) {
            case OPENING:
               this.generateBox($$0, $$2, $$4, $$5, $$6, $$4 + 3 - 1, $$5 + 3 - 1, $$6, CAVE_AIR, CAVE_AIR, false);
               break;
            case WOOD_DOOR:
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4, $$5, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4, $$5 + 1, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4, $$5 + 2, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4 + 1, $$5 + 2, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4 + 2, $$5 + 2, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4 + 2, $$5 + 1, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4 + 2, $$5, $$6, $$2);
               this.placeBlock($$0, Blocks.OAK_DOOR.defaultBlockState(), $$4 + 1, $$5, $$6, $$2);
               this.placeBlock($$0, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), $$4 + 1, $$5 + 1, $$6, $$2);
               break;
            case GRATES:
               this.placeBlock($$0, Blocks.CAVE_AIR.defaultBlockState(), $$4 + 1, $$5, $$6, $$2);
               this.placeBlock($$0, Blocks.CAVE_AIR.defaultBlockState(), $$4 + 1, $$5 + 1, $$6, $$2);
               this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true), $$4, $$5, $$6, $$2);
               this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true), $$4, $$5 + 1, $$6, $$2);
               this.placeBlock(
                  $$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true).setValue(IronBarsBlock.WEST, true), $$4, $$5 + 2, $$6, $$2
               );
               this.placeBlock(
                  $$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true).setValue(IronBarsBlock.WEST, true), $$4 + 1, $$5 + 2, $$6, $$2
               );
               this.placeBlock(
                  $$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true).setValue(IronBarsBlock.WEST, true), $$4 + 2, $$5 + 2, $$6, $$2
               );
               this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true), $$4 + 2, $$5 + 1, $$6, $$2);
               this.placeBlock($$0, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true), $$4 + 2, $$5, $$6, $$2);
               break;
            case IRON_DOOR:
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4, $$5, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4, $$5 + 1, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4, $$5 + 2, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4 + 1, $$5 + 2, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4 + 2, $$5 + 2, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4 + 2, $$5 + 1, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BRICKS.defaultBlockState(), $$4 + 2, $$5, $$6, $$2);
               this.placeBlock($$0, Blocks.IRON_DOOR.defaultBlockState(), $$4 + 1, $$5, $$6, $$2);
               this.placeBlock($$0, Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), $$4 + 1, $$5 + 1, $$6, $$2);
               this.placeBlock($$0, Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.NORTH), $$4 + 2, $$5 + 1, $$6 + 1, $$2);
               this.placeBlock($$0, Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.SOUTH), $$4 + 2, $$5 + 1, $$6 - 1, $$2);
         }
      }

      protected StrongholdPieces.StrongholdPiece.SmallDoorType randomSmallDoor(RandomSource $$0) {
         int $$1 = $$0.nextInt(5);
         switch ($$1) {
            case 0:
            case 1:
            default:
               return StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING;
            case 2:
               return StrongholdPieces.StrongholdPiece.SmallDoorType.WOOD_DOOR;
            case 3:
               return StrongholdPieces.StrongholdPiece.SmallDoorType.GRATES;
            case 4:
               return StrongholdPieces.StrongholdPiece.SmallDoorType.IRON_DOOR;
         }
      }

      @Nullable
      protected StructurePiece generateSmallDoorChildForward(StrongholdPieces.StartPiece $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4) {
         Direction $$5 = this.getOrientation();
         if ($$5 != null) {
            switch ($$5) {
               case NORTH:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.minX() + $$3, this.boundingBox.minY() + $$4, this.boundingBox.minZ() - 1, $$5, this.getGenDepth()
                  );
               case SOUTH:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.minX() + $$3, this.boundingBox.minY() + $$4, this.boundingBox.maxZ() + 1, $$5, this.getGenDepth()
                  );
               case WEST:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY() + $$4, this.boundingBox.minZ() + $$3, $$5, this.getGenDepth()
                  );
               case EAST:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY() + $$4, this.boundingBox.minZ() + $$3, $$5, this.getGenDepth()
                  );
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateSmallDoorChildLeft(StrongholdPieces.StartPiece $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4) {
         Direction $$5 = this.getOrientation();
         if ($$5 != null) {
            switch ($$5) {
               case NORTH:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() - 1,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() + $$4,
                     Direction.WEST,
                     this.getGenDepth()
                  );
               case SOUTH:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() - 1,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() + $$4,
                     Direction.WEST,
                     this.getGenDepth()
                  );
               case WEST:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() + $$4,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() - 1,
                     Direction.NORTH,
                     this.getGenDepth()
                  );
               case EAST:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() + $$4,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() - 1,
                     Direction.NORTH,
                     this.getGenDepth()
                  );
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateSmallDoorChildRight(StrongholdPieces.StartPiece $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4) {
         Direction $$5 = this.getOrientation();
         if ($$5 != null) {
            switch ($$5) {
               case NORTH:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.maxX() + 1,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() + $$4,
                     Direction.EAST,
                     this.getGenDepth()
                  );
               case SOUTH:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.maxX() + 1,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() + $$4,
                     Direction.EAST,
                     this.getGenDepth()
                  );
               case WEST:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() + $$4,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.maxZ() + 1,
                     Direction.SOUTH,
                     this.getGenDepth()
                  );
               case EAST:
                  return StrongholdPieces.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() + $$4,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.maxZ() + 1,
                     Direction.SOUTH,
                     this.getGenDepth()
                  );
            }
         }

         return null;
      }

      protected static boolean isOkBox(BoundingBox $$0) {
         return $$0.minY() > 10;
      }

      protected static enum SmallDoorType {
         OPENING,
         WOOD_DOOR,
         GRATES,
         IRON_DOOR;

         @Deprecated
         public static final Codec<StrongholdPieces.StrongholdPiece.SmallDoorType> LEGACY_CODEC = ExtraCodecs.legacyEnum(
            StrongholdPieces.StrongholdPiece.SmallDoorType::valueOf
         );
      }
   }

   public abstract static class Turn extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 5;
      protected static final int HEIGHT = 5;
      protected static final int DEPTH = 5;

      protected Turn(StructurePieceType $$0, int $$1, BoundingBox $$2) {
         super($$0, $$1, $$2);
      }

      public Turn(StructurePieceType $$0, CompoundTag $$1) {
         super($$0, $$1);
      }
   }
}
