package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.CappedProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendLoot;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class OceanRuinPieces {
   static final StructureProcessor WARM_SUSPICIOUS_BLOCK_PROCESSOR = archyRuleProcessor(
      Blocks.SAND, Blocks.SUSPICIOUS_SAND, BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY
   );
   static final StructureProcessor COLD_SUSPICIOUS_BLOCK_PROCESSOR = archyRuleProcessor(
      Blocks.GRAVEL, Blocks.SUSPICIOUS_GRAVEL, BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY
   );
   private static final Identifier[] WARM_RUINS = new Identifier[]{
      Identifier.withDefaultNamespace("underwater_ruin/warm_1"),
      Identifier.withDefaultNamespace("underwater_ruin/warm_2"),
      Identifier.withDefaultNamespace("underwater_ruin/warm_3"),
      Identifier.withDefaultNamespace("underwater_ruin/warm_4"),
      Identifier.withDefaultNamespace("underwater_ruin/warm_5"),
      Identifier.withDefaultNamespace("underwater_ruin/warm_6"),
      Identifier.withDefaultNamespace("underwater_ruin/warm_7"),
      Identifier.withDefaultNamespace("underwater_ruin/warm_8")
   };
   private static final Identifier[] RUINS_BRICK = new Identifier[]{
      Identifier.withDefaultNamespace("underwater_ruin/brick_1"),
      Identifier.withDefaultNamespace("underwater_ruin/brick_2"),
      Identifier.withDefaultNamespace("underwater_ruin/brick_3"),
      Identifier.withDefaultNamespace("underwater_ruin/brick_4"),
      Identifier.withDefaultNamespace("underwater_ruin/brick_5"),
      Identifier.withDefaultNamespace("underwater_ruin/brick_6"),
      Identifier.withDefaultNamespace("underwater_ruin/brick_7"),
      Identifier.withDefaultNamespace("underwater_ruin/brick_8")
   };
   private static final Identifier[] RUINS_CRACKED = new Identifier[]{
      Identifier.withDefaultNamespace("underwater_ruin/cracked_1"),
      Identifier.withDefaultNamespace("underwater_ruin/cracked_2"),
      Identifier.withDefaultNamespace("underwater_ruin/cracked_3"),
      Identifier.withDefaultNamespace("underwater_ruin/cracked_4"),
      Identifier.withDefaultNamespace("underwater_ruin/cracked_5"),
      Identifier.withDefaultNamespace("underwater_ruin/cracked_6"),
      Identifier.withDefaultNamespace("underwater_ruin/cracked_7"),
      Identifier.withDefaultNamespace("underwater_ruin/cracked_8")
   };
   private static final Identifier[] RUINS_MOSSY = new Identifier[]{
      Identifier.withDefaultNamespace("underwater_ruin/mossy_1"),
      Identifier.withDefaultNamespace("underwater_ruin/mossy_2"),
      Identifier.withDefaultNamespace("underwater_ruin/mossy_3"),
      Identifier.withDefaultNamespace("underwater_ruin/mossy_4"),
      Identifier.withDefaultNamespace("underwater_ruin/mossy_5"),
      Identifier.withDefaultNamespace("underwater_ruin/mossy_6"),
      Identifier.withDefaultNamespace("underwater_ruin/mossy_7"),
      Identifier.withDefaultNamespace("underwater_ruin/mossy_8")
   };
   private static final Identifier[] BIG_RUINS_BRICK = new Identifier[]{
      Identifier.withDefaultNamespace("underwater_ruin/big_brick_1"),
      Identifier.withDefaultNamespace("underwater_ruin/big_brick_2"),
      Identifier.withDefaultNamespace("underwater_ruin/big_brick_3"),
      Identifier.withDefaultNamespace("underwater_ruin/big_brick_8")
   };
   private static final Identifier[] BIG_RUINS_MOSSY = new Identifier[]{
      Identifier.withDefaultNamespace("underwater_ruin/big_mossy_1"),
      Identifier.withDefaultNamespace("underwater_ruin/big_mossy_2"),
      Identifier.withDefaultNamespace("underwater_ruin/big_mossy_3"),
      Identifier.withDefaultNamespace("underwater_ruin/big_mossy_8")
   };
   private static final Identifier[] BIG_RUINS_CRACKED = new Identifier[]{
      Identifier.withDefaultNamespace("underwater_ruin/big_cracked_1"),
      Identifier.withDefaultNamespace("underwater_ruin/big_cracked_2"),
      Identifier.withDefaultNamespace("underwater_ruin/big_cracked_3"),
      Identifier.withDefaultNamespace("underwater_ruin/big_cracked_8")
   };
   private static final Identifier[] BIG_WARM_RUINS = new Identifier[]{
      Identifier.withDefaultNamespace("underwater_ruin/big_warm_4"),
      Identifier.withDefaultNamespace("underwater_ruin/big_warm_5"),
      Identifier.withDefaultNamespace("underwater_ruin/big_warm_6"),
      Identifier.withDefaultNamespace("underwater_ruin/big_warm_7")
   };

   private static StructureProcessor archyRuleProcessor(Block $$0, Block $$1, ResourceKey<LootTable> $$2) {
      return new CappedProcessor(
         new RuleProcessor(
            List.of(
               new ProcessorRule(new BlockMatchTest($$0), AlwaysTrueTest.INSTANCE, PosAlwaysTrueTest.INSTANCE, $$1.defaultBlockState(), new AppendLoot($$2))
            )
         ),
         ConstantInt.of(5)
      );
   }

   private static Identifier getSmallWarmRuin(RandomSource $$0) {
      return (Identifier)Util.getRandom(WARM_RUINS, $$0);
   }

   private static Identifier getBigWarmRuin(RandomSource $$0) {
      return (Identifier)Util.getRandom(BIG_WARM_RUINS, $$0);
   }

   public static void addPieces(StructureTemplateManager $$0, BlockPos $$1, Rotation $$2, StructurePieceAccessor $$3, RandomSource $$4, OceanRuinStructure $$5) {
      boolean $$6 = $$4.nextFloat() <= $$5.largeProbability;
      float $$7 = $$6 ? 0.9F : 0.8F;
      addPiece($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      if ($$6 && $$4.nextFloat() <= $$5.clusterProbability) {
         addClusterRuins($$0, $$4, $$2, $$1, $$5, $$3);
      }
   }

   private static void addClusterRuins(
      StructureTemplateManager $$0, RandomSource $$1, Rotation $$2, BlockPos $$3, OceanRuinStructure $$4, StructurePieceAccessor $$5
   ) {
      BlockPos $$6 = new BlockPos($$3.getX(), 90, $$3.getZ());
      BlockPos $$7 = StructureTemplate.transform(new BlockPos(15, 0, 15), Mirror.NONE, $$2, BlockPos.ZERO).offset($$6);
      BoundingBox $$8 = BoundingBox.fromCorners($$6, $$7);
      BlockPos $$9 = new BlockPos(Math.min($$6.getX(), $$7.getX()), $$6.getY(), Math.min($$6.getZ(), $$7.getZ()));
      List<BlockPos> $$10 = allPositions($$1, $$9);
      int $$11 = Mth.nextInt($$1, 4, 8);

      for (int $$12 = 0; $$12 < $$11; $$12++) {
         if (!$$10.isEmpty()) {
            int $$13 = $$1.nextInt($$10.size());
            BlockPos $$14 = $$10.remove($$13);
            Rotation $$15 = Rotation.getRandom($$1);
            BlockPos $$16 = StructureTemplate.transform(new BlockPos(5, 0, 6), Mirror.NONE, $$15, BlockPos.ZERO).offset($$14);
            BoundingBox $$17 = BoundingBox.fromCorners($$14, $$16);
            if (!$$17.intersects($$8)) {
               addPiece($$0, $$14, $$15, $$5, $$1, $$4, false, 0.8F);
            }
         }
      }
   }

   private static List<BlockPos> allPositions(RandomSource $$0, BlockPos $$1) {
      List<BlockPos> $$2 = Lists.newArrayList();
      $$2.add($$1.offset(-16 + Mth.nextInt($$0, 1, 8), 0, 16 + Mth.nextInt($$0, 1, 7)));
      $$2.add($$1.offset(-16 + Mth.nextInt($$0, 1, 8), 0, Mth.nextInt($$0, 1, 7)));
      $$2.add($$1.offset(-16 + Mth.nextInt($$0, 1, 8), 0, -16 + Mth.nextInt($$0, 4, 8)));
      $$2.add($$1.offset(Mth.nextInt($$0, 1, 7), 0, 16 + Mth.nextInt($$0, 1, 7)));
      $$2.add($$1.offset(Mth.nextInt($$0, 1, 7), 0, -16 + Mth.nextInt($$0, 4, 6)));
      $$2.add($$1.offset(16 + Mth.nextInt($$0, 1, 7), 0, 16 + Mth.nextInt($$0, 3, 8)));
      $$2.add($$1.offset(16 + Mth.nextInt($$0, 1, 7), 0, Mth.nextInt($$0, 1, 7)));
      $$2.add($$1.offset(16 + Mth.nextInt($$0, 1, 7), 0, -16 + Mth.nextInt($$0, 4, 8)));
      return $$2;
   }

   private static void addPiece(
      StructureTemplateManager $$0, BlockPos $$1, Rotation $$2, StructurePieceAccessor $$3, RandomSource $$4, OceanRuinStructure $$5, boolean $$6, float $$7
   ) {
      switch ($$5.biomeTemp) {
         case WARM:
         default:
            Identifier $$8 = $$6 ? getBigWarmRuin($$4) : getSmallWarmRuin($$4);
            $$3.addPiece(new OceanRuinPieces.OceanRuinPiece($$0, $$8, $$1, $$2, $$7, $$5.biomeTemp, $$6));
            break;
         case COLD:
            Identifier[] $$9 = $$6 ? BIG_RUINS_BRICK : RUINS_BRICK;
            Identifier[] $$10 = $$6 ? BIG_RUINS_CRACKED : RUINS_CRACKED;
            Identifier[] $$11 = $$6 ? BIG_RUINS_MOSSY : RUINS_MOSSY;
            int $$12 = $$4.nextInt($$9.length);
            $$3.addPiece(new OceanRuinPieces.OceanRuinPiece($$0, $$9[$$12], $$1, $$2, $$7, $$5.biomeTemp, $$6));
            $$3.addPiece(new OceanRuinPieces.OceanRuinPiece($$0, $$10[$$12], $$1, $$2, 0.7F, $$5.biomeTemp, $$6));
            $$3.addPiece(new OceanRuinPieces.OceanRuinPiece($$0, $$11[$$12], $$1, $$2, 0.5F, $$5.biomeTemp, $$6));
      }
   }

   public static class OceanRuinPiece extends TemplateStructurePiece {
      private final OceanRuinStructure.Type biomeType;
      private final float integrity;
      private final boolean isLarge;

      public OceanRuinPiece(StructureTemplateManager $$0, Identifier $$1, BlockPos $$2, Rotation $$3, float $$4, OceanRuinStructure.Type $$5, boolean $$6) {
         super(StructurePieceType.OCEAN_RUIN, 0, $$0, $$1, $$1.toString(), makeSettings($$3, $$4, $$5), $$2);
         this.integrity = $$4;
         this.biomeType = $$5;
         this.isLarge = $$6;
      }

      private OceanRuinPiece(StructureTemplateManager $$0, CompoundTag $$1, Rotation $$2, float $$3, OceanRuinStructure.Type $$4, boolean $$5) {
         super(StructurePieceType.OCEAN_RUIN, $$1, $$0, $$3x -> makeSettings($$2, $$3, $$4));
         this.integrity = $$3;
         this.biomeType = $$4;
         this.isLarge = $$5;
      }

      private static StructurePlaceSettings makeSettings(Rotation $$0, float $$1, OceanRuinStructure.Type $$2) {
         StructureProcessor $$3 = $$2 == OceanRuinStructure.Type.COLD
            ? OceanRuinPieces.COLD_SUSPICIOUS_BLOCK_PROCESSOR
            : OceanRuinPieces.WARM_SUSPICIOUS_BLOCK_PROCESSOR;
         return new StructurePlaceSettings()
            .setRotation($$0)
            .setMirror(Mirror.NONE)
            .addProcessor(new BlockRotProcessor($$1))
            .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
            .addProcessor($$3);
      }

      public static OceanRuinPieces.OceanRuinPiece create(StructureTemplateManager $$0, CompoundTag $$1) {
         Rotation $$2 = (Rotation)$$1.read("Rot", Rotation.LEGACY_CODEC).orElseThrow();
         float $$3 = $$1.getFloatOr("Integrity", 0.0F);
         OceanRuinStructure.Type $$4 = (OceanRuinStructure.Type)$$1.read("BiomeType", OceanRuinStructure.Type.LEGACY_CODEC).orElseThrow();
         boolean $$5 = $$1.getBooleanOr("IsLarge", false);
         return new OceanRuinPieces.OceanRuinPiece($$0, $$1, $$2, $$3, $$4, $$5);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
         super.addAdditionalSaveData($$0, $$1);
         $$1.store("Rot", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
         $$1.putFloat("Integrity", this.integrity);
         $$1.store("BiomeType", OceanRuinStructure.Type.LEGACY_CODEC, this.biomeType);
         $$1.putBoolean("IsLarge", this.isLarge);
      }

      @Override
      protected void handleDataMarker(String $$0, BlockPos $$1, net.minecraft.world.level.ServerLevelAccessor $$2, RandomSource $$3, BoundingBox $$4) {
         if ("chest".equals($$0)) {
            $$2.setBlock($$1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.WATERLOGGED, $$2.getFluidState($$1).is(FluidTags.WATER)), 2);
            BlockEntity $$5 = $$2.getBlockEntity($$1);
            if ($$5 instanceof ChestBlockEntity) {
               ((ChestBlockEntity)$$5)
                  .setLootTable(this.isLarge ? BuiltInLootTables.UNDERWATER_RUIN_BIG : BuiltInLootTables.UNDERWATER_RUIN_SMALL, $$3.nextLong());
            }
         } else if ("drowned".equals($$0)) {
            Drowned $$6 = (Drowned)EntityType.DROWNED.create($$2.getLevel(), EntitySpawnReason.STRUCTURE);
            if ($$6 != null) {
               $$6.setPersistenceRequired();
               $$6.snapTo($$1, 0.0F, 0.0F);
               $$6.finalizeSpawn($$2, $$2.getCurrentDifficultyAt($$1), EntitySpawnReason.STRUCTURE, null);
               $$2.addFreshEntityWithPassengers($$6);
               if ($$1.getY() > $$2.getSeaLevel()) {
                  $$2.setBlock($$1, Blocks.AIR.defaultBlockState(), 2);
               } else {
                  $$2.setBlock($$1, Blocks.WATER.defaultBlockState(), 2);
               }
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
         int $$7 = $$0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.templatePosition.getX(), this.templatePosition.getZ());
         this.templatePosition = new BlockPos(this.templatePosition.getX(), $$7, this.templatePosition.getZ());
         BlockPos $$8 = StructureTemplate.transform(
               new BlockPos(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1),
               Mirror.NONE,
               this.placeSettings.getRotation(),
               BlockPos.ZERO
            )
            .offset(this.templatePosition);
         this.templatePosition = new BlockPos(this.templatePosition.getX(), this.getHeight(this.templatePosition, $$0, $$8), this.templatePosition.getZ());
         super.postProcess($$0, $$1, $$2, $$3, $$4, $$5, $$6);
      }

      private int getHeight(BlockPos $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
         int $$3 = $$0.getY();
         int $$4 = 512;
         int $$5 = $$3 - 1;
         int $$6 = 0;

         for (BlockPos $$7 : BlockPos.betweenClosed($$0, $$2)) {
            int $$8 = $$7.getX();
            int $$9 = $$7.getZ();
            int $$10 = $$0.getY() - 1;
            MutableBlockPos $$11 = new MutableBlockPos($$8, $$10, $$9);
            BlockState $$12 = $$1.getBlockState($$11);

            for (FluidState $$13 = $$1.getFluidState($$11);
               ($$12.isAir() || $$13.is(FluidTags.WATER) || $$12.is(BlockTags.ICE)) && $$10 > $$1.getMinY() + 1;
               $$13 = $$1.getFluidState($$11)
            ) {
               $$11.set($$8, --$$10, $$9);
               $$12 = $$1.getBlockState($$11);
            }

            $$4 = Math.min($$4, $$10);
            if ($$10 < $$5 - 2) {
               $$6++;
            }
         }

         int $$14 = Math.abs($$0.getX() - $$2.getX());
         if ($$5 - $$4 > 2 && $$6 > $$14 - 2) {
            $$3 = $$4 + 1;
         }

         return $$3;
      }
   }
}
