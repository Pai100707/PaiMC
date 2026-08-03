package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralMushroomFeature extends CoralFeature {
   public CoralMushroomFeature(Codec<NoneFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   protected boolean placeFeature(net.minecraft.world.level.LevelAccessor $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      int $$4 = $$1.nextInt(3) + 3;
      int $$5 = $$1.nextInt(3) + 3;
      int $$6 = $$1.nextInt(3) + 3;
      int $$7 = $$1.nextInt(3) + 1;
      MutableBlockPos $$8 = $$2.mutable();

      for (int $$9 = 0; $$9 <= $$5; $$9++) {
         for (int $$10 = 0; $$10 <= $$4; $$10++) {
            for (int $$11 = 0; $$11 <= $$6; $$11++) {
               $$8.set($$9 + $$2.getX(), $$10 + $$2.getY(), $$11 + $$2.getZ());
               $$8.move(Direction.DOWN, $$7);
               if (($$9 != 0 && $$9 != $$5 || $$10 != 0 && $$10 != $$4)
                  && ($$11 != 0 && $$11 != $$6 || $$10 != 0 && $$10 != $$4)
                  && ($$9 != 0 && $$9 != $$5 || $$11 != 0 && $$11 != $$6)
                  && ($$9 == 0 || $$9 == $$5 || $$10 == 0 || $$10 == $$4 || $$11 == 0 || $$11 == $$6)
                  && !($$1.nextFloat() < 0.1F)
                  && !this.placeCoralBlock($$0, $$1, $$8, $$3)) {
               }
            }
         }
      }

      return true;
   }
}
