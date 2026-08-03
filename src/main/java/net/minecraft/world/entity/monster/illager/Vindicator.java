package net.minecraft.world.entity.monster.illager;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class Vindicator extends AbstractIllager {
   private static final String TAG_JOHNNY = "Johnny";
   static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = $$0 -> $$0 == Difficulty.NORMAL || $$0 == Difficulty.HARD;
   private static final boolean DEFAULT_JOHNNY = false;
   boolean isJohnny = false;

   public Vindicator(net.minecraft.world.entity.EntityType<? extends Vindicator> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Creaking.class, 8.0F, 1.0, 1.2));
      this.goalSelector.addGoal(2, new Vindicator.VindicatorBreakDoorGoal(this));
      this.goalSelector.addGoal(3, new AbstractIllager.RaiderOpenDoorGoal(this));
      this.goalSelector.addGoal(4, new Raider.HoldGroundAttackGoal(this, 10.0F));
      this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, false));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
      this.targetSelector.addGoal(4, new Vindicator.VindicatorJohnnyAttackGoal(this));
      this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, net.minecraft.world.entity.Mob.class, 8.0F));
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this)) {
         boolean $$1 = $$0.isRaided(this.blockPosition());
         this.getNavigation().setCanOpenDoors($$1);
      }

      super.customServerAiStep($$0);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.35F)
         .add(Attributes.FOLLOW_RANGE, 12.0)
         .add(Attributes.MAX_HEALTH, 24.0)
         .add(Attributes.ATTACK_DAMAGE, 5.0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      if (this.isJohnny) {
         $$0.putBoolean("Johnny", true);
      }
   }

   @Override
   public AbstractIllager.IllagerArmPose getArmPose() {
      if (this.isAggressive()) {
         return AbstractIllager.IllagerArmPose.ATTACKING;
      } else {
         return this.isCelebrating() ? AbstractIllager.IllagerArmPose.CELEBRATING : AbstractIllager.IllagerArmPose.CROSSED;
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.isJohnny = $$0.getBooleanOr("Johnny", false);
   }

   @Override
   public SoundEvent getCelebrateSound() {
      return SoundEvents.VINDICATOR_CELEBRATE;
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
      this.getNavigation().setCanOpenDoors(true);
      RandomSource $$5 = $$0.getRandom();
      this.populateDefaultEquipmentSlots($$5, $$1);
      this.populateDefaultEquipmentEnchantments($$0, $$5, $$1);
      return $$4;
   }

   @Override
   protected void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
      if (this.getCurrentRaid() == null) {
         this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
      }
   }

   @Override
   public void setCustomName(@Nullable Component $$0) {
      super.setCustomName($$0);
      if (!this.isJohnny && $$0 != null && $$0.getString().equals("Johnny")) {
         this.isJohnny = true;
      }
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.VINDICATOR_AMBIENT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.VINDICATOR_DEATH;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.VINDICATOR_HURT;
   }

   @Override
   public void applyRaidBuffs(ServerLevel $$0, int $$1, boolean $$2) {
      ItemStack $$3 = new ItemStack(Items.IRON_AXE);
      Raid $$4 = this.getCurrentRaid();
      boolean $$5 = this.random.nextFloat() <= $$4.getEnchantOdds();
      if ($$5) {
         ResourceKey<EnchantmentProvider> $$6 = $$1 > $$4.getNumGroups(Difficulty.NORMAL)
            ? VanillaEnchantmentProviders.RAID_VINDICATOR_POST_WAVE_5
            : VanillaEnchantmentProviders.RAID_VINDICATOR;
         EnchantmentHelper.enchantItemFromProvider($$3, $$0.registryAccess(), $$6, $$0.getCurrentDifficultyAt(this.blockPosition()), this.random);
      }

      this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, $$3);
   }

   static class VindicatorBreakDoorGoal extends BreakDoorGoal {
      public VindicatorBreakDoorGoal(net.minecraft.world.entity.Mob $$0) {
         super($$0, 6, Vindicator.DOOR_BREAKING_PREDICATE);
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      @Override
      public boolean canContinueToUse() {
         Vindicator $$0 = (Vindicator)this.mob;
         return $$0.hasActiveRaid() && super.canContinueToUse();
      }

      @Override
      public boolean canUse() {
         Vindicator $$0 = (Vindicator)this.mob;
         return $$0.hasActiveRaid() && $$0.random.nextInt(reducedTickDelay(10)) == 0 && super.canUse();
      }

      @Override
      public void start() {
         super.start();
         this.mob.setNoActionTime(0);
      }
   }

   static class VindicatorJohnnyAttackGoal extends NearestAttackableTargetGoal<net.minecraft.world.entity.LivingEntity> {
      public VindicatorJohnnyAttackGoal(Vindicator $$0) {
         super($$0, net.minecraft.world.entity.LivingEntity.class, 0, true, true, ($$0x, $$1) -> $$0x.attackable());
      }

      @Override
      public boolean canUse() {
         return ((Vindicator)this.mob).isJohnny && super.canUse();
      }

      @Override
      public void start() {
         super.start();
         this.mob.setNoActionTime(0);
      }
   }
}
