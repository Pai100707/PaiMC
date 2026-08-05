/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Blaze
extends Monster {
    private float allowedHeightOffset = 0.5f;
    private int nextHeightOffsetChangeTick;
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Blaze.class, EntityDataSerializers.BYTE);

    public Blaze(EntityType<? extends Blaze> $$0, Level $$1) {
        super((EntityType<? extends Monster>)$$0, $$1);
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setPathfindingMalus(PathType.LAVA, 8.0f);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0f);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new BlazeAttackGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 1.0, 0.0f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.MOVEMENT_SPEED, 0.23f).add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    public void aiStep() {
        if (!this.onGround() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
        if (this.level().isClientSide()) {
            if (this.random.nextInt(24) == 0 && !this.isSilent()) {
                this.level().playLocalSound(this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5, SoundEvents.BLAZE_BURN, this.getSoundSource(), 1.0f + this.random.nextFloat(), this.random.nextFloat() * 0.7f + 0.3f, false);
            }
            for (int $$0 = 0; $$0 < 2; ++$$0) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
            }
        }
        super.aiStep();
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        LivingEntity $$1;
        --this.nextHeightOffsetChangeTick;
        if (this.nextHeightOffsetChangeTick <= 0) {
            this.nextHeightOffsetChangeTick = 100;
            this.allowedHeightOffset = (float)this.random.triangle(0.5, 6.891);
        }
        if (($$1 = this.getTarget()) != null && $$1.getEyeY() > this.getEyeY() + (double)this.allowedHeightOffset && this.canAttack($$1)) {
            Vec3 $$2 = this.getDeltaMovement();
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, ((double)0.3f - $$2.y) * (double)0.3f, 0.0));
            this.needsSync = true;
        }
        super.customServerAiStep($$0);
    }

    @Override
    public boolean isOnFire() {
        return this.isCharged();
    }

    private boolean isCharged() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    void setCharged(boolean $$0) {
        byte $$1 = this.entityData.get(DATA_FLAGS_ID);
        $$1 = $$0 ? (byte)($$1 | 1) : (byte)($$1 & 0xFFFFFFFE);
        this.entityData.set(DATA_FLAGS_ID, $$1);
    }

    static class BlazeAttackGoal
    extends Goal {
        private final Blaze blaze;
        private int attackStep;
        private int attackTime;
        private int lastSeen;

        public BlazeAttackGoal(Blaze $$0) {
            this.blaze = $$0;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity $$0 = this.blaze.getTarget();
            return $$0 != null && $$0.isAlive() && this.blaze.canAttack($$0);
        }

        @Override
        public void start() {
            this.attackStep = 0;
        }

        @Override
        public void stop() {
            this.blaze.setCharged(false);
            this.lastSeen = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            --this.attackTime;
            LivingEntity $$0 = this.blaze.getTarget();
            if ($$0 == null) {
                return;
            }
            boolean $$1 = this.blaze.getSensing().hasLineOfSight($$0);
            this.lastSeen = $$1 ? 0 : ++this.lastSeen;
            double $$2 = this.blaze.distanceToSqr($$0);
            if ($$2 < 4.0) {
                if (!$$1) {
                    return;
                }
                if (this.attackTime <= 0) {
                    this.attackTime = 20;
                    this.blaze.doHurtTarget(BlazeAttackGoal.getServerLevel(this.blaze), $$0);
                }
                this.blaze.getMoveControl().setWantedPosition($$0.getX(), $$0.getY(), $$0.getZ(), 1.0);
            } else if ($$2 < this.getFollowDistance() * this.getFollowDistance() && $$1) {
                double $$3 = $$0.getX() - this.blaze.getX();
                double $$4 = $$0.getY(0.5) - this.blaze.getY(0.5);
                double $$5 = $$0.getZ() - this.blaze.getZ();
                if (this.attackTime <= 0) {
                    ++this.attackStep;
                    if (this.attackStep == 1) {
                        this.attackTime = 60;
                        this.blaze.setCharged(true);
                    } else if (this.attackStep <= 4) {
                        this.attackTime = 6;
                    } else {
                        this.attackTime = 100;
                        this.attackStep = 0;
                        this.blaze.setCharged(false);
                    }
                    if (this.attackStep > 1) {
                        double $$6 = Math.sqrt(Math.sqrt($$2)) * 0.5;
                        if (!this.blaze.isSilent()) {
                            this.blaze.level().levelEvent(null, 1018, this.blaze.blockPosition(), 0);
                        }
                        for (int $$7 = 0; $$7 < 1; ++$$7) {
                            Vec3 $$8 = new Vec3(this.blaze.getRandom().triangle($$3, 2.297 * $$6), $$4, this.blaze.getRandom().triangle($$5, 2.297 * $$6));
                            SmallFireball $$9 = new SmallFireball(this.blaze.level(), this.blaze, $$8.normalize());
                            $$9.setPos($$9.getX(), this.blaze.getY(0.5) + 0.5, $$9.getZ());
                            this.blaze.level().addFreshEntity($$9);
                        }
                    }
                }
                this.blaze.getLookControl().setLookAt($$0, 10.0f, 10.0f);
            } else if (this.lastSeen < 5) {
                this.blaze.getMoveControl().setWantedPosition($$0.getX(), $$0.getY(), $$0.getZ(), 1.0);
            }
            super.tick();
        }

        private double getFollowDistance() {
            return this.blaze.getAttributeValue(Attributes.FOLLOW_RANGE);
        }
    }
}

