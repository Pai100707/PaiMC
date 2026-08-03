package net.minecraft.world.level.block.entity.trialspawner;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.random.WeightedList.Builder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jspecify.annotations.Nullable;

public class TrialSpawnerStateData {
   private static final String TAG_SPAWN_DATA = "spawn_data";
   private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
   private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
   private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
   final Set<UUID> detectedPlayers = new HashSet<>();
   final Set<UUID> currentMobs = new HashSet<>();
   long cooldownEndsAt;
   long nextMobSpawnsAt;
   int totalMobsSpawned;
   Optional<net.minecraft.world.level.SpawnData> nextSpawnData = Optional.empty();
   Optional<ResourceKey<LootTable>> ejectingLootTable = Optional.empty();
   @Nullable
   private Entity displayEntity;
   @Nullable
   private WeightedList<ItemStack> dispensing;
   double spin;
   double oSpin;

   public TrialSpawnerStateData.Packed pack() {
      return new TrialSpawnerStateData.Packed(
         Set.copyOf(this.detectedPlayers),
         Set.copyOf(this.currentMobs),
         this.cooldownEndsAt,
         this.nextMobSpawnsAt,
         this.totalMobsSpawned,
         this.nextSpawnData,
         this.ejectingLootTable
      );
   }

   public void apply(TrialSpawnerStateData.Packed $$0) {
      this.detectedPlayers.clear();
      this.detectedPlayers.addAll($$0.detectedPlayers);
      this.currentMobs.clear();
      this.currentMobs.addAll($$0.currentMobs);
      this.cooldownEndsAt = $$0.cooldownEndsAt;
      this.nextMobSpawnsAt = $$0.nextMobSpawnsAt;
      this.totalMobsSpawned = $$0.totalMobsSpawned;
      this.nextSpawnData = $$0.nextSpawnData;
      this.ejectingLootTable = $$0.ejectingLootTable;
   }

   public void reset() {
      this.currentMobs.clear();
      this.nextSpawnData = Optional.empty();
      this.resetStatistics();
   }

   public void resetStatistics() {
      this.detectedPlayers.clear();
      this.totalMobsSpawned = 0;
      this.nextMobSpawnsAt = 0L;
      this.cooldownEndsAt = 0L;
   }

   public boolean hasMobToSpawn(TrialSpawner $$0, RandomSource $$1) {
      boolean $$2 = this.getOrCreateNextSpawnData($$0, $$1).getEntityToSpawn().getString("id").isPresent();
      return $$2 || !$$0.activeConfig().spawnPotentialsDefinition().isEmpty();
   }

