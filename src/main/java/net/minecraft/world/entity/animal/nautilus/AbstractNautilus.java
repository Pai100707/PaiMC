package net.minecraft.world.entity.animal.nautilus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractNautilus
   extends net.minecraft.world.entity.TamableAnimal
   implements net.minecraft.world.entity.HasCustomInventoryScreen,
   net.minecraft.world.entity.PlayerRideableJumping {
   public static final int INVENTORY_SLOT_OFFSET = 500;
   public static final int INVENTORY_ROWS = 3;
   public static final int SMALL_RESTRICTION_RADIUS = 16;
   public static final int LARGE_RESTRICTION_RADIUS = 32;
   public static final int RESTRICTION_RADIUS_BUFFER = 8;
   private static final int EFFECT_DURATION = 60;
   private static final int EFFECT_REFRESH_RATE = 40;
   private static final double NAUTILUS_WATER_RESISTANCE = 0.9;
   private static final float IN_WATER_SPEED_MODIFIER = 0.011F;
   private static final float RIDDEN_SPEED_MODIFIER_IN_WATER = 0.0325F;
   private static final float RIDDEN_SPEED_MODIFIER_ON_LAND = 0.02F;
   private static final EntityDataAccessor<Boolean> DASH = SynchedEntityData.defineId(AbstractNautilus.class, EntityDataSerializers.BOOLEAN);
   private static final int DASH_COOLDOWN_TICKS = 40;
   private static final int DASH_MINIMUM_DURATION_TICKS = 5;
   private static final float DASH_MOMENTUM_IN_WATER = 1.2F;
   private static final float DASH_MOMENTUM_ON_LAND = 0.5F;
   private int dashCooldown = 0;
   protected float playerJumpPendingScale;
   protected SimpleContainer inventory;
   private static final double BUBBLE_SPREAD_FACTOR = 0.8;
   private static final double BUBBLE_DIRECTION_SCALE = 1.1;
   private static final double BUBBLE_Y_OFFSET = 0.25;
   private static final double BUBBLE_PROBABILITY_MULTIPLIER = 2.0;
   private static final float BUBBLE_PROBABILITY_MIN = 0.15F;
   private static final float BUBBLE_PROBABILITY_MAX = 1.0F;

   protected AbstractNautilus(net.minecraft.world.entity.EntityType<? extends AbstractNautilus> $$0, Level $$1) {
      super($$0, $$1);
      this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.011F, 0.0F, true);
      this.lookControl = new SmoothSwimmingLookControl(this, 10);
      this.setPathfindingMalus(PathType.WATER, 0.0F);
      this.createInventory();
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return !this.isTame() && !this.isBaby() ? $$0.is(ItemTags.NAUTILUS_TAMING_ITEMS) : $$0.is(ItemTags.NAUTILUS_FOOD);
   }

   @Override
   protected void usePlayerItem(Player $$0, InteractionHand $$1, ItemStack $$2) {
      if ($$2.is(ItemTags.NAUTILUS_BUCKET_FOOD)) {
         $$0.setItemInHand($$1, ItemUtils.createFilledResult($$2, $$0, new ItemStack(Items.WATER_BUCKET)));
      } else {
         super.usePlayerItem($$0, $$1, $$2);
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes()
         .add(Attributes.MAX_HEALTH, 15.0)
         .add(Attributes.MOVEMENT_SPEED, 1.0)
         .add(Attributes.ATTACK_DAMAGE, 3.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.3F);
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   protected PathNavigation createNavigation(Level $$0) {
      return new WaterBoundPathNavigation(this, $$0);
   }

   @Override
   public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
      return 0.0F;
   }

   public static boolean checkNautilusSpawnRules(
      net.minecraft.world.entity.EntityType<? extends AbstractNautilus> $$0,
      LevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      int $$5 = $$1.getSeaLevel();
      int $$6 = $$5 - 25;
      return $$3.getY() >= $$6
         && $$3.getY() <= $$5 - 5
         && $$1.getFluidState($$3.below()).is(FluidTags.WATER)
         && $$1.getBlockState($$3.above()).is(Blocks.WATER);
   }

   @Override
   public boolean checkSpawnObstruction(LevelReader $$0) {
      return $$0.isUnobstructed(this);
   }

   @Override
   public boolean canUseSlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return $$0 != net.minecraft.world.entity.EquipmentSlot.SADDLE && $$0 != net.minecraft.world.entity.EquipmentSlot.BODY
         ? super.canUseSlot($$0)
         : this.isAlive() && !this.isBaby() && this.isTame();
   }

   @Override
   protected boolean canDispenserEquipIntoSlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return $$0 == net.minecraft.world.entity.EquipmentSlot.BODY
         || $$0 == net.minecraft.world.entity.EquipmentSlot.SADDLE
         || super.canDispenserEquipIntoSlot($$0);
   }

   @Override
   protected boolean canAddPassenger(net.minecraft.world.entity.Entity $$0) {
      return !this.isVehicle();
   }

   
   @Override
   public net.minecraft.world.entity.LivingEntity getControllingPassenger() {
      return (net.minecraft.world.entity.LivingEntity)(this.isSaddled() && this.getFirstPassenger() instanceof Player $$1
         ? $$1
         : super.getControllingPassenger());
   }

   @Override
   protected Vec3 getRiddenInput(Player $$0, Vec3 $$1) {
      float $$2 = $$0.xxa;
      float $$3 = 0.0F;
      float $$4 = 0.0F;
      if ($$0.zza != 0.0F) {
         float $$5 = Mth.cos($$0.getXRot() * (float) (Math.PI / 180.0));
         float $$6 = -Mth.sin($$0.getXRot() * (float) (Math.PI / 180.0));
         if ($$0.zza < 0.0F) {
            $$5 *= -0.5F;
            $$6 *= -0.5F;
         }

         $$4 = $$6;
         $$3 = $$5;
      }

      return new Vec3($$2, $$4, $$3);
   }

   protected Vec2 getRiddenRotation(net.minecraft.world.entity.LivingEntity $$0) {
      return new Vec2($$0.getXRot() * 0.5F, $$0.getYRot());
   }

   @Override
   protected void tickRidden(Player $$0, Vec3 $$1) {
      super.tickRidden($$0, $$1);
      Vec2 $$2 = this.getRiddenRotation($$0);
      float $$3 = this.getYRot();
      float $$4 = Mth.wrapDegrees($$2.y - $$3);
      float $$5 = 0.5F;
      $$3 += $$4 * 0.5F;
      this.setRot($$3, $$2.x);
      this.yRotO = this.yBodyRot = this.yHeadRot = $$3;
      if (this.isLocalInstanceAuthoritative()) {
         if (this.playerJumpPendingScale > 0.0F && !this.isJumping()) {
            this.executeRidersJump(this.playerJumpPendingScale, $$0);
         }

         this.playerJumpPendingScale = 0.0F;
      }
   }

   @Override
   protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
      float $$4 = this.getSpeed();
      this.moveRelative($$4, $$0);
      this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
   }

   @Override
   protected float getRiddenSpeed(Player $$0) {
      return this.isInWater()
         ? 0.0325F * (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED)
         : 0.02F * (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
   }

   protected void doPlayerRide(Player $$0) {
      if (!this.level().isClientSide()) {
         $$0.startRiding(this);
         if (!this.isVehicle()) {
            this.clearHome();
         }
      }
   }

   private int getNautilusRestrictionRadius() {
      return !this.isBaby() && this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.SADDLE).isEmpty() ? 32 : 16;
   }

   protected void checkRestriction() {
      if (!this.isLeashed() && !this.isVehicle() && this.isTame()) {
         int $$0 = this.getNautilusRestrictionRadius();
         if (!this.hasHome() || !this.getHomePosition().closerThan(this.blockPosition(), $$0 + 8) || $$0 != this.getHomeRadius()) {
            this.setHomeTo(this.blockPosition(), $$0);
         }
      }
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      this.checkRestriction();
      super.customServerAiStep($$0);
   }

   private void applyEffects(Level $$0) {
      if (this.getFirstPassenger() instanceof Player $$2) {
         boolean $$3 = $$2.hasEffect(MobEffects.BREATH_OF_THE_NAUTILUS);
         boolean $$4 = $$0.getGameTime() % 40L == 0L;
         if (!$$3 || $$4) {
            $$2.addEffect(new MobEffectInstance(MobEffects.BREATH_OF_THE_NAUTILUS, 60, 0, true, true, true));
         }
      }
   }

   private void spawnBubbles() {
      double $$0 = this.getDeltaMovement().length();
      double $$1 = Mth.clamp($$0 * 2.0, 0.15F, 1.0);
      if (this.random.nextFloat() < $$1) {
         float $$2 = this.getYRot();
         float $$3 = Mth.clamp(this.getXRot(), -10.0F, 10.0F);
         Vec3 $$4 = this.calculateViewVector($$3, $$2);
         double $$5 = this.random.nextDouble() * 0.8 * (1.0 + $$0);
         double $$6 = (this.random.nextFloat() - 0.5) * $$5;
         double $$7 = (this.random.nextFloat() - 0.5) * $$5;
         double $$8 = (this.random.nextFloat() - 0.5) * $$5;
         this.level().addParticle(ParticleTypes.BUBBLE, this.getX() - $$4.x * 1.1, this.getY() - $$4.y + 0.25, this.getZ() - $$4.z * 1.1, $$6, $$7, $$8);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.level().isClientSide()) {
         this.applyEffects(this.level());
      }

      if (this.isDashing() && this.dashCooldown < 35) {
         this.setDashing(false);
      }

      if (this.dashCooldown > 0) {
         this.dashCooldown--;
         if (this.dashCooldown == 0) {
            this.makeSound(this.getDashReadySound());
         }
      }

      if (this.isInWater()) {
         this.spawnBubbles();
      }
   }

   @Override
   public boolean canJump() {
      return this.isSaddled();
   }

   @Override
   public void onPlayerJump(int $$0) {
      if (this.isSaddled() && this.dashCooldown <= 0) {
         this.playerJumpPendingScale = this.getPlayerJumpPendingScale($$0);
      }
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DASH, false);
   }

   public boolean isDashing() {
      return (Boolean)this.entityData.get(DASH);
   }

   public void setDashing(boolean $$0) {
      this.entityData.set(DASH, $$0);
   }

   protected void executeRidersJump(float $$0, Player $$1) {
      this.addDeltaMovement(
         $$1.getLookAngle().scale((this.isInWater() ? 1.2F : 0.5F) * $$0 * this.getAttributeValue(Attributes.MOVEMENT_SPEED) * this.getBlockSpeedFactor())
      );
      this.dashCooldown = 40;
      this.setDashing(true);
      this.needsSync = true;
   }

   @Override
   public void handleStartJump(int $$0) {
      this.makeSound(this.getDashSound());
      this.gameEvent(GameEvent.ENTITY_ACTION);
      this.setDashing(true);
   }

   @Override
   public int getJumpCooldown() {
      return this.dashCooldown;
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      if (!this.firstTick && DASH.equals($$0)) {
         this.dashCooldown = this.dashCooldown == 0 ? 40 : this.dashCooldown;
      }

      super.onSyncedDataUpdated($$0);
   }

   @Override
   public void handleStopJump() {
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
   }

   
   protected SoundEvent getDashSound() {
      return null;
   }

   
   protected SoundEvent getDashReadySound() {
      return null;
   }

   @Override
   public InteractionResult interact(Player $$0, InteractionHand $$1) {
      this.setPersistenceRequired();
      return super.interact($$0, $$1);
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if (this.isBaby()) {
         return super.mobInteract($$0, $$1);
      } else if (this.isTame() && $$0.isSecondaryUseActive()) {
         this.openCustomInventoryScreen($$0);
         return InteractionResult.SUCCESS;
      } else {
         if (!$$2.isEmpty()) {
            if (!this.level().isClientSide() && !this.isTame() && this.isFood($$2)) {
               this.usePlayerItem($$0, $$1, $$2);
               this.tryToTame($$0);
               return InteractionResult.SUCCESS_SERVER;
            }

            if (this.isFood($$2) && this.getHealth() < this.getMaxHealth()) {
               FoodProperties $$3 = (FoodProperties)$$2.get(DataComponents.FOOD);
               this.heal($$3 != null ? 2 * $$3.nutrition() : 1.0F);
               this.usePlayerItem($$0, $$1, $$2);
               this.playEatingSound();
               return InteractionResult.SUCCESS;
            }

            InteractionResult $$4 = $$2.interactLivingEntity($$0, this, $$1);
            if ($$4.consumesAction()) {
               return $$4;
            }
         }

         if (this.isTame() && !$$0.isSecondaryUseActive() && !this.isFood($$2)) {
            this.doPlayerRide($$0);
            return InteractionResult.SUCCESS;
         } else {
            return super.mobInteract($$0, $$1);
         }
      }
   }

   private void tryToTame(Player $$0) {
      if (this.random.nextInt(3) == 0) {
         this.tame($$0);
         this.navigation.stop();
         this.level().broadcastEntityEvent(this, (byte)7);
      } else {
         this.level().broadcastEntityEvent(this, (byte)6);
      }

      this.playEatingSound();
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return true;
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      boolean $$3 = super.hurtServer($$0, $$1, $$2);
      if ($$3 && $$1.getEntity() instanceof net.minecraft.world.entity.LivingEntity $$4) {
         NautilusAi.setAngerTarget($$0, this, $$4);
      }

      return $$3;
   }

   @Override
   public boolean canBeAffected(MobEffectInstance $$0) {
      return $$0.getEffect() == MobEffects.POISON ? false : super.canBeAffected($$0);
   }

   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      RandomSource $$4 = $$0.getRandom();
      NautilusAi.initMemories(this, $$4);
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   @Override
   protected Holder<SoundEvent> getEquipSound(net.minecraft.world.entity.EquipmentSlot $$0, ItemStack $$1, Equippable $$2) {
      if ($$0 == net.minecraft.world.entity.EquipmentSlot.SADDLE && this.isUnderWater()) {
         return SoundEvents.NAUTILUS_SADDLE_UNDERWATER_EQUIP;
      } else {
         return (Holder<SoundEvent>)($$0 == net.minecraft.world.entity.EquipmentSlot.SADDLE
            ? SoundEvents.NAUTILUS_SADDLE_EQUIP
            : super.getEquipSound($$0, $$1, $$2));
      }
   }

   public final int getInventorySize() {
      return AbstractMountInventoryMenu.getInventorySize(this.getInventoryColumns());
   }

   protected void createInventory() {
      SimpleContainer $$0 = this.inventory;
      this.inventory = new SimpleContainer(this.getInventorySize());
      if ($$0 != null) {
         int $$1 = Math.min($$0.getContainerSize(), this.inventory.getContainerSize());

         for (int $$2 = 0; $$2 < $$1; $$2++) {
            ItemStack $$3 = $$0.getItem($$2);
            if (!$$3.isEmpty()) {
               this.inventory.setItem($$2, $$3.copy());
            }
         }
      }
   }

   @Override
   public void openCustomInventoryScreen(Player $$0) {
      if (!this.level().isClientSide() && (!this.isVehicle() || this.hasPassenger($$0)) && this.isTame()) {
         $$0.openNautilusInventory(this, this.inventory);
      }
   }

   
   @Override
   public net.minecraft.world.entity.SlotAccess getSlot(int $$0) {
      int $$1 = $$0 - 500;
      return $$1 >= 0 && $$1 < this.inventory.getContainerSize() ? this.inventory.getSlot($$1) : super.getSlot($$0);
   }

   public boolean hasInventoryChanged(Container $$0) {
      return this.inventory != $$0;
   }

   public int getInventoryColumns() {
      return 0;
   }

   protected boolean isMobControlled() {
      return this.getFirstPassenger() instanceof net.minecraft.world.entity.Mob;
   }

   protected boolean isAggravated() {
      return this.getBrain().hasMemoryValue(MemoryModuleType.ANGRY_AT) || this.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
   }
}
