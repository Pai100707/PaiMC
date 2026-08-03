package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;

public class CreakingHeartDecorator extends TreeDecorator {
   public static final MapCodec<CreakingHeartDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
      .fieldOf("probability")
      .xmap(CreakingHeartDecorator::new, $$0 -> $$0.probability);
   private final float probability;

   public CreakingHeartDecorator(float $$0) {
      this.probability = $$0;
   }

   @Override
   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.CREAKING_HEART;
   }

   @Override
   public void place(TreeDecorator.Context $$0) {
      RandomSource $$1 = $$0.random();
      List<BlockPos> $$2 = $$0.logs();
      if (!$$2.isEmpty()) {
         if (!($$1.nextFloat() >= this.probability)) {
            List<BlockPos> $$3 = new ArrayList<>($$2);
            Util.shuffle($$3, $$1);
            Optional<BlockPos> $$4 = $$3.stream().filter($$1x -> {
               for (Direction $$2x : Direction.values()) {
                  if (!$$0.checkBlock($$1x.relative($$2x), $$0xx -> $$0xx.is(BlockTags.LOGS))) {
                     return false;
                  }
               }

               return true;
            }).findFirst();
            if (!$$4.isEmpty()) {
               $$0.setBlock(
                  $$4.get(),
                  Blocks.CREAKING_HEART
                     .defaultBlockState()
                     .setValue(CreakingHeartBlock.STATE, CreakingHeartState.DORMANT)
                     .setValue(CreakingHeartBlock.NATURAL, true)
               );
            }
         }
      }
   }
}
