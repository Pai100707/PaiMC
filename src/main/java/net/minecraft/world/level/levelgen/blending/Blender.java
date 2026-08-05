package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

public class Blender {
   private static final Blender EMPTY = new Blender(new Long2ObjectOpenHashMap(), new Long2ObjectOpenHashMap()) {
      @Override
      public Blender.BlendingOutput blendOffsetAndFactor(int $$0, int $$1) {
         return new Blender.BlendingOutput(1.0, 0.0);
      }

      @Override
      public double blendDensity(DensityFunction.FunctionContext $$0, double $$1) {
         return $$1;
      }

      @Override
      public BiomeResolver getBiomeResolver(BiomeResolver $$0) {
         return $$0;
      }
   };
   private static final NormalNoise SHIFT_NOISE = NormalNoise.create(new XoroshiroRandomSource(42L), NoiseData.DEFAULT_SHIFT);
   private static final int HEIGHT_BLENDING_RANGE_CELLS = QuartPos.fromSection(7) - 1;
   private static final int HEIGHT_BLENDING_RANGE_CHUNKS = QuartPos.toSection(HEIGHT_BLENDING_RANGE_CELLS + 3);
   private static final int DENSITY_BLENDING_RANGE_CELLS = 2;
   private static final int DENSITY_BLENDING_RANGE_CHUNKS = QuartPos.toSection(5);
   private static final double OLD_CHUNK_XZ_RADIUS = 8.0;
   private final Long2ObjectOpenHashMap<BlendingData> heightAndBiomeBlendingData;
   private final Long2ObjectOpenHashMap<BlendingData> densityBlendingData;

   public static Blender empty() {
      return EMPTY;
   }

   public static Blender of(WorldGenRegion $$0) {
      if (!SharedConstants.DEBUG_DISABLE_BLENDING && $$0 != null) {
         net.minecraft.world.level.ChunkPos $$1 = $$0.getCenter();
         if (!$$0.isOldChunkAround($$1, HEIGHT_BLENDING_RANGE_CHUNKS)) {
            return EMPTY;
         } else {
            Long2ObjectOpenHashMap<BlendingData> $$2 = new Long2ObjectOpenHashMap();
            Long2ObjectOpenHashMap<BlendingData> $$3 = new Long2ObjectOpenHashMap();
            int $$4 = Mth.square(HEIGHT_BLENDING_RANGE_CHUNKS + 1);

            for (int $$5 = -HEIGHT_BLENDING_RANGE_CHUNKS; $$5 <= HEIGHT_BLENDING_RANGE_CHUNKS; $$5++) {
               for (int $$6 = -HEIGHT_BLENDING_RANGE_CHUNKS; $$6 <= HEIGHT_BLENDING_RANGE_CHUNKS; $$6++) {
                  if ($$5 * $$5 + $$6 * $$6 <= $$4) {
                     int $$7 = $$1.x + $$5;
                     int $$8 = $$1.z + $$6;
                     BlendingData $$9 = BlendingData.getOrUpdateBlendingData($$0, $$7, $$8);
                     if ($$9 != null) {
                        $$2.put(net.minecraft.world.level.ChunkPos.asLong($$7, $$8), $$9);
                        if ($$5 >= -DENSITY_BLENDING_RANGE_CHUNKS
                           && $$5 <= DENSITY_BLENDING_RANGE_CHUNKS
                           && $$6 >= -DENSITY_BLENDING_RANGE_CHUNKS
                           && $$6 <= DENSITY_BLENDING_RANGE_CHUNKS) {
                           $$3.put(net.minecraft.world.level.ChunkPos.asLong($$7, $$8), $$9);
                        }
                     }
                  }
               }
            }

            return $$2.isEmpty() && $$3.isEmpty() ? EMPTY : new Blender($$2, $$3);
         }
      } else {
         return EMPTY;
      }
   }

   Blender(Long2ObjectOpenHashMap<BlendingData> $$0, Long2ObjectOpenHashMap<BlendingData> $$1) {
      this.heightAndBiomeBlendingData = $$0;
      this.densityBlendingData = $$1;
   }

   public boolean isEmpty() {
      return this.heightAndBiomeBlendingData.isEmpty() && this.densityBlendingData.isEmpty();
   }

