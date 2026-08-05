package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CopperGolemStatueBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<CopperGolemStatueBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(CopperGolemStatueBlock::getWeatheringState), propertiesCodec())
         .apply($$0, CopperGolemStatueBlock::new)
   );
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<CopperGolemStatueBlock.Pose> POSE = BlockStateProperties.COPPER_GOLEM_POSE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE = Block.column(10.0, 0.0, 14.0);
   private final WeatheringCopper.WeatherState weatheringState;

   @Override
   public MapCodec<? extends CopperGolemStatueBlock> codec() {
      return CODEC;
   }

   public CopperGolemStatueBlock(WeatheringCopper.WeatherState $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.weatheringState = $$0;
      this.registerDefaultState(
         this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(POSE, CopperGolemStatueBlock.Pose.STANDING).setValue(WATERLOGGED, false)
      );
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      super.createBlockStateDefinition($$0);
      $$0.add(FACING, POSE, WATERLOGGED);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      return this.defaultBlockState().setValue(FACING, $$0.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, $$1.getType() == Fluids.WATER);
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
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   public WeatheringCopper.WeatherState getWeatheringState() {
      return this.weatheringState;
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if ($$0.is(ItemTags.AXES)) {
         return InteractionResult.PASS;
      } else {
         this.updatePose($$2, $$1, $$3, $$4);
         return InteractionResult.SUCCESS;
      }
   }

   void updatePose(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, Player $$3) {
      $$0.playSound(null, $$2, SoundEvents.COPPER_GOLEM_BECOME_STATUE, SoundSource.BLOCKS);
      $$0.setBlock($$2, $$1.setValue(POSE, $$1.getValue(POSE).getNextPose()), 3);
      $$0.gameEvent($$3, GameEvent.BLOCK_CHANGE, $$2);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return $$1 == PathComputationType.WATER && $$0.getFluidState().is(FluidTags.WATER);
   }

   
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new CopperGolemStatueBlockEntity($$0, $$1);
   }

   @Override
   public boolean shouldChangedStateKeepBlockEntity(BlockState $$0) {
      return $$0.is(BlockTags.COPPER_GOLEM_STATUES);
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(POSE).ordinal() + 1;
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return $$0.getBlockEntity($$1) instanceof CopperGolemStatueBlockEntity $$4
         ? $$4.getItem(this.asItem().getDefaultInstance(), $$2.getValue(POSE))
         : super.getCloneItemStack($$0, $$1, $$2, $$3);
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      $$1.updateNeighbourForOutputSignal($$2, $$0.getBlock());
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

   public static enum Pose implements StringRepresentable {
      STANDING("standing"),
      SITTING("sitting"),
      RUNNING("running"),
      STAR("star");

      public static final IntFunction<CopperGolemStatueBlock.Pose> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.ZERO);
      public static final Codec<CopperGolemStatueBlock.Pose> CODEC = StringRepresentable.fromEnum(CopperGolemStatueBlock.Pose::values);
      private final String name;

      private Pose(final String $$0) {
         this.name = $$0;
      }

      public String getSerializedName() {
         return this.name;
      }

      public CopperGolemStatueBlock.Pose getNextPose() {
         return BY_ID.apply(this.ordinal() + 1);
      }
   }
}
