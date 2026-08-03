package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PotatoBlock extends CropBlock {
   public static final MapCodec<PotatoBlock> CODEC = simpleCodec(PotatoBlock::new);
   private static final VoxelShape[] SHAPES = Block.boxes(7, $$0 -> Block.column(16.0, 0.0, 2 + $$0));

   @Override
   public MapCodec<PotatoBlock> codec() {
      return CODEC;
   }

   public PotatoBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected net.minecraft.world.level.ItemLike getBaseSeedId() {
      return Items.POTATO;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES[this.getAge($$0)];
   }
}
