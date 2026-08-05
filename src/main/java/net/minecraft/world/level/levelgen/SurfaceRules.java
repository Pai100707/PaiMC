package net.minecraft.world.level.levelgen;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceRules {
   public static final SurfaceRules.ConditionSource ON_FLOOR = stoneDepthCheck(0, false, CaveSurface.FLOOR);
   public static final SurfaceRules.ConditionSource UNDER_FLOOR = stoneDepthCheck(0, true, CaveSurface.FLOOR);
   public static final SurfaceRules.ConditionSource DEEP_UNDER_FLOOR = stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);
   public static final SurfaceRules.ConditionSource VERY_DEEP_UNDER_FLOOR = stoneDepthCheck(0, true, 30, CaveSurface.FLOOR);
   public static final SurfaceRules.ConditionSource ON_CEILING = stoneDepthCheck(0, false, CaveSurface.CEILING);
   public static final SurfaceRules.ConditionSource UNDER_CEILING = stoneDepthCheck(0, true, CaveSurface.CEILING);

   public static SurfaceRules.ConditionSource stoneDepthCheck(int $$0, boolean $$1, CaveSurface $$2) {
      return new SurfaceRules.StoneDepthCheck($$0, $$1, 0, $$2);
   }

   public static SurfaceRules.ConditionSource stoneDepthCheck(int $$0, boolean $$1, int $$2, CaveSurface $$3) {
      return new SurfaceRules.StoneDepthCheck($$0, $$1, $$2, $$3);
   }

   public static SurfaceRules.ConditionSource not(SurfaceRules.ConditionSource $$0) {
      return new SurfaceRules.NotConditionSource($$0);
   }

   public static SurfaceRules.ConditionSource yBlockCheck(VerticalAnchor $$0, int $$1) {
      return new SurfaceRules.YConditionSource($$0, $$1, false);
   }

   public static SurfaceRules.ConditionSource yStartCheck(VerticalAnchor $$0, int $$1) {
      return new SurfaceRules.YConditionSource($$0, $$1, true);
   }

   public static SurfaceRules.ConditionSource waterBlockCheck(int $$0, int $$1) {
      return new SurfaceRules.WaterConditionSource($$0, $$1, false);
   }

   public static SurfaceRules.ConditionSource waterStartCheck(int $$0, int $$1) {
      return new SurfaceRules.WaterConditionSource($$0, $$1, true);
   }

   @SafeVarargs
   public static SurfaceRules.ConditionSource isBiome(ResourceKey<Biome>... $$0) {
      return isBiome(List.of($$0));
   }

   private static SurfaceRules.BiomeConditionSource isBiome(List<ResourceKey<Biome>> $$0) {
      return new SurfaceRules.BiomeConditionSource($$0);
   }

   public static SurfaceRules.ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> $$0, double $$1) {
      return noiseCondition($$0, $$1, Double.MAX_VALUE);
   }

   public static SurfaceRules.ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> $$0, double $$1, double $$2) {
      return new SurfaceRules.NoiseThresholdConditionSource($$0, $$1, $$2);
   }

   public static SurfaceRules.ConditionSource verticalGradient(String $$0, VerticalAnchor $$1, VerticalAnchor $$2) {
      return new SurfaceRules.VerticalGradientConditionSource(Identifier.parse($$0), $$1, $$2);
   }

   public static SurfaceRules.ConditionSource steep() {
      return SurfaceRules.Steep.INSTANCE;
   }

   public static SurfaceRules.ConditionSource hole() {
      return SurfaceRules.Hole.INSTANCE;
   }

   public static SurfaceRules.ConditionSource abovePreliminarySurface() {
      return SurfaceRules.AbovePreliminarySurface.INSTANCE;
   }

   public static SurfaceRules.ConditionSource temperature() {
      return SurfaceRules.Temperature.INSTANCE;
   }

   public static SurfaceRules.RuleSource ifTrue(SurfaceRules.ConditionSource $$0, SurfaceRules.RuleSource $$1) {
      return new SurfaceRules.TestRuleSource($$0, $$1);
   }

   public static SurfaceRules.RuleSource sequence(SurfaceRules.RuleSource... $$0) {
      if ($$0.length == 0) {
         throw new IllegalArgumentException("Need at least 1 rule for a sequence");
      } else {
         return new SurfaceRules.SequenceRuleSource(Arrays.asList($$0));
      }
   }

   public static SurfaceRules.RuleSource state(BlockState $$0) {
      return new SurfaceRules.BlockRuleSource($$0);
   }

   public static SurfaceRules.RuleSource bandlands() {
      return SurfaceRules.Bandlands.INSTANCE;
   }

   static <A> MapCodec<? extends A> register(Registry<MapCodec<? extends A>> $$0, String $$1, KeyDispatchDataCodec<? extends A> $$2) {
      return (MapCodec<? extends A>)Registry.register($$0, $$1, $$2.codec());
   }

   static enum AbovePreliminarySurface implements SurfaceRules.ConditionSource {
      INSTANCE;

      static final KeyDispatchDataCodec<SurfaceRules.AbovePreliminarySurface> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(SurfaceRules.Context $$0) {
         return $$0.abovePreliminarySurface;
      }
   }

   static enum Bandlands implements SurfaceRules.RuleSource {
      INSTANCE;

      static final KeyDispatchDataCodec<SurfaceRules.Bandlands> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
         return CODEC;
      }

      public SurfaceRules.SurfaceRule apply(SurfaceRules.Context $$0) {
         return $$0.system::getBand;
      }
   }

   static final class BiomeConditionSource implements SurfaceRules.ConditionSource {
      static final KeyDispatchDataCodec<SurfaceRules.BiomeConditionSource> CODEC = KeyDispatchDataCodec.of(
         ResourceKey.codec(Registries.BIOME).listOf().fieldOf("biome_is").xmap(SurfaceRules::isBiome, $$0 -> $$0.biomes)
      );
      private final List<ResourceKey<Biome>> biomes;
      final Predicate<ResourceKey<Biome>> biomeNameTest;

      BiomeConditionSource(List<ResourceKey<Biome>> $$0) {
         this.biomes = $$0;
         this.biomeNameTest = Set.copyOf($$0)::contains;
      }

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(final SurfaceRules.Context $$0) {
         class BiomeCondition extends SurfaceRules.LazyYCondition {
            BiomeCondition() {
               super($$0);
            }

            @Override
            protected boolean compute() {
               return this.context.biome.get().is(BiomeConditionSource.this.biomeNameTest);
            }
         }

         return new BiomeCondition();
      }

      @Override
      public boolean equals(Object $$0) {
         if (this == $$0) {
            return true;
         } else {
            return $$0 instanceof SurfaceRules.BiomeConditionSource $$1 ? this.biomes.equals($$1.biomes) : false;
         }
      }

      @Override
      public int hashCode() {
         return this.biomes.hashCode();
      }

      @Override
      public String toString() {
         return "BiomeConditionSource[biomes=" + this.biomes + "]";
      }
   }

   record BlockRuleSource(BlockState resultState, SurfaceRules.StateRule rule) implements SurfaceRules.RuleSource {
      static final KeyDispatchDataCodec<SurfaceRules.BlockRuleSource> CODEC = KeyDispatchDataCodec.of(
         BlockState.CODEC.xmap(SurfaceRules.BlockRuleSource::new, SurfaceRules.BlockRuleSource::resultState).fieldOf("result_state")
      );

      BlockRuleSource(BlockState $$0) {
         this($$0, new SurfaceRules.StateRule($$0));
      }

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
         return CODEC;
      }

      public SurfaceRules.SurfaceRule apply(SurfaceRules.Context $$0) {
         return this.rule;
      }
   }

   interface Condition {
      boolean test();
   }

   public interface ConditionSource extends Function<SurfaceRules.Context, SurfaceRules.Condition> {
      Codec<SurfaceRules.ConditionSource> CODEC = BuiltInRegistries.MATERIAL_CONDITION.byNameCodec().dispatch($$0 -> $$0.codec().codec(), Function.identity());

      static MapCodec<? extends SurfaceRules.ConditionSource> bootstrap(Registry<MapCodec<? extends SurfaceRules.ConditionSource>> $$0) {
         SurfaceRules.register($$0, "biome", SurfaceRules.BiomeConditionSource.CODEC);
         SurfaceRules.register($$0, "noise_threshold", SurfaceRules.NoiseThresholdConditionSource.CODEC);
         SurfaceRules.register($$0, "vertical_gradient", SurfaceRules.VerticalGradientConditionSource.CODEC);
         SurfaceRules.register($$0, "y_above", SurfaceRules.YConditionSource.CODEC);
         SurfaceRules.register($$0, "water", SurfaceRules.WaterConditionSource.CODEC);
         SurfaceRules.register($$0, "temperature", SurfaceRules.Temperature.CODEC);
         SurfaceRules.register($$0, "steep", SurfaceRules.Steep.CODEC);
         SurfaceRules.register($$0, "not", SurfaceRules.NotConditionSource.CODEC);
         SurfaceRules.register($$0, "hole", SurfaceRules.Hole.CODEC);
         SurfaceRules.register($$0, "above_preliminary_surface", SurfaceRules.AbovePreliminarySurface.CODEC);
         return SurfaceRules.register($$0, "stone_depth", SurfaceRules.StoneDepthCheck.CODEC);
      }

      KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec();
   }

   protected static final class Context {
      private static final int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE = 8;
      private static final int SURFACE_CELL_BITS = 4;
      private static final int SURFACE_CELL_SIZE = 16;
      private static final int SURFACE_CELL_MASK = 15;
      final SurfaceSystem system;
      final SurfaceRules.Condition temperature = new SurfaceRules.Context.TemperatureHelperCondition(this);
      final SurfaceRules.Condition steep = new SurfaceRules.Context.SteepMaterialCondition(this);
      final SurfaceRules.Condition hole = new SurfaceRules.Context.HoleCondition(this);
      final SurfaceRules.Condition abovePreliminarySurface = new SurfaceRules.Context.AbovePreliminarySurfaceCondition();
      final RandomState randomState;
      final ChunkAccess chunk;
      private final NoiseChunk noiseChunk;
      private final Function<BlockPos, Holder<Biome>> biomeGetter;
      final WorldGenerationContext context;
      private long lastPreliminarySurfaceCellOrigin = Long.MAX_VALUE;
      private final int[] preliminarySurfaceCache = new int[4];
      long lastUpdateXZ = -9223372036854775807L;
      int blockX;
      int blockZ;
      int surfaceDepth;
      private long lastSurfaceDepth2Update = this.lastUpdateXZ - 1L;
      private double surfaceSecondary;
      private long lastMinSurfaceLevelUpdate = this.lastUpdateXZ - 1L;
      private int minSurfaceLevel;
      long lastUpdateY = -9223372036854775807L;
      final MutableBlockPos pos = new MutableBlockPos();
      Supplier<Holder<Biome>> biome;
      int blockY;
      int waterHeight;
      int stoneDepthBelow;
      int stoneDepthAbove;

      protected Context(
         SurfaceSystem $$0,
         RandomState $$1,
         ChunkAccess $$2,
         NoiseChunk $$3,
         Function<BlockPos, Holder<Biome>> $$4,
         Registry<Biome> $$5,
         WorldGenerationContext $$6
      ) {
         this.system = $$0;
         this.randomState = $$1;
         this.chunk = $$2;
         this.noiseChunk = $$3;
         this.biomeGetter = $$4;
         this.context = $$6;
      }

      protected void updateXZ(int $$0, int $$1) {
         this.lastUpdateXZ++;
         this.lastUpdateY++;
         this.blockX = $$0;
         this.blockZ = $$1;
         this.surfaceDepth = this.system.getSurfaceDepth($$0, $$1);
      }

      protected void updateY(int $$0, int $$1, int $$2, int $$3, int $$4, int $$5) {
         this.lastUpdateY++;
         this.biome = Suppliers.memoize(() -> this.biomeGetter.apply(this.pos.set($$3, $$4, $$5)));
         this.blockY = $$4;
         this.waterHeight = $$2;
         this.stoneDepthBelow = $$1;
         this.stoneDepthAbove = $$0;
      }

      protected double getSurfaceSecondary() {
         if (this.lastSurfaceDepth2Update != this.lastUpdateXZ) {
            this.lastSurfaceDepth2Update = this.lastUpdateXZ;
            this.surfaceSecondary = this.system.getSurfaceSecondary(this.blockX, this.blockZ);
         }

         return this.surfaceSecondary;
      }

      public int getSeaLevel() {
         return this.system.getSeaLevel();
      }

      private static int blockCoordToSurfaceCell(int $$0) {
         return $$0 >> 4;
      }

      private static int surfaceCellToBlockCoord(int $$0) {
         return $$0 << 4;
      }

      protected int getMinSurfaceLevel() {
         if (this.lastMinSurfaceLevelUpdate != this.lastUpdateXZ) {
            this.lastMinSurfaceLevelUpdate = this.lastUpdateXZ;
            int $$0 = blockCoordToSurfaceCell(this.blockX);
            int $$1 = blockCoordToSurfaceCell(this.blockZ);
            long $$2 = net.minecraft.world.level.ChunkPos.asLong($$0, $$1);
            if (this.lastPreliminarySurfaceCellOrigin != $$2) {
               this.lastPreliminarySurfaceCellOrigin = $$2;
               this.preliminarySurfaceCache[0] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord($$0), surfaceCellToBlockCoord($$1));
               this.preliminarySurfaceCache[1] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord($$0 + 1), surfaceCellToBlockCoord($$1));
               this.preliminarySurfaceCache[2] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord($$0), surfaceCellToBlockCoord($$1 + 1));
               this.preliminarySurfaceCache[3] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord($$0 + 1), surfaceCellToBlockCoord($$1 + 1));
            }

            int $$3 = Mth.floor(
               Mth.lerp2(
                  (this.blockX & 15) / 16.0F,
                  (this.blockZ & 15) / 16.0F,
                  this.preliminarySurfaceCache[0],
                  this.preliminarySurfaceCache[1],
                  this.preliminarySurfaceCache[2],
                  this.preliminarySurfaceCache[3]
               )
            );
            this.minSurfaceLevel = $$3 + this.surfaceDepth - 8;
         }

         return this.minSurfaceLevel;
      }

      final class AbovePreliminarySurfaceCondition implements SurfaceRules.Condition {
         @Override
         public boolean test() {
            return Context.this.blockY >= Context.this.getMinSurfaceLevel();
         }
      }

      static final class HoleCondition extends SurfaceRules.LazyXZCondition {
         HoleCondition(SurfaceRules.Context $$0) {
            super($$0);
         }

         @Override
         protected boolean compute() {
            return this.context.surfaceDepth <= 0;
         }
      }

      static class SteepMaterialCondition extends SurfaceRules.LazyXZCondition {
         SteepMaterialCondition(SurfaceRules.Context $$0) {
            super($$0);
         }

         @Override
         protected boolean compute() {
            int $$0 = this.context.blockX & 15;
            int $$1 = this.context.blockZ & 15;
            int $$2 = Math.max($$1 - 1, 0);
            int $$3 = Math.min($$1 + 1, 15);
            ChunkAccess $$4 = this.context.chunk;
            int $$5 = $$4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, $$0, $$2);
            int $$6 = $$4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, $$0, $$3);
            if ($$6 >= $$5 + 4) {
               return true;
            } else {
               int $$7 = Math.max($$0 - 1, 0);
               int $$8 = Math.min($$0 + 1, 15);
               int $$9 = $$4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, $$7, $$1);
               int $$10 = $$4.getHeight(Heightmap.Types.WORLD_SURFACE_WG, $$8, $$1);
               return $$9 >= $$10 + 4;
            }
         }
      }

      static class TemperatureHelperCondition extends SurfaceRules.LazyYCondition {
         TemperatureHelperCondition(SurfaceRules.Context $$0) {
            super($$0);
         }

         @Override
         protected boolean compute() {
            return ((Biome)this.context.biome.get().value())
               .coldEnoughToSnow(this.context.pos.set(this.context.blockX, this.context.blockY, this.context.blockZ), this.context.getSeaLevel());
         }
      }
   }

   static enum Hole implements SurfaceRules.ConditionSource {
      INSTANCE;

      static final KeyDispatchDataCodec<SurfaceRules.Hole> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(SurfaceRules.Context $$0) {
         return $$0.hole;
      }
   }

   abstract static class LazyCondition implements SurfaceRules.Condition {
      protected final SurfaceRules.Context context;
      private long lastUpdate;
      
      Boolean result;

      protected LazyCondition(SurfaceRules.Context $$0) {
         this.context = $$0;
         this.lastUpdate = this.getContextLastUpdate() - 1L;
      }

      @Override
      public boolean test() {
         long $$0 = this.getContextLastUpdate();
         if ($$0 == this.lastUpdate) {
            if (this.result == null) {
               throw new IllegalStateException("Update triggered but the result is null");
            } else {
               return this.result;
            }
         } else {
            this.lastUpdate = $$0;
            this.result = this.compute();
            return this.result;
         }
      }

      protected abstract long getContextLastUpdate();

      protected abstract boolean compute();
   }

   abstract static class LazyXZCondition extends SurfaceRules.LazyCondition {
      protected LazyXZCondition(SurfaceRules.Context $$0) {
         super($$0);
      }

      @Override
      protected long getContextLastUpdate() {
         return this.context.lastUpdateXZ;
      }
   }

   abstract static class LazyYCondition extends SurfaceRules.LazyCondition {
      protected LazyYCondition(SurfaceRules.Context $$0) {
         super($$0);
      }

      @Override
      protected long getContextLastUpdate() {
         return this.context.lastUpdateY;
      }
   }

   record NoiseThresholdConditionSource(ResourceKey<NormalNoise.NoiseParameters> noise, double minThreshold, double maxThreshold)
      implements SurfaceRules.ConditionSource {
      static final KeyDispatchDataCodec<SurfaceRules.NoiseThresholdConditionSource> CODEC = KeyDispatchDataCodec.of(
         RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                  ResourceKey.codec(Registries.NOISE).fieldOf("noise").forGetter(SurfaceRules.NoiseThresholdConditionSource::noise),
                  Codec.DOUBLE.fieldOf("min_threshold").forGetter(SurfaceRules.NoiseThresholdConditionSource::minThreshold),
                  Codec.DOUBLE.fieldOf("max_threshold").forGetter(SurfaceRules.NoiseThresholdConditionSource::maxThreshold)
               )
               .apply($$0, SurfaceRules.NoiseThresholdConditionSource::new)
         )
      );

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(final SurfaceRules.Context $$0) {
         final NormalNoise $$1 = $$0.randomState.getOrCreateNoise(this.noise);

         class NoiseThresholdCondition extends SurfaceRules.LazyXZCondition {
            NoiseThresholdCondition() {
               super($$0);
            }

            @Override
            protected boolean compute() {
               double $$0 = $$1.getValue(this.context.blockX, 0.0, this.context.blockZ);
               return $$0 >= NoiseThresholdConditionSource.this.minThreshold && $$0 <= NoiseThresholdConditionSource.this.maxThreshold;
            }
         }

         return new NoiseThresholdCondition();
      }
   }

   record NotCondition(SurfaceRules.Condition target) implements SurfaceRules.Condition {
      @Override
      public boolean test() {
         return !this.target.test();
      }
   }

   record NotConditionSource(SurfaceRules.ConditionSource target) implements SurfaceRules.ConditionSource {
      static final KeyDispatchDataCodec<SurfaceRules.NotConditionSource> CODEC = KeyDispatchDataCodec.of(
         SurfaceRules.ConditionSource.CODEC.xmap(SurfaceRules.NotConditionSource::new, SurfaceRules.NotConditionSource::target).fieldOf("invert")
      );

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(SurfaceRules.Context $$0) {
         return new SurfaceRules.NotCondition(this.target.apply($$0));
      }
   }

   public interface RuleSource extends Function<SurfaceRules.Context, SurfaceRules.SurfaceRule> {
      Codec<SurfaceRules.RuleSource> CODEC = BuiltInRegistries.MATERIAL_RULE.byNameCodec().dispatch($$0 -> $$0.codec().codec(), Function.identity());

      static MapCodec<? extends SurfaceRules.RuleSource> bootstrap(Registry<MapCodec<? extends SurfaceRules.RuleSource>> $$0) {
         SurfaceRules.register($$0, "bandlands", SurfaceRules.Bandlands.CODEC);
         SurfaceRules.register($$0, "block", SurfaceRules.BlockRuleSource.CODEC);
         SurfaceRules.register($$0, "sequence", SurfaceRules.SequenceRuleSource.CODEC);
         return SurfaceRules.register($$0, "condition", SurfaceRules.TestRuleSource.CODEC);
      }

      KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec();
   }

   record SequenceRule(List<SurfaceRules.SurfaceRule> rules) implements SurfaceRules.SurfaceRule {
      
      @Override
      public BlockState tryApply(int $$0, int $$1, int $$2) {
         for (SurfaceRules.SurfaceRule $$3 : this.rules) {
            BlockState $$4 = $$3.tryApply($$0, $$1, $$2);
            if ($$4 != null) {
               return $$4;
            }
         }

         return null;
      }
   }

   record SequenceRuleSource(List<SurfaceRules.RuleSource> sequence) implements SurfaceRules.RuleSource {
      static final KeyDispatchDataCodec<SurfaceRules.SequenceRuleSource> CODEC = KeyDispatchDataCodec.of(
         SurfaceRules.RuleSource.CODEC.listOf().xmap(SurfaceRules.SequenceRuleSource::new, SurfaceRules.SequenceRuleSource::sequence).fieldOf("sequence")
      );

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
         return CODEC;
      }

      public SurfaceRules.SurfaceRule apply(SurfaceRules.Context $$0) {
         if (this.sequence.size() == 1) {
            return this.sequence.get(0).apply($$0);
         } else {
            Builder<SurfaceRules.SurfaceRule> $$1 = ImmutableList.builder();

            for (SurfaceRules.RuleSource $$2 : this.sequence) {
               $$1.add($$2.apply($$0));
            }

            return new SurfaceRules.SequenceRule($$1.build());
         }
      }
   }

   record StateRule(BlockState state) implements SurfaceRules.SurfaceRule {
      @Override
      public BlockState tryApply(int $$0, int $$1, int $$2) {
         return this.state;
      }
   }

   static enum Steep implements SurfaceRules.ConditionSource {
      INSTANCE;

      static final KeyDispatchDataCodec<SurfaceRules.Steep> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(SurfaceRules.Context $$0) {
         return $$0.steep;
      }
   }

   record StoneDepthCheck(int offset, boolean addSurfaceDepth, int secondaryDepthRange, CaveSurface surfaceType) implements SurfaceRules.ConditionSource {
      static final KeyDispatchDataCodec<SurfaceRules.StoneDepthCheck> CODEC = KeyDispatchDataCodec.of(
         RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                  Codec.INT.fieldOf("offset").forGetter(SurfaceRules.StoneDepthCheck::offset),
                  Codec.BOOL.fieldOf("add_surface_depth").forGetter(SurfaceRules.StoneDepthCheck::addSurfaceDepth),
                  Codec.INT.fieldOf("secondary_depth_range").forGetter(SurfaceRules.StoneDepthCheck::secondaryDepthRange),
                  CaveSurface.CODEC.fieldOf("surface_type").forGetter(SurfaceRules.StoneDepthCheck::surfaceType)
               )
               .apply($$0, SurfaceRules.StoneDepthCheck::new)
         )
      );

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(final SurfaceRules.Context $$0) {
         final boolean $$1 = this.surfaceType == CaveSurface.CEILING;

         class StoneDepthCondition extends SurfaceRules.LazyYCondition {
            StoneDepthCondition() {
               super($$0);
            }

            @Override
            protected boolean compute() {
               int $$0 = $$1 ? this.context.stoneDepthBelow : this.context.stoneDepthAbove;
               int $$1 = StoneDepthCheck.this.addSurfaceDepth ? this.context.surfaceDepth : 0;
               int $$2 = StoneDepthCheck.this.secondaryDepthRange == 0
                  ? 0
                  : (int)Mth.map(this.context.getSurfaceSecondary(), -1.0, 1.0, 0.0, StoneDepthCheck.this.secondaryDepthRange);
               return $$0 <= 1 + StoneDepthCheck.this.offset + $$1 + $$2;
            }
         }

         return new StoneDepthCondition();
      }
   }

   protected interface SurfaceRule {
      
      BlockState tryApply(int var1, int var2, int var3);
   }

   static enum Temperature implements SurfaceRules.ConditionSource {
      INSTANCE;

      static final KeyDispatchDataCodec<SurfaceRules.Temperature> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(SurfaceRules.Context $$0) {
         return $$0.temperature;
      }
   }

   record TestRule(SurfaceRules.Condition condition, SurfaceRules.SurfaceRule followup) implements SurfaceRules.SurfaceRule {
      
      @Override
      public BlockState tryApply(int $$0, int $$1, int $$2) {
         return !this.condition.test() ? null : this.followup.tryApply($$0, $$1, $$2);
      }
   }

   record TestRuleSource(SurfaceRules.ConditionSource ifTrue, SurfaceRules.RuleSource thenRun) implements SurfaceRules.RuleSource {
      static final KeyDispatchDataCodec<SurfaceRules.TestRuleSource> CODEC = KeyDispatchDataCodec.of(
         RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                  SurfaceRules.ConditionSource.CODEC.fieldOf("if_true").forGetter(SurfaceRules.TestRuleSource::ifTrue),
                  SurfaceRules.RuleSource.CODEC.fieldOf("then_run").forGetter(SurfaceRules.TestRuleSource::thenRun)
               )
               .apply($$0, SurfaceRules.TestRuleSource::new)
         )
      );

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
         return CODEC;
      }

      public SurfaceRules.SurfaceRule apply(SurfaceRules.Context $$0) {
         return new SurfaceRules.TestRule(this.ifTrue.apply($$0), this.thenRun.apply($$0));
      }
   }

   record VerticalGradientConditionSource(Identifier randomName, VerticalAnchor trueAtAndBelow, VerticalAnchor falseAtAndAbove)
      implements SurfaceRules.ConditionSource {
      static final KeyDispatchDataCodec<SurfaceRules.VerticalGradientConditionSource> CODEC = KeyDispatchDataCodec.of(
         RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                  Identifier.CODEC.fieldOf("random_name").forGetter(SurfaceRules.VerticalGradientConditionSource::randomName),
                  VerticalAnchor.CODEC.fieldOf("true_at_and_below").forGetter(SurfaceRules.VerticalGradientConditionSource::trueAtAndBelow),
                  VerticalAnchor.CODEC.fieldOf("false_at_and_above").forGetter(SurfaceRules.VerticalGradientConditionSource::falseAtAndAbove)
               )
               .apply($$0, SurfaceRules.VerticalGradientConditionSource::new)
         )
      );

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(final SurfaceRules.Context $$0) {
         final int $$1 = this.trueAtAndBelow().resolveY($$0.context);
         final int $$2 = this.falseAtAndAbove().resolveY($$0.context);
         final PositionalRandomFactory $$3 = $$0.randomState.getOrCreateRandomFactory(this.randomName());

         class VerticalGradientCondition extends SurfaceRules.LazyYCondition {
            VerticalGradientCondition() {
               super($$0);
            }

            @Override
            protected boolean compute() {
               int $$0 = this.context.blockY;
               if ($$0 <= $$1) {
                  return true;
               } else if ($$0 >= $$2) {
                  return false;
               } else {
                  double $$1 = Mth.map($$0, $$1, $$2, 1.0, 0.0);
                  RandomSource $$2 = $$3.at(this.context.blockX, $$0, this.context.blockZ);
                  return $$2.nextFloat() < $$1;
               }
            }
         }

         return new VerticalGradientCondition();
      }
   }

   record WaterConditionSource(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) implements SurfaceRules.ConditionSource {
      static final KeyDispatchDataCodec<SurfaceRules.WaterConditionSource> CODEC = KeyDispatchDataCodec.of(
         RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                  Codec.INT.fieldOf("offset").forGetter(SurfaceRules.WaterConditionSource::offset),
                  Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier").forGetter(SurfaceRules.WaterConditionSource::surfaceDepthMultiplier),
                  Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.WaterConditionSource::addStoneDepth)
               )
               .apply($$0, SurfaceRules.WaterConditionSource::new)
         )
      );

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(final SurfaceRules.Context $$0) {
         class WaterCondition extends SurfaceRules.LazyYCondition {
            WaterCondition() {
               super($$0);
            }

            @Override
            protected boolean compute() {
               return this.context.waterHeight == Integer.MIN_VALUE
                  || this.context.blockY + (WaterConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0)
                     >= this.context.waterHeight
                        + WaterConditionSource.this.offset
                        + this.context.surfaceDepth * WaterConditionSource.this.surfaceDepthMultiplier;
            }
         }

         return new WaterCondition();
      }
   }

   record YConditionSource(VerticalAnchor anchor, int surfaceDepthMultiplier, boolean addStoneDepth) implements SurfaceRules.ConditionSource {
      static final KeyDispatchDataCodec<SurfaceRules.YConditionSource> CODEC = KeyDispatchDataCodec.of(
         RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                  VerticalAnchor.CODEC.fieldOf("anchor").forGetter(SurfaceRules.YConditionSource::anchor),
                  Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier").forGetter(SurfaceRules.YConditionSource::surfaceDepthMultiplier),
                  Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.YConditionSource::addStoneDepth)
               )
               .apply($$0, SurfaceRules.YConditionSource::new)
         )
      );

      @Override
      public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
         return CODEC;
      }

      public SurfaceRules.Condition apply(final SurfaceRules.Context $$0) {
         class YCondition extends SurfaceRules.LazyYCondition {
            YCondition() {
               super($$0);
            }

            @Override
            protected boolean compute() {
               return this.context.blockY + (YConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0)
                  >= YConditionSource.this.anchor.resolveY(this.context.context) + this.context.surfaceDepth * YConditionSource.this.surfaceDepthMultiplier;
            }
         }

         return new YCondition();
      }
   }
}
