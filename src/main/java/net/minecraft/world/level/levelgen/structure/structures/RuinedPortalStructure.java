package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalStructure extends Structure {
   private static final String[] STRUCTURE_LOCATION_PORTALS = new String[]{
      "ruined_portal/portal_1",
      "ruined_portal/portal_2",
      "ruined_portal/portal_3",
      "ruined_portal/portal_4",
      "ruined_portal/portal_5",
      "ruined_portal/portal_6",
      "ruined_portal/portal_7",
      "ruined_portal/portal_8",
      "ruined_portal/portal_9",
      "ruined_portal/portal_10"
   };
   private static final String[] STRUCTURE_LOCATION_GIANT_PORTALS = new String[]{
      "ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"
   };
   private static final float PROBABILITY_OF_GIANT_PORTAL = 0.05F;
   private static final int MIN_Y_INDEX = 15;
   private final List<RuinedPortalStructure.Setup> setups;
   public static final MapCodec<RuinedPortalStructure> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            settingsCodec($$0), ExtraCodecs.nonEmptyList(RuinedPortalStructure.Setup.CODEC.listOf()).fieldOf("setups").forGetter($$0x -> $$0x.setups)
         )
         .apply($$0, RuinedPortalStructure::new)
   );

   public RuinedPortalStructure(Structure.StructureSettings $$0, List<RuinedPortalStructure.Setup> $$1) {
      super($$0);
      this.setups = $$1;
   }

   public RuinedPortalStructure(Structure.StructureSettings $$0, RuinedPortalStructure.Setup $$1) {
      this($$0, List.of($$1));
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      RuinedPortalPiece.Properties $$1 = new RuinedPortalPiece.Properties();
      WorldgenRandom $$2 = $$0.random();
      RuinedPortalStructure.Setup $$3 = null;
      if (this.setups.size() > 1) {
         float $$4 = 0.0F;

         for (RuinedPortalStructure.Setup $$5 : this.setups) {
            $$4 += $$5.weight();
         }

         float $$6 = $$2.nextFloat();

         for (RuinedPortalStructure.Setup $$7 : this.setups) {
            $$6 -= $$7.weight() / $$4;
            if ($$6 < 0.0F) {
               $$3 = $$7;
               break;
            }
         }
      } else {
         $$3 = this.setups.get(0);
      }

      if ($$3 == null) {
         throw new IllegalStateException();
      } else {
         RuinedPortalStructure.Setup $$8 = $$3;
         $$1.airPocket = sample($$2, $$8.airPocketProbability());
         $$1.mossiness = $$8.mossiness();
         $$1.overgrown = $$8.overgrown();
         $$1.vines = $$8.vines();
         $$1.replaceWithBlackstone = $$8.replaceWithBlackstone();
         Identifier $$9;
         if ($$2.nextFloat() < 0.05F) {
            $$9 = Identifier.withDefaultNamespace(STRUCTURE_LOCATION_GIANT_PORTALS[$$2.nextInt(STRUCTURE_LOCATION_GIANT_PORTALS.length)]);
         } else {
            $$9 = Identifier.withDefaultNamespace(STRUCTURE_LOCATION_PORTALS[$$2.nextInt(STRUCTURE_LOCATION_PORTALS.length)]);
         }

         StructureTemplate $$11 = $$0.structureTemplateManager().getOrCreate($$9);
         Rotation $$12 = (Rotation)Util.getRandom(Rotation.values(), $$2);
         Mirror $$13 = $$2.nextFloat() < 0.5F ? Mirror.NONE : Mirror.FRONT_BACK;
         BlockPos $$14 = new BlockPos($$11.getSize().getX() / 2, 0, $$11.getSize().getZ() / 2);
         ChunkGenerator $$15 = $$0.chunkGenerator();
         net.minecraft.world.level.LevelHeightAccessor $$16 = $$0.heightAccessor();
         RandomState $$17 = $$0.randomState();
         BlockPos $$18 = $$0.chunkPos().getWorldPosition();
         BoundingBox $$19 = $$11.getBoundingBox($$18, $$12, $$14, $$13);
         BlockPos $$20 = $$19.getCenter();
         int $$21 = $$15.getBaseHeight($$20.getX(), $$20.getZ(), RuinedPortalPiece.getHeightMapType($$8.placement()), $$16, $$17) - 1;
         int $$22 = findSuitableY($$2, $$15, $$8.placement(), $$1.airPocket, $$21, $$19.getYSpan(), $$19, $$16, $$17);
         BlockPos $$23 = new BlockPos($$18.getX(), $$22, $$18.getZ());
         return Optional.of(
            new Structure.GenerationStub(
               $$23,
               (Consumer<StructurePiecesBuilder>)($$11x -> {
                  if ($$8.canBeCold()) {
                     $$1.cold = isCold(
                        $$23,
                        $$0.chunkGenerator()
                           .getBiomeSource()
                           .getNoiseBiome(QuartPos.fromBlock($$23.getX()), QuartPos.fromBlock($$23.getY()), QuartPos.fromBlock($$23.getZ()), $$17.sampler()),
                        $$15.getSeaLevel()
                     );
                  }

                  $$11x.addPiece(new RuinedPortalPiece($$0.structureTemplateManager(), $$23, $$8.placement(), $$1, $$9, $$11, $$12, $$13, $$14));
               })
            )
         );
      }
   }

   private static boolean sample(WorldgenRandom $$0, float $$1) {
      if ($$1 == 0.0F) {
         return false;
      } else {
         return $$1 == 1.0F ? true : $$0.nextFloat() < $$1;
      }
   }

   private static boolean isCold(BlockPos $$0, Holder<Biome> $$1, int $$2) {
      return ((Biome)$$1.value()).coldEnoughToSnow($$0, $$2);
   }

   private static int findSuitableY(
      RandomSource $$0,
      ChunkGenerator $$1,
      RuinedPortalPiece.VerticalPlacement $$2,
      boolean $$3,
      int $$4,
      int $$5,
      BoundingBox $$6,
      net.minecraft.world.level.LevelHeightAccessor $$7,
      RandomState $$8
   ) {
      int $$9 = $$7.getMinY() + 15;
      int $$10;
      if ($$2 == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
         if ($$3) {
            $$10 = Mth.randomBetweenInclusive($$0, 32, 100);
         } else if ($$0.nextFloat() < 0.5F) {
            $$10 = Mth.randomBetweenInclusive($$0, 27, 29);
         } else {
            $$10 = Mth.randomBetweenInclusive($$0, 29, 100);
         }
      } else if ($$2 == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
         int $$13 = $$4 - $$5;
         $$10 = getRandomWithinInterval($$0, 70, $$13);
      } else if ($$2 == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
         int $$15 = $$4 - $$5;
         $$10 = getRandomWithinInterval($$0, $$9, $$15);
      } else if ($$2 == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED) {
         $$10 = $$4 - $$5 + Mth.randomBetweenInclusive($$0, 2, 8);
      } else {
         $$10 = $$4;
      }

      List<BlockPos> $$19 = ImmutableList.of(
         new BlockPos($$6.minX(), 0, $$6.minZ()),
         new BlockPos($$6.maxX(), 0, $$6.minZ()),
         new BlockPos($$6.minX(), 0, $$6.maxZ()),
         new BlockPos($$6.maxX(), 0, $$6.maxZ())
      );
      List<net.minecraft.world.level.NoiseColumn> $$20 = $$19.stream()
         .map($$3x -> $$1.getBaseColumn($$3x.getX(), $$3x.getZ(), $$7, $$8))
         .collect(Collectors.toList());
      Heightmap.Types $$21 = $$2 == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;

      int $$22;
      for ($$22 = $$10; $$22 > $$9; $$22--) {
         int $$23 = 0;

         for (net.minecraft.world.level.NoiseColumn $$24 : $$20) {
            BlockState $$25 = $$24.getBlock($$22);
            if ($$21.isOpaque().test($$25)) {
               if (++$$23 == 3) {
                  return $$22;
               }
            }
         }
      }

      return $$22;
   }

   private static int getRandomWithinInterval(RandomSource $$0, int $$1, int $$2) {
      return $$1 < $$2 ? Mth.randomBetweenInclusive($$0, $$1, $$2) : $$2;
   }

   @Override
   public StructureType<?> type() {
      return StructureType.RUINED_PORTAL;
   }

   public record Setup(
      RuinedPortalPiece.VerticalPlacement placement,
      float airPocketProbability,
      float mossiness,
      boolean overgrown,
      boolean vines,
      boolean canBeCold,
      boolean replaceWithBlackstone,
      float weight
   ) {
      public static final Codec<RuinedPortalStructure.Setup> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               RuinedPortalPiece.VerticalPlacement.CODEC.fieldOf("placement").forGetter(RuinedPortalStructure.Setup::placement),
               Codec.floatRange(0.0F, 1.0F).fieldOf("air_pocket_probability").forGetter(RuinedPortalStructure.Setup::airPocketProbability),
               Codec.floatRange(0.0F, 1.0F).fieldOf("mossiness").forGetter(RuinedPortalStructure.Setup::mossiness),
               Codec.BOOL.fieldOf("overgrown").forGetter(RuinedPortalStructure.Setup::overgrown),
               Codec.BOOL.fieldOf("vines").forGetter(RuinedPortalStructure.Setup::vines),
               Codec.BOOL.fieldOf("can_be_cold").forGetter(RuinedPortalStructure.Setup::canBeCold),
               Codec.BOOL.fieldOf("replace_with_blackstone").forGetter(RuinedPortalStructure.Setup::replaceWithBlackstone),
               ExtraCodecs.POSITIVE_FLOAT.fieldOf("weight").forGetter(RuinedPortalStructure.Setup::weight)
            )
            .apply($$0, RuinedPortalStructure.Setup::new)
      );
   }
}
