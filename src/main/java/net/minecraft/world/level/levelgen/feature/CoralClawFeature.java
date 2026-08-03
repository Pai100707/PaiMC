package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralClawFeature extends CoralFeature {
   public CoralClawFeature(Codec<NoneFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   protected boolean placeFeature(net.minecraft.world.level.LevelAccessor $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      if (!this.placeCoralBlock($$0, $$1, $$2, $$3)) {
         return false;
      } else {
         Direction $$4 = Plane.HORIZONTAL.getRandomDirection($$1);
         int $$5 = $$1.nextInt(2) + 2;
         List<Direction> $$6 = Util.toShuffledList(Stream.of($$4, $$4.getClockWise(), $$4.getCounterClockWise()), $$1);

         for (Direction $$8 : $$6.subList(0, $$5)) {
            MutableBlockPos $$9 = $$2.mutable();
            int $$10 = $$1.nextInt(2) + 1;
            $$9.move($$8);
            int $$12;
            Direction $$11;
            if ($$8 == $$4) {
               $$11 = $$4;
               $$12 = $$1.nextInt(3) + 2;
            } else {
               $$9.move(Direction.UP);
               Direction[] $$13 = new Direction[]{$$8, Direction.UP};
               $$11 = (Direction)Util.getRandom($$13, $$1);
               $$12 = $$1.nextInt(3) + 3;
            }

            for (int $$16 = 0; $$16 < $$10 && this.placeCoralBlock($$0, $$1, $$9, $$3); $$16++) {
               $$9.move($$11);
            }

            $$9.move($$11.getOpposite());
            $$9.move(Direction.UP);

            for (int $$17 = 0; $$17 < $$12; $$17++) {
               $$9.move($$4);
               if (!this.placeCoralBlock($$0, $$1, $$9, $$3)) {
                  break;
               }

               if ($$1.nextFloat() < 0.25F) {
                  $$9.move(Direction.UP);
               }
            }
         }

         return true;
      }
   }
}
