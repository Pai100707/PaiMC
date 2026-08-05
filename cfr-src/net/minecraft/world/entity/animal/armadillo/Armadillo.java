/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.armadillo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.armadillo.ArmadilloAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public class Armadillo
extends Animal {
    public static final float BABY_SCALE = 0.6f;
    public static final float MAX_HEAD_ROTATION_EXTENT = 32.5f;
    public static final int SCARE_CHECK_INTERVAL = 80;
    private static final double SCARE_DISTANCE_HORIZONTAL = 7.0;
    private static final double SCARE_DISTANCE_VERTICAL = 2.0;
    private static final EntityDataAccessor<ArmadilloState> ARMADILLO_STATE = SynchedEntityData.defineId(Armadillo.class, EntityDataSerializers.ARMADILLO_STATE);
    private long inStateTicks = 0L;
    public final AnimationState rollOutAnimationState = new AnimationState();
    public final AnimationState rollUpAnimationState = new AnimationState();
    public final AnimationState peekAnimationState = new AnimationState();
    private int scuteTime;
    private boolean peekReceivedClient = false;

    public Armadillo(EntityType<? extends Animal> $$0, Level $$1) {
        super($$0, $$1);
        this.getNavigation().setCanFloat(true);
        this.scuteTime = this.pickNextScuteDropTime();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return EntityType.ARMADILLO.create($$0, EntitySpawnReason.BREEDING);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.14);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(ARMADILLO_STATE, ArmadilloState.IDLE);
    }

    public boolean isScared() {
        return this.entityData.get(ARMADILLO_STATE) != ArmadilloState.IDLE;
    }

    public boolean shouldHideInShell() {
        return this.getState().shouldHideInShell(this.inStateTicks);
    }

    public boolean shouldSwitchToScaredState() {
        return this.getState() == ArmadilloState.ROLLING && this.inStateTicks > (long)ArmadilloState.ROLLING.animationDuration();
    }

    public ArmadilloState getState() {
        return this.entityData.get(ARMADILLO_STATE);
    }

    public void switchToState(ArmadilloState $$0) {
        this.entityData.set(ARMADILLO_STATE, $$0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (ARMADILLO_STATE.equals($$0)) {
            this.inStateTicks = 0L;
        }
        super.onSyncedDataUpdated($$0);
    }

    protected Brain.Provider<Armadillo> brainProvider() {
        return ArmadilloAi.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return ArmadilloAi.makeBrain(this.brainProvider().makeBrain($$0));
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        ProfilerFiller $$1 = Profiler.get();
        $$1.push("armadilloBrain");
        this.brain.tick($$0, this);
        $$1.pop();
        $$1.push("armadilloActivityUpdate");
        ArmadilloAi.updateActivity(this);
        $$1.pop();
        if (this.isAlive() && --this.scuteTime <= 0 && this.shouldDropLoot($$0)) {
            if (this.dropFromGiftLootTable($$0, BuiltInLootTables.ARMADILLO_SHED, this::spawnAtLocation)) {
                this.playSound(SoundEvents.ARMADILLO_SCUTE_DROP, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                this.gameEvent(GameEvent.ENTITY_PLACE);
            }
            this.scuteTime = this.pickNextScuteDropTime();
        }
        super.customServerAiStep($$0);
    }

    private int pickNextScuteDropTime() {
        return this.random.nextInt(20 * TimeUtil.SECONDS_PER_MINUTE * 5) + 20 * TimeUtil.SECONDS_PER_MINUTE * 5;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
        if (this.isScared()) {
            this.clampHeadRotationToBody();
        }
        ++this.inStateTicks;
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.6f : 1.0f;
    }

    private void setupAnimationStates() {
        switch (this.getState().ordinal()) {
            case 0: {
                this.rollOutAnimationState.stop();
                this.rollUpAnimationState.stop();
                this.peekAnimationState.stop();
                break;
            }
            case 3: {
                this.rollOutAnimationState.startIfStopped(this.tickCount);
                this.rollUpAnimationState.stop();
                this.peekAnimationState.stop();
                break;
            }
            case 1: {
                this.rollOutAnimationState.stop();
                this.rollUpAnimationState.startIfStopped(this.tickCount);
                this.peekAnimationState.stop();
                break;
            }
            case 2: {
                this.rollOutAnimationState.stop();
                this.rollUpAnimationState.stop();
                if (this.peekReceivedClient) {
                    this.peekAnimationState.stop();
                    this.peekReceivedClient = false;
                }
                if (this.inStateTicks == 0L) {
                    this.peekAnimationState.start(this.tickCount);
                    this.peekAnimationState.fastForward(ArmadilloState.SCARED.animationDuration(), 1.0f);
                    break;
                }
                this.peekAnimationState.startIfStopped(this.tickCount);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 64 && this.level().isClientSide()) {
            this.peekReceivedClient = true;
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMADILLO_PEEK, this.getSoundSource(), 1.0f, 1.0f, false);
        } else {
            super.handleEntityEvent($$0);
        }
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.ARMADILLO_FOOD);
    }

    public static boolean checkArmadilloSpawnRules(EntityType<Armadillo> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getBlockState($$3.below()).is(BlockTags.ARMADILLO_SPAWNABLE_ON) && Armadillo.isBrightEnoughToSpawn($$1, $$3);
    }

    public boolean isScaredBy(LivingEntity $$0) {
        if (!this.getBoundingBox().inflate(7.0, 2.0, 7.0).intersects($$0.getBoundingBox())) {
            return false;
        }
        if ($$0.getType().is(EntityTypeTags.UNDEAD)) {
            return true;
        }
        if (this.getLastHurtByMob() == $$0) {
            return true;
        }
        if ($$0 instanceof Player) {
            Player $$1 = (Player)$$0;
            if ($$1.isSpectator()) {
                return false;
            }
            return $$1.isSprinting() || $$1.isPassenger();
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("state", ArmadilloState.CODEC, this.getState());
        $$0.putInt("scute_time", this.scuteTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$02) {
        super.readAdditionalSaveData($$02);
        this.switchToState($$02.read("state", ArmadilloState.CODEC).orElse(ArmadilloState.IDLE));
        $$02.getInt("scute_time").ifPresent($$0 -> {
            this.scuteTime = $$0;
        });
    }

    public void rollUp() {
        if (this.isScared()) {
            return;
        }
        this.stopInPlace();
        this.resetLove();
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(SoundEvents.ARMADILLO_ROLL);
        this.switchToState(ArmadilloState.ROLLING);
    }

    public void rollOut() {
        if (!this.isScared()) {
            return;
        }
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(SoundEvents.ARMADILLO_UNROLL_FINISH);
        this.switchToState(ArmadilloState.IDLE);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (this.isScared()) {
            $$2 = ($$2 - 1.0f) / 2.0f;
        }
        return super.hurtServer($$0, $$1, $$2);
    }

    @Override
    protected void actuallyHurt(ServerLevel $$0, DamageSource $$1, float $$2) {
        super.actuallyHurt($$0, $$1, $$2);
        if (this.isNoAi() || this.isDeadOrDying()) {
            return;
        }
        if ($$1.getEntity() instanceof LivingEntity) {
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.DANGER_DETECTED_RECENTLY, true, 80L);
            if (this.canStayRolledUp()) {
                this.rollUp();
            }
        } else if ($$1.is(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES)) {
            this.rollOut();
        }
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        ItemStack $$2 = $$0.getItemInHand($$1);
        if ($$2.is(Items.BRUSH) && this.brushOffScute($$0, $$2)) {
            $$2.hurtAndBreak(16, (LivingEntity)$$0, $$1.asEquipmentSlot());
            return InteractionResult.SUCCESS;
        }
        if (this.isScared()) {
            return InteractionResult.FAIL;
        }
        return super.mobInteract($$0, $$1);
    }

    public boolean brushOffScute(@Nullable Entity $$0, ItemStack $$1) {
        if (this.isBaby()) {
            return false;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)level;
            this.dropFromEntityInteractLootTable($$2, BuiltInLootTables.ARMADILLO_BRUSH, $$0, $$1, this::spawnAtLocation);
            this.playSound(SoundEvents.ARMADILLO_BRUSH);
            this.gameEvent(GameEvent.ENTITY_INTERACT);
        }
        return true;
    }

    public boolean canStayRolledUp() {
        return !this.isPanicking() && !this.isInLiquid() && !this.isLeashed() && !this.isPassenger() && !this.isVehicle();
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && !this.isScared();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isScared()) {
            return null;
        }
        return SoundEvents.ARMADILLO_AMBIENT;
    }

    @Override
    protected void playEatingSound() {
        this.makeSound(SoundEvents.ARMADILLO_EAT);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMADILLO_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        if (this.isScared()) {
            return SoundEvents.ARMADILLO_HURT_REDUCED;
        }
        return SoundEvents.ARMADILLO_HURT;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.ARMADILLO_STEP, 0.15f, 1.0f);
    }

    @Override
    public int getMaxHeadYRot() {
        if (this.isScared()) {
            return 0;
        }
        return 32;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this){

            @Override
            public void clientTick() {
                if (!Armadillo.this.isScared()) {
                    super.clientTick();
                }
            }
        };
    }

    public static enum ArmadilloState implements StringRepresentable
    {
        IDLE("idle", false, 0, 0){

            @Override
            public boolean shouldHideInShell(long $$0) {
                return false;
            }
        }
        ,
        ROLLING("rolling", true, 10, 1){

            @Override
            public boolean shouldHideInShell(long $$0) {
                return $$0 > 5L;
            }
        }
        ,
        SCARED("scared", true, 50, 2){

            @Override
            public boolean shouldHideInShell(long $$0) {
                return true;
            }
        }
        ,
        UNROLLING("unrolling", true, 30, 3){

            @Override
            public boolean shouldHideInShell(long $$0) {
                return $$0 < 26L;
            }
        };

        static final Codec<ArmadilloState> CODEC;
        private static final IntFunction<ArmadilloState> BY_ID;
        public static final StreamCodec<ByteBuf, ArmadilloState> STREAM_CODEC;
        private final String name;
        private final boolean isThreatened;
        private final int animationDuration;
        private final int id;

        ArmadilloState(String $$0, boolean $$1, int $$2, int $$3) {
            this.name = $$0;
            this.isThreatened = $$1;
            this.animationDuration = $$2;
            this.id = $$3;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        private int id() {
            return this.id;
        }

        public abstract boolean shouldHideInShell(long var1);

        public boolean isThreatened() {
            return this.isThreatened;
        }

        public int animationDuration() {
            return this.animationDuration;
        }

        static {
            CODEC = StringRepresentable.fromEnum(ArmadilloState::values);
            BY_ID = ByIdMap.continuous(ArmadilloState::id, ArmadilloState.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ArmadilloState::id);
        }
    }
}

