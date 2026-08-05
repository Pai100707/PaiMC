/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import com.google.common.annotations.VisibleForTesting;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public class Slime
extends Mob
implements Enemy {
    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Slime.class, EntityDataSerializers.INT);
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 127;
    public static final int MAX_NATURAL_SIZE = 4;
    private static final boolean DEFAULT_WAS_ON_GROUND = false;
    public float targetSquish;
    public float squish;
    public float oSquish;
    private boolean wasOnGround = false;

    public Slime(EntityType<? extends Slime> $$0, Level $$1) {
        super((EntityType<? extends Mob>)$$0, $$1);
        this.fixupDimensions();
        this.moveControl = new SlimeMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SlimeFloatGoal(this));
        this.goalSelector.addGoal(2, new SlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new SlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new SlimeKeepOnJumpingGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, ($$0, $$1) -> Math.abs($$0.getY() - this.getY()) <= 4.0));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, true));
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(ID_SIZE, 1);
    }

    @VisibleForTesting
    public void setSize(int $$0, boolean $$1) {
        int $$2 = Mth.clamp($$0, 1, 127);
        this.entityData.set(ID_SIZE, $$2);
        this.reapplyPosition();
        this.refreshDimensions();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue($$2 * $$2);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2f + 0.1f * (float)$$2);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue($$2);
        if ($$1) {
            this.setHealth(this.getMaxHealth());
        }
        this.xpReward = $$2;
    }

    public int getSize() {
        return this.entityData.get(ID_SIZE);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putInt("Size", this.getSize() - 1);
        $$0.putBoolean("wasOnGround", this.wasOnGround);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.setSize($$0.getIntOr("Size", 0) + 1, false);
        super.readAdditionalSaveData($$0);
        this.wasOnGround = $$0.getBooleanOr("wasOnGround", false);
    }

    public boolean isTiny() {
        return this.getSize() <= 1;
    }

    protected ParticleOptions getParticleType() {
        return ParticleTypes.ITEM_SLIME;
    }

    @Override
    public void tick() {
        this.oSquish = this.squish;
        this.squish += (this.targetSquish - this.squish) * 0.5f;
        super.tick();
        if (this.onGround() && !this.wasOnGround) {
            float $$0 = this.getDimensions(this.getPose()).width() * 2.0f;
            float $$1 = $$0 / 2.0f;
            int $$2 = 0;
            while ((float)$$2 < $$0 * 16.0f) {
                float $$3 = this.random.nextFloat() * ((float)Math.PI * 2);
                float $$4 = this.random.nextFloat() * 0.5f + 0.5f;
                float $$5 = Mth.sin($$3) * $$1 * $$4;
                float $$6 = Mth.cos($$3) * $$1 * $$4;
                this.level().addParticle(this.getParticleType(), this.getX() + (double)$$5, this.getY(), this.getZ() + (double)$$6, 0.0, 0.0, 0.0);
                ++$$2;
            }
            this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) / 0.8f);
            this.targetSquish = -0.5f;
        } else if (!this.onGround() && this.wasOnGround) {
            this.targetSquish = 1.0f;
        }
        this.wasOnGround = this.onGround();
        this.decreaseSquish();
    }

    protected void decreaseSquish() {
        this.targetSquish *= 0.6f;
    }

    protected int getJumpDelay() {
        return this.random.nextInt(20) + 10;
    }

    @Override
    public void refreshDimensions() {
        double $$0 = this.getX();
        double $$1 = this.getY();
        double $$2 = this.getZ();
        super.refreshDimensions();
        this.setPos($$0, $$1, $$2);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (ID_SIZE.equals($$0)) {
            this.refreshDimensions();
            this.setYRot(this.yHeadRot);
            this.yBodyRot = this.yHeadRot;
            if (this.isInWater() && this.random.nextInt(20) == 0) {
                this.doWaterSplashEffect();
            }
        }
        super.onSyncedDataUpdated($$0);
    }

    public EntityType<? extends Slime> getType() {
        return super.getType();
    }

    @Override
    public void remove(Entity.RemovalReason $$0) {
        int $$1 = this.getSize();
        if (!this.level().isClientSide() && $$1 > 1 && this.isDeadOrDying()) {
            float $$2 = this.getDimensions(this.getPose()).width();
            float $$32 = $$2 / 2.0f;
            int $$4 = $$1 / 2;
            int $$5 = 2 + this.random.nextInt(3);
            PlayerTeam $$6 = this.getTeam();
            for (int $$7 = 0; $$7 < $$5; ++$$7) {
                float $$8 = ((float)($$7 % 2) - 0.5f) * $$32;
                float $$9 = ((float)($$7 / 2) - 0.5f) * $$32;
                this.convertTo(this.getType(), new ConversionParams(ConversionType.SPLIT_ON_DEATH, false, false, $$6), EntitySpawnReason.TRIGGERED, $$3 -> {
                    $$3.setSize($$4, true);
                    $$3.snapTo(this.getX() + (double)$$8, this.getY() + 0.5, this.getZ() + (double)$$9, this.random.nextFloat() * 360.0f, 0.0f);
                });
            }
        }
        super.remove($$0);
    }

    @Override
    public void push(Entity $$0) {
        super.push($$0);
        if ($$0 instanceof IronGolem && this.isDealsDamage()) {
            this.dealDamage((LivingEntity)$$0);
        }
    }

    @Override
    public void playerTouch(Player $$0) {
        if (this.isDealsDamage()) {
            this.dealDamage($$0);
        }
    }

    protected void dealDamage(LivingEntity $$0) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            DamageSource $$2;
            ServerLevel $$1 = (ServerLevel)level;
            if (this.isAlive() && this.isWithinMeleeAttackRange($$0) && this.hasLineOfSight($$0) && $$0.hurtServer($$1, $$2 = this.damageSources().mobAttack(this), this.getAttackDamage())) {
                this.playSound(SoundEvents.SLIME_ATTACK, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                EnchantmentHelper.doPostAttackEffects($$1, $$0, $$2);
            }
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity $$0, EntityDimensions $$1, float $$2) {
        return new Vec3(0.0, (double)$$1.height() - 0.015625 * (double)this.getSize() * (double)$$2, 0.0);
    }

    protected boolean isDealsDamage() {
        return !this.isTiny() && this.isEffectiveAi();
    }

    protected float getAttackDamage() {
        return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        if (this.isTiny()) {
            return SoundEvents.SLIME_HURT_SMALL;
        }
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isTiny()) {
            return SoundEvents.SLIME_DEATH_SMALL;
        }
        return SoundEvents.SLIME_DEATH;
    }

    protected SoundEvent getSquishSound() {
        if (this.isTiny()) {
            return SoundEvents.SLIME_SQUISH_SMALL;
        }
        return SoundEvents.SLIME_SQUISH;
    }

    public static boolean checkSlimeSpawnRules(EntityType<Slime> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        if ($$1.getDifficulty() != Difficulty.PEACEFUL) {
            boolean $$7;
            if (EntitySpawnReason.isSpawner($$2)) {
                return Slime.checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
            }
            if ($$1.getBiome($$3).is(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS) && $$3.getY() > 50 && $$3.getY() < 70) {
                float $$5 = $$1.environmentAttributes().getValue(EnvironmentAttributes.SURFACE_SLIME_SPAWN_CHANCE, $$3).floatValue();
                if ($$4.nextFloat() < $$5 && $$1.getMaxLocalRawBrightness($$3) <= $$4.nextInt(8)) {
                    return Slime.checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
                }
            }
            if (!($$1 instanceof WorldGenLevel)) {
                return false;
            }
            ChunkPos $$6 = new ChunkPos($$3);
            boolean bl = $$7 = WorldgenRandom.seedSlimeChunk($$6.x, $$6.z, ((WorldGenLevel)$$1).getSeed(), 987234911L).nextInt(10) == 0;
            if ($$4.nextInt(10) == 0 && $$7 && $$3.getY() < 40) {
                return Slime.checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
            }
        }
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f * (float)this.getSize();
    }

    @Override
    public int getMaxHeadXRot() {
        return 0;
    }

    protected boolean doPlayJumpSound() {
        return this.getSize() > 0;
    }

    @Override
    public void jumpFromGround() {
        Vec3 $$0 = this.getDeltaMovement();
        this.setDeltaMovement($$0.x, this.getJumpPower(), $$0.z);
        this.needsSync = true;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        RandomSource $$4 = $$0.getRandom();
        int $$5 = $$4.nextInt(3);
        if ($$5 < 2 && $$4.nextFloat() < 0.5f * $$1.getSpecialMultiplier()) {
            ++$$5;
        }
        int $$6 = 1 << $$5;
        this.setSize($$6, true);
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    float getSoundPitch() {
        float $$0 = this.isTiny() ? 1.4f : 0.8f;
        return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * $$0;
    }

    protected SoundEvent getJumpSound() {
        return this.isTiny() ? SoundEvents.SLIME_JUMP_SMALL : SoundEvents.SLIME_JUMP;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return super.getDefaultDimensions($$0).scale(this.getSize());
    }

    static class SlimeMoveControl
    extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final Slime slime;
        private boolean isAggressive;

        public SlimeMoveControl(Slime $$0) {
            super($$0);
            this.slime = $$0;
            this.yRot = 180.0f * $$0.getYRot() / (float)Math.PI;
        }

        public void setDirection(float $$0, boolean $$1) {
            this.yRot = $$0;
            this.isAggressive = $$1;
        }

        public void setWantedMovement(double $$0) {
            this.speedModifier = $$0;
            this.operation = MoveControl.Operation.MOVE_TO;
        }

        @Override
        public void tick() {
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0f));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setZza(0.0f);
                return;
            }
            this.operation = MoveControl.Operation.WAIT;
            if (this.mob.onGround()) {
                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                if (this.jumpDelay-- <= 0) {
                    this.jumpDelay = this.slime.getJumpDelay();
                    if (this.isAggressive) {
                        this.jumpDelay /= 3;
                    }
                    this.slime.getJumpControl().jump();
                    if (this.slime.doPlayJumpSound()) {
                        this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.getSoundPitch());
                    }
                } else {
                    this.slime.xxa = 0.0f;
                    this.slime.zza = 0.0f;
                    this.mob.setSpeed(0.0f);
                }
            } else {
                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            }
        }
    }

    static class SlimeFloatGoal
    extends Goal {
        private final Slime slime;

        public SlimeFloatGoal(Slime $$0) {
            this.slime = $$0;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
            $$0.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return (this.slime.isInWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            MoveControl moveControl;
            if (this.slime.getRandom().nextFloat() < 0.8f) {
                this.slime.getJumpControl().jump();
            }
            if ((moveControl = this.slime.getMoveControl()) instanceof SlimeMoveControl) {
                SlimeMoveControl $$0 = (SlimeMoveControl)moveControl;
                $$0.setWantedMovement(1.2);
            }
        }
    }

    static class SlimeAttackGoal
    extends Goal {
        private final Slime slime;
        private int growTiredTimer;

        public SlimeAttackGoal(Slime $$0) {
            this.slime = $$0;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity $$0 = this.slime.getTarget();
            if ($$0 == null) {
                return false;
            }
            if (!this.slime.canAttack($$0)) {
                return false;
            }
            return this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public void start() {
            this.growTiredTimer = SlimeAttackGoal.reducedTickDelay(300);
            super.start();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity $$0 = this.slime.getTarget();
            if ($$0 == null) {
                return false;
            }
            if (!this.slime.canAttack($$0)) {
                return false;
            }
            return --this.growTiredTimer > 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            MoveControl moveControl;
            LivingEntity $$0 = this.slime.getTarget();
            if ($$0 != null) {
                this.slime.lookAt($$0, 10.0f, 10.0f);
            }
            if ((moveControl = this.slime.getMoveControl()) instanceof SlimeMoveControl) {
                SlimeMoveControl $$1 = (SlimeMoveControl)moveControl;
                $$1.setDirection(this.slime.getYRot(), this.slime.isDealsDamage());
            }
        }
    }

    static class SlimeRandomDirectionGoal
    extends Goal {
        private final Slime slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public SlimeRandomDirectionGoal(Slime $$0) {
            this.slime = $$0;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.slime.getTarget() == null && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION)) && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public void tick() {
            MoveControl moveControl;
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = this.adjustedTickDelay(40 + this.slime.getRandom().nextInt(60));
                this.chosenDegrees = this.slime.getRandom().nextInt(360);
            }
            if ((moveControl = this.slime.getMoveControl()) instanceof SlimeMoveControl) {
                SlimeMoveControl $$0 = (SlimeMoveControl)moveControl;
                $$0.setDirection(this.chosenDegrees, false);
            }
        }
    }

    static class SlimeKeepOnJumpingGoal
    extends Goal {
        private final Slime slime;

        public SlimeKeepOnJumpingGoal(Slime $$0) {
            this.slime = $$0;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.slime.isPassenger();
        }

        @Override
        public void tick() {
            MoveControl moveControl = this.slime.getMoveControl();
            if (moveControl instanceof SlimeMoveControl) {
                SlimeMoveControl $$0 = (SlimeMoveControl)moveControl;
                $$0.setWantedMovement(1.0);
            }
        }
    }
}

