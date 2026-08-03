package net.minecraft.world.entity.animal.equine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SkeletonHorse extends AbstractHorse {
   private final SkeletonTrapGoal skeletonTrapGoal = new SkeletonTrapGoal(this);
   private static final int TRAP_MAX_LIFE = 18000;
   private static final boolean DEFAULT_IS_TRAP = false;
   private static final int DEFAULT_TRAP_TIME = 0;
   private static final net.minecraft.world.entity.EntityDimensions BABY_DIMENSIONS = net.minecraft.world.entity.EntityType.SKELETON_HORSE
      .getDimensions()
      .withAttachments(
         net.minecraft.world.entity.EntityAttachments.builder()
            .attach(
               net.minecraft.world.entity.EntityAttachment.PASSENGER, 0.0F, net.minecraft.world.entity.EntityType.SKELETON_HORSE.getHeight() - 0.03125F, 0.0F
            )
      )
      .scale(0.5F);
   private boolean isTrap = false;
   private int trapTime = 0;

   public SkeletonHorse(net.minecraft.world.entity.EntityType<? extends SkeletonHorse> $$0, Level $$1) {
      super($$0, $$1);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
   }

   public static boolean checkSkeletonHorseSpawnRules(
      net.minecraft.world.entity.EntityType<? extends Animal> $$0,
      LevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      return !net.minecraft.world.entity.EntitySpawnReason.isSpawner($$2)
         ? Animal.checkAnimalSpawnRules($$0, $$1, $$2, $$3, $$4)
         : net.minecraft.world.entity.EntitySpawnReason.ignoresLightRequirements($$2) || isBrightEnoughToSpawn($$1, $$3);
   }

   @Override
   protected void randomizeAttributes(RandomSource $$0) {
      this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(generateJumpStrength($$0::nextDouble));
   }

   @Override
   protected void addBehaviourGoals() {
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return this.isEyeInFluid(FluidTags.WATER) ? SoundEvents.SKELETON_HORSE_AMBIENT_WATER : SoundEvents.SKELETON_HORSE_AMBIENT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.SKELETON_HORSE_DEATH;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.SKELETON_HORSE_HURT;
   }

   @Override
   protected SoundEvent getSwimSound() {
      if (this.onGround()) {
         if (!this.isVehicle()) {
            return SoundEvents.SKELETON_HORSE_STEP_WATER;
         }

         this.gallopSoundCounter++;
         if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
            return SoundEvents.SKELETON_HORSE_GALLOP_WATER;
         }

         if (this.gallopSoundCounter <= 5) {
            return SoundEvents.SKELETON_HORSE_STEP_WATER;
         }
      }

      return SoundEvents.SKELETON_HORSE_SWIM;
   }

   @Override
   protected void playSwimSound(float $$0) {
      if (this.onGround()) {
         super.playSwimSound(0.3F);
      } else {
         super.playSwimSound(Math.min(0.1F, $$0 * 25.0F));
      }
   }

   @Override
   protected void playJumpSound() {
      if (this.isInWater()) {
         this.playSound(SoundEvents.SKELETON_HORSE_JUMP_WATER, 0.4F, 1.0F);
      } else {
         super.playJumpSound();
      }
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose $$0) {
      return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.isTrap() && this.trapTime++ >= 18000) {
         this.discard();
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putBoolean("SkeletonTrap", this.isTrap());
      $$0.putInt("SkeletonTrapTime", this.trapTime);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setTrap($$0.getBooleanOr("SkeletonTrap", false));
      this.trapTime = $$0.getIntOr("SkeletonTrapTime", 0);
   }

   @Override
   protected float getWaterSlowDown() {
      return 0.96F;
   }

   public boolean isTrap() {
      return this.isTrap;
   }

   public void setTrap(boolean $$0) {
      if ($$0 != this.isTrap) {
         this.isTrap = $$0;
         if ($$0) {
            this.goalSelector.addGoal(1, this.skeletonTrapGoal);
         } else {
            this.goalSelector.removeGoal(this.skeletonTrapGoal);
         }
      }
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.AgeableMob getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      return net.minecraft.world.entity.EntityType.SKELETON_HORSE.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      return (InteractionResult)(!this.isTamed() ? InteractionResult.PASS : super.mobInteract($$0, $$1));
   }

   @Override
   public boolean canUseSlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return true;
   }
}
