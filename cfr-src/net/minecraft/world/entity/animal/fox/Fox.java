/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.fox;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Fox
extends Animal {
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.BYTE);
    private static final int FLAG_SITTING = 1;
    public static final int FLAG_CROUCHING = 4;
    public static final int FLAG_INTERESTED = 8;
    public static final int FLAG_POUNCING = 16;
    private static final int FLAG_SLEEPING = 32;
    private static final int FLAG_FACEPLANTED = 64;
    private static final int FLAG_DEFENDING = 128;
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_TRUSTED_ID_0 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_TRUSTED_ID_1 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    static final Predicate<ItemEntity> ALLOWED_ITEMS = $$0 -> !$$0.hasPickUpDelay() && $$0.isAlive();
    private static final Predicate<Entity> TRUSTED_TARGET_SELECTOR = $$0 -> {
        if ($$0 instanceof LivingEntity) {
            LivingEntity $$1 = (LivingEntity)$$0;
            return $$1.getLastHurtMob() != null && $$1.getLastHurtMobTimestamp() < $$1.tickCount + 600;
        }
        return false;
    };
    static final Predicate<Entity> STALKABLE_PREY = $$0 -> $$0 instanceof Chicken || $$0 instanceof Rabbit;
    private static final Predicate<Entity> AVOID_PLAYERS = $$0 -> !$$0.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test((Entity)$$0);
    private static final int MIN_TICKS_BEFORE_EAT = 600;
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.FOX.getDimensions().scale(0.5f).withEyeHeight(0.2975f);
    private static final Codec<List<EntityReference<LivingEntity>>> TRUSTED_LIST_CODEC = EntityReference.codec().listOf();
    private static final boolean DEFAULT_SLEEPING = false;
    private static final boolean DEFAULT_SITTING = false;
    private static final boolean DEFAULT_CROUCHING = false;
    private Goal landTargetGoal;
    private Goal turtleEggTargetGoal;
    private Goal fishTargetGoal;
    private float interestedAngle;
    private float interestedAngleO;
    float crouchAmount;
    float crouchAmountO;
    private int ticksSinceEaten;

    public Fox(EntityType<? extends Fox> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
        this.lookControl = new FoxLookControl();
        this.moveControl = new FoxMoveControl();
        this.setPathfindingMalus(PathType.DANGER_OTHER, 0.0f);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 0.0f);
        this.setCanPickUpLoot(true);
        this.getNavigation().setRequiredPathLength(32.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_TRUSTED_ID_0, Optional.empty());
        $$0.define(DATA_TRUSTED_ID_1, Optional.empty());
        $$0.define(DATA_TYPE_ID, Variant.DEFAULT.getId());
        $$0.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    protected void registerGoals() {
        this.landTargetGoal = new NearestAttackableTargetGoal<Animal>(this, Animal.class, 10, false, false, ($$0, $$1) -> $$0 instanceof Chicken || $$0 instanceof Rabbit);
        this.turtleEggTargetGoal = new NearestAttackableTargetGoal<Turtle>(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR);
        this.fishTargetGoal = new NearestAttackableTargetGoal<AbstractFish>(this, AbstractFish.class, 20, false, false, ($$0, $$1) -> $$0 instanceof AbstractSchoolingFish);
        this.goalSelector.addGoal(0, new FoxFloatGoal());
        this.goalSelector.addGoal(0, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
        this.goalSelector.addGoal(1, new FaceplantGoal());
        this.goalSelector.addGoal(2, new FoxPanicGoal(2.2));
        this.goalSelector.addGoal(3, new FoxBreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<Player>(this, Player.class, 16.0f, 1.6, 1.4, $$0 -> AVOID_PLAYERS.test((Entity)$$0) && !this.trusts((LivingEntity)$$0) && !this.isDefending()));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<Wolf>(this, Wolf.class, 8.0f, 1.6, 1.4, $$0 -> !((Wolf)$$0).isTame() && !this.isDefending()));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<PolarBear>(this, PolarBear.class, 8.0f, 1.6, 1.4, $$0 -> !this.isDefending()));
        this.goalSelector.addGoal(5, new StalkPreyGoal());
        this.goalSelector.addGoal(6, new FoxPounceGoal());
        this.goalSelector.addGoal(6, new SeekShelterGoal(1.25));
        this.goalSelector.addGoal(7, new FoxMeleeAttackGoal((double)1.2f, true));
        this.goalSelector.addGoal(7, new SleepGoal());
        this.goalSelector.addGoal(8, new FoxFollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(9, new FoxStrollThroughVillageGoal(32, 200));
        this.goalSelector.addGoal(10, new FoxEatBerriesGoal((double)1.2f, 12, 1));
        this.goalSelector.addGoal(10, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(11, new FoxSearchForItemsGoal());
        this.goalSelector.addGoal(12, new FoxLookAtPlayerGoal(this, Player.class, 24.0f));
        this.goalSelector.addGoal(13, new PerchAndSearchGoal());
        this.targetSelector.addGoal(3, new DefendTrustedTargetGoal(LivingEntity.class, false, false, ($$0, $$1) -> TRUSTED_TARGET_SELECTOR.test($$0) && !this.trusts($$0)));
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide() && this.isAlive() && this.isEffectiveAi()) {
            LivingEntity $$2;
            ++this.ticksSinceEaten;
            ItemStack $$0 = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (this.canEat($$0)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack $$1 = $$0.finishUsingItem(this.level(), this);
                    if (!$$1.isEmpty()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, $$1);
                    }
                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1f) {
                    this.playEatingSound();
                    this.level().broadcastEntityEvent(this, (byte)45);
                }
            }
            if (($$2 = this.getTarget()) == null || !$$2.isAlive()) {
                this.setIsCrouching(false);
                this.setIsInterested(false);
            }
        }
        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0f;
            this.zza = 0.0f;
        }
        super.aiStep();
        if (this.isDefending() && this.random.nextFloat() < 0.05f) {
            this.playSound(SoundEvents.FOX_AGGRO, 1.0f, 1.0f);
        }
    }

    @Override
    protected boolean isImmobile() {
        return this.isDeadOrDying();
    }

    private boolean canEat(ItemStack $$0) {
        return this.isConsumableFood($$0) && this.getTarget() == null && this.onGround() && !this.isSleeping();
    }

    private boolean isConsumableFood(ItemStack $$0) {
        return $$0.has(DataComponents.FOOD) && $$0.has(DataComponents.CONSUMABLE);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
        if ($$0.nextFloat() < 0.2f) {
            ItemStack $$8;
            float $$2 = $$0.nextFloat();
            if ($$2 < 0.05f) {
                ItemStack $$3 = new ItemStack(Items.EMERALD);
            } else if ($$2 < 0.2f) {
                ItemStack $$4 = new ItemStack(Items.EGG);
            } else if ($$2 < 0.4f) {
                ItemStack $$5 = $$0.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
            } else if ($$2 < 0.6f) {
                ItemStack $$6 = new ItemStack(Items.WHEAT);
            } else if ($$2 < 0.8f) {
                ItemStack $$7 = new ItemStack(Items.LEATHER);
            } else {
                $$8 = new ItemStack(Items.FEATHER);
            }
            this.setItemSlot(EquipmentSlot.MAINHAND, $$8);
        }
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 45) {
            ItemStack $$1 = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!$$1.isEmpty()) {
                for (int $$2 = 0; $$2 < 8; ++$$2) {
                    Vec3 $$3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, (double)this.random.nextFloat() * 0.1 + 0.1, 0.0).xRot(-this.getXRot() * ((float)Math.PI / 180)).yRot(-this.getYRot() * ((float)Math.PI / 180));
                    this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, $$1), this.getX() + this.getLookAngle().x / 2.0, this.getY(), this.getZ() + this.getLookAngle().z / 2.0, $$3.x, $$3.y + 0.05, $$3.z);
                }
            }
        } else {
            super.handleEntityEvent($$0);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.MAX_HEALTH, 10.0).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.SAFE_FALL_DISTANCE, 5.0).add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public @Nullable Fox getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        Fox $$2 = EntityType.FOX.create($$0, EntitySpawnReason.BREEDING);
        if ($$2 != null) {
            $$2.setVariant(this.random.nextBoolean() ? this.getVariant() : ((Fox)$$1).getVariant());
        }
        return $$2;
    }

    public static boolean checkFoxSpawnRules(EntityType<Fox> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getBlockState($$3.below()).is(BlockTags.FOXES_SPAWNABLE_ON) && Fox.isBrightEnoughToSpawn($$1, $$3);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        Holder<Biome> $$4 = $$0.getBiome(this.blockPosition());
        Variant $$5 = Variant.byBiome($$4);
        boolean $$6 = false;
        if ($$3 instanceof FoxGroupData) {
            FoxGroupData $$7 = (FoxGroupData)$$3;
            $$5 = $$7.variant;
            if ($$7.getGroupSize() >= 2) {
                $$6 = true;
            }
        } else {
            $$3 = new FoxGroupData($$5);
        }
        this.setVariant($$5);
        if ($$6) {
            this.setAge(-24000);
        }
        if ($$0 instanceof ServerLevel) {
            this.setTargetGoals();
        }
        this.populateDefaultEquipmentSlots($$0.getRandom(), $$1);
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    private void setTargetGoals() {
        if (this.getVariant() == Variant.RED) {
            this.targetSelector.addGoal(4, this.landTargetGoal);
            this.targetSelector.addGoal(4, this.turtleEggTargetGoal);
            this.targetSelector.addGoal(6, this.fishTargetGoal);
        } else {
            this.targetSelector.addGoal(4, this.fishTargetGoal);
            this.targetSelector.addGoal(6, this.landTargetGoal);
            this.targetSelector.addGoal(6, this.turtleEggTargetGoal);
        }
    }

    @Override
    protected void playEatingSound() {
        this.playSound(SoundEvents.FOX_EAT, 1.0f, 1.0f);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_TYPE_ID));
    }

    private void setVariant(Variant $$0) {
        this.entityData.set(DATA_TYPE_ID, $$0.getId());
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.FOX_VARIANT) {
            return Fox.castComponentValue($$0, this.getVariant());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.FOX_VARIANT);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.FOX_VARIANT) {
            this.setVariant(Fox.castComponentValue(DataComponents.FOX_VARIANT, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    Stream<EntityReference<LivingEntity>> getTrustedEntities() {
        return Stream.concat(this.entityData.get(DATA_TRUSTED_ID_0).stream(), this.entityData.get(DATA_TRUSTED_ID_1).stream());
    }

    void addTrustedEntity(LivingEntity $$0) {
        this.addTrustedEntity(EntityReference.of($$0));
    }

    private void addTrustedEntity(EntityReference<LivingEntity> $$0) {
        if (this.entityData.get(DATA_TRUSTED_ID_0).isPresent()) {
            this.entityData.set(DATA_TRUSTED_ID_1, Optional.of($$0));
        } else {
            this.entityData.set(DATA_TRUSTED_ID_0, Optional.of($$0));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("Trusted", TRUSTED_LIST_CODEC, this.getTrustedEntities().toList());
        $$0.putBoolean("Sleeping", this.isSleeping());
        $$0.store("Type", Variant.CODEC, this.getVariant());
        $$0.putBoolean("Sitting", this.isSitting());
        $$0.putBoolean("Crouching", this.isCrouching());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.clearTrusted();
        $$0.read("Trusted", TRUSTED_LIST_CODEC).orElse(List.of()).forEach(this::addTrustedEntity);
        this.setSleeping($$0.getBooleanOr("Sleeping", false));
        this.setVariant($$0.read("Type", Variant.CODEC).orElse(Variant.DEFAULT));
        this.setSitting($$0.getBooleanOr("Sitting", false));
        this.setIsCrouching($$0.getBooleanOr("Crouching", false));
        if (this.level() instanceof ServerLevel) {
            this.setTargetGoals();
        }
    }

    private void clearTrusted() {
        this.entityData.set(DATA_TRUSTED_ID_0, Optional.empty());
        this.entityData.set(DATA_TRUSTED_ID_1, Optional.empty());
    }

    public boolean isSitting() {
        return this.getFlag(1);
    }

    public void setSitting(boolean $$0) {
        this.setFlag(1, $$0);
    }

    public boolean isFaceplanted() {
        return this.getFlag(64);
    }

    void setFaceplanted(boolean $$0) {
        this.setFlag(64, $$0);
    }

    boolean isDefending() {
        return this.getFlag(128);
    }

    void setDefending(boolean $$0) {
        this.setFlag(128, $$0);
    }

    @Override
    public boolean isSleeping() {
        return this.getFlag(32);
    }

    void setSleeping(boolean $$0) {
        this.setFlag(32, $$0);
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

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot $$0) {
        return $$0 == EquipmentSlot.MAINHAND && this.canPickUpLoot();
    }

    @Override
    public boolean canHoldItem(ItemStack $$0) {
        ItemStack $$1 = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return $$1.isEmpty() || this.ticksSinceEaten > 0 && this.isConsumableFood($$0) && !this.isConsumableFood($$1);
    }

    private void spitOutItem(ItemStack $$0) {
        if ($$0.isEmpty() || this.level().isClientSide()) {
            return;
        }
        ItemEntity $$1 = new ItemEntity(this.level(), this.getX() + this.getLookAngle().x, this.getY() + 1.0, this.getZ() + this.getLookAngle().z, $$0);
        $$1.setPickUpDelay(40);
        $$1.setThrower(this);
        this.playSound(SoundEvents.FOX_SPIT, 1.0f, 1.0f);
        this.level().addFreshEntity($$1);
    }

    private void dropItemStack(ItemStack $$0) {
        ItemEntity $$1 = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), $$0);
        this.level().addFreshEntity($$1);
    }

    @Override
    protected void pickUpItem(ServerLevel $$0, ItemEntity $$1) {
        ItemStack $$2 = $$1.getItem();
        if (this.canHoldItem($$2)) {
            int $$3 = $$2.getCount();
            if ($$3 > 1) {
                this.dropItemStack($$2.split($$3 - 1));
            }
            this.spitOutItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
            this.onItemPickup($$1);
            this.setItemSlot(EquipmentSlot.MAINHAND, $$2.split(1));
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take($$1, $$2.getCount());
            $$1.discard();
            this.ticksSinceEaten = 0;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isEffectiveAi()) {
            boolean $$0 = this.isInWater();
            if ($$0 || this.getTarget() != null || this.level().isThundering()) {
                this.wakeUp();
            }
            if ($$0 || this.isSleeping()) {
                this.setSitting(false);
            }
            if (this.isFaceplanted() && this.level().random.nextFloat() < 0.2f) {
                BlockPos $$1 = this.blockPosition();
                BlockState $$2 = this.level().getBlockState($$1);
                this.level().levelEvent(2001, $$1, Block.getId($$2));
            }
        }
        this.interestedAngleO = this.interestedAngle;
        this.interestedAngle = this.isInterested() ? (this.interestedAngle += (1.0f - this.interestedAngle) * 0.4f) : (this.interestedAngle += (0.0f - this.interestedAngle) * 0.4f);
        this.crouchAmountO = this.crouchAmount;
        if (this.isCrouching()) {
            this.crouchAmount += 0.2f;
            if (this.crouchAmount > 3.0f) {
                this.crouchAmount = 3.0f;
            }
        } else {
            this.crouchAmount = 0.0f;
        }
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.FOX_FOOD);
    }

    @Override
    protected void onOffspringSpawnedFromEgg(Player $$0, Mob $$1) {
        ((Fox)$$1).addTrustedEntity($$0);
    }

    public boolean isPouncing() {
        return this.getFlag(16);
    }

    public void setIsPouncing(boolean $$0) {
        this.setFlag(16, $$0);
    }

    public boolean isFullyCrouched() {
        return this.crouchAmount == 3.0f;
    }

    public void setIsCrouching(boolean $$0) {
        this.setFlag(4, $$0);
    }

    @Override
    public boolean isCrouching() {
        return this.getFlag(4);
    }

    public void setIsInterested(boolean $$0) {
        this.setFlag(8, $$0);
    }

    public boolean isInterested() {
        return this.getFlag(8);
    }

    public float getHeadRollAngle(float $$0) {
        return Mth.lerp($$0, this.interestedAngleO, this.interestedAngle) * 0.11f * (float)Math.PI;
    }

    public float getCrouchAmount(float $$0) {
        return Mth.lerp($$0, this.crouchAmountO, this.crouchAmount);
    }

    @Override
    public void setTarget(@Nullable LivingEntity $$0) {
        if (this.isDefending() && $$0 == null) {
            this.setDefending(false);
        }
        super.setTarget($$0);
    }

    void wakeUp() {
        this.setSleeping(false);
    }

    void clearStates() {
        this.setIsInterested(false);
        this.setIsCrouching(false);
        this.setSitting(false);
        this.setSleeping(false);
        this.setDefending(false);
        this.setFaceplanted(false);
    }

    boolean canMove() {
        return !this.isSleeping() && !this.isSitting() && !this.isFaceplanted();
    }

    @Override
    public void playAmbientSound() {
        SoundEvent $$0 = this.getAmbientSound();
        if ($$0 == SoundEvents.FOX_SCREECH) {
            this.playSound($$0, 2.0f, this.getVoicePitch());
        } else {
            super.playAmbientSound();
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        List<Entity> $$0;
        if (this.isSleeping()) {
            return SoundEvents.FOX_SLEEP;
        }
        if (!this.level().isBrightOutside() && this.random.nextFloat() < 0.1f && ($$0 = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0, 16.0, 16.0), EntitySelector.NO_SPECTATORS)).isEmpty()) {
            return SoundEvents.FOX_SCREECH;
        }
        return SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    boolean trusts(LivingEntity $$0) {
        return this.getTrustedEntities().anyMatch($$1 -> $$1.matches($$0));
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel $$0, DamageSource $$1) {
        ItemStack $$2 = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!$$2.isEmpty()) {
            this.spawnAtLocation($$0, $$2);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        super.dropAllDeathLoot($$0, $$1);
    }

    public static boolean isPathClear(Fox $$0, LivingEntity $$1) {
        double $$2 = $$1.getZ() - $$0.getZ();
        double $$3 = $$1.getX() - $$0.getX();
        double $$4 = $$2 / $$3;
        int $$5 = 6;
        for (int $$6 = 0; $$6 < 6; ++$$6) {
            double $$7 = $$4 == 0.0 ? 0.0 : $$2 * (double)((float)$$6 / 6.0f);
            double $$8 = $$4 == 0.0 ? $$3 * (double)((float)$$6 / 6.0f) : $$7 / $$4;
            for (int $$9 = 1; $$9 < 4; ++$$9) {
                if ($$0.level().getBlockState(BlockPos.containing($$0.getX() + $$8, $$0.getY() + (double)$$9, $$0.getZ() + $$7)).canBeReplaced()) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.55f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    public class FoxLookControl
    extends LookControl {
        public FoxLookControl() {
            super(Fox.this);
        }

        @Override
        public void tick() {
            if (!Fox.this.isSleeping()) {
                super.tick();
            }
        }

        @Override
        protected boolean resetXRotOnTick() {
            return !Fox.this.isPouncing() && !Fox.this.isCrouching() && !Fox.this.isInterested() && !Fox.this.isFaceplanted();
        }
    }

    class FoxMoveControl
    extends MoveControl {
        public FoxMoveControl() {
            super(Fox.this);
        }

        @Override
        public void tick() {
            if (Fox.this.canMove()) {
                super.tick();
            }
        }
    }

    public static enum Variant implements StringRepresentable
    {
        RED(0, "red"),
        SNOW(1, "snow");

        public static final Variant DEFAULT;
        public static final StringRepresentable.EnumCodec<Variant> CODEC;
        private static final IntFunction<Variant> BY_ID;
        public static final StreamCodec<ByteBuf, Variant> STREAM_CODEC;
        private final int id;
        private final String name;

        private Variant(int $$0, String $$1) {
            this.id = $$0;
            this.name = $$1;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public static Variant byId(int $$0) {
            return BY_ID.apply($$0);
        }

        public static Variant byBiome(Holder<Biome> $$0) {
            return $$0.is(BiomeTags.SPAWNS_SNOW_FOXES) ? SNOW : RED;
        }

        static {
            DEFAULT = RED;
            CODEC = StringRepresentable.fromEnum(Variant::values);
            BY_ID = ByIdMap.continuous(Variant::getId, Variant.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::getId);
        }
    }

    class FoxFloatGoal
    extends FloatGoal {
        public FoxFloatGoal() {
            super(Fox.this);
        }

        @Override
        public void start() {
            super.start();
            Fox.this.clearStates();
        }

        @Override
        public boolean canUse() {
            return Fox.this.isInWater() && Fox.this.getFluidHeight(FluidTags.WATER) > 0.25 || Fox.this.isInLava();
        }
    }

    class FaceplantGoal
    extends Goal {
        int countdown;

        public FaceplantGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return Fox.this.isFaceplanted();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && this.countdown > 0;
        }

        @Override
        public void start() {
            this.countdown = this.adjustedTickDelay(40);
        }

        @Override
        public void stop() {
            Fox.this.setFaceplanted(false);
        }

        @Override
        public void tick() {
            --this.countdown;
        }
    }

    class FoxPanicGoal
    extends PanicGoal {
        public FoxPanicGoal(double $$0) {
            super(Fox.this, $$0);
        }

        @Override
        public boolean shouldPanic() {
            return !Fox.this.isDefending() && super.shouldPanic();
        }
    }

    class FoxBreedGoal
    extends BreedGoal {
        public FoxBreedGoal(Fox fox, double $$0) {
            super(fox, $$0);
        }

        @Override
        public void start() {
            ((Fox)this.animal).clearStates();
            ((Fox)this.partner).clearStates();
            super.start();
        }

        @Override
        protected void breed() {
            Fox $$0 = (Fox)this.animal.getBreedOffspring(this.level, this.partner);
            if ($$0 == null) {
                return;
            }
            ServerPlayer $$1 = this.animal.getLoveCause();
            ServerPlayer $$2 = this.partner.getLoveCause();
            ServerPlayer $$3 = $$1;
            if ($$1 != null) {
                $$0.addTrustedEntity($$1);
            } else {
                $$3 = $$2;
            }
            if ($$2 != null && $$1 != $$2) {
                $$0.addTrustedEntity($$2);
            }
            if ($$3 != null) {
                $$3.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger($$3, this.animal, this.partner, $$0);
            }
            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            $$0.setAge(-24000);
            $$0.snapTo(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0f, 0.0f);
            this.level.addFreshEntityWithPassengers($$0);
            this.level.broadcastEntityEvent(this.animal, (byte)18);
            if (this.level.getGameRules().get(GameRules.MOB_DROPS).booleanValue()) {
                this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
            }
        }
    }

    class StalkPreyGoal
    extends Goal {
        public StalkPreyGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (Fox.this.isSleeping()) {
                return false;
            }
            LivingEntity $$0 = Fox.this.getTarget();
            return $$0 != null && $$0.isAlive() && STALKABLE_PREY.test($$0) && Fox.this.distanceToSqr($$0) > 36.0 && !Fox.this.isCrouching() && !Fox.this.isInterested() && !Fox.this.jumping;
        }

        @Override
        public void start() {
            Fox.this.setSitting(false);
            Fox.this.setFaceplanted(false);
        }

        @Override
        public void stop() {
            LivingEntity $$0 = Fox.this.getTarget();
            if ($$0 != null && Fox.isPathClear(Fox.this, $$0)) {
                Fox.this.setIsInterested(true);
                Fox.this.setIsCrouching(true);
                Fox.this.getNavigation().stop();
                Fox.this.getLookControl().setLookAt($$0, Fox.this.getMaxHeadYRot(), Fox.this.getMaxHeadXRot());
            } else {
                Fox.this.setIsInterested(false);
                Fox.this.setIsCrouching(false);
            }
        }

        @Override
        public void tick() {
            LivingEntity $$0 = Fox.this.getTarget();
            if ($$0 == null) {
                return;
            }
            Fox.this.getLookControl().setLookAt($$0, Fox.this.getMaxHeadYRot(), Fox.this.getMaxHeadXRot());
            if (Fox.this.distanceToSqr($$0) <= 36.0) {
                Fox.this.setIsInterested(true);
                Fox.this.setIsCrouching(true);
                Fox.this.getNavigation().stop();
            } else {
                Fox.this.getNavigation().moveTo($$0, 1.5);
            }
        }
    }

    public class FoxPounceGoal
    extends JumpGoal {
        @Override
        public boolean canUse() {
            if (!Fox.this.isFullyCrouched()) {
                return false;
            }
            LivingEntity $$0 = Fox.this.getTarget();
            if ($$0 == null || !$$0.isAlive()) {
                return false;
            }
            if ($$0.getMotionDirection() != $$0.getDirection()) {
                return false;
            }
            boolean $$1 = Fox.isPathClear(Fox.this, $$0);
            if (!$$1) {
                Fox.this.getNavigation().createPath($$0, 0);
                Fox.this.setIsCrouching(false);
                Fox.this.setIsInterested(false);
            }
            return $$1;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity $$0 = Fox.this.getTarget();
            if ($$0 == null || !$$0.isAlive()) {
                return false;
            }
            double $$1 = Fox.this.getDeltaMovement().y;
            return !($$1 * $$1 < (double)0.05f && Math.abs(Fox.this.getXRot()) < 15.0f && Fox.this.onGround() || Fox.this.isFaceplanted());
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public void start() {
            Fox.this.setJumping(true);
            Fox.this.setIsPouncing(true);
            Fox.this.setIsInterested(false);
            LivingEntity $$0 = Fox.this.getTarget();
            if ($$0 != null) {
                Fox.this.getLookControl().setLookAt($$0, 60.0f, 30.0f);
                Vec3 $$1 = new Vec3($$0.getX() - Fox.this.getX(), $$0.getY() - Fox.this.getY(), $$0.getZ() - Fox.this.getZ()).normalize();
                Fox.this.setDeltaMovement(Fox.this.getDeltaMovement().add($$1.x * 0.8, 0.9, $$1.z * 0.8));
            }
            Fox.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            Fox.this.setIsCrouching(false);
            Fox.this.crouchAmount = 0.0f;
            Fox.this.crouchAmountO = 0.0f;
            Fox.this.setIsInterested(false);
            Fox.this.setIsPouncing(false);
        }

        @Override
        public void tick() {
            LivingEntity $$0 = Fox.this.getTarget();
            if ($$0 != null) {
                Fox.this.getLookControl().setLookAt($$0, 60.0f, 30.0f);
            }
            if (!Fox.this.isFaceplanted()) {
                Vec3 $$1 = Fox.this.getDeltaMovement();
                if ($$1.y * $$1.y < (double)0.03f && Fox.this.getXRot() != 0.0f) {
                    Fox.this.setXRot(Mth.rotLerp(0.2f, Fox.this.getXRot(), 0.0f));
                } else {
                    double $$2 = $$1.horizontalDistance();
                    double $$3 = Math.signum(-$$1.y) * Math.acos($$2 / $$1.length()) * 57.2957763671875;
                    Fox.this.setXRot((float)$$3);
                }
            }
            if ($$0 != null && Fox.this.distanceTo($$0) <= 2.0f) {
                Fox.this.doHurtTarget(FoxPounceGoal.getServerLevel(Fox.this.level()), $$0);
            } else if (Fox.this.getXRot() > 0.0f && Fox.this.onGround() && (float)Fox.this.getDeltaMovement().y != 0.0f && Fox.this.level().getBlockState(Fox.this.blockPosition()).is(Blocks.SNOW)) {
                Fox.this.setXRot(60.0f);
                Fox.this.setTarget(null);
                Fox.this.setFaceplanted(true);
            }
        }
    }

    class SeekShelterGoal
    extends FleeSunGoal {
        private int interval;

        public SeekShelterGoal(double $$0) {
            super(Fox.this, $$0);
            this.interval = SeekShelterGoal.reducedTickDelay(100);
        }

        @Override
        public boolean canUse() {
            if (Fox.this.isSleeping() || this.mob.getTarget() != null) {
                return false;
            }
            if (Fox.this.level().isThundering() && Fox.this.level().canSeeSky(this.mob.blockPosition())) {
                return this.setWantedPos();
            }
            if (this.interval > 0) {
                --this.interval;
                return false;
            }
            this.interval = 100;
            BlockPos $$0 = this.mob.blockPosition();
            return Fox.this.level().isBrightOutside() && Fox.this.level().canSeeSky($$0) && !((ServerLevel)Fox.this.level()).isVillage($$0) && this.setWantedPos();
        }

        @Override
        public void start() {
            Fox.this.clearStates();
            super.start();
        }
    }

    class FoxMeleeAttackGoal
    extends MeleeAttackGoal {
        public FoxMeleeAttackGoal(double $$0, boolean $$1) {
            super(Fox.this, $$0, $$1);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity $$0) {
            if (this.canPerformAttack($$0)) {
                this.resetAttackCooldown();
                this.mob.doHurtTarget(FoxMeleeAttackGoal.getServerLevel(this.mob), $$0);
                Fox.this.playSound(SoundEvents.FOX_BITE, 1.0f, 1.0f);
            }
        }

        @Override
        public void start() {
            Fox.this.setIsInterested(false);
            super.start();
        }

        @Override
        public boolean canUse() {
            return !Fox.this.isSitting() && !Fox.this.isSleeping() && !Fox.this.isCrouching() && !Fox.this.isFaceplanted() && super.canUse();
        }
    }

    class SleepGoal
    extends FoxBehaviorGoal {
        private static final int WAIT_TIME_BEFORE_SLEEP = SleepGoal.reducedTickDelay(140);
        private int countdown;

        public SleepGoal() {
            this.countdown = Fox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (Fox.this.xxa != 0.0f || Fox.this.yya != 0.0f || Fox.this.zza != 0.0f) {
                return false;
            }
            return this.canSleep() || Fox.this.isSleeping();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canSleep();
        }

        private boolean canSleep() {
            if (this.countdown > 0) {
                --this.countdown;
                return false;
            }
            return Fox.this.level().isBrightOutside() && this.hasShelter() && !this.alertable() && !Fox.this.isInPowderSnow;
        }

        @Override
        public void stop() {
            this.countdown = Fox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            Fox.this.clearStates();
        }

        @Override
        public void start() {
            Fox.this.setSitting(false);
            Fox.this.setIsCrouching(false);
            Fox.this.setIsInterested(false);
            Fox.this.setJumping(false);
            Fox.this.setSleeping(true);
            Fox.this.getNavigation().stop();
            Fox.this.getMoveControl().setWantedPosition(Fox.this.getX(), Fox.this.getY(), Fox.this.getZ(), 0.0);
        }
    }

    static class FoxFollowParentGoal
    extends FollowParentGoal {
        private final Fox fox;

        public FoxFollowParentGoal(Fox $$0, double $$1) {
            super($$0, $$1);
            this.fox = $$0;
        }

        @Override
        public boolean canUse() {
            return !this.fox.isDefending() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.fox.isDefending() && super.canContinueToUse();
        }

        @Override
        public void start() {
            this.fox.clearStates();
            super.start();
        }
    }

    class FoxStrollThroughVillageGoal
    extends StrollThroughVillageGoal {
        public FoxStrollThroughVillageGoal(int $$0, int $$1) {
            super(Fox.this, $$1);
        }

        @Override
        public void start() {
            Fox.this.clearStates();
            super.start();
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.canFoxMove();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.canFoxMove();
        }

        private boolean canFoxMove() {
            return !Fox.this.isSleeping() && !Fox.this.isSitting() && !Fox.this.isDefending() && Fox.this.getTarget() == null;
        }
    }

    public class FoxEatBerriesGoal
    extends MoveToBlockGoal {
        private static final int WAIT_TICKS = 40;
        protected int ticksWaited;

        public FoxEatBerriesGoal(double $$1, int $$2, int $$3) {
            super(Fox.this, $$1, $$2, $$3);
        }

        @Override
        public double acceptedDistance() {
            return 2.0;
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 100 == 0;
        }

        @Override
        protected boolean isValidTarget(LevelReader $$0, BlockPos $$1) {
            BlockState $$2 = $$0.getBlockState($$1);
            return $$2.is(Blocks.SWEET_BERRY_BUSH) && $$2.getValue(SweetBerryBushBlock.AGE) >= 2 || CaveVines.hasGlowBerries($$2);
        }

        @Override
        public void tick() {
            if (this.isReachedTarget()) {
                if (this.ticksWaited >= 40) {
                    this.onReachedTarget();
                } else {
                    ++this.ticksWaited;
                }
            } else if (!this.isReachedTarget() && Fox.this.random.nextFloat() < 0.05f) {
                Fox.this.playSound(SoundEvents.FOX_SNIFF, 1.0f, 1.0f);
            }
            super.tick();
        }

        protected void onReachedTarget() {
            if (!FoxEatBerriesGoal.getServerLevel(Fox.this.level()).getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                return;
            }
            BlockState $$0 = Fox.this.level().getBlockState(this.blockPos);
            if ($$0.is(Blocks.SWEET_BERRY_BUSH)) {
                this.pickSweetBerries($$0);
            } else if (CaveVines.hasGlowBerries($$0)) {
                this.pickGlowBerry($$0);
            }
        }

        private void pickGlowBerry(BlockState $$0) {
            CaveVines.use(Fox.this, $$0, Fox.this.level(), this.blockPos);
        }

        private void pickSweetBerries(BlockState $$0) {
            int $$1 = $$0.getValue(SweetBerryBushBlock.AGE);
            $$0.setValue(SweetBerryBushBlock.AGE, 1);
            int $$2 = 1 + Fox.this.level().random.nextInt(2) + ($$1 == 3 ? 1 : 0);
            ItemStack $$3 = Fox.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if ($$3.isEmpty()) {
                Fox.this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
                --$$2;
            }
            if ($$2 > 0) {
                Block.popResource(Fox.this.level(), this.blockPos, new ItemStack(Items.SWEET_BERRIES, $$2));
            }
            Fox.this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0f, 1.0f);
            Fox.this.level().setBlock(this.blockPos, (BlockState)$$0.setValue(SweetBerryBushBlock.AGE, 1), 2);
            Fox.this.level().gameEvent(GameEvent.BLOCK_CHANGE, this.blockPos, GameEvent.Context.of(Fox.this));
        }

        @Override
        public boolean canUse() {
            return !Fox.this.isSleeping() && super.canUse();
        }

        @Override
        public void start() {
            this.ticksWaited = 0;
            Fox.this.setSitting(false);
            super.start();
        }
    }

    class FoxSearchForItemsGoal
    extends Goal {
        public FoxSearchForItemsGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                return false;
            }
            if (Fox.this.getTarget() != null || Fox.this.getLastHurtByMob() != null) {
                return false;
            }
            if (!Fox.this.canMove()) {
                return false;
            }
            if (Fox.this.getRandom().nextInt(FoxSearchForItemsGoal.reducedTickDelay(10)) != 0) {
                return false;
            }
            List<ItemEntity> $$0 = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS);
            return !$$0.isEmpty() && Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
        }

        @Override
        public void tick() {
            List<ItemEntity> $$0 = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS);
            ItemStack $$1 = Fox.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if ($$1.isEmpty() && !$$0.isEmpty()) {
                Fox.this.getNavigation().moveTo($$0.get(0), (double)1.2f);
            }
        }

        @Override
        public void start() {
            List<ItemEntity> $$0 = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS);
            if (!$$0.isEmpty()) {
                Fox.this.getNavigation().moveTo($$0.get(0), (double)1.2f);
            }
        }
    }

    class FoxLookAtPlayerGoal
    extends LookAtPlayerGoal {
        public FoxLookAtPlayerGoal(Mob $$0, Class<? extends LivingEntity> $$1, float $$2) {
            super($$0, $$1, $$2);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !Fox.this.isFaceplanted() && !Fox.this.isInterested();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !Fox.this.isFaceplanted() && !Fox.this.isInterested();
        }
    }

    class PerchAndSearchGoal
    extends FoxBehaviorGoal {
        private double relX;
        private double relZ;
        private int lookTime;
        private int looksRemaining;

        public PerchAndSearchGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return Fox.this.getLastHurtByMob() == null && Fox.this.getRandom().nextFloat() < 0.02f && !Fox.this.isSleeping() && Fox.this.getTarget() == null && Fox.this.getNavigation().isDone() && !this.alertable() && !Fox.this.isPouncing() && !Fox.this.isCrouching();
        }

        @Override
        public boolean canContinueToUse() {
            return this.looksRemaining > 0;
        }

        @Override
        public void start() {
            this.resetLook();
            this.looksRemaining = 2 + Fox.this.getRandom().nextInt(3);
            Fox.this.setSitting(true);
            Fox.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            Fox.this.setSitting(false);
        }

        @Override
        public void tick() {
            --this.lookTime;
            if (this.lookTime <= 0) {
                --this.looksRemaining;
                this.resetLook();
            }
            Fox.this.getLookControl().setLookAt(Fox.this.getX() + this.relX, Fox.this.getEyeY(), Fox.this.getZ() + this.relZ, Fox.this.getMaxHeadYRot(), Fox.this.getMaxHeadXRot());
        }

        private void resetLook() {
            double $$0 = Math.PI * 2 * Fox.this.getRandom().nextDouble();
            this.relX = Math.cos($$0);
            this.relZ = Math.sin($$0);
            this.lookTime = this.adjustedTickDelay(80 + Fox.this.getRandom().nextInt(20));
        }
    }

    class DefendTrustedTargetGoal
    extends NearestAttackableTargetGoal<LivingEntity> {
        private @Nullable LivingEntity trustedLastHurtBy;
        private @Nullable LivingEntity trustedLastHurt;
        private int timestamp;

        public DefendTrustedTargetGoal(Class<LivingEntity> $$0, boolean $$1, @Nullable boolean $$2, TargetingConditions.Selector $$3) {
            super(Fox.this, $$0, 10, $$1, $$2, $$3);
        }

        @Override
        public boolean canUse() {
            if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
                return false;
            }
            ServerLevel $$0 = DefendTrustedTargetGoal.getServerLevel(Fox.this.level());
            for (EntityReference<LivingEntity> $$1 : Fox.this.getTrustedEntities().toList()) {
                LivingEntity $$2 = $$1.getEntity($$0, LivingEntity.class);
                if ($$2 == null) continue;
                this.trustedLastHurt = $$2;
                this.trustedLastHurtBy = $$2.getLastHurtByMob();
                int $$3 = $$2.getLastHurtByMobTimestamp();
                return $$3 != this.timestamp && this.canAttack(this.trustedLastHurtBy, this.targetConditions);
            }
            return false;
        }

        @Override
        public void start() {
            this.setTarget(this.trustedLastHurtBy);
            this.target = this.trustedLastHurtBy;
            if (this.trustedLastHurt != null) {
                this.timestamp = this.trustedLastHurt.getLastHurtByMobTimestamp();
            }
            Fox.this.playSound(SoundEvents.FOX_AGGRO, 1.0f, 1.0f);
            Fox.this.setDefending(true);
            Fox.this.wakeUp();
            super.start();
        }
    }

    public static class FoxGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        public FoxGroupData(Variant $$0) {
            super(false);
            this.variant = $$0;
        }
    }

    abstract class FoxBehaviorGoal
    extends Goal {
        private final TargetingConditions alertableTargeting;

        FoxBehaviorGoal() {
            this.alertableTargeting = TargetingConditions.forCombat().range(12.0).ignoreLineOfSight().selector(new FoxAlertableEntitiesSelector());
        }

        protected boolean hasShelter() {
            BlockPos $$0 = BlockPos.containing(Fox.this.getX(), Fox.this.getBoundingBox().maxY, Fox.this.getZ());
            return !Fox.this.level().canSeeSky($$0) && Fox.this.getWalkTargetValue($$0) >= 0.0f;
        }

        protected boolean alertable() {
            return !FoxBehaviorGoal.getServerLevel(Fox.this.level()).getNearbyEntities(LivingEntity.class, this.alertableTargeting, Fox.this, Fox.this.getBoundingBox().inflate(12.0, 6.0, 12.0)).isEmpty();
        }
    }

    public class FoxAlertableEntitiesSelector
    implements TargetingConditions.Selector {
        @Override
        public boolean test(LivingEntity $$0, ServerLevel $$1) {
            Player $$2;
            if ($$0 instanceof Fox) {
                return false;
            }
            if ($$0 instanceof Chicken || $$0 instanceof Rabbit || $$0 instanceof Monster) {
                return true;
            }
            if ($$0 instanceof TamableAnimal) {
                return !((TamableAnimal)$$0).isTame();
            }
            if ($$0 instanceof Player && (($$2 = (Player)$$0).isSpectator() || $$2.isCreative())) {
                return false;
            }
            if (Fox.this.trusts($$0)) {
                return false;
            }
            return !$$0.isSleeping() && !$$0.isDiscrete();
        }
    }
}

