package net.minecraft.world.entity.monster.zombie;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Drowned extends Zombie implements RangedAttackMob {
   public static final float NAUTILUS_SHELL_CHANCE = 0.03F;
   private static final float ZOMBIE_NAUTILUS_JOCKEY_CHANCE = 0.5F;
   boolean searchingForLand;

   public Drowned(net.minecraft.world.entity.EntityType<? extends Drowned> $$0, Level $$1) {
      super($$0, $$1);
      this.moveControl = new Drowned.DrownedMoveControl(this);
      this.setPathfindingMalus(PathType.WATER, 0.0F);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Zombie.createAttributes().add(Attributes.STEP_HEIGHT, 1.0);
   }

   @Override
   protected PathNavigation createNavigation(Level $$0) {
      return new AmphibiousPathNavigation(this, $$0);
   }

   @Override
   protected void addBehaviourGoals() {
      this.goalSelector.addGoal(1, new Drowned.DrownedGoToWaterGoal(this, 1.0));
      this.goalSelector.addGoal(2, new Drowned.DrownedTridentAttackGoal(this, 1.0, 40, 10.0F));
      this.goalSelector.addGoal(2, new Drowned.DrownedAttackGoal(this, 1.0, false));
      this.goalSelector.addGoal(5, new Drowned.DrownedGoToBeachGoal(this, 1.0));
      this.goalSelector.addGoal(6, new Drowned.DrownedSwimUpGoal(this, 1.0, this.level().getSeaLevel()));
      this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Drowned.class).setAlertOthers(ZombifiedPiglin.class));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, ($$0, $$1) -> this.okTarget($$0)));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Axolotl.class, true, false));
      this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
   }

   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      $$3 = super.finalizeSpawn($$0, $$1, $$2, $$3);
      if (this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND).isEmpty() && $$0.getRandom().nextFloat() < 0.03F) {
         this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new ItemStack(Items.NAUTILUS_SHELL));
         this.setGuaranteedDrop(net.minecraft.world.entity.EquipmentSlot.OFFHAND);
      }

      if (($$2 == net.minecraft.world.entity.EntitySpawnReason.NATURAL || $$2 == net.minecraft.world.entity.EntitySpawnReason.STRUCTURE)
         && this.getMainHandItem().is(Items.TRIDENT)
         && $$0.getRandom().nextFloat() < 0.5F
         && !this.isBaby()
         && !$$0.getBiome(this.blockPosition()).is(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS)) {
         ZombieNautilus $$4 = net.minecraft.world.entity.EntityType.ZOMBIE_NAUTILUS.create(this.level(), net.minecraft.world.entity.EntitySpawnReason.JOCKEY);
         if ($$4 != null) {
            if ($$2 == net.minecraft.world.entity.EntitySpawnReason.STRUCTURE) {
               $$4.setPersistenceRequired();
            }

            $$4.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
            $$4.finalizeSpawn($$0, $$1, $$2, null);
            this.startRiding($$4, false, false);
            $$0.addFreshEntity($$4);
         }
      }

      return $$3;
   }

   public static boolean checkDrownedSpawnRules(
      net.minecraft.world.entity.EntityType<Drowned> $$0,
      ServerLevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      if (!$$1.getFluidState($$3.below()).is(FluidTags.WATER) && !net.minecraft.world.entity.EntitySpawnReason.isSpawner($$2)) {
         return false;
      } else {
         Holder<Biome> $$5 = $$1.getBiome($$3);
         boolean $$6 = $$1.getDifficulty() != Difficulty.PEACEFUL
            && (net.minecraft.world.entity.EntitySpawnReason.ignoresLightRequirements($$2) || isDarkEnoughToSpawn($$1, $$3, $$4))
            && (net.minecraft.world.entity.EntitySpawnReason.isSpawner($$2) || $$1.getFluidState($$3).is(FluidTags.WATER));
         if (!$$6 || !net.minecraft.world.entity.EntitySpawnReason.isSpawner($$2) && $$2 != net.minecraft.world.entity.EntitySpawnReason.REINFORCEMENT) {
            return $$5.is(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS) ? $$4.nextInt(15) == 0 && $$6 : $$4.nextInt(40) == 0 && isDeepEnoughToSpawn($$1, $$3) && $$6;
         } else {
            return true;
         }
      }
   }

   private static boolean isDeepEnoughToSpawn(LevelAccessor $$0, BlockPos $$1) {
      return $$1.getY() < $$0.getSeaLevel() - 5;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return this.isInWater() ? SoundEvents.DROWNED_AMBIENT_WATER : SoundEvents.DROWNED_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return this.isInWater() ? SoundEvents.DROWNED_HURT_WATER : SoundEvents.DROWNED_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return this.isInWater() ? SoundEvents.DROWNED_DEATH_WATER : SoundEvents.DROWNED_DEATH;
   }

   @Override
   protected SoundEvent getStepSound() {
      return SoundEvents.DROWNED_STEP;
   }

   @Override
   protected SoundEvent getSwimSound() {
      return SoundEvents.DROWNED_SWIM;
   }

   @Override
   protected boolean canSpawnInLiquids() {
      return true;
   }

   @Override
   protected void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
      if ($$0.nextFloat() > 0.9) {
         int $$2 = $$0.nextInt(16);
         if ($$2 < 10) {
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
         } else {
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
         }
      }
   }

   @Override
   protected boolean canReplaceCurrentItem(ItemStack $$0, ItemStack $$1, net.minecraft.world.entity.EquipmentSlot $$2) {
      return $$1.is(Items.NAUTILUS_SHELL) ? false : super.canReplaceCurrentItem($$0, $$1, $$2);
   }

   @Override
   protected boolean convertsInWater() {
      return false;
   }

   @Override
   public boolean checkSpawnObstruction(LevelReader $$0) {
      return $$0.isUnobstructed(this);
   }

   public boolean okTarget(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0 != null ? !this.level().isBrightOutside() || $$0.isInWater() : false;
   }

   @Override
   public boolean isPushedByFluid() {
      return !this.isSwimming();
   }

   boolean wantsToSwim() {
      if (this.searchingForLand) {
         return true;
      } else {
         net.minecraft.world.entity.LivingEntity $$0 = this.getTarget();
         return $$0 != null && $$0.isInWater();
      }
   }

   @Override
   protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
      if (this.isUnderWater() && this.wantsToSwim()) {
         this.moveRelative(0.01F, $$0);
         this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
      } else {
         super.travelInWater($$0, $$1, $$2, $$3);
      }
   }

   @Override
   public void updateSwimming() {
      if (!this.level().isClientSide()) {
         this.setSwimming(this.isEffectiveAi() && this.isUnderWater() && this.wantsToSwim());
      }
   }

   @Override
   public boolean isVisuallySwimming() {
      return this.isSwimming() && !this.isPassenger();
   }

   protected boolean closeToNextPos() {
      Path $$0 = this.getNavigation().getPath();
      if ($$0 != null) {
         BlockPos $$1 = $$0.getTarget();
         if ($$1 != null) {
            double $$2 = this.distanceToSqr($$1.getX(), $$1.getY(), $$1.getZ());
            if ($$2 < 4.0) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public void performRangedAttack(net.minecraft.world.entity.LivingEntity $$0, float $$1) {
      ItemStack $$2 = this.getMainHandItem();
      ItemStack $$3 = $$2.is(Items.TRIDENT) ? $$2 : new ItemStack(Items.TRIDENT);
      ThrownTrident $$4 = new ThrownTrident(this.level(), this, $$3);
      double $$5 = $$0.getX() - this.getX();
      double $$6 = $$0.getY(0.3333333333333333) - $$4.getY();
      double $$7 = $$0.getZ() - this.getZ();
      double $$8 = Math.sqrt($$5 * $$5 + $$7 * $$7);
      if (this.level() instanceof ServerLevel $$9) {
         Projectile.spawnProjectileUsingShoot($$4, $$9, $$3, $$5, $$6 + $$8 * 0.2F, $$7, 1.6F, 14 - this.level().getDifficulty().getId() * 4);
      }

      this.playSound(SoundEvents.DROWNED_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   @Override
   public TagKey<Item> getPreferredWeaponType() {
      return ItemTags.DROWNED_PREFERRED_WEAPONS;
   }

   public void setSearchingForLand(boolean $$0) {
      this.searchingForLand = $$0;
   }

   @Override
   public void rideTick() {
      super.rideTick();
      if (this.getControlledVehicle() instanceof net.minecraft.world.entity.PathfinderMob $$0) {
         this.yBodyRot = $$0.yBodyRot;
      }
   }

   @Override
   public boolean wantsToPickUp(ServerLevel $$0, ItemStack $$1) {
      return $$1.is(ItemTags.SPEARS) ? false : super.wantsToPickUp($$0, $$1);
   }

   static class DrownedAttackGoal extends ZombieAttackGoal {
      private final Drowned drowned;

      public DrownedAttackGoal(Drowned $$0, double $$1, boolean $$2) {
         super($$0, $$1, $$2);
         this.drowned = $$0;
      }

      @Override
      public boolean canUse() {
         return super.canUse() && this.drowned.okTarget(this.drowned.getTarget());
      }

      @Override
      public boolean canContinueToUse() {
         return super.canContinueToUse() && this.drowned.okTarget(this.drowned.getTarget());
      }
   }

   static class DrownedGoToBeachGoal extends MoveToBlockGoal {
      private final Drowned drowned;

      public DrownedGoToBeachGoal(Drowned $$0, double $$1) {
         super($$0, $$1, 8, 2);
         this.drowned = $$0;
      }

      @Override
      public boolean canUse() {
         return super.canUse()
            && !this.drowned.level().isBrightOutside()
            && this.drowned.isInWater()
            && this.drowned.getY() >= this.drowned.level().getSeaLevel() - 3;
      }

      @Override
      public boolean canContinueToUse() {
         return super.canContinueToUse();
      }

      @Override
      protected boolean isValidTarget(LevelReader $$0, BlockPos $$1) {
         BlockPos $$2 = $$1.above();
         return $$0.isEmptyBlock($$2) && $$0.isEmptyBlock($$2.above()) ? $$0.getBlockState($$1).entityCanStandOn($$0, $$1, this.drowned) : false;
      }

      @Override
      public void start() {
         this.drowned.setSearchingForLand(false);
         super.start();
      }

      @Override
      public void stop() {
         super.stop();
      }
   }

   static class DrownedGoToWaterGoal extends Goal {
      private final net.minecraft.world.entity.PathfinderMob mob;
      private double wantedX;
      private double wantedY;
      private double wantedZ;
      private final double speedModifier;
      private final Level level;

      public DrownedGoToWaterGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1) {
         this.mob = $$0;
         this.speedModifier = $$1;
         this.level = $$0.level();
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         if (!this.level.isBrightOutside()) {
            return false;
         } else if (this.mob.isInWater()) {
            return false;
         } else {
            Vec3 $$0 = this.getWaterPos();
            if ($$0 == null) {
               return false;
            } else {
               this.wantedX = $$0.x;
               this.wantedY = $$0.y;
               this.wantedZ = $$0.z;
               return true;
            }
         }
      }

      @Override
      public boolean canContinueToUse() {
         return !this.mob.getNavigation().isDone();
      }

      @Override
      public void start() {
         this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
      }

      
      private Vec3 getWaterPos() {
         RandomSource $$0 = this.mob.getRandom();
         BlockPos $$1 = this.mob.blockPosition();

         for (int $$2 = 0; $$2 < 10; $$2++) {
            BlockPos $$3 = $$1.offset($$0.nextInt(20) - 10, 2 - $$0.nextInt(8), $$0.nextInt(20) - 10);
            if (this.level.getBlockState($$3).is(Blocks.WATER)) {
               return Vec3.atBottomCenterOf($$3);
            }
         }

         return null;
      }
   }

   static class DrownedMoveControl extends MoveControl {
      private final Drowned drowned;

      public DrownedMoveControl(Drowned $$0) {
         super($$0);
         this.drowned = $$0;
      }

      @Override
      public void tick() {
         net.minecraft.world.entity.LivingEntity $$0 = this.drowned.getTarget();
         if (this.drowned.wantsToSwim() && this.drowned.isInWater()) {
            if ($$0 != null && $$0.getY() > this.drowned.getY() || this.drowned.searchingForLand) {
               this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0, 0.002, 0.0));
            }

            if (this.operation != MoveControl.Operation.MOVE_TO || this.drowned.getNavigation().isDone()) {
               this.drowned.setSpeed(0.0F);
               return;
            }

            double $$1 = this.wantedX - this.drowned.getX();
            double $$2 = this.wantedY - this.drowned.getY();
            double $$3 = this.wantedZ - this.drowned.getZ();
            double $$4 = Math.sqrt($$1 * $$1 + $$2 * $$2 + $$3 * $$3);
            $$2 /= $$4;
            float $$5 = (float)(Mth.atan2($$3, $$1) * 180.0F / (float)Math.PI) - 90.0F;
            this.drowned.setYRot(this.rotlerp(this.drowned.getYRot(), $$5, 90.0F));
            this.drowned.yBodyRot = this.drowned.getYRot();
            float $$6 = (float)(this.speedModifier * this.drowned.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float $$7 = Mth.lerp(0.125F, this.drowned.getSpeed(), $$6);
            this.drowned.setSpeed($$7);
            this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add($$7 * $$1 * 0.005, $$7 * $$2 * 0.1, $$7 * $$3 * 0.005));
         } else {
            if (!this.drowned.onGround()) {
               this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0, -0.008, 0.0));
            }

            super.tick();
         }
      }
   }

   static class DrownedSwimUpGoal extends Goal {
      private final Drowned drowned;
      private final double speedModifier;
      private final int seaLevel;
      private boolean stuck;

      public DrownedSwimUpGoal(Drowned $$0, double $$1, int $$2) {
         this.drowned = $$0;
         this.speedModifier = $$1;
         this.seaLevel = $$2;
      }

      @Override
      public boolean canUse() {
         return !this.drowned.level().isBrightOutside() && this.drowned.isInWater() && this.drowned.getY() < this.seaLevel - 2;
      }

      @Override
      public boolean canContinueToUse() {
         return this.canUse() && !this.stuck;
      }

      @Override
      public void tick() {
         if (this.drowned.getY() < this.seaLevel - 1 && (this.drowned.getNavigation().isDone() || this.drowned.closeToNextPos())) {
            Vec3 $$0 = DefaultRandomPos.getPosTowards(
               this.drowned, 4, 8, new Vec3(this.drowned.getX(), this.seaLevel - 1, this.drowned.getZ()), (float) (Math.PI / 2)
            );
            if ($$0 == null) {
               this.stuck = true;
               return;
            }

            this.drowned.getNavigation().moveTo($$0.x, $$0.y, $$0.z, this.speedModifier);
         }
      }

      @Override
      public void start() {
         this.drowned.setSearchingForLand(true);
         this.stuck = false;
      }

      @Override
      public void stop() {
         this.drowned.setSearchingForLand(false);
      }
   }

   static class DrownedTridentAttackGoal extends RangedAttackGoal {
      private final Drowned drowned;

      public DrownedTridentAttackGoal(RangedAttackMob $$0, double $$1, int $$2, float $$3) {
         super($$0, $$1, $$2, $$3);
         this.drowned = (Drowned)$$0;
      }

      @Override
      public boolean canUse() {
         return super.canUse() && this.drowned.getMainHandItem().is(Items.TRIDENT);
      }

      @Override
      public void start() {
         super.start();
         this.drowned.setAggressive(true);
         this.drowned.startUsingItem(InteractionHand.MAIN_HAND);
      }

      @Override
      public void stop() {
         super.stop();
         this.drowned.stopUsingItem();
         this.drowned.setAggressive(false);
      }
   }
}
