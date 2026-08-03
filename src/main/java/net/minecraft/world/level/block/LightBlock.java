package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LightBlock extends Block implements SimpleWaterloggedBlock {
   public static final MapCodec<LightBlock> CODEC = simpleCodec(LightBlock::new);
   public static final int MAX_LEVEL = 15;
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final ToIntFunction<BlockState> LIGHT_EMISSION = $$0 -> $$0.getValue(LEVEL);

   @Override
   public MapCodec<LightBlock> codec() {
      return CODEC;
   }

   public LightBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 15).setValue(WATERLOGGED, false));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(LEVEL, WATERLOGGED);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$1.isClientSide() && $$3.canUseGameMasterBlocks()) {
         $$1.setBlock($$2, $$0.cycle(LEVEL), 2);
         return InteractionResult.SUCCESS_SERVER;
      } else {
         return InteractionResult.CONSUME;
      }
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return $$3.isHoldingItem(Items.LIGHT) ? Shapes.block() : Shapes.empty();
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState $$0) {
      return $$0.getFluidState().isEmpty();
   }

   @Override
   protected RenderShape getRenderShape(BlockState $$0) {
      return RenderShape.INVISIBLE;
   }

   @Override
   protected float getShadeBrightness(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return 1.0F;
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
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return setLightOnStack(super.getCloneItemStack($$0, $$1, $$2, $$3), $$2.getValue(LEVEL));
   }

   public static ItemStack setLightOnStack(ItemStack $$0, int $$1) {
      $$0.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(LEVEL, $$1));
      return $$0;
   }
}
