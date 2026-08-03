package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class EndCityPieces {
   private static final int MAX_GEN_DEPTH = 8;
   static final EndCityPieces.SectionGenerator HOUSE_TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
      @Override
      public void init() {
      }

      @Override
      public boolean generate(StructureTemplateManager $$0, int $$1, EndCityPieces.EndCityPiece $$2, BlockPos $$3, List<StructurePiece> $$4, RandomSource $$5) {
         if ($$1 > 8) {
            return false;
         } else {
            Rotation $$6 = $$2.placeSettings().getRotation();
            EndCityPieces.EndCityPiece $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$2, $$3, "base_floor", $$6, true));
            int $$8 = $$5.nextInt(3);
            if ($$8 == 0) {
               $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(-1, 4, -1), "base_roof", $$6, true));
            } else if ($$8 == 1) {
               $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(-1, 0, -1), "second_floor_2", $$6, false));
               $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(-1, 8, -1), "second_roof", $$6, false));
               EndCityPieces.recursiveChildren($$0, EndCityPieces.TOWER_GENERATOR, $$1 + 1, $$7, null, $$4, $$5);
            } else if ($$8 == 2) {
               $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(-1, 0, -1), "second_floor_2", $$6, false));
               $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(-1, 4, -1), "third_floor_2", $$6, false));
               $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(-1, 8, -1), "third_roof", $$6, true));
               EndCityPieces.recursiveChildren($$0, EndCityPieces.TOWER_GENERATOR, $$1 + 1, $$7, null, $$4, $$5);
            }

            return true;
         }
      }
   };
   static final List<Tuple<Rotation, BlockPos>> TOWER_BRIDGES = Lists.newArrayList(
      new Tuple[]{
         new Tuple(Rotation.NONE, new BlockPos(1, -1, 0)),
         new Tuple(Rotation.CLOCKWISE_90, new BlockPos(6, -1, 1)),
         new Tuple(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 5)),
         new Tuple(Rotation.CLOCKWISE_180, new BlockPos(5, -1, 6))
      }
   );
   static final EndCityPieces.SectionGenerator TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
      @Override
      public void init() {
      }

      @Override
      public boolean generate(StructureTemplateManager $$0, int $$1, EndCityPieces.EndCityPiece $$2, BlockPos $$3, List<StructurePiece> $$4, RandomSource $$5) {
         Rotation $$6 = $$2.placeSettings().getRotation();
         EndCityPieces.EndCityPiece $$7 = EndCityPieces.addHelper(
            $$4, EndCityPieces.addPiece($$0, $$2, new BlockPos(3 + $$5.nextInt(2), -3, 3 + $$5.nextInt(2)), "tower_base", $$6, true)
         );
         $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(0, 7, 0), "tower_piece", $$6, true));
         EndCityPieces.EndCityPiece $$8 = $$5.nextInt(3) == 0 ? $$7 : null;
         int $$9 = 1 + $$5.nextInt(3);

         for (int $$10 = 0; $$10 < $$9; $$10++) {
            $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(0, 4, 0), "tower_piece", $$6, true));
            if ($$10 < $$9 - 1 && $$5.nextBoolean()) {
               $$8 = $$7;
            }
         }

         if ($$8 != null) {
            for (Tuple<Rotation, BlockPos> $$11 : EndCityPieces.TOWER_BRIDGES) {
               if ($$5.nextBoolean()) {
                  EndCityPieces.EndCityPiece $$12 = EndCityPieces.addHelper(
                     $$4, EndCityPieces.addPiece($$0, $$8, (BlockPos)$$11.getB(), "bridge_end", $$6.getRotated((Rotation)$$11.getA()), true)
                  );
                  EndCityPieces.recursiveChildren($$0, EndCityPieces.TOWER_BRIDGE_GENERATOR, $$1 + 1, $$12, null, $$4, $$5);
               }
            }

            $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(-1, 4, -1), "tower_top", $$6, true));
         } else {
            if ($$1 != 7) {
               return EndCityPieces.recursiveChildren($$0, EndCityPieces.FAT_TOWER_GENERATOR, $$1 + 1, $$7, null, $$4, $$5);
            }

            $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(-1, 4, -1), "tower_top", $$6, true));
         }

         return true;
      }
   };
   static final EndCityPieces.SectionGenerator TOWER_BRIDGE_GENERATOR = new EndCityPieces.SectionGenerator() {
      public boolean shipCreated;

      @Override
      public void init() {
         this.shipCreated = false;
      }

      @Override
      public boolean generate(StructureTemplateManager $$0, int $$1, EndCityPieces.EndCityPiece $$2, BlockPos $$3, List<StructurePiece> $$4, RandomSource $$5) {
         Rotation $$6 = $$2.placeSettings().getRotation();
         int $$7 = $$5.nextInt(4) + 1;
         EndCityPieces.EndCityPiece $$8 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$2, new BlockPos(0, 0, -4), "bridge_piece", $$6, true));
         $$8.setGenDepth(-1);
         int $$9 = 0;

         for (int $$10 = 0; $$10 < $$7; $$10++) {
            if ($$5.nextBoolean()) {
               $$8 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$8, new BlockPos(0, $$9, -4), "bridge_piece", $$6, true));
               $$9 = 0;
            } else {
               if ($$5.nextBoolean()) {
                  $$8 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$8, new BlockPos(0, $$9, -4), "bridge_steep_stairs", $$6, true));
               } else {
                  $$8 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$8, new BlockPos(0, $$9, -8), "bridge_gentle_stairs", $$6, true));
               }

               $$9 = 4;
            }
         }

         if (!this.shipCreated && $$5.nextInt(10 - $$1) == 0) {
            EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$8, new BlockPos(-8 + $$5.nextInt(8), $$9, -70 + $$5.nextInt(10)), "ship", $$6, true));
            this.shipCreated = true;
         } else if (!EndCityPieces.recursiveChildren($$0, EndCityPieces.HOUSE_TOWER_GENERATOR, $$1 + 1, $$8, new BlockPos(-3, $$9 + 1, -11), $$4, $$5)) {
            return false;
         }

         $$8 = EndCityPieces.addHelper(
            $$4, EndCityPieces.addPiece($$0, $$8, new BlockPos(4, $$9, 0), "bridge_end", $$6.getRotated(Rotation.CLOCKWISE_180), true)
         );
         $$8.setGenDepth(-1);
         return true;
      }
   };
   static final List<Tuple<Rotation, BlockPos>> FAT_TOWER_BRIDGES = Lists.newArrayList(
      new Tuple[]{
         new Tuple(Rotation.NONE, new BlockPos(4, -1, 0)),
         new Tuple(Rotation.CLOCKWISE_90, new BlockPos(12, -1, 4)),
         new Tuple(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 8)),
         new Tuple(Rotation.CLOCKWISE_180, new BlockPos(8, -1, 12))
      }
   );
   static final EndCityPieces.SectionGenerator FAT_TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
      @Override
      public void init() {
      }

      @Override
      public boolean generate(StructureTemplateManager $$0, int $$1, EndCityPieces.EndCityPiece $$2, BlockPos $$3, List<StructurePiece> $$4, RandomSource $$5) {
         Rotation $$6 = $$2.placeSettings().getRotation();
         EndCityPieces.EndCityPiece $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$2, new BlockPos(-3, 4, -3), "fat_tower_base", $$6, true));
         $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(0, 4, 0), "fat_tower_middle", $$6, true));

         for (int $$8 = 0; $$8 < 2 && $$5.nextInt(3) != 0; $$8++) {
            $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(0, 8, 0), "fat_tower_middle", $$6, true));

            for (Tuple<Rotation, BlockPos> $$9 : EndCityPieces.FAT_TOWER_BRIDGES) {
               if ($$5.nextBoolean()) {
                  EndCityPieces.EndCityPiece $$10 = EndCityPieces.addHelper(
                     $$4, EndCityPieces.addPiece($$0, $$7, (BlockPos)$$9.getB(), "bridge_end", $$6.getRotated((Rotation)$$9.getA()), true)
                  );
                  EndCityPieces.recursiveChildren($$0, EndCityPieces.TOWER_BRIDGE_GENERATOR, $$1 + 1, $$10, null, $$4, $$5);
               }
            }
         }

         $$7 = EndCityPieces.addHelper($$4, EndCityPieces.addPiece($$0, $$7, new BlockPos(-2, 8, -2), "fat_tower_top", $$6, true));
         return true;
      }
   };

   static EndCityPieces.EndCityPiece addPiece(StructureTemplateManager $$0, EndCityPieces.EndCityPiece $$1, BlockPos $$2, String $$3, Rotation $$4, boolean $$5) {
      EndCityPieces.EndCityPiece $$6 = new EndCityPieces.EndCityPiece($$0, $$3, $$1.templatePosition(), $$4, $$5);
      BlockPos $$7 = $$1.template().calculateConnectedPosition($$1.placeSettings(), $$2, $$6.placeSettings(), BlockPos.ZERO);
      $$6.move($$7.getX(), $$7.getY(), $$7.getZ());
      return $$6;
   }

   public static void startHouseTower(StructureTemplateManager $$0, BlockPos $$1, Rotation $$2, List<StructurePiece> $$3, RandomSource $$4) {
      FAT_TOWER_GENERATOR.init();
      HOUSE_TOWER_GENERATOR.init();
      TOWER_BRIDGE_GENERATOR.init();
      TOWER_GENERATOR.init();
      EndCityPieces.EndCityPiece $$5 = addHelper($$3, new EndCityPieces.EndCityPiece($$0, "base_floor", $$1, $$2, true));
      $$5 = addHelper($$3, addPiece($$0, $$5, new BlockPos(-1, 0, -1), "second_floor_1", $$2, false));
      $$5 = addHelper($$3, addPiece($$0, $$5, new BlockPos(-1, 4, -1), "third_floor_1", $$2, false));
      $$5 = addHelper($$3, addPiece($$0, $$5, new BlockPos(-1, 8, -1), "third_roof", $$2, true));
      recursiveChildren($$0, TOWER_GENERATOR, 1, $$5, null, $$3, $$4);
   }

   static EndCityPieces.EndCityPiece addHelper(List<StructurePiece> $$0, EndCityPieces.EndCityPiece $$1) {
      $$0.add($$1);
      return $$1;
   }

   static boolean recursiveChildren(
      StructureTemplateManager $$0,
      EndCityPieces.SectionGenerator $$1,
      int $$2,
      EndCityPieces.EndCityPiece $$3,
      BlockPos $$4,
      List<StructurePiece> $$5,
      RandomSource $$6
   ) {
      if ($$2 > 8) {
         return false;
      } else {
         List<StructurePiece> $$7 = Lists.newArrayList();
         if ($$1.generate($$0, $$2, $$3, $$4, $$7, $$6)) {
            boolean $$8 = false;
            int $$9 = $$6.nextInt();

            for (StructurePiece $$10 : $$7) {
               $$10.setGenDepth($$9);
               StructurePiece $$11 = StructurePiece.findCollisionPiece($$5, $$10.getBoundingBox());
               if ($$11 != null && $$11.getGenDepth() != $$3.getGenDepth()) {
                  $$8 = true;
                  break;
               }
            }

            if (!$$8) {
               $$5.addAll($$7);
               return true;
            }
         }

         return false;
      }
   }

   public static class EndCityPiece extends TemplateStructurePiece {
      public EndCityPiece(StructureTemplateManager $$0, String $$1, BlockPos $$2, Rotation $$3, boolean $$4) {
         super(StructurePieceType.END_CITY_PIECE, 0, $$0, makeIdentifier($$1), $$1, makeSettings($$4, $$3), $$2);
      }

      public EndCityPiece(StructureTemplateManager $$0, CompoundTag $$1) {
         super(
            StructurePieceType.END_CITY_PIECE,
            $$1,
            $$0,
            $$1x -> makeSettings($$1.getBooleanOr("OW", false), (Rotation)$$1.read("Rot", Rotation.LEGACY_CODEC).orElseThrow())
         );
      }

      private static StructurePlaceSettings makeSettings(boolean $$0, Rotation $$1) {
         BlockIgnoreProcessor $$2 = $$0 ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
         return new StructurePlaceSettings().setIgnoreEntities(true).addProcessor($$2).setRotation($$1);
      }

      @Override
      protected Identifier makeTemplateLocation() {
         return makeIdentifier(this.templateName);
      }

      private static Identifier makeIdentifier(String $$0) {
         return Identifier.withDefaultNamespace("end_city/" + $$0);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.store("Rot", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
         $$1.putBoolean("OW", this.placeSettings.getProcessors().get(0) == BlockIgnoreProcessor.STRUCTURE_BLOCK);
      }

      @Override
      protected void handleDataMarker(String $$0, BlockPos $$1, net.minecraft.world.level.ServerLevelAccessor $$2, RandomSource $$3, BoundingBox $$4) {
         if ($$0.startsWith("Chest")) {
            BlockPos $$5 = $$1.below();
            if ($$4.isInside($$5)) {
               RandomizableContainer.setBlockEntityLootTable($$2, $$3, $$5, BuiltInLootTables.END_CITY_TREASURE);
            }
         } else if ($$4.isInside($$1) && net.minecraft.world.level.Level.isInSpawnableBounds($$1)) {
            if ($$0.startsWith("Sentry")) {
               Shulker $$6 = (Shulker)EntityType.SHULKER.create($$2.getLevel(), EntitySpawnReason.STRUCTURE);
               if ($$6 != null) {
                  $$6.setPos($$1.getX() + 0.5, $$1.getY(), $$1.getZ() + 0.5);
                  $$2.addFreshEntity($$6);
               }
            } else if ($$0.startsWith("Elytra")) {
               ItemFrame $$7 = new ItemFrame($$2.getLevel(), $$1, this.placeSettings.getRotation().rotate(Direction.SOUTH));
               $$7.setItem(new ItemStack(Items.ELYTRA), false);
               $$2.addFreshEntity($$7);
            }
         }
      }
   }

   interface SectionGenerator {
      void init();

      boolean generate(StructureTemplateManager var1, int var2, EndCityPieces.EndCityPiece var3, BlockPos var4, List<StructurePiece> var5, RandomSource var6);
   }
}
