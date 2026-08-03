package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class StandingSignBlock extends SignBlock {
   public static final MapCodec<StandingSignBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(WoodType.CODEC.fieldOf("wood_type").forGetter(SignBlock::type), propertiesCodec()).apply($$0, StandingSignBlock::new)
   );
   public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

   @Override
   public MapCodec<StandingSignBlock> codec() {
      return CODEC;
   }

   public StandingSignBlock(WoodType $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1.sound($$0.soundType()));
      this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0).setValue(WATERLOGGED, false));
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return $$1.getBlockState($$2.below()).isSolid();
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      return this.defaultBlockState()
         .setValue(ROTATION, RotationSegment.convertToSegment($$0.getRotation() + 180.0F))
         .setValue(WATERLOGGED, $$1.getType() == Fluids.WATER);
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
      return $$4 == Direction.DOWN && !this.canSurvive($$0, $$1, $$3)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   public float getYRotationDegrees(BlockState $$0) {
      return RotationSegment.convertToDegrees($$0.getValue(ROTATION));
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(ROTATION, $$1.rotate($$0.getValue(ROTATION), 16));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.setValue(ROTATION, $$1.mirror($$0.getValue(ROTATION), 16));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(ROTATION, WATERLOGGED);
   }
}
