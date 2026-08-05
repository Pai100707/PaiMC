package net.minecraft.world.entity.npc.villager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.util.SpawnUtil.Strategy;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.GolemSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class Villager extends AbstractVillager implements net.minecraft.world.entity.ReputationEventHandler, VillagerDataHolder {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.VILLAGER_DATA);
   public static final int BREEDING_FOOD_THRESHOLD = 12;
   public static final Map<Item, Integer> FOOD_POINTS = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
   private static final int TRADES_PER_LEVEL = 2;
   private static final int MAX_GOSSIP_TOPICS = 10;
   private static final int GOSSIP_COOLDOWN = 1200;
   private static final int GOSSIP_DECAY_INTERVAL = 24000;
   private static final int HOW_FAR_AWAY_TO_TALK_TO_OTHER_VILLAGERS_ABOUT_GOLEMS = 10;
   private static final int HOW_MANY_VILLAGERS_NEED_TO_AGREE_TO_SPAWN_A_GOLEM = 5;
   private static final long TIME_SINCE_SLEEPING_FOR_GOLEM_SPAWNING = 24000L;
   @VisibleForTesting
   public static final float SPEED_MODIFIER = 0.5F;
   private static final int DEFAULT_XP = 0;
   private static final byte DEFAULT_FOOD_LEVEL = 0;
   private static final int DEFAULT_LAST_RESTOCK = 0;
   private static final int DEFAULT_LAST_GOSSIP_DECAY = 0;
   private static final int DEFAULT_RESTOCKS_TODAY = 0;
   private static final boolean DEFAULT_ASSIGN_PROFESSION_WHEN_SPAWNED = false;
   private int updateMerchantTimer;
   private boolean increaseProfessionLevelOnUpdate;
   
   private Player lastTradedPlayer;
   private boolean chasing;
   private int foodLevel = 0;
   private final GossipContainer gossips = new GossipContainer();
   private long lastGossipTime;
   private long lastGossipDecayTime = 0L;
   private int villagerXp = 0;
   private long lastRestockGameTime = 0L;
   private int numberOfRestocksToday = 0;
   private long lastRestockCheckDay;
   private boolean assignProfessionWhenSpawned = false;
   private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
      MemoryModuleType.HOME,
      MemoryModuleType.JOB_SITE,
      MemoryModuleType.POTENTIAL_JOB_SITE,
      MemoryModuleType.MEETING_POINT,
      MemoryModuleType.NEAREST_LIVING_ENTITIES,
      MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
      MemoryModuleType.VISIBLE_VILLAGER_BABIES,
      MemoryModuleType.NEAREST_PLAYERS,
      MemoryModuleType.NEAREST_VISIBLE_PLAYER,
      MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
      MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
      MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
      new MemoryModuleType[]{
         MemoryModuleType.WALK_TARGET,
         MemoryModuleType.LOOK_TARGET,
         MemoryModuleType.INTERACTION_TARGET,
         MemoryModuleType.BREED_TARGET,
         MemoryModuleType.PATH,
         MemoryModuleType.DOORS_TO_CLOSE,
         MemoryModuleType.NEAREST_BED,
         MemoryModuleType.HURT_BY,
         MemoryModuleType.HURT_BY_ENTITY,
         MemoryModuleType.NEAREST_HOSTILE,
         MemoryModuleType.SECONDARY_JOB_SITE,
         MemoryModuleType.HIDING_PLACE,
         MemoryModuleType.HEARD_BELL_TIME,
         MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
         MemoryModuleType.LAST_SLEPT,
         MemoryModuleType.LAST_WOKEN,
         MemoryModuleType.LAST_WORKED_AT_POI,
         MemoryModuleType.GOLEM_DETECTED_RECENTLY
      }
   );
   private static final ImmutableList<SensorType<? extends Sensor<? super Villager>>> SENSOR_TYPES = ImmutableList.of(
      SensorType.NEAREST_LIVING_ENTITIES,
      SensorType.NEAREST_PLAYERS,
      SensorType.NEAREST_ITEMS,
      SensorType.NEAREST_BED,
      SensorType.HURT_BY,
      SensorType.VILLAGER_HOSTILES,
      SensorType.VILLAGER_BABIES,
      SensorType.SECONDARY_POIS,
      SensorType.GOLEM_DETECTED
   );
   public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<Villager, Holder<PoiType>>> POI_MEMORIES = ImmutableMap.of(
      MemoryModuleType.HOME,
      (BiPredicate<Villager, Holder>)($$0, $$1) -> $$1.is(PoiTypes.HOME),
      MemoryModuleType.JOB_SITE,
      (BiPredicate<Villager, Holder>)($$0, $$1) -> ((VillagerProfession)$$0.getVillagerData().profession().value()).heldJobSite().test($$1),
      MemoryModuleType.POTENTIAL_JOB_SITE,
      (BiPredicate<Villager, Holder>)($$0, $$1) -> VillagerProfession.ALL_ACQUIRABLE_JOBS.test($$1),
      MemoryModuleType.MEETING_POINT,
      (BiPredicate<Villager, Holder>)($$0, $$1) -> $$1.is(PoiTypes.MEETING)
   );

   public Villager(net.minecraft.world.entity.EntityType<? extends Villager> $$0, Level $$1) {
      this($$0, $$1, VillagerType.PLAINS);
   }

   public Villager(net.minecraft.world.entity.EntityType<? extends Villager> $$0, Level $$1, ResourceKey<VillagerType> $$2) {
      this($$0, $$1, $$1.registryAccess().getOrThrow($$2));
   }

   public Villager(net.minecraft.world.entity.EntityType<? extends Villager> $$0, Level $$1, Holder<VillagerType> $$2) {
      super($$0, $$1);
      this.getNavigation().setCanOpenDoors(true);
      this.getNavigation().setCanFloat(true);
      this.getNavigation().setRequiredPathLength(48.0F);
      this.setCanPickUpLoot(true);
      this.setVillagerData(this.getVillagerData().withType($$2).withProfession($$1.registryAccess(), VillagerProfession.NONE));
   }

   @Override
   public Brain<Villager> getBrain() {
      return (Brain<Villager>)super.getBrain();
   }

   @Override
   protected Brain.Provider<Villager> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   @Override
   protected Brain<?> makeBrain(Dynamic<?> $$0) {
      Brain<Villager> $$1 = this.brainProvider().makeBrain($$0);
      this.registerBrainGoals($$1);
      return $$1;
   }

   public void refreshBrain(ServerLevel $$0) {
      Brain<Villager> $$1 = this.getBrain();
      $$1.stopAll($$0, this);
      this.brain = $$1.copyWithoutBehaviors();
      this.registerBrainGoals(this.getBrain());
   }

   private void registerBrainGoals(Brain<Villager> $$0) {
      Holder<VillagerProfession> $$1 = this.getVillagerData().profession();
      if (this.isBaby()) {
         $$0.setSchedule(EnvironmentAttributes.BABY_VILLAGER_ACTIVITY);
         $$0.addActivity(Activity.PLAY, VillagerGoalPackages.getPlayPackage(0.5F));
      } else {
         $$0.setSchedule(EnvironmentAttributes.VILLAGER_ACTIVITY);
         $$0.addActivityWithConditions(
            Activity.WORK, VillagerGoalPackages.getWorkPackage($$1, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT))
         );
      }

      $$0.addActivity(Activity.CORE, VillagerGoalPackages.getCorePackage($$1, 0.5F));
      $$0.addActivityWithConditions(
         Activity.MEET, VillagerGoalPackages.getMeetPackage($$1, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT))
      );
      $$0.addActivity(Activity.REST, VillagerGoalPackages.getRestPackage($$1, 0.5F));
      $$0.addActivity(Activity.IDLE, VillagerGoalPackages.getIdlePackage($$1, 0.5F));
      $$0.addActivity(Activity.PANIC, VillagerGoalPackages.getPanicPackage($$1, 0.5F));
      $$0.addActivity(Activity.PRE_RAID, VillagerGoalPackages.getPreRaidPackage($$1, 0.5F));
      $$0.addActivity(Activity.RAID, VillagerGoalPackages.getRaidPackage($$1, 0.5F));
      $$0.addActivity(Activity.HIDE, VillagerGoalPackages.getHidePackage($$1, 0.5F));
      $$0.setCoreActivities(ImmutableSet.of(Activity.CORE));
      $$0.setDefaultActivity(Activity.IDLE);
      $$0.setActiveActivityIfPossible(Activity.IDLE);
      $$0.updateActivityFromSchedule(this.level().environmentAttributes(), this.level().getGameTime(), this.position());
   }

   @Override
   protected void ageBoundaryReached() {
      super.ageBoundaryReached();
      if (this.level() instanceof ServerLevel) {
         this.refreshBrain((ServerLevel)this.level());
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5);
   }

   public boolean assignProfessionWhenSpawned() {
      return this.assignProfessionWhenSpawned;
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      ProfilerFiller $$1 = Profiler.get();
      $$1.push("villagerBrain");
      this.getBrain().tick($$0, this);
      $$1.pop();
      if (this.assignProfessionWhenSpawned) {
         this.assignProfessionWhenSpawned = false;
      }

      if (!this.isTrading() && this.updateMerchantTimer > 0) {
         this.updateMerchantTimer--;
         if (this.updateMerchantTimer <= 0) {
            if (this.increaseProfessionLevelOnUpdate) {
               this.increaseMerchantCareer($$0);
               this.increaseProfessionLevelOnUpdate = false;
            }

            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
         }
      }

      if (this.lastTradedPlayer != null) {
         $$0.onReputationEvent(ReputationEventType.TRADE, this.lastTradedPlayer, this);
         $$0.broadcastEntityEvent(this, (byte)14);
         this.lastTradedPlayer = null;
      }

      if (!this.isNoAi() && this.random.nextInt(100) == 0) {
         Raid $$2 = $$0.getRaidAt(this.blockPosition());
         if ($$2 != null && $$2.isActive() && !$$2.isOver()) {
            $$0.broadcastEntityEvent(this, (byte)42);
         }
      }

      if (this.getVillagerData().profession().is(VillagerProfession.NONE) && this.isTrading()) {
         this.stopTrading();
      }

      super.customServerAiStep($$0);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.getUnhappyCounter() > 0) {
         this.setUnhappyCounter(this.getUnhappyCounter() - 1);
      }

      this.maybeDecayGossip();
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if ($$2.is(Items.VILLAGER_SPAWN_EGG) || !this.isAlive() || this.isTrading() || this.isSleeping()) {
         return super.mobInteract($$0, $$1);
      } else if (this.isBaby()) {
         this.setUnhappy();
         return InteractionResult.SUCCESS;
      } else {
         if (!this.level().isClientSide()) {
            boolean $$3 = this.getOffers().isEmpty();
            if ($$1 == InteractionHand.MAIN_HAND) {
               if ($$3) {
                  this.setUnhappy();
               }

               $$0.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if ($$3) {
               return InteractionResult.CONSUME;
            }

            this.startTrading($$0);
         }

         return InteractionResult.SUCCESS;
      }
   }

   private void setUnhappy() {
      this.setUnhappyCounter(40);
      if (!this.level().isClientSide()) {
         this.makeSound(SoundEvents.VILLAGER_NO);
      }
   }

   private void startTrading(Player $$0) {
      this.updateSpecialPrices($$0);
      this.setTradingPlayer($$0);
      this.openTradingScreen($$0, this.getDisplayName(), this.getVillagerData().level());
   }

   @Override
   public void setTradingPlayer(Player $$0) {
      boolean $$1 = this.getTradingPlayer() != null && $$0 == null;
      super.setTradingPlayer($$0);
      if ($$1) {
         this.stopTrading();
      }
   }

   @Override
   protected void stopTrading() {
      super.stopTrading();
      this.resetSpecialPrices();
   }

   private void resetSpecialPrices() {
      if (!this.level().isClientSide()) {
         for (MerchantOffer $$0 : this.getOffers()) {
            $$0.resetSpecialPriceDiff();
         }
      }
   }

   public boolean canRestock() {
      return true;
   }

   public void restock() {
      this.updateDemand();

      for (MerchantOffer $$0 : this.getOffers()) {
         $$0.resetUses();
      }

      this.resendOffersToTradingPlayer();
      this.lastRestockGameTime = this.level().getGameTime();
      this.numberOfRestocksToday++;
   }

   private void resendOffersToTradingPlayer() {
      MerchantOffers $$0 = this.getOffers();
      Player $$1 = this.getTradingPlayer();
      if ($$1 != null && !$$0.isEmpty()) {
         $$1.sendMerchantOffers(
            $$1.containerMenu.containerId, $$0, this.getVillagerData().level(), this.getVillagerXp(), this.showProgressBar(), this.canRestock()
         );
      }
   }

   private boolean needsToRestock() {
      for (MerchantOffer $$0 : this.getOffers()) {
         if ($$0.needsRestock()) {
            return true;
         }
      }

      return false;
   }

   private boolean allowedToRestock() {
      return this.numberOfRestocksToday == 0 || this.numberOfRestocksToday < 2 && this.level().getGameTime() > this.lastRestockGameTime + 2400L;
   }

   public boolean shouldRestock(ServerLevel $$0) {
      long $$1 = this.lastRestockGameTime + 12000L;
      long $$2 = this.level().getGameTime();
      boolean $$3 = $$2 > $$1;
      long $$4 = $$0.getDayCount();
      $$3 |= this.lastRestockCheckDay > 0L && $$4 > this.lastRestockCheckDay;
      this.lastRestockCheckDay = $$4;
      if ($$3) {
         this.lastRestockGameTime = $$2;
         this.resetNumberOfRestocks();
      }

      return this.allowedToRestock() && this.needsToRestock();
   }

   private void catchUpDemand() {
      int $$0 = 2 - this.numberOfRestocksToday;
      if ($$0 > 0) {
         for (MerchantOffer $$1 : this.getOffers()) {
            $$1.resetUses();
         }
      }

      for (int $$2 = 0; $$2 < $$0; $$2++) {
         this.updateDemand();
      }

      this.resendOffersToTradingPlayer();
   }

   private void updateDemand() {
      for (MerchantOffer $$0 : this.getOffers()) {
         $$0.updateDemand();
      }
   }

   private void updateSpecialPrices(Player $$0) {
      int $$1 = this.getPlayerReputation($$0);
      if ($$1 != 0) {
         for (MerchantOffer $$2 : this.getOffers()) {
            $$2.addToSpecialPriceDiff(-Mth.floor($$1 * $$2.getPriceMultiplier()));
         }
      }

      if ($$0.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) {
         MobEffectInstance $$3 = $$0.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
         int $$4 = $$3.getAmplifier();

         for (MerchantOffer $$5 : this.getOffers()) {
            double $$6 = 0.3 + 0.0625 * $$4;
            int $$7 = (int)Math.floor($$6 * $$5.getBaseCostA().getCount());
            $$5.addToSpecialPriceDiff(-Math.max($$7, 1));
         }
      }
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_VILLAGER_DATA, createDefaultVillagerData());
   }

   public static VillagerData createDefaultVillagerData() {
      return new VillagerData(
         BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS), BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE), 1
      );
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("VillagerData", VillagerData.CODEC, this.getVillagerData());
      $$0.putByte("FoodLevel", (byte)this.foodLevel);
      $$0.store("Gossips", GossipContainer.CODEC, this.gossips);
      $$0.putInt("Xp", this.villagerXp);
      $$0.putLong("LastRestock", this.lastRestockGameTime);
      $$0.putLong("LastGossipDecay", this.lastGossipDecayTime);
      $$0.putInt("RestocksToday", this.numberOfRestocksToday);
      if (this.assignProfessionWhenSpawned) {
         $$0.putBoolean("AssignProfessionWhenSpawned", true);
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.entityData.set(DATA_VILLAGER_DATA, $$0.read("VillagerData", VillagerData.CODEC).orElseGet(Villager::createDefaultVillagerData));
      this.foodLevel = $$0.getByteOr("FoodLevel", (byte)0);
      this.gossips.clear();
      $$0.read("Gossips", GossipContainer.CODEC).ifPresent(this.gossips::putAll);
      this.villagerXp = $$0.getIntOr("Xp", 0);
      this.lastRestockGameTime = $$0.getLongOr("LastRestock", 0L);
      this.lastGossipDecayTime = $$0.getLongOr("LastGossipDecay", 0L);
      if (this.level() instanceof ServerLevel) {
         this.refreshBrain((ServerLevel)this.level());
      }

      this.numberOfRestocksToday = $$0.getIntOr("RestocksToday", 0);
      this.assignProfessionWhenSpawned = $$0.getBooleanOr("AssignProfessionWhenSpawned", false);
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return false;
   }

   
   @Override
   protected SoundEvent getAmbientSound() {
      if (this.isSleeping()) {
         return null;
      } else {
         return this.isTrading() ? SoundEvents.VILLAGER_TRADE : SoundEvents.VILLAGER_AMBIENT;
      }
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.VILLAGER_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.VILLAGER_DEATH;
   }

   public void playWorkSound() {
      this.makeSound(((VillagerProfession)this.getVillagerData().profession().value()).workSound());
   }

   @Override
   public void setVillagerData(VillagerData $$0) {
      VillagerData $$1 = this.getVillagerData();
      if (!$$1.profession().equals($$0.profession())) {
         this.offers = null;
      }

      this.entityData.set(DATA_VILLAGER_DATA, $$0);
   }

   @Override
   public VillagerData getVillagerData() {
      return (VillagerData)this.entityData.get(DATA_VILLAGER_DATA);
   }

   @Override
   protected void rewardTradeXp(MerchantOffer $$0) {
      int $$1 = 3 + this.random.nextInt(4);
      this.villagerXp = this.villagerXp + $$0.getXp();
      this.lastTradedPlayer = this.getTradingPlayer();
      if (this.shouldIncreaseLevel()) {
         this.updateMerchantTimer = 40;
         this.increaseProfessionLevelOnUpdate = true;
         $$1 += 5;
      }

      if ($$0.shouldRewardExp()) {
         this.level().addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), $$1));
      }
   }

   @Override
   public void setLastHurtByMob(net.minecraft.world.entity.LivingEntity $$0) {
      if ($$0 != null && this.level() instanceof ServerLevel) {
         ((ServerLevel)this.level()).onReputationEvent(ReputationEventType.VILLAGER_HURT, $$0, this);
         if (this.isAlive() && $$0 instanceof Player) {
            this.level().broadcastEntityEvent(this, (byte)13);
         }
      }

      super.setLastHurtByMob($$0);
   }

   @Override
   public void die(DamageSource $$0) {
      LOGGER.info("Villager {} died, message: '{}'", this, $$0.getLocalizedDeathMessage(this).getString());
      net.minecraft.world.entity.Entity $$1 = $$0.getEntity();
      if ($$1 != null) {
         this.tellWitnessesThatIWasMurdered($$1);
      }

      this.releaseAllPois();
      super.die($$0);
   }

   private void releaseAllPois() {
      this.releasePoi(MemoryModuleType.HOME);
      this.releasePoi(MemoryModuleType.JOB_SITE);
      this.releasePoi(MemoryModuleType.POTENTIAL_JOB_SITE);
      this.releasePoi(MemoryModuleType.MEETING_POINT);
   }

   private void tellWitnessesThatIWasMurdered(net.minecraft.world.entity.Entity $$0) {
      if (this.level() instanceof ServerLevel $$1) {
         Optional<NearestVisibleLivingEntities> $$3 = this.brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
         if (!$$3.isEmpty()) {
            $$3.get()
               .findAll(net.minecraft.world.entity.ReputationEventHandler.class::isInstance)
               .forEach($$2x -> $$1.onReputationEvent(ReputationEventType.VILLAGER_KILLED, $$0, (net.minecraft.world.entity.ReputationEventHandler)$$2x));
         }
      }
   }

   public void releasePoi(MemoryModuleType<GlobalPos> $$0) {
      if (this.level() instanceof ServerLevel) {
         MinecraftServer $$1 = ((ServerLevel)this.level()).getServer();
         this.brain.getMemory($$0).ifPresent($$2 -> {
            ServerLevel $$3 = $$1.getLevel($$2.dimension());
            if ($$3 != null) {
               PoiManager $$4 = $$3.getPoiManager();
               Optional<Holder<PoiType>> $$5 = $$4.getType($$2.pos());
               BiPredicate<Villager, Holder<PoiType>> $$6 = POI_MEMORIES.get($$0);
               if ($$5.isPresent() && $$6.test(this, $$5.get())) {
                  $$4.release($$2.pos());
                  $$3.debugSynchronizers().updatePoi($$2.pos());
               }
            }
         });
      }
   }

   @Override
   public boolean canBreed() {
      return this.foodLevel + this.countFoodPointsInInventory() >= 12 && !this.isSleeping() && this.getAge() == 0;
   }

   private boolean hungry() {
      return this.foodLevel < 12;
   }

   private void eatUntilFull() {
      if (this.hungry() && this.countFoodPointsInInventory() != 0) {
         for (int $$0 = 0; $$0 < this.getInventory().getContainerSize(); $$0++) {
            ItemStack $$1 = this.getInventory().getItem($$0);
            if (!$$1.isEmpty()) {
               Integer $$2 = FOOD_POINTS.get($$1.getItem());
               if ($$2 != null) {
                  int $$3 = $$1.getCount();

                  for (int $$4 = $$3; $$4 > 0; $$4--) {
                     this.foodLevel = this.foodLevel + $$2;
                     this.getInventory().removeItem($$0, 1);
                     if (!this.hungry()) {
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   public int getPlayerReputation(Player $$0) {
      return this.gossips.getReputation($$0.getUUID(), $$0x -> true);
   }

   private void digestFood(int $$0) {
      this.foodLevel -= $$0;
   }

   public void eatAndDigestFood() {
      this.eatUntilFull();
      this.digestFood(12);
   }

   public void setOffers(MerchantOffers $$0) {
      this.offers = $$0;
   }

   private boolean shouldIncreaseLevel() {
      int $$0 = this.getVillagerData().level();
      return VillagerData.canLevelUp($$0) && this.villagerXp >= VillagerData.getMaxXpPerLevel($$0);
   }

   private void increaseMerchantCareer(ServerLevel $$0) {
      this.setVillagerData(this.getVillagerData().withLevel(this.getVillagerData().level() + 1));
      this.updateTrades($$0);
   }

   @Override
   protected Component getTypeName() {
      return ((VillagerProfession)this.getVillagerData().profession().value()).name();
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 12) {
         this.addParticlesAroundSelf(ParticleTypes.HEART);
      } else if ($$0 == 13) {
         this.addParticlesAroundSelf(ParticleTypes.ANGRY_VILLAGER);
      } else if ($$0 == 14) {
         this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
      } else if ($$0 == 42) {
         this.addParticlesAroundSelf(ParticleTypes.SPLASH);
      } else {
         super.handleEntityEvent($$0);
      }
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      if ($$2 == net.minecraft.world.entity.EntitySpawnReason.BREEDING) {
         this.setVillagerData(this.getVillagerData().withProfession($$0.registryAccess(), VillagerProfession.NONE));
      }

      if ($$2 == net.minecraft.world.entity.EntitySpawnReason.COMMAND
         || $$2 == net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE
         || net.minecraft.world.entity.EntitySpawnReason.isSpawner($$2)
         || $$2 == net.minecraft.world.entity.EntitySpawnReason.DISPENSER) {
         this.setVillagerData(this.getVillagerData().withType($$0.registryAccess(), VillagerType.byBiome($$0.getBiome(this.blockPosition()))));
      }

      if ($$2 == net.minecraft.world.entity.EntitySpawnReason.STRUCTURE) {
         this.assignProfessionWhenSpawned = true;
      }

      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   
   public Villager getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      double $$2 = this.random.nextDouble();
      Holder<VillagerType> $$3;
      if ($$2 < 0.5) {
         $$3 = $$0.registryAccess().getOrThrow(VillagerType.byBiome($$0.getBiome(this.blockPosition())));
      } else if ($$2 < 0.75) {
         $$3 = this.getVillagerData().type();
      } else {
         $$3 = ((Villager)$$1).getVillagerData().type();
      }

      Villager $$6 = new Villager(net.minecraft.world.entity.EntityType.VILLAGER, $$0, $$3);
      $$6.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$6.blockPosition()), net.minecraft.world.entity.EntitySpawnReason.BREEDING, null);
      return $$6;
   }

   @Override
   public void thunderHit(ServerLevel $$0, net.minecraft.world.entity.LightningBolt $$1) {
      if ($$0.getDifficulty() != Difficulty.PEACEFUL) {
         LOGGER.info("Villager {} was struck by lightning {}.", this, $$1);
         Witch $$2 = this.convertTo(
            net.minecraft.world.entity.EntityType.WITCH, net.minecraft.world.entity.ConversionParams.single(this, false, false), $$1x -> {
               $$1x.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$1x.blockPosition()), net.minecraft.world.entity.EntitySpawnReason.CONVERSION, null);
               $$1x.setPersistenceRequired();
               this.releaseAllPois();
            }
         );
         if ($$2 == null) {
            super.thunderHit($$0, $$1);
         }
      } else {
         super.thunderHit($$0, $$1);
      }
   }

   @Override
   protected void pickUpItem(ServerLevel $$0, ItemEntity $$1) {
      InventoryCarrier.pickUpItem($$0, this, this, $$1);
   }

   @Override
   public boolean wantsToPickUp(ServerLevel $$0, ItemStack $$1) {
      Item $$2 = $$1.getItem();
      return ($$1.is(ItemTags.VILLAGER_PICKS_UP) || ((VillagerProfession)this.getVillagerData().profession().value()).requestedItems().contains($$2))
         && this.getInventory().canAddItem($$1);
   }

   public boolean hasExcessFood() {
      return this.countFoodPointsInInventory() >= 24;
   }

   public boolean wantsMoreFood() {
      return this.countFoodPointsInInventory() < 12;
   }

   private int countFoodPointsInInventory() {
      SimpleContainer $$0 = this.getInventory();
      return FOOD_POINTS.entrySet().stream().mapToInt($$1 -> $$0.countItem($$1.getKey()) * $$1.getValue()).sum();
   }

   public boolean hasFarmSeeds() {
      return this.getInventory().hasAnyMatching($$0 -> $$0.is(ItemTags.VILLAGER_PLANTABLE_SEEDS));
   }

   @Override
   protected void updateTrades(ServerLevel $$0) {
      VillagerData $$1 = this.getVillagerData();
      ResourceKey<VillagerProfession> $$2 = (ResourceKey<VillagerProfession>)$$1.profession().unwrapKey().orElse(null);
      if ($$2 != null) {
         Int2ObjectMap<VillagerTrades.ItemListing[]> $$4;
         if (this.level().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
            Int2ObjectMap<VillagerTrades.ItemListing[]> $$3 = VillagerTrades.EXPERIMENTAL_TRADES.get($$2);
            $$4 = $$3 != null ? $$3 : VillagerTrades.TRADES.get($$2);
         } else {
            $$4 = VillagerTrades.TRADES.get($$2);
         }

         if ($$4 != null && !$$4.isEmpty()) {
            VillagerTrades.ItemListing[] $$6 = (VillagerTrades.ItemListing[])$$4.get($$1.level());
            if ($$6 != null) {
               MerchantOffers $$7 = this.getOffers();
               this.addOffersFromItemListings($$0, $$7, $$6, 2);
               if (SharedConstants.DEBUG_UNLOCK_ALL_TRADES && $$1.level() < $$4.size()) {
                  this.increaseMerchantCareer($$0);
               }
            }
         }
      }
   }

   public void gossip(ServerLevel $$0, Villager $$1, long $$2) {
      if (($$2 < this.lastGossipTime || $$2 >= this.lastGossipTime + 1200L) && ($$2 < $$1.lastGossipTime || $$2 >= $$1.lastGossipTime + 1200L)) {
         this.gossips.transferFrom($$1.gossips, this.random, 10);
         this.lastGossipTime = $$2;
         $$1.lastGossipTime = $$2;
         this.spawnGolemIfNeeded($$0, $$2, 5);
      }
   }

   private void maybeDecayGossip() {
      long $$0 = this.level().getGameTime();
      if (this.lastGossipDecayTime == 0L) {
         this.lastGossipDecayTime = $$0;
      } else if ($$0 >= this.lastGossipDecayTime + 24000L) {
         this.gossips.decay();
         this.lastGossipDecayTime = $$0;
      }
   }

   public void spawnGolemIfNeeded(ServerLevel $$0, long $$1, int $$2) {
      if (this.wantsToSpawnGolem($$1)) {
         AABB $$3 = this.getBoundingBox().inflate(10.0, 10.0, 10.0);
         List<Villager> $$4 = $$0.getEntitiesOfClass(Villager.class, $$3);
         List<Villager> $$5 = $$4.stream().filter($$1x -> $$1x.wantsToSpawnGolem($$1)).limit(5L).toList();
         if ($$5.size() >= $$2) {
            if (!SpawnUtil.trySpawnMob(
                  net.minecraft.world.entity.EntityType.IRON_GOLEM,
                  net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED,
                  $$0,
                  this.blockPosition(),
                  10,
                  8,
                  6,
                  Strategy.LEGACY_IRON_GOLEM,
                  false
               )
               .isEmpty()) {
               $$4.forEach(GolemSensor::golemDetected);
            }
         }
      }
   }

   public boolean wantsToSpawnGolem(long $$0) {
      return !this.golemSpawnConditionsMet(this.level().getGameTime()) ? false : !this.brain.hasMemoryValue(MemoryModuleType.GOLEM_DETECTED_RECENTLY);
   }

   @Override
   public void onReputationEventFrom(ReputationEventType $$0, net.minecraft.world.entity.Entity $$1) {
      if ($$0 == ReputationEventType.ZOMBIE_VILLAGER_CURED) {
         this.gossips.add($$1.getUUID(), GossipType.MAJOR_POSITIVE, 20);
         this.gossips.add($$1.getUUID(), GossipType.MINOR_POSITIVE, 25);
      } else if ($$0 == ReputationEventType.TRADE) {
         this.gossips.add($$1.getUUID(), GossipType.TRADING, 2);
      } else if ($$0 == ReputationEventType.VILLAGER_HURT) {
         this.gossips.add($$1.getUUID(), GossipType.MINOR_NEGATIVE, 25);
      } else if ($$0 == ReputationEventType.VILLAGER_KILLED) {
         this.gossips.add($$1.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
      }
   }

   @Override
   public int getVillagerXp() {
      return this.villagerXp;
   }

   public void setVillagerXp(int $$0) {
      this.villagerXp = $$0;
   }

   private void resetNumberOfRestocks() {
      this.catchUpDemand();
      this.numberOfRestocksToday = 0;
   }

   public GossipContainer getGossips() {
      return this.gossips;
   }

   public void setGossips(GossipContainer $$0) {
      this.gossips.putAll($$0);
   }

   @Override
   public void startSleeping(BlockPos $$0) {
      super.startSleeping($$0);
      this.brain.setMemory(MemoryModuleType.LAST_SLEPT, this.level().getGameTime());
      this.brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
   }

   @Override
   public void stopSleeping() {
      super.stopSleeping();
      this.brain.setMemory(MemoryModuleType.LAST_WOKEN, this.level().getGameTime());
   }

   private boolean golemSpawnConditionsMet(long $$0) {
      Optional<Long> $$1 = this.brain.getMemory(MemoryModuleType.LAST_SLEPT);
      return $$1.filter($$1x -> $$0 - $$1x < 24000L).isPresent();
   }

   
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.VILLAGER_VARIANT ? castComponentValue((DataComponentType<T>)$$0, this.getVillagerData().type()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.VILLAGER_VARIANT);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.VILLAGER_VARIANT) {
         Holder<VillagerType> $$2 = castComponentValue(DataComponents.VILLAGER_VARIANT, $$1);
         this.setVillagerData(this.getVillagerData().withType($$2));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }
}
