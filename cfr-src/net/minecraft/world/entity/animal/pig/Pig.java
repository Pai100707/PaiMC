/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.pig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.pig.PigVariants;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Pig
extends Animal
implements ItemSteerable {
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Holder<PigVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.PIG_VARIANT);
    private final ItemBasedSteering steering;

    public Pig(EntityType<? extends Pig> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
        this.steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, $$0 -> $$0.is(Items.CARROT_ON_A_STICK), false));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, $$0 -> $$0.is(ItemTags.PIG_FOOD), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Player $$0;
        Entity entity;
        if (this.isSaddled() && (entity = this.getFirstPassenger()) instanceof Player && ($$0 = (Player)entity).isHolding(Items.CARROT_ON_A_STICK)) {
            return $$0;
        }
        return super.getControllingPassenger();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (DATA_BOOST_TIME.equals($$0) && this.level().isClientSide()) {
            this.steering.onSynced();
        }
        super.onSyncedDataUpdated($$0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_BOOST_TIME, 0);
        $$0.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), PigVariants.DEFAULT));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        VariantUtils.writeVariant($$0, this.getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        VariantUtils.readVariant($$0, Registries.PIG_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.PIG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.PIG_STEP, 0.15f, 1.0f);
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        boolean $$2 = this.isFood($$0.getItemInHand($$1));
        if (!$$2 && this.isSaddled() && !this.isVehicle() && !$$0.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                $$0.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }
        InteractionResult $$3 = super.mobInteract($$0, $$1);
        if (!$$3.consumesAction()) {
            ItemStack $$4 = $$0.getItemInHand($$1);
            if (this.isEquippableInSlot($$4, EquipmentSlot.SADDLE)) {
                return $$4.interactLivingEntity($$0, this, $$1);
            }
            return InteractionResult.PASS;
        }
        return $$3;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot $$0) {
        if ($$0 == EquipmentSlot.SADDLE) {
            return this.isAlive() && !this.isBaby();
        }
        return super.canUseSlot($$0);
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot $$0) {
        return $$0 == EquipmentSlot.SADDLE || super.canDispenserEquipIntoSlot($$0);
    }

    @Override
    protected Holder<SoundEvent> getEquipSound(EquipmentSlot $$0, ItemStack $$1, Equippable $$2) {
        if ($$0 == EquipmentSlot.SADDLE) {
            return SoundEvents.PIG_SADDLE;
        }
        return super.getEquipSound($$0, $$1, $$2);
    }

    @Override
    public void thunderHit(ServerLevel $$0, LightningBolt $$12) {
        if ($$0.getDifficulty() != Difficulty.PEACEFUL) {
            ZombifiedPiglin $$2 = this.convertTo(EntityType.ZOMBIFIED_PIGLIN, ConversionParams.single(this, false, true), $$1 -> {
                $$1.populateDefaultEquipmentSlots(this.getRandom(), $$0.getCurrentDifficultyAt(this.blockPosition()));
                $$1.setPersistenceRequired();
            });
            if ($$2 == null) {
                super.thunderHit($$0, $$12);
            }
        } else {
            super.thunderHit($$0, $$12);
        }
    }

    @Override
    protected void tickRidden(Player $$0, Vec3 $$1) {
        super.tickRidden($$0, $$1);
        this.setRot($$0.getYRot(), $$0.getXRot() * 0.5f);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yRotO = this.yHeadRot;
        this.steering.tickBoost();
    }

    @Override
    protected Vec3 getRiddenInput(Player $$0, Vec3 $$1) {
        return new Vec3(0.0, 0.0, 1.0);
    }

    @Override
    protected float getRiddenSpeed(Player $$0) {
        return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225 * (double)this.steering.boostFactor());
    }

    @Override
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }

    @Override
    public @Nullable Pig getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        Pig $$2 = EntityType.PIG.create($$0, EntitySpawnReason.BREEDING);
        if ($$2 != null && $$1 instanceof Pig) {
            Pig $$3 = (Pig)$$1;
            $$2.setVariant(this.random.nextBoolean() ? this.getVariant() : $$3.getVariant());
        }
        return $$2;
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.PIG_FOOD);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.6f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    private void setVariant(Holder<PigVariant> $$0) {
        this.entityData.set(DATA_VARIANT_ID, $$0);
    }

    public Holder<PigVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.PIG_VARIANT) {
            return Pig.castComponentValue($$0, this.getVariant());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.PIG_VARIANT);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.PIG_VARIANT) {
            this.setVariant(Pig.castComponentValue(DataComponents.PIG_VARIANT, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        VariantUtils.selectVariantToSpawn(SpawnContext.create($$0, this.blockPosition()), Registries.PIG_VARIANT).ifPresent(this::setVariant);
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }
}

