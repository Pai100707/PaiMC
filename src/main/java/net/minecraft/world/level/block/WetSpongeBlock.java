package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WetSpongeBlock extends Block {
   public static final MapCodec<WetSpongeBlock> CODEC = simpleCodec(WetSpongeBlock::new);

   @Override
   public MapCodec<WetSpongeBlock> codec() {
      return CODEC;
   }

   protected WetSpongeBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if ((Boolean)$$1.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, $$2)) {
         $$1.setBlock($$2, Blocks.SPONGE.defaultBlockState(), 3);
         $$1.levelEvent(2009, $$2, 0);
         $$1.playSound(null, $$2, SoundEvents.WET_SPONGE_DRIES, SoundSource.BLOCKS, 1.0F, (1.0F + $$1.getRandom().nextFloat() * 0.2F) * 0.7F);
      }
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      Direction $$4 = Direction.getRandom($$3);
      if ($$4 != Direction.UP) {
         BlockPos $$5 = $$2.relative($$4);
         BlockState $$6 = $$1.getBlockState($$5);
         if (!$$0.canOcclude() || !$$6.isFaceSturdy($$1, $$5, $$4.getOpposite())) {
            double $$7 = $$2.getX();
            double $$8 = $$2.getY();
            double $$9 = $$2.getZ();
            if ($$4 == Direction.DOWN) {
               $$8 -= 0.05;
               $$7 += $$3.nextDouble();
               $$9 += $$3.nextDouble();
            } else {
               $$8 += $$3.nextDouble() * 0.8;
               if ($$4.getAxis() == Axis.X) {
                  $$9 += $$3.nextDouble();
                  if ($$4 == Direction.EAST) {
                     $$7++;
                  } else {
                     $$7 += 0.05;
                  }
               } else {
                  $$7 += $$3.nextDouble();
                  if ($$4 == Direction.SOUTH) {
                     $$9++;
                  } else {
                     $$9 += 0.05;
                  }
               }
            }

            $$1.addParticle(ParticleTypes.DRIPPING_WATER, $$7, $$8, $$9, 0.0, 0.0, 0.0);
         }
      }
   }
}
