package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class SnowLayerBlock extends Block {
   public static final MapCodec<SnowLayerBlock> CODEC = simpleCodec(SnowLayerBlock::new);
   public static final int MAX_HEIGHT = 8;
   public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
   private static final VoxelShape[] SHAPES = Block.boxes(8, $$0 -> Block.column(16.0, 0.0, $$0 * 2));
   public static final int HEIGHT_IMPASSABLE = 5;

   @Override
   public MapCodec<SnowLayerBlock> codec() {
      return CODEC;
   }

   protected SnowLayerBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return $$1 == PathComputationType.LAND ? $$0.getValue(LAYERS) < 5 : false;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES[$$0.getValue(LAYERS)];
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES[$$0.getValue(LAYERS) - 1];
   }

   @Override
   protected VoxelShape getBlockSupportShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return SHAPES[$$0.getValue(LAYERS)];
   }

   @Override
   protected VoxelShape getVisualShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES[$$0.getValue(LAYERS)];
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState $$0) {
      return true;
   }

   @Override
   protected float getShadeBrightness(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.getValue(LAYERS) == 8 ? 0.2F : 1.0F;
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockState $$3 = $$1.getBlockState($$2.below());
      if ($$3.is(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
         return false;
      } else {
         return $$3.is(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON)
            ? true
            : Block.isFaceFull($$3.getCollisionShape($$1, $$2.below()), Direction.UP) || $$3.is(this) && $$3.getValue(LAYERS) == 8;
      }
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
      return !$$0.canSurvive($$1, $$3) ? Blocks.AIR.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$1.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, $$2) > 11) {
         dropResources($$0, $$1, $$2);
         $$1.removeBlock($$2, false);
      }
   }

   @Override
   protected boolean canBeReplaced(BlockState $$0, BlockPlaceContext $$1) {
      int $$2 = $$0.getValue(LAYERS);
      if (!$$1.getItemInHand().is(this.asItem()) || $$2 >= 8) {
         return $$2 == 1;
      } else {
         return $$1.replacingClickedOnBlock() ? $$1.getClickedFace() == Direction.UP : true;
      }
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = $$0.getLevel().getBlockState($$0.getClickedPos());
      if ($$1.is(this)) {
         int $$2 = $$1.getValue(LAYERS);
         return $$1.setValue(LAYERS, Math.min(8, $$2 + 1));
      } else {
         return super.getStateForPlacement($$0);
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(LAYERS);
   }
}