   public Blender.BlendingOutput blendOffsetAndFactor(int $$0, int $$1) {
      int $$2 = QuartPos.fromBlock($$0);
      int $$3 = QuartPos.fromBlock($$1);
      double $$4 = this.getBlendingDataValue($$2, 0, $$3, BlendingData::getHeight);
      if ($$4 != Double.MAX_VALUE) {
         return new Blender.BlendingOutput(0.0, heightToOffset($$4));
      } else {
         MutableDouble $$5 = new MutableDouble(0.0);
         MutableDouble $$6 = new MutableDouble(0.0);
         MutableDouble $$7 = new MutableDouble(Double.POSITIVE_INFINITY);
         this.heightAndBiomeBlendingData
            .forEach(
               ($$5x, $$6x) -> $$6x.iterateHeights(
                  QuartPos.fromSection(net.minecraft.world.level.ChunkPos.getX($$5x)),
                  QuartPos.fromSection(net.minecraft.world.level.ChunkPos.getZ($$5x)),
                  ($$5xx, $$6xx, $$7x) -> {
                     double $$8x = Mth.length($$2 - $$5xx, $$3 - $$6xx);
                     if (!($$8x > HEIGHT_BLENDING_RANGE_CELLS)) {
                        if ($$8x < $$7.doubleValue()) {
                           $$7.setValue($$8x);
                        }

                        double $$9x = 1.0 / ($$8x * $$8x * $$8x * $$8x);
                        $$6.add($$7x * $$9x);
                        $$5.add($$9x);
                     }
                  }
               )
            );
         if ($$7.doubleValue() == Double.POSITIVE_INFINITY) {
            return new Blender.BlendingOutput(1.0, 0.0);
         } else {
            double $$8 = $$6.doubleValue() / $$5.doubleValue();
            double $$9 = Mth.clamp($$7.doubleValue() / (HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
            $$9 = 3.0 * $$9 * $$9 - 2.0 * $$9 * $$9 * $$9;
            return new Blender.BlendingOutput($$9, heightToOffset($$8));
         }
      }
   }

   private static double heightToOffset(double $$0) {
      double $$1 = 1.0;
      double $$2 = $$0 + 0.5;
      double $$3 = Mth.positiveModulo($$2, 8.0);
      return 1.0 * (32.0 * ($$2 - 128.0) - 3.0 * ($$2 - 120.0) * $$3 + 3.0 * $$3 * $$3) / (128.0 * (32.0 - 3.0 * $$3));
   }

   public double blendDensity(DensityFunction.FunctionContext $$0, double $$1) {
      int $$2 = QuartPos.fromBlock($$0.blockX());
      int $$3 = $$0.blockY() / 8;
      int $$4 = QuartPos.fromBlock($$0.blockZ());
      double $$5 = this.getBlendingDataValue($$2, $$3, $$4, BlendingData::getDensity);
      if ($$5 != Double.MAX_VALUE) {
         return $$5;
      } else {
         MutableDouble $$6 = new MutableDouble(0.0);
         MutableDouble $$7 = new MutableDouble(0.0);
         MutableDouble $$8 = new MutableDouble(Double.POSITIVE_INFINITY);
         this.densityBlendingData
            .forEach(
               ($$6x, $$7x) -> $$7x.iterateDensities(
                  QuartPos.fromSection(net.minecraft.world.level.ChunkPos.getX($$6x)),
                  QuartPos.fromSection(net.minecraft.world.level.ChunkPos.getZ($$6x)),
                  $$3 - 1,
                  $$3 + 1,
                  ($$6xx, $$7xx, $$8x, $$9x) -> {
                     double $$10x = Mth.length($$2 - $$6xx, ($$3 - $$7xx) * 2, $$4 - $$8x);
                     if (!($$10x > 2.0)) {
                        if ($$10x < $$8.doubleValue()) {
                           $$8.setValue($$10x);
                        }

                        double $$11 = 1.0 / ($$10x * $$10x * $$10x * $$10x);
                        $$7.add($$9x * $$11);
                        $$6.add($$11);
                     }
                  }
               )
            );
         if ($$8.doubleValue() == Double.POSITIVE_INFINITY) {
            return $$1;
         } else {
            double $$9 = $$7.doubleValue() / $$6.doubleValue();
            double $$10 = Mth.clamp($$8.doubleValue() / 3.0, 0.0, 1.0);
            return Mth.lerp($$10, $$9, $$1);
         }
      }
   }

   private double getBlendingDataValue(int $$0, int $$1, int $$2, Blender.CellValueGetter $$3) {
      int $$4 = QuartPos.toSection($$0);
      int $$5 = QuartPos.toSection($$2);
      boolean $$6 = ($$0 & 3) == 0;
      boolean $$7 = ($$2 & 3) == 0;
      double $$8 = this.getBlendingDataValue($$3, $$4, $$5, $$0, $$1, $$2);
      if ($$8 == Double.MAX_VALUE) {
         if ($$6 && $$7) {
            $$8 = this.getBlendingDataValue($$3, $$4 - 1, $$5 - 1, $$0, $$1, $$2);
         }

         if ($$8 == Double.MAX_VALUE) {
            if ($$6) {
               $$8 = this.getBlendingDataValue($$3, $$4 - 1, $$5, $$0, $$1, $$2);
            }

            if ($$8 == Double.MAX_VALUE && $$7) {
               $$8 = this.getBlendingDataValue($$3, $$4, $$5 - 1, $$0, $$1, $$2);
            }
         }
      }

      return $$8;
   }

   private double getBlendingDataValue(Blender.CellValueGetter $$0, int $$1, int $$2, int $$3, int $$4, int $$5) {
      BlendingData $$6 = (BlendingData)this.heightAndBiomeBlendingData.get(net.minecraft.world.level.ChunkPos.asLong($$1, $$2));
      return $$6 != null ? $$0.get($$6, $$3 - QuartPos.fromSection($$1), $$4, $$5 - QuartPos.fromSection($$2)) : Double.MAX_VALUE;
   }

   public BiomeResolver getBiomeResolver(BiomeResolver $$0) {
      return ($$1, $$2, $$3, $$4) -> {
         Holder<Biome> $$5 = this.blendBiome($$1, $$2, $$3);
         return $$5 == null ? $$0.getNoiseBiome($$1, $$2, $$3, $$4) : $$5;
      };
   }

   private Holder<Biome> blendBiome(int $$0, int $$1, int $$2) {
      MutableDouble $$3 = new MutableDouble(Double.POSITIVE_INFINITY);
      MutableObject<Holder<Biome>> $$4 = new MutableObject();
      this.heightAndBiomeBlendingData
         .forEach(
            ($$5x, $$6x) -> $$6x.iterateBiomes(
               QuartPos.fromSection(net.minecraft.world.level.ChunkPos.getX($$5x)),
               $$1,
               QuartPos.fromSection(net.minecraft.world.level.ChunkPos.getZ($$5x)),
               ($$4xx, $$5xx, $$6xx) -> {
                  double $$7 = Mth.length($$0 - $$4xx, $$2 - $$5xx);
                  if (!($$7 > HEIGHT_BLENDING_RANGE_CELLS)) {
                     if ($$7 < $$3.doubleValue()) {
                        $$4.setValue($$6xx);
                        $$3.setValue($$7);
                     }
                  }
               }
            )
         );
      if ($$3.doubleValue() == Double.POSITIVE_INFINITY) {
         return null;
      } else {
         double $$5 = SHIFT_NOISE.getValue($$0, 0.0, $$2) * 12.0;
         double $$6 = Mth.clamp(($$3.doubleValue() + $$5) / (HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
         return $$6 > 0.5 ? null : (Holder)$$4.get();
      }
   }

   public static void generateBorderTicks(WorldGenRegion $$0, ChunkAccess $$1) {
      if (!SharedConstants.DEBUG_DISABLE_BLENDING) {
         net.minecraft.world.level.ChunkPos $$2 = $$1.getPos();
         boolean $$3 = $$1.isOldNoiseGeneration();
         MutableBlockPos $$4 = new MutableBlockPos();
         BlockPos $$5 = new BlockPos($$2.getMinBlockX(), 0, $$2.getMinBlockZ());
         BlendingData $$6 = $$1.getBlendingData();
         if ($$6 != null) {
            int $$7 = $$6.getAreaWithOldGeneration().getMinY();
            int $$8 = $$6.getAreaWithOldGeneration().getMaxY();
            if ($$3) {
               for (int $$9 = 0; $$9 < 16; $$9++) {
                  for (int $$10 = 0; $$10 < 16; $$10++) {
                     generateBorderTick($$1, $$4.setWithOffset($$5, $$9, $$7 - 1, $$10));
                     generateBorderTick($$1, $$4.setWithOffset($$5, $$9, $$7, $$10));
                     generateBorderTick($$1, $$4.setWithOffset($$5, $$9, $$8, $$10));
                     generateBorderTick($$1, $$4.setWithOffset($$5, $$9, $$8 + 1, $$10));
                  }
               }
            }

            for (Direction $$11 : Plane.HORIZONTAL) {
               if ($$0.getChunk($$2.x + $$11.getStepX(), $$2.z + $$11.getStepZ()).isOldNoiseGeneration() != $$3) {
                  int $$12 = $$11 == Direction.EAST ? 15 : 0;
                  int $$13 = $$11 == Direction.WEST ? 0 : 15;
                  int $$14 = $$11 == Direction.SOUTH ? 15 : 0;
                  int $$15 = $$11 == Direction.NORTH ? 0 : 15;

                  for (int $$16 = $$12; $$16 <= $$13; $$16++) {
                     for (int $$17 = $$14; $$17 <= $$15; $$17++) {
                        int $$18 = Math.min($$8, $$1.getHeight(Heightmap.Types.MOTION_BLOCKING, $$16, $$17)) + 1;

                        for (int $$19 = $$7; $$19 < $$18; $$19++) {
                           generateBorderTick($$1, $$4.setWithOffset($$5, $$16, $$19, $$17));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void generateBorderTick(ChunkAccess $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      if ($$2.is(BlockTags.LEAVES)) {
         $$0.markPosForPostprocessing($$1);
      }

      FluidState $$3 = $$0.getFluidState($$1);
      if (!$$3.isEmpty()) {
         $$0.markPosForPostprocessing($$1);
      }
   }

   public static void addAroundOldChunksCarvingMaskFilter(net.minecraft.world.level.WorldGenLevel $$0, ProtoChunk $$1) {
      if (!SharedConstants.DEBUG_DISABLE_BLENDING) {
         net.minecraft.world.level.ChunkPos $$2 = $$1.getPos();
         Builder<Direction8, BlendingData> $$3 = ImmutableMap.builder();

         for (Direction8 $$4 : Direction8.values()) {
            int $$5 = $$2.x + $$4.getStepX();
            int $$6 = $$2.z + $$4.getStepZ();
            BlendingData $$7 = $$0.getChunk($$5, $$6).getBlendingData();
            if ($$7 != null) {
               $$3.put($$4, $$7);
            }
         }

         ImmutableMap<Direction8, BlendingData> $$8 = $$3.build();
         if ($$1.isOldNoiseGeneration() || !$$8.isEmpty()) {
            Blender.DistanceGetter $$9 = makeOldChunkDistanceGetter($$1.getBlendingData(), $$8);
            CarvingMask.Mask $$10 = ($$1x, $$2x, $$3x) -> {
               double $$4x = $$1x + 0.5 + SHIFT_NOISE.getValue($$1x, $$2x, $$3x) * 4.0;
               double $$5x = $$2x + 0.5 + SHIFT_NOISE.getValue($$2x, $$3x, $$1x) * 4.0;
               double $$6x = $$3x + 0.5 + SHIFT_NOISE.getValue($$3x, $$1x, $$2x) * 4.0;
               return $$9.getDistance($$4x, $$5x, $$6x) < 4.0;
            };
            $$1.getOrCreateCarvingMask().setAdditionalMask($$10);
         }
      }
   }

   public static Blender.DistanceGetter makeOldChunkDistanceGetter(BlendingData $$0, Map<Direction8, BlendingData> $$1) {
      List<Blender.DistanceGetter> $$2 = Lists.newArrayList();
      if ($$0 != null) {
         $$2.add(makeOffsetOldChunkDistanceGetter(null, $$0));
      }

      $$1.forEach(($$1x, $$2x) -> $$2.add(makeOffsetOldChunkDistanceGetter($$1x, $$2x)));
      return ($$1x, $$2x, $$3) -> {
         double $$4 = Double.POSITIVE_INFINITY;

         for (Blender.DistanceGetter $$5 : $$2) {
            double $$6 = $$5.getDistance($$1x, $$2x, $$3);
            if ($$6 < $$4) {
               $$4 = $$6;
            }
         }

         return $$4;
      };
   }

   private static Blender.DistanceGetter makeOffsetOldChunkDistanceGetter(Direction8 $$0, BlendingData $$1) {
      double $$2 = 0.0;
      double $$3 = 0.0;
      if ($$0 != null) {
         for (Direction $$4 : $$0.getDirections()) {
            $$2 += $$4.getStepX() * 16;
            $$3 += $$4.getStepZ() * 16;
         }
      }

      double $$5 = $$2;
      double $$6 = $$3;
      double $$7 = $$1.getAreaWithOldGeneration().getHeight() / 2.0;
      double $$8 = $$1.getAreaWithOldGeneration().getMinY() + $$7;
      return ($$4x, $$5x, $$6x) -> distanceToCube($$4x - 8.0 - $$5, $$5x - $$8, $$6x - 8.0 - $$6, 8.0, $$7, 8.0);
   }

   private static double distanceToCube(double $$0, double $$1, double $$2, double $$3, double $$4, double $$5) {
      double $$6 = Math.abs($$0) - $$3;
      double $$7 = Math.abs($$1) - $$4;
      double $$8 = Math.abs($$2) - $$5;
      return Mth.length(Math.max(0.0, $$6), Math.max(0.0, $$7), Math.max(0.0, $$8));
   }

   public record BlendingOutput(double alpha, double blendingOffset) {
   }

   interface CellValueGetter {
      double get(BlendingData var1, int var2, int var3, int var4);
   }

   public interface DistanceGetter {
      double getDistance(double var1, double var3, double var5);
   }
}
