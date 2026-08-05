package net.minecraft.world.entity.animal.allay;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem.Data;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem.Listener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem.Ticker;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem.User;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class Allay extends net.minecraft.world.entity.PathfinderMob implements InventoryCarrier, VibrationSystem {
   private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 1, 1);
   private static final int LIFTING_ITEM_ANIMATION_DURATION = 5;
   private static final float DANCING_LOOP_DURATION = 55.0F;
   private static final float SPINNING_ANIMATION_DURATION = 15.0F;
   private static final int DEFAULT_DUPLICATION_COOLDOWN = 0;
   private static final int DUPLICATION_COOLDOWN_TICKS = 6000;
   private static final int NUM_OF_DUPLICATION_HEARTS = 3;
   public static final int MAX_NOTEBLOCK_DISTANCE = 1024;
   private static final EntityDataAccessor<Boolean> DATA_DANCING = SynchedEntityData.defineId(Allay.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_CAN_DUPLICATE = SynchedEntityData.defineId(Allay.class, EntityDataSerializers.BOOLEAN);
   protected static final ImmutableList<SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(
      SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NEAREST_ITEMS
   );
   protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
      MemoryModuleType.PATH,
      MemoryModuleType.LOOK_TARGET,
      MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
      MemoryModuleType.WALK_TARGET,
      MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
      MemoryModuleType.HURT_BY,
      MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
      MemoryModuleType.LIKED_PLAYER,
      MemoryModuleType.LIKED_NOTEBLOCK_POSITION,
      MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS,
      MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
      MemoryModuleType.IS_PANICKING,
      new MemoryModuleType[0]
   );
   public static final ImmutableList<Float> THROW_SOUND_PITCHES = ImmutableList.of(
      0.5625F, 0.625F, 0.75F, 0.9375F, 1.0F, 1.0F, 1.125F, 1.25F, 1.5F, 1.875F, 2.0F, 2.25F, new Float[]{2.5F, 3.0F, 3.75F, 4.0F}
   );
   private final DynamicGameEventListener<Listener> dynamicVibrationListener;
   private Data vibrationData;
   private final User vibrationUser;
   private final DynamicGameEventListener<Allay.JukeboxListener> dynamicJukeboxListener;
   private final SimpleContainer inventory = new SimpleContainer(1);
   
   private BlockPos jukeboxPos;
   private long duplicationCooldown = 0L;
   private float holdingItemAnimationTicks;
   private float holdingItemAnimationTicks0;
   private float dancingAnimationTicks;
   private float spinningAnimationTicks;
   private float spinningAnimationTicks0;

   public Allay(net.minecraft.world.entity.EntityType<? extends Allay> $$0, Level $$1) {
      super($$0, $$1);
      this.moveControl = new FlyingMoveControl(this, 20, true);
      this.setCanPickUpLoot(this.canPickUpLoot());
      this.vibrationUser = new Allay.VibrationUser();
      this.vibrationData = new Data();
      this.dynamicVibrationListener = new DynamicGameEventListener(new Listener(this));
      this.dynamicJukeboxListener = new DynamicGameEventListener(
         new Allay.JukeboxListener(this.vibrationUser.getPositionSource(), ((GameEvent)GameEvent.JUKEBOX_PLAY.value()).notificationRadius())
      );
   }

   @Override
   protected Brain.Provider<Allay> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   @Override
   protected Brain<?> makeBrain(Dynamic<?> $$0) {
      return AllayAi.makeBrain(this.brainProvider().makeBrain($$0));
   }

   @Override
   public Brain<Allay> getBrain() {
      return (Brain<Allay>)super.getBrain();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 20.0)
         .add(Attributes.FLYING_SPEED, 0.1F)
         .add(Attributes.MOVEMENT_SPEED, 0.1F)
         .add(Attributes.ATTACK_DAMAGE, 2.0);
   }

   @Override
   protected PathNavigation createNavigation(Level $$0) {
      FlyingPathNavigation $$1 = new FlyingPathNavigation(this, $$0);
      $$1.setCanOpenDoors(false);
      $$1.setCanFloat(true);
      $$1.setRequiredPathLength(48.0F);
      return $$1;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_DANCING, false);
      $$0.define(DATA_CAN_DUPLICATE, true);
   }

   @Override
   public void travel(Vec3 $$0) {
      this.travelFlying($$0, this.getSpeed());
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      return this.isLikedPlayer($$1.getEntity()) ? false : super.hurtServer($$0, $$1, $$2);
   }

   @Override
   protected boolean considersEntityAsAlly(net.minecraft.world.entity.Entity $$0) {
      return this.isLikedPlayer($$0) || super.considersEntityAsAlly($$0);
   }

   private boolean isLikedPlayer(net.minecraft.world.entity.Entity $$0) {
      if (!($$0 instanceof Player $$1)) {
         return false;
      } else {
         Optional<UUID> $$2 = this.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
         return $$2.isPresent() && $$1.getUUID().equals($$2.get());
      }
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
   }

   @Override
   protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return this.hasItemInSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND)
         ? SoundEvents.ALLAY_AMBIENT_WITH_ITEM
         : SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.ALLAY_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.ALLAY_DEATH;
   }

   @Override
   protected float getSoundVolume() {
      return 0.4F;
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      ProfilerFiller $$1 = Profiler.get();
      $$1.push("allayBrain");
      this.getBrain().tick($$0, this);
      $$1.pop();
      $$1.push("allayActivityUpdate");
      AllayAi.updateActivity(this);
      $$1.pop();
      super.customServerAiStep($$0);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide() && this.isAlive() && this.tickCount % 10 == 0) {
         this.heal(1.0F);
      }

      if (this.isDancing() && this.shouldStopDancing() && this.tickCount % 20 == 0) {
         this.setDancing(false);
         this.jukeboxPos = null;
      }

      this.updateDuplicationCooldown();
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         this.holdingItemAnimationTicks0 = this.holdingItemAnimationTicks;
         if (this.hasItemInHand()) {
            this.holdingItemAnimationTicks = Mth.clamp(this.holdingItemAnimationTicks + 1.0F, 0.0F, 5.0F);
         } else {
            this.holdingItemAnimationTicks = Mth.clamp(this.holdingItemAnimationTicks - 1.0F, 0.0F, 5.0F);
         }

         if (this.isDancing()) {
            this.dancingAnimationTicks++;
            this.spinningAnimationTicks0 = this.spinningAnimationTicks;
            if (this.isSpinning()) {
               this.spinningAnimationTicks++;
            } else {
               this.spinningAnimationTicks--;
            }

            this.spinningAnimationTicks = Mth.clamp(this.spinningAnimationTicks, 0.0F, 15.0F);
         } else {
            this.dancingAnimationTicks = 0.0F;
            this.spinningAnimationTicks = 0.0F;
            this.spinningAnimationTicks0 = 0.0F;
         }
      } else {
         Ticker.tick(this.level(), this.vibrationData, this.vibrationUser);
         if (this.isPanicking()) {
            this.setDancing(false);
         }
      }
   }

   @Override
   public boolean canPickUpLoot() {
      return !this.isOnPickupCooldown() && this.hasItemInHand();
   }

   public boolean hasItemInHand() {
      return !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
   }

   @Override
   protected boolean canDispenserEquipIntoSlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return false;
   }

   private boolean isOnPickupCooldown() {
      return this.getBrain().checkMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT);
   }

   @Override
   protected InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      ItemStack $$3 = this.getItemInHand(InteractionHand.MAIN_HAND);
      if (this.isDancing() && $$2.is(ItemTags.DUPLICATES_ALLAYS) && this.canDuplicate()) {
         this.duplicateAllay();
         this.level().broadcastEntityEvent(this, (byte)18);
         this.level().playSound($$0, this, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 2.0F, 1.0F);
         this.removeInteractionItem($$0, $$2);
         return InteractionResult.SUCCESS;
      } else if ($$3.isEmpty() && !$$2.isEmpty()) {
         ItemStack $$4 = $$2.copyWithCount(1);
         this.setItemInHand(InteractionHand.MAIN_HAND, $$4);
         this.removeInteractionItem($$0, $$2);
         this.level().playSound($$0, this, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.NEUTRAL, 2.0F, 1.0F);
         this.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, $$0.getUUID());
         return InteractionResult.SUCCESS;
      } else if (!$$3.isEmpty() && $$1 == InteractionHand.MAIN_HAND && $$2.isEmpty()) {
         this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
         this.level().playSound($$0, this, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 2.0F, 1.0F);
         this.swing(InteractionHand.MAIN_HAND);

         for (ItemStack $$5 : this.getInventory().removeAllItems()) {
            BehaviorUtils.throwItem(this, $$5, this.position());
         }

         this.getBrain().eraseMemory(MemoryModuleType.LIKED_PLAYER);
         $$0.addItem($$3);
         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract($$0, $$1);
      }
   }

   public void setJukeboxPlaying(BlockPos $$0, boolean $$1) {
      if ($$1) {
         if (!this.isDancing()) {
            this.jukeboxPos = $$0;
            this.setDancing(true);
         }
      } else if ($$0.equals(this.jukeboxPos) || this.jukeboxPos == null) {
         this.jukeboxPos = null;
         this.setDancing(false);
      }
   }

   @Override
   public SimpleContainer getInventory() {
      return this.inventory;
   }

   @Override
   protected Vec3i getPickupReach() {
      return ITEM_PICKUP_REACH;
   }

   @Override
   public boolean wantsToPickUp(ServerLevel $$0, ItemStack $$1) {
      ItemStack $$2 = this.getItemInHand(InteractionHand.MAIN_HAND);
      return !$$2.isEmpty()
         && (Boolean)$$0.getGameRules().get(GameRules.MOB_GRIEFING)
         && this.inventory.canAddItem($$1)
         && this.allayConsidersItemEqual($$2, $$1);
   }

   private boolean allayConsidersItemEqual(ItemStack $$0, ItemStack $$1) {
      return ItemStack.isSameItem($$0, $$1) && !this.hasNonMatchingPotion($$0, $$1);
   }

   private boolean hasNonMatchingPotion(ItemStack $$0, ItemStack $$1) {
      PotionContents $$2 = (PotionContents)$$0.get(DataComponents.POTION_CONTENTS);
      PotionContents $$3 = (PotionContents)$$1.get(DataComponents.POTION_CONTENTS);
      return !Objects.equals($$2, $$3);
   }

   @Override
   protected void pickUpItem(ServerLevel $$0, ItemEntity $$1) {
      InventoryCarrier.pickUpItem($$0, this, this, $$1);
   }

   @Override
   public boolean isFlapping() {
      return !this.onGround();
   }

   @Override
   public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> $$0) {
      if (this.level() instanceof ServerLevel $$1) {
         $$0.accept(this.dynamicVibrationListener, $$1);
         $$0.accept(this.dynamicJukeboxListener, $$1);
      }
   }

   public boolean isDancing() {
      return (Boolean)this.entityData.get(DATA_DANCING);
   }

   public void setDancing(boolean $$0) {
      if (!this.level().isClientSide() && this.isEffectiveAi() && (!$$0 || !this.isPanicking())) {
         this.entityData.set(DATA_DANCING, $$0);
      }
   }

   private boolean shouldStopDancing() {
      return this.jukeboxPos == null
         || !this.jukeboxPos.closerToCenterThan(this.position(), ((GameEvent)GameEvent.JUKEBOX_PLAY.value()).notificationRadius())
         || !this.level().getBlockState(this.jukeboxPos).is(Blocks.JUKEBOX);
   }

   public float getHoldingItemAnimationProgress(float $$0) {
      return Mth.lerp($$0, this.holdingItemAnimationTicks0, this.holdingItemAnimationTicks) / 5.0F;
   }

   public boolean isSpinning() {
      float $$0 = this.dancingAnimationTicks % 55.0F;
      return $$0 < 15.0F;
   }

   public float getSpinningProgress(float $$0) {
      return Mth.lerp($$0, this.spinningAnimationTicks0, this.spinningAnimationTicks) / 15.0F;
   }

   @Override
   public boolean equipmentHasChanged(ItemStack $$0, ItemStack $$1) {
      return !this.allayConsidersItemEqual($$0, $$1);
   }

   @Override
   protected void dropEquipment(ServerLevel $$0) {
      super.dropEquipment($$0);
      this.inventory.removeAllItems().forEach($$1x -> this.spawnAtLocation($$0, $$1x));
      ItemStack $$1 = this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
      if (!$$1.isEmpty() && !EnchantmentHelper.has($$1, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
         this.spawnAtLocation($$0, $$1);
         this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      }
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return false;
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      this.writeInventoryToTag($$0);
      $$0.store("listener", Data.CODEC, this.vibrationData);
      $$0.putLong("DuplicationCooldown", this.duplicationCooldown);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.readInventoryFromTag($$0);
      this.vibrationData = (Data)$$0.read("listener", Data.CODEC).orElseGet(Data::new);
      this.setDuplicationCooldown($$0.getIntOr("DuplicationCooldown", 0));
   }

   @Override
   protected boolean shouldStayCloseToLeashHolder() {
      return false;
   }

   private void updateDuplicationCooldown() {
      if (!this.level().isClientSide() && this.duplicationCooldown > 0L) {
         this.setDuplicationCooldown(this.duplicationCooldown - 1L);
      }
   }

   private void setDuplicationCooldown(long $$0) {
      this.duplicationCooldown = $$0;
      this.entityData.set(DATA_CAN_DUPLICATE, $$0 == 0L);
   }

   private void duplicateAllay() {
      Allay $$0 = net.minecraft.world.entity.EntityType.ALLAY.create(this.level(), net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$0 != null) {
         $$0.snapTo(this.position());
         $$0.setPersistenceRequired();
         $$0.resetDuplicationCooldown();
         this.resetDuplicationCooldown();
         this.level().addFreshEntity($$0);
      }
   }

   private void resetDuplicationCooldown() {
      this.setDuplicationCooldown(6000L);
   }

   private boolean canDuplicate() {
      return (Boolean)this.entityData.get(DATA_CAN_DUPLICATE);
   }

   private void removeInteractionItem(Player $$0, ItemStack $$1) {
      $$1.consume(1, $$0);
   }

   @Override
   public Vec3 getLeashOffset() {
      return new Vec3(0.0, this.getEyeHeight() * 0.6, this.getBbWidth() * 0.1);
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 18) {
         for (int $$1 = 0; $$1 < 3; $$1++) {
            this.spawnHeartParticle();
         }
      } else {
         super.handleEntityEvent($$0);
      }
   }

   private void spawnHeartParticle() {
      double $$0 = this.random.nextGaussian() * 0.02;
      double $$1 = this.random.nextGaussian() * 0.02;
      double $$2 = this.random.nextGaussian() * 0.02;
      this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), $$0, $$1, $$2);
   }

   public Data getVibrationData() {
      return this.vibrationData;
   }

   public User getVibrationUser() {
      return this.vibrationUser;
   }

   class JukeboxListener implements GameEventListener {
      private final PositionSource listenerSource;
      private final int listenerRadius;

      public JukeboxListener(final PositionSource $$0, final int $$1) {
         this.listenerSource = $$0;
         this.listenerRadius = $$1;
      }

      public PositionSource getListenerSource() {
         return this.listenerSource;
      }

      public int getListenerRadius() {
         return this.listenerRadius;
      }

      public boolean handleGameEvent(ServerLevel $$0, Holder<GameEvent> $$1, Context $$2, Vec3 $$3) {
         if ($$1.is(GameEvent.JUKEBOX_PLAY)) {
            Allay.this.setJukeboxPlaying(BlockPos.containing($$3), true);
            return true;
         } else if ($$1.is(GameEvent.JUKEBOX_STOP_PLAY)) {
            Allay.this.setJukeboxPlaying(BlockPos.containing($$3), false);
            return true;
         } else {
            return false;
         }
      }
   }

   class VibrationUser implements User {
      private static final int VIBRATION_EVENT_LISTENER_RANGE = 16;
      private final PositionSource positionSource = new EntityPositionSource(Allay.this, Allay.this.getEyeHeight());

      public int getListenerRadius() {
         return 16;
      }

      public PositionSource getPositionSource() {
         return this.positionSource;
      }

      public boolean canReceiveVibration(ServerLevel $$0, BlockPos $$1, Holder<GameEvent> $$2, Context $$3) {
         if (Allay.this.isNoAi()) {
            return false;
         } else {
            Optional<GlobalPos> $$4 = Allay.this.getBrain().getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
            if ($$4.isEmpty()) {
               return true;
            } else {
               GlobalPos $$5 = $$4.get();
               return $$5.isCloseEnough($$0.dimension(), Allay.this.blockPosition(), 1024) && $$5.pos().equals($$1);
            }
         }
      }

      public void onReceiveVibration(
         ServerLevel $$0,
         BlockPos $$1,
         Holder<GameEvent> $$2,
         net.minecraft.world.entity.Entity $$3,
         net.minecraft.world.entity.Entity $$4,
         float $$5
      ) {
         if ($$2.is(GameEvent.NOTE_BLOCK_PLAY)) {
            AllayAi.hearNoteblock(Allay.this, new BlockPos($$1));
         }
      }

      public TagKey<GameEvent> getListenableEvents() {
         return GameEventTags.ALLAY_CAN_LISTEN;
      }
   }
}
