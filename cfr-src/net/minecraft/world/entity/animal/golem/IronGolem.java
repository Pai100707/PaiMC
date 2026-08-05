/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GolemRandomStrollInVillageGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveBackToVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class IronGolem
extends AbstractGolem
implements NeutralMob {
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(IronGolem.class, EntityDataSerializers.BYTE);
    private static final int IRON_INGOT_HEAL_AMOUNT = 25;
    private static final boolean DEFAULT_PLAYER_CREATED = false;
    private int attackAnimationTick;
    private int offerFlowerTick;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private long persistentAngerEndTime;
    private @Nullable EntityReference<LivingEntity> persistentAngerTarget;

    public IronGolem(EntityType<? extends IronGolem> $$0, Level $$1) {
        super((EntityType<? extends AbstractGolem>)$$0, $$1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9, 32.0f));
        this.goalSelector.addGoal(2, new MoveBackToVillageGoal((PathfinderMob)this, 0.6, false));
        this.goalSelector.addGoal(4, new GolemRandomStrollInVillageGoal(this, 0.6));
        this.goalSelector.addGoal(5, new OfferFlowerGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new DefendVillageTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<Mob>(this, Mob.class, 5, false, false, ($$0, $$1) -> $$0 instanceof Enemy && !($$0 instanceof Creeper)));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<IronGolem>(this, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_FLAGS_ID, (byte)0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ATTACK_DAMAGE, 15.0).add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected int decreaseAirSupply(int $$0) {
        return $$0;
    }

    @Override
    protected void doPush(Entity $$0) {
        if ($$0 instanceof Enemy && !($$0 instanceof Creeper) && this.getRandom().nextInt(20) == 0) {
            this.setTarget((LivingEntity)$$0);
        }
        super.doPush($$0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackAnimationTick > 0) {
            --this.attackAnimationTick;
        }
        if (this.offerFlowerTick > 0) {
            --this.offerFlowerTick;
        }
        if (!this.level().isClientSide()) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return this.getDeltaMovement().horizontalDistanceSqr() > 2.500000277905201E-7 && this.random.nextInt(5) == 0;
    }

    @Override
    public boolean canAttackType(EntityType<?> $$0) {
        if (this.isPlayerCreated() && $$0 == EntityType.PLAYER) {
            return false;
        }
        if ($$0 == EntityType.CREEPER) {
            return false;
        }
        return super.canAttackType($$0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean("PlayerCreated", this.isPlayerCreated());
        this.addPersistentAngerSaveData($$0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setPlayerCreated($$0.getBooleanOr("PlayerCreated", false));
        this.readPersistentAngerSaveData(this.level(), $$0);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setPersistentAngerEndTime(long $$0) {
        this.persistentAngerEndTime = $$0;
    }

    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> $$0) {
        this.persistentAngerTarget = $$0;
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    private float getAttackDamage() {
        return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @Override
    public boolean doHurtTarget(ServerLevel $$0, Entity $$1) {
        this.attackAnimationTick = 10;
        $$0.broadcastEntityEvent(this, (byte)4);
        float $$2 = this.getAttackDamage();
        float $$3 = (int)$$2 > 0 ? $$2 / 2.0f + (float)this.random.nextInt((int)$$2) : $$2;
        DamageSource $$4 = this.damageSources().mobAttack(this);
        boolean $$5 = $$1.hurtServer($$0, $$4, $$3);
        if ($$5) {
            double d;
            if ($$1 instanceof LivingEntity) {
                LivingEntity $$6 = (LivingEntity)$$1;
                d = $$6.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            } else {
                d = 0.0;
            }
            double $$7 = d;
            double $$8 = Math.max(0.0, 1.0 - $$7);
            $$1.setDeltaMovement($$1.getDeltaMovement().add(0.0, (double)0.4f * $$8, 0.0));
            EnchantmentHelper.doPostAttackEffects($$0, $$1, $$4);
        }
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        return $$5;
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        Crackiness.Level $$3 = this.getCrackiness();
        boolean $$4 = super.hurtServer($$0, $$1, $$2);
        if ($$4 && this.getCrackiness() != $$3) {
            this.playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.0f, 1.0f);
        }
        return $$4;
    }

    public Crackiness.Level getCrackiness() {
        return Crackiness.GOLEM.byFraction(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 4) {
            this.attackAnimationTick = 10;
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        } else if ($$0 == 11) {
            this.offerFlowerTick = 400;
        } else if ($$0 == 34) {
            this.offerFlowerTick = 0;
        } else {
            super.handleEntityEvent($$0);
        }
    }

    public int getAttackAnimationTick() {
        return this.attackAnimationTick;
    }

    public void offerFlower(boolean $$0) {
        if ($$0) {
            this.offerFlowerTick = 400;
            this.level().broadcastEntityEvent(this, (byte)11);
        } else {
            this.offerFlowerTick = 0;
            this.level().broadcastEntityEvent(this, (byte)34);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        ItemStack $$2 = $$0.getItemInHand($$1);
        if (!$$2.is(Items.IRON_INGOT)) {
            return InteractionResult.PASS;
        }
        float $$3 = this.getHealth();
        this.heal(25.0f);
        if (this.getHealth() == $$3) {
            return InteractionResult.PASS;
        }
        float $$4 = 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f;
        this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0f, $$4);
        $$2.consume(1, $$0);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0f, 1.0f);
    }

    public int getOfferFlowerTick() {
        return this.offerFlowerTick;
    }

    public boolean isPlayerCreated() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setPlayerCreated(boolean $$0) {
        byte $$1 = this.entityData.get(DATA_FLAGS_ID);
        if ($$0) {
            this.entityData.set(DATA_FLAGS_ID, (byte)($$1 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)($$1 & 0xFFFFFFFE));
        }
    }

    @Override
    public void die(DamageSource $$0) {
        super.die($$0);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader $$0) {
        BlockPos $$1 = this.blockPosition();
        BlockPos $$2 = $$1.below();
        BlockState $$3 = $$0.getBlockState($$2);
        if ($$3.entityCanStandOn($$0, $$2, this)) {
            for (int $$4 = 1; $$4 < 3; ++$$4) {
                BlockState $$6;
                BlockPos $$5 = $$1.above($$4);
                if (NaturalSpawner.isValidEmptySpawnBlock($$0, $$5, $$6 = $$0.getBlockState($$5), $$6.getFluidState(), EntityType.IRON_GOLEM)) continue;
                return false;
            }
            return NaturalSpawner.isValidEmptySpawnBlock($$0, $$1, $$0.getBlockState($$1), Fluids.EMPTY.defaultFluidState(), EntityType.IRON_GOLEM) && $$0.isUnobstructed(this);
        }
        return false;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.875f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }
}

