package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class NetherFossilPieces {
   private static final Identifier[] FOSSILS = new Identifier[]{
      Identifier.withDefaultNamespace("nether_fossils/fossil_1"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_2"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_3"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_4"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_5"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_6"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_7"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_8"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_9"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_10"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_11"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_12"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_13"),
      Identifier.withDefaultNamespace("nether_fossils/fossil_14")
   };

   public static void addPieces(StructureTemplateManager $$0, StructurePieceAccessor $$1, RandomSource $$2, BlockPos $$3) {
      Rotation $$4 = Rotation.getRandom($$2);
      $$1.addPiece(new NetherFossilPieces.NetherFossilPiece($$0, (Identifier)Util.getRandom(FOSSILS, $$2), $$3, $$4));
   }

   public static class NetherFossilPiece extends TemplateStructurePiece {
      public NetherFossilPiece(StructureTemplateManager $$0, Identifier $$1, BlockPos $$2, Rotation $$3) {
         super(StructurePieceType.NETHER_FOSSIL, 0, $$0, $$1, $$1.toString(), makeSettings($$3), $$2);
      }

      public NetherFossilPiece(StructureTemplateManager $$0, CompoundTag $$1) {
         super(StructurePieceType.NETHER_FOSSIL, $$1, $$0, $$1x -> makeSettings((Rotation)$$1.read("Rot", Rotation.LEGACY_CODEC).orElseThrow()));
      }

      private static StructurePlaceSettings makeSettings(Rotation $$0) {
         return new StructurePlaceSettings().setRotation($$0).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.store("Rot", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
      }

      @Override
      protected void handleDataMarker(String $$0, BlockPos $$1, net.minecraft.world.level.ServerLevelAccessor $$2, RandomSource $$3, BoundingBox $$4) {
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
         BoundingBox $$7 = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
         $$4.encapsulate($$7);
         super.postProcess($$0, $$1, $$2, $$3, $$4, $$5, $$6);
         this.placeDriedGhast($$0, $$3, $$7, $$4);
      }

      private void placeDriedGhast(net.minecraft.world.level.WorldGenLevel $$0, RandomSource $$1, BoundingBox $$2, BoundingBox $$3) {
         RandomSource $$4 = RandomSource.create($$0.getSeed()).forkPositional().at($$2.getCenter());
         if ($$4.nextFloat() < 0.5F) {
            int $$5 = $$2.minX() + $$4.nextInt($$2.getXSpan());
            int $$6 = $$2.minY();
            int $$7 = $$2.minZ() + $$4.nextInt($$2.getZSpan());
            BlockPos $$8 = new BlockPos($$5, $$6, $$7);
            if ($$0.getBlockState($$8).isAir() && $$3.isInside($$8)) {
               $$0.setBlock($$8, Blocks.DRIED_GHAST.defaultBlockState().rotate(Rotation.getRandom($$4)), 2);
            }
         }
      }
   }
}
