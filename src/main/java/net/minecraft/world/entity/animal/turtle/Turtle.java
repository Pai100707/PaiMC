package net.minecraft.world.entity.animal.turtle;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class Turtle extends Animal {
   private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
   private static final float BABY_SCALE = 0.3F;
   private static final net.minecraft.world.entity.EntityDimensions BABY_DIMENSIONS = net.minecraft.world.entity.EntityType.TURTLE
      .getDimensions()
      .withAttachments(
         net.minecraft.world.entity.EntityAttachments.builder()
            .attach(net.minecraft.world.entity.EntityAttachment.PASSENGER, 0.0F, net.minecraft.world.entity.EntityType.TURTLE.getHeight(), -0.25F)
      )
      .scale(0.3F);
   private static final boolean DEFAULT_HAS_EGG = false;
   int layEggCounter;
   public static final TargetingConditions.Selector BABY_ON_LAND_SELECTOR = ($$0, $$1) -> $$0.isBaby() && !$$0.isInWater();
   BlockPos homePos = BlockPos.ZERO;
   
   BlockPos travelPos;
   boolean goingHome;

   public Turtle(net.minecraft.world.entity.EntityType<? extends Turtle> $$0, Level $$1) {
      super($$0, $$1);
      this.setPathfindingMalus(PathType.WATER, 0.0F);
      this.setPathfindingMalus(PathType.DOOR_IRON_CLOSED, -1.0F);
      this.setPathfindingMalus(PathType.DOOR_WOOD_CLOSED, -1.0F);
      this.setPathfindingMalus(PathType.DOOR_OPEN, -1.0F);
      this.moveControl = new Turtle.TurtleMoveControl(this);
   }

   public void setHomePos(BlockPos $$0) {
      this.homePos = $$0;
   }

   public boolean hasEgg() {
      return (Boolean)this.entityData.get(HAS_EGG);
   }

   void setHasEgg(boolean $$0) {
      this.entityData.set(HAS_EGG, $$0);
   }

   public boolean isLayingEgg() {
      return (Boolean)this.entityData.get(LAYING_EGG);
   }

   void setLayingEgg(boolean $$0) {
      this.layEggCounter = $$0 ? 1 : 0;
      this.entityData.set(LAYING_EGG, $$0);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(HAS_EGG, false);
      $$0.define(LAYING_EGG, false);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("home_pos", BlockPos.CODEC, this.homePos);
      $$0.putBoolean("has_egg", this.hasEgg());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      this.setHomePos($$0.read("home_pos", BlockPos.CODEC).orElse(this.blockPosition()));
      super.readAdditionalSaveData($$0);
      this.setHasEgg($$0.getBooleanOr("has_egg", false));
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      this.setHomePos(this.blockPosition());
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   public static boolean checkTurtleSpawnRules(
      net.minecraft.world.entity.EntityType<Turtle> $$0, LevelAccessor $$1, net.minecraft.world.entity.EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4
   ) {
      return $$3.getY() < $$1.getSeaLevel() + 4 && TurtleEggBlock.onSand($$1, $$3) && isBrightEnoughToSpawn($$1, $$3);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new Turtle.TurtlePanicGoal(this, 1.2));
      this.goalSelector.addGoal(1, new Turtle.TurtleBreedGoal(this, 1.0));
      this.goalSelector.addGoal(1, new Turtle.TurtleLayEggGoal(this, 1.0));
      this.goalSelector.addGoal(2, new TemptGoal(this, 1.1, $$0 -> $$0.is(ItemTags.TURTLE_FOOD), false));
      this.goalSelector.addGoal(3, new Turtle.TurtleGoToWaterGoal(this, 1.0));
      this.goalSelector.addGoal(4, new Turtle.TurtleGoHomeGoal(this, 1.0));
      this.goalSelector.addGoal(7, new Turtle.TurtleTravelGoal(this, 1.0));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(9, new Turtle.TurtleRandomStrollGoal(this, 1.0, 100));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.STEP_HEIGHT, 1.0);
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   public int getAmbientSoundInterval() {
      return 200;
   }

   
   @Override
   protected SoundEvent getAmbientSound() {
      return !this.isInWater() && this.onGround() && !this.isBaby() ? SoundEvents.TURTLE_AMBIENT_LAND : super.getAmbientSound();
   }

   @Override
   protected void playSwimSound(float $$0) {
      super.playSwimSound($$0 * 1.5F);
   }

   @Override
   protected SoundEvent getSwimSound() {
      return SoundEvents.TURTLE_SWIM;
   }

   
   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return this.isBaby() ? SoundEvents.TURTLE_HURT_BABY : SoundEvents.TURTLE_HURT;
   }

   
   @Override
   protected SoundEvent getDeathSound() {
      return this.isBaby() ? SoundEvents.TURTLE_DEATH_BABY : SoundEvents.TURTLE_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      SoundEvent $$2 = this.isBaby() ? SoundEvents.TURTLE_SHAMBLE_BABY : SoundEvents.TURTLE_SHAMBLE;
      this.playSound($$2, 0.15F, 1.0F);
   }

   @Override
   public boolean canFallInLove() {
      return super.canFallInLove() && !this.hasEgg();
   }

   @Override
   protected float nextStep() {
      return this.moveDist + 0.15F;
   }

   @Override
   public float getAgeScale() {
      return this.isBaby() ? 0.3F : 1.0F;
   }

   @Override
   protected PathNavigation createNavigation(Level $$0) {
      return new Turtle.TurtlePathNavigation(this, $$0);
   }

   
   @Override
   public net.minecraft.world.entity.AgeableMob getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      return net.minecraft.world.entity.EntityType.TURTLE.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.TURTLE_FOOD);
   }

   @Override
   public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
      if (!this.goingHome && $$1.getFluidState($$0).is(FluidTags.WATER)) {
         return 10.0F;
      } else {
         return TurtleEggBlock.onSand($$1, $$0) ? 10.0F : $$1.getPathfindingCostFromLightLevels($$0);
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.isAlive() && this.isLayingEgg() && this.layEggCounter >= 1 && this.layEggCounter % 5 == 0) {
         BlockPos $$0 = this.blockPosition();
         if (TurtleEggBlock.onSand(this.level(), $$0)) {
            this.level().levelEvent(2001, $$0, Block.getId(this.level().getBlockState($$0.below())));
            this.gameEvent(GameEvent.ENTITY_ACTION);
         }
      }
   }

   @Override
   protected void ageBoundaryReached() {
      super.ageBoundaryReached();
      if (!this.isBaby() && this.level() instanceof ServerLevel $$0 && (Boolean)$$0.getGameRules().get(GameRules.MOB_DROPS)) {
         this.dropFromGiftLootTable($$0, BuiltInLootTables.TURTLE_GROW, this::spawnAtLocation);
      }
   }

   @Override
   protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
      this.moveRelative(0.1F, $$0);
      this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
      if (this.getTarget() == null && (!this.goingHome || !this.homePos.closerToCenterThan(this.position(), 20.0))) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
      }
   }

   @Override
   public boolean canBeLeashed() {
      return false;
   }

   @Override
   public void thunderHit(ServerLevel $$0, net.minecraft.world.entity.LightningBolt $$1) {
      this.hurtServer($$0, this.damageSources().lightningBolt(), Float.MAX_VALUE);
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose $$0) {
      return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
   }

   static class TurtleBreedGoal extends BreedGoal {
      private final Turtle turtle;

      TurtleBreedGoal(Turtle $$0, double $$1) {
         super($$0, $$1);
         this.turtle = $$0;
      }

      @Override
      public boolean canUse() {
         return super.canUse() && !this.turtle.hasEgg();
      }

      @Override
      protected void breed() {
         ServerPlayer $$0 = this.animal.getLoveCause();
         if ($$0 == null && this.partner.getLoveCause() != null) {
            $$0 = this.partner.getLoveCause();
         }

         if ($$0 != null) {
            $$0.awardStat(Stats.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger($$0, this.animal, this.partner, null);
         }

         this.turtle.setHasEgg(true);
         this.animal.setAge(6000);
         this.partner.setAge(6000);
         this.animal.resetLove();
         this.partner.resetLove();
         RandomSource $$1 = this.animal.getRandom();
         if ((Boolean)getServerLevel(this.level).getGameRules().get(GameRules.MOB_DROPS)) {
            this.level
               .addFreshEntity(
                  new net.minecraft.world.entity.ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), $$1.nextInt(7) + 1)
               );
         }
      }
   }

   static class TurtleGoHomeGoal extends Goal {
      private final Turtle turtle;
      private final double speedModifier;
      private boolean stuck;
      private int closeToHomeTryTicks;
      private static final int GIVE_UP_TICKS = 600;

      TurtleGoHomeGoal(Turtle $$0, double $$1) {
         this.turtle = $$0;
         this.speedModifier = $$1;
      }

      @Override
      public boolean canUse() {
         if (this.turtle.isBaby()) {
            return false;
         } else if (this.turtle.hasEgg()) {
            return true;
         } else {
            return this.turtle.getRandom().nextInt(reducedTickDelay(700)) != 0 ? false : !this.turtle.homePos.closerToCenterThan(this.turtle.position(), 64.0);
         }
      }

      @Override
      public void start() {
         this.turtle.goingHome = true;
         this.stuck = false;
         this.closeToHomeTryTicks = 0;
      }

      @Override
      public void stop() {
         this.turtle.goingHome = false;
      }

      @Override
      public boolean canContinueToUse() {
         return !this.turtle.homePos.closerToCenterThan(this.turtle.position(), 7.0) && !this.stuck && this.closeToHomeTryTicks <= this.adjustedTickDelay(600);
      }

      @Override
      public void tick() {
         BlockPos $$0 = this.turtle.homePos;
         boolean $$1 = $$0.closerToCenterThan(this.turtle.position(), 16.0);
         if ($$1) {
            this.closeToHomeTryTicks++;
         }

         if (this.turtle.getNavigation().isDone()) {
            Vec3 $$2 = Vec3.atBottomCenterOf($$0);
            Vec3 $$3 = DefaultRandomPos.getPosTowards(this.turtle, 16, 3, $$2, (float) (Math.PI / 10));
            if ($$3 == null) {
               $$3 = DefaultRandomPos.getPosTowards(this.turtle, 8, 7, $$2, (float) (Math.PI / 2));
            }

            if ($$3 != null && !$$1 && !this.turtle.level().getBlockState(BlockPos.containing($$3)).is(Blocks.WATER)) {
               $$3 = DefaultRandomPos.getPosTowards(this.turtle, 16, 5, $$2, (float) (Math.PI / 2));
            }

            if ($$3 == null) {
               this.stuck = true;
               return;
            }

            this.turtle.getNavigation().moveTo($$3.x, $$3.y, $$3.z, this.speedModifier);
         }
      }
   }

   static class TurtleGoToWaterGoal extends MoveToBlockGoal {
      private static final int GIVE_UP_TICKS = 1200;
      private final Turtle turtle;

      TurtleGoToWaterGoal(Turtle $$0, double $$1) {
         super($$0, $$0.isBaby() ? 2.0 : $$1, 24);
         this.turtle = $$0;
         this.verticalSearchStart = -1;
      }

      @Override
      public boolean canContinueToUse() {
         return !this.turtle.isInWater() && this.tryTicks <= 1200 && this.isValidTarget(this.turtle.level(), this.blockPos);
      }

      @Override
      public boolean canUse() {
         if (this.turtle.isBaby() && !this.turtle.isInWater()) {
            return super.canUse();
         } else {
            return !this.turtle.goingHome && !this.turtle.isInWater() && !this.turtle.hasEgg() ? super.canUse() : false;
         }
      }

      @Override
      public boolean shouldRecalculatePath() {
         return this.tryTicks % 160 == 0;
      }

      @Override
      protected boolean isValidTarget(LevelReader $$0, BlockPos $$1) {
         return $$0.getBlockState($$1).is(Blocks.WATER);
      }
   }

   static class TurtleLayEggGoal extends MoveToBlockGoal {
      private final Turtle turtle;

      TurtleLayEggGoal(Turtle $$0, double $$1) {
         super($$0, $$1, 16);
         this.turtle = $$0;
      }

      @Override
      public boolean canUse() {
         return this.turtle.hasEgg() && this.turtle.homePos.closerToCenterThan(this.turtle.position(), 9.0) ? super.canUse() : false;
      }

      @Override
      public boolean canContinueToUse() {
         return super.canContinueToUse() && this.turtle.hasEgg() && this.turtle.homePos.closerToCenterThan(this.turtle.position(), 9.0);
      }

      @Override
      public void tick() {
         super.tick();
         BlockPos $$0 = this.turtle.blockPosition();
         if (!this.turtle.isInWater() && this.isReachedTarget()) {
            if (this.turtle.layEggCounter < 1) {
               this.turtle.setLayingEgg(true);
            } else if (this.turtle.layEggCounter > this.adjustedTickDelay(200)) {
               Level $$1 = this.turtle.level();
               $$1.playSound(null, $$0, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + $$1.random.nextFloat() * 0.2F);
               BlockPos $$2 = this.blockPos.above();
               BlockState $$3 = (BlockState)Blocks.TURTLE_EGG.defaultBlockState().setValue(TurtleEggBlock.EGGS, this.turtle.random.nextInt(4) + 1);
               $$1.setBlock($$2, $$3, 3);
               $$1.gameEvent(GameEvent.BLOCK_PLACE, $$2, Context.of(this.turtle, $$3));
               this.turtle.setHasEgg(false);
               this.turtle.setLayingEgg(false);
               this.turtle.setInLoveTime(600);
            }

            if (this.turtle.isLayingEgg()) {
               this.turtle.layEggCounter++;
            }
         }
      }

      @Override
      protected boolean isValidTarget(LevelReader $$0, BlockPos $$1) {
         return !$$0.isEmptyBlock($$1.above()) ? false : TurtleEggBlock.isSand($$0, $$1);
      }
   }

   static class TurtleMoveControl extends MoveControl {
      private final Turtle turtle;

      TurtleMoveControl(Turtle $$0) {
         super($$0);
         this.turtle = $$0;
      }

      private void updateSpeed() {
         if (this.turtle.isInWater()) {
            this.turtle.setDeltaMovement(this.turtle.getDeltaMovement().add(0.0, 0.005, 0.0));
            if (!this.turtle.homePos.closerToCenterThan(this.turtle.position(), 16.0)) {
               this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 2.0F, 0.08F));
            }

            if (this.turtle.isBaby()) {
               this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 3.0F, 0.06F));
            }
         } else if (this.turtle.onGround()) {
            this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 2.0F, 0.06F));
         }
      }

      @Override
      public void tick() {
         this.updateSpeed();
         if (this.operation == MoveControl.Operation.MOVE_TO && !this.turtle.getNavigation().isDone()) {
            double $$0 = this.wantedX - this.turtle.getX();
            double $$1 = this.wantedY - this.turtle.getY();
            double $$2 = this.wantedZ - this.turtle.getZ();
            double $$3 = Math.sqrt($$0 * $$0 + $$1 * $$1 + $$2 * $$2);
            if ($$3 < 1.0E-5F) {
               this.mob.setSpeed(0.0F);
            } else {
               $$1 /= $$3;
               float $$4 = (float)(Mth.atan2($$2, $$0) * 180.0F / (float)Math.PI) - 90.0F;
               this.turtle.setYRot(this.rotlerp(this.turtle.getYRot(), $$4, 90.0F));
               this.turtle.yBodyRot = this.turtle.getYRot();
               float $$5 = (float)(this.speedModifier * this.turtle.getAttributeValue(Attributes.MOVEMENT_SPEED));
               this.turtle.setSpeed(Mth.lerp(0.125F, this.turtle.getSpeed(), $$5));
               this.turtle.setDeltaMovement(this.turtle.getDeltaMovement().add(0.0, this.turtle.getSpeed() * $$1 * 0.1, 0.0));
            }
         } else {
            this.turtle.setSpeed(0.0F);
         }
      }
   }

   static class TurtlePanicGoal extends PanicGoal {
      TurtlePanicGoal(Turtle $$0, double $$1) {
         super($$0, $$1);
      }

      @Override
      public boolean canUse() {
         if (!this.shouldPanic()) {
            return false;
         } else {
            BlockPos $$0 = this.lookForWater(this.mob.level(), this.mob, 7);
            if ($$0 != null) {
               this.posX = $$0.getX();
               this.posY = $$0.getY();
               this.posZ = $$0.getZ();
               return true;
            } else {
               return this.findRandomPosition();
            }
         }
      }
   }

   static class TurtlePathNavigation extends AmphibiousPathNavigation {
      TurtlePathNavigation(Turtle $$0, Level $$1) {
         super($$0, $$1);
      }

      @Override
      public boolean isStableDestination(BlockPos $$0) {
         return this.mob instanceof Turtle $$1 && $$1.travelPos != null
            ? this.level.getBlockState($$0).is(Blocks.WATER)
            : !this.level.getBlockState($$0.below()).isAir();
      }
   }

   static class TurtleRandomStrollGoal extends RandomStrollGoal {
      private final Turtle turtle;

      TurtleRandomStrollGoal(Turtle $$0, double $$1, int $$2) {
         super($$0, $$1, $$2);
         this.turtle = $$0;
      }

      @Override
      public boolean canUse() {
         return !this.mob.isInWater() && !this.turtle.goingHome && !this.turtle.hasEgg() ? super.canUse() : false;
      }
   }

   static class TurtleTravelGoal extends Goal {
      private final Turtle turtle;
      private final double speedModifier;
      private boolean stuck;

      TurtleTravelGoal(Turtle $$0, double $$1) {
         this.turtle = $$0;
         this.speedModifier = $$1;
      }

      @Override
      public boolean canUse() {
         return !this.turtle.goingHome && !this.turtle.hasEgg() && this.turtle.isInWater();
      }

      @Override
      public void start() {
         int $$0 = 512;
         int $$1 = 4;
         RandomSource $$2 = this.turtle.random;
         int $$3 = $$2.nextInt(1025) - 512;
         int $$4 = $$2.nextInt(9) - 4;
         int $$5 = $$2.nextInt(1025) - 512;
         if ($$4 + this.turtle.getY() > this.turtle.level().getSeaLevel() - 1) {
            $$4 = 0;
         }

         this.turtle.travelPos = BlockPos.containing($$3 + this.turtle.getX(), $$4 + this.turtle.getY(), $$5 + this.turtle.getZ());
         this.stuck = false;
      }

      @Override
      public void tick() {
         if (this.turtle.travelPos == null) {
            this.stuck = true;
         } else {
            if (this.turtle.getNavigation().isDone()) {
               Vec3 $$0 = Vec3.atBottomCenterOf(this.turtle.travelPos);
               Vec3 $$1 = DefaultRandomPos.getPosTowards(this.turtle, 16, 3, $$0, (float) (Math.PI / 10));
               if ($$1 == null) {
                  $$1 = DefaultRandomPos.getPosTowards(this.turtle, 8, 7, $$0, (float) (Math.PI / 2));
               }

               if ($$1 != null) {
                  int $$2 = Mth.floor($$1.x);
                  int $$3 = Mth.floor($$1.z);
                  int $$4 = 34;
                  if (!this.turtle.level().hasChunksAt($$2 - 34, $$3 - 34, $$2 + 34, $$3 + 34)) {
                     $$1 = null;
                  }
               }

               if ($$1 == null) {
                  this.stuck = true;
                  return;
               }

               this.turtle.getNavigation().moveTo($$1.x, $$1.y, $$1.z, this.speedModifier);
            }
         }
      }

      @Override
      public boolean canContinueToUse() {
         return !this.turtle.getNavigation().isDone() && !this.stuck && !this.turtle.goingHome && !this.turtle.isInLove() && !this.turtle.hasEgg();
      }

      @Override
      public void stop() {
         this.turtle.travelPos = null;
         super.stop();
      }
   }
}
