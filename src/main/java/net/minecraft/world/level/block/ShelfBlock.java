package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SideChainPart;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShelfBlock extends BaseEntityBlock implements SelectableSlotContainer, SideChainPartBlock, SimpleWaterloggedBlock {
   public static final MapCodec<ShelfBlock> CODEC = simpleCodec(ShelfBlock::new);
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<SideChainPart> SIDE_CHAIN_PART = BlockStateProperties.SIDE_CHAIN_PART;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(
      Shapes.or(
         Block.box(0.0, 12.0, 11.0, 16.0, 16.0, 13.0),
         new VoxelShape[]{Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0), Block.box(0.0, 0.0, 11.0, 16.0, 4.0, 13.0)}
      )
   );

   @Override
   public MapCodec<ShelfBlock> codec() {
      return CODEC;
   }

   public ShelfBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(
         this.stateDefinition
            .any()
            .setValue(FACING, Direction.NORTH)
            .setValue(POWERED, false)
            .setValue(SIDE_CHAIN_PART, SideChainPart.UNCONNECTED)
            .setValue(WATERLOGGED, false)
      );
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get($$0.getValue(FACING));
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState $$0) {
      return true;
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return $$1 == PathComputationType.WATER && $$0.getFluidState().is(FluidTags.WATER);
   }

   
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new ShelfBlockEntity($$0, $$1);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, POWERED, SIDE_CHAIN_PART, WATERLOGGED);
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      Containers.updateNeighboursAfterDestroy($$0, $$1, $$2);
      this.updateNeighborsAfterPoweringDown($$1, $$2, $$0);
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      if (!$$1.isClientSide()) {
         boolean $$6 = $$1.hasNeighborSignal($$2);
         if ($$0.getValue(POWERED) != $$6) {
            BlockState $$7 = $$0.setValue(POWERED, $$6);
            if (!$$6) {
               $$7 = $$7.setValue(SIDE_CHAIN_PART, SideChainPart.UNCONNECTED);
            }

            $$1.setBlock($$2, $$7, 3);
            this.playSound($$1, $$2, $$6 ? SoundEvents.SHELF_ACTIVATE : SoundEvents.SHELF_DEACTIVATE);
            $$1.gameEvent($$6 ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, $$2, GameEvent.Context.of($$7));
         }
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      return this.defaultBlockState()
         .setValue(FACING, $$0.getHorizontalDirection().getOpposite())
         .setValue(POWERED, $$0.getLevel().hasNeighborSignal($$0.getClickedPos()))
         .setValue(WATERLOGGED, $$1.getType() == Fluids.WATER);
   }

   @Override
   public BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }

   @Override
   public int getRows() {
      return 1;
   }

   @Override
   public int getColumns() {
      return 3;
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if ($$2.getBlockEntity($$3) instanceof ShelfBlockEntity $$7 && !$$5.equals(InteractionHand.OFF_HAND)) {
         OptionalInt $$9 = this.getHitSlot($$6, $$1.getValue(FACING));
         if ($$9.isEmpty()) {
            return InteractionResult.PASS;
         } else {
            Inventory $$10 = $$4.getInventory();
            if ($$2.isClientSide()) {
               return (InteractionResult)($$10.getSelectedItem().isEmpty() ? InteractionResult.PASS : InteractionResult.SUCCESS);
            } else if (!$$1.getValue(POWERED)) {
               boolean $$11 = swapSingleItem($$0, $$4, $$7, $$9.getAsInt(), $$10);
               if ($$11) {
                  this.playSound($$2, $$3, $$0.isEmpty() ? SoundEvents.SHELF_TAKE_ITEM : SoundEvents.SHELF_SINGLE_SWAP);
               } else {
                  if ($$0.isEmpty()) {
                     return InteractionResult.PASS;
                  }

                  this.playSound($$2, $$3, SoundEvents.SHELF_PLACE_ITEM);
               }

               return InteractionResult.SUCCESS.heldItemTransformedTo($$0);
            } else {
               ItemStack $$12 = $$10.getSelectedItem();
               boolean $$13 = this.swapHotbar($$2, $$3, $$10);
               if (!$$13) {
                  return InteractionResult.CONSUME;
               } else {
                  this.playSound($$2, $$3, SoundEvents.SHELF_MULTI_SWAP);
                  return $$12 == $$10.getSelectedItem() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS.heldItemTransformedTo($$10.getSelectedItem());
               }
            }
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private static boolean swapSingleItem(ItemStack $$0, Player $$1, ShelfBlockEntity $$2, int $$3, Inventory $$4) {
      ItemStack $$5 = $$2.swapItemNoUpdate($$3, $$0);
      ItemStack $$6 = $$1.hasInfiniteMaterials() && $$5.isEmpty() ? $$0.copy() : $$5;
      $$4.setItem($$4.getSelectedSlot(), $$6);
      $$4.setChanged();
      $$2.setChanged(
         $$6.has(DataComponents.USE_EFFECTS) && !((UseEffects)$$6.get(DataComponents.USE_EFFECTS)).interactVibrations() ? null : GameEvent.ITEM_INTERACT_FINISH
      );
      return !$$5.isEmpty();
   }

   private boolean swapHotbar(net.minecraft.world.level.Level $$0, BlockPos $$1, Inventory $$2) {
      List<BlockPos> $$3 = this.getAllBlocksConnectedTo($$0, $$1);
      if ($$3.isEmpty()) {
         return false;
      } else {
         boolean $$4 = false;

         for (int $$5 = 0; $$5 < $$3.size(); $$5++) {
            ShelfBlockEntity $$6 = (ShelfBlockEntity)$$0.getBlockEntity($$3.get($$5));
            if ($$6 != null) {
               for (int $$7 = 0; $$7 < $$6.getContainerSize(); $$7++) {
                  int $$8 = 9 - ($$3.size() - $$5) * $$6.getContainerSize() + $$7;
                  if ($$8 >= 0 && $$8 <= $$2.getContainerSize()) {
                     ItemStack $$9 = $$2.removeItemNoUpdate($$8);
                     ItemStack $$10 = $$6.swapItemNoUpdate($$7, $$9);
                     if (!$$9.isEmpty() || !$$10.isEmpty()) {
                        $$2.setItem($$8, $$10);
                        $$4 = true;
                     }
                  }
               }

               $$2.setChanged();
               $$6.setChanged(GameEvent.ENTITY_INTERACT);
            }
         }

         return $$4;
      }
   }

   @Override
   public SideChainPart getSideChainPart(BlockState $$0) {
      return $$0.getValue(SIDE_CHAIN_PART);
   }

   @Override
   public BlockState setSideChainPart(BlockState $$0, SideChainPart $$1) {
      return $$0.setValue(SIDE_CHAIN_PART, $$1);
   }

   @Override
   public Direction getFacing(BlockState $$0) {
      return $$0.getValue(FACING);
   }

   @Override
   public boolean isConnectable(BlockState $$0) {
      return $$0.is(BlockTags.WOODEN_SHELVES) && $$0.hasProperty(POWERED) && $$0.getValue(POWERED);
   }

   @Override
   public int getMaxChainLength() {
      return 3;
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if ($$0.getValue(POWERED)) {
         this.updateSelfAndNeighborsOnPoweringUp($$1, $$2, $$0, $$3);
      } else {
         this.updateNeighborsAfterPoweringDown($$1, $$2, $$0);
      }
   }

   private void playSound(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, SoundEvent $$2) {
      $$0.playSound(null, $$1, $$2, SoundSource.BLOCKS, 1.0F, 1.0F);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      if ($$0.getValue(WATERLOGGED)) {
         $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
      }

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      if ($$1.isClientSide()) {
         return 0;
      } else if ($$3 != ((Direction)$$0.getValue(FACING)).getOpposite()) {
         return 0;
      } else if ($$1.getBlockEntity($$2) instanceof ShelfBlockEntity $$4) {
         int $$5 = $$4.getItem(0).isEmpty() ? 0 : 1;
         int $$6 = $$4.getItem(1).isEmpty() ? 0 : 1;
         int $$7 = $$4.getItem(2).isEmpty() ? 0 : 1;
         return $$5 | $$6 << 1 | $$7 << 2;
      } else {
         return 0;
      }
   }
}
