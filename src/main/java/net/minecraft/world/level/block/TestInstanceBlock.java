package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TestInstanceBlock extends BaseEntityBlock implements GameMasterBlock {
   public static final MapCodec<TestInstanceBlock> CODEC = simpleCodec(TestInstanceBlock::new);

   public TestInstanceBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new TestInstanceBlockEntity($$0, $$1);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$1.getBlockEntity($$2) instanceof TestInstanceBlockEntity $$6) {
         if (!$$3.canUseGameMasterBlocks()) {
            return InteractionResult.PASS;
         } else {
            if ($$3.level().isClientSide()) {
               $$3.openTestInstanceBlock($$6);
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   protected MapCodec<TestInstanceBlock> codec() {
      return CODEC;
   }
}
