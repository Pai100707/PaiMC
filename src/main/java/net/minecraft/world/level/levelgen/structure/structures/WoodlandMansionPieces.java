package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class WoodlandMansionPieces {
   public static void generateMansion(
      StructureTemplateManager $$0, BlockPos $$1, Rotation $$2, List<WoodlandMansionPieces.WoodlandMansionPiece> $$3, RandomSource $$4
   ) {
      WoodlandMansionPieces.MansionGrid $$5 = new WoodlandMansionPieces.MansionGrid($$4);
      WoodlandMansionPieces.MansionPiecePlacer $$6 = new WoodlandMansionPieces.MansionPiecePlacer($$0, $$4);
      $$6.createMansion($$1, $$2, $$3, $$5);
   }

   static class FirstFloorRoomCollection extends WoodlandMansionPieces.FloorRoomCollection {
      @Override
      public String get1x1(RandomSource $$0) {
         return "1x1_a" + ($$0.nextInt(5) + 1);
      }

      @Override
      public String get1x1Secret(RandomSource $$0) {
         return "1x1_as" + ($$0.nextInt(4) + 1);
      }

      @Override
      public String get1x2SideEntrance(RandomSource $$0, boolean $$1) {
         return "1x2_a" + ($$0.nextInt(9) + 1);
      }

      @Override
      public String get1x2FrontEntrance(RandomSource $$0, boolean $$1) {
         return "1x2_b" + ($$0.nextInt(5) + 1);
      }

      @Override
      public String get1x2Secret(RandomSource $$0) {
         return "1x2_s" + ($$0.nextInt(2) + 1);
      }

      @Override
      public String get2x2(RandomSource $$0) {
         return "2x2_a" + ($$0.nextInt(4) + 1);
      }

      @Override
      public String get2x2Secret(RandomSource $$0) {
         return "2x2_s1";
      }
   }

   abstract static class FloorRoomCollection {
      public abstract String get1x1(RandomSource var1);

      public abstract String get1x1Secret(RandomSource var1);

      public abstract String get1x2SideEntrance(RandomSource var1, boolean var2);

      public abstract String get1x2FrontEntrance(RandomSource var1, boolean var2);

      public abstract String get1x2Secret(RandomSource var1);

      public abstract String get2x2(RandomSource var1);

      public abstract String get2x2Secret(RandomSource var1);
   }

   static class MansionGrid {
      private static final int DEFAULT_SIZE = 11;
      private static final int CLEAR = 0;
      private static final int CORRIDOR = 1;
      private static final int ROOM = 2;
      private static final int START_ROOM = 3;
      private static final int TEST_ROOM = 4;
      private static final int BLOCKED = 5;
      private static final int ROOM_1x1 = 65536;
      private static final int ROOM_1x2 = 131072;
      private static final int ROOM_2x2 = 262144;
      private static final int ROOM_ORIGIN_FLAG = 1048576;
      private static final int ROOM_DOOR_FLAG = 2097152;
      private static final int ROOM_STAIRS_FLAG = 4194304;
      private static final int ROOM_CORRIDOR_FLAG = 8388608;
      private static final int ROOM_TYPE_MASK = 983040;
      private static final int ROOM_ID_MASK = 65535;
      private final RandomSource random;
      final WoodlandMansionPieces.SimpleGrid baseGrid;
      final WoodlandMansionPieces.SimpleGrid thirdFloorGrid;
      final WoodlandMansionPieces.SimpleGrid[] floorRooms;
      final int entranceX;
      final int entranceY;

      public MansionGrid(RandomSource $$0) {
         this.random = $$0;
         int $$1 = 11;
         this.entranceX = 7;
         this.entranceY = 4;
         this.baseGrid = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
         this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
         this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
         this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
         this.baseGrid.set(0, 0, 11, 1, 5);
         this.baseGrid.set(0, 9, 11, 11, 5);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, Direction.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, Direction.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, Direction.WEST, 3);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, Direction.WEST, 3);

         while (this.cleanEdges(this.baseGrid)) {
         }

         this.floorRooms = new WoodlandMansionPieces.SimpleGrid[3];
         this.floorRooms[0] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.floorRooms[1] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.floorRooms[2] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.identifyRooms(this.baseGrid, this.floorRooms[0]);
         this.identifyRooms(this.baseGrid, this.floorRooms[1]);
         this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.thirdFloorGrid = new WoodlandMansionPieces.SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
         this.setupThirdFloor();
         this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
      }

      public static boolean isHouse(WoodlandMansionPieces.SimpleGrid $$0, int $$1, int $$2) {
         int $$3 = $$0.get($$1, $$2);
         return $$3 == 1 || $$3 == 2 || $$3 == 3 || $$3 == 4;
      }

      public boolean isRoomId(WoodlandMansionPieces.SimpleGrid $$0, int $$1, int $$2, int $$3, int $$4) {
         return (this.floorRooms[$$3].get($$1, $$2) & 65535) == $$4;
      }

      
      public Direction get1x2RoomDirection(WoodlandMansionPieces.SimpleGrid $$0, int $$1, int $$2, int $$3, int $$4) {
         for (Direction $$5 : Plane.HORIZONTAL) {
            if (this.isRoomId($$0, $$1 + $$5.getStepX(), $$2 + $$5.getStepZ(), $$3, $$4)) {
               return $$5;
            }
         }

         return null;
      }

      private void recursiveCorridor(WoodlandMansionPieces.SimpleGrid $$0, int $$1, int $$2, Direction $$3, int $$4) {
         if ($$4 > 0) {
            $$0.set($$1, $$2, 1);
            $$0.setif($$1 + $$3.getStepX(), $$2 + $$3.getStepZ(), 0, 1);

            for (int $$5 = 0; $$5 < 8; $$5++) {
               Direction $$6 = Direction.from2DDataValue(this.random.nextInt(4));
               if ($$6 != $$3.getOpposite() && ($$6 != Direction.EAST || !this.random.nextBoolean())) {
                  int $$7 = $$1 + $$3.getStepX();
                  int $$8 = $$2 + $$3.getStepZ();
                  if ($$0.get($$7 + $$6.getStepX(), $$8 + $$6.getStepZ()) == 0 && $$0.get($$7 + $$6.getStepX() * 2, $$8 + $$6.getStepZ() * 2) == 0) {
                     this.recursiveCorridor($$0, $$1 + $$3.getStepX() + $$6.getStepX(), $$2 + $$3.getStepZ() + $$6.getStepZ(), $$6, $$4 - 1);
                     break;
                  }
               }
            }

            Direction $$9 = $$3.getClockWise();
            Direction $$10 = $$3.getCounterClockWise();
            $$0.setif($$1 + $$9.getStepX(), $$2 + $$9.getStepZ(), 0, 2);
            $$0.setif($$1 + $$10.getStepX(), $$2 + $$10.getStepZ(), 0, 2);
            $$0.setif($$1 + $$3.getStepX() + $$9.getStepX(), $$2 + $$3.getStepZ() + $$9.getStepZ(), 0, 2);
            $$0.setif($$1 + $$3.getStepX() + $$10.getStepX(), $$2 + $$3.getStepZ() + $$10.getStepZ(), 0, 2);
            $$0.setif($$1 + $$3.getStepX() * 2, $$2 + $$3.getStepZ() * 2, 0, 2);
            $$0.setif($$1 + $$9.getStepX() * 2, $$2 + $$9.getStepZ() * 2, 0, 2);
            $$0.setif($$1 + $$10.getStepX() * 2, $$2 + $$10.getStepZ() * 2, 0, 2);
         }
      }

      private boolean cleanEdges(WoodlandMansionPieces.SimpleGrid $$0) {
         boolean $$1 = false;

         for (int $$2 = 0; $$2 < $$0.height; $$2++) {
            for (int $$3 = 0; $$3 < $$0.width; $$3++) {
               if ($$0.get($$3, $$2) == 0) {
                  int $$4 = 0;
                  $$4 += isHouse($$0, $$3 + 1, $$2) ? 1 : 0;
                  $$4 += isHouse($$0, $$3 - 1, $$2) ? 1 : 0;
                  $$4 += isHouse($$0, $$3, $$2 + 1) ? 1 : 0;
                  $$4 += isHouse($$0, $$3, $$2 - 1) ? 1 : 0;
                  if ($$4 >= 3) {
                     $$0.set($$3, $$2, 2);
                     $$1 = true;
                  } else if ($$4 == 2) {
                     int $$5 = 0;
                     $$5 += isHouse($$0, $$3 + 1, $$2 + 1) ? 1 : 0;
                     $$5 += isHouse($$0, $$3 - 1, $$2 + 1) ? 1 : 0;
                     $$5 += isHouse($$0, $$3 + 1, $$2 - 1) ? 1 : 0;
                     $$5 += isHouse($$0, $$3 - 1, $$2 - 1) ? 1 : 0;
                     if ($$5 <= 1) {
                        $$0.set($$3, $$2, 2);
                        $$1 = true;
                     }
                  }
               }
            }
         }

         return $$1;
      }

      private void setupThirdFloor() {
         List<Tuple<Integer, Integer>> $$0 = Lists.newArrayList();
         WoodlandMansionPieces.SimpleGrid $$1 = this.floorRooms[1];

         for (int $$2 = 0; $$2 < this.thirdFloorGrid.height; $$2++) {
            for (int $$3 = 0; $$3 < this.thirdFloorGrid.width; $$3++) {
               int $$4 = $$1.get($$3, $$2);
               int $$5 = $$4 & 983040;
               if ($$5 == 131072 && ($$4 & 2097152) == 2097152) {
                  $$0.add(new Tuple($$3, $$2));
               }
            }
         }

         if ($$0.isEmpty()) {
            this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
         } else {
            Tuple<Integer, Integer> $$6 = $$0.get(this.random.nextInt($$0.size()));
            int $$7 = $$1.get((Integer)$$6.getA(), (Integer)$$6.getB());
            $$1.set((Integer)$$6.getA(), (Integer)$$6.getB(), $$7 | 4194304);
            Direction $$8 = this.get1x2RoomDirection(this.baseGrid, (Integer)$$6.getA(), (Integer)$$6.getB(), 1, $$7 & 65535);
            int $$9 = (Integer)$$6.getA() + $$8.getStepX();
            int $$10 = (Integer)$$6.getB() + $$8.getStepZ();

            for (int $$11 = 0; $$11 < this.thirdFloorGrid.height; $$11++) {
               for (int $$12 = 0; $$12 < this.thirdFloorGrid.width; $$12++) {
                  if (!isHouse(this.baseGrid, $$12, $$11)) {
                     this.thirdFloorGrid.set($$12, $$11, 5);
                  } else if ($$12 == (Integer)$$6.getA() && $$11 == (Integer)$$6.getB()) {
                     this.thirdFloorGrid.set($$12, $$11, 3);
                  } else if ($$12 == $$9 && $$11 == $$10) {
                     this.thirdFloorGrid.set($$12, $$11, 3);
                     this.floorRooms[2].set($$12, $$11, 8388608);
                  }
               }
            }

            List<Direction> $$13 = Lists.newArrayList();

            for (Direction $$14 : Plane.HORIZONTAL) {
               if (this.thirdFloorGrid.get($$9 + $$14.getStepX(), $$10 + $$14.getStepZ()) == 0) {
                  $$13.add($$14);
               }
            }

            if ($$13.isEmpty()) {
               this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
               $$1.set((Integer)$$6.getA(), (Integer)$$6.getB(), $$7);
            } else {
               Direction $$15 = $$13.get(this.random.nextInt($$13.size()));
               this.recursiveCorridor(this.thirdFloorGrid, $$9 + $$15.getStepX(), $$10 + $$15.getStepZ(), $$15, 4);

               while (this.cleanEdges(this.thirdFloorGrid)) {
               }
            }
         }
      }

      private void identifyRooms(WoodlandMansionPieces.SimpleGrid $$0, WoodlandMansionPieces.SimpleGrid $$1) {
         ObjectArrayList<Tuple<Integer, Integer>> $$2 = new ObjectArrayList();

         for (int $$3 = 0; $$3 < $$0.height; $$3++) {
            for (int $$4 = 0; $$4 < $$0.width; $$4++) {
               if ($$0.get($$4, $$3) == 2) {
                  $$2.add(new Tuple($$4, $$3));
               }
            }
         }

         Util.shuffle($$2, this.random);
         int $$5 = 10;
         ObjectListIterator var20 = $$2.iterator();

         while (var20.hasNext()) {
            Tuple<Integer, Integer> $$6 = (Tuple<Integer, Integer>)var20.next();
            int $$7 = (Integer)$$6.getA();
            int $$8 = (Integer)$$6.getB();
            if ($$1.get($$7, $$8) == 0) {
               int $$9 = $$7;
               int $$10 = $$7;
               int $$11 = $$8;
               int $$12 = $$8;
               int $$13 = 65536;
               if ($$1.get($$7 + 1, $$8) == 0
                  && $$1.get($$7, $$8 + 1) == 0
                  && $$1.get($$7 + 1, $$8 + 1) == 0
                  && $$0.get($$7 + 1, $$8) == 2
                  && $$0.get($$7, $$8 + 1) == 2
                  && $$0.get($$7 + 1, $$8 + 1) == 2) {
                  $$10 = $$7 + 1;
                  $$12 = $$8 + 1;
                  $$13 = 262144;
               } else if ($$1.get($$7 - 1, $$8) == 0
                  && $$1.get($$7, $$8 + 1) == 0
                  && $$1.get($$7 - 1, $$8 + 1) == 0
                  && $$0.get($$7 - 1, $$8) == 2
                  && $$0.get($$7, $$8 + 1) == 2
                  && $$0.get($$7 - 1, $$8 + 1) == 2) {
                  $$9 = $$7 - 1;
                  $$12 = $$8 + 1;
                  $$13 = 262144;
               } else if ($$1.get($$7 - 1, $$8) == 0
                  && $$1.get($$7, $$8 - 1) == 0
                  && $$1.get($$7 - 1, $$8 - 1) == 0
                  && $$0.get($$7 - 1, $$8) == 2
                  && $$0.get($$7, $$8 - 1) == 2
                  && $$0.get($$7 - 1, $$8 - 1) == 2) {
                  $$9 = $$7 - 1;
                  $$11 = $$8 - 1;
                  $$13 = 262144;
               } else if ($$1.get($$7 + 1, $$8) == 0 && $$0.get($$7 + 1, $$8) == 2) {
                  $$10 = $$7 + 1;
                  $$13 = 131072;
               } else if ($$1.get($$7, $$8 + 1) == 0 && $$0.get($$7, $$8 + 1) == 2) {
                  $$12 = $$8 + 1;
                  $$13 = 131072;
               } else if ($$1.get($$7 - 1, $$8) == 0 && $$0.get($$7 - 1, $$8) == 2) {
                  $$9 = $$7 - 1;
                  $$13 = 131072;
               } else if ($$1.get($$7, $$8 - 1) == 0 && $$0.get($$7, $$8 - 1) == 2) {
                  $$11 = $$8 - 1;
                  $$13 = 131072;
               }

               int $$14 = this.random.nextBoolean() ? $$9 : $$10;
               int $$15 = this.random.nextBoolean() ? $$11 : $$12;
               int $$16 = 2097152;
               if (!$$0.edgesTo($$14, $$15, 1)) {
                  $$14 = $$14 == $$9 ? $$10 : $$9;
                  $$15 = $$15 == $$11 ? $$12 : $$11;
                  if (!$$0.edgesTo($$14, $$15, 1)) {
                     $$15 = $$15 == $$11 ? $$12 : $$11;
                     if (!$$0.edgesTo($$14, $$15, 1)) {
                        $$14 = $$14 == $$9 ? $$10 : $$9;
                        $$15 = $$15 == $$11 ? $$12 : $$11;
                        if (!$$0.edgesTo($$14, $$15, 1)) {
                           $$16 = 0;
                           $$14 = $$9;
                           $$15 = $$11;
                        }
                     }
                  }
               }

               for (int $$17 = $$11; $$17 <= $$12; $$17++) {
                  for (int $$18 = $$9; $$18 <= $$10; $$18++) {
                     if ($$18 == $$14 && $$17 == $$15) {
                        $$1.set($$18, $$17, 1048576 | $$16 | $$13 | $$5);
                     } else {
                        $$1.set($$18, $$17, $$13 | $$5);
                     }
                  }
               }

               $$5++;
            }
         }
      }
   }

   static class MansionPiecePlacer {
      private final StructureTemplateManager structureTemplateManager;
      private final RandomSource random;
      private int startX;
      private int startY;

      public MansionPiecePlacer(StructureTemplateManager $$0, RandomSource $$1) {
         this.structureTemplateManager = $$0;
         this.random = $$1;
      }

      public void createMansion(BlockPos $$0, Rotation $$1, List<WoodlandMansionPieces.WoodlandMansionPiece> $$2, WoodlandMansionPieces.MansionGrid $$3) {
         WoodlandMansionPieces.PlacementData $$4 = new WoodlandMansionPieces.PlacementData();
         $$4.position = $$0;
         $$4.rotation = $$1;
         $$4.wallType = "wall_flat";
         WoodlandMansionPieces.PlacementData $$5 = new WoodlandMansionPieces.PlacementData();
         this.entrance($$2, $$4);
         $$5.position = $$4.position.above(8);
         $$5.rotation = $$4.rotation;
         $$5.wallType = "wall_window";
         if (!$$2.isEmpty()) {
         }

         WoodlandMansionPieces.SimpleGrid $$6 = $$3.baseGrid;
         WoodlandMansionPieces.SimpleGrid $$7 = $$3.thirdFloorGrid;
         this.startX = $$3.entranceX + 1;
         this.startY = $$3.entranceY + 1;
         int $$8 = $$3.entranceX + 1;
         int $$9 = $$3.entranceY;
         this.traverseOuterWalls($$2, $$4, $$6, Direction.SOUTH, this.startX, this.startY, $$8, $$9);
         this.traverseOuterWalls($$2, $$5, $$6, Direction.SOUTH, this.startX, this.startY, $$8, $$9);
         WoodlandMansionPieces.PlacementData $$10 = new WoodlandMansionPieces.PlacementData();
         $$10.position = $$4.position.above(19);
         $$10.rotation = $$4.rotation;
         $$10.wallType = "wall_window";
         boolean $$11 = false;

         for (int $$12 = 0; $$12 < $$7.height && !$$11; $$12++) {
            for (int $$13 = $$7.width - 1; $$13 >= 0 && !$$11; $$13--) {
               if (WoodlandMansionPieces.MansionGrid.isHouse($$7, $$13, $$12)) {
                  $$10.position = $$10.position.relative($$1.rotate(Direction.SOUTH), 8 + ($$12 - this.startY) * 8);
                  $$10.position = $$10.position.relative($$1.rotate(Direction.EAST), ($$13 - this.startX) * 8);
                  this.traverseWallPiece($$2, $$10);
                  this.traverseOuterWalls($$2, $$10, $$7, Direction.SOUTH, $$13, $$12, $$13, $$12);
                  $$11 = true;
               }
            }
         }

         this.createRoof($$2, $$0.above(16), $$1, $$6, $$7);
         this.createRoof($$2, $$0.above(27), $$1, $$7, null);
         if (!$$2.isEmpty()) {
         }

         WoodlandMansionPieces.FloorRoomCollection[] $$14 = new WoodlandMansionPieces.FloorRoomCollection[]{
            new WoodlandMansionPieces.FirstFloorRoomCollection(),
            new WoodlandMansionPieces.SecondFloorRoomCollection(),
            new WoodlandMansionPieces.ThirdFloorRoomCollection()
         };

         for (int $$15 = 0; $$15 < 3; $$15++) {
            BlockPos $$16 = $$0.above(8 * $$15 + ($$15 == 2 ? 3 : 0));
            WoodlandMansionPieces.SimpleGrid $$17 = $$3.floorRooms[$$15];
            WoodlandMansionPieces.SimpleGrid $$18 = $$15 == 2 ? $$7 : $$6;
            String $$19 = $$15 == 0 ? "carpet_south_1" : "carpet_south_2";
            String $$20 = $$15 == 0 ? "carpet_west_1" : "carpet_west_2";

            for (int $$21 = 0; $$21 < $$18.height; $$21++) {
               for (int $$22 = 0; $$22 < $$18.width; $$22++) {
                  if ($$18.get($$22, $$21) == 1) {
                     BlockPos $$23 = $$16.relative($$1.rotate(Direction.SOUTH), 8 + ($$21 - this.startY) * 8);
                     $$23 = $$23.relative($$1.rotate(Direction.EAST), ($$22 - this.startX) * 8);
                     $$2.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "corridor_floor", $$23, $$1));
                     if ($$18.get($$22, $$21 - 1) == 1 || ($$17.get($$22, $$21 - 1) & 8388608) == 8388608) {
                        $$2.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, "carpet_north", $$23.relative($$1.rotate(Direction.EAST), 1).above(), $$1
                           )
                        );
                     }

                     if ($$18.get($$22 + 1, $$21) == 1 || ($$17.get($$22 + 1, $$21) & 8388608) == 8388608) {
                        $$2.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager,
                              "carpet_east",
                              $$23.relative($$1.rotate(Direction.SOUTH), 1).relative($$1.rotate(Direction.EAST), 5).above(),
                              $$1
                           )
                        );
                     }

                     if ($$18.get($$22, $$21 + 1) == 1 || ($$17.get($$22, $$21 + 1) & 8388608) == 8388608) {
                        $$2.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, $$19, $$23.relative($$1.rotate(Direction.SOUTH), 5).relative($$1.rotate(Direction.WEST), 1), $$1
                           )
                        );
                     }

                     if ($$18.get($$22 - 1, $$21) == 1 || ($$17.get($$22 - 1, $$21) & 8388608) == 8388608) {
                        $$2.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, $$20, $$23.relative($$1.rotate(Direction.WEST), 1).relative($$1.rotate(Direction.NORTH), 1), $$1
                           )
                        );
                     }
                  }
               }
            }

            String $$24 = $$15 == 0 ? "indoors_wall_1" : "indoors_wall_2";
            String $$25 = $$15 == 0 ? "indoors_door_1" : "indoors_door_2";
            List<Direction> $$26 = Lists.newArrayList();

            for (int $$27 = 0; $$27 < $$18.height; $$27++) {
               for (int $$28 = 0; $$28 < $$18.width; $$28++) {
                  boolean $$29 = $$15 == 2 && $$18.get($$28, $$27) == 3;
                  if ($$18.get($$28, $$27) == 2 || $$29) {
                     int $$30 = $$17.get($$28, $$27);
                     int $$31 = $$30 & 983040;
                     int $$32 = $$30 & 65535;
                     $$29 = $$29 && ($$30 & 8388608) == 8388608;
                     $$26.clear();
                     if (($$30 & 2097152) == 2097152) {
                        for (Direction $$33 : Plane.HORIZONTAL) {
                           if ($$18.get($$28 + $$33.getStepX(), $$27 + $$33.getStepZ()) == 1) {
                              $$26.add($$33);
                           }
                        }
                     }

                     Direction $$34 = null;
                     if (!$$26.isEmpty()) {
                        $$34 = $$26.get(this.random.nextInt($$26.size()));
                     } else if (($$30 & 1048576) == 1048576) {
                        $$34 = Direction.UP;
                     }

                     BlockPos $$35 = $$16.relative($$1.rotate(Direction.SOUTH), 8 + ($$27 - this.startY) * 8);
                     $$35 = $$35.relative($$1.rotate(Direction.EAST), -1 + ($$28 - this.startX) * 8);
                     if (WoodlandMansionPieces.MansionGrid.isHouse($$18, $$28 - 1, $$27) && !$$3.isRoomId($$18, $$28 - 1, $$27, $$15, $$32)) {
                        $$2.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, $$34 == Direction.WEST ? $$25 : $$24, $$35, $$1));
                     }

                     if ($$18.get($$28 + 1, $$27) == 1 && !$$29) {
                        BlockPos $$36 = $$35.relative($$1.rotate(Direction.EAST), 8);
                        $$2.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, $$34 == Direction.EAST ? $$25 : $$24, $$36, $$1));
                     }

                     if (WoodlandMansionPieces.MansionGrid.isHouse($$18, $$28, $$27 + 1) && !$$3.isRoomId($$18, $$28, $$27 + 1, $$15, $$32)) {
                        BlockPos $$37 = $$35.relative($$1.rotate(Direction.SOUTH), 7);
                        $$37 = $$37.relative($$1.rotate(Direction.EAST), 7);
                        $$2.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, $$34 == Direction.SOUTH ? $$25 : $$24, $$37, $$1.getRotated(Rotation.CLOCKWISE_90)
                           )
                        );
                     }

                     if ($$18.get($$28, $$27 - 1) == 1 && !$$29) {
                        BlockPos $$38 = $$35.relative($$1.rotate(Direction.NORTH), 1);
                        $$38 = $$38.relative($$1.rotate(Direction.EAST), 7);
                        $$2.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, $$34 == Direction.NORTH ? $$25 : $$24, $$38, $$1.getRotated(Rotation.CLOCKWISE_90)
                           )
                        );
                     }

                     if ($$31 == 65536) {
                        this.addRoom1x1($$2, $$35, $$1, $$34, $$14[$$15]);
                     } else if ($$31 == 131072 && $$34 != null) {
                        Direction $$39 = $$3.get1x2RoomDirection($$18, $$28, $$27, $$15, $$32);
                        boolean $$40 = ($$30 & 4194304) == 4194304;
                        this.addRoom1x2($$2, $$35, $$1, $$39, $$34, $$14[$$15], $$40);
                     } else if ($$31 == 262144 && $$34 != null && $$34 != Direction.UP) {
                        Direction $$41 = $$34.getClockWise();
                        if (!$$3.isRoomId($$18, $$28 + $$41.getStepX(), $$27 + $$41.getStepZ(), $$15, $$32)) {
                           $$41 = $$41.getOpposite();
                        }

                        this.addRoom2x2($$2, $$35, $$1, $$41, $$34, $$14[$$15]);
                     } else if ($$31 == 262144 && $$34 == Direction.UP) {
                        this.addRoom2x2Secret($$2, $$35, $$1, $$14[$$15]);
                     }
                  }
               }
            }
         }
      }

      private void traverseOuterWalls(
         List<WoodlandMansionPieces.WoodlandMansionPiece> $$0,
         WoodlandMansionPieces.PlacementData $$1,
         WoodlandMansionPieces.SimpleGrid $$2,
         Direction $$3,
         int $$4,
         int $$5,
         int $$6,
         int $$7
      ) {
         int $$8 = $$4;
         int $$9 = $$5;
         Direction $$10 = $$3;

         do {
            if (!WoodlandMansionPieces.MansionGrid.isHouse($$2, $$8 + $$3.getStepX(), $$9 + $$3.getStepZ())) {
               this.traverseTurn($$0, $$1);
               $$3 = $$3.getClockWise();
               if ($$8 != $$6 || $$9 != $$7 || $$10 != $$3) {
                  this.traverseWallPiece($$0, $$1);
               }
            } else if (WoodlandMansionPieces.MansionGrid.isHouse($$2, $$8 + $$3.getStepX(), $$9 + $$3.getStepZ())
               && WoodlandMansionPieces.MansionGrid.isHouse(
                  $$2, $$8 + $$3.getStepX() + $$3.getCounterClockWise().getStepX(), $$9 + $$3.getStepZ() + $$3.getCounterClockWise().getStepZ()
               )) {
               this.traverseInnerTurn($$0, $$1);
               $$8 += $$3.getStepX();
               $$9 += $$3.getStepZ();
               $$3 = $$3.getCounterClockWise();
            } else {
               $$8 += $$3.getStepX();
               $$9 += $$3.getStepZ();
               if ($$8 != $$6 || $$9 != $$7 || $$10 != $$3) {
                  this.traverseWallPiece($$0, $$1);
               }
            }
         } while ($$8 != $$6 || $$9 != $$7 || $$10 != $$3);
      }

      private void createRoof(
         List<WoodlandMansionPieces.WoodlandMansionPiece> $$0,
         BlockPos $$1,
         Rotation $$2,
         WoodlandMansionPieces.SimpleGrid $$3,
         WoodlandMansionPieces.SimpleGrid $$4
      ) {
         for (int $$5 = 0; $$5 < $$3.height; $$5++) {
            for (int $$6 = 0; $$6 < $$3.width; $$6++) {
               BlockPos $$27 = $$1.relative($$2.rotate(Direction.SOUTH), 8 + ($$5 - this.startY) * 8);
               $$27 = $$27.relative($$2.rotate(Direction.EAST), ($$6 - this.startX) * 8);
               boolean $$8 = $$4 != null && WoodlandMansionPieces.MansionGrid.isHouse($$4, $$6, $$5);
               if (WoodlandMansionPieces.MansionGrid.isHouse($$3, $$6, $$5) && !$$8) {
                  $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof", $$27.above(3), $$2));
                  if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$6 + 1, $$5)) {
                     BlockPos $$9 = $$27.relative($$2.rotate(Direction.EAST), 6);
                     $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", $$9, $$2));
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$6 - 1, $$5)) {
                     BlockPos $$10 = $$27.relative($$2.rotate(Direction.EAST), 0);
                     $$10 = $$10.relative($$2.rotate(Direction.SOUTH), 7);
                     $$0.add(
                        new WoodlandMansionPieces.WoodlandMansionPiece(
                           this.structureTemplateManager, "roof_front", $$10, $$2.getRotated(Rotation.CLOCKWISE_180)
                        )
                     );
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$6, $$5 - 1)) {
                     BlockPos $$11 = $$27.relative($$2.rotate(Direction.WEST), 1);
                     $$0.add(
                        new WoodlandMansionPieces.WoodlandMansionPiece(
                           this.structureTemplateManager, "roof_front", $$11, $$2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                        )
                     );
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$6, $$5 + 1)) {
                     BlockPos $$12 = $$27.relative($$2.rotate(Direction.EAST), 6);
                     $$12 = $$12.relative($$2.rotate(Direction.SOUTH), 6);
                     $$0.add(
                        new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", $$12, $$2.getRotated(Rotation.CLOCKWISE_90))
                     );
                  }
               }
            }
         }

         if ($$4 != null) {
            for (int $$13 = 0; $$13 < $$3.height; $$13++) {
               for (int $$14 = 0; $$14 < $$3.width; $$14++) {
                  BlockPos var17 = $$1.relative($$2.rotate(Direction.SOUTH), 8 + ($$13 - this.startY) * 8);
                  var17 = var17.relative($$2.rotate(Direction.EAST), ($$14 - this.startX) * 8);
                  boolean $$16 = WoodlandMansionPieces.MansionGrid.isHouse($$4, $$14, $$13);
                  if (WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14, $$13) && $$16) {
                     if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14 + 1, $$13)) {
                        BlockPos $$17 = var17.relative($$2.rotate(Direction.EAST), 7);
                        $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall", $$17, $$2));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14 - 1, $$13)) {
                        BlockPos $$18 = var17.relative($$2.rotate(Direction.WEST), 1);
                        $$18 = $$18.relative($$2.rotate(Direction.SOUTH), 6);
                        $$0.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, "small_wall", $$18, $$2.getRotated(Rotation.CLOCKWISE_180)
                           )
                        );
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14, $$13 - 1)) {
                        BlockPos $$19 = var17.relative($$2.rotate(Direction.WEST), 0);
                        $$19 = $$19.relative($$2.rotate(Direction.NORTH), 1);
                        $$0.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, "small_wall", $$19, $$2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                           )
                        );
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14, $$13 + 1)) {
                        BlockPos $$20 = var17.relative($$2.rotate(Direction.EAST), 6);
                        $$20 = $$20.relative($$2.rotate(Direction.SOUTH), 7);
                        $$0.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, "small_wall", $$20, $$2.getRotated(Rotation.CLOCKWISE_90)
                           )
                        );
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14 + 1, $$13)) {
                        if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14, $$13 - 1)) {
                           BlockPos $$21 = var17.relative($$2.rotate(Direction.EAST), 7);
                           $$21 = $$21.relative($$2.rotate(Direction.NORTH), 2);
                           $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", $$21, $$2));
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14, $$13 + 1)) {
                           BlockPos $$22 = var17.relative($$2.rotate(Direction.EAST), 8);
                           $$22 = $$22.relative($$2.rotate(Direction.SOUTH), 7);
                           $$0.add(
                              new WoodlandMansionPieces.WoodlandMansionPiece(
                                 this.structureTemplateManager, "small_wall_corner", $$22, $$2.getRotated(Rotation.CLOCKWISE_90)
                              )
                           );
                        }
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14 - 1, $$13)) {
                        if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14, $$13 - 1)) {
                           BlockPos $$23 = var17.relative($$2.rotate(Direction.WEST), 2);
                           $$23 = $$23.relative($$2.rotate(Direction.NORTH), 1);
                           $$0.add(
                              new WoodlandMansionPieces.WoodlandMansionPiece(
                                 this.structureTemplateManager, "small_wall_corner", $$23, $$2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                              )
                           );
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$14, $$13 + 1)) {
                           BlockPos $$24 = var17.relative($$2.rotate(Direction.WEST), 1);
                           $$24 = $$24.relative($$2.rotate(Direction.SOUTH), 8);
                           $$0.add(
                              new WoodlandMansionPieces.WoodlandMansionPiece(
                                 this.structureTemplateManager, "small_wall_corner", $$24, $$2.getRotated(Rotation.CLOCKWISE_180)
                              )
                           );
                        }
                     }
                  }
               }
            }
         }

         for (int $$25 = 0; $$25 < $$3.height; $$25++) {
            for (int $$26 = 0; $$26 < $$3.width; $$26++) {
               BlockPos var19 = $$1.relative($$2.rotate(Direction.SOUTH), 8 + ($$25 - this.startY) * 8);
               var19 = var19.relative($$2.rotate(Direction.EAST), ($$26 - this.startX) * 8);
               boolean $$28 = $$4 != null && WoodlandMansionPieces.MansionGrid.isHouse($$4, $$26, $$25);
               if (WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26, $$25) && !$$28) {
                  if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26 + 1, $$25)) {
                     BlockPos $$29 = var19.relative($$2.rotate(Direction.EAST), 6);
                     if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26, $$25 + 1)) {
                        BlockPos $$30 = $$29.relative($$2.rotate(Direction.SOUTH), 6);
                        $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", $$30, $$2));
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26 + 1, $$25 + 1)) {
                        BlockPos $$31 = $$29.relative($$2.rotate(Direction.SOUTH), 5);
                        $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", $$31, $$2));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26, $$25 - 1)) {
                        $$0.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, "roof_corner", $$29, $$2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                           )
                        );
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26 + 1, $$25 - 1)) {
                        BlockPos $$32 = var19.relative($$2.rotate(Direction.EAST), 9);
                        $$32 = $$32.relative($$2.rotate(Direction.NORTH), 2);
                        $$0.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, "roof_inner_corner", $$32, $$2.getRotated(Rotation.CLOCKWISE_90)
                           )
                        );
                     }
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26 - 1, $$25)) {
                     BlockPos $$33 = var19.relative($$2.rotate(Direction.EAST), 0);
                     $$33 = $$33.relative($$2.rotate(Direction.SOUTH), 0);
                     if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26, $$25 + 1)) {
                        BlockPos $$34 = $$33.relative($$2.rotate(Direction.SOUTH), 6);
                        $$0.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, "roof_corner", $$34, $$2.getRotated(Rotation.CLOCKWISE_90)
                           )
                        );
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26 - 1, $$25 + 1)) {
                        BlockPos $$35 = $$33.relative($$2.rotate(Direction.SOUTH), 8);
                        $$35 = $$35.relative($$2.rotate(Direction.WEST), 3);
                        $$0.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, "roof_inner_corner", $$35, $$2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                           )
                        );
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26, $$25 - 1)) {
                        $$0.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, "roof_corner", $$33, $$2.getRotated(Rotation.CLOCKWISE_180)
                           )
                        );
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse($$3, $$26 - 1, $$25 - 1)) {
                        BlockPos $$36 = $$33.relative($$2.rotate(Direction.SOUTH), 1);
                        $$0.add(
                           new WoodlandMansionPieces.WoodlandMansionPiece(
                              this.structureTemplateManager, "roof_inner_corner", $$36, $$2.getRotated(Rotation.CLOCKWISE_180)
                           )
                        );
                     }
                  }
               }
            }
         }
      }

      private void entrance(List<WoodlandMansionPieces.WoodlandMansionPiece> $$0, WoodlandMansionPieces.PlacementData $$1) {
         Direction $$2 = $$1.rotation.rotate(Direction.WEST);
         $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "entrance", $$1.position.relative($$2, 9), $$1.rotation));
         $$1.position = $$1.position.relative($$1.rotation.rotate(Direction.SOUTH), 16);
      }

      private void traverseWallPiece(List<WoodlandMansionPieces.WoodlandMansionPiece> $$0, WoodlandMansionPieces.PlacementData $$1) {
         $$0.add(
            new WoodlandMansionPieces.WoodlandMansionPiece(
               this.structureTemplateManager, $$1.wallType, $$1.position.relative($$1.rotation.rotate(Direction.EAST), 7), $$1.rotation
            )
         );
         $$1.position = $$1.position.relative($$1.rotation.rotate(Direction.SOUTH), 8);
      }

      private void traverseTurn(List<WoodlandMansionPieces.WoodlandMansionPiece> $$0, WoodlandMansionPieces.PlacementData $$1) {
         $$1.position = $$1.position.relative($$1.rotation.rotate(Direction.SOUTH), -1);
         $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "wall_corner", $$1.position, $$1.rotation));
         $$1.position = $$1.position.relative($$1.rotation.rotate(Direction.SOUTH), -7);
         $$1.position = $$1.position.relative($$1.rotation.rotate(Direction.WEST), -6);
         $$1.rotation = $$1.rotation.getRotated(Rotation.CLOCKWISE_90);
      }

      private void traverseInnerTurn(List<WoodlandMansionPieces.WoodlandMansionPiece> $$0, WoodlandMansionPieces.PlacementData $$1) {
         $$1.position = $$1.position.relative($$1.rotation.rotate(Direction.SOUTH), 6);
         $$1.position = $$1.position.relative($$1.rotation.rotate(Direction.EAST), 8);
         $$1.rotation = $$1.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
      }

      private void addRoom1x1(
         List<WoodlandMansionPieces.WoodlandMansionPiece> $$0, BlockPos $$1, Rotation $$2, Direction $$3, WoodlandMansionPieces.FloorRoomCollection $$4
      ) {
         Rotation $$5 = Rotation.NONE;
         String $$6 = $$4.get1x1(this.random);
         if ($$3 != Direction.EAST) {
            if ($$3 == Direction.NORTH) {
               $$5 = $$5.getRotated(Rotation.COUNTERCLOCKWISE_90);
            } else if ($$3 == Direction.WEST) {
               $$5 = $$5.getRotated(Rotation.CLOCKWISE_180);
            } else if ($$3 == Direction.SOUTH) {
               $$5 = $$5.getRotated(Rotation.CLOCKWISE_90);
            } else {
               $$6 = $$4.get1x1Secret(this.random);
            }
         }

         BlockPos $$7 = StructureTemplate.getZeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, $$5, 7, 7);
         $$5 = $$5.getRotated($$2);
         $$7 = $$7.rotate($$2);
         BlockPos $$8 = $$1.offset($$7.getX(), 0, $$7.getZ());
         $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, $$6, $$8, $$5));
      }

      private void addRoom1x2(
         List<WoodlandMansionPieces.WoodlandMansionPiece> $$0,
         BlockPos $$1,
         Rotation $$2,
         Direction $$3,
         Direction $$4,
         WoodlandMansionPieces.FloorRoomCollection $$5,
         boolean $$6
      ) {
         if ($$4 == Direction.EAST && $$3 == Direction.SOUTH) {
            BlockPos $$7 = $$1.relative($$2.rotate(Direction.EAST), 1);
            $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, $$5.get1x2SideEntrance(this.random, $$6), $$7, $$2));
         } else if ($$4 == Direction.EAST && $$3 == Direction.NORTH) {
            BlockPos $$8 = $$1.relative($$2.rotate(Direction.EAST), 1);
            $$8 = $$8.relative($$2.rotate(Direction.SOUTH), 6);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2SideEntrance(this.random, $$6), $$8, $$2, Mirror.LEFT_RIGHT
               )
            );
         } else if ($$4 == Direction.WEST && $$3 == Direction.NORTH) {
            BlockPos $$9 = $$1.relative($$2.rotate(Direction.EAST), 7);
            $$9 = $$9.relative($$2.rotate(Direction.SOUTH), 6);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2SideEntrance(this.random, $$6), $$9, $$2.getRotated(Rotation.CLOCKWISE_180)
               )
            );
         } else if ($$4 == Direction.WEST && $$3 == Direction.SOUTH) {
            BlockPos $$10 = $$1.relative($$2.rotate(Direction.EAST), 7);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2SideEntrance(this.random, $$6), $$10, $$2, Mirror.FRONT_BACK
               )
            );
         } else if ($$4 == Direction.SOUTH && $$3 == Direction.EAST) {
            BlockPos $$11 = $$1.relative($$2.rotate(Direction.EAST), 1);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2SideEntrance(this.random, $$6), $$11, $$2.getRotated(Rotation.CLOCKWISE_90), Mirror.LEFT_RIGHT
               )
            );
         } else if ($$4 == Direction.SOUTH && $$3 == Direction.WEST) {
            BlockPos $$12 = $$1.relative($$2.rotate(Direction.EAST), 7);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2SideEntrance(this.random, $$6), $$12, $$2.getRotated(Rotation.CLOCKWISE_90)
               )
            );
         } else if ($$4 == Direction.NORTH && $$3 == Direction.WEST) {
            BlockPos $$13 = $$1.relative($$2.rotate(Direction.EAST), 7);
            $$13 = $$13.relative($$2.rotate(Direction.SOUTH), 6);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2SideEntrance(this.random, $$6), $$13, $$2.getRotated(Rotation.CLOCKWISE_90), Mirror.FRONT_BACK
               )
            );
         } else if ($$4 == Direction.NORTH && $$3 == Direction.EAST) {
            BlockPos $$14 = $$1.relative($$2.rotate(Direction.EAST), 1);
            $$14 = $$14.relative($$2.rotate(Direction.SOUTH), 6);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2SideEntrance(this.random, $$6), $$14, $$2.getRotated(Rotation.COUNTERCLOCKWISE_90)
               )
            );
         } else if ($$4 == Direction.SOUTH && $$3 == Direction.NORTH) {
            BlockPos $$15 = $$1.relative($$2.rotate(Direction.EAST), 1);
            $$15 = $$15.relative($$2.rotate(Direction.NORTH), 8);
            $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, $$5.get1x2FrontEntrance(this.random, $$6), $$15, $$2));
         } else if ($$4 == Direction.NORTH && $$3 == Direction.SOUTH) {
            BlockPos $$16 = $$1.relative($$2.rotate(Direction.EAST), 7);
            $$16 = $$16.relative($$2.rotate(Direction.SOUTH), 14);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2FrontEntrance(this.random, $$6), $$16, $$2.getRotated(Rotation.CLOCKWISE_180)
               )
            );
         } else if ($$4 == Direction.WEST && $$3 == Direction.EAST) {
            BlockPos $$17 = $$1.relative($$2.rotate(Direction.EAST), 15);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2FrontEntrance(this.random, $$6), $$17, $$2.getRotated(Rotation.CLOCKWISE_90)
               )
            );
         } else if ($$4 == Direction.EAST && $$3 == Direction.WEST) {
            BlockPos $$18 = $$1.relative($$2.rotate(Direction.WEST), 7);
            $$18 = $$18.relative($$2.rotate(Direction.SOUTH), 6);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2FrontEntrance(this.random, $$6), $$18, $$2.getRotated(Rotation.COUNTERCLOCKWISE_90)
               )
            );
         } else if ($$4 == Direction.UP && $$3 == Direction.EAST) {
            BlockPos $$19 = $$1.relative($$2.rotate(Direction.EAST), 15);
            $$0.add(
               new WoodlandMansionPieces.WoodlandMansionPiece(
                  this.structureTemplateManager, $$5.get1x2Secret(this.random), $$19, $$2.getRotated(Rotation.CLOCKWISE_90)
               )
            );
         } else if ($$4 == Direction.UP && $$3 == Direction.SOUTH) {
            BlockPos $$20 = $$1.relative($$2.rotate(Direction.EAST), 1);
            $$20 = $$20.relative($$2.rotate(Direction.NORTH), 0);
            $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, $$5.get1x2Secret(this.random), $$20, $$2));
         }
      }

      private void addRoom2x2(
         List<WoodlandMansionPieces.WoodlandMansionPiece> $$0,
         BlockPos $$1,
         Rotation $$2,
         Direction $$3,
         Direction $$4,
         WoodlandMansionPieces.FloorRoomCollection $$5
      ) {
         int $$6 = 0;
         int $$7 = 0;
         Rotation $$8 = $$2;
         Mirror $$9 = Mirror.NONE;
         if ($$4 == Direction.EAST && $$3 == Direction.SOUTH) {
            $$6 = -7;
         } else if ($$4 == Direction.EAST && $$3 == Direction.NORTH) {
            $$6 = -7;
            $$7 = 6;
            $$9 = Mirror.LEFT_RIGHT;
         } else if ($$4 == Direction.NORTH && $$3 == Direction.EAST) {
            $$6 = 1;
            $$7 = 14;
            $$8 = $$2.getRotated(Rotation.COUNTERCLOCKWISE_90);
         } else if ($$4 == Direction.NORTH && $$3 == Direction.WEST) {
            $$6 = 7;
            $$7 = 14;
            $$8 = $$2.getRotated(Rotation.COUNTERCLOCKWISE_90);
            $$9 = Mirror.LEFT_RIGHT;
         } else if ($$4 == Direction.SOUTH && $$3 == Direction.WEST) {
            $$6 = 7;
            $$7 = -8;
            $$8 = $$2.getRotated(Rotation.CLOCKWISE_90);
         } else if ($$4 == Direction.SOUTH && $$3 == Direction.EAST) {
            $$6 = 1;
            $$7 = -8;
            $$8 = $$2.getRotated(Rotation.CLOCKWISE_90);
            $$9 = Mirror.LEFT_RIGHT;
         } else if ($$4 == Direction.WEST && $$3 == Direction.NORTH) {
            $$6 = 15;
            $$7 = 6;
            $$8 = $$2.getRotated(Rotation.CLOCKWISE_180);
         } else if ($$4 == Direction.WEST && $$3 == Direction.SOUTH) {
            $$6 = 15;
            $$9 = Mirror.FRONT_BACK;
         }

         BlockPos $$10 = $$1.relative($$2.rotate(Direction.EAST), $$6);
         $$10 = $$10.relative($$2.rotate(Direction.SOUTH), $$7);
         $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, $$5.get2x2(this.random), $$10, $$8, $$9));
      }

      private void addRoom2x2Secret(
         List<WoodlandMansionPieces.WoodlandMansionPiece> $$0, BlockPos $$1, Rotation $$2, WoodlandMansionPieces.FloorRoomCollection $$3
      ) {
         BlockPos $$4 = $$1.relative($$2.rotate(Direction.EAST), 1);
         $$0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, $$3.get2x2Secret(this.random), $$4, $$2, Mirror.NONE));
      }
   }

   static class PlacementData {
      public Rotation rotation;
      public BlockPos position;
      public String wallType;
   }

   static class SecondFloorRoomCollection extends WoodlandMansionPieces.FloorRoomCollection {
      @Override
      public String get1x1(RandomSource $$0) {
         return "1x1_b" + ($$0.nextInt(5) + 1);
      }

      @Override
      public String get1x1Secret(RandomSource $$0) {
         return "1x1_as" + ($$0.nextInt(4) + 1);
      }

      @Override
      public String get1x2SideEntrance(RandomSource $$0, boolean $$1) {
         return $$1 ? "1x2_c_stairs" : "1x2_c" + ($$0.nextInt(4) + 1);
      }

      @Override
      public String get1x2FrontEntrance(RandomSource $$0, boolean $$1) {
         return $$1 ? "1x2_d_stairs" : "1x2_d" + ($$0.nextInt(5) + 1);
      }

      @Override
      public String get1x2Secret(RandomSource $$0) {
         return "1x2_se" + ($$0.nextInt(1) + 1);
      }

      @Override
      public String get2x2(RandomSource $$0) {
         return "2x2_b" + ($$0.nextInt(5) + 1);
      }

      @Override
      public String get2x2Secret(RandomSource $$0) {
         return "2x2_s1";
      }
   }

   static class SimpleGrid {
      private final int[][] grid;
      final int width;
      final int height;
      private final int valueIfOutside;

      public SimpleGrid(int $$0, int $$1, int $$2) {
         this.width = $$0;
         this.height = $$1;
         this.valueIfOutside = $$2;
         this.grid = new int[$$0][$$1];
      }

      public void set(int $$0, int $$1, int $$2) {
         if ($$0 >= 0 && $$0 < this.width && $$1 >= 0 && $$1 < this.height) {
            this.grid[$$0][$$1] = $$2;
         }
      }

      public void set(int $$0, int $$1, int $$2, int $$3, int $$4) {
         for (int $$5 = $$1; $$5 <= $$3; $$5++) {
            for (int $$6 = $$0; $$6 <= $$2; $$6++) {
               this.set($$6, $$5, $$4);
            }
         }
      }

      public int get(int $$0, int $$1) {
         return $$0 >= 0 && $$0 < this.width && $$1 >= 0 && $$1 < this.height ? this.grid[$$0][$$1] : this.valueIfOutside;
      }

      public void setif(int $$0, int $$1, int $$2, int $$3) {
         if (this.get($$0, $$1) == $$2) {
            this.set($$0, $$1, $$3);
         }
      }

      public boolean edgesTo(int $$0, int $$1, int $$2) {
         return this.get($$0 - 1, $$1) == $$2 || this.get($$0 + 1, $$1) == $$2 || this.get($$0, $$1 + 1) == $$2 || this.get($$0, $$1 - 1) == $$2;
      }
   }

   static class ThirdFloorRoomCollection extends WoodlandMansionPieces.SecondFloorRoomCollection {
   }

   public static class WoodlandMansionPiece extends TemplateStructurePiece {
      public WoodlandMansionPiece(StructureTemplateManager $$0, String $$1, BlockPos $$2, Rotation $$3) {
         this($$0, $$1, $$2, $$3, Mirror.NONE);
      }

      public WoodlandMansionPiece(StructureTemplateManager $$0, String $$1, BlockPos $$2, Rotation $$3, Mirror $$4) {
         super(StructurePieceType.WOODLAND_MANSION_PIECE, 0, $$0, makeLocation($$1), $$1, makeSettings($$4, $$3), $$2);
      }

      public WoodlandMansionPiece(StructureTemplateManager $$0, CompoundTag $$1) {
         super(
            StructurePieceType.WOODLAND_MANSION_PIECE,
            $$1,
            $$0,
            $$1x -> makeSettings((Mirror)$$1.read("Mi", Mirror.LEGACY_CODEC).orElseThrow(), (Rotation)$$1.read("Rot", Rotation.LEGACY_CODEC).orElseThrow())
         );
      }

      @Override
      protected Identifier makeTemplateLocation() {
         return makeLocation(this.templateName);
      }

      private static Identifier makeLocation(String $$0) {
         return Identifier.withDefaultNamespace("woodland_mansion/" + $$0);
      }

      private static StructurePlaceSettings makeSettings(Mirror $$0, Rotation $$1) {
         return new StructurePlaceSettings().setIgnoreEntities(true).setRotation($$1).setMirror($$0).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.store("Rot", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
         $$1.store("Mi", Mirror.LEGACY_CODEC, this.placeSettings.getMirror());
      }

      @Override
      protected void handleDataMarker(String $$0, BlockPos $$1, net.minecraft.world.level.ServerLevelAccessor $$2, RandomSource $$3, BoundingBox $$4) {
         if ($$0.startsWith("Chest")) {
            Rotation $$5 = this.placeSettings.getRotation();
            BlockState $$6 = Blocks.CHEST.defaultBlockState();
            if ("ChestWest".equals($$0)) {
               $$6 = $$6.setValue(ChestBlock.FACING, $$5.rotate(Direction.WEST));
            } else if ("ChestEast".equals($$0)) {
               $$6 = $$6.setValue(ChestBlock.FACING, $$5.rotate(Direction.EAST));
            } else if ("ChestSouth".equals($$0)) {
               $$6 = $$6.setValue(ChestBlock.FACING, $$5.rotate(Direction.SOUTH));
            } else if ("ChestNorth".equals($$0)) {
               $$6 = $$6.setValue(ChestBlock.FACING, $$5.rotate(Direction.NORTH));
            }

            this.createChest($$2, $$4, $$3, $$1, BuiltInLootTables.WOODLAND_MANSION, $$6);
         } else {
            List<Mob> $$7 = new ArrayList<>();
            switch ($$0) {
               case "Mage":
                  $$7.add((Mob)EntityType.EVOKER.create($$2.getLevel(), EntitySpawnReason.STRUCTURE));
                  break;
               case "Warrior":
                  $$7.add((Mob)EntityType.VINDICATOR.create($$2.getLevel(), EntitySpawnReason.STRUCTURE));
                  break;
               case "Group of Allays":
                  int $$8 = $$2.getRandom().nextInt(3) + 1;

                  for (int $$9 = 0; $$9 < $$8; $$9++) {
                     $$7.add((Mob)EntityType.ALLAY.create($$2.getLevel(), EntitySpawnReason.STRUCTURE));
                  }
                  break;
               default:
                  return;
            }

            for (Mob $$10 : $$7) {
               if ($$10 != null) {
                  $$10.setPersistenceRequired();
                  $$10.snapTo($$1, 0.0F, 0.0F);
                  $$10.finalizeSpawn($$2, $$2.getCurrentDifficultyAt($$10.blockPosition()), EntitySpawnReason.STRUCTURE, null);
                  $$2.addFreshEntityWithPassengers($$10);
                  $$2.setBlock($$1, Blocks.AIR.defaultBlockState(), 2);
               }
            }
         }
      }
   }
}
