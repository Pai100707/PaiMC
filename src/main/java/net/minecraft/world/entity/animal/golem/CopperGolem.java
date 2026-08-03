package net.minecraft.world.entity.animal.golem;

import com.mojang.serialization.Dynamic;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CopperGolem extends AbstractGolem implements net.minecraft.world.entity.ContainerUser, net.minecraft.world.entity.Shearable {
   private static final long IGNORE_WEATHERING_TICK = -2L;
   private static final long UNSET_WEATHERING_TICK = -1L;
   private static final int WEATHERING_TICK_FROM = 504000;
   private static final int WEATHERING_TICK_TO = 552000;
   private static final int SPIN_ANIMATION_MIN_COOLDOWN = 200;
   private static final int SPIN_ANIMATION_MAX_COOLDOWN = 240;
   private static final float SPIN_SOUND_TIME_INTERVAL_OFFSET = 10.0F;
   private static final float TURN_TO_STATUE_CHANCE = 0.0058F;
   private static final int SPAWN_COOLDOWN_MIN = 60;
   private static final int SPAWN_COOLDOWN_MAX = 100;
   private static final EntityDataAccessor<WeatherState> DATA_WEATHER_STATE = SynchedEntityData.defineId(
      CopperGolem.class, EntityDataSerializers.WEATHERING_COPPER_STATE
   );
   private static final EntityDataAccessor<CopperGolemState> COPPER_GOLEM_STATE = SynchedEntityData.defineId(
      CopperGolem.class, EntityDataSerializers.COPPER_GOLEM_STATE
   );
   @Nullable
   private BlockPos openedChestPos;
   @Nullable
   private UUID lastLightningBoltUUID;
   private long nextWeatheringTick = -1L;
   private int idleAnimationStartTick = 0;
   private final net.minecraft.world.entity.AnimationState idleAnimationState = new net.minecraft.world.entity.AnimationState();
   private final net.minecraft.world.entity.AnimationState interactionGetItemAnimationState = new net.minecraft.world.entity.AnimationState();
   private final net.minecraft.world.entity.AnimationState interactionGetNoItemAnimationState = new net.minecraft.world.entity.AnimationState();
   private final net.minecraft.world.entity.AnimationState interactionDropItemAnimationState = new net.minecraft.world.entity.AnimationState();
   private final net.minecraft.world.entity.AnimationState interactionDropNoItemAnimationState = new net.minecraft.world.entity.AnimationState();
   public static final net.minecraft.world.entity.EquipmentSlot EQUIPMENT_SLOT_ANTENNA = net.minecraft.world.entity.EquipmentSlot.SADDLE;

   public CopperGolem(net.minecraft.world.entity.EntityType<? extends AbstractGolem> $$0, Level $$1) {
      super($$0, $$1);
      this.getNavigation().setRequiredPathLength(48.0F);
      this.getNavigation().setCanOpenDoors(true);
      this.setPersistenceRequired();
      this.setState(CopperGolemState.IDLE);
      this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(PathType.DANGER_OTHER, 16.0F);
      this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
      this.getBrain().setMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, this.getRandom().nextInt(60, 100));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.2F)
         .add(Attributes.STEP_HEIGHT, 1.0)
         .add(Attributes.MAX_HEALTH, 12.0);
   }

   public CopperGolemState getState() {
      return (CopperGolemState)this.entityData.get(COPPER_GOLEM_STATE);
   }

   public void setState(CopperGolemState $$0) {
      this.entityData.set(COPPER_GOLEM_STATE, $$0);
   }

   public WeatherState getWeatherState() {
      return (WeatherState)this.entityData.get(DATA_WEATHER_STATE);
   }

   public void setWeatherState(WeatherState $$0) {
      this.entityData.set(DATA_WEATHER_STATE, $$0);
   }

   public void setOpenedChestPos(BlockPos $$0) {
      this.openedChestPos = $$0;
   }

   public void clearOpenedChestPos() {
      this.openedChestPos = null;
   }

   public net.minecraft.world.entity.AnimationState getIdleAnimationState() {
      return this.idleAnimationState;
   }

   public net.minecraft.world.entity.AnimationState getInteractionGetItemAnimationState() {
      return this.interactionGetItemAnimationState;
   }

   public net.minecraft.world.entity.AnimationState getInteractionGetNoItemAnimationState() {
      return this.interactionGetNoItemAnimationState;
   }

   public net.minecraft.world.entity.AnimationState getInteractionDropItemAnimationState() {
      return this.interactionDropItemAnimationState;
   }

   public net.minecraft.world.entity.AnimationState getInteractionDropNoItemAnimationState() {
      return this.interactionDropNoItemAnimationState;
   }

   @Override
   protected Brain.Provider<CopperGolem> brainProvider() {
      return CopperGolemAi.brainProvider();
   }

   @Override
   protected Brain<?> makeBrain(Dynamic<?> $$0) {
      return CopperGolemAi.makeBrain(this.brainProvider().makeBrain($$0));
   }

   @Override
   public Brain<CopperGolem> getBrain() {
      return (Brain<CopperGolem>)super.getBrain();
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_WEATHER_STATE, WeatherState.UNAFFECTED);
      $$0.define(COPPER_GOLEM_STATE, CopperGolemState.IDLE);
   }

   @Override
   public void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putLong("next_weather_age", this.nextWeatheringTick);
      $$0.store("weather_state", WeatherState.CODEC, this.getWeatherState());
   }

   @Override
   public void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.nextWeatheringTick = $$0.getLongOr("next_weather_age", -1L);
      this.setWeatherState($$0.read("weather_state", WeatherState.CODEC).orElse(WeatherState.UNAFFECTED));
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      ProfilerFiller $$1 = Profiler.get();
      $$1.push("copperGolemBrain");
      this.getBrain().tick($$0, this);
      $$1.pop();
      $$1.push("copperGolemActivityUpdate");
      CopperGolemAi.updateActivity(this);
      $$1.pop();
      super.customServerAiStep($$0);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         if (!this.isNoAi()) {
            this.setupAnimationStates();
         }
      } else {
         this.updateWeathering((ServerLevel)this.level(), this.level().getRandom(), this.level().getGameTime());
      }
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if ($$2.isEmpty()) {
         ItemStack $$3 = this.getMainHandItem();
         if (!$$3.isEmpty()) {
            BehaviorUtils.throwItem(this, $$3, $$0.position());
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
         }
      }

      Level $$4 = this.level();
      if ($$2.is(Items.SHEARS) && this.readyForShearing()) {
         if ($$4 instanceof ServerLevel $$5) {
            this.shear($$5, SoundSource.PLAYERS, $$2);
            this.gameEvent(GameEvent.SHEAR, $$0);
            $$2.hurtAndBreak(1, $$0, $$1);
         }

         return InteractionResult.SUCCESS;
      } else if ($$4.isClientSide()) {
         return InteractionResult.PASS;
      } else if ($$2.is(Items.HONEYCOMB) && this.nextWeatheringTick != -2L) {
         $$4.levelEvent(this, 3003, this.blockPosition(), 0);
         this.nextWeatheringTick = -2L;
         this.usePlayerItem($$0, $$1, $$2);
         return InteractionResult.SUCCESS_SERVER;
      } else if ($$2.is(ItemTags.AXES) && this.nextWeatheringTick == -2L) {
         $$4.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
         $$4.levelEvent(this, 3004, this.blockPosition(), 0);
         this.nextWeatheringTick = -1L;
         $$2.hurtAndBreak(1, $$0, $$1.asEquipmentSlot());
         return InteractionResult.SUCCESS_SERVER;
      } else {
         if ($$2.is(ItemTags.AXES)) {
            WeatherState $$6 = this.getWeatherState();
            if ($$6 != WeatherState.UNAFFECTED) {
               $$4.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
               $$4.levelEvent(this, 3005, this.blockPosition(), 0);
               this.nextWeatheringTick = -1L;
               this.entityData.set(DATA_WEATHER_STATE, $$6.previous(), true);
               $$2.hurtAndBreak(1, $$0, $$1.asEquipmentSlot());
               return InteractionResult.SUCCESS_SERVER;
            }
         }

         return super.mobInteract($$0, $$1);
      }
   }

   private void updateWeathering(ServerLevel $$0, RandomSource $$1, long $$2) {
      if (this.nextWeatheringTick != -2L) {
         if (this.nextWeatheringTick == -1L) {
            this.nextWeatheringTick = $$2 + $$1.nextIntBetweenInclusive(504000, 552000);
         } else {
            WeatherState $$3 = (WeatherState)this.entityData.get(DATA_WEATHER_STATE);
            boolean $$4 = $$3.equals(WeatherState.OXIDIZED);
            if ($$2 >= this.nextWeatheringTick && !$$4) {
               WeatherState $$5 = $$3.next();
               boolean $$6 = $$5.equals(WeatherState.OXIDIZED);
               this.setWeatherState($$5);
               this.nextWeatheringTick = $$6 ? 0L : this.nextWeatheringTick + $$1.nextIntBetweenInclusive(504000, 552000);
            }

            if ($$4 && this.canTurnToStatue($$0)) {
               this.turnToStatue($$0);
            }
         }
      }
   }

   private boolean canTurnToStatue(Level $$0) {
      return $$0.getBlockState(this.blockPosition()).isAir() && $$0.random.nextFloat() <= 0.0058F;
   }

   private void turnToStatue(ServerLevel $$0) {
      BlockPos $$1 = this.blockPosition();
      $$0.setBlock(
         $$1,
         (BlockState)((BlockState)Blocks.OXIDIZED_COPPER_GOLEM_STATUE
               .defaultBlockState()
               .setValue(
                  CopperGolemStatueBlock.POSE,
                  net.minecraft.world.level.block.CopperGolemStatueBlock.Pose.values()[this.random
                     .nextInt(0, net.minecraft.world.level.block.CopperGolemStatueBlock.Pose.values().length)]
               ))
            .setValue(CopperGolemStatueBlock.FACING, Direction.fromYRot(this.getYRot())),
         3
      );
      if ($$0.getBlockEntity($$1) instanceof CopperGolemStatueBlockEntity $$2) {
         $$2.createStatue(this);
         this.dropPreservedEquipment($$0);
         this.discard();
         this.playSound(SoundEvents.COPPER_GOLEM_BECOME_STATUE);
         if (this.isLeashed()) {
            if ((Boolean)$$0.getGameRules().get(GameRules.ENTITY_DROPS)) {
               this.dropLeash();
            } else {
               this.removeLeash();
            }
         }
      }
   }

   private void setupAnimationStates() {
      switch (this.getState()) {
         case IDLE:
            this.interactionGetNoItemAnimationState.stop();
            this.interactionGetItemAnimationState.stop();
            this.interactionDropItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.stop();
            if (this.idleAnimationStartTick == this.tickCount) {
               this.idleAnimationState.start(this.tickCount);
            } else if (this.idleAnimationStartTick == 0) {
               this.idleAnimationStartTick = this.tickCount + this.random.nextInt(200, 240);
            }

            if (this.tickCount == this.idleAnimationStartTick + 10.0F) {
               this.playHeadSpinSound();
               this.idleAnimationStartTick = 0;
            }
            break;
         case GETTING_ITEM:
            this.idleAnimationState.stop();
            this.idleAnimationStartTick = 0;
            this.interactionGetNoItemAnimationState.stop();
            this.interactionDropItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.stop();
            this.interactionGetItemAnimationState.startIfStopped(this.tickCount);
            break;
         case GETTING_NO_ITEM:
            this.idleAnimationState.stop();
            this.idleAnimationStartTick = 0;
            this.interactionGetItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.stop();
            this.interactionDropItemAnimationState.stop();
            this.interactionGetNoItemAnimationState.startIfStopped(this.tickCount);
            break;
         case DROPPING_ITEM:
            this.idleAnimationState.stop();
            this.idleAnimationStartTick = 0;
            this.interactionGetItemAnimationState.stop();
            this.interactionGetNoItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.stop();
            this.interactionDropItemAnimationState.startIfStopped(this.tickCount);
            break;
         case DROPPING_NO_ITEM:
            this.idleAnimationState.stop();
            this.idleAnimationStartTick = 0;
            this.interactionGetItemAnimationState.stop();
            this.interactionGetNoItemAnimationState.stop();
            this.interactionDropItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.startIfStopped(this.tickCount);
      }
   }

   public void spawn(WeatherState $$0) {
      this.setWeatherState($$0);
      this.playSpawnSound();
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      this.playSpawnSound();
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   public void playSpawnSound() {
      this.playSound(SoundEvents.COPPER_GOLEM_SPAWN);
   }

   private void playHeadSpinSound() {
      if (!this.isSilent()) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSpinHeadSound(), this.getSoundSource(), 1.0F, 1.0F, false);
      }
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).hurtSound();
   }

   @Override
   protected SoundEvent getDeathSound() {
      return CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).deathSound();
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).stepSound(), 1.0F, 1.0F);
   }

   private SoundEvent getSpinHeadSound() {
      return CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).spinHeadSound();
   }

   @Override
   public Vec3 getLeashOffset() {
      return new Vec3(0.0, 0.75F * this.getEyeHeight(), 0.0);
   }

   @Override
   public boolean hasContainerOpen(ContainerOpenersCounter $$0, BlockPos $$1) {
      if (this.openedChestPos == null) {
         return false;
      } else {
         BlockState $$2 = this.level().getBlockState(this.openedChestPos);
         return this.openedChestPos.equals($$1)
            || $$2.getBlock() instanceof ChestBlock
               && $$2.getValue(ChestBlock.TYPE) != ChestType.SINGLE
               && ChestBlock.getConnectedBlockPos(this.openedChestPos, $$2).equals($$1);
      }
   }

   @Override
   public double getContainerInteractionRange() {
      return 3.0;
   }

   @Override
   public void shear(ServerLevel $$0, SoundSource $$1, ItemStack $$2) {
      $$0.playSound(null, this, SoundEvents.COPPER_GOLEM_SHEAR, $$1, 1.0F, 1.0F);
      ItemStack $$3 = this.getItemBySlot(EQUIPMENT_SLOT_ANTENNA);
      this.setItemSlot(EQUIPMENT_SLOT_ANTENNA, ItemStack.EMPTY);
      this.spawnAtLocation($$0, $$3, 1.5F);
   }

   @Override
   public boolean readyForShearing() {
      return this.isAlive() && this.getItemBySlot(EQUIPMENT_SLOT_ANTENNA).is(ItemTags.SHEARABLE_FROM_COPPER_GOLEM);
   }

   @Override
   protected void dropEquipment(ServerLevel $$0) {
      super.dropEquipment($$0);
      this.dropPreservedEquipment($$0);
   }

   @Override
   protected void actuallyHurt(ServerLevel $$0, DamageSource $$1, float $$2) {
      super.actuallyHurt($$0, $$1, $$2);
      this.setState(CopperGolemState.IDLE);
   }

   @Override
   public void thunderHit(ServerLevel $$0, net.minecraft.world.entity.LightningBolt $$1) {
      super.thunderHit($$0, $$1);
      UUID $$2 = $$1.getUUID();
      if (!$$2.equals(this.lastLightningBoltUUID)) {
         this.lastLightningBoltUUID = $$2;
         WeatherState $$3 = this.getWeatherState();
         if ($$3 != WeatherState.UNAFFECTED) {
            this.nextWeatheringTick = -1L;
            this.entityData.set(DATA_WEATHER_STATE, $$3.previous(), true);
         }
      }
   }
}
