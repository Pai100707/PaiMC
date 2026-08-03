package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TintedGlassBlock extends TransparentBlock {
   public static final MapCodec<TintedGlassBlock> CODEC = simpleCodec(TintedGlassBlock::new);

   @Override
   public MapCodec<TintedGlassBlock> codec() {
      return CODEC;
   }

   public TintedGlassBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState $$0) {
      return false;
   }

   @Override
   protected int getLightBlock(BlockState $$0) {
      return 15;
   }
}
