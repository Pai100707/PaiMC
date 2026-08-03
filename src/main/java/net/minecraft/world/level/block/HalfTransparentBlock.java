package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class HalfTransparentBlock extends Block {
   public static final MapCodec<HalfTransparentBlock> CODEC = simpleCodec(HalfTransparentBlock::new);

   @Override
   protected MapCodec<? extends HalfTransparentBlock> codec() {
      return CODEC;
   }

   protected HalfTransparentBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected boolean skipRendering(BlockState $$0, BlockState $$1, Direction $$2) {
      return $$1.is(this) ? true : super.skipRendering($$0, $$1, $$2);
   }
}
