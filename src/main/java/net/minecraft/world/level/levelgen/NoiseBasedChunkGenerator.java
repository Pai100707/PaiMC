package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
   public static final MapCodec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter($$0x -> $$0x.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter($$0x -> $$0x.settings)
         )
         .apply($$0, $$0.stable(NoiseBasedChunkGenerator::new))
   );
   private static final BlockState AIR = Blocks.AIR.defaultBlockState();
   private final Holder<NoiseGeneratorSettings> settings;
   private final Supplier<Aquifer.FluidPicker> globalFluidPicker;

   public NoiseBasedChunkGenerator(BiomeSource $$0, Holder<NoiseGeneratorSettings> $$1) {
      super($$0);
      this.settings = $$1;
      this.globalFluidPicker = Suppliers.memoize(() -> createFluidPicker((NoiseGeneratorSettings)$$1.value()));
   }

   private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings $$0) {
      Aquifer.FluidStatus $$1 = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
      int $$2 = $$0.seaLevel();
      Aquifer.FluidStatus $$3 = new Aquifer.FluidStatus($$2, $$0.defaultFluid());
      Aquifer.FluidStatus $$4 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
      return ($$4x, $$5, $$6) -> {
         if (SharedConstants.DEBUG_DISABLE_FLUID_GENERATION) {
            return $$4;
         } else {
            return $$5 < Math.min(-54, $$2) ? $$1 : $$3;
         }
      };
   }

   @Override
   public CompletableFuture<ChunkAccess> createBiomes(RandomState $$0, Blender $$1, net.minecraft.world.level.StructureManager $$2, ChunkAccess $$3) {
      return CompletableFuture.supplyAsync(() -> {
         this.doCreateBiomes($$1, $$0, $$2, $$3);
         return $$3;
      }, Util.backgroundExecutor().forName("init_biomes"));
   }

   private void doCreateBiomes(Blender $$0, RandomState $$1, net.minecraft.world.level.StructureManager $$2, ChunkAccess $$3) {
      NoiseChunk $$4 = $$3.getOrCreateNoiseChunk($$3x -> this.createNoiseChunk($$3x, $$2, $$0, $$1));
      BiomeResolver $$5 = BelowZeroRetrogen.getBiomeResolver($$0.getBiomeResolver(this.biomeSource), $$3);
      $$3.fillBiomesFromNoise($$5, $$4.cachedClimateSampler($$1.router(), ((NoiseGeneratorSettings)this.settings.value()).spawnTarget()));
   }

   private NoiseChunk createNoiseChunk(ChunkAccess $$0, net.minecraft.world.level.StructureManager $$1, Blender $$2, RandomState $$3) {
      return NoiseChunk.forChunk(
         $$0, $$3, Beardifier.forStructuresInChunk($$1, $$0.getPos()), (NoiseGeneratorSettings)this.settings.value(), this.globalFluidPicker.get(), $$2
      );
   }

   @Override
   protected MapCodec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   public Holder<NoiseGeneratorSettings> generatorSettings() {
      return this.settings;
   }

   public boolean stable(ResourceKey<NoiseGeneratorSettings> $$0) {
      return this.settings.is($$0);
   }

   @Override
   public int getBaseHeight(int $$0, int $$1, Heightmap.Types $$2, net.minecraft.world.level.LevelHeightAccessor $$3, RandomState $$4) {
      return this.iterateNoiseColumn($$3, $$4, $$0, $$1, null, $$2.isOpaque()).orElse($$3.getMinY());
   }

   @Override
   public net.minecraft.world.level.NoiseColumn getBaseColumn(int $$0, int $$1, net.minecraft.world.level.LevelHeightAccessor $$2, RandomState $$3) {
      MutableObject<net.minecraft.world.level.NoiseColumn> $$4 = new MutableObject();
      this.iterateNoiseColumn($$2, $$3, $$0, $$1, $$4, null);
      return (net.minecraft.world.level.NoiseColumn)$$4.get();
   }

   @Override
   public void addDebugScreenInfo(List<String> $$0, RandomState $$1, BlockPos $$2) {
      DecimalFormat $$3 = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.ROOT));
      NoiseRouter $$4 = $$1.router();
      DensityFunction.SinglePointContext $$5 = new DensityFunction.SinglePointContext($$2.getX(), $$2.getY(), $$2.getZ());
      double $$6 = $$4.ridges().compute($$5);
      $$0.add(
         "NoiseRouter T: "
            + $$3.format($$4.temperature().compute($$5))
            + " V: "
            + $$3.format($$4.vegetation().compute($$5))
            + " C: "
            + $$3.format($$4.continents().compute($$5))
            + " E: "
            + $$3.format($$4.erosion().compute($$5))
            + " D: "
            + $$3.format($$4.depth().compute($$5))
            + " W: "
            + $$3.format($$6)
            + " PV: "
            + $$3.format(NoiseRouterData.peaksAndValleys((float)$$6))
            + " PS: "
            + $$3.format($$4.preliminarySurfaceLevel().compute($$5))
            + " N: "
            + $$3.format($$4.finalDensity().compute($$5))
      );
   }

   private OptionalInt iterateNoiseColumn(
      net.minecraft.world.level.LevelHeightAccessor $$0,
      RandomState $$1,
      int $$2,
      int $$3,
      @Nullable MutableObject<net.minecraft.world.level.NoiseColumn> $$4,
      @Nullable Predicate<BlockState> $$5
   ) {
      NoiseSettings $$6 = ((NoiseGeneratorSettings)this.settings.value()).noiseSettings().clampToHeightAccessor($$0);
      int $$7 = $$6.getCellHeight();
      int $$8 = $$6.minY();
      int $$9 = Mth.floorDiv($$8, $$7);
      int $$10 = Mth.floorDiv($$6.height(), $$7);
      if ($$10 <= 0) {
         return OptionalInt.empty();
      } else {
         BlockState[] $$11;
         if ($$4 == null) {
            $$11 = null;
         } else {
            $$11 = new BlockState[$$6.height()];
            $$4.setValue(new net.minecraft.world.level.NoiseColumn($$8, $$11));
         }

         int $$13 = $$6.getCellWidth();
         int $$14 = Math.floorDiv($$2, $$13);
         int $$15 = Math.floorDiv($$3, $$13);
         int $$16 = Math.floorMod($$2, $$13);
         int $$17 = Math.floorMod($$3, $$13);
         int $$18 = $$14 * $$13;
         int $$19 = $$15 * $$13;
         double $$20 = (double)$$16 / $$13;
         double $$21 = (double)$$17 / $$13;
         NoiseChunk $$22 = new NoiseChunk(
            1,
            $$1,
            $$18,
            $$19,
            $$6,
            DensityFunctions.BeardifierMarker.INSTANCE,
            (NoiseGeneratorSettings)this.settings.value(),
            this.globalFluidPicker.get(),
            Blender.empty()
         );
         $$22.initializeForFirstCellX();
         $$22.advanceCellX(0);

         for (int $$23 = $$10 - 1; $$23 >= 0; $$23--) {
            $$22.selectCellYZ($$23, 0);

            for (int $$24 = $$7 - 1; $$24 >= 0; $$24--) {
               int $$25 = ($$9 + $$23) * $$7 + $$24;
               double $$26 = (double)$$24 / $$7;
               $$22.updateForY($$25, $$26);
               $$22.updateForX($$2, $$20);
               $$22.updateForZ($$3, $$21);
               BlockState $$27 = $$22.getInterpolatedState();
               BlockState $$28 = $$27 == null ? ((NoiseGeneratorSettings)this.settings.value()).defaultBlock() : $$27;
               if ($$11 != null) {
                  int $$29 = $$23 * $$7 + $$24;
                  $$11[$$29] = $$28;
               }

               if ($$5 != null && $$5.test($$28)) {
                  $$22.stopInterpolation();
                  return OptionalInt.of($$25 + 1);
               }
            }
         }

         $$22.stopInterpolation();
         return OptionalInt.empty();
      }
   }

   @Override
   public void buildSurface(WorldGenRegion $$0, net.minecraft.world.level.StructureManager $$1, RandomState $$2, ChunkAccess $$3) {
      if (!SharedConstants.debugVoidTerrain($$3.getPos()) && !SharedConstants.DEBUG_DISABLE_SURFACE) {
         WorldGenerationContext $$4 = new WorldGenerationContext(this, $$0);
         this.buildSurface($$3, $$4, $$2, $$1, $$0.getBiomeManager(), $$0.registryAccess().lookupOrThrow(Registries.BIOME), Blender.of($$0));
      }
   }

   @VisibleForTesting
   public void buildSurface(
      ChunkAccess $$0,
      WorldGenerationContext $$1,
      RandomState $$2,
      net.minecraft.world.level.StructureManager $$3,
      BiomeManager $$4,
      Registry<Biome> $$5,
      Blender $$6
   ) {
      NoiseChunk $$7 = $$0.getOrCreateNoiseChunk($$3x -> this.createNoiseChunk($$3x, $$3, $$6, $$2));
      NoiseGeneratorSettings $$8 = (NoiseGeneratorSettings)this.settings.value();
      $$2.surfaceSystem().buildSurface($$2, $$4, $$5, $$8.useLegacyRandomSource(), $$1, $$0, $$7, $$8.surfaceRule());
   }

   @Override
   public void applyCarvers(WorldGenRegion $$0, long $$1, RandomState $$2, BiomeManager $$3, net.minecraft.world.level.StructureManager $$4, ChunkAccess $$5) {
      if (!SharedConstants.DEBUG_DISABLE_CARVERS) {
         BiomeManager $$6 = $$3.withDifferentSource(($$1x, $$2x, $$3x) -> this.biomeSource.getNoiseBiome($$1x, $$2x, $$3x, $$2.sampler()));
         WorldgenRandom $$7 = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
         int $$8 = 8;
         net.minecraft.world.level.ChunkPos $$9 = $$5.getPos();
         NoiseChunk $$10 = $$5.getOrCreateNoiseChunk($$3x -> this.createNoiseChunk($$3x, $$4, Blender.of($$0), $$2));
         Aquifer $$11 = $$10.aquifer();
         CarvingContext $$12 = new CarvingContext(
            this, $$0.registryAccess(), $$5.getHeightAccessorForGeneration(), $$10, $$2, ((NoiseGeneratorSettings)this.settings.value()).surfaceRule()
         );
         CarvingMask $$13 = ((ProtoChunk)$$5).getOrCreateCarvingMask();

         for (int $$14 = -8; $$14 <= 8; $$14++) {
            for (int $$15 = -8; $$15 <= 8; $$15++) {
               net.minecraft.world.level.ChunkPos $$16 = new net.minecraft.world.level.ChunkPos($$9.x + $$14, $$9.z + $$15);
               ChunkAccess $$17 = $$0.getChunk($$16.x, $$16.z);
               BiomeGenerationSettings $$18 = $$17.carverBiome(
                  () -> this.getBiomeGenerationSettings(
                     this.biomeSource.getNoiseBiome(QuartPos.fromBlock($$16.getMinBlockX()), 0, QuartPos.fromBlock($$16.getMinBlockZ()), $$2.sampler())
                  )
               );
               Iterable<Holder<ConfiguredWorldCarver<?>>> $$19 = $$18.getCarvers();
               int $$20 = 0;

               for (Holder<ConfiguredWorldCarver<?>> $$21 : $$19) {
                  ConfiguredWorldCarver<?> $$22 = (ConfiguredWorldCarver<?>)$$21.value();
                  $$7.setLargeFeatureSeed($$1 + $$20, $$16.x, $$16.z);
                  if ($$22.isStartChunk($$7)) {
                     $$22.carve($$12, $$5, $$6::getBiome, $$7, $$11, $$16, $$13);
                  }

                  $$20++;
               }
            }
         }
      }
   }

   @Override
   public CompletableFuture<ChunkAccess> fillFromNoise(Blender $$0, RandomState $$1, net.minecraft.world.level.StructureManager $$2, ChunkAccess $$3) {
      NoiseSettings $$4 = ((NoiseGeneratorSettings)this.settings.value()).noiseSettings().clampToHeightAccessor($$3.getHeightAccessorForGeneration());
      int $$5 = $$4.minY();
      int $$6 = Mth.floorDiv($$5, $$4.getCellHeight());
      int $$7 = Mth.floorDiv($$4.height(), $$4.getCellHeight());
      return $$7 <= 0 ? CompletableFuture.completedFuture($$3) : CompletableFuture.supplyAsync(() -> {
         int $$8 = $$3.getSectionIndex($$7 * $$4.getCellHeight() - 1 + $$5);
         int $$9 = $$3.getSectionIndex($$5);
         Set<LevelChunkSection> $$10 = Sets.newHashSet();

         for (int $$11 = $$8; $$11 >= $$9; $$11--) {
            LevelChunkSection $$12 = $$3.getSection($$11);
            $$12.acquire();
            $$10.add($$12);
         }

         ChunkAccess var20;
         try {
            var20 = this.doFill($$0, $$2, $$1, $$3, $$6, $$7);
         } finally {
            for (LevelChunkSection $$14 : $$10) {
               $$14.release();
            }
         }

         return var20;
      }, Util.backgroundExecutor().forName("wgen_fill_noise"));
   }

   private ChunkAccess doFill(Blender $$0, net.minecraft.world.level.StructureManager $$1, RandomState $$2, ChunkAccess $$3, int $$4, int $$5) {
      NoiseChunk $$6 = $$3.getOrCreateNoiseChunk($$3x -> this.createNoiseChunk($$3x, $$1, $$0, $$2));
      Heightmap $$7 = $$3.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
      Heightmap $$8 = $$3.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
      net.minecraft.world.level.ChunkPos $$9 = $$3.getPos();
      int $$10 = $$9.getMinBlockX();
      int $$11 = $$9.getMinBlockZ();
      Aquifer $$12 = $$6.aquifer();
      $$6.initializeForFirstCellX();
      MutableBlockPos $$13 = new MutableBlockPos();
      int $$14 = $$6.cellWidth();
      int $$15 = $$6.cellHeight();
      int $$16 = 16 / $$14;
      int $$17 = 16 / $$14;

      for (int $$18 = 0; $$18 < $$16; $$18++) {
         $$6.advanceCellX($$18);

         for (int $$19 = 0; $$19 < $$17; $$19++) {
            int $$20 = $$3.getSectionsCount() - 1;
            LevelChunkSection $$21 = $$3.getSection($$20);

            for (int $$22 = $$5 - 1; $$22 >= 0; $$22--) {
               $$6.selectCellYZ($$22, $$19);

               for (int $$23 = $$15 - 1; $$23 >= 0; $$23--) {
                  int $$24 = ($$4 + $$22) * $$15 + $$23;
                  int $$25 = $$24 & 15;
                  int $$26 = $$3.getSectionIndex($$24);
                  if ($$20 != $$26) {
                     $$20 = $$26;
                     $$21 = $$3.getSection($$26);
                  }

                  double $$27 = (double)$$23 / $$15;
                  $$6.updateForY($$24, $$27);

                  for (int $$28 = 0; $$28 < $$14; $$28++) {
                     int $$29 = $$10 + $$18 * $$14 + $$28;
                     int $$30 = $$29 & 15;
                     double $$31 = (double)$$28 / $$14;
                     $$6.updateForX($$29, $$31);

                     for (int $$32 = 0; $$32 < $$14; $$32++) {
                        int $$33 = $$11 + $$19 * $$14 + $$32;
                        int $$34 = $$33 & 15;
                        double $$35 = (double)$$32 / $$14;
                        $$6.updateForZ($$33, $$35);
                        BlockState $$36 = $$6.getInterpolatedState();
                        if ($$36 == null) {
                           $$36 = ((NoiseGeneratorSettings)this.settings.value()).defaultBlock();
                        }

                        $$36 = this.debugPreliminarySurfaceLevel($$6, $$29, $$24, $$33, $$36);
                        if ($$36 != AIR && !SharedConstants.debugVoidTerrain($$3.getPos())) {
                           $$21.setBlockState($$30, $$25, $$34, $$36, false);
                           $$7.update($$30, $$24, $$34, $$36);
                           $$8.update($$30, $$24, $$34, $$36);
                           if ($$12.shouldScheduleFluidUpdate() && !$$36.getFluidState().isEmpty()) {
                              $$13.set($$29, $$24, $$33);
                              $$3.markPosForPostprocessing($$13);
                           }
                        }
                     }
                  }
               }
            }
         }

         $$6.swapSlices();
      }

      $$6.stopInterpolation();
      return $$3;
   }

   private BlockState debugPreliminarySurfaceLevel(NoiseChunk $$0, int $$1, int $$2, int $$3, BlockState $$4) {
      if (SharedConstants.DEBUG_AQUIFERS && $$3 >= 0 && $$3 % 4 == 0) {
         int $$5 = $$0.preliminarySurfaceLevel($$1, $$3);
         int $$6 = $$5 + 8;
         if ($$2 == $$6) {
            $$4 = $$6 < this.getSeaLevel() ? Blocks.SLIME_BLOCK.defaultBlockState() : Blocks.HONEY_BLOCK.defaultBlockState();
         }
      }

      return $$4;
   }

   @Override
   public int getGenDepth() {
      return ((NoiseGeneratorSettings)this.settings.value()).noiseSettings().height();
   }

   @Override
   public int getSeaLevel() {
      return ((NoiseGeneratorSettings)this.settings.value()).seaLevel();
   }

   @Override
   public int getMinY() {
      return ((NoiseGeneratorSettings)this.settings.value()).noiseSettings().minY();
   }

   @Override
   public void spawnOriginalMobs(WorldGenRegion $$0) {
      if (!((NoiseGeneratorSettings)this.settings.value()).disableMobGeneration()) {
         net.minecraft.world.level.ChunkPos $$1 = $$0.getCenter();
         Holder<Biome> $$2 = $$0.getBiome($$1.getWorldPosition().atY($$0.getMaxY()));
         WorldgenRandom $$3 = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
         $$3.setDecorationSeed($$0.getSeed(), $$1.getMinBlockX(), $$1.getMinBlockZ());
         net.minecraft.world.level.NaturalSpawner.spawnMobsForChunkGeneration($$0, $$2, $$1, $$3);
      }
   }
}
