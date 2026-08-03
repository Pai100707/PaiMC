package net.minecraft.world.entity.animal.squid;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Squid extends AgeableWaterCreature {
   public float xBodyRot;
   public float xBodyRotO;
   public float zBodyRot;
   public float zBodyRotO;
   public float tentacleMovement;
   public float oldTentacleMovement;
   public float tentacleAngle;
   public float oldTentacleAngle;
   private float speed;
   private float tentacleSpeed;
   private float rotateSpeed;
   Vec3 movementVector = Vec3.ZERO;

   public Squid(net.minecraft.world.entity.EntityType<? extends Squid> $$0, Level $$1) {
      super($$0, $$1);
      this.random.setSeed(this.getId());
      this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new Squid.SquidRandomMovementGoal(this));
      this.goalSelector.addGoal(1, new Squid.SquidFleeGoal());
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.SQUID_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.SQUID_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.SQUID_DEATH;
   }

   protected SoundEvent getSquirtSound() {
      return SoundEvents.SQUID_SQUIRT;
   }

   @Override
   public boolean canBeLeashed() {
      return true;
   }

   @Override
   protected float getSoundVolume() {
      return 0.4F;
   }

   @Override
   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.EVENTS;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.AgeableMob getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      return net.minecraft.world.entity.EntityType.SQUID.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
   }

   @Override
   protected double getDefaultGravity() {
      return 0.08;
   }

   @Override
   public void aiStep() {
      super.aiStep();
      this.xBodyRotO = this.xBodyRot;
      this.zBodyRotO = this.zBodyRot;
      this.oldTentacleMovement = this.tentacleMovement;
      this.oldTentacleAngle = this.tentacleAngle;
      this.tentacleMovement = this.tentacleMovement + this.tentacleSpeed;
      if (this.tentacleMovement > Math.PI * 2) {
         if (this.level().isClientSide()) {
            this.tentacleMovement = (float) (Math.PI * 2);
         } else {
            this.tentacleMovement -= (float) (Math.PI * 2);
            if (this.random.nextInt(10) == 0) {
               this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
            }

            this.level().broadcastEntityEvent(this, (byte)19);
         }
      }

      if (this.isInWater()) {
         if (this.tentacleMovement < (float) Math.PI) {
            float $$0 = this.tentacleMovement / (float) Math.PI;
            this.tentacleAngle = Mth.sin($$0 * $$0 * (float) Math.PI) * (float) Math.PI * 0.25F;
            if ($$0 > 0.75) {
               if (this.isLocalInstanceAuthoritative()) {
                  this.setDeltaMovement(this.movementVector);
               }

               this.rotateSpeed = 1.0F;
            } else {
               this.rotateSpeed *= 0.8F;
            }
         } else {
            this.tentacleAngle = 0.0F;
            if (this.isLocalInstanceAuthoritative()) {
               this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            }

            this.rotateSpeed *= 0.99F;
         }

         Vec3 $$1 = this.getDeltaMovement();
         double $$2 = $$1.horizontalDistance();
         this.yBodyRot = this.yBodyRot + (-((float)Mth.atan2($$1.x, $$1.z)) * (180.0F / (float)Math.PI) - this.yBodyRot) * 0.1F;
         this.setYRot(this.yBodyRot);
         this.zBodyRot = this.zBodyRot + (float) Math.PI * this.rotateSpeed * 1.5F;
         this.xBodyRot = this.xBodyRot + (-((float)Mth.atan2($$2, $$1.y)) * (180.0F / (float)Math.PI) - this.xBodyRot) * 0.1F;
      } else {
         this.tentacleAngle = Mth.abs(Mth.sin(this.tentacleMovement)) * (float) Math.PI * 0.25F;
         if (!this.level().isClientSide()) {
            double $$3 = this.getDeltaMovement().y;
            if (this.hasEffect(MobEffects.LEVITATION)) {
               $$3 = 0.05 * (this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1);
            } else {
               $$3 -= this.getGravity();
            }

            this.setDeltaMovement(0.0, $$3 * 0.98F, 0.0);
         }

         this.xBodyRot = this.xBodyRot + (-90.0F - this.xBodyRot) * 0.02F;
      }
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (super.hurtServer($$0, $$1, $$2) && this.getLastHurtByMob() != null) {
         this.spawnInk();
         return true;
      } else {
         return false;
      }
   }

   private Vec3 rotateVector(Vec3 $$0) {
      Vec3 $$1 = $$0.xRot(this.xBodyRotO * (float) (Math.PI / 180.0));
      return $$1.yRot(-this.yBodyRotO * (float) (Math.PI / 180.0));
   }

   private void spawnInk() {
      this.makeSound(this.getSquirtSound());
      Vec3 $$0 = this.rotateVector(new Vec3(0.0, -1.0, 0.0)).add(this.getX(), this.getY(), this.getZ());

      for (int $$1 = 0; $$1 < 30; $$1++) {
         Vec3 $$2 = this.rotateVector(new Vec3(this.random.nextFloat() * 0.6 - 0.3, -1.0, this.random.nextFloat() * 0.6 - 0.3));
         float $$3 = this.isBaby() ? 0.1F : 0.3F;
         Vec3 $$4 = $$2.scale($$3 + this.random.nextFloat() * 2.0F);
         ((ServerLevel)this.level()).sendParticles(this.getInkParticle(), $$0.x, $$0.y + 0.5, $$0.z, 0, $$4.x, $$4.y, $$4.z, 0.1F);
      }
   }

   protected ParticleOptions getInkParticle() {
      return ParticleTypes.SQUID_INK;
   }

   @Override
   public void travel(Vec3 $$0) {
      this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 19) {
         this.tentacleMovement = 0.0F;
      } else {
         super.handleEntityEvent($$0);
      }
   }

   public boolean hasMovementVector() {
      return this.movementVector.lengthSqr() > 1.0E-5F;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      net.minecraft.world.entity.SpawnGroupData $$4 = Objects.requireNonNullElseGet(
         $$3, () -> new net.minecraft.world.entity.AgeableMob.AgeableMobGroupData(0.05F)
      );
      return super.finalizeSpawn($$0, $$1, $$2, $$4);
   }

   class SquidFleeGoal extends Goal {
      private static final float SQUID_FLEE_SPEED = 3.0F;
      private static final float SQUID_FLEE_MIN_DISTANCE = 5.0F;
      private static final float SQUID_FLEE_MAX_DISTANCE = 10.0F;
      private int fleeTicks;

      @Override
      public boolean canUse() {
         net.minecraft.world.entity.LivingEntity $$0 = Squid.this.getLastHurtByMob();
         return Squid.this.isInWater() && $$0 != null ? Squid.this.distanceToSqr($$0) < 100.0 : false;
      }

      @Override
      public void start() {
         this.fleeTicks = 0;
      }

      @Override
      public boolean requiresUpdateEveryTick() {
         return true;
      }

      @Override
      public void tick() {
         this.fleeTicks++;
         net.minecraft.world.entity.LivingEntity $$0 = Squid.this.getLastHurtByMob();
         if ($$0 != null) {
            Vec3 $$1 = new Vec3(Squid.this.getX() - $$0.getX(), Squid.this.getY() - $$0.getY(), Squid.this.getZ() - $$0.getZ());
            BlockState $$2 = Squid.this.level()
               .getBlockState(BlockPos.containing(Squid.this.getX() + $$1.x, Squid.this.getY() + $$1.y, Squid.this.getZ() + $$1.z));
            FluidState $$3 = Squid.this.level()
               .getFluidState(BlockPos.containing(Squid.this.getX() + $$1.x, Squid.this.getY() + $$1.y, Squid.this.getZ() + $$1.z));
            if ($$3.is(FluidTags.WATER) || $$2.isAir()) {
               double $$4 = $$1.length();
               if ($$4 > 0.0) {
                  $$1.normalize();
                  double $$5 = 3.0;
                  if ($$4 > 5.0) {
                     $$5 -= ($$4 - 5.0) / 5.0;
                  }

                  if ($$5 > 0.0) {
                     $$1 = $$1.scale($$5);
                  }
               }

               if ($$2.isAir()) {
                  $$1 = $$1.subtract(0.0, $$1.y, 0.0);
               }

               Squid.this.movementVector = new Vec3($$1.x / 20.0, $$1.y / 20.0, $$1.z / 20.0);
            }

            if (this.fleeTicks % 10 == 5) {
               Squid.this.level().addParticle(ParticleTypes.BUBBLE, Squid.this.getX(), Squid.this.getY(), Squid.this.getZ(), 0.0, 0.0, 0.0);
            }
         }
      }
   }

   static class SquidRandomMovementGoal extends Goal {
      private final Squid squid;

      public SquidRandomMovementGoal(Squid $$0) {
         this.squid = $$0;
      }

      @Override
      public boolean canUse() {
         return true;
      }

      @Override
      public void tick() {
         int $$0 = this.squid.getNoActionTime();
         if ($$0 > 100) {
            this.squid.movementVector = Vec3.ZERO;
         } else if (this.squid.getRandom().nextInt(reducedTickDelay(50)) == 0 || !this.squid.wasTouchingWater || !this.squid.hasMovementVector()) {
            float $$1 = this.squid.getRandom().nextFloat() * (float) (Math.PI * 2);
            this.squid.movementVector = new Vec3(Mth.cos($$1) * 0.2F, -0.1F + this.squid.getRandom().nextFloat() * 0.2F, Mth.sin($$1) * 0.2F);
         }
      }
   }
}
