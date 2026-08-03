package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CactusBlock extends Block {
   public static final MapCodec<CactusBlock> CODEC = simpleCodec(CactusBlock::new);
   public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
   public static final int MAX_AGE = 15;
   private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);
   private static final VoxelShape SHAPE_COLLISION = Block.column(14.0, 0.0, 15.0);
   private static final int MAX_CACTUS_GROWING_HEIGHT = 3;
   private static final int ATTEMPT_GROW_CACTUS_FLOWER_AGE = 8;
   private static final double ATTEMPT_GROW_CACTUS_FLOWER_SMALL_CACTUS_CHANCE = 0.1;
   private static final double ATTEMPT_GROW_CACTUS_FLOWER_TALL_CACTUS_CHANCE = 0.25;

   @Override
   public MapCodec<CactusBlock> codec() {
      return CODEC;
   }

   protected CactusBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!$$0.canSurvive($$1, $$2)) {
         $$1.destroyBlock($$2, true);
      }
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      BlockPos $$4 = $$2.above();
      if ($$1.isEmptyBlock($$4)) {
         int $$5 = 1;
         int $$6 = $$0.getValue(AGE);

         while ($$1.getBlockState($$2.below($$5)).is(this)) {
            if (++$$5 == 3 && $$6 == 15) {
               return;
            }
         }

         if ($$6 == 8 && this.canSurvive(this.defaultBlockState(), $$1, $$2.above())) {
            double $$7 = $$5 >= 3 ? 0.25 : 0.1;
            if ($$3.nextDouble() <= $$7) {
               $$1.setBlockAndUpdate($$4, Blocks.CACTUS_FLOWER.defaultBlockState());
            }
         } else if ($$6 == 15 && $$5 < 3) {
            $$1.setBlockAndUpdate($$4, this.defaultBlockState());
            BlockState $$8 = $$0.setValue(AGE, 0);
            $$1.setBlock($$2, $$8, 260);
            $$1.neighborChanged($$8, $$4, this, null, false);
         }

         if ($$6 < 15) {
            $$1.setBlock($$2, $$0.setValue(AGE, $$6 + 1), 260);
         }
      }
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE_COLLISION;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
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
      if (!$$0.canSurvive($$1, $$3)) {
         $$2.scheduleTick($$3, this, 1);
      }

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      for (Direction $$3 : Plane.HORIZONTAL) {
         BlockState $$4 = $$1.getBlockState($$2.relative($$3));
         if ($$4.isSolid() || $$1.getFluidState($$2.relative($$3)).is(FluidTags.LAVA)) {
            return false;
         }
      }

      BlockState $$5 = $$1.getBlockState($$2.below());
      return ($$5.is(Blocks.CACTUS) || $$5.is(BlockTags.SAND)) && !$$1.getBlockState($$2.above()).liquid();
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      $$3.hurt($$1.damageSources().cactus(), 1.0F);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AGE);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }
}
