package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class CoralFeature extends Feature<NoneFeatureConfiguration> {
   public CoralFeature(Codec<NoneFeatureConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> $$0) {
      RandomSource $$1 = $$0.random();
      net.minecraft.world.level.WorldGenLevel $$2 = $$0.level();
      BlockPos $$3 = $$0.origin();
      Optional<Block> $$4 = BuiltInRegistries.BLOCK.getRandomElementOf(BlockTags.CORAL_BLOCKS, $$1).map(Holder::value);
      return $$4.isEmpty() ? false : this.placeFeature($$2, $$1, $$3, $$4.get().defaultBlockState());
   }

   protected abstract boolean placeFeature(net.minecraft.world.level.LevelAccessor var1, RandomSource var2, BlockPos var3, BlockState var4);

   protected boolean placeCoralBlock(net.minecraft.world.level.LevelAccessor $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      BlockPos $$4 = $$2.above();
      BlockState $$5 = $$0.getBlockState($$2);
      if (($$5.is(Blocks.WATER) || $$5.is(BlockTags.CORALS)) && $$0.getBlockState($$4).is(Blocks.WATER)) {
         $$0.setBlock($$2, $$3, 3);
         if ($$1.nextFloat() < 0.25F) {
            BuiltInRegistries.BLOCK
               .getRandomElementOf(BlockTags.CORALS, $$1)
               .<Block>map(Holder::value)
               .ifPresent($$2x -> $$0.setBlock($$4, $$2x.defaultBlockState(), 2));
         } else if ($$1.nextFloat() < 0.05F) {
            $$0.setBlock($$4, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, $$1.nextInt(4) + 1), 2);
         }

         for (Direction $$6 : Plane.HORIZONTAL) {
            if ($$1.nextFloat() < 0.2F) {
               BlockPos $$7 = $$2.relative($$6);
               if ($$0.getBlockState($$7).is(Blocks.WATER)) {
                  BuiltInRegistries.BLOCK.getRandomElementOf(BlockTags.WALL_CORALS, $$1).<Block>map(Holder::value).ifPresent($$3x -> {
                     BlockState $$4x = $$3x.defaultBlockState();
                     if ($$4x.hasProperty(BaseCoralWallFanBlock.FACING)) {
                        $$4x = $$4x.setValue(BaseCoralWallFanBlock.FACING, $$6);
                     }

                     $$0.setBlock($$7, $$4x, 2);
                  });
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
