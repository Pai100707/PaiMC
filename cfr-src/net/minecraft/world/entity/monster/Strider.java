/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public class Strider
extends Animal
implements ItemSteerable {
    private static final Identifier SUFFOCATING_MODIFIER_ID = Identifier.withDefaultNamespace("suffocating");
    private static final AttributeModifier SUFFOCATING_MODIFIER = new AttributeModifier(SUFFOCATING_MODIFIER_ID, -0.34f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final float SUFFOCATE_STEERING_MODIFIER = 0.35f;
    private static final float STEERING_MODIFIER = 0.55f;
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private final ItemBasedSteering steering;
    private @Nullable TemptGoal temptGoal;

    public Strider(EntityType<? extends Strider> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
        this.steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME);
        this.blocksBuilding = true;
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setPathfindingMalus(PathType.LAVA, 0.0f);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0f);
    }

    public static boolean checkStriderSpawnRules(EntityType<Strider> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        BlockPos.MutableBlockPos $$5 = $$3.mutable();
        do {
            $$5.move(Direction.UP);
        } while ($$1.getFluidState($$5).is(FluidTags.LAVA));
        return $$1.getBlockState($$5).isAir();
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
        $$0.define(DATA_SUFFOCATING, false);
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
            return SoundEvents.STRIDER_SADDLE;
        }
        return super.getEquipSound($$0, $$1, $$2);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.65));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.temptGoal = new TemptGoal(this, 1.4, $$0 -> $$0.is(ItemTags.STRIDER_TEMPT_ITEMS), false);
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(4, new StriderGoToLavaGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 60));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0f));
    }

    public void setSuffocating(boolean $$0) {
        this.entityData.set(DATA_SUFFOCATING, $$0);
        AttributeInstance $$1 = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if ($$1 != null) {
            if ($$0) {
                $$1.addOrUpdateTransientModifier(SUFFOCATING_MODIFIER);
            } else {
                $$1.removeModifier(SUFFOCATING_MODIFIER_ID);
            }
        }
    }

    public boolean isSuffocating() {
        return this.entityData.get(DATA_SUFFOCATING);
    }

    @Override
    public boolean canStandOnFluid(FluidState $$0) {
        return $$0.is(FluidTags.LAVA);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity $$0, EntityDimensions $$1, float $$2) {
        if (!this.level().isClientSide()) {
            return super.getPassengerAttachmentPoint($$0, $$1, $$2);
        }
        float $$3 = Math.min(0.25f, this.walkAnimation.speed());
        float $$4 = this.walkAnimation.position();
        float $$5 = 0.12f * Mth.cos($$4 * 1.5f) * 2.0f * $$3;
        return super.getPassengerAttachmentPoint($$0, $$1, $$2).add(0.0, $$5 * $$2, 0.0);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader $$0) {
        return $$0.isUnobstructed(this);
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Player $$0;
        Entity entity;
        if (this.isSaddled() && (entity = this.getFirstPassenger()) instanceof Player && ($$0 = (Player)entity).isHolding(Items.WARPED_FUNGUS_ON_A_STICK)) {
            return $$0;
        }
        return super.getControllingPassenger();
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity $$0) {
        Vec3[] $$1 = new Vec3[]{Strider.getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), $$0.getYRot()), Strider.getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), $$0.getYRot() - 22.5f), Strider.getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), $$0.getYRot() + 22.5f), Strider.getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), $$0.getYRot() - 45.0f), Strider.getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), $$0.getYRot() + 45.0f)};
        LinkedHashSet $$2 = Sets.newLinkedHashSet();
        double $$3 = this.getBoundingBox().maxY;
        double $$4 = this.getBoundingBox().minY - 0.5;
        BlockPos.MutableBlockPos $$5 = new BlockPos.MutableBlockPos();
        for (Vec3 $$6 : $$1) {
            $$5.set(this.getX() + $$6.x, $$3, this.getZ() + $$6.z);
            for (double $$7 = $$3; $$7 > $$4; $$7 -= 1.0) {
                $$2.add($$5.immutable());
                $$5.move(Direction.DOWN);
            }
        }
        for (BlockPos $$8 : $$2) {
            double $$9;
            if (this.level().getFluidState($$8).is(FluidTags.LAVA) || !DismountHelper.isBlockFloorValid($$9 = this.level().getBlockFloorHeight($$8))) continue;
            Vec3 $$10 = Vec3.upFromBottomCenterOf($$8, $$9);
            for (Pose $$11 : $$0.getDismountPoses()) {
                AABB $$12 = $$0.getLocalBoundsForPose($$11);
                if (!DismountHelper.canDismountTo(this.level(), $$0, $$12.move($$10))) continue;
                $$0.setPose($$11);
                return $$10;
            }
        }
        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Override
    protected void tickRidden(Player $$0, Vec3 $$1) {
        this.setRot($$0.getYRot(), $$0.getXRot() * 0.5f);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yRotO = this.yHeadRot;
        this.steering.tickBoost();
        super.tickRidden($$0, $$1);
    }

    @Override
    protected Vec3 getRiddenInput(Player $$0, Vec3 $$1) {
        return new Vec3(0.0, 0.0, 1.0);
    }

    @Override
    protected float getRiddenSpeed(Player $$0) {
        return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)(this.isSuffocating() ? 0.35f : 0.55f) * (double)this.steering.boostFactor());
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.6f;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(this.isInLava() ? SoundEvents.STRIDER_STEP_LAVA : SoundEvents.STRIDER_STEP, 1.0f, 1.0f);
    }

    @Override
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }

    @Override
    protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
        if (this.isInLava()) {
            this.resetFallDistance();
            return;
        }
        super.checkFallDamage($$0, $$1, $$2, $$3);
    }

    @Override
    public void tick() {
        if (this.isBeingTempted() && this.random.nextInt(140) == 0) {
            this.makeSound(SoundEvents.STRIDER_HAPPY);
        } else if (this.isPanicking() && this.random.nextInt(60) == 0) {
            this.makeSound(SoundEvents.STRIDER_RETREAT);
        }
        if (!this.isNoAi()) {
            Strider $$3;
            BlockState $$0 = this.level().getBlockState(this.blockPosition());
            BlockState $$1 = this.getBlockStateOnLegacy();
            boolean $$2 = $$0.is(BlockTags.STRIDER_WARM_BLOCKS) || $$1.is(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0;
            Entity entity = this.getVehicle();
            boolean $$4 = entity instanceof Strider && ($$3 = (Strider)entity).isSuffocating();
            this.setSuffocating(!$$2 || $$4);
        }
        super.tick();
        this.floatStrider();
    }

    private boolean isBeingTempted() {
        return this.temptGoal != null && this.temptGoal.isRunning();
    }

    @Override
    protected boolean shouldPassengersInheritMalus() {
        return true;
    }

    private void floatStrider() {
        if (this.isInLava()) {
            CollisionContext $$0 = CollisionContext.of(this);
            if (!$$0.isAbove(LiquidBlock.SHAPE_STABLE, this.blockPosition(), true) || this.level().getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
            } else {
                this.setOnGround(true);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MOVEMENT_SPEED, 0.175f);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.isPanicking() || this.isBeingTempted()) {
            return null;
        }
        return SoundEvents.STRIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.STRIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRIDER_DEATH;
    }

    @Override
    protected boolean canAddPassenger(Entity $$0) {
        return !this.isVehicle() && !this.isEyeInFluid(FluidTags.LAVA);
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level $$0) {
        return new StriderPathNavigation(this, $$0);
    }

    @Override
    public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
        if ($$1.getBlockState($$0).getFluidState().is(FluidTags.LAVA)) {
            return 10.0f;
        }
        return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0f;
    }

    @Override
    public @Nullable Strider getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return EntityType.STRIDER.create($$0, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.STRIDER_FOOD);
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
        if ($$2 && !this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STRIDER_EAT, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        return $$3;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.6f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        if (this.isBaby()) {
            return super.finalizeSpawn($$0, $$1, $$2, $$3);
        }
        RandomSource $$4 = $$0.getRandom();
        if ($$4.nextInt(30) == 0) {
            Mob $$5 = EntityType.ZOMBIFIED_PIGLIN.create($$0.getLevel(), EntitySpawnReason.JOCKEY);
            if ($$5 != null) {
                $$3 = this.spawnJockey($$0, $$1, $$5, new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds($$4), false));
                $$5.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
                this.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                this.setGuaranteedDrop(EquipmentSlot.SADDLE);
            }
        } else if ($$4.nextInt(10) == 0) {
            AgeableMob $$6 = EntityType.STRIDER.create($$0.getLevel(), EntitySpawnReason.JOCKEY);
            if ($$6 != null) {
                $$6.setAge(-24000);
                $$3 = this.spawnJockey($$0, $$1, $$6, null);
            }
        } else {
            $$3 = new AgeableMob.AgeableMobGroupData(0.5f);
        }
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    private SpawnGroupData spawnJockey(ServerLevelAccessor $$0, DifficultyInstance $$1, Mob $$2, @Nullable SpawnGroupData $$3) {
        $$2.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
        $$2.finalizeSpawn($$0, $$1, EntitySpawnReason.JOCKEY, $$3);
        $$2.startRiding(this, true, false);
        return new AgeableMob.AgeableMobGroupData(0.0f);
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    static class StriderGoToLavaGoal
    extends MoveToBlockGoal {
        private final Strider strider;

        StriderGoToLavaGoal(Strider $$0, double $$1) {
            super($$0, $$1, 8, 2);
            this.strider = $$0;
        }

        @Override
        public BlockPos getMoveToTarget() {
            return this.blockPos;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.strider.isInLava() && this.isValidTarget(this.strider.level(), this.blockPos);
        }

        @Override
        public boolean canUse() {
            return !this.strider.isInLava() && super.canUse();
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 20 == 0;
        }

        @Override
        protected boolean isValidTarget(LevelReader $$0, BlockPos $$1) {
            return $$0.getBlockState($$1).is(Blocks.LAVA) && $$0.getBlockState($$1.above()).isPathfindable(PathComputationType.LAND);
        }
    }

    static class StriderPathNavigation
    extends GroundPathNavigation {
        StriderPathNavigation(Strider $$0, Level $$1) {
            super($$0, $$1);
        }

        @Override
        protected PathFinder createPathFinder(int $$0) {
            this.nodeEvaluator = new WalkNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, $$0);
        }

        @Override
        protected boolean hasValidPathType(PathType $$0) {
            if ($$0 == PathType.LAVA || $$0 == PathType.DAMAGE_FIRE || $$0 == PathType.DANGER_FIRE) {
                return true;
            }
            return super.hasValidPathType($$0);
        }

        @Override
        public boolean isStableDestination(BlockPos $$0) {
            return this.level.getBlockState($$0).is(Blocks.LAVA) || super.isStableDestination($$0);
        }
    }
}

