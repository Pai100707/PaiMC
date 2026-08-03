package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.TestBlockMode;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class TestBlock extends BaseEntityBlock implements GameMasterBlock {
   public static final MapCodec<TestBlock> CODEC = simpleCodec(TestBlock::new);
   public static final EnumProperty<TestBlockMode> MODE = BlockStateProperties.TEST_BLOCK_MODE;

   public TestBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new TestBlockEntity($$0, $$1);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockItemStateProperties $$1 = (BlockItemStateProperties)$$0.getItemInHand().get(DataComponents.BLOCK_STATE);
      BlockState $$2 = this.defaultBlockState();
      if ($$1 != null) {
         TestBlockMode $$3 = (TestBlockMode)$$1.get(MODE);
         if ($$3 != null) {
            $$2 = $$2.setValue(MODE, $$3);
         }
      }

      return $$2;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(MODE);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$1.getBlockEntity($$2) instanceof TestBlockEntity $$6) {
         if (!$$3.canUseGameMasterBlocks()) {
            return InteractionResult.PASS;
         } else {
            if ($$1.isClientSide()) {
               $$3.openTestBlock($$6);
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      TestBlockEntity $$4 = getServerTestBlockEntity($$1, $$2);
      if ($$4 != null) {
         $$4.reset();
      }
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, @Nullable Orientation $$4, boolean $$5) {
      TestBlockEntity $$6 = getServerTestBlockEntity($$1, $$2);
      if ($$6 != null) {
         if ($$6.getMode() != TestBlockMode.START) {
            boolean $$7 = $$1.hasNeighborSignal($$2);
            boolean $$8 = $$6.isPowered();
            if ($$7 && !$$8) {
               $$6.setPowered(true);
               $$6.trigger();
            } else if (!$$7 && $$8) {
               $$6.setPowered(false);
            }
         }
      }
   }

   @Nullable
   private static TestBlockEntity getServerTestBlockEntity(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      return $$0 instanceof ServerLevel $$2 && $$2.getBlockEntity($$1) instanceof TestBlockEntity $$3 ? $$3 : null;
   }

   @Override
   public int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      if ($$0.getValue(MODE) != TestBlockMode.START) {
         return 0;
      } else if ($$1.getBlockEntity($$2) instanceof TestBlockEntity $$5) {
         return $$5.isPowered() ? 15 : 0;
      } else {
         return 0;
      }
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      ItemStack $$4 = super.getCloneItemStack($$0, $$1, $$2, $$3);
      return setModeOnStack($$4, $$2.getValue(MODE));
   }

   public static ItemStack setModeOnStack(ItemStack $$0, TestBlockMode $$1) {
      $$0.set(
         DataComponents.BLOCK_STATE, ((BlockItemStateProperties)$$0.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY)).with(MODE, $$1)
      );
      return $$0;
   }

   @Override
   protected MapCodec<TestBlock> codec() {
      return CODEC;
   }
}
