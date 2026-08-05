package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.FileUtil;
import net.minecraft.util.Util;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.NaturalSpawner.SpawnState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import org.slf4j.Logger;

public class ServerChunkCache extends ChunkSource {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final DistanceManager distanceManager;
   private final ServerLevel level;
   final Thread mainThread;
   final ThreadedLevelLightEngine lightEngine;
   private final ServerChunkCache.MainThreadExecutor mainThreadProcessor;
   public final ChunkMap chunkMap;
   private final DimensionDataStorage dataStorage;
   private final TicketStorage ticketStorage;
   private long lastInhabitedUpdate;
   private boolean spawnEnemies = true;
   private static final int CACHE_SIZE = 4;
   private final long[] lastChunkPos = new long[4];
   private final ChunkStatus[] lastChunkStatus = new ChunkStatus[4];
   private final ChunkAccess[] lastChunk = new ChunkAccess[4];
   private final List<LevelChunk> spawningChunks = new ObjectArrayList();
   private final Set<ChunkHolder> chunkHoldersToBroadcast = new ReferenceOpenHashSet();
   @VisibleForDebug
   
   private SpawnState lastSpawnState;

   public ServerChunkCache(
      ServerLevel $$0,
      LevelStorageAccess $$1,
      DataFixer $$2,
      StructureTemplateManager $$3,
      Executor $$4,
      ChunkGenerator $$5,
      int $$6,
      int $$7,
      boolean $$8,
      ChunkStatusUpdateListener $$9,
      Supplier<DimensionDataStorage> $$10
   ) {
      this.level = $$0;
      this.mainThreadProcessor = new ServerChunkCache.MainThreadExecutor($$0);
      this.mainThread = Thread.currentThread();
      Path $$11 = $$1.getDimensionPath($$0.dimension()).resolve("data");

      try {
         FileUtil.createDirectoriesSafe($$11);
      } catch (IOException var14) {
         LOGGER.error("Failed to create dimension data storage directory", var14);
      }

      this.dataStorage = new DimensionDataStorage($$11, $$2, $$0.registryAccess());
      this.ticketStorage = (TicketStorage)this.dataStorage.computeIfAbsent(TicketStorage.TYPE);
      this.chunkMap = new ChunkMap($$0, $$1, $$2, $$3, $$4, this.mainThreadProcessor, this, $$5, $$9, $$10, this.ticketStorage, $$6, $$8);
      this.lightEngine = this.chunkMap.getLightEngine();
      this.distanceManager = this.chunkMap.getDistanceManager();
      this.distanceManager.updateSimulationDistance($$7);
      this.clearCache();
   }

   public ThreadedLevelLightEngine getLightEngine() {
      return this.lightEngine;
   }

   
   private ChunkHolder getVisibleChunkIfPresent(long $$0) {
      return this.chunkMap.getVisibleChunkIfPresent($$0);
   }

