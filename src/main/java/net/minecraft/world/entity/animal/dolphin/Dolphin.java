package net.minecraft.world.entity.animal.dolphin;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreathAirGoal;
import net.minecraft.world.entity.ai.goal.DolphinJumpGoal;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Dolphin extends AgeableWaterCreature {
   private static final EntityDataAccessor<Boolean> GOT_FISH = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> MOISTNESS_LEVEL = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.INT);
   static final TargetingConditions SWIM_WITH_PLAYER_TARGETING = TargetingConditions.forNonCombat().range(10.0).ignoreLineOfSight();
   public static final int TOTAL_AIR_SUPPLY = 4800;
   private static final int TOTAL_MOISTNESS_LEVEL = 2400;
   public static final Predicate<ItemEntity> ALLOWED_ITEMS = $$0 -> !$$0.hasPickUpDelay() && $$0.isAlive() && $$0.isInWater();
   public static final float BABY_SCALE = 0.65F;
   private static final boolean DEFAULT_GOT_FISH = false;
   @Nullable
   BlockPos treasurePos;

   public Dolphin(net.minecraft.world.entity.EntityType<? extends Dolphin> $$0, Level $$1) {
      super($$0, $$1);
      this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
      this.lookControl = new SmoothSwimmingLookControl(this, 10);
      this.setCanPickUpLoot(true);
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      this.setAirSupply(this.getMaxAirSupply());
      this.setXRot(0.0F);
      net.minecraft.world.entity.SpawnGroupData $$4 = Objects.requireNonNullElseGet(
         $$3, () -> new net.minecraft.world.entity.AgeableMob.AgeableMobGroupData(0.1F)
      );
      return super.finalizeSpawn($$0, $$1, $$2, $$4);
   }

   @Nullable
   public Dolphin getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      return net.minecraft.world.entity.EntityType.DOLPHIN.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
   }

   @Override
   public float getAgeScale() {
      return this.isBaby() ? 0.65F : 1.0F;
   }

   @Override
   protected void handleAirSupply(int $$0) {
   }

   public boolean gotFish() {
      return (Boolean)this.entityData.get(GOT_FISH);
   }

   public void setGotFish(boolean $$0) {
      this.entityData.set(GOT_FISH, $$0);
   }

   public int getMoistnessLevel() {
      return (Integer)this.entityData.get(MOISTNESS_LEVEL);
   }

   public void setMoisntessLevel(int $$0) {
      this.entityData.set(MOISTNESS_LEVEL, $$0);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(GOT_FISH, false);
      $$0.define(MOISTNESS_LEVEL, 2400);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putBoolean("GotFish", this.gotFish());
      $$0.putInt("Moistness", this.getMoistnessLevel());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setGotFish($$0.getBooleanOr("GotFish", false));
      this.setMoisntessLevel($$0.getIntOr("Moistness", 2400));
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new BreathAirGoal(this));
      this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
      this.goalSelector.addGoal(1, new Dolphin.DolphinSwimToTreasureGoal(this));
      this.goalSelector.addGoal(2, new Dolphin.DolphinSwimWithPlayerGoal(this, 4.0));
      this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 10));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(5, new DolphinJumpGoal(this, 10));
      this.goalSelector.addGoal(6, new MeleeAttackGoal(this, 1.2F, true));
      this.goalSelector.addGoal(8, new Dolphin.PlayWithItemsGoal());
      this.goalSelector.addGoal(8, new FollowBoatGoal(this));
      this.goalSelector.addGoal(9, new AvoidEntityGoal<>(this, Guardian.class, 8.0F, 1.0, 1.0));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Guardian.class).setAlertOthers());
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 10.0)
         .add(Attributes.MOVEMENT_SPEED, 1.2F)
         .add(Attributes.ATTACK_DAMAGE, 3.0);
   }

   @Override
   protected PathNavigation createNavigation(Level $$0) {
      return new WaterBoundPathNavigation(this, $$0);
   }

   @Override
   public void playAttackSound() {
      this.playSound(SoundEvents.DOLPHIN_ATTACK, 1.0F, 1.0F);
   }

   @Override
   public boolean canAttack(net.minecraft.world.entity.LivingEntity $$0) {
      return !this.isBaby() && super.canAttack($$0);
   }

   @Override
   public int getMaxAirSupply() {
      return 4800;
   }

   @Override
   protected int increaseAirSupply(int $$0) {
      return this.getMaxAirSupply();
   }

   @Override
   public int getMaxHeadXRot() {
      return 1;
   }

   @Override
   public int getMaxHeadYRot() {
      return 1;
   }

   @Override
   protected boolean canRide(net.minecraft.world.entity.Entity $$0) {
      return true;
   }

   @Override
   protected boolean canDispenserEquipIntoSlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return $$0 == net.minecraft.world.entity.EquipmentSlot.MAINHAND && this.canPickUpLoot();
   }

   @Override
   protected void pickUpItem(ServerLevel $$0, ItemEntity $$1) {
      if (this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).isEmpty()) {
         ItemStack $$2 = $$1.getItem();
         if (this.canHoldItem($$2)) {
            this.onItemPickup($$1);
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, $$2);
            this.setGuaranteedDrop(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            this.take($$1, $$2.getCount());
            $$1.discard();
         }
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isNoAi()) {
         this.setAirSupply(this.getMaxAirSupply());
      } else {
         if (this.isInWaterOrRain()) {
            this.setMoisntessLevel(2400);
         } else {
            this.setMoisntessLevel(this.getMoistnessLevel() - 1);
            if (this.getMoistnessLevel() <= 0) {
               this.hurt(this.damageSources().dryOut(), 1.0F);
            }

            if (this.onGround()) {
               this.setDeltaMovement(
                  this.getDeltaMovement().add((this.random.nextFloat() * 2.0F - 1.0F) * 0.2F, 0.5, (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F)
               );
               this.setYRot(this.random.nextFloat() * 360.0F);
               this.setOnGround(false);
               this.needsSync = true;
            }
         }

         if (this.level().isClientSide() && this.isInWater() && this.getDeltaMovement().lengthSqr() > 0.03) {
            Vec3 $$0 = this.getViewVector(0.0F);
            float $$1 = Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)) * 0.3F;
            float $$2 = Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)) * 0.3F;
            float $$3 = 1.2F - this.random.nextFloat() * 0.7F;

            for (int $$4 = 0; $$4 < 2; $$4++) {
               this.level()
                  .addParticle(ParticleTypes.DOLPHIN, this.getX() - $$0.x * $$3 + $$1, this.getY() - $$0.y, this.getZ() - $$0.z * $$3 + $$2, 0.0, 0.0, 0.0);
               this.level()
                  .addParticle(ParticleTypes.DOLPHIN, this.getX() - $$0.x * $$3 - $$1, this.getY() - $$0.y, this.getZ() - $$0.z * $$3 - $$2, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 38) {
         this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
      } else {
         super.handleEntityEvent($$0);
      }
   }

   private void addParticlesAroundSelf(ParticleOptions $$0) {
      for (int $$1 = 0; $$1 < 7; $$1++) {
         double $$2 = this.random.nextGaussian() * 0.01;
         double $$3 = this.random.nextGaussian() * 0.01;
         double $$4 = this.random.nextGaussian() * 0.01;
         this.level().addParticle($$0, this.getRandomX(1.0), this.getRandomY() + 0.2, this.getRandomZ(1.0), $$2, $$3, $$4);
      }
   }

   @Override
   protected InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if (!$$2.isEmpty() && $$2.is(ItemTags.FISHES)) {
         if (!this.level().isClientSide()) {
            this.playSound(SoundEvents.DOLPHIN_EAT, 1.0F, 1.0F);
         }

         if (this.isBaby()) {
            $$2.consume(1, $$0);
            this.ageUp(getSpeedUpSecondsWhenFeeding(-this.age), true);
         } else {
            this.setGotFish(true);
            $$2.consume(1, $$0);
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract($$0, $$1);
      }
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.DOLPHIN_HURT;
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.DOLPHIN_DEATH;
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return this.isInWater() ? SoundEvents.DOLPHIN_AMBIENT_WATER : SoundEvents.DOLPHIN_AMBIENT;
   }

   @Override
   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.DOLPHIN_SPLASH;
   }

   @Override
   protected SoundEvent getSwimSound() {
      return SoundEvents.DOLPHIN_SWIM;
   }

   protected boolean closeToNextPos() {
      BlockPos $$0 = this.getNavigation().getTargetPos();
      return $$0 != null ? $$0.closerToCenterThan(this.position(), 12.0) : false;
   }

   @Override
   protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
      this.moveRelative(this.getSpeed(), $$0);
      this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
      if (this.getTarget() == null) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
      }
   }

   @Override
   public boolean canBeLeashed() {
      return true;
   }

   static class DolphinSwimToTreasureGoal extends Goal {
      private final Dolphin dolphin;
      private boolean stuck;

      DolphinSwimToTreasureGoal(Dolphin $$0) {
         this.dolphin = $$0;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      @Override
      public boolean isInterruptable() {
         return false;
      }

      @Override
      public boolean canUse() {
         return this.dolphin.gotFish() && this.dolphin.getAirSupply() >= 100;
      }

      @Override
      public boolean canContinueToUse() {
         BlockPos $$0 = this.dolphin.treasurePos;
         return $$0 == null
            ? false
            : !BlockPos.containing($$0.getX(), this.dolphin.getY(), $$0.getZ()).closerToCenterThan(this.dolphin.position(), 4.0)
               && !this.stuck
               && this.dolphin.getAirSupply() >= 100;
      }

      @Override
      public void start() {
         if (this.dolphin.level() instanceof ServerLevel) {
            ServerLevel $$0 = (ServerLevel)this.dolphin.level();
            this.stuck = false;
            this.dolphin.getNavigation().stop();
            BlockPos $$1 = this.dolphin.blockPosition();
            BlockPos $$2 = $$0.findNearestMapStructure(StructureTags.DOLPHIN_LOCATED, $$1, 50, false);
            if ($$2 != null) {
               this.dolphin.treasurePos = $$2;
               $$0.broadcastEntityEvent(this.dolphin, (byte)38);
            } else {
               this.stuck = true;
            }
         }
      }

      @Override
      public void stop() {
         BlockPos $$0 = this.dolphin.treasurePos;
         if ($$0 == null || BlockPos.containing($$0.getX(), this.dolphin.getY(), $$0.getZ()).closerToCenterThan(this.dolphin.position(), 4.0) || this.stuck) {
            this.dolphin.setGotFish(false);
         }
      }

      @Override
      public void tick() {
         if (this.dolphin.treasurePos != null) {
            Level $$0 = this.dolphin.level();
            if (this.dolphin.closeToNextPos() || this.dolphin.getNavigation().isDone()) {
               Vec3 $$1 = Vec3.atCenterOf(this.dolphin.treasurePos);
               Vec3 $$2 = DefaultRandomPos.getPosTowards(this.dolphin, 16, 1, $$1, (float) (Math.PI / 8));
               if ($$2 == null) {
                  $$2 = DefaultRandomPos.getPosTowards(this.dolphin, 8, 4, $$1, (float) (Math.PI / 2));
               }

               if ($$2 != null) {
                  BlockPos $$3 = BlockPos.containing($$2);
                  if (!$$0.getFluidState($$3).is(FluidTags.WATER) || !$$0.getBlockState($$3).isPathfindable(PathComputationType.WATER)) {
                     $$2 = DefaultRandomPos.getPosTowards(this.dolphin, 8, 5, $$1, (float) (Math.PI / 2));
                  }
               }

               if ($$2 == null) {
                  this.stuck = true;
                  return;
               }

               this.dolphin.getLookControl().setLookAt($$2.x, $$2.y, $$2.z, this.dolphin.getMaxHeadYRot() + 20, this.dolphin.getMaxHeadXRot());
               this.dolphin.getNavigation().moveTo($$2.x, $$2.y, $$2.z, 1.3);
               if ($$0.random.nextInt(this.adjustedTickDelay(80)) == 0) {
                  $$0.broadcastEntityEvent(this.dolphin, (byte)38);
               }
            }
         }
      }
   }

   static class DolphinSwimWithPlayerGoal extends Goal {
      private final Dolphin dolphin;
      private final double speedModifier;
      @Nullable
      private Player player;

      DolphinSwimWithPlayerGoal(Dolphin $$0, double $$1) {
         this.dolphin = $$0;
         this.speedModifier = $$1;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         this.player = getServerLevel(this.dolphin).getNearestPlayer(Dolphin.SWIM_WITH_PLAYER_TARGETING, this.dolphin);
         return this.player == null ? false : this.player.isSwimming() && this.dolphin.getTarget() != this.player;
      }

      @Override
      public boolean canContinueToUse() {
         return this.player != null && this.player.isSwimming() && this.dolphin.distanceToSqr(this.player) < 256.0;
      }

      @Override
      public void start() {
         this.player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100), this.dolphin);
      }

      @Override
      public void stop() {
         this.player = null;
         this.dolphin.getNavigation().stop();
      }

      @Override
      public void tick() {
         this.dolphin.getLookControl().setLookAt(this.player, this.dolphin.getMaxHeadYRot() + 20, this.dolphin.getMaxHeadXRot());
         if (this.dolphin.distanceToSqr(this.player) < 6.25) {
            this.dolphin.getNavigation().stop();
         } else {
            this.dolphin.getNavigation().moveTo(this.player, this.speedModifier);
         }

         if (this.player.isSwimming() && this.player.level().random.nextInt(6) == 0) {
            this.player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100), this.dolphin);
         }
      }
   }

   class PlayWithItemsGoal extends Goal {
      private int cooldown;

      @Override
      public boolean canUse() {
         if (this.cooldown > Dolphin.this.tickCount) {
            return false;
         } else {
            List<ItemEntity> $$0 = Dolphin.this.level()
               .getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Dolphin.ALLOWED_ITEMS);
            return !$$0.isEmpty() || !Dolphin.this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).isEmpty();
         }
      }

      @Override
      public void start() {
         List<ItemEntity> $$0 = Dolphin.this.level()
            .getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Dolphin.ALLOWED_ITEMS);
         if (!$$0.isEmpty()) {
            Dolphin.this.getNavigation().moveTo($$0.get(0), 1.2F);
            Dolphin.this.playSound(SoundEvents.DOLPHIN_PLAY, 1.0F, 1.0F);
         }

         this.cooldown = 0;
      }

      @Override
      public void stop() {
         ItemStack $$0 = Dolphin.this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
         if (!$$0.isEmpty()) {
            this.drop($$0);
            Dolphin.this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.cooldown = Dolphin.this.tickCount + Dolphin.this.random.nextInt(100);
         }
      }

      @Override
      public void tick() {
         List<ItemEntity> $$0 = Dolphin.this.level()
            .getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Dolphin.ALLOWED_ITEMS);
         ItemStack $$1 = Dolphin.this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
         if (!$$1.isEmpty()) {
            this.drop($$1);
            Dolphin.this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
         } else if (!$$0.isEmpty()) {
            Dolphin.this.getNavigation().moveTo($$0.get(0), 1.2F);
         }
      }

      private void drop(ItemStack $$0) {
         if (!$$0.isEmpty()) {
            double $$1 = Dolphin.this.getEyeY() - 0.3F;
            ItemEntity $$2 = new ItemEntity(Dolphin.this.level(), Dolphin.this.getX(), $$1, Dolphin.this.getZ(), $$0);
            $$2.setPickUpDelay(40);
            $$2.setThrower(Dolphin.this);
            float $$3 = 0.3F;
            float $$4 = Dolphin.this.random.nextFloat() * (float) (Math.PI * 2);
            float $$5 = 0.02F * Dolphin.this.random.nextFloat();
            $$2.setDeltaMovement(
               0.3F * -Mth.sin(Dolphin.this.getYRot() * (float) (Math.PI / 180.0)) * Mth.cos(Dolphin.this.getXRot() * (float) (Math.PI / 180.0))
                  + Mth.cos($$4) * $$5,
               0.3F * Mth.sin(Dolphin.this.getXRot() * (float) (Math.PI / 180.0)) * 1.5F,
               0.3F * Mth.cos(Dolphin.this.getYRot() * (float) (Math.PI / 180.0)) * Mth.cos(Dolphin.this.getXRot() * (float) (Math.PI / 180.0))
                  + Mth.sin($$4) * $$5
            );
            Dolphin.this.level().addFreshEntity($$2);
         }
      }
   }
}
