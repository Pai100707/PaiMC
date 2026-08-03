package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LavaSubmergedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class RuinedPortalPiece extends TemplateStructurePiece {
   private static final float PROBABILITY_OF_GOLD_GONE = 0.3F;
   private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_NETHERRACK = 0.07F;
   private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_LAVA = 0.2F;
   private final RuinedPortalPiece.VerticalPlacement verticalPlacement;
   private final RuinedPortalPiece.Properties properties;

   public RuinedPortalPiece(
      StructureTemplateManager $$0,
      BlockPos $$1,
      RuinedPortalPiece.VerticalPlacement $$2,
      RuinedPortalPiece.Properties $$3,
      Identifier $$4,
      StructureTemplate $$5,
      Rotation $$6,
      Mirror $$7,
      BlockPos $$8
   ) {
      super(StructurePieceType.RUINED_PORTAL, 0, $$0, $$4, $$4.toString(), makeSettings($$7, $$6, $$2, $$8, $$3), $$1);
      this.verticalPlacement = $$2;
      this.properties = $$3;
   }

   public RuinedPortalPiece(StructureTemplateManager $$0, CompoundTag $$1) {
      super(StructurePieceType.RUINED_PORTAL, $$1, $$0, $$2 -> makeSettings($$0, $$1, $$2));
      this.verticalPlacement = (RuinedPortalPiece.VerticalPlacement)$$1.read("VerticalPlacement", RuinedPortalPiece.VerticalPlacement.CODEC).orElseThrow();
      this.properties = (RuinedPortalPiece.Properties)$$1.read("Properties", RuinedPortalPiece.Properties.CODEC).orElseThrow();
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
      super.addAdditionalSaveData($$0, $$1);
      $$1.store("Rotation", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
      $$1.store("Mirror", Mirror.LEGACY_CODEC, this.placeSettings.getMirror());
      $$1.store("VerticalPlacement", RuinedPortalPiece.VerticalPlacement.CODEC, this.verticalPlacement);
      $$1.store("Properties", RuinedPortalPiece.Properties.CODEC, this.properties);
   }

   private static StructurePlaceSettings makeSettings(StructureTemplateManager $$0, CompoundTag $$1, Identifier $$2) {
      StructureTemplate $$3 = $$0.getOrCreate($$2);
      BlockPos $$4 = new BlockPos($$3.getSize().getX() / 2, 0, $$3.getSize().getZ() / 2);
      return makeSettings(
         (Mirror)$$1.read("Mirror", Mirror.LEGACY_CODEC).orElseThrow(),
         (Rotation)$$1.read("Rotation", Rotation.LEGACY_CODEC).orElseThrow(),
         (RuinedPortalPiece.VerticalPlacement)$$1.read("VerticalPlacement", RuinedPortalPiece.VerticalPlacement.CODEC).orElseThrow(),
         $$4,
         (RuinedPortalPiece.Properties)RuinedPortalPiece.Properties.CODEC.parse(new Dynamic(NbtOps.INSTANCE, $$1.get("Properties"))).getPartialOrThrow()
      );
   }

   private static StructurePlaceSettings makeSettings(
      Mirror $$0, Rotation $$1, RuinedPortalPiece.VerticalPlacement $$2, BlockPos $$3, RuinedPortalPiece.Properties $$4
   ) {
      BlockIgnoreProcessor $$5 = $$4.airPocket ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
      List<ProcessorRule> $$6 = Lists.newArrayList();
      $$6.add(getBlockReplaceRule(Blocks.GOLD_BLOCK, 0.3F, Blocks.AIR));
      $$6.add(getLavaProcessorRule($$2, $$4));
      if (!$$4.cold) {
         $$6.add(getBlockReplaceRule(Blocks.NETHERRACK, 0.07F, Blocks.MAGMA_BLOCK));
      }

      StructurePlaceSettings $$7 = new StructurePlaceSettings()
         .setRotation($$1)
         .setMirror($$0)
         .setRotationPivot($$3)
         .addProcessor($$5)
         .addProcessor(new RuleProcessor($$6))
         .addProcessor(new BlockAgeProcessor($$4.mossiness))
         .addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE))
         .addProcessor(new LavaSubmergedBlockProcessor());
      if ($$4.replaceWithBlackstone) {
         $$7.addProcessor(BlackstoneReplaceProcessor.INSTANCE);
      }

      return $$7;
   }

   private static ProcessorRule getLavaProcessorRule(RuinedPortalPiece.VerticalPlacement $$0, RuinedPortalPiece.Properties $$1) {
      if ($$0 == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR) {
         return getBlockReplaceRule(Blocks.LAVA, Blocks.MAGMA_BLOCK);
      } else {
         return $$1.cold ? getBlockReplaceRule(Blocks.LAVA, Blocks.NETHERRACK) : getBlockReplaceRule(Blocks.LAVA, 0.2F, Blocks.MAGMA_BLOCK);
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
      BoundingBox $$7 = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
      if ($$4.isInside($$7.getCenter())) {
         $$4.encapsulate($$7);
         super.postProcess($$0, $$1, $$2, $$3, $$4, $$5, $$6);
         this.spreadNetherrack($$3, $$0);
         this.addNetherrackDripColumnsBelowPortal($$3, $$0);
         if (this.properties.vines || this.properties.overgrown) {
            BlockPos.betweenClosedStream(this.getBoundingBox()).forEach($$2x -> {
               if (this.properties.vines) {
                  this.maybeAddVines($$3, $$0, $$2x);
               }

               if (this.properties.overgrown) {
                  this.maybeAddLeavesAbove($$3, $$0, $$2x);
               }
            });
         }
      }
   }

   @Override
   protected void handleDataMarker(String $$0, BlockPos $$1, net.minecraft.world.level.ServerLevelAccessor $$2, RandomSource $$3, BoundingBox $$4) {
   }

   private void maybeAddVines(RandomSource $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2) {
      BlockState $$3 = $$1.getBlockState($$2);
      if (!$$3.isAir() && !$$3.is(Blocks.VINE)) {
         Direction $$4 = getRandomHorizontalDirection($$0);
         BlockPos $$5 = $$2.relative($$4);
         BlockState $$6 = $$1.getBlockState($$5);
         if ($$6.isAir()) {
            if (Block.isFaceFull($$3.getCollisionShape($$1, $$2), $$4)) {
               BooleanProperty $$7 = VineBlock.getPropertyForFace($$4.getOpposite());
               $$1.setBlock($$5, Blocks.VINE.defaultBlockState().setValue($$7, true), 3);
            }
         }
      }
   }

   private void maybeAddLeavesAbove(RandomSource $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2) {
      if ($$0.nextFloat() < 0.5F && $$1.getBlockState($$2).is(Blocks.NETHERRACK) && $$1.getBlockState($$2.above()).isAir()) {
         $$1.setBlock($$2.above(), Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 3);
      }
   }

   private void addNetherrackDripColumnsBelowPortal(RandomSource $$0, net.minecraft.world.level.LevelAccessor $$1) {
      for (int $$2 = this.boundingBox.minX() + 1; $$2 < this.boundingBox.maxX(); $$2++) {
         for (int $$3 = this.boundingBox.minZ() + 1; $$3 < this.boundingBox.maxZ(); $$3++) {
            BlockPos $$4 = new BlockPos($$2, this.boundingBox.minY(), $$3);
            if ($$1.getBlockState($$4).is(Blocks.NETHERRACK)) {
               this.addNetherrackDripColumn($$0, $$1, $$4.below());
            }
         }
      }
   }

   private void addNetherrackDripColumn(RandomSource $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2) {
      MutableBlockPos $$3 = $$2.mutable();
      this.placeNetherrackOrMagma($$0, $$1, $$3);
      int $$4 = 8;

      while ($$4 > 0 && $$0.nextFloat() < 0.5F) {
         $$3.move(Direction.DOWN);
         $$4--;
         this.placeNetherrackOrMagma($$0, $$1, $$3);
      }
   }

   private void spreadNetherrack(RandomSource $$0, net.minecraft.world.level.LevelAccessor $$1) {
      boolean $$2 = this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE
         || this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
      BlockPos $$3 = this.boundingBox.getCenter();
      int $$4 = $$3.getX();
      int $$5 = $$3.getZ();
      float[] $$6 = new float[]{1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.9F, 0.9F, 0.8F, 0.7F, 0.6F, 0.4F, 0.2F};
      int $$7 = $$6.length;
      int $$8 = (this.boundingBox.getXSpan() + this.boundingBox.getZSpan()) / 2;
      int $$9 = $$0.nextInt(Math.max(1, 8 - $$8 / 2));
      int $$10 = 3;
      MutableBlockPos $$11 = BlockPos.ZERO.mutable();

      for (int $$12 = $$4 - $$7; $$12 <= $$4 + $$7; $$12++) {
         for (int $$13 = $$5 - $$7; $$13 <= $$5 + $$7; $$13++) {
            int $$14 = Math.abs($$12 - $$4) + Math.abs($$13 - $$5);
            int $$15 = Math.max(0, $$14 + $$9);
            if ($$15 < $$7) {
               float $$16 = $$6[$$15];
               if ($$0.nextDouble() < $$16) {
                  int $$17 = getSurfaceY($$1, $$12, $$13, this.verticalPlacement);
                  int $$18 = $$2 ? $$17 : Math.min(this.boundingBox.minY(), $$17);
                  $$11.set($$12, $$18, $$13);
                  if (Math.abs($$18 - this.boundingBox.minY()) <= 3 && this.canBlockBeReplacedByNetherrackOrMagma($$1, $$11)) {
                     this.placeNetherrackOrMagma($$0, $$1, $$11);
                     if (this.properties.overgrown) {
                        this.maybeAddLeavesAbove($$0, $$1, $$11);
                     }

                     this.addNetherrackDripColumn($$0, $$1, $$11.below());
                  }
               }
            }
         }
      }
   }

   private boolean canBlockBeReplacedByNetherrackOrMagma(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      return !$$2.is(Blocks.AIR)
         && !$$2.is(Blocks.OBSIDIAN)
         && !$$2.is(BlockTags.FEATURES_CANNOT_REPLACE)
         && (this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_NETHER || !$$2.is(Blocks.LAVA));
   }

   private void placeNetherrackOrMagma(RandomSource $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2) {
      if (!this.properties.cold && $$0.nextFloat() < 0.07F) {
         $$1.setBlock($$2, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
      } else {
         $$1.setBlock($$2, Blocks.NETHERRACK.defaultBlockState(), 3);
      }
   }

   private static int getSurfaceY(net.minecraft.world.level.LevelAccessor $$0, int $$1, int $$2, RuinedPortalPiece.VerticalPlacement $$3) {
      return $$0.getHeight(getHeightMapType($$3), $$1, $$2) - 1;
   }

   public static Heightmap.Types getHeightMapType(RuinedPortalPiece.VerticalPlacement $$0) {
      return $$0 == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
   }

   private static ProcessorRule getBlockReplaceRule(Block $$0, float $$1, Block $$2) {
      return new ProcessorRule(new RandomBlockMatchTest($$0, $$1), AlwaysTrueTest.INSTANCE, $$2.defaultBlockState());
   }

   private static ProcessorRule getBlockReplaceRule(Block $$0, Block $$1) {
      return new ProcessorRule(new BlockMatchTest($$0), AlwaysTrueTest.INSTANCE, $$1.defaultBlockState());
   }

   public static class Properties {
      public static final Codec<RuinedPortalPiece.Properties> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.BOOL.fieldOf("cold").forGetter($$0x -> $$0x.cold),
               Codec.FLOAT.fieldOf("mossiness").forGetter($$0x -> $$0x.mossiness),
               Codec.BOOL.fieldOf("air_pocket").forGetter($$0x -> $$0x.airPocket),
               Codec.BOOL.fieldOf("overgrown").forGetter($$0x -> $$0x.overgrown),
               Codec.BOOL.fieldOf("vines").forGetter($$0x -> $$0x.vines),
               Codec.BOOL.fieldOf("replace_with_blackstone").forGetter($$0x -> $$0x.replaceWithBlackstone)
            )
            .apply($$0, RuinedPortalPiece.Properties::new)
      );
      public boolean cold;
      public float mossiness;
      public boolean airPocket;
      public boolean overgrown;
      public boolean vines;
      public boolean replaceWithBlackstone;

      public Properties() {
      }

      public Properties(boolean $$0, float $$1, boolean $$2, boolean $$3, boolean $$4, boolean $$5) {
         this.cold = $$0;
         this.mossiness = $$1;
         this.airPocket = $$2;
         this.overgrown = $$3;
         this.vines = $$4;
         this.replaceWithBlackstone = $$5;
      }
   }

   public static enum VerticalPlacement implements StringRepresentable {
      ON_LAND_SURFACE("on_land_surface"),
      PARTLY_BURIED("partly_buried"),
      ON_OCEAN_FLOOR("on_ocean_floor"),
      IN_MOUNTAIN("in_mountain"),
      UNDERGROUND("underground"),
      IN_NETHER("in_nether");

      public static final Codec<RuinedPortalPiece.VerticalPlacement> CODEC = StringRepresentable.fromEnum(RuinedPortalPiece.VerticalPlacement::values);
      private final String name;

      private VerticalPlacement(final String $$0) {
         this.name = $$0;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
