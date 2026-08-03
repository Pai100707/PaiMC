package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DragonEggBlock extends FallingBlock {
   public static final MapCodec<DragonEggBlock> CODEC = simpleCodec(DragonEggBlock::new);
   private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);

   @Override
   public MapCodec<DragonEggBlock> codec() {
      return CODEC;
   }

   public DragonEggBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      this.teleport($$0, $$1, $$2);
      return InteractionResult.SUCCESS;
   }

   @Override
   protected void attack(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3) {
      this.teleport($$0, $$1, $$2);
   }

   private void teleport(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      WorldBorder $$3 = $$1.getWorldBorder();

      for (int $$4 = 0; $$4 < 1000; $$4++) {
         BlockPos $$5 = $$2.offset(
            $$1.random.nextInt(16) - $$1.random.nextInt(16), $$1.random.nextInt(8) - $$1.random.nextInt(8), $$1.random.nextInt(16) - $$1.random.nextInt(16)
         );
         if ($$1.getBlockState($$5).isAir() && $$3.isWithinBounds($$5) && !$$1.isOutsideBuildHeight($$5)) {
            if ($$1.isClientSide()) {
               for (int $$6 = 0; $$6 < 128; $$6++) {
                  double $$7 = $$1.random.nextDouble();
                  float $$8 = ($$1.random.nextFloat() - 0.5F) * 0.2F;
                  float $$9 = ($$1.random.nextFloat() - 0.5F) * 0.2F;
                  float $$10 = ($$1.random.nextFloat() - 0.5F) * 0.2F;
                  double $$11 = Mth.lerp($$7, $$5.getX(), $$2.getX()) + ($$1.random.nextDouble() - 0.5) + 0.5;
                  double $$12 = Mth.lerp($$7, $$5.getY(), $$2.getY()) + $$1.random.nextDouble() - 0.5;
                  double $$13 = Mth.lerp($$7, $$5.getZ(), $$2.getZ()) + ($$1.random.nextDouble() - 0.5) + 0.5;
                  $$1.addParticle(ParticleTypes.PORTAL, $$11, $$12, $$13, $$8, $$9, $$10);
               }
            } else {
               $$1.setBlock($$5, $$0, 2);
               $$1.removeBlock($$2, false);
            }

            return;
         }
      }
   }

   @Override
   protected int getDelayAfterPlace() {
      return 5;
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Override
   public int getDustColor(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return -16777216;
   }
}
