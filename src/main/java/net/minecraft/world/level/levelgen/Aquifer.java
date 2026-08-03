package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jspecify.annotations.Nullable;

public interface Aquifer {
   static Aquifer create(
      NoiseChunk $$0, net.minecraft.world.level.ChunkPos $$1, NoiseRouter $$2, PositionalRandomFactory $$3, int $$4, int $$5, Aquifer.FluidPicker $$6
   ) {
      return new Aquifer.NoiseBasedAquifer($$0, $$1, $$2, $$3, $$4, $$5, $$6);
   }

   static Aquifer createDisabled(final Aquifer.FluidPicker $$0) {
      return new Aquifer() {
         @Nullable
         @Override
         public BlockState computeSubstance(DensityFunction.FunctionContext $$0x, double $$1) {
            return $$1 > 0.0 ? null : $$0.computeFluid($$0.blockX(), $$0.blockY(), $$0.blockZ()).at($$0.blockY());
         }

         @Override
         public boolean shouldScheduleFluidUpdate() {
            return false;
         }
      };
   }

   @Nullable
   BlockState computeSubstance(DensityFunction.FunctionContext var1, double var2);

   boolean shouldScheduleFluidUpdate();

   public interface FluidPicker {
      Aquifer.FluidStatus computeFluid(int var1, int var2, int var3);
   }

   public record FluidStatus(int fluidLevel, BlockState fluidType) {

      public BlockState at(int $$0) {
         return $$0 < this.fluidLevel ? this.fluidType : Blocks.AIR.defaultBlockState();
      }
   }

