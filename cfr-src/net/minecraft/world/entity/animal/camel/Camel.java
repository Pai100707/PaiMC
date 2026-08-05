/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.camel;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.CamelAi;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Camel
extends AbstractHorse {
    public static final float BABY_SCALE = 0.45f;
    public static final int DASH_COOLDOWN_TICKS = 55;
    public static final int MAX_HEAD_Y_ROT = 30;
    private static final float RUNNING_SPEED_BONUS = 0.1f;
    private static final float DASH_VERTICAL_MOMENTUM = 1.4285f;
    private static final float DASH_HORIZONTAL_MOMENTUM = 22.2222f;
    private static final int DASH_MINIMUM_DURATION_TICKS = 5;
    private static final int SITDOWN_DURATION_TICKS = 40;
    private static final int STANDUP_DURATION_TICKS = 52;
    private static final int IDLE_MINIMAL_DURATION_TICKS = 80;
    private static final float SITTING_HEIGHT_DIFFERENCE = 1.43f;
    private static final long DEFAULT_LAST_POSE_CHANGE_TICK = 0L;
    public static final EntityDataAccessor<Boolean> DASH = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Long> LAST_POSE_CHANGE_TICK = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.LONG);
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState sitPoseAnimationState = new AnimationState();
    public final AnimationState sitUpAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState dashAnimationState = new AnimationState();
    private static final EntityDimensions SITTING_DIMENSIONS = EntityDimensions.scalable(EntityType.CAMEL.getWidth(), EntityType.CAMEL.getHeight() - 1.43f).withEyeHeight(0.845f);
    private int dashCooldown = 0;
    private int idleAnimationTimeout = 0;

    public Camel(EntityType<? extends Camel> $$0, Level $$1) {
        super((EntityType<? extends AbstractHorse>)$$0, $$1);
        this.moveControl = new CamelMoveControl();
        this.lookControl = new CamelLookControl();
        GroundPathNavigation $$2 = (GroundPathNavigation)this.getNavigation();
        $$2.setCanFloat(true);
        $$2.setCanWalkOverFences(true);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putLong("LastPoseTick", this.entityData.get(LAST_POSE_CHANGE_TICK));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        long $$1 = $$0.getLongOr("LastPoseTick", 0L);
        if ($$1 < 0L) {
            this.setPose(Pose.SITTING);
        }
        this.resetLastPoseChangeTick($$1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Camel.createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.MOVEMENT_SPEED, 0.09f).add(Attributes.JUMP_STRENGTH, 0.42f).add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DASH, false);
        $$0.define(LAST_POSE_CHANGE_TICK, 0L);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        CamelAi.initMemories(this, $$0.getRandom());
        this.resetLastPoseChangeTickToFullStand($$0.getLevel().getGameTime());
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    public static boolean checkCamelSpawnRules(EntityType<Camel> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getBlockState($$3.below()).is(BlockTags.CAMELS_SPAWNABLE_ON) && Camel.isBrightEnoughToSpawn($$1, $$3);
    }

    protected Brain.Provider<Camel> brainProvider() {
        return CamelAi.brainProvider();
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return CamelAi.makeBrain(this.brainProvider().makeBrain($$0));
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return $$0 == Pose.SITTING ? SITTING_DIMENSIONS.scale(this.getAgeScale()) : super.getDefaultDimensions($$0);
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        ProfilerFiller $$1 = Profiler.get();
        $$1.push("camelBrain");
        Brain<?> $$2 = this.getBrain();
        $$2.tick($$0, this);
        $$1.pop();
        $$1.push("camelActivityUpdate");
        CamelAi.updateActivity(this);
        $$1.pop();
        super.customServerAiStep($$0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isDashing() && this.dashCooldown < 50 && (this.onGround() || this.isInLiquid() || this.isPassenger())) {
            this.setDashing(false);
        }
        if (this.dashCooldown > 0) {
            --this.dashCooldown;
            if (this.dashCooldown == 0) {
                this.level().playSound(null, this.blockPosition(), this.getDashReadySound(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        }
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
        if (this.refuseToMove()) {
            this.clampHeadRotationToBody();
        }
        if (this.isCamelSitting() && this.isInWater()) {
            this.standUpInstantly();
        }
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
        if (this.isCamelVisuallySitting()) {
            this.sitUpAnimationState.stop();
            this.dashAnimationState.stop();
            if (this.isVisuallySittingDown()) {
                this.sitAnimationState.startIfStopped(this.tickCount);
                this.sitPoseAnimationState.stop();
            } else {
                this.sitAnimationState.stop();
                this.sitPoseAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.sitAnimationState.stop();
            this.sitPoseAnimationState.stop();
            this.dashAnimationState.animateWhen(this.isDashing(), this.tickCount);
            this.sitUpAnimationState.animateWhen(this.isInPoseTransition() && this.getPoseTime() >= 0L, this.tickCount);
        }
    }

    @Override
    protected void updateWalkAnimation(float $$0) {
        float $$2;
        if (this.getPose() == Pose.STANDING && !this.dashAnimationState.isStarted()) {
            float $$1 = Math.min($$0 * 6.0f, 1.0f);
        } else {
            $$2 = 0.0f;
        }
        this.walkAnimation.update($$2, 0.2f, this.isBaby() ? 3.0f : 1.0f);
    }

    @Override
    public void travel(Vec3 $$0) {
        if (this.refuseToMove() && this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            $$0 = $$0.multiply(0.0, 1.0, 0.0);
        }
        super.travel($$0);
    }

    @Override
    protected void tickRidden(Player $$0, Vec3 $$1) {
        super.tickRidden($$0, $$1);
        if ($$0.zza > 0.0f && this.isCamelSitting() && !this.isInPoseTransition()) {
            this.standUp();
        }
    }

    public boolean refuseToMove() {
        return this.isCamelSitting() || this.isInPoseTransition();
    }

    @Override
    protected float getRiddenSpeed(Player $$0) {
        float $$1 = $$0.isSprinting() && this.getJumpCooldown() == 0 ? 0.1f : 0.0f;
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) + $$1;
    }

    @Override
    protected Vec2 getRiddenRotation(LivingEntity $$0) {
        if (this.refuseToMove()) {
            return new Vec2(this.getXRot(), this.getYRot());
        }
        return super.getRiddenRotation($$0);
    }

    @Override
    protected Vec3 getRiddenInput(Player $$0, Vec3 $$1) {
        if (this.refuseToMove()) {
            return Vec3.ZERO;
        }
        return super.getRiddenInput($$0, $$1);
    }

    @Override
    public boolean canJump() {
        return !this.refuseToMove() && super.canJump();
    }

    @Override
    public void onPlayerJump(int $$0) {
        if (!this.isSaddled() || this.dashCooldown > 0 || !this.onGround()) {
            return;
        }
        super.onPlayerJump($$0);
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    @Override
    protected void executeRidersJump(float $$0, Vec3 $$1) {
        double $$2 = this.getJumpPower();
        this.addDeltaMovement(this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale((double)(22.2222f * $$0) * this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)this.getBlockSpeedFactor()).add(0.0, (double)(1.4285f * $$0) * $$2, 0.0));
        this.dashCooldown = 55;
        this.setDashing(true);
        this.needsSync = true;
    }

    public boolean isDashing() {
        return this.entityData.get(DASH);
    }

    public void setDashing(boolean $$0) {
        this.entityData.set(DASH, $$0);
    }

    @Override
    public void handleStartJump(int $$0) {
        this.makeSound(this.getDashingSound());
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.setDashing(true);
    }

    protected SoundEvent getDashingSound() {
        return SoundEvents.CAMEL_DASH;
    }

    protected SoundEvent getDashReadySound() {
        return SoundEvents.CAMEL_DASH_READY;
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    public int getJumpCooldown() {
        return this.dashCooldown;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAMEL_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAMEL_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.CAMEL_HURT;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        if ($$1.is(BlockTags.CAMEL_SAND_STEP_SOUND_BLOCKS)) {
            this.playSound(SoundEvents.CAMEL_STEP_SAND, 1.0f, 1.0f);
        } else {
            this.playSound(SoundEvents.CAMEL_STEP, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.CAMEL_FOOD);
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        ItemStack $$2 = $$0.getItemInHand($$1);
        if ($$0.isSecondaryUseActive() && !this.isBaby()) {
            this.openCustomInventoryScreen($$0);
            return InteractionResult.SUCCESS;
        }
        InteractionResult $$3 = $$2.interactLivingEntity($$0, this, $$1);
        if ($$3.consumesAction()) {
            return $$3;
        }
        if (this.isFood($$2)) {
            return this.fedFood($$0, $$2);
        }
        if (this.getPassengers().size() < 2 && !this.isBaby()) {
            this.doPlayerRide($$0);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onElasticLeashPull() {
        super.onElasticLeashPull();
        if (this.isCamelSitting() && !this.isInPoseTransition() && this.canCamelChangePose()) {
            this.standUp();
        }
    }

    @Override
    public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.02, 0.48, 0.25, 0.82);
    }

    public boolean canCamelChangePose() {
        return this.wouldNotSuffocateAtTargetPose(this.isCamelSitting() ? Pose.STANDING : Pose.SITTING);
    }

    @Override
    protected boolean handleEating(Player $$0, ItemStack $$1) {
        boolean $$4;
        boolean $$3;
        boolean $$2;
        if (!this.isFood($$1)) {
            return false;
        }
        boolean bl = $$2 = this.getHealth() < this.getMaxHealth();
        if ($$2) {
            this.heal(2.0f);
        }
        boolean bl2 = $$3 = this.isTamed() && this.getAge() == 0 && this.canFallInLove();
        if ($$3) {
            this.setInLove($$0);
        }
        if ($$4 = this.isBaby()) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level().isClientSide()) {
                this.ageUp(10);
            }
        }
        if ($$2 || $$3 || $$4) {
            SoundEvent $$5;
            if (!this.isSilent() && ($$5 = this.getEatingSound()) != null) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), $$5, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            }
            this.gameEvent(GameEvent.EAT);
            return true;
        }
        return false;
    }

    @Override
    protected boolean canPerformRearing() {
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean canMate(Animal $$0) {
        if ($$0 == this) return false;
        if (!($$0 instanceof Camel)) return false;
        Camel $$1 = (Camel)$$0;
        if (!this.canParent()) return false;
        if (!$$1.canParent()) return false;
        return true;
    }

    @Override
    public @Nullable Camel getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return EntityType.CAMEL.create($$0, EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.CAMEL_EAT;
    }

    @Override
    protected void actuallyHurt(ServerLevel $$0, DamageSource $$1, float $$2) {
        this.standUpInstantly();
        super.actuallyHurt($$0, $$1, $$2);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity $$0, EntityDimensions $$1, float $$2) {
        int $$3 = Math.max(this.getPassengers().indexOf($$0), 0);
        boolean $$4 = $$3 == 0;
        float $$5 = 0.5f;
        float $$6 = (float)(this.isRemoved() ? (double)0.01f : this.getBodyAnchorAnimationYOffset($$4, 0.0f, $$1, $$2));
        if (this.getPassengers().size() > 1) {
            if (!$$4) {
                $$5 = -0.7f;
            }
            if ($$0 instanceof Animal) {
                $$5 += 0.2f;
            }
        }
        return new Vec3(0.0, $$6, $$5 * $$2).yRot(-this.getYRot() * ((float)Math.PI / 180));
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.45f : 1.0f;
    }

    private double getBodyAnchorAnimationYOffset(boolean $$0, float $$1, EntityDimensions $$2, float $$3) {
        double $$4 = $$2.height() - 0.375f * $$3;
        float $$5 = $$3 * 1.43f;
        float $$6 = $$5 - $$3 * 0.2f;
        float $$7 = $$5 - $$6;
        boolean $$8 = this.isInPoseTransition();
        boolean $$9 = this.isCamelSitting();
        if ($$8) {
            float $$14;
            int $$13;
            int $$10;
            int n = $$10 = $$9 ? 40 : 52;
            if ($$9) {
                int $$11 = 28;
                float $$12 = $$0 ? 0.5f : 0.1f;
            } else {
                $$13 = $$0 ? 24 : 32;
                $$14 = $$0 ? 0.6f : 0.35f;
            }
            float $$15 = Mth.clamp((float)this.getPoseTime() + $$1, 0.0f, (float)$$10);
            boolean $$16 = $$15 < (float)$$13;
            float $$17 = $$16 ? $$15 / (float)$$13 : ($$15 - (float)$$13) / (float)($$10 - $$13);
            float $$18 = $$5 - $$14 * $$6;
            $$4 += $$9 ? (double)Mth.lerp($$17, $$16 ? $$5 : $$18, $$16 ? $$18 : $$7) : (double)Mth.lerp($$17, $$16 ? $$7 - $$5 : $$7 - $$18, $$16 ? $$7 - $$18 : 0.0f);
        }
        if ($$9 && !$$8) {
            $$4 += (double)$$7;
        }
        return $$4;
    }

    @Override
    public Vec3 getLeashOffset(float $$0) {
        EntityDimensions $$1 = this.getDimensions(this.getPose());
        float $$2 = this.getAgeScale();
        return new Vec3(0.0, this.getBodyAnchorAnimationYOffset(true, $$0, $$1, $$2) - (double)(0.2f * $$2), $$1.width() * 0.56f);
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    protected boolean canAddPassenger(Entity $$0) {
        return this.getPassengers().size() <= 2;
    }

    public boolean isCamelSitting() {
        return this.entityData.get(LAST_POSE_CHANGE_TICK) < 0L;
    }

    public boolean isCamelVisuallySitting() {
        return this.getPoseTime() < 0L != this.isCamelSitting();
    }

    public boolean isInPoseTransition() {
        long $$0 = this.getPoseTime();
        return $$0 < (long)(this.isCamelSitting() ? 40 : 52);
    }

    private boolean isVisuallySittingDown() {
        return this.isCamelSitting() && this.getPoseTime() < 40L && this.getPoseTime() >= 0L;
    }

    public void sitDown() {
        if (this.isCamelSitting()) {
            return;
        }
        this.makeSound(this.getSitDownSound());
        this.setPose(Pose.SITTING);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.resetLastPoseChangeTick(-this.level().getGameTime());
    }

    public void standUp() {
        if (!this.isCamelSitting()) {
            return;
        }
        this.makeSound(this.getStandUpSound());
        this.setPose(Pose.STANDING);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.resetLastPoseChangeTick(this.level().getGameTime());
    }

    protected SoundEvent getStandUpSound() {
        return SoundEvents.CAMEL_STAND;
    }

    protected SoundEvent getSitDownSound() {
        return SoundEvents.CAMEL_SIT;
    }

    public void standUpInstantly() {
        this.setPose(Pose.STANDING);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.resetLastPoseChangeTickToFullStand(this.level().getGameTime());
    }

    @VisibleForTesting
    public void resetLastPoseChangeTick(long $$0) {
        this.entityData.set(LAST_POSE_CHANGE_TICK, $$0);
    }

    private void resetLastPoseChangeTickToFullStand(long $$0) {
        this.resetLastPoseChangeTick(Math.max(0L, $$0 - 52L - 1L));
    }

    public long getPoseTime() {
        return this.level().getGameTime() - Math.abs(this.entityData.get(LAST_POSE_CHANGE_TICK));
    }

    @Override
    protected Holder<SoundEvent> getEquipSound(EquipmentSlot $$0, ItemStack $$1, Equippable $$2) {
        if ($$0 == EquipmentSlot.SADDLE) {
            return this.getSaddleSound();
        }
        return super.getEquipSound($$0, $$1, $$2);
    }

    protected Holder.Reference<SoundEvent> getSaddleSound() {
        return SoundEvents.CAMEL_SADDLE;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (!this.firstTick && DASH.equals($$0)) {
            this.dashCooldown = this.dashCooldown == 0 ? 55 : this.dashCooldown;
        }
        super.onSyncedDataUpdated($$0);
    }

    @Override
    public boolean isTamed() {
        return true;
    }

    @Override
    public void openCustomInventoryScreen(Player $$0) {
        if (!this.level().isClientSide()) {
            $$0.openHorseInventory(this, this.inventory);
        }
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new CamelBodyRotationControl(this);
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    class CamelMoveControl
    extends MoveControl {
        public CamelMoveControl() {
            super(Camel.this);
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO && !Camel.this.isLeashed() && Camel.this.isCamelSitting() && !Camel.this.isInPoseTransition() && Camel.this.canCamelChangePose()) {
                Camel.this.standUp();
            }
            super.tick();
        }
    }

    class CamelLookControl
    extends LookControl {
        CamelLookControl() {
            super(Camel.this);
        }

        @Override
        public void tick() {
            if (!Camel.this.hasControllingPassenger()) {
                super.tick();
            }
        }
    }

    class CamelBodyRotationControl
    extends BodyRotationControl {
        public CamelBodyRotationControl(Camel $$0) {
            super($$0);
        }

        @Override
        public void clientTick() {
            if (!Camel.this.refuseToMove()) {
                super.clientTick();
            }
        }
    }
}

