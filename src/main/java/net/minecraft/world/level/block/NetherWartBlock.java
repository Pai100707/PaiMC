package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherWartBlock extends VegetationBlock {
   public static final MapCodec<NetherWartBlock> CODEC = simpleCodec(NetherWartBlock::new);
   public static final int MAX_AGE = 3;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
   private static final VoxelShape[] SHAPES = Block.boxes(3, $$0 -> Block.column(16.0, 0.0, 5 + $$0 * 3));

   @Override
   public MapCodec<NetherWartBlock> codec() {
      return CODEC;
   }

   protected NetherWartBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES[$$0.getValue(AGE)];
   }

   @Override
   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.is(Blocks.SOUL_SAND);
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return $$0.getValue(AGE) < 3;
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      int $$4 = $$0.getValue(AGE);
      if ($$4 < 3 && $$3.nextInt(10) == 0) {
         $$0 = $$0.setValue(AGE, $$4 + 1);
         $$1.setBlock($$2, $$0, 2);
      }
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return new ItemStack(Items.NETHER_WART);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AGE);
   }
}
