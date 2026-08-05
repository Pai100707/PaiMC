/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.chicken;

import java.util.Optional;
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
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
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
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Chicken
extends Animal {
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.CHICKEN.getDimensions().scale(0.5f).withEyeHeight(0.2975f);
    private static final EntityDataAccessor<Holder<ChickenVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Chicken.class, EntityDataSerializers.CHICKEN_VARIANT);
    private static final boolean DEFAULT_CHICKEN_JOCKEY = false;
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    public float flapping = 1.0f;
    private float nextFlap = 1.0f;
    public int eggTime = this.random.nextInt(6000) + 6000;
    public boolean isChickenJockey = false;

    public Chicken(EntityType<? extends Chicken> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, $$0 -> $$0.is(ItemTags.CHICKEN_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed += (this.onGround() ? -1.0f : 4.0f) * 0.3f;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0f, 1.0f);
        if (!this.onGround() && this.flapping < 1.0f) {
            this.flapping = 1.0f;
        }
        this.flapping *= 0.9f;
        Vec3 $$0 = this.getDeltaMovement();
        if (!this.onGround() && $$0.y < 0.0) {
            this.setDeltaMovement($$0.multiply(1.0, 0.6, 1.0));
        }
        this.flap += this.flapping * 2.0f;
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            if (this.isAlive() && !this.isBaby() && !this.isChickenJockey() && --this.eggTime <= 0) {
                if (this.dropFromGiftLootTable($$1, BuiltInLootTables.CHICKEN_LAY, this::spawnAtLocation)) {
                    this.playSound(SoundEvents.CHICKEN_EGG, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                    this.gameEvent(GameEvent.ENTITY_PLACE);
                }
                this.eggTime = this.random.nextInt(6000) + 6000;
            }
        }
    }

    @Override
    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    @Override
    protected void onFlap() {
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHICKEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15f, 1.0f);
    }

    @Override
    public @Nullable Chicken getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        Chicken $$2 = EntityType.CHICKEN.create($$0, EntitySpawnReason.BREEDING);
        if ($$2 != null && $$1 instanceof Chicken) {
            Chicken $$3 = (Chicken)$$1;
            $$2.setVariant(this.random.nextBoolean() ? this.getVariant() : $$3.getVariant());
        }
        return $$2;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        VariantUtils.selectVariantToSpawn(SpawnContext.create($$0, this.blockPosition()), Registries.CHICKEN_VARIANT).ifPresent(this::setVariant);
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.CHICKEN_FOOD);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel $$0) {
        if (this.isChickenJockey()) {
            return 10;
        }
        return super.getBaseExperienceReward($$0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), ChickenVariants.TEMPERATE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$02) {
        super.readAdditionalSaveData($$02);
        this.isChickenJockey = $$02.getBooleanOr("IsChickenJockey", false);
        $$02.getInt("EggLayTime").ifPresent($$0 -> {
            this.eggTime = $$0;
        });
        VariantUtils.readVariant($$02, Registries.CHICKEN_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean("IsChickenJockey", this.isChickenJockey);
        $$0.putInt("EggLayTime", this.eggTime);
        VariantUtils.writeVariant($$0, this.getVariant());
    }

    public void setVariant(Holder<ChickenVariant> $$0) {
        this.entityData.set(DATA_VARIANT_ID, $$0);
    }

    public Holder<ChickenVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.CHICKEN_VARIANT) {
            return Chicken.castComponentValue($$0, new EitherHolder<ChickenVariant>(this.getVariant()));
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.CHICKEN_VARIANT);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.CHICKEN_VARIANT) {
            Optional<Holder<ChickenVariant>> $$2 = Chicken.castComponentValue(DataComponents.CHICKEN_VARIANT, $$1).unwrap(this.registryAccess());
            if ($$2.isPresent()) {
                this.setVariant($$2.get());
                return true;
            }
            return false;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    @Override
    public boolean removeWhenFarAway(double $$0) {
        return this.isChickenJockey();
    }

    @Override
    protected void positionRider(Entity $$0, Entity.MoveFunction $$1) {
        super.positionRider($$0, $$1);
        if ($$0 instanceof LivingEntity) {
            ((LivingEntity)$$0).yBodyRot = this.yBodyRot;
        }
    }

    public boolean isChickenJockey() {
        return this.isChickenJockey;
    }

    public void setChickenJockey(boolean $$0) {
        this.isChickenJockey = $$0;
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }
}

