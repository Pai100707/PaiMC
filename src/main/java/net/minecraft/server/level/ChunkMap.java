package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ConsecutiveExecutor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkMap extends SimpleRegionStorage implements ChunkHolder.PlayerProvider, GeneratingChunkMap {
   private static final ChunkResult<List<ChunkAccess>> UNLOADED_CHUNK_LIST_RESULT = ChunkResult.error("Unloaded chunks found in range");
   private static final CompletableFuture<ChunkResult<List<ChunkAccess>>> UNLOADED_CHUNK_LIST_FUTURE = CompletableFuture.completedFuture(
      UNLOADED_CHUNK_LIST_RESULT
   );
   private static final byte CHUNK_TYPE_REPLACEABLE = -1;
   private static final byte CHUNK_TYPE_UNKNOWN = 0;
   private static final byte CHUNK_TYPE_FULL = 1;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int CHUNK_SAVED_PER_TICK = 200;
   private static final int CHUNK_SAVED_EAGERLY_PER_TICK = 20;
   private static final int EAGER_CHUNK_SAVE_COOLDOWN_IN_MILLIS = 10000;
   private static final int MAX_ACTIVE_CHUNK_WRITES = 128;
   public static final int MIN_VIEW_DISTANCE = 2;
   public static final int MAX_VIEW_DISTANCE = 32;
   public static final int FORCED_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
   private final Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = new Long2ObjectLinkedOpenHashMap();
   private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
   private final Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads = new Long2ObjectLinkedOpenHashMap();
   private final List<ChunkGenerationTask> pendingGenerationTasks = new ArrayList<>();
   final ServerLevel level;
   private final ThreadedLevelLightEngine lightEngine;
   private final BlockableEventLoop<Runnable> mainThreadExecutor;
   private final RandomState randomState;
   private final ChunkGeneratorStructureState chunkGeneratorState;
   private final TicketStorage ticketStorage;
   private final PoiManager poiManager;
   final LongSet toDrop = new LongOpenHashSet();
   private boolean modified;
   private final ChunkTaskDispatcher worldgenTaskDispatcher;
   private final ChunkTaskDispatcher lightTaskDispatcher;
   private final ChunkStatusUpdateListener chunkStatusListener;
   private final ChunkMap.DistanceManager distanceManager;
   private final String storageName;
   private final PlayerMap playerMap = new PlayerMap();
   private final Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = new Int2ObjectOpenHashMap();
   private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
   private final Long2LongMap nextChunkSaveTime = new Long2LongOpenHashMap();
   private final LongSet chunksToEagerlySave = new LongLinkedOpenHashSet();
   private final Queue<Runnable> unloadQueue = Queues.newConcurrentLinkedQueue();
   private final AtomicInteger activeChunkWrites = new AtomicInteger();
   private int serverViewDistance;
   private final WorldGenContext worldGenContext;

   public ChunkMap(
      ServerLevel $$0,
      LevelStorageAccess $$1,
      DataFixer $$2,
      StructureTemplateManager $$3,
      Executor $$4,
      BlockableEventLoop<Runnable> $$5,
      LightChunkGetter $$6,
      ChunkGenerator $$7,
      ChunkStatusUpdateListener $$8,
      Supplier<DimensionDataStorage> $$9,
      TicketStorage $$10,
      int $$11,
      boolean $$12
   ) {
      super(
         new RegionStorageInfo($$1.getLevelId(), $$0.dimension(), "chunk"),
         $$1.getDimensionPath($$0.dimension()).resolve("region"),
         $$2,
         $$12,
         DataFixTypes.CHUNK,
         LegacyStructureDataHandler.getLegacyTagFixer($$0.dimension(), $$9, $$2)
      );
      Path $$13 = $$1.getDimensionPath($$0.dimension());
      this.storageName = $$13.getFileName().toString();
      this.level = $$0;
      RegistryAccess $$14 = $$0.registryAccess();
      long $$15 = $$0.getSeed();
      if ($$7 instanceof NoiseBasedChunkGenerator $$16) {
         this.randomState = RandomState.create((NoiseGeneratorSettings)$$16.generatorSettings().value(), $$14.lookupOrThrow(Registries.NOISE), $$15);
      } else {
         this.randomState = RandomState.create(NoiseGeneratorSettings.dummy(), $$14.lookupOrThrow(Registries.NOISE), $$15);
      }

      this.chunkGeneratorState = $$7.createState($$14.lookupOrThrow(Registries.STRUCTURE_SET), this.randomState, $$15);
      this.mainThreadExecutor = $$5;
      ConsecutiveExecutor $$17 = new ConsecutiveExecutor($$4, "worldgen");
      this.chunkStatusListener = $$8;
      ConsecutiveExecutor $$18 = new ConsecutiveExecutor($$4, "light");
      this.worldgenTaskDispatcher = new ChunkTaskDispatcher($$17, $$4);
      this.lightTaskDispatcher = new ChunkTaskDispatcher($$18, $$4);
      this.lightEngine = new ThreadedLevelLightEngine($$6, this, this.level.dimensionType().hasSkyLight(), $$18, this.lightTaskDispatcher);
      this.distanceManager = new ChunkMap.DistanceManager($$10, $$4, $$5);
      this.ticketStorage = $$10;
      this.poiManager = new PoiManager(
         new RegionStorageInfo($$1.getLevelId(), $$0.dimension(), "poi"), $$13.resolve("poi"), $$2, $$12, $$14, $$0.getServer(), $$0
      );
      this.setServerViewDistance($$11);
      this.worldGenContext = new WorldGenContext($$0, $$7, $$3, this.lightEngine, $$5, this::setChunkUnsaved);
   }

   private void setChunkUnsaved(ChunkPos $$0) {
      this.chunksToEagerlySave.add($$0.toLong());
   }

   protected ChunkGenerator generator() {
      return this.worldGenContext.generator();
   }

   protected ChunkGeneratorStructureState generatorState() {
      return this.chunkGeneratorState;
   }

   protected RandomState randomState() {
      return this.randomState;
   }

   public boolean isChunkTracked(ServerPlayer $$0, int $$1, int $$2) {
      return $$0.getChunkTrackingView().contains($$1, $$2) && !$$0.connection.chunkSender.isPending(ChunkPos.asLong($$1, $$2));
   }

   private boolean isChunkOnTrackedBorder(ServerPlayer $$0, int $$1, int $$2) {
      if (!this.isChunkTracked($$0, $$1, $$2)) {
         return false;
      } else {
         for (int $$3 = -1; $$3 <= 1; $$3++) {
            for (int $$4 = -1; $$4 <= 1; $$4++) {
               if (($$3 != 0 || $$4 != 0) && !this.isChunkTracked($$0, $$1 + $$3, $$2 + $$4)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   protected ThreadedLevelLightEngine getLightEngine() {
      return this.lightEngine;
   }

   @Nullable
   public ChunkHolder getUpdatingChunkIfPresent(long $$0) {
      return (ChunkHolder)this.updatingChunkMap.get($$0);
   }

   @Nullable
   protected ChunkHolder getVisibleChunkIfPresent(long $$0) {
      return (ChunkHolder)this.visibleChunkMap.get($$0);
   }

   @Nullable
   public ChunkStatus getLatestStatus(long $$0) {
      ChunkHolder $$1 = this.getVisibleChunkIfPresent($$0);
      return $$1 != null ? $$1.getLatestStatus() : null;
   }

   protected IntSupplier getChunkQueueLevel(long $$0) {
      return () -> {
         ChunkHolder $$1 = this.getVisibleChunkIfPresent($$0);
         return $$1 == null ? ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1 : Math.min($$1.getQueueLevel(), ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1);
      };
   }

   public String getChunkDebugData(ChunkPos $$0) {
      ChunkHolder $$1 = this.getVisibleChunkIfPresent($$0.toLong());
      if ($$1 == null) {
         return "null";
      } else {
         String $$2 = $$1.getTicketLevel() + "\n";
         ChunkStatus $$3 = $$1.getLatestStatus();
         ChunkAccess $$4 = $$1.getLatestChunk();
         if ($$3 != null) {
            $$2 = $$2 + "St: §" + $$3.getIndex() + $$3 + "§r\n";
         }

         if ($$4 != null) {
            $$2 = $$2 + "Ch: §" + $$4.getPersistedStatus().getIndex() + $$4.getPersistedStatus() + "§r\n";
         }

         FullChunkStatus $$5 = $$1.getFullStatus();
         $$2 = $$2 + '§' + $$5.ordinal() + $$5;
         return $$2 + "§r";
      }
   }

   CompletableFuture<ChunkResult<List<ChunkAccess>>> getChunkRangeFuture(ChunkHolder $$0, int $$1, IntFunction<ChunkStatus> $$2) {
      if ($$1 == 0) {
         ChunkStatus $$3 = $$2.apply(0);
         return $$0.scheduleChunkGenerationTask($$3, this).thenApply($$0x -> $$0x.map(List::of));
      } else {
         int $$4 = Mth.square($$1 * 2 + 1);
         List<CompletableFuture<ChunkResult<ChunkAccess>>> $$5 = new ArrayList<>($$4);
         ChunkPos $$6 = $$0.getPos();

         for (int $$7 = -$$1; $$7 <= $$1; $$7++) {
            for (int $$8 = -$$1; $$8 <= $$1; $$8++) {
               int $$9 = Math.max(Math.abs($$8), Math.abs($$7));
               long $$10 = ChunkPos.asLong($$6.x + $$8, $$6.z + $$7);
               ChunkHolder $$11 = this.getUpdatingChunkIfPresent($$10);
               if ($$11 == null) {
                  return UNLOADED_CHUNK_LIST_FUTURE;
               }

               ChunkStatus $$12 = $$2.apply($$9);
               $$5.add($$11.scheduleChunkGenerationTask($$12, this));
            }
         }

         return Util.sequence($$5).thenApply($$0x -> {
            List<ChunkAccess> $$1x = new ArrayList<>($$0x.size());

            for (ChunkResult<ChunkAccess> $$2x : $$0x) {
               if ($$2x == null) {
                  throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
               }

               ChunkAccess $$3 = $$2x.orElse(null);
               if ($$3 == null) {
                  return UNLOADED_CHUNK_LIST_RESULT;
               }

               $$1x.add($$3);
            }

            return ChunkResult.of($$1x);
         });
      }
   }

   public ReportedException debugFuturesAndCreateReportedException(IllegalStateException $$0, String $$1) {
      StringBuilder $$2 = new StringBuilder();
      Consumer<ChunkHolder> $$3 = $$1x -> $$1x.getAllFutures().forEach($$2x -> {
         ChunkStatus $$3x = (ChunkStatus)$$2x.getFirst();
         CompletableFuture<ChunkResult<ChunkAccess>> $$4x = (CompletableFuture<ChunkResult<ChunkAccess>>)$$2x.getSecond();
         if ($$4x != null && $$4x.isDone() && $$4x.join() == null) {
            $$2.append($$1x.getPos()).append(" - status: ").append($$3x).append(" future: ").append($$4x).append(System.lineSeparator());
         }
      });
      $$2.append("Updating:").append(System.lineSeparator());
      this.updatingChunkMap.values().forEach($$3);
      $$2.append("Visible:").append(System.lineSeparator());
      this.visibleChunkMap.values().forEach($$3);
      CrashReport $$4 = CrashReport.forThrowable($$0, "Chunk loading");
      CrashReportCategory $$5 = $$4.addCategory("Chunk loading");
      $$5.setDetail("Details", $$1);
      $$5.setDetail("Futures", $$2);
      return new ReportedException($$4);
   }

   public CompletableFuture<ChunkResult<LevelChunk>> prepareEntityTickingChunk(ChunkHolder $$0) {
      return this.getChunkRangeFuture($$0, 2, $$0x -> ChunkStatus.FULL).thenApply($$0x -> $$0x.map($$0xx -> (LevelChunk)$$0xx.get($$0xx.size() / 2)));
   }

   @Nullable
   ChunkHolder updateChunkScheduling(long $$0, int $$1, @Nullable ChunkHolder $$2, int $$3) {
      if (!ChunkLevel.isLoaded($$3) && !ChunkLevel.isLoaded($$1)) {
         return $$2;
      } else {
         if ($$2 != null) {
            $$2.setTicketLevel($$1);
         }

         if ($$2 != null) {
            if (!ChunkLevel.isLoaded($$1)) {
               this.toDrop.add($$0);
            } else {
               this.toDrop.remove($$0);
            }
         }

         if (ChunkLevel.isLoaded($$1) && $$2 == null) {
            $$2 = (ChunkHolder)this.pendingUnloads.remove($$0);
            if ($$2 != null) {
               $$2.setTicketLevel($$1);
            } else {
               $$2 = new ChunkHolder(new ChunkPos($$0), $$1, this.level, this.lightEngine, this::onLevelChange, this);
            }

            this.updatingChunkMap.put($$0, $$2);
            this.modified = true;
         }

         return $$2;
      }
   }

   private void onLevelChange(ChunkPos $$0, IntSupplier $$1, int $$2, IntConsumer $$3) {
      this.worldgenTaskDispatcher.onLevelChange($$0, $$1, $$2, $$3);
      this.lightTaskDispatcher.onLevelChange($$0, $$1, $$2, $$3);
   }

   public void close() throws IOException {
      try {
         this.worldgenTaskDispatcher.close();
         this.lightTaskDispatcher.close();
         this.poiManager.close();
      } finally {
         super.close();
      }
   }

   protected void saveAllChunks(boolean $$0) {
      if ($$0) {
         List<ChunkHolder> $$1 = this.visibleChunkMap
            .values()
            .stream()
            .filter(ChunkHolder::wasAccessibleSinceLastSave)
            .peek(ChunkHolder::refreshAccessibility)
            .toList();
         MutableBoolean $$2 = new MutableBoolean();

         do {
            $$2.setFalse();
            $$1.stream().map($$0x -> {
               this.mainThreadExecutor.managedBlock($$0x::isReadyForSaving);
               return $$0x.getLatestChunk();
            }).filter($$0x -> $$0x instanceof ImposterProtoChunk || $$0x instanceof LevelChunk).filter(this::save).forEach($$1x -> $$2.setTrue());
         } while ($$2.isTrue());

         this.poiManager.flushAll();
         this.processUnloads(() -> true);
         this.synchronize(true).join();
      } else {
         this.nextChunkSaveTime.clear();
         long $$3 = Util.getMillis();
         ObjectIterator var4 = this.visibleChunkMap.values().iterator();

         while (var4.hasNext()) {
            ChunkHolder $$4 = (ChunkHolder)var4.next();
            this.saveChunkIfNeeded($$4, $$3);
         }
      }
   }

   protected void tick(BooleanSupplier $$0) {
      ProfilerFiller $$1 = Profiler.get();
      $$1.push("poi");
      this.poiManager.tick($$0);
      $$1.popPush("chunk_unload");
      if (!this.level.noSave()) {
         this.processUnloads($$0);
      }

      $$1.pop();
   }

   public boolean hasWork() {
      return this.lightEngine.hasLightWork()
         || !this.pendingUnloads.isEmpty()
         || !this.updatingChunkMap.isEmpty()
         || this.poiManager.hasWork()
         || !this.toDrop.isEmpty()
         || !this.unloadQueue.isEmpty()
         || this.worldgenTaskDispatcher.hasWork()
         || this.lightTaskDispatcher.hasWork()
         || this.distanceManager.hasTickets();
   }

   private void processUnloads(BooleanSupplier $$0) {
      for (LongIterator $$1 = this.toDrop.iterator(); $$1.hasNext(); $$1.remove()) {
         long $$2 = $$1.nextLong();
         ChunkHolder $$3 = (ChunkHolder)this.updatingChunkMap.get($$2);
         if ($$3 != null) {
            this.updatingChunkMap.remove($$2);
            this.pendingUnloads.put($$2, $$3);
            this.modified = true;
            this.scheduleUnload($$2, $$3);
         }
      }

      int $$4 = Math.max(0, this.unloadQueue.size() - 2000);

      Runnable $$5;
      while (($$4 > 0 || $$0.getAsBoolean()) && ($$5 = this.unloadQueue.poll()) != null) {
         $$4--;
         $$5.run();
      }

      this.saveChunksEagerly($$0);
   }

   private void saveChunksEagerly(BooleanSupplier $$0) {
      long $$1 = Util.getMillis();
      int $$2 = 0;
      LongIterator $$3 = this.chunksToEagerlySave.iterator();

      while ($$2 < 20 && this.activeChunkWrites.get() < 128 && $$0.getAsBoolean() && $$3.hasNext()) {
         long $$4 = $$3.nextLong();
         ChunkHolder $$5 = (ChunkHolder)this.visibleChunkMap.get($$4);
         ChunkAccess $$6 = $$5 != null ? $$5.getLatestChunk() : null;
         if ($$6 == null || !$$6.isUnsaved()) {
            $$3.remove();
         } else if (this.saveChunkIfNeeded($$5, $$1)) {
            $$2++;
            $$3.remove();
         }
      }
   }

   private void scheduleUnload(long $$0, ChunkHolder $$1) {
      CompletableFuture<?> $$2 = $$1.getSaveSyncFuture();
      $$2.thenRunAsync(() -> {
         CompletableFuture<?> $$3 = $$1.getSaveSyncFuture();
         if ($$3 != $$2) {
            this.scheduleUnload($$0, $$1);
         } else {
            ChunkAccess $$4 = $$1.getLatestChunk();
            if (this.pendingUnloads.remove($$0, $$1) && $$4 != null) {
               if ($$4 instanceof LevelChunk $$5) {
                  $$5.setLoaded(false);
               }

               this.save($$4);
               if ($$4 instanceof LevelChunk $$6) {
                  this.level.unload($$6);
               }

               this.lightEngine.updateChunkStatus($$4.getPos());
               this.lightEngine.tryScheduleUpdate();
               this.nextChunkSaveTime.remove($$4.getPos().toLong());
            }
         }
      }, this.unloadQueue::add).whenComplete(($$1x, $$2x) -> {
         if ($$2x != null) {
            LOGGER.error("Failed to save chunk {}", $$1.getPos(), $$2x);
         }
      });
   }

   protected boolean promoteChunkMap() {
      if (!this.modified) {
         return false;
      } else {
         this.visibleChunkMap = this.updatingChunkMap.clone();
         this.modified = false;
         return true;
      }
   }

   private CompletableFuture<ChunkAccess> scheduleChunkLoad(ChunkPos $$0) {
      CompletableFuture<Optional<SerializableChunkData>> $$1 = this.readChunk($$0).thenApplyAsync($$1x -> $$1x.map($$1xx -> {
         SerializableChunkData $$2x = SerializableChunkData.parse(this.level, this.level.palettedContainerFactory(), $$1xx);
         if ($$2x == null) {
            LOGGER.error("Chunk file at {} is missing level data, skipping", $$0);
         }

         return $$2x;
      }), Util.backgroundExecutor().forName("parseChunk"));
      CompletableFuture<?> $$2 = this.poiManager.prefetch($$0);
      return $$1.<Object, Optional>thenCombine((CompletionStage<? extends Object>)$$2, ($$0x, $$1x) -> $$0x).thenApplyAsync($$1x -> {
         Profiler.get().incrementCounter("chunkLoad");
         if ($$1x.isPresent()) {
            ChunkAccess $$2x = ((SerializableChunkData)$$1x.get()).read(this.level, this.poiManager, this.storageInfo(), $$0);
            this.markPosition($$0, $$2x.getPersistedStatus().getChunkType());
            return $$2x;
         } else {
            return this.createEmptyChunk($$0);
         }
      }, this.mainThreadExecutor).exceptionallyAsync($$1x -> this.handleChunkLoadFailure($$1x, $$0), this.mainThreadExecutor);
   }

   private ChunkAccess handleChunkLoadFailure(Throwable $$0, ChunkPos $$1) {
      Throwable $$3 = $$0 instanceof CompletionException $$2 ? $$2.getCause() : $$0;
      Throwable $$5 = $$3 instanceof ReportedException $$4 ? $$4.getCause() : $$3;
      boolean $$6 = $$5 instanceof Error;
      boolean $$7 = $$5 instanceof IOException || $$5 instanceof NbtException;
      if (!$$6) {
         if (!$$7) {
         }

         this.level.getServer().reportChunkLoadFailure($$5, this.storageInfo(), $$1);
         return this.createEmptyChunk($$1);
      } else {
         CrashReport $$8 = CrashReport.forThrowable($$0, "Exception loading chunk");
         CrashReportCategory $$9 = $$8.addCategory("Chunk being loaded");
         $$9.setDetail("pos", $$1);
         this.markPositionReplaceable($$1);
         throw new ReportedException($$8);
      }
   }

   private ChunkAccess createEmptyChunk(ChunkPos $$0) {
      this.markPositionReplaceable($$0);
      return new ProtoChunk($$0, UpgradeData.EMPTY, this.level, this.level.palettedContainerFactory(), null);
   }

   private void markPositionReplaceable(ChunkPos $$0) {
      this.chunkTypeCache.put($$0.toLong(), (byte)-1);
   }

   private byte markPosition(ChunkPos $$0, ChunkType $$1) {
      return this.chunkTypeCache.put($$0.toLong(), (byte)($$1 == ChunkType.PROTOCHUNK ? -1 : 1));
   }

   @Override
   public GenerationChunkHolder acquireGeneration(long $$0) {
      ChunkHolder $$1 = (ChunkHolder)this.updatingChunkMap.get($$0);
      $$1.increaseGenerationRefCount();
      return $$1;
   }

   @Override
   public void releaseGeneration(GenerationChunkHolder $$0) {
      $$0.decreaseGenerationRefCount();
   }

   @Override
   public CompletableFuture<ChunkAccess> applyStep(GenerationChunkHolder $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2) {
      ChunkPos $$3 = $$0.getPos();
      if ($$1.targetStatus() == ChunkStatus.EMPTY) {
         return this.scheduleChunkLoad($$3);
      } else {
         try {
            GenerationChunkHolder $$4 = (GenerationChunkHolder)$$2.get($$3.x, $$3.z);
            ChunkAccess $$5 = $$4.getChunkIfPresentUnchecked($$1.targetStatus().getParent());
            if ($$5 == null) {
               throw new IllegalStateException("Parent chunk missing");
            } else {
               return $$1.apply(this.worldGenContext, $$2, $$5);
            }
         } catch (Exception var8) {
            var8.getStackTrace();
            CrashReport $$7 = CrashReport.forThrowable(var8, "Exception generating new chunk");
            CrashReportCategory $$8 = $$7.addCategory("Chunk to be generated");
            $$8.setDetail("Status being generated", () -> $$1.targetStatus().getName());
            $$8.setDetail("Location", String.format(Locale.ROOT, "%d,%d", $$3.x, $$3.z));
            $$8.setDetail("Position hash", ChunkPos.asLong($$3.x, $$3.z));
            $$8.setDetail("Generator", this.generator());
            this.mainThreadExecutor.execute(() -> {
               throw new ReportedException($$7);
            });
            throw new ReportedException($$7);
         }
      }
   }

   @Override
   public ChunkGenerationTask scheduleGenerationTask(ChunkStatus $$0, ChunkPos $$1) {
      ChunkGenerationTask $$2 = ChunkGenerationTask.create(this, $$0, $$1);
      this.pendingGenerationTasks.add($$2);
      return $$2;
   }

   private void runGenerationTask(ChunkGenerationTask $$0) {
      GenerationChunkHolder $$1 = $$0.getCenter();
      this.worldgenTaskDispatcher.submit(() -> {
         CompletableFuture<?> $$1x = $$0.runUntilWait();
         if ($$1x != null) {
            $$1x.thenRun(() -> this.runGenerationTask($$0));
         }
      }, $$1.getPos().toLong(), $$1::getQueueLevel);
   }

   @Override
   public void runGenerationTasks() {
      this.pendingGenerationTasks.forEach(this::runGenerationTask);
      this.pendingGenerationTasks.clear();
   }

   public CompletableFuture<ChunkResult<LevelChunk>> prepareTickingChunk(ChunkHolder $$0) {
      CompletableFuture<ChunkResult<List<ChunkAccess>>> $$1 = this.getChunkRangeFuture($$0, 1, $$0x -> ChunkStatus.FULL);
      return $$1.thenApplyAsync($$1x -> $$1x.map($$1xx -> {
         LevelChunk $$2 = (LevelChunk)$$1xx.get($$1xx.size() / 2);
         $$2.postProcessGeneration(this.level);
         this.level.startTickingChunk($$2);
         CompletableFuture<?> $$3 = $$0.getSendSyncFuture();
         if ($$3.isDone()) {
            this.onChunkReadyToSend($$0, $$2);
         } else {
            $$3.thenAcceptAsync($$2x -> this.onChunkReadyToSend($$0, $$2), this.mainThreadExecutor);
         }

         return $$2;
      }), this.mainThreadExecutor);
   }

   private void onChunkReadyToSend(ChunkHolder $$0, LevelChunk $$1) {
      ChunkPos $$2 = $$1.getPos();

      for (ServerPlayer $$3 : this.playerMap.getAllPlayers()) {
         if ($$3.getChunkTrackingView().contains($$2)) {
            markChunkPendingToSend($$3, $$1);
         }
      }

      this.level.getChunkSource().onChunkReadyToSend($$0);
      this.level.debugSynchronizers().registerChunk($$1);
   }

   public CompletableFuture<ChunkResult<LevelChunk>> prepareAccessibleChunk(ChunkHolder $$0) {
      return this.getChunkRangeFuture($$0, 1, ChunkLevel::getStatusAroundFullChunk)
         .thenApply($$0x -> $$0x.map($$0xx -> (LevelChunk)$$0xx.get($$0xx.size() / 2)));
   }

   Stream<ChunkHolder> allChunksWithAtLeastStatus(ChunkStatus $$0) {
      int $$1 = ChunkLevel.byStatus($$0);
      return this.visibleChunkMap.values().stream().filter($$1x -> $$1x.getTicketLevel() <= $$1);
   }

   private boolean saveChunkIfNeeded(ChunkHolder $$0, long $$1) {
      if ($$0.wasAccessibleSinceLastSave() && $$0.isReadyForSaving()) {
         ChunkAccess $$2 = $$0.getLatestChunk();
         if (!($$2 instanceof ImposterProtoChunk) && !($$2 instanceof LevelChunk)) {
            return false;
         } else if (!$$2.isUnsaved()) {
            return false;
         } else {
            long $$3 = $$2.getPos().toLong();
            long $$4 = this.nextChunkSaveTime.getOrDefault($$3, -1L);
            if ($$1 < $$4) {
               return false;
            } else {
               boolean $$5 = this.save($$2);
               $$0.refreshAccessibility();
               if ($$5) {
                  this.nextChunkSaveTime.put($$3, $$1 + 10000L);
               }

               return $$5;
            }
         }
      } else {
         return false;
      }
   }

   private boolean save(ChunkAccess $$0) {
      this.poiManager.flush($$0.getPos());
      if (!$$0.tryMarkSaved()) {
         return false;
      } else {
         ChunkPos $$1 = $$0.getPos();

         try {
            ChunkStatus $$2 = $$0.getPersistedStatus();
            if ($$2.getChunkType() != ChunkType.LEVELCHUNK) {
               if (this.isExistingChunkFull($$1)) {
                  return false;
               }

               if ($$2 == ChunkStatus.EMPTY && $$0.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                  return false;
               }
            }

            Profiler.get().incrementCounter("chunkSave");
            this.activeChunkWrites.incrementAndGet();
            SerializableChunkData $$3 = SerializableChunkData.copyOf(this.level, $$0);
            CompletableFuture<CompoundTag> $$4 = CompletableFuture.supplyAsync($$3::write, Util.backgroundExecutor());
            this.write($$1, $$4::join).handle(($$1x, $$2x) -> {
               if ($$2x != null) {
                  this.level.getServer().reportChunkSaveFailure($$2x, this.storageInfo(), $$1);
               }

               this.activeChunkWrites.decrementAndGet();
               return null;
            });
            this.markPosition($$1, $$2.getChunkType());
            return true;
         } catch (Exception var6) {
            this.level.getServer().reportChunkSaveFailure(var6, this.storageInfo(), $$1);
            return false;
         }
      }
   }

   private boolean isExistingChunkFull(ChunkPos $$0) {
      byte $$1 = this.chunkTypeCache.get($$0.toLong());
      if ($$1 != 0) {
         return $$1 == 1;
      } else {
         CompoundTag $$2;
         try {
            $$2 = this.readChunk($$0).join().orElse(null);
            if ($$2 == null) {
               this.markPositionReplaceable($$0);
               return false;
            }
         } catch (Exception var5) {
            LOGGER.error("Failed to read chunk {}", $$0, var5);
            this.markPositionReplaceable($$0);
            return false;
         }

         ChunkType $$5 = SerializableChunkData.getChunkStatusFromTag($$2).getChunkType();
         return this.markPosition($$0, $$5) == 1;
      }
   }

   protected void setServerViewDistance(int $$0) {
      int $$1 = Mth.clamp($$0, 2, 32);
      if ($$1 != this.serverViewDistance) {
         this.serverViewDistance = $$1;
         this.distanceManager.updatePlayerTickets(this.serverViewDistance);

         for (ServerPlayer $$2 : this.playerMap.getAllPlayers()) {
            this.updateChunkTracking($$2);
         }
      }
   }

   int getPlayerViewDistance(ServerPlayer $$0) {
      return Mth.clamp($$0.requestedViewDistance(), 2, this.serverViewDistance);
   }

   private void markChunkPendingToSend(ServerPlayer $$0, ChunkPos $$1) {
      LevelChunk $$2 = this.getChunkToSend($$1.toLong());
      if ($$2 != null) {
         markChunkPendingToSend($$0, $$2);
      }
   }

   private static void markChunkPendingToSend(ServerPlayer $$0, LevelChunk $$1) {
      $$0.connection.chunkSender.markChunkPendingToSend($$1);
   }

   private static void dropChunk(ServerPlayer $$0, ChunkPos $$1) {
      $$0.connection.chunkSender.dropChunk($$0, $$1);
   }

   @Nullable
   public LevelChunk getChunkToSend(long $$0) {
      ChunkHolder $$1 = this.getVisibleChunkIfPresent($$0);
      return $$1 == null ? null : $$1.getChunkToSend();
   }

   public int size() {
      return this.visibleChunkMap.size();
   }

   public net.minecraft.server.level.DistanceManager getDistanceManager() {
      return this.distanceManager;
   }

   void dumpChunks(Writer $$0) throws IOException {
      CsvOutput $$1 = CsvOutput.builder()
         .addColumn("x")
         .addColumn("z")
         .addColumn("level")
         .addColumn("in_memory")
         .addColumn("status")
         .addColumn("full_status")
         .addColumn("accessible_ready")
         .addColumn("ticking_ready")
         .addColumn("entity_ticking_ready")
         .addColumn("ticket")
         .addColumn("spawning")
         .addColumn("block_entity_count")
         .addColumn("ticking_ticket")
         .addColumn("ticking_level")
         .addColumn("block_ticks")
         .addColumn("fluid_ticks")
         .build($$0);
      ObjectBidirectionalIterator var3 = this.visibleChunkMap.long2ObjectEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<ChunkHolder> $$2 = (Entry<ChunkHolder>)var3.next();
         long $$3 = $$2.getLongKey();
         ChunkPos $$4 = new ChunkPos($$3);
         ChunkHolder $$5 = (ChunkHolder)$$2.getValue();
         Optional<ChunkAccess> $$6 = Optional.ofNullable($$5.getLatestChunk());
         Optional<LevelChunk> $$7 = $$6.flatMap($$0x -> $$0x instanceof LevelChunk ? Optional.of((LevelChunk)$$0x) : Optional.empty());
         $$1.writeRow(
            new Object[]{
               $$4.x,
               $$4.z,
               $$5.getTicketLevel(),
               $$6.isPresent(),
               $$6.map(ChunkAccess::getPersistedStatus).orElse(null),
               $$7.map(LevelChunk::getFullStatus).orElse(null),
               printFuture($$5.getFullChunkFuture()),
               printFuture($$5.getTickingChunkFuture()),
               printFuture($$5.getEntityTickingChunkFuture()),
               this.ticketStorage.getTicketDebugString($$3, false),
               this.anyPlayerCloseEnoughForSpawning($$4),
               $$7.<Integer>map($$0x -> $$0x.getBlockEntities().size()).orElse(0),
               this.ticketStorage.getTicketDebugString($$3, true),
               this.distanceManager.getChunkLevel($$3, true),
               $$7.<Integer>map($$0x -> $$0x.getBlockTicks().count()).orElse(0),
               $$7.<Integer>map($$0x -> $$0x.getFluidTicks().count()).orElse(0)
            }
         );
      }
   }

   private static String printFuture(CompletableFuture<ChunkResult<LevelChunk>> $$0) {
      try {
         ChunkResult<LevelChunk> $$1 = $$0.getNow(null);
         if ($$1 != null) {
            return $$1.isSuccess() ? "done" : "unloaded";
         } else {
            return "not completed";
         }
      } catch (CompletionException var2) {
         return "failed " + var2.getCause().getMessage();
      } catch (CancellationException var3) {
         return "cancelled";
      }
   }

   private CompletableFuture<Optional<CompoundTag>> readChunk(ChunkPos $$0) {
      return this.read($$0).thenApplyAsync($$0x -> $$0x.map(this::upgradeChunkTag), Util.backgroundExecutor().forName("upgradeChunk"));
   }

   private CompoundTag upgradeChunkTag(CompoundTag $$0) {
      return this.upgradeChunkTag($$0, -1, getChunkDataFixContextTag(this.level.dimension(), this.generator().getTypeNameForDataFixer()));
   }

   public static CompoundTag getChunkDataFixContextTag(ResourceKey<Level> $$0, Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> $$1) {
      CompoundTag $$2 = new CompoundTag();
      $$2.putString("dimension", $$0.identifier().toString());
      $$1.ifPresent($$1x -> $$2.putString("generator", $$1x.identifier().toString()));
      return $$2;
   }

   void collectSpawningChunks(List<LevelChunk> $$0) {
      LongIterator $$1 = this.distanceManager.getSpawnCandidateChunks();

      while ($$1.hasNext()) {
         ChunkHolder $$2 = (ChunkHolder)this.visibleChunkMap.get($$1.nextLong());
         if ($$2 != null) {
            LevelChunk $$3 = $$2.getTickingChunk();
            if ($$3 != null && this.anyPlayerCloseEnoughForSpawningInternal($$2.getPos())) {
               $$0.add($$3);
            }
         }
      }
   }

   void forEachBlockTickingChunk(Consumer<LevelChunk> $$0) {
      this.distanceManager.forEachEntityTickingChunk($$1 -> {
         ChunkHolder $$2 = (ChunkHolder)this.visibleChunkMap.get($$1);
         if ($$2 != null) {
            LevelChunk $$3 = $$2.getTickingChunk();
            if ($$3 != null) {
               $$0.accept($$3);
            }
         }
      });
   }

   boolean anyPlayerCloseEnoughForSpawning(ChunkPos $$0) {
      TriState $$1 = this.distanceManager.hasPlayersNearby($$0.toLong());
      return $$1 == TriState.DEFAULT ? this.anyPlayerCloseEnoughForSpawningInternal($$0) : $$1.toBoolean(true);
   }

   boolean anyPlayerCloseEnoughTo(BlockPos $$0, int $$1) {
      Vec3 $$2 = new Vec3($$0);

      for (ServerPlayer $$3 : this.playerMap.getAllPlayers()) {
         if (this.playerIsCloseEnoughTo($$3, $$2, $$1)) {
            return true;
         }
      }

      return false;
   }

   private boolean anyPlayerCloseEnoughForSpawningInternal(ChunkPos $$0) {
      for (ServerPlayer $$1 : this.playerMap.getAllPlayers()) {
         if (this.playerIsCloseEnoughForSpawning($$1, $$0)) {
            return true;
         }
      }

      return false;
   }

   public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos $$0) {
      long $$1 = $$0.toLong();
      if (!this.distanceManager.hasPlayersNearby($$1).toBoolean(true)) {
         return List.of();
      } else {
         Builder<ServerPlayer> $$2 = ImmutableList.builder();

         for (ServerPlayer $$3 : this.playerMap.getAllPlayers()) {
            if (this.playerIsCloseEnoughForSpawning($$3, $$0)) {
               $$2.add($$3);
            }
         }

         return $$2.build();
      }
   }

   private boolean playerIsCloseEnoughForSpawning(ServerPlayer $$0, ChunkPos $$1) {
      if ($$0.isSpectator()) {
         return false;
      } else {
         double $$2 = euclideanDistanceSquared($$1, $$0.position());
         return $$2 < 16384.0;
      }
   }

   private boolean playerIsCloseEnoughTo(ServerPlayer $$0, Vec3 $$1, int $$2) {
      if ($$0.isSpectator()) {
         return false;
      } else {
         double $$3 = $$0.position().distanceTo($$1);
         return $$3 < $$2;
      }
   }

   private static double euclideanDistanceSquared(ChunkPos $$0, Vec3 $$1) {
      double $$2 = SectionPos.sectionToBlockCoord($$0.x, 8);
      double $$3 = SectionPos.sectionToBlockCoord($$0.z, 8);
      double $$4 = $$2 - $$1.x;
      double $$5 = $$3 - $$1.z;
      return $$4 * $$4 + $$5 * $$5;
   }

   private boolean skipPlayer(ServerPlayer $$0) {
      return $$0.isSpectator() && !(Boolean)this.level.getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS);
   }

   void updatePlayerStatus(ServerPlayer $$0, boolean $$1) {
      boolean $$2 = this.skipPlayer($$0);
      boolean $$3 = this.playerMap.ignoredOrUnknown($$0);
      if ($$1) {
         this.playerMap.addPlayer($$0, $$2);
         this.updatePlayerPos($$0);
         if (!$$2) {
            this.distanceManager.addPlayer(SectionPos.of($$0), $$0);
         }

         $$0.setChunkTrackingView(ChunkTrackingView.EMPTY);
         this.updateChunkTracking($$0);
      } else {
         SectionPos $$4 = $$0.getLastSectionPos();
         this.playerMap.removePlayer($$0);
         if (!$$3) {
            this.distanceManager.removePlayer($$4, $$0);
         }

         this.applyChunkTrackingView($$0, ChunkTrackingView.EMPTY);
      }
   }

   private void updatePlayerPos(ServerPlayer $$0) {
      SectionPos $$1 = SectionPos.of($$0);
      $$0.setLastSectionPos($$1);
   }

   public void move(ServerPlayer $$0) {
      ObjectIterator $$2 = this.entityMap.values().iterator();

      while ($$2.hasNext()) {
         ChunkMap.TrackedEntity $$1 = (ChunkMap.TrackedEntity)$$2.next();
         if ($$1.entity == $$0) {
            $$1.updatePlayers(this.level.players());
         } else {
            $$1.updatePlayer($$0);
         }
      }

      SectionPos $$2x = $$0.getLastSectionPos();
      SectionPos $$3 = SectionPos.of($$0);
      boolean $$4 = this.playerMap.ignored($$0);
      boolean $$5 = this.skipPlayer($$0);
      boolean $$6 = $$2x.asLong() != $$3.asLong();
      if ($$6 || $$4 != $$5) {
         this.updatePlayerPos($$0);
         if (!$$4) {
            this.distanceManager.removePlayer($$2x, $$0);
         }

         if (!$$5) {
            this.distanceManager.addPlayer($$3, $$0);
         }

         if (!$$4 && $$5) {
            this.playerMap.ignorePlayer($$0);
         }

         if ($$4 && !$$5) {
            this.playerMap.unIgnorePlayer($$0);
         }

         this.updateChunkTracking($$0);
      }
   }

   private void updateChunkTracking(ServerPlayer $$0) {
      ChunkPos $$1 = $$0.chunkPosition();
      int $$2 = this.getPlayerViewDistance($$0);
      if (!($$0.getChunkTrackingView() instanceof ChunkTrackingView.Positioned $$3 && $$3.center().equals($$1) && $$3.viewDistance() == $$2)) {
         this.applyChunkTrackingView($$0, ChunkTrackingView.of($$1, $$2));
      }
   }

   private void applyChunkTrackingView(ServerPlayer $$0, ChunkTrackingView $$1) {
      if ($$0.level() == this.level) {
         ChunkTrackingView $$2 = $$0.getChunkTrackingView();
         if ($$1 instanceof ChunkTrackingView.Positioned $$3 && !($$2 instanceof ChunkTrackingView.Positioned $$4 && $$4.center().equals($$3.center()))) {
            $$0.connection.send(new ClientboundSetChunkCacheCenterPacket($$3.center().x, $$3.center().z));
         }

         ChunkTrackingView.difference($$2, $$1, $$1x -> this.markChunkPendingToSend($$0, $$1x), $$1x -> dropChunk($$0, $$1x));
         $$0.setChunkTrackingView($$1);
      }
   }

   @Override
   public List<ServerPlayer> getPlayers(ChunkPos $$0, boolean $$1) {
      Set<ServerPlayer> $$2 = this.playerMap.getAllPlayers();
      Builder<ServerPlayer> $$3 = ImmutableList.builder();

      for (ServerPlayer $$4 : $$2) {
         if ($$1 && this.isChunkOnTrackedBorder($$4, $$0.x, $$0.z) || !$$1 && this.isChunkTracked($$4, $$0.x, $$0.z)) {
            $$3.add($$4);
         }
      }

      return $$3.build();
   }

   protected void addEntity(Entity $$0) {
      if (!($$0 instanceof EnderDragonPart)) {
         EntityType<?> $$1 = $$0.getType();
         int $$2 = $$1.clientTrackingRange() * 16;
         if ($$2 != 0) {
            int $$3 = $$1.updateInterval();
            if (this.entityMap.containsKey($$0.getId())) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
            } else {
               ChunkMap.TrackedEntity $$4 = new ChunkMap.TrackedEntity($$0, $$2, $$3, $$1.trackDeltas());
               this.entityMap.put($$0.getId(), $$4);
               $$4.updatePlayers(this.level.players());
               if ($$0 instanceof ServerPlayer $$5) {
                  this.updatePlayerStatus($$5, true);
                  ObjectIterator var7 = this.entityMap.values().iterator();

                  while (var7.hasNext()) {
                     ChunkMap.TrackedEntity $$6 = (ChunkMap.TrackedEntity)var7.next();
                     if ($$6.entity != $$5) {
                        $$6.updatePlayer($$5);
                     }
                  }
               }
            }
         }
      }
   }

   protected void removeEntity(Entity $$0) {
      if ($$0 instanceof ServerPlayer $$1) {
         this.updatePlayerStatus($$1, false);
         ObjectIterator var3 = this.entityMap.values().iterator();

         while (var3.hasNext()) {
            ChunkMap.TrackedEntity $$2 = (ChunkMap.TrackedEntity)var3.next();
            $$2.removePlayer($$1);
         }
      }

      ChunkMap.TrackedEntity $$3 = (ChunkMap.TrackedEntity)this.entityMap.remove($$0.getId());
      if ($$3 != null) {
         $$3.broadcastRemoved();
      }
   }

   protected void tick() {
      for (ServerPlayer $$0 : this.playerMap.getAllPlayers()) {
         this.updateChunkTracking($$0);
      }

      List<ServerPlayer> $$1 = Lists.newArrayList();
      List<ServerPlayer> $$2 = this.level.players();
      ObjectIterator var3 = this.entityMap.values().iterator();

      while (var3.hasNext()) {
         ChunkMap.TrackedEntity $$3 = (ChunkMap.TrackedEntity)var3.next();
         SectionPos $$4 = $$3.lastSectionPos;
         SectionPos $$5 = SectionPos.of($$3.entity);
         boolean $$6 = !Objects.equals($$4, $$5);
         if ($$6) {
            $$3.updatePlayers($$2);
            Entity $$7 = $$3.entity;
            if ($$7 instanceof ServerPlayer) {
               $$1.add((ServerPlayer)$$7);
            }

            $$3.lastSectionPos = $$5;
         }

         if ($$6 || $$3.entity.needsSync || this.distanceManager.inEntityTickingRange($$5.chunk().toLong())) {
            $$3.serverEntity.sendChanges();
         }
      }

      if (!$$1.isEmpty()) {
         var3 = this.entityMap.values().iterator();

         while (var3.hasNext()) {
            ChunkMap.TrackedEntity $$8 = (ChunkMap.TrackedEntity)var3.next();
            $$8.updatePlayers($$1);
         }
      }
   }

   public void sendToTrackingPlayers(Entity $$0, Packet<? super ClientGamePacketListener> $$1) {
      ChunkMap.TrackedEntity $$2 = (ChunkMap.TrackedEntity)this.entityMap.get($$0.getId());
      if ($$2 != null) {
         $$2.sendToTrackingPlayers($$1);
      }
   }

   public void sendToTrackingPlayersFiltered(Entity $$0, Packet<? super ClientGamePacketListener> $$1, Predicate<ServerPlayer> $$2) {
      ChunkMap.TrackedEntity $$3 = (ChunkMap.TrackedEntity)this.entityMap.get($$0.getId());
      if ($$3 != null) {
         $$3.sendToTrackingPlayersFiltered($$1, $$2);
      }
   }

   protected void sendToTrackingPlayersAndSelf(Entity $$0, Packet<? super ClientGamePacketListener> $$1) {
      ChunkMap.TrackedEntity $$2 = (ChunkMap.TrackedEntity)this.entityMap.get($$0.getId());
      if ($$2 != null) {
         $$2.sendToTrackingPlayersAndSelf($$1);
      }
   }

   public boolean isTrackedByAnyPlayer(Entity $$0) {
      ChunkMap.TrackedEntity $$1 = (ChunkMap.TrackedEntity)this.entityMap.get($$0.getId());
      return $$1 != null ? !$$1.seenBy.isEmpty() : false;
   }

   public void forEachEntityTrackedBy(ServerPlayer $$0, Consumer<Entity> $$1) {
      ObjectIterator var3 = this.entityMap.values().iterator();

      while (var3.hasNext()) {
         ChunkMap.TrackedEntity $$2 = (ChunkMap.TrackedEntity)var3.next();
         if ($$2.seenBy.contains($$0.connection)) {
            $$1.accept($$2.entity);
         }
      }
   }

   public void resendBiomesForChunks(List<ChunkAccess> $$0) {
      Map<ServerPlayer, List<LevelChunk>> $$1 = new HashMap<>();

      for (ChunkAccess $$2 : $$0) {
         ChunkPos $$3 = $$2.getPos();
         LevelChunk $$5;
         if ($$2 instanceof LevelChunk $$4) {
            $$5 = $$4;
         } else {
            $$5 = this.level.getChunk($$3.x, $$3.z);
         }

         for (ServerPlayer $$7 : this.getPlayers($$3, false)) {
            $$1.computeIfAbsent($$7, $$0x -> new ArrayList<>()).add($$5);
         }
      }

      $$1.forEach(($$0x, $$1x) -> $$0x.connection.send(ClientboundChunksBiomesPacket.forChunks($$1x)));
   }

   protected PoiManager getPoiManager() {
      return this.poiManager;
   }

   public String getStorageName() {
      return this.storageName;
   }

   void onFullChunkStatusChange(ChunkPos $$0, FullChunkStatus $$1) {
      this.chunkStatusListener.onChunkStatusChange($$0, $$1);
   }

   public void waitForLightBeforeSending(ChunkPos $$0, int $$1) {
      int $$2 = $$1 + 1;
      ChunkPos.rangeClosed($$0, $$2).forEach($$0x -> {
         ChunkHolder $$1x = this.getVisibleChunkIfPresent($$0x.toLong());
         if ($$1x != null) {
            $$1x.addSendDependency(this.lightEngine.waitForPendingTasks($$0x.x, $$0x.z));
         }
      });
   }

   public void forEachReadyToSendChunk(Consumer<LevelChunk> $$0) {
      ObjectIterator var2 = this.visibleChunkMap.values().iterator();

      while (var2.hasNext()) {
         ChunkHolder $$1 = (ChunkHolder)var2.next();
         LevelChunk $$2 = $$1.getChunkToSend();
         if ($$2 != null) {
            $$0.accept($$2);
         }
      }
   }

   class DistanceManager extends net.minecraft.server.level.DistanceManager {
      protected DistanceManager(final TicketStorage $$0, final Executor $$1, final Executor $$2) {
         super($$0, $$1, $$2);
      }

      @Override
      protected boolean isChunkToRemove(long $$0) {
         return ChunkMap.this.toDrop.contains($$0);
      }

      @Nullable
      @Override
      protected ChunkHolder getChunk(long $$0) {
         return ChunkMap.this.getUpdatingChunkIfPresent($$0);
      }

      @Nullable
      @Override
      protected ChunkHolder updateChunkScheduling(long $$0, int $$1, @Nullable ChunkHolder $$2, int $$3) {
         return ChunkMap.this.updateChunkScheduling($$0, $$1, $$2, $$3);
      }
   }

   class TrackedEntity implements ServerEntity.Synchronizer {
      final ServerEntity serverEntity;
      final Entity entity;
      private final int range;
      SectionPos lastSectionPos;
      final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

      public TrackedEntity(final Entity $$0, final int $$1, final int $$2, final boolean $$3) {
         this.serverEntity = new ServerEntity(ChunkMap.this.level, $$0, $$2, $$3, this);
         this.entity = $$0;
         this.range = $$1;
         this.lastSectionPos = SectionPos.of($$0);
      }

      @Override
      public boolean equals(Object $$0) {
         return $$0 instanceof ChunkMap.TrackedEntity ? ((ChunkMap.TrackedEntity)$$0).entity.getId() == this.entity.getId() : false;
      }

      @Override
      public int hashCode() {
         return this.entity.getId();
      }

      @Override
      public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> $$0) {
         for (ServerPlayerConnection $$1 : this.seenBy) {
            $$1.send($$0);
         }
      }

      @Override
      public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> $$0) {
         this.sendToTrackingPlayers($$0);
         if (this.entity instanceof ServerPlayer $$1) {
            $$1.connection.send($$0);
         }
      }

      @Override
      public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> $$0, Predicate<ServerPlayer> $$1) {
         for (ServerPlayerConnection $$2 : this.seenBy) {
            if ($$1.test($$2.getPlayer())) {
               $$2.send($$0);
            }
         }
      }

      public void broadcastRemoved() {
         for (ServerPlayerConnection $$0 : this.seenBy) {
            this.serverEntity.removePairing($$0.getPlayer());
         }
      }

      public void removePlayer(ServerPlayer $$0) {
         if (this.seenBy.remove($$0.connection)) {
            this.serverEntity.removePairing($$0);
            if (this.seenBy.isEmpty()) {
               ChunkMap.this.level.debugSynchronizers().dropEntity(this.entity);
            }
         }
      }

      public void updatePlayer(ServerPlayer $$0) {
         if ($$0 != this.entity) {
            Vec3 $$1 = $$0.position().subtract(this.entity.position());
            int $$2 = ChunkMap.this.getPlayerViewDistance($$0);
            double $$3 = Math.min(this.getEffectiveRange(), $$2 * 16);
            double $$4 = $$1.x * $$1.x + $$1.z * $$1.z;
            double $$5 = $$3 * $$3;
            boolean $$6 = $$4 <= $$5
               && this.entity.broadcastToPlayer($$0)
               && ChunkMap.this.isChunkTracked($$0, this.entity.chunkPosition().x, this.entity.chunkPosition().z);
            if ($$6) {
               if (this.seenBy.add($$0.connection)) {
                  this.serverEntity.addPairing($$0);
                  if (this.seenBy.size() == 1) {
                     ChunkMap.this.level.debugSynchronizers().registerEntity(this.entity);
                  }

                  ChunkMap.this.level.debugSynchronizers().startTrackingEntity($$0, this.entity);
               }
            } else {
               this.removePlayer($$0);
            }
         }
      }

      private int scaledRange(int $$0) {
         return ChunkMap.this.level.getServer().getScaledTrackingDistance($$0);
      }

      private int getEffectiveRange() {
         int $$0 = this.range;

         for (Entity $$1 : this.entity.getIndirectPassengers()) {
            int $$2 = $$1.getType().clientTrackingRange() * 16;
            if ($$2 > $$0) {
               $$0 = $$2;
            }
         }

         return this.scaledRange($$0);
      }

      public void updatePlayers(List<ServerPlayer> $$0) {
         for (ServerPlayer $$1 : $$0) {
            this.updatePlayer($$1);
         }
      }
   }
}
