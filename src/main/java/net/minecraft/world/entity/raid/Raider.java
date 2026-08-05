package net.minecraft.world.entity.raid;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PathfindToRaidGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class Raider extends PatrollingMonster {
   protected static final EntityDataAccessor<Boolean> IS_CELEBRATING = SynchedEntityData.defineId(Raider.class, EntityDataSerializers.BOOLEAN);
   static final Predicate<ItemEntity> ALLOWED_ITEMS = $$0 -> !$$0.hasPickUpDelay()
      && $$0.isAlive()
      && ItemStack.matches($$0.getItem(), Raid.getOminousBannerInstance($$0.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
   private static final int DEFAULT_WAVE = 0;
   private static final boolean DEFAULT_CAN_JOIN_RAID = false;
   
   protected Raid raid;
   private int wave = 0;
   private boolean canJoinRaid = false;
   private int ticksOutsideRaid;

   protected Raider(net.minecraft.world.entity.EntityType<? extends Raider> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new Raider.ObtainRaidLeaderBannerGoal<>(this));
      this.goalSelector.addGoal(3, new PathfindToRaidGoal<>(this));
      this.goalSelector.addGoal(4, new Raider.RaiderMoveThroughVillageGoal(this, 1.05F, 1));
      this.goalSelector.addGoal(5, new Raider.RaiderCelebration(this));
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(IS_CELEBRATING, false);
   }

   public abstract void applyRaidBuffs(ServerLevel var1, int var2, boolean var3);

   public boolean canJoinRaid() {
      return this.canJoinRaid;
   }

   public void setCanJoinRaid(boolean $$0) {
      this.canJoinRaid = $$0;
   }

   @Override
   public void aiStep() {
      if (this.level() instanceof ServerLevel $$0 && this.isAlive()) {
         Raid $$1 = this.getCurrentRaid();
         if (this.canJoinRaid()) {
            if ($$1 == null) {
               if (this.level().getGameTime() % 20L == 0L) {
                  Raid $$2 = $$0.getRaidAt(this.blockPosition());
                  if ($$2 != null && Raids.canJoinRaid(this)) {
                     $$2.joinRaid($$0, $$2.getGroupsSpawned(), this, null, true);
                  }
               }
            } else {
               net.minecraft.world.entity.LivingEntity $$3 = this.getTarget();
               if ($$3 != null
                  && ($$3.getType() == net.minecraft.world.entity.EntityType.PLAYER || $$3.getType() == net.minecraft.world.entity.EntityType.IRON_GOLEM)) {
                  this.noActionTime = 0;
               }
            }
         }
      }

      super.aiStep();
   }

   @Override
   protected void updateNoActionTime() {
      this.noActionTime += 2;
   }

   @Override
   public void die(DamageSource $$0) {
      if (this.level() instanceof ServerLevel $$1) {
         net.minecraft.world.entity.Entity $$2 = $$0.getEntity();
         Raid $$3 = this.getCurrentRaid();
         if ($$3 != null) {
            if (this.isPatrolLeader()) {
               $$3.removeLeader(this.getWave());
            }

            if ($$2 != null && $$2.getType() == net.minecraft.world.entity.EntityType.PLAYER) {
               $$3.addHeroOfTheVillage($$2);
            }

            $$3.removeFromRaid($$1, this, false);
         }
      }

      super.die($$0);
   }

   @Override
   public boolean canJoinPatrol() {
      return !this.hasActiveRaid();
   }

   public void setCurrentRaid(Raid $$0) {
      this.raid = $$0;
   }

   
   public Raid getCurrentRaid() {
      return this.raid;
   }

   public boolean isCaptain() {
      ItemStack $$0 = this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
      boolean $$1 = !$$0.isEmpty() && ItemStack.matches($$0, Raid.getOminousBannerInstance(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
      boolean $$2 = this.isPatrolLeader();
      return $$1 && $$2;
   }

   public boolean hasRaid() {
      return !(this.level() instanceof ServerLevel $$0) ? false : this.getCurrentRaid() != null || $$0.getRaidAt(this.blockPosition()) != null;
   }

   public boolean hasActiveRaid() {
      return this.getCurrentRaid() != null && this.getCurrentRaid().isActive();
   }

   public void setWave(int $$0) {
      this.wave = $$0;
   }

   public int getWave() {
      return this.wave;
   }

   public boolean isCelebrating() {
      return (Boolean)this.entityData.get(IS_CELEBRATING);
   }

   public void setCelebrating(boolean $$0) {
      this.entityData.set(IS_CELEBRATING, $$0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putInt("Wave", this.wave);
      $$0.putBoolean("CanJoinRaid", this.canJoinRaid);
      if (this.raid != null && this.level() instanceof ServerLevel $$1) {
         $$1.getRaids().getId(this.raid).ifPresent($$1x -> $$0.putInt("RaidId", $$1x));
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.wave = $$0.getIntOr("Wave", 0);
      this.canJoinRaid = $$0.getBooleanOr("CanJoinRaid", false);
      if (this.level() instanceof ServerLevel $$1) {
         $$0.getInt("RaidId").ifPresent($$1x -> {
            this.raid = $$1.getRaids().get($$1x);
            if (this.raid != null) {
               this.raid.addWaveMob($$1, this.wave, this, false);
               if (this.isPatrolLeader()) {
                  this.raid.setLeader(this.wave, this);
               }
            }
         });
      }
   }

   @Override
   protected void pickUpItem(ServerLevel $$0, ItemEntity $$1) {
      ItemStack $$2 = $$1.getItem();
      boolean $$3 = this.hasActiveRaid() && this.getCurrentRaid().getLeader(this.getWave()) != null;
      if (this.hasActiveRaid() && !$$3 && ItemStack.matches($$2, Raid.getOminousBannerInstance(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)))
         )
       {
         net.minecraft.world.entity.EquipmentSlot $$4 = net.minecraft.world.entity.EquipmentSlot.HEAD;
         ItemStack $$5 = this.getItemBySlot($$4);
         double $$6 = this.getDropChances().byEquipment($$4);
         if (!$$5.isEmpty() && Math.max(this.random.nextFloat() - 0.1F, 0.0F) < $$6) {
            this.spawnAtLocation($$0, $$5);
         }

         this.onItemPickup($$1);
         this.setItemSlot($$4, $$2);
         this.take($$1, $$2.getCount());
         $$1.discard();
         this.getCurrentRaid().setLeader(this.getWave(), this);
         this.setPatrolLeader(true);
      } else {
         super.pickUpItem($$0, $$1);
      }
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return this.getCurrentRaid() == null ? super.removeWhenFarAway($$0) : false;
   }

   @Override
   public boolean requiresCustomPersistence() {
      return super.requiresCustomPersistence() || this.getCurrentRaid() != null;
   }

   public int getTicksOutsideRaid() {
      return this.ticksOutsideRaid;
   }

   public void setTicksOutsideRaid(int $$0) {
      this.ticksOutsideRaid = $$0;
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (this.hasActiveRaid()) {
         this.getCurrentRaid().updateBossbar();
      }

      return super.hurtServer($$0, $$1, $$2);
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      this.setCanJoinRaid(this.getType() != net.minecraft.world.entity.EntityType.WITCH || $$2 != net.minecraft.world.entity.EntitySpawnReason.NATURAL);
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   public abstract SoundEvent getCelebrateSound();

   protected static class HoldGroundAttackGoal extends Goal {
      private final Raider mob;
      private final float hostileRadiusSqr;
      public final TargetingConditions shoutTargeting = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight().ignoreInvisibilityTesting();

      public HoldGroundAttackGoal(AbstractIllager $$0, float $$1) {
         this.mob = $$0;
         this.hostileRadiusSqr = $$1 * $$1;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         net.minecraft.world.entity.LivingEntity $$0 = this.mob.getLastHurtByMob();
         return this.mob.getCurrentRaid() == null
            && this.mob.isPatrolling()
            && this.mob.getTarget() != null
            && !this.mob.isAggressive()
            && ($$0 == null || $$0.getType() != net.minecraft.world.entity.EntityType.PLAYER);
      }

      @Override
      public void start() {
         super.start();
         this.mob.getNavigation().stop();

         for (Raider $$1 : getServerLevel(this.mob)
            .getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0, 8.0, 8.0))) {
            $$1.setTarget(this.mob.getTarget());
         }
      }

      @Override
      public void stop() {
         super.stop();
         net.minecraft.world.entity.LivingEntity $$0 = this.mob.getTarget();
         if ($$0 != null) {
            for (Raider $$2 : getServerLevel(this.mob)
               .getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0, 8.0, 8.0))) {
               $$2.setTarget($$0);
               $$2.setAggressive(true);
            }

            this.mob.setAggressive(true);
         }
      }

      @Override
      public boolean requiresUpdateEveryTick() {
         return true;
      }

      @Override
      public void tick() {
         net.minecraft.world.entity.LivingEntity $$0 = this.mob.getTarget();
         if ($$0 != null) {
            if (this.mob.distanceToSqr($$0) > this.hostileRadiusSqr) {
               this.mob.getLookControl().setLookAt($$0, 30.0F, 30.0F);
               if (this.mob.random.nextInt(50) == 0) {
                  this.mob.playAmbientSound();
               }
            } else {
               this.mob.setAggressive(true);
            }

            super.tick();
         }
      }
   }

   public class ObtainRaidLeaderBannerGoal<T extends Raider> extends Goal {
      private final T mob;
      private Int2LongOpenHashMap unreachableBannerCache = new Int2LongOpenHashMap();
      
      private Path pathToBanner;
      
      private ItemEntity pursuedBannerItemEntity;

      public ObtainRaidLeaderBannerGoal(final T $$1) {
         this.mob = $$1;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         if (this.cannotPickUpBanner()) {
            return false;
         } else {
            Int2LongOpenHashMap $$0 = new Int2LongOpenHashMap();
            double $$1 = Raider.this.getAttributeValue(Attributes.FOLLOW_RANGE);

            for (ItemEntity $$3 : this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate($$1, 8.0, $$1), Raider.ALLOWED_ITEMS)) {
               long $$4 = this.unreachableBannerCache.getOrDefault($$3.getId(), Long.MIN_VALUE);
               if (Raider.this.level().getGameTime() < $$4) {
                  $$0.put($$3.getId(), $$4);
               } else {
                  Path $$5 = this.mob.getNavigation().createPath($$3, 1);
                  if ($$5 != null && $$5.canReach()) {
                     this.pathToBanner = $$5;
                     this.pursuedBannerItemEntity = $$3;
                     return true;
                  }

                  $$0.put($$3.getId(), Raider.this.level().getGameTime() + 600L);
               }
            }

            this.unreachableBannerCache = $$0;
            return false;
         }
      }

      @Override
      public boolean canContinueToUse() {
         if (this.pursuedBannerItemEntity == null || this.pathToBanner == null) {
            return false;
         } else if (this.pursuedBannerItemEntity.isRemoved()) {
            return false;
         } else {
            return this.pathToBanner.isDone() ? false : !this.cannotPickUpBanner();
         }
      }

      private boolean cannotPickUpBanner() {
         if (!this.mob.hasActiveRaid()) {
            return true;
         } else if (this.mob.getCurrentRaid().isOver()) {
            return true;
         } else if (!this.mob.canBeLeader()) {
            return true;
         } else if (ItemStack.matches(
            this.mob.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD),
            Raid.getOminousBannerInstance(this.mob.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN))
         )) {
            return true;
         } else {
            Raider $$0 = Raider.this.raid.getLeader(this.mob.getWave());
            return $$0 != null && $$0.isAlive();
         }
      }

      @Override
      public void start() {
         this.mob.getNavigation().moveTo(this.pathToBanner, 1.15F);
      }

      @Override
      public void stop() {
         this.pathToBanner = null;
         this.pursuedBannerItemEntity = null;
      }

      @Override
      public void tick() {
         if (this.pursuedBannerItemEntity != null && this.pursuedBannerItemEntity.closerThan(this.mob, 1.414)) {
            this.mob.pickUpItem(getServerLevel(Raider.this.level()), this.pursuedBannerItemEntity);
         }
      }
   }

   public class RaiderCelebration extends Goal {
      private final Raider mob;

      RaiderCelebration(final Raider $$1) {
         this.mob = $$1;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         Raid $$0 = this.mob.getCurrentRaid();
         return this.mob.isAlive() && this.mob.getTarget() == null && $$0 != null && $$0.isLoss();
      }

      @Override
      public void start() {
         this.mob.setCelebrating(true);
         super.start();
      }

      @Override
      public void stop() {
         this.mob.setCelebrating(false);
         super.stop();
      }

      @Override
      public void tick() {
         if (!this.mob.isSilent() && this.mob.random.nextInt(this.adjustedTickDelay(100)) == 0) {
            Raider.this.makeSound(Raider.this.getCelebrateSound());
         }

         if (!this.mob.isPassenger() && this.mob.random.nextInt(this.adjustedTickDelay(50)) == 0) {
            this.mob.getJumpControl().jump();
         }

         super.tick();
      }
   }

   static class RaiderMoveThroughVillageGoal extends Goal {
      private final Raider raider;
      private final double speedModifier;
      private BlockPos poiPos;
      private final List<BlockPos> visited = Lists.newArrayList();
      private final int distanceToPoi;
      private boolean stuck;

      public RaiderMoveThroughVillageGoal(Raider $$0, double $$1, int $$2) {
         this.raider = $$0;
         this.speedModifier = $$1;
         this.distanceToPoi = $$2;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         this.updateVisited();
         return this.isValidRaid() && this.hasSuitablePoi() && this.raider.getTarget() == null;
      }

      private boolean isValidRaid() {
         return this.raider.hasActiveRaid() && !this.raider.getCurrentRaid().isOver();
      }

      private boolean hasSuitablePoi() {
         ServerLevel $$0 = (ServerLevel)this.raider.level();
         BlockPos $$1 = this.raider.blockPosition();
         Optional<BlockPos> $$2 = $$0.getPoiManager()
            .getRandom($$0x -> $$0x.is(PoiTypes.HOME), this::hasNotVisited, PoiManager.Occupancy.ANY, $$1, 48, this.raider.random);
         if ($$2.isEmpty()) {
            return false;
         } else {
            this.poiPos = $$2.get().immutable();
            return true;
         }
      }

      @Override
      public boolean canContinueToUse() {
         return this.raider.getNavigation().isDone()
            ? false
            : this.raider.getTarget() == null
               && !this.poiPos.closerToCenterThan(this.raider.position(), this.raider.getBbWidth() + this.distanceToPoi)
               && !this.stuck;
      }

      @Override
      public void stop() {
         if (this.poiPos.closerToCenterThan(this.raider.position(), this.distanceToPoi)) {
            this.visited.add(this.poiPos);
         }
      }

      @Override
      public void start() {
         super.start();
         this.raider.setNoActionTime(0);
         this.raider.getNavigation().moveTo(this.poiPos.getX(), this.poiPos.getY(), this.poiPos.getZ(), this.speedModifier);
         this.stuck = false;
      }

      @Override
      public void tick() {
         if (this.raider.getNavigation().isDone()) {
            Vec3 $$0 = Vec3.atBottomCenterOf(this.poiPos);
            Vec3 $$1 = DefaultRandomPos.getPosTowards(this.raider, 16, 7, $$0, (float) (Math.PI / 10));
            if ($$1 == null) {
               $$1 = DefaultRandomPos.getPosTowards(this.raider, 8, 7, $$0, (float) (Math.PI / 2));
            }

            if ($$1 == null) {
               this.stuck = true;
               return;
            }

            this.raider.getNavigation().moveTo($$1.x, $$1.y, $$1.z, this.speedModifier);
         }
      }

      private boolean hasNotVisited(BlockPos $$0) {
         for (BlockPos $$1 : this.visited) {
            if (Objects.equals($$0, $$1)) {
               return false;
            }
         }

         return true;
      }

      private void updateVisited() {
         if (this.visited.size() > 2) {
            this.visited.remove(0);
         }
      }
   }
}
