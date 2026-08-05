/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.equine;

import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStandGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractHorse
extends Animal
implements HasCustomInventoryScreen,
OwnableEntity,
PlayerRideableJumping {
    public static final int CHEST_SLOT_OFFSET = 499;
    public static final int INVENTORY_SLOT_OFFSET = 500;
    public static final double BREEDING_CROSS_FACTOR = 0.15;
    private static final float MIN_MOVEMENT_SPEED = (float)AbstractHorse.generateSpeed(() -> 0.0);
    private static final float MAX_MOVEMENT_SPEED = (float)AbstractHorse.generateSpeed(() -> 1.0);
    private static final float MIN_JUMP_STRENGTH = (float)AbstractHorse.generateJumpStrength(() -> 0.0);
    private static final float MAX_JUMP_STRENGTH = (float)AbstractHorse.generateJumpStrength(() -> 1.0);
    private static final float MIN_HEALTH = AbstractHorse.generateMaxHealth($$0 -> 0);
    private static final float MAX_HEALTH = AbstractHorse.generateMaxHealth($$0 -> $$0 - 1);
    private static final float BACKWARDS_MOVE_SPEED_FACTOR = 0.25f;
    private static final float SIDEWAYS_MOVE_SPEED_FACTOR = 0.5f;
    private static final TargetingConditions.Selector PARENT_HORSE_SELECTOR = ($$0, $$1) -> {
        AbstractHorse $$2;
        return $$0 instanceof AbstractHorse && ($$2 = (AbstractHorse)$$0).isBred();
    };
    private static final TargetingConditions MOMMY_TARGETING = TargetingConditions.forNonCombat().range(16.0).ignoreLineOfSight().selector(PARENT_HORSE_SELECTOR);
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BYTE);
    private static final int FLAG_TAME = 2;
    private static final int FLAG_BRED = 8;
    private static final int FLAG_EATING = 16;
    private static final int FLAG_STANDING = 32;
    private static final int FLAG_OPEN_MOUTH = 64;
    public static final int INVENTORY_ROWS = 3;
    private static final int DEFAULT_TEMPER = 0;
    private static final boolean DEFAULT_EATING_HAYSTACK = false;
    private static final boolean DEFAULT_BRED = false;
    private static final boolean DEFAULT_TAME = false;
    private int eatingCounter;
    private int mouthCounter;
    private int standCounter;
    public int tailCounter;
    public int sprintCounter;
    protected SimpleContainer inventory;
    protected int temper = 0;
    protected float playerJumpPendingScale;
    protected boolean allowStandSliding;
    private float eatAnim;
    private float eatAnimO;
    private float standAnim;
    private float standAnimO;
    private float mouthAnim;
    private float mouthAnimO;
    protected boolean canGallop = true;
    protected int gallopSoundCounter;
    private @Nullable EntityReference<LivingEntity> owner;

    protected AbstractHorse(EntityType<? extends AbstractHorse> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
        this.createInventory();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MountPanicGoal(1.2));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0, AbstractHorse.class));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        if (this.canPerformRearing()) {
            this.goalSelector.addGoal(9, new RandomStandGoal(this));
        }
        this.addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, $$0 -> $$0.is(ItemTags.HORSE_TEMPT_ITEMS), false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_ID_FLAGS, (byte)0);
    }

    protected boolean getFlag(int $$0) {
        return (this.entityData.get(DATA_ID_FLAGS) & $$0) != 0;
    }

    protected void setFlag(int $$0, boolean $$1) {
        byte $$2 = this.entityData.get(DATA_ID_FLAGS);
        if ($$1) {
            this.entityData.set(DATA_ID_FLAGS, (byte)($$2 | $$0));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte)($$2 & ~$$0));
        }
    }

    public boolean isTamed() {
        return this.getFlag(2);
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return this.owner;
    }

    public void setOwner(@Nullable LivingEntity $$0) {
        this.owner = EntityReference.of($$0);
    }

    public void setTamed(boolean $$0) {
        this.setFlag(2, $$0);
    }

    @Override
    public void onElasticLeashPull() {
        super.onElasticLeashPull();
        if (this.isEating()) {
            this.setEating(false);
        }
    }

    @Override
    public boolean supportQuadLeash() {
        return true;
    }

    @Override
    public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.04, 0.52, 0.23, 0.87);
    }

    public boolean isEating() {
        return this.getFlag(16);
    }

    public boolean isStanding() {
        return this.getFlag(32);
    }

    public boolean isBred() {
        return this.getFlag(8);
    }

    public void setBred(boolean $$0) {
        this.setFlag(8, $$0);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot $$0) {
        if ($$0 == EquipmentSlot.SADDLE) {
            return this.isAlive() && !this.isBaby() && this.isTamed();
        }
        return super.canUseSlot($$0);
    }

    public void equipBodyArmor(Player $$0, ItemStack $$1) {
        if (this.isEquippableInSlot($$1, EquipmentSlot.BODY)) {
            this.setBodyArmorItem($$1.consumeAndReturn(1, $$0));
        }
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot $$0) {
        return ($$0 == EquipmentSlot.BODY || $$0 == EquipmentSlot.SADDLE) && this.isTamed() || super.canDispenserEquipIntoSlot($$0);
    }

    public int getTemper() {
        return this.temper;
    }

    public void setTemper(int $$0) {
        this.temper = $$0;
    }

    public int modifyTemper(int $$0) {
        int $$1 = Mth.clamp(this.getTemper() + $$0, 0, this.getMaxTemper());
        this.setTemper($$1);
        return $$1;
    }

    @Override
    public boolean isPushable() {
        return !this.isVehicle();
    }

    private void eating() {
        SoundEvent $$0;
        this.openMouth();
        if (!this.isSilent() && ($$0 = this.getEatingSound()) != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), $$0, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
    }

    @Override
    public boolean causeFallDamage(double $$0, float $$1, DamageSource $$2) {
        int $$3;
        if ($$0 > 1.0) {
            this.playSound(SoundEvents.HORSE_LAND, 0.4f, 1.0f);
        }
        if (($$3 = this.calculateFallDamage($$0, $$1)) <= 0) {
            return false;
        }
        this.hurt($$2, $$3);
        this.propagateFallToPassengers($$0, $$1, $$2);
        this.playBlockFallSound();
        return true;
    }

    public final int getInventorySize() {
        return AbstractMountInventoryMenu.getInventorySize(this.getInventoryColumns());
    }

    protected void createInventory() {
        SimpleContainer $$0 = this.inventory;
        this.inventory = new SimpleContainer(this.getInventorySize());
        if ($$0 != null) {
            int $$1 = Math.min($$0.getContainerSize(), this.inventory.getContainerSize());
            for (int $$2 = 0; $$2 < $$1; ++$$2) {
                ItemStack $$3 = $$0.getItem($$2);
                if ($$3.isEmpty()) continue;
                this.inventory.setItem($$2, $$3.copy());
            }
        }
    }

    @Override
    protected Holder<SoundEvent> getEquipSound(EquipmentSlot $$0, ItemStack $$1, Equippable $$2) {
        if ($$0 == EquipmentSlot.SADDLE) {
            return SoundEvents.HORSE_SADDLE;
        }
        return super.getEquipSound($$0, $$1, $$2);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        boolean $$3 = super.hurtServer($$0, $$1, $$2);
        if ($$3 && this.random.nextInt(3) == 0) {
            this.standIfPossible();
        }
        return $$3;
    }

    protected boolean canPerformRearing() {
        return true;
    }

    protected @Nullable SoundEvent getEatingSound() {
        return null;
    }

    protected @Nullable SoundEvent getAngrySound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        if ($$1.liquid()) {
            return;
        }
        BlockState $$2 = this.level().getBlockState($$0.above());
        SoundType $$3 = $$1.getSoundType();
        if ($$2.is(Blocks.SNOW)) {
            $$3 = $$2.getSoundType();
        }
        if (this.isVehicle() && this.canGallop) {
            ++this.gallopSoundCounter;
            if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
                this.playGallopSound($$3);
            } else if (this.gallopSoundCounter <= 5) {
                this.playSound(SoundEvents.HORSE_STEP_WOOD, $$3.getVolume() * 0.15f, $$3.getPitch());
            }
        } else if (this.isWoodSoundType($$3)) {
            this.playSound(SoundEvents.HORSE_STEP_WOOD, $$3.getVolume() * 0.15f, $$3.getPitch());
        } else {
            this.playSound(SoundEvents.HORSE_STEP, $$3.getVolume() * 0.15f, $$3.getPitch());
        }
    }

    private boolean isWoodSoundType(SoundType $$0) {
        return $$0 == SoundType.WOOD || $$0 == SoundType.NETHER_WOOD || $$0 == SoundType.STEM || $$0 == SoundType.CHERRY_WOOD || $$0 == SoundType.BAMBOO_WOOD;
    }

    protected void playGallopSound(SoundType $$0) {
        this.playSound(SoundEvents.HORSE_GALLOP, $$0.getVolume() * 0.15f, $$0.getPitch());
    }

    public static AttributeSupplier.Builder createBaseHorseAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.JUMP_STRENGTH, 0.7).add(Attributes.MAX_HEALTH, 53.0).add(Attributes.MOVEMENT_SPEED, 0.225f).add(Attributes.STEP_HEIGHT, 1.0).add(Attributes.SAFE_FALL_DISTANCE, 6.0).add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.5);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 6;
    }

    public int getMaxTemper() {
        return 100;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    public void openCustomInventoryScreen(Player $$0) {
        if (!this.level().isClientSide() && (!this.isVehicle() || this.hasPassenger($$0)) && this.isTamed()) {
            $$0.openHorseInventory(this, this.inventory);
        }
    }

    public InteractionResult fedFood(Player $$0, ItemStack $$1) {
        boolean $$2 = this.handleEating($$0, $$1);
        if ($$2) {
            $$1.consume(1, $$0);
        }
        return $$2 || this.level().isClientSide() ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
    }

    protected boolean handleEating(Player $$0, ItemStack $$1) {
        boolean $$2 = false;
        float $$3 = 0.0f;
        int $$4 = 0;
        int $$5 = 0;
        if ($$1.is(Items.WHEAT)) {
            $$3 = 2.0f;
            $$4 = 20;
            $$5 = 3;
        } else if ($$1.is(Items.SUGAR)) {
            $$3 = 1.0f;
            $$4 = 30;
            $$5 = 3;
        } else if ($$1.is(Blocks.HAY_BLOCK.asItem())) {
            $$3 = 20.0f;
            $$4 = 180;
        } else if ($$1.is(Items.APPLE)) {
            $$3 = 3.0f;
            $$4 = 60;
            $$5 = 3;
        } else if ($$1.is(Items.RED_MUSHROOM)) {
            $$3 = 3.0f;
            $$4 = 0;
            $$5 = 3;
        } else if ($$1.is(Items.CARROT)) {
            $$3 = 3.0f;
            $$4 = 60;
            $$5 = 3;
        } else if ($$1.is(Items.GOLDEN_CARROT)) {
            $$3 = 4.0f;
            $$4 = 60;
            $$5 = 5;
            if (!this.level().isClientSide() && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
                $$2 = true;
                this.setInLove($$0);
            }
        } else if ($$1.is(Items.GOLDEN_APPLE) || $$1.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            $$3 = 10.0f;
            $$4 = 240;
            $$5 = 10;
            if (!this.level().isClientSide() && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
                $$2 = true;
                this.setInLove($$0);
            }
        }
        if (this.getHealth() < this.getMaxHealth() && $$3 > 0.0f) {
            this.heal($$3);
            $$2 = true;
        }
        if (this.isBaby() && $$4 > 0) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level().isClientSide()) {
                this.ageUp($$4);
                $$2 = true;
            }
        }
        if (!($$5 <= 0 || !$$2 && this.isTamed() || this.getTemper() >= this.getMaxTemper() || this.level().isClientSide())) {
            this.modifyTemper($$5);
            $$2 = true;
        }
        if ($$2) {
            this.eating();
            this.gameEvent(GameEvent.EAT);
        }
        return $$2;
    }

    protected void doPlayerRide(Player $$0) {
        this.setEating(false);
        this.clearStanding();
        if (!this.level().isClientSide()) {
            $$0.setYRot(this.getYRot());
            $$0.setXRot(this.getXRot());
            $$0.startRiding(this);
        }
    }

    @Override
    public boolean isImmobile() {
        return super.isImmobile() && this.isVehicle() && this.isSaddled() || this.isEating() || this.isStanding();
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.HORSE_FOOD);
    }

    private void moveTail() {
        this.tailCounter = 1;
    }

    @Override
    protected void dropEquipment(ServerLevel $$0) {
        super.dropEquipment($$0);
        if (this.inventory == null) {
            return;
        }
        for (int $$1 = 0; $$1 < this.inventory.getContainerSize(); ++$$1) {
            ItemStack $$2 = this.inventory.getItem($$1);
            if ($$2.isEmpty() || EnchantmentHelper.has($$2, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) continue;
            this.spawnAtLocation($$0, $$2);
        }
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void aiStep() {
        void $$1;
        block9: {
            block8: {
                if (this.random.nextInt(200) == 0) {
                    this.moveTail();
                }
                super.aiStep();
                Level level = this.level();
                if (!(level instanceof ServerLevel)) break block8;
                ServerLevel $$0 = (ServerLevel)level;
                if (this.isAlive()) break block9;
            }
            return;
        }
        if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
            this.heal(1.0f);
        }
        if (this.canEatGrass()) {
            if (!this.isEating() && !this.isVehicle() && this.random.nextInt(300) == 0 && $$1.getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
                this.setEating(true);
            }
            if (this.isEating() && ++this.eatingCounter > 50) {
                this.eatingCounter = 0;
                this.setEating(false);
            }
        }
        this.followMommy((ServerLevel)$$1);
    }

    protected void followMommy(ServerLevel $$0) {
        AbstractHorse $$1;
        if (this.isBred() && this.isBaby() && !this.isEating() && ($$1 = $$0.getNearestEntity(AbstractHorse.class, MOMMY_TARGETING, (LivingEntity)this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().inflate(16.0))) != null && this.distanceToSqr($$1) > 4.0) {
            this.navigation.createPath($$1, 0);
        }
    }

    public boolean canEatGrass() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mouthCounter > 0 && ++this.mouthCounter > 30) {
            this.mouthCounter = 0;
            this.setFlag(64, false);
        }
        if (this.standCounter > 0 && --this.standCounter <= 0) {
            this.clearStanding();
        }
        if (this.tailCounter > 0 && ++this.tailCounter > 8) {
            this.tailCounter = 0;
        }
        if (this.sprintCounter > 0) {
            ++this.sprintCounter;
            if (this.sprintCounter > 300) {
                this.sprintCounter = 0;
            }
        }
        this.eatAnimO = this.eatAnim;
        if (this.isEating()) {
            this.eatAnim += (1.0f - this.eatAnim) * 0.4f + 0.05f;
            if (this.eatAnim > 1.0f) {
                this.eatAnim = 1.0f;
            }
        } else {
            this.eatAnim += (0.0f - this.eatAnim) * 0.4f - 0.05f;
            if (this.eatAnim < 0.0f) {
                this.eatAnim = 0.0f;
            }
        }
        this.standAnimO = this.standAnim;
        if (this.isStanding()) {
            this.eatAnimO = this.eatAnim = 0.0f;
            this.standAnim += (1.0f - this.standAnim) * 0.4f + 0.05f;
            if (this.standAnim > 1.0f) {
                this.standAnim = 1.0f;
            }
        } else {
            this.allowStandSliding = false;
            this.standAnim += (0.8f * this.standAnim * this.standAnim * this.standAnim - this.standAnim) * 0.6f - 0.05f;
            if (this.standAnim < 0.0f) {
                this.standAnim = 0.0f;
            }
        }
        this.mouthAnimO = this.mouthAnim;
        if (this.getFlag(64)) {
            this.mouthAnim += (1.0f - this.mouthAnim) * 0.7f + 0.05f;
            if (this.mouthAnim > 1.0f) {
                this.mouthAnim = 1.0f;
            }
        } else {
            this.mouthAnim += (0.0f - this.mouthAnim) * 0.7f - 0.05f;
            if (this.mouthAnim < 0.0f) {
                this.mouthAnim = 0.0f;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        if (this.isVehicle() || this.isBaby()) {
            return super.mobInteract($$0, $$1);
        }
        if (this.isTamed() && $$0.isSecondaryUseActive()) {
            this.openCustomInventoryScreen($$0);
            return InteractionResult.SUCCESS;
        }
        ItemStack $$2 = $$0.getItemInHand($$1);
        if (!$$2.isEmpty()) {
            InteractionResult $$3 = $$2.interactLivingEntity($$0, this, $$1);
            if ($$3.consumesAction()) {
                return $$3;
            }
            if (this.isEquippableInSlot($$2, EquipmentSlot.BODY) && !this.isWearingBodyArmor()) {
                this.equipBodyArmor($$0, $$2);
                return InteractionResult.SUCCESS;
            }
        }
        this.doPlayerRide($$0);
        return InteractionResult.SUCCESS;
    }

    private void openMouth() {
        if (!this.level().isClientSide()) {
            this.mouthCounter = 1;
            this.setFlag(64, true);
        }
    }

    public void setEating(boolean $$0) {
        this.setFlag(16, $$0);
    }

    public void setStanding(int $$0) {
        this.setEating(false);
        this.setFlag(32, true);
        this.standCounter = $$0;
    }

    public void clearStanding() {
        this.setFlag(32, false);
        this.standCounter = 0;
    }

    public @Nullable SoundEvent getAmbientStandSound() {
        return this.getAmbientSound();
    }

    public void standIfPossible() {
        if (this.canPerformRearing() && (this.isEffectiveAi() || !this.level().isClientSide())) {
            this.setStanding(20);
        }
    }

    public void makeMad() {
        if (!this.isStanding() && !this.level().isClientSide()) {
            this.standIfPossible();
            this.makeSound(this.getAngrySound());
        }
    }

    public boolean tameWithName(Player $$0) {
        this.setOwner($$0);
        this.setTamed(true);
        if ($$0 instanceof ServerPlayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)$$0, this);
        }
        this.level().broadcastEntityEvent(this, (byte)7);
        return true;
    }

    @Override
    protected void tickRidden(Player $$0, Vec3 $$1) {
        super.tickRidden($$0, $$1);
        Vec2 $$2 = this.getRiddenRotation($$0);
        this.setRot($$2.y, $$2.x);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yRotO = this.yHeadRot;
        if (this.isLocalInstanceAuthoritative()) {
            if ($$1.z <= 0.0) {
                this.gallopSoundCounter = 0;
            }
            if (this.onGround()) {
                if (this.playerJumpPendingScale > 0.0f && !this.isJumping()) {
                    this.executeRidersJump(this.playerJumpPendingScale, $$1);
                }
                this.playerJumpPendingScale = 0.0f;
            }
        }
    }

    protected Vec2 getRiddenRotation(LivingEntity $$0) {
        return new Vec2($$0.getXRot() * 0.5f, $$0.getYRot());
    }

    @Override
    protected void addPassenger(Entity $$0) {
        super.addPassenger($$0);
        $$0.absSnapRotationTo(this.getViewYRot(0.0f), this.getViewXRot(0.0f));
    }

    @Override
    protected Vec3 getRiddenInput(Player $$0, Vec3 $$1) {
        if (this.onGround() && this.playerJumpPendingScale == 0.0f && this.isStanding() && !this.allowStandSliding) {
            return Vec3.ZERO;
        }
        float $$2 = $$0.xxa * 0.5f;
        float $$3 = $$0.zza;
        if ($$3 <= 0.0f) {
            $$3 *= 0.25f;
        }
        return new Vec3($$2, 0.0, $$3);
    }

    @Override
    protected float getRiddenSpeed(Player $$0) {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    protected void executeRidersJump(float $$0, Vec3 $$1) {
        double $$2 = this.getJumpPower($$0);
        Vec3 $$3 = this.getDeltaMovement();
        this.setDeltaMovement($$3.x, $$2, $$3.z);
        this.needsSync = true;
        if ($$1.z > 0.0) {
            float $$4 = Mth.sin(this.getYRot() * ((float)Math.PI / 180));
            float $$5 = Mth.cos(this.getYRot() * ((float)Math.PI / 180));
            this.setDeltaMovement(this.getDeltaMovement().add(-0.4f * $$4 * $$0, 0.0, 0.4f * $$5 * $$0));
        }
    }

    protected void playJumpSound() {
        this.playSound(SoundEvents.HORSE_JUMP, 0.4f, 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean("EatingHaystack", this.isEating());
        $$0.putBoolean("Bred", this.isBred());
        $$0.putInt("Temper", this.getTemper());
        $$0.putBoolean("Tame", this.isTamed());
        EntityReference.store(this.owner, $$0, "Owner");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setEating($$0.getBooleanOr("EatingHaystack", false));
        this.setBred($$0.getBooleanOr("Bred", false));
        this.setTemper($$0.getIntOr("Temper", 0));
        this.setTamed($$0.getBooleanOr("Tame", false));
        this.owner = EntityReference.readWithOldOwnerConversion($$0, "Owner", this.level());
    }

    @Override
    public boolean canMate(Animal $$0) {
        return false;
    }

    protected boolean canParent() {
        return !this.isVehicle() && !this.isPassenger() && this.isTamed() && !this.isBaby() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
    }

    public boolean isMobControlled() {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return null;
    }

    protected void setOffspringAttributes(AgeableMob $$0, AbstractHorse $$1) {
        this.setOffspringAttribute($$0, $$1, Attributes.MAX_HEALTH, MIN_HEALTH, MAX_HEALTH);
        this.setOffspringAttribute($$0, $$1, Attributes.JUMP_STRENGTH, MIN_JUMP_STRENGTH, MAX_JUMP_STRENGTH);
        this.setOffspringAttribute($$0, $$1, Attributes.MOVEMENT_SPEED, MIN_MOVEMENT_SPEED, MAX_MOVEMENT_SPEED);
    }

    private void setOffspringAttribute(AgeableMob $$0, AbstractHorse $$1, Holder<Attribute> $$2, double $$3, double $$4) {
        double $$5 = AbstractHorse.createOffspringAttribute(this.getAttributeBaseValue($$2), $$0.getAttributeBaseValue($$2), $$3, $$4, this.random);
        $$1.getAttribute($$2).setBaseValue($$5);
    }

    static double createOffspringAttribute(double $$0, double $$1, double $$2, double $$3, RandomSource $$4) {
        double $$8;
        if ($$3 <= $$2) {
            throw new IllegalArgumentException("Incorrect range for an attribute");
        }
        $$0 = Mth.clamp($$0, $$2, $$3);
        $$1 = Mth.clamp($$1, $$2, $$3);
        double $$5 = 0.15 * ($$3 - $$2);
        double $$7 = ($$0 + $$1) / 2.0;
        double $$6 = Math.abs($$0 - $$1) + $$5 * 2.0;
        double $$9 = $$7 + $$6 * ($$8 = ($$4.nextDouble() + $$4.nextDouble() + $$4.nextDouble()) / 3.0 - 0.5);
        if ($$9 > $$3) {
            double $$10 = $$9 - $$3;
            return $$3 - $$10;
        }
        if ($$9 < $$2) {
            double $$11 = $$2 - $$9;
            return $$2 + $$11;
        }
        return $$9;
    }

    public float getEatAnim(float $$0) {
        return Mth.lerp($$0, this.eatAnimO, this.eatAnim);
    }

    public float getStandAnim(float $$0) {
        return Mth.lerp($$0, this.standAnimO, this.standAnim);
    }

    public float getMouthAnim(float $$0) {
        return Mth.lerp($$0, this.mouthAnimO, this.mouthAnim);
    }

    @Override
    public void onPlayerJump(int $$0) {
        if (!this.isSaddled()) {
            return;
        }
        if ($$0 < 0) {
            $$0 = 0;
        } else {
            this.allowStandSliding = true;
            this.standIfPossible();
        }
        this.playerJumpPendingScale = this.getPlayerJumpPendingScale($$0);
    }

    @Override
    public boolean canJump() {
        return this.isSaddled();
    }

    @Override
    public void handleStartJump(int $$0) {
        this.allowStandSliding = true;
        this.standIfPossible();
        this.playJumpSound();
    }

    @Override
    public void handleStopJump() {
    }

    protected void spawnTamingParticles(boolean $$0) {
        SimpleParticleType $$1 = $$0 ? ParticleTypes.HEART : ParticleTypes.SMOKE;
        for (int $$2 = 0; $$2 < 7; ++$$2) {
            double $$3 = this.random.nextGaussian() * 0.02;
            double $$4 = this.random.nextGaussian() * 0.02;
            double $$5 = this.random.nextGaussian() * 0.02;
            this.level().addParticle($$1, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), $$3, $$4, $$5);
        }
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 7) {
            this.spawnTamingParticles(true);
        } else if ($$0 == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent($$0);
        }
    }

    @Override
    protected void positionRider(Entity $$0, Entity.MoveFunction $$1) {
        super.positionRider($$0, $$1);
        if ($$0 instanceof LivingEntity) {
            ((LivingEntity)$$0).yBodyRot = this.yBodyRot;
        }
    }

    protected static float generateMaxHealth(IntUnaryOperator $$0) {
        return 15.0f + (float)$$0.applyAsInt(8) + (float)$$0.applyAsInt(9);
    }

    protected static double generateJumpStrength(DoubleSupplier $$0) {
        return (double)0.4f + $$0.getAsDouble() * 0.2 + $$0.getAsDouble() * 0.2 + $$0.getAsDouble() * 0.2;
    }

    protected static double generateSpeed(DoubleSupplier $$0) {
        return ((double)0.45f + $$0.getAsDouble() * 0.3 + $$0.getAsDouble() * 0.3 + $$0.getAsDouble() * 0.3) * 0.25;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public @Nullable SlotAccess getSlot(int $$0) {
        int $$1 = $$0 - 500;
        if ($$1 >= 0 && $$1 < this.inventory.getContainerSize()) {
            return this.inventory.getSlot($$1);
        }
        return super.getSlot($$0);
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Entity entity;
        if (this.isSaddled() && (entity = this.getFirstPassenger()) instanceof Player) {
            Player $$0 = (Player)entity;
            return $$0;
        }
        return super.getControllingPassenger();
    }

    private @Nullable Vec3 getDismountLocationInDirection(Vec3 $$0, LivingEntity $$1) {
        double $$2 = this.getX() + $$0.x;
        double $$3 = this.getBoundingBox().minY;
        double $$4 = this.getZ() + $$0.z;
        BlockPos.MutableBlockPos $$5 = new BlockPos.MutableBlockPos();
        block0: for (Pose $$6 : $$1.getDismountPoses()) {
            $$5.set($$2, $$3, $$4);
            double $$7 = this.getBoundingBox().maxY + 0.75;
            do {
                double $$8 = this.level().getBlockFloorHeight($$5);
                if ((double)$$5.getY() + $$8 > $$7) continue block0;
                if (DismountHelper.isBlockFloorValid($$8)) {
                    AABB $$9 = $$1.getLocalBoundsForPose($$6);
                    Vec3 $$10 = new Vec3($$2, (double)$$5.getY() + $$8, $$4);
                    if (DismountHelper.canDismountTo(this.level(), $$1, $$9.move($$10))) {
                        $$1.setPose($$6);
                        return $$10;
                    }
                }
                $$5.move(Direction.UP);
            } while ((double)$$5.getY() < $$7);
        }
        return null;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity $$0) {
        Vec3 $$1 = AbstractHorse.getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), this.getYRot() + ($$0.getMainArm() == HumanoidArm.RIGHT ? 90.0f : -90.0f));
        Vec3 $$2 = this.getDismountLocationInDirection($$1, $$0);
        if ($$2 != null) {
            return $$2;
        }
        Vec3 $$3 = AbstractHorse.getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), this.getYRot() + ($$0.getMainArm() == HumanoidArm.LEFT ? 90.0f : -90.0f));
        Vec3 $$4 = this.getDismountLocationInDirection($$3, $$0);
        if ($$4 != null) {
            return $$4;
        }
        return this.position();
    }

    protected void randomizeAttributes(RandomSource $$0) {
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        if ($$3 == null) {
            $$3 = new AgeableMob.AgeableMobGroupData(0.2f);
        }
        this.randomizeAttributes($$0.getRandom());
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    public boolean hasInventoryChanged(Container $$0) {
        return this.inventory != $$0;
    }

    public int getAmbientStandInterval() {
        return this.getAmbientSoundInterval();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity $$0, EntityDimensions $$1, float $$2) {
        return super.getPassengerAttachmentPoint($$0, $$1, $$2).add(new Vec3(0.0, 0.15 * (double)this.standAnimO * (double)$$2, -0.7 * (double)this.standAnimO * (double)$$2).yRot(-this.getYRot() * ((float)Math.PI / 180)));
    }

    public int getInventoryColumns() {
        return 0;
    }

    class MountPanicGoal
    extends PanicGoal {
        public MountPanicGoal(double $$0) {
            super(AbstractHorse.this, $$0);
        }

        @Override
        public boolean shouldPanic() {
            return !AbstractHorse.this.isMobControlled() && super.shouldPanic();
        }
    }
}

