package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceSystem {
   private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
   private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
   private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
   private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
   private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
   private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
   private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
   private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
   private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
   private final BlockState defaultBlock;
   private final int seaLevel;
   private final BlockState[] clayBands;
   private final NormalNoise clayBandsOffsetNoise;
   private final NormalNoise badlandsPillarNoise;
   private final NormalNoise badlandsPillarRoofNoise;
   private final NormalNoise badlandsSurfaceNoise;
   private final NormalNoise icebergPillarNoise;
   private final NormalNoise icebergPillarRoofNoise;
   private final NormalNoise icebergSurfaceNoise;
   private final PositionalRandomFactory noiseRandom;
   private final NormalNoise surfaceNoise;
   private final NormalNoise surfaceSecondaryNoise;

   public SurfaceSystem(RandomState $$0, BlockState $$1, int $$2, PositionalRandomFactory $$3) {
      this.defaultBlock = $$1;
      this.seaLevel = $$2;
      this.noiseRandom = $$3;
      this.clayBandsOffsetNoise = $$0.getOrCreateNoise(Noises.CLAY_BANDS_OFFSET);
      this.clayBands = generateBands($$3.fromHashOf(Identifier.withDefaultNamespace("clay_bands")));
      this.surfaceNoise = $$0.getOrCreateNoise(Noises.SURFACE);
      this.surfaceSecondaryNoise = $$0.getOrCreateNoise(Noises.SURFACE_SECONDARY);
      this.badlandsPillarNoise = $$0.getOrCreateNoise(Noises.BADLANDS_PILLAR);
      this.badlandsPillarRoofNoise = $$0.getOrCreateNoise(Noises.BADLANDS_PILLAR_ROOF);
      this.badlandsSurfaceNoise = $$0.getOrCreateNoise(Noises.BADLANDS_SURFACE);
      this.icebergPillarNoise = $$0.getOrCreateNoise(Noises.ICEBERG_PILLAR);
      this.icebergPillarRoofNoise = $$0.getOrCreateNoise(Noises.ICEBERG_PILLAR_ROOF);
      this.icebergSurfaceNoise = $$0.getOrCreateNoise(Noises.ICEBERG_SURFACE);
   }

   public void buildSurface(
      RandomState $$0,
      BiomeManager $$1,
      Registry<Biome> $$2,
      boolean $$3,
      WorldGenerationContext $$4,
      final ChunkAccess $$5,
      NoiseChunk $$6,
      SurfaceRules.RuleSource $$7
   ) {
      final MutableBlockPos $$8 = new MutableBlockPos();
      final net.minecraft.world.level.ChunkPos $$9 = $$5.getPos();
      int $$10 = $$9.getMinBlockX();
      int $$11 = $$9.getMinBlockZ();
      BlockColumn $$12 = new BlockColumn() {
         @Override
         public BlockState getBlock(int $$0) {
            return $$5.getBlockState($$8.setY($$0));
         }

         @Override
         public void setBlock(int $$0, BlockState $$1x) {
            net.minecraft.world.level.LevelHeightAccessor $$2x = $$5.getHeightAccessorForGeneration();
            if ($$2x.isInsideBuildHeight($$0)) {
               $$5.setBlockState($$8.setY($$0), $$1x);
               if (!$$1x.getFluidState().isEmpty()) {
                  $$5.markPosForPostprocessing($$8);
               }
            }
         }

         @Override
         public String toString() {
            return "ChunkBlockColumn " + $$9;
         }
      };
      SurfaceRules.Context $$13 = new SurfaceRules.Context(this, $$0, $$5, $$6, $$1::getBiome, $$2, $$4);
      SurfaceRules.SurfaceRule $$14 = $$7.apply($$13);
      MutableBlockPos $$15 = new MutableBlockPos();

      for (int $$16 = 0; $$16 < 16; $$16++) {
         for (int $$17 = 0; $$17 < 16; $$17++) {
            int $$18 = $$10 + $$16;
            int $$19 = $$11 + $$17;
            int $$20 = $$5.getHeight(Heightmap.Types.WORLD_SURFACE_WG, $$16, $$17) + 1;
            $$8.setX($$18).setZ($$19);
            Holder<Biome> $$21 = $$1.getBiome($$15.set($$18, $$3 ? 0 : $$20, $$19));
            if ($$21.is(Biomes.ERODED_BADLANDS)) {
               this.erodedBadlandsExtension($$12, $$18, $$19, $$20, $$5);
            }

            int $$22 = $$5.getHeight(Heightmap.Types.WORLD_SURFACE_WG, $$16, $$17) + 1;
            $$13.updateXZ($$18, $$19);
            int $$23 = 0;
            int $$24 = Integer.MIN_VALUE;
            int $$25 = Integer.MAX_VALUE;
            int $$26 = $$5.getMinY();

            for (int $$27 = $$22; $$27 >= $$26; $$27--) {
               BlockState $$28 = $$12.getBlock($$27);
               if ($$28.isAir()) {
                  $$23 = 0;
                  $$24 = Integer.MIN_VALUE;
               } else if (!$$28.getFluidState().isEmpty()) {
                  if ($$24 == Integer.MIN_VALUE) {
                     $$24 = $$27 + 1;
                  }
               } else {
                  if ($$25 >= $$27) {
                     $$25 = DimensionType.WAY_BELOW_MIN_Y;

                     for (int $$29 = $$27 - 1; $$29 >= $$26 - 1; $$29--) {
                        BlockState $$30 = $$12.getBlock($$29);
                        if (!this.isStone($$30)) {
                           $$25 = $$29 + 1;
                           break;
                        }
                     }
                  }

                  $$23++;
                  int $$31 = $$27 - $$25 + 1;
                  $$13.updateY($$23, $$31, $$24, $$18, $$27, $$19);
                  if ($$28 == this.defaultBlock) {
                     BlockState $$32 = $$14.tryApply($$18, $$27, $$19);
                     if ($$32 != null) {
                        $$12.setBlock($$27, $$32);
                     }
                  }
               }
            }

            if ($$21.is(Biomes.FROZEN_OCEAN) || $$21.is(Biomes.DEEP_FROZEN_OCEAN)) {
               this.frozenOceanExtension($$13.getMinSurfaceLevel(), (Biome)$$21.value(), $$12, $$15, $$18, $$19, $$20);
            }
         }
      }
   }

   protected int getSurfaceDepth(int $$0, int $$1) {
      double $$2 = this.surfaceNoise.getValue($$0, 0.0, $$1);
      return (int)($$2 * 2.75 + 3.0 + this.noiseRandom.at($$0, 0, $$1).nextDouble() * 0.25);
   }

   protected double getSurfaceSecondary(int $$0, int $$1) {
      return this.surfaceSecondaryNoise.getValue($$0, 0.0, $$1);
   }

   private boolean isStone(BlockState $$0) {
      return !$$0.isAir() && $$0.getFluidState().isEmpty();
   }

   public int getSeaLevel() {
      return this.seaLevel;
   }

   @Deprecated
   public Optional<BlockState> topMaterial(
      SurfaceRules.RuleSource $$0, CarvingContext $$1, Function<BlockPos, Holder<Biome>> $$2, ChunkAccess $$3, NoiseChunk $$4, BlockPos $$5, boolean $$6
   ) {
      SurfaceRules.Context $$7 = new SurfaceRules.Context(this, $$1.randomState(), $$3, $$4, $$2, $$1.registryAccess().lookupOrThrow(Registries.BIOME), $$1);
      SurfaceRules.SurfaceRule $$8 = $$0.apply($$7);
      int $$9 = $$5.getX();
      int $$10 = $$5.getY();
      int $$11 = $$5.getZ();
      $$7.updateXZ($$9, $$11);
      $$7.updateY(1, 1, $$6 ? $$10 + 1 : Integer.MIN_VALUE, $$9, $$10, $$11);
      BlockState $$12 = $$8.tryApply($$9, $$10, $$11);
      return Optional.ofNullable($$12);
   }

   private void erodedBadlandsExtension(BlockColumn $$0, int $$1, int $$2, int $$3, net.minecraft.world.level.LevelHeightAccessor $$4) {
      double $$5 = 0.2;
      double $$6 = Math.min(
         Math.abs(this.badlandsSurfaceNoise.getValue($$1, 0.0, $$2) * 8.25), this.badlandsPillarNoise.getValue($$1 * 0.2, 0.0, $$2 * 0.2) * 15.0
      );
      if (!($$6 <= 0.0)) {
         double $$7 = 0.75;
         double $$8 = 1.5;
         double $$9 = Math.abs(this.badlandsPillarRoofNoise.getValue($$1 * 0.75, 0.0, $$2 * 0.75) * 1.5);
         double $$10 = 64.0 + Math.min($$6 * $$6 * 2.5, Math.ceil($$9 * 50.0) + 24.0);
         int $$11 = Mth.floor($$10);
         if ($$3 <= $$11) {
            for (int $$12 = $$11; $$12 >= $$4.getMinY(); $$12--) {
               BlockState $$13 = $$0.getBlock($$12);
               if ($$13.is(this.defaultBlock.getBlock())) {
                  break;
               }

               if ($$13.is(Blocks.WATER)) {
                  return;
               }
            }

            for (int $$14 = $$11; $$14 >= $$4.getMinY() && $$0.getBlock($$14).isAir(); $$14--) {
               $$0.setBlock($$14, this.defaultBlock);
            }
         }
      }
   }

   private void frozenOceanExtension(int $$0, Biome $$1, BlockColumn $$2, MutableBlockPos $$3, int $$4, int $$5, int $$6) {
      double $$7 = 1.28;
      double $$8 = Math.min(
         Math.abs(this.icebergSurfaceNoise.getValue($$4, 0.0, $$5) * 8.25), this.icebergPillarNoise.getValue($$4 * 1.28, 0.0, $$5 * 1.28) * 15.0
      );
      if (!($$8 <= 1.8)) {
         double $$9 = 1.17;
         double $$10 = 1.5;
         double $$11 = Math.abs(this.icebergPillarRoofNoise.getValue($$4 * 1.17, 0.0, $$5 * 1.17) * 1.5);
         double $$12 = Math.min($$8 * $$8 * 1.2, Math.ceil($$11 * 40.0) + 14.0);
         if ($$1.shouldMeltFrozenOceanIcebergSlightly($$3.set($$4, this.seaLevel, $$5), this.seaLevel)) {
            $$12 -= 2.0;
         }

         double $$13;
         if ($$12 > 2.0) {
            $$13 = this.seaLevel - $$12 - 7.0;
            $$12 += this.seaLevel;
         } else {
            $$12 = 0.0;
            $$13 = 0.0;
         }

         double $$15 = $$12;
         RandomSource $$16 = this.noiseRandom.at($$4, 0, $$5);
         int $$17 = 2 + $$16.nextInt(4);
         int $$18 = this.seaLevel + 18 + $$16.nextInt(10);
         int $$19 = 0;

         for (int $$20 = Math.max($$6, (int)$$12 + 1); $$20 >= $$0; $$20--) {
            if ($$2.getBlock($$20).isAir() && $$20 < (int)$$15 && $$16.nextDouble() > 0.01
               || $$2.getBlock($$20).is(Blocks.WATER) && $$20 > (int)$$13 && $$20 < this.seaLevel && $$13 != 0.0 && $$16.nextDouble() > 0.15) {
               if ($$19 <= $$17 && $$20 > $$18) {
                  $$2.setBlock($$20, SNOW_BLOCK);
                  $$19++;
               } else {
                  $$2.setBlock($$20, PACKED_ICE);
               }
            }
         }
      }
   }

   private static BlockState[] generateBands(RandomSource $$0) {
      BlockState[] $$1 = new BlockState[192];
      Arrays.fill($$1, TERRACOTTA);

      for (int $$2 = 0; $$2 < $$1.length; $$2++) {
         $$2 += $$0.nextInt(5) + 1;
         if ($$2 < $$1.length) {
            $$1[$$2] = ORANGE_TERRACOTTA;
         }
      }

      makeBands($$0, $$1, 1, YELLOW_TERRACOTTA);
      makeBands($$0, $$1, 2, BROWN_TERRACOTTA);
      makeBands($$0, $$1, 1, RED_TERRACOTTA);
      int $$3 = $$0.nextIntBetweenInclusive(9, 15);
      int $$4 = 0;

      for (int $$5 = 0; $$4 < $$3 && $$5 < $$1.length; $$5 += $$0.nextInt(16) + 4) {
         $$1[$$5] = WHITE_TERRACOTTA;
         if ($$5 - 1 > 0 && $$0.nextBoolean()) {
            $$1[$$5 - 1] = LIGHT_GRAY_TERRACOTTA;
         }

         if ($$5 + 1 < $$1.length && $$0.nextBoolean()) {
            $$1[$$5 + 1] = LIGHT_GRAY_TERRACOTTA;
         }

         $$4++;
      }

      return $$1;
   }

   private static void makeBands(RandomSource $$0, BlockState[] $$1, int $$2, BlockState $$3) {
      int $$4 = $$0.nextIntBetweenInclusive(6, 15);

      for (int $$5 = 0; $$5 < $$4; $$5++) {
         int $$6 = $$2 + $$0.nextInt(3);
         int $$7 = $$0.nextInt($$1.length);

         for (int $$8 = 0; $$7 + $$8 < $$1.length && $$8 < $$6; $$8++) {
            $$1[$$7 + $$8] = $$3;
         }
      }
   }

   protected BlockState getBand(int $$0, int $$1, int $$2) {
      int $$3 = (int)Math.round(this.clayBandsOffsetNoise.getValue($$0, 0.0, $$2) * 4.0);
      return this.clayBands[($$1 + $$3 + this.clayBands.length) % this.clayBands.length];
   }
}
