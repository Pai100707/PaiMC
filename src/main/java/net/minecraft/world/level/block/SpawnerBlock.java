package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SpawnerBlock extends BaseEntityBlock {
   public static final MapCodec<SpawnerBlock> CODEC = simpleCodec(SpawnerBlock::new);

   @Override
   public MapCodec<SpawnerBlock> codec() {
      return CODEC;
   }

   protected SpawnerBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new SpawnerBlockEntity($$0, $$1);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return createTickerHelper($$2, BlockEntityType.MOB_SPAWNER, $$0.isClientSide() ? SpawnerBlockEntity::clientTick : SpawnerBlockEntity::serverTick);
   }

   @Override
   protected void spawnAfterBreak(BlockState $$0, ServerLevel $$1, BlockPos $$2, ItemStack $$3, boolean $$4) {
      super.spawnAfterBreak($$0, $$1, $$2, $$3, $$4);
      if ($$4) {
         int $$5 = 15 + $$1.random.nextInt(15) + $$1.random.nextInt(15);
         this.popExperience($$1, $$2, $$5);
      }
   }
}
