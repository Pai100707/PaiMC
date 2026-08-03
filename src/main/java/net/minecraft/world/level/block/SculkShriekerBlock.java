package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class SculkShriekerBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<SculkShriekerBlock> CODEC = simpleCodec(SculkShriekerBlock::new);
   public static final BooleanProperty SHRIEKING = BlockStateProperties.SHRIEKING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty CAN_SUMMON = BlockStateProperties.CAN_SUMMON;
   private static final VoxelShape SHAPE_COLLISION = Block.column(16.0, 0.0, 8.0);
   public static final double TOP_Y = SHAPE_COLLISION.max(Axis.Y);

   @Override
   public MapCodec<SculkShriekerBlock> codec() {
      return CODEC;
   }

   public SculkShriekerBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(SHRIEKING, false).setValue(WATERLOGGED, false).setValue(CAN_SUMMON, false));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(SHRIEKING);
      $$0.add(WATERLOGGED);
      $$0.add(CAN_SUMMON);
   }

   @Override
   public void stepOn(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Entity $$3) {
      if ($$0 instanceof ServerLevel $$4) {
         ServerPlayer $$5 = SculkShriekerBlockEntity.tryGetPlayer($$3);
         if ($$5 != null) {
            $$4.getBlockEntity($$1, BlockEntityType.SCULK_SHRIEKER).ifPresent($$2x -> $$2x.tryShriek($$4, $$5));
         }
      }

      super.stepOn($$0, $$1, $$2, $$3);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(SHRIEKING)) {
         $$1.setBlock($$2, $$0.setValue(SHRIEKING, false), 3);
         $$1.getBlockEntity($$2, BlockEntityType.SCULK_SHRIEKER).ifPresent($$1x -> $$1x.tryRespond($$1));
      }
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE_COLLISION;
   }

   @Override
   protected VoxelShape getOcclusionShape(BlockState $$0) {
      return SHAPE_COLLISION;
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState $$0) {
      return true;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new SculkShriekerBlockEntity($$0, $$1);
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

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(WATERLOGGED, $$0.getLevel().getFluidState($$0.getClickedPos()).getType() == Fluids.WATER);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected void spawnAfterBreak(BlockState $$0, ServerLevel $$1, BlockPos $$2, ItemStack $$3, boolean $$4) {
      super.spawnAfterBreak($$0, $$1, $$2, $$3, $$4);
      if ($$4) {
         this.tryDropExperience($$1, $$2, $$3, ConstantInt.of(5));
      }
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return !$$0.isClientSide()
         ? BaseEntityBlock.createTickerHelper(
            $$2, BlockEntityType.SCULK_SHRIEKER, ($$0x, $$1x, $$2x, $$3) -> VibrationSystem.Ticker.tick($$0x, $$3.getVibrationData(), $$3.getVibrationUser())
         )
         : null;
   }
}
