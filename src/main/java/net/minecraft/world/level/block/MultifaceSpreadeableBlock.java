package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class MultifaceSpreadeableBlock extends MultifaceBlock {
   public MultifaceSpreadeableBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   public abstract MapCodec<? extends MultifaceSpreadeableBlock> codec();

   public abstract MultifaceSpreader getSpreader();
}
