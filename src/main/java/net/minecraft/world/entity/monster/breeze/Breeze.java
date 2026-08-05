package net.minecraft.world.entity.monster.breeze;

import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.debug.DebugBreezeInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueSource.Registration;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Breeze extends Monster {
   private static final int SLIDE_PARTICLES_AMOUNT = 20;
   private static final int IDLE_PARTICLES_AMOUNT = 1;
   private static final int JUMP_DUST_PARTICLES_AMOUNT = 20;
   private static final int JUMP_TRAIL_PARTICLES_AMOUNT = 3;
   private static final int JUMP_TRAIL_DURATION_TICKS = 5;
   private static final int JUMP_CIRCLE_DISTANCE_Y = 10;
   private static final float FALL_DISTANCE_SOUND_TRIGGER_THRESHOLD = 3.0F;
   private static final int WHIRL_SOUND_FREQUENCY_MIN = 1;
   private static final int WHIRL_SOUND_FREQUENCY_MAX = 80;
   public net.minecraft.world.entity.AnimationState idle = new net.minecraft.world.entity.AnimationState();
   public net.minecraft.world.entity.AnimationState slide = new net.minecraft.world.entity.AnimationState();
   public net.minecraft.world.entity.AnimationState slideBack = new net.minecraft.world.entity.AnimationState();
   public net.minecraft.world.entity.AnimationState longJump = new net.minecraft.world.entity.AnimationState();
   public net.minecraft.world.entity.AnimationState shoot = new net.minecraft.world.entity.AnimationState();
   public net.minecraft.world.entity.AnimationState inhale = new net.minecraft.world.entity.AnimationState();
   private int jumpTrailStartedTick = 0;
   private int soundTick = 0;
   private static final ProjectileDeflection PROJECTILE_DEFLECTION = ($$0, $$1, $$2) -> {
      $$1.level().playSound(null, $$1, SoundEvents.BREEZE_DEFLECT, $$1.getSoundSource(), 1.0F, 1.0F);
      ProjectileDeflection.REVERSE.deflect($$0, $$1, $$2);
   };

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.63F)
         .add(Attributes.MAX_HEALTH, 30.0)
         .add(Attributes.FOLLOW_RANGE, 24.0)
         .add(Attributes.ATTACK_DAMAGE, 3.0);
   }

   public Breeze(net.minecraft.world.entity.EntityType<? extends Monster> $$0, Level $$1) {
      super($$0, $$1);
      this.setPathfindingMalus(PathType.DANGER_TRAPDOOR, -1.0F);
      this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
      this.xpReward = 10;
   }

   @Override
   protected Brain<?> makeBrain(Dynamic<?> $$0) {
      return BreezeAi.makeBrain(this, this.brainProvider().makeBrain($$0));
   }

   @Override
   public Brain<Breeze> getBrain() {
      return (Brain<Breeze>)super.getBrain();
   }

   @Override
   protected Brain.Provider<Breeze> brainProvider() {
      return Brain.provider(BreezeAi.MEMORY_TYPES, BreezeAi.SENSOR_TYPES);
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      if (this.level().isClientSide() && DATA_POSE.equals($$0)) {
         this.resetAnimations();
         net.minecraft.world.entity.Pose $$1 = this.getPose();
         switch ($$1) {
            case SHOOTING:
               this.shoot.startIfStopped(this.tickCount);
               break;
            case INHALING:
               this.inhale.startIfStopped(this.tickCount);
               break;
            case SLIDING:
               this.slide.startIfStopped(this.tickCount);
         }
      }

      super.onSyncedDataUpdated($$0);
   }

   private void resetAnimations() {
      this.shoot.stop();
      this.idle.stop();
      this.inhale.stop();
      this.longJump.stop();
   }

   @Override
   public void tick() {
      net.minecraft.world.entity.Pose $$0 = this.getPose();
      switch ($$0) {
         case SHOOTING:
         case INHALING:
         case STANDING:
            this.resetJumpTrail().emitGroundParticles(1 + this.getRandom().nextInt(1));
            break;
         case SLIDING:
            this.emitGroundParticles(20);
            break;
         case LONG_JUMPING:
            this.longJump.startIfStopped(this.tickCount);
            this.emitJumpTrailParticles();
      }

      this.idle.startIfStopped(this.tickCount);
      if ($$0 != net.minecraft.world.entity.Pose.SLIDING && this.slide.isStarted()) {
         this.slideBack.start(this.tickCount);
         this.slide.stop();
      }

      this.soundTick = this.soundTick == 0 ? this.random.nextIntBetweenInclusive(1, 80) : this.soundTick - 1;
      if (this.soundTick == 0) {
         this.playWhirlSound();
      }

      super.tick();
   }

   public Breeze resetJumpTrail() {
      this.jumpTrailStartedTick = 0;
      return this;
   }

   public void emitJumpTrailParticles() {
      if (++this.jumpTrailStartedTick <= 5) {
         BlockState $$0 = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();
         Vec3 $$1 = this.getDeltaMovement();
         Vec3 $$2 = this.position().add($$1).add(0.0, 0.1F, 0.0);

         for (int $$3 = 0; $$3 < 3; $$3++) {
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, $$0), $$2.x, $$2.y, $$2.z, 0.0, 0.0, 0.0);
         }
      }
   }

   public void emitGroundParticles(int $$0) {
      if (!this.isPassenger()) {
         Vec3 $$1 = this.getBoundingBox().getCenter();
         Vec3 $$2 = new Vec3($$1.x, this.position().y, $$1.z);
         BlockState $$3 = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();
         if ($$3.getRenderShape() != RenderShape.INVISIBLE) {
            for (int $$4 = 0; $$4 < $$0; $$4++) {
               this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, $$3), $$2.x, $$2.y, $$2.z, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   @Override
   public void playAmbientSound() {
      if (this.getTarget() == null || !this.onGround()) {
         this.level().playLocalSound(this, this.getAmbientSound(), this.getSoundSource(), 1.0F, 1.0F);
      }
   }

   public void playWhirlSound() {
      float $$0 = 0.7F + 0.4F * this.random.nextFloat();
      float $$1 = 0.8F + 0.2F * this.random.nextFloat();
      this.level().playLocalSound(this, SoundEvents.BREEZE_WHIRL, this.getSoundSource(), $$1, $$0);
   }

   @Override
   public ProjectileDeflection deflection(Projectile $$0) {
      if ($$0.getType() != net.minecraft.world.entity.EntityType.BREEZE_WIND_CHARGE && $$0.getType() != net.minecraft.world.entity.EntityType.WIND_CHARGE) {
         return this.getType().is(EntityTypeTags.DEFLECTS_PROJECTILES) ? PROJECTILE_DEFLECTION : ProjectileDeflection.NONE;
      } else {
         return ProjectileDeflection.NONE;
      }
   }

   @Override
   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.BREEZE_DEATH;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.BREEZE_HURT;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return this.onGround() ? SoundEvents.BREEZE_IDLE_GROUND : SoundEvents.BREEZE_IDLE_AIR;
   }

   public Optional<net.minecraft.world.entity.LivingEntity> getHurtBy() {
      return this.getBrain()
         .getMemory(MemoryModuleType.HURT_BY)
         .<net.minecraft.world.entity.Entity>map(DamageSource::getEntity)
         .filter($$0 -> $$0 instanceof net.minecraft.world.entity.LivingEntity)
         .map($$0 -> (net.minecraft.world.entity.LivingEntity)$$0);
   }

   public boolean withinInnerCircleRange(Vec3 $$0) {
      Vec3 $$1 = this.blockPosition().getCenter();
      return $$0.closerThan($$1, 4.0, 10.0);
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      ProfilerFiller $$1 = Profiler.get();
      $$1.push("breezeBrain");
      this.getBrain().tick($$0, this);
      $$1.popPush("breezeActivityUpdate");
      BreezeAi.updateActivity(this);
      $$1.pop();
      super.customServerAiStep($$0);
   }

   @Override
   public boolean canAttackType(net.minecraft.world.entity.EntityType<?> $$0) {
      return $$0 == net.minecraft.world.entity.EntityType.PLAYER || $$0 == net.minecraft.world.entity.EntityType.IRON_GOLEM;
   }

   @Override
   public int getMaxHeadYRot() {
      return 30;
   }

   @Override
   public int getHeadRotSpeed() {
      return 25;
   }

   public double getFiringYPosition() {
      return this.getY() + this.getBbHeight() / 2.0F + 0.3F;
   }

   @Override
   public boolean isInvulnerableTo(ServerLevel $$0, DamageSource $$1) {
      return $$1.getEntity() instanceof Breeze || super.isInvulnerableTo($$0, $$1);
   }

   @Override
   public double getFluidJumpThreshold() {
      return this.getEyeHeight();
   }

   @Override
   public boolean causeFallDamage(double $$0, float $$1, DamageSource $$2) {
      if ($$0 > 3.0) {
         this.playSound(SoundEvents.BREEZE_LAND, 1.0F, 1.0F);
      }

      return super.causeFallDamage($$0, $$1, $$2);
   }

   @Override
   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.EVENTS;
   }

   
   @Override
   public net.minecraft.world.entity.LivingEntity getTarget() {
      return this.getTargetFromBrain();
   }

   @Override
   public void registerDebugValues(ServerLevel $$0, Registration $$1) {
      super.registerDebugValues($$0, $$1);
      $$1.register(
         DebugSubscriptions.BREEZES,
         () -> new DebugBreezeInfo(
            this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map(net.minecraft.world.entity.Entity::getId),
            this.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET)
         )
      );
   }
}
