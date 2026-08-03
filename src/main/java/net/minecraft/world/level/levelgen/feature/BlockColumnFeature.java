package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;

public class BlockColumnFeature extends Feature<BlockColumnConfiguration> {
   public BlockColumnFeature(Codec<BlockColumnConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<BlockColumnConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      BlockColumnConfiguration $$2 = $$0.config();
      RandomSource $$3 = $$0.random();
      int $$4 = $$2.layers().size();
      int[] $$5 = new int[$$4];
      int $$6 = 0;

      for (int $$7 = 0; $$7 < $$4; $$7++) {
         $$5[$$7] = $$2.layers().get($$7).height().sample($$3);
         $$6 += $$5[$$7];
      }

      if ($$6 == 0) {
         return false;
      } else {
         MutableBlockPos $$8 = $$0.origin().mutable();
         MutableBlockPos $$9 = $$8.mutable().move($$2.direction());

         for (int $$10 = 0; $$10 < $$6; $$10++) {
            if (!$$2.allowedPlacement().test($$1, $$9)) {
               truncate($$5, $$6, $$10, $$2.prioritizeTip());
               break;
            }

            $$9.move($$2.direction());
         }

         for (int $$11 = 0; $$11 < $$4; $$11++) {
            int $$12 = $$5[$$11];
            if ($$12 != 0) {
               BlockColumnConfiguration.Layer $$13 = $$2.layers().get($$11);

               for (int $$14 = 0; $$14 < $$12; $$14++) {
                  $$1.setBlock($$8, $$13.state().getState($$3, $$8), 2);
                  $$8.move($$2.direction());
               }
            }
         }

         return true;
      }
   }

   private static void truncate(int[] $$0, int $$1, int $$2, boolean $$3) {
      int $$4 = $$1 - $$2;
      int $$5 = $$3 ? 1 : -1;
      int $$6 = $$3 ? 0 : $$0.length - 1;
      int $$7 = $$3 ? $$0.length : -1;

      for (int $$8 = $$6; $$8 != $$7 && $$4 > 0; $$8 += $$5) {
         int $$9 = $$0[$$8];
         int $$10 = Math.min($$9, $$4);
         $$4 -= $$10;
         $$0[$$8] -= $$10;
      }
   }
}
