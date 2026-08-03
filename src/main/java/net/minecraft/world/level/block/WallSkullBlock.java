package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallSkullBlock extends AbstractSkullBlock {
   public static final MapCodec<WallSkullBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(SkullBlock.Type.CODEC.fieldOf("kind").forGetter(AbstractSkullBlock::getType), propertiesCodec()).apply($$0, WallSkullBlock::new)
   );
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.boxZ(8.0, 8.0, 16.0));

   @Override
   public MapCodec<? extends WallSkullBlock> codec() {
      return CODEC;
   }

   protected WallSkullBlock(SkullBlock.Type $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
      this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get($$0.getValue(FACING));
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = super.getStateForPlacement($$0);
      net.minecraft.world.level.BlockGetter $$2 = $$0.getLevel();
      BlockPos $$3 = $$0.getClickedPos();
      Direction[] $$4 = $$0.getNearestLookingDirections();

      for (Direction $$5 : $$4) {
         if ($$5.getAxis().isHorizontal()) {
            Direction $$6 = $$5.getOpposite();
            $$1 = $$1.setValue(FACING, $$6);
            if (!$$2.getBlockState($$3.relative($$5)).canBeReplaced($$0)) {
               return $$1;
            }
         }
      }

      return null;
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
      super.createBlockStateDefinition($$0);
      $$0.add(FACING);
   }
}
