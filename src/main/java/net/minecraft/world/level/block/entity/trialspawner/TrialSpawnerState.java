package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public enum TrialSpawnerState implements StringRepresentable {
   INACTIVE("inactive", 0, TrialSpawnerState.ParticleEmission.NONE, -1.0, false),
   WAITING_FOR_PLAYERS("waiting_for_players", 4, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, 200.0, true),
   ACTIVE("active", 8, TrialSpawnerState.ParticleEmission.FLAMES_AND_SMOKE, 1000.0, true),
   WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, -1.0, false),
   EJECTING_REWARD("ejecting_reward", 8, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, -1.0, false),
   COOLDOWN("cooldown", 0, TrialSpawnerState.ParticleEmission.SMOKE_INSIDE_AND_TOP_FACE, -1.0, false);

   private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
   private static final int TIME_BETWEEN_EACH_EJECTION = Mth.floor(30.0F);
   private final String name;
   private final int lightLevel;
   private final double spinningMobSpeed;
   private final TrialSpawnerState.ParticleEmission particleEmission;
   private final boolean isCapableOfSpawning;

   private TrialSpawnerState(final String $$0, final int $$1, final TrialSpawnerState.ParticleEmission $$2, final double $$3, final boolean $$4) {
      this.name = $$0;
      this.lightLevel = $$1;
      this.particleEmission = $$2;
      this.spinningMobSpeed = $$3;
      this.isCapableOfSpawning = $$4;
   }

   TrialSpawnerState tickAndGetNext(BlockPos $$0, TrialSpawner $$1, ServerLevel $$2) {
      TrialSpawnerStateData $$3 = $$1.getStateData();
      TrialSpawnerConfig $$4 = $$1.activeConfig();

      return switch (this) {
         case INACTIVE -> $$3.getOrCreateDisplayEntity($$1, $$2, WAITING_FOR_PLAYERS) == null ? this : WAITING_FOR_PLAYERS;
         case WAITING_FOR_PLAYERS -> {
            if (!$$1.canSpawnInLevel($$2)) {
               $$3.resetStatistics();
               yield this;
            } else if (!$$3.hasMobToSpawn($$1, $$2.random)) {
               yield INACTIVE;
            } else {
               $$3.tryDetectPlayers($$2, $$0, $$1);
               yield $$3.detectedPlayers.isEmpty() ? this : ACTIVE;
            }
         }
         case ACTIVE -> {
            if (!$$1.canSpawnInLevel($$2)) {
               $$3.resetStatistics();
               yield WAITING_FOR_PLAYERS;
            } else if (!$$3.hasMobToSpawn($$1, $$2.random)) {
               yield INACTIVE;
            } else {
               int $$5 = $$3.countAdditionalPlayers($$0);
               $$3.tryDetectPlayers($$2, $$0, $$1);
               if ($$1.isOminous()) {
                  this.spawnOminousOminousItemSpawner($$2, $$0, $$1);
               }

               if ($$3.hasFinishedSpawningAllMobs($$4, $$5)) {
                  if ($$3.haveAllCurrentMobsDied()) {
                     $$3.cooldownEndsAt = $$2.getGameTime() + $$1.getTargetCooldownLength();
                     $$3.totalMobsSpawned = 0;
                     $$3.nextMobSpawnsAt = 0L;
                     yield WAITING_FOR_REWARD_EJECTION;
                  }
               } else if ($$3.isReadyToSpawnNextMob($$2, $$4, $$5)) {
                  $$1.spawnMob($$2, $$0).ifPresent($$4x -> {
                     $$3.currentMobs.add($$4x);
                     $$3.totalMobsSpawned++;
                     $$3.nextMobSpawnsAt = $$2.getGameTime() + $$4.ticksBetweenSpawn();
                     $$4.spawnPotentialsDefinition().getRandom($$2.getRandom()).ifPresent($$2xx -> {
                        $$3.nextSpawnData = Optional.of($$2xx);
                        $$1.markUpdated();
                     });
                  });
               }

               yield this;
            }
         }
         case WAITING_FOR_REWARD_EJECTION -> {
            if ($$3.isReadyToOpenShutter($$2, 40.0F, $$1.getTargetCooldownLength())) {
               $$2.playSound(null, $$0, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS);
               yield EJECTING_REWARD;
            } else {
               yield this;
            }
         }
         case EJECTING_REWARD -> {
            if (!$$3.isReadyToEjectItems($$2, TIME_BETWEEN_EACH_EJECTION, $$1.getTargetCooldownLength())) {
               yield this;
            } else if ($$3.detectedPlayers.isEmpty()) {
               $$2.playSound(null, $$0, SoundEvents.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS);
               $$3.ejectingLootTable = Optional.empty();
               yield COOLDOWN;
            } else {
               if ($$3.ejectingLootTable.isEmpty()) {
                  $$3.ejectingLootTable = $$4.lootTablesToEject().getRandom($$2.getRandom());
               }

               $$3.ejectingLootTable.ifPresent($$3x -> $$1.ejectReward($$2, $$0, $$3x));
               $$3.detectedPlayers.remove($$3.detectedPlayers.iterator().next());
               yield this;
            }
         }
         case COOLDOWN -> {
            $$3.tryDetectPlayers($$2, $$0, $$1);
            if (!$$3.detectedPlayers.isEmpty()) {
               $$3.totalMobsSpawned = 0;
               $$3.nextMobSpawnsAt = 0L;
               yield ACTIVE;
            } else if ($$3.isCooldownFinished($$2)) {
               $$1.removeOminous($$2, $$0);
               $$3.reset();
               yield WAITING_FOR_PLAYERS;
            } else {
               yield this;
            }
         }
      };
   }

   private void spawnOminousOminousItemSpawner(ServerLevel $$0, BlockPos $$1, TrialSpawner $$2) {
      TrialSpawnerStateData $$3 = $$2.getStateData();
      TrialSpawnerConfig $$4 = $$2.activeConfig();
      ItemStack $$5 = $$3.getDispensingItems($$0, $$4, $$1).getRandom($$0.random).orElse(ItemStack.EMPTY);
      if (!$$5.isEmpty()) {
         if (this.timeToSpawnItemSpawner($$0, $$3)) {
            calculatePositionToSpawnSpawner($$0, $$1, $$2, $$3).ifPresent($$4x -> {
               OminousItemSpawner $$5x = OminousItemSpawner.create($$0, $$5);
               $$5x.snapTo($$4x);
               $$0.addFreshEntity($$5x);
               float $$6 = ($$0.getRandom().nextFloat() - $$0.getRandom().nextFloat()) * 0.2F + 1.0F;
               $$0.playSound(null, BlockPos.containing($$4x), SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundSource.BLOCKS, 1.0F, $$6);
               $$3.cooldownEndsAt = $$0.getGameTime() + $$2.ominousConfig().ticksBetweenItemSpawners();
            });
         }
      }
   }

   private static Optional<Vec3> calculatePositionToSpawnSpawner(ServerLevel $$0, BlockPos $$1, TrialSpawner $$2, TrialSpawnerStateData $$3) {
      List<Player> $$4 = $$3.detectedPlayers
         .stream()
         .map($$0::getPlayerByUUID)
         .filter(Objects::nonNull)
         .filter(
            $$2x -> !$$2x.isCreative()
               && !$$2x.isSpectator()
               && $$2x.isAlive()
               && $$2x.distanceToSqr($$1.getCenter()) <= Mth.square($$2.getRequiredPlayerRange())
         )
         .toList();
      if ($$4.isEmpty()) {
         return Optional.empty();
      } else {
         Entity $$5 = selectEntityToSpawnItemAbove($$4, $$3.currentMobs, $$2, $$1, $$0);
         return $$5 == null ? Optional.empty() : calculatePositionAbove($$5, $$0);
      }
   }

   private static Optional<Vec3> calculatePositionAbove(Entity $$0, ServerLevel $$1) {
      Vec3 $$2 = $$0.position();
      Vec3 $$3 = $$2.relative(Direction.UP, $$0.getBbHeight() + 2.0F + $$1.random.nextInt(4));
      BlockHitResult $$4 = $$1.clip(
         new net.minecraft.world.level.ClipContext(
            $$2, $$3, net.minecraft.world.level.ClipContext.Block.VISUAL, net.minecraft.world.level.ClipContext.Fluid.NONE, CollisionContext.empty()
         )
      );
      Vec3 $$5 = $$4.getBlockPos().getCenter().relative(Direction.DOWN, 1.0);
      BlockPos $$6 = BlockPos.containing($$5);
      return !$$1.getBlockState($$6).getCollisionShape($$1, $$6).isEmpty() ? Optional.empty() : Optional.of($$5);
   }

   @Nullable
   private static Entity selectEntityToSpawnItemAbove(List<Player> $$0, Set<UUID> $$1, TrialSpawner $$2, BlockPos $$3, ServerLevel $$4) {
      Stream<Entity> $$5 = $$1.stream()
         .map($$4::getEntity)
         .filter(Objects::nonNull)
         .filter($$2x -> $$2x.isAlive() && $$2x.distanceToSqr($$3.getCenter()) <= Mth.square($$2.getRequiredPlayerRange()));
      List<? extends Entity> $$6 = $$4.random.nextBoolean() ? $$5.toList() : $$0;
      if ($$6.isEmpty()) {
         return null;
      } else {
         return $$6.size() == 1 ? $$6.getFirst() : (Entity)Util.getRandom($$6, $$4.random);
      }
   }

   private boolean timeToSpawnItemSpawner(ServerLevel $$0, TrialSpawnerStateData $$1) {
      return $$0.getGameTime() >= $$1.cooldownEndsAt;
   }

   public int lightLevel() {
      return this.lightLevel;
   }

   public double spinningMobSpeed() {
      return this.spinningMobSpeed;
   }

   public boolean hasSpinningMob() {
      return this.spinningMobSpeed >= 0.0;
   }

   public boolean isCapableOfSpawning() {
      return this.isCapableOfSpawning;
   }

   public void emitParticles(net.minecraft.world.level.Level $$0, BlockPos $$1, boolean $$2) {
      this.particleEmission.emit($$0, $$0.getRandom(), $$1, $$2);
   }

   public String getSerializedName() {
      return this.name;
   }

   static class LightLevel {
      private static final int UNLIT = 0;
      private static final int HALF_LIT = 4;
      private static final int LIT = 8;

      private LightLevel() {
      }
   }

   interface ParticleEmission {
      TrialSpawnerState.ParticleEmission NONE = ($$0, $$1, $$2, $$3) -> {};
      TrialSpawnerState.ParticleEmission SMALL_FLAMES = ($$0, $$1, $$2, $$3) -> {
         if ($$1.nextInt(2) == 0) {
            Vec3 $$4 = $$2.getCenter().offsetRandom($$1, 0.9F);
            addParticle($$3 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, $$4, $$0);
         }
      };
      TrialSpawnerState.ParticleEmission FLAMES_AND_SMOKE = ($$0, $$1, $$2, $$3) -> {
         Vec3 $$4 = $$2.getCenter().offsetRandom($$1, 1.0F);
         addParticle(ParticleTypes.SMOKE, $$4, $$0);
         addParticle($$3 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, $$4, $$0);
      };
      TrialSpawnerState.ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = ($$0, $$1, $$2, $$3) -> {
         Vec3 $$4 = $$2.getCenter().offsetRandom($$1, 0.9F);
         if ($$1.nextInt(3) == 0) {
            addParticle(ParticleTypes.SMOKE, $$4, $$0);
         }

         if ($$0.getGameTime() % 20L == 0L) {
            Vec3 $$5 = $$2.getCenter().add(0.0, 0.5, 0.0);
            int $$6 = $$0.getRandom().nextInt(4) + 20;

            for (int $$7 = 0; $$7 < $$6; $$7++) {
               addParticle(ParticleTypes.SMOKE, $$5, $$0);
            }
         }
      };

      private static void addParticle(SimpleParticleType $$0, Vec3 $$1, net.minecraft.world.level.Level $$2) {
         $$2.addParticle($$0, $$1.x(), $$1.y(), $$1.z(), 0.0, 0.0, 0.0);
      }

      void emit(net.minecraft.world.level.Level var1, RandomSource var2, BlockPos var3, boolean var4);
   }

   static class SpinningMob {
      private static final double NONE = -1.0;
      private static final double SLOW = 200.0;
      private static final double FAST = 1000.0;

      private SpinningMob() {
      }
   }
}
