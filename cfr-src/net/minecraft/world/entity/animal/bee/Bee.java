/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.bee;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.debug.DebugBeeInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Bee
extends Animal
implements NeutralMob,
FlyingAnimal {
    public static final float FLAP_DEGREES_PER_TICK = 120.32113f;
    public static final int TICKS_PER_FLAP = Mth.ceil(1.4959966f);
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Long> DATA_ANGER_END_TIME = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.LONG);
    private static final int FLAG_ROLL = 2;
    private static final int FLAG_HAS_STUNG = 4;
    private static final int FLAG_HAS_NECTAR = 8;
    private static final int STING_DEATH_COUNTDOWN = 1200;
    private static final int TICKS_BEFORE_GOING_TO_KNOWN_FLOWER = 600;
    private static final int TICKS_WITHOUT_NECTAR_BEFORE_GOING_HOME = 3600;
    private static final int MIN_ATTACK_DIST = 4;
    private static final int MAX_CROPS_GROWABLE = 10;
    private static final int POISON_SECONDS_NORMAL = 10;
    private static final int POISON_SECONDS_HARD = 18;
    private static final int TOO_FAR_DISTANCE = 48;
    private static final int HIVE_CLOSE_ENOUGH_DISTANCE = 2;
    private static final int RESTRICTED_WANDER_DISTANCE_REDUCTION = 24;
    private static final int DEFAULT_WANDER_DISTANCE_REDUCTION = 16;
    private static final int PATHFIND_TO_HIVE_WHEN_CLOSER_THAN = 16;
    private static final int HIVE_SEARCH_DISTANCE = 20;
    public static final String TAG_CROPS_GROWN_SINCE_POLLINATION = "CropsGrownSincePollination";
    public static final String TAG_CANNOT_ENTER_HIVE_TICKS = "CannotEnterHiveTicks";
    public static final String TAG_TICKS_SINCE_POLLINATION = "TicksSincePollination";
    public static final String TAG_HAS_STUNG = "HasStung";
    public static final String TAG_HAS_NECTAR = "HasNectar";
    public static final String TAG_FLOWER_POS = "flower_pos";
    public static final String TAG_HIVE_POS = "hive_pos";
    public static final boolean DEFAULT_HAS_NECTAR = false;
    private static final boolean DEFAULT_HAS_STUNG = false;
    private static final int DEFAULT_TICKS_SINCE_POLLINATION = 0;
    private static final int DEFAULT_CANNOT_ENTER_HIVE_TICKS = 0;
    private static final int DEFAULT_CROPS_GROWN_SINCE_POLLINATION = 0;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private @Nullable EntityReference<LivingEntity> persistentAngerTarget;
    private float rollAmount;
    private float rollAmountO;
    private int timeSinceSting;
    int ticksWithoutNectarSinceExitingHive = 0;
    private int stayOutOfHiveCountdown = 0;
    private int numCropsGrownSincePollination = 0;
    private static final int COOLDOWN_BEFORE_LOCATING_NEW_HIVE = 200;
    int remainingCooldownBeforeLocatingNewHive;
    private static final int COOLDOWN_BEFORE_LOCATING_NEW_FLOWER = 200;
    private static final int MIN_FIND_FLOWER_RETRY_COOLDOWN = 20;
    private static final int MAX_FIND_FLOWER_RETRY_COOLDOWN = 60;
    int remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(this.random, 20, 60);
    @Nullable BlockPos savedFlowerPos;
    @Nullable BlockPos hivePos;
    BeePollinateGoal beePollinateGoal;
    BeeGoToHiveGoal goToHiveGoal;
    private BeeGoToKnownFlowerGoal goToKnownFlowerGoal;
    private int underWaterTicks;

    public Bee(EntityType<? extends Bee> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.lookControl = new BeeLookControl(this);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0f);
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0f);
        this.setPathfindingMalus(PathType.COCOA, -1.0f);
        this.setPathfindingMalus(PathType.FENCE, -1.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_FLAGS_ID, (byte)0);
        $$0.define(DATA_ANGER_END_TIME, -1L);
    }

    @Override
    public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
        if ($$1.getBlockState($$0).isAir()) {
            return 10.0f;
        }
        return 0.0f;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BeeAttackGoal(this, 1.4f, true));
        this.goalSelector.addGoal(1, new BeeEnterHiveGoal());
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, $$0 -> $$0.is(ItemTags.BEE_FOOD), false));
        this.goalSelector.addGoal(3, new ValidateHiveGoal());
        this.goalSelector.addGoal(3, new ValidateFlowerGoal());
        this.beePollinateGoal = new BeePollinateGoal();
        this.goalSelector.addGoal(4, this.beePollinateGoal);
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(5, new BeeLocateHiveGoal());
        this.goToHiveGoal = new BeeGoToHiveGoal();
        this.goalSelector.addGoal(5, this.goToHiveGoal);
        this.goToKnownFlowerGoal = new BeeGoToKnownFlowerGoal();
        this.goalSelector.addGoal(6, this.goToKnownFlowerGoal);
        this.goalSelector.addGoal(7, new BeeGrowCropGoal());
        this.goalSelector.addGoal(8, new BeeWanderGoal());
        this.goalSelector.addGoal(9, new FloatGoal(this));
        this.targetSelector.addGoal(1, new BeeHurtByOtherGoal(this).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new BeeBecomeAngryTargetGoal(this));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<Bee>(this, true));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.storeNullable(TAG_HIVE_POS, BlockPos.CODEC, this.hivePos);
        $$0.storeNullable(TAG_FLOWER_POS, BlockPos.CODEC, this.savedFlowerPos);
        $$0.putBoolean(TAG_HAS_NECTAR, this.hasNectar());
        $$0.putBoolean(TAG_HAS_STUNG, this.hasStung());
        $$0.putInt(TAG_TICKS_SINCE_POLLINATION, this.ticksWithoutNectarSinceExitingHive);
        $$0.putInt(TAG_CANNOT_ENTER_HIVE_TICKS, this.stayOutOfHiveCountdown);
        $$0.putInt(TAG_CROPS_GROWN_SINCE_POLLINATION, this.numCropsGrownSincePollination);
        this.addPersistentAngerSaveData($$0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setHasNectar($$0.getBooleanOr(TAG_HAS_NECTAR, false));
        this.setHasStung($$0.getBooleanOr(TAG_HAS_STUNG, false));
        this.ticksWithoutNectarSinceExitingHive = $$0.getIntOr(TAG_TICKS_SINCE_POLLINATION, 0);
        this.stayOutOfHiveCountdown = $$0.getIntOr(TAG_CANNOT_ENTER_HIVE_TICKS, 0);
        this.numCropsGrownSincePollination = $$0.getIntOr(TAG_CROPS_GROWN_SINCE_POLLINATION, 0);
        this.hivePos = $$0.read(TAG_HIVE_POS, BlockPos.CODEC).orElse(null);
        this.savedFlowerPos = $$0.read(TAG_FLOWER_POS, BlockPos.CODEC).orElse(null);
        this.readPersistentAngerSaveData(this.level(), $$0);
    }

    @Override
    public boolean doHurtTarget(ServerLevel $$0, Entity $$1) {
        DamageSource $$2 = this.damageSources().sting(this);
        boolean $$3 = $$1.hurtServer($$0, $$2, (int)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if ($$3) {
            EnchantmentHelper.doPostAttackEffects($$0, $$1, $$2);
            if ($$1 instanceof LivingEntity) {
                LivingEntity $$4 = (LivingEntity)$$1;
                $$4.setStingerCount($$4.getStingerCount() + 1);
                int $$5 = 0;
                if (this.level().getDifficulty() == Difficulty.NORMAL) {
                    $$5 = 10;
                } else if (this.level().getDifficulty() == Difficulty.HARD) {
                    $$5 = 18;
                }
                if ($$5 > 0) {
                    $$4.addEffect(new MobEffectInstance(MobEffects.POISON, $$5 * 20, 0), this);
                }
            }
            this.setHasStung(true);
            this.stopBeingAngry();
            this.playSound(SoundEvents.BEE_STING, 1.0f, 1.0f);
        }
        return $$3;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasNectar() && this.getCropsGrownSincePollination() < 10 && this.random.nextFloat() < 0.05f) {
            for (int $$0 = 0; $$0 < this.random.nextInt(2) + 1; ++$$0) {
                this.spawnFluidParticle(this.level(), this.getX() - (double)0.3f, this.getX() + (double)0.3f, this.getZ() - (double)0.3f, this.getZ() + (double)0.3f, this.getY(0.5), ParticleTypes.FALLING_NECTAR);
            }
        }
        this.updateRollAmount();
    }

    private void spawnFluidParticle(Level $$0, double $$1, double $$2, double $$3, double $$4, double $$5, ParticleOptions $$6) {
        $$0.addParticle($$6, Mth.lerp($$0.random.nextDouble(), $$1, $$2), $$5, Mth.lerp($$0.random.nextDouble(), $$3, $$4), 0.0, 0.0, 0.0);
    }

    void pathfindRandomlyTowards(BlockPos $$0) {
        Vec3 $$8;
        Vec3 $$1 = Vec3.atBottomCenterOf($$0);
        int $$2 = 0;
        BlockPos $$3 = this.blockPosition();
        int $$4 = (int)$$1.y - $$3.getY();
        if ($$4 > 2) {
            $$2 = 4;
        } else if ($$4 < -2) {
            $$2 = -4;
        }
        int $$5 = 6;
        int $$6 = 8;
        int $$7 = $$3.distManhattan($$0);
        if ($$7 < 15) {
            $$5 = $$7 / 2;
            $$6 = $$7 / 2;
        }
        if (($$8 = AirRandomPos.getPosTowards(this, $$5, $$6, $$2, $$1, 0.3141592741012573)) == null) {
            return;
        }
        this.navigation.setMaxVisitedNodesMultiplier(0.5f);
        this.navigation.moveTo($$8.x, $$8.y, $$8.z, 1.0);
    }

    public @Nullable BlockPos getSavedFlowerPos() {
        return this.savedFlowerPos;
    }

    public boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    public void setSavedFlowerPos(BlockPos $$0) {
        this.savedFlowerPos = $$0;
    }

    @VisibleForDebug
    public int getTravellingTicks() {
        return Math.max(this.goToHiveGoal.travellingTicks, this.goToKnownFlowerGoal.travellingTicks);
    }

    @VisibleForDebug
    public List<BlockPos> getBlacklistedHives() {
        return this.goToHiveGoal.blacklistedTargets;
    }

    private boolean isTiredOfLookingForNectar() {
        return this.ticksWithoutNectarSinceExitingHive > 3600;
    }

    void dropHive() {
        this.hivePos = null;
        this.remainingCooldownBeforeLocatingNewHive = 200;
    }

    void dropFlower() {
        this.savedFlowerPos = null;
        this.remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(this.random, 20, 60);
    }

    boolean wantsToEnterHive() {
        if (this.stayOutOfHiveCountdown > 0 || this.beePollinateGoal.isPollinating() || this.hasStung() || this.getTarget() != null) {
            return false;
        }
        boolean $$0 = this.hasNectar() || this.isTiredOfLookingForNectar() || this.level().environmentAttributes().getValue(EnvironmentAttributes.BEES_STAY_IN_HIVE, this.position()) != false;
        return $$0 && !this.isHiveNearFire();
    }

    public void setStayOutOfHiveCountdown(int $$0) {
        this.stayOutOfHiveCountdown = $$0;
    }

    public float getRollAmount(float $$0) {
        return Mth.lerp($$0, this.rollAmountO, this.rollAmount);
    }

    private void updateRollAmount() {
        this.rollAmountO = this.rollAmount;
        this.rollAmount = this.isRolling() ? Math.min(1.0f, this.rollAmount + 0.2f) : Math.max(0.0f, this.rollAmount - 0.24f);
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        boolean $$1 = this.hasStung();
        this.underWaterTicks = this.isInWater() ? ++this.underWaterTicks : 0;
        if (this.underWaterTicks > 20) {
            this.hurtServer($$0, this.damageSources().drown(), 1.0f);
        }
        if ($$1) {
            ++this.timeSinceSting;
            if (this.timeSinceSting % 5 == 0 && this.random.nextInt(Mth.clamp(1200 - this.timeSinceSting, 1, 1200)) == 0) {
                this.hurtServer($$0, this.damageSources().generic(), this.getHealth());
            }
        }
        if (!this.hasNectar()) {
            ++this.ticksWithoutNectarSinceExitingHive;
        }
        this.updatePersistentAnger($$0, false);
    }

    public void resetTicksWithoutNectarSinceExitingHive() {
        this.ticksWithoutNectarSinceExitingHive = 0;
    }

    private boolean isHiveNearFire() {
        BeehiveBlockEntity $$0 = this.getBeehiveBlockEntity();
        return $$0 != null && $$0.isFireNearby();
    }

    @Override
    public long getPersistentAngerEndTime() {
        return this.entityData.get(DATA_ANGER_END_TIME);
    }

    @Override
    public void setPersistentAngerEndTime(long $$0) {
        this.entityData.set(DATA_ANGER_END_TIME, $$0);
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> $$0) {
        this.persistentAngerTarget = $$0;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    private boolean doesHiveHaveSpace(BlockPos $$0) {
        BlockEntity $$1 = this.level().getBlockEntity($$0);
        if ($$1 instanceof BeehiveBlockEntity) {
            return !((BeehiveBlockEntity)$$1).isFull();
        }
        return false;
    }

    @VisibleForDebug
    public boolean hasHive() {
        return this.hivePos != null;
    }

    @VisibleForDebug
    public @Nullable BlockPos getHivePos() {
        return this.hivePos;
    }

    @VisibleForDebug
    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    int getCropsGrownSincePollination() {
        return this.numCropsGrownSincePollination;
    }

    private void resetNumCropsGrownSincePollination() {
        this.numCropsGrownSincePollination = 0;
    }

    void incrementNumCropsGrownSincePollination() {
        ++this.numCropsGrownSincePollination;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            if (this.stayOutOfHiveCountdown > 0) {
                --this.stayOutOfHiveCountdown;
            }
            if (this.remainingCooldownBeforeLocatingNewHive > 0) {
                --this.remainingCooldownBeforeLocatingNewHive;
            }
            if (this.remainingCooldownBeforeLocatingNewFlower > 0) {
                --this.remainingCooldownBeforeLocatingNewFlower;
            }
            boolean $$0 = this.isAngry() && !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0;
            this.setRolling($$0);
            if (this.tickCount % 20 == 0 && !this.isHiveValid()) {
                this.hivePos = null;
            }
        }
    }

    @Nullable BeehiveBlockEntity getBeehiveBlockEntity() {
        if (this.hivePos == null) {
            return null;
        }
        if (this.isTooFarAway(this.hivePos)) {
            return null;
        }
        return this.level().getBlockEntity(this.hivePos, BlockEntityType.BEEHIVE).orElse(null);
    }

    boolean isHiveValid() {
        return this.getBeehiveBlockEntity() != null;
    }

    public boolean hasNectar() {
        return this.getFlag(8);
    }

    void setHasNectar(boolean $$0) {
        if ($$0) {
            this.resetTicksWithoutNectarSinceExitingHive();
        }
        this.setFlag(8, $$0);
    }

    public boolean hasStung() {
        return this.getFlag(4);
    }

    private void setHasStung(boolean $$0) {
        this.setFlag(4, $$0);
    }

    private boolean isRolling() {
        return this.getFlag(2);
    }

    private void setRolling(boolean $$0) {
        this.setFlag(2, $$0);
    }

    boolean isTooFarAway(BlockPos $$0) {
        return !this.closerThan($$0, 48);
    }

    private void setFlag(int $$0, boolean $$1) {
        if ($$1) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | $$0));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~$$0));
        }
    }

    private boolean getFlag(int $$0) {
        return (this.entityData.get(DATA_FLAGS_ID) & $$0) != 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.FLYING_SPEED, 0.6f).add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected PathNavigation createNavigation(Level $$0) {
        FlyingPathNavigation $$1 = new FlyingPathNavigation(this, $$0){

            @Override
            public boolean isStableDestination(BlockPos $$0) {
                return !this.level.getBlockState($$0.below()).isAir();
            }

            @Override
            public void tick() {
                if (Bee.this.beePollinateGoal.isPollinating()) {
                    return;
                }
                super.tick();
            }
        };
        $$1.setCanOpenDoors(false);
        $$1.setCanFloat(false);
        $$1.setRequiredPathLength(48.0f);
        return $$1;
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        FlowerBlock $$4;
        MobEffectInstance $$5;
        BlockItem $$3;
        FeatureElement featureElement;
        ItemStack $$2 = $$0.getItemInHand($$1);
        if (this.isFood($$2) && (featureElement = $$2.getItem()) instanceof BlockItem && (featureElement = ($$3 = (BlockItem)featureElement).getBlock()) instanceof FlowerBlock && ($$5 = ($$4 = (FlowerBlock)featureElement).getBeeInteractionEffect()) != null) {
            this.usePlayerItem($$0, $$1, $$2);
            if (!this.level().isClientSide()) {
                this.addEffect($$5);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract($$0, $$1);
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.BEE_FOOD);
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public @Nullable Bee getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return EntityType.BEE.create($$0, EntitySpawnReason.BREEDING);
    }

    @Override
    protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
    }

    @Override
    public boolean isFlapping() {
        return this.isFlying() && this.tickCount % TICKS_PER_FLAP == 0;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    public void dropOffNectar() {
        this.setHasNectar(false);
        this.resetNumCropsGrownSincePollination();
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (this.isInvulnerableTo($$0, $$1)) {
            return false;
        }
        this.beePollinateGoal.stopPollinating();
        return super.hurtServer($$0, $$1, $$2);
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> $$0) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01, 0.0));
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.5f * this.getEyeHeight(), this.getBbWidth() * 0.2f);
    }

    boolean closerThan(BlockPos $$0, int $$1) {
        return $$0.closerThan(this.blockPosition(), $$1);
    }

    public void setHivePos(BlockPos $$0) {
        this.hivePos = $$0;
    }

    public static boolean attractsBees(BlockState $$0) {
        if ($$0.is(BlockTags.BEE_ATTRACTIVE)) {
            if ($$0.getValueOrElse(BlockStateProperties.WATERLOGGED, false).booleanValue()) {
                return false;
            }
            if ($$0.is(Blocks.SUNFLOWER)) {
                return $$0.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
            }
            return true;
        }
        return false;
    }

    @Override
    public void registerDebugValues(ServerLevel $$0, DebugValueSource.Registration $$1) {
        super.registerDebugValues($$0, $$1);
        $$1.register(DebugSubscriptions.BEES, () -> new DebugBeeInfo(Optional.ofNullable(this.getHivePos()), Optional.ofNullable(this.getSavedFlowerPos()), this.getTravellingTicks(), this.getBlacklistedHives()));
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    class BeeLookControl
    extends LookControl {
        BeeLookControl(Mob $$0) {
            super($$0);
        }

        @Override
        public void tick() {
            if (Bee.this.isAngry()) {
                return;
            }
            super.tick();
        }

        @Override
        protected boolean resetXRotOnTick() {
            return !Bee.this.beePollinateGoal.isPollinating();
        }
    }

    class BeeAttackGoal
    extends MeleeAttackGoal {
        BeeAttackGoal(PathfinderMob $$0, double $$1, boolean $$2) {
            super($$0, $$1, $$2);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && Bee.this.isAngry() && !Bee.this.hasStung();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && Bee.this.isAngry() && !Bee.this.hasStung();
        }
    }

    class BeeEnterHiveGoal
    extends BaseBeeGoal {
        BeeEnterHiveGoal() {
        }

        @Override
        public boolean canBeeUse() {
            BeehiveBlockEntity $$0;
            if (Bee.this.hivePos != null && Bee.this.wantsToEnterHive() && Bee.this.hivePos.closerToCenterThan(Bee.this.position(), 2.0) && ($$0 = Bee.this.getBeehiveBlockEntity()) != null) {
                if ($$0.isFull()) {
                    Bee.this.hivePos = null;
                } else {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            BeehiveBlockEntity $$0 = Bee.this.getBeehiveBlockEntity();
            if ($$0 != null) {
                $$0.addOccupant(Bee.this);
            }
        }
    }

    class ValidateHiveGoal
    extends BaseBeeGoal {
        private final int VALIDATE_HIVE_COOLDOWN;
        private long lastValidateTick;

        ValidateHiveGoal() {
            this.VALIDATE_HIVE_COOLDOWN = Mth.nextInt(Bee.this.random, 20, 40);
            this.lastValidateTick = -1L;
        }

        @Override
        public void start() {
            if (Bee.this.hivePos != null && Bee.this.level().isLoaded(Bee.this.hivePos) && !Bee.this.isHiveValid()) {
                Bee.this.dropHive();
            }
            this.lastValidateTick = Bee.this.level().getGameTime();
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.level().getGameTime() > this.lastValidateTick + (long)this.VALIDATE_HIVE_COOLDOWN;
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }
    }

    class ValidateFlowerGoal
    extends BaseBeeGoal {
        private final int validateFlowerCooldown;
        private long lastValidateTick;

        ValidateFlowerGoal() {
            this.validateFlowerCooldown = Mth.nextInt(Bee.this.random, 20, 40);
            this.lastValidateTick = -1L;
        }

        @Override
        public void start() {
            if (Bee.this.savedFlowerPos != null && Bee.this.level().isLoaded(Bee.this.savedFlowerPos) && !this.isFlower(Bee.this.savedFlowerPos)) {
                Bee.this.dropFlower();
            }
            this.lastValidateTick = Bee.this.level().getGameTime();
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.level().getGameTime() > this.lastValidateTick + (long)this.validateFlowerCooldown;
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }

        private boolean isFlower(BlockPos $$0) {
            return Bee.attractsBees(Bee.this.level().getBlockState($$0));
        }
    }

    class BeePollinateGoal
    extends BaseBeeGoal {
        private static final int MIN_POLLINATION_TICKS = 400;
        private static final double ARRIVAL_THRESHOLD = 0.1;
        private static final int POSITION_CHANGE_CHANCE = 25;
        private static final float SPEED_MODIFIER = 0.35f;
        private static final float HOVER_HEIGHT_WITHIN_FLOWER = 0.6f;
        private static final float HOVER_POS_OFFSET = 0.33333334f;
        private static final int FLOWER_SEARCH_RADIUS = 5;
        private int successfulPollinatingTicks;
        private int lastSoundPlayedTick;
        private boolean pollinating;
        private @Nullable Vec3 hoverPos;
        private int pollinatingTicks;
        private static final int MAX_POLLINATING_TICKS = 600;
        private Long2LongOpenHashMap unreachableFlowerCache;

        BeePollinateGoal() {
            this.unreachableFlowerCache = new Long2LongOpenHashMap();
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canBeeUse() {
            if (Bee.this.remainingCooldownBeforeLocatingNewFlower > 0) {
                return false;
            }
            if (Bee.this.hasNectar()) {
                return false;
            }
            if (Bee.this.level().isRaining()) {
                return false;
            }
            Optional<BlockPos> $$0 = this.findNearbyFlower();
            if ($$0.isPresent()) {
                Bee.this.savedFlowerPos = $$0.get();
                Bee.this.navigation.moveTo((double)Bee.this.savedFlowerPos.getX() + 0.5, (double)Bee.this.savedFlowerPos.getY() + 0.5, (double)Bee.this.savedFlowerPos.getZ() + 0.5, 1.2f);
                return true;
            }
            Bee.this.remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(Bee.this.random, 20, 60);
            return false;
        }

        @Override
        public boolean canBeeContinueToUse() {
            if (!this.pollinating) {
                return false;
            }
            if (!Bee.this.hasSavedFlowerPos()) {
                return false;
            }
            if (Bee.this.level().isRaining()) {
                return false;
            }
            if (this.hasPollinatedLongEnough()) {
                return Bee.this.random.nextFloat() < 0.2f;
            }
            return true;
        }

        private boolean hasPollinatedLongEnough() {
            return this.successfulPollinatingTicks > 400;
        }

        boolean isPollinating() {
            return this.pollinating;
        }

        void stopPollinating() {
            this.pollinating = false;
        }

        @Override
        public void start() {
            this.successfulPollinatingTicks = 0;
            this.pollinatingTicks = 0;
            this.lastSoundPlayedTick = 0;
            this.pollinating = true;
            Bee.this.resetTicksWithoutNectarSinceExitingHive();
        }

        @Override
        public void stop() {
            if (this.hasPollinatedLongEnough()) {
                Bee.this.setHasNectar(true);
            }
            this.pollinating = false;
            Bee.this.navigation.stop();
            Bee.this.remainingCooldownBeforeLocatingNewFlower = 200;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (!Bee.this.hasSavedFlowerPos()) {
                return;
            }
            ++this.pollinatingTicks;
            if (this.pollinatingTicks > 600) {
                Bee.this.dropFlower();
                this.pollinating = false;
                Bee.this.remainingCooldownBeforeLocatingNewFlower = 200;
                return;
            }
            Vec3 $$0 = Vec3.atBottomCenterOf(Bee.this.savedFlowerPos).add(0.0, 0.6f, 0.0);
            if ($$0.distanceTo(Bee.this.position()) > 1.0) {
                this.hoverPos = $$0;
                this.setWantedPos();
                return;
            }
            if (this.hoverPos == null) {
                this.hoverPos = $$0;
            }
            boolean $$1 = Bee.this.position().distanceTo(this.hoverPos) <= 0.1;
            boolean $$2 = true;
            if (!$$1 && this.pollinatingTicks > 600) {
                Bee.this.dropFlower();
                return;
            }
            if ($$1) {
                boolean $$3;
                boolean bl = $$3 = Bee.this.random.nextInt(25) == 0;
                if ($$3) {
                    this.hoverPos = new Vec3($$0.x() + (double)this.getOffset(), $$0.y(), $$0.z() + (double)this.getOffset());
                    Bee.this.navigation.stop();
                } else {
                    $$2 = false;
                }
                Bee.this.getLookControl().setLookAt($$0.x(), $$0.y(), $$0.z());
            }
            if ($$2) {
                this.setWantedPos();
            }
            ++this.successfulPollinatingTicks;
            if (Bee.this.random.nextFloat() < 0.05f && this.successfulPollinatingTicks > this.lastSoundPlayedTick + 60) {
                this.lastSoundPlayedTick = this.successfulPollinatingTicks;
                Bee.this.playSound(SoundEvents.BEE_POLLINATE, 1.0f, 1.0f);
            }
        }

        private void setWantedPos() {
            Bee.this.getMoveControl().setWantedPosition(this.hoverPos.x(), this.hoverPos.y(), this.hoverPos.z(), 0.35f);
        }

        private float getOffset() {
            return (Bee.this.random.nextFloat() * 2.0f - 1.0f) * 0.33333334f;
        }

        private Optional<BlockPos> findNearbyFlower() {
            Iterable<BlockPos> $$0 = BlockPos.withinManhattan(Bee.this.blockPosition(), 5, 5, 5);
            Long2LongOpenHashMap $$1 = new Long2LongOpenHashMap();
            for (BlockPos $$2 : $$0) {
                long $$3 = this.unreachableFlowerCache.getOrDefault($$2.asLong(), Long.MIN_VALUE);
                if (Bee.this.level().getGameTime() < $$3) {
                    $$1.put($$2.asLong(), $$3);
                    continue;
                }
                if (!Bee.attractsBees(Bee.this.level().getBlockState($$2))) continue;
                Path $$4 = Bee.this.navigation.createPath($$2, 1);
                if ($$4 != null && $$4.canReach()) {
                    return Optional.of($$2);
                }
                $$1.put($$2.asLong(), Bee.this.level().getGameTime() + 600L);
            }
            this.unreachableFlowerCache = $$1;
            return Optional.empty();
        }
    }

    class BeeLocateHiveGoal
    extends BaseBeeGoal {
        BeeLocateHiveGoal() {
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.remainingCooldownBeforeLocatingNewHive == 0 && !Bee.this.hasHive() && Bee.this.wantsToEnterHive();
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Bee.this.remainingCooldownBeforeLocatingNewHive = 200;
            List<BlockPos> $$0 = this.findNearbyHivesWithSpace();
            if ($$0.isEmpty()) {
                return;
            }
            for (BlockPos $$1 : $$0) {
                if (Bee.this.goToHiveGoal.isTargetBlacklisted($$1)) continue;
                Bee.this.hivePos = $$1;
                return;
            }
            Bee.this.goToHiveGoal.clearBlacklist();
            Bee.this.hivePos = $$0.get(0);
        }

        private List<BlockPos> findNearbyHivesWithSpace() {
            BlockPos $$02 = Bee.this.blockPosition();
            PoiManager $$12 = ((ServerLevel)Bee.this.level()).getPoiManager();
            Stream<PoiRecord> $$2 = $$12.getInRange($$0 -> $$0.is(PoiTypeTags.BEE_HOME), $$02, 20, PoiManager.Occupancy.ANY);
            return $$2.map(PoiRecord::getPos).filter(Bee.this::doesHiveHaveSpace).sorted(Comparator.comparingDouble($$1 -> $$1.distSqr($$02))).collect(Collectors.toList());
        }
    }

    @VisibleForDebug
    public class BeeGoToHiveGoal
    extends BaseBeeGoal {
        public static final int MAX_TRAVELLING_TICKS = 2400;
        int travellingTicks;
        private static final int MAX_BLACKLISTED_TARGETS = 3;
        final List<BlockPos> blacklistedTargets;
        private @Nullable Path lastPath;
        private static final int TICKS_BEFORE_HIVE_DROP = 60;
        private int ticksStuck;

        BeeGoToHiveGoal() {
            this.blacklistedTargets = Lists.newArrayList();
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.hivePos != null && !Bee.this.isTooFarAway(Bee.this.hivePos) && !Bee.this.hasHome() && Bee.this.wantsToEnterHive() && !this.hasReachedTarget(Bee.this.hivePos) && Bee.this.level().getBlockState(Bee.this.hivePos).is(BlockTags.BEEHIVES);
        }

        @Override
        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        @Override
        public void start() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            Bee.this.navigation.stop();
            Bee.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        @Override
        public void tick() {
            if (Bee.this.hivePos == null) {
                return;
            }
            ++this.travellingTicks;
            if (this.travellingTicks > this.adjustedTickDelay(2400)) {
                this.dropAndBlacklistHive();
                return;
            }
            if (Bee.this.navigation.isInProgress()) {
                return;
            }
            if (Bee.this.closerThan(Bee.this.hivePos, 16)) {
                boolean $$0 = this.pathfindDirectlyTowards(Bee.this.hivePos);
                if (!$$0) {
                    this.dropAndBlacklistHive();
                } else if (this.lastPath != null && Bee.this.navigation.getPath().sameAs(this.lastPath)) {
                    ++this.ticksStuck;
                    if (this.ticksStuck > 60) {
                        Bee.this.dropHive();
                        this.ticksStuck = 0;
                    }
                } else {
                    this.lastPath = Bee.this.navigation.getPath();
                }
                return;
            }
            if (Bee.this.isTooFarAway(Bee.this.hivePos)) {
                Bee.this.dropHive();
                return;
            }
            Bee.this.pathfindRandomlyTowards(Bee.this.hivePos);
        }

        private boolean pathfindDirectlyTowards(BlockPos $$0) {
            int $$1 = Bee.this.closerThan($$0, 3) ? 1 : 2;
            Bee.this.navigation.setMaxVisitedNodesMultiplier(10.0f);
            Bee.this.navigation.moveTo($$0.getX(), $$0.getY(), $$0.getZ(), $$1, 1.0);
            return Bee.this.navigation.getPath() != null && Bee.this.navigation.getPath().canReach();
        }

        boolean isTargetBlacklisted(BlockPos $$0) {
            return this.blacklistedTargets.contains($$0);
        }

        private void blacklistTarget(BlockPos $$0) {
            this.blacklistedTargets.add($$0);
            while (this.blacklistedTargets.size() > 3) {
                this.blacklistedTargets.remove(0);
            }
        }

        void clearBlacklist() {
            this.blacklistedTargets.clear();
        }

        private void dropAndBlacklistHive() {
            if (Bee.this.hivePos != null) {
                this.blacklistTarget(Bee.this.hivePos);
            }
            Bee.this.dropHive();
        }

        private boolean hasReachedTarget(BlockPos $$0) {
            if (Bee.this.closerThan($$0, 2)) {
                return true;
            }
            Path $$1 = Bee.this.navigation.getPath();
            return $$1 != null && $$1.getTarget().equals($$0) && $$1.canReach() && $$1.isDone();
        }
    }

    public class BeeGoToKnownFlowerGoal
    extends BaseBeeGoal {
        private static final int MAX_TRAVELLING_TICKS = 2400;
        int travellingTicks;

        BeeGoToKnownFlowerGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.savedFlowerPos != null && !Bee.this.hasHome() && this.wantsToGoToKnownFlower() && !Bee.this.closerThan(Bee.this.savedFlowerPos, 2);
        }

        @Override
        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        @Override
        public void start() {
            this.travellingTicks = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.travellingTicks = 0;
            Bee.this.navigation.stop();
            Bee.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        @Override
        public void tick() {
            if (Bee.this.savedFlowerPos == null) {
                return;
            }
            ++this.travellingTicks;
            if (this.travellingTicks > this.adjustedTickDelay(2400)) {
                Bee.this.dropFlower();
                return;
            }
            if (Bee.this.navigation.isInProgress()) {
                return;
            }
            if (Bee.this.isTooFarAway(Bee.this.savedFlowerPos)) {
                Bee.this.dropFlower();
                return;
            }
            Bee.this.pathfindRandomlyTowards(Bee.this.savedFlowerPos);
        }

        private boolean wantsToGoToKnownFlower() {
            return Bee.this.ticksWithoutNectarSinceExitingHive > 600;
        }
    }

    class BeeGrowCropGoal
    extends BaseBeeGoal {
        static final int GROW_CHANCE = 30;

        BeeGrowCropGoal() {
        }

        @Override
        public boolean canBeeUse() {
            if (Bee.this.getCropsGrownSincePollination() >= 10) {
                return false;
            }
            if (Bee.this.random.nextFloat() < 0.3f) {
                return false;
            }
            return Bee.this.hasNectar() && Bee.this.isHiveValid();
        }

        @Override
        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        @Override
        public void tick() {
            if (Bee.this.random.nextInt(this.adjustedTickDelay(30)) != 0) {
                return;
            }
            for (int $$0 = 1; $$0 <= 2; ++$$0) {
                BonemealableBlock $$8;
                BlockPos $$1 = Bee.this.blockPosition().below($$0);
                BlockState $$2 = Bee.this.level().getBlockState($$1);
                Block $$3 = $$2.getBlock();
                BlockState $$4 = null;
                if (!$$2.is(BlockTags.BEE_GROWABLES)) continue;
                if ($$3 instanceof CropBlock) {
                    CropBlock $$5 = (CropBlock)$$3;
                    if (!$$5.isMaxAge($$2)) {
                        $$4 = $$5.getStateForAge($$5.getAge($$2) + 1);
                    }
                } else if ($$3 instanceof StemBlock) {
                    int $$6 = $$2.getValue(StemBlock.AGE);
                    if ($$6 < 7) {
                        $$4 = (BlockState)$$2.setValue(StemBlock.AGE, $$6 + 1);
                    }
                } else if ($$2.is(Blocks.SWEET_BERRY_BUSH)) {
                    int $$7 = $$2.getValue(SweetBerryBushBlock.AGE);
                    if ($$7 < 3) {
                        $$4 = (BlockState)$$2.setValue(SweetBerryBushBlock.AGE, $$7 + 1);
                    }
                } else if (($$2.is(Blocks.CAVE_VINES) || $$2.is(Blocks.CAVE_VINES_PLANT)) && ($$8 = (BonemealableBlock)((Object)$$2.getBlock())).isValidBonemealTarget(Bee.this.level(), $$1, $$2)) {
                    $$8.performBonemeal((ServerLevel)Bee.this.level(), Bee.this.random, $$1, $$2);
                    $$4 = Bee.this.level().getBlockState($$1);
                }
                if ($$4 == null) continue;
                Bee.this.level().levelEvent(2011, $$1, 15);
                Bee.this.level().setBlockAndUpdate($$1, $$4);
                Bee.this.incrementNumCropsGrownSincePollination();
            }
        }
    }

    class BeeWanderGoal
    extends Goal {
        BeeWanderGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return Bee.this.navigation.isDone() && Bee.this.random.nextInt(10) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return Bee.this.navigation.isInProgress();
        }

        @Override
        public void start() {
            Vec3 $$0 = this.findPos();
            if ($$0 != null) {
                Bee.this.navigation.moveTo(Bee.this.navigation.createPath(BlockPos.containing($$0), 1), 1.0);
            }
        }

        private @Nullable Vec3 findPos() {
            Vec3 $$2;
            if (Bee.this.isHiveValid() && !Bee.this.closerThan(Bee.this.hivePos, this.getWanderThreshold())) {
                Vec3 $$0 = Vec3.atCenterOf(Bee.this.hivePos);
                Vec3 $$1 = $$0.subtract(Bee.this.position()).normalize();
            } else {
                $$2 = Bee.this.getViewVector(0.0f);
            }
            int $$3 = 8;
            Vec3 $$4 = HoverRandomPos.getPos(Bee.this, 8, 7, $$2.x, $$2.z, 1.5707964f, 3, 1);
            if ($$4 != null) {
                return $$4;
            }
            return AirAndWaterRandomPos.getPos(Bee.this, 8, 4, -2, $$2.x, $$2.z, 1.5707963705062866);
        }

        private int getWanderThreshold() {
            int $$0 = Bee.this.hasHive() || Bee.this.hasSavedFlowerPos() ? 24 : 16;
            return 48 - $$0;
        }
    }

    class BeeHurtByOtherGoal
    extends HurtByTargetGoal {
        BeeHurtByOtherGoal(Bee $$0) {
            super($$0, new Class[0]);
        }

        @Override
        public boolean canContinueToUse() {
            return Bee.this.isAngry() && super.canContinueToUse();
        }

        @Override
        protected void alertOther(Mob $$0, LivingEntity $$1) {
            if ($$0 instanceof Bee && this.mob.hasLineOfSight($$1)) {
                $$0.setTarget($$1);
            }
        }
    }

    static class BeeBecomeAngryTargetGoal
    extends NearestAttackableTargetGoal<Player> {
        BeeBecomeAngryTargetGoal(Bee $$0) {
            super($$0, Player.class, 10, true, false, $$0::isAngryAt);
        }

        @Override
        public boolean canUse() {
            return this.beeCanTarget() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            boolean $$0 = this.beeCanTarget();
            if (!$$0 || this.mob.getTarget() == null) {
                this.targetMob = null;
                return false;
            }
            return super.canContinueToUse();
        }

        private boolean beeCanTarget() {
            Bee $$0 = (Bee)this.mob;
            return $$0.isAngry() && !$$0.hasStung();
        }
    }

    abstract class BaseBeeGoal
    extends Goal {
        BaseBeeGoal() {
        }

        public abstract boolean canBeeUse();

        public abstract boolean canBeeContinueToUse();

        @Override
        public boolean canUse() {
            return this.canBeeUse() && !Bee.this.isAngry();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canBeeContinueToUse() && !Bee.this.isAngry();
        }
    }
}

