package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AmethystClusterBlock extends AmethystBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<AmethystClusterBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.FLOAT.fieldOf("height").forGetter($$0x -> $$0x.height), Codec.FLOAT.fieldOf("width").forGetter($$0x -> $$0x.width), propertiesCodec()
         )
         .apply($$0, AmethystClusterBlock::new)
   );
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
   private final float height;
   private final float width;
   private final Map<Direction, VoxelShape> shapes;

   @Override
   public MapCodec<AmethystClusterBlock> codec() {
      return CODEC;
   }

   public AmethystClusterBlock(float $$0, float $$1, BlockBehaviour.Properties $$2) {
      super($$2);
      this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false).setValue(FACING, Direction.UP));
      this.shapes = Shapes.rotateAll(Block.boxZ($$1, 16.0F - $$0, 16.0));
      this.height = $$0;
      this.width = $$1;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.get($$0.getValue(FACING));
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      Direction $$3 = $$0.getValue(FACING);
      BlockPos $$4 = $$2.relative($$3.getOpposite());
      return $$1.getBlockState($$4).isFaceSturdy($$1, $$4, $$3);
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

      return $$4 == ((Direction)$$0.getValue(FACING)).getOpposite() && !$$0.canSurvive($$1, $$3)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      net.minecraft.world.level.LevelAccessor $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      return this.defaultBlockState().setValue(WATERLOGGED, $$1.getFluidState($$2).getType() == Fluids.WATER).setValue(FACING, $$0.getClickedFace());
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
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(WATERLOGGED, FACING);
   }
}
