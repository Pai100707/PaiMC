package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Frog extends Animal {
   protected static final ImmutableList<SensorType<? extends Sensor<? super Frog>>> SENSOR_TYPES = ImmutableList.of(
      SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FROG_ATTACKABLES, SensorType.FROG_TEMPTATIONS, SensorType.IS_IN_WATER
   );
   protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
      MemoryModuleType.LOOK_TARGET,
      MemoryModuleType.NEAREST_LIVING_ENTITIES,
      MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
      MemoryModuleType.WALK_TARGET,
      MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
      MemoryModuleType.PATH,
      MemoryModuleType.BREED_TARGET,
      MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
      MemoryModuleType.LONG_JUMP_MID_JUMP,
      MemoryModuleType.ATTACK_TARGET,
      MemoryModuleType.TEMPTING_PLAYER,
      MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
      new MemoryModuleType[]{
         MemoryModuleType.IS_TEMPTED,
         MemoryModuleType.HURT_BY,
         MemoryModuleType.HURT_BY_ENTITY,
         MemoryModuleType.NEAREST_ATTACKABLE,
         MemoryModuleType.IS_IN_WATER,
         MemoryModuleType.IS_PREGNANT,
         MemoryModuleType.IS_PANICKING,
         MemoryModuleType.UNREACHABLE_TONGUE_TARGETS
      }
   );
   private static final EntityDataAccessor<Holder<FrogVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.FROG_VARIANT);
   private static final EntityDataAccessor<OptionalInt> DATA_TONGUE_TARGET_ID = SynchedEntityData.defineId(
      Frog.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT
   );
   private static final int FROG_FALL_DAMAGE_REDUCTION = 5;
   private static final ResourceKey<FrogVariant> DEFAULT_VARIANT = FrogVariants.TEMPERATE;
   public final net.minecraft.world.entity.AnimationState jumpAnimationState = new net.minecraft.world.entity.AnimationState();
   public final net.minecraft.world.entity.AnimationState croakAnimationState = new net.minecraft.world.entity.AnimationState();
   public final net.minecraft.world.entity.AnimationState tongueAnimationState = new net.minecraft.world.entity.AnimationState();
   public final net.minecraft.world.entity.AnimationState swimIdleAnimationState = new net.minecraft.world.entity.AnimationState();

   public Frog(net.minecraft.world.entity.EntityType<? extends Animal> $$0, Level $$1) {
      super($$0, $$1);
      this.lookControl = new Frog.FrogLookControl(this);
      this.setPathfindingMalus(PathType.WATER, 4.0F);
      this.setPathfindingMalus(PathType.TRAPDOOR, -1.0F);
      this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
   }

   @Override
   protected Brain.Provider<Frog> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   @Override
   protected Brain<?> makeBrain(Dynamic<?> $$0) {
      return FrogAi.makeBrain(this.brainProvider().makeBrain($$0));
   }

   @Override
   public Brain<Frog> getBrain() {
      return (Brain<Frog>)super.getBrain();
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      Registry<FrogVariant> $$1 = this.registryAccess().lookupOrThrow(Registries.FROG_VARIANT);
      $$0.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), DEFAULT_VARIANT));
      $$0.define(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
   }

   public void eraseTongueTarget() {
      this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
   }

   public Optional<net.minecraft.world.entity.Entity> getTongueTarget() {
      return ((OptionalInt)this.entityData.get(DATA_TONGUE_TARGET_ID))
         .stream()
         .<net.minecraft.world.entity.Entity>mapToObj(this.level()::getEntity)
         .filter(Objects::nonNull)
         .findFirst();
   }

   public void setTongueTarget(net.minecraft.world.entity.Entity $$0) {
      this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.of($$0.getId()));
   }

   @Override
   public int getHeadRotSpeed() {
      return 35;
   }

   @Override
   public int getMaxHeadYRot() {
      return 5;
   }

   public Holder<FrogVariant> getVariant() {
      return (Holder<FrogVariant>)this.entityData.get(DATA_VARIANT_ID);
   }

   private void setVariant(Holder<FrogVariant> $$0) {
      this.entityData.set(DATA_VARIANT_ID, $$0);
   }

   @Nullable
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.FROG_VARIANT ? castComponentValue((DataComponentType<T>)$$0, this.getVariant()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.FROG_VARIANT);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.FROG_VARIANT) {
         this.setVariant(castComponentValue(DataComponents.FROG_VARIANT, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      VariantUtils.writeVariant($$0, this.getVariant());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      VariantUtils.<FrogVariant>readVariant($$0, Registries.FROG_VARIANT).ifPresent(this::setVariant);
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      ProfilerFiller $$1 = Profiler.get();
      $$1.push("frogBrain");
      this.getBrain().tick($$0, this);
      $$1.pop();
      $$1.push("frogActivityUpdate");
      FrogAi.updateActivity(this);
      $$1.pop();
      super.customServerAiStep($$0);
   }

   @Override
   public void tick() {
      if (this.level().isClientSide()) {
         this.swimIdleAnimationState.animateWhen(this.isInWater() && !this.walkAnimation.isMoving(), this.tickCount);
      }

      super.tick();
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      if (DATA_POSE.equals($$0)) {
         net.minecraft.world.entity.Pose $$1 = this.getPose();
         if ($$1 == net.minecraft.world.entity.Pose.LONG_JUMPING) {
            this.jumpAnimationState.start(this.tickCount);
         } else {
            this.jumpAnimationState.stop();
         }

         if ($$1 == net.minecraft.world.entity.Pose.CROAKING) {
            this.croakAnimationState.start(this.tickCount);
         } else {
            this.croakAnimationState.stop();
         }

         if ($$1 == net.minecraft.world.entity.Pose.USING_TONGUE) {
            this.tongueAnimationState.start(this.tickCount);
         } else {
            this.tongueAnimationState.stop();
         }
      }

      super.onSyncedDataUpdated($$0);
   }

   @Override
   protected void updateWalkAnimation(float $$0) {
      float $$1;
      if (this.jumpAnimationState.isStarted()) {
         $$1 = 0.0F;
      } else {
         $$1 = Math.min($$0 * 25.0F, 1.0F);
      }

      this.walkAnimation.update($$1, 0.4F, this.isBaby() ? 3.0F : 1.0F);
   }

   @Override
   public void playEatingSound() {
      this.level().playSound(null, this, SoundEvents.FROG_EAT, SoundSource.NEUTRAL, 2.0F, 1.0F);
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.AgeableMob getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      Frog $$2 = net.minecraft.world.entity.EntityType.FROG.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$2 != null) {
         FrogAi.initMemories($$2, $$0.getRandom());
      }

      return $$2;
   }

   @Override
   public boolean isBaby() {
      return false;
   }

   @Override
   public void setBaby(boolean $$0) {
   }

   @Override
   public void spawnChildFromBreeding(ServerLevel $$0, Animal $$1) {
      this.finalizeSpawnChildFromBreeding($$0, $$1, null);
      this.getBrain().setMemory(MemoryModuleType.IS_PREGNANT, Unit.INSTANCE);
   }

   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      VariantUtils.<FrogVariant>selectVariantToSpawn(SpawnContext.create($$0, this.blockPosition()), Registries.FROG_VARIANT).ifPresent(this::setVariant);
      FrogAi.initMemories(this, $$0.getRandom());
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes()
         .add(Attributes.MOVEMENT_SPEED, 1.0)
         .add(Attributes.MAX_HEALTH, 10.0)
         .add(Attributes.ATTACK_DAMAGE, 10.0)
         .add(Attributes.STEP_HEIGHT, 1.0);
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.FROG_AMBIENT;
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.FROG_HURT;
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.FROG_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.FROG_STEP, 0.15F, 1.0F);
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   protected int calculateFallDamage(double $$0, float $$1) {
      return super.calculateFallDamage($$0, $$1) - 5;
   }

   @Override
   protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
      this.moveRelative(this.getSpeed(), $$0);
      this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
   }

   public static boolean canEat(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0 instanceof Slime $$1 && $$1.getSize() != 1 ? false : $$0.getType().is(EntityTypeTags.FROG_FOOD);
   }

   @Override
   protected PathNavigation createNavigation(Level $$0) {
      return new Frog.FrogPathNavigation(this, $$0);
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.LivingEntity getTarget() {
      return this.getTargetFromBrain();
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.FROG_FOOD);
   }

   public static boolean checkFrogSpawnRules(
      net.minecraft.world.entity.EntityType<? extends Animal> $$0,
      LevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      return $$1.getBlockState($$3.below()).is(BlockTags.FROGS_SPAWNABLE_ON) && isBrightEnoughToSpawn($$1, $$3);
   }

   class FrogLookControl extends LookControl {
      FrogLookControl(final net.minecraft.world.entity.Mob $$0) {
         super($$0);
      }

      @Override
      protected boolean resetXRotOnTick() {
         return Frog.this.getTongueTarget().isEmpty();
      }
   }

   static class FrogNodeEvaluator extends AmphibiousNodeEvaluator {
      private final MutableBlockPos belowPos = new MutableBlockPos();

      public FrogNodeEvaluator(boolean $$0) {
         super($$0);
      }

      public Node getStart() {
         return !this.mob.isInWater()
            ? super.getStart()
            : this.getStartNode(
               new BlockPos(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY), Mth.floor(this.mob.getBoundingBox().minZ))
            );
      }

      public PathType getPathType(PathfindingContext $$0, int $$1, int $$2, int $$3) {
         this.belowPos.set($$1, $$2 - 1, $$3);
         BlockState $$4 = $$0.getBlockState(this.belowPos);
         return $$4.is(BlockTags.FROG_PREFER_JUMP_TO) ? PathType.OPEN : super.getPathType($$0, $$1, $$2, $$3);
      }
   }

   static class FrogPathNavigation extends AmphibiousPathNavigation {
      FrogPathNavigation(Frog $$0, Level $$1) {
         super($$0, $$1);
      }

      @Override
      public boolean canCutCorner(PathType $$0) {
         return $$0 != PathType.WATER_BORDER && super.canCutCorner($$0);
      }

      @Override
      protected PathFinder createPathFinder(int $$0) {
         this.nodeEvaluator = new Frog.FrogNodeEvaluator(true);
         return new PathFinder(this.nodeEvaluator, $$0);
      }
   }
}
