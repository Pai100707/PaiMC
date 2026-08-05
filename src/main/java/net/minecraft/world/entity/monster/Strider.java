package net.minecraft.world.entity.monster;

import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
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

public class Strider extends Animal implements net.minecraft.world.entity.ItemSteerable {
   private static final Identifier SUFFOCATING_MODIFIER_ID = Identifier.withDefaultNamespace("suffocating");
   private static final AttributeModifier SUFFOCATING_MODIFIER = new AttributeModifier(
      SUFFOCATING_MODIFIER_ID, -0.34F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE
   );
   private static final float SUFFOCATE_STEERING_MODIFIER = 0.35F;
   private static final float STEERING_MODIFIER = 0.55F;
   private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
   private final net.minecraft.world.entity.ItemBasedSteering steering = new net.minecraft.world.entity.ItemBasedSteering(this.entityData, DATA_BOOST_TIME);
   
   private TemptGoal temptGoal;

   public Strider(net.minecraft.world.entity.EntityType<? extends Strider> $$0, Level $$1) {
      super($$0, $$1);
      this.blocksBuilding = true;
      this.setPathfindingMalus(PathType.WATER, -1.0F);
      this.setPathfindingMalus(PathType.LAVA, 0.0F);
      this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
      this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
   }

   public static boolean checkStriderSpawnRules(
      net.minecraft.world.entity.EntityType<Strider> $$0, LevelAccessor $$1, net.minecraft.world.entity.EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4
   ) {
      MutableBlockPos $$5 = $$3.mutable();

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
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_BOOST_TIME, 0);
      $$0.define(DATA_SUFFOCATING, false);
   }

   @Override
   public boolean canUseSlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return $$0 != net.minecraft.world.entity.EquipmentSlot.SADDLE ? super.canUseSlot($$0) : this.isAlive() && !this.isBaby();
   }

   @Override
   protected boolean canDispenserEquipIntoSlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return $$0 == net.minecraft.world.entity.EquipmentSlot.SADDLE || super.canDispenserEquipIntoSlot($$0);
   }

   @Override
   protected Holder<SoundEvent> getEquipSound(net.minecraft.world.entity.EquipmentSlot $$0, ItemStack $$1, Equippable $$2) {
      return (Holder<SoundEvent>)($$0 == net.minecraft.world.entity.EquipmentSlot.SADDLE ? SoundEvents.STRIDER_SADDLE : super.getEquipSound($$0, $$1, $$2));
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.65));
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
      this.temptGoal = new TemptGoal(this, 1.4, $$0 -> $$0.is(ItemTags.STRIDER_TEMPT_ITEMS), false);
      this.goalSelector.addGoal(3, this.temptGoal);
      this.goalSelector.addGoal(4, new Strider.StriderGoToLavaGoal(this, 1.0));
      this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0));
      this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 60));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0F));
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
      return (Boolean)this.entityData.get(DATA_SUFFOCATING);
   }

   @Override
   public boolean canStandOnFluid(FluidState $$0) {
      return $$0.is(FluidTags.LAVA);
   }

   @Override
   protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.EntityDimensions $$1, float $$2) {
      if (!this.level().isClientSide()) {
         return super.getPassengerAttachmentPoint($$0, $$1, $$2);
      } else {
         float $$3 = Math.min(0.25F, this.walkAnimation.speed());
         float $$4 = this.walkAnimation.position();
         float $$5 = 0.12F * Mth.cos($$4 * 1.5F) * 2.0F * $$3;
         return super.getPassengerAttachmentPoint($$0, $$1, $$2).add(0.0, $$5 * $$2, 0.0);
      }
   }

   @Override
   public boolean checkSpawnObstruction(LevelReader $$0) {
      return $$0.isUnobstructed(this);
   }

   
   @Override
   public net.minecraft.world.entity.LivingEntity getControllingPassenger() {
      return (net.minecraft.world.entity.LivingEntity)(this.isSaddled()
            && this.getFirstPassenger() instanceof Player $$0
            && $$0.isHolding(Items.WARPED_FUNGUS_ON_A_STICK)
         ? $$0
         : super.getControllingPassenger());
   }

   @Override
   public Vec3 getDismountLocationForPassenger(net.minecraft.world.entity.LivingEntity $$0) {
      Vec3[] $$1 = new Vec3[]{
         getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), $$0.getYRot()),
         getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), $$0.getYRot() - 22.5F),
         getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), $$0.getYRot() + 22.5F),
         getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), $$0.getYRot() - 45.0F),
         getCollisionHorizontalEscapeVector(this.getBbWidth(), $$0.getBbWidth(), $$0.getYRot() + 45.0F)
      };
      Set<BlockPos> $$2 = Sets.newLinkedHashSet();
      double $$3 = this.getBoundingBox().maxY;
      double $$4 = this.getBoundingBox().minY - 0.5;
      MutableBlockPos $$5 = new MutableBlockPos();

      for (Vec3 $$6 : $$1) {
         $$5.set(this.getX() + $$6.x, $$3, this.getZ() + $$6.z);

         for (double $$7 = $$3; $$7 > $$4; $$7--) {
            $$2.add($$5.immutable());
            $$5.move(Direction.DOWN);
         }
      }

      for (BlockPos $$8 : $$2) {
         if (!this.level().getFluidState($$8).is(FluidTags.LAVA)) {
            double $$9 = this.level().getBlockFloorHeight($$8);
            if (DismountHelper.isBlockFloorValid($$9)) {
               Vec3 $$10 = Vec3.upFromBottomCenterOf($$8, $$9);
               UnmodifiableIterator var14 = $$0.getDismountPoses().iterator();

               while (var14.hasNext()) {
                  net.minecraft.world.entity.Pose $$11 = (net.minecraft.world.entity.Pose)var14.next();
                  AABB $$12 = $$0.getLocalBoundsForPose($$11);
                  if (DismountHelper.canDismountTo(this.level(), $$0, $$12.move($$10))) {
                     $$0.setPose($$11);
                     return $$10;
                  }
               }
            }
         }
      }

      return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
   }

   @Override
   protected void tickRidden(Player $$0, Vec3 $$1) {
      this.setRot($$0.getYRot(), $$0.getXRot() * 0.5F);
      this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
      this.steering.tickBoost();
      super.tickRidden($$0, $$1);
   }

   @Override
   protected Vec3 getRiddenInput(Player $$0, Vec3 $$1) {
      return new Vec3(0.0, 0.0, 1.0);
   }

   @Override
   protected float getRiddenSpeed(Player $$0) {
      return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (this.isSuffocating() ? 0.35F : 0.55F) * this.steering.boostFactor());
   }

   @Override
   protected float nextStep() {
      return this.moveDist + 0.6F;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(this.isInLava() ? SoundEvents.STRIDER_STEP_LAVA : SoundEvents.STRIDER_STEP, 1.0F, 1.0F);
   }

   @Override
   public boolean boost() {
      return this.steering.boost(this.getRandom());
   }

   @Override
   protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
      if (this.isInLava()) {
         this.resetFallDistance();
      } else {
         super.checkFallDamage($$0, $$1, $$2, $$3);
      }
   }

   @Override
   public void tick() {
      if (this.isBeingTempted() && this.random.nextInt(140) == 0) {
         this.makeSound(SoundEvents.STRIDER_HAPPY);
      } else if (this.isPanicking() && this.random.nextInt(60) == 0) {
         this.makeSound(SoundEvents.STRIDER_RETREAT);
      }

      if (!this.isNoAi()) {
         BlockState $$0 = this.level().getBlockState(this.blockPosition());
         BlockState $$1 = this.getBlockStateOnLegacy();
         boolean $$2 = $$0.is(BlockTags.STRIDER_WARM_BLOCKS) || $$1.is(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0;
         boolean $$4 = this.getVehicle() instanceof Strider $$3 && $$3.isSuffocating();
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
         if ($$0.isAbove(LiquidBlock.SHAPE_STABLE, this.blockPosition(), true) && !this.level().getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
            this.setOnGround(true);
         } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
         }
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes().add(Attributes.MOVEMENT_SPEED, 0.175F);
   }

   
   @Override
   protected SoundEvent getAmbientSound() {
      return !this.isPanicking() && !this.isBeingTempted() ? SoundEvents.STRIDER_AMBIENT : null;
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
   protected boolean canAddPassenger(net.minecraft.world.entity.Entity $$0) {
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
      return new Strider.StriderPathNavigation(this, $$0);
   }

   @Override
   public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
      if ($$1.getBlockState($$0).getFluidState().is(FluidTags.LAVA)) {
         return 10.0F;
      } else {
         return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
      }
   }

   
   public Strider getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      return net.minecraft.world.entity.EntityType.STRIDER.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
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
      } else {
         InteractionResult $$3 = super.mobInteract($$0, $$1);
         if (!$$3.consumesAction()) {
            ItemStack $$4 = $$0.getItemInHand($$1);
            return (InteractionResult)(this.isEquippableInSlot($$4, net.minecraft.world.entity.EquipmentSlot.SADDLE)
               ? $$4.interactLivingEntity($$0, this, $$1)
               : InteractionResult.PASS);
         } else {
            if ($$2 && !this.isSilent()) {
               this.level()
                  .playSound(
                     null,
                     this.getX(),
                     this.getY(),
                     this.getZ(),
                     SoundEvents.STRIDER_EAT,
                     this.getSoundSource(),
                     1.0F,
                     1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                  );
            }

            return $$3;
         }
      }
   }

   @Override
   public Vec3 getLeashOffset() {
      return new Vec3(0.0, 0.6F * this.getEyeHeight(), this.getBbWidth() * 0.4F);
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      if (this.isBaby()) {
         return super.finalizeSpawn($$0, $$1, $$2, $$3);
      } else {
         RandomSource $$4 = $$0.getRandom();
         if ($$4.nextInt(30) == 0) {
            net.minecraft.world.entity.Mob $$5 = net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN
               .create($$0.getLevel(), net.minecraft.world.entity.EntitySpawnReason.JOCKEY);
            if ($$5 != null) {
               $$3 = this.spawnJockey($$0, $$1, $$5, new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds($$4), false));
               $$5.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
               this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
               this.setGuaranteedDrop(net.minecraft.world.entity.EquipmentSlot.SADDLE);
            }
         } else if ($$4.nextInt(10) == 0) {
            net.minecraft.world.entity.AgeableMob $$6 = net.minecraft.world.entity.EntityType.STRIDER
               .create($$0.getLevel(), net.minecraft.world.entity.EntitySpawnReason.JOCKEY);
            if ($$6 != null) {
               $$6.setAge(-24000);
               $$3 = this.spawnJockey($$0, $$1, $$6, null);
            }
         } else {
            $$3 = new net.minecraft.world.entity.AgeableMob.AgeableMobGroupData(0.5F);
         }

         return super.finalizeSpawn($$0, $$1, $$2, $$3);
      }
   }

   private net.minecraft.world.entity.SpawnGroupData spawnJockey(
      ServerLevelAccessor $$0, DifficultyInstance $$1, net.minecraft.world.entity.Mob $$2, net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      $$2.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
      $$2.finalizeSpawn($$0, $$1, net.minecraft.world.entity.EntitySpawnReason.JOCKEY, $$3);
      $$2.startRiding(this, true, false);
      return new net.minecraft.world.entity.AgeableMob.AgeableMobGroupData(0.0F);
   }

   static class StriderGoToLavaGoal extends MoveToBlockGoal {
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

   static class StriderPathNavigation extends GroundPathNavigation {
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
         return $$0 != PathType.LAVA && $$0 != PathType.DAMAGE_FIRE && $$0 != PathType.DANGER_FIRE ? super.hasValidPathType($$0) : true;
      }

      @Override
      public boolean isStableDestination(BlockPos $$0) {
         return this.level.getBlockState($$0).is(Blocks.LAVA) || super.isStableDestination($$0);
      }
   }
}
