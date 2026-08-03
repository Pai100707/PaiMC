package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public abstract class ThrowableProjectile extends Projectile {
   private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;

   protected ThrowableProjectile(net.minecraft.world.entity.EntityType<? extends ThrowableProjectile> $$0, Level $$1) {
      super($$0, $$1);
   }

   protected ThrowableProjectile(net.minecraft.world.entity.EntityType<? extends ThrowableProjectile> $$0, double $$1, double $$2, double $$3, Level $$4) {
      this($$0, $$4);
      this.setPos($$1, $$2, $$3);
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double $$0) {
      if (this.tickCount < 2 && $$0 < 12.25) {
         return false;
      } else {
         double $$1 = this.getBoundingBox().getSize() * 4.0;
         if (Double.isNaN($$1)) {
            $$1 = 4.0;
         }

         $$1 *= 64.0;
         return $$0 < $$1 * $$1;
      }
   }

   @Override
   public boolean canUsePortal(boolean $$0) {
      return true;
   }

   @Override
   public void tick() {
      this.handleFirstTickBubbleColumn();
      this.applyGravity();
      this.applyInertia();
      HitResult $$0 = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      Vec3 $$1;
      if ($$0.getType() != Type.MISS) {
         $$1 = $$0.getLocation();
      } else {
         $$1 = this.position().add(this.getDeltaMovement());
      }

      this.setPos($$1);
      this.updateRotation();
      this.applyEffectsFromBlocks();
      super.tick();
      if ($$0.getType() != Type.MISS && this.isAlive()) {
         this.hitTargetOrDeflectSelf($$0);
      }
   }

   private void applyInertia() {
      Vec3 $$0 = this.getDeltaMovement();
      Vec3 $$1 = this.position();
      float $$4;
      if (this.isInWater()) {
         for (int $$2 = 0; $$2 < 4; $$2++) {
            float $$3 = 0.25F;
            this.level().addParticle(ParticleTypes.BUBBLE, $$1.x - $$0.x * 0.25, $$1.y - $$0.y * 0.25, $$1.z - $$0.z * 0.25, $$0.x, $$0.y, $$0.z);
         }

         $$4 = 0.8F;
      } else {
         $$4 = 0.99F;
      }

      this.setDeltaMovement($$0.scale($$4));
   }

   private void handleFirstTickBubbleColumn() {
      if (this.firstTick) {
         for (BlockPos $$0 : BlockPos.betweenClosed(this.getBoundingBox())) {
            BlockState $$1 = this.level().getBlockState($$0);
            if ($$1.is(Blocks.BUBBLE_COLUMN)) {
               $$1.entityInside(this.level(), $$0, this, net.minecraft.world.entity.InsideBlockEffectApplier.NOOP, true);
            }
         }
      }
   }

   @Override
   protected double getDefaultGravity() {
      return 0.03;
   }
}
