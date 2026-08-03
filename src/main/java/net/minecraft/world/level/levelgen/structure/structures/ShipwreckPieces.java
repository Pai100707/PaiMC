package net.minecraft.world.level.levelgen.structure.structures;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class ShipwreckPieces {
   private static final int NUMBER_OF_BLOCKS_ALLOWED_IN_WORLD_GEN_REGION = 32;
   static final BlockPos PIVOT = new BlockPos(4, 0, 15);
   private static final Identifier[] STRUCTURE_LOCATION_BEACHED = new Identifier[]{
      Identifier.withDefaultNamespace("shipwreck/with_mast"),
      Identifier.withDefaultNamespace("shipwreck/sideways_full"),
      Identifier.withDefaultNamespace("shipwreck/sideways_fronthalf"),
      Identifier.withDefaultNamespace("shipwreck/sideways_backhalf"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_full"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_fronthalf"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_backhalf"),
      Identifier.withDefaultNamespace("shipwreck/with_mast_degraded"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_full_degraded"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_fronthalf_degraded"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_backhalf_degraded")
   };
   private static final Identifier[] STRUCTURE_LOCATION_OCEAN = new Identifier[]{
      Identifier.withDefaultNamespace("shipwreck/with_mast"),
      Identifier.withDefaultNamespace("shipwreck/upsidedown_full"),
      Identifier.withDefaultNamespace("shipwreck/upsidedown_fronthalf"),
      Identifier.withDefaultNamespace("shipwreck/upsidedown_backhalf"),
      Identifier.withDefaultNamespace("shipwreck/sideways_full"),
      Identifier.withDefaultNamespace("shipwreck/sideways_fronthalf"),
      Identifier.withDefaultNamespace("shipwreck/sideways_backhalf"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_full"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_fronthalf"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_backhalf"),
      Identifier.withDefaultNamespace("shipwreck/with_mast_degraded"),
      Identifier.withDefaultNamespace("shipwreck/upsidedown_full_degraded"),
      Identifier.withDefaultNamespace("shipwreck/upsidedown_fronthalf_degraded"),
      Identifier.withDefaultNamespace("shipwreck/upsidedown_backhalf_degraded"),
      Identifier.withDefaultNamespace("shipwreck/sideways_full_degraded"),
      Identifier.withDefaultNamespace("shipwreck/sideways_fronthalf_degraded"),
      Identifier.withDefaultNamespace("shipwreck/sideways_backhalf_degraded"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_full_degraded"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_fronthalf_degraded"),
      Identifier.withDefaultNamespace("shipwreck/rightsideup_backhalf_degraded")
   };
   static final Map<String, ResourceKey<LootTable>> MARKERS_TO_LOOT = Map.of(
      "map_chest", BuiltInLootTables.SHIPWRECK_MAP, "treasure_chest", BuiltInLootTables.SHIPWRECK_TREASURE, "supply_chest", BuiltInLootTables.SHIPWRECK_SUPPLY
   );

   public static ShipwreckPieces.ShipwreckPiece addRandomPiece(
      StructureTemplateManager $$0, BlockPos $$1, Rotation $$2, StructurePieceAccessor $$3, RandomSource $$4, boolean $$5
   ) {
      Identifier $$6 = (Identifier)Util.getRandom($$5 ? STRUCTURE_LOCATION_BEACHED : STRUCTURE_LOCATION_OCEAN, $$4);
      ShipwreckPieces.ShipwreckPiece $$7 = new ShipwreckPieces.ShipwreckPiece($$0, $$6, $$1, $$2, $$5);
      $$3.addPiece($$7);
      return $$7;
   }

   public static class ShipwreckPiece extends TemplateStructurePiece {
      private final boolean isBeached;

      public ShipwreckPiece(StructureTemplateManager $$0, Identifier $$1, BlockPos $$2, Rotation $$3, boolean $$4) {
         super(StructurePieceType.SHIPWRECK_PIECE, 0, $$0, $$1, $$1.toString(), makeSettings($$3), $$2);
         this.isBeached = $$4;
      }

      public ShipwreckPiece(StructureTemplateManager $$0, CompoundTag $$1) {
         super(StructurePieceType.SHIPWRECK_PIECE, $$1, $$0, $$1x -> makeSettings((Rotation)$$1.read("Rot", Rotation.LEGACY_CODEC).orElseThrow()));
         this.isBeached = $$1.getBooleanOr("isBeached", false);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.putBoolean("isBeached", this.isBeached);
         $$1.store("Rot", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
      }

      private static StructurePlaceSettings makeSettings(Rotation $$0) {
         return new StructurePlaceSettings()
            .setRotation($$0)
            .setMirror(Mirror.NONE)
            .setRotationPivot(ShipwreckPieces.PIVOT)
            .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
      }

      @Override
      protected void handleDataMarker(String $$0, BlockPos $$1, net.minecraft.world.level.ServerLevelAccessor $$2, RandomSource $$3, BoundingBox $$4) {
         ResourceKey<LootTable> $$5 = ShipwreckPieces.MARKERS_TO_LOOT.get($$0);
         if ($$5 != null) {
            RandomizableContainer.setBlockEntityLootTable($$2, $$3, $$1.below(), $$5);
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
         if (this.isTooBigToFitInWorldGenRegion()) {
            super.postProcess($$0, $$1, $$2, $$3, $$4, $$5, $$6);
         } else {
            int $$7 = $$0.getMaxY() + 1;
            int $$8 = 0;
            Vec3i $$9 = this.template.getSize();
            Heightmap.Types $$10 = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
            int $$11 = $$9.getX() * $$9.getZ();
            if ($$11 == 0) {
               $$8 = $$0.getHeight($$10, this.templatePosition.getX(), this.templatePosition.getZ());
            } else {
               BlockPos $$12 = this.templatePosition.offset($$9.getX() - 1, 0, $$9.getZ() - 1);

               for (BlockPos $$13 : BlockPos.betweenClosed(this.templatePosition, $$12)) {
                  int $$14 = $$0.getHeight($$10, $$13.getX(), $$13.getZ());
                  $$8 += $$14;
                  $$7 = Math.min($$7, $$14);
               }

               $$8 /= $$11;
            }

            this.adjustPositionHeight(this.isBeached ? this.calculateBeachedPosition($$7, $$3) : $$8);
            super.postProcess($$0, $$1, $$2, $$3, $$4, $$5, $$6);
         }
      }

      public boolean isTooBigToFitInWorldGenRegion() {
         Vec3i $$0 = this.template.getSize();
         return $$0.getX() > 32 || $$0.getY() > 32;
      }

      public int calculateBeachedPosition(int $$0, RandomSource $$1) {
         return $$0 - this.template.getSize().getY() / 2 - $$1.nextInt(3);
      }

      public void adjustPositionHeight(int $$0) {
         this.templatePosition = new BlockPos(this.templatePosition.getX(), $$0, this.templatePosition.getZ());
      }
   }
}
