package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.TraversalNodeStatus;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class SpongeBlock extends Block {
   public static final MapCodec<SpongeBlock> CODEC = simpleCodec(SpongeBlock::new);
   public static final int MAX_DEPTH = 6;
   public static final int MAX_COUNT = 64;
   private static final Direction[] ALL_DIRECTIONS = Direction.values();

   @Override
   public MapCodec<SpongeBlock> codec() {
      return CODEC;
   }

   protected SpongeBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (!$$3.is($$0.getBlock())) {
         this.tryAbsorbWater($$1, $$2);
      }
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, @Nullable Orientation $$4, boolean $$5) {
      this.tryAbsorbWater($$1, $$2);
      super.neighborChanged($$0, $$1, $$2, $$3, $$4, $$5);
   }

   protected void tryAbsorbWater(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      if (this.removeWaterBreadthFirstSearch($$0, $$1)) {
         $$0.setBlock($$1, Blocks.WET_SPONGE.defaultBlockState(), 2);
         $$0.playSound(null, $$1, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   private boolean removeWaterBreadthFirstSearch(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      return BlockPos.breadthFirstTraversal($$1, 6, 65, ($$0x, $$1x) -> {
         for (Direction $$2 : ALL_DIRECTIONS) {
            $$1x.accept($$0x.relative($$2));
         }
      }, $$2 -> {
         if ($$2.equals($$1)) {
            return TraversalNodeStatus.ACCEPT;
         } else {
            BlockState $$3 = $$0.getBlockState($$2);
            FluidState $$4 = $$0.getFluidState($$2);
            if (!$$4.is(FluidTags.WATER)) {
               return TraversalNodeStatus.SKIP;
            } else if ($$3.getBlock() instanceof BucketPickup $$6 && !$$6.pickupBlock(null, $$0, $$2, $$3).isEmpty()) {
               return TraversalNodeStatus.ACCEPT;
            } else {
               if ($$3.getBlock() instanceof LiquidBlock) {
                  $$0.setBlock($$2, Blocks.AIR.defaultBlockState(), 3);
               } else {
                  if (!$$3.is(Blocks.KELP) && !$$3.is(Blocks.KELP_PLANT) && !$$3.is(Blocks.SEAGRASS) && !$$3.is(Blocks.TALL_SEAGRASS)) {
                     return TraversalNodeStatus.SKIP;
                  }

                  BlockEntity $$7 = $$3.hasBlockEntity() ? $$0.getBlockEntity($$2) : null;
                  dropResources($$3, $$0, $$2, $$7);
                  $$0.setBlock($$2, Blocks.AIR.defaultBlockState(), 3);
               }

               return TraversalNodeStatus.ACCEPT;
            }
         }
      }) > 1;
   }
}
