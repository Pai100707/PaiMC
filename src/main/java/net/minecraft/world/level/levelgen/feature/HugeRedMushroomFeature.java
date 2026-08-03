package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class HugeRedMushroomFeature extends AbstractHugeMushroomFeature {
   public HugeRedMushroomFeature(Codec<HugeMushroomFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   protected void makeCap(
      net.minecraft.world.level.LevelAccessor $$0, RandomSource $$1, BlockPos $$2, int $$3, MutableBlockPos $$4, HugeMushroomFeatureConfiguration $$5
   ) {
      for (int $$6 = $$3 - 3; $$6 <= $$3; $$6++) {
         int $$7 = $$6 < $$3 ? $$5.foliageRadius : $$5.foliageRadius - 1;
         int $$8 = $$5.foliageRadius - 2;

         for (int $$9 = -$$7; $$9 <= $$7; $$9++) {
            for (int $$10 = -$$7; $$10 <= $$7; $$10++) {
               boolean $$11 = $$9 == -$$7;
               boolean $$12 = $$9 == $$7;
               boolean $$13 = $$10 == -$$7;
               boolean $$14 = $$10 == $$7;
               boolean $$15 = $$11 || $$12;
               boolean $$16 = $$13 || $$14;
               if ($$6 >= $$3 || $$15 != $$16) {
                  $$4.setWithOffset($$2, $$9, $$6, $$10);
                  BlockState $$17 = $$5.capProvider.getState($$1, $$2);
                  if ($$17.hasProperty(HugeMushroomBlock.WEST)
                     && $$17.hasProperty(HugeMushroomBlock.EAST)
                     && $$17.hasProperty(HugeMushroomBlock.NORTH)
                     && $$17.hasProperty(HugeMushroomBlock.SOUTH)
                     && $$17.hasProperty(HugeMushroomBlock.UP)) {
                     $$17 = $$17.setValue(HugeMushroomBlock.UP, $$6 >= $$3 - 1)
                        .setValue(HugeMushroomBlock.WEST, $$9 < -$$8)
                        .setValue(HugeMushroomBlock.EAST, $$9 > $$8)
                        .setValue(HugeMushroomBlock.NORTH, $$10 < -$$8)
                        .setValue(HugeMushroomBlock.SOUTH, $$10 > $$8);
                  }

                  this.placeMushroomBlock($$0, $$4, $$17);
               }
            }
         }
      }
   }

   @Override
   protected int getTreeRadiusForHeight(int $$0, int $$1, int $$2, int $$3) {
      int $$4 = 0;
      if ($$3 < $$1 && $$3 >= $$1 - 3) {
         $$4 = $$2;
      } else if ($$3 == $$1) {
         $$4 = $$2;
      }

      return $$4;
   }
}
