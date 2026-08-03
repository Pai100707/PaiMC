package net.minecraft.world.entity.monster.skeleton;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

public class WitherSkeleton extends AbstractSkeleton {
   public WitherSkeleton(net.minecraft.world.entity.EntityType<? extends WitherSkeleton> $$0, Level $$1) {
      super($$0, $$1);
      this.setPathfindingMalus(PathType.LAVA, 8.0F);
   }

   @Override
   protected void registerGoals() {
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractPiglin.class, true));
      super.registerGoals();
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.WITHER_SKELETON_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.WITHER_SKELETON_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.WITHER_SKELETON_DEATH;
   }

   @Override
   SoundEvent getStepSound() {
      return SoundEvents.WITHER_SKELETON_STEP;
   }

   @Override
   public TagKey<Item> getPreferredWeaponType() {
      return null;
   }

   @Override
   public boolean canHoldItem(ItemStack $$0) {
      return !$$0.is(ItemTags.WITHER_SKELETON_DISLIKED_WEAPONS) && super.canHoldItem($$0);
   }

   @Override
   protected void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
      this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
   }

   @Override
   protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor $$0, RandomSource $$1, DifficultyInstance $$2) {
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      net.minecraft.world.entity.SpawnGroupData $$4 = super.finalizeSpawn($$0, $$1, $$2, $$3);
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0);
      this.reassessWeaponGoal();
      return $$4;
   }

   @Override
   public boolean doHurtTarget(ServerLevel $$0, net.minecraft.world.entity.Entity $$1) {
      if (!super.doHurtTarget($$0, $$1)) {
         return false;
      } else {
         if ($$1 instanceof net.minecraft.world.entity.LivingEntity) {
            ((net.minecraft.world.entity.LivingEntity)$$1).addEffect(new MobEffectInstance(MobEffects.WITHER, 200), this);
         }

         return true;
      }
   }

   @Override
   protected AbstractArrow getArrow(ItemStack $$0, float $$1, @Nullable ItemStack $$2) {
      AbstractArrow $$3 = super.getArrow($$0, $$1, $$2);
      $$3.igniteForSeconds(100.0F);
      return $$3;
   }

   @Override
   public boolean canBeAffected(MobEffectInstance $$0) {
      return $$0.is(MobEffects.WITHER) ? false : super.canBeAffected($$0);
   }
}
