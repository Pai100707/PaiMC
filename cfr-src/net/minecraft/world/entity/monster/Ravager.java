/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Ravager
extends Raider {
    private static final Predicate<Entity> ROAR_TARGET_WITH_GRIEFING = $$0 -> !($$0 instanceof Ravager) && $$0.isAlive();
    private static final Predicate<Entity> ROAR_TARGET_WITHOUT_GRIEFING = $$0 -> ROAR_TARGET_WITH_GRIEFING.test((Entity)$$0) && !$$0.getType().equals(EntityType.ARMOR_STAND);
    private static final Predicate<LivingEntity> ROAR_TARGET_ON_CLIENT = $$0 -> !($$0 instanceof Ravager) && $$0.isAlive() && $$0.isLocalInstanceAuthoritative();
    private static final double BASE_MOVEMENT_SPEED = 0.3;
    private static final double ATTACK_MOVEMENT_SPEED = 0.35;
    private static final int STUNNED_COLOR = 8356754;
    private static final float STUNNED_COLOR_BLUE = 0.57254905f;
    private static final float STUNNED_COLOR_GREEN = 0.5137255f;
    private static final float STUNNED_COLOR_RED = 0.49803922f;
    public static final int ATTACK_DURATION = 10;
    public static final int STUN_DURATION = 40;
    private static final int DEFAULT_ATTACK_TICK = 0;
    private static final int DEFAULT_STUN_TICK = 0;
    private static final int DEFAULT_ROAR_TICK = 0;
    private int attackTick = 0;
    private int stunnedTick = 0;
    private int roarTick = 0;

    public Ravager(EntityType<? extends Ravager> $$0, Level $$1) {
        super((EntityType<? extends Raider>)$$0, $$1);
        this.xpReward = 20;
        this.setPathfindingMalus(PathType.LEAVES, 0.0f);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<AbstractVillager>((Mob)this, AbstractVillager.class, true, ($$0, $$1) -> !$$0.isBaby()));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, true));
    }

    @Override
    protected void updateControlFlags() {
        boolean $$0 = !(this.getControllingPassenger() instanceof Mob) || this.getControllingPassenger().getType().is(EntityTypeTags.RAIDERS);
        boolean $$1 = !(this.getVehicle() instanceof AbstractBoat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, $$0);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, $$0 && $$1);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, $$0);
        this.goalSelector.setControlFlag(Goal.Flag.TARGET, $$0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.KNOCKBACK_RESISTANCE, 0.75).add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ATTACK_KNOCKBACK, 1.5).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putInt("AttackTick", this.attackTick);
        $$0.putInt("StunTick", this.stunnedTick);
        $$0.putInt("RoarTick", this.roarTick);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.attackTick = $$0.getIntOr("AttackTick", 0);
        this.stunnedTick = $$0.getIntOr("StunTick", 0);
        this.roarTick = $$0.getIntOr("RoarTick", 0);
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.RAVAGER_CELEBRATE;
    }

    @Override
    public int getMaxHeadYRot() {
        return 45;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.isAlive()) {
            return;
        }
        if (this.isImmobile()) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
        } else {
            double $$0 = this.getTarget() != null ? 0.35 : 0.3;
            double $$1 = this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Mth.lerp(0.1, $$1, $$0));
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)level;
            if (this.horizontalCollision && $$2.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                boolean $$3 = false;
                AABB $$4 = this.getBoundingBox().inflate(0.2);
                for (BlockPos $$5 : BlockPos.betweenClosed(Mth.floor($$4.minX), Mth.floor($$4.minY), Mth.floor($$4.minZ), Mth.floor($$4.maxX), Mth.floor($$4.maxY), Mth.floor($$4.maxZ))) {
                    BlockState $$6 = $$2.getBlockState($$5);
                    Block $$7 = $$6.getBlock();
                    if (!($$7 instanceof LeavesBlock)) continue;
                    $$3 = $$2.destroyBlock($$5, true, this) || $$3;
                }
                if (!$$3 && this.onGround()) {
                    this.jumpFromGround();
                }
            }
        }
        if (this.roarTick > 0) {
            --this.roarTick;
            if (this.roarTick == 10) {
                this.roar();
            }
        }
        if (this.attackTick > 0) {
            --this.attackTick;
        }
        if (this.stunnedTick > 0) {
            --this.stunnedTick;
            this.stunEffect();
            if (this.stunnedTick == 0) {
                this.playSound(SoundEvents.RAVAGER_ROAR, 1.0f, 1.0f);
                this.roarTick = 20;
            }
        }
    }

    private void stunEffect() {
        if (this.random.nextInt(6) == 0) {
            double $$0 = this.getX() - (double)this.getBbWidth() * Math.sin(this.yBodyRot * ((float)Math.PI / 180)) + (this.random.nextDouble() * 0.6 - 0.3);
            double $$1 = this.getY() + (double)this.getBbHeight() - 0.3;
            double $$2 = this.getZ() + (double)this.getBbWidth() * Math.cos(this.yBodyRot * ((float)Math.PI / 180)) + (this.random.nextDouble() * 0.6 - 0.3);
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.49803922f, 0.5137255f, 0.57254905f), $$0, $$1, $$2, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.attackTick > 0 || this.stunnedTick > 0 || this.roarTick > 0;
    }

    @Override
    public boolean hasLineOfSight(Entity $$0) {
        if (this.stunnedTick > 0 || this.roarTick > 0) {
            return false;
        }
        return super.hasLineOfSight($$0);
    }

    @Override
    protected void blockedByItem(LivingEntity $$0) {
        if (this.roarTick == 0) {
            if (this.random.nextDouble() < 0.5) {
                this.stunnedTick = 40;
                this.playSound(SoundEvents.RAVAGER_STUNNED, 1.0f, 1.0f);
                this.level().broadcastEntityEvent(this, (byte)39);
                $$0.push(this);
            } else {
                this.strongKnockback($$0);
            }
            $$0.hurtMarked = true;
        }
    }

    private void roar() {
        Level level;
        if (this.isAlive() && (level = this.level()) instanceof ServerLevel) {
            ServerLevel $$0 = (ServerLevel)level;
            Predicate<Entity> $$1 = $$0.getGameRules().get(GameRules.MOB_GRIEFING) != false ? ROAR_TARGET_WITH_GRIEFING : ROAR_TARGET_WITHOUT_GRIEFING;
            List<Entity> $$2 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0), $$1);
            for (LivingEntity livingEntity : $$2) {
                if (!(livingEntity instanceof AbstractIllager)) {
                    livingEntity.hurtServer($$0, this.damageSources().mobAttack(this), 6.0f);
                }
                if (livingEntity instanceof Player) continue;
                this.strongKnockback(livingEntity);
            }
            this.gameEvent(GameEvent.ENTITY_ACTION);
            $$0.broadcastEntityEvent(this, (byte)69);
        }
    }

    private void applyRoarKnockbackClient() {
        List<LivingEntity> $$0 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0), ROAR_TARGET_ON_CLIENT);
        for (LivingEntity $$1 : $$0) {
            this.strongKnockback($$1);
        }
    }

    private void strongKnockback(Entity $$0) {
        double $$1 = $$0.getX() - this.getX();
        double $$2 = $$0.getZ() - this.getZ();
        double $$3 = Math.max($$1 * $$1 + $$2 * $$2, 0.001);
        $$0.push($$1 / $$3 * 4.0, 0.2, $$2 / $$3 * 4.0);
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 4) {
            this.attackTick = 10;
            this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0f, 1.0f);
        } else if ($$0 == 39) {
            this.stunnedTick = 40;
        } else if ($$0 == 69) {
            this.addRoarParticleEffects();
            this.applyRoarKnockbackClient();
        }
        super.handleEntityEvent($$0);
    }

    private void addRoarParticleEffects() {
        Vec3 $$0 = this.getBoundingBox().getCenter();
        for (int $$1 = 0; $$1 < 40; ++$$1) {
            double $$2 = this.random.nextGaussian() * 0.2;
            double $$3 = this.random.nextGaussian() * 0.2;
            double $$4 = this.random.nextGaussian() * 0.2;
            this.level().addParticle(ParticleTypes.POOF, $$0.x, $$0.y, $$0.z, $$2, $$3, $$4);
        }
    }

    public int getAttackTick() {
        return this.attackTick;
    }

    public int getStunnedTick() {
        return this.stunnedTick;
    }

    public int getRoarTick() {
        return this.roarTick;
    }

    @Override
    public boolean doHurtTarget(ServerLevel $$0, Entity $$1) {
        this.attackTick = 10;
        $$0.broadcastEntityEvent(this, (byte)4);
        this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0f, 1.0f);
        return super.doHurtTarget($$0, $$1);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.15f, 1.0f);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader $$0) {
        return !$$0.containsAnyLiquid(this.getBoundingBox());
    }

    @Override
    public void applyRaidBuffs(ServerLevel $$0, int $$1, boolean $$2) {
    }

    @Override
    public boolean canBeLeader() {
        return false;
    }

    @Override
    protected AABB getAttackBoundingBox(double $$0) {
        AABB $$1 = super.getAttackBoundingBox($$0);
        return $$1.deflate(0.05, 0.0, 0.05);
    }
}

