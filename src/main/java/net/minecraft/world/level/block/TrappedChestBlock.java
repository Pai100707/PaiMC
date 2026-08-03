package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TrappedChestBlock extends ChestBlock {
   public static final MapCodec<TrappedChestBlock> CODEC = simpleCodec(TrappedChestBlock::new);

   @Override
   public MapCodec<TrappedChestBlock> codec() {
      return CODEC;
   }

   public TrappedChestBlock(BlockBehaviour.Properties $$0) {
      super(() -> BlockEntityType.TRAPPED_CHEST, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, $$0);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new TrappedChestBlockEntity($$0, $$1);
   }

   @Override
   protected Stat<Identifier> getOpenChestStat() {
      return Stats.CUSTOM.get(Stats.TRIGGER_TRAPPED_CHEST);
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return true;
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return Mth.clamp(ChestBlockEntity.getOpenCount($$1, $$2), 0, 15);
   }

   @Override
   protected int getDirectSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$3 == Direction.UP ? $$0.getSignal($$1, $$2, $$3) : 0;
   }
}
