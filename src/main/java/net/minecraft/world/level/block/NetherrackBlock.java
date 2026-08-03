package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NetherrackBlock extends Block implements BonemealableBlock {
   public static final MapCodec<NetherrackBlock> CODEC = simpleCodec(NetherrackBlock::new);

   @Override
   public MapCodec<NetherrackBlock> codec() {
      return CODEC;
   }

   public NetherrackBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      if (!$$0.getBlockState($$1.above()).propagatesSkylightDown()) {
         return false;
      } else {
         for (BlockPos $$3 : BlockPos.betweenClosed($$1.offset(-1, -1, -1), $$1.offset(1, 1, 1))) {
            if ($$0.getBlockState($$3).is(BlockTags.NYLIUM)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      boolean $$4 = false;
      boolean $$5 = false;

      for (BlockPos $$6 : BlockPos.betweenClosed($$2.offset(-1, -1, -1), $$2.offset(1, 1, 1))) {
         BlockState $$7 = $$0.getBlockState($$6);
         if ($$7.is(Blocks.WARPED_NYLIUM)) {
            $$5 = true;
         }

         if ($$7.is(Blocks.CRIMSON_NYLIUM)) {
            $$4 = true;
         }

         if ($$5 && $$4) {
            break;
         }
      }

      if ($$5 && $$4) {
         $$0.setBlock($$2, $$1.nextBoolean() ? Blocks.WARPED_NYLIUM.defaultBlockState() : Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
      } else if ($$5) {
         $$0.setBlock($$2, Blocks.WARPED_NYLIUM.defaultBlockState(), 3);
      } else if ($$4) {
         $$0.setBlock($$2, Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
      }
   }

   @Override
   public BonemealableBlock.Type getType() {
      return BonemealableBlock.Type.NEIGHBOR_SPREADER;
   }
}
