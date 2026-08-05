/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.serialization.Dynamic
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Dynamic;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.AngerManagement;
import net.minecraft.world.entity.monster.warden.WardenAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class Warden
extends Monster
implements VibrationSystem {
    private static final int VIBRATION_COOLDOWN_TICKS = 40;
    private static final int TIME_TO_USE_MELEE_UNTIL_SONIC_BOOM = 200;
    private static final int MAX_HEALTH = 500;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3f;
    private static final float KNOCKBACK_RESISTANCE = 1.0f;
    private static final float ATTACK_KNOCKBACK = 1.5f;
    private static final int ATTACK_DAMAGE = 30;
    private static final int FOLLOW_RANGE = 24;
    private static final EntityDataAccessor<Integer> CLIENT_ANGER_LEVEL = SynchedEntityData.defineId(Warden.class, EntityDataSerializers.INT);
    private static final int DARKNESS_DISPLAY_LIMIT = 200;
    private static final int DARKNESS_DURATION = 260;
    private static final int DARKNESS_RADIUS = 20;
    private static final int DARKNESS_INTERVAL = 120;
    private static final int ANGERMANAGEMENT_TICK_DELAY = 20;
    private static final int DEFAULT_ANGER = 35;
    private static final int PROJECTILE_ANGER = 10;
    private static final int ON_HURT_ANGER_BOOST = 20;
    private static final int RECENT_PROJECTILE_TICK_THRESHOLD = 100;
    private static final int TOUCH_COOLDOWN_TICKS = 20;
    private static final int DIGGING_PARTICLES_AMOUNT = 30;
    private static final float DIGGING_PARTICLES_DURATION = 4.5f;
    private static final float DIGGING_PARTICLES_OFFSET = 0.7f;
    private static final int PROJECTILE_ANGER_DISTANCE = 30;
    private int tendrilAnimation;
    private int tendrilAnimationO;
    private int heartAnimation;
    private int heartAnimationO;
    public AnimationState roarAnimationState = new AnimationState();
    public AnimationState sniffAnimationState = new AnimationState();
    public AnimationState emergeAnimationState = new AnimationState();
    public AnimationState diggingAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState sonicBoomAnimationState = new AnimationState();
    private final DynamicGameEventListener<VibrationSystem.Listener> dynamicGameEventListener;
    private final VibrationSystem.User vibrationUser;
    private VibrationSystem.Data vibrationData;
    AngerManagement angerManagement = new AngerManagement(this::canTargetEntity, Collections.emptyList());

    public Warden(EntityType<? extends Monster> $$0, Level $$1) {
        super($$0, $$1);
        this.vibrationUser = new VibrationUser();
        this.vibrationData = new VibrationSystem.Data();
        this.dynamicGameEventListener = new DynamicGameEventListener<VibrationSystem.Listener>(new VibrationSystem.Listener(this));
        this.xpReward = 5;
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0f);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 8.0f);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 8.0f);
        this.setPathfindingMalus(PathType.LAVA, 8.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0f);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0f);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
        return new ClientboundAddEntityPacket((Entity)this, $$0, this.hasPose(Pose.EMERGING) ? 1 : 0);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
        super.recreateFromPacket($$0);
        if ($$0.getData() == 1) {
            this.setPose(Pose.EMERGING);
        }
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader $$0) {
        return super.checkSpawnObstruction($$0) && $$0.noCollision(this, this.getType().getDimensions().makeBoundingBox(this.position()));
    }

    @Override
    public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
        return 0.0f;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel $$0, DamageSource $$1) {
        if (this.isDiggingOrEmerging() && !$$1.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo($$0, $$1);
    }

    boolean isDiggingOrEmerging() {
        return this.hasPose(Pose.DIGGING) || this.hasPose(Pose.EMERGING);
    }

    @Override
    protected boolean canRide(Entity $$0) {
        return false;
    }

    @Override
    public float getSecondsToDisableBlocking() {
        return 5.0f;
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.55f;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 500.0).add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ATTACK_KNOCKBACK, 1.5).add(Attributes.ATTACK_DAMAGE, 30.0).add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public boolean dampensVibrations() {
        return true;
    }

    @Override
    protected float getSoundVolume() {
        return 4.0f;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.hasPose(Pose.ROARING) || this.isDiggingOrEmerging()) {
            return null;
        }
        return this.getAngerLevel().getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.WARDEN_STEP, 10.0f, 1.0f);
    }

    @Override
    public boolean doHurtTarget(ServerLevel $$0, Entity $$1) {
        $$0.broadcastEntityEvent(this, (byte)4);
        this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 10.0f, this.getVoicePitch());
        SonicBoom.setCooldown(this, 40);
        return super.doHurtTarget($$0, $$1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(CLIENT_ANGER_LEVEL, 0);
    }

    public int getClientAngerLevel() {
        return this.entityData.get(CLIENT_ANGER_LEVEL);
    }

    private void syncClientAngerLevel() {
        this.entityData.set(CLIENT_ANGER_LEVEL, this.getActiveAnger());
    }

    @Override
    public void tick() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$0 = (ServerLevel)level;
            VibrationSystem.Ticker.tick($$0, this.vibrationData, this.vibrationUser);
            if (this.isPersistenceRequired() || this.requiresCustomPersistence()) {
                WardenAi.setDigCooldown(this);
            }
        }
        super.tick();
        if (this.level().isClientSide()) {
            if (this.tickCount % this.getHeartBeatDelay() == 0) {
                this.heartAnimation = 10;
                if (!this.isSilent()) {
                    this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, this.getSoundSource(), 5.0f, this.getVoicePitch(), false);
                }
            }
            this.tendrilAnimationO = this.tendrilAnimation;
            if (this.tendrilAnimation > 0) {
                --this.tendrilAnimation;
            }
            this.heartAnimationO = this.heartAnimation;
            if (this.heartAnimation > 0) {
                --this.heartAnimation;
            }
            switch (this.getPose()) {
                case EMERGING: {
                    this.clientDiggingParticles(this.emergeAnimationState);
                    break;
                }
                case DIGGING: {
                    this.clientDiggingParticles(this.diggingAnimationState);
                }
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        ProfilerFiller $$1 = Profiler.get();
        $$1.push("wardenBrain");
        this.getBrain().tick($$0, this);
        $$1.pop();
        super.customServerAiStep($$0);
        if ((this.tickCount + this.getId()) % 120 == 0) {
            Warden.applyDarknessAround($$0, this.position(), this, 20);
        }
        if (this.tickCount % 20 == 0) {
            this.angerManagement.tick($$0, this::canTargetEntity);
            this.syncClientAngerLevel();
        }
        WardenAi.updateActivity(this);
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 4) {
            this.roarAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
        } else if ($$0 == 61) {
            this.tendrilAnimation = 10;
        } else if ($$0 == 62) {
            this.sonicBoomAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent($$0);
        }
    }

    private int getHeartBeatDelay() {
        float $$0 = (float)this.getClientAngerLevel() / (float)AngerLevel.ANGRY.getMinimumAnger();
        return 40 - Mth.floor(Mth.clamp($$0, 0.0f, 1.0f) * 30.0f);
    }

    public float getTendrilAnimation(float $$0) {
        return Mth.lerp($$0, this.tendrilAnimationO, this.tendrilAnimation) / 10.0f;
    }

    public float getHeartAnimation(float $$0) {
        return Mth.lerp($$0, this.heartAnimationO, this.heartAnimation) / 10.0f;
    }

    private void clientDiggingParticles(AnimationState $$0) {
        if ((float)$$0.getTimeInMillis(this.tickCount) < 4500.0f) {
            RandomSource $$1 = this.getRandom();
            BlockState $$2 = this.getBlockStateOn();
            if ($$2.getRenderShape() != RenderShape.INVISIBLE) {
                for (int $$3 = 0; $$3 < 30; ++$$3) {
                    double $$4 = this.getX() + (double)Mth.randomBetween($$1, -0.7f, 0.7f);
                    double $$5 = this.getY();
                    double $$6 = this.getZ() + (double)Mth.randomBetween($$1, -0.7f, 0.7f);
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, $$2), $$4, $$5, $$6, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (DATA_POSE.equals($$0)) {
            switch (this.getPose()) {
                case ROARING: {
                    this.roarAnimationState.start(this.tickCount);
                    break;
                }
                case SNIFFING: {
                    this.sniffAnimationState.start(this.tickCount);
                    break;
                }
                case EMERGING: {
                    this.emergeAnimationState.start(this.tickCount);
                    break;
                }
                case DIGGING: {
                    this.diggingAnimationState.start(this.tickCount);
                }
            }
        }
        super.onSyncedDataUpdated($$0);
    }

    @Override
    public boolean ignoreExplosion(Explosion $$0) {
        return this.isDiggingOrEmerging();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return WardenAi.makeBrain(this, $$0);
    }

    public Brain<Warden> getBrain() {
        return super.getBrain();
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> $$0) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            $$0.accept(this.dynamicGameEventListener, $$1);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Contract(value="null->false")
    public boolean canTargetEntity(@Nullable Entity $$0) {
        if (!($$0 instanceof LivingEntity)) return false;
        LivingEntity $$1 = (LivingEntity)$$0;
        if (this.level() != $$0.level()) return false;
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test($$0)) return false;
        if (this.isAlliedTo($$0)) return false;
        if ($$1.getType() == EntityType.ARMOR_STAND) return false;
        if ($$1.getType() == EntityType.WARDEN) return false;
        if ($$1.isInvulnerable()) return false;
        if ($$1.isDeadOrDying()) return false;
        if (!this.level().getWorldBorder().isWithinBounds($$1.getBoundingBox())) return false;
        return true;
    }

    public static void applyDarknessAround(ServerLevel $$0, Vec3 $$1, @Nullable Entity $$2, int $$3) {
        MobEffectInstance $$4 = new MobEffectInstance(MobEffects.DARKNESS, 260, 0, false, false);
        MobEffectUtil.addEffectToPlayersAround($$0, $$2, $$1, $$3, $$4, 200);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("anger", AngerManagement.codec(this::canTargetEntity), this.angerManagement);
        $$0.store("listener", VibrationSystem.Data.CODEC, this.vibrationData);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.angerManagement = $$0.read("anger", AngerManagement.codec(this::canTargetEntity)).orElseGet(() -> new AngerManagement(this::canTargetEntity, Collections.emptyList()));
        this.syncClientAngerLevel();
        this.vibrationData = $$0.read("listener", VibrationSystem.Data.CODEC).orElseGet(VibrationSystem.Data::new);
    }

    private void playListeningSound() {
        if (!this.hasPose(Pose.ROARING)) {
            this.playSound(this.getAngerLevel().getListeningSound(), 10.0f, this.getVoicePitch());
        }
    }

    public AngerLevel getAngerLevel() {
        return AngerLevel.byAnger(this.getActiveAnger());
    }

    private int getActiveAnger() {
        return this.angerManagement.getActiveAnger(this.getTarget());
    }

    public void clearAnger(Entity $$0) {
        this.angerManagement.clearAnger($$0);
    }

    public void increaseAngerAt(@Nullable Entity $$0) {
        this.increaseAngerAt($$0, 35, true);
    }

    @VisibleForTesting
    public void increaseAngerAt(@Nullable Entity $$0, int $$1, boolean $$2) {
        if (!this.isNoAi() && this.canTargetEntity($$0)) {
            WardenAi.setDigCooldown(this);
            boolean $$3 = !(this.getTarget() instanceof Player);
            int $$4 = this.angerManagement.increaseAnger($$0, $$1);
            if ($$0 instanceof Player && $$3 && AngerLevel.byAnger($$4).isAngry()) {
                this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
            if ($$2) {
                this.playListeningSound();
            }
        }
    }

    public Optional<LivingEntity> getEntityAngryAt() {
        if (this.getAngerLevel().isAngry()) {
            return this.angerManagement.getActiveEntity();
        }
        return Optional.empty();
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }

    @Override
    public boolean removeWhenFarAway(double $$0) {
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        this.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        if ($$2 == EntitySpawnReason.TRIGGERED) {
            this.setPose(Pose.EMERGING);
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, WardenAi.EMERGE_DURATION);
            this.playSound(SoundEvents.WARDEN_AGITATED, 5.0f, 1.0f);
        }
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        boolean $$3 = super.hurtServer($$0, $$1, $$2);
        if (!this.isNoAi() && !this.isDiggingOrEmerging()) {
            Entity $$4 = $$1.getEntity();
            this.increaseAngerAt($$4, AngerLevel.ANGRY.getMinimumAnger() + 20, false);
            if (this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty() && $$4 instanceof LivingEntity) {
                LivingEntity $$5 = (LivingEntity)$$4;
                if ($$1.isDirect() || this.closerThan($$5, 5.0)) {
                    this.setAttackTarget($$5);
                }
            }
        }
        return $$3;
    }

    public void setAttackTarget(LivingEntity $$0) {
        this.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
        this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, $$0);
        this.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        SonicBoom.setCooldown(this, 200);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        EntityDimensions $$1 = super.getDefaultDimensions($$0);
        if (this.isDiggingOrEmerging()) {
            return EntityDimensions.fixed($$1.width(), 1.0f);
        }
        return $$1;
    }

    @Override
    public boolean isPushable() {
        return !this.isDiggingOrEmerging() && super.isPushable();
    }

    @Override
    protected void doPush(Entity $$0) {
        if (!this.isNoAi() && !this.getBrain().hasMemoryValue(MemoryModuleType.TOUCH_COOLDOWN)) {
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.TOUCH_COOLDOWN, Unit.INSTANCE, 20L);
            this.increaseAngerAt($$0);
            WardenAi.setDisturbanceLocation(this, $$0.blockPosition());
        }
        super.doPush($$0);
    }

    @VisibleForTesting
    public AngerManagement getAngerManagement() {
        return this.angerManagement;
    }

    @Override
    protected PathNavigation createNavigation(Level $$0) {
        return new GroundPathNavigation(this, this, $$0){

            @Override
            protected PathFinder createPathFinder(int $$0) {
                this.nodeEvaluator = new WalkNodeEvaluator();
                return new PathFinder(this, this.nodeEvaluator, $$0){

                    @Override
                    protected float distance(Node $$0, Node $$1) {
                        return $$0.distanceToXZ($$1);
                    }
                };
            }
        };
    }

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    class VibrationUser
    implements VibrationSystem.User {
        private static final int GAME_EVENT_LISTENER_RANGE = 16;
        private final PositionSource positionSource;

        VibrationUser() {
            this.positionSource = new EntityPositionSource(Warden.this, Warden.this.getEyeHeight());
        }

        @Override
        public int getListenerRadius() {
            return 16;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.WARDEN_CAN_LISTEN;
        }

        @Override
        public boolean canTriggerAvoidVibration() {
            return true;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel $$0, BlockPos $$1, Holder<GameEvent> $$2, GameEvent.Context $$3) {
            LivingEntity $$4;
            if (Warden.this.isNoAi() || Warden.this.isDeadOrDying() || Warden.this.getBrain().hasMemoryValue(MemoryModuleType.VIBRATION_COOLDOWN) || Warden.this.isDiggingOrEmerging() || !$$0.getWorldBorder().isWithinBounds($$1)) {
                return false;
            }
            Entity entity = $$3.sourceEntity();
            return !(entity instanceof LivingEntity) || Warden.this.canTargetEntity($$4 = (LivingEntity)entity);
        }

        @Override
        public void onReceiveVibration(ServerLevel $$0, BlockPos $$1, Holder<GameEvent> $$2, @Nullable Entity $$3, @Nullable Entity $$4, float $$5) {
            if (Warden.this.isDeadOrDying()) {
                return;
            }
            Warden.this.brain.setMemoryWithExpiry(MemoryModuleType.VIBRATION_COOLDOWN, Unit.INSTANCE, 40L);
            $$0.broadcastEntityEvent(Warden.this, (byte)61);
            Warden.this.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 5.0f, Warden.this.getVoicePitch());
            BlockPos $$6 = $$1;
            if ($$4 != null) {
                if (Warden.this.closerThan($$4, 30.0)) {
                    if (Warden.this.getBrain().hasMemoryValue(MemoryModuleType.RECENT_PROJECTILE)) {
                        if (Warden.this.canTargetEntity($$4)) {
                            $$6 = $$4.blockPosition();
                        }
                        Warden.this.increaseAngerAt($$4);
                    } else {
                        Warden.this.increaseAngerAt($$4, 10, true);
                    }
                }
                Warden.this.getBrain().setMemoryWithExpiry(MemoryModuleType.RECENT_PROJECTILE, Unit.INSTANCE, 100L);
            } else {
                Warden.this.increaseAngerAt($$3);
            }
            if (!Warden.this.getAngerLevel().isAngry()) {
                Optional<LivingEntity> $$7 = Warden.this.angerManagement.getActiveEntity();
                if ($$4 != null || $$7.isEmpty() || $$7.get() == $$3) {
                    WardenAi.setDisturbanceLocation(Warden.this, $$6);
                }
            }
        }
    }
}

