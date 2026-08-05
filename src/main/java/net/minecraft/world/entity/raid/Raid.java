package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.SectionPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BannerPatternLayers.Builder;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public class Raid {
   public static final net.minecraft.world.entity.SpawnPlacementType RAVAGER_SPAWN_PLACEMENT_TYPE = net.minecraft.world.entity.SpawnPlacements.getPlacementType(
      net.minecraft.world.entity.EntityType.RAVAGER
   );
   public static final MapCodec<Raid> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.BOOL.fieldOf("started").forGetter($$0x -> $$0x.started),
            Codec.BOOL.fieldOf("active").forGetter($$0x -> $$0x.active),
            Codec.LONG.fieldOf("ticks_active").forGetter($$0x -> $$0x.ticksActive),
            Codec.INT.fieldOf("raid_omen_level").forGetter($$0x -> $$0x.raidOmenLevel),
            Codec.INT.fieldOf("groups_spawned").forGetter($$0x -> $$0x.groupsSpawned),
            Codec.INT.fieldOf("cooldown_ticks").forGetter($$0x -> $$0x.raidCooldownTicks),
            Codec.INT.fieldOf("post_raid_ticks").forGetter($$0x -> $$0x.postRaidTicks),
            Codec.FLOAT.fieldOf("total_health").forGetter($$0x -> $$0x.totalHealth),
            Codec.INT.fieldOf("group_count").forGetter($$0x -> $$0x.numGroups),
            Raid.RaidStatus.CODEC.fieldOf("status").forGetter($$0x -> $$0x.status),
            BlockPos.CODEC.fieldOf("center").forGetter($$0x -> $$0x.center),
            UUIDUtil.CODEC_SET.fieldOf("heroes_of_the_village").forGetter($$0x -> $$0x.heroesOfTheVillage)
         )
         .apply($$0, Raid::new)
   );
   private static final int ALLOW_SPAWNING_WITHIN_VILLAGE_SECONDS_THRESHOLD = 7;
   private static final int SECTION_RADIUS_FOR_FINDING_NEW_VILLAGE_CENTER = 2;
   private static final int VILLAGE_SEARCH_RADIUS = 32;
   private static final int RAID_TIMEOUT_TICKS = 48000;
   private static final int NUM_SPAWN_ATTEMPTS = 5;
   private static final Component OMINOUS_BANNER_PATTERN_NAME = Component.translatable("block.minecraft.ominous_banner");
   private static final String RAIDERS_REMAINING = "event.minecraft.raid.raiders_remaining";
   public static final int VILLAGE_RADIUS_BUFFER = 16;
   private static final int POST_RAID_TICK_LIMIT = 40;
   private static final int DEFAULT_PRE_RAID_TICKS = 300;
   public static final int MAX_NO_ACTION_TIME = 2400;
   public static final int MAX_CELEBRATION_TICKS = 600;
   private static final int OUTSIDE_RAID_BOUNDS_TIMEOUT = 30;
   public static final int DEFAULT_MAX_RAID_OMEN_LEVEL = 5;
   private static final int LOW_MOB_THRESHOLD = 2;
   private static final Component RAID_NAME_COMPONENT = Component.translatable("event.minecraft.raid");
   private static final Component RAID_BAR_VICTORY_COMPONENT = Component.translatable("event.minecraft.raid.victory.full");
   private static final Component RAID_BAR_DEFEAT_COMPONENT = Component.translatable("event.minecraft.raid.defeat.full");
   private static final int HERO_OF_THE_VILLAGE_DURATION = 48000;
   private static final int VALID_RAID_RADIUS = 96;
   public static final int VALID_RAID_RADIUS_SQR = 9216;
   public static final int RAID_REMOVAL_THRESHOLD_SQR = 12544;
   private final Map<Integer, Raider> groupToLeaderMap = Maps.newHashMap();
   private final Map<Integer, Set<Raider>> groupRaiderMap = Maps.newHashMap();
   private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
   private long ticksActive;
   private BlockPos center;
   private boolean started;
   private float totalHealth;
   private int raidOmenLevel;
   private boolean active;
   private int groupsSpawned;
   private final ServerBossEvent raidEvent = new ServerBossEvent(RAID_NAME_COMPONENT, BossBarColor.RED, BossBarOverlay.NOTCHED_10);
   private int postRaidTicks;
   private int raidCooldownTicks;
   private final RandomSource random = RandomSource.create();
   private final int numGroups;
   private Raid.RaidStatus status;
   private int celebrationTicks;
   private Optional<BlockPos> waveSpawnPos = Optional.empty();

   public Raid(BlockPos $$0, Difficulty $$1) {
      this.active = true;
      this.raidCooldownTicks = 300;
      this.raidEvent.setProgress(0.0F);
      this.center = $$0;
      this.numGroups = this.getNumGroups($$1);
      this.status = Raid.RaidStatus.ONGOING;
   }

   private Raid(boolean $$0, boolean $$1, long $$2, int $$3, int $$4, int $$5, int $$6, float $$7, int $$8, Raid.RaidStatus $$9, BlockPos $$10, Set<UUID> $$11) {
      this.started = $$0;
      this.active = $$1;
      this.ticksActive = $$2;
      this.raidOmenLevel = $$3;
      this.groupsSpawned = $$4;
      this.raidCooldownTicks = $$5;
      this.postRaidTicks = $$6;
      this.totalHealth = $$7;
      this.center = $$10;
      this.numGroups = $$8;
      this.status = $$9;
      this.heroesOfTheVillage.addAll($$11);
   }

   public boolean isOver() {
      return this.isVictory() || this.isLoss();
   }

   public boolean isBetweenWaves() {
      return this.hasFirstWaveSpawned() && this.getTotalRaidersAlive() == 0 && this.raidCooldownTicks > 0;
   }

   public boolean hasFirstWaveSpawned() {
      return this.groupsSpawned > 0;
   }

   public boolean isStopped() {
      return this.status == Raid.RaidStatus.STOPPED;
   }

   public boolean isVictory() {
      return this.status == Raid.RaidStatus.VICTORY;
   }

   public boolean isLoss() {
      return this.status == Raid.RaidStatus.LOSS;
   }

   public float getTotalHealth() {
      return this.totalHealth;
   }

   public Set<Raider> getAllRaiders() {
      Set<Raider> $$0 = Sets.newHashSet();

      for (Set<Raider> $$1 : this.groupRaiderMap.values()) {
         $$0.addAll($$1);
      }

      return $$0;
   }

   public boolean isStarted() {
      return this.started;
   }

   public int getGroupsSpawned() {
      return this.groupsSpawned;
   }

   private Predicate<ServerPlayer> validPlayer() {
      return $$0 -> {
         BlockPos $$1 = $$0.blockPosition();
         return $$0.isAlive() && $$0.level().getRaidAt($$1) == this;
      };
   }

   private void updatePlayers(ServerLevel $$0) {
      Set<ServerPlayer> $$1 = Sets.newHashSet(this.raidEvent.getPlayers());
      List<ServerPlayer> $$2 = $$0.getPlayers(this.validPlayer());

      for (ServerPlayer $$3 : $$2) {
         if (!$$1.contains($$3)) {
            this.raidEvent.addPlayer($$3);
         }
      }

      for (ServerPlayer $$4 : $$1) {
         if (!$$2.contains($$4)) {
            this.raidEvent.removePlayer($$4);
         }
      }
   }

   public int getMaxRaidOmenLevel() {
      return 5;
   }

   public int getRaidOmenLevel() {
      return this.raidOmenLevel;
   }

   public void setRaidOmenLevel(int $$0) {
      this.raidOmenLevel = $$0;
   }

   public boolean absorbRaidOmen(ServerPlayer $$0) {
      MobEffectInstance $$1 = $$0.getEffect(MobEffects.RAID_OMEN);
      if ($$1 == null) {
         return false;
      } else {
         this.raidOmenLevel = this.raidOmenLevel + $$1.getAmplifier() + 1;
         this.raidOmenLevel = Mth.clamp(this.raidOmenLevel, 0, this.getMaxRaidOmenLevel());
         if (!this.hasFirstWaveSpawned()) {
            $$0.awardStat(Stats.RAID_TRIGGER);
            CriteriaTriggers.RAID_OMEN.trigger($$0);
         }

         return true;
      }
   }

   public void stop() {
      this.active = false;
      this.raidEvent.removeAllPlayers();
      this.status = Raid.RaidStatus.STOPPED;
   }

   public void tick(ServerLevel $$0) {
      if (!this.isStopped()) {
         if (this.status == Raid.RaidStatus.ONGOING) {
            boolean $$1 = this.active;
            this.active = $$0.hasChunkAt(this.center);
            if ($$0.getDifficulty() == Difficulty.PEACEFUL) {
               this.stop();
               return;
            }

            if ($$1 != this.active) {
               this.raidEvent.setVisible(this.active);
            }

            if (!this.active) {
               return;
            }

            if (!$$0.isVillage(this.center)) {
               this.moveRaidCenterToNearbyVillageSection($$0);
            }

            if (!$$0.isVillage(this.center)) {
               if (this.groupsSpawned > 0) {
                  this.status = Raid.RaidStatus.LOSS;
               } else {
                  this.stop();
               }
            }

            this.ticksActive++;
            if (this.ticksActive >= 48000L) {
               this.stop();
               return;
            }

            int $$2 = this.getTotalRaidersAlive();
            if ($$2 == 0 && this.hasMoreWaves()) {
               if (this.raidCooldownTicks <= 0) {
                  if (this.raidCooldownTicks == 0 && this.groupsSpawned > 0) {
                     this.raidCooldownTicks = 300;
                     this.raidEvent.setName(RAID_NAME_COMPONENT);
                     return;
                  }
               } else {
                  boolean $$3 = this.waveSpawnPos.isPresent();
                  boolean $$4 = !$$3 && this.raidCooldownTicks % 5 == 0;
                  if ($$3 && !$$0.isPositionEntityTicking(this.waveSpawnPos.get())) {
                     $$4 = true;
                  }

                  if ($$4) {
                     this.waveSpawnPos = this.getValidSpawnPos($$0);
                  }

                  if (this.raidCooldownTicks == 300 || this.raidCooldownTicks % 20 == 0) {
                     this.updatePlayers($$0);
                  }

                  this.raidCooldownTicks--;
                  this.raidEvent.setProgress(Mth.clamp((300 - this.raidCooldownTicks) / 300.0F, 0.0F, 1.0F));
               }
            }

            if (this.ticksActive % 20L == 0L) {
               this.updatePlayers($$0);
               this.updateRaiders($$0);
               if ($$2 > 0) {
                  if ($$2 <= 2) {
                     this.raidEvent
                        .setName(
                           RAID_NAME_COMPONENT.copy().append(" - ").append(Component.translatable("event.minecraft.raid.raiders_remaining", new Object[]{$$2}))
                        );
                  } else {
                     this.raidEvent.setName(RAID_NAME_COMPONENT);
                  }
               } else {
                  this.raidEvent.setName(RAID_NAME_COMPONENT);
               }
            }

            if (SharedConstants.DEBUG_RAIDS) {
               this.raidEvent
                  .setName(
                     RAID_NAME_COMPONENT.copy()
                        .append(" wave: ")
                        .append(this.groupsSpawned + "")
                        .append(CommonComponents.SPACE)
                        .append("Raiders alive: ")
                        .append(this.getTotalRaidersAlive() + "")
                        .append(CommonComponents.SPACE)
                        .append(this.getHealthOfLivingRaiders() + "")
                        .append(" / ")
                        .append(this.totalHealth + "")
                        .append(" Is bonus? ")
                        .append((this.hasBonusWave() && this.hasSpawnedBonusWave()) + "")
                        .append(" Status: ")
                        .append(this.status.getSerializedName())
                  );
            }

            boolean $$5 = false;
            int $$6 = 0;

            while (this.shouldSpawnGroup()) {
               BlockPos $$7 = this.waveSpawnPos.orElseGet(() -> this.findRandomSpawnPos($$0, 20));
               if ($$7 != null) {
                  this.started = true;
                  this.spawnGroup($$0, $$7);
                  if (!$$5) {
                     this.playSound($$0, $$7);
                     $$5 = true;
                  }
               } else {
                  $$6++;
               }

               if ($$6 > 5) {
                  this.stop();
                  break;
               }
            }

            if (this.isStarted() && !this.hasMoreWaves() && $$2 == 0) {
               if (this.postRaidTicks < 40) {
                  this.postRaidTicks++;
               } else {
                  this.status = Raid.RaidStatus.VICTORY;

                  for (UUID $$8 : this.heroesOfTheVillage) {
                     net.minecraft.world.entity.Entity $$9 = $$0.getEntity($$8);
                     if ($$9 instanceof net.minecraft.world.entity.LivingEntity $$10 && !$$9.isSpectator()) {
                        $$10.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 48000, this.raidOmenLevel - 1, false, false, true));
                        if ($$10 instanceof ServerPlayer $$11) {
                           $$11.awardStat(Stats.RAID_WIN);
                           CriteriaTriggers.RAID_WIN.trigger($$11);
                        }
                     }
                  }
               }
            }

            this.setDirty($$0);
         } else if (this.isOver()) {
            this.celebrationTicks++;
            if (this.celebrationTicks >= 600) {
               this.stop();
               return;
            }

            if (this.celebrationTicks % 20 == 0) {
               this.updatePlayers($$0);
               this.raidEvent.setVisible(true);
               if (this.isVictory()) {
                  this.raidEvent.setProgress(0.0F);
                  this.raidEvent.setName(RAID_BAR_VICTORY_COMPONENT);
               } else {
                  this.raidEvent.setName(RAID_BAR_DEFEAT_COMPONENT);
               }
            }
         }
      }
   }

   private void moveRaidCenterToNearbyVillageSection(ServerLevel $$0) {
      Stream<SectionPos> $$1 = SectionPos.cube(SectionPos.of(this.center), 2);
      $$1.filter($$0::isVillage)
         .<BlockPos>map(SectionPos::center)
         .min(Comparator.comparingDouble($$0x -> $$0x.distSqr(this.center)))
         .ifPresent(this::setCenter);
   }

   private Optional<BlockPos> getValidSpawnPos(ServerLevel $$0) {
      BlockPos $$1 = this.findRandomSpawnPos($$0, 8);
      return $$1 != null ? Optional.of($$1) : Optional.empty();
   }

   private boolean hasMoreWaves() {
      return this.hasBonusWave() ? !this.hasSpawnedBonusWave() : !this.isFinalWave();
   }

   private boolean isFinalWave() {
      return this.getGroupsSpawned() == this.numGroups;
   }

   private boolean hasBonusWave() {
      return this.raidOmenLevel > 1;
   }

   private boolean hasSpawnedBonusWave() {
      return this.getGroupsSpawned() > this.numGroups;
   }

   private boolean shouldSpawnBonusGroup() {
      return this.isFinalWave() && this.getTotalRaidersAlive() == 0 && this.hasBonusWave();
   }

   private void updateRaiders(ServerLevel $$0) {
      Iterator<Set<Raider>> $$1 = this.groupRaiderMap.values().iterator();
      Set<Raider> $$2 = Sets.newHashSet();

      while ($$1.hasNext()) {
         Set<Raider> $$3 = $$1.next();

         for (Raider $$4 : $$3) {
            BlockPos $$5 = $$4.blockPosition();
            if ($$4.isRemoved() || $$4.level().dimension() != $$0.dimension() || this.center.distSqr($$5) >= 12544.0) {
               $$2.add($$4);
            } else if ($$4.tickCount > 600) {
               if ($$0.getEntity($$4.getUUID()) == null) {
                  $$2.add($$4);
               }

               if (!$$0.isVillage($$5) && $$4.getNoActionTime() > 2400) {
                  $$4.setTicksOutsideRaid($$4.getTicksOutsideRaid() + 1);
               }

               if ($$4.getTicksOutsideRaid() >= 30) {
                  $$2.add($$4);
               }
            }
         }
      }

      for (Raider $$6 : $$2) {
         this.removeFromRaid($$0, $$6, true);
         if ($$6.isPatrolLeader()) {
            this.removeLeader($$6.getWave());
         }
      }
   }

   private void playSound(ServerLevel $$0, BlockPos $$1) {
      float $$2 = 13.0F;
      int $$3 = 64;
      Collection<ServerPlayer> $$4 = this.raidEvent.getPlayers();
      long $$5 = this.random.nextLong();

      for (ServerPlayer $$6 : $$0.players()) {
         Vec3 $$7 = $$6.position();
         Vec3 $$8 = Vec3.atCenterOf($$1);
         double $$9 = Math.sqrt(($$8.x - $$7.x) * ($$8.x - $$7.x) + ($$8.z - $$7.z) * ($$8.z - $$7.z));
         double $$10 = $$7.x + 13.0 / $$9 * ($$8.x - $$7.x);
         double $$11 = $$7.z + 13.0 / $$9 * ($$8.z - $$7.z);
         if ($$9 <= 64.0 || $$4.contains($$6)) {
            $$6.connection.send(new ClientboundSoundPacket(SoundEvents.RAID_HORN, SoundSource.NEUTRAL, $$10, $$6.getY(), $$11, 64.0F, 1.0F, $$5));
         }
      }
   }

   private void spawnGroup(ServerLevel $$0, BlockPos $$1) {
      boolean $$2 = false;
      int $$3 = this.groupsSpawned + 1;
      this.totalHealth = 0.0F;
      DifficultyInstance $$4 = $$0.getCurrentDifficultyAt($$1);
      boolean $$5 = this.shouldSpawnBonusGroup();

      for (Raid.RaiderType $$6 : Raid.RaiderType.VALUES) {
         int $$7 = this.getDefaultNumSpawns($$6, $$3, $$5) + this.getPotentialBonusSpawns($$6, this.random, $$3, $$4, $$5);
         int $$8 = 0;

         for (int $$9 = 0; $$9 < $$7; $$9++) {
            Raider $$10 = $$6.entityType.create($$0, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if ($$10 == null) {
               break;
            }

            if (!$$2 && $$10.canBeLeader()) {
               $$10.setPatrolLeader(true);
               this.setLeader($$3, $$10);
               $$2 = true;
            }

            this.joinRaid($$0, $$3, $$10, $$1, false);
            if ($$6.entityType == net.minecraft.world.entity.EntityType.RAVAGER) {
               Raider $$11 = null;
               if ($$3 == this.getNumGroups(Difficulty.NORMAL)) {
                  $$11 = net.minecraft.world.entity.EntityType.PILLAGER.create($$0, net.minecraft.world.entity.EntitySpawnReason.EVENT);
               } else if ($$3 >= this.getNumGroups(Difficulty.HARD)) {
                  if ($$8 == 0) {
                     $$11 = net.minecraft.world.entity.EntityType.EVOKER.create($$0, net.minecraft.world.entity.EntitySpawnReason.EVENT);
                  } else {
                     $$11 = net.minecraft.world.entity.EntityType.VINDICATOR.create($$0, net.minecraft.world.entity.EntitySpawnReason.EVENT);
                  }
               }

               $$8++;
               if ($$11 != null) {
                  this.joinRaid($$0, $$3, $$11, $$1, false);
                  $$11.snapTo($$1, 0.0F, 0.0F);
                  $$11.startRiding($$10, false, false);
               }
            }
         }
      }

      this.waveSpawnPos = Optional.empty();
      this.groupsSpawned++;
      this.updateBossbar();
      this.setDirty($$0);
   }

   public void joinRaid(ServerLevel $$0, int $$1, Raider $$2, BlockPos $$3, boolean $$4) {
      boolean $$5 = this.addWaveMob($$0, $$1, $$2);
      if ($$5) {
         $$2.setCurrentRaid(this);
         $$2.setWave($$1);
         $$2.setCanJoinRaid(true);
         $$2.setTicksOutsideRaid(0);
         if (!$$4 && $$3 != null) {
            $$2.setPos($$3.getX() + 0.5, $$3.getY() + 1.0, $$3.getZ() + 0.5);
            $$2.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$3), net.minecraft.world.entity.EntitySpawnReason.EVENT, null);
            $$2.applyRaidBuffs($$0, $$1, false);
            $$2.setOnGround(true);
            $$0.addFreshEntityWithPassengers($$2);
         }
      }
   }

   public void updateBossbar() {
      this.raidEvent.setProgress(Mth.clamp(this.getHealthOfLivingRaiders() / this.totalHealth, 0.0F, 1.0F));
   }

   public float getHealthOfLivingRaiders() {
      float $$0 = 0.0F;

      for (Set<Raider> $$1 : this.groupRaiderMap.values()) {
         for (Raider $$2 : $$1) {
            $$0 += $$2.getHealth();
         }
      }

      return $$0;
   }

   private boolean shouldSpawnGroup() {
      return this.raidCooldownTicks == 0 && (this.groupsSpawned < this.numGroups || this.shouldSpawnBonusGroup()) && this.getTotalRaidersAlive() == 0;
   }

   public int getTotalRaidersAlive() {
      return this.groupRaiderMap.values().stream().mapToInt(Set::size).sum();
   }

   public void removeFromRaid(ServerLevel $$0, Raider $$1, boolean $$2) {
      Set<Raider> $$3 = this.groupRaiderMap.get($$1.getWave());
      if ($$3 != null) {
         boolean $$4 = $$3.remove($$1);
         if ($$4) {
            if ($$2) {
               this.totalHealth = this.totalHealth - $$1.getHealth();
            }

            $$1.setCurrentRaid(null);
            this.updateBossbar();
            this.setDirty($$0);
         }
      }
   }

   private void setDirty(ServerLevel $$0) {
      $$0.getRaids().setDirty();
   }

   public static ItemStack getOminousBannerInstance(HolderGetter<BannerPattern> $$0) {
      ItemStack $$1 = new ItemStack(Items.WHITE_BANNER);
      BannerPatternLayers $$2 = new Builder()
         .addIfRegistered($$0, BannerPatterns.RHOMBUS_MIDDLE, DyeColor.CYAN)
         .addIfRegistered($$0, BannerPatterns.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY)
         .addIfRegistered($$0, BannerPatterns.STRIPE_CENTER, DyeColor.GRAY)
         .addIfRegistered($$0, BannerPatterns.BORDER, DyeColor.LIGHT_GRAY)
         .addIfRegistered($$0, BannerPatterns.STRIPE_MIDDLE, DyeColor.BLACK)
         .addIfRegistered($$0, BannerPatterns.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY)
         .addIfRegistered($$0, BannerPatterns.CIRCLE_MIDDLE, DyeColor.LIGHT_GRAY)
         .addIfRegistered($$0, BannerPatterns.BORDER, DyeColor.BLACK)
         .build();
      $$1.set(DataComponents.BANNER_PATTERNS, $$2);
      $$1.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.BANNER_PATTERNS, true));
      $$1.set(DataComponents.ITEM_NAME, OMINOUS_BANNER_PATTERN_NAME);
      $$1.set(DataComponents.RARITY, Rarity.UNCOMMON);
      return $$1;
   }

   
   public Raider getLeader(int $$0) {
      return this.groupToLeaderMap.get($$0);
   }

   
   private BlockPos findRandomSpawnPos(ServerLevel $$0, int $$1) {
      int $$2 = this.raidCooldownTicks / 20;
      float $$3 = 0.22F * $$2 - 0.24F;
      MutableBlockPos $$4 = new MutableBlockPos();
      float $$5 = $$0.random.nextFloat() * (float) (Math.PI * 2);

      for (int $$6 = 0; $$6 < $$1; $$6++) {
         float $$7 = $$5 + (float) Math.PI * $$6 / 8.0F;
         int $$8 = this.center.getX() + Mth.floor(Mth.cos($$7) * 32.0F * $$3) + $$0.random.nextInt(3) * Mth.floor($$3);
         int $$9 = this.center.getZ() + Mth.floor(Mth.sin($$7) * 32.0F * $$3) + $$0.random.nextInt(3) * Mth.floor($$3);
         int $$10 = $$0.getHeight(Types.WORLD_SURFACE, $$8, $$9);
         if (Mth.abs($$10 - this.center.getY()) <= 96) {
            $$4.set($$8, $$10, $$9);
            if (!$$0.isVillage($$4) || $$2 <= 7) {
               int $$11 = 10;
               if ($$0.hasChunksAt($$4.getX() - 10, $$4.getZ() - 10, $$4.getX() + 10, $$4.getZ() + 10)
                  && $$0.isPositionEntityTicking($$4)
                  && (
                     RAVAGER_SPAWN_PLACEMENT_TYPE.isSpawnPositionOk($$0, $$4, net.minecraft.world.entity.EntityType.RAVAGER)
                        || $$0.getBlockState($$4.below()).is(Blocks.SNOW) && $$0.getBlockState($$4).isAir()
                  )) {
                  return $$4;
               }
            }
         }
      }

      return null;
   }

   private boolean addWaveMob(ServerLevel $$0, int $$1, Raider $$2) {
      return this.addWaveMob($$0, $$1, $$2, true);
   }

   public boolean addWaveMob(ServerLevel $$0, int $$1, Raider $$2, boolean $$3) {
      this.groupRaiderMap.computeIfAbsent($$1, $$0x -> Sets.newHashSet());
      Set<Raider> $$4 = this.groupRaiderMap.get($$1);
      Raider $$5 = null;

      for (Raider $$6 : $$4) {
         if ($$6.getUUID().equals($$2.getUUID())) {
            $$5 = $$6;
            break;
         }
      }

      if ($$5 != null) {
         $$4.remove($$5);
         $$4.add($$2);
      }

      $$4.add($$2);
      if ($$3) {
         this.totalHealth = this.totalHealth + $$2.getHealth();
      }

      this.updateBossbar();
      this.setDirty($$0);
      return true;
   }

   public void setLeader(int $$0, Raider $$1) {
      this.groupToLeaderMap.put($$0, $$1);
      $$1.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, getOminousBannerInstance($$1.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
      $$1.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 2.0F);
   }

   public void removeLeader(int $$0) {
      this.groupToLeaderMap.remove($$0);
   }

   public BlockPos getCenter() {
      return this.center;
   }

   private void setCenter(BlockPos $$0) {
      this.center = $$0;
   }

   private int getDefaultNumSpawns(Raid.RaiderType $$0, int $$1, boolean $$2) {
      return $$2 ? $$0.spawnsPerWaveBeforeBonus[this.numGroups] : $$0.spawnsPerWaveBeforeBonus[$$1];
   }

   private int getPotentialBonusSpawns(Raid.RaiderType $$0, RandomSource $$1, int $$2, DifficultyInstance $$3, boolean $$4) {
      Difficulty $$5 = $$3.getDifficulty();
      boolean $$6 = $$5 == Difficulty.EASY;
      boolean $$7 = $$5 == Difficulty.NORMAL;
      int $$9;
      switch ($$0) {
         case VINDICATOR:
         case PILLAGER:
            if ($$6) {
               $$9 = $$1.nextInt(2);
            } else if ($$7) {
               $$9 = 1;
            } else {
               $$9 = 2;
            }
            break;
         case EVOKER:
         default:
            return 0;
         case WITCH:
            if ($$6 || $$2 <= 2 || $$2 == 4) {
               return 0;
            }

            $$9 = 1;
            break;
         case RAVAGER:
            $$9 = !$$6 && $$4 ? 1 : 0;
      }

      return $$9 > 0 ? $$1.nextInt($$9 + 1) : 0;
   }

   public boolean isActive() {
      return this.active;
   }

   public int getNumGroups(Difficulty $$0) {
      return switch ($$0) {
         case PEACEFUL -> 0;
         case EASY -> 3;
         case NORMAL -> 5;
         case HARD -> 7;
         default -> throw new MatchException(null, null);
      };
   }

   public float getEnchantOdds() {
      int $$0 = this.getRaidOmenLevel();
      if ($$0 == 2) {
         return 0.1F;
      } else if ($$0 == 3) {
         return 0.25F;
      } else if ($$0 == 4) {
         return 0.5F;
      } else {
         return $$0 == 5 ? 0.75F : 0.0F;
      }
   }

   public void addHeroOfTheVillage(net.minecraft.world.entity.Entity $$0) {
      this.heroesOfTheVillage.add($$0.getUUID());
   }

   static enum RaidStatus implements StringRepresentable {
      ONGOING("ongoing"),
      VICTORY("victory"),
      LOSS("loss"),
      STOPPED("stopped");

      public static final Codec<Raid.RaidStatus> CODEC = StringRepresentable.fromEnum(Raid.RaidStatus::values);
      private final String name;

      private RaidStatus(final String $$0) {
         this.name = $$0;
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   static enum RaiderType {
      VINDICATOR(net.minecraft.world.entity.EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
      EVOKER(net.minecraft.world.entity.EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
      PILLAGER(net.minecraft.world.entity.EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
      WITCH(net.minecraft.world.entity.EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
      RAVAGER(net.minecraft.world.entity.EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

      static final Raid.RaiderType[] VALUES = values();
      final net.minecraft.world.entity.EntityType<? extends Raider> entityType;
      final int[] spawnsPerWaveBeforeBonus;

      private RaiderType(final net.minecraft.world.entity.EntityType<? extends Raider> $$0, final int[] $$1) {
         this.entityType = $$0;
         this.spawnsPerWaveBeforeBonus = $$1;
      }
   }
}
