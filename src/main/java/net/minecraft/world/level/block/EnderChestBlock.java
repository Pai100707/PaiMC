package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnderChestBlock extends AbstractChestBlock<EnderChestBlockEntity> implements SimpleWaterloggedBlock {
   public static final MapCodec<EnderChestBlock> CODEC = simpleCodec(EnderChestBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 14.0);
   private static final Component CONTAINER_TITLE = Component.translatable("container.enderchest");

   @Override
   public MapCodec<EnderChestBlock> codec() {
      return CODEC;
   }

   protected EnderChestBlock(BlockBehaviour.Properties $$0) {
      super($$0, () -> BlockEntityType.ENDER_CHEST);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
   }

   @Override
   public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(
      BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, boolean $$3
   ) {
      return DoubleBlockCombiner.Combiner::acceptNone;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      return this.defaultBlockState().setValue(FACING, $$0.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, $$1.getType() == Fluids.WATER);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      PlayerEnderChestContainer $$5 = $$3.getEnderChestInventory();
      if ($$5 != null && $$1.getBlockEntity($$2) instanceof EnderChestBlockEntity $$7) {
         BlockPos $$9 = $$2.above();
         if ($$1.getBlockState($$9).isRedstoneConductor($$1, $$9)) {
            return InteractionResult.SUCCESS;
         } else {
            if ($$1 instanceof ServerLevel $$10) {
               $$5.setActiveChest($$7);
               $$3.openMenu(new SimpleMenuProvider(($$1x, $$2x, $$3x) -> ChestMenu.threeRows($$1x, $$2x, $$5), CONTAINER_TITLE));
               $$3.awardStat(Stats.OPEN_ENDERCHEST);
               PiglinAi.angerNearbyPiglins($$10, $$3, true);
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new EnderChestBlockEntity($$0, $$1);
   }

   
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return $$0.isClientSide() ? createTickerHelper($$2, BlockEntityType.ENDER_CHEST, EnderChestBlockEntity::lidAnimateTick) : null;
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      for (int $$4 = 0; $$4 < 3; $$4++) {
         int $$5 = $$3.nextInt(2) * 2 - 1;
         int $$6 = $$3.nextInt(2) * 2 - 1;
         double $$7 = $$2.getX() + 0.5 + 0.25 * $$5;
         double $$8 = $$2.getY() + $$3.nextFloat();
         double $$9 = $$2.getZ() + 0.5 + 0.25 * $$6;
         double $$10 = $$3.nextFloat() * $$5;
         double $$11 = ($$3.nextFloat() - 0.5) * 0.125;
         double $$12 = $$3.nextFloat() * $$6;
         $$1.addParticle(ParticleTypes.PORTAL, $$7, $$8, $$9, $$10, $$11, $$12);
      }
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, WATERLOGGED);
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
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      BlockEntity $$4 = $$1.getBlockEntity($$2);
      if ($$4 instanceof EnderChestBlockEntity) {
         ((EnderChestBlockEntity)$$4).recheckOpen();
      }
   }
}
