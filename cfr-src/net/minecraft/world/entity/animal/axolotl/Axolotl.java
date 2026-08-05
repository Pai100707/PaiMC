/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.BinaryAnimator;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.EasingType;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.axolotl.AxolotlAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Axolotl
extends Animal
implements Bucketable {
    public static final int TOTAL_PLAYDEAD_TIME = 200;
    private static final int POSE_ANIMATION_TICKS = 10;
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Axolotl>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.AXOLOTL_ATTACKABLES, SensorType.FOOD_TEMPTATIONS);
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_VISIBLE_ADULT, (Object[])new MemoryModuleType[]{MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.PLAY_DEAD_TICKS, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.HAS_HUNTING_COOLDOWN, MemoryModuleType.IS_PANICKING});
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PLAYING_DEAD = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
    public static final double PLAYER_REGEN_DETECTION_RANGE = 20.0;
    public static final int RARE_VARIANT_CHANCE = 1200;
    private static final int AXOLOTL_TOTAL_AIR_SUPPLY = 6000;
    public static final String VARIANT_TAG = "Variant";
    private static final int REHYDRATE_AIR_SUPPLY = 1800;
    private static final int REGEN_BUFF_MAX_DURATION = 2400;
    private static final boolean DEFAULT_FROM_BUCKET = false;
    public final BinaryAnimator playingDeadAnimator = new BinaryAnimator(10, EasingType.IN_OUT_SINE);
    public final BinaryAnimator inWaterAnimator = new BinaryAnimator(10, EasingType.IN_OUT_SINE);
    public final BinaryAnimator onGroundAnimator = new BinaryAnimator(10, EasingType.IN_OUT_SINE);
    public final BinaryAnimator movingAnimator = new BinaryAnimator(10, EasingType.IN_OUT_SINE);
    private static final int REGEN_BUFF_BASE_DURATION = 100;

    public Axolotl(EntityType<? extends Axolotl> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.moveControl = new AxolotlMoveControl(this);
        this.lookControl = new AxolotlLookControl(this, 20);
    }

    @Override
    public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
        return 0.0f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_VARIANT, 0);
        $$0.define(DATA_PLAYING_DEAD, false);
        $$0.define(FROM_BUCKET, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store(VARIANT_TAG, Variant.LEGACY_CODEC, this.getVariant());
        $$0.putBoolean("FromBucket", this.fromBucket());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setVariant($$0.read(VARIANT_TAG, Variant.LEGACY_CODEC).orElse(Variant.DEFAULT));
        this.setFromBucket($$0.getBooleanOr("FromBucket", false));
    }

    @Override
    public void playAmbientSound() {
        if (this.isPlayingDead()) {
            return;
        }
        super.playAmbientSound();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        boolean $$4 = false;
        if ($$2 == EntitySpawnReason.BUCKET) {
            return $$3;
        }
        RandomSource $$5 = $$0.getRandom();
        if ($$3 instanceof AxolotlGroupData) {
            if (((AxolotlGroupData)$$3).getGroupSize() >= 2) {
                $$4 = true;
            }
        } else {
            $$3 = new AxolotlGroupData(Variant.getCommonSpawnVariant($$5), Variant.getCommonSpawnVariant($$5));
        }
        this.setVariant(((AxolotlGroupData)$$3).getVariant($$5));
        if ($$4) {
            this.setAge(-24000);
        }
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public void baseTick() {
        Level level;
        int $$0 = this.getAirSupply();
        super.baseTick();
        if (!this.isNoAi() && (level = this.level()) instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            this.handleAirSupply($$1, $$0);
        }
        if (this.level().isClientSide()) {
            this.tickAnimations();
        }
    }

    private void tickAnimations() {
        AnimationState $$3;
        if (this.isPlayingDead()) {
            AnimationState $$0 = AnimationState.PLAYING_DEAD;
        } else if (this.isInWater()) {
            AnimationState $$1 = AnimationState.IN_WATER;
        } else if (this.onGround()) {
            AnimationState $$2 = AnimationState.ON_GROUND;
        } else {
            $$3 = AnimationState.IN_AIR;
        }
        this.playingDeadAnimator.tick($$3 == AnimationState.PLAYING_DEAD);
        this.inWaterAnimator.tick($$3 == AnimationState.IN_WATER);
        this.onGroundAnimator.tick($$3 == AnimationState.ON_GROUND);
        boolean $$4 = this.walkAnimation.isMoving() || this.getXRot() != this.xRotO || this.getYRot() != this.yRotO;
        this.movingAnimator.tick($$4);
    }

    protected void handleAirSupply(ServerLevel $$0, int $$1) {
        if (this.isAlive() && !this.isInWaterOrRain()) {
            this.setAirSupply($$1 - 1);
            if (this.shouldTakeDrowningDamage()) {
                this.setAirSupply(0);
                this.hurtServer($$0, this.damageSources().dryOut(), 2.0f);
            }
        } else {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    public void rehydrate() {
        int $$0 = this.getAirSupply() + 1800;
        this.setAirSupply(Math.min($$0, this.getMaxAirSupply()));
    }

    @Override
    public int getMaxAirSupply() {
        return 6000;
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_VARIANT));
    }

    private void setVariant(Variant $$0) {
        this.entityData.set(DATA_VARIANT, $$0.getId());
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.AXOLOTL_VARIANT) {
            return Axolotl.castComponentValue($$0, this.getVariant());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.AXOLOTL_VARIANT);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.AXOLOTL_VARIANT) {
            this.setVariant(Axolotl.castComponentValue(DataComponents.AXOLOTL_VARIANT, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    private static boolean useRareVariant(RandomSource $$0) {
        return $$0.nextInt(1200) == 0;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader $$0) {
        return $$0.isUnobstructed(this);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public void setPlayingDead(boolean $$0) {
        this.entityData.set(DATA_PLAYING_DEAD, $$0);
    }

    public boolean isPlayingDead() {
        return this.entityData.get(DATA_PLAYING_DEAD);
    }

    @Override
    public boolean fromBucket() {
        return this.entityData.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean $$0) {
        this.entityData.set(FROM_BUCKET, $$0);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        Axolotl $$2 = EntityType.AXOLOTL.create($$0, EntitySpawnReason.BREEDING);
        if ($$2 != null) {
            Variant $$4;
            if (Axolotl.useRareVariant(this.random)) {
                Variant $$3 = Variant.getRareSpawnVariant(this.random);
            } else {
                $$4 = this.random.nextBoolean() ? this.getVariant() : ((Axolotl)$$1).getVariant();
            }
            $$2.setVariant($$4);
            $$2.setPersistenceRequired();
        }
        return $$2;
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.AXOLOTL_FOOD);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        ProfilerFiller $$1 = Profiler.get();
        $$1.push("axolotlBrain");
        this.getBrain().tick($$0, this);
        $$1.pop();
        $$1.push("axolotlActivityUpdate");
        AxolotlAi.updateActivity(this);
        $$1.pop();
        if (!this.isNoAi()) {
            Optional<Integer> $$2 = this.getBrain().getMemory(MemoryModuleType.PLAY_DEAD_TICKS);
            this.setPlayingDead($$2.isPresent() && $$2.get() > 0);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected PathNavigation createNavigation(Level $$0) {
        return new AmphibiousPathNavigation(this, $$0);
    }

    @Override
    public void playAttackSound() {
        this.playSound(SoundEvents.AXOLOTL_ATTACK, 1.0f, 1.0f);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        float $$3 = this.getHealth();
        if (!this.isNoAi() && this.level().random.nextInt(3) == 0 && ((float)this.level().random.nextInt(3) < $$2 || $$3 / this.getMaxHealth() < 0.5f) && $$2 < $$3 && this.isInWater() && ($$1.getEntity() != null || $$1.getDirectEntity() != null) && !this.isPlayingDead()) {
            this.brain.setMemory(MemoryModuleType.PLAY_DEAD_TICKS, 200);
        }
        return super.hurtServer($$0, $$1, $$2);
    }

    @Override
    public int getMaxHeadXRot() {
        return 1;
    }

    @Override
    public int getMaxHeadYRot() {
        return 1;
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        return Bucketable.bucketMobPickup($$0, $$1, this).orElse(super.mobInteract($$0, $$1));
    }

    @Override
    public void saveToBucketTag(ItemStack $$02) {
        Bucketable.saveDefaultDataToBucketTag(this, $$02);
        $$02.copyFrom(DataComponents.AXOLOTL_VARIANT, this);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, $$02, $$0 -> {
            $$0.putInt("Age", this.getAge());
            Brain<Axolotl> $$1 = this.getBrain();
            if ($$1.hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)) {
                $$0.putLong("HuntingCooldown", $$1.getTimeUntilExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN));
            }
        });
    }

    @Override
    public void loadFromBucketTag(CompoundTag $$0) {
        Bucketable.loadDefaultDataFromBucketTag(this, $$0);
        this.setAge($$0.getIntOr("Age", 0));
        $$0.getLong("HuntingCooldown").ifPresentOrElse($$1 -> this.getBrain().setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, $$0.getLongOr("HuntingCooldown", 0L)), () -> this.getBrain().setMemory(MemoryModuleType.HAS_HUNTING_COOLDOWN, Optional.empty()));
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.AXOLOTL_BUCKET);
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_AXOLOTL;
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return !this.isPlayingDead() && super.canBeSeenAsEnemy();
    }

    public static void onStopAttacking(ServerLevel $$0, Axolotl $$1, LivingEntity $$2) {
        Entity $$4;
        DamageSource $$3;
        if ($$2.isDeadOrDying() && ($$3 = $$2.getLastDamageSource()) != null && ($$4 = $$3.getEntity()) != null && $$4.getType() == EntityType.PLAYER) {
            Player $$5 = (Player)$$4;
            List<Player> $$6 = $$0.getEntitiesOfClass(Player.class, $$1.getBoundingBox().inflate(20.0));
            if ($$6.contains($$5)) {
                $$1.applySupportingEffects($$5);
            }
        }
    }

    public void applySupportingEffects(Player $$0) {
        MobEffectInstance $$1 = $$0.getEffect(MobEffects.REGENERATION);
        if ($$1 == null || $$1.endsWithin(2399)) {
            int $$2 = $$1 != null ? $$1.getDuration() : 0;
            int $$3 = Math.min(2400, 100 + $$2);
            $$0.addEffect(new MobEffectInstance(MobEffects.REGENERATION, $$3, 0), this);
        }
        $$0.removeEffect(MobEffects.MINING_FATIGUE);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.AXOLOTL_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.AXOLOTL_DEATH;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isInWater() ? SoundEvents.AXOLOTL_IDLE_WATER : SoundEvents.AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.AXOLOTL_SPLASH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.AXOLOTL_SWIM;
    }

    protected Brain.Provider<Axolotl> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return AxolotlAi.makeBrain(this.brainProvider().makeBrain($$0));
    }

    public Brain<Axolotl> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
        this.moveRelative(this.getSpeed(), $$0);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
    }

    @Override
    protected void usePlayerItem(Player $$0, InteractionHand $$1, ItemStack $$2) {
        if ($$2.is(Items.TROPICAL_FISH_BUCKET)) {
            $$0.setItemInHand($$1, ItemUtils.createFilledResult($$2, $$0, new ItemStack(Items.WATER_BUCKET)));
        } else {
            super.usePlayerItem($$0, $$1, $$2);
        }
    }

    @Override
    public boolean removeWhenFarAway(double $$0) {
        return !this.fromBucket() && !this.hasCustomName();
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }

    public static boolean checkAxolotlSpawnRules(EntityType<? extends LivingEntity> $$0, ServerLevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getBlockState($$3.below()).is(BlockTags.AXOLOTLS_SPAWNABLE_ON);
    }

    static class AxolotlMoveControl
    extends SmoothSwimmingMoveControl {
        private final Axolotl axolotl;

        public AxolotlMoveControl(Axolotl $$0) {
            super($$0, 85, 10, 0.1f, 0.5f, false);
            this.axolotl = $$0;
        }

        @Override
        public void tick() {
            if (!this.axolotl.isPlayingDead()) {
                super.tick();
            }
        }
    }

    class AxolotlLookControl
    extends SmoothSwimmingLookControl {
        public AxolotlLookControl(Axolotl $$0, int $$1) {
            super($$0, $$1);
        }

        @Override
        public void tick() {
            if (!Axolotl.this.isPlayingDead()) {
                super.tick();
            }
        }
    }

    public static enum Variant implements StringRepresentable
    {
        LUCY(0, "lucy", true),
        WILD(1, "wild", true),
        GOLD(2, "gold", true),
        CYAN(3, "cyan", true),
        BLUE(4, "blue", false);

        public static final Variant DEFAULT;
        private static final IntFunction<Variant> BY_ID;
        public static final StreamCodec<ByteBuf, Variant> STREAM_CODEC;
        public static final Codec<Variant> CODEC;
        @Deprecated
        public static final Codec<Variant> LEGACY_CODEC;
        private final int id;
        private final String name;
        private final boolean common;

        private Variant(int $$0, String $$1, boolean $$2) {
            this.id = $$0;
            this.name = $$1;
            this.common = $$2;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Variant byId(int $$0) {
            return BY_ID.apply($$0);
        }

        public static Variant getCommonSpawnVariant(RandomSource $$0) {
            return Variant.getSpawnVariant($$0, true);
        }

        public static Variant getRareSpawnVariant(RandomSource $$0) {
            return Variant.getSpawnVariant($$0, false);
        }

        private static Variant getSpawnVariant(RandomSource $$0, boolean $$12) {
            Variant[] $$2 = (Variant[])Arrays.stream(Variant.values()).filter($$1 -> $$1.common == $$12).toArray(Variant[]::new);
            return Util.getRandom($$2, $$0);
        }

        static {
            DEFAULT = LUCY;
            BY_ID = ByIdMap.continuous(Variant::getId, Variant.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::getId);
            CODEC = StringRepresentable.fromEnum(Variant::values);
            LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, Variant::getId);
        }
    }

    public static class AxolotlGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant[] types;

        public AxolotlGroupData(Variant ... $$0) {
            super(false);
            this.types = $$0;
        }

        public Variant getVariant(RandomSource $$0) {
            return this.types[$$0.nextInt(this.types.length)];
        }
    }

    public static enum AnimationState {
        PLAYING_DEAD,
        IN_WATER,
        ON_GROUND,
        IN_AIR;

    }
}

