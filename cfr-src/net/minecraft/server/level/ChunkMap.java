/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Queues
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2LongMap
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
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
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ChunkTaskDispatcher;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GeneratingChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
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
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkMap
extends SimpleRegionStorage
implements ChunkHolder.PlayerProvider,
GeneratingChunkMap {
    private static final ChunkResult<List<ChunkAccess>> UNLOADED_CHUNK_LIST_RESULT = ChunkResult.error("Unloaded chunks found in range");
    private static final CompletableFuture<ChunkResult<List<ChunkAccess>>> UNLOADED_CHUNK_LIST_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK_LIST_RESULT);
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
    private final List<ChunkGenerationTask> pendingGenerationTasks = new ArrayList<ChunkGenerationTask>();
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
    private final DistanceManager distanceManager;
    private final String storageName;
    private final PlayerMap playerMap = new PlayerMap();
    private final Int2ObjectMap<TrackedEntity> entityMap = new Int2ObjectOpenHashMap();
    private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
    private final Long2LongMap nextChunkSaveTime = new Long2LongOpenHashMap();
    private final LongSet chunksToEagerlySave = new LongLinkedOpenHashSet();
    private final Queue<Runnable> unloadQueue = Queues.newConcurrentLinkedQueue();
    private final AtomicInteger activeChunkWrites = new AtomicInteger();
    private int serverViewDistance;
    private final WorldGenContext worldGenContext;

    public ChunkMap(ServerLevel $$0, LevelStorageSource.LevelStorageAccess $$1, DataFixer $$2, StructureTemplateManager $$3, Executor $$4, BlockableEventLoop<Runnable> $$5, LightChunkGetter $$6, ChunkGenerator $$7, ChunkStatusUpdateListener $$8, Supplier<DimensionDataStorage> $$9, TicketStorage $$10, int $$11, boolean $$12) {
        super(new RegionStorageInfo($$1.getLevelId(), $$0.dimension(), "chunk"), $$1.getDimensionPath($$0.dimension()).resolve("region"), $$2, $$12, DataFixTypes.CHUNK, LegacyStructureDataHandler.getLegacyTagFixer($$0.dimension(), $$9, $$2));
        Path $$13 = $$1.getDimensionPath($$0.dimension());
        this.storageName = $$13.getFileName().toString();
        this.level = $$0;
        RegistryAccess $$14 = $$0.registryAccess();
        long $$15 = $$0.getSeed();
        if ($$7 instanceof NoiseBasedChunkGenerator) {
            NoiseBasedChunkGenerator $$16 = (NoiseBasedChunkGenerator)$$7;
            this.randomState = RandomState.create($$16.generatorSettings().value(), $$14.lookupOrThrow(Registries.NOISE), $$15);
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
        this.distanceManager = new DistanceManager($$10, $$4, $$5);
        this.ticketStorage = $$10;
        this.poiManager = new PoiManager(new RegionStorageInfo($$1.getLevelId(), $$0.dimension(), "poi"), $$13.resolve("poi"), $$2, $$12, $$14, $$0.getServer(), $$0);
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
        }
        for (int $$3 = -1; $$3 <= 1; ++$$3) {
            for (int $$4 = -1; $$4 <= 1; ++$$4) {
                if ($$3 == 0 && $$4 == 0 || this.isChunkTracked($$0, $$1 + $$3, $$2 + $$4)) continue;
                return true;
            }
        }
        return false;
    }

    protected ThreadedLevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    public @Nullable ChunkHolder getUpdatingChunkIfPresent(long $$0) {
        return (ChunkHolder)this.updatingChunkMap.get($$0);
    }

    protected @Nullable ChunkHolder getVisibleChunkIfPresent(long $$0) {
        return (ChunkHolder)this.visibleChunkMap.get($$0);
    }

    public @Nullable ChunkStatus getLatestStatus(long $$0) {
        ChunkHolder $$1 = this.getVisibleChunkIfPresent($$0);
        return $$1 != null ? $$1.getLatestStatus() : null;
    }

    protected IntSupplier getChunkQueueLevel(long $$0) {
        return () -> {
            ChunkHolder $$1 = this.getVisibleChunkIfPresent($$0);
            if ($$1 == null) {
                return ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1;
            }
            return Math.min($$1.getQueueLevel(), ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1);
        };
    }

    public String getChunkDebugData(ChunkPos $$0) {
        ChunkHolder $$1 = this.getVisibleChunkIfPresent($$0.toLong());
        if ($$1 == null) {
            return "null";
        }
        String $$2 = $$1.getTicketLevel() + "\n";
        ChunkStatus $$3 = $$1.getLatestStatus();
        ChunkAccess $$4 = $$1.getLatestChunk();
        if ($$3 != null) {
            $$2 = $$2 + "St: \u00a7" + $$3.getIndex() + String.valueOf($$3) + "\u00a7r\n";
        }
        if ($$4 != null) {
            $$2 = $$2 + "Ch: \u00a7" + $$4.getPersistedStatus().getIndex() + String.valueOf($$4.getPersistedStatus()) + "\u00a7r\n";
        }
        FullChunkStatus $$5 = $$1.getFullStatus();
        $$2 = $$2 + String.valueOf('\u00a7') + $$5.ordinal() + String.valueOf((Object)$$5);
        return $$2 + "\u00a7r";
    }

    CompletableFuture<ChunkResult<List<ChunkAccess>>> getChunkRangeFuture(ChunkHolder $$02, int $$1, IntFunction<ChunkStatus> $$2) {
        if ($$1 == 0) {
            ChunkStatus $$3 = $$2.apply(0);
            return $$02.scheduleChunkGenerationTask($$3, this).thenApply($$0 -> $$0.map(List::of));
        }
        int $$4 = Mth.square($$1 * 2 + 1);
        ArrayList<CompletableFuture<ChunkResult<ChunkAccess>>> $$5 = new ArrayList<CompletableFuture<ChunkResult<ChunkAccess>>>($$4);
        ChunkPos $$6 = $$02.getPos();
        for (int $$7 = -$$1; $$7 <= $$1; ++$$7) {
            for (int $$8 = -$$1; $$8 <= $$1; ++$$8) {
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
        return Util.sequence($$5).thenApply($$0 -> {
            ArrayList<ChunkAccess> $$1 = new ArrayList<ChunkAccess>($$0.size());
            for (ChunkResult $$2 : $$0) {
                if ($$2 == null) {
                    throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                }
                ChunkAccess $$3 = $$2.orElse(null);
                if ($$3 == null) {
                    return UNLOADED_CHUNK_LIST_RESULT;
                }
                $$1.add($$3);
            }
            return ChunkResult.of($$1);
        });
    }

    public ReportedException debugFuturesAndCreateReportedException(IllegalStateException $$0, String $$12) {
        StringBuilder $$2 = new StringBuilder();
        Consumer<ChunkHolder> $$3 = $$1 -> $$1.getAllFutures().forEach($$2 -> {
            ChunkStatus $$3 = (ChunkStatus)$$2.getFirst();
            CompletableFuture $$4 = (CompletableFuture)$$2.getSecond();
            if ($$4 != null && $$4.isDone() && $$4.join() == null) {
                $$2.append($$1.getPos()).append(" - status: ").append($$3).append(" future: ").append($$4).append(System.lineSeparator());
            }
        });
        $$2.append("Updating:").append(System.lineSeparator());
        this.updatingChunkMap.values().forEach($$3);
        $$2.append("Visible:").append(System.lineSeparator());
        this.visibleChunkMap.values().forEach($$3);
        CrashReport $$4 = CrashReport.forThrowable($$0, "Chunk loading");
        CrashReportCategory $$5 = $$4.addCategory("Chunk loading");
        $$5.setDetail("Details", $$12);
        $$5.setDetail("Futures", $$2);
        return new ReportedException($$4);
    }

    public CompletableFuture<ChunkResult<LevelChunk>> prepareEntityTickingChunk(ChunkHolder $$03) {
        return this.getChunkRangeFuture($$03, 2, $$0 -> ChunkStatus.FULL).thenApply($$02 -> $$02.map($$0 -> (LevelChunk)$$0.get($$0.size() / 2)));
    }

    @Nullable ChunkHolder updateChunkScheduling(long $$0, int $$1, @Nullable ChunkHolder $$2, int $$3) {
        if (!ChunkLevel.isLoaded($$3) && !ChunkLevel.isLoaded($$1)) {
            return $$2;
        }
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
            this.updatingChunkMap.put($$0, (Object)$$2);
            this.modified = true;
        }
        return $$2;
    }

    private void onLevelChange(ChunkPos $$0, IntSupplier $$1, int $$2, IntConsumer $$3) {
        this.worldgenTaskDispatcher.onLevelChange($$0, $$1, $$2, $$3);
        this.lightTaskDispatcher.onLevelChange($$0, $$1, $$2, $$3);
    }

    @Override
    public void close() throws IOException {
        try {
            this.worldgenTaskDispatcher.close();
            this.lightTaskDispatcher.close();
            this.poiManager.close();
        }
        finally {
            super.close();
        }
    }

    protected void saveAllChunks(boolean $$02) {
        if ($$02) {
            List<ChunkHolder> $$12 = this.visibleChunkMap.values().stream().filter(ChunkHolder::wasAccessibleSinceLastSave).peek(ChunkHolder::refreshAccessibility).toList();
            MutableBoolean $$2 = new MutableBoolean();
            do {
                $$2.setFalse();
                $$12.stream().map($$0 -> {
                    this.mainThreadExecutor.managedBlock($$0::isReadyForSaving);
                    return $$0.getLatestChunk();
                }).filter($$0 -> $$0 instanceof ImposterProtoChunk || $$0 instanceof LevelChunk).filter(this::save).forEach($$1 -> $$2.setTrue());
            } while ($$2.isTrue());
            this.poiManager.flushAll();
            this.processUnloads(() -> true);
            this.synchronize(true).join();
        } else {
            this.nextChunkSaveTime.clear();
            long $$3 = Util.getMillis();
            for (ChunkHolder $$4 : this.visibleChunkMap.values()) {
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
        return this.lightEngine.hasLightWork() || !this.pendingUnloads.isEmpty() || !this.updatingChunkMap.isEmpty() || this.poiManager.hasWork() || !this.toDrop.isEmpty() || !this.unloadQueue.isEmpty() || this.worldgenTaskDispatcher.hasWork() || this.lightTaskDispatcher.hasWork() || this.distanceManager.hasTickets();
    }

    private void processUnloads(BooleanSupplier $$0) {
        Runnable $$5;
        LongIterator $$1 = this.toDrop.iterator();
        while ($$1.hasNext()) {
            long $$2 = $$1.nextLong();
            ChunkHolder $$3 = (ChunkHolder)this.updatingChunkMap.get($$2);
            if ($$3 != null) {
                this.updatingChunkMap.remove($$2);
                this.pendingUnloads.put($$2, (Object)$$3);
                this.modified = true;
                this.scheduleUnload($$2, $$3);
            }
            $$1.remove();
        }
        for (int $$4 = Math.max(0, this.unloadQueue.size() - 2000); ($$4 > 0 || $$0.getAsBoolean()) && ($$5 = this.unloadQueue.poll()) != null; --$$4) {
            $$5.run();
        }
        this.saveChunksEagerly($$0);
    }

    private void saveChunksEagerly(BooleanSupplier $$0) {
        long $$1 = Util.getMillis();
        int $$2 = 0;
        LongIterator $$3 = this.chunksToEagerlySave.iterator();
        while ($$2 < 20 && this.activeChunkWrites.get() < 128 && $$0.getAsBoolean() && $$3.hasNext()) {
            ChunkAccess $$6;
            long $$4 = $$3.nextLong();
            ChunkHolder $$5 = (ChunkHolder)this.visibleChunkMap.get($$4);
            ChunkAccess chunkAccess = $$6 = $$5 != null ? $$5.getLatestChunk() : null;
            if ($$6 == null || !$$6.isUnsaved()) {
                $$3.remove();
                continue;
            }
            if (!this.saveChunkIfNeeded($$5, $$1)) continue;
            ++$$2;
            $$3.remove();
        }
    }

    private void scheduleUnload(long $$0, ChunkHolder $$12) {
        CompletableFuture<?> $$22 = $$12.getSaveSyncFuture();
        ((CompletableFuture)$$22.thenRunAsync(() -> {
            CompletableFuture<?> $$3 = $$12.getSaveSyncFuture();
            if ($$3 != $$22) {
                this.scheduleUnload($$0, $$12);
                return;
            }
            ChunkAccess $$4 = $$12.getLatestChunk();
            if (this.pendingUnloads.remove($$0, (Object)$$12) && $$4 != null) {
                if ($$4 instanceof LevelChunk) {
                    LevelChunk $$5 = (LevelChunk)$$4;
                    $$5.setLoaded(false);
                }
                this.save($$4);
                if ($$4 instanceof LevelChunk) {
                    LevelChunk $$6 = (LevelChunk)$$4;
                    this.level.unload($$6);
                }
                this.lightEngine.updateChunkStatus($$4.getPos());
                this.lightEngine.tryScheduleUpdate();
                this.nextChunkSaveTime.remove($$4.getPos().toLong());
            }
        }, this.unloadQueue::add)).whenComplete(($$1, $$2) -> {
            if ($$2 != null) {
                LOGGER.error("Failed to save chunk {}", (Object)$$12.getPos(), $$2);
            }
        });
    }

    protected boolean promoteChunkMap() {
        if (!this.modified) {
            return false;
        }
        this.visibleChunkMap = this.updatingChunkMap.clone();
        this.modified = false;
        return true;
    }

    private CompletableFuture<ChunkAccess> scheduleChunkLoad(ChunkPos $$02) {
        CompletionStage $$13 = this.readChunk($$02).thenApplyAsync($$12 -> $$12.map($$1 -> {
            SerializableChunkData $$2 = SerializableChunkData.parse(this.level, this.level.palettedContainerFactory(), $$1);
            if ($$2 == null) {
                LOGGER.error("Chunk file at {} is missing level data, skipping", (Object)$$02);
            }
            return $$2;
        }), Util.backgroundExecutor().forName("parseChunk"));
        CompletableFuture<?> $$2 = this.poiManager.prefetch($$02);
        return ((CompletableFuture)((CompletableFuture)((CompletableFuture)$$13).thenCombine($$2, ($$0, $$1) -> $$0)).thenApplyAsync($$1 -> {
            Profiler.get().incrementCounter("chunkLoad");
            if ($$1.isPresent()) {
                ProtoChunk $$2 = ((SerializableChunkData)$$1.get()).read(this.level, this.poiManager, this.storageInfo(), $$02);
                this.markPosition($$02, ((ChunkAccess)$$2).getPersistedStatus().getChunkType());
                return $$2;
            }
            return this.createEmptyChunk($$02);
        }, (Executor)this.mainThreadExecutor)).exceptionallyAsync($$1 -> this.handleChunkLoadFailure((Throwable)$$1, $$02), (Executor)this.mainThreadExecutor);
    }

    private ChunkAccess handleChunkLoadFailure(Throwable $$0, ChunkPos $$1) {
        boolean $$7;
        Throwable throwable;
        Throwable $$3;
        if ($$0 instanceof CompletionException) {
            CompletionException $$2 = (CompletionException)$$0;
            v0 = $$2.getCause();
        } else {
            v0 = $$3 = $$0;
        }
        if ($$3 instanceof ReportedException) {
            ReportedException $$4 = (ReportedException)$$3;
            throwable = $$4.getCause();
        } else {
            throwable = $$3;
        }
        Throwable $$5 = throwable;
        boolean $$6 = $$5 instanceof Error;
        boolean bl = $$7 = $$5 instanceof IOException || $$5 instanceof NbtException;
        if (!$$6) {
            if (!$$7) {
                // empty if block
            }
        } else {
            CrashReport $$8 = CrashReport.forThrowable($$0, "Exception loading chunk");
            CrashReportCategory $$9 = $$8.addCategory("Chunk being loaded");
            $$9.setDetail("pos", $$1);
            this.markPositionReplaceable($$1);
            throw new ReportedException($$8);
        }
        this.level.getServer().reportChunkLoadFailure($$5, this.storageInfo(), $$1);
        return this.createEmptyChunk($$1);
    }

    private ChunkAccess createEmptyChunk(ChunkPos $$0) {
        this.markPositionReplaceable($$0);
        return new ProtoChunk($$0, UpgradeData.EMPTY, this.level, this.level.palettedContainerFactory(), null);
    }

    private void markPositionReplaceable(ChunkPos $$0) {
        this.chunkTypeCache.put($$0.toLong(), (byte)-1);
    }

    private byte markPosition(ChunkPos $$0, ChunkType $$1) {
        return this.chunkTypeCache.put($$0.toLong(), $$1 == ChunkType.PROTOCHUNK ? (byte)-1 : 1);
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
        }
        try {
            GenerationChunkHolder $$4 = $$2.get($$3.x, $$3.z);
            ChunkAccess $$5 = $$4.getChunkIfPresentUnchecked($$1.targetStatus().getParent());
            if ($$5 == null) {
                throw new IllegalStateException("Parent chunk missing");
            }
            return $$1.apply(this.worldGenContext, $$2, $$5);
        }
        catch (Exception $$6) {
            $$6.getStackTrace();
            CrashReport $$7 = CrashReport.forThrowable($$6, "Exception generating new chunk");
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

    @Override
    public ChunkGenerationTask scheduleGenerationTask(ChunkStatus $$0, ChunkPos $$1) {
        ChunkGenerationTask $$2 = ChunkGenerationTask.create(this, $$0, $$1);
        this.pendingGenerationTasks.add($$2);
        return $$2;
    }

    private void runGenerationTask(ChunkGenerationTask $$0) {
        GenerationChunkHolder $$1 = $$0.getCenter();
        this.worldgenTaskDispatcher.submit(() -> {
            CompletableFuture<?> $$1 = $$0.runUntilWait();
            if ($$1 == null) {
                return;
            }
            $$1.thenRun(() -> this.runGenerationTask($$0));
        }, $$1.getPos().toLong(), $$1::getQueueLevel);
    }

    @Override
    public void runGenerationTasks() {
        this.pendingGenerationTasks.forEach(this::runGenerationTask);
        this.pendingGenerationTasks.clear();
    }

    public CompletableFuture<ChunkResult<LevelChunk>> prepareTickingChunk(ChunkHolder $$02) {
        CompletableFuture<ChunkResult<List<ChunkAccess>>> $$1 = this.getChunkRangeFuture($$02, 1, $$0 -> ChunkStatus.FULL);
        return $$1.thenApplyAsync($$12 -> $$12.map($$1 -> {
            LevelChunk $$22 = (LevelChunk)$$1.get($$1.size() / 2);
            $$22.postProcessGeneration(this.level);
            this.level.startTickingChunk($$22);
            CompletableFuture<?> $$3 = $$02.getSendSyncFuture();
            if ($$3.isDone()) {
                this.onChunkReadyToSend($$02, $$22);
            } else {
                $$3.thenAcceptAsync($$2 -> this.onChunkReadyToSend($$02, $$22), (Executor)this.mainThreadExecutor);
            }
            return $$22;
        }), (Executor)this.mainThreadExecutor);
    }

    private void onChunkReadyToSend(ChunkHolder $$0, LevelChunk $$1) {
        ChunkPos $$2 = $$1.getPos();
        for (ServerPlayer $$3 : this.playerMap.getAllPlayers()) {
            if (!$$3.getChunkTrackingView().contains($$2)) continue;
            ChunkMap.markChunkPendingToSend($$3, $$1);
        }
        this.level.getChunkSource().onChunkReadyToSend($$0);
        this.level.debugSynchronizers().registerChunk($$1);
    }

    public CompletableFuture<ChunkResult<LevelChunk>> prepareAccessibleChunk(ChunkHolder $$0) {
        return this.getChunkRangeFuture($$0, 1, ChunkLevel::getStatusAroundFullChunk).thenApply($$02 -> $$02.map($$0 -> (LevelChunk)$$0.get($$0.size() / 2)));
    }

    Stream<ChunkHolder> allChunksWithAtLeastStatus(ChunkStatus $$0) {
        int $$12 = ChunkLevel.byStatus($$0);
        return this.visibleChunkMap.values().stream().filter($$1 -> $$1.getTicketLevel() <= $$12);
    }

    private boolean saveChunkIfNeeded(ChunkHolder $$0, long $$1) {
        if (!$$0.wasAccessibleSinceLastSave() || !$$0.isReadyForSaving()) {
            return false;
        }
        ChunkAccess $$2 = $$0.getLatestChunk();
        if ($$2 instanceof ImposterProtoChunk || $$2 instanceof LevelChunk) {
            if (!$$2.isUnsaved()) {
                return false;
            }
            long $$3 = $$2.getPos().toLong();
            long $$4 = this.nextChunkSaveTime.getOrDefault($$3, -1L);
            if ($$1 < $$4) {
                return false;
            }
            boolean $$5 = this.save($$2);
            $$0.refreshAccessibility();
            if ($$5) {
                this.nextChunkSaveTime.put($$3, $$1 + 10000L);
            }
            return $$5;
        }
        return false;
    }

    private boolean save(ChunkAccess $$0) {
        this.poiManager.flush($$0.getPos());
        if (!$$0.tryMarkSaved()) {
            return false;
        }
        ChunkPos $$12 = $$0.getPos();
        try {
            ChunkStatus $$22 = $$0.getPersistedStatus();
            if ($$22.getChunkType() != ChunkType.LEVELCHUNK) {
                if (this.isExistingChunkFull($$12)) {
                    return false;
                }
                if ($$22 == ChunkStatus.EMPTY && $$0.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                    return false;
                }
            }
            Profiler.get().incrementCounter("chunkSave");
            this.activeChunkWrites.incrementAndGet();
            SerializableChunkData $$3 = SerializableChunkData.copyOf(this.level, $$0);
            CompletableFuture<CompoundTag> $$4 = CompletableFuture.supplyAsync($$3::write, Util.backgroundExecutor());
            this.write($$12, $$4::join).handle(($$1, $$2) -> {
                if ($$2 != null) {
                    this.level.getServer().reportChunkSaveFailure((Throwable)$$2, this.storageInfo(), $$12);
                }
                this.activeChunkWrites.decrementAndGet();
                return null;
            });
            this.markPosition($$12, $$22.getChunkType());
            return true;
        }
        catch (Exception $$5) {
            this.level.getServer().reportChunkSaveFailure($$5, this.storageInfo(), $$12);
            return false;
        }
    }

    /*
     * WARNING - void declaration
     */
    private boolean isExistingChunkFull(ChunkPos $$0) {
        void $$4;
        byte $$1 = this.chunkTypeCache.get($$0.toLong());
        if ($$1 != 0) {
            return $$1 == 1;
        }
        try {
            CompoundTag $$2 = this.readChunk($$0).join().orElse(null);
            if ($$2 == null) {
                this.markPositionReplaceable($$0);
                return false;
            }
        }
        catch (Exception $$3) {
            LOGGER.error("Failed to read chunk {}", (Object)$$0, (Object)$$3);
            this.markPositionReplaceable($$0);
            return false;
        }
        ChunkType $$5 = SerializableChunkData.getChunkStatusFromTag((CompoundTag)$$4).getChunkType();
        return this.markPosition($$0, $$5) == 1;
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
            ChunkMap.markChunkPendingToSend($$0, $$2);
        }
    }

    private static void markChunkPendingToSend(ServerPlayer $$0, LevelChunk $$1) {
        $$0.connection.chunkSender.markChunkPendingToSend($$1);
    }

    private static void dropChunk(ServerPlayer $$0, ChunkPos $$1) {
        $$0.connection.chunkSender.dropChunk($$0, $$1);
    }

    public @Nullable LevelChunk getChunkToSend(long $$0) {
        ChunkHolder $$1 = this.getVisibleChunkIfPresent($$0);
        if ($$1 == null) {
            return null;
        }
        return $$1.getChunkToSend();
    }

    public int size() {
        return this.visibleChunkMap.size();
    }

    public net.minecraft.server.level.DistanceManager getDistanceManager() {
        return this.distanceManager;
    }

    void dumpChunks(Writer $$02) throws IOException {
        CsvOutput $$1 = CsvOutput.builder().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").addColumn("ticking_ticket").addColumn("ticking_level").addColumn("block_ticks").addColumn("fluid_ticks").build($$02);
        for (Long2ObjectMap.Entry $$2 : this.visibleChunkMap.long2ObjectEntrySet()) {
            long $$3 = $$2.getLongKey();
            ChunkPos $$4 = new ChunkPos($$3);
            ChunkHolder $$5 = (ChunkHolder)$$2.getValue();
            Optional<ChunkAccess> $$6 = Optional.ofNullable($$5.getLatestChunk());
            Optional<Object> $$7 = $$6.flatMap($$0 -> $$0 instanceof LevelChunk ? Optional.of((LevelChunk)$$0) : Optional.empty());
            $$1.writeRow($$4.x, $$4.z, $$5.getTicketLevel(), $$6.isPresent(), $$6.map(ChunkAccess::getPersistedStatus).orElse(null), $$7.map(LevelChunk::getFullStatus).orElse(null), ChunkMap.printFuture($$5.getFullChunkFuture()), ChunkMap.printFuture($$5.getTickingChunkFuture()), ChunkMap.printFuture($$5.getEntityTickingChunkFuture()), this.ticketStorage.getTicketDebugString($$3, false), this.anyPlayerCloseEnoughForSpawning($$4), $$7.map($$0 -> $$0.getBlockEntities().size()).orElse(0), this.ticketStorage.getTicketDebugString($$3, true), this.distanceManager.getChunkLevel($$3, true), $$7.map($$0 -> $$0.getBlockTicks().count()).orElse(0), $$7.map($$0 -> $$0.getFluidTicks().count()).orElse(0));
        }
    }

    private static String printFuture(CompletableFuture<ChunkResult<LevelChunk>> $$0) {
        try {
            ChunkResult $$1 = $$0.getNow(null);
            if ($$1 != null) {
                return $$1.isSuccess() ? "done" : "unloaded";
            }
            return "not completed";
        }
        catch (CompletionException $$2) {
            return "failed " + $$2.getCause().getMessage();
        }
        catch (CancellationException $$3) {
            return "cancelled";
        }
    }

    private CompletableFuture<Optional<CompoundTag>> readChunk(ChunkPos $$02) {
        return this.read($$02).thenApplyAsync($$0 -> $$0.map(this::upgradeChunkTag), Util.backgroundExecutor().forName("upgradeChunk"));
    }

    private CompoundTag upgradeChunkTag(CompoundTag $$0) {
        return this.upgradeChunkTag($$0, -1, ChunkMap.getChunkDataFixContextTag(this.level.dimension(), this.generator().getTypeNameForDataFixer()));
    }

    public static CompoundTag getChunkDataFixContextTag(ResourceKey<Level> $$0, Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> $$12) {
        CompoundTag $$2 = new CompoundTag();
        $$2.putString("dimension", $$0.identifier().toString());
        $$12.ifPresent($$1 -> $$2.putString("generator", $$1.identifier().toString()));
        return $$2;
    }

    void collectSpawningChunks(List<LevelChunk> $$0) {
        LongIterator $$1 = this.distanceManager.getSpawnCandidateChunks();
        while ($$1.hasNext()) {
            LevelChunk $$3;
            ChunkHolder $$2 = (ChunkHolder)this.visibleChunkMap.get($$1.nextLong());
            if ($$2 == null || ($$3 = $$2.getTickingChunk()) == null || !this.anyPlayerCloseEnoughForSpawningInternal($$2.getPos())) continue;
            $$0.add($$3);
        }
    }

    void forEachBlockTickingChunk(Consumer<LevelChunk> $$0) {
        this.distanceManager.forEachEntityTickingChunk($$1 -> {
            ChunkHolder $$2 = (ChunkHolder)this.visibleChunkMap.get($$1);
            if ($$2 == null) {
                return;
            }
            LevelChunk $$3 = $$2.getTickingChunk();
            if ($$3 == null) {
                return;
            }
            $$0.accept($$3);
        });
    }

    boolean anyPlayerCloseEnoughForSpawning(ChunkPos $$0) {
        TriState $$1 = this.distanceManager.hasPlayersNearby($$0.toLong());
        if ($$1 == TriState.DEFAULT) {
            return this.anyPlayerCloseEnoughForSpawningInternal($$0);
        }
        return $$1.toBoolean(true);
    }

    boolean anyPlayerCloseEnoughTo(BlockPos $$0, int $$1) {
        Vec3 $$2 = new Vec3($$0);
        for (ServerPlayer $$3 : this.playerMap.getAllPlayers()) {
            if (!this.playerIsCloseEnoughTo($$3, $$2, $$1)) continue;
            return true;
        }
        return false;
    }

    private boolean anyPlayerCloseEnoughForSpawningInternal(ChunkPos $$0) {
        for (ServerPlayer $$1 : this.playerMap.getAllPlayers()) {
            if (!this.playerIsCloseEnoughForSpawning($$1, $$0)) continue;
            return true;
        }
        return false;
    }

    public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos $$0) {
        long $$1 = $$0.toLong();
        if (!this.distanceManager.hasPlayersNearby($$1).toBoolean(true)) {
            return List.of();
        }
        ImmutableList.Builder $$2 = ImmutableList.builder();
        for (ServerPlayer $$3 : this.playerMap.getAllPlayers()) {
            if (!this.playerIsCloseEnoughForSpawning($$3, $$0)) continue;
            $$2.add((Object)$$3);
        }
        return $$2.build();
    }

    private boolean playerIsCloseEnoughForSpawning(ServerPlayer $$0, ChunkPos $$1) {
        if ($$0.isSpectator()) {
            return false;
        }
        double $$2 = ChunkMap.euclideanDistanceSquared($$1, $$0.position());
        return $$2 < 16384.0;
    }

    private boolean playerIsCloseEnoughTo(ServerPlayer $$0, Vec3 $$1, int $$2) {
        if ($$0.isSpectator()) {
            return false;
        }
        double $$3 = $$0.position().distanceTo($$1);
        return $$3 < (double)$$2;
    }

    private static double euclideanDistanceSquared(ChunkPos $$0, Vec3 $$1) {
        double $$2 = SectionPos.sectionToBlockCoord($$0.x, 8);
        double $$3 = SectionPos.sectionToBlockCoord($$0.z, 8);
        double $$4 = $$2 - $$1.x;
        double $$5 = $$3 - $$1.z;
        return $$4 * $$4 + $$5 * $$5;
    }

    private boolean skipPlayer(ServerPlayer $$0) {
        return $$0.isSpectator() && this.level.getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS) == false;
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
        boolean $$6;
        for (TrackedEntity $$1 : this.entityMap.values()) {
            if ($$1.entity == $$0) {
                $$1.updatePlayers(this.level.players());
                continue;
            }
            $$1.updatePlayer($$0);
        }
        SectionPos $$2 = $$0.getLastSectionPos();
        SectionPos $$3 = SectionPos.of($$0);
        boolean $$4 = this.playerMap.ignored($$0);
        boolean $$5 = this.skipPlayer($$0);
        boolean bl = $$6 = $$2.asLong() != $$3.asLong();
        if ($$6 || $$4 != $$5) {
            this.updatePlayerPos($$0);
            if (!$$4) {
                this.distanceManager.removePlayer($$2, $$0);
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
        ChunkTrackingView.Positioned $$3;
        ChunkPos $$1 = $$0.chunkPosition();
        int $$2 = this.getPlayerViewDistance($$0);
        ChunkTrackingView chunkTrackingView = $$0.getChunkTrackingView();
        if (chunkTrackingView instanceof ChunkTrackingView.Positioned && ($$3 = (ChunkTrackingView.Positioned)chunkTrackingView).center().equals($$1) && $$3.viewDistance() == $$2) {
            return;
        }
        this.applyChunkTrackingView($$0, ChunkTrackingView.of($$1, $$2));
    }

    private void applyChunkTrackingView(ServerPlayer $$0, ChunkTrackingView $$12) {
        if ($$0.level() != this.level) {
            return;
        }
        ChunkTrackingView $$2 = $$0.getChunkTrackingView();
        if ($$12 instanceof ChunkTrackingView.Positioned) {
            ChunkTrackingView.Positioned $$4;
            ChunkTrackingView.Positioned $$3 = (ChunkTrackingView.Positioned)$$12;
            if (!($$2 instanceof ChunkTrackingView.Positioned) || !($$4 = (ChunkTrackingView.Positioned)$$2).center().equals($$3.center())) {
                $$0.connection.send(new ClientboundSetChunkCacheCenterPacket($$3.center().x, $$3.center().z));
            }
        }
        ChunkTrackingView.difference($$2, $$12, $$1 -> this.markChunkPendingToSend($$0, (ChunkPos)$$1), $$1 -> ChunkMap.dropChunk($$0, $$1));
        $$0.setChunkTrackingView($$12);
    }

    @Override
    public List<ServerPlayer> getPlayers(ChunkPos $$0, boolean $$1) {
        Set<ServerPlayer> $$2 = this.playerMap.getAllPlayers();
        ImmutableList.Builder $$3 = ImmutableList.builder();
        for (ServerPlayer $$4 : $$2) {
            if ((!$$1 || !this.isChunkOnTrackedBorder($$4, $$0.x, $$0.z)) && ($$1 || !this.isChunkTracked($$4, $$0.x, $$0.z))) continue;
            $$3.add((Object)$$4);
        }
        return $$3.build();
    }

    protected void addEntity(Entity $$0) {
        if ($$0 instanceof EnderDragonPart) {
            return;
        }
        EntityType<?> $$1 = $$0.getType();
        int $$2 = $$1.clientTrackingRange() * 16;
        if ($$2 == 0) {
            return;
        }
        int $$3 = $$1.updateInterval();
        if (this.entityMap.containsKey($$0.getId())) {
            throw Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
        }
        TrackedEntity $$4 = new TrackedEntity($$0, $$2, $$3, $$1.trackDeltas());
        this.entityMap.put($$0.getId(), (Object)$$4);
        $$4.updatePlayers(this.level.players());
        if ($$0 instanceof ServerPlayer) {
            ServerPlayer $$5 = (ServerPlayer)$$0;
            this.updatePlayerStatus($$5, true);
            for (TrackedEntity $$6 : this.entityMap.values()) {
                if ($$6.entity == $$5) continue;
                $$6.updatePlayer($$5);
            }
        }
    }

    protected void removeEntity(Entity $$0) {
        TrackedEntity $$3;
        if ($$0 instanceof ServerPlayer) {
            ServerPlayer $$1 = (ServerPlayer)$$0;
            this.updatePlayerStatus($$1, false);
            for (TrackedEntity $$2 : this.entityMap.values()) {
                $$2.removePlayer($$1);
            }
        }
        if (($$3 = (TrackedEntity)this.entityMap.remove($$0.getId())) != null) {
            $$3.broadcastRemoved();
        }
    }

    protected void tick() {
        for (ServerPlayer $$0 : this.playerMap.getAllPlayers()) {
            this.updateChunkTracking($$0);
        }
        ArrayList $$1 = Lists.newArrayList();
        List<ServerPlayer> $$2 = this.level.players();
        for (TrackedEntity $$3 : this.entityMap.values()) {
            boolean $$6;
            SectionPos $$4 = $$3.lastSectionPos;
            SectionPos $$5 = SectionPos.of($$3.entity);
            boolean bl = $$6 = !Objects.equals($$4, $$5);
            if ($$6) {
                $$3.updatePlayers($$2);
                Entity $$7 = $$3.entity;
                if ($$7 instanceof ServerPlayer) {
                    $$1.add((ServerPlayer)$$7);
                }
                $$3.lastSectionPos = $$5;
            }
            if (!$$6 && !$$3.entity.needsSync && !this.distanceManager.inEntityTickingRange($$5.chunk().toLong())) continue;
            $$3.serverEntity.sendChanges();
        }
        if (!$$1.isEmpty()) {
            for (TrackedEntity $$8 : this.entityMap.values()) {
                $$8.updatePlayers($$1);
            }
        }
    }

    public void sendToTrackingPlayers(Entity $$0, Packet<? super ClientGamePacketListener> $$1) {
        TrackedEntity $$2 = (TrackedEntity)this.entityMap.get($$0.getId());
        if ($$2 != null) {
            $$2.sendToTrackingPlayers($$1);
        }
    }

    public void sendToTrackingPlayersFiltered(Entity $$0, Packet<? super ClientGamePacketListener> $$1, Predicate<ServerPlayer> $$2) {
        TrackedEntity $$3 = (TrackedEntity)this.entityMap.get($$0.getId());
        if ($$3 != null) {
            $$3.sendToTrackingPlayersFiltered($$1, $$2);
        }
    }

    protected void sendToTrackingPlayersAndSelf(Entity $$0, Packet<? super ClientGamePacketListener> $$1) {
        TrackedEntity $$2 = (TrackedEntity)this.entityMap.get($$0.getId());
        if ($$2 != null) {
            $$2.sendToTrackingPlayersAndSelf($$1);
        }
    }

    public boolean isTrackedByAnyPlayer(Entity $$0) {
        TrackedEntity $$1 = (TrackedEntity)this.entityMap.get($$0.getId());
        if ($$1 != null) {
            return !$$1.seenBy.isEmpty();
        }
        return false;
    }

    public void forEachEntityTrackedBy(ServerPlayer $$0, Consumer<Entity> $$1) {
        for (TrackedEntity $$2 : this.entityMap.values()) {
            if (!$$2.seenBy.contains($$0.connection)) continue;
            $$1.accept($$2.entity);
        }
    }

    public void resendBiomesForChunks(List<ChunkAccess> $$02) {
        HashMap<ServerPlayer, List> $$12 = new HashMap<ServerPlayer, List>();
        for (ChunkAccess $$2 : $$02) {
            LevelChunk $$6;
            ChunkPos $$3 = $$2.getPos();
            if ($$2 instanceof LevelChunk) {
                LevelChunk $$4;
                LevelChunk $$5 = $$4 = (LevelChunk)$$2;
            } else {
                $$6 = this.level.getChunk($$3.x, $$3.z);
            }
            for (ServerPlayer $$7 : this.getPlayers($$3, false)) {
                $$12.computeIfAbsent($$7, $$0 -> new ArrayList()).add($$6);
            }
        }
        $$12.forEach(($$0, $$1) -> $$0.connection.send(ClientboundChunksBiomesPacket.forChunks($$1)));
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

    public void waitForLightBeforeSending(ChunkPos $$02, int $$1) {
        int $$2 = $$1 + 1;
        ChunkPos.rangeClosed($$02, $$2).forEach($$0 -> {
            ChunkHolder $$1 = this.getVisibleChunkIfPresent($$0.toLong());
            if ($$1 != null) {
                $$1.addSendDependency(this.lightEngine.waitForPendingTasks($$0.x, $$0.z));
            }
        });
    }

    public void forEachReadyToSendChunk(Consumer<LevelChunk> $$0) {
        for (ChunkHolder $$1 : this.visibleChunkMap.values()) {
            LevelChunk $$2 = $$1.getChunkToSend();
            if ($$2 == null) continue;
            $$0.accept($$2);
        }
    }

    class DistanceManager
    extends net.minecraft.server.level.DistanceManager {
        protected DistanceManager(TicketStorage $$0, Executor $$1, Executor $$2) {
            super($$0, $$1, $$2);
        }

        @Override
        protected boolean isChunkToRemove(long $$0) {
            return ChunkMap.this.toDrop.contains($$0);
        }

        @Override
        protected @Nullable ChunkHolder getChunk(long $$0) {
            return ChunkMap.this.getUpdatingChunkIfPresent($$0);
        }

        @Override
        protected @Nullable ChunkHolder updateChunkScheduling(long $$0, int $$1, @Nullable ChunkHolder $$2, int $$3) {
            return ChunkMap.this.updateChunkScheduling($$0, $$1, $$2, $$3);
        }
    }

    class TrackedEntity
    implements ServerEntity.Synchronizer {
        final ServerEntity serverEntity;
        final Entity entity;
        private final int range;
        SectionPos lastSectionPos;
        final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

        public TrackedEntity(Entity $$0, int $$1, int $$2, boolean $$3) {
            this.serverEntity = new ServerEntity(ChunkMap.this.level, $$0, $$2, $$3, this);
            this.entity = $$0;
            this.range = $$1;
            this.lastSectionPos = SectionPos.of($$0);
        }

        public boolean equals(Object $$0) {
            if ($$0 instanceof TrackedEntity) {
                return ((TrackedEntity)$$0).entity.getId() == this.entity.getId();
            }
            return false;
        }

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
            Entity entity = this.entity;
            if (entity instanceof ServerPlayer) {
                ServerPlayer $$1 = (ServerPlayer)entity;
                $$1.connection.send($$0);
            }
        }

        @Override
        public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> $$0, Predicate<ServerPlayer> $$1) {
            for (ServerPlayerConnection $$2 : this.seenBy) {
                if (!$$1.test($$2.getPlayer())) continue;
                $$2.send($$0);
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
            boolean $$6;
            if ($$0 == this.entity) {
                return;
            }
            Vec3 $$1 = $$0.position().subtract(this.entity.position());
            int $$2 = ChunkMap.this.getPlayerViewDistance($$0);
            double $$4 = $$1.x * $$1.x + $$1.z * $$1.z;
            double $$3 = Math.min(this.getEffectiveRange(), $$2 * 16);
            double $$5 = $$3 * $$3;
            boolean bl = $$6 = $$4 <= $$5 && this.entity.broadcastToPlayer($$0) && ChunkMap.this.isChunkTracked($$0, this.entity.chunkPosition().x, this.entity.chunkPosition().z);
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

        private int scaledRange(int $$0) {
            return ChunkMap.this.level.getServer().getScaledTrackingDistance($$0);
        }

        private int getEffectiveRange() {
            int $$0 = this.range;
            for (Entity $$1 : this.entity.getIndirectPassengers()) {
                int $$2 = $$1.getType().clientTrackingRange() * 16;
                if ($$2 <= $$0) continue;
                $$0 = $$2;
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

