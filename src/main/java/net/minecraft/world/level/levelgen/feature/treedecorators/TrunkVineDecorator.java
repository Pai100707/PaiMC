package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;

public class TrunkVineDecorator extends TreeDecorator {
   public static final MapCodec<TrunkVineDecorator> CODEC = MapCodec.unit(() -> TrunkVineDecorator.INSTANCE);
   public static final TrunkVineDecorator INSTANCE = new TrunkVineDecorator();

   @Override
   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.TRUNK_VINE;
   }

   @Override
   public void place(TreeDecorator.Context $$0) {
      RandomSource $$1 = $$0.random();
      $$0.logs().forEach($$2 -> {
         if ($$1.nextInt(3) > 0) {
            BlockPos $$3 = $$2.west();
            if ($$0.isAir($$3)) {
               $$0.placeVine($$3, VineBlock.EAST);
            }
         }

         if ($$1.nextInt(3) > 0) {
            BlockPos $$4 = $$2.east();
            if ($$0.isAir($$4)) {
               $$0.placeVine($$4, VineBlock.WEST);
            }
         }

         if ($$1.nextInt(3) > 0) {
            BlockPos $$5 = $$2.north();
            if ($$0.isAir($$5)) {
               $$0.placeVine($$5, VineBlock.SOUTH);
            }
         }

         if ($$1.nextInt(3) > 0) {
            BlockPos $$6 = $$2.south();
            if ($$0.isAir($$6)) {
               $$0.placeVine($$6, VineBlock.NORTH);
            }
         }
      });
   }
}
