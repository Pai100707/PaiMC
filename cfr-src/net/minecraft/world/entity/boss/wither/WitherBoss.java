/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.boss.wither;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WitherBoss
extends Monster
implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> DATA_TARGET_A = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_B = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_C = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final List<EntityDataAccessor<Integer>> DATA_TARGETS = ImmutableList.of(DATA_TARGET_A, DATA_TARGET_B, DATA_TARGET_C);
    private static final EntityDataAccessor<Integer> DATA_ID_INV = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final int INVULNERABLE_TICKS = 220;
    private static final int DEFAULT_INVULNERABLE_TICKS = 0;
    private final float[] xRotHeads = new float[2];
    private final float[] yRotHeads = new float[2];
    private final float[] xRotOHeads = new float[2];
    private final float[] yRotOHeads = new float[2];
    private final int[] nextHeadUpdate = new int[2];
    private final int[] idleHeadUpdates = new int[2];
    private int destroyBlocksTick;
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(true);
    private static final TargetingConditions.Selector LIVING_ENTITY_SELECTOR = ($$0, $$1) -> !$$0.getType().is(EntityTypeTags.WITHER_FRIENDS) && $$0.attackable();
    private static final TargetingConditions TARGETING_CONDITIONS = TargetingConditions.forCombat().range(20.0).selector(LIVING_ENTITY_SELECTOR);

    public WitherBoss(EntityType<? extends WitherBoss> $$0, Level $$1) {
        super((EntityType<? extends Monster>)$$0, $$1);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setHealth(this.getMaxHealth());
        this.xpReward = 50;
    }

    @Override
    protected PathNavigation createNavigation(Level $$0) {
        FlyingPathNavigation $$1 = new FlyingPathNavigation(this, $$0);
        $$1.setCanOpenDoors(false);
        $$1.setCanFloat(true);
        return $$1;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new WitherDoNothingGoal());
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 20.0f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, 0, false, false, LIVING_ENTITY_SELECTOR));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_TARGET_A, 0);
        $$0.define(DATA_TARGET_B, 0);
        $$0.define(DATA_TARGET_C, 0);
        $$0.define(DATA_ID_INV, 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putInt("Invul", this.getInvulnerableTicks());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setInvulnerableTicks($$0.getIntOr("Invul", 0));
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Component $$0) {
        super.setCustomName($$0);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public void aiStep() {
        Entity $$1;
        Vec3 $$0 = this.getDeltaMovement().multiply(1.0, 0.6, 1.0);
        if (!this.level().isClientSide() && this.getAlternativeTarget(0) > 0 && ($$1 = this.level().getEntity(this.getAlternativeTarget(0))) != null) {
            double $$2 = $$0.y;
            if (this.getY() < $$1.getY() || !this.isPowered() && this.getY() < $$1.getY() + 5.0) {
                $$2 = Math.max(0.0, $$2);
                $$2 += 0.3 - $$2 * (double)0.6f;
            }
            $$0 = new Vec3($$0.x, $$2, $$0.z);
            Vec3 $$3 = new Vec3($$1.getX() - this.getX(), 0.0, $$1.getZ() - this.getZ());
            if ($$3.horizontalDistanceSqr() > 9.0) {
                Vec3 $$4 = $$3.normalize();
                $$0 = $$0.add($$4.x * 0.3 - $$0.x * 0.6, 0.0, $$4.z * 0.3 - $$0.z * 0.6);
            }
        }
        this.setDeltaMovement($$0);
        if ($$0.horizontalDistanceSqr() > 0.05) {
            this.setYRot((float)Mth.atan2($$0.z, $$0.x) * 57.295776f - 90.0f);
        }
        super.aiStep();
        for (int $$5 = 0; $$5 < 2; ++$$5) {
            this.yRotOHeads[$$5] = this.yRotHeads[$$5];
            this.xRotOHeads[$$5] = this.xRotHeads[$$5];
        }
        for (int $$6 = 0; $$6 < 2; ++$$6) {
            int $$7 = this.getAlternativeTarget($$6 + 1);
            Entity $$8 = null;
            if ($$7 > 0) {
                $$8 = this.level().getEntity($$7);
            }
            if ($$8 != null) {
                double $$9 = this.getHeadX($$6 + 1);
                double $$10 = this.getHeadY($$6 + 1);
                double $$11 = this.getHeadZ($$6 + 1);
                double $$12 = $$8.getX() - $$9;
                double $$13 = $$8.getEyeY() - $$10;
                double $$14 = $$8.getZ() - $$11;
                double $$15 = Math.sqrt($$12 * $$12 + $$14 * $$14);
                float $$16 = (float)(Mth.atan2($$14, $$12) * 57.2957763671875) - 90.0f;
                float $$17 = (float)(-(Mth.atan2($$13, $$15) * 57.2957763671875));
                this.xRotHeads[$$6] = this.rotlerp(this.xRotHeads[$$6], $$17, 40.0f);
                this.yRotHeads[$$6] = this.rotlerp(this.yRotHeads[$$6], $$16, 10.0f);
                continue;
            }
            this.yRotHeads[$$6] = this.rotlerp(this.yRotHeads[$$6], this.yBodyRot, 10.0f);
        }
        boolean $$18 = this.isPowered();
        for (int $$19 = 0; $$19 < 3; ++$$19) {
            double $$20 = this.getHeadX($$19);
            double $$21 = this.getHeadY($$19);
            double $$22 = this.getHeadZ($$19);
            float $$23 = 0.3f * this.getScale();
            this.level().addParticle(ParticleTypes.SMOKE, $$20 + this.random.nextGaussian() * (double)$$23, $$21 + this.random.nextGaussian() * (double)$$23, $$22 + this.random.nextGaussian() * (double)$$23, 0.0, 0.0, 0.0);
            if (!$$18 || this.level().random.nextInt(4) != 0) continue;
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.7f, 0.7f, 0.5f), $$20 + this.random.nextGaussian() * (double)$$23, $$21 + this.random.nextGaussian() * (double)$$23, $$22 + this.random.nextGaussian() * (double)$$23, 0.0, 0.0, 0.0);
        }
        if (this.getInvulnerableTicks() > 0) {
            float $$24 = 3.3f * this.getScale();
            for (int $$25 = 0; $$25 < 3; ++$$25) {
                this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.7f, 0.7f, 0.9f), this.getX() + this.random.nextGaussian(), this.getY() + (double)(this.random.nextFloat() * $$24), this.getZ() + this.random.nextGaussian(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        if (this.getInvulnerableTicks() > 0) {
            int $$1 = this.getInvulnerableTicks() - 1;
            this.bossEvent.setProgress(1.0f - (float)$$1 / 220.0f);
            if ($$1 <= 0) {
                $$0.explode((Entity)this, this.getX(), this.getEyeY(), this.getZ(), 7.0f, false, Level.ExplosionInteraction.MOB);
                if (!this.isSilent()) {
                    $$0.globalLevelEvent(1023, this.blockPosition(), 0);
                }
            }
            this.setInvulnerableTicks($$1);
            if (this.tickCount % 10 == 0) {
                this.heal(10.0f);
            }
            return;
        }
        super.customServerAiStep($$0);
        for (int $$2 = 1; $$2 < 3; ++$$2) {
            int $$8;
            if (this.tickCount < this.nextHeadUpdate[$$2 - 1]) continue;
            this.nextHeadUpdate[$$2 - 1] = this.tickCount + 10 + this.random.nextInt(10);
            if ($$0.getDifficulty() == Difficulty.NORMAL || $$0.getDifficulty() == Difficulty.HARD) {
                int n = $$2 - 1;
                int n2 = this.idleHeadUpdates[n];
                this.idleHeadUpdates[n] = n2 + 1;
                if (n2 > 15) {
                    float $$3 = 10.0f;
                    float $$4 = 5.0f;
                    double $$5 = Mth.nextDouble(this.random, this.getX() - 10.0, this.getX() + 10.0);
                    double $$6 = Mth.nextDouble(this.random, this.getY() - 5.0, this.getY() + 5.0);
                    double $$7 = Mth.nextDouble(this.random, this.getZ() - 10.0, this.getZ() + 10.0);
                    this.performRangedAttack($$2 + 1, $$5, $$6, $$7, true);
                    this.idleHeadUpdates[$$2 - 1] = 0;
                }
            }
            if (($$8 = this.getAlternativeTarget($$2)) > 0) {
                LivingEntity $$9 = (LivingEntity)$$0.getEntity($$8);
                if ($$9 == null || !this.canAttack($$9) || this.distanceToSqr($$9) > 900.0 || !this.hasLineOfSight($$9)) {
                    this.setAlternativeTarget($$2, 0);
                    continue;
                }
                this.performRangedAttack($$2 + 1, $$9);
                this.nextHeadUpdate[$$2 - 1] = this.tickCount + 40 + this.random.nextInt(20);
                this.idleHeadUpdates[$$2 - 1] = 0;
                continue;
            }
            List<LivingEntity> $$10 = $$0.getNearbyEntities(LivingEntity.class, TARGETING_CONDITIONS, this, this.getBoundingBox().inflate(20.0, 8.0, 20.0));
            if ($$10.isEmpty()) continue;
            LivingEntity $$11 = $$10.get(this.random.nextInt($$10.size()));
            this.setAlternativeTarget($$2, $$11.getId());
        }
        if (this.getTarget() != null) {
            this.setAlternativeTarget(0, this.getTarget().getId());
        } else {
            this.setAlternativeTarget(0, 0);
        }
        if (this.destroyBlocksTick > 0) {
            --this.destroyBlocksTick;
            if (this.destroyBlocksTick == 0 && $$0.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                boolean $$12 = false;
                int $$13 = Mth.floor(this.getBbWidth() / 2.0f + 1.0f);
                int $$14 = Mth.floor(this.getBbHeight());
                for (BlockPos $$15 : BlockPos.betweenClosed(this.getBlockX() - $$13, this.getBlockY(), this.getBlockZ() - $$13, this.getBlockX() + $$13, this.getBlockY() + $$14, this.getBlockZ() + $$13)) {
                    BlockState $$16 = $$0.getBlockState($$15);
                    if (!WitherBoss.canDestroy($$16)) continue;
                    $$12 = $$0.destroyBlock($$15, true, this) || $$12;
                }
                if ($$12) {
                    $$0.levelEvent(null, 1022, this.blockPosition(), 0);
                }
            }
        }
        if (this.tickCount % 20 == 0) {
            this.heal(1.0f);
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    public static boolean canDestroy(BlockState $$0) {
        return !$$0.isAir() && !$$0.is(BlockTags.WITHER_IMMUNE);
    }

    public void makeInvulnerable() {
        this.setInvulnerableTicks(220);
        this.bossEvent.setProgress(0.0f);
        this.setHealth(this.getMaxHealth() / 3.0f);
    }

    @Override
    public void makeStuckInBlock(BlockState $$0, Vec3 $$1) {
    }

    @Override
    public void startSeenByPlayer(ServerPlayer $$0) {
        super.startSeenByPlayer($$0);
        this.bossEvent.addPlayer($$0);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer $$0) {
        super.stopSeenByPlayer($$0);
        this.bossEvent.removePlayer($$0);
    }

    private double getHeadX(int $$0) {
        if ($$0 <= 0) {
            return this.getX();
        }
        float $$1 = (this.yBodyRot + (float)(180 * ($$0 - 1))) * ((float)Math.PI / 180);
        float $$2 = Mth.cos($$1);
        return this.getX() + (double)$$2 * 1.3 * (double)this.getScale();
    }

    private double getHeadY(int $$0) {
        float $$1 = $$0 <= 0 ? 3.0f : 2.2f;
        return this.getY() + (double)($$1 * this.getScale());
    }

    private double getHeadZ(int $$0) {
        if ($$0 <= 0) {
            return this.getZ();
        }
        float $$1 = (this.yBodyRot + (float)(180 * ($$0 - 1))) * ((float)Math.PI / 180);
        float $$2 = Mth.sin($$1);
        return this.getZ() + (double)$$2 * 1.3 * (double)this.getScale();
    }

    private float rotlerp(float $$0, float $$1, float $$2) {
        float $$3 = Mth.wrapDegrees($$1 - $$0);
        if ($$3 > $$2) {
            $$3 = $$2;
        }
        if ($$3 < -$$2) {
            $$3 = -$$2;
        }
        return $$0 + $$3;
    }

    private void performRangedAttack(int $$0, LivingEntity $$1) {
        this.performRangedAttack($$0, $$1.getX(), $$1.getY() + (double)$$1.getEyeHeight() * 0.5, $$1.getZ(), $$0 == 0 && this.random.nextFloat() < 0.001f);
    }

    private void performRangedAttack(int $$0, double $$1, double $$2, double $$3, boolean $$4) {
        if (!this.isSilent()) {
            this.level().levelEvent(null, 1024, this.blockPosition(), 0);
        }
        double $$5 = this.getHeadX($$0);
        double $$6 = this.getHeadY($$0);
        double $$7 = this.getHeadZ($$0);
        double $$8 = $$1 - $$5;
        double $$9 = $$2 - $$6;
        double $$10 = $$3 - $$7;
        Vec3 $$11 = new Vec3($$8, $$9, $$10);
        WitherSkull $$12 = new WitherSkull(this.level(), this, $$11.normalize());
        $$12.setOwner(this);
        if ($$4) {
            $$12.setDangerous(true);
        }
        $$12.setPos($$5, $$6, $$7);
        this.level().addFreshEntity($$12);
    }

    @Override
    public void performRangedAttack(LivingEntity $$0, float $$1) {
        this.performRangedAttack(0, $$0);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        Entity $$3;
        if (this.isInvulnerableTo($$0, $$1)) {
            return false;
        }
        if ($$1.is(DamageTypeTags.WITHER_IMMUNE_TO) || $$1.getEntity() instanceof WitherBoss) {
            return false;
        }
        if (this.getInvulnerableTicks() > 0 && !$$1.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (this.isPowered() && (($$3 = $$1.getDirectEntity()) instanceof AbstractArrow || $$3 instanceof WindCharge)) {
            return false;
        }
        Entity $$4 = $$1.getEntity();
        if ($$4 != null && $$4.getType().is(EntityTypeTags.WITHER_FRIENDS)) {
            return false;
        }
        if (this.destroyBlocksTick <= 0) {
            this.destroyBlocksTick = 20;
        }
        int $$5 = 0;
        while ($$5 < this.idleHeadUpdates.length) {
            int n = $$5++;
            this.idleHeadUpdates[n] = this.idleHeadUpdates[n] + 3;
        }
        return super.hurtServer($$0, $$1, $$2);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel $$0, DamageSource $$1, boolean $$2) {
        super.dropCustomDeathLoot($$0, $$1, $$2);
        ItemEntity $$3 = this.spawnAtLocation($$0, Items.NETHER_STAR);
        if ($$3 != null) {
            $$3.setExtendedLifetime();
        }
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && !this.getType().isAllowedInPeaceful()) {
            this.discard();
            return;
        }
        this.noActionTime = 0;
    }

    @Override
    public boolean addEffect(MobEffectInstance $$0, @Nullable Entity $$1) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.MOVEMENT_SPEED, 0.6f).add(Attributes.FLYING_SPEED, 0.6f).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ARMOR, 4.0);
    }

    public float[] getHeadYRots() {
        return this.yRotHeads;
    }

    public float[] getHeadXRots() {
        return this.xRotHeads;
    }

    public int getInvulnerableTicks() {
        return this.entityData.get(DATA_ID_INV);
    }

    public void setInvulnerableTicks(int $$0) {
        this.entityData.set(DATA_ID_INV, $$0);
    }

    public int getAlternativeTarget(int $$0) {
        return this.entityData.get(DATA_TARGETS.get($$0));
    }

    public void setAlternativeTarget(int $$0, int $$1) {
        this.entityData.set(DATA_TARGETS.get($$0), $$1);
    }

    public boolean isPowered() {
        return this.getHealth() <= this.getMaxHealth() / 2.0f;
    }

    @Override
    protected boolean canRide(Entity $$0) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean $$0) {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance $$0) {
        if ($$0.is(MobEffects.WITHER)) {
            return false;
        }
        return super.canBeAffected($$0);
    }

    class WitherDoNothingGoal
    extends Goal {
        public WitherDoNothingGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return WitherBoss.this.getInvulnerableTicks() > 0;
        }
    }
}

