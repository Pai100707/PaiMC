package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class Structure {
   public static final Codec<Structure> DIRECT_CODEC = BuiltInRegistries.STRUCTURE_TYPE.byNameCodec().dispatch(Structure::type, StructureType::codec);
   public static final Codec<Holder<Structure>> CODEC = RegistryFileCodec.create(Registries.STRUCTURE, DIRECT_CODEC);
   protected final Structure.StructureSettings settings;

   public static <S extends Structure> RecordCodecBuilder<S, Structure.StructureSettings> settingsCodec(Instance<S> $$0) {
      return Structure.StructureSettings.CODEC.forGetter($$0x -> $$0x.settings);
   }

   public static <S extends Structure> MapCodec<S> simpleCodec(Function<Structure.StructureSettings, S> $$0) {
      return RecordCodecBuilder.mapCodec($$1 -> $$1.group(settingsCodec($$1)).apply($$1, $$0));
   }

   protected Structure(Structure.StructureSettings $$0) {
      this.settings = $$0;
   }

   public HolderSet<Biome> biomes() {
      return this.settings.biomes;
   }

   public Map<MobCategory, StructureSpawnOverride> spawnOverrides() {
      return this.settings.spawnOverrides;
   }

   public GenerationStep.Decoration step() {
      return this.settings.step;
   }

   public TerrainAdjustment terrainAdaptation() {
      return this.settings.terrainAdaptation;
   }

   public BoundingBox adjustBoundingBox(BoundingBox $$0) {
      return this.terrainAdaptation() != TerrainAdjustment.NONE ? $$0.inflatedBy(12) : $$0;
   }

   public StructureStart generate(
      Holder<Structure> $$0,
      ResourceKey<net.minecraft.world.level.Level> $$1,
      RegistryAccess $$2,
      ChunkGenerator $$3,
      BiomeSource $$4,
      RandomState $$5,
      StructureTemplateManager $$6,
      long $$7,
      net.minecraft.world.level.ChunkPos $$8,
      int $$9,
      net.minecraft.world.level.LevelHeightAccessor $$10,
      Predicate<Holder<Biome>> $$11
   ) {
      ProfiledDuration $$12 = JvmProfiler.INSTANCE.onStructureGenerate($$8, $$1, $$0);
      Structure.GenerationContext $$13 = new Structure.GenerationContext($$2, $$3, $$4, $$5, $$6, $$7, $$8, $$10, $$11);
      Optional<Structure.GenerationStub> $$14 = this.findValidGenerationPoint($$13);
      if ($$14.isPresent()) {
         StructurePiecesBuilder $$15 = $$14.get().getPiecesBuilder();
         StructureStart $$16 = new StructureStart(this, $$8, $$9, $$15.build());
         if ($$16.isValid()) {
            if ($$12 != null) {
               $$12.finish(true);
            }

            return $$16;
         }
      }

      if ($$12 != null) {
         $$12.finish(false);
      }

      return StructureStart.INVALID_START;
   }

   protected static Optional<Structure.GenerationStub> onTopOfChunkCenter(
      Structure.GenerationContext $$0, Heightmap.Types $$1, Consumer<StructurePiecesBuilder> $$2
   ) {
      net.minecraft.world.level.ChunkPos $$3 = $$0.chunkPos();
      int $$4 = $$3.getMiddleBlockX();
      int $$5 = $$3.getMiddleBlockZ();
      int $$6 = $$0.chunkGenerator().getFirstOccupiedHeight($$4, $$5, $$1, $$0.heightAccessor(), $$0.randomState());
      return Optional.of(new Structure.GenerationStub(new BlockPos($$4, $$6, $$5), $$2));
   }

   private static boolean isValidBiome(Structure.GenerationStub $$0, Structure.GenerationContext $$1) {
      BlockPos $$2 = $$0.position();
      return $$1.validBiome
         .test(
            $$1.chunkGenerator
               .getBiomeSource()
               .getNoiseBiome(QuartPos.fromBlock($$2.getX()), QuartPos.fromBlock($$2.getY()), QuartPos.fromBlock($$2.getZ()), $$1.randomState.sampler())
         );
   }

   public void afterPlace(
      net.minecraft.world.level.WorldGenLevel $$0,
      net.minecraft.world.level.StructureManager $$1,
      ChunkGenerator $$2,
      RandomSource $$3,
      BoundingBox $$4,
      net.minecraft.world.level.ChunkPos $$5,
      PiecesContainer $$6
   ) {
   }

   private static int[] getCornerHeights(Structure.GenerationContext $$0, int $$1, int $$2, int $$3, int $$4) {
      ChunkGenerator $$5 = $$0.chunkGenerator();
      net.minecraft.world.level.LevelHeightAccessor $$6 = $$0.heightAccessor();
      RandomState $$7 = $$0.randomState();
      return new int[]{
         $$5.getFirstOccupiedHeight($$1, $$3, Heightmap.Types.WORLD_SURFACE_WG, $$6, $$7),
         $$5.getFirstOccupiedHeight($$1, $$3 + $$4, Heightmap.Types.WORLD_SURFACE_WG, $$6, $$7),
         $$5.getFirstOccupiedHeight($$1 + $$2, $$3, Heightmap.Types.WORLD_SURFACE_WG, $$6, $$7),
         $$5.getFirstOccupiedHeight($$1 + $$2, $$3 + $$4, Heightmap.Types.WORLD_SURFACE_WG, $$6, $$7)
      };
   }

   public static int getMeanFirstOccupiedHeight(Structure.GenerationContext $$0, int $$1, int $$2, int $$3, int $$4) {
      int[] $$5 = getCornerHeights($$0, $$1, $$2, $$3, $$4);
      return ($$5[0] + $$5[1] + $$5[2] + $$5[3]) / 4;
   }

   protected static int getLowestY(Structure.GenerationContext $$0, int $$1, int $$2) {
      net.minecraft.world.level.ChunkPos $$3 = $$0.chunkPos();
      int $$4 = $$3.getMinBlockX();
      int $$5 = $$3.getMinBlockZ();
      return getLowestY($$0, $$4, $$5, $$1, $$2);
   }

   protected static int getLowestY(Structure.GenerationContext $$0, int $$1, int $$2, int $$3, int $$4) {
      int[] $$5 = getCornerHeights($$0, $$1, $$3, $$2, $$4);
      return Math.min(Math.min($$5[0], $$5[1]), Math.min($$5[2], $$5[3]));
   }

   @Deprecated
   protected BlockPos getLowestYIn5by5BoxOffset7Blocks(Structure.GenerationContext $$0, Rotation $$1) {
      int $$2 = 5;
      int $$3 = 5;
      if ($$1 == Rotation.CLOCKWISE_90) {
         $$2 = -5;
      } else if ($$1 == Rotation.CLOCKWISE_180) {
         $$2 = -5;
         $$3 = -5;
      } else if ($$1 == Rotation.COUNTERCLOCKWISE_90) {
         $$3 = -5;
      }

      net.minecraft.world.level.ChunkPos $$4 = $$0.chunkPos();
      int $$5 = $$4.getBlockX(7);
      int $$6 = $$4.getBlockZ(7);
      return new BlockPos($$5, getLowestY($$0, $$5, $$6, $$2, $$3), $$6);
   }

   protected abstract Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext var1);

   public Optional<Structure.GenerationStub> findValidGenerationPoint(Structure.GenerationContext $$0) {
      return this.findGenerationPoint($$0).filter($$1 -> isValidBiome($$1, $$0));
   }

   public abstract StructureType<?> type();

   public record GenerationContext(
      RegistryAccess registryAccess,
      ChunkGenerator chunkGenerator,
      BiomeSource biomeSource,
      RandomState randomState,
      StructureTemplateManager structureTemplateManager,
      WorldgenRandom random,
      long seed,
      net.minecraft.world.level.ChunkPos chunkPos,
      net.minecraft.world.level.LevelHeightAccessor heightAccessor,
      Predicate<Holder<Biome>> validBiome
   ) {

      public GenerationContext(
         RegistryAccess $$0,
         ChunkGenerator $$1,
         BiomeSource $$2,
         RandomState $$3,
         StructureTemplateManager $$4,
         long $$5,
         net.minecraft.world.level.ChunkPos $$6,
         net.minecraft.world.level.LevelHeightAccessor $$7,
         Predicate<Holder<Biome>> $$8
      ) {
         this($$0, $$1, $$2, $$3, $$4, makeRandom($$5, $$6), $$5, $$6, $$7, $$8);
      }

      private static WorldgenRandom makeRandom(long $$0, net.minecraft.world.level.ChunkPos $$1) {
         WorldgenRandom $$2 = new WorldgenRandom(new LegacyRandomSource(0L));
         $$2.setLargeFeatureSeed($$0, $$1.x, $$1.z);
         return $$2;
      }
   }

   public record GenerationStub(BlockPos position, Either<Consumer<StructurePiecesBuilder>, StructurePiecesBuilder> generator) {
      public GenerationStub(BlockPos $$0, Consumer<StructurePiecesBuilder> $$1) {
         this($$0, Either.left($$1));
      }

      public StructurePiecesBuilder getPiecesBuilder() {
         return (StructurePiecesBuilder)this.generator.map($$0 -> {
            StructurePiecesBuilder $$1 = new StructurePiecesBuilder();
            $$0.accept($$1);
            return $$1;
         }, $$0 -> $$0);
      }
   }

   public record StructureSettings(
      HolderSet<Biome> biomes, Map<MobCategory, StructureSpawnOverride> spawnOverrides, GenerationStep.Decoration step, TerrainAdjustment terrainAdaptation
   ) {
      static final Structure.StructureSettings DEFAULT = new Structure.StructureSettings(
         HolderSet.direct(new Holder[0]), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE
      );
      public static final MapCodec<Structure.StructureSettings> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(Structure.StructureSettings::biomes),
               Codec.simpleMap(MobCategory.CODEC, StructureSpawnOverride.CODEC, StringRepresentable.keys(MobCategory.values()))
                  .fieldOf("spawn_overrides")
                  .forGetter(Structure.StructureSettings::spawnOverrides),
               GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(Structure.StructureSettings::step),
               TerrainAdjustment.CODEC
                  .optionalFieldOf("terrain_adaptation", DEFAULT.terrainAdaptation)
                  .forGetter(Structure.StructureSettings::terrainAdaptation)
            )
            .apply($$0, Structure.StructureSettings::new)
      );

      public StructureSettings(HolderSet<Biome> $$0) {
         this($$0, DEFAULT.spawnOverrides, DEFAULT.step, DEFAULT.terrainAdaptation);
      }

      public static class Builder {
         private final HolderSet<Biome> biomes;
         private Map<MobCategory, StructureSpawnOverride> spawnOverrides;
         private GenerationStep.Decoration step;
         private TerrainAdjustment terrainAdaption;

         public Builder(HolderSet<Biome> $$0) {
            this.spawnOverrides = Structure.StructureSettings.DEFAULT.spawnOverrides;
            this.step = Structure.StructureSettings.DEFAULT.step;
            this.terrainAdaption = Structure.StructureSettings.DEFAULT.terrainAdaptation;
            this.biomes = $$0;
         }

         public Structure.StructureSettings.Builder spawnOverrides(Map<MobCategory, StructureSpawnOverride> $$0) {
            this.spawnOverrides = $$0;
            return this;
         }

         public Structure.StructureSettings.Builder generationStep(GenerationStep.Decoration $$0) {
            this.step = $$0;
            return this;
         }

         public Structure.StructureSettings.Builder terrainAdapation(TerrainAdjustment $$0) {
            this.terrainAdaption = $$0;
            return this;
         }

         public Structure.StructureSettings build() {
            return new Structure.StructureSettings(this.biomes, this.spawnOverrides, this.step, this.terrainAdaption);
         }
      }
   }
}
