package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

public class CaveWorldCarver extends WorldCarver<CaveCarverConfiguration> {
   public CaveWorldCarver(Codec<CaveCarverConfiguration> $$0) {
      super($$0);
   }

   public boolean isStartChunk(CaveCarverConfiguration $$0, RandomSource $$1) {
      return $$1.nextFloat() <= $$0.probability;
   }

   public boolean carve(
      CarvingContext $$0,
      CaveCarverConfiguration $$1,
      ChunkAccess $$2,
      Function<BlockPos, Holder<Biome>> $$3,
      RandomSource $$4,
      Aquifer $$5,
      net.minecraft.world.level.ChunkPos $$6,
      CarvingMask $$7
   ) {
      int $$8 = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
      int $$9 = $$4.nextInt($$4.nextInt($$4.nextInt(this.getCaveBound()) + 1) + 1);

      for (int $$10 = 0; $$10 < $$9; $$10++) {
         double $$11 = $$6.getBlockX($$4.nextInt(16));
         double $$12 = $$1.y.sample($$4, $$0);
         double $$13 = $$6.getBlockZ($$4.nextInt(16));
         double $$14 = $$1.horizontalRadiusMultiplier.sample($$4);
         double $$15 = $$1.verticalRadiusMultiplier.sample($$4);
         double $$16 = $$1.floorLevel.sample($$4);
         WorldCarver.CarveSkipChecker $$17 = ($$1x, $$2x, $$3x, $$4x, $$5x) -> shouldSkip($$2x, $$3x, $$4x, $$16);
         int $$18 = 1;
         if ($$4.nextInt(4) == 0) {
            double $$19 = $$1.yScale.sample($$4);
            float $$20 = 1.0F + $$4.nextFloat() * 6.0F;
            this.createRoom($$0, $$1, $$2, $$3, $$5, $$11, $$12, $$13, $$20, $$19, $$7, $$17);
            $$18 += $$4.nextInt(4);
         }

         for (int $$21 = 0; $$21 < $$18; $$21++) {
            float $$22 = $$4.nextFloat() * (float) (Math.PI * 2);
            float $$23 = ($$4.nextFloat() - 0.5F) / 4.0F;
            float $$24 = this.getThickness($$4);
            int $$25 = $$8 - $$4.nextInt($$8 / 4);
            int $$26 = 0;
            this.createTunnel($$0, $$1, $$2, $$3, $$4.nextLong(), $$5, $$11, $$12, $$13, $$14, $$15, $$24, $$22, $$23, 0, $$25, this.getYScale(), $$7, $$17);
         }
      }

      return true;
   }

   protected int getCaveBound() {
      return 15;
   }

   protected float getThickness(RandomSource $$0) {
      float $$1 = $$0.nextFloat() * 2.0F + $$0.nextFloat();
      if ($$0.nextInt(10) == 0) {
         $$1 *= $$0.nextFloat() * $$0.nextFloat() * 3.0F + 1.0F;
      }

      return $$1;
   }

   protected double getYScale() {
      return 1.0;
   }

   protected void createRoom(
      CarvingContext $$0,
      CaveCarverConfiguration $$1,
      ChunkAccess $$2,
      Function<BlockPos, Holder<Biome>> $$3,
      Aquifer $$4,
      double $$5,
      double $$6,
      double $$7,
      float $$8,
      double $$9,
      CarvingMask $$10,
      WorldCarver.CarveSkipChecker $$11
   ) {
      double $$12 = 1.5 + Mth.sin((float) (Math.PI / 2)) * $$8;
      double $$13 = $$12 * $$9;
      this.carveEllipsoid($$0, $$1, $$2, $$3, $$4, $$5 + 1.0, $$6, $$7, $$12, $$13, $$10, $$11);
   }

   protected void createTunnel(
      CarvingContext $$0,
      CaveCarverConfiguration $$1,
      ChunkAccess $$2,
      Function<BlockPos, Holder<Biome>> $$3,
      long $$4,
      Aquifer $$5,
      double $$6,
      double $$7,
      double $$8,
      double $$9,
      double $$10,
      float $$11,
      float $$12,
      float $$13,
      int $$14,
      int $$15,
      double $$16,
      CarvingMask $$17,
      WorldCarver.CarveSkipChecker $$18
   ) {
      RandomSource $$19 = RandomSource.create($$4);
      int $$20 = $$19.nextInt($$15 / 2) + $$15 / 4;
      boolean $$21 = $$19.nextInt(6) == 0;
      float $$22 = 0.0F;
      float $$23 = 0.0F;

      for (int $$24 = $$14; $$24 < $$15; $$24++) {
         double $$25 = 1.5 + Mth.sin((float) Math.PI * $$24 / $$15) * $$11;
         double $$26 = $$25 * $$16;
         float $$27 = Mth.cos($$13);
         $$6 += Mth.cos($$12) * $$27;
         $$7 += Mth.sin($$13);
         $$8 += Mth.sin($$12) * $$27;
         $$13 *= $$21 ? 0.92F : 0.7F;
         $$13 += $$23 * 0.1F;
         $$12 += $$22 * 0.1F;
         $$23 *= 0.9F;
         $$22 *= 0.75F;
         $$23 += ($$19.nextFloat() - $$19.nextFloat()) * $$19.nextFloat() * 2.0F;
         $$22 += ($$19.nextFloat() - $$19.nextFloat()) * $$19.nextFloat() * 4.0F;
         if ($$24 == $$20 && $$11 > 1.0F) {
            this.createTunnel(
               $$0,
               $$1,
               $$2,
               $$3,
               $$19.nextLong(),
               $$5,
               $$6,
               $$7,
               $$8,
               $$9,
               $$10,
               $$19.nextFloat() * 0.5F + 0.5F,
               $$12 - (float) (Math.PI / 2),
               $$13 / 3.0F,
               $$24,
               $$15,
               1.0,
               $$17,
               $$18
            );
            this.createTunnel(
               $$0,
               $$1,
               $$2,
               $$3,
               $$19.nextLong(),
               $$5,
               $$6,
               $$7,
               $$8,
               $$9,
               $$10,
               $$19.nextFloat() * 0.5F + 0.5F,
               $$12 + (float) (Math.PI / 2),
               $$13 / 3.0F,
               $$24,
               $$15,
               1.0,
               $$17,
               $$18
            );
            return;
         }

         if ($$19.nextInt(4) != 0) {
            if (!canReach($$2.getPos(), $$6, $$8, $$24, $$15, $$11)) {
               return;
            }

            this.carveEllipsoid($$0, $$1, $$2, $$3, $$5, $$6, $$7, $$8, $$25 * $$9, $$26 * $$10, $$17, $$18);
         }
      }
   }

   private static boolean shouldSkip(double $$0, double $$1, double $$2, double $$3) {
      return $$1 <= $$3 ? true : $$0 * $$0 + $$1 * $$1 + $$2 * $$2 >= 1.0;
   }
}
