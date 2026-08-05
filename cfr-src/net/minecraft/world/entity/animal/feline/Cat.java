/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.feline;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class Cat
extends TamableAnimal {
    public static final double TEMPT_SPEED_MOD = 0.6;
    public static final double WALK_SPEED_MOD = 0.8;
    public static final double SPRINT_SPEED_MOD = 1.33;
    private static final EntityDataAccessor<Holder<CatVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.CAT_VARIANT);
    private static final EntityDataAccessor<Boolean> IS_LYING = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RELAX_STATE_ONE = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
    private static final ResourceKey<CatVariant> DEFAULT_VARIANT = CatVariants.BLACK;
    private static final DyeColor DEFAULT_COLLAR_COLOR = DyeColor.RED;
    private @Nullable CatAvoidEntityGoal<Player> avoidPlayersGoal;
    private @Nullable TemptGoal temptGoal;
    private float lieDownAmount;
    private float lieDownAmountO;
    private float lieDownAmountTail;
    private float lieDownAmountOTail;
    private boolean isLyingOnTopOfSleepingPlayer;
    private float relaxStateOneAmount;
    private float relaxStateOneAmountO;

    public Cat(EntityType<? extends Cat> $$0, Level $$1) {
        super((EntityType<? extends TamableAnimal>)$$0, $$1);
        this.reassessTameGoals();
    }

    @Override
    protected void registerGoals() {
        this.temptGoal = new CatTemptGoal(this, 0.6, $$0 -> $$0.is(ItemTags.CAT_FOOD), true);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.5));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new CatRelaxOnOwnerGoal(this));
        this.goalSelector.addGoal(4, this.temptGoal);
        this.goalSelector.addGoal(5, new CatLieOnBedGoal(this, 1.1, 8));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0f, 5.0f));
        this.goalSelector.addGoal(7, new CatSitOnBlockGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3f));
        this.goalSelector.addGoal(9, new OcelotAttackGoal(this));
        this.goalSelector.addGoal(10, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 0.8, 1.0000001E-5f));
        this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.targetSelector.addGoal(1, new NonTameRandomTargetGoal<Rabbit>(this, Rabbit.class, false, null));
        this.targetSelector.addGoal(1, new NonTameRandomTargetGoal<Turtle>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    public Holder<CatVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    private void setVariant(Holder<CatVariant> $$0) {
        this.entityData.set(DATA_VARIANT_ID, $$0);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.CAT_VARIANT) {
            return Cat.castComponentValue($$0, this.getVariant());
        }
        if ($$0 == DataComponents.CAT_COLLAR) {
            return Cat.castComponentValue($$0, this.getCollarColor());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.CAT_VARIANT);
        this.applyImplicitComponentIfPresent($$0, DataComponents.CAT_COLLAR);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.CAT_VARIANT) {
            this.setVariant(Cat.castComponentValue(DataComponents.CAT_VARIANT, $$1));
            return true;
        }
        if ($$0 == DataComponents.CAT_COLLAR) {
            this.setCollarColor(Cat.castComponentValue(DataComponents.CAT_COLLAR, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    public void setLying(boolean $$0) {
        this.entityData.set(IS_LYING, $$0);
    }

    public boolean isLying() {
        return this.entityData.get(IS_LYING);
    }

    void setRelaxStateOne(boolean $$0) {
        this.entityData.set(RELAX_STATE_ONE, $$0);
    }

    boolean isRelaxStateOne() {
        return this.entityData.get(RELAX_STATE_ONE);
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    private void setCollarColor(DyeColor $$0) {
        this.entityData.set(DATA_COLLAR_COLOR, $$0.getId());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), DEFAULT_VARIANT));
        $$0.define(IS_LYING, false);
        $$0.define(RELAX_STATE_ONE, false);
        $$0.define(DATA_COLLAR_COLOR, DEFAULT_COLLAR_COLOR.getId());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        VariantUtils.writeVariant($$0, this.getVariant());
        $$0.store("CollarColor", DyeColor.LEGACY_ID_CODEC, this.getCollarColor());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        VariantUtils.readVariant($$0, Registries.CAT_VARIANT).ifPresent(this::setVariant);
        this.setCollarColor($$0.read("CollarColor", DyeColor.LEGACY_ID_CODEC).orElse(DEFAULT_COLLAR_COLOR));
    }

    @Override
    public void customServerAiStep(ServerLevel $$0) {
        if (this.getMoveControl().hasWanted()) {
            double $$1 = this.getMoveControl().getSpeedModifier();
            if ($$1 == 0.6) {
                this.setPose(Pose.CROUCHING);
                this.setSprinting(false);
            } else if ($$1 == 1.33) {
                this.setPose(Pose.STANDING);
                this.setSprinting(true);
            } else {
                this.setPose(Pose.STANDING);
                this.setSprinting(false);
            }
        } else {
            this.setPose(Pose.STANDING);
            this.setSprinting(false);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.isTame()) {
            if (this.isInLove()) {
                return SoundEvents.CAT_PURR;
            }
            if (this.random.nextInt(4) == 0) {
                return SoundEvents.CAT_PURREOW;
            }
            return SoundEvents.CAT_AMBIENT;
        }
        return SoundEvents.CAT_STRAY_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    public void hiss() {
        this.makeSound(SoundEvents.CAT_HISS);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void playEatingSound() {
        this.playSound(SoundEvents.CAT_EAT, 1.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.temptGoal != null && this.temptGoal.isRunning() && !this.isTame() && this.tickCount % 100 == 0) {
            this.playSound(SoundEvents.CAT_BEG_FOR_FOOD, 1.0f, 1.0f);
        }
        this.handleLieDown();
    }

    private void handleLieDown() {
        if ((this.isLying() || this.isRelaxStateOne()) && this.tickCount % 5 == 0) {
            this.playSound(SoundEvents.CAT_PURR, 0.6f + 0.4f * (this.random.nextFloat() - this.random.nextFloat()), 1.0f);
        }
        this.updateLieDownAmount();
        this.updateRelaxStateOneAmount();
        this.isLyingOnTopOfSleepingPlayer = false;
        if (this.isLying()) {
            BlockPos $$0 = this.blockPosition();
            List<Player> $$1 = this.level().getEntitiesOfClass(Player.class, new AABB($$0).inflate(2.0, 2.0, 2.0));
            for (Player $$2 : $$1) {
                if (!$$2.isSleeping()) continue;
                this.isLyingOnTopOfSleepingPlayer = true;
                break;
            }
        }
    }

    public boolean isLyingOnTopOfSleepingPlayer() {
        return this.isLyingOnTopOfSleepingPlayer;
    }

    private void updateLieDownAmount() {
        this.lieDownAmountO = this.lieDownAmount;
        this.lieDownAmountOTail = this.lieDownAmountTail;
        if (this.isLying()) {
            this.lieDownAmount = Math.min(1.0f, this.lieDownAmount + 0.15f);
            this.lieDownAmountTail = Math.min(1.0f, this.lieDownAmountTail + 0.08f);
        } else {
            this.lieDownAmount = Math.max(0.0f, this.lieDownAmount - 0.22f);
            this.lieDownAmountTail = Math.max(0.0f, this.lieDownAmountTail - 0.13f);
        }
    }

    private void updateRelaxStateOneAmount() {
        this.relaxStateOneAmountO = this.relaxStateOneAmount;
        this.relaxStateOneAmount = this.isRelaxStateOne() ? Math.min(1.0f, this.relaxStateOneAmount + 0.1f) : Math.max(0.0f, this.relaxStateOneAmount - 0.13f);
    }

    public float getLieDownAmount(float $$0) {
        return Mth.lerp($$0, this.lieDownAmountO, this.lieDownAmount);
    }

    public float getLieDownAmountTail(float $$0) {
        return Mth.lerp($$0, this.lieDownAmountOTail, this.lieDownAmountTail);
    }

    public float getRelaxStateOneAmount(float $$0) {
        return Mth.lerp($$0, this.relaxStateOneAmountO, this.relaxStateOneAmount);
    }

    @Override
    public @Nullable Cat getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        Cat $$2 = EntityType.CAT.create($$0, EntitySpawnReason.BREEDING);
        if ($$2 != null && $$1 instanceof Cat) {
            Cat $$3 = (Cat)$$1;
            if (this.random.nextBoolean()) {
                $$2.setVariant(this.getVariant());
            } else {
                $$2.setVariant($$3.getVariant());
            }
            if (this.isTame()) {
                $$2.setOwnerReference(this.getOwnerReference());
                $$2.setTame(true, true);
                DyeColor $$4 = this.getCollarColor();
                DyeColor $$5 = $$3.getCollarColor();
                $$2.setCollarColor(DyeColor.getMixedColor($$0, $$4, $$5));
            }
        }
        return $$2;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public boolean canMate(Animal $$0) {
        void $$2;
        if (!this.isTame()) {
            return false;
        }
        if (!($$0 instanceof Cat)) {
            return false;
        }
        Cat $$1 = (Cat)$$0;
        return $$2.isTame() && super.canMate($$0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        $$3 = super.finalizeSpawn($$0, $$1, $$2, $$3);
        VariantUtils.selectVariantToSpawn(SpawnContext.create($$0, this.blockPosition()), Registries.CAT_VARIANT).ifPresent(this::setVariant);
        return $$3;
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        InteractionResult $$8;
        ItemStack $$2 = $$0.getItemInHand($$1);
        Item $$3 = $$2.getItem();
        if (this.isTame()) {
            if (this.isOwnedBy($$0)) {
                InteractionResult $$7;
                if ($$3 instanceof DyeItem) {
                    DyeItem $$4 = (DyeItem)$$3;
                    DyeColor $$5 = $$4.getDyeColor();
                    if ($$5 != this.getCollarColor()) {
                        if (!this.level().isClientSide()) {
                            this.setCollarColor($$5);
                            $$2.consume(1, $$0);
                            this.setPersistenceRequired();
                        }
                        return InteractionResult.SUCCESS;
                    }
                } else if (this.isFood($$2) && this.getHealth() < this.getMaxHealth()) {
                    if (!this.level().isClientSide()) {
                        this.usePlayerItem($$0, $$1, $$2);
                        FoodProperties $$6 = $$2.get(DataComponents.FOOD);
                        this.heal($$6 != null ? (float)$$6.nutrition() : 1.0f);
                        this.playEatingSound();
                    }
                    return InteractionResult.SUCCESS;
                }
                if (!($$7 = super.mobInteract($$0, $$1)).consumesAction()) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    return InteractionResult.SUCCESS;
                }
                return $$7;
            }
        } else if (this.isFood($$2)) {
            if (!this.level().isClientSide()) {
                this.usePlayerItem($$0, $$1, $$2);
                this.tryToTame($$0);
                this.setPersistenceRequired();
                this.playEatingSound();
            }
            return InteractionResult.SUCCESS;
        }
        if (($$8 = super.mobInteract($$0, $$1)).consumesAction()) {
            this.setPersistenceRequired();
        }
        return $$8;
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.CAT_FOOD);
    }

    @Override
    public boolean removeWhenFarAway(double $$0) {
        return !this.isTame() && this.tickCount > 2400;
    }

    @Override
    public void setTame(boolean $$0, boolean $$1) {
        super.setTame($$0, $$1);
        this.reassessTameGoals();
    }

    protected void reassessTameGoals() {
        if (this.avoidPlayersGoal == null) {
            this.avoidPlayersGoal = new CatAvoidEntityGoal<Player>(this, Player.class, 16.0f, 0.8, 1.33);
        }
        this.goalSelector.removeGoal(this.avoidPlayersGoal);
        if (!this.isTame()) {
            this.goalSelector.addGoal(4, this.avoidPlayersGoal);
        }
    }

    private void tryToTame(Player $$0) {
        if (this.random.nextInt(3) == 0) {
            this.tame($$0);
            this.setOrderedToSit(true);
            this.level().broadcastEntityEvent(this, (byte)7);
        } else {
            this.level().broadcastEntityEvent(this, (byte)6);
        }
    }

    @Override
    public boolean isSteppingCarefully() {
        return this.isCrouching() || super.isSteppingCarefully();
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    static class CatTemptGoal
    extends TemptGoal {
        private @Nullable Player selectedPlayer;
        private final Cat cat;

        public CatTemptGoal(Cat $$0, double $$1, Predicate<ItemStack> $$2, boolean $$3) {
            super($$0, $$1, $$2, $$3);
            this.cat = $$0;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.selectedPlayer == null && this.mob.getRandom().nextInt(this.adjustedTickDelay(600)) == 0) {
                this.selectedPlayer = this.player;
            } else if (this.mob.getRandom().nextInt(this.adjustedTickDelay(500)) == 0) {
                this.selectedPlayer = null;
            }
        }

        @Override
        protected boolean canScare() {
            if (this.selectedPlayer != null && this.selectedPlayer.equals(this.player)) {
                return false;
            }
            return super.canScare();
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.cat.isTame();
        }
    }

    static class CatRelaxOnOwnerGoal
    extends Goal {
        private final Cat cat;
        private @Nullable Player ownerPlayer;
        private @Nullable BlockPos goalPos;
        private int onBedTicks;

        public CatRelaxOnOwnerGoal(Cat $$0) {
            this.cat = $$0;
        }

        @Override
        public boolean canUse() {
            if (!this.cat.isTame()) {
                return false;
            }
            if (this.cat.isOrderedToSit()) {
                return false;
            }
            LivingEntity $$0 = this.cat.getOwner();
            if ($$0 instanceof Player) {
                Player $$12;
                this.ownerPlayer = $$12 = (Player)$$0;
                if (!$$0.isSleeping()) {
                    return false;
                }
                if (this.cat.distanceToSqr(this.ownerPlayer) > 100.0) {
                    return false;
                }
                BlockPos $$2 = this.ownerPlayer.blockPosition();
                BlockState $$3 = this.cat.level().getBlockState($$2);
                if ($$3.is(BlockTags.BEDS)) {
                    this.goalPos = $$3.getOptionalValue(BedBlock.FACING).map($$1 -> $$2.relative($$1.getOpposite())).orElseGet(() -> new BlockPos($$2));
                    return !this.spaceIsOccupied();
                }
            }
            return false;
        }

        private boolean spaceIsOccupied() {
            List<Cat> $$0 = this.cat.level().getEntitiesOfClass(Cat.class, new AABB(this.goalPos).inflate(2.0));
            for (Cat $$1 : $$0) {
                if ($$1 == this.cat || !$$1.isLying() && !$$1.isRelaxStateOne()) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.cat.isTame() && !this.cat.isOrderedToSit() && this.ownerPlayer != null && this.ownerPlayer.isSleeping() && this.goalPos != null && !this.spaceIsOccupied();
        }

        @Override
        public void start() {
            if (this.goalPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().moveTo(this.goalPos.getX(), this.goalPos.getY(), this.goalPos.getZ(), 1.1f);
            }
        }

        @Override
        public void stop() {
            this.cat.setLying(false);
            if (this.ownerPlayer.getSleepTimer() >= 100 && this.cat.level().getRandom().nextFloat() < this.cat.level().environmentAttributes().getValue(EnvironmentAttributes.CAT_WAKING_UP_GIFT_CHANCE, this.cat.position()).floatValue()) {
                this.giveMorningGift();
            }
            this.onBedTicks = 0;
            this.cat.setRelaxStateOne(false);
            this.cat.getNavigation().stop();
        }

        private void giveMorningGift() {
            RandomSource $$0 = this.cat.getRandom();
            BlockPos.MutableBlockPos $$12 = new BlockPos.MutableBlockPos();
            $$12.set(this.cat.isLeashed() ? this.cat.getLeashHolder().blockPosition() : this.cat.blockPosition());
            this.cat.randomTeleport($$12.getX() + $$0.nextInt(11) - 5, $$12.getY() + $$0.nextInt(5) - 2, $$12.getZ() + $$0.nextInt(11) - 5, false);
            $$12.set(this.cat.blockPosition());
            this.cat.dropFromGiftLootTable(CatRelaxOnOwnerGoal.getServerLevel(this.cat), BuiltInLootTables.CAT_MORNING_GIFT, ($$1, $$2) -> $$1.addFreshEntity(new ItemEntity((Level)$$1, (double)$$12.getX() - (double)Mth.sin(this.cat.yBodyRot * ((float)Math.PI / 180)), $$12.getY(), (double)$$12.getZ() + (double)Mth.cos(this.cat.yBodyRot * ((float)Math.PI / 180)), (ItemStack)$$2)));
        }

        @Override
        public void tick() {
            if (this.ownerPlayer != null && this.goalPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().moveTo(this.goalPos.getX(), this.goalPos.getY(), this.goalPos.getZ(), 1.1f);
                if (this.cat.distanceToSqr(this.ownerPlayer) < 2.5) {
                    ++this.onBedTicks;
                    if (this.onBedTicks > this.adjustedTickDelay(16)) {
                        this.cat.setLying(true);
                        this.cat.setRelaxStateOne(false);
                    } else {
                        this.cat.lookAt(this.ownerPlayer, 45.0f, 45.0f);
                        this.cat.setRelaxStateOne(true);
                    }
                } else {
                    this.cat.setLying(false);
                }
            }
        }
    }

    static class CatAvoidEntityGoal<T extends LivingEntity>
    extends AvoidEntityGoal<T> {
        private final Cat cat;

        public CatAvoidEntityGoal(Cat $$0, Class<T> $$1, float $$2, double $$3, double $$4) {
            super($$0, $$1, $$2, $$3, $$4, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            this.cat = $$0;
        }

        @Override
        public boolean canUse() {
            return !this.cat.isTame() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.cat.isTame() && super.canContinueToUse();
        }
    }
}

