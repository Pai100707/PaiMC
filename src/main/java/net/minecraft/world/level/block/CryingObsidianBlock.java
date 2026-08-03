package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CryingObsidianBlock extends Block {
   public static final MapCodec<CryingObsidianBlock> CODEC = simpleCodec(CryingObsidianBlock::new);

   @Override
   public MapCodec<CryingObsidianBlock> codec() {
      return CODEC;
   }

   public CryingObsidianBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$3.nextInt(5) == 0) {
         Direction $$4 = Direction.getRandom($$3);
         if ($$4 != Direction.UP) {
            BlockPos $$5 = $$2.relative($$4);
            BlockState $$6 = $$1.getBlockState($$5);
            if (!$$0.canOcclude() || !$$6.isFaceSturdy($$1, $$5, $$4.getOpposite())) {
               double $$7 = $$4.getStepX() == 0 ? $$3.nextDouble() : 0.5 + $$4.getStepX() * 0.6;
               double $$8 = $$4.getStepY() == 0 ? $$3.nextDouble() : 0.5 + $$4.getStepY() * 0.6;
               double $$9 = $$4.getStepZ() == 0 ? $$3.nextDouble() : 0.5 + $$4.getStepZ() * 0.6;
               $$1.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, $$2.getX() + $$7, $$2.getY() + $$8, $$2.getZ() + $$9, 0.0, 0.0, 0.0);
            }
         }
      }
   }
}