   public static class NoiseBasedAquifer implements Aquifer {
      private static final int X_RANGE = 10;
      private static final int Y_RANGE = 9;
      private static final int Z_RANGE = 10;
      private static final int X_SEPARATION = 6;
      private static final int Y_SEPARATION = 3;
      private static final int Z_SEPARATION = 6;
      private static final int X_SPACING = 16;
      private static final int Y_SPACING = 12;
      private static final int Z_SPACING = 16;
      private static final int X_SPACING_SHIFT = 4;
      private static final int Z_SPACING_SHIFT = 4;
      private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
      private static final double FLOWING_UPDATE_SIMULARITY = similarity(Mth.square(10), Mth.square(12));
      private static final int SAMPLE_OFFSET_X = -5;
      private static final int SAMPLE_OFFSET_Y = 1;
      private static final int SAMPLE_OFFSET_Z = -5;
      private static final int MIN_CELL_SAMPLE_X = 0;
      private static final int MIN_CELL_SAMPLE_Y = -1;
      private static final int MIN_CELL_SAMPLE_Z = 0;
      private static final int MAX_CELL_SAMPLE_X = 1;
      private static final int MAX_CELL_SAMPLE_Y = 1;
      private static final int MAX_CELL_SAMPLE_Z = 1;
      private final NoiseChunk noiseChunk;
      private final DensityFunction barrierNoise;
      private final DensityFunction fluidLevelFloodednessNoise;
      private final DensityFunction fluidLevelSpreadNoise;
      private final DensityFunction lavaNoise;
      private final PositionalRandomFactory positionalRandomFactory;
      private final Aquifer.FluidStatus[] aquiferCache;
      private final long[] aquiferLocationCache;
      private final Aquifer.FluidPicker globalFluidPicker;
      private final DensityFunction erosion;
      private final DensityFunction depth;
      private boolean shouldScheduleFluidUpdate;
      private final int skipSamplingAboveY;
      private final int minGridX;
      private final int minGridY;
      private final int minGridZ;
      private final int gridSizeX;
      private final int gridSizeZ;
      private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{
         {0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
      };

      NoiseBasedAquifer(
         NoiseChunk $$0, net.minecraft.world.level.ChunkPos $$1, NoiseRouter $$2, PositionalRandomFactory $$3, int $$4, int $$5, Aquifer.FluidPicker $$6
      ) {
         this.noiseChunk = $$0;
         this.barrierNoise = $$2.barrierNoise();
         this.fluidLevelFloodednessNoise = $$2.fluidLevelFloodednessNoise();
         this.fluidLevelSpreadNoise = $$2.fluidLevelSpreadNoise();
         this.lavaNoise = $$2.lavaNoise();
         this.erosion = $$2.erosion();
         this.depth = $$2.depth();
         this.positionalRandomFactory = $$3;
         this.minGridX = gridX($$1.getMinBlockX() + -5) + 0;
         this.globalFluidPicker = $$6;
         int $$7 = gridX($$1.getMaxBlockX() + -5) + 1;
         this.gridSizeX = $$7 - this.minGridX + 1;
         this.minGridY = gridY($$4 + 1) + -1;
         int $$8 = gridY($$4 + $$5 + 1) + 1;
         int $$9 = $$8 - this.minGridY + 1;
         this.minGridZ = gridZ($$1.getMinBlockZ() + -5) + 0;
         int $$10 = gridZ($$1.getMaxBlockZ() + -5) + 1;
         this.gridSizeZ = $$10 - this.minGridZ + 1;
         int $$11 = this.gridSizeX * $$9 * this.gridSizeZ;
         this.aquiferCache = new Aquifer.FluidStatus[$$11];
         this.aquiferLocationCache = new long[$$11];
         Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
         int $$12 = this.adjustSurfaceLevel(
            $$0.maxPreliminarySurfaceLevel(fromGridX(this.minGridX, 0), fromGridZ(this.minGridZ, 0), fromGridX($$7, 9), fromGridZ($$10, 9))
         );
         int $$13 = gridY($$12 + 12) - -1;
         this.skipSamplingAboveY = fromGridY($$13, 11) - 1;
      }

      private int getIndex(int $$0, int $$1, int $$2) {
         int $$3 = $$0 - this.minGridX;
         int $$4 = $$1 - this.minGridY;
         int $$5 = $$2 - this.minGridZ;
         return ($$4 * this.gridSizeZ + $$5) * this.gridSizeX + $$3;
      }

      @Nullable
      @Override
      public BlockState computeSubstance(DensityFunction.FunctionContext $$0, double $$1) {
         if ($$1 > 0.0) {
            this.shouldScheduleFluidUpdate = false;
            return null;
         } else {
            int $$2 = $$0.blockX();
            int $$3 = $$0.blockY();
            int $$4 = $$0.blockZ();
            Aquifer.FluidStatus $$5 = this.globalFluidPicker.computeFluid($$2, $$3, $$4);
            if ($$3 > this.skipSamplingAboveY) {
               this.shouldScheduleFluidUpdate = false;
               return $$5.at($$3);
            } else if ($$5.at($$3).is(Blocks.LAVA)) {
               this.shouldScheduleFluidUpdate = false;
               return SharedConstants.DEBUG_DISABLE_FLUID_GENERATION ? Blocks.AIR.defaultBlockState() : Blocks.LAVA.defaultBlockState();
            } else {
               int $$6 = gridX($$2 + -5);
               int $$7 = gridY($$3 + 1);
               int $$8 = gridZ($$4 + -5);
               int $$9 = Integer.MAX_VALUE;
               int $$10 = Integer.MAX_VALUE;
               int $$11 = Integer.MAX_VALUE;
               int $$12 = Integer.MAX_VALUE;
               int $$13 = 0;
               int $$14 = 0;
               int $$15 = 0;
               int $$16 = 0;

               for (int $$17 = 0; $$17 <= 1; $$17++) {
                  for (int $$18 = -1; $$18 <= 1; $$18++) {
                     for (int $$19 = 0; $$19 <= 1; $$19++) {
                        int $$20 = $$6 + $$17;
                        int $$21 = $$7 + $$18;
                        int $$22 = $$8 + $$19;
                        int $$23 = this.getIndex($$20, $$21, $$22);
                        long $$24 = this.aquiferLocationCache[$$23];
                        long $$25;
                        if ($$24 != Long.MAX_VALUE) {
                           $$25 = $$24;
                        } else {
                           RandomSource $$26 = this.positionalRandomFactory.at($$20, $$21, $$22);
                           $$25 = BlockPos.asLong(fromGridX($$20, $$26.nextInt(10)), fromGridY($$21, $$26.nextInt(9)), fromGridZ($$22, $$26.nextInt(10)));
                           this.aquiferLocationCache[$$23] = $$25;
                        }

                        int $$28 = BlockPos.getX($$25) - $$2;
                        int $$29 = BlockPos.getY($$25) - $$3;
                        int $$30 = BlockPos.getZ($$25) - $$4;
                        int $$31 = $$28 * $$28 + $$29 * $$29 + $$30 * $$30;
                        if ($$9 >= $$31) {
                           $$16 = $$15;
                           $$15 = $$14;
                           $$14 = $$13;
                           $$13 = $$23;
                           $$12 = $$11;
                           $$11 = $$10;
                           $$10 = $$9;
                           $$9 = $$31;
                        } else if ($$10 >= $$31) {
                           $$16 = $$15;
                           $$15 = $$14;
                           $$14 = $$23;
                           $$12 = $$11;
                           $$11 = $$10;
                           $$10 = $$31;
                        } else if ($$11 >= $$31) {
                           $$16 = $$15;
                           $$15 = $$23;
                           $$12 = $$11;
                           $$11 = $$31;
                        } else if ($$12 >= $$31) {
                           $$16 = $$23;
                           $$12 = $$31;
                        }
                     }
                  }
               }

               Aquifer.FluidStatus $$32 = this.getAquiferStatus($$13);
               double $$33 = similarity($$9, $$10);
               BlockState $$34 = $$32.at($$3);
               BlockState $$35 = SharedConstants.DEBUG_DISABLE_FLUID_GENERATION ? Blocks.AIR.defaultBlockState() : $$34;
               if ($$33 <= 0.0) {
                  if ($$33 >= FLOWING_UPDATE_SIMULARITY) {
                     Aquifer.FluidStatus $$36 = this.getAquiferStatus($$14);
                     this.shouldScheduleFluidUpdate = !$$32.equals($$36);
                  } else {
                     this.shouldScheduleFluidUpdate = false;
                  }

                  return $$35;
               } else if ($$34.is(Blocks.WATER) && this.globalFluidPicker.computeFluid($$2, $$3 - 1, $$4).at($$3 - 1).is(Blocks.LAVA)) {
                  this.shouldScheduleFluidUpdate = true;
                  return $$35;
               } else {
                  MutableDouble $$37 = new MutableDouble(Double.NaN);
                  Aquifer.FluidStatus $$38 = this.getAquiferStatus($$14);
                  double $$39 = $$33 * this.calculatePressure($$0, $$37, $$32, $$38);
                  if ($$1 + $$39 > 0.0) {
                     this.shouldScheduleFluidUpdate = false;
                     return null;
                  } else {
                     Aquifer.FluidStatus $$40 = this.getAquiferStatus($$15);
                     double $$41 = similarity($$9, $$11);
                     if ($$41 > 0.0) {
                        double $$42 = $$33 * $$41 * this.calculatePressure($$0, $$37, $$32, $$40);
                        if ($$1 + $$42 > 0.0) {
                           this.shouldScheduleFluidUpdate = false;
                           return null;
                        }
                     }

                     double $$43 = similarity($$10, $$11);
                     if ($$43 > 0.0) {
                        double $$44 = $$33 * $$43 * this.calculatePressure($$0, $$37, $$38, $$40);
                        if ($$1 + $$44 > 0.0) {
                           this.shouldScheduleFluidUpdate = false;
                           return null;
                        }
                     }

                     boolean $$45 = !$$32.equals($$38);
                     boolean $$46 = $$43 >= FLOWING_UPDATE_SIMULARITY && !$$38.equals($$40);
                     boolean $$47 = $$41 >= FLOWING_UPDATE_SIMULARITY && !$$32.equals($$40);
                     if (!$$45 && !$$46 && !$$47) {
                        this.shouldScheduleFluidUpdate = $$41 >= FLOWING_UPDATE_SIMULARITY
                           && similarity($$9, $$12) >= FLOWING_UPDATE_SIMULARITY
                           && !$$32.equals(this.getAquiferStatus($$16));
                     } else {
                        this.shouldScheduleFluidUpdate = true;
                     }

                     return $$35;
                  }
               }
            }
         }
      }

      @Override
      public boolean shouldScheduleFluidUpdate() {
         return this.shouldScheduleFluidUpdate;
      }

      private static double similarity(int $$0, int $$1) {
         double $$2 = 25.0;
         return 1.0 - ($$1 - $$0) / 25.0;
      }

      private double calculatePressure(DensityFunction.FunctionContext $$0, MutableDouble $$1, Aquifer.FluidStatus $$2, Aquifer.FluidStatus $$3) {
         int $$4 = $$0.blockY();
         BlockState $$5 = $$2.at($$4);
         BlockState $$6 = $$3.at($$4);
         if ((!$$5.is(Blocks.LAVA) || !$$6.is(Blocks.WATER)) && (!$$5.is(Blocks.WATER) || !$$6.is(Blocks.LAVA))) {
            int $$7 = Math.abs($$2.fluidLevel - $$3.fluidLevel);
            if ($$7 == 0) {
               return 0.0;
            } else {
               double $$8 = 0.5 * ($$2.fluidLevel + $$3.fluidLevel);
               double $$9 = $$4 + 0.5 - $$8;
               double $$10 = $$7 / 2.0;
               double $$11 = 0.0;
               double $$12 = 2.5;
               double $$13 = 1.5;
               double $$14 = 3.0;
               double $$15 = 10.0;
               double $$16 = 3.0;
               double $$17 = $$10 - Math.abs($$9);
               double $$19;
               if ($$9 > 0.0) {
                  double $$18 = 0.0 + $$17;
                  if ($$18 > 0.0) {
                     $$19 = $$18 / 1.5;
                  } else {
                     $$19 = $$18 / 2.5;
                  }
               } else {
                  double $$21 = 3.0 + $$17;
                  if ($$21 > 0.0) {
                     $$19 = $$21 / 3.0;
                  } else {
                     $$19 = $$21 / 10.0;
                  }
               }

               double $$24 = 2.0;
               double $$28;
               if (!($$19 < -2.0) && !($$19 > 2.0)) {
                  double $$26 = $$1.doubleValue();
                  if (Double.isNaN($$26)) {
                     double $$27 = this.barrierNoise.compute($$0);
                     $$1.setValue($$27);
                     $$28 = $$27;
                  } else {
                     $$28 = $$26;
                  }
               } else {
                  $$28 = 0.0;
               }

               return 2.0 * ($$28 + $$19);
            }
         } else {
            return 2.0;
         }
      }

      private static int gridX(int $$0) {
         return $$0 >> 4;
      }

      private static int fromGridX(int $$0, int $$1) {
         return ($$0 << 4) + $$1;
      }

      private static int gridY(int $$0) {
         return Math.floorDiv($$0, 12);
      }

      private static int fromGridY(int $$0, int $$1) {
         return $$0 * 12 + $$1;
      }

      private static int gridZ(int $$0) {
         return $$0 >> 4;
      }

      private static int fromGridZ(int $$0, int $$1) {
         return ($$0 << 4) + $$1;
      }

      private Aquifer.FluidStatus getAquiferStatus(int $$0) {
         Aquifer.FluidStatus $$1 = this.aquiferCache[$$0];
         if ($$1 != null) {
            return $$1;
         } else {
            long $$2 = this.aquiferLocationCache[$$0];
            Aquifer.FluidStatus $$3 = this.computeFluid(BlockPos.getX($$2), BlockPos.getY($$2), BlockPos.getZ($$2));
            this.aquiferCache[$$0] = $$3;
            return $$3;
         }
      }

      private Aquifer.FluidStatus computeFluid(int $$0, int $$1, int $$2) {
         Aquifer.FluidStatus $$3 = this.globalFluidPicker.computeFluid($$0, $$1, $$2);
         int $$4 = Integer.MAX_VALUE;
         int $$5 = $$1 + 12;
         int $$6 = $$1 - 12;
         boolean $$7 = false;

         for (int[] $$8 : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
            int $$9 = $$0 + SectionPos.sectionToBlockCoord($$8[0]);
            int $$10 = $$2 + SectionPos.sectionToBlockCoord($$8[1]);
            int $$11 = this.noiseChunk.preliminarySurfaceLevel($$9, $$10);
            int $$12 = this.adjustSurfaceLevel($$11);
            boolean $$13 = $$8[0] == 0 && $$8[1] == 0;
            if ($$13 && $$6 > $$12) {
               return $$3;
            }

            boolean $$14 = $$5 > $$12;
            if ($$14 || $$13) {
               Aquifer.FluidStatus $$15 = this.globalFluidPicker.computeFluid($$9, $$12, $$10);
               if (!$$15.at($$12).isAir()) {
                  if ($$13) {
                     $$7 = true;
                  }

                  if ($$14) {
                     return $$15;
                  }
               }
            }

            $$4 = Math.min($$4, $$11);
         }

         int $$16 = this.computeSurfaceLevel($$0, $$1, $$2, $$3, $$4, $$7);
         return new Aquifer.FluidStatus($$16, this.computeFluidType($$0, $$1, $$2, $$3, $$16));
      }

      private int adjustSurfaceLevel(int $$0) {
         return $$0 + 8;
      }

      private int computeSurfaceLevel(int $$0, int $$1, int $$2, Aquifer.FluidStatus $$3, int $$4, boolean $$5) {
         DensityFunction.SinglePointContext $$6 = new DensityFunction.SinglePointContext($$0, $$1, $$2);
         double $$7;
         double $$8;
         if (OverworldBiomeBuilder.isDeepDarkRegion(this.erosion, this.depth, $$6)) {
            $$7 = -1.0;
            $$8 = -1.0;
         } else {
            int $$9 = $$4 + 8 - $$1;
            int $$10 = 64;
            double $$11 = $$5 ? Mth.clampedMap($$9, 0.0, 64.0, 1.0, 0.0) : 0.0;
            double $$12 = Mth.clamp(this.fluidLevelFloodednessNoise.compute($$6), -1.0, 1.0);
            double $$13 = Mth.map($$11, 1.0, 0.0, -0.3, 0.8);
            double $$14 = Mth.map($$11, 1.0, 0.0, -0.8, 0.4);
            $$7 = $$12 - $$14;
            $$8 = $$12 - $$13;
         }

         int $$17;
         if ($$8 > 0.0) {
            $$17 = $$3.fluidLevel;
         } else if ($$7 > 0.0) {
            $$17 = this.computeRandomizedFluidSurfaceLevel($$0, $$1, $$2, $$4);
         } else {
            $$17 = DimensionType.WAY_BELOW_MIN_Y;
         }

         return $$17;
      }

      private int computeRandomizedFluidSurfaceLevel(int $$0, int $$1, int $$2, int $$3) {
         int $$4 = 16;
         int $$5 = 40;
         int $$6 = Math.floorDiv($$0, 16);
         int $$7 = Math.floorDiv($$1, 40);
         int $$8 = Math.floorDiv($$2, 16);
         int $$9 = $$7 * 40 + 20;
         int $$10 = 10;
         double $$11 = this.fluidLevelSpreadNoise.compute(new DensityFunction.SinglePointContext($$6, $$7, $$8)) * 10.0;
         int $$12 = Mth.quantize($$11, 3);
         int $$13 = $$9 + $$12;
         return Math.min($$3, $$13);
      }

      private BlockState computeFluidType(int $$0, int $$1, int $$2, Aquifer.FluidStatus $$3, int $$4) {
         BlockState $$5 = $$3.fluidType;
         if ($$4 <= -10 && $$4 != DimensionType.WAY_BELOW_MIN_Y && $$3.fluidType != Blocks.LAVA.defaultBlockState()) {
            int $$6 = 64;
            int $$7 = 40;
            int $$8 = Math.floorDiv($$0, 64);
            int $$9 = Math.floorDiv($$1, 40);
            int $$10 = Math.floorDiv($$2, 64);
            double $$11 = this.lavaNoise.compute(new DensityFunction.SinglePointContext($$8, $$9, $$10));
            if (Math.abs($$11) > 0.3) {
               $$5 = Blocks.LAVA.defaultBlockState();
            }
         }

         return $$5;
      }
   }
}
