package net.minecraft.world.entity.animal.camel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CamelHusk extends Camel {
   public CamelHusk(net.minecraft.world.entity.EntityType<? extends Camel> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return true;
   }

   @Override
   public boolean isMobControlled() {
      return this.getFirstPassenger() instanceof net.minecraft.world.entity.Mob;
   }

   @Override
   public InteractionResult interact(Player $$0, InteractionHand $$1) {
      this.setPersistenceRequired();
      return super.interact($$0, $$1);
   }

   @Override
   public boolean canBeLeashed() {
      return !this.isMobControlled();
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.CAMEL_HUSK_FOOD);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.CAMEL_HUSK_AMBIENT;
   }

   @Override
   public boolean canMate(Animal $$0) {
      return false;
   }

   
   @Override
   public Camel getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      return null;
   }

   @Override
   public boolean canFallInLove() {
      return false;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.CAMEL_HUSK_DEATH;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.CAMEL_HUSK_HURT;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      if ($$1.is(BlockTags.CAMEL_SAND_STEP_SOUND_BLOCKS)) {
         this.playSound(SoundEvents.CAMEL_HUSK_STEP_SAND, 0.4F, 1.0F);
      } else {
         this.playSound(SoundEvents.CAMEL_HUSK_STEP, 0.4F, 1.0F);
      }
   }

   @Override
   protected SoundEvent getDashingSound() {
      return SoundEvents.CAMEL_HUSK_DASH;
   }

   @Override
   protected SoundEvent getDashReadySound() {
      return SoundEvents.CAMEL_HUSK_DASH_READY;
   }

   @Override
   protected SoundEvent getEatingSound() {
      return SoundEvents.CAMEL_HUSK_EAT;
   }

   @Override
   protected SoundEvent getStandUpSound() {
      return SoundEvents.CAMEL_HUSK_STAND;
   }

   @Override
   protected SoundEvent getSitDownSound() {
      return SoundEvents.CAMEL_HUSK_SIT;
   }

   @Override
   protected Reference<SoundEvent> getSaddleSound() {
      return SoundEvents.CAMEL_HUSK_SADDLE;
   }

   @Override
   public float chargeSpeedModifier() {
      return 4.0F;
   }
}
