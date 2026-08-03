package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class HugeBrownMushroomFeature extends AbstractHugeMushroomFeature {
   public HugeBrownMushroomFeature(Codec<HugeMushroomFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   protected void makeCap(
      net.minecraft.world.level.LevelAccessor $$0, RandomSource $$1, BlockPos $$2, int $$3, MutableBlockPos $$4, HugeMushroomFeatureConfiguration $$5
   ) {
      int $$6 = $$5.foliageRadius;

      for (int $$7 = -$$6; $$7 <= $$6; $$7++) {
         for (int $$8 = -$$6; $$8 <= $$6; $$8++) {
            boolean $$9 = $$7 == -$$6;
            boolean $$10 = $$7 == $$6;
            boolean $$11 = $$8 == -$$6;
            boolean $$12 = $$8 == $$6;
            boolean $$13 = $$9 || $$10;
            boolean $$14 = $$11 || $$12;
            if (!$$13 || !$$14) {
               $$4.setWithOffset($$2, $$7, $$3, $$8);
               boolean $$15 = $$9 || $$14 && $$7 == 1 - $$6;
               boolean $$16 = $$10 || $$14 && $$7 == $$6 - 1;
               boolean $$17 = $$11 || $$13 && $$8 == 1 - $$6;
               boolean $$18 = $$12 || $$13 && $$8 == $$6 - 1;
               BlockState $$19 = $$5.capProvider.getState($$1, $$2);
               if ($$19.hasProperty(HugeMushroomBlock.WEST)
                  && $$19.hasProperty(HugeMushroomBlock.EAST)
                  && $$19.hasProperty(HugeMushroomBlock.NORTH)
                  && $$19.hasProperty(HugeMushroomBlock.SOUTH)) {
                  $$19 = $$19.setValue(HugeMushroomBlock.WEST, $$15)
                     .setValue(HugeMushroomBlock.EAST, $$16)
                     .setValue(HugeMushroomBlock.NORTH, $$17)
                     .setValue(HugeMushroomBlock.SOUTH, $$18);
               }

               this.placeMushroomBlock($$0, $$4, $$19);
            }
         }
      }
   }

   @Override
   protected int getTreeRadiusForHeight(int $$0, int $$1, int $$2, int $$3) {
      return $$3 <= 3 ? 0 : $$2;
   }
}
