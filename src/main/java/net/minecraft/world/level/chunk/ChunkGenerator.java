package net.minecraft.world.level.chunk;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.FeatureCountTracker;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class ChunkGenerator {
   public static final Codec<ChunkGenerator> CODEC = BuiltInRegistries.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
   protected final BiomeSource biomeSource;
   private final Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep;
   private final Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter;

   public ChunkGenerator(BiomeSource $$0) {
      this($$0, $$0x -> ((Biome)$$0x.value()).getGenerationSettings());
   }

   public ChunkGenerator(BiomeSource $$0, Function<Holder<Biome>, BiomeGenerationSettings> $$1) {
      this.biomeSource = $$0;
      this.generationSettingsGetter = $$1;
      this.featuresPerStep = Suppliers.memoize(
         () -> FeatureSorter.buildFeaturesPerStep(List.copyOf($$0.possibleBiomes()), $$1xx -> $$1.apply($$1xx).features(), true)
      );
   }

   public void validate() {
      this.featuresPerStep.get();
   }

   protected abstract MapCodec<? extends ChunkGenerator> codec();

   public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> $$0, RandomState $$1, long $$2) {
      return ChunkGeneratorStructureState.createForNormal($$1, $$2, this.biomeSource, $$0);
   }

   public Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
      return BuiltInRegistries.CHUNK_GENERATOR.getResourceKey(this.codec());
   }

   public CompletableFuture<ChunkAccess> createBiomes(RandomState $$0, Blender $$1, net.minecraft.world.level.StructureManager $$2, ChunkAccess $$3) {
      return CompletableFuture.supplyAsync(() -> {
         $$3.fillBiomesFromNoise(this.biomeSource, $$0.sampler());
         return $$3;
      }, Util.backgroundExecutor().forName("init_biomes"));
   }

   public abstract void applyCarvers(
      WorldGenRegion var1, long var2, RandomState var4, BiomeManager var5, net.minecraft.world.level.StructureManager var6, ChunkAccess var7
   );

   
   public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel $$0, HolderSet<Structure> $$1, BlockPos $$2, int $$3, boolean $$4) {
      if (SharedConstants.DEBUG_DISABLE_FEATURES) {
         return null;
      } else {
         ChunkGeneratorStructureState $$5 = $$0.getChunkSource().getGeneratorState();
         Map<StructurePlacement, Set<Holder<Structure>>> $$6 = new Object2ObjectArrayMap();

         for (Holder<Structure> $$7 : $$1) {
            for (StructurePlacement $$8 : $$5.getPlacementsForStructure($$7)) {
               $$6.computeIfAbsent($$8, $$0x -> new ObjectArraySet()).add($$7);
            }
         }

         if ($$6.isEmpty()) {
            return null;
         } else {
            Pair<BlockPos, Holder<Structure>> $$9 = null;
            double $$10 = Double.MAX_VALUE;
            net.minecraft.world.level.StructureManager $$11 = $$0.structureManager();
            List<Entry<StructurePlacement, Set<Holder<Structure>>>> $$12 = new ArrayList<>($$6.size());

            for (Entry<StructurePlacement, Set<Holder<Structure>>> $$13 : $$6.entrySet()) {
               StructurePlacement $$14 = $$13.getKey();
               if ($$14 instanceof ConcentricRingsStructurePlacement $$15) {
                  Pair<BlockPos, Holder<Structure>> $$16 = this.getNearestGeneratedStructure($$13.getValue(), $$0, $$11, $$2, $$4, $$15);
                  if ($$16 != null) {
                     BlockPos $$17 = (BlockPos)$$16.getFirst();
                     double $$18 = $$2.distSqr($$17);
                     if ($$18 < $$10) {
                        $$10 = $$18;
                        $$9 = $$16;
                     }
                  }
               } else if ($$14 instanceof RandomSpreadStructurePlacement) {
                  $$12.add($$13);
               }
            }

            if (!$$12.isEmpty()) {
               int $$19 = SectionPos.blockToSectionCoord($$2.getX());
               int $$20 = SectionPos.blockToSectionCoord($$2.getZ());

               for (int $$21 = 0; $$21 <= $$3; $$21++) {
                  boolean $$22 = false;

                  for (Entry<StructurePlacement, Set<Holder<Structure>>> $$23 : $$12) {
                     RandomSpreadStructurePlacement $$24 = (RandomSpreadStructurePlacement)$$23.getKey();
                     Pair<BlockPos, Holder<Structure>> $$25 = getNearestGeneratedStructure(
                        $$23.getValue(), $$0, $$11, $$19, $$20, $$21, $$4, $$5.getLevelSeed(), $$24
                     );
                     if ($$25 != null) {
                        $$22 = true;
                        double $$26 = $$2.distSqr((Vec3i)$$25.getFirst());
                        if ($$26 < $$10) {
                           $$10 = $$26;
                           $$9 = $$25;
                        }
                     }
                  }

                  if ($$22) {
                     return $$9;
                  }
               }
            }

            return $$9;
         }
      }
   }

   
   private Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(
      Set<Holder<Structure>> $$0,
      ServerLevel $$1,
      net.minecraft.world.level.StructureManager $$2,
      BlockPos $$3,
      boolean $$4,
      ConcentricRingsStructurePlacement $$5
   ) {
      List<net.minecraft.world.level.ChunkPos> $$6 = $$1.getChunkSource().getGeneratorState().getRingPositionsFor($$5);
      if ($$6 == null) {
         throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
      } else {
         Pair<BlockPos, Holder<Structure>> $$7 = null;
         double $$8 = Double.MAX_VALUE;
         MutableBlockPos $$9 = new MutableBlockPos();

         for (net.minecraft.world.level.ChunkPos $$10 : $$6) {
            $$9.set(SectionPos.sectionToBlockCoord($$10.x, 8), 32, SectionPos.sectionToBlockCoord($$10.z, 8));
            double $$11 = $$9.distSqr($$3);
            boolean $$12 = $$7 == null || $$11 < $$8;
            if ($$12) {
               Pair<BlockPos, Holder<Structure>> $$13 = getStructureGeneratingAt($$0, $$1, $$2, $$4, $$5, $$10);
               if ($$13 != null) {
                  $$7 = $$13;
                  $$8 = $$11;
               }
            }
         }

         return $$7;
      }
   }

   
   private static Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(
      Set<Holder<Structure>> $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.StructureManager $$2,
      int $$3,
      int $$4,
      int $$5,
      boolean $$6,
      long $$7,
      RandomSpreadStructurePlacement $$8
   ) {
      int $$9 = $$8.spacing();

      for (int $$10 = -$$5; $$10 <= $$5; $$10++) {
         boolean $$11 = $$10 == -$$5 || $$10 == $$5;

         for (int $$12 = -$$5; $$12 <= $$5; $$12++) {
            boolean $$13 = $$12 == -$$5 || $$12 == $$5;
            if ($$11 || $$13) {
               int $$14 = $$3 + $$9 * $$10;
               int $$15 = $$4 + $$9 * $$12;
               net.minecraft.world.level.ChunkPos $$16 = $$8.getPotentialStructureChunk($$7, $$14, $$15);
               Pair<BlockPos, Holder<Structure>> $$17 = getStructureGeneratingAt($$0, $$1, $$2, $$6, $$8, $$16);
               if ($$17 != null) {
                  return $$17;
               }
            }
         }
      }

      return null;
   }

   
   private static Pair<BlockPos, Holder<Structure>> getStructureGeneratingAt(
      Set<Holder<Structure>> $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.StructureManager $$2,
      boolean $$3,
      StructurePlacement $$4,
      net.minecraft.world.level.ChunkPos $$5
   ) {
      for (Holder<Structure> $$6 : $$0) {
         StructureCheckResult $$7 = $$2.checkStructurePresence($$5, (Structure)$$6.value(), $$4, $$3);
         if ($$7 != StructureCheckResult.START_NOT_PRESENT) {
            if (!$$3 && $$7 == StructureCheckResult.START_PRESENT) {
               return Pair.of($$4.getLocatePos($$5), $$6);
            }

            ChunkAccess $$8 = $$1.getChunk($$5.x, $$5.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart $$9 = $$2.getStartForStructure(SectionPos.bottomOf($$8), (Structure)$$6.value(), $$8);
            if ($$9 != null && $$9.isValid() && (!$$3 || tryAddReference($$2, $$9))) {
               return Pair.of($$4.getLocatePos($$9.getChunkPos()), $$6);
            }
         }
      }

      return null;
   }

   private static boolean tryAddReference(net.minecraft.world.level.StructureManager $$0, StructureStart $$1) {
      if ($$1.canBeReferenced()) {
         $$0.addReference($$1);
         return true;
      } else {
         return false;
      }
   }

   public void applyBiomeDecoration(net.minecraft.world.level.WorldGenLevel $$0, ChunkAccess $$1, net.minecraft.world.level.StructureManager $$2) {
      net.minecraft.world.level.ChunkPos $$3 = $$1.getPos();
      if (!SharedConstants.debugVoidTerrain($$3)) {
         SectionPos $$4 = SectionPos.of($$3, $$0.getMinSectionY());
         BlockPos $$5 = $$4.origin();
         Registry<Structure> $$6 = $$0.registryAccess().lookupOrThrow(Registries.STRUCTURE);
         Map<Integer, List<Structure>> $$7 = $$6.stream().collect(Collectors.groupingBy($$0x -> $$0x.step().ordinal()));
         List<FeatureSorter.StepFeatureData> $$8 = this.featuresPerStep.get();
         WorldgenRandom $$9 = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
         long $$10 = $$9.setDecorationSeed($$0.getSeed(), $$5.getX(), $$5.getZ());
         Set<Holder<Biome>> $$11 = new ObjectArraySet();
         net.minecraft.world.level.ChunkPos.rangeClosed($$4.chunk(), 1).forEach($$2x -> {
            ChunkAccess $$3x = $$0.getChunk($$2x.x, $$2x.z);

            for (LevelChunkSection $$4x : $$3x.getSections()) {
               $$4x.getBiomes().getAll($$11::add);
            }
         });
         $$11.retainAll(this.biomeSource.possibleBiomes());
         int $$12 = $$8.size();

         try {
            Registry<PlacedFeature> $$13 = $$0.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
            int $$14 = Math.max(GenerationStep.Decoration.values().length, $$12);

            for (int $$15 = 0; $$15 < $$14; $$15++) {
               int $$16 = 0;
               if ($$2.shouldGenerateStructures()) {
                  for (Structure $$18 : $$7.getOrDefault($$15, Collections.emptyList())) {
                     $$9.setFeatureSeed($$10, $$16, $$15);
                     Supplier<String> $$19 = () -> $$6.getResourceKey($$18).map(Object::toString).orElseGet($$18::toString);

                     try {
                        $$0.setCurrentlyGenerating($$19);
                        $$2.startsForStructure($$4, $$18).forEach($$5x -> $$5x.placeInChunk($$0, $$2, this, $$9, getWritableArea($$1), $$3));
                     } catch (Exception var29) {
                        CrashReport $$21 = CrashReport.forThrowable(var29, "Feature placement");
                        $$21.addCategory("Feature").setDetail("Description", $$19::get);
                        throw new ReportedException($$21);
                     }

                     $$16++;
                  }
               }

               if ($$15 < $$12) {
                  IntSet $$22 = new IntArraySet();

                  for (Holder<Biome> $$23 : $$11) {
                     List<HolderSet<PlacedFeature>> $$24 = this.generationSettingsGetter.apply($$23).features();
                     if ($$15 < $$24.size()) {
                        HolderSet<PlacedFeature> $$25 = $$24.get($$15);
                        FeatureSorter.StepFeatureData $$26 = $$8.get($$15);
                        $$25.stream().<PlacedFeature>map(Holder::value).forEach($$2x -> $$22.add($$26.indexMapping().applyAsInt($$2x)));
                     }
                  }

                  int $$27 = $$22.size();
                  int[] $$28 = $$22.toIntArray();
                  Arrays.sort($$28);
                  FeatureSorter.StepFeatureData $$29 = $$8.get($$15);

                  for (int $$30 = 0; $$30 < $$27; $$30++) {
                     int $$31 = $$28[$$30];
                     PlacedFeature $$32 = $$29.features().get($$31);
                     Supplier<String> $$33 = () -> $$13.getResourceKey($$32).map(Object::toString).orElseGet($$32::toString);
                     $$9.setFeatureSeed($$10, $$31, $$15);

                     try {
                        $$0.setCurrentlyGenerating($$33);
                        $$32.placeWithBiomeCheck($$0, this, $$9, $$5);
                     } catch (Exception var30) {
                        CrashReport $$35 = CrashReport.forThrowable(var30, "Feature placement");
                        $$35.addCategory("Feature").setDetail("Description", $$33::get);
                        throw new ReportedException($$35);
                     }
                  }
               }
            }

            $$0.setCurrentlyGenerating(null);
            if (SharedConstants.DEBUG_FEATURE_COUNT) {
               FeatureCountTracker.chunkDecorated($$0.getLevel());
            }
         } catch (Exception var31) {
            CrashReport $$37 = CrashReport.forThrowable(var31, "Biome decoration");
            $$37.addCategory("Generation").setDetail("CenterX", $$3.x).setDetail("CenterZ", $$3.z).setDetail("Decoration Seed", $$10);
            throw new ReportedException($$37);
         }
      }
   }

   private static BoundingBox getWritableArea(ChunkAccess $$0) {
      net.minecraft.world.level.ChunkPos $$1 = $$0.getPos();
      int $$2 = $$1.getMinBlockX();
      int $$3 = $$1.getMinBlockZ();
      net.minecraft.world.level.LevelHeightAccessor $$4 = $$0.getHeightAccessorForGeneration();
      int $$5 = $$4.getMinY() + 1;
      int $$6 = $$4.getMaxY();
      return new BoundingBox($$2, $$5, $$3, $$2 + 15, $$6, $$3 + 15);
   }

   public abstract void buildSurface(WorldGenRegion var1, net.minecraft.world.level.StructureManager var2, RandomState var3, ChunkAccess var4);

   public abstract void spawnOriginalMobs(WorldGenRegion var1);

   public int getSpawnHeight(net.minecraft.world.level.LevelHeightAccessor $$0) {
      return 64;
   }

   public BiomeSource getBiomeSource() {
      return this.biomeSource;
   }

   public abstract int getGenDepth();

   public WeightedList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> $$0, net.minecraft.world.level.StructureManager $$1, MobCategory $$2, BlockPos $$3) {
      Map<Structure, LongSet> $$4 = $$1.getAllStructuresAt($$3);

      for (Entry<Structure, LongSet> $$5 : $$4.entrySet()) {
         Structure $$6 = $$5.getKey();
         StructureSpawnOverride $$7 = $$6.spawnOverrides().get($$2);
         if ($$7 != null) {
            MutableBoolean $$8 = new MutableBoolean(false);
            Predicate<StructureStart> $$9 = $$7.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE
               ? $$2x -> $$1.structureHasPieceAt($$3, $$2x)
               : $$1x -> $$1x.getBoundingBox().isInside($$3);
            $$1.fillStartsForStructure($$6, $$5.getValue(), $$2x -> {
               if ($$8.isFalse() && $$9.test($$2x)) {
                  $$8.setTrue();
               }
            });
            if ($$8.isTrue()) {
               return $$7.spawns();
            }
         }
      }

      return ((Biome)$$0.value()).getMobSettings().getMobs($$2);
   }

   public void createStructures(
      RegistryAccess $$0,
      ChunkGeneratorStructureState $$1,
      net.minecraft.world.level.StructureManager $$2,
      ChunkAccess $$3,
      StructureTemplateManager $$4,
      ResourceKey<net.minecraft.world.level.Level> $$5
   ) {
      if (!SharedConstants.DEBUG_DISABLE_STRUCTURES) {
         net.minecraft.world.level.ChunkPos $$6 = $$3.getPos();
         SectionPos $$7 = SectionPos.bottomOf($$3);
         RandomState $$8 = $$1.randomState();
         $$1.possibleStructureSets().forEach($$9 -> {
            StructurePlacement $$10 = ((StructureSet)$$9.value()).placement();
            List<StructureSet.StructureSelectionEntry> $$11 = ((StructureSet)$$9.value()).structures();

            for (StructureSet.StructureSelectionEntry $$12 : $$11) {
               StructureStart $$13 = $$2.getStartForStructure($$7, (Structure)$$12.structure().value(), $$3);
               if ($$13 != null && $$13.isValid()) {
                  return;
               }
            }

            if ($$10.isStructureChunk($$1, $$6.x, $$6.z)) {
               if ($$11.size() == 1) {
                  this.tryGenerateStructure($$11.get(0), $$2, $$0, $$8, $$4, $$1.getLevelSeed(), $$3, $$6, $$7, $$5);
               } else {
                  ArrayList<StructureSet.StructureSelectionEntry> $$14 = new ArrayList<>($$11.size());
                  $$14.addAll($$11);
                  WorldgenRandom $$15 = new WorldgenRandom(new LegacyRandomSource(0L));
                  $$15.setLargeFeatureSeed($$1.getLevelSeed(), $$6.x, $$6.z);
                  int $$16 = 0;

                  for (StructureSet.StructureSelectionEntry $$17 : $$14) {
                     $$16 += $$17.weight();
                  }

                  while (!$$14.isEmpty()) {
                     int $$18 = $$15.nextInt($$16);
                     int $$19 = 0;

                     for (StructureSet.StructureSelectionEntry $$20 : $$14) {
                        $$18 -= $$20.weight();
                        if ($$18 < 0) {
                           break;
                        }

                        $$19++;
                     }

                     StructureSet.StructureSelectionEntry $$21 = $$14.get($$19);
                     if (this.tryGenerateStructure($$21, $$2, $$0, $$8, $$4, $$1.getLevelSeed(), $$3, $$6, $$7, $$5)) {
                        return;
                     }

                     $$14.remove($$19);
                     $$16 -= $$21.weight();
                  }
               }
            }
         });
      }
   }

   private boolean tryGenerateStructure(
      StructureSet.StructureSelectionEntry $$0,
      net.minecraft.world.level.StructureManager $$1,
      RegistryAccess $$2,
      RandomState $$3,
      StructureTemplateManager $$4,
      long $$5,
      ChunkAccess $$6,
      net.minecraft.world.level.ChunkPos $$7,
      SectionPos $$8,
      ResourceKey<net.minecraft.world.level.Level> $$9
   ) {
      Structure $$10 = (Structure)$$0.structure().value();
      int $$11 = fetchReferences($$1, $$6, $$8, $$10);
      HolderSet<Biome> $$12 = $$10.biomes();
      Predicate<Holder<Biome>> $$13 = $$12::contains;
      StructureStart $$14 = $$10.generate($$0.structure(), $$9, $$2, this, this.biomeSource, $$3, $$4, $$5, $$7, $$11, $$6, $$13);
      if ($$14.isValid()) {
         $$1.setStartForStructure($$8, $$10, $$14, $$6);
         return true;
      } else {
         return false;
      }
   }

   private static int fetchReferences(net.minecraft.world.level.StructureManager $$0, ChunkAccess $$1, SectionPos $$2, Structure $$3) {
      StructureStart $$4 = $$0.getStartForStructure($$2, $$3, $$1);
      return $$4 != null ? $$4.getReferences() : 0;
   }

   public void createReferences(net.minecraft.world.level.WorldGenLevel $$0, net.minecraft.world.level.StructureManager $$1, ChunkAccess $$2) {
      int $$3 = 8;
      net.minecraft.world.level.ChunkPos $$4 = $$2.getPos();
      int $$5 = $$4.x;
      int $$6 = $$4.z;
      int $$7 = $$4.getMinBlockX();
      int $$8 = $$4.getMinBlockZ();
      SectionPos $$9 = SectionPos.bottomOf($$2);

      for (int $$10 = $$5 - 8; $$10 <= $$5 + 8; $$10++) {
         for (int $$11 = $$6 - 8; $$11 <= $$6 + 8; $$11++) {
            long $$12 = net.minecraft.world.level.ChunkPos.asLong($$10, $$11);

            for (StructureStart $$13 : $$0.getChunk($$10, $$11).getAllStarts().values()) {
               try {
                  if ($$13.isValid() && $$13.getBoundingBox().intersects($$7, $$8, $$7 + 15, $$8 + 15)) {
                     $$1.addReferenceForStructure($$9, $$13.getStructure(), $$12, $$2);
                  }
               } catch (Exception var21) {
                  CrashReport $$15 = CrashReport.forThrowable(var21, "Generating structure reference");
                  CrashReportCategory $$16 = $$15.addCategory("Structure");
                  Optional<? extends Registry<Structure>> $$17 = $$0.registryAccess().lookup(Registries.STRUCTURE);
                  $$16.setDetail("Id", () -> $$17.<String>map($$1xx -> $$1xx.getKey($$13.getStructure()).toString()).orElse("UNKNOWN"));
                  $$16.setDetail("Name", () -> BuiltInRegistries.STRUCTURE_TYPE.getKey($$13.getStructure().type()).toString());
                  $$16.setDetail("Class", () -> $$13.getStructure().getClass().getCanonicalName());
                  throw new ReportedException($$15);
               }
            }
         }
      }
   }

   public abstract CompletableFuture<ChunkAccess> fillFromNoise(
      Blender var1, RandomState var2, net.minecraft.world.level.StructureManager var3, ChunkAccess var4
   );

   public abstract int getSeaLevel();

   public abstract int getMinY();

   public abstract int getBaseHeight(int var1, int var2, Heightmap.Types var3, net.minecraft.world.level.LevelHeightAccessor var4, RandomState var5);

   public abstract net.minecraft.world.level.NoiseColumn getBaseColumn(int var1, int var2, net.minecraft.world.level.LevelHeightAccessor var3, RandomState var4);

   public int getFirstFreeHeight(int $$0, int $$1, Heightmap.Types $$2, net.minecraft.world.level.LevelHeightAccessor $$3, RandomState $$4) {
      return this.getBaseHeight($$0, $$1, $$2, $$3, $$4);
   }

   public int getFirstOccupiedHeight(int $$0, int $$1, Heightmap.Types $$2, net.minecraft.world.level.LevelHeightAccessor $$3, RandomState $$4) {
      return this.getBaseHeight($$0, $$1, $$2, $$3, $$4) - 1;
   }

   public abstract void addDebugScreenInfo(List<String> var1, RandomState var2, BlockPos var3);

   @Deprecated
   public BiomeGenerationSettings getBiomeGenerationSettings(Holder<Biome> $$0) {
      return this.generationSettingsGetter.apply($$0);
   }
}
