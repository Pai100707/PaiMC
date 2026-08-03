package net.minecraft.world.entity.projectile.hurtingprojectile;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

public abstract class AbstractHurtingProjectile extends Projectile {
   public static final double INITAL_ACCELERATION_POWER = 0.1;
   public static final double DEFLECTION_SCALE = 0.5;
   public double accelerationPower = 0.1;

   protected AbstractHurtingProjectile(net.minecraft.world.entity.EntityType<? extends AbstractHurtingProjectile> $$0, Level $$1) {
      super($$0, $$1);
   }

   protected AbstractHurtingProjectile(
      net.minecraft.world.entity.EntityType<? extends AbstractHurtingProjectile> $$0, double $$1, double $$2, double $$3, Level $$4
   ) {
      this($$0, $$4);
      this.setPos($$1, $$2, $$3);
   }

   public AbstractHurtingProjectile(
      net.minecraft.world.entity.EntityType<? extends AbstractHurtingProjectile> $$0, double $$1, double $$2, double $$3, Vec3 $$4, Level $$5
   ) {
      this($$0, $$5);
      this.snapTo($$1, $$2, $$3, this.getYRot(), this.getXRot());
      this.reapplyPosition();
      this.assignDirectionalMovement($$4, this.accelerationPower);
   }

   public AbstractHurtingProjectile(
      net.minecraft.world.entity.EntityType<? extends AbstractHurtingProjectile> $$0, net.minecraft.world.entity.LivingEntity $$1, Vec3 $$2, Level $$3
   ) {
      this($$0, $$1.getX(), $$1.getY(), $$1.getZ(), $$2, $$3);
      this.setOwner($$1);
      this.setRot($$1.getYRot(), $$1.getXRot());
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double $$0) {
      double $$1 = this.getBoundingBox().getSize() * 4.0;
      if (Double.isNaN($$1)) {
         $$1 = 4.0;
      }

      $$1 *= 64.0;
      return $$0 < $$1 * $$1;
   }

   protected Block getClipType() {
      return Block.COLLIDER;
   }

   @Override
   public void tick() {
      net.minecraft.world.entity.Entity $$0 = this.getOwner();
      this.applyInertia();
      if (this.level().isClientSide() || ($$0 == null || !$$0.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
         HitResult $$1 = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity, this.getClipType());
         Vec3 $$2;
         if ($$1.getType() != Type.MISS) {
            $$2 = $$1.getLocation();
         } else {
            $$2 = this.position().add(this.getDeltaMovement());
         }

         ProjectileUtil.rotateTowardsMovement(this, 0.2F);
         this.setPos($$2);
         this.applyEffectsFromBlocks();
         super.tick();
         if (this.shouldBurn()) {
            this.igniteForSeconds(1.0F);
         }

         if ($$1.getType() != Type.MISS && this.isAlive()) {
            this.hitTargetOrDeflectSelf($$1);
         }

         this.createParticleTrail();
      } else {
         this.discard();
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

         $$4 = this.getLiquidInertia();
      } else {
         $$4 = this.getInertia();
      }

      this.setDeltaMovement($$0.add($$0.normalize().scale(this.accelerationPower)).scale($$4));
   }

   private void createParticleTrail() {
      ParticleOptions $$0 = this.getTrailParticle();
      Vec3 $$1 = this.position();
      if ($$0 != null) {
         this.level().addParticle($$0, $$1.x, $$1.y + 0.5, $$1.z, 0.0, 0.0, 0.0);
      }
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      return false;
   }

   @Override
   protected boolean canHitEntity(net.minecraft.world.entity.Entity $$0) {
      return super.canHitEntity($$0) && !$$0.noPhysics;
   }

   protected boolean shouldBurn() {
      return true;
   }

   @Nullable
   protected ParticleOptions getTrailParticle() {
      return ParticleTypes.SMOKE;
   }

   protected float getInertia() {
      return 0.95F;
   }

   protected float getLiquidInertia() {
      return 0.8F;
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putDouble("acceleration_power", this.accelerationPower);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.accelerationPower = $$0.getDoubleOr("acceleration_power", 0.1);
   }

   @Override
   public float getLightLevelDependentMagicValue() {
      return 1.0F;
   }

   private void assignDirectionalMovement(Vec3 $$0, double $$1) {
      this.setDeltaMovement($$0.normalize().scale($$1));
      this.needsSync = true;
   }

   @Override
   protected void onDeflection(boolean $$0) {
      super.onDeflection($$0);
      if ($$0) {
         this.accelerationPower = 0.1;
      } else {
         this.accelerationPower *= 0.5;
      }
   }
}
