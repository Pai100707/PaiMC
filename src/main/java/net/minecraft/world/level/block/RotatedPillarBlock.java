package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class RotatedPillarBlock extends Block {
   public static final MapCodec<RotatedPillarBlock> CODEC = simpleCodec(RotatedPillarBlock::new);
   public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;

   @Override
   public MapCodec<? extends RotatedPillarBlock> codec() {
      return CODEC;
   }

   public RotatedPillarBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Axis.Y));
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return rotatePillar($$0, $$1);
   }

   public static BlockState rotatePillar(BlockState $$0, Rotation $$1) {
      switch ($$1) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            switch ((Axis)$$0.getValue(AXIS)) {
               case X:
                  return $$0.setValue(AXIS, Axis.Z);
               case Z:
                  return $$0.setValue(AXIS, Axis.X);
               default:
                  return $$0;
            }
         default:
            return $$0;
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AXIS);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(AXIS, $$0.getClickedFace().getAxis());
   }
}
