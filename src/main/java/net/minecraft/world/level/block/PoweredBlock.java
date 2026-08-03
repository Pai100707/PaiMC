package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredBlock extends Block {
   public static final MapCodec<PoweredBlock> CODEC = simpleCodec(PoweredBlock::new);

   @Override
   public MapCodec<PoweredBlock> codec() {
      return CODEC;
   }

   public PoweredBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return true;
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return 15;
   }
}
