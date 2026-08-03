package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherSproutsBlock extends VegetationBlock {
   public static final MapCodec<NetherSproutsBlock> CODEC = simpleCodec(NetherSproutsBlock::new);
   private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 3.0);

   @Override
   public MapCodec<NetherSproutsBlock> codec() {
      return CODEC;
   }

   public NetherSproutsBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.is(BlockTags.NYLIUM) || $$0.is(Blocks.SOUL_SOIL) || super.mayPlaceOn($$0, $$1, $$2);
   }
}
