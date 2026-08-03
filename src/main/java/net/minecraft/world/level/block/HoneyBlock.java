package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HoneyBlock extends HalfTransparentBlock {
   public static final MapCodec<HoneyBlock> CODEC = simpleCodec(HoneyBlock::new);
   private static final double SLIDE_STARTS_WHEN_VERTICAL_SPEED_IS_AT_LEAST = 0.13;
   private static final double MIN_FALL_SPEED_TO_BE_CONSIDERED_SLIDING = 0.08;
   private static final double THROTTLE_SLIDE_SPEED_TO = 0.05;
   private static final int SLIDE_ADVANCEMENT_CHECK_INTERVAL = 20;
   private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 15.0);

   @Override
   public MapCodec<HoneyBlock> codec() {
      return CODEC;
   }

   public HoneyBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   private static boolean doesEntityDoHoneyBlockSlideEffects(Entity $$0) {
      return $$0 instanceof LivingEntity || $$0 instanceof AbstractMinecart || $$0 instanceof PrimedTnt || $$0 instanceof AbstractBoat;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   public void fallOn(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, Entity $$3, double $$4) {
      $$3.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
      if (!$$0.isClientSide()) {
         $$0.broadcastEntityEvent($$3, (byte)54);
      }

      if ($$3.causeFallDamage($$4, 0.2F, $$0.damageSources().fall())) {
         $$3.playSound(this.soundType.getFallSound(), this.soundType.getVolume() * 0.5F, this.soundType.getPitch() * 0.75F);
      }
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if (this.isSlidingDown($$2, $$3)) {
         this.maybeDoSlideAchievement($$3, $$2);
         this.doSlideMovement($$3);
         this.maybeDoSlideEffects($$1, $$3);
      }

      super.entityInside($$0, $$1, $$2, $$3, $$4, $$5);
   }

   private static double getOldDeltaY(double $$0) {
      return $$0 / 0.98F + 0.08;
   }

   private static double getNewDeltaY(double $$0) {
      return ($$0 - 0.08) * 0.98F;
   }

   private boolean isSlidingDown(BlockPos $$0, Entity $$1) {
      if ($$1.onGround()) {
         return false;
      } else if ($$1.getY() > $$0.getY() + 0.9375 - 1.0E-7) {
         return false;
      } else if (getOldDeltaY($$1.getDeltaMovement().y) >= -0.08) {
         return false;
      } else {
         double $$2 = Math.abs($$0.getX() + 0.5 - $$1.getX());
         double $$3 = Math.abs($$0.getZ() + 0.5 - $$1.getZ());
         double $$4 = 0.4375 + $$1.getBbWidth() / 2.0F;
         return $$2 + 1.0E-7 > $$4 || $$3 + 1.0E-7 > $$4;
      }
   }

   private void maybeDoSlideAchievement(Entity $$0, BlockPos $$1) {
      if ($$0 instanceof ServerPlayer && $$0.level().getGameTime() % 20L == 0L) {
         CriteriaTriggers.HONEY_BLOCK_SLIDE.trigger((ServerPlayer)$$0, $$0.level().getBlockState($$1));
      }
   }

   private void doSlideMovement(Entity $$0) {
      Vec3 $$1 = $$0.getDeltaMovement();
      if (getOldDeltaY($$0.getDeltaMovement().y) < -0.13) {
         double $$2 = -0.05 / getOldDeltaY($$0.getDeltaMovement().y);
         $$0.setDeltaMovement(new Vec3($$1.x * $$2, getNewDeltaY(-0.05), $$1.z * $$2));
      } else {
         $$0.setDeltaMovement(new Vec3($$1.x, getNewDeltaY(-0.05), $$1.z));
      }

      $$0.resetFallDistance();
   }

   private void maybeDoSlideEffects(net.minecraft.world.level.Level $$0, Entity $$1) {
      if (doesEntityDoHoneyBlockSlideEffects($$1)) {
         if ($$0.random.nextInt(5) == 0) {
            $$1.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
         }

         if (!$$0.isClientSide() && $$0.random.nextInt(5) == 0) {
            $$0.broadcastEntityEvent($$1, (byte)53);
         }
      }
   }

   public static void showSlideParticles(Entity $$0) {
      showParticles($$0, 5);
   }

   public static void showJumpParticles(Entity $$0) {
      showParticles($$0, 10);
   }

   private static void showParticles(Entity $$0, int $$1) {
      if ($$0.level().isClientSide()) {
         BlockState $$2 = Blocks.HONEY_BLOCK.defaultBlockState();

         for (int $$3 = 0; $$3 < $$1; $$3++) {
            $$0.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, $$2), $$0.getX(), $$0.getY(), $$0.getZ(), 0.0, 0.0, 0.0);
         }
      }
   }
}
