package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class VaultBlock extends BaseEntityBlock {
   public static final MapCodec<VaultBlock> CODEC = simpleCodec(VaultBlock::new);
   public static final Property<VaultState> STATE = BlockStateProperties.VAULT_STATE;
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty OMINOUS = BlockStateProperties.OMINOUS;

   @Override
   public MapCodec<VaultBlock> codec() {
      return CODEC;
   }

   public VaultBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(STATE, VaultState.INACTIVE).setValue(OMINOUS, false));
   }

   @Override
   public InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if (!$$0.isEmpty() && $$1.getValue(STATE) == VaultState.ACTIVE) {
         if ($$2 instanceof ServerLevel $$7) {
            if (!($$7.getBlockEntity($$3) instanceof VaultBlockEntity $$8)) {
               return InteractionResult.TRY_WITH_EMPTY_HAND;
            }

            VaultBlockEntity.Server.tryInsertKey($$7, $$3, $$1, $$8.getConfig(), $$8.getServerData(), $$8.getSharedData(), $$4, $$0);
         }

         return InteractionResult.SUCCESS_SERVER;
      } else {
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new VaultBlockEntity($$0, $$1);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, STATE, OMINOUS);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return $$0 instanceof ServerLevel $$3
         ? createTickerHelper(
            $$2,
            BlockEntityType.VAULT,
            ($$1x, $$2x, $$3x, $$4) -> VaultBlockEntity.Server.tick($$3, $$2x, $$3x, $$4.getConfig(), $$4.getServerData(), $$4.getSharedData())
         )
         : createTickerHelper(
            $$2, BlockEntityType.VAULT, ($$0x, $$1x, $$2x, $$3x) -> VaultBlockEntity.Client.tick($$0x, $$1x, $$2x, $$3x.getClientData(), $$3x.getSharedData())
         );
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(FACING, $$0.getHorizontalDirection().getOpposite());
   }

   @Override
   public BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }
}
