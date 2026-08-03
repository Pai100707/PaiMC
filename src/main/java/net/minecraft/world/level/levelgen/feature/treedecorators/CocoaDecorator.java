package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;

public class CocoaDecorator extends TreeDecorator {
   public static final MapCodec<CocoaDecorator> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(CocoaDecorator::new, $$0 -> $$0.probability);
   private final float probability;

   public CocoaDecorator(float $$0) {
      this.probability = $$0;
   }

   @Override
   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.COCOA;
   }

   @Override
   public void place(TreeDecorator.Context $$0) {
      RandomSource $$1 = $$0.random();
      if (!($$1.nextFloat() >= this.probability)) {
         List<BlockPos> $$2 = $$0.logs();
         if (!$$2.isEmpty()) {
            int $$3 = $$2.getFirst().getY();
            $$2.stream().filter($$1x -> $$1x.getY() - $$3 <= 2).forEach($$2x -> {
               for (Direction $$3x : Plane.HORIZONTAL) {
                  if ($$1.nextFloat() <= 0.25F) {
                     Direction $$4 = $$3x.getOpposite();
                     BlockPos $$5 = $$2x.offset($$4.getStepX(), 0, $$4.getStepZ());
                     if ($$0.isAir($$5)) {
                        $$0.setBlock($$5, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, $$1.nextInt(3)).setValue(CocoaBlock.FACING, $$3x));
                     }
                  }
               }
            });
         }
      }
   }
}
