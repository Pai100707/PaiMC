package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public interface VibrationSystem {
   List<ResourceKey<GameEvent>> RESONANCE_EVENTS = List.of(
      GameEvent.RESONATE_1.key(),
      GameEvent.RESONATE_2.key(),
      GameEvent.RESONATE_3.key(),
      GameEvent.RESONATE_4.key(),
      GameEvent.RESONATE_5.key(),
      GameEvent.RESONATE_6.key(),
      GameEvent.RESONATE_7.key(),
      GameEvent.RESONATE_8.key(),
      GameEvent.RESONATE_9.key(),
      GameEvent.RESONATE_10.key(),
      GameEvent.RESONATE_11.key(),
      GameEvent.RESONATE_12.key(),
      GameEvent.RESONATE_13.key(),
      GameEvent.RESONATE_14.key(),
      GameEvent.RESONATE_15.key()
   );
   int NO_VIBRATION_FREQUENCY = 0;
   ToIntFunction<ResourceKey<GameEvent>> VIBRATION_FREQUENCY_FOR_EVENT = (ToIntFunction<ResourceKey<GameEvent>>)Util.make(
      new Reference2IntOpenHashMap(), $$0 -> {
         $$0.defaultReturnValue(0);
         $$0.put(GameEvent.STEP.key(), 1);
         $$0.put(GameEvent.SWIM.key(), 1);
         $$0.put(GameEvent.FLAP.key(), 1);
         $$0.put(GameEvent.PROJECTILE_LAND.key(), 2);
         $$0.put(GameEvent.HIT_GROUND.key(), 2);
         $$0.put(GameEvent.SPLASH.key(), 2);
         $$0.put(GameEvent.ITEM_INTERACT_FINISH.key(), 3);
         $$0.put(GameEvent.PROJECTILE_SHOOT.key(), 3);
         $$0.put(GameEvent.INSTRUMENT_PLAY.key(), 3);
         $$0.put(GameEvent.ENTITY_ACTION.key(), 4);
         $$0.put(GameEvent.ELYTRA_GLIDE.key(), 4);
         $$0.put(GameEvent.UNEQUIP.key(), 4);
         $$0.put(GameEvent.ENTITY_DISMOUNT.key(), 5);
         $$0.put(GameEvent.EQUIP.key(), 5);
         $$0.put(GameEvent.ENTITY_INTERACT.key(), 6);
         $$0.put(GameEvent.SHEAR.key(), 6);
         $$0.put(GameEvent.ENTITY_MOUNT.key(), 6);
         $$0.put(GameEvent.ENTITY_DAMAGE.key(), 7);
         $$0.put(GameEvent.DRINK.key(), 8);
         $$0.put(GameEvent.EAT.key(), 8);
         $$0.put(GameEvent.CONTAINER_CLOSE.key(), 9);
         $$0.put(GameEvent.BLOCK_CLOSE.key(), 9);
         $$0.put(GameEvent.BLOCK_DEACTIVATE.key(), 9);
         $$0.put(GameEvent.BLOCK_DETACH.key(), 9);
         $$0.put(GameEvent.CONTAINER_OPEN.key(), 10);
         $$0.put(GameEvent.BLOCK_OPEN.key(), 10);
         $$0.put(GameEvent.BLOCK_ACTIVATE.key(), 10);
         $$0.put(GameEvent.BLOCK_ATTACH.key(), 10);
         $$0.put(GameEvent.PRIME_FUSE.key(), 10);
         $$0.put(GameEvent.NOTE_BLOCK_PLAY.key(), 10);
         $$0.put(GameEvent.BLOCK_CHANGE.key(), 11);
         $$0.put(GameEvent.BLOCK_DESTROY.key(), 12);
         $$0.put(GameEvent.FLUID_PICKUP.key(), 12);
         $$0.put(GameEvent.BLOCK_PLACE.key(), 13);
         $$0.put(GameEvent.FLUID_PLACE.key(), 13);
         $$0.put(GameEvent.ENTITY_PLACE.key(), 14);
         $$0.put(GameEvent.LIGHTNING_STRIKE.key(), 14);
         $$0.put(GameEvent.TELEPORT.key(), 14);
         $$0.put(GameEvent.ENTITY_DIE.key(), 15);
         $$0.put(GameEvent.EXPLODE.key(), 15);

         for (int $$1 = 1; $$1 <= 15; $$1++) {
            $$0.put(getResonanceEventByFrequency($$1), $$1);
         }
      }
   );

   VibrationSystem.Data getVibrationData();

   VibrationSystem.User getVibrationUser();

   static int getGameEventFrequency(Holder<GameEvent> $$0) {
      return $$0.unwrapKey().map(VibrationSystem::getGameEventFrequency).orElse(0);
   }

   static int getGameEventFrequency(ResourceKey<GameEvent> $$0) {
      return VIBRATION_FREQUENCY_FOR_EVENT.applyAsInt($$0);
   }

   static ResourceKey<GameEvent> getResonanceEventByFrequency(int $$0) {
      return RESONANCE_EVENTS.get($$0 - 1);
   }

   static int getRedstoneStrengthForDistance(float $$0, int $$1) {
      double $$2 = 15.0 / $$1;
      return Math.max(1, 15 - Mth.floor($$2 * $$0));
   }

   public static final class Data {
      public static Codec<VibrationSystem.Data> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               VibrationInfo.CODEC.lenientOptionalFieldOf("event").forGetter($$0x -> Optional.ofNullable($$0x.currentVibration)),
               VibrationSelector.CODEC.fieldOf("selector").forGetter(VibrationSystem.Data::getSelectionStrategy),
               ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(VibrationSystem.Data::getTravelTimeInTicks)
            )
            .apply($$0, ($$0x, $$1, $$2) -> new VibrationSystem.Data((VibrationInfo)$$0x.orElse(null), $$1, $$2, true))
      );
      public static final String NBT_TAG_KEY = "listener";
      
      VibrationInfo currentVibration;
      private int travelTimeInTicks;
      final VibrationSelector selectionStrategy;
      private boolean reloadVibrationParticle;

      private Data(VibrationInfo $$0, VibrationSelector $$1, int $$2, boolean $$3) {
         this.currentVibration = $$0;
         this.travelTimeInTicks = $$2;
         this.selectionStrategy = $$1;
         this.reloadVibrationParticle = $$3;
      }

      public Data() {
         this(null, new VibrationSelector(), 0, false);
      }

      public VibrationSelector getSelectionStrategy() {
         return this.selectionStrategy;
      }

      
      public VibrationInfo getCurrentVibration() {
         return this.currentVibration;
      }

      public void setCurrentVibration(VibrationInfo $$0) {
         this.currentVibration = $$0;
      }

      public int getTravelTimeInTicks() {
         return this.travelTimeInTicks;
      }

      public void setTravelTimeInTicks(int $$0) {
         this.travelTimeInTicks = $$0;
      }

      public void decrementTravelTime() {
         this.travelTimeInTicks = Math.max(0, this.travelTimeInTicks - 1);
      }

      public boolean shouldReloadVibrationParticle() {
         return this.reloadVibrationParticle;
      }

      public void setReloadVibrationParticle(boolean $$0) {
         this.reloadVibrationParticle = $$0;
      }
   }

   public static class Listener implements GameEventListener {
      private final VibrationSystem system;

      public Listener(VibrationSystem $$0) {
         this.system = $$0;
      }

      @Override
      public PositionSource getListenerSource() {
         return this.system.getVibrationUser().getPositionSource();
      }

      @Override
      public int getListenerRadius() {
         return this.system.getVibrationUser().getListenerRadius();
      }

      @Override
      public boolean handleGameEvent(ServerLevel $$0, Holder<GameEvent> $$1, GameEvent.Context $$2, Vec3 $$3) {
         VibrationSystem.Data $$4 = this.system.getVibrationData();
         VibrationSystem.User $$5 = this.system.getVibrationUser();
         if ($$4.getCurrentVibration() != null) {
            return false;
         } else if (!$$5.isValidVibration($$1, $$2)) {
            return false;
         } else {
            Optional<Vec3> $$6 = $$5.getPositionSource().getPosition($$0);
            if ($$6.isEmpty()) {
               return false;
            } else {
               Vec3 $$7 = $$6.get();
               if (!$$5.canReceiveVibration($$0, BlockPos.containing($$3), $$1, $$2)) {
                  return false;
               } else if (isOccluded($$0, $$3, $$7)) {
                  return false;
               } else {
                  this.scheduleVibration($$0, $$4, $$1, $$2, $$3, $$7);
                  return true;
               }
            }
         }
      }

      public void forceScheduleVibration(ServerLevel $$0, Holder<GameEvent> $$1, GameEvent.Context $$2, Vec3 $$3) {
         this.system
            .getVibrationUser()
            .getPositionSource()
            .getPosition($$0)
            .ifPresent($$4 -> this.scheduleVibration($$0, this.system.getVibrationData(), $$1, $$2, $$3, $$4));
      }

      private void scheduleVibration(ServerLevel $$0, VibrationSystem.Data $$1, Holder<GameEvent> $$2, GameEvent.Context $$3, Vec3 $$4, Vec3 $$5) {
         $$1.selectionStrategy.addCandidate(new VibrationInfo($$2, (float)$$4.distanceTo($$5), $$4, $$3.sourceEntity()), $$0.getGameTime());
      }

      public static float distanceBetweenInBlocks(BlockPos $$0, BlockPos $$1) {
         return (float)Math.sqrt($$0.distSqr($$1));
      }

      private static boolean isOccluded(net.minecraft.world.level.Level $$0, Vec3 $$1, Vec3 $$2) {
         Vec3 $$3 = new Vec3(Mth.floor($$1.x) + 0.5, Mth.floor($$1.y) + 0.5, Mth.floor($$1.z) + 0.5);
         Vec3 $$4 = new Vec3(Mth.floor($$2.x) + 0.5, Mth.floor($$2.y) + 0.5, Mth.floor($$2.z) + 0.5);

         for (Direction $$5 : Direction.values()) {
            Vec3 $$6 = $$3.relative($$5, 1.0E-5F);
            if ($$0.isBlockInLine(new net.minecraft.world.level.ClipBlockStateContext($$6, $$4, $$0x -> $$0x.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS)))
                  .getType()
               != Type.BLOCK) {
               return false;
            }
         }

         return true;
      }
   }

   public interface Ticker {
      static void tick(net.minecraft.world.level.Level $$0, VibrationSystem.Data $$1, VibrationSystem.User $$2) {
         if ($$0 instanceof ServerLevel $$3) {
            if ($$1.currentVibration == null) {
               trySelectAndScheduleVibration($$3, $$1, $$2);
            }

            if ($$1.currentVibration != null) {
               boolean $$5 = $$1.getTravelTimeInTicks() > 0;
               tryReloadVibrationParticle($$3, $$1, $$2);
               $$1.decrementTravelTime();
               if ($$1.getTravelTimeInTicks() <= 0) {
                  $$5 = receiveVibration($$3, $$1, $$2, $$1.currentVibration);
               }

               if ($$5) {
                  $$2.onDataChanged();
               }
            }
         }
      }

      private static void trySelectAndScheduleVibration(ServerLevel $$0, VibrationSystem.Data $$1, VibrationSystem.User $$2) {
         $$1.getSelectionStrategy().chosenCandidate($$0.getGameTime()).ifPresent($$3 -> {
            $$1.setCurrentVibration($$3);
            Vec3 $$4 = $$3.pos();
            $$1.setTravelTimeInTicks($$2.calculateTravelTimeInTicks($$3.distance()));
            $$0.sendParticles(new VibrationParticleOption($$2.getPositionSource(), $$1.getTravelTimeInTicks()), $$4.x, $$4.y, $$4.z, 1, 0.0, 0.0, 0.0, 0.0);
            $$2.onDataChanged();
            $$1.getSelectionStrategy().startOver();
         });
      }

      private static void tryReloadVibrationParticle(ServerLevel $$0, VibrationSystem.Data $$1, VibrationSystem.User $$2) {
         if ($$1.shouldReloadVibrationParticle()) {
            if ($$1.currentVibration == null) {
               $$1.setReloadVibrationParticle(false);
            } else {
               Vec3 $$3 = $$1.currentVibration.pos();
               PositionSource $$4 = $$2.getPositionSource();
               Vec3 $$5 = $$4.getPosition($$0).orElse($$3);
               int $$6 = $$1.getTravelTimeInTicks();
               int $$7 = $$2.calculateTravelTimeInTicks($$1.currentVibration.distance());
               double $$8 = 1.0 - (double)$$6 / $$7;
               double $$9 = Mth.lerp($$8, $$3.x, $$5.x);
               double $$10 = Mth.lerp($$8, $$3.y, $$5.y);
               double $$11 = Mth.lerp($$8, $$3.z, $$5.z);
               boolean $$12 = $$0.sendParticles(new VibrationParticleOption($$4, $$6), $$9, $$10, $$11, 1, 0.0, 0.0, 0.0, 0.0) > 0;
               if ($$12) {
                  $$1.setReloadVibrationParticle(false);
               }
            }
         }
      }

      private static boolean receiveVibration(ServerLevel $$0, VibrationSystem.Data $$1, VibrationSystem.User $$2, VibrationInfo $$3) {
         BlockPos $$4 = BlockPos.containing($$3.pos());
         BlockPos $$5 = $$2.getPositionSource().getPosition($$0).<BlockPos>map(BlockPos::containing).orElse($$4);
         if ($$2.requiresAdjacentChunksToBeTicking() && !areAdjacentChunksTicking($$0, $$5)) {
            return false;
         } else {
            $$2.onReceiveVibration(
               $$0,
               $$4,
               $$3.gameEvent(),
               $$3.getEntity($$0).orElse(null),
               $$3.getProjectileOwner($$0).orElse(null),
               VibrationSystem.Listener.distanceBetweenInBlocks($$4, $$5)
            );
            $$1.setCurrentVibration(null);
            return true;
         }
      }

      private static boolean areAdjacentChunksTicking(net.minecraft.world.level.Level $$0, BlockPos $$1) {
         net.minecraft.world.level.ChunkPos $$2 = new net.minecraft.world.level.ChunkPos($$1);

         for (int $$3 = $$2.x - 1; $$3 <= $$2.x + 1; $$3++) {
            for (int $$4 = $$2.z - 1; $$4 <= $$2.z + 1; $$4++) {
               if (!$$0.shouldTickBlocksAt(net.minecraft.world.level.ChunkPos.asLong($$3, $$4)) || $$0.getChunkSource().getChunkNow($$3, $$4) == null) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public interface User {
      int getListenerRadius();

      PositionSource getPositionSource();

      boolean canReceiveVibration(ServerLevel var1, BlockPos var2, Holder<GameEvent> var3, GameEvent.Context var4);

      void onReceiveVibration(ServerLevel var1, BlockPos var2, Holder<GameEvent> var3, Entity var4, Entity var5, float var6);

      default TagKey<GameEvent> getListenableEvents() {
         return GameEventTags.VIBRATIONS;
      }

      default boolean canTriggerAvoidVibration() {
         return false;
      }

      default boolean requiresAdjacentChunksToBeTicking() {
         return false;
      }

      default int calculateTravelTimeInTicks(float $$0) {
         return Mth.floor($$0);
      }

      default boolean isValidVibration(Holder<GameEvent> $$0, GameEvent.Context $$1) {
         if (!$$0.is(this.getListenableEvents())) {
            return false;
         } else {
            Entity $$2 = $$1.sourceEntity();
            if ($$2 != null) {
               if ($$2.isSpectator()) {
                  return false;
               }

               if ($$2.isSteppingCarefully() && $$0.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                  if (this.canTriggerAvoidVibration() && $$2 instanceof ServerPlayer $$3) {
                     CriteriaTriggers.AVOID_VIBRATION.trigger($$3);
                  }

                  return false;
               }

               if ($$2.dampensVibrations()) {
                  return false;
               }
            }

            return $$1.affectedState() != null ? !$$1.affectedState().is(BlockTags.DAMPENS_VIBRATIONS) : true;
         }
      }

      default void onDataChanged() {
      }
   }
}
