package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SmokerBlock extends AbstractFurnaceBlock {
   public static final MapCodec<SmokerBlock> CODEC = simpleCodec(SmokerBlock::new);

   @Override
   public MapCodec<SmokerBlock> codec() {
      return CODEC;
   }

   protected SmokerBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new SmokerBlockEntity($$0, $$1);
   }

   
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return createFurnaceTicker($$0, $$2, BlockEntityType.SMOKER);
   }

   @Override
   protected void openContainer(net.minecraft.world.level.Level $$0, BlockPos $$1, Player $$2) {
      BlockEntity $$3 = $$0.getBlockEntity($$1);
      if ($$3 instanceof SmokerBlockEntity) {
         $$2.openMenu((MenuProvider)$$3);
         $$2.awardStat(Stats.INTERACT_WITH_SMOKER);
      }
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(LIT)) {
         double $$4 = $$2.getX() + 0.5;
         double $$5 = $$2.getY();
         double $$6 = $$2.getZ() + 0.5;
         if ($$3.nextDouble() < 0.1) {
            $$1.playLocalSound($$4, $$5, $$6, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         $$1.addParticle(ParticleTypes.SMOKE, $$4, $$5 + 1.1, $$6, 0.0, 0.0, 0.0);
      }
   }
}
