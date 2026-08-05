/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.goat;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.goat.GoatAi;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Goat
extends Animal {
    public static final EntityDimensions LONG_JUMPING_DIMENSIONS = EntityDimensions.scalable(0.9f, 1.3f).scale(0.7f);
    private static final int ADULT_ATTACK_DAMAGE = 2;
    private static final int BABY_ATTACK_DAMAGE = 1;
    protected static final ImmutableList<SensorType<? extends Sensor<? super Goat>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.FOOD_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, (Object[])new MemoryModuleType[]{MemoryModuleType.IS_TEMPTED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleType.RAM_TARGET, MemoryModuleType.IS_PANICKING});
    public static final int GOAT_FALL_DAMAGE_REDUCTION = 10;
    public static final double GOAT_SCREAMING_CHANCE = 0.02;
    public static final double UNIHORN_CHANCE = (double)0.1f;
    private static final EntityDataAccessor<Boolean> DATA_IS_SCREAMING_GOAT = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_LEFT_HORN = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_RIGHT_HORN = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
    private static final boolean DEFAULT_IS_SCREAMING = false;
    private static final boolean DEFAULT_HAS_LEFT_HORN = true;
    private static final boolean DEFAULT_HAS_RIGHT_HORN = true;
    private boolean isLoweringHead;
    private int lowerHeadTick;

    public Goat(EntityType<? extends Goat> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0f);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0f);
    }

    public ItemStack createHorn() {
        RandomSource $$02 = RandomSource.create(this.getUUID().hashCode());
        TagKey<Instrument> $$1 = this.isScreamingGoat() ? InstrumentTags.SCREAMING_GOAT_HORNS : InstrumentTags.REGULAR_GOAT_HORNS;
        return this.level().registryAccess().lookupOrThrow(Registries.INSTRUMENT).getRandomElementOf($$1, $$02).map($$0 -> InstrumentItem.create(Items.GOAT_HORN, $$0)).orElseGet(() -> new ItemStack(Items.GOAT_HORN));
    }

    protected Brain.Provider<Goat> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return GoatAi.makeBrain(this.brainProvider().makeBrain($$0));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2f).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void ageBoundaryReached() {
        if (this.isBaby()) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1.0);
            this.removeHorns();
        } else {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0);
            this.addHorns();
        }
    }

    @Override
    protected int calculateFallDamage(double $$0, float $$1) {
        return super.calculateFallDamage($$0, $$1) - 10;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isScreamingGoat()) {
            return SoundEvents.GOAT_SCREAMING_AMBIENT;
        }
        return SoundEvents.GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        if (this.isScreamingGoat()) {
            return SoundEvents.GOAT_SCREAMING_HURT;
        }
        return SoundEvents.GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isScreamingGoat()) {
            return SoundEvents.GOAT_SCREAMING_DEATH;
        }
        return SoundEvents.GOAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.GOAT_STEP, 0.15f, 1.0f);
    }

    protected SoundEvent getMilkingSound() {
        if (this.isScreamingGoat()) {
            return SoundEvents.GOAT_SCREAMING_MILK;
        }
        return SoundEvents.GOAT_MILK;
    }

    @Override
    public @Nullable Goat getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        Goat $$2 = EntityType.GOAT.create($$0, EntitySpawnReason.BREEDING);
        if ($$2 != null) {
            AgeableMob $$4;
            GoatAi.initMemories($$2, $$0.getRandom());
            AgeableMob $$3 = $$0.getRandom().nextBoolean() ? this : $$1;
            boolean $$5 = $$3 instanceof Goat && ((Goat)($$4 = $$3)).isScreamingGoat() || $$0.getRandom().nextDouble() < 0.02;
            $$2.setScreamingGoat($$5);
        }
        return $$2;
    }

    public Brain<Goat> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        ProfilerFiller $$1 = Profiler.get();
        $$1.push("goatBrain");
        this.getBrain().tick($$0, this);
        $$1.pop();
        $$1.push("goatActivityUpdate");
        GoatAi.updateActivity(this);
        $$1.pop();
        super.customServerAiStep($$0);
    }

    @Override
    public int getMaxHeadYRot() {
        return 15;
    }

    @Override
    public void setYHeadRot(float $$0) {
        int $$1 = this.getMaxHeadYRot();
        float $$2 = Mth.degreesDifference(this.yBodyRot, $$0);
        float $$3 = Mth.clamp($$2, (float)(-$$1), (float)$$1);
        super.setYHeadRot(this.yBodyRot + $$3);
    }

    @Override
    protected void playEatingSound() {
        this.level().playSound(null, this, this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_EAT : SoundEvents.GOAT_EAT, SoundSource.NEUTRAL, 1.0f, Mth.randomBetween(this.level().random, 0.8f, 1.2f));
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.GOAT_FOOD);
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        ItemStack $$2 = $$0.getItemInHand($$1);
        if ($$2.is(Items.BUCKET) && !this.isBaby()) {
            $$0.playSound(this.getMilkingSound(), 1.0f, 1.0f);
            ItemStack $$3 = ItemUtils.createFilledResult($$2, $$0, Items.MILK_BUCKET.getDefaultInstance());
            $$0.setItemInHand($$1, $$3);
            return InteractionResult.SUCCESS;
        }
        InteractionResult $$4 = super.mobInteract($$0, $$1);
        if ($$4.consumesAction() && this.isFood($$2)) {
            this.playEatingSound();
        }
        return $$4;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        RandomSource $$4 = $$0.getRandom();
        GoatAi.initMemories(this, $$4);
        this.setScreamingGoat($$4.nextDouble() < 0.02);
        this.ageBoundaryReached();
        if (!this.isBaby() && (double)$$4.nextFloat() < (double)0.1f) {
            EntityDataAccessor<Boolean> $$5 = $$4.nextBoolean() ? DATA_HAS_LEFT_HORN : DATA_HAS_RIGHT_HORN;
            this.entityData.set($$5, false);
        }
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return $$0 == Pose.LONG_JUMPING ? LONG_JUMPING_DIMENSIONS.scale(this.getAgeScale()) : super.getDefaultDimensions($$0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean("IsScreamingGoat", this.isScreamingGoat());
        $$0.putBoolean("HasLeftHorn", this.hasLeftHorn());
        $$0.putBoolean("HasRightHorn", this.hasRightHorn());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setScreamingGoat($$0.getBooleanOr("IsScreamingGoat", false));
        this.entityData.set(DATA_HAS_LEFT_HORN, $$0.getBooleanOr("HasLeftHorn", true));
        this.entityData.set(DATA_HAS_RIGHT_HORN, $$0.getBooleanOr("HasRightHorn", true));
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 58) {
            this.isLoweringHead = true;
        } else if ($$0 == 59) {
            this.isLoweringHead = false;
        } else {
            super.handleEntityEvent($$0);
        }
    }

    @Override
    public void aiStep() {
        this.lowerHeadTick = this.isLoweringHead ? ++this.lowerHeadTick : (this.lowerHeadTick -= 2);
        this.lowerHeadTick = Mth.clamp(this.lowerHeadTick, 0, 20);
        super.aiStep();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_IS_SCREAMING_GOAT, false);
        $$0.define(DATA_HAS_LEFT_HORN, true);
        $$0.define(DATA_HAS_RIGHT_HORN, true);
    }

    public boolean hasLeftHorn() {
        return this.entityData.get(DATA_HAS_LEFT_HORN);
    }

    public boolean hasRightHorn() {
        return this.entityData.get(DATA_HAS_RIGHT_HORN);
    }

    public boolean dropHorn() {
        EntityDataAccessor<Boolean> $$4;
        boolean $$0 = this.hasLeftHorn();
        boolean $$1 = this.hasRightHorn();
        if (!$$0 && !$$1) {
            return false;
        }
        if (!$$0) {
            EntityDataAccessor<Boolean> $$2 = DATA_HAS_RIGHT_HORN;
        } else if (!$$1) {
            EntityDataAccessor<Boolean> $$3 = DATA_HAS_LEFT_HORN;
        } else {
            $$4 = this.random.nextBoolean() ? DATA_HAS_LEFT_HORN : DATA_HAS_RIGHT_HORN;
        }
        this.entityData.set($$4, false);
        Vec3 $$5 = this.position();
        ItemStack $$6 = this.createHorn();
        double $$7 = Mth.randomBetween(this.random, -0.2f, 0.2f);
        double $$8 = Mth.randomBetween(this.random, 0.3f, 0.7f);
        double $$9 = Mth.randomBetween(this.random, -0.2f, 0.2f);
        ItemEntity $$10 = new ItemEntity(this.level(), $$5.x(), $$5.y(), $$5.z(), $$6, $$7, $$8, $$9);
        this.level().addFreshEntity($$10);
        return true;
    }

    public void addHorns() {
        this.entityData.set(DATA_HAS_LEFT_HORN, true);
        this.entityData.set(DATA_HAS_RIGHT_HORN, true);
    }

    public void removeHorns() {
        this.entityData.set(DATA_HAS_LEFT_HORN, false);
        this.entityData.set(DATA_HAS_RIGHT_HORN, false);
    }

    public boolean isScreamingGoat() {
        return this.entityData.get(DATA_IS_SCREAMING_GOAT);
    }

    public void setScreamingGoat(boolean $$0) {
        this.entityData.set(DATA_IS_SCREAMING_GOAT, $$0);
    }

    public float getRammingXHeadRot() {
        return (float)this.lowerHeadTick / 20.0f * 30.0f * ((float)Math.PI / 180);
    }

    public static boolean checkGoatSpawnRules(EntityType<? extends Animal> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getBlockState($$3.below()).is(BlockTags.GOATS_SPAWNABLE_ON) && Goat.isBrightEnoughToSpawn($$1, $$3);
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }
}

