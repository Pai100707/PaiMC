package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class OceanMonumentPieces {
   private OceanMonumentPieces() {
   }

   static class FitDoubleXRoom implements OceanMonumentPieces.MonumentRoomFitter {
      @Override
      public boolean fits(OceanMonumentPieces.RoomDefinition $$0) {
         return $$0.hasOpening[Direction.EAST.get3DDataValue()] && !$$0.connections[Direction.EAST.get3DDataValue()].claimed;
      }

      @Override
      public OceanMonumentPieces.OceanMonumentPiece create(Direction $$0, OceanMonumentPieces.RoomDefinition $$1, RandomSource $$2) {
         $$1.claimed = true;
         $$1.connections[Direction.EAST.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleXRoom($$0, $$1);
      }
   }

   static class FitDoubleXYRoom implements OceanMonumentPieces.MonumentRoomFitter {
      @Override
      public boolean fits(OceanMonumentPieces.RoomDefinition $$0) {
         if ($$0.hasOpening[Direction.EAST.get3DDataValue()]
            && !$$0.connections[Direction.EAST.get3DDataValue()].claimed
            && $$0.hasOpening[Direction.UP.get3DDataValue()]
            && !$$0.connections[Direction.UP.get3DDataValue()].claimed) {
            OceanMonumentPieces.RoomDefinition $$1 = $$0.connections[Direction.EAST.get3DDataValue()];
            return $$1.hasOpening[Direction.UP.get3DDataValue()] && !$$1.connections[Direction.UP.get3DDataValue()].claimed;
         } else {
            return false;
         }
      }

      @Override
      public OceanMonumentPieces.OceanMonumentPiece create(Direction $$0, OceanMonumentPieces.RoomDefinition $$1, RandomSource $$2) {
         $$1.claimed = true;
         $$1.connections[Direction.EAST.get3DDataValue()].claimed = true;
         $$1.connections[Direction.UP.get3DDataValue()].claimed = true;
         $$1.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleXYRoom($$0, $$1);
      }
   }

   static class FitDoubleYRoom implements OceanMonumentPieces.MonumentRoomFitter {
      @Override
      public boolean fits(OceanMonumentPieces.RoomDefinition $$0) {
         return $$0.hasOpening[Direction.UP.get3DDataValue()] && !$$0.connections[Direction.UP.get3DDataValue()].claimed;
      }

      @Override
      public OceanMonumentPieces.OceanMonumentPiece create(Direction $$0, OceanMonumentPieces.RoomDefinition $$1, RandomSource $$2) {
         $$1.claimed = true;
         $$1.connections[Direction.UP.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleYRoom($$0, $$1);
      }
   }

   static class FitDoubleYZRoom implements OceanMonumentPieces.MonumentRoomFitter {
      @Override
      public boolean fits(OceanMonumentPieces.RoomDefinition $$0) {
         if ($$0.hasOpening[Direction.NORTH.get3DDataValue()]
            && !$$0.connections[Direction.NORTH.get3DDataValue()].claimed
            && $$0.hasOpening[Direction.UP.get3DDataValue()]
            && !$$0.connections[Direction.UP.get3DDataValue()].claimed) {
            OceanMonumentPieces.RoomDefinition $$1 = $$0.connections[Direction.NORTH.get3DDataValue()];
            return $$1.hasOpening[Direction.UP.get3DDataValue()] && !$$1.connections[Direction.UP.get3DDataValue()].claimed;
         } else {
            return false;
         }
      }

      @Override
      public OceanMonumentPieces.OceanMonumentPiece create(Direction $$0, OceanMonumentPieces.RoomDefinition $$1, RandomSource $$2) {
         $$1.claimed = true;
         $$1.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         $$1.connections[Direction.UP.get3DDataValue()].claimed = true;
         $$1.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleYZRoom($$0, $$1);
      }
   }

   static class FitDoubleZRoom implements OceanMonumentPieces.MonumentRoomFitter {
      @Override
      public boolean fits(OceanMonumentPieces.RoomDefinition $$0) {
         return $$0.hasOpening[Direction.NORTH.get3DDataValue()] && !$$0.connections[Direction.NORTH.get3DDataValue()].claimed;
      }

      @Override
      public OceanMonumentPieces.OceanMonumentPiece create(Direction $$0, OceanMonumentPieces.RoomDefinition $$1, RandomSource $$2) {
         OceanMonumentPieces.RoomDefinition $$3 = $$1;
         if (!$$1.hasOpening[Direction.NORTH.get3DDataValue()] || $$1.connections[Direction.NORTH.get3DDataValue()].claimed) {
            $$3 = $$1.connections[Direction.SOUTH.get3DDataValue()];
         }

         $$3.claimed = true;
         $$3.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleZRoom($$0, $$3);
      }
   }

   static class FitSimpleRoom implements OceanMonumentPieces.MonumentRoomFitter {
      @Override
      public boolean fits(OceanMonumentPieces.RoomDefinition $$0) {
         return true;
      }

      @Override
      public OceanMonumentPieces.OceanMonumentPiece create(Direction $$0, OceanMonumentPieces.RoomDefinition $$1, RandomSource $$2) {
         $$1.claimed = true;
         return new OceanMonumentPieces.OceanMonumentSimpleRoom($$0, $$1, $$2);
      }
   }

   static class FitSimpleTopRoom implements OceanMonumentPieces.MonumentRoomFitter {
      @Override
      public boolean fits(OceanMonumentPieces.RoomDefinition $$0) {
         return !$$0.hasOpening[Direction.WEST.get3DDataValue()]
            && !$$0.hasOpening[Direction.EAST.get3DDataValue()]
            && !$$0.hasOpening[Direction.NORTH.get3DDataValue()]
            && !$$0.hasOpening[Direction.SOUTH.get3DDataValue()]
            && !$$0.hasOpening[Direction.UP.get3DDataValue()];
      }

      @Override
      public OceanMonumentPieces.OceanMonumentPiece create(Direction $$0, OceanMonumentPieces.RoomDefinition $$1, RandomSource $$2) {
         $$1.claimed = true;
         return new OceanMonumentPieces.OceanMonumentSimpleTopRoom($$0, $$1);
      }
   }

   public static class MonumentBuilding extends OceanMonumentPieces.OceanMonumentPiece {
      private static final int WIDTH = 58;
      private static final int HEIGHT = 22;
      private static final int DEPTH = 58;
      public static final int BIOME_RANGE_CHECK = 29;
      private static final int TOP_POSITION = 61;
      private OceanMonumentPieces.RoomDefinition sourceRoom;
      private OceanMonumentPieces.RoomDefinition coreRoom;
      private final List<OceanMonumentPieces.OceanMonumentPiece> childPieces = Lists.newArrayList();

      public MonumentBuilding(RandomSource $$0, int $$1, int $$2, Direction $$3) {
         super(StructurePieceType.OCEAN_MONUMENT_BUILDING, $$3, 0, makeBoundingBox($$1, 39, $$2, $$3, 58, 23, 58));
         this.setOrientation($$3);
         List<OceanMonumentPieces.RoomDefinition> $$4 = this.generateRoomGraph($$0);
         this.sourceRoom.claimed = true;
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentEntryRoom($$3, this.sourceRoom));
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentCoreRoom($$3, this.coreRoom));
         List<OceanMonumentPieces.MonumentRoomFitter> $$5 = Lists.newArrayList();
         $$5.add(new OceanMonumentPieces.FitDoubleXYRoom());
         $$5.add(new OceanMonumentPieces.FitDoubleYZRoom());
         $$5.add(new OceanMonumentPieces.FitDoubleZRoom());
         $$5.add(new OceanMonumentPieces.FitDoubleXRoom());
         $$5.add(new OceanMonumentPieces.FitDoubleYRoom());
         $$5.add(new OceanMonumentPieces.FitSimpleTopRoom());
         $$5.add(new OceanMonumentPieces.FitSimpleRoom());

         for (OceanMonumentPieces.RoomDefinition $$6 : $$4) {
            if (!$$6.claimed && !$$6.isSpecial()) {
               for (OceanMonumentPieces.MonumentRoomFitter $$7 : $$5) {
                  if ($$7.fits($$6)) {
                     this.childPieces.add($$7.create($$3, $$6, $$0));
                     break;
                  }
               }
            }
         }

         BlockPos $$8 = this.getWorldPos(9, 0, 22);

         for (OceanMonumentPieces.OceanMonumentPiece $$9 : this.childPieces) {
            $$9.getBoundingBox().move($$8);
         }

         BoundingBox $$10 = BoundingBox.fromCorners(this.getWorldPos(1, 1, 1), this.getWorldPos(23, 8, 21));
         BoundingBox $$11 = BoundingBox.fromCorners(this.getWorldPos(34, 1, 1), this.getWorldPos(56, 8, 21));
         BoundingBox $$12 = BoundingBox.fromCorners(this.getWorldPos(22, 13, 22), this.getWorldPos(35, 17, 35));
         int $$13 = $$0.nextInt();
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentWingRoom($$3, $$10, $$13++));
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentWingRoom($$3, $$11, $$13++));
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentPenthouse($$3, $$12));
      }

      public MonumentBuilding(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_BUILDING, $$0);
      }

      private List<OceanMonumentPieces.RoomDefinition> generateRoomGraph(RandomSource $$0) {
         OceanMonumentPieces.RoomDefinition[] $$1 = new OceanMonumentPieces.RoomDefinition[75];

         for (int $$2 = 0; $$2 < 5; $$2++) {
            for (int $$3 = 0; $$3 < 4; $$3++) {
               int $$4 = 0;
               int $$5 = getRoomIndex($$2, 0, $$3);
               $$1[$$5] = new OceanMonumentPieces.RoomDefinition($$5);
            }
         }

         for (int $$6 = 0; $$6 < 5; $$6++) {
            for (int $$7 = 0; $$7 < 4; $$7++) {
               int $$8 = 1;
               int $$9 = getRoomIndex($$6, 1, $$7);
               $$1[$$9] = new OceanMonumentPieces.RoomDefinition($$9);
            }
         }

         for (int $$10 = 1; $$10 < 4; $$10++) {
            for (int $$11 = 0; $$11 < 2; $$11++) {
               int $$12 = 2;
               int $$13 = getRoomIndex($$10, 2, $$11);
               $$1[$$13] = new OceanMonumentPieces.RoomDefinition($$13);
            }
         }

         this.sourceRoom = $$1[GRIDROOM_SOURCE_INDEX];

         for (int $$14 = 0; $$14 < 5; $$14++) {
            for (int $$15 = 0; $$15 < 5; $$15++) {
               for (int $$16 = 0; $$16 < 3; $$16++) {
                  int $$17 = getRoomIndex($$14, $$16, $$15);
                  if ($$1[$$17] != null) {
                     for (Direction $$18 : Direction.values()) {
                        int $$19 = $$14 + $$18.getStepX();
                        int $$20 = $$16 + $$18.getStepY();
                        int $$21 = $$15 + $$18.getStepZ();
                        if ($$19 >= 0 && $$19 < 5 && $$21 >= 0 && $$21 < 5 && $$20 >= 0 && $$20 < 3) {
                           int $$22 = getRoomIndex($$19, $$20, $$21);
                           if ($$1[$$22] != null) {
                              if ($$21 == $$15) {
                                 $$1[$$17].setConnection($$18, $$1[$$22]);
                              } else {
                                 $$1[$$17].setConnection($$18.getOpposite(), $$1[$$22]);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         OceanMonumentPieces.RoomDefinition $$23 = new OceanMonumentPieces.RoomDefinition(1003);
         OceanMonumentPieces.RoomDefinition $$24 = new OceanMonumentPieces.RoomDefinition(1001);
         OceanMonumentPieces.RoomDefinition $$25 = new OceanMonumentPieces.RoomDefinition(1002);
         $$1[GRIDROOM_TOP_CONNECT_INDEX].setConnection(Direction.UP, $$23);
         $$1[GRIDROOM_LEFTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, $$24);
         $$1[GRIDROOM_RIGHTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, $$25);
         $$23.claimed = true;
         $$24.claimed = true;
         $$25.claimed = true;
         this.sourceRoom.isSource = true;
         this.coreRoom = $$1[getRoomIndex($$0.nextInt(4), 0, 2)];
         this.coreRoom.claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         ObjectArrayList<OceanMonumentPieces.RoomDefinition> $$26 = new ObjectArrayList();

         for (OceanMonumentPieces.RoomDefinition $$27 : $$1) {
            if ($$27 != null) {
               $$27.updateOpenings();
               $$26.add($$27);
            }
         }

         $$23.updateOpenings();
         Util.shuffle($$26, $$0);
         int $$28 = 1;
         ObjectListIterator var34 = $$26.iterator();

         while (var34.hasNext()) {
            OceanMonumentPieces.RoomDefinition $$29 = (OceanMonumentPieces.RoomDefinition)var34.next();
            int $$30 = 0;
            int $$31 = 0;

            while ($$30 < 2 && $$31 < 5) {
               $$31++;
               int $$32 = $$0.nextInt(6);
               if ($$29.hasOpening[$$32]) {
                  int $$33 = Direction.from3DDataValue($$32).getOpposite().get3DDataValue();
                  $$29.hasOpening[$$32] = false;
                  $$29.connections[$$32].hasOpening[$$33] = false;
                  if ($$29.findSource($$28++) && $$29.connections[$$32].findSource($$28++)) {
                     $$30++;
                  } else {
                     $$29.hasOpening[$$32] = true;
                     $$29.connections[$$32].hasOpening[$$33] = true;
                  }
               }
            }
         }

         $$26.add($$23);
         $$26.add($$24);
         $$26.add($$25);
         return $$26;
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
         int $$7 = Math.max($$0.getSeaLevel(), 64) - this.boundingBox.minY();
         this.generateWaterBox($$0, $$4, 0, 0, 0, 58, $$7, 58);
         this.generateWing(false, 0, $$0, $$3, $$4);
         this.generateWing(true, 33, $$0, $$3, $$4);
         this.generateEntranceArchs($$0, $$3, $$4);
         this.generateEntranceWall($$0, $$3, $$4);
         this.generateRoofPiece($$0, $$3, $$4);
         this.generateLowerWall($$0, $$3, $$4);
         this.generateMiddleWall($$0, $$3, $$4);
         this.generateUpperWall($$0, $$3, $$4);

         for (int $$8 = 0; $$8 < 7; $$8++) {
            int $$9 = 0;

            while ($$9 < 7) {
               if ($$9 == 0 && $$8 == 3) {
                  $$9 = 6;
               }

               int $$10 = $$8 * 9;
               int $$11 = $$9 * 9;

               for (int $$12 = 0; $$12 < 4; $$12++) {
                  for (int $$13 = 0; $$13 < 4; $$13++) {
                     this.placeBlock($$0, BASE_LIGHT, $$10 + $$12, 0, $$11 + $$13, $$4);
                     this.fillColumnDown($$0, BASE_LIGHT, $$10 + $$12, -1, $$11 + $$13, $$4);
                  }
               }

               if ($$8 != 0 && $$8 != 6) {
                  $$9 += 6;
               } else {
                  $$9++;
               }
            }
         }

         for (int $$14 = 0; $$14 < 5; $$14++) {
            this.generateWaterBox($$0, $$4, -1 - $$14, 0 + $$14 * 2, -1 - $$14, -1 - $$14, 23, 58 + $$14);
            this.generateWaterBox($$0, $$4, 58 + $$14, 0 + $$14 * 2, -1 - $$14, 58 + $$14, 23, 58 + $$14);
            this.generateWaterBox($$0, $$4, 0 - $$14, 0 + $$14 * 2, -1 - $$14, 57 + $$14, 23, -1 - $$14);
            this.generateWaterBox($$0, $$4, 0 - $$14, 0 + $$14 * 2, 58 + $$14, 57 + $$14, 23, 58 + $$14);
         }

         for (OceanMonumentPieces.OceanMonumentPiece $$15 : this.childPieces) {
            if ($$15.getBoundingBox().intersects($$4)) {
               $$15.postProcess($$0, $$1, $$2, $$3, $$4, $$5, $$6);
            }
         }
      }

      private void generateWing(boolean $$0, int $$1, net.minecraft.world.level.WorldGenLevel $$2, RandomSource $$3, BoundingBox $$4) {
         int $$5 = 24;
         if (this.chunkIntersects($$4, $$1, 0, $$1 + 23, 20)) {
            this.generateBox($$2, $$4, $$1 + 0, 0, 0, $$1 + 24, 0, 20, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$2, $$4, $$1 + 0, 1, 0, $$1 + 24, 10, 20);

            for (int $$6 = 0; $$6 < 4; $$6++) {
               this.generateBox($$2, $$4, $$1 + $$6, $$6 + 1, $$6, $$1 + $$6, $$6 + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$2, $$4, $$1 + $$6 + 7, $$6 + 5, $$6 + 7, $$1 + $$6 + 7, $$6 + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$2, $$4, $$1 + 17 - $$6, $$6 + 5, $$6 + 7, $$1 + 17 - $$6, $$6 + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$2, $$4, $$1 + 24 - $$6, $$6 + 1, $$6, $$1 + 24 - $$6, $$6 + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$2, $$4, $$1 + $$6 + 1, $$6 + 1, $$6, $$1 + 23 - $$6, $$6 + 1, $$6, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$2, $$4, $$1 + $$6 + 8, $$6 + 5, $$6 + 7, $$1 + 16 - $$6, $$6 + 5, $$6 + 7, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox($$2, $$4, $$1 + 4, 4, 4, $$1 + 6, 4, 20, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$2, $$4, $$1 + 7, 4, 4, $$1 + 17, 4, 6, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$2, $$4, $$1 + 18, 4, 4, $$1 + 20, 4, 20, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$2, $$4, $$1 + 11, 8, 11, $$1 + 13, 8, 20, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock($$2, DOT_DECO_DATA, $$1 + 12, 9, 12, $$4);
            this.placeBlock($$2, DOT_DECO_DATA, $$1 + 12, 9, 15, $$4);
            this.placeBlock($$2, DOT_DECO_DATA, $$1 + 12, 9, 18, $$4);
            int $$7 = $$1 + ($$0 ? 19 : 5);
            int $$8 = $$1 + ($$0 ? 5 : 19);

            for (int $$9 = 20; $$9 >= 5; $$9 -= 3) {
               this.placeBlock($$2, DOT_DECO_DATA, $$7, 5, $$9, $$4);
            }

            for (int $$10 = 19; $$10 >= 7; $$10 -= 3) {
               this.placeBlock($$2, DOT_DECO_DATA, $$8, 5, $$10, $$4);
            }

            for (int $$11 = 0; $$11 < 4; $$11++) {
               int $$12 = $$0 ? $$1 + 24 - (17 - $$11 * 3) : $$1 + 17 - $$11 * 3;
               this.placeBlock($$2, DOT_DECO_DATA, $$12, 5, 5, $$4);
            }

            this.placeBlock($$2, DOT_DECO_DATA, $$8, 5, 5, $$4);
            this.generateBox($$2, $$4, $$1 + 11, 1, 12, $$1 + 13, 7, 12, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$2, $$4, $$1 + 12, 1, 11, $$1 + 12, 7, 13, BASE_GRAY, BASE_GRAY, false);
         }
      }

      private void generateEntranceArchs(net.minecraft.world.level.WorldGenLevel $$0, RandomSource $$1, BoundingBox $$2) {
         if (this.chunkIntersects($$2, 22, 5, 35, 17)) {
            this.generateWaterBox($$0, $$2, 25, 0, 0, 32, 8, 20);

            for (int $$3 = 0; $$3 < 4; $$3++) {
               this.generateBox($$0, $$2, 24, 2, 5 + $$3 * 4, 24, 4, 5 + $$3 * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$2, 22, 4, 5 + $$3 * 4, 23, 4, 5 + $$3 * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.placeBlock($$0, BASE_LIGHT, 25, 5, 5 + $$3 * 4, $$2);
               this.placeBlock($$0, BASE_LIGHT, 26, 6, 5 + $$3 * 4, $$2);
               this.placeBlock($$0, LAMP_BLOCK, 26, 5, 5 + $$3 * 4, $$2);
               this.generateBox($$0, $$2, 33, 2, 5 + $$3 * 4, 33, 4, 5 + $$3 * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$2, 34, 4, 5 + $$3 * 4, 35, 4, 5 + $$3 * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.placeBlock($$0, BASE_LIGHT, 32, 5, 5 + $$3 * 4, $$2);
               this.placeBlock($$0, BASE_LIGHT, 31, 6, 5 + $$3 * 4, $$2);
               this.placeBlock($$0, LAMP_BLOCK, 31, 5, 5 + $$3 * 4, $$2);
               this.generateBox($$0, $$2, 27, 6, 5 + $$3 * 4, 30, 6, 5 + $$3 * 4, BASE_GRAY, BASE_GRAY, false);
            }
         }
      }

      private void generateEntranceWall(net.minecraft.world.level.WorldGenLevel $$0, RandomSource $$1, BoundingBox $$2) {
         if (this.chunkIntersects($$2, 15, 20, 42, 21)) {
            this.generateBox($$0, $$2, 15, 0, 21, 42, 0, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 26, 1, 21, 31, 3, 21);
            this.generateBox($$0, $$2, 21, 12, 21, 36, 12, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 17, 11, 21, 40, 11, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 16, 10, 21, 41, 10, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 15, 7, 21, 42, 9, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 16, 6, 21, 41, 6, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 17, 5, 21, 40, 5, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 21, 4, 21, 36, 4, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 22, 3, 21, 26, 3, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 31, 3, 21, 35, 3, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 23, 2, 21, 25, 2, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 32, 2, 21, 34, 2, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 28, 4, 20, 29, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock($$0, BASE_LIGHT, 27, 3, 21, $$2);
            this.placeBlock($$0, BASE_LIGHT, 30, 3, 21, $$2);
            this.placeBlock($$0, BASE_LIGHT, 26, 2, 21, $$2);
            this.placeBlock($$0, BASE_LIGHT, 31, 2, 21, $$2);
            this.placeBlock($$0, BASE_LIGHT, 25, 1, 21, $$2);
            this.placeBlock($$0, BASE_LIGHT, 32, 1, 21, $$2);

            for (int $$3 = 0; $$3 < 7; $$3++) {
               this.placeBlock($$0, BASE_BLACK, 28 - $$3, 6 + $$3, 21, $$2);
               this.placeBlock($$0, BASE_BLACK, 29 + $$3, 6 + $$3, 21, $$2);
            }

            for (int $$4 = 0; $$4 < 4; $$4++) {
               this.placeBlock($$0, BASE_BLACK, 28 - $$4, 9 + $$4, 21, $$2);
               this.placeBlock($$0, BASE_BLACK, 29 + $$4, 9 + $$4, 21, $$2);
            }

            this.placeBlock($$0, BASE_BLACK, 28, 12, 21, $$2);
            this.placeBlock($$0, BASE_BLACK, 29, 12, 21, $$2);

            for (int $$5 = 0; $$5 < 3; $$5++) {
               this.placeBlock($$0, BASE_BLACK, 22 - $$5 * 2, 8, 21, $$2);
               this.placeBlock($$0, BASE_BLACK, 22 - $$5 * 2, 9, 21, $$2);
               this.placeBlock($$0, BASE_BLACK, 35 + $$5 * 2, 8, 21, $$2);
               this.placeBlock($$0, BASE_BLACK, 35 + $$5 * 2, 9, 21, $$2);
            }

            this.generateWaterBox($$0, $$2, 15, 13, 21, 42, 15, 21);
            this.generateWaterBox($$0, $$2, 15, 1, 21, 15, 6, 21);
            this.generateWaterBox($$0, $$2, 16, 1, 21, 16, 5, 21);
            this.generateWaterBox($$0, $$2, 17, 1, 21, 20, 4, 21);
            this.generateWaterBox($$0, $$2, 21, 1, 21, 21, 3, 21);
            this.generateWaterBox($$0, $$2, 22, 1, 21, 22, 2, 21);
            this.generateWaterBox($$0, $$2, 23, 1, 21, 24, 1, 21);
            this.generateWaterBox($$0, $$2, 42, 1, 21, 42, 6, 21);
            this.generateWaterBox($$0, $$2, 41, 1, 21, 41, 5, 21);
            this.generateWaterBox($$0, $$2, 37, 1, 21, 40, 4, 21);
            this.generateWaterBox($$0, $$2, 36, 1, 21, 36, 3, 21);
            this.generateWaterBox($$0, $$2, 33, 1, 21, 34, 1, 21);
            this.generateWaterBox($$0, $$2, 35, 1, 21, 35, 2, 21);
         }
      }

      private void generateRoofPiece(net.minecraft.world.level.WorldGenLevel $$0, RandomSource $$1, BoundingBox $$2) {
         if (this.chunkIntersects($$2, 21, 21, 36, 36)) {
            this.generateBox($$0, $$2, 21, 0, 22, 36, 0, 36, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 21, 1, 22, 36, 23, 36);

            for (int $$3 = 0; $$3 < 4; $$3++) {
               this.generateBox($$0, $$2, 21 + $$3, 13 + $$3, 21 + $$3, 36 - $$3, 13 + $$3, 21 + $$3, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$2, 21 + $$3, 13 + $$3, 36 - $$3, 36 - $$3, 13 + $$3, 36 - $$3, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$2, 21 + $$3, 13 + $$3, 22 + $$3, 21 + $$3, 13 + $$3, 35 - $$3, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$2, 36 - $$3, 13 + $$3, 22 + $$3, 36 - $$3, 13 + $$3, 35 - $$3, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox($$0, $$2, 25, 16, 25, 32, 16, 32, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 25, 17, 25, 25, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$2, 32, 17, 25, 32, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$2, 25, 17, 32, 25, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$2, 32, 17, 32, 32, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock($$0, BASE_LIGHT, 26, 20, 26, $$2);
            this.placeBlock($$0, BASE_LIGHT, 27, 21, 27, $$2);
            this.placeBlock($$0, LAMP_BLOCK, 27, 20, 27, $$2);
            this.placeBlock($$0, BASE_LIGHT, 26, 20, 31, $$2);
            this.placeBlock($$0, BASE_LIGHT, 27, 21, 30, $$2);
            this.placeBlock($$0, LAMP_BLOCK, 27, 20, 30, $$2);
            this.placeBlock($$0, BASE_LIGHT, 31, 20, 31, $$2);
            this.placeBlock($$0, BASE_LIGHT, 30, 21, 30, $$2);
            this.placeBlock($$0, LAMP_BLOCK, 30, 20, 30, $$2);
            this.placeBlock($$0, BASE_LIGHT, 31, 20, 26, $$2);
            this.placeBlock($$0, BASE_LIGHT, 30, 21, 27, $$2);
            this.placeBlock($$0, LAMP_BLOCK, 30, 20, 27, $$2);
            this.generateBox($$0, $$2, 28, 21, 27, 29, 21, 27, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 27, 21, 28, 27, 21, 29, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 28, 21, 30, 29, 21, 30, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 30, 21, 28, 30, 21, 29, BASE_GRAY, BASE_GRAY, false);
         }
      }

      private void generateLowerWall(net.minecraft.world.level.WorldGenLevel $$0, RandomSource $$1, BoundingBox $$2) {
         if (this.chunkIntersects($$2, 0, 21, 6, 58)) {
            this.generateBox($$0, $$2, 0, 0, 21, 6, 0, 57, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 0, 1, 21, 6, 7, 57);
            this.generateBox($$0, $$2, 4, 4, 21, 6, 4, 53, BASE_GRAY, BASE_GRAY, false);

            for (int $$3 = 0; $$3 < 4; $$3++) {
               this.generateBox($$0, $$2, $$3, $$3 + 1, 21, $$3, $$3 + 1, 57 - $$3, BASE_LIGHT, BASE_LIGHT, false);
            }

            for (int $$4 = 23; $$4 < 53; $$4 += 3) {
               this.placeBlock($$0, DOT_DECO_DATA, 5, 5, $$4, $$2);
            }

            this.placeBlock($$0, DOT_DECO_DATA, 5, 5, 52, $$2);

            for (int $$5 = 0; $$5 < 4; $$5++) {
               this.generateBox($$0, $$2, $$5, $$5 + 1, 21, $$5, $$5 + 1, 57 - $$5, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox($$0, $$2, 4, 1, 52, 6, 3, 52, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 5, 1, 51, 5, 3, 53, BASE_GRAY, BASE_GRAY, false);
         }

         if (this.chunkIntersects($$2, 51, 21, 58, 58)) {
            this.generateBox($$0, $$2, 51, 0, 21, 57, 0, 57, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 51, 1, 21, 57, 7, 57);
            this.generateBox($$0, $$2, 51, 4, 21, 53, 4, 53, BASE_GRAY, BASE_GRAY, false);

            for (int $$6 = 0; $$6 < 4; $$6++) {
               this.generateBox($$0, $$2, 57 - $$6, $$6 + 1, 21, 57 - $$6, $$6 + 1, 57 - $$6, BASE_LIGHT, BASE_LIGHT, false);
            }

            for (int $$7 = 23; $$7 < 53; $$7 += 3) {
               this.placeBlock($$0, DOT_DECO_DATA, 52, 5, $$7, $$2);
            }

            this.placeBlock($$0, DOT_DECO_DATA, 52, 5, 52, $$2);
            this.generateBox($$0, $$2, 51, 1, 52, 53, 3, 52, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 52, 1, 51, 52, 3, 53, BASE_GRAY, BASE_GRAY, false);
         }

         if (this.chunkIntersects($$2, 0, 51, 57, 57)) {
            this.generateBox($$0, $$2, 7, 0, 51, 50, 0, 57, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 7, 1, 51, 50, 10, 57);

            for (int $$8 = 0; $$8 < 4; $$8++) {
               this.generateBox($$0, $$2, $$8 + 1, $$8 + 1, 57 - $$8, 56 - $$8, $$8 + 1, 57 - $$8, BASE_LIGHT, BASE_LIGHT, false);
            }
         }
      }

      private void generateMiddleWall(net.minecraft.world.level.WorldGenLevel $$0, RandomSource $$1, BoundingBox $$2) {
         if (this.chunkIntersects($$2, 7, 21, 13, 50)) {
            this.generateBox($$0, $$2, 7, 0, 21, 13, 0, 50, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 7, 1, 21, 13, 10, 50);
            this.generateBox($$0, $$2, 11, 8, 21, 13, 8, 53, BASE_GRAY, BASE_GRAY, false);

            for (int $$3 = 0; $$3 < 4; $$3++) {
               this.generateBox($$0, $$2, $$3 + 7, $$3 + 5, 21, $$3 + 7, $$3 + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
            }

            for (int $$4 = 21; $$4 <= 45; $$4 += 3) {
               this.placeBlock($$0, DOT_DECO_DATA, 12, 9, $$4, $$2);
            }
         }

         if (this.chunkIntersects($$2, 44, 21, 50, 54)) {
            this.generateBox($$0, $$2, 44, 0, 21, 50, 0, 50, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 44, 1, 21, 50, 10, 50);
            this.generateBox($$0, $$2, 44, 8, 21, 46, 8, 53, BASE_GRAY, BASE_GRAY, false);

            for (int $$5 = 0; $$5 < 4; $$5++) {
               this.generateBox($$0, $$2, 50 - $$5, $$5 + 5, 21, 50 - $$5, $$5 + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
            }

            for (int $$6 = 21; $$6 <= 45; $$6 += 3) {
               this.placeBlock($$0, DOT_DECO_DATA, 45, 9, $$6, $$2);
            }
         }

         if (this.chunkIntersects($$2, 8, 44, 49, 54)) {
            this.generateBox($$0, $$2, 14, 0, 44, 43, 0, 50, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 14, 1, 44, 43, 10, 50);

            for (int $$7 = 12; $$7 <= 45; $$7 += 3) {
               this.placeBlock($$0, DOT_DECO_DATA, $$7, 9, 45, $$2);
               this.placeBlock($$0, DOT_DECO_DATA, $$7, 9, 52, $$2);
               if ($$7 == 12 || $$7 == 18 || $$7 == 24 || $$7 == 33 || $$7 == 39 || $$7 == 45) {
                  this.placeBlock($$0, DOT_DECO_DATA, $$7, 9, 47, $$2);
                  this.placeBlock($$0, DOT_DECO_DATA, $$7, 9, 50, $$2);
                  this.placeBlock($$0, DOT_DECO_DATA, $$7, 10, 45, $$2);
                  this.placeBlock($$0, DOT_DECO_DATA, $$7, 10, 46, $$2);
                  this.placeBlock($$0, DOT_DECO_DATA, $$7, 10, 51, $$2);
                  this.placeBlock($$0, DOT_DECO_DATA, $$7, 10, 52, $$2);
                  this.placeBlock($$0, DOT_DECO_DATA, $$7, 11, 47, $$2);
                  this.placeBlock($$0, DOT_DECO_DATA, $$7, 11, 50, $$2);
                  this.placeBlock($$0, DOT_DECO_DATA, $$7, 12, 48, $$2);
                  this.placeBlock($$0, DOT_DECO_DATA, $$7, 12, 49, $$2);
               }
            }

            for (int $$8 = 0; $$8 < 3; $$8++) {
               this.generateBox($$0, $$2, 8 + $$8, 5 + $$8, 54, 49 - $$8, 5 + $$8, 54, BASE_GRAY, BASE_GRAY, false);
            }

            this.generateBox($$0, $$2, 11, 8, 54, 46, 8, 54, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$2, 14, 8, 44, 43, 8, 53, BASE_GRAY, BASE_GRAY, false);
         }
      }

      private void generateUpperWall(net.minecraft.world.level.WorldGenLevel $$0, RandomSource $$1, BoundingBox $$2) {
         if (this.chunkIntersects($$2, 14, 21, 20, 43)) {
            this.generateBox($$0, $$2, 14, 0, 21, 20, 0, 43, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 14, 1, 22, 20, 14, 43);
            this.generateBox($$0, $$2, 18, 12, 22, 20, 12, 39, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 18, 12, 21, 20, 12, 21, BASE_LIGHT, BASE_LIGHT, false);

            for (int $$3 = 0; $$3 < 4; $$3++) {
               this.generateBox($$0, $$2, $$3 + 14, $$3 + 9, 21, $$3 + 14, $$3 + 9, 43 - $$3, BASE_LIGHT, BASE_LIGHT, false);
            }

            for (int $$4 = 23; $$4 <= 39; $$4 += 3) {
               this.placeBlock($$0, DOT_DECO_DATA, 19, 13, $$4, $$2);
            }
         }

         if (this.chunkIntersects($$2, 37, 21, 43, 43)) {
            this.generateBox($$0, $$2, 37, 0, 21, 43, 0, 43, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 37, 1, 22, 43, 14, 43);
            this.generateBox($$0, $$2, 37, 12, 22, 39, 12, 39, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$2, 37, 12, 21, 39, 12, 21, BASE_LIGHT, BASE_LIGHT, false);

            for (int $$5 = 0; $$5 < 4; $$5++) {
               this.generateBox($$0, $$2, 43 - $$5, $$5 + 9, 21, 43 - $$5, $$5 + 9, 43 - $$5, BASE_LIGHT, BASE_LIGHT, false);
            }

            for (int $$6 = 23; $$6 <= 39; $$6 += 3) {
               this.placeBlock($$0, DOT_DECO_DATA, 38, 13, $$6, $$2);
            }
         }

         if (this.chunkIntersects($$2, 15, 37, 42, 43)) {
            this.generateBox($$0, $$2, 21, 0, 37, 36, 0, 43, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox($$0, $$2, 21, 1, 37, 36, 14, 43);
            this.generateBox($$0, $$2, 21, 12, 37, 36, 12, 39, BASE_GRAY, BASE_GRAY, false);

            for (int $$7 = 0; $$7 < 4; $$7++) {
               this.generateBox($$0, $$2, 15 + $$7, $$7 + 9, 43 - $$7, 42 - $$7, $$7 + 9, 43 - $$7, BASE_LIGHT, BASE_LIGHT, false);
            }

            for (int $$8 = 21; $$8 <= 36; $$8 += 3) {
               this.placeBlock($$0, DOT_DECO_DATA, $$8, 13, 38, $$2);
            }
         }
      }
   }

   interface MonumentRoomFitter {
      boolean fits(OceanMonumentPieces.RoomDefinition var1);

      OceanMonumentPieces.OceanMonumentPiece create(Direction var1, OceanMonumentPieces.RoomDefinition var2, RandomSource var3);
   }

   public static class OceanMonumentCoreRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentCoreRoom(Direction $$0, OceanMonumentPieces.RoomDefinition $$1) {
         super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, 1, $$0, $$1, 2, 2, 2);
      }

      public OceanMonumentCoreRoom(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, $$0);
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
         this.generateBoxOnFillOnly($$0, $$4, 1, 8, 0, 14, 8, 14, BASE_GRAY);
         int $$7 = 7;
         BlockState $$8 = BASE_LIGHT;
         this.generateBox($$0, $$4, 0, 7, 0, 0, 7, 15, $$8, $$8, false);
         this.generateBox($$0, $$4, 15, 7, 0, 15, 7, 15, $$8, $$8, false);
         this.generateBox($$0, $$4, 1, 7, 0, 15, 7, 0, $$8, $$8, false);
         this.generateBox($$0, $$4, 1, 7, 15, 14, 7, 15, $$8, $$8, false);

         for (int $$9 = 1; $$9 <= 6; $$9++) {
            $$8 = BASE_LIGHT;
            if ($$9 == 2 || $$9 == 6) {
               $$8 = BASE_GRAY;
            }

            for (int $$11 = 0; $$11 <= 15; $$11 += 15) {
               this.generateBox($$0, $$4, $$11, $$9, 0, $$11, $$9, 1, $$8, $$8, false);
               this.generateBox($$0, $$4, $$11, $$9, 6, $$11, $$9, 9, $$8, $$8, false);
               this.generateBox($$0, $$4, $$11, $$9, 14, $$11, $$9, 15, $$8, $$8, false);
            }

            this.generateBox($$0, $$4, 1, $$9, 0, 1, $$9, 0, $$8, $$8, false);
            this.generateBox($$0, $$4, 6, $$9, 0, 9, $$9, 0, $$8, $$8, false);
            this.generateBox($$0, $$4, 14, $$9, 0, 14, $$9, 0, $$8, $$8, false);
            this.generateBox($$0, $$4, 1, $$9, 15, 14, $$9, 15, $$8, $$8, false);
         }

         this.generateBox($$0, $$4, 6, 3, 6, 9, 6, 9, BASE_BLACK, BASE_BLACK, false);
         this.generateBox($$0, $$4, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.GOLD_BLOCK.defaultBlockState(), false);

         for (int $$12 = 3; $$12 <= 6; $$12 += 3) {
            for (int $$13 = 6; $$13 <= 9; $$13 += 3) {
               this.placeBlock($$0, LAMP_BLOCK, $$13, $$12, 6, $$4);
               this.placeBlock($$0, LAMP_BLOCK, $$13, $$12, 9, $$4);
            }
         }

         this.generateBox($$0, $$4, 5, 1, 6, 5, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 1, 9, 5, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 10, 1, 6, 10, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 10, 1, 9, 10, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, 1, 5, 6, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 9, 1, 5, 9, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, 1, 10, 6, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 9, 1, 10, 9, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 2, 5, 5, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 2, 10, 5, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 10, 2, 5, 10, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 10, 2, 10, 10, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 7, 1, 5, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 10, 7, 1, 10, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 7, 9, 5, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 10, 7, 9, 10, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 7, 5, 6, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 7, 10, 6, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 9, 7, 5, 14, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 9, 7, 10, 14, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 2, 1, 2, 2, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 3, 1, 2, 3, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 13, 1, 2, 13, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 12, 1, 2, 12, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 2, 1, 12, 2, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 3, 1, 13, 3, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 13, 1, 12, 13, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 12, 1, 13, 12, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
      }
   }

   public static class OceanMonumentDoubleXRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleXRoom(Direction $$0, OceanMonumentPieces.RoomDefinition $$1) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, 1, $$0, $$1, 2, 1, 1);
      }

      public OceanMonumentDoubleXRoom(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, $$0);
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
         OceanMonumentPieces.RoomDefinition $$7 = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition $$8 = this.roomDefinition;
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor($$0, $$4, 8, 0, $$7.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor($$0, $$4, 0, 0, $$8.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if ($$8.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 1, 4, 1, 7, 4, 6, BASE_GRAY);
         }

         if ($$7.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 8, 4, 1, 14, 4, 6, BASE_GRAY);
         }

         this.generateBox($$0, $$4, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 15, 3, 0, 15, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 3, 0, 15, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 3, 7, 14, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 0, 2, 0, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 15, 2, 0, 15, 2, 7, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 1, 2, 0, 15, 2, 0, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 1, 2, 7, 14, 2, 7, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 15, 1, 0, 15, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 1, 0, 15, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 1, 0, 10, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, 2, 0, 9, 2, 3, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 5, 3, 0, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock($$0, LAMP_BLOCK, 6, 2, 3, $$4);
         this.placeBlock($$0, LAMP_BLOCK, 9, 2, 3, $$4);
         if ($$8.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 1, 0, 4, 2, 0);
         }

         if ($$8.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 1, 7, 4, 2, 7);
         }

         if ($$8.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 0, 1, 3, 0, 2, 4);
         }

         if ($$7.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 11, 1, 0, 12, 2, 0);
         }

         if ($$7.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 11, 1, 7, 12, 2, 7);
         }

         if ($$7.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 15, 1, 3, 15, 2, 4);
         }
      }
   }

   public static class OceanMonumentDoubleXYRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleXYRoom(Direction $$0, OceanMonumentPieces.RoomDefinition $$1) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, 1, $$0, $$1, 2, 2, 1);
      }

      public OceanMonumentDoubleXYRoom(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, $$0);
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
         OceanMonumentPieces.RoomDefinition $$7 = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition $$8 = this.roomDefinition;
         OceanMonumentPieces.RoomDefinition $$9 = $$8.connections[Direction.UP.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition $$10 = $$7.connections[Direction.UP.get3DDataValue()];
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor($$0, $$4, 8, 0, $$7.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor($$0, $$4, 0, 0, $$8.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if ($$9.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 1, 8, 1, 7, 8, 6, BASE_GRAY);
         }

         if ($$10.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 8, 8, 1, 14, 8, 6, BASE_GRAY);
         }

         for (int $$11 = 1; $$11 <= 7; $$11++) {
            BlockState $$12 = BASE_LIGHT;
            if ($$11 == 2 || $$11 == 6) {
               $$12 = BASE_GRAY;
            }

            this.generateBox($$0, $$4, 0, $$11, 0, 0, $$11, 7, $$12, $$12, false);
            this.generateBox($$0, $$4, 15, $$11, 0, 15, $$11, 7, $$12, $$12, false);
            this.generateBox($$0, $$4, 1, $$11, 0, 15, $$11, 0, $$12, $$12, false);
            this.generateBox($$0, $$4, 1, $$11, 7, 14, $$11, 7, $$12, $$12, false);
         }

         this.generateBox($$0, $$4, 2, 1, 3, 2, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 3, 1, 2, 4, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 3, 1, 5, 4, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 13, 1, 3, 13, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 11, 1, 2, 12, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 11, 1, 5, 12, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 1, 3, 5, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 10, 1, 3, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 7, 2, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 5, 2, 5, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 10, 5, 2, 10, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 5, 5, 5, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 10, 5, 5, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock($$0, BASE_LIGHT, 6, 6, 2, $$4);
         this.placeBlock($$0, BASE_LIGHT, 9, 6, 2, $$4);
         this.placeBlock($$0, BASE_LIGHT, 6, 6, 5, $$4);
         this.placeBlock($$0, BASE_LIGHT, 9, 6, 5, $$4);
         this.generateBox($$0, $$4, 5, 4, 3, 6, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 9, 4, 3, 10, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock($$0, LAMP_BLOCK, 5, 4, 2, $$4);
         this.placeBlock($$0, LAMP_BLOCK, 5, 4, 5, $$4);
         this.placeBlock($$0, LAMP_BLOCK, 10, 4, 2, $$4);
         this.placeBlock($$0, LAMP_BLOCK, 10, 4, 5, $$4);
         if ($$8.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 1, 0, 4, 2, 0);
         }

         if ($$8.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 1, 7, 4, 2, 7);
         }

         if ($$8.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 0, 1, 3, 0, 2, 4);
         }

         if ($$7.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 11, 1, 0, 12, 2, 0);
         }

         if ($$7.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 11, 1, 7, 12, 2, 7);
         }

         if ($$7.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 15, 1, 3, 15, 2, 4);
         }

         if ($$9.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 5, 0, 4, 6, 0);
         }

         if ($$9.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 5, 7, 4, 6, 7);
         }

         if ($$9.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 0, 5, 3, 0, 6, 4);
         }

         if ($$10.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 11, 5, 0, 12, 6, 0);
         }

         if ($$10.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 11, 5, 7, 12, 6, 7);
         }

         if ($$10.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 15, 5, 3, 15, 6, 4);
         }
      }
   }

   public static class OceanMonumentDoubleYRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleYRoom(Direction $$0, OceanMonumentPieces.RoomDefinition $$1) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, 1, $$0, $$1, 1, 2, 1);
      }

      public OceanMonumentDoubleYRoom(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, $$0);
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
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor($$0, $$4, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         OceanMonumentPieces.RoomDefinition $$7 = this.roomDefinition.connections[Direction.UP.get3DDataValue()];
         if ($$7.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 1, 8, 1, 6, 8, 6, BASE_GRAY);
         }

         this.generateBox($$0, $$4, 0, 4, 0, 0, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 7, 4, 0, 7, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 4, 0, 6, 4, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 4, 7, 6, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 2, 4, 1, 2, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 4, 2, 1, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 4, 1, 5, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, 4, 2, 6, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 2, 4, 5, 2, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 4, 5, 1, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 4, 5, 5, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, 4, 5, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
         OceanMonumentPieces.RoomDefinition $$8 = this.roomDefinition;

         for (int $$9 = 1; $$9 <= 5; $$9 += 4) {
            int $$10 = 0;
            if ($$8.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateBox($$0, $$4, 2, $$9, $$10, 2, $$9 + 2, $$10, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 5, $$9, $$10, 5, $$9 + 2, $$10, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 3, $$9 + 2, $$10, 4, $$9 + 2, $$10, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox($$0, $$4, 0, $$9, $$10, 7, $$9 + 2, $$10, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 0, $$9 + 1, $$10, 7, $$9 + 1, $$10, BASE_GRAY, BASE_GRAY, false);
            }

            int var13 = 7;
            if ($$8.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateBox($$0, $$4, 2, $$9, var13, 2, $$9 + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 5, $$9, var13, 5, $$9 + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 3, $$9 + 2, var13, 4, $$9 + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox($$0, $$4, 0, $$9, var13, 7, $$9 + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 0, $$9 + 1, var13, 7, $$9 + 1, var13, BASE_GRAY, BASE_GRAY, false);
            }

            int $$11 = 0;
            if ($$8.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateBox($$0, $$4, $$11, $$9, 2, $$11, $$9 + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, $$11, $$9, 5, $$11, $$9 + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, $$11, $$9 + 2, 3, $$11, $$9 + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox($$0, $$4, $$11, $$9, 0, $$11, $$9 + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, $$11, $$9 + 1, 0, $$11, $$9 + 1, 7, BASE_GRAY, BASE_GRAY, false);
            }

            int var14 = 7;
            if ($$8.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateBox($$0, $$4, var14, $$9, 2, var14, $$9 + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, var14, $$9, 5, var14, $$9 + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, var14, $$9 + 2, 3, var14, $$9 + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox($$0, $$4, var14, $$9, 0, var14, $$9 + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, var14, $$9 + 1, 0, var14, $$9 + 1, 7, BASE_GRAY, BASE_GRAY, false);
            }

            $$8 = $$7;
         }
      }
   }

   public static class OceanMonumentDoubleYZRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleYZRoom(Direction $$0, OceanMonumentPieces.RoomDefinition $$1) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, 1, $$0, $$1, 1, 2, 2);
      }

      public OceanMonumentDoubleYZRoom(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, $$0);
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
         OceanMonumentPieces.RoomDefinition $$7 = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition $$8 = this.roomDefinition;
         OceanMonumentPieces.RoomDefinition $$9 = $$7.connections[Direction.UP.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition $$10 = $$8.connections[Direction.UP.get3DDataValue()];
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor($$0, $$4, 0, 8, $$7.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor($$0, $$4, 0, 0, $$8.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if ($$10.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 1, 8, 1, 6, 8, 7, BASE_GRAY);
         }

         if ($$9.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 1, 8, 8, 6, 8, 14, BASE_GRAY);
         }

         for (int $$11 = 1; $$11 <= 7; $$11++) {
            BlockState $$12 = BASE_LIGHT;
            if ($$11 == 2 || $$11 == 6) {
               $$12 = BASE_GRAY;
            }

            this.generateBox($$0, $$4, 0, $$11, 0, 0, $$11, 15, $$12, $$12, false);
            this.generateBox($$0, $$4, 7, $$11, 0, 7, $$11, 15, $$12, $$12, false);
            this.generateBox($$0, $$4, 1, $$11, 0, 6, $$11, 0, $$12, $$12, false);
            this.generateBox($$0, $$4, 1, $$11, 15, 6, $$11, 15, $$12, $$12, false);
         }

         for (int $$13 = 1; $$13 <= 7; $$13++) {
            BlockState $$14 = BASE_BLACK;
            if ($$13 == 2 || $$13 == 6) {
               $$14 = LAMP_BLOCK;
            }

            this.generateBox($$0, $$4, 3, $$13, 7, 4, $$13, 8, $$14, $$14, false);
         }

         if ($$8.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 1, 0, 4, 2, 0);
         }

         if ($$8.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 7, 1, 3, 7, 2, 4);
         }

         if ($$8.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 0, 1, 3, 0, 2, 4);
         }

         if ($$7.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 1, 15, 4, 2, 15);
         }

         if ($$7.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 0, 1, 11, 0, 2, 12);
         }

         if ($$7.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 7, 1, 11, 7, 2, 12);
         }

         if ($$10.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 5, 0, 4, 6, 0);
         }

         if ($$10.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 7, 5, 3, 7, 6, 4);
            this.generateBox($$0, $$4, 5, 4, 2, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 6, 1, 2, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 6, 1, 5, 6, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
         }

         if ($$10.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 0, 5, 3, 0, 6, 4);
            this.generateBox($$0, $$4, 1, 4, 2, 2, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 1, 1, 2, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 1, 1, 5, 1, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
         }

         if ($$9.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 5, 15, 4, 6, 15);
         }

         if ($$9.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 0, 5, 11, 0, 6, 12);
            this.generateBox($$0, $$4, 1, 4, 10, 2, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 1, 1, 10, 1, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 1, 1, 13, 1, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
         }

         if ($$9.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 7, 5, 11, 7, 6, 12);
            this.generateBox($$0, $$4, 5, 4, 10, 6, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 6, 1, 10, 6, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 6, 1, 13, 6, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
         }
      }
   }

   public static class OceanMonumentDoubleZRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleZRoom(Direction $$0, OceanMonumentPieces.RoomDefinition $$1) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, 1, $$0, $$1, 1, 1, 2);
      }

      public OceanMonumentDoubleZRoom(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, $$0);
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
         OceanMonumentPieces.RoomDefinition $$7 = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition $$8 = this.roomDefinition;
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor($$0, $$4, 0, 8, $$7.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor($$0, $$4, 0, 0, $$8.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if ($$8.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 1, 4, 1, 6, 4, 7, BASE_GRAY);
         }

         if ($$7.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 1, 4, 8, 6, 4, 14, BASE_GRAY);
         }

         this.generateBox($$0, $$4, 0, 3, 0, 0, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 7, 3, 0, 7, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 3, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 3, 15, 6, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 0, 2, 0, 0, 2, 15, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 7, 2, 0, 7, 2, 15, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 1, 2, 0, 7, 2, 0, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 1, 2, 15, 6, 2, 15, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 0, 1, 0, 0, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 7, 1, 0, 7, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 1, 0, 7, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 1, 15, 6, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 1, 1, 1, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, 1, 1, 6, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 3, 1, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, 3, 1, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 1, 13, 1, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, 1, 13, 6, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 3, 13, 1, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, 3, 13, 6, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 2, 1, 6, 2, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 1, 6, 5, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 2, 1, 9, 2, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 1, 9, 5, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 3, 2, 6, 4, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 3, 2, 9, 4, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 2, 2, 7, 2, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 2, 7, 5, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock($$0, LAMP_BLOCK, 2, 2, 5, $$4);
         this.placeBlock($$0, LAMP_BLOCK, 5, 2, 5, $$4);
         this.placeBlock($$0, LAMP_BLOCK, 2, 2, 10, $$4);
         this.placeBlock($$0, LAMP_BLOCK, 5, 2, 10, $$4);
         this.placeBlock($$0, BASE_LIGHT, 2, 3, 5, $$4);
         this.placeBlock($$0, BASE_LIGHT, 5, 3, 5, $$4);
         this.placeBlock($$0, BASE_LIGHT, 2, 3, 10, $$4);
         this.placeBlock($$0, BASE_LIGHT, 5, 3, 10, $$4);
         if ($$8.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 1, 0, 4, 2, 0);
         }

         if ($$8.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 7, 1, 3, 7, 2, 4);
         }

         if ($$8.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 0, 1, 3, 0, 2, 4);
         }

         if ($$7.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 1, 15, 4, 2, 15);
         }

         if ($$7.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 0, 1, 11, 0, 2, 12);
         }

         if ($$7.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 7, 1, 11, 7, 2, 12);
         }
      }
   }

   public static class OceanMonumentEntryRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentEntryRoom(Direction $$0, OceanMonumentPieces.RoomDefinition $$1) {
         super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, 1, $$0, $$1, 1, 1, 1);
      }

      public OceanMonumentEntryRoom(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, $$0);
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
         this.generateBox($$0, $$4, 0, 3, 0, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 0, 2, 0, 1, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, 2, 0, 7, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 0, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 1, 0, 2, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 5, 1, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 1, 7, 4, 2, 7);
         }

         if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 0, 1, 3, 1, 2, 4);
         }

         if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 6, 1, 3, 7, 2, 4);
         }
      }
   }

   public static class OceanMonumentPenthouse extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentPenthouse(Direction $$0, BoundingBox $$1) {
         super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, $$0, 1, $$1);
      }

      public OceanMonumentPenthouse(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, $$0);
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
         this.generateBox($$0, $$4, 2, -1, 2, 11, -1, 11, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 0, -1, 0, 1, -1, 11, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 12, -1, 0, 13, -1, 11, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 2, -1, 0, 11, -1, 1, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 2, -1, 12, 11, -1, 13, BASE_GRAY, BASE_GRAY, false);
         this.generateBox($$0, $$4, 0, 0, 0, 0, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 13, 0, 0, 13, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 0, 0, 12, 0, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 0, 13, 12, 0, 13, BASE_LIGHT, BASE_LIGHT, false);

         for (int $$7 = 2; $$7 <= 11; $$7 += 3) {
            this.placeBlock($$0, LAMP_BLOCK, 0, 0, $$7, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 13, 0, $$7, $$4);
            this.placeBlock($$0, LAMP_BLOCK, $$7, 0, 0, $$4);
         }

         this.generateBox($$0, $$4, 2, 0, 3, 4, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 9, 0, 3, 11, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 4, 0, 9, 9, 0, 11, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock($$0, BASE_LIGHT, 5, 0, 8, $$4);
         this.placeBlock($$0, BASE_LIGHT, 8, 0, 8, $$4);
         this.placeBlock($$0, BASE_LIGHT, 10, 0, 10, $$4);
         this.placeBlock($$0, BASE_LIGHT, 3, 0, 10, $$4);
         this.generateBox($$0, $$4, 3, 0, 3, 3, 0, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox($$0, $$4, 10, 0, 3, 10, 0, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox($$0, $$4, 6, 0, 10, 7, 0, 10, BASE_BLACK, BASE_BLACK, false);
         int $$8 = 3;

         for (int $$9 = 0; $$9 < 2; $$9++) {
            for (int $$10 = 2; $$10 <= 8; $$10 += 3) {
               this.generateBox($$0, $$4, $$8, 0, $$10, $$8, 2, $$10, BASE_LIGHT, BASE_LIGHT, false);
            }

            $$8 = 10;
         }

         this.generateBox($$0, $$4, 5, 0, 10, 5, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 8, 0, 10, 8, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 6, -1, 7, 7, -1, 8, BASE_BLACK, BASE_BLACK, false);
         this.generateWaterBox($$0, $$4, 6, -1, 3, 7, -1, 4);
         this.spawnElder($$0, $$4, 6, 1, 6);
      }
   }

   protected abstract static class OceanMonumentPiece extends StructurePiece {
      protected static final BlockState BASE_GRAY = Blocks.PRISMARINE.defaultBlockState();
      protected static final BlockState BASE_LIGHT = Blocks.PRISMARINE_BRICKS.defaultBlockState();
      protected static final BlockState BASE_BLACK = Blocks.DARK_PRISMARINE.defaultBlockState();
      protected static final BlockState DOT_DECO_DATA = BASE_LIGHT;
      protected static final BlockState LAMP_BLOCK = Blocks.SEA_LANTERN.defaultBlockState();
      protected static final boolean DO_FILL = true;
      protected static final BlockState FILL_BLOCK = Blocks.WATER.defaultBlockState();
      protected static final Set<Block> FILL_KEEP = ImmutableSet.builder()
         .add(Blocks.ICE)
         .add(Blocks.PACKED_ICE)
         .add(Blocks.BLUE_ICE)
         .add(FILL_BLOCK.getBlock())
         .build();
      protected static final int GRIDROOM_WIDTH = 8;
      protected static final int GRIDROOM_DEPTH = 8;
      protected static final int GRIDROOM_HEIGHT = 4;
      protected static final int GRID_WIDTH = 5;
      protected static final int GRID_DEPTH = 5;
      protected static final int GRID_HEIGHT = 3;
      protected static final int GRID_FLOOR_COUNT = 25;
      protected static final int GRID_SIZE = 75;
      protected static final int GRIDROOM_SOURCE_INDEX = getRoomIndex(2, 0, 0);
      protected static final int GRIDROOM_TOP_CONNECT_INDEX = getRoomIndex(2, 2, 0);
      protected static final int GRIDROOM_LEFTWING_CONNECT_INDEX = getRoomIndex(0, 1, 0);
      protected static final int GRIDROOM_RIGHTWING_CONNECT_INDEX = getRoomIndex(4, 1, 0);
      protected static final int LEFTWING_INDEX = 1001;
      protected static final int RIGHTWING_INDEX = 1002;
      protected static final int PENTHOUSE_INDEX = 1003;
      protected OceanMonumentPieces.RoomDefinition roomDefinition;

      protected static int getRoomIndex(int $$0, int $$1, int $$2) {
         return $$1 * 25 + $$2 * 5 + $$0;
      }

      public OceanMonumentPiece(StructurePieceType $$0, Direction $$1, int $$2, BoundingBox $$3) {
         super($$0, $$2, $$3);
         this.setOrientation($$1);
      }

      protected OceanMonumentPiece(StructurePieceType $$0, int $$1, Direction $$2, OceanMonumentPieces.RoomDefinition $$3, int $$4, int $$5, int $$6) {
         super($$0, $$1, makeBoundingBox($$2, $$3, $$4, $$5, $$6));
         this.setOrientation($$2);
         this.roomDefinition = $$3;
      }

      private static BoundingBox makeBoundingBox(Direction $$0, OceanMonumentPieces.RoomDefinition $$1, int $$2, int $$3, int $$4) {
         int $$5 = $$1.index;
         int $$6 = $$5 % 5;
         int $$7 = $$5 / 5 % 5;
         int $$8 = $$5 / 25;
         BoundingBox $$9 = makeBoundingBox(0, 0, 0, $$0, $$2 * 8, $$3 * 4, $$4 * 8);
         switch ($$0) {
            case NORTH:
               $$9.move($$6 * 8, $$8 * 4, -($$7 + $$4) * 8 + 1);
               break;
            case SOUTH:
               $$9.move($$6 * 8, $$8 * 4, $$7 * 8);
               break;
            case WEST:
               $$9.move(-($$7 + $$4) * 8 + 1, $$8 * 4, $$6 * 8);
               break;
            case EAST:
            default:
               $$9.move($$7 * 8, $$8 * 4, $$6 * 8);
         }

         return $$9;
      }

      public OceanMonumentPiece(StructurePieceType $$0, CompoundTag $$1) {
         super($$0, $$1);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
      }

      protected void generateWaterBox(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, int $$2, int $$3, int $$4, int $$5, int $$6, int $$7) {
         for (int $$8 = $$3; $$8 <= $$6; $$8++) {
            for (int $$9 = $$2; $$9 <= $$5; $$9++) {
               for (int $$10 = $$4; $$10 <= $$7; $$10++) {
                  BlockState $$11 = this.getBlock($$0, $$9, $$8, $$10, $$1);
                  if (!FILL_KEEP.contains($$11.getBlock())) {
                     if (this.getWorldY($$8) >= $$0.getSeaLevel() && $$11 != FILL_BLOCK) {
                        this.placeBlock($$0, Blocks.AIR.defaultBlockState(), $$9, $$8, $$10, $$1);
                     } else {
                        this.placeBlock($$0, FILL_BLOCK, $$9, $$8, $$10, $$1);
                     }
                  }
               }
            }
         }
      }

      protected void generateDefaultFloor(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, int $$2, int $$3, boolean $$4) {
         if ($$4) {
            this.generateBox($$0, $$1, $$2 + 0, 0, $$3 + 0, $$2 + 2, 0, $$3 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$1, $$2 + 5, 0, $$3 + 0, $$2 + 8 - 1, 0, $$3 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$1, $$2 + 3, 0, $$3 + 0, $$2 + 4, 0, $$3 + 2, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$1, $$2 + 3, 0, $$3 + 5, $$2 + 4, 0, $$3 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$1, $$2 + 3, 0, $$3 + 2, $$2 + 4, 0, $$3 + 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$1, $$2 + 3, 0, $$3 + 5, $$2 + 4, 0, $$3 + 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$1, $$2 + 2, 0, $$3 + 3, $$2 + 2, 0, $$3 + 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$1, $$2 + 5, 0, $$3 + 3, $$2 + 5, 0, $$3 + 4, BASE_LIGHT, BASE_LIGHT, false);
         } else {
            this.generateBox($$0, $$1, $$2 + 0, 0, $$3 + 0, $$2 + 8 - 1, 0, $$3 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
         }
      }

      protected void generateBoxOnFillOnly(
         net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, int $$2, int $$3, int $$4, int $$5, int $$6, int $$7, BlockState $$8
      ) {
         for (int $$9 = $$3; $$9 <= $$6; $$9++) {
            for (int $$10 = $$2; $$10 <= $$5; $$10++) {
               for (int $$11 = $$4; $$11 <= $$7; $$11++) {
                  if (this.getBlock($$0, $$10, $$9, $$11, $$1) == FILL_BLOCK) {
                     this.placeBlock($$0, $$8, $$10, $$9, $$11, $$1);
                  }
               }
            }
         }
      }

      protected boolean chunkIntersects(BoundingBox $$0, int $$1, int $$2, int $$3, int $$4) {
         int $$5 = this.getWorldX($$1, $$2);
         int $$6 = this.getWorldZ($$1, $$2);
         int $$7 = this.getWorldX($$3, $$4);
         int $$8 = this.getWorldZ($$3, $$4);
         return $$0.intersects(Math.min($$5, $$7), Math.min($$6, $$8), Math.max($$5, $$7), Math.max($$6, $$8));
      }

      protected void spawnElder(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, int $$2, int $$3, int $$4) {
         BlockPos $$5 = this.getWorldPos($$2, $$3, $$4);
         if ($$1.isInside($$5)) {
            ElderGuardian $$6 = (ElderGuardian)EntityType.ELDER_GUARDIAN.create($$0.getLevel(), EntitySpawnReason.STRUCTURE);
            if ($$6 != null) {
               $$6.heal($$6.getMaxHealth());
               $$6.snapTo($$5.getX() + 0.5, $$5.getY(), $$5.getZ() + 0.5, 0.0F, 0.0F);
               $$6.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$6.blockPosition()), EntitySpawnReason.STRUCTURE, null);
               $$0.addFreshEntityWithPassengers($$6);
            }
         }
      }
   }

   public static class OceanMonumentSimpleRoom extends OceanMonumentPieces.OceanMonumentPiece {
      private int mainDesign;

      public OceanMonumentSimpleRoom(Direction $$0, OceanMonumentPieces.RoomDefinition $$1, RandomSource $$2) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, 1, $$0, $$1, 1, 1, 1);
         this.mainDesign = $$2.nextInt(3);
      }

      public OceanMonumentSimpleRoom(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, $$0);
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
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor($$0, $$4, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 1, 4, 1, 6, 4, 6, BASE_GRAY);
         }

         boolean $$7 = this.mainDesign != 0
            && $$3.nextBoolean()
            && !this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]
            && !this.roomDefinition.hasOpening[Direction.UP.get3DDataValue()]
            && this.roomDefinition.countOpenings() > 1;
         if (this.mainDesign == 0) {
            this.generateBox($$0, $$4, 0, 1, 0, 2, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 0, 3, 0, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 0, 2, 0, 0, 2, 2, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$4, 1, 2, 0, 2, 2, 0, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock($$0, LAMP_BLOCK, 1, 2, 1, $$4);
            this.generateBox($$0, $$4, 5, 1, 0, 7, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 5, 3, 0, 7, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 7, 2, 0, 7, 2, 2, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$4, 5, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock($$0, LAMP_BLOCK, 6, 2, 1, $$4);
            this.generateBox($$0, $$4, 0, 1, 5, 2, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 0, 3, 5, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 0, 2, 5, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$4, 1, 2, 7, 2, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock($$0, LAMP_BLOCK, 1, 2, 6, $$4);
            this.generateBox($$0, $$4, 5, 1, 5, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 5, 3, 5, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 7, 2, 5, 7, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$4, 5, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock($$0, LAMP_BLOCK, 6, 2, 6, $$4);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateBox($$0, $$4, 3, 3, 0, 4, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox($$0, $$4, 3, 3, 0, 4, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 3, 2, 0, 4, 2, 0, BASE_GRAY, BASE_GRAY, false);
               this.generateBox($$0, $$4, 3, 1, 0, 4, 1, 1, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateBox($$0, $$4, 3, 3, 7, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox($$0, $$4, 3, 3, 6, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 3, 2, 7, 4, 2, 7, BASE_GRAY, BASE_GRAY, false);
               this.generateBox($$0, $$4, 3, 1, 6, 4, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateBox($$0, $$4, 0, 3, 3, 0, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox($$0, $$4, 0, 3, 3, 1, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 0, 2, 3, 0, 2, 4, BASE_GRAY, BASE_GRAY, false);
               this.generateBox($$0, $$4, 0, 1, 3, 1, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateBox($$0, $$4, 7, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox($$0, $$4, 6, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 7, 2, 3, 7, 2, 4, BASE_GRAY, BASE_GRAY, false);
               this.generateBox($$0, $$4, 6, 1, 3, 7, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            }
         } else if (this.mainDesign == 1) {
            this.generateBox($$0, $$4, 2, 1, 2, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 2, 1, 5, 2, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 5, 1, 5, 5, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 5, 1, 2, 5, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock($$0, LAMP_BLOCK, 2, 2, 2, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 2, 2, 5, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 5, 2, 5, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 5, 2, 2, $$4);
            this.generateBox($$0, $$4, 0, 1, 0, 1, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 0, 1, 1, 0, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 0, 1, 7, 1, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 0, 1, 6, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 6, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 7, 1, 6, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 6, 1, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 7, 1, 1, 7, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock($$0, BASE_GRAY, 1, 2, 0, $$4);
            this.placeBlock($$0, BASE_GRAY, 0, 2, 1, $$4);
            this.placeBlock($$0, BASE_GRAY, 1, 2, 7, $$4);
            this.placeBlock($$0, BASE_GRAY, 0, 2, 6, $$4);
            this.placeBlock($$0, BASE_GRAY, 6, 2, 7, $$4);
            this.placeBlock($$0, BASE_GRAY, 7, 2, 6, $$4);
            this.placeBlock($$0, BASE_GRAY, 6, 2, 0, $$4);
            this.placeBlock($$0, BASE_GRAY, 7, 2, 1, $$4);
            if (!this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateBox($$0, $$4, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 1, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
               this.generateBox($$0, $$4, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateBox($$0, $$4, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 1, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
               this.generateBox($$0, $$4, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateBox($$0, $$4, 0, 3, 1, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 0, 2, 1, 0, 2, 6, BASE_GRAY, BASE_GRAY, false);
               this.generateBox($$0, $$4, 0, 1, 1, 0, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateBox($$0, $$4, 7, 3, 1, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, 7, 2, 1, 7, 2, 6, BASE_GRAY, BASE_GRAY, false);
               this.generateBox($$0, $$4, 7, 1, 1, 7, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
            }
         } else if (this.mainDesign == 2) {
            this.generateBox($$0, $$4, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox($$0, $$4, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox($$0, $$4, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
            this.generateBox($$0, $$4, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox($$0, $$4, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
            this.generateBox($$0, $$4, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
            this.generateBox($$0, $$4, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
            this.generateBox($$0, $$4, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateWaterBox($$0, $$4, 3, 1, 0, 4, 2, 0);
            }

            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateWaterBox($$0, $$4, 3, 1, 7, 4, 2, 7);
            }

            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateWaterBox($$0, $$4, 0, 1, 3, 0, 2, 4);
            }

            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateWaterBox($$0, $$4, 7, 1, 3, 7, 2, 4);
            }
         }

         if ($$7) {
            this.generateBox($$0, $$4, 3, 1, 3, 4, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 3, 2, 3, 4, 2, 4, BASE_GRAY, BASE_GRAY, false);
            this.generateBox($$0, $$4, 3, 3, 3, 4, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         }
      }
   }

   public static class OceanMonumentSimpleTopRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentSimpleTopRoom(Direction $$0, OceanMonumentPieces.RoomDefinition $$1) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, 1, $$0, $$1, 1, 1, 1);
      }

      public OceanMonumentSimpleTopRoom(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, $$0);
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
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor($$0, $$4, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly($$0, $$4, 1, 4, 1, 6, 4, 6, BASE_GRAY);
         }

         for (int $$7 = 1; $$7 <= 6; $$7++) {
            for (int $$8 = 1; $$8 <= 6; $$8++) {
               if ($$3.nextInt(3) != 0) {
                  int $$9 = 2 + ($$3.nextInt(4) == 0 ? 0 : 1);
                  BlockState $$10 = Blocks.WET_SPONGE.defaultBlockState();
                  this.generateBox($$0, $$4, $$7, $$9, $$8, $$7, 3, $$8, $$10, $$10, false);
               }
            }
         }

         this.generateBox($$0, $$4, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox($$0, $$4, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox($$0, $$4, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
         this.generateBox($$0, $$4, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox($$0, $$4, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox($$0, $$4, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
         this.generateBox($$0, $$4, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
         this.generateBox($$0, $$4, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
         this.generateBox($$0, $$4, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
         if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox($$0, $$4, 3, 1, 0, 4, 2, 0);
         }
      }
   }

   public static class OceanMonumentWingRoom extends OceanMonumentPieces.OceanMonumentPiece {
      private int mainDesign;

      public OceanMonumentWingRoom(Direction $$0, BoundingBox $$1, int $$2) {
         super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, $$0, 1, $$1);
         this.mainDesign = $$2 & 1;
      }

      public OceanMonumentWingRoom(CompoundTag $$0) {
         super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, $$0);
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
         if (this.mainDesign == 0) {
            for (int $$7 = 0; $$7 < 4; $$7++) {
               this.generateBox($$0, $$4, 10 - $$7, 3 - $$7, 20 - $$7, 12 + $$7, 3 - $$7, 20, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox($$0, $$4, 7, 0, 6, 15, 0, 16, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 6, 0, 6, 6, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 16, 0, 6, 16, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 7, 1, 7, 7, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 15, 1, 7, 15, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 7, 1, 6, 9, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 13, 1, 6, 15, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 8, 1, 7, 9, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 13, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 9, 0, 5, 13, 0, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 10, 0, 7, 12, 0, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox($$0, $$4, 8, 0, 10, 8, 0, 12, BASE_BLACK, BASE_BLACK, false);
            this.generateBox($$0, $$4, 14, 0, 10, 14, 0, 12, BASE_BLACK, BASE_BLACK, false);

            for (int $$8 = 18; $$8 >= 7; $$8 -= 3) {
               this.placeBlock($$0, LAMP_BLOCK, 6, 3, $$8, $$4);
               this.placeBlock($$0, LAMP_BLOCK, 16, 3, $$8, $$4);
            }

            this.placeBlock($$0, LAMP_BLOCK, 10, 0, 10, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 12, 0, 10, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 10, 0, 12, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 12, 0, 12, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 8, 3, 6, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 14, 3, 6, $$4);
            this.placeBlock($$0, BASE_LIGHT, 4, 2, 4, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 4, 1, 4, $$4);
            this.placeBlock($$0, BASE_LIGHT, 4, 0, 4, $$4);
            this.placeBlock($$0, BASE_LIGHT, 18, 2, 4, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 18, 1, 4, $$4);
            this.placeBlock($$0, BASE_LIGHT, 18, 0, 4, $$4);
            this.placeBlock($$0, BASE_LIGHT, 4, 2, 18, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 4, 1, 18, $$4);
            this.placeBlock($$0, BASE_LIGHT, 4, 0, 18, $$4);
            this.placeBlock($$0, BASE_LIGHT, 18, 2, 18, $$4);
            this.placeBlock($$0, LAMP_BLOCK, 18, 1, 18, $$4);
            this.placeBlock($$0, BASE_LIGHT, 18, 0, 18, $$4);
            this.placeBlock($$0, BASE_LIGHT, 9, 7, 20, $$4);
            this.placeBlock($$0, BASE_LIGHT, 13, 7, 20, $$4);
            this.generateBox($$0, $$4, 6, 0, 21, 7, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 15, 0, 21, 16, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
            this.spawnElder($$0, $$4, 11, 2, 16);
         } else if (this.mainDesign == 1) {
            this.generateBox($$0, $$4, 9, 3, 18, 13, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 9, 0, 18, 9, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox($$0, $$4, 13, 0, 18, 13, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
            int $$9 = 9;
            int $$10 = 20;
            int $$11 = 5;

            for (int $$12 = 0; $$12 < 2; $$12++) {
               this.placeBlock($$0, BASE_LIGHT, $$9, 6, 20, $$4);
               this.placeBlock($$0, LAMP_BLOCK, $$9, 5, 20, $$4);
               this.placeBlock($$0, BASE_LIGHT, $$9, 4, 20, $$4);
               $$9 = 13;
            }

            this.generateBox($$0, $$4, 7, 3, 7, 15, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
            int var14 = 10;

            for (int $$13 = 0; $$13 < 2; $$13++) {
               this.generateBox($$0, $$4, var14, 0, 10, var14, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, var14, 0, 12, var14, 6, 12, BASE_LIGHT, BASE_LIGHT, false);
               this.placeBlock($$0, LAMP_BLOCK, var14, 0, 10, $$4);
               this.placeBlock($$0, LAMP_BLOCK, var14, 0, 12, $$4);
               this.placeBlock($$0, LAMP_BLOCK, var14, 4, 10, $$4);
               this.placeBlock($$0, LAMP_BLOCK, var14, 4, 12, $$4);
               var14 = 12;
            }

            var14 = 8;

            for (int $$14 = 0; $$14 < 2; $$14++) {
               this.generateBox($$0, $$4, var14, 0, 7, var14, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox($$0, $$4, var14, 0, 14, var14, 2, 14, BASE_LIGHT, BASE_LIGHT, false);
               var14 = 14;
            }

            this.generateBox($$0, $$4, 8, 3, 8, 8, 3, 13, BASE_BLACK, BASE_BLACK, false);
            this.generateBox($$0, $$4, 14, 3, 8, 14, 3, 13, BASE_BLACK, BASE_BLACK, false);
            this.spawnElder($$0, $$4, 11, 5, 13);
         }
      }
   }

   static class RoomDefinition {
      final int index;
      final OceanMonumentPieces.RoomDefinition[] connections = new OceanMonumentPieces.RoomDefinition[6];
      final boolean[] hasOpening = new boolean[6];
      boolean claimed;
      boolean isSource;
      private int scanIndex;

      public RoomDefinition(int $$0) {
         this.index = $$0;
      }

      public void setConnection(Direction $$0, OceanMonumentPieces.RoomDefinition $$1) {
         this.connections[$$0.get3DDataValue()] = $$1;
         $$1.connections[$$0.getOpposite().get3DDataValue()] = this;
      }

      public void updateOpenings() {
         for (int $$0 = 0; $$0 < 6; $$0++) {
            this.hasOpening[$$0] = this.connections[$$0] != null;
         }
      }

      public boolean findSource(int $$0) {
         if (this.isSource) {
            return true;
         } else {
            this.scanIndex = $$0;

            for (int $$1 = 0; $$1 < 6; $$1++) {
               if (this.connections[$$1] != null && this.hasOpening[$$1] && this.connections[$$1].scanIndex != $$0 && this.connections[$$1].findSource($$0)) {
                  return true;
               }
            }

            return false;
         }
      }

      public boolean isSpecial() {
         return this.index >= 75;
      }

      public int countOpenings() {
         int $$0 = 0;

         for (int $$1 = 0; $$1 < 6; $$1++) {
            if (this.hasOpening[$$1]) {
               $$0++;
            }
         }

         return $$0;
      }
   }
}
