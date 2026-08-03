package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SugarCaneBlock extends Block {
   public static final MapCodec<SugarCaneBlock> CODEC = simpleCodec(SugarCaneBlock::new);
   public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
   private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 16.0);

   @Override
   public MapCodec<SugarCaneBlock> codec() {
      return CODEC;
   }

   protected SugarCaneBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!$$0.canSurvive($$1, $$2)) {
         $$1.destroyBlock($$2, true);
      }
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$1.isEmptyBlock($$2.above())) {
         int $$4 = 1;

         while ($$1.getBlockState($$2.below($$4)).is(this)) {
            $$4++;
         }

         if ($$4 < 3) {
            int $$5 = $$0.getValue(AGE);
            if ($$5 == 15) {
               $$1.setBlockAndUpdate($$2.above(), this.defaultBlockState());
               $$1.setBlock($$2, $$0.setValue(AGE, 0), 260);
            } else {
               $$1.setBlock($$2, $$0.setValue(AGE, $$5 + 1), 260);
            }
         }
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
      if (!$$0.canSurvive($$1, $$3)) {
         $$2.scheduleTick($$3, this, 1);
      }

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockState $$3 = $$1.getBlockState($$2.below());
      if ($$3.is(this)) {
         return true;
      } else {
         if ($$3.is(BlockTags.DIRT) || $$3.is(BlockTags.SAND)) {
            BlockPos $$4 = $$2.below();

            for (Direction $$5 : Plane.HORIZONTAL) {
               BlockState $$6 = $$1.getBlockState($$4.relative($$5));
               FluidState $$7 = $$1.getFluidState($$4.relative($$5));
               if ($$7.is(FluidTags.WATER) || $$6.is(Blocks.FROSTED_ICE)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AGE);
   }
}