   private void storeInCache(long $$0, ChunkAccess $$1, ChunkStatus $$2) {
      for (int $$3 = 3; $$3 > 0; $$3--) {
         this.lastChunkPos[$$3] = this.lastChunkPos[$$3 - 1];
         this.lastChunkStatus[$$3] = this.lastChunkStatus[$$3 - 1];
         this.lastChunk[$$3] = this.lastChunk[$$3 - 1];
      }

      this.lastChunkPos[0] = $$0;
      this.lastChunkStatus[0] = $$2;
      this.lastChunk[0] = $$1;
   }

   
   public ChunkAccess getChunk(int $$0, int $$1, ChunkStatus $$2, boolean $$3) {
      if (Thread.currentThread() != this.mainThread) {
         return CompletableFuture.<ChunkAccess>supplyAsync(() -> this.getChunk($$0, $$1, $$2, $$3), this.mainThreadProcessor).join();
      } else {
         ProfilerFiller $$4 = Profiler.get();
         $$4.incrementCounter("getChunk");
         long $$5 = ChunkPos.asLong($$0, $$1);

         for (int $$6 = 0; $$6 < 4; $$6++) {
            if ($$5 == this.lastChunkPos[$$6] && $$2 == this.lastChunkStatus[$$6]) {
               ChunkAccess $$7 = this.lastChunk[$$6];
               if ($$7 != null || !$$3) {
                  return $$7;
               }
            }
         }

         $$4.incrementCounter("getChunkCacheMiss");
         CompletableFuture<ChunkResult<ChunkAccess>> $$8 = this.getChunkFutureMainThread($$0, $$1, $$2, $$3);
         this.mainThreadProcessor.managedBlock($$8::isDone);
         ChunkResult<ChunkAccess> $$9 = $$8.join();
         ChunkAccess $$10 = $$9.orElse(null);
         if ($$10 == null && $$3) {
            throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + $$9.getError()));
         } else {
            this.storeInCache($$5, $$10, $$2);
            return $$10;
         }
      }
   }

   
   public LevelChunk getChunkNow(int $$0, int $$1) {
      if (Thread.currentThread() != this.mainThread) {
         return null;
      } else {
         Profiler.get().incrementCounter("getChunkNow");
         long $$2 = ChunkPos.asLong($$0, $$1);

         for (int $$3 = 0; $$3 < 4; $$3++) {
            if ($$2 == this.lastChunkPos[$$3] && this.lastChunkStatus[$$3] == ChunkStatus.FULL) {
               ChunkAccess $$4 = this.lastChunk[$$3];
               return $$4 instanceof LevelChunk ? (LevelChunk)$$4 : null;
            }
         }

         ChunkHolder $$5 = this.getVisibleChunkIfPresent($$2);
         if ($$5 == null) {
            return null;
         } else {
            ChunkAccess $$6 = $$5.getChunkIfPresent(ChunkStatus.FULL);
            if ($$6 != null) {
               this.storeInCache($$2, $$6, ChunkStatus.FULL);
               if ($$6 instanceof LevelChunk) {
                  return (LevelChunk)$$6;
               }
            }

            return null;
         }
      }
   }

   private void clearCache() {
      Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
      Arrays.fill(this.lastChunkStatus, null);
      Arrays.fill(this.lastChunk, null);
   }

   public CompletableFuture<ChunkResult<ChunkAccess>> getChunkFuture(int $$0, int $$1, ChunkStatus $$2, boolean $$3) {
      boolean $$4 = Thread.currentThread() == this.mainThread;
      CompletableFuture<ChunkResult<ChunkAccess>> $$5;
      if ($$4) {
         $$5 = this.getChunkFutureMainThread($$0, $$1, $$2, $$3);
         this.mainThreadProcessor.managedBlock($$5::isDone);
      } else {
         $$5 = CompletableFuture.<CompletableFuture<ChunkResult<ChunkAccess>>>supplyAsync(
               () -> this.getChunkFutureMainThread($$0, $$1, $$2, $$3), this.mainThreadProcessor
            )
            .thenCompose($$0x -> $$0x);
      }

      return $$5;
   }

   private CompletableFuture<ChunkResult<ChunkAccess>> getChunkFutureMainThread(int $$0, int $$1, ChunkStatus $$2, boolean $$3) {
      ChunkPos $$4 = new ChunkPos($$0, $$1);
      long $$5 = $$4.toLong();
      int $$6 = ChunkLevel.byStatus($$2);
      ChunkHolder $$7 = this.getVisibleChunkIfPresent($$5);
      if ($$3) {
         this.addTicket(new Ticket(TicketType.UNKNOWN, $$6), $$4);
         if (this.chunkAbsent($$7, $$6)) {
            ProfilerFiller $$8 = Profiler.get();
            $$8.push("chunkLoad");
            this.runDistanceManagerUpdates();
            $$7 = this.getVisibleChunkIfPresent($$5);
            $$8.pop();
            if (this.chunkAbsent($$7, $$6)) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
            }
         }
      }

      return this.chunkAbsent($$7, $$6) ? GenerationChunkHolder.UNLOADED_CHUNK_FUTURE : $$7.scheduleChunkGenerationTask($$2, this.chunkMap);
   }

   private boolean chunkAbsent(ChunkHolder $$0, int $$1) {
      return $$0 == null || $$0.getTicketLevel() > $$1;
   }

   public boolean hasChunk(int $$0, int $$1) {
      ChunkHolder $$2 = this.getVisibleChunkIfPresent(new ChunkPos($$0, $$1).toLong());
      int $$3 = ChunkLevel.byStatus(ChunkStatus.FULL);
      return !this.chunkAbsent($$2, $$3);
   }

   
   public LightChunk getChunkForLighting(int $$0, int $$1) {
      long $$2 = ChunkPos.asLong($$0, $$1);
      ChunkHolder $$3 = this.getVisibleChunkIfPresent($$2);
      return $$3 == null ? null : $$3.getChunkIfPresentUnchecked(ChunkStatus.INITIALIZE_LIGHT.getParent());
   }

   public Level getLevel() {
      return this.level;
   }

   public boolean pollTask() {
      return this.mainThreadProcessor.pollTask();
   }

   boolean runDistanceManagerUpdates() {
      boolean $$0 = this.distanceManager.runAllUpdates(this.chunkMap);
      boolean $$1 = this.chunkMap.promoteChunkMap();
      this.chunkMap.runGenerationTasks();
      if (!$$0 && !$$1) {
         return false;
      } else {
         this.clearCache();
         return true;
      }
   }

   public boolean isPositionTicking(long $$0) {
      if (!this.level.shouldTickBlocksAt($$0)) {
         return false;
      } else {
         ChunkHolder $$1 = this.getVisibleChunkIfPresent($$0);
         return $$1 == null ? false : $$1.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).isSuccess();
      }
   }

   public void save(boolean $$0) {
      this.runDistanceManagerUpdates();
      this.chunkMap.saveAllChunks($$0);
   }

   public void close() throws IOException {
      this.save(true);
      this.dataStorage.close();
      this.lightEngine.close();
      this.chunkMap.close();
   }

   public void tick(BooleanSupplier $$0, boolean $$1) {
      ProfilerFiller $$2 = Profiler.get();
      $$2.push("purge");
      if (this.level.tickRateManager().runsNormally() || !$$1) {
         this.ticketStorage.purgeStaleTickets(this.chunkMap);
      }

      this.runDistanceManagerUpdates();
      $$2.popPush("chunks");
      if ($$1) {
         this.tickChunks();
         this.chunkMap.tick();
      }

      $$2.popPush("unload");
      this.chunkMap.tick($$0);
      $$2.pop();
      this.clearCache();
   }

   private void tickChunks() {
      long $$0 = this.level.getGameTime();
      long $$1 = $$0 - this.lastInhabitedUpdate;
      this.lastInhabitedUpdate = $$0;
      if (!this.level.isDebug()) {
         ProfilerFiller $$2 = Profiler.get();
         $$2.push("pollingChunks");
         if (this.level.tickRateManager().runsNormally()) {
            $$2.push("tickingChunks");
            this.tickChunks($$2, $$1);
            $$2.pop();
         }

         this.broadcastChangedChunks($$2);
         $$2.pop();
      }
   }

   private void broadcastChangedChunks(ProfilerFiller $$0) {
      $$0.push("broadcast");

      for (ChunkHolder $$1 : this.chunkHoldersToBroadcast) {
         LevelChunk $$2 = $$1.getTickingChunk();
         if ($$2 != null) {
            $$1.broadcastChanges($$2);
         }
      }

      this.chunkHoldersToBroadcast.clear();
      $$0.pop();
   }

   private void tickChunks(ProfilerFiller $$0, long $$1) {
      $$0.push("naturalSpawnCount");
      int $$2 = this.distanceManager.getNaturalSpawnChunkCount();
      SpawnState $$3 = NaturalSpawner.createState($$2, this.level.getAllEntities(), this::getFullChunk, new LocalMobCapCalculator(this.chunkMap));
      this.lastSpawnState = $$3;
      boolean $$4 = (Boolean)this.level.getGameRules().get(GameRules.SPAWN_MOBS);
      int $$5 = (Integer)this.level.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
      List<MobCategory> $$7;
      if ($$4) {
         boolean $$6 = this.level.getGameTime() % 400L == 0L;
         $$7 = NaturalSpawner.getFilteredSpawningCategories($$3, true, this.spawnEnemies, $$6);
      } else {
         $$7 = List.of();
      }

      List<LevelChunk> $$9 = this.spawningChunks;

      try {
         $$0.popPush("filteringSpawningChunks");
         this.chunkMap.collectSpawningChunks($$9);
         $$0.popPush("shuffleSpawningChunks");
         Util.shuffle($$9, this.level.random);
         $$0.popPush("tickSpawningChunks");

         for (LevelChunk $$10 : $$9) {
            this.tickSpawningChunk($$10, $$1, $$7, $$3);
         }
      } finally {
         $$9.clear();
      }

      $$0.popPush("tickTickingChunks");
      this.chunkMap.forEachBlockTickingChunk($$1x -> this.level.tickChunk($$1x, $$5));
      if ($$4) {
         $$0.popPush("customSpawners");
         this.level.tickCustomSpawners(this.spawnEnemies);
      }

      $$0.pop();
   }

   private void tickSpawningChunk(LevelChunk $$0, long $$1, List<MobCategory> $$2, SpawnState $$3) {
      ChunkPos $$4 = $$0.getPos();
      $$0.incrementInhabitedTime($$1);
      if (this.distanceManager.inEntityTickingRange($$4.toLong())) {
         this.level.tickThunder($$0);
      }

      if (!$$2.isEmpty()) {
         if (this.level.canSpawnEntitiesInChunk($$4)) {
            NaturalSpawner.spawnForChunk(this.level, $$0, $$3, $$2);
         }
      }
   }

   private void getFullChunk(long $$0, Consumer<LevelChunk> $$1) {
      ChunkHolder $$2 = this.getVisibleChunkIfPresent($$0);
      if ($$2 != null) {
         $$2.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).ifSuccess($$1);
      }
   }

   public String gatherStats() {
      return Integer.toString(this.getLoadedChunksCount());
   }

   @VisibleForTesting
   public int getPendingTasksCount() {
      return this.mainThreadProcessor.getPendingTasksCount();
   }

   public ChunkGenerator getGenerator() {
      return this.chunkMap.generator();
   }

   public ChunkGeneratorStructureState getGeneratorState() {
      return this.chunkMap.generatorState();
   }

   public RandomState randomState() {
      return this.chunkMap.randomState();
   }

   public int getLoadedChunksCount() {
      return this.chunkMap.size();
   }

   public void blockChanged(BlockPos $$0) {
      int $$1 = SectionPos.blockToSectionCoord($$0.getX());
      int $$2 = SectionPos.blockToSectionCoord($$0.getZ());
      ChunkHolder $$3 = this.getVisibleChunkIfPresent(ChunkPos.asLong($$1, $$2));
      if ($$3 != null && $$3.blockChanged($$0)) {
         this.chunkHoldersToBroadcast.add($$3);
      }
   }

   public void onLightUpdate(LightLayer $$0, SectionPos $$1) {
      this.mainThreadProcessor.execute(() -> {
         ChunkHolder $$2 = this.getVisibleChunkIfPresent($$1.chunk().toLong());
         if ($$2 != null && $$2.sectionLightChanged($$0, $$1.y())) {
            this.chunkHoldersToBroadcast.add($$2);
         }
      });
   }

   public boolean hasActiveTickets() {
      return this.ticketStorage.shouldKeepDimensionActive();
   }

   public void addTicket(Ticket $$0, ChunkPos $$1) {
      this.ticketStorage.addTicket($$0, $$1);
   }

   public CompletableFuture<?> addTicketAndLoadWithRadius(TicketType $$0, ChunkPos $$1, int $$2) {
      if (!$$0.doesLoad()) {
         throw new IllegalStateException("Ticket type " + $$0 + " does not trigger chunk loading");
      } else if ($$0.canExpireIfUnloaded()) {
         throw new IllegalStateException("Ticket type " + $$0 + " can expire before it loads, cannot fetch asynchronously");
      } else {
         this.addTicketWithRadius($$0, $$1, $$2);
         this.runDistanceManagerUpdates();
         ChunkHolder $$3 = this.getVisibleChunkIfPresent($$1.toLong());
         Objects.requireNonNull($$3, "No chunk was scheduled for loading");
         return this.chunkMap.getChunkRangeFuture($$3, $$2, $$0x -> ChunkStatus.FULL);
      }
   }

   public void addTicketWithRadius(TicketType $$0, ChunkPos $$1, int $$2) {
      this.ticketStorage.addTicketWithRadius($$0, $$1, $$2);
   }

   public void removeTicketWithRadius(TicketType $$0, ChunkPos $$1, int $$2) {
      this.ticketStorage.removeTicketWithRadius($$0, $$1, $$2);
   }

   public boolean updateChunkForced(ChunkPos $$0, boolean $$1) {
      return this.ticketStorage.updateChunkForced($$0, $$1);
   }

   public LongSet getForceLoadedChunks() {
      return this.ticketStorage.getForceLoadedChunks();
   }

   public void move(ServerPlayer $$0) {
      if (!$$0.isRemoved()) {
         this.chunkMap.move($$0);
         if ($$0.isReceivingWaypoints()) {
            this.level.getWaypointManager().updatePlayer($$0);
         }
      }
   }

   public void removeEntity(Entity $$0) {
      this.chunkMap.removeEntity($$0);
   }

   public void addEntity(Entity $$0) {
      this.chunkMap.addEntity($$0);
   }

   public void sendToTrackingPlayersAndSelf(Entity $$0, Packet<? super ClientGamePacketListener> $$1) {
      this.chunkMap.sendToTrackingPlayersAndSelf($$0, $$1);
   }

   public void sendToTrackingPlayers(Entity $$0, Packet<? super ClientGamePacketListener> $$1) {
      this.chunkMap.sendToTrackingPlayers($$0, $$1);
   }

   public void setViewDistance(int $$0) {
      this.chunkMap.setServerViewDistance($$0);
   }

   public void setSimulationDistance(int $$0) {
      this.distanceManager.updateSimulationDistance($$0);
   }

   public void setSpawnSettings(boolean $$0) {
      this.spawnEnemies = $$0;
   }

   public String getChunkDebugData(ChunkPos $$0) {
      return this.chunkMap.getChunkDebugData($$0);
   }

   public DimensionDataStorage getDataStorage() {
      return this.dataStorage;
   }

   public PoiManager getPoiManager() {
      return this.chunkMap.getPoiManager();
   }

   public ChunkScanAccess chunkScanner() {
      return this.chunkMap.chunkScanner();
   }

   @VisibleForDebug
   
   public SpawnState getLastSpawnState() {
      return this.lastSpawnState;
   }

   public void deactivateTicketsOnClosing() {
      this.ticketStorage.deactivateTicketsOnClosing();
   }

   public void onChunkReadyToSend(ChunkHolder $$0) {
      if ($$0.hasChangesToBroadcast()) {
         this.chunkHoldersToBroadcast.add($$0);
      }
   }

   final class MainThreadExecutor extends BlockableEventLoop<Runnable> {
      MainThreadExecutor(final Level $$0) {
         super("Chunk source main thread executor for " + $$0.dimension().identifier());
      }

      public void managedBlock(BooleanSupplier $$0) {
         super.managedBlock(() -> net.minecraft.server.MinecraftServer.throwIfFatalException() && $$0.getAsBoolean());
      }

      public Runnable wrapRunnable(Runnable $$0) {
         return $$0;
      }

      protected boolean shouldRun(Runnable $$0) {
         return true;
      }

      protected boolean scheduleExecutables() {
         return true;
      }

      protected Thread getRunningThread() {
         return ServerChunkCache.this.mainThread;
      }

      protected void doRunTask(Runnable $$0) {
         Profiler.get().incrementCounter("runTask");
         super.doRunTask($$0);
      }

      protected boolean pollTask() {
         if (ServerChunkCache.this.runDistanceManagerUpdates()) {
            return true;
         } else {
            ServerChunkCache.this.lightEngine.tryScheduleUpdate();
            return super.pollTask();
         }
      }
   }
}