   public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig $$0, int $$1) {
      return this.totalMobsSpawned >= $$0.calculateTargetTotalMobs($$1);
   }

   public boolean haveAllCurrentMobsDied() {
      return this.currentMobs.isEmpty();
   }

   public boolean isReadyToSpawnNextMob(ServerLevel $$0, TrialSpawnerConfig $$1, int $$2) {
      return $$0.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < $$1.calculateTargetSimultaneousMobs($$2);
   }

   public int countAdditionalPlayers(BlockPos $$0) {
      if (this.detectedPlayers.isEmpty()) {
         Util.logAndPauseIfInIde("Trial Spawner at " + $$0 + " has no detected players");
      }

      return Math.max(0, this.detectedPlayers.size() - 1);
   }

   public void tryDetectPlayers(ServerLevel $$0, BlockPos $$1, TrialSpawner $$2) {
      boolean $$3 = ($$1.asLong() + $$0.getGameTime()) % 20L != 0L;
      if (!$$3) {
         if (!$$2.getState().equals(TrialSpawnerState.COOLDOWN) || !$$2.isOminous()) {
            List<UUID> $$4 = $$2.getPlayerDetector().detect($$0, $$2.getEntitySelector(), $$1, $$2.getRequiredPlayerRange(), true);
            boolean $$7;
            if (!$$2.isOminous() && !$$4.isEmpty()) {
               Optional<Pair<Player, Holder<MobEffect>>> $$6 = findPlayerWithOminousEffect($$0, $$4);
               $$6.ifPresent($$3x -> {
                  Player $$4x = (Player)$$3x.getFirst();
                  if ($$3x.getSecond() == MobEffects.BAD_OMEN) {
                     transformBadOmenIntoTrialOmen($$4x);
                  }

                  $$0.levelEvent(3020, BlockPos.containing($$4x.getEyePosition()), 0);
                  $$2.applyOminous($$0, $$1);
               });
               $$7 = $$6.isPresent();
            } else {
               $$7 = false;
            }

            if (!$$2.getState().equals(TrialSpawnerState.COOLDOWN) || $$7) {
               boolean $$8 = $$2.getStateData().detectedPlayers.isEmpty();
               List<UUID> $$9 = $$8 ? $$4 : $$2.getPlayerDetector().detect($$0, $$2.getEntitySelector(), $$1, $$2.getRequiredPlayerRange(), false);
               if (this.detectedPlayers.addAll($$9)) {
                  this.nextMobSpawnsAt = Math.max($$0.getGameTime() + 40L, this.nextMobSpawnsAt);
                  if (!$$7) {
                     int $$10 = $$2.isOminous() ? 3019 : 3013;
                     $$0.levelEvent($$10, $$1, this.detectedPlayers.size());
                  }
               }
            }
         }
      }
   }

   private static Optional<Pair<Player, Holder<MobEffect>>> findPlayerWithOminousEffect(ServerLevel $$0, List<UUID> $$1) {
      Player $$2 = null;

      for (UUID $$3 : $$1) {
         Player $$4 = $$0.getPlayerByUUID($$3);
         if ($$4 != null) {
            Holder<MobEffect> $$5 = MobEffects.TRIAL_OMEN;
            if ($$4.hasEffect($$5)) {
               return Optional.of(Pair.of($$4, $$5));
            }

            if ($$4.hasEffect(MobEffects.BAD_OMEN)) {
               $$2 = $$4;
            }
         }
      }

      return Optional.ofNullable($$2).map($$0x -> Pair.of($$0x, MobEffects.BAD_OMEN));
   }

   public void resetAfterBecomingOminous(TrialSpawner $$0, ServerLevel $$1) {
      this.currentMobs.stream().map($$1::getEntity).forEach($$1x -> {
         if ($$1x != null) {
            $$1.levelEvent(3012, $$1x.blockPosition(), TrialSpawner.FlameParticle.NORMAL.encode());
            if ($$1x instanceof Mob $$2) {
               $$2.dropPreservedEquipment($$1);
            }

            $$1x.remove(RemovalReason.DISCARDED);
         }
      });
      if (!$$0.ominousConfig().spawnPotentialsDefinition().isEmpty()) {
         this.nextSpawnData = Optional.empty();
      }

      this.totalMobsSpawned = 0;
      this.currentMobs.clear();
      this.nextMobSpawnsAt = $$1.getGameTime() + $$0.ominousConfig().ticksBetweenSpawn();
      $$0.markUpdated();
      this.cooldownEndsAt = $$1.getGameTime() + $$0.ominousConfig().ticksBetweenItemSpawners();
   }

   private static void transformBadOmenIntoTrialOmen(Player $$0) {
      MobEffectInstance $$1 = $$0.getEffect(MobEffects.BAD_OMEN);
      if ($$1 != null) {
         int $$2 = $$1.getAmplifier() + 1;
         int $$3 = 18000 * $$2;
         $$0.removeEffect(MobEffects.BAD_OMEN);
         $$0.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN, $$3, 0));
      }
   }

   public boolean isReadyToOpenShutter(ServerLevel $$0, float $$1, int $$2) {
      long $$3 = this.cooldownEndsAt - $$2;
      return (float)$$0.getGameTime() >= (float)$$3 + $$1;
   }

   public boolean isReadyToEjectItems(ServerLevel $$0, float $$1, int $$2) {
      long $$3 = this.cooldownEndsAt - $$2;
      return (float)($$0.getGameTime() - $$3) % $$1 == 0.0F;
   }

   public boolean isCooldownFinished(ServerLevel $$0) {
      return $$0.getGameTime() >= this.cooldownEndsAt;
   }

   protected net.minecraft.world.level.SpawnData getOrCreateNextSpawnData(TrialSpawner $$0, RandomSource $$1) {
      if (this.nextSpawnData.isPresent()) {
         return this.nextSpawnData.get();
      } else {
         WeightedList<net.minecraft.world.level.SpawnData> $$2 = $$0.activeConfig().spawnPotentialsDefinition();
         Optional<net.minecraft.world.level.SpawnData> $$3 = $$2.isEmpty() ? this.nextSpawnData : $$2.getRandom($$1);
         this.nextSpawnData = Optional.of($$3.orElseGet(net.minecraft.world.level.SpawnData::new));
         $$0.markUpdated();
         return this.nextSpawnData.get();
      }
   }

   @Nullable
   public Entity getOrCreateDisplayEntity(TrialSpawner $$0, net.minecraft.world.level.Level $$1, TrialSpawnerState $$2) {
      if (!$$2.hasSpinningMob()) {
         return null;
      } else {
         if (this.displayEntity == null) {
            CompoundTag $$3 = this.getOrCreateNextSpawnData($$0, $$1.getRandom()).getEntityToSpawn();
            if ($$3.getString("id").isPresent()) {
               this.displayEntity = EntityType.loadEntityRecursive($$3, $$1, EntitySpawnReason.TRIAL_SPAWNER, EntityProcessor.NOP);
            }
         }

         return this.displayEntity;
      }
   }

   public CompoundTag getUpdateTag(TrialSpawnerState $$0) {
      CompoundTag $$1 = new CompoundTag();
      if ($$0 == TrialSpawnerState.ACTIVE) {
         $$1.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
      }

      this.nextSpawnData.ifPresent($$1x -> $$1.store("spawn_data", net.minecraft.world.level.SpawnData.CODEC, $$1x));
      return $$1;
   }

   public double getSpin() {
      return this.spin;
   }

   public double getOSpin() {
      return this.oSpin;
   }

   WeightedList<ItemStack> getDispensingItems(ServerLevel $$0, TrialSpawnerConfig $$1, BlockPos $$2) {
      if (this.dispensing != null) {
         return this.dispensing;
      } else {
         LootTable $$3 = $$0.getServer().reloadableRegistries().getLootTable($$1.itemsToDropWhenOminous());
         LootParams $$4 = new LootParams.Builder($$0).create(LootContextParamSets.EMPTY);
         long $$5 = lowResolutionPosition($$0, $$2);
         ObjectArrayList<ItemStack> $$6 = $$3.getRandomItems($$4, $$5);
         if ($$6.isEmpty()) {
            return WeightedList.of();
         } else {
            Builder<ItemStack> $$7 = WeightedList.builder();
            ObjectListIterator var10 = $$6.iterator();

            while (var10.hasNext()) {
               ItemStack $$8 = (ItemStack)var10.next();
               $$7.add($$8.copyWithCount(1), $$8.getCount());
            }

            this.dispensing = $$7.build();
            return this.dispensing;
         }
      }
   }

   private static long lowResolutionPosition(ServerLevel $$0, BlockPos $$1) {
      BlockPos $$2 = new BlockPos(Mth.floor($$1.getX() / 30.0F), Mth.floor($$1.getY() / 20.0F), Mth.floor($$1.getZ() / 30.0F));
      return $$0.getSeed() + $$2.asLong();
   }

   public record Packed(
      Set<UUID> detectedPlayers,
      Set<UUID> currentMobs,
      long cooldownEndsAt,
      long nextMobSpawnsAt,
      int totalMobsSpawned,
      Optional<net.minecraft.world.level.SpawnData> nextSpawnData,
      Optional<ResourceKey<LootTable>> ejectingLootTable
   ) {
      public static final MapCodec<TrialSpawnerStateData.Packed> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               UUIDUtil.CODEC_SET.lenientOptionalFieldOf("registered_players", Set.of()).forGetter(TrialSpawnerStateData.Packed::detectedPlayers),
               UUIDUtil.CODEC_SET.lenientOptionalFieldOf("current_mobs", Set.of()).forGetter(TrialSpawnerStateData.Packed::currentMobs),
               Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", 0L).forGetter(TrialSpawnerStateData.Packed::cooldownEndsAt),
               Codec.LONG.lenientOptionalFieldOf("next_mob_spawns_at", 0L).forGetter(TrialSpawnerStateData.Packed::nextMobSpawnsAt),
               Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", 0).forGetter(TrialSpawnerStateData.Packed::totalMobsSpawned),
               net.minecraft.world.level.SpawnData.CODEC.lenientOptionalFieldOf("spawn_data").forGetter(TrialSpawnerStateData.Packed::nextSpawnData),
               LootTable.KEY_CODEC.lenientOptionalFieldOf("ejecting_loot_table").forGetter(TrialSpawnerStateData.Packed::ejectingLootTable)
            )
            .apply($$0, TrialSpawnerStateData.Packed::new)
      );
   }
}
