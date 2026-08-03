package net.minecraft.world.entity.animal.fish;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFish extends WaterAnimal implements Bucketable {
   private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(AbstractFish.class, EntityDataSerializers.BOOLEAN);
   private static final boolean DEFAULT_FROM_BUCKET = false;

   public AbstractFish(net.minecraft.world.entity.EntityType<? extends AbstractFish> $$0, Level $$1) {
      super($$0, $$1);
      this.moveControl = new AbstractFish.FishMoveControl(this);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0);
   }

   @Override
   public boolean requiresCustomPersistence() {
      return super.requiresCustomPersistence() || this.fromBucket();
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return !this.fromBucket() && !this.hasCustomName();
   }

   @Override
   public int getMaxSpawnClusterSize() {
      return 8;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(FROM_BUCKET, false);
   }

   @Override
   public boolean fromBucket() {
      return (Boolean)this.entityData.get(FROM_BUCKET);
   }

   @Override
   public void setFromBucket(boolean $$0) {
      this.entityData.set(FROM_BUCKET, $$0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putBoolean("FromBucket", this.fromBucket());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setFromBucket($$0.getBooleanOr("FromBucket", false));
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new PanicGoal(this, 1.25));
      this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.6, 1.4, net.minecraft.world.entity.EntitySelector.NO_SPECTATORS));
      this.goalSelector.addGoal(4, new AbstractFish.FishSwimGoal(this));
   }

   @Override
   protected PathNavigation createNavigation(Level $$0) {
      return new WaterBoundPathNavigation(this, $$0);
   }

   @Override
   protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
      this.moveRelative(0.01F, $$0);
      this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
      if (this.getTarget() == null) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
      }
   }

   @Override
   public void aiStep() {
      if (!this.isInWater() && this.onGround() && this.verticalCollision) {
         this.setDeltaMovement(
            this.getDeltaMovement().add((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F, 0.4F, (this.random.nextFloat() * 2.0F - 1.0F) * 0.05F)
         );
         this.setOnGround(false);
         this.needsSync = true;
         this.makeSound(this.getFlopSound());
      }

      super.aiStep();
   }

   @Override
   protected InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      return Bucketable.bucketMobPickup($$0, $$1, this).orElse(super.mobInteract($$0, $$1));
   }

   @Override
   public void saveToBucketTag(ItemStack $$0) {
      Bucketable.saveDefaultDataToBucketTag(this, $$0);
   }

   @Override
   public void loadFromBucketTag(CompoundTag $$0) {
      Bucketable.loadDefaultDataFromBucketTag(this, $$0);
   }

   @Override
   public SoundEvent getPickupSound() {
      return SoundEvents.BUCKET_FILL_FISH;
   }

   protected boolean canRandomSwim() {
      return true;
   }

   protected abstract SoundEvent getFlopSound();

   @Override
   protected SoundEvent getSwimSound() {
      return SoundEvents.FISH_SWIM;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
   }

   static class FishMoveControl extends MoveControl {
      private final AbstractFish fish;

      FishMoveControl(AbstractFish $$0) {
         super($$0);
         this.fish = $$0;
      }

      @Override
      public void tick() {
         if (this.fish.isEyeInFluid(FluidTags.WATER)) {
            this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, 0.005, 0.0));
         }

         if (this.operation == MoveControl.Operation.MOVE_TO && !this.fish.getNavigation().isDone()) {
            float $$0 = (float)(this.speedModifier * this.fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
            this.fish.setSpeed(Mth.lerp(0.125F, this.fish.getSpeed(), $$0));
            double $$1 = this.wantedX - this.fish.getX();
            double $$2 = this.wantedY - this.fish.getY();
            double $$3 = this.wantedZ - this.fish.getZ();
            if ($$2 != 0.0) {
               double $$4 = Math.sqrt($$1 * $$1 + $$2 * $$2 + $$3 * $$3);
               this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, this.fish.getSpeed() * ($$2 / $$4) * 0.1, 0.0));
            }

            if ($$1 != 0.0 || $$3 != 0.0) {
               float $$5 = (float)(Mth.atan2($$3, $$1) * 180.0F / (float)Math.PI) - 90.0F;
               this.fish.setYRot(this.rotlerp(this.fish.getYRot(), $$5, 90.0F));
               this.fish.yBodyRot = this.fish.getYRot();
            }
         } else {
            this.fish.setSpeed(0.0F);
         }
      }
   }

   static class FishSwimGoal extends RandomSwimmingGoal {
      private final AbstractFish fish;

      public FishSwimGoal(AbstractFish $$0) {
         super($$0, 1.0, 40);
         this.fish = $$0;
      }

      @Override
      public boolean canUse() {
         return this.fish.canRandomSwim() && super.canUse();
      }
   }
}
