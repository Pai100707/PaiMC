package net.minecraft.world.entity.animal.sniffer;

import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class Sniffer extends Animal {
   private static final int DIGGING_PARTICLES_DELAY_TICKS = 1700;
   private static final int DIGGING_PARTICLES_DURATION_TICKS = 6000;
   private static final int DIGGING_PARTICLES_AMOUNT = 30;
   private static final int DIGGING_DROP_SEED_OFFSET_TICKS = 120;
   private static final int SNIFFER_BABY_AGE_TICKS = 48000;
   private static final float DIGGING_BB_HEIGHT_OFFSET = 0.4F;
   private static final net.minecraft.world.entity.EntityDimensions DIGGING_DIMENSIONS = net.minecraft.world.entity.EntityDimensions.scalable(
         net.minecraft.world.entity.EntityType.SNIFFER.getWidth(), net.minecraft.world.entity.EntityType.SNIFFER.getHeight() - 0.4F
      )
      .withEyeHeight(0.81F);
   private static final EntityDataAccessor<Sniffer.State> DATA_STATE = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.SNIFFER_STATE);
   private static final EntityDataAccessor<Integer> DATA_DROP_SEED_AT_TICK = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.INT);
   public final net.minecraft.world.entity.AnimationState feelingHappyAnimationState = new net.minecraft.world.entity.AnimationState();
   public final net.minecraft.world.entity.AnimationState scentingAnimationState = new net.minecraft.world.entity.AnimationState();
   public final net.minecraft.world.entity.AnimationState sniffingAnimationState = new net.minecraft.world.entity.AnimationState();
   public final net.minecraft.world.entity.AnimationState diggingAnimationState = new net.minecraft.world.entity.AnimationState();
   public final net.minecraft.world.entity.AnimationState risingAnimationState = new net.minecraft.world.entity.AnimationState();

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes().add(Attributes.MOVEMENT_SPEED, 0.1F).add(Attributes.MAX_HEALTH, 14.0);
   }

   public Sniffer(net.minecraft.world.entity.EntityType<? extends Animal> $$0, Level $$1) {
      super($$0, $$1);
      this.getNavigation().setCanFloat(true);
      this.setPathfindingMalus(PathType.WATER, -1.0F);
      this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0F);
      this.setPathfindingMalus(PathType.DAMAGE_CAUTIOUS, -1.0F);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_STATE, Sniffer.State.IDLING);
      $$0.define(DATA_DROP_SEED_AT_TICK, 0);
   }

   @Override
   public void onPathfindingStart() {
      super.onPathfindingStart();
      if (this.isOnFire() || this.isInWater()) {
         this.setPathfindingMalus(PathType.WATER, 0.0F);
      }
   }

   @Override
   public void onPathfindingDone() {
      this.setPathfindingMalus(PathType.WATER, -1.0F);
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose $$0) {
      return this.getState() == Sniffer.State.DIGGING ? DIGGING_DIMENSIONS.scale(this.getAgeScale()) : super.getDefaultDimensions($$0);
   }

   public boolean isSearching() {
      return this.getState() == Sniffer.State.SEARCHING;
   }

   public boolean isTempted() {
      return this.brain.getMemory(MemoryModuleType.IS_TEMPTED).orElse(false);
   }

   public boolean canSniff() {
      return !this.isTempted() && !this.isPanicking() && !this.isInWater() && !this.isInLove() && this.onGround() && !this.isPassenger() && !this.isLeashed();
   }

   public boolean canPlayDiggingSound() {
      return this.getState() == Sniffer.State.DIGGING || this.getState() == Sniffer.State.SEARCHING;
   }

   private BlockPos getHeadBlock() {
      Vec3 $$0 = this.getHeadPosition();
      return BlockPos.containing($$0.x(), this.getY() + 0.2F, $$0.z());
   }

   private Vec3 getHeadPosition() {
      return this.position().add(this.getForward().scale(2.25));
   }

   @Override
   public boolean supportQuadLeash() {
      return true;
   }

   @Override
   public Vec3[] getQuadLeashOffsets() {
      return net.minecraft.world.entity.Leashable.createQuadLeashOffsets(this, -0.01, 0.63, 0.38, 1.15);
   }

   private Sniffer.State getState() {
      return (Sniffer.State)this.entityData.get(DATA_STATE);
   }

   private Sniffer setState(Sniffer.State $$0) {
      this.entityData.set(DATA_STATE, $$0);
      return this;
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      if (DATA_STATE.equals($$0)) {
         Sniffer.State $$1 = this.getState();
         this.resetAnimations();
         switch ($$1) {
            case FEELING_HAPPY:
               this.feelingHappyAnimationState.startIfStopped(this.tickCount);
               break;
            case SCENTING:
               this.scentingAnimationState.startIfStopped(this.tickCount);
               break;
            case SNIFFING:
               this.sniffingAnimationState.startIfStopped(this.tickCount);
            case SEARCHING:
            default:
               break;
            case DIGGING:
               this.diggingAnimationState.startIfStopped(this.tickCount);
               break;
            case RISING:
               this.risingAnimationState.startIfStopped(this.tickCount);
         }

         this.refreshDimensions();
      }

      super.onSyncedDataUpdated($$0);
   }

   private void resetAnimations() {
      this.diggingAnimationState.stop();
      this.sniffingAnimationState.stop();
      this.risingAnimationState.stop();
      this.feelingHappyAnimationState.stop();
      this.scentingAnimationState.stop();
   }

   public Sniffer transitionTo(Sniffer.State $$0) {
      switch ($$0) {
         case IDLING:
            this.setState(Sniffer.State.IDLING);
            break;
         case FEELING_HAPPY:
            this.playSound(SoundEvents.SNIFFER_HAPPY, 1.0F, 1.0F);
            this.setState(Sniffer.State.FEELING_HAPPY);
            break;
         case SCENTING:
            this.setState(Sniffer.State.SCENTING).onScentingStart();
            break;
         case SNIFFING:
            this.playSound(SoundEvents.SNIFFER_SNIFFING, 1.0F, 1.0F);
            this.setState(Sniffer.State.SNIFFING);
            break;
         case SEARCHING:
            this.setState(Sniffer.State.SEARCHING);
            break;
         case DIGGING:
            this.setState(Sniffer.State.DIGGING).onDiggingStart();
            break;
         case RISING:
            this.playSound(SoundEvents.SNIFFER_DIGGING_STOP, 1.0F, 1.0F);
            this.setState(Sniffer.State.RISING);
      }

      return this;
   }

   private Sniffer onScentingStart() {
      this.playSound(SoundEvents.SNIFFER_SCENTING, 1.0F, this.isBaby() ? 1.3F : 1.0F);
      return this;
   }

   private Sniffer onDiggingStart() {
      this.entityData.set(DATA_DROP_SEED_AT_TICK, this.tickCount + 120);
      this.level().broadcastEntityEvent(this, (byte)63);
      return this;
   }

   public Sniffer onDiggingComplete(boolean $$0) {
      if ($$0) {
         this.storeExploredPosition(this.getOnPos());
      }

      return this;
   }

   Optional<BlockPos> calculateDigPosition() {
      return IntStream.range(0, 5)
         .mapToObj($$0 -> LandRandomPos.getPos(this, 10 + 2 * $$0, 3))
         .filter(Objects::nonNull)
         .<BlockPos>map(BlockPos::containing)
         .filter($$0 -> this.level().getWorldBorder().isWithinBounds($$0))
         .<BlockPos>map(BlockPos::below)
         .filter(this::canDig)
         .findFirst();
   }

   boolean canDig() {
      return !this.isPanicking()
         && !this.isTempted()
         && !this.isBaby()
         && !this.isInWater()
         && this.onGround()
         && !this.isPassenger()
         && this.canDig(this.getHeadBlock().below());
   }

   private boolean canDig(BlockPos $$0) {
      return this.level().getBlockState($$0).is(BlockTags.SNIFFER_DIGGABLE_BLOCK)
         && this.getExploredPositions().noneMatch($$1 -> GlobalPos.of(this.level().dimension(), $$0).equals($$1))
         && Optional.ofNullable(this.getNavigation().createPath($$0, 1)).<Boolean>map(Path::canReach).orElse(false);
   }

   private void dropSeed() {
      if (this.level() instanceof ServerLevel $$0 && (Integer)this.entityData.get(DATA_DROP_SEED_AT_TICK) == this.tickCount) {
         BlockPos $$2 = this.getHeadBlock();
         this.dropFromGiftLootTable($$0, BuiltInLootTables.SNIFFER_DIGGING, ($$1x, $$2x) -> {
            ItemEntity $$3 = new ItemEntity(this.level(), $$2.getX(), $$2.getY(), $$2.getZ(), $$2x);
            $$3.setDefaultPickUpDelay();
            $$1x.addFreshEntity($$3);
         });
         this.playSound(SoundEvents.SNIFFER_DROP_SEED, 1.0F, 1.0F);
      }
   }

   private Sniffer emitDiggingParticles(net.minecraft.world.entity.AnimationState $$0) {
      boolean $$1 = $$0.getTimeInMillis(this.tickCount) > 1700L && $$0.getTimeInMillis(this.tickCount) < 6000L;
      if ($$1) {
         BlockPos $$2 = this.getHeadBlock();
         BlockState $$3 = this.level().getBlockState($$2.below());
         if ($$3.getRenderShape() != RenderShape.INVISIBLE) {
            for (int $$4 = 0; $$4 < 30; $$4++) {
               Vec3 $$5 = Vec3.atCenterOf($$2).add(0.0, -0.65F, 0.0);
               this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, $$3), $$5.x, $$5.y, $$5.z, 0.0, 0.0, 0.0);
            }

            if (this.tickCount % 10 == 0) {
               this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), $$3.getSoundType().getHitSound(), this.getSoundSource(), 0.5F, 0.5F, false);
            }
         }
      }

      if (this.tickCount % 10 == 0) {
         this.level().gameEvent(GameEvent.ENTITY_ACTION, this.getHeadBlock(), Context.of(this));
      }

      return this;
   }

   private Sniffer storeExploredPosition(BlockPos $$0) {
      List<GlobalPos> $$1 = this.getExploredPositions().limit(20L).collect(Collectors.toList());
      $$1.add(0, GlobalPos.of(this.level().dimension(), $$0));
      this.getBrain().setMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS, $$1);
      return this;
   }

   private Stream<GlobalPos> getExploredPositions() {
      return this.getBrain().getMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS).stream().flatMap(Collection::stream);
   }

   @Override
   public void jumpFromGround() {
      super.jumpFromGround();
      double $$0 = this.moveControl.getSpeedModifier();
      if ($$0 > 0.0) {
         double $$1 = this.getDeltaMovement().horizontalDistanceSqr();
         if ($$1 < 0.01) {
            this.moveRelative(0.1F, new Vec3(0.0, 0.0, 1.0));
         }
      }
   }

   @Override
   public void spawnChildFromBreeding(ServerLevel $$0, Animal $$1) {
      ItemStack $$2 = new ItemStack(Items.SNIFFER_EGG);
      ItemEntity $$3 = new ItemEntity($$0, this.position().x(), this.position().y(), this.position().z(), $$2);
      $$3.setDefaultPickUpDelay();
      this.finalizeSpawnChildFromBreeding($$0, $$1, null);
      this.playSound(SoundEvents.SNIFFER_EGG_PLOP, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.5F);
      $$0.addFreshEntity($$3);
   }

   @Override
   public void die(DamageSource $$0) {
      this.transitionTo(Sniffer.State.IDLING);
      super.die($$0);
   }

   @Override
   public void tick() {
      switch (this.getState()) {
         case SEARCHING:
            this.playSearchingSound();
            break;
         case DIGGING:
            this.emitDiggingParticles(this.diggingAnimationState).dropSeed();
      }

      super.tick();
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      boolean $$3 = this.isFood($$2);
      InteractionResult $$4 = super.mobInteract($$0, $$1);
      if ($$4.consumesAction() && $$3) {
         this.playEatingSound();
      }

      return $$4;
   }

   @Override
   protected void playEatingSound() {
      this.level().playSound(null, this, SoundEvents.SNIFFER_EAT, SoundSource.NEUTRAL, 1.0F, Mth.randomBetween(this.level().random, 0.8F, 1.2F));
   }

   private void playSearchingSound() {
      if (this.level().isClientSide() && this.tickCount % 20 == 0) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.SNIFFER_SEARCHING, this.getSoundSource(), 1.0F, 1.0F, false);
      }
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.SNIFFER_STEP, 0.15F, 1.0F);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return Set.of(Sniffer.State.DIGGING, Sniffer.State.SEARCHING).contains(this.getState()) ? null : SoundEvents.SNIFFER_IDLE;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.SNIFFER_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.SNIFFER_DEATH;
   }

   @Override
   public int getMaxHeadYRot() {
      return 50;
   }

   @Override
   public void setBaby(boolean $$0) {
      this.setAge($$0 ? -48000 : 0);
   }

   @Override
   public net.minecraft.world.entity.AgeableMob getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      return net.minecraft.world.entity.EntityType.SNIFFER.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
   }

   @Override
   public boolean canMate(Animal $$0) {
      if (!($$0 instanceof Sniffer $$1)) {
         return false;
      } else {
         Set<Sniffer.State> $$2 = Set.of(Sniffer.State.IDLING, Sniffer.State.SCENTING, Sniffer.State.FEELING_HAPPY);
         return $$2.contains(this.getState()) && $$2.contains($$1.getState()) && super.canMate($$0);
      }
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.SNIFFER_FOOD);
   }

   @Override
   protected Brain<?> makeBrain(Dynamic<?> $$0) {
      return SnifferAi.makeBrain(this.brainProvider().makeBrain($$0));
   }

   @Override
   public Brain<Sniffer> getBrain() {
      return (Brain<Sniffer>)super.getBrain();
   }

   @Override
   protected Brain.Provider<Sniffer> brainProvider() {
      return Brain.provider(SnifferAi.MEMORY_TYPES, SnifferAi.SENSOR_TYPES);
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      ProfilerFiller $$1 = Profiler.get();
      $$1.push("snifferBrain");
      this.getBrain().tick($$0, this);
      $$1.popPush("snifferActivityUpdate");
      SnifferAi.updateActivity(this);
      $$1.pop();
      super.customServerAiStep($$0);
   }

   public static enum State {
      IDLING(0),
      FEELING_HAPPY(1),
      SCENTING(2),
      SNIFFING(3),
      SEARCHING(4),
      DIGGING(5),
      RISING(6);

      public static final IntFunction<Sniffer.State> BY_ID = ByIdMap.continuous(Sniffer.State::id, values(), OutOfBoundsStrategy.ZERO);
      public static final StreamCodec<ByteBuf, Sniffer.State> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Sniffer.State::id);
      private final int id;

      private State(final int $$0) {
         this.id = $$0;
      }

      public int id() {
         return this.id;
      }
   }
}
