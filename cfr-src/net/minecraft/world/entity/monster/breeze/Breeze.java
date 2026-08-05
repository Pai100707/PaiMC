/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
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
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.breeze.BreezeAi;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Breeze
extends Monster {
    private static final int SLIDE_PARTICLES_AMOUNT = 20;
    private static final int IDLE_PARTICLES_AMOUNT = 1;
    private static final int JUMP_DUST_PARTICLES_AMOUNT = 20;
    private static final int JUMP_TRAIL_PARTICLES_AMOUNT = 3;
    private static final int JUMP_TRAIL_DURATION_TICKS = 5;
    private static final int JUMP_CIRCLE_DISTANCE_Y = 10;
    private static final float FALL_DISTANCE_SOUND_TRIGGER_THRESHOLD = 3.0f;
    private static final int WHIRL_SOUND_FREQUENCY_MIN = 1;
    private static final int WHIRL_SOUND_FREQUENCY_MAX = 80;
    public AnimationState idle = new AnimationState();
    public AnimationState slide = new AnimationState();
    public AnimationState slideBack = new AnimationState();
    public AnimationState longJump = new AnimationState();
    public AnimationState shoot = new AnimationState();
    public AnimationState inhale = new AnimationState();
    private int jumpTrailStartedTick = 0;
    private int soundTick = 0;
    private static final ProjectileDeflection PROJECTILE_DEFLECTION = ($$0, $$1, $$2) -> {
        $$1.level().playSound(null, $$1, SoundEvents.BREEZE_DEFLECT, $$1.getSoundSource(), 1.0f, 1.0f);
        ProjectileDeflection.REVERSE.deflect($$0, $$1, $$2);
    };

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.63f).add(Attributes.MAX_HEALTH, 30.0).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    public Breeze(EntityType<? extends Monster> $$0, Level $$1) {
        super($$0, $$1);
        this.setPathfindingMalus(PathType.DANGER_TRAPDOOR, -1.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0f);
        this.xpReward = 10;
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return BreezeAi.makeBrain(this, this.brainProvider().makeBrain($$0));
    }

    public Brain<Breeze> getBrain() {
        return super.getBrain();
    }

    protected Brain.Provider<Breeze> brainProvider() {
        return Brain.provider(BreezeAi.MEMORY_TYPES, BreezeAi.SENSOR_TYPES);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (this.level().isClientSide() && DATA_POSE.equals($$0)) {
            this.resetAnimations();
            Pose $$1 = this.getPose();
            switch ($$1) {
                case SHOOTING: {
                    this.shoot.startIfStopped(this.tickCount);
                    break;
                }
                case INHALING: {
                    this.inhale.startIfStopped(this.tickCount);
                    break;
                }
                case SLIDING: {
                    this.slide.startIfStopped(this.tickCount);
                }
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
        Pose $$0 = this.getPose();
        switch ($$0) {
            case SLIDING: {
                this.emitGroundParticles(20);
                break;
            }
            case SHOOTING: 
            case INHALING: 
            case STANDING: {
                this.resetJumpTrail().emitGroundParticles(1 + this.getRandom().nextInt(1));
                break;
            }
            case LONG_JUMPING: {
                this.longJump.startIfStopped(this.tickCount);
                this.emitJumpTrailParticles();
            }
        }
        this.idle.startIfStopped(this.tickCount);
        if ($$0 != Pose.SLIDING && this.slide.isStarted()) {
            this.slideBack.start(this.tickCount);
            this.slide.stop();
        }
        int n = this.soundTick = this.soundTick == 0 ? this.random.nextIntBetweenInclusive(1, 80) : this.soundTick - 1;
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
        if (++this.jumpTrailStartedTick > 5) {
            return;
        }
        BlockState $$0 = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();
        Vec3 $$1 = this.getDeltaMovement();
        Vec3 $$2 = this.position().add($$1).add(0.0, 0.1f, 0.0);
        for (int $$3 = 0; $$3 < 3; ++$$3) {
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, $$0), $$2.x, $$2.y, $$2.z, 0.0, 0.0, 0.0);
        }
    }

    public void emitGroundParticles(int $$0) {
        BlockState $$3;
        if (this.isPassenger()) {
            return;
        }
        Vec3 $$1 = this.getBoundingBox().getCenter();
        Vec3 $$2 = new Vec3($$1.x, this.position().y, $$1.z);
        BlockState blockState = $$3 = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();
        if ($$3.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }
        for (int $$4 = 0; $$4 < $$0; ++$$4) {
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, $$3), $$2.x, $$2.y, $$2.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void playAmbientSound() {
        if (this.getTarget() != null && this.onGround()) {
            return;
        }
        this.level().playLocalSound(this, this.getAmbientSound(), this.getSoundSource(), 1.0f, 1.0f);
    }

    public void playWhirlSound() {
        float $$0 = 0.7f + 0.4f * this.random.nextFloat();
        float $$1 = 0.8f + 0.2f * this.random.nextFloat();
        this.level().playLocalSound(this, SoundEvents.BREEZE_WHIRL, this.getSoundSource(), $$1, $$0);
    }

    @Override
    public ProjectileDeflection deflection(Projectile $$0) {
        if ($$0.getType() == EntityType.BREEZE_WIND_CHARGE || $$0.getType() == EntityType.WIND_CHARGE) {
            return ProjectileDeflection.NONE;
        }
        return this.getType().is(EntityTypeTags.DEFLECTS_PROJECTILES) ? PROJECTILE_DEFLECTION : ProjectileDeflection.NONE;
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

    public Optional<LivingEntity> getHurtBy() {
        return this.getBrain().getMemory(MemoryModuleType.HURT_BY).map(DamageSource::getEntity).filter($$0 -> $$0 instanceof LivingEntity).map($$0 -> (LivingEntity)$$0);
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
    public boolean canAttackType(EntityType<?> $$0) {
        return $$0 == EntityType.PLAYER || $$0 == EntityType.IRON_GOLEM;
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
        return this.getY() + (double)(this.getBbHeight() / 2.0f) + (double)0.3f;
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
            this.playSound(SoundEvents.BREEZE_LAND, 1.0f, 1.0f);
        }
        return super.causeFallDamage($$0, $$1, $$2);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }

    @Override
    public void registerDebugValues(ServerLevel $$0, DebugValueSource.Registration $$1) {
        super.registerDebugValues($$0, $$1);
        $$1.register(DebugSubscriptions.BREEZES, () -> new DebugBreezeInfo(this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map(Entity::getId), this.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET)));
    }
}

