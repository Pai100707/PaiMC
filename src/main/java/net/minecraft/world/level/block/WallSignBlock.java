package net.minecraft.world.level.block;

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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallSignBlock extends SignBlock {
   public static final MapCodec<WallSignBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(WoodType.CODEC.fieldOf("wood_type").forGetter(SignBlock::type), propertiesCodec()).apply($$0, WallSignBlock::new)
   );
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.boxZ(16.0, 4.5, 12.5, 14.0, 16.0));

   @Override
   public MapCodec<WallSignBlock> codec() {
      return CODEC;
   }

   public WallSignBlock(WoodType $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1.sound($$0.soundType()));
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get($$0.getValue(FACING));
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return $$1.getBlockState($$2.relative(((Direction)$$0.getValue(FACING)).getOpposite())).isSolid();
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = this.defaultBlockState();
      FluidState $$2 = $$0.getLevel().getFluidState($$0.getClickedPos());
      net.minecraft.world.level.LevelReader $$3 = $$0.getLevel();
      BlockPos $$4 = $$0.getClickedPos();
      Direction[] $$5 = $$0.getNearestLookingDirections();

      for (Direction $$6 : $$5) {
         if ($$6.getAxis().isHorizontal()) {
            Direction $$7 = $$6.getOpposite();
            $$1 = $$1.setValue(FACING, $$7);
            if ($$1.canSurvive($$3, $$4)) {
               return $$1.setValue(WATERLOGGED, $$2.getType() == Fluids.WATER);
            }
         }
      }

      return null;
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
      return $$4.getOpposite() == $$0.getValue(FACING) && !$$0.canSurvive($$1, $$3)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   public float getYRotationDegrees(BlockState $$0) {
      return ((Direction)$$0.getValue(FACING)).toYRot();
   }

   @Override
   public Vec3 getSignHitboxCenterPosition(BlockState $$0) {
      return SHAPES.get($$0.getValue(FACING)).bounds().getCenter();
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
}
