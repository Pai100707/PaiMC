package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeetrootBlock extends CropBlock {
   public static final MapCodec<BeetrootBlock> CODEC = simpleCodec(BeetrootBlock::new);
   public static final int MAX_AGE = 3;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
   private static final VoxelShape[] SHAPES = Block.boxes(3, $$0 -> Block.column(16.0, 0.0, 2 + $$0 * 2));

   @Override
   public MapCodec<BeetrootBlock> codec() {
      return CODEC;
   }

   public BeetrootBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected IntegerProperty getAgeProperty() {
      return AGE;
   }

   @Override
   public int getMaxAge() {
      return 3;
   }

   @Override
   protected net.minecraft.world.level.ItemLike getBaseSeedId() {
      return Items.BEETROOT_SEEDS;
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$3.nextInt(3) != 0) {
         super.randomTick($$0, $$1, $$2, $$3);
      }
   }

   @Override
   protected int getBonemealAgeIncrease(net.minecraft.world.level.Level $$0) {
      return super.getBonemealAgeIncrease($$0) / 3;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AGE);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES[this.getAge($$0)];
   }
}
