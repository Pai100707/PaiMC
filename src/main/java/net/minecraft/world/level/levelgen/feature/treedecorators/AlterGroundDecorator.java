package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AlterGroundDecorator extends TreeDecorator {
   public static final MapCodec<AlterGroundDecorator> CODEC = BlockStateProvider.CODEC.fieldOf("provider").xmap(AlterGroundDecorator::new, $$0 -> $$0.provider);
   private final BlockStateProvider provider;

   public AlterGroundDecorator(BlockStateProvider $$0) {
      this.provider = $$0;
   }

   @Override
   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.ALTER_GROUND;
   }

   @Override
   public void place(TreeDecorator.Context $$0) {
      List<BlockPos> $$1 = TreeFeature.getLowestTrunkOrRootOfTree($$0);
      if (!$$1.isEmpty()) {
         int $$2 = $$1.get(0).getY();
         $$1.stream().filter($$1x -> $$1x.getY() == $$2).forEach($$1x -> {
            this.placeCircle($$0, $$1x.west().north());
            this.placeCircle($$0, $$1x.east(2).north());
            this.placeCircle($$0, $$1x.west().south(2));
            this.placeCircle($$0, $$1x.east(2).south(2));

            for (int $$2x = 0; $$2x < 5; $$2x++) {
               int $$3 = $$0.random().nextInt(64);
               int $$4 = $$3 % 8;
               int $$5 = $$3 / 8;
               if ($$4 == 0 || $$4 == 7 || $$5 == 0 || $$5 == 7) {
                  this.placeCircle($$0, $$1x.offset(-3 + $$4, 0, -3 + $$5));
               }
            }
         });
      }
   }

   private void placeCircle(TreeDecorator.Context $$0, BlockPos $$1) {
      for (int $$2 = -2; $$2 <= 2; $$2++) {
         for (int $$3 = -2; $$3 <= 2; $$3++) {
            if (Math.abs($$2) != 2 || Math.abs($$3) != 2) {
               this.placeBlockAt($$0, $$1.offset($$2, 0, $$3));
            }
         }
      }
   }

   private void placeBlockAt(TreeDecorator.Context $$0, BlockPos $$1) {
      for (int $$2 = 2; $$2 >= -3; $$2--) {
         BlockPos $$3 = $$1.above($$2);
         if (Feature.isGrassOrDirt($$0.level(), $$3)) {
            $$0.setBlock($$3, this.provider.getState($$0.random(), $$1));
            break;
         }

         if (!$$0.isAir($$3) && $$2 < 0) {
            break;
         }
      }
   }
}
