package net.minecraft.world.entity.animal.cow;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractCow extends Animal {
   private static final net.minecraft.world.entity.EntityDimensions BABY_DIMENSIONS = net.minecraft.world.entity.EntityType.COW
      .getDimensions()
      .scale(0.5F)
      .withEyeHeight(0.665F);

   public AbstractCow(net.minecraft.world.entity.EntityType<? extends AbstractCow> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, $$0 -> $$0.is(ItemTags.COW_FOOD), false));
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.COW_FOOD);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.COW_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.COW_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.COW_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
   }

   @Override
   protected float getSoundVolume() {
      return 0.4F;
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if ($$2.is(Items.BUCKET) && !this.isBaby()) {
         $$0.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
         ItemStack $$3 = ItemUtils.createFilledResult($$2, $$0, Items.MILK_BUCKET.getDefaultInstance());
         $$0.setItemInHand($$1, $$3);
         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract($$0, $$1);
      }
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose $$0) {
      return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
   }
}
