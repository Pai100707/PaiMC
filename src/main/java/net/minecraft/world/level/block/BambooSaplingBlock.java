package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BambooSaplingBlock extends Block implements BonemealableBlock {
   public static final MapCodec<BambooSaplingBlock> CODEC = simpleCodec(BambooSaplingBlock::new);
   private static final VoxelShape SHAPE = Block.column(8.0, 0.0, 12.0);

   @Override
   public MapCodec<BambooSaplingBlock> codec() {
      return CODEC;
   }

   public BambooSaplingBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE.move($$0.getOffset($$2));
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$3.nextInt(3) == 0 && $$1.isEmptyBlock($$2.above()) && $$1.getRawBrightness($$2.above(), 0) >= 9) {
         this.growBamboo($$1, $$2);
      }
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return $$1.getBlockState($$2.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
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
         return Blocks.AIR.defaultBlockState();
      } else {
         return $$4 == Direction.UP && $$6.is(Blocks.BAMBOO) ? Blocks.BAMBOO.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      }
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return new ItemStack(Items.BAMBOO);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return $$0.getBlockState($$1.above()).isAir();
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      this.growBamboo($$0, $$2);
   }

   protected void growBamboo(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      $$0.setBlock($$1.above(), Blocks.BAMBOO.defaultBlockState().setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL), 3);
   }
}
