package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StructureVoidBlock extends Block {
   public static final MapCodec<StructureVoidBlock> CODEC = simpleCodec(StructureVoidBlock::new);
   private static final VoxelShape SHAPE = Block.cube(6.0);

   @Override
   public MapCodec<StructureVoidBlock> codec() {
      return CODEC;
   }

   protected StructureVoidBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected RenderShape getRenderShape(BlockState $$0) {
      return RenderShape.INVISIBLE;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected float getShadeBrightness(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return 1.0F;
   }
}
