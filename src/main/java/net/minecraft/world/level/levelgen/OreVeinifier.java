package net.minecraft.world.level.levelgen;

import net.minecraft.SharedConstants;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class OreVeinifier {
   private static final float VEININESS_THRESHOLD = 0.4F;
   private static final int EDGE_ROUNDOFF_BEGIN = 20;
   private static final double MAX_EDGE_ROUNDOFF = 0.2;
   private static final float VEIN_SOLIDNESS = 0.7F;
   private static final float MIN_RICHNESS = 0.1F;
   private static final float MAX_RICHNESS = 0.3F;
   private static final float MAX_RICHNESS_THRESHOLD = 0.6F;
   private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.02F;
   private static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3F;

   private OreVeinifier() {
   }

   protected static NoiseChunk.BlockStateFiller create(DensityFunction $$0, DensityFunction $$1, DensityFunction $$2, PositionalRandomFactory $$3) {
      BlockState $$4 = SharedConstants.DEBUG_ORE_VEINS ? Blocks.AIR.defaultBlockState() : null;
      return $$5 -> {
         double $$6 = $$0.compute($$5);
         int $$7 = $$5.blockY();
         OreVeinifier.VeinType $$8 = $$6 > 0.0 ? OreVeinifier.VeinType.COPPER : OreVeinifier.VeinType.IRON;
         double $$9 = Math.abs($$6);
         int $$10 = $$8.maxY - $$7;
         int $$11 = $$7 - $$8.minY;
         if ($$11 >= 0 && $$10 >= 0) {
            int $$12 = Math.min($$10, $$11);
            double $$13 = Mth.clampedMap($$12, 0.0, 20.0, -0.2, 0.0);
            if ($$9 + $$13 < 0.4F) {
               return $$4;
            } else {
               RandomSource $$14 = $$3.at($$5.blockX(), $$7, $$5.blockZ());
               if ($$14.nextFloat() > 0.7F) {
                  return $$4;
               } else if ($$1.compute($$5) >= 0.0) {
                  return $$4;
               } else {
                  double $$15 = Mth.clampedMap($$9, 0.4F, 0.6F, 0.1F, 0.3F);
                  if ($$14.nextFloat() < $$15 && $$2.compute($$5) > -0.3F) {
                     return $$14.nextFloat() < 0.02F ? $$8.rawOreBlock : $$8.ore;
                  } else {
                     return SharedConstants.DEBUG_ORE_VEINS ? Blocks.OAK_BUTTON.defaultBlockState() : $$8.filler;
                  }
               }
            }
         } else {
            return $$4;
         }
      };
   }

   protected static enum VeinType {
      COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.RAW_COPPER_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
      IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

      final BlockState ore;
      final BlockState rawOreBlock;
      final BlockState filler;
      protected final int minY;
      protected final int maxY;

      private VeinType(final BlockState $$0, final BlockState $$1, final BlockState $$2, final int $$3, final int $$4) {
         this.ore = $$0;
         this.rawOreBlock = $$1;
         this.filler = $$2;
         this.minY = $$3;
         this.maxY = $$4;
      }
   }
}
