package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

public class CanyonWorldCarver extends WorldCarver<CanyonCarverConfiguration> {
   public CanyonWorldCarver(Codec<CanyonCarverConfiguration> $$0) {
      super($$0);
   }

   public boolean isStartChunk(CanyonCarverConfiguration $$0, RandomSource $$1) {
      return $$1.nextFloat() <= $$0.probability;
   }

   public boolean carve(
      CarvingContext $$0,
      CanyonCarverConfiguration $$1,
      ChunkAccess $$2,
      Function<BlockPos, Holder<Biome>> $$3,
      RandomSource $$4,
      Aquifer $$5,
      net.minecraft.world.level.ChunkPos $$6,
      CarvingMask $$7
   ) {
      int $$8 = (this.getRange() * 2 - 1) * 16;
      double $$9 = $$6.getBlockX($$4.nextInt(16));
      int $$10 = $$1.y.sample($$4, $$0);
      double $$11 = $$6.getBlockZ($$4.nextInt(16));
      float $$12 = $$4.nextFloat() * (float) (Math.PI * 2);
      float $$13 = $$1.verticalRotation.sample($$4);
      double $$14 = $$1.yScale.sample($$4);
      float $$15 = $$1.shape.thickness.sample($$4);
      int $$16 = (int)($$8 * $$1.shape.distanceFactor.sample($$4));
      int $$17 = 0;
      this.doCarve($$0, $$1, $$2, $$3, $$4.nextLong(), $$5, $$9, $$10, $$11, $$15, $$12, $$13, 0, $$16, $$14, $$7);
      return true;
   }

   private void doCarve(
      CarvingContext $$0,
      CanyonCarverConfiguration $$1,
      ChunkAccess $$2,
      Function<BlockPos, Holder<Biome>> $$3,
      long $$4,
      Aquifer $$5,
      double $$6,
      double $$7,
      double $$8,
      float $$9,
      float $$10,
      float $$11,
      int $$12,
      int $$13,
      double $$14,
      CarvingMask $$15
   ) {
      RandomSource $$16 = RandomSource.create($$4);
      float[] $$17 = this.initWidthFactors($$0, $$1, $$16);
      float $$18 = 0.0F;
      float $$19 = 0.0F;

      for (int $$20 = $$12; $$20 < $$13; $$20++) {
         double $$21 = 1.5 + Mth.sin($$20 * (float) Math.PI / $$13) * $$9;
         double $$22 = $$21 * $$14;
         $$21 *= $$1.shape.horizontalRadiusFactor.sample($$16);
         $$22 = this.updateVerticalRadius($$1, $$16, $$22, $$13, $$20);
         float $$23 = Mth.cos($$11);
         float $$24 = Mth.sin($$11);
         $$6 += Mth.cos($$10) * $$23;
         $$7 += $$24;
         $$8 += Mth.sin($$10) * $$23;
         $$11 *= 0.7F;
         $$11 += $$19 * 0.05F;
         $$10 += $$18 * 0.05F;
         $$19 *= 0.8F;
         $$18 *= 0.5F;
         $$19 += ($$16.nextFloat() - $$16.nextFloat()) * $$16.nextFloat() * 2.0F;
         $$18 += ($$16.nextFloat() - $$16.nextFloat()) * $$16.nextFloat() * 4.0F;
         if ($$16.nextInt(4) != 0) {
            if (!canReach($$2.getPos(), $$6, $$8, $$20, $$13, $$9)) {
               return;
            }

            this.carveEllipsoid(
               $$0, $$1, $$2, $$3, $$5, $$6, $$7, $$8, $$21, $$22, $$15, ($$1x, $$2x, $$3x, $$4x, $$5x) -> this.shouldSkip($$1x, $$17, $$2x, $$3x, $$4x, $$5x)
            );
         }
      }
   }

   private float[] initWidthFactors(CarvingContext $$0, CanyonCarverConfiguration $$1, RandomSource $$2) {
      int $$3 = $$0.getGenDepth();
      float[] $$4 = new float[$$3];
      float $$5 = 1.0F;

      for (int $$6 = 0; $$6 < $$3; $$6++) {
         if ($$6 == 0 || $$2.nextInt($$1.shape.widthSmoothness) == 0) {
            $$5 = 1.0F + $$2.nextFloat() * $$2.nextFloat();
         }

         $$4[$$6] = $$5 * $$5;
      }

      return $$4;
   }

   private double updateVerticalRadius(CanyonCarverConfiguration $$0, RandomSource $$1, double $$2, float $$3, float $$4) {
      float $$5 = 1.0F - Mth.abs(0.5F - $$4 / $$3) * 2.0F;
      float $$6 = $$0.shape.verticalRadiusDefaultFactor + $$0.shape.verticalRadiusCenterFactor * $$5;
      return $$6 * $$2 * Mth.randomBetween($$1, 0.75F, 1.0F);
   }

   private boolean shouldSkip(CarvingContext $$0, float[] $$1, double $$2, double $$3, double $$4, int $$5) {
      int $$6 = $$5 - $$0.getMinGenY();
      return ($$2 * $$2 + $$4 * $$4) * $$1[$$6 - 1] + $$3 * $$3 / 6.0 >= 1.0;
   }
}
