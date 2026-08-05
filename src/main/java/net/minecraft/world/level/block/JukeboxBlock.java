package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class JukeboxBlock extends BaseEntityBlock {
   public static final MapCodec<JukeboxBlock> CODEC = simpleCodec(JukeboxBlock::new);
   public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

   @Override
   public MapCodec<JukeboxBlock> codec() {
      return CODEC;
   }

   protected JukeboxBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, false));
   }

   @Override
   public void setPlacedBy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, LivingEntity $$3, ItemStack $$4) {
      super.setPlacedBy($$0, $$1, $$2, $$3, $$4);
      TypedEntityData<BlockEntityType<?>> $$5 = (TypedEntityData<BlockEntityType<?>>)$$4.get(DataComponents.BLOCK_ENTITY_DATA);
      if ($$5 != null && $$5.contains("RecordItem")) {
         $$0.setBlock($$1, $$2.setValue(HAS_RECORD, true), 2);
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$0.getValue(HAS_RECORD) && $$1.getBlockEntity($$2) instanceof JukeboxBlockEntity $$5) {
         $$5.popOutTheItem();
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if ($$1.getValue(HAS_RECORD)) {
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      } else {
         ItemStack $$7 = $$4.getItemInHand($$5);
         InteractionResult $$8 = JukeboxPlayable.tryInsertIntoJukebox($$2, $$3, $$7, $$4);
         return (InteractionResult)(!$$8.consumesAction() ? InteractionResult.TRY_WITH_EMPTY_HAND : $$8);
      }
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      Containers.updateNeighboursAfterDestroy($$0, $$1, $$2);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new JukeboxBlockEntity($$0, $$1);
   }

   @Override
   public boolean isSignalSource(BlockState $$0) {
      return true;
   }

   @Override
   public int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$1.getBlockEntity($$2) instanceof JukeboxBlockEntity $$4 && $$4.getSongPlayer().isPlaying() ? 15 : 0;
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return $$1.getBlockEntity($$2) instanceof JukeboxBlockEntity $$4 ? $$4.getComparatorOutput() : 0;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(HAS_RECORD);
   }

   
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return $$1.getValue(HAS_RECORD) ? createTickerHelper($$2, BlockEntityType.JUKEBOX, JukeboxBlockEntity::tick) : null;
   }
}
