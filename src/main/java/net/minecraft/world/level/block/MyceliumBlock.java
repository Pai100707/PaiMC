package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MyceliumBlock extends SpreadingSnowyDirtBlock {
   public static final MapCodec<MyceliumBlock> CODEC = simpleCodec(MyceliumBlock::new);

   @Override
   public MapCodec<MyceliumBlock> codec() {
      return CODEC;
   }

   public MyceliumBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      super.animateTick($$0, $$1, $$2, $$3);
      if ($$3.nextInt(10) == 0) {
         $$1.addParticle(ParticleTypes.MYCELIUM, $$2.getX() + $$3.nextDouble(), $$2.getY() + 1.1, $$2.getZ() + $$3.nextDouble(), 0.0, 0.0, 0.0);
      }
   }
}
