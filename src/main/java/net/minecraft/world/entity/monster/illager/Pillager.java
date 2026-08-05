package net.minecraft.world.entity.monster.illager;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Pillager extends AbstractIllager implements CrossbowAttackMob, InventoryCarrier {
   private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Pillager.class, EntityDataSerializers.BOOLEAN);
   private static final int INVENTORY_SIZE = 5;
   private static final int SLOT_OFFSET = 300;
   private final SimpleContainer inventory = new SimpleContainer(5);

   public Pillager(net.minecraft.world.entity.EntityType<? extends Pillager> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Creaking.class, 8.0F, 1.0, 1.2));
      this.goalSelector.addGoal(2, new Raider.HoldGroundAttackGoal(this, 10.0F));
      this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal<>(this, 1.0, 8.0F));
      this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, net.minecraft.world.entity.Mob.class, 15.0F));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.35F)
         .add(Attributes.MAX_HEALTH, 24.0)
         .add(Attributes.ATTACK_DAMAGE, 5.0)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(IS_CHARGING_CROSSBOW, false);
   }

   @Override
   public boolean canUseNonMeleeWeapon(ItemStack $$0) {
      return $$0.getItem() == Items.CROSSBOW;
   }

   public boolean isChargingCrossbow() {
      return (Boolean)this.entityData.get(IS_CHARGING_CROSSBOW);
   }

   @Override
   public void setChargingCrossbow(boolean $$0) {
      this.entityData.set(IS_CHARGING_CROSSBOW, $$0);
   }

   @Override
   public void onCrossbowAttackPerformed() {
      this.noActionTime = 0;
   }

   @Override
   public TagKey<Item> getPreferredWeaponType() {
      return ItemTags.PILLAGER_PREFERRED_WEAPONS;
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      this.writeInventoryToTag($$0);
   }

   @Override
   public AbstractIllager.IllagerArmPose getArmPose() {
      if (this.isChargingCrossbow()) {
         return AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
      } else if (this.isHolding(Items.CROSSBOW)) {
         return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
      } else {
         return this.isAggressive() ? AbstractIllager.IllagerArmPose.ATTACKING : AbstractIllager.IllagerArmPose.NEUTRAL;
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.readInventoryFromTag($$0);
      this.setCanPickUpLoot(true);
   }

   @Override
   public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
      return 0.0F;
   }

   @Override
   public int getMaxSpawnClusterSize() {
      return 1;
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      RandomSource $$4 = $$0.getRandom();
      this.populateDefaultEquipmentSlots($$4, $$1);
      this.populateDefaultEquipmentEnchantments($$0, $$4, $$1);
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   @Override
   protected void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
      this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
   }

   @Override
   protected void enchantSpawnedWeapon(ServerLevelAccessor $$0, RandomSource $$1, DifficultyInstance $$2) {
      super.enchantSpawnedWeapon($$0, $$1, $$2);
      if ($$1.nextInt(300) == 0) {
         ItemStack $$3 = this.getMainHandItem();
         if ($$3.is(Items.CROSSBOW)) {
            EnchantmentHelper.enchantItemFromProvider($$3, $$0.registryAccess(), VanillaEnchantmentProviders.PILLAGER_SPAWN_CROSSBOW, $$2, $$1);
         }
      }
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.PILLAGER_AMBIENT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.PILLAGER_DEATH;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.PILLAGER_HURT;
   }

   @Override
   public void performRangedAttack(net.minecraft.world.entity.LivingEntity $$0, float $$1) {
      this.performCrossbowAttack(this, 1.6F);
   }

   @Override
   public SimpleContainer getInventory() {
      return this.inventory;
   }

   @Override
   protected void pickUpItem(ServerLevel $$0, ItemEntity $$1) {
      ItemStack $$2 = $$1.getItem();
      if ($$2.getItem() instanceof BannerItem) {
         super.pickUpItem($$0, $$1);
      } else if (this.wantsItem($$2)) {
         this.onItemPickup($$1);
         ItemStack $$3 = this.inventory.addItem($$2);
         if ($$3.isEmpty()) {
            $$1.discard();
         } else {
            $$2.setCount($$3.getCount());
         }
      }
   }

   private boolean wantsItem(ItemStack $$0) {
      return this.hasActiveRaid() && $$0.is(Items.WHITE_BANNER);
   }

   
   @Override
   public net.minecraft.world.entity.SlotAccess getSlot(int $$0) {
      int $$1 = $$0 - 300;
      return $$1 >= 0 && $$1 < this.inventory.getContainerSize() ? this.inventory.getSlot($$1) : super.getSlot($$0);
   }

   @Override
   public void applyRaidBuffs(ServerLevel $$0, int $$1, boolean $$2) {
      Raid $$3 = this.getCurrentRaid();
      boolean $$4 = this.random.nextFloat() <= $$3.getEnchantOdds();
      if ($$4) {
         ItemStack $$5 = new ItemStack(Items.CROSSBOW);
         ResourceKey<EnchantmentProvider> $$6;
         if ($$1 > $$3.getNumGroups(Difficulty.NORMAL)) {
            $$6 = VanillaEnchantmentProviders.RAID_PILLAGER_POST_WAVE_5;
         } else if ($$1 > $$3.getNumGroups(Difficulty.EASY)) {
            $$6 = VanillaEnchantmentProviders.RAID_PILLAGER_POST_WAVE_3;
         } else {
            $$6 = null;
         }

         if ($$6 != null) {
            EnchantmentHelper.enchantItemFromProvider($$5, $$0.registryAccess(), $$6, $$0.getCurrentDifficultyAt(this.blockPosition()), this.getRandom());
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, $$5);
         }
      }
   }

   @Override
   public SoundEvent getCelebrateSound() {
      return SoundEvents.PILLAGER_CELEBRATE;
   }
}
