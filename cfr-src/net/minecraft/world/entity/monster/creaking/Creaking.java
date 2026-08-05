/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.creaking;

import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.creaking.CreakingAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Creaking
extends Monster {
    private static final EntityDataAccessor<Boolean> CAN_MOVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_TEARING_DOWN = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> HOME_POS = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final int ATTACK_ANIMATION_DURATION = 15;
    private static final int MAX_HEALTH = 1;
    private static final float ATTACK_DAMAGE = 3.0f;
    private static final float FOLLOW_RANGE = 32.0f;
    private static final float ACTIVATION_RANGE_SQ = 144.0f;
    public static final int ATTACK_INTERVAL = 40;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.4f;
    public static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.3f;
    public static final int CREAKING_ORANGE = 16545810;
    public static final int CREAKING_GRAY = 0x5F5F5F;
    public static final int INVULNERABILITY_ANIMATION_DURATION = 8;
    public static final int TWITCH_DEATH_DURATION = 45;
    private static final int MAX_PLAYER_STUCK_COUNTER = 4;
    private int attackAnimationRemainingTicks;
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState invulnerabilityAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
    private int invulnerabilityAnimationRemainingTicks;
    private boolean eyesGlowing;
    private int nextFlickerTime;
    private int playerStuckCounter;

    public Creaking(EntityType<? extends Creaking> $$0, Level $$1) {
        super((EntityType<? extends Monster>)$$0, $$1);
        this.lookControl = new CreakingLookControl(this);
        this.moveControl = new CreakingMoveControl(this);
        this.jumpControl = new CreakingJumpControl(this);
        GroundPathNavigation $$2 = (GroundPathNavigation)this.getNavigation();
        $$2.setCanFloat(true);
        this.xpReward = 0;
    }

    public void setTransient(BlockPos $$0) {
        this.setHomePos($$0);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 8.0f);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 8.0f);
        this.setPathfindingMalus(PathType.LAVA, 8.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0f);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0f);
    }

    public boolean isHeartBound() {
        return this.getHomePos() != null;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new CreakingBodyRotationControl(this);
    }

    protected Brain.Provider<Creaking> brainProvider() {
        return CreakingAi.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return CreakingAi.makeBrain(this, this.brainProvider().makeBrain($$0));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(CAN_MOVE, true);
        $$0.define(IS_ACTIVE, false);
        $$0.define(IS_TEARING_DOWN, false);
        $$0.define(HOME_POS, Optional.empty());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.4f).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.STEP_HEIGHT, 1.0625);
    }

    public boolean canMove() {
        return this.entityData.get(CAN_MOVE);
    }

    @Override
    public boolean doHurtTarget(ServerLevel $$0, Entity $$1) {
        if (!($$1 instanceof LivingEntity)) {
            return false;
        }
        this.attackAnimationRemainingTicks = 15;
        this.level().broadcastEntityEvent(this, (byte)4);
        return super.doHurtTarget($$0, $$1);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        CreakingHeartBlockEntity $$6;
        BlockPos $$3 = this.getHomePos();
        if ($$3 == null || $$1.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer($$0, $$1, $$2);
        }
        if (this.isInvulnerableTo($$0, $$1) || this.invulnerabilityAnimationRemainingTicks > 0 || this.isDeadOrDying()) {
            return false;
        }
        Player $$4 = this.blameSourceForDamage($$1);
        Entity $$5 = $$1.getDirectEntity();
        if (!($$5 instanceof LivingEntity) && !($$5 instanceof Projectile) && $$4 == null) {
            return false;
        }
        this.invulnerabilityAnimationRemainingTicks = 8;
        this.level().broadcastEntityEvent(this, (byte)66);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        BlockEntity blockEntity = this.level().getBlockEntity($$3);
        if (blockEntity instanceof CreakingHeartBlockEntity && ($$6 = (CreakingHeartBlockEntity)blockEntity).isProtector(this)) {
            if ($$4 != null) {
                $$6.creakingHurt();
            }
            this.playHurtSound($$1);
        }
        return true;
    }

    public Player blameSourceForDamage(DamageSource $$0) {
        this.resolveMobResponsibleForDamage($$0);
        return this.resolvePlayerResponsibleForDamage($$0);
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && this.canMove();
    }

    @Override
    public void push(double $$0, double $$1, double $$2) {
        if (!this.canMove()) {
            return;
        }
        super.push($$0, $$1, $$2);
    }

    public Brain<Creaking> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        ProfilerFiller $$1 = Profiler.get();
        $$1.push("creakingBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        $$1.pop();
        CreakingAi.updateActivity(this);
    }

    @Override
    public void aiStep() {
        if (this.invulnerabilityAnimationRemainingTicks > 0) {
            --this.invulnerabilityAnimationRemainingTicks;
        }
        if (this.attackAnimationRemainingTicks > 0) {
            --this.attackAnimationRemainingTicks;
        }
        if (!this.level().isClientSide()) {
            boolean $$0 = this.entityData.get(CAN_MOVE);
            boolean $$1 = this.checkCanMove();
            if ($$1 != $$0) {
                this.gameEvent(GameEvent.ENTITY_ACTION);
                if ($$1) {
                    this.makeSound(SoundEvents.CREAKING_UNFREEZE);
                } else {
                    this.stopInPlace();
                    this.makeSound(SoundEvents.CREAKING_FREEZE);
                }
            }
            this.entityData.set(CAN_MOVE, $$1);
        }
        super.aiStep();
    }

    @Override
    public void tick() {
        BlockPos $$0;
        if (!this.level().isClientSide() && ($$0 = this.getHomePos()) != null) {
            CreakingHeartBlockEntity $$1;
            boolean $$2;
            BlockEntity blockEntity = this.level().getBlockEntity($$0);
            boolean bl = $$2 = blockEntity instanceof CreakingHeartBlockEntity && ($$1 = (CreakingHeartBlockEntity)blockEntity).isProtector(this);
            if (!$$2) {
                this.setHealth(0.0f);
            }
        }
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
            this.checkEyeBlink();
        }
    }

    @Override
    protected void tickDeath() {
        if (this.isHeartBound() && this.isTearingDown()) {
            ++this.deathTime;
            if (!this.level().isClientSide() && this.deathTime > 45 && !this.isRemoved()) {
                this.tearDown();
            }
        } else {
            super.tickDeath();
        }
    }

    @Override
    protected void updateWalkAnimation(float $$0) {
        float $$1 = Math.min($$0 * 25.0f, 3.0f);
        this.walkAnimation.update($$1, 0.4f, 1.0f);
    }

    private void setupAnimationStates() {
        this.attackAnimationState.animateWhen(this.attackAnimationRemainingTicks > 0, this.tickCount);
        this.invulnerabilityAnimationState.animateWhen(this.invulnerabilityAnimationRemainingTicks > 0, this.tickCount);
        this.deathAnimationState.animateWhen(this.isTearingDown(), this.tickCount);
    }

    public void tearDown() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$0 = (ServerLevel)level;
            AABB $$1 = this.getBoundingBox();
            Vec3 $$2 = $$1.getCenter();
            double $$3 = $$1.getXsize() * 0.3;
            double $$4 = $$1.getYsize() * 0.3;
            double $$5 = $$1.getZsize() * 0.3;
            $$0.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK_CRUMBLE, Blocks.PALE_OAK_WOOD.defaultBlockState()), $$2.x, $$2.y, $$2.z, 100, $$3, $$4, $$5, 0.0);
            $$0.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK_CRUMBLE, (BlockState)Blocks.CREAKING_HEART.defaultBlockState().setValue(CreakingHeartBlock.STATE, CreakingHeartState.AWAKE)), $$2.x, $$2.y, $$2.z, 10, $$3, $$4, $$5, 0.0);
        }
        this.makeSound(this.getDeathSound());
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    public void creakingDeathEffects(DamageSource $$0) {
        this.blameSourceForDamage($$0);
        this.die($$0);
        this.makeSound(SoundEvents.CREAKING_TWITCH);
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 66) {
            this.invulnerabilityAnimationRemainingTicks = 8;
            this.playHurtSound(this.damageSources().generic());
        } else if ($$0 == 4) {
            this.attackAnimationRemainingTicks = 15;
            this.playAttackSound();
        } else {
            super.handleEntityEvent($$0);
        }
    }

    @Override
    public boolean fireImmune() {
        return this.isHeartBound() || super.fireImmune();
    }

    @Override
    public boolean canUsePortal(boolean $$0) {
        return !this.isHeartBound() && super.canUsePortal($$0);
    }

    @Override
    protected PathNavigation createNavigation(Level $$0) {
        return new CreakingPathNavigation(this, $$0);
    }

    public boolean playerIsStuckInYou() {
        List $$0 = this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
        if ($$0.isEmpty()) {
            this.playerStuckCounter = 0;
            return false;
        }
        AABB $$1 = this.getBoundingBox();
        for (Player $$2 : $$0) {
            if (!$$1.contains($$2.getEyePosition())) continue;
            ++this.playerStuckCounter;
            return this.playerStuckCounter > 4;
        }
        this.playerStuckCounter = 0;
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        $$0.read("home_pos", BlockPos.CODEC).ifPresent(this::setTransient);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.storeNullable("home_pos", BlockPos.CODEC, this.getHomePos());
    }

    public void setHomePos(BlockPos $$0) {
        this.entityData.set(HOME_POS, Optional.of($$0));
    }

    public @Nullable BlockPos getHomePos() {
        return this.entityData.get(HOME_POS).orElse(null);
    }

    public void setTearingDown() {
        this.entityData.set(IS_TEARING_DOWN, true);
    }

    public boolean isTearingDown() {
        return this.entityData.get(IS_TEARING_DOWN);
    }

    public boolean hasGlowingEyes() {
        return this.eyesGlowing;
    }

    public void checkEyeBlink() {
        if (this.deathTime > this.nextFlickerTime) {
            this.nextFlickerTime = this.deathTime + this.getRandom().nextIntBetweenInclusive(this.eyesGlowing ? 2 : this.deathTime / 4, this.eyesGlowing ? 8 : this.deathTime / 2);
            this.eyesGlowing = !this.eyesGlowing;
        }
    }

    @Override
    public void playAttackSound() {
        this.makeSound(SoundEvents.CREAKING_ATTACK);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isActive()) {
            return null;
        }
        return SoundEvents.CREAKING_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return this.isHeartBound() ? SoundEvents.CREAKING_SWAY : super.getHurtSound($$0);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREAKING_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.CREAKING_STEP, 0.15f, 1.0f);
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }

    @Override
    public void knockback(double $$0, double $$1, double $$2) {
        if (!this.canMove()) {
            return;
        }
        super.knockback($$0, $$1, $$2);
    }

    public boolean checkCanMove() {
        List $$0 = this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
        boolean $$1 = this.isActive();
        if ($$0.isEmpty()) {
            if ($$1) {
                this.deactivate();
            }
            return true;
        }
        boolean $$2 = false;
        for (Player $$3 : $$0) {
            if (!this.canAttack($$3) || this.isAlliedTo($$3)) continue;
            $$2 = true;
            if ($$1 && !LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM.test($$3) || !this.isLookingAtMe($$3, 0.5, false, true, this.getEyeY(), this.getY() + 0.5 * (double)this.getScale(), (this.getEyeY() + this.getY()) / 2.0)) continue;
            if ($$1) {
                return false;
            }
            if (!($$3.distanceToSqr(this) < 144.0)) continue;
            this.activate($$3);
            return false;
        }
        if (!$$2 && $$1) {
            this.deactivate();
        }
        return true;
    }

    public void activate(Player $$0) {
        this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, $$0);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(SoundEvents.CREAKING_ACTIVATE);
        this.setIsActive(true);
    }

    public void deactivate() {
        this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(SoundEvents.CREAKING_DEACTIVATE);
        this.setIsActive(false);
    }

    public void setIsActive(boolean $$0) {
        this.entityData.set(IS_ACTIVE, $$0);
    }

    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }

    @Override
    public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
        return 0.0f;
    }

    class CreakingLookControl
    extends LookControl {
        public CreakingLookControl(Creaking $$0) {
            super($$0);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }
        }
    }

    class CreakingMoveControl
    extends MoveControl {
        public CreakingMoveControl(Creaking $$0) {
            super($$0);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }
        }
    }

    class CreakingJumpControl
    extends JumpControl {
        public CreakingJumpControl(Creaking $$0) {
            super($$0);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            } else {
                Creaking.this.setJumping(false);
            }
        }
    }

    class CreakingBodyRotationControl
    extends BodyRotationControl {
        public CreakingBodyRotationControl(Creaking $$0) {
            super($$0);
        }

        @Override
        public void clientTick() {
            if (Creaking.this.canMove()) {
                super.clientTick();
            }
        }
    }

    class CreakingPathNavigation
    extends GroundPathNavigation {
        CreakingPathNavigation(Creaking $$0, Level $$1) {
            super($$0, $$1);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }
        }

        @Override
        protected PathFinder createPathFinder(int $$0) {
            this.nodeEvaluator = new HomeNodeEvaluator();
            this.nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(this.nodeEvaluator, $$0);
        }
    }

    class HomeNodeEvaluator
    extends WalkNodeEvaluator {
        private static final int MAX_DISTANCE_TO_HOME_SQ = 1024;

        HomeNodeEvaluator() {
        }

        @Override
        public PathType getPathType(PathfindingContext $$0, int $$1, int $$2, int $$3) {
            BlockPos $$4 = Creaking.this.getHomePos();
            if ($$4 == null) {
                return super.getPathType($$0, $$1, $$2, $$3);
            }
            double $$5 = $$4.distSqr(new Vec3i($$1, $$2, $$3));
            if ($$5 > 1024.0 && $$5 >= $$4.distSqr($$0.mobPosition())) {
                return PathType.BLOCKED;
            }
            return super.getPathType($$0, $$1, $$2, $$3);
        }
    }
}

