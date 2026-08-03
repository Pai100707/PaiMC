package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

@Deprecated
public class CountOnEveryLayerPlacement extends PlacementModifier {
   public static final MapCodec<CountOnEveryLayerPlacement> CODEC = IntProvider.codec(0, 256)
      .fieldOf("count")
      .xmap(CountOnEveryLayerPlacement::new, $$0 -> $$0.count);
   private final IntProvider count;

   private CountOnEveryLayerPlacement(IntProvider $$0) {
      this.count = $$0;
   }

   public static CountOnEveryLayerPlacement of(IntProvider $$0) {
      return new CountOnEveryLayerPlacement($$0);
   }

   public static CountOnEveryLayerPlacement of(int $$0) {
      return of(ConstantInt.of($$0));
   }

   @Override
   public Stream<BlockPos> getPositions(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      Builder<BlockPos> $$3 = Stream.builder();
      int $$4 = 0;

      boolean $$5;
      do {
         $$5 = false;

         for (int $$6 = 0; $$6 < this.count.sample($$1); $$6++) {
            int $$7 = $$1.nextInt(16) + $$2.getX();
            int $$8 = $$1.nextInt(16) + $$2.getZ();
            int $$9 = $$0.getHeight(Heightmap.Types.MOTION_BLOCKING, $$7, $$8);
            int $$10 = findOnGroundYPosition($$0, $$7, $$9, $$8, $$4);
            if ($$10 != Integer.MAX_VALUE) {
               $$3.add(new BlockPos($$7, $$10, $$8));
               $$5 = true;
            }
         }

         $$4++;
      } while ($$5);

      return $$3.build();
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.COUNT_ON_EVERY_LAYER;
   }

   private static int findOnGroundYPosition(PlacementContext $$0, int $$1, int $$2, int $$3, int $$4) {
      MutableBlockPos $$5 = new MutableBlockPos($$1, $$2, $$3);
      int $$6 = 0;
      BlockState $$7 = $$0.getBlockState($$5);

      for (int $$8 = $$2; $$8 >= $$0.getMinY() + 1; $$8--) {
         $$5.setY($$8 - 1);
         BlockState $$9 = $$0.getBlockState($$5);
         if (!isEmpty($$9) && isEmpty($$7) && !$$9.is(Blocks.BEDROCK)) {
            if ($$6 == $$4) {
               return $$5.getY() + 1;
            }

            $$6++;
         }

         $$7 = $$9;
      }

      return Integer.MAX_VALUE;
   }

   private static boolean isEmpty(BlockState $$0) {
      return $$0.isAir() || $$0.is(Blocks.WATER) || $$0.is(Blocks.LAVA);
   }
}
