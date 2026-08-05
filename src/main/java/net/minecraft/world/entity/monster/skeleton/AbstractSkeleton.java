package net.minecraft.world.entity.monster.skeleton;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpecialDates;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;

public abstract class AbstractSkeleton extends Monster implements RangedAttackMob {
   private static final int HARD_ATTACK_INTERVAL = 20;
   private static final int NORMAL_ATTACK_INTERVAL = 40;
   protected static final int INCREASED_HARD_ATTACK_INTERVAL = 50;
   protected static final int INCREASED_NORMAL_ATTACK_INTERVAL = 70;
   private final RangedBowAttackGoal<AbstractSkeleton> bowGoal = new RangedBowAttackGoal<>(this, 1.0, 20, 15.0F);
   private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal(this, 1.2, false) {
      @Override
      public void stop() {
         super.stop();
         AbstractSkeleton.this.setAggressive(false);
      }

      @Override
      public void start() {
         super.start();
         AbstractSkeleton.this.setAggressive(true);
      }
   };

   protected AbstractSkeleton(net.minecraft.world.entity.EntityType<? extends AbstractSkeleton> $$0, Level $$1) {
      super($$0, $$1);
      this.reassessWeaponGoal();
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(2, new RestrictSunGoal(this));
      this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
      this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Wolf.class, 6.0F, 1.0, 1.2));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25);
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   abstract SoundEvent getStepSound();

   @Override
   public void rideTick() {
      super.rideTick();
      if (this.getControlledVehicle() instanceof net.minecraft.world.entity.PathfinderMob $$0) {
         this.yBodyRot = $$0.yBodyRot;
      }
   }

   @Override
   protected void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
      super.populateDefaultEquipmentSlots($$0, $$1);
      this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      $$3 = super.finalizeSpawn($$0, $$1, $$2, $$3);
      RandomSource $$4 = $$0.getRandom();
      this.populateDefaultEquipmentSlots($$4, $$1);
      this.populateDefaultEquipmentEnchantments($$0, $$4, $$1);
      this.reassessWeaponGoal();
      this.setCanPickUpLoot($$4.nextFloat() < 0.55F * $$1.getSpecialMultiplier());
      if (this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty() && SpecialDates.isHalloween() && $$4.nextFloat() < 0.25F) {
         this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack($$4.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
         this.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0.0F);
      }

      return $$3;
   }

   public void reassessWeaponGoal() {
      if (this.level() != null && !this.level().isClientSide()) {
         this.goalSelector.removeGoal(this.meleeGoal);
         this.goalSelector.removeGoal(this.bowGoal);
         ItemStack $$0 = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
         if ($$0.is(Items.BOW)) {
            int $$1 = this.getHardAttackInterval();
            if (this.level().getDifficulty() != Difficulty.HARD) {
               $$1 = this.getAttackInterval();
            }

            this.bowGoal.setMinAttackInterval($$1);
            this.goalSelector.addGoal(4, this.bowGoal);
         } else {
            this.goalSelector.addGoal(4, this.meleeGoal);
         }
      }
   }

   protected int getHardAttackInterval() {
      return 20;
   }

   protected int getAttackInterval() {
      return 40;
   }

   @Override
   public void performRangedAttack(net.minecraft.world.entity.LivingEntity $$0, float $$1) {
      ItemStack $$2 = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
      ItemStack $$3 = this.getProjectile($$2);
      AbstractArrow $$4 = this.getArrow($$3, $$1, $$2);
      double $$5 = $$0.getX() - this.getX();
      double $$6 = $$0.getY(0.3333333333333333) - $$4.getY();
      double $$7 = $$0.getZ() - this.getZ();
      double $$8 = Math.sqrt($$5 * $$5 + $$7 * $$7);
      if (this.level() instanceof ServerLevel $$9) {
         Projectile.spawnProjectileUsingShoot($$4, $$9, $$3, $$5, $$6 + $$8 * 0.2F, $$7, 1.6F, 14 - $$9.getDifficulty().getId() * 4);
      }

      this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   protected AbstractArrow getArrow(ItemStack $$0, float $$1, ItemStack $$2) {
      return ProjectileUtil.getMobArrow(this, $$0, $$1, $$2);
   }

   @Override
   public boolean canUseNonMeleeWeapon(ItemStack $$0) {
      return $$0.getItem() == Items.BOW;
   }

   @Override
   public TagKey<Item> getPreferredWeaponType() {
      return ItemTags.SKELETON_PREFERRED_WEAPONS;
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.reassessWeaponGoal();
   }

   @Override
   public void onEquipItem(net.minecraft.world.entity.EquipmentSlot $$0, ItemStack $$1, ItemStack $$2) {
      super.onEquipItem($$0, $$1, $$2);
      if (!this.level().isClientSide()) {
         this.reassessWeaponGoal();
      }
   }

   public boolean isShaking() {
      return this.isFullyFrozen();
   }

   @Override
   public boolean wantsToPickUp(ServerLevel $$0, ItemStack $$1) {
      return $$1.is(ItemTags.SPEARS) ? false : super.wantsToPickUp($$0, $$1);
   }
}
