/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.happyghast;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.happyghast.HappyGhastAi;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class HappyGhast
extends Animal {
    public static final float BABY_SCALE = 0.2375f;
    public static final int WANDER_GROUND_DISTANCE = 16;
    public static final int SMALL_RESTRICTION_RADIUS = 32;
    public static final int LARGE_RESTRICTION_RADIUS = 64;
    public static final int RESTRICTION_RADIUS_BUFFER = 16;
    public static final int FAST_HEALING_TICKS = 20;
    public static final int SLOW_HEALING_TICKS = 600;
    public static final int MAX_PASSANGERS = 4;
    private static final int STILL_TIMEOUT_ON_LOAD_GRACE_PERIOD = 60;
    private static final int MAX_STILL_TIMEOUT = 10;
    public static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0f;
    private int leashHolderTime = 0;
    private int serverStillTimeout;
    private static final EntityDataAccessor<Boolean> IS_LEASH_HOLDER = SynchedEntityData.defineId(HappyGhast.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STAYS_STILL = SynchedEntityData.defineId(HappyGhast.class, EntityDataSerializers.BOOLEAN);
    private static final float MAX_SCALE = 1.0f;

    public HappyGhast(EntityType<? extends HappyGhast> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
        this.moveControl = new Ghast.GhastMoveControl(this, true, this::isOnStillTimeout);
        this.lookControl = new HappyGhastLookControl();
    }

    private void setServerStillTimeout(int $$0) {
        Level level;
        if (this.serverStillTimeout <= 0 && $$0 > 0 && (level = this.level()) instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
            $$1.getChunkSource().chunkMap.sendToTrackingPlayers(this, ClientboundEntityPositionSyncPacket.of(this));
        }
        this.serverStillTimeout = $$0;
        this.syncStayStillFlag();
    }

    private PathNavigation createBabyNavigation(Level $$0) {
        return new BabyFlyingPathNavigation(this, $$0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new HappyGhastFloatGoal());
        this.goalSelector.addGoal(4, new TemptGoal.ForNonPathfinders((Mob)this, 1.0, $$0 -> this.isWearingBodyArmor() || this.isBaby() ? $$0.is(ItemTags.HAPPY_GHAST_FOOD) : $$0.is(ItemTags.HAPPY_GHAST_TEMPT_ITEMS), false, 7.0));
        this.goalSelector.addGoal(5, new Ghast.RandomFloatAroundGoal(this, 16));
    }

    private void adultGhastSetup() {
        this.moveControl = new Ghast.GhastMoveControl(this, true, this::isOnStillTimeout);
        this.lookControl = new HappyGhastLookControl();
        this.navigation = this.createNavigation(this.level());
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$02 = (ServerLevel)level;
            this.removeAllGoals($$0 -> true);
            this.registerGoals();
            this.brain.stopAll($$02, this);
            this.brain.clearMemories();
        }
    }

    private void babyGhastSetup() {
        this.moveControl = new FlyingMoveControl(this, 180, true);
        this.lookControl = new LookControl(this);
        this.navigation = this.createBabyNavigation(this.level());
        this.setServerStillTimeout(0);
        this.removeAllGoals($$0 -> true);
    }

    @Override
    protected void ageBoundaryReached() {
        if (this.isBaby()) {
            this.babyGhastSetup();
        } else {
            this.adultGhastSetup();
        }
        super.ageBoundaryReached();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.TEMPT_RANGE, 16.0).add(Attributes.FLYING_SPEED, 0.05).add(Attributes.MOVEMENT_SPEED, 0.05).add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.CAMERA_DISTANCE, 8.0);
    }

    @Override
    protected float sanitizeScale(float $$0) {
        return Math.min($$0, 1.0f);
    }

    @Override
    protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void travel(Vec3 $$0) {
        float $$1 = (float)this.getAttributeValue(Attributes.FLYING_SPEED) * 5.0f / 3.0f;
        this.travelFlying($$0, $$1, $$1, $$1);
    }

    @Override
    public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
        if (!$$1.isEmptyBlock($$0)) {
            return 0.0f;
        }
        if ($$1.isEmptyBlock($$0.below()) && !$$1.isEmptyBlock($$0.below(2))) {
            return 10.0f;
        }
        return 5.0f;
    }

    @Override
    public boolean canBreatheUnderwater() {
        if (this.isBaby()) {
            return true;
        }
        return super.canBreatheUnderwater();
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    public int getAmbientSoundInterval() {
        int $$0 = super.getAmbientSoundInterval();
        if (this.isVehicle()) {
            return $$0 * 6;
        }
        return $$0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? SoundEvents.GHASTLING_AMBIENT : SoundEvents.HAPPY_GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return this.isBaby() ? SoundEvents.GHASTLING_HURT : SoundEvents.HAPPY_GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isBaby() ? SoundEvents.GHASTLING_DEATH : SoundEvents.HAPPY_GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return this.isBaby() ? 1.0f : 4.0f;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return EntityType.HAPPY_GHAST.create($$0, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.2375f : 1.0f;
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.HAPPY_GHAST_FOOD);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot $$0) {
        if ($$0 == EquipmentSlot.BODY) {
            return this.isAlive() && !this.isBaby();
        }
        return super.canUseSlot($$0);
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot $$0) {
        return $$0 == EquipmentSlot.BODY;
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        InteractionResult $$3;
        if (this.isBaby()) {
            return super.mobInteract($$0, $$1);
        }
        ItemStack $$2 = $$0.getItemInHand($$1);
        if (!$$2.isEmpty() && ($$3 = $$2.interactLivingEntity($$0, this, $$1)).consumesAction()) {
            return $$3;
        }
        if (this.isWearingBodyArmor() && !$$0.isSecondaryUseActive()) {
            this.doPlayerRide($$0);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract($$0, $$1);
    }

    private void doPlayerRide(Player $$0) {
        if (!this.level().isClientSide()) {
            $$0.startRiding(this);
        }
    }

    @Override
    protected void addPassenger(Entity $$0) {
        if (!this.isVehicle()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HARNESS_GOGGLES_DOWN, this.getSoundSource(), 1.0f, 1.0f);
        }
        super.addPassenger($$0);
        if (!this.level().isClientSide()) {
            if (!this.scanPlayerAboveGhast()) {
                this.setServerStillTimeout(0);
            } else if (this.serverStillTimeout > 10) {
                this.setServerStillTimeout(10);
            }
        }
    }

    @Override
    protected void removePassenger(Entity $$0) {
        super.removePassenger($$0);
        if (!this.level().isClientSide()) {
            this.setServerStillTimeout(10);
        }
        if (!this.isVehicle()) {
            this.clearHome();
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HARNESS_GOGGLES_UP, this.getSoundSource(), 1.0f, 1.0f);
        }
    }

    @Override
    protected boolean canAddPassenger(Entity $$0) {
        return this.getPassengers().size() < 4;
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Entity $$0 = this.getFirstPassenger();
        if (this.isWearingBodyArmor() && !this.isOnStillTimeout() && $$0 instanceof Player) {
            Player $$1 = (Player)$$0;
            return $$1;
        }
        return super.getControllingPassenger();
    }

    @Override
    protected Vec3 getRiddenInput(Player $$0, Vec3 $$1) {
        float $$2 = $$0.xxa;
        float $$3 = 0.0f;
        float $$4 = 0.0f;
        if ($$0.zza != 0.0f) {
            float $$5 = Mth.cos($$0.getXRot() * ((float)Math.PI / 180));
            float $$6 = -Mth.sin($$0.getXRot() * ((float)Math.PI / 180));
            if ($$0.zza < 0.0f) {
                $$5 *= -0.5f;
                $$6 *= -0.5f;
            }
            $$4 = $$6;
            $$3 = $$5;
        }
        if ($$0.isJumping()) {
            $$4 += 0.5f;
        }
        return new Vec3($$2, $$4, $$3).scale((double)3.9f * this.getAttributeValue(Attributes.FLYING_SPEED));
    }

    protected Vec2 getRiddenRotation(LivingEntity $$0) {
        return new Vec2($$0.getXRot() * 0.5f, $$0.getYRot());
    }

    @Override
    protected void tickRidden(Player $$0, Vec3 $$1) {
        super.tickRidden($$0, $$1);
        Vec2 $$2 = this.getRiddenRotation($$0);
        float $$3 = this.getYRot();
        float $$4 = Mth.wrapDegrees($$2.y - $$3);
        float $$5 = 0.08f;
        this.setRot($$3 += $$4 * 0.08f, $$2.x);
        this.yBodyRot = this.yHeadRot = $$3;
        this.yRotO = this.yHeadRot;
    }

    protected Brain.Provider<HappyGhast> brainProvider() {
        return HappyGhastAi.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return HappyGhastAi.makeBrain(this.brainProvider().makeBrain($$0));
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        if (this.isBaby()) {
            ProfilerFiller $$1 = Profiler.get();
            $$1.push("happyGhastBrain");
            this.brain.tick($$0, this);
            $$1.pop();
            $$1.push("happyGhastActivityUpdate");
            HappyGhastAi.updateActivity(this);
            $$1.pop();
        }
        this.checkRestriction();
        super.customServerAiStep($$0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.leashHolderTime > 0) {
            --this.leashHolderTime;
        }
        this.setLeashHolder(this.leashHolderTime > 0);
        if (this.serverStillTimeout > 0) {
            if (this.tickCount > 60) {
                --this.serverStillTimeout;
            }
            this.setServerStillTimeout(this.serverStillTimeout);
        }
        if (this.scanPlayerAboveGhast()) {
            this.setServerStillTimeout(10);
        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide()) {
            this.setRequiresPrecisePosition(this.isOnStillTimeout());
        }
        super.aiStep();
        this.continuousHeal();
    }

    private int getHappyGhastRestrictionRadius() {
        if (!this.isBaby() && this.getItemBySlot(EquipmentSlot.BODY).isEmpty()) {
            return 64;
        }
        return 32;
    }

    private void checkRestriction() {
        if (this.isLeashed() || this.isVehicle()) {
            return;
        }
        int $$0 = this.getHappyGhastRestrictionRadius();
        if (this.hasHome() && this.getHomePosition().closerThan(this.blockPosition(), $$0 + 16) && $$0 == this.getHomeRadius()) {
            return;
        }
        this.setHomeTo(this.blockPosition(), $$0);
    }

    /*
     * WARNING - void declaration
     */
    private void continuousHeal() {
        void $$1;
        block5: {
            block4: {
                Level level = this.level();
                if (!(level instanceof ServerLevel)) break block4;
                ServerLevel $$0 = (ServerLevel)level;
                if (this.isAlive() && this.deathTime == 0 && this.getMaxHealth() != this.getHealth()) break block5;
            }
            return;
        }
        boolean $$2 = this.isInClouds() || $$1.precipitationAt(this.blockPosition()) != Biome.Precipitation.NONE;
        if (this.tickCount % ($$2 ? 20 : 600) == 0) {
            this.heal(1.0f);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(IS_LEASH_HOLDER, false);
        $$0.define(STAYS_STILL, false);
    }

    private void setLeashHolder(boolean $$0) {
        this.entityData.set(IS_LEASH_HOLDER, $$0);
    }

    public boolean isLeashHolder() {
        return this.entityData.get(IS_LEASH_HOLDER);
    }

    private void syncStayStillFlag() {
        this.entityData.set(STAYS_STILL, this.serverStillTimeout > 0);
    }

    public boolean staysStill() {
        return this.entityData.get(STAYS_STILL);
    }

    @Override
    public boolean supportQuadLeashAsHolder() {
        return true;
    }

    @Override
    public Vec3[] getQuadLeashHolderOffsets() {
        return Leashable.createQuadLeashOffsets(this, -0.03125, 0.4375, 0.46875, 0.03125);
    }

    @Override
    public Vec3 getLeashOffset() {
        return Vec3.ZERO;
    }

    @Override
    public double leashElasticDistance() {
        return 10.0;
    }

    @Override
    public double leashSnapDistance() {
        return 16.0;
    }

    @Override
    public void onElasticLeashPull() {
        super.onElasticLeashPull();
        this.getMoveControl().setWait();
    }

    @Override
    public void notifyLeashHolder(Leashable $$0) {
        if ($$0.supportQuadLeash()) {
            this.leashHolderTime = 5;
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putInt("still_timeout", this.serverStillTimeout);
    }

    @Override
    public void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setServerStillTimeout($$0.getIntOr("still_timeout", 0));
    }

    public boolean isOnStillTimeout() {
        return this.staysStill() || this.serverStillTimeout > 0;
    }

    private boolean scanPlayerAboveGhast() {
        AABB $$0 = this.getBoundingBox();
        AABB $$1 = new AABB($$0.minX - 1.0, $$0.maxY - (double)1.0E-5f, $$0.minZ - 1.0, $$0.maxX + 1.0, $$0.maxY + $$0.getYsize() / 2.0, $$0.maxZ + 1.0);
        for (Player player : this.level().players()) {
            Entity $$3;
            if (player.isSpectator() || ($$3 = player.getRootVehicle()) instanceof HappyGhast || !$$1.contains($$3.position())) continue;
            return true;
        }
        return false;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new HappyGhastBodyRotationControl();
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity $$0) {
        if (this.isBaby() || !this.isAlive()) {
            return false;
        }
        if (this.level().isClientSide() && $$0 instanceof Player && $$0.position().y >= this.getBoundingBox().maxY) {
            return true;
        }
        if (this.isVehicle() && $$0 instanceof HappyGhast) {
            return true;
        }
        return this.isOnStillTimeout();
    }

    @Override
    public boolean isFlyingVehicle() {
        return !this.isBaby();
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity $$0) {
        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    class HappyGhastLookControl
    extends LookControl {
        HappyGhastLookControl() {
            super(HappyGhast.this);
        }

        @Override
        public void tick() {
            if (HappyGhast.this.isOnStillTimeout()) {
                float $$0 = HappyGhastLookControl.wrapDegrees90(HappyGhast.this.getYRot());
                HappyGhast.this.setYRot(HappyGhast.this.getYRot() - $$0);
                HappyGhast.this.setYHeadRot(HappyGhast.this.getYRot());
                return;
            }
            if (this.lookAtCooldown > 0) {
                --this.lookAtCooldown;
                double $$1 = this.wantedX - HappyGhast.this.getX();
                double $$2 = this.wantedZ - HappyGhast.this.getZ();
                HappyGhast.this.setYRot(-((float)Mth.atan2($$1, $$2)) * 57.295776f);
                HappyGhast.this.yHeadRot = HappyGhast.this.yBodyRot = HappyGhast.this.getYRot();
                return;
            }
            Ghast.faceMovementDirection(this.mob);
        }

        public static float wrapDegrees90(float $$0) {
            float $$1 = $$0 % 90.0f;
            if ($$1 >= 45.0f) {
                $$1 -= 90.0f;
            }
            if ($$1 < -45.0f) {
                $$1 += 90.0f;
            }
            return $$1;
        }
    }

    static class BabyFlyingPathNavigation
    extends FlyingPathNavigation {
        public BabyFlyingPathNavigation(HappyGhast $$0, Level $$1) {
            super($$0, $$1);
            this.setCanOpenDoors(false);
            this.setCanFloat(true);
            this.setRequiredPathLength(48.0f);
        }

        @Override
        protected boolean canMoveDirectly(Vec3 $$0, Vec3 $$1) {
            return BabyFlyingPathNavigation.isClearForMovementBetween(this.mob, $$0, $$1, false);
        }
    }

    class HappyGhastFloatGoal
    extends FloatGoal {
        public HappyGhastFloatGoal() {
            super(HappyGhast.this);
        }

        @Override
        public boolean canUse() {
            return !HappyGhast.this.isOnStillTimeout() && super.canUse();
        }
    }

    class HappyGhastBodyRotationControl
    extends BodyRotationControl {
        public HappyGhastBodyRotationControl() {
            super(HappyGhast.this);
        }

        @Override
        public void clientTick() {
            if (HappyGhast.this.isVehicle()) {
                HappyGhast.this.yBodyRot = HappyGhast.this.yHeadRot = HappyGhast.this.getYRot();
            }
            super.clientTick();
        }
    }
}

