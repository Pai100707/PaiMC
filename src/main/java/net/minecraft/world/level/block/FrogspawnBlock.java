package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FrogspawnBlock extends Block {
   public static final MapCodec<FrogspawnBlock> CODEC = simpleCodec(FrogspawnBlock::new);
   private static final int MIN_TADPOLES_SPAWN = 2;
   private static final int MAX_TADPOLES_SPAWN = 5;
   private static final int DEFAULT_MIN_HATCH_TICK_DELAY = 3600;
   private static final int DEFAULT_MAX_HATCH_TICK_DELAY = 12000;
   private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 1.5);
   private static int minHatchTickDelay = 3600;
   private static int maxHatchTickDelay = 12000;

   @Override
   public MapCodec<FrogspawnBlock> codec() {
      return CODEC;
   }

   public FrogspawnBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return mayPlaceOn($$1, $$2.below());
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      $$1.scheduleTick($$2, this, getFrogspawnHatchDelay($$1.getRandom()));
   }

   private static int getFrogspawnHatchDelay(RandomSource $$0) {
      return $$0.nextInt(minHatchTickDelay, maxHatchTickDelay);
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
      return !this.canSurvive($$0, $$1, $$3) ? Blocks.AIR.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!this.canSurvive($$0, $$1, $$2)) {
         this.destroyBlock($$1, $$2);
      } else {
         this.hatchFrogspawn($$1, $$2, $$3);
      }
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if ($$3.getType().equals(EntityType.FALLING_BLOCK)) {
         this.destroyBlock($$1, $$2);
      }
   }

   private static boolean mayPlaceOn(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      FluidState $$2 = $$0.getFluidState($$1);
      FluidState $$3 = $$0.getFluidState($$1.above());
      return $$2.getType() == Fluids.WATER && $$3.getType() == Fluids.EMPTY;
   }

   private void hatchFrogspawn(ServerLevel $$0, BlockPos $$1, RandomSource $$2) {
      this.destroyBlock($$0, $$1);
      $$0.playSound(null, $$1, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
      this.spawnTadpoles($$0, $$1, $$2);
   }

   private void destroyBlock(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      $$0.destroyBlock($$1, false);
   }

   private void spawnTadpoles(ServerLevel $$0, BlockPos $$1, RandomSource $$2) {
      int $$3 = $$2.nextInt(2, 6);

      for (int $$4 = 1; $$4 <= $$3; $$4++) {
         Tadpole $$5 = (Tadpole)EntityType.TADPOLE.create($$0, EntitySpawnReason.BREEDING);
         if ($$5 != null) {
            double $$6 = $$1.getX() + this.getRandomTadpolePositionOffset($$2);
            double $$7 = $$1.getZ() + this.getRandomTadpolePositionOffset($$2);
            int $$8 = $$2.nextInt(1, 361);
            $$5.snapTo($$6, $$1.getY() - 0.5, $$7, $$8, 0.0F);
            $$5.setPersistenceRequired();
            $$0.addFreshEntity($$5);
         }
      }
   }

   private double getRandomTadpolePositionOffset(RandomSource $$0) {
      double $$1 = 0.2F;
      return Mth.clamp($$0.nextDouble(), 0.2F, 0.7999999970197678);
   }

   @VisibleForTesting
   public static void setHatchDelay(int $$0, int $$1) {
      minHatchTickDelay = $$0;
      maxHatchTickDelay = $$1;
   }

   @VisibleForTesting
   public static void setDefaultHatchDelay() {
      minHatchTickDelay = 3600;
      maxHatchTickDelay = 12000;
   }
}
