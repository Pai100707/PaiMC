package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class RailBlock extends BaseRailBlock {
   public static final MapCodec<RailBlock> CODEC = simpleCodec(RailBlock::new);
   public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

   @Override
   public MapCodec<RailBlock> codec() {
      return CODEC;
   }

   protected RailBlock(BlockBehaviour.Properties $$0) {
      super(false, $$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, false));
   }

   @Override
   protected void updateState(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3) {
      if ($$3.defaultBlockState().isSignalSource() && new RailState($$1, $$2, $$0).countPotentialConnections() == 3) {
         this.updateDir($$1, $$2, $$0, false);
      }
   }

   @Override
   public Property<RailShape> getShapeProperty() {
      return SHAPE;
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      RailShape $$2 = $$0.getValue(SHAPE);
      RailShape $$3 = this.rotate($$2, $$1);
      return $$0.setValue(SHAPE, $$3);
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      RailShape $$2 = $$0.getValue(SHAPE);
      RailShape $$3 = this.mirror($$2, $$1);
      return $$0.setValue(SHAPE, $$3);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(SHAPE, WATERLOGGED);
   }
}
