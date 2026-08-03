package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class IcebergFeature extends Feature<BlockStateConfiguration> {
   public IcebergFeature(Codec<BlockStateConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<BlockStateConfiguration> $$0) {
      BlockPos $$1 = $$0.origin();
      net.minecraft.world.level.WorldGenLevel $$2 = $$0.level();
      $$1 = new BlockPos($$1.getX(), $$0.chunkGenerator().getSeaLevel(), $$1.getZ());
      RandomSource $$3 = $$0.random();
      boolean $$4 = $$3.nextDouble() > 0.7;
      BlockState $$5 = $$0.config().state;
      double $$6 = $$3.nextDouble() * 2.0 * Math.PI;
      int $$7 = 11 - $$3.nextInt(5);
      int $$8 = 3 + $$3.nextInt(3);
      boolean $$9 = $$3.nextDouble() > 0.7;
      int $$10 = 11;
      int $$11 = $$9 ? $$3.nextInt(6) + 6 : $$3.nextInt(15) + 3;
      if (!$$9 && $$3.nextDouble() > 0.9) {
         $$11 += $$3.nextInt(19) + 7;
      }

      int $$12 = Math.min($$11 + $$3.nextInt(11), 18);
      int $$13 = Math.min($$11 + $$3.nextInt(7) - $$3.nextInt(5), 11);
      int $$14 = $$9 ? $$7 : 11;

      for (int $$15 = -$$14; $$15 < $$14; $$15++) {
         for (int $$16 = -$$14; $$16 < $$14; $$16++) {
            for (int $$17 = 0; $$17 < $$11; $$17++) {
               int $$18 = $$9 ? this.heightDependentRadiusEllipse($$17, $$11, $$13) : this.heightDependentRadiusRound($$3, $$17, $$11, $$13);
               if ($$9 || $$15 < $$18) {
                  this.generateIcebergBlock($$2, $$3, $$1, $$11, $$15, $$17, $$16, $$18, $$14, $$9, $$8, $$6, $$4, $$5);
               }
            }
         }
      }

      this.smooth($$2, $$1, $$13, $$11, $$9, $$7);

      for (int $$19 = -$$14; $$19 < $$14; $$19++) {
         for (int $$20 = -$$14; $$20 < $$14; $$20++) {
            for (int $$21 = -1; $$21 > -$$12; $$21--) {
               int $$22 = $$9 ? Mth.ceil($$14 * (1.0F - (float)Math.pow($$21, 2.0) / ($$12 * 8.0F))) : $$14;
               int $$23 = this.heightDependentRadiusSteep($$3, -$$21, $$12, $$13);
               if ($$19 < $$23) {
                  this.generateIcebergBlock($$2, $$3, $$1, $$12, $$19, $$21, $$20, $$23, $$22, $$9, $$8, $$6, $$4, $$5);
               }
            }
         }
      }

      boolean $$24 = $$9 ? $$3.nextDouble() > 0.1 : $$3.nextDouble() > 0.7;
      if ($$24) {
         this.generateCutOut($$3, $$2, $$13, $$11, $$1, $$9, $$7, $$6, $$8);
      }

      return true;
   }

   private void generateCutOut(
      RandomSource $$0, net.minecraft.world.level.LevelAccessor $$1, int $$2, int $$3, BlockPos $$4, boolean $$5, int $$6, double $$7, int $$8
   ) {
      int $$9 = $$0.nextBoolean() ? -1 : 1;
      int $$10 = $$0.nextBoolean() ? -1 : 1;
      int $$11 = $$0.nextInt(Math.max($$2 / 2 - 2, 1));
      if ($$0.nextBoolean()) {
         $$11 = $$2 / 2 + 1 - $$0.nextInt(Math.max($$2 - $$2 / 2 - 1, 1));
      }

      int $$12 = $$0.nextInt(Math.max($$2 / 2 - 2, 1));
      if ($$0.nextBoolean()) {
         $$12 = $$2 / 2 + 1 - $$0.nextInt(Math.max($$2 - $$2 / 2 - 1, 1));
      }

      if ($$5) {
         $$11 = $$12 = $$0.nextInt(Math.max($$6 - 5, 1));
      }

      BlockPos $$13 = new BlockPos($$9 * $$11, 0, $$10 * $$12);
      double $$14 = $$5 ? $$7 + (Math.PI / 2) : $$0.nextDouble() * 2.0 * Math.PI;

      for (int $$15 = 0; $$15 < $$3 - 3; $$15++) {
         int $$16 = this.heightDependentRadiusRound($$0, $$15, $$3, $$2);
         this.carve($$16, $$15, $$4, $$1, false, $$14, $$13, $$6, $$8);
      }

      for (int $$17 = -1; $$17 > -$$3 + $$0.nextInt(5); $$17--) {
         int $$18 = this.heightDependentRadiusSteep($$0, -$$17, $$3, $$2);
         this.carve($$18, $$17, $$4, $$1, true, $$14, $$13, $$6, $$8);
      }
   }

   private void carve(int $$0, int $$1, BlockPos $$2, net.minecraft.world.level.LevelAccessor $$3, boolean $$4, double $$5, BlockPos $$6, int $$7, int $$8) {
      int $$9 = $$0 + 1 + $$7 / 3;
      int $$10 = Math.min($$0 - 3, 3) + $$8 / 2 - 1;

      for (int $$11 = -$$9; $$11 < $$9; $$11++) {
         for (int $$12 = -$$9; $$12 < $$9; $$12++) {
            double $$13 = this.signedDistanceEllipse($$11, $$12, $$6, $$9, $$10, $$5);
            if ($$13 < 0.0) {
               BlockPos $$14 = $$2.offset($$11, $$1, $$12);
               BlockState $$15 = $$3.getBlockState($$14);
               if (isIcebergState($$15) || $$15.is(Blocks.SNOW_BLOCK)) {
                  if ($$4) {
                     this.setBlock($$3, $$14, Blocks.WATER.defaultBlockState());
                  } else {
                     this.setBlock($$3, $$14, Blocks.AIR.defaultBlockState());
                     this.removeFloatingSnowLayer($$3, $$14);
                  }
               }
            }
         }
      }
   }

   private void removeFloatingSnowLayer(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      if ($$0.getBlockState($$1.above()).is(Blocks.SNOW)) {
         this.setBlock($$0, $$1.above(), Blocks.AIR.defaultBlockState());
      }
   }

   private void generateIcebergBlock(
      net.minecraft.world.level.LevelAccessor $$0,
      RandomSource $$1,
      BlockPos $$2,
      int $$3,
      int $$4,
      int $$5,
      int $$6,
      int $$7,
      int $$8,
      boolean $$9,
      int $$10,
      double $$11,
      boolean $$12,
      BlockState $$13
   ) {
      double $$14 = $$9
         ? this.signedDistanceEllipse($$4, $$6, BlockPos.ZERO, $$8, this.getEllipseC($$5, $$3, $$10), $$11)
         : this.signedDistanceCircle($$4, $$6, BlockPos.ZERO, $$7, $$1);
      if ($$14 < 0.0) {
         BlockPos $$15 = $$2.offset($$4, $$5, $$6);
         double $$16 = $$9 ? -0.5 : -6 - $$1.nextInt(3);
         if ($$14 > $$16 && $$1.nextDouble() > 0.9) {
            return;
         }

         this.setIcebergBlock($$15, $$0, $$1, $$3 - $$5, $$3, $$9, $$12, $$13);
      }
   }

   private void setIcebergBlock(
      BlockPos $$0, net.minecraft.world.level.LevelAccessor $$1, RandomSource $$2, int $$3, int $$4, boolean $$5, boolean $$6, BlockState $$7
   ) {
      BlockState $$8 = $$1.getBlockState($$0);
      if ($$8.isAir() || $$8.is(Blocks.SNOW_BLOCK) || $$8.is(Blocks.ICE) || $$8.is(Blocks.WATER)) {
         boolean $$9 = !$$5 || $$2.nextDouble() > 0.05;
         int $$10 = $$5 ? 3 : 2;
         if ($$6 && !$$8.is(Blocks.WATER) && $$3 <= $$2.nextInt(Math.max(1, $$4 / $$10)) + $$4 * 0.6 && $$9) {
            this.setBlock($$1, $$0, Blocks.SNOW_BLOCK.defaultBlockState());
         } else {
            this.setBlock($$1, $$0, $$7);
         }
      }
   }

   private int getEllipseC(int $$0, int $$1, int $$2) {
      int $$3 = $$2;
      if ($$0 > 0 && $$1 - $$0 <= 3) {
         $$3 = $$2 - (4 - ($$1 - $$0));
      }

      return $$3;
   }

   private double signedDistanceCircle(int $$0, int $$1, BlockPos $$2, int $$3, RandomSource $$4) {
      float $$5 = 10.0F * Mth.clamp($$4.nextFloat(), 0.2F, 0.8F) / $$3;
      return $$5 + Math.pow($$0 - $$2.getX(), 2.0) + Math.pow($$1 - $$2.getZ(), 2.0) - Math.pow($$3, 2.0);
   }

   private double signedDistanceEllipse(int $$0, int $$1, BlockPos $$2, int $$3, int $$4, double $$5) {
      return Math.pow((($$0 - $$2.getX()) * Math.cos($$5) - ($$1 - $$2.getZ()) * Math.sin($$5)) / $$3, 2.0)
         + Math.pow((($$0 - $$2.getX()) * Math.sin($$5) + ($$1 - $$2.getZ()) * Math.cos($$5)) / $$4, 2.0)
         - 1.0;
   }

   private int heightDependentRadiusRound(RandomSource $$0, int $$1, int $$2, int $$3) {
      float $$4 = 3.5F - $$0.nextFloat();
      float $$5 = (1.0F - (float)Math.pow($$1, 2.0) / ($$2 * $$4)) * $$3;
      if ($$2 > 15 + $$0.nextInt(5)) {
         int $$6 = $$1 < 3 + $$0.nextInt(6) ? $$1 / 2 : $$1;
         $$5 = (1.0F - $$6 / ($$2 * $$4 * 0.4F)) * $$3;
      }

      return Mth.ceil($$5 / 2.0F);
   }

   private int heightDependentRadiusEllipse(int $$0, int $$1, int $$2) {
      float $$3 = 1.0F;
      float $$4 = (1.0F - (float)Math.pow($$0, 2.0) / ($$1 * 1.0F)) * $$2;
      return Mth.ceil($$4 / 2.0F);
   }

   private int heightDependentRadiusSteep(RandomSource $$0, int $$1, int $$2, int $$3) {
      float $$4 = 1.0F + $$0.nextFloat() / 2.0F;
      float $$5 = (1.0F - $$1 / ($$2 * $$4)) * $$3;
      return Mth.ceil($$5 / 2.0F);
   }

   private static boolean isIcebergState(BlockState $$0) {
      return $$0.is(Blocks.PACKED_ICE) || $$0.is(Blocks.SNOW_BLOCK) || $$0.is(Blocks.BLUE_ICE);
   }

   private boolean belowIsAir(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      return $$0.getBlockState($$1.below()).isAir();
   }

   private void smooth(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, int $$2, int $$3, boolean $$4, int $$5) {
      int $$6 = $$4 ? $$5 : $$2 / 2;

      for (int $$7 = -$$6; $$7 <= $$6; $$7++) {
         for (int $$8 = -$$6; $$8 <= $$6; $$8++) {
            for (int $$9 = 0; $$9 <= $$3; $$9++) {
               BlockPos $$10 = $$1.offset($$7, $$9, $$8);
               BlockState $$11 = $$0.getBlockState($$10);
               if (isIcebergState($$11) || $$11.is(Blocks.SNOW)) {
                  if (this.belowIsAir($$0, $$10)) {
                     this.setBlock($$0, $$10, Blocks.AIR.defaultBlockState());
                     this.setBlock($$0, $$10.above(), Blocks.AIR.defaultBlockState());
                  } else if (isIcebergState($$11)) {
                     BlockState[] $$12 = new BlockState[]{
                        $$0.getBlockState($$10.west()), $$0.getBlockState($$10.east()), $$0.getBlockState($$10.north()), $$0.getBlockState($$10.south())
                     };
                     int $$13 = 0;

                     for (BlockState $$14 : $$12) {
                        if (!isIcebergState($$14)) {
                           $$13++;
                        }
                     }

                     if ($$13 >= 3) {
                        this.setBlock($$0, $$10, Blocks.AIR.defaultBlockState());
                     }
                  }
               }
            }
         }
      }
   }
}
