package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class LeaveVineDecorator extends TreeDecorator {
   public static final MapCodec<LeaveVineDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
      .fieldOf("probability")
      .xmap(LeaveVineDecorator::new, $$0 -> $$0.probability);
   private final float probability;

   @Override
   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.LEAVE_VINE;
   }

   public LeaveVineDecorator(float $$0) {
      this.probability = $$0;
   }

   @Override
   public void place(TreeDecorator.Context $$0) {
      RandomSource $$1 = $$0.random();
      $$0.leaves().forEach($$2 -> {
         if ($$1.nextFloat() < this.probability) {
            BlockPos $$3 = $$2.west();
            if ($$0.isAir($$3)) {
               addHangingVine($$3, VineBlock.EAST, $$0);
            }
         }

         if ($$1.nextFloat() < this.probability) {
            BlockPos $$4 = $$2.east();
            if ($$0.isAir($$4)) {
               addHangingVine($$4, VineBlock.WEST, $$0);
            }
         }

         if ($$1.nextFloat() < this.probability) {
            BlockPos $$5 = $$2.north();
            if ($$0.isAir($$5)) {
               addHangingVine($$5, VineBlock.SOUTH, $$0);
            }
         }

         if ($$1.nextFloat() < this.probability) {
            BlockPos $$6 = $$2.south();
            if ($$0.isAir($$6)) {
               addHangingVine($$6, VineBlock.NORTH, $$0);
            }
         }
      });
   }

   private static void addHangingVine(BlockPos $$0, BooleanProperty $$1, TreeDecorator.Context $$2) {
      $$2.placeVine($$0, $$1);
      int $$3 = 4;

      for (BlockPos var4 = $$0.below(); $$2.isAir(var4) && $$3 > 0; $$3--) {
         $$2.placeVine(var4, $$1);
         var4 = var4.below();
      }
   }
}
