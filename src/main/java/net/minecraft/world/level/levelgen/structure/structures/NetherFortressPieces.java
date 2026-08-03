package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public class NetherFortressPieces {
   private static final int MAX_DEPTH = 30;
   private static final int LOWEST_Y_POSITION = 10;
   public static final int MAGIC_START_Y = 64;
   static final NetherFortressPieces.PieceWeight[] BRIDGE_PIECE_WEIGHTS = new NetherFortressPieces.PieceWeight[]{
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.BridgeStraight.class, 30, 0, true),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.BridgeCrossing.class, 10, 4),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.RoomCrossing.class, 10, 4),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.StairsRoom.class, 10, 3),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.MonsterThrone.class, 5, 2),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleEntrance.class, 5, 1)
   };
   static final NetherFortressPieces.PieceWeight[] CASTLE_PIECE_WEIGHTS = new NetherFortressPieces.PieceWeight[]{
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorPiece.class, 25, 0, true),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorCrossingPiece.class, 15, 5),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorRightTurnPiece.class, 5, 10),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.class, 5, 10),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleCorridorStairsPiece.class, 10, 3, true),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleCorridorTBalconyPiece.class, 7, 2),
      new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleStalkRoom.class, 5, 2)
   };

   @Nullable
   static NetherFortressPieces.NetherBridgePiece findAndCreateBridgePieceFactory(
      NetherFortressPieces.PieceWeight $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4, int $$5, Direction $$6, int $$7
   ) {
      Class<? extends NetherFortressPieces.NetherBridgePiece> $$8 = $$0.pieceClass;
      NetherFortressPieces.NetherBridgePiece $$9 = null;
      if ($$8 == NetherFortressPieces.BridgeStraight.class) {
         $$9 = NetherFortressPieces.BridgeStraight.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$8 == NetherFortressPieces.BridgeCrossing.class) {
         $$9 = NetherFortressPieces.BridgeCrossing.createPiece($$1, $$3, $$4, $$5, $$6, $$7);
      } else if ($$8 == NetherFortressPieces.RoomCrossing.class) {
         $$9 = NetherFortressPieces.RoomCrossing.createPiece($$1, $$3, $$4, $$5, $$6, $$7);
      } else if ($$8 == NetherFortressPieces.StairsRoom.class) {
         $$9 = NetherFortressPieces.StairsRoom.createPiece($$1, $$3, $$4, $$5, $$7, $$6);
      } else if ($$8 == NetherFortressPieces.MonsterThrone.class) {
         $$9 = NetherFortressPieces.MonsterThrone.createPiece($$1, $$3, $$4, $$5, $$7, $$6);
      } else if ($$8 == NetherFortressPieces.CastleEntrance.class) {
         $$9 = NetherFortressPieces.CastleEntrance.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$8 == NetherFortressPieces.CastleSmallCorridorPiece.class) {
         $$9 = NetherFortressPieces.CastleSmallCorridorPiece.createPiece($$1, $$3, $$4, $$5, $$6, $$7);
      } else if ($$8 == NetherFortressPieces.CastleSmallCorridorRightTurnPiece.class) {
         $$9 = NetherFortressPieces.CastleSmallCorridorRightTurnPiece.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$8 == NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.class) {
         $$9 = NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else if ($$8 == NetherFortressPieces.CastleCorridorStairsPiece.class) {
         $$9 = NetherFortressPieces.CastleCorridorStairsPiece.createPiece($$1, $$3, $$4, $$5, $$6, $$7);
      } else if ($$8 == NetherFortressPieces.CastleCorridorTBalconyPiece.class) {
         $$9 = NetherFortressPieces.CastleCorridorTBalconyPiece.createPiece($$1, $$3, $$4, $$5, $$6, $$7);
      } else if ($$8 == NetherFortressPieces.CastleSmallCorridorCrossingPiece.class) {
         $$9 = NetherFortressPieces.CastleSmallCorridorCrossingPiece.createPiece($$1, $$3, $$4, $$5, $$6, $$7);
      } else if ($$8 == NetherFortressPieces.CastleStalkRoom.class) {
         $$9 = NetherFortressPieces.CastleStalkRoom.createPiece($$1, $$3, $$4, $$5, $$6, $$7);
      }

      return $$9;
   }

   public static class BridgeCrossing extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 19;
      private static final int HEIGHT = 10;
      private static final int DEPTH = 19;

      public BridgeCrossing(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, $$0, $$1);
         this.setOrientation($$2);
      }

      protected BridgeCrossing(int $$0, int $$1, Direction $$2) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0, StructurePiece.makeBoundingBox($$0, 64, $$1, $$2, 19, 10, 19));
         this.setOrientation($$2);
      }

      protected BridgeCrossing(StructurePieceType $$0, CompoundTag $$1) {
         super($$0, $$1);
      }

      public BridgeCrossing(CompoundTag $$0) {
         this(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildForward((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 8, 3, false);
         this.generateChildLeft((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 3, 8, false);
         this.generateChildRight((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 3, 8, false);
      }

      @Nullable
      public static NetherFortressPieces.BridgeCrossing createPiece(StructurePieceAccessor $$0, int $$1, int $$2, int $$3, Direction $$4, int $$5) {
         BoundingBox $$6 = BoundingBox.orientBox($$1, $$2, $$3, -8, -3, 0, 19, 10, 19, $$4);
         return isOkBox($$6) && $$0.findCollisionPiece($$6) == null ? new NetherFortressPieces.BridgeCrossing($$5, $$6, $$4) : null;
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
         this.generateBox($$0, $$4, 7, 3, 0, 11, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 3, 7, 18, 4, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 8, 5, 0, 10, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 5, 8, 18, 7, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 7, 5, 0, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 7, 5, 11, 7, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 11, 5, 0, 11, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 5, 7, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 11, 5, 7, 18, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 5, 11, 7, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 7, 2, 0, 11, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 7, 2, 13, 11, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 7, 0, 0, 11, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 7, 0, 15, 11, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for (int $$7 = 7; $$7 <= 11; $$7++) {
            for (int $$8 = 0; $$8 <= 2; $$8++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$7, -1, $$8, $$4);
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$7, -1, 18 - $$8, $$4);
            }
         }

         this.generateBox($$0, $$4, 0, 2, 7, 5, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 0, 7, 3, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for (int $$9 = 0; $$9 <= 2; $$9++) {
            for (int $$10 = 7; $$10 <= 11; $$10++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$9, -1, $$10, $$4);
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), 18 - $$9, -1, $$10, $$4);
            }
         }
      }
   }

   public static class BridgeEndFiller extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 10;
      private static final int DEPTH = 8;
      private final int selfSeed;

      public BridgeEndFiller(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, $$0, $$2);
         this.setOrientation($$3);
         this.selfSeed = $$1.nextInt();
      }

      public BridgeEndFiller(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, $$0);
         this.selfSeed = $$0.getIntOr("Seed", 0);
      }

      @Nullable
      public static NetherFortressPieces.BridgeEndFiller createPiece(
         StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6
      ) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -3, 0, 5, 10, 8, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new NetherFortressPieces.BridgeEndFiller($$6, $$1, $$7, $$5) : null;
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putInt("Seed", this.selfSeed);
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
         RandomSource $$7 = RandomSource.create(this.selfSeed);

         for (int $$8 = 0; $$8 <= 4; $$8++) {
            for (int $$9 = 3; $$9 <= 4; $$9++) {
               int $$10 = $$7.nextInt(8);
               this.generateBox(
                  $$0, $$4, $$8, $$9, 0, $$8, $$9, $$10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false
               );
            }
         }

         int $$11 = $$7.nextInt(8);
         this.generateBox($$0, $$4, 0, 5, 0, 0, 5, $$11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         $$11 = $$7.nextInt(8);
         this.generateBox($$0, $$4, 4, 5, 0, 4, 5, $$11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for (int $$13 = 0; $$13 <= 4; $$13++) {
            int $$14 = $$7.nextInt(5);
            this.generateBox($$0, $$4, $$13, 2, 0, $$13, 2, $$14, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         }

         for (int $$15 = 0; $$15 <= 4; $$15++) {
            for (int $$16 = 0; $$16 <= 1; $$16++) {
               int $$17 = $$7.nextInt(3);
               this.generateBox(
                  $$0, $$4, $$15, $$16, 0, $$15, $$16, $$17, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false
               );
            }
         }
      }
   }

   public static class BridgeStraight extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 10;
      private static final int DEPTH = 19;

      public BridgeStraight(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, $$0, $$2);
         this.setOrientation($$3);
      }

      public BridgeStraight(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildForward((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 1, 3, false);
      }

      @Nullable
      public static NetherFortressPieces.BridgeStraight createPiece(
         StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6
      ) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, -3, 0, 5, 10, 19, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new NetherFortressPieces.BridgeStraight($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 3, 0, 4, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 5, 0, 3, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 5, 0, 0, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 5, 0, 4, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 4, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 0, 0, 4, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for (int $$7 = 0; $$7 <= 4; $$7++) {
            for (int $$8 = 0; $$8 <= 2; $$8++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$7, -1, $$8, $$4);
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$7, -1, 18 - $$8, $$4);
            }
         }

         BlockState $$9 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
         BlockState $$10 = $$9.setValue(FenceBlock.EAST, true);
         BlockState $$11 = $$9.setValue(FenceBlock.WEST, true);
         this.generateBox($$0, $$4, 0, 1, 1, 0, 4, 1, $$10, $$10, false);
         this.generateBox($$0, $$4, 0, 3, 4, 0, 4, 4, $$10, $$10, false);
         this.generateBox($$0, $$4, 0, 3, 14, 0, 4, 14, $$10, $$10, false);
         this.generateBox($$0, $$4, 0, 1, 17, 0, 4, 17, $$10, $$10, false);
         this.generateBox($$0, $$4, 4, 1, 1, 4, 4, 1, $$11, $$11, false);
         this.generateBox($$0, $$4, 4, 3, 4, 4, 4, 4, $$11, $$11, false);
         this.generateBox($$0, $$4, 4, 3, 14, 4, 4, 14, $$11, $$11, false);
         this.generateBox($$0, $$4, 4, 1, 17, 4, 4, 17, $$11, $$11, false);
      }
   }

   public static class CastleCorridorStairsPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 14;
      private static final int DEPTH = 10;

      public CastleCorridorStairsPiece(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, $$0, $$1);
         this.setOrientation($$2);
      }

      public CastleCorridorStairsPiece(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildForward((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 1, 0, true);
      }

      @Nullable
      public static NetherFortressPieces.CastleCorridorStairsPiece createPiece(StructurePieceAccessor $$0, int $$1, int $$2, int $$3, Direction $$4, int $$5) {
         BoundingBox $$6 = BoundingBox.orientBox($$1, $$2, $$3, -1, -7, 0, 5, 14, 10, $$4);
         return isOkBox($$6) && $$0.findCollisionPiece($$6) == null ? new NetherFortressPieces.CastleCorridorStairsPiece($$5, $$6, $$4) : null;
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
         BlockState $$7 = Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
         BlockState $$8 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);

         for (int $$9 = 0; $$9 <= 9; $$9++) {
            int $$10 = Math.max(1, 7 - $$9);
            int $$11 = Math.min(Math.max($$10 + 5, 14 - $$9), 13);
            int $$12 = $$9;
            this.generateBox($$0, $$4, 0, 0, $$9, 4, $$10, $$9, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox($$0, $$4, 1, $$10 + 1, $$9, 3, $$11 - 1, $$9, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            if ($$9 <= 6) {
               this.placeBlock($$0, $$7, 1, $$10 + 1, $$9, $$4);
               this.placeBlock($$0, $$7, 2, $$10 + 1, $$9, $$4);
               this.placeBlock($$0, $$7, 3, $$10 + 1, $$9, $$4);
            }

            this.generateBox($$0, $$4, 0, $$11, $$9, 4, $$11, $$9, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(
               $$0, $$4, 0, $$10 + 1, $$9, 0, $$11 - 1, $$9, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false
            );
            this.generateBox(
               $$0, $$4, 4, $$10 + 1, $$9, 4, $$11 - 1, $$9, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false
            );
            if (($$9 & 1) == 0) {
               this.generateBox($$0, $$4, 0, $$10 + 2, $$9, 0, $$10 + 3, $$9, $$8, $$8, false);
               this.generateBox($$0, $$4, 4, $$10 + 2, $$9, 4, $$10 + 3, $$9, $$8, $$8, false);
            }

            for (int $$13 = 0; $$13 <= 4; $$13++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$13, -1, $$12, $$4);
            }
         }
      }
   }

   public static class CastleCorridorTBalconyPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 9;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 9;

      public CastleCorridorTBalconyPiece(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, $$0, $$1);
         this.setOrientation($$2);
      }

      public CastleCorridorTBalconyPiece(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         int $$3 = 1;
         Direction $$4 = this.getOrientation();
         if ($$4 == Direction.WEST || $$4 == Direction.NORTH) {
            $$3 = 5;
         }

         this.generateChildLeft((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 0, $$3, $$2.nextInt(8) > 0);
         this.generateChildRight((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 0, $$3, $$2.nextInt(8) > 0);
      }

      @Nullable
      public static NetherFortressPieces.CastleCorridorTBalconyPiece createPiece(StructurePieceAccessor $$0, int $$1, int $$2, int $$3, Direction $$4, int $$5) {
         BoundingBox $$6 = BoundingBox.orientBox($$1, $$2, $$3, -3, 0, 0, 9, 7, 9, $$4);
         return isOkBox($$6) && $$0.findCollisionPiece($$6) == null ? new NetherFortressPieces.CastleCorridorTBalconyPiece($$5, $$6, $$4) : null;
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
         BlockState $$7 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
         BlockState $$8 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
         this.generateBox($$0, $$4, 0, 0, 0, 8, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 8, 5, 8, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 6, 0, 8, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 2, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 6, 2, 0, 8, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 3, 0, 1, 4, 0, $$8, $$8, false);
         this.generateBox($$0, $$4, 7, 3, 0, 7, 4, 0, $$8, $$8, false);
         this.generateBox($$0, $$4, 0, 2, 4, 8, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 1, 4, 2, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 6, 1, 4, 7, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 3, 8, 7, 3, 8, $$8, $$8, false);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true).setValue(FenceBlock.SOUTH, true), 0, 3, 8, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.SOUTH, true), 8, 3, 8, $$4);
         this.generateBox($$0, $$4, 0, 3, 6, 0, 3, 7, $$7, $$7, false);
         this.generateBox($$0, $$4, 8, 3, 6, 8, 3, 7, $$7, $$7, false);
         this.generateBox($$0, $$4, 0, 3, 4, 0, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 8, 3, 4, 8, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 3, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 6, 3, 5, 7, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 4, 5, 1, 5, 5, $$8, $$8, false);
         this.generateBox($$0, $$4, 7, 4, 5, 7, 5, 5, $$8, $$8, false);

         for (int $$9 = 0; $$9 <= 5; $$9++) {
            for (int $$10 = 0; $$10 <= 8; $$10++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$10, -1, $$9, $$4);
            }
         }
      }
   }

   public static class CastleEntrance extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 13;
      private static final int HEIGHT = 14;
      private static final int DEPTH = 13;

      public CastleEntrance(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, $$0, $$2);
         this.setOrientation($$3);
      }

      public CastleEntrance(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildForward((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 5, 3, true);
      }

      @Nullable
      public static NetherFortressPieces.CastleEntrance createPiece(
         StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6
      ) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -5, -3, 0, 13, 14, 13, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new NetherFortressPieces.CastleEntrance($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 8, 0, 7, 8, 0, Blocks.NETHER_BRICK_FENCE.defaultBlockState(), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), false);
         BlockState $$7 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
         BlockState $$8 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);

         for (int $$9 = 1; $$9 <= 11; $$9 += 2) {
            this.generateBox($$0, $$4, $$9, 10, 0, $$9, 11, 0, $$7, $$7, false);
            this.generateBox($$0, $$4, $$9, 10, 12, $$9, 11, 12, $$7, $$7, false);
            this.generateBox($$0, $$4, 0, 10, $$9, 0, 11, $$9, $$8, $$8, false);
            this.generateBox($$0, $$4, 12, 10, $$9, 12, 11, $$9, $$8, $$8, false);
            this.placeBlock($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$9, 13, 0, $$4);
            this.placeBlock($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$9, 13, 12, $$4);
            this.placeBlock($$0, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, $$9, $$4);
            this.placeBlock($$0, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, $$9, $$4);
            if ($$9 != 11) {
               this.placeBlock($$0, $$7, $$9 + 1, 13, 0, $$4);
               this.placeBlock($$0, $$7, $$9 + 1, 13, 12, $$4);
               this.placeBlock($$0, $$8, 0, 13, $$9 + 1, $$4);
               this.placeBlock($$0, $$8, 12, 13, $$9 + 1, $$4);
            }
         }

         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.EAST, true), 0, 13, 0, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true).setValue(FenceBlock.EAST, true), 0, 13, 12, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true).setValue(FenceBlock.WEST, true), 12, 13, 12, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.WEST, true), 12, 13, 0, $$4);

         for (int $$10 = 3; $$10 <= 9; $$10 += 2) {
            this.generateBox($$0, $$4, 1, 7, $$10, 1, 8, $$10, $$8.setValue(FenceBlock.WEST, true), $$8.setValue(FenceBlock.WEST, true), false);
            this.generateBox($$0, $$4, 11, 7, $$10, 11, 8, $$10, $$8.setValue(FenceBlock.EAST, true), $$8.setValue(FenceBlock.EAST, true), false);
         }

         this.generateBox($$0, $$4, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for (int $$11 = 4; $$11 <= 8; $$11++) {
            for (int $$12 = 0; $$12 <= 2; $$12++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$11, -1, $$12, $$4);
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$11, -1, 12 - $$12, $$4);
            }
         }

         for (int $$13 = 0; $$13 <= 2; $$13++) {
            for (int $$14 = 4; $$14 <= 8; $$14++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$13, -1, $$14, $$4);
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - $$13, -1, $$14, $$4);
            }
         }

         this.generateBox($$0, $$4, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 6, 1, 6, 6, 4, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.placeBlock($$0, Blocks.NETHER_BRICKS.defaultBlockState(), 6, 0, 6, $$4);
         this.placeBlock($$0, Blocks.LAVA.defaultBlockState(), 6, 5, 6, $$4);
         BlockPos $$15 = this.getWorldPos(6, 5, 6);
         if ($$4.isInside($$15)) {
            $$0.scheduleTick($$15, Fluids.LAVA, 0);
         }
      }
   }

   public static class CastleSmallCorridorCrossingPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;

      public CastleSmallCorridorCrossingPiece(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, $$0, $$1);
         this.setOrientation($$2);
      }

      public CastleSmallCorridorCrossingPiece(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildForward((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 1, 0, true);
         this.generateChildLeft((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 0, 1, true);
         this.generateChildRight((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 0, 1, true);
      }

      @Nullable
      public static NetherFortressPieces.CastleSmallCorridorCrossingPiece createPiece(
         StructurePieceAccessor $$0, int $$1, int $$2, int $$3, Direction $$4, int $$5
      ) {
         BoundingBox $$6 = BoundingBox.orientBox($$1, $$2, $$3, -1, 0, 0, 5, 7, 5, $$4);
         return isOkBox($$6) && $$0.findCollisionPiece($$6) == null ? new NetherFortressPieces.CastleSmallCorridorCrossingPiece($$5, $$6, $$4) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for (int $$7 = 0; $$7 <= 4; $$7++) {
            for (int $$8 = 0; $$8 <= 4; $$8++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$7, -1, $$8, $$4);
            }
         }
      }
   }

   public static class CastleSmallCorridorLeftTurnPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;
      private boolean isNeedingChest;

      public CastleSmallCorridorLeftTurnPiece(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, $$0, $$2);
         this.setOrientation($$3);
         this.isNeedingChest = $$1.nextInt(3) == 0;
      }

      public CastleSmallCorridorLeftTurnPiece(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, $$0);
         this.isNeedingChest = $$0.getBooleanOr("Chest", false);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("Chest", this.isNeedingChest);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildLeft((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 0, 1, true);
      }

      @Nullable
      public static NetherFortressPieces.CastleSmallCorridorLeftTurnPiece createPiece(
         StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6
      ) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, 0, 0, 5, 7, 5, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new NetherFortressPieces.CastleSmallCorridorLeftTurnPiece($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         BlockState $$7 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
         BlockState $$8 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
         this.generateBox($$0, $$4, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 3, 1, 4, 4, 1, $$8, $$8, false);
         this.generateBox($$0, $$4, 4, 3, 3, 4, 4, 3, $$8, $$8, false);
         this.generateBox($$0, $$4, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 3, 4, 1, 4, 4, $$7, $$7, false);
         this.generateBox($$0, $$4, 3, 3, 4, 3, 4, 4, $$7, $$7, false);
         if (this.isNeedingChest && $$4.isInside(this.getWorldPos(3, 2, 3))) {
            this.isNeedingChest = false;
            this.createChest($$0, $$4, $$3, 3, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
         }

         this.generateBox($$0, $$4, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for (int $$9 = 0; $$9 <= 4; $$9++) {
            for (int $$10 = 0; $$10 <= 4; $$10++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$9, -1, $$10, $$4);
            }
         }
      }
   }

   public static class CastleSmallCorridorPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;

      public CastleSmallCorridorPiece(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, $$0, $$1);
         this.setOrientation($$2);
      }

      public CastleSmallCorridorPiece(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildForward((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 1, 0, true);
      }

      @Nullable
      public static NetherFortressPieces.CastleSmallCorridorPiece createPiece(StructurePieceAccessor $$0, int $$1, int $$2, int $$3, Direction $$4, int $$5) {
         BoundingBox $$6 = BoundingBox.orientBox($$1, $$2, $$3, -1, 0, 0, 5, 7, 5, $$4);
         return isOkBox($$6) && $$0.findCollisionPiece($$6) == null ? new NetherFortressPieces.CastleSmallCorridorPiece($$5, $$6, $$4) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         BlockState $$7 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
         this.generateBox($$0, $$4, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 3, 1, 0, 4, 1, $$7, $$7, false);
         this.generateBox($$0, $$4, 0, 3, 3, 0, 4, 3, $$7, $$7, false);
         this.generateBox($$0, $$4, 4, 3, 1, 4, 4, 1, $$7, $$7, false);
         this.generateBox($$0, $$4, 4, 3, 3, 4, 4, 3, $$7, $$7, false);
         this.generateBox($$0, $$4, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for (int $$8 = 0; $$8 <= 4; $$8++) {
            for (int $$9 = 0; $$9 <= 4; $$9++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$8, -1, $$9, $$4);
            }
         }
      }
   }

   public static class CastleSmallCorridorRightTurnPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;
      private boolean isNeedingChest;

      public CastleSmallCorridorRightTurnPiece(int $$0, RandomSource $$1, BoundingBox $$2, Direction $$3) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, $$0, $$2);
         this.setOrientation($$3);
         this.isNeedingChest = $$1.nextInt(3) == 0;
      }

      public CastleSmallCorridorRightTurnPiece(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, $$0);
         this.isNeedingChest = $$0.getBooleanOr("Chest", false);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("Chest", this.isNeedingChest);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildRight((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 0, 1, true);
      }

      @Nullable
      public static NetherFortressPieces.CastleSmallCorridorRightTurnPiece createPiece(
         StructurePieceAccessor $$0, RandomSource $$1, int $$2, int $$3, int $$4, Direction $$5, int $$6
      ) {
         BoundingBox $$7 = BoundingBox.orientBox($$2, $$3, $$4, -1, 0, 0, 5, 7, 5, $$5);
         return isOkBox($$7) && $$0.findCollisionPiece($$7) == null ? new NetherFortressPieces.CastleSmallCorridorRightTurnPiece($$6, $$1, $$7, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         BlockState $$7 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
         BlockState $$8 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
         this.generateBox($$0, $$4, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 3, 1, 0, 4, 1, $$8, $$8, false);
         this.generateBox($$0, $$4, 0, 3, 3, 0, 4, 3, $$8, $$8, false);
         this.generateBox($$0, $$4, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 3, 4, 1, 4, 4, $$7, $$7, false);
         this.generateBox($$0, $$4, 3, 3, 4, 3, 4, 4, $$7, $$7, false);
         if (this.isNeedingChest && $$4.isInside(this.getWorldPos(1, 2, 3))) {
            this.isNeedingChest = false;
            this.createChest($$0, $$4, $$3, 1, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
         }

         this.generateBox($$0, $$4, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for (int $$9 = 0; $$9 <= 4; $$9++) {
            for (int $$10 = 0; $$10 <= 4; $$10++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$9, -1, $$10, $$4);
            }
         }
      }
   }

   public static class CastleStalkRoom extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 13;
      private static final int HEIGHT = 14;
      private static final int DEPTH = 13;

      public CastleStalkRoom(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, $$0, $$1);
         this.setOrientation($$2);
      }

      public CastleStalkRoom(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildForward((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 5, 3, true);
         this.generateChildForward((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 5, 11, true);
      }

      @Nullable
      public static NetherFortressPieces.CastleStalkRoom createPiece(StructurePieceAccessor $$0, int $$1, int $$2, int $$3, Direction $$4, int $$5) {
         BoundingBox $$6 = BoundingBox.orientBox($$1, $$2, $$3, -5, -3, 0, 13, 14, 13, $$4);
         return isOkBox($$6) && $$0.findCollisionPiece($$6) == null ? new NetherFortressPieces.CastleStalkRoom($$5, $$6, $$4) : null;
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
         this.generateBox($$0, $$4, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState $$7 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
         BlockState $$8 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
         BlockState $$9 = $$8.setValue(FenceBlock.WEST, true);
         BlockState $$10 = $$8.setValue(FenceBlock.EAST, true);

         for (int $$11 = 1; $$11 <= 11; $$11 += 2) {
            this.generateBox($$0, $$4, $$11, 10, 0, $$11, 11, 0, $$7, $$7, false);
            this.generateBox($$0, $$4, $$11, 10, 12, $$11, 11, 12, $$7, $$7, false);
            this.generateBox($$0, $$4, 0, 10, $$11, 0, 11, $$11, $$8, $$8, false);
            this.generateBox($$0, $$4, 12, 10, $$11, 12, 11, $$11, $$8, $$8, false);
            this.placeBlock($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$11, 13, 0, $$4);
            this.placeBlock($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$11, 13, 12, $$4);
            this.placeBlock($$0, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, $$11, $$4);
            this.placeBlock($$0, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, $$11, $$4);
            if ($$11 != 11) {
               this.placeBlock($$0, $$7, $$11 + 1, 13, 0, $$4);
               this.placeBlock($$0, $$7, $$11 + 1, 13, 12, $$4);
               this.placeBlock($$0, $$8, 0, 13, $$11 + 1, $$4);
               this.placeBlock($$0, $$8, 12, 13, $$11 + 1, $$4);
            }
         }

         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.EAST, true), 0, 13, 0, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true).setValue(FenceBlock.EAST, true), 0, 13, 12, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true).setValue(FenceBlock.WEST, true), 12, 13, 12, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.WEST, true), 12, 13, 0, $$4);

         for (int $$12 = 3; $$12 <= 9; $$12 += 2) {
            this.generateBox($$0, $$4, 1, 7, $$12, 1, 8, $$12, $$9, $$9, false);
            this.generateBox($$0, $$4, 11, 7, $$12, 11, 8, $$12, $$10, $$10, false);
         }

         BlockState $$13 = Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);

         for (int $$14 = 0; $$14 <= 6; $$14++) {
            int $$15 = $$14 + 4;

            for (int $$16 = 5; $$16 <= 7; $$16++) {
               this.placeBlock($$0, $$13, $$16, 5 + $$14, $$15, $$4);
            }

            if ($$15 >= 5 && $$15 <= 8) {
               this.generateBox(
                  $$0, $$4, 5, 5, $$15, 7, $$14 + 4, $$15, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false
               );
            } else if ($$15 >= 9 && $$15 <= 10) {
               this.generateBox(
                  $$0, $$4, 5, 8, $$15, 7, $$14 + 4, $$15, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false
               );
            }

            if ($$14 >= 1) {
               this.generateBox($$0, $$4, 5, 6 + $$14, $$15, 7, 9 + $$14, $$15, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }
         }

         for (int $$17 = 5; $$17 <= 7; $$17++) {
            this.placeBlock($$0, $$13, $$17, 12, 11, $$4);
         }

         this.generateBox($$0, $$4, 5, 6, 7, 5, 7, 7, $$10, $$10, false);
         this.generateBox($$0, $$4, 7, 6, 7, 7, 7, 7, $$9, $$9, false);
         this.generateBox($$0, $$4, 5, 13, 12, 7, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 5, 2, 3, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 5, 9, 3, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 5, 4, 2, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 9, 5, 2, 10, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 9, 5, 9, 10, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 10, 5, 4, 10, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState $$18 = $$13.setValue(StairBlock.FACING, Direction.EAST);
         BlockState $$19 = $$13.setValue(StairBlock.FACING, Direction.WEST);
         this.placeBlock($$0, $$19, 4, 5, 2, $$4);
         this.placeBlock($$0, $$19, 4, 5, 3, $$4);
         this.placeBlock($$0, $$19, 4, 5, 9, $$4);
         this.placeBlock($$0, $$19, 4, 5, 10, $$4);
         this.placeBlock($$0, $$18, 8, 5, 2, $$4);
         this.placeBlock($$0, $$18, 8, 5, 3, $$4);
         this.placeBlock($$0, $$18, 8, 5, 9, $$4);
         this.placeBlock($$0, $$18, 8, 5, 10, $$4);
         this.generateBox($$0, $$4, 3, 4, 4, 4, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
         this.generateBox($$0, $$4, 8, 4, 4, 9, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
         this.generateBox($$0, $$4, 3, 5, 4, 4, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
         this.generateBox($$0, $$4, 8, 5, 4, 9, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for (int $$20 = 4; $$20 <= 8; $$20++) {
            for (int $$21 = 0; $$21 <= 2; $$21++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$20, -1, $$21, $$4);
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$20, -1, 12 - $$21, $$4);
            }
         }

         for (int $$22 = 0; $$22 <= 2; $$22++) {
            for (int $$23 = 4; $$23 <= 8; $$23++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$22, -1, $$23, $$4);
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - $$22, -1, $$23, $$4);
            }
         }
      }
   }

   public static class MonsterThrone extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 7;
      private static final int HEIGHT = 8;
      private static final int DEPTH = 9;
      private boolean hasPlacedSpawner;

      public MonsterThrone(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, $$0, $$1);
         this.setOrientation($$2);
      }

      public MonsterThrone(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, $$0);
         this.hasPlacedSpawner = $$0.getBooleanOr("Mob", false);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("Mob", this.hasPlacedSpawner);
      }

      @Nullable
      public static NetherFortressPieces.MonsterThrone createPiece(StructurePieceAccessor $$0, int $$1, int $$2, int $$3, int $$4, Direction $$5) {
         BoundingBox $$6 = BoundingBox.orientBox($$1, $$2, $$3, -2, 0, 0, 7, 8, 9, $$5);
         return isOkBox($$6) && $$0.findCollisionPiece($$6) == null ? new NetherFortressPieces.MonsterThrone($$4, $$6, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 2, 0, 6, 7, 7, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState $$7 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
         BlockState $$8 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true), 1, 6, 3, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), 5, 6, 3, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true).setValue(FenceBlock.NORTH, true), 0, 6, 3, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.NORTH, true), 6, 6, 3, $$4);
         this.generateBox($$0, $$4, 0, 6, 4, 0, 6, 7, $$8, $$8, false);
         this.generateBox($$0, $$4, 6, 6, 4, 6, 6, 7, $$8, $$8, false);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true).setValue(FenceBlock.SOUTH, true), 0, 6, 8, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.SOUTH, true), 6, 6, 8, $$4);
         this.generateBox($$0, $$4, 1, 6, 8, 5, 6, 8, $$7, $$7, false);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), 1, 7, 8, $$4);
         this.generateBox($$0, $$4, 2, 7, 8, 4, 7, 8, $$7, $$7, false);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true), 5, 7, 8, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), 2, 8, 8, $$4);
         this.placeBlock($$0, $$7, 3, 8, 8, $$4);
         this.placeBlock($$0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true), 4, 8, 8, $$4);
         if (!this.hasPlacedSpawner) {
            BlockPos $$9 = this.getWorldPos(3, 5, 5);
            if ($$4.isInside($$9)) {
               this.hasPlacedSpawner = true;
               $$0.setBlock($$9, Blocks.SPAWNER.defaultBlockState(), 2);
               if ($$0.getBlockEntity($$9) instanceof SpawnerBlockEntity $$11) {
                  $$11.setEntityId(EntityType.BLAZE, $$3);
               }
            }
         }

         for (int $$12 = 0; $$12 <= 6; $$12++) {
            for (int $$13 = 0; $$13 <= 6; $$13++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$12, -1, $$13, $$4);
            }
         }
      }
   }

   abstract static class NetherBridgePiece extends StructurePiece {
      protected NetherBridgePiece(StructurePieceType $$0, int $$1, BoundingBox $$2) {
         super($$0, $$1, $$2);
      }

      public NetherBridgePiece(StructurePieceType $$0, CompoundTag $$1) {
         super($$0, $$1);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
      }

      private int updatePieceWeight(List<NetherFortressPieces.PieceWeight> $$0) {
         boolean $$1 = false;
         int $$2 = 0;

         for (NetherFortressPieces.PieceWeight $$3 : $$0) {
            if ($$3.maxPlaceCount > 0 && $$3.placeCount < $$3.maxPlaceCount) {
               $$1 = true;
            }

            $$2 += $$3.weight;
         }

         return $$1 ? $$2 : -1;
      }

      @Nullable
      private NetherFortressPieces.NetherBridgePiece generatePiece(
         NetherFortressPieces.StartPiece $$0,
         List<NetherFortressPieces.PieceWeight> $$1,
         StructurePieceAccessor $$2,
         RandomSource $$3,
         int $$4,
         int $$5,
         int $$6,
         Direction $$7,
         int $$8
      ) {
         int $$9 = this.updatePieceWeight($$1);
         boolean $$10 = $$9 > 0 && $$8 <= 30;
         int $$11 = 0;

         while ($$11 < 5 && $$10) {
            $$11++;
            int $$12 = $$3.nextInt($$9);

            for (NetherFortressPieces.PieceWeight $$13 : $$1) {
               $$12 -= $$13.weight;
               if ($$12 < 0) {
                  if (!$$13.doPlace($$8) || $$13 == $$0.previousPiece && !$$13.allowInRow) {
                     break;
                  }

                  NetherFortressPieces.NetherBridgePiece $$14 = NetherFortressPieces.findAndCreateBridgePieceFactory($$13, $$2, $$3, $$4, $$5, $$6, $$7, $$8);
                  if ($$14 != null) {
                     $$13.placeCount++;
                     $$0.previousPiece = $$13;
                     if (!$$13.isValid()) {
                        $$1.remove($$13);
                     }

                     return $$14;
                  }
               }
            }
         }

         return NetherFortressPieces.BridgeEndFiller.createPiece($$2, $$3, $$4, $$5, $$6, $$7, $$8);
      }

      @Nullable
      private StructurePiece generateAndAddPiece(
         NetherFortressPieces.StartPiece $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4, int $$5, Direction $$6, int $$7, boolean $$8
      ) {
         if (Math.abs($$3 - $$0.getBoundingBox().minX()) <= 112 && Math.abs($$5 - $$0.getBoundingBox().minZ()) <= 112) {
            List<NetherFortressPieces.PieceWeight> $$9 = $$0.availableBridgePieces;
            if ($$8) {
               $$9 = $$0.availableCastlePieces;
            }

            StructurePiece $$10 = this.generatePiece($$0, $$9, $$1, $$2, $$3, $$4, $$5, $$6, $$7 + 1);
            if ($$10 != null) {
               $$1.addPiece($$10);
               $$0.pendingChildren.add($$10);
            }

            return $$10;
         } else {
            return NetherFortressPieces.BridgeEndFiller.createPiece($$1, $$2, $$3, $$4, $$5, $$6, $$7);
         }
      }

      @Nullable
      protected StructurePiece generateChildForward(
         NetherFortressPieces.StartPiece $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4, boolean $$5
      ) {
         Direction $$6 = this.getOrientation();
         if ($$6 != null) {
            switch ($$6) {
               case NORTH:
                  return this.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.minX() + $$3, this.boundingBox.minY() + $$4, this.boundingBox.minZ() - 1, $$6, this.getGenDepth(), $$5
                  );
               case SOUTH:
                  return this.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.minX() + $$3, this.boundingBox.minY() + $$4, this.boundingBox.maxZ() + 1, $$6, this.getGenDepth(), $$5
                  );
               case WEST:
                  return this.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.minX() - 1, this.boundingBox.minY() + $$4, this.boundingBox.minZ() + $$3, $$6, this.getGenDepth(), $$5
                  );
               case EAST:
                  return this.generateAndAddPiece(
                     $$0, $$1, $$2, this.boundingBox.maxX() + 1, this.boundingBox.minY() + $$4, this.boundingBox.minZ() + $$3, $$6, this.getGenDepth(), $$5
                  );
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateChildLeft(
         NetherFortressPieces.StartPiece $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4, boolean $$5
      ) {
         Direction $$6 = this.getOrientation();
         if ($$6 != null) {
            switch ($$6) {
               case NORTH:
                  return this.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() - 1,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() + $$4,
                     Direction.WEST,
                     this.getGenDepth(),
                     $$5
                  );
               case SOUTH:
                  return this.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() - 1,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() + $$4,
                     Direction.WEST,
                     this.getGenDepth(),
                     $$5
                  );
               case WEST:
                  return this.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() + $$4,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() - 1,
                     Direction.NORTH,
                     this.getGenDepth(),
                     $$5
                  );
               case EAST:
                  return this.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() + $$4,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() - 1,
                     Direction.NORTH,
                     this.getGenDepth(),
                     $$5
                  );
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateChildRight(
         NetherFortressPieces.StartPiece $$0, StructurePieceAccessor $$1, RandomSource $$2, int $$3, int $$4, boolean $$5
      ) {
         Direction $$6 = this.getOrientation();
         if ($$6 != null) {
            switch ($$6) {
               case NORTH:
                  return this.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.maxX() + 1,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() + $$4,
                     Direction.EAST,
                     this.getGenDepth(),
                     $$5
                  );
               case SOUTH:
                  return this.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.maxX() + 1,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.minZ() + $$4,
                     Direction.EAST,
                     this.getGenDepth(),
                     $$5
                  );
               case WEST:
                  return this.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() + $$4,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.maxZ() + 1,
                     Direction.SOUTH,
                     this.getGenDepth(),
                     $$5
                  );
               case EAST:
                  return this.generateAndAddPiece(
                     $$0,
                     $$1,
                     $$2,
                     this.boundingBox.minX() + $$4,
                     this.boundingBox.minY() + $$3,
                     this.boundingBox.maxZ() + 1,
                     Direction.SOUTH,
                     this.getGenDepth(),
                     $$5
                  );
            }
         }

         return null;
      }

      protected static boolean isOkBox(BoundingBox $$0) {
         return $$0.minY() > 10;
      }
   }

   static class PieceWeight {
      public final Class<? extends NetherFortressPieces.NetherBridgePiece> pieceClass;
      public final int weight;
      public int placeCount;
      public final int maxPlaceCount;
      public final boolean allowInRow;

      public PieceWeight(Class<? extends NetherFortressPieces.NetherBridgePiece> $$0, int $$1, int $$2, boolean $$3) {
         this.pieceClass = $$0;
         this.weight = $$1;
         this.maxPlaceCount = $$2;
         this.allowInRow = $$3;
      }

      public PieceWeight(Class<? extends NetherFortressPieces.NetherBridgePiece> $$0, int $$1, int $$2) {
         this($$0, $$1, $$2, false);
      }

      public boolean doPlace(int $$0) {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }

      public boolean isValid() {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }
   }

   public static class RoomCrossing extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 7;
      private static final int HEIGHT = 9;
      private static final int DEPTH = 7;

      public RoomCrossing(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, $$0, $$1);
         this.setOrientation($$2);
      }

      public RoomCrossing(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildForward((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 2, 0, false);
         this.generateChildLeft((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 0, 2, false);
         this.generateChildRight((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 0, 2, false);
      }

      @Nullable
      public static NetherFortressPieces.RoomCrossing createPiece(StructurePieceAccessor $$0, int $$1, int $$2, int $$3, Direction $$4, int $$5) {
         BoundingBox $$6 = BoundingBox.orientBox($$1, $$2, $$3, -2, 0, 0, 7, 9, 7, $$4);
         return isOkBox($$6) && $$0.findCollisionPiece($$6) == null ? new NetherFortressPieces.RoomCrossing($$5, $$6, $$4) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 6, 7, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState $$7 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
         BlockState $$8 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
         this.generateBox($$0, $$4, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 5, 0, 4, 5, 0, $$7, $$7, false);
         this.generateBox($$0, $$4, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 5, 6, 4, 5, 6, $$7, $$7, false);
         this.generateBox($$0, $$4, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 5, 2, 0, 5, 4, $$8, $$8, false);
         this.generateBox($$0, $$4, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 6, 5, 2, 6, 5, 4, $$8, $$8, false);

         for (int $$9 = 0; $$9 <= 6; $$9++) {
            for (int $$10 = 0; $$10 <= 6; $$10++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$9, -1, $$10, $$4);
            }
         }
      }
   }

   public static class StairsRoom extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 7;
      private static final int HEIGHT = 11;
      private static final int DEPTH = 7;

      public StairsRoom(int $$0, BoundingBox $$1, Direction $$2) {
         super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, $$0, $$1);
         this.setOrientation($$2);
      }

      public StairsRoom(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, $$0);
      }

      @Override
      public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
         this.generateChildRight((NetherFortressPieces.StartPiece)$$0, $$1, $$2, 6, 2, false);
      }

      @Nullable
      public static NetherFortressPieces.StairsRoom createPiece(StructurePieceAccessor $$0, int $$1, int $$2, int $$3, int $$4, Direction $$5) {
         BoundingBox $$6 = BoundingBox.orientBox($$1, $$2, $$3, -2, 0, 0, 7, 11, 7, $$5);
         return isOkBox($$6) && $$0.findCollisionPiece($$6) == null ? new NetherFortressPieces.StairsRoom($$4, $$6, $$5) : null;
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
         this.generateBox($$0, $$4, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 6, 10, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState $$7 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
         BlockState $$8 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
         this.generateBox($$0, $$4, 0, 3, 2, 0, 5, 4, $$8, $$8, false);
         this.generateBox($$0, $$4, 6, 3, 2, 6, 5, 2, $$8, $$8, false);
         this.generateBox($$0, $$4, 6, 3, 4, 6, 5, 4, $$8, $$8, false);
         this.placeBlock($$0, Blocks.NETHER_BRICKS.defaultBlockState(), 5, 2, 5, $$4);
         this.generateBox($$0, $$4, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 6, 8, 2, 6, 8, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox($$0, $$4, 2, 5, 0, 4, 5, 0, $$7, $$7, false);

         for (int $$9 = 0; $$9 <= 6; $$9++) {
            for (int $$10 = 0; $$10 <= 6; $$10++) {
               this.fillColumnDown($$0, Blocks.NETHER_BRICKS.defaultBlockState(), $$9, -1, $$10, $$4);
            }
         }
      }
   }

   public static class StartPiece extends NetherFortressPieces.BridgeCrossing {
      @Nullable
      NetherFortressPieces.PieceWeight previousPiece;
      final List<NetherFortressPieces.PieceWeight> availableBridgePieces = new ArrayList<>();
      final List<NetherFortressPieces.PieceWeight> availableCastlePieces = new ArrayList<>();
      public final List<StructurePiece> pendingChildren = Lists.newArrayList();

      public StartPiece(RandomSource $$0, int $$1, int $$2) {
         super($$1, $$2, getRandomHorizontalDirection($$0));

         for (NetherFortressPieces.PieceWeight $$3 : NetherFortressPieces.BRIDGE_PIECE_WEIGHTS) {
            $$3.placeCount = 0;
            this.availableBridgePieces.add($$3);
         }

         for (NetherFortressPieces.PieceWeight $$4 : NetherFortressPieces.CASTLE_PIECE_WEIGHTS) {
            $$4.placeCount = 0;
            this.availableCastlePieces.add($$4);
         }
      }

      public StartPiece(CompoundTag $$0) {
         super(StructurePieceType.NETHER_FORTRESS_START, $$0);
      }
   }
}
