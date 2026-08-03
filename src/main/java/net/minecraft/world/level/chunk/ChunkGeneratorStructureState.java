package net.minecraft.world.level.chunk;

import com.google.common.base.Stopwatch;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkGeneratorStructureState {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final RandomState randomState;
   private final BiomeSource biomeSource;
   private final long levelSeed;
   private final long concentricRingsSeed;
   private final Map<Structure, List<StructurePlacement>> placementsForStructure = new Object2ObjectOpenHashMap();
   private final Map<ConcentricRingsStructurePlacement, CompletableFuture<List<net.minecraft.world.level.ChunkPos>>> ringPositions = new Object2ObjectArrayMap();
   private boolean hasGeneratedPositions;
   private final List<Holder<StructureSet>> possibleStructureSets;

   public static ChunkGeneratorStructureState createForFlat(RandomState $$0, long $$1, BiomeSource $$2, Stream<Holder<StructureSet>> $$3) {
      List<Holder<StructureSet>> $$4 = $$3.filter($$1x -> hasBiomesForStructureSet((StructureSet)$$1x.value(), $$2)).toList();
      return new ChunkGeneratorStructureState($$0, $$2, $$1, 0L, $$4);
   }

   public static ChunkGeneratorStructureState createForNormal(RandomState $$0, long $$1, BiomeSource $$2, HolderLookup<StructureSet> $$3) {
      List<Holder<StructureSet>> $$4 = $$3.listElements()
         .filter($$1x -> hasBiomesForStructureSet((StructureSet)$$1x.value(), $$2))
         .collect(Collectors.toUnmodifiableList());
      return new ChunkGeneratorStructureState($$0, $$2, $$1, $$1, $$4);
   }

   private static boolean hasBiomesForStructureSet(StructureSet $$0, BiomeSource $$1) {
      Stream<Holder<Biome>> $$2 = $$0.structures().stream().flatMap($$0x -> {
         Structure $$1x = (Structure)$$0x.structure().value();
         return $$1x.biomes().stream();
      });
      return $$2.anyMatch($$1.possibleBiomes()::contains);
   }

   private ChunkGeneratorStructureState(RandomState $$0, BiomeSource $$1, long $$2, long $$3, List<Holder<StructureSet>> $$4) {
      this.randomState = $$0;
      this.levelSeed = $$2;
      this.biomeSource = $$1;
      this.concentricRingsSeed = $$3;
      this.possibleStructureSets = $$4;
   }

   public List<Holder<StructureSet>> possibleStructureSets() {
      return this.possibleStructureSets;
   }

   private void generatePositions() {
      Set<Holder<Biome>> $$0 = this.biomeSource.possibleBiomes();
      this.possibleStructureSets().forEach($$1 -> {
         StructureSet $$2 = (StructureSet)$$1.value();
         boolean $$3 = false;

         for (StructureSet.StructureSelectionEntry $$4 : $$2.structures()) {
            Structure $$5 = (Structure)$$4.structure().value();
            if ($$5.biomes().stream().anyMatch($$0::contains)) {
               this.placementsForStructure.computeIfAbsent($$5, $$0xx -> new ArrayList<>()).add($$2.placement());
               $$3 = true;
            }
         }

         if ($$3 && $$2.placement() instanceof ConcentricRingsStructurePlacement $$7) {
            this.ringPositions.put($$7, this.generateRingPositions((Holder<StructureSet>)$$1, $$7));
         }
      });
   }

   private CompletableFuture<List<net.minecraft.world.level.ChunkPos>> generateRingPositions(Holder<StructureSet> $$0, ConcentricRingsStructurePlacement $$1) {
      if ($$1.count() == 0) {
         return CompletableFuture.completedFuture(List.of());
      } else {
         Stopwatch $$2 = Stopwatch.createStarted(Util.TICKER);
         int $$3 = $$1.distance();
         int $$4 = $$1.count();
         List<CompletableFuture<net.minecraft.world.level.ChunkPos>> $$5 = new ArrayList<>($$4);
         int $$6 = $$1.spread();
         HolderSet<Biome> $$7 = $$1.preferredBiomes();
         RandomSource $$8 = RandomSource.create();
         $$8.setSeed(this.concentricRingsSeed);
         double $$9 = $$8.nextDouble() * Math.PI * 2.0;
         int $$10 = 0;
         int $$11 = 0;

         for (int $$12 = 0; $$12 < $$4; $$12++) {
            double $$13 = 4 * $$3 + $$3 * $$11 * 6 + ($$8.nextDouble() - 0.5) * ($$3 * 2.5);
            int $$14 = (int)Math.round(Math.cos($$9) * $$13);
            int $$15 = (int)Math.round(Math.sin($$9) * $$13);
            RandomSource $$16 = $$8.fork();
            $$5.add(
               CompletableFuture.supplyAsync(
                  () -> {
                     Pair<BlockPos, Holder<Biome>> $$4x = this.biomeSource
                        .findBiomeHorizontal(
                           SectionPos.sectionToBlockCoord($$14, 8),
                           0,
                           SectionPos.sectionToBlockCoord($$15, 8),
                           112,
                           $$7::contains,
                           $$16,
                           this.randomState.sampler()
                        );
                     if ($$4x != null) {
                        BlockPos $$5x = (BlockPos)$$4x.getFirst();
                        return new net.minecraft.world.level.ChunkPos(SectionPos.blockToSectionCoord($$5x.getX()), SectionPos.blockToSectionCoord($$5x.getZ()));
                     } else {
                        return new net.minecraft.world.level.ChunkPos($$14, $$15);
                     }
                  },
                  Util.backgroundExecutor().forName("structureRings")
               )
            );
            $$9 += (Math.PI * 2) / $$6;
            if (++$$10 == $$6) {
               $$11++;
               $$10 = 0;
               $$6 += 2 * $$6 / ($$11 + 1);
               $$6 = Math.min($$6, $$4 - $$12);
               $$9 += $$8.nextDouble() * Math.PI * 2.0;
            }
         }

         return Util.sequence($$5).thenApply($$2x -> {
            double $$3x = $$2.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0;
            LOGGER.debug("Calculation for {} took {}s", $$0, $$3x);
            return $$2x;
         });
      }
   }

   public void ensureStructuresGenerated() {
      if (!this.hasGeneratedPositions) {
         this.generatePositions();
         this.hasGeneratedPositions = true;
      }
   }

   @Nullable
   public List<net.minecraft.world.level.ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement $$0) {
      this.ensureStructuresGenerated();
      CompletableFuture<List<net.minecraft.world.level.ChunkPos>> $$1 = this.ringPositions.get($$0);
      return $$1 != null ? $$1.join() : null;
   }

   public List<StructurePlacement> getPlacementsForStructure(Holder<Structure> $$0) {
      this.ensureStructuresGenerated();
      return this.placementsForStructure.getOrDefault($$0.value(), List.of());
   }

   public RandomState randomState() {
      return this.randomState;
   }

   public boolean hasStructureChunkInRange(Holder<StructureSet> $$0, int $$1, int $$2, int $$3) {
      StructurePlacement $$4 = ((StructureSet)$$0.value()).placement();

      for (int $$5 = $$1 - $$3; $$5 <= $$1 + $$3; $$5++) {
         for (int $$6 = $$2 - $$3; $$6 <= $$2 + $$3; $$6++) {
            if ($$4.isStructureChunk(this, $$5, $$6)) {
               return true;
            }
         }
      }

      return false;
   }

   public long getLevelSeed() {
      return this.levelSeed;
   }
}
