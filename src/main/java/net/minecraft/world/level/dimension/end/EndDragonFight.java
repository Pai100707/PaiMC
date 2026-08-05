package net.minecraft.world.level.dimension.end;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class EndDragonFight {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_TICKS_BEFORE_DRAGON_RESPAWN = 1200;
   private static final int TIME_BETWEEN_CRYSTAL_SCANS = 100;
   public static final int TIME_BETWEEN_PLAYER_SCANS = 20;
   private static final int ARENA_SIZE_CHUNKS = 8;
   public static final int ARENA_TICKET_LEVEL = 9;
   private static final int GATEWAY_COUNT = 20;
   private static final int GATEWAY_DISTANCE = 96;
   public static final int DRAGON_SPAWN_Y = 128;
   private final Predicate<Entity> validPlayer;
   private final ServerBossEvent dragonEvent = (ServerBossEvent)new ServerBossEvent(
         Component.translatable("entity.minecraft.ender_dragon"), BossBarColor.PINK, BossBarOverlay.PROGRESS
      )
      .setPlayBossMusic(true)
      .setCreateWorldFog(true);
   private final ServerLevel level;
   private final BlockPos origin;
   private final ObjectArrayList<Integer> gateways = new ObjectArrayList();
   private final BlockPattern exitPortalPattern;
   private int ticksSinceDragonSeen;
   private int crystalsAlive;
   private int ticksSinceCrystalsScanned;
   private int ticksSinceLastPlayerScan = 21;
   private boolean dragonKilled;
   private boolean previouslyKilled;
   private boolean skipArenaLoadedCheck = false;
   
   private UUID dragonUUID;
   private boolean needsStateScanning = true;
   
   private BlockPos portalLocation;
   
   private DragonRespawnAnimation respawnStage;
   private int respawnTime;
   
   private List<EndCrystal> respawnCrystals;

   public EndDragonFight(ServerLevel $$0, long $$1, EndDragonFight.Data $$2) {
      this($$0, $$1, $$2, BlockPos.ZERO);
   }

   public EndDragonFight(ServerLevel $$0, long $$1, EndDragonFight.Data $$2, BlockPos $$3) {
      this.level = $$0;
      this.origin = $$3;
      this.validPlayer = EntitySelector.ENTITY_STILL_ALIVE.and(EntitySelector.withinDistance($$3.getX(), 128 + $$3.getY(), $$3.getZ(), 192.0));
      this.needsStateScanning = $$2.needsStateScanning;
      this.dragonUUID = $$2.dragonUUID.orElse(null);
      this.dragonKilled = $$2.dragonKilled;
      this.previouslyKilled = $$2.previouslyKilled;
      if ($$2.isRespawning) {
         this.respawnStage = DragonRespawnAnimation.START;
      }

      this.portalLocation = $$2.exitPortalLocation.orElse(null);
      this.gateways.addAll($$2.gateways.orElseGet(() -> {
         ObjectArrayList<Integer> $$1x = new ObjectArrayList(ContiguousSet.create(Range.closedOpen(0, 20), DiscreteDomain.integers()));
         Util.shuffle($$1x, RandomSource.create($$1));
         return $$1x;
      }));
      this.exitPortalPattern = BlockPatternBuilder.start()
         .aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
         .aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
         .aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
         .aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ")
         .aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ")
         .where('#', BlockInWorld.hasState(BlockPredicate.forBlock(Blocks.BEDROCK)))
         .build();
   }

   @Deprecated
   @VisibleForTesting
   public void skipArenaLoadedCheck() {
      this.skipArenaLoadedCheck = true;
   }

   public EndDragonFight.Data saveData() {
      return new EndDragonFight.Data(
         this.needsStateScanning,
         this.dragonKilled,
         this.previouslyKilled,
         false,
         Optional.ofNullable(this.dragonUUID),
         Optional.ofNullable(this.portalLocation),
         Optional.of(this.gateways)
      );
   }

   public void tick() {
      this.dragonEvent.setVisible(!this.dragonKilled);
      if (++this.ticksSinceLastPlayerScan >= 20) {
         this.updatePlayers();
         this.ticksSinceLastPlayerScan = 0;
      }

      if (!this.dragonEvent.getPlayers().isEmpty()) {
         this.level.getChunkSource().addTicketWithRadius(TicketType.DRAGON, new net.minecraft.world.level.ChunkPos(0, 0), 9);
         boolean $$0 = this.isArenaLoaded();
         if (this.needsStateScanning && $$0) {
            this.scanState();
            this.needsStateScanning = false;
         }

         if (this.respawnStage != null) {
            if (this.respawnCrystals == null && $$0) {
               this.respawnStage = null;
               this.tryRespawn();
            }

            this.respawnStage.tick(this.level, this, this.respawnCrystals, this.respawnTime++, this.portalLocation);
         }

         if (!this.dragonKilled) {
            if ((this.dragonUUID == null || ++this.ticksSinceDragonSeen >= 1200) && $$0) {
               this.findOrCreateDragon();
               this.ticksSinceDragonSeen = 0;
            }

            if (++this.ticksSinceCrystalsScanned >= 100 && $$0) {
               this.updateCrystalCount();
               this.ticksSinceCrystalsScanned = 0;
            }
         }
      } else {
         this.level.getChunkSource().removeTicketWithRadius(TicketType.DRAGON, new net.minecraft.world.level.ChunkPos(0, 0), 9);
      }
   }

   private void scanState() {
      LOGGER.info("Scanning for legacy world dragon fight...");
      boolean $$0 = this.hasActiveExitPortal();
      if ($$0) {
         LOGGER.info("Found that the dragon has been killed in this world already.");
         this.previouslyKilled = true;
      } else {
         LOGGER.info("Found that the dragon has not yet been killed in this world.");
         this.previouslyKilled = false;
         if (this.findExitPortal() == null) {
            this.spawnExitPortal(false);
         }
      }

      List<? extends EnderDragon> $$1 = this.level.getDragons();
      if ($$1.isEmpty()) {
         this.dragonKilled = true;
      } else {
         EnderDragon $$2 = $$1.get(0);
         this.dragonUUID = $$2.getUUID();
         LOGGER.info("Found that there's a dragon still alive ({})", $$2);
         this.dragonKilled = false;
         if (!$$0) {
            LOGGER.info("But we didn't have a portal, let's remove it.");
            $$2.discard();
            this.dragonUUID = null;
         }
      }

      if (!this.previouslyKilled && this.dragonKilled) {
         this.dragonKilled = false;
      }
   }

   private void findOrCreateDragon() {
      List<? extends EnderDragon> $$0 = this.level.getDragons();
      if ($$0.isEmpty()) {
         LOGGER.debug("Haven't seen the dragon, respawning it");
         this.createNewDragon();
      } else {
         LOGGER.debug("Haven't seen our dragon, but found another one to use.");
         this.dragonUUID = $$0.get(0).getUUID();
      }
   }

   protected void setRespawnStage(DragonRespawnAnimation $$0) {
      if (this.respawnStage == null) {
         throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");
      } else {
         this.respawnTime = 0;
         if ($$0 == DragonRespawnAnimation.END) {
            this.respawnStage = null;
            this.dragonKilled = false;
            EnderDragon $$1 = this.createNewDragon();
            if ($$1 != null) {
               for (ServerPlayer $$2 : this.dragonEvent.getPlayers()) {
                  CriteriaTriggers.SUMMONED_ENTITY.trigger($$2, $$1);
               }
            }
         } else {
            this.respawnStage = $$0;
         }
      }
   }

   private boolean hasActiveExitPortal() {
      for (int $$0 = -8; $$0 <= 8; $$0++) {
         for (int $$1 = -8; $$1 <= 8; $$1++) {
            LevelChunk $$2 = this.level.getChunk($$0, $$1);

            for (BlockEntity $$3 : $$2.getBlockEntities().values()) {
               if ($$3 instanceof TheEndPortalBlockEntity) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   
   private BlockPattern.BlockPatternMatch findExitPortal() {
      net.minecraft.world.level.ChunkPos $$0 = new net.minecraft.world.level.ChunkPos(this.origin);

      for (int $$1 = -8 + $$0.x; $$1 <= 8 + $$0.x; $$1++) {
         for (int $$2 = -8 + $$0.z; $$2 <= 8 + $$0.z; $$2++) {
            LevelChunk $$3 = this.level.getChunk($$1, $$2);

            for (BlockEntity $$4 : $$3.getBlockEntities().values()) {
               if ($$4 instanceof TheEndPortalBlockEntity) {
                  BlockPattern.BlockPatternMatch $$5 = this.exitPortalPattern.find(this.level, $$4.getBlockPos());
                  if ($$5 != null) {
                     BlockPos $$6 = $$5.getBlock(3, 3, 3).getPos();
                     if (this.portalLocation == null) {
                        this.portalLocation = $$6;
                     }

                     return $$5;
                  }
               }
            }
         }
      }

      BlockPos $$7 = EndPodiumFeature.getLocation(this.origin);
      int $$8 = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, $$7).getY();

      for (int $$9 = $$8; $$9 >= this.level.getMinY(); $$9--) {
         BlockPattern.BlockPatternMatch $$10 = this.exitPortalPattern.find(this.level, new BlockPos($$7.getX(), $$9, $$7.getZ()));
         if ($$10 != null) {
            if (this.portalLocation == null) {
               this.portalLocation = $$10.getBlock(3, 3, 3).getPos();
            }

            return $$10;
         }
      }

      return null;
   }

   private boolean isArenaLoaded() {
      if (this.skipArenaLoadedCheck) {
         return true;
      } else {
         net.minecraft.world.level.ChunkPos $$0 = new net.minecraft.world.level.ChunkPos(this.origin);

         for (int $$1 = -8 + $$0.x; $$1 <= 8 + $$0.x; $$1++) {
            for (int $$2 = 8 + $$0.z; $$2 <= 8 + $$0.z; $$2++) {
               ChunkAccess $$3 = this.level.getChunk($$1, $$2, ChunkStatus.FULL, false);
               if (!($$3 instanceof LevelChunk)) {
                  return false;
               }

               FullChunkStatus $$4 = ((LevelChunk)$$3).getFullStatus();
               if (!$$4.isOrAfter(FullChunkStatus.BLOCK_TICKING)) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private void updatePlayers() {
      Set<ServerPlayer> $$0 = Sets.newHashSet();

      for (ServerPlayer $$1 : this.level.getPlayers(this.validPlayer)) {
         this.dragonEvent.addPlayer($$1);
         $$0.add($$1);
      }

      Set<ServerPlayer> $$2 = Sets.newHashSet(this.dragonEvent.getPlayers());
      $$2.removeAll($$0);

      for (ServerPlayer $$3 : $$2) {
         this.dragonEvent.removePlayer($$3);
      }
   }

   private void updateCrystalCount() {
      this.ticksSinceCrystalsScanned = 0;
      this.crystalsAlive = 0;

      for (SpikeFeature.EndSpike $$0 : SpikeFeature.getSpikesForLevel(this.level)) {
         this.crystalsAlive = this.crystalsAlive + this.level.getEntitiesOfClass(EndCrystal.class, $$0.getTopBoundingBox()).size();
      }

      LOGGER.debug("Found {} end crystals still alive", this.crystalsAlive);
   }

   public void setDragonKilled(EnderDragon $$0) {
      if ($$0.getUUID().equals(this.dragonUUID)) {
         this.dragonEvent.setProgress(0.0F);
         this.dragonEvent.setVisible(false);
         this.spawnExitPortal(true);
         this.spawnNewGateway();
         if (!this.previouslyKilled) {
            this.level
               .setBlockAndUpdate(
                  this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.getLocation(this.origin)), Blocks.DRAGON_EGG.defaultBlockState()
               );
         }

         this.previouslyKilled = true;
         this.dragonKilled = true;
      }
   }

   @Deprecated
   @VisibleForTesting
   public void removeAllGateways() {
      this.gateways.clear();
   }

   private void spawnNewGateway() {
      if (!this.gateways.isEmpty()) {
         int $$0 = (Integer)this.gateways.remove(this.gateways.size() - 1);
         int $$1 = Mth.floor(96.0 * Math.cos(2.0 * (-Math.PI + (Math.PI / 20) * $$0)));
         int $$2 = Mth.floor(96.0 * Math.sin(2.0 * (-Math.PI + (Math.PI / 20) * $$0)));
         this.spawnNewGateway(new BlockPos($$1, 75, $$2));
      }
   }

   private void spawnNewGateway(BlockPos $$0) {
      this.level.levelEvent(3000, $$0, 0);
      this.level
         .registryAccess()
         .lookup(Registries.CONFIGURED_FEATURE)
         .flatMap($$0x -> $$0x.get(EndFeatures.END_GATEWAY_DELAYED))
         .ifPresent($$1 -> ((ConfiguredFeature)$$1.value()).place(this.level, this.level.getChunkSource().getGenerator(), RandomSource.create(), $$0));
   }

   private void spawnExitPortal(boolean $$0) {
      EndPodiumFeature $$1 = new EndPodiumFeature($$0);
      if (this.portalLocation == null) {
         this.portalLocation = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.origin)).below();

         while (this.level.getBlockState(this.portalLocation).is(Blocks.BEDROCK) && this.portalLocation.getY() > 63) {
            this.portalLocation = this.portalLocation.below();
         }

         this.portalLocation = this.portalLocation.atY(Math.max(this.level.getMinY() + 1, this.portalLocation.getY()));
      }

      if ($$1.place(FeatureConfiguration.NONE, this.level, this.level.getChunkSource().getGenerator(), RandomSource.create(), this.portalLocation)) {
         int $$2 = Mth.positiveCeilDiv(4, 16);
         this.level.getChunkSource().chunkMap.waitForLightBeforeSending(new net.minecraft.world.level.ChunkPos(this.portalLocation), $$2);
      }
   }

   
   private EnderDragon createNewDragon() {
      this.level.getChunkAt(new BlockPos(this.origin.getX(), 128 + this.origin.getY(), this.origin.getZ()));
      EnderDragon $$0 = (EnderDragon)EntityType.ENDER_DRAGON.create(this.level, EntitySpawnReason.EVENT);
      if ($$0 != null) {
         $$0.setDragonFight(this);
         $$0.setFightOrigin(this.origin);
         $$0.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
         $$0.snapTo(this.origin.getX(), 128 + this.origin.getY(), this.origin.getZ(), this.level.random.nextFloat() * 360.0F, 0.0F);
         this.level.addFreshEntity($$0);
         this.dragonUUID = $$0.getUUID();
      }

      return $$0;
   }

   public void updateDragon(EnderDragon $$0) {
      if ($$0.getUUID().equals(this.dragonUUID)) {
         this.dragonEvent.setProgress($$0.getHealth() / $$0.getMaxHealth());
         this.ticksSinceDragonSeen = 0;
         if ($$0.hasCustomName()) {
            this.dragonEvent.setName($$0.getDisplayName());
         }
      }
   }

   public int getCrystalsAlive() {
      return this.crystalsAlive;
   }

   public void onCrystalDestroyed(EndCrystal $$0, DamageSource $$1) {
      if (this.respawnStage != null && this.respawnCrystals.contains($$0)) {
         LOGGER.debug("Aborting respawn sequence");
         this.respawnStage = null;
         this.respawnTime = 0;
         this.resetSpikeCrystals();
         this.spawnExitPortal(true);
      } else {
         this.updateCrystalCount();
         if (this.level.getEntity(this.dragonUUID) instanceof EnderDragon $$3) {
            $$3.onCrystalDestroyed(this.level, $$0, $$0.blockPosition(), $$1);
         }
      }
   }

   public boolean hasPreviouslyKilledDragon() {
      return this.previouslyKilled;
   }

   public void tryRespawn() {
      if (this.dragonKilled && this.respawnStage == null) {
         BlockPos $$0 = this.portalLocation;
         if ($$0 == null) {
            LOGGER.debug("Tried to respawn, but need to find the portal first.");
            BlockPattern.BlockPatternMatch $$1 = this.findExitPortal();
            if ($$1 == null) {
               LOGGER.debug("Couldn't find a portal, so we made one.");
               this.spawnExitPortal(true);
            } else {
               LOGGER.debug("Found the exit portal & saved its location for next time.");
            }

            $$0 = this.portalLocation;
         }

         List<EndCrystal> $$2 = Lists.newArrayList();
         BlockPos $$3 = $$0.above(1);

         for (Direction $$4 : Plane.HORIZONTAL) {
            List<EndCrystal> $$5 = this.level.getEntitiesOfClass(EndCrystal.class, new AABB($$3.relative($$4, 2)));
            if ($$5.isEmpty()) {
               return;
            }

            $$2.addAll($$5);
         }

         LOGGER.debug("Found all crystals, respawning dragon.");
         this.respawnDragon($$2);
      }
   }

   private void respawnDragon(List<EndCrystal> $$0) {
      if (this.dragonKilled && this.respawnStage == null) {
         for (BlockPattern.BlockPatternMatch $$1 = this.findExitPortal(); $$1 != null; $$1 = this.findExitPortal()) {
            for (int $$2 = 0; $$2 < this.exitPortalPattern.getWidth(); $$2++) {
               for (int $$3 = 0; $$3 < this.exitPortalPattern.getHeight(); $$3++) {
                  for (int $$4 = 0; $$4 < this.exitPortalPattern.getDepth(); $$4++) {
                     BlockInWorld $$5 = $$1.getBlock($$2, $$3, $$4);
                     if ($$5.getState().is(Blocks.BEDROCK) || $$5.getState().is(Blocks.END_PORTAL)) {
                        this.level.setBlockAndUpdate($$5.getPos(), Blocks.END_STONE.defaultBlockState());
                     }
                  }
               }
            }
         }

         this.respawnStage = DragonRespawnAnimation.START;
         this.respawnTime = 0;
         this.spawnExitPortal(false);
         this.respawnCrystals = $$0;
      }
   }

   public void resetSpikeCrystals() {
      for (SpikeFeature.EndSpike $$0 : SpikeFeature.getSpikesForLevel(this.level)) {
         for (EndCrystal $$2 : this.level.getEntitiesOfClass(EndCrystal.class, $$0.getTopBoundingBox())) {
            $$2.setInvulnerable(false);
            $$2.setBeamTarget(null);
         }
      }
   }

   
   public UUID getDragonUUID() {
      return this.dragonUUID;
   }

   public record Data(
      boolean needsStateScanning,
      boolean dragonKilled,
      boolean previouslyKilled,
      boolean isRespawning,
      Optional<UUID> dragonUUID,
      Optional<BlockPos> exitPortalLocation,
      Optional<List<Integer>> gateways
   ) {
      public static final Codec<EndDragonFight.Data> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.BOOL.fieldOf("NeedsStateScanning").orElse(true).forGetter(EndDragonFight.Data::needsStateScanning),
               Codec.BOOL.fieldOf("DragonKilled").orElse(false).forGetter(EndDragonFight.Data::dragonKilled),
               Codec.BOOL.fieldOf("PreviouslyKilled").orElse(false).forGetter(EndDragonFight.Data::previouslyKilled),
               Codec.BOOL.lenientOptionalFieldOf("IsRespawning", false).forGetter(EndDragonFight.Data::isRespawning),
               UUIDUtil.CODEC.lenientOptionalFieldOf("Dragon").forGetter(EndDragonFight.Data::dragonUUID),
               BlockPos.CODEC.lenientOptionalFieldOf("ExitPortalLocation").forGetter(EndDragonFight.Data::exitPortalLocation),
               Codec.list(Codec.INT).lenientOptionalFieldOf("Gateways").forGetter(EndDragonFight.Data::gateways)
            )
            .apply($$0, EndDragonFight.Data::new)
      );
      public static final EndDragonFight.Data DEFAULT = new EndDragonFight.Data(true, false, false, false, Optional.empty(), Optional.empty(), Optional.empty());
   }
}
