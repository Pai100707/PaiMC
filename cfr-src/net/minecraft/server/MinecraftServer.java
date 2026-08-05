/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.authlib.GameProfile
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.jtracy.DiscontinuousFrame
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.DataFixer;
import com.mojang.jtracy.DiscontinuousFrame;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.runtime.ObjectMethods;
import java.net.Proxy;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.ServerInfo;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.Services;
import net.minecraft.server.SuppressedExceptionCollector;
import net.minecraft.server.TickTask;
import net.minecraft.server.WorldStem;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ChunkLoadCounter;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.notifications.NotificationManager;
import net.minecraft.server.notifications.ServerActivityMonitor;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.FileUtil;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.PngInfo;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Util;
import net.minecraft.util.debug.ServerDebugSubscribers;
import net.minecraft.util.debugchart.SampleLogger;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.Stopwatches;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class MinecraftServer
extends ReentrantBlockableEventLoop<TickTask>
implements ServerInfo,
CommandSource,
ChunkIOErrorReporter {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String VANILLA_BRAND = "vanilla";
    private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8f;
    private static final int TICK_STATS_SPAN = 100;
    private static final long OVERLOADED_THRESHOLD_NANOS = 20L * TimeUtil.NANOSECONDS_PER_SECOND / 20L;
    private static final int OVERLOADED_TICKS_THRESHOLD = 20;
    private static final long OVERLOADED_WARNING_INTERVAL_NANOS = 10L * TimeUtil.NANOSECONDS_PER_SECOND;
    private static final int OVERLOADED_TICKS_WARNING_INTERVAL = 100;
    private static final long STATUS_EXPIRE_TIME_NANOS = 5L * TimeUtil.NANOSECONDS_PER_SECOND;
    private static final long PREPARE_LEVELS_DEFAULT_DELAY_NANOS = 10L * TimeUtil.NANOSECONDS_PER_MILLISECOND;
    private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
    public static final int SPAWN_POSITION_SEARCH_RADIUS = 5;
    private static final int SERVER_ACTIVITY_MONITOR_SECONDS_BETWEEN_NOTIFICATIONS = 30;
    private static final int AUTOSAVE_INTERVAL = 6000;
    private static final int MIMINUM_AUTOSAVE_TICKS = 100;
    private static final int MAX_TICK_LATENCY = 3;
    public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
    public static final LevelSettings DEMO_SETTINGS = new LevelSettings("Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(FeatureFlags.DEFAULT_FLAGS), WorldDataConfiguration.DEFAULT);
    public static final NameAndId ANONYMOUS_PLAYER_PROFILE = new NameAndId(Util.NIL_UUID, "Anonymous Player");
    protected final LevelStorageSource.LevelStorageAccess storageSource;
    protected final PlayerDataStorage playerDataStorage;
    private final List<Runnable> tickables = Lists.newArrayList();
    private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    private Consumer<ProfileResults> onMetricsRecordingStopped = $$0 -> this.stopRecordingMetrics();
    private Consumer<Path> onMetricsRecordingFinished = $$0 -> {};
    private boolean willStartRecordingMetrics;
    private @Nullable TimeProfiler debugCommandProfiler;
    private boolean debugCommandProfilerDelayStart;
    private final ServerConnectionListener connection;
    private final LevelLoadListener levelLoadListener;
    private @Nullable ServerStatus status;
    private @Nullable ServerStatus.Favicon statusIcon;
    private final RandomSource random = RandomSource.create();
    private final DataFixer fixerUpper;
    private String localIp;
    private int port = -1;
    private final LayeredRegistryAccess<RegistryLayer> registries;
    private final Map<ResourceKey<Level>, ServerLevel> levels = Maps.newLinkedHashMap();
    private PlayerList playerList;
    private volatile boolean running = true;
    private boolean stopped;
    private int tickCount;
    private int ticksUntilAutosave = 6000;
    protected final Proxy proxy;
    private boolean onlineMode;
    private boolean preventProxyConnections;
    private @Nullable String motd;
    private int playerIdleTimeout;
    private final long[] tickTimesNanos = new long[100];
    private long aggregatedTickTimesNanos = 0L;
    private @Nullable KeyPair keyPair;
    private @Nullable GameProfile singleplayerProfile;
    private boolean isDemo;
    private volatile boolean isReady;
    private long lastOverloadWarningNanos;
    protected final Services services;
    private final NotificationManager notificationManager;
    private final ServerActivityMonitor serverActivityMonitor;
    private long lastServerStatus;
    private final Thread serverThread;
    private long lastTickNanos = Util.getNanos();
    private long taskExecutionStartNanos = Util.getNanos();
    private long idleTimeNanos;
    private long nextTickTimeNanos = Util.getNanos();
    private boolean waitingForNextTick = false;
    private long delayedTasksMaxNextTickTimeNanos;
    private boolean mayHaveDelayedTasks;
    private final PackRepository packRepository;
    private final ServerScoreboard scoreboard = new ServerScoreboard(this);
    private @Nullable Stopwatches stopwatches;
    private @Nullable CommandStorage commandStorage;
    private final CustomBossEvents customBossEvents = new CustomBossEvents();
    private final ServerFunctionManager functionManager;
    private boolean enforceWhitelist;
    private boolean usingWhitelist;
    private float smoothedTickTimeMillis;
    private final Executor executor;
    private @Nullable String serverId;
    private ReloadableResources resources;
    private final StructureTemplateManager structureTemplateManager;
    private final ServerTickRateManager tickRateManager;
    private final ServerDebugSubscribers debugSubscribers = new ServerDebugSubscribers(this);
    protected final WorldData worldData;
    private LevelData.RespawnData effectiveRespawnData = LevelData.RespawnData.DEFAULT;
    private final PotionBrewing potionBrewing;
    private FuelValues fuelValues;
    private int emptyTicks;
    private volatile boolean isSaving;
    private static final AtomicReference<@Nullable RuntimeException> fatalException = new AtomicReference();
    private final SuppressedExceptionCollector suppressedExceptions = new SuppressedExceptionCollector();
    private final DiscontinuousFrame tickFrame;
    private final PacketProcessor packetProcessor;

    public static <S extends MinecraftServer> S spin(Function<Thread, S> $$02) {
        AtomicReference<MinecraftServer> $$12 = new AtomicReference<MinecraftServer>();
        Thread $$2 = new Thread(() -> ((MinecraftServer)$$12.get()).runServer(), "Server thread");
        $$2.setUncaughtExceptionHandler(($$0, $$1) -> LOGGER.error("Uncaught exception in server thread", $$1));
        if (Runtime.getRuntime().availableProcessors() > 4) {
            $$2.setPriority(8);
        }
        MinecraftServer $$3 = (MinecraftServer)$$02.apply($$2);
        $$12.set($$3);
        $$2.start();
        return (S)$$3;
    }

    public MinecraftServer(Thread $$02, LevelStorageSource.LevelStorageAccess $$1, PackRepository $$2, WorldStem $$3, Proxy $$4, DataFixer $$5, Services $$6, LevelLoadListener $$7) {
        super("Server");
        this.registries = $$3.registries();
        this.worldData = $$3.worldData();
        if (!this.registries.compositeAccess().lookupOrThrow(Registries.LEVEL_STEM).containsKey(LevelStem.OVERWORLD)) {
            throw new IllegalStateException("Missing Overworld dimension data");
        }
        this.proxy = $$4;
        this.packRepository = $$2;
        this.resources = new ReloadableResources($$3.resourceManager(), $$3.dataPackResources());
        this.services = $$6;
        this.connection = new ServerConnectionListener(this);
        this.tickRateManager = new ServerTickRateManager(this);
        this.levelLoadListener = $$7;
        this.storageSource = $$1;
        this.playerDataStorage = $$1.createPlayerStorage();
        this.fixerUpper = $$5;
        this.functionManager = new ServerFunctionManager(this, this.resources.managers.getFunctionLibrary());
        HolderLookup.RegistryLookup $$8 = this.registries.compositeAccess().lookupOrThrow(Registries.BLOCK).filterFeatures(this.worldData.enabledFeatures());
        this.structureTemplateManager = new StructureTemplateManager($$3.resourceManager(), $$1, $$5, $$8);
        this.serverThread = $$02;
        this.executor = Util.backgroundExecutor();
        this.potionBrewing = PotionBrewing.bootstrap(this.worldData.enabledFeatures());
        this.resources.managers.getRecipeManager().finalizeRecipeLoading(this.worldData.enabledFeatures());
        this.fuelValues = FuelValues.vanillaBurnTimes(this.registries.compositeAccess(), this.worldData.enabledFeatures());
        this.tickFrame = TracyClient.createDiscontinuousFrame((String)"Server Tick");
        this.notificationManager = new NotificationManager();
        this.serverActivityMonitor = new ServerActivityMonitor(this.notificationManager, 30);
        this.packetProcessor = new PacketProcessor($$02);
    }

    protected abstract boolean initServer() throws IOException;

    public ChunkLoadStatusView createChunkLoadStatusView(final int $$0) {
        return new ChunkLoadStatusView(){
            private @Nullable ChunkMap chunkMap;
            private int centerChunkX;
            private int centerChunkZ;

            @Override
            public void moveTo(ResourceKey<Level> $$02, ChunkPos $$1) {
                ServerLevel $$2 = MinecraftServer.this.getLevel($$02);
                this.chunkMap = $$2 != null ? $$2.getChunkSource().chunkMap : null;
                this.centerChunkX = $$1.x;
                this.centerChunkZ = $$1.z;
            }

            @Override
            public @Nullable ChunkStatus get(int $$02, int $$1) {
                if (this.chunkMap == null) {
                    return null;
                }
                return this.chunkMap.getLatestStatus(ChunkPos.asLong($$02 + this.centerChunkX - $$0, $$1 + this.centerChunkZ - $$0));
            }

            @Override
            public int radius() {
                return $$0;
            }
        };
    }

    protected void loadLevel() {
        boolean $$0 = !JvmProfiler.INSTANCE.isRunning() && SharedConstants.DEBUG_JFR_PROFILING_ENABLE_LEVEL_LOADING && JvmProfiler.INSTANCE.start(Environment.from(this));
        ProfiledDuration $$1 = JvmProfiler.INSTANCE.onWorldLoadedStarted();
        this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().shouldReportAsModified());
        this.createLevels();
        this.forceDifficulty();
        this.prepareLevels();
        if ($$1 != null) {
            $$1.finish(true);
        }
        if ($$0) {
            try {
                JvmProfiler.INSTANCE.stop();
            }
            catch (Throwable $$2) {
                LOGGER.warn("Failed to stop JFR profiling", $$2);
            }
        }
    }

    protected void forceDifficulty() {
    }

    protected void createLevels() {
        ServerLevelData $$0 = this.worldData.overworldData();
        boolean $$1 = this.worldData.isDebugWorld();
        HolderLookup.RegistryLookup $$2 = this.registries.compositeAccess().lookupOrThrow(Registries.LEVEL_STEM);
        WorldOptions $$3 = this.worldData.worldGenOptions();
        long $$4 = $$3.seed();
        long $$5 = BiomeManager.obfuscateSeed($$4);
        ImmutableList $$6 = ImmutableList.of((Object)new PhantomSpawner(), (Object)new PatrolSpawner(), (Object)new CatSpawner(), (Object)new VillageSiege(), (Object)new WanderingTraderSpawner($$0));
        LevelStem $$7 = $$2.getValue(LevelStem.OVERWORLD);
        ServerLevel $$8 = new ServerLevel(this, this.executor, this.storageSource, $$0, Level.OVERWORLD, $$7, $$1, $$5, (List<CustomSpawner>)$$6, true, null);
        this.levels.put(Level.OVERWORLD, $$8);
        DimensionDataStorage $$9 = $$8.getDataStorage();
        this.scoreboard.load($$9.computeIfAbsent(ScoreboardSaveData.TYPE).getData());
        this.commandStorage = new CommandStorage($$9);
        this.stopwatches = $$9.computeIfAbsent(Stopwatches.TYPE);
        if (!$$0.isInitialized()) {
            try {
                MinecraftServer.setInitialSpawn($$8, $$0, $$3.generateBonusChest(), $$1, this.levelLoadListener);
                $$0.setInitialized(true);
                if ($$1) {
                    this.setupDebugLevel(this.worldData);
                }
            }
            catch (Throwable $$10) {
                CrashReport $$11 = CrashReport.forThrowable($$10, "Exception initializing level");
                try {
                    $$8.fillReportDetails($$11);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                throw new ReportedException($$11);
            }
            $$0.setInitialized(true);
        }
        GlobalPos $$12 = this.selectLevelLoadFocusPos();
        this.levelLoadListener.updateFocus($$12.dimension(), new ChunkPos($$12.pos()));
        if (this.worldData.getCustomBossEvents() != null) {
            this.getCustomBossEvents().load(this.worldData.getCustomBossEvents(), this.registryAccess());
        }
        RandomSequences $$13 = $$8.getRandomSequences();
        boolean $$14 = false;
        for (Map.Entry $$15 : $$2.entrySet()) {
            ServerLevel $$20;
            ResourceKey $$16 = $$15.getKey();
            if ($$16 != LevelStem.OVERWORLD) {
                ResourceKey<Level> $$17 = ResourceKey.create(Registries.DIMENSION, $$16.identifier());
                DerivedLevelData $$18 = new DerivedLevelData(this.worldData, $$0);
                ServerLevel $$19 = new ServerLevel(this, this.executor, this.storageSource, $$18, $$17, (LevelStem)$$15.getValue(), $$1, $$5, (List<CustomSpawner>)ImmutableList.of(), false, $$13);
                this.levels.put($$17, $$19);
            } else {
                $$20 = $$8;
            }
            Optional<WorldBorder.Settings> $$21 = $$0.getLegacyWorldBorderSettings();
            if ($$21.isPresent()) {
                WorldBorder.Settings $$22 = $$21.get();
                DimensionDataStorage $$23 = $$20.getDataStorage();
                if ($$23.get(WorldBorder.TYPE) == null) {
                    double $$24 = $$20.dimensionType().coordinateScale();
                    WorldBorder.Settings $$25 = new WorldBorder.Settings($$22.centerX() / $$24, $$22.centerZ() / $$24, $$22.damagePerBlock(), $$22.safeZone(), $$22.warningBlocks(), $$22.warningTime(), $$22.size(), $$22.lerpTime(), $$22.lerpTarget());
                    WorldBorder $$26 = new WorldBorder($$25);
                    $$26.applyInitialSettings($$20.getGameTime());
                    $$23.set(WorldBorder.TYPE, $$26);
                }
                $$14 = true;
            }
            $$20.getWorldBorder().setAbsoluteMaxSize(this.getAbsoluteMaxWorldSize());
            this.getPlayerList().addWorldborderListener($$20);
        }
        if ($$14) {
            $$0.setLegacyWorldBorderSettings(Optional.empty());
        }
    }

    private static void setInitialSpawn(ServerLevel $$02, ServerLevelData $$1, boolean $$2, boolean $$32, LevelLoadListener $$4) {
        if (SharedConstants.DEBUG_ONLY_GENERATE_HALF_THE_WORLD && SharedConstants.DEBUG_WORLD_RECREATE) {
            $$1.setSpawn(LevelData.RespawnData.of($$02.dimension(), new BlockPos(0, 64, -100), 0.0f, 0.0f));
            return;
        }
        if ($$32) {
            $$1.setSpawn(LevelData.RespawnData.of($$02.dimension(), BlockPos.ZERO.above(80), 0.0f, 0.0f));
            return;
        }
        ServerChunkCache $$5 = $$02.getChunkSource();
        ChunkPos $$6 = new ChunkPos($$5.randomState().sampler().findSpawnPosition());
        $$4.start(LevelLoadListener.Stage.PREPARE_GLOBAL_SPAWN, 0);
        $$4.updateFocus($$02.dimension(), $$6);
        int $$7 = $$5.getGenerator().getSpawnHeight($$02);
        if ($$7 < $$02.getMinY()) {
            BlockPos $$8 = $$6.getWorldPosition();
            $$7 = $$02.getHeight(Heightmap.Types.WORLD_SURFACE, $$8.getX() + 8, $$8.getZ() + 8);
        }
        $$1.setSpawn(LevelData.RespawnData.of($$02.dimension(), $$6.getWorldPosition().offset(8, $$7, 8), 0.0f, 0.0f));
        int $$9 = 0;
        int $$10 = 0;
        int $$11 = 0;
        int $$12 = -1;
        for (int $$13 = 0; $$13 < Mth.square(11); ++$$13) {
            BlockPos $$14;
            if ($$9 >= -5 && $$9 <= 5 && $$10 >= -5 && $$10 <= 5 && ($$14 = PlayerSpawnFinder.getSpawnPosInChunk($$02, new ChunkPos($$6.x + $$9, $$6.z + $$10))) != null) {
                $$1.setSpawn(LevelData.RespawnData.of($$02.dimension(), $$14, 0.0f, 0.0f));
                break;
            }
            if ($$9 == $$10 || $$9 < 0 && $$9 == -$$10 || $$9 > 0 && $$9 == 1 - $$10) {
                int $$15 = $$11;
                $$11 = -$$12;
                $$12 = $$15;
            }
            $$9 += $$11;
            $$10 += $$12;
        }
        if ($$2) {
            $$02.registryAccess().lookup(Registries.CONFIGURED_FEATURE).flatMap($$0 -> $$0.get(MiscOverworldFeatures.BONUS_CHEST)).ifPresent($$3 -> ((ConfiguredFeature)$$3.value()).place($$02, $$5.getGenerator(), $$0.random, $$1.getRespawnData().pos()));
        }
        $$4.finish(LevelLoadListener.Stage.PREPARE_GLOBAL_SPAWN);
    }

    private void setupDebugLevel(WorldData $$0) {
        $$0.setDifficulty(Difficulty.PEACEFUL);
        $$0.setDifficultyLocked(true);
        ServerLevelData $$1 = $$0.overworldData();
        $$1.setRaining(false);
        $$1.setThundering(false);
        $$1.setClearWeatherTime(1000000000);
        $$1.setDayTime(6000L);
        $$1.setGameType(GameType.SPECTATOR);
    }

    private void prepareLevels() {
        ChunkLoadCounter $$0 = new ChunkLoadCounter();
        for (ServerLevel $$1 : this.levels.values()) {
            $$0.track($$1, () -> {
                TicketStorage $$1 = $$1.getDataStorage().get(TicketStorage.TYPE);
                if ($$1 != null) {
                    $$1.activateAllDeactivatedTickets();
                }
            });
        }
        this.levelLoadListener.start(LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS, $$0.totalChunks());
        do {
            this.levelLoadListener.update(LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS, $$0.readyChunks(), $$0.totalChunks());
            this.nextTickTimeNanos = Util.getNanos() + PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
            this.waitUntilNextTick();
        } while ($$0.pendingChunks() > 0);
        this.levelLoadListener.finish(LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS);
        this.updateMobSpawningFlags();
        this.updateEffectiveRespawnData();
    }

    protected GlobalPos selectLevelLoadFocusPos() {
        return this.worldData.overworldData().getRespawnData().globalPos();
    }

    public GameType getDefaultGameType() {
        return this.worldData.getGameType();
    }

    public boolean isHardcore() {
        return this.worldData.isHardcore();
    }

    public abstract LevelBasedPermissionSet operatorUserPermissions();

    public abstract PermissionSet getFunctionCompilationPermissions();

    public abstract boolean shouldRconBroadcast();

    public boolean saveAllChunks(boolean $$0, boolean $$1, boolean $$2) {
        this.scoreboard.storeToSaveDataIfDirty(this.overworld().getDataStorage().computeIfAbsent(ScoreboardSaveData.TYPE));
        boolean $$3 = false;
        for (ServerLevel $$4 : this.getAllLevels()) {
            if (!$$0) {
                LOGGER.info("Saving chunks for level '{}'/{}", (Object)$$4, (Object)$$4.dimension().identifier());
            }
            $$4.save(null, $$1, SharedConstants.DEBUG_DONT_SAVE_WORLD || $$4.noSave && !$$2);
            $$3 = true;
        }
        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save(this.registryAccess()));
        this.storageSource.saveDataTag(this.registryAccess(), this.worldData, this.getPlayerList().getSingleplayerData());
        if ($$1) {
            for (ServerLevel $$5 : this.getAllLevels()) {
                LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", (Object)$$5.getChunkSource().chunkMap.getStorageName());
            }
            LOGGER.info("ThreadedAnvilChunkStorage: All dimensions are saved");
        }
        return $$3;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean saveEverything(boolean $$0, boolean $$1, boolean $$2) {
        try {
            this.isSaving = true;
            this.getPlayerList().saveAll();
            boolean bl = this.saveAllChunks($$0, $$1, $$2);
            return bl;
        }
        finally {
            this.isSaving = false;
        }
    }

    @Override
    public void close() {
        this.stopServer();
    }

    public void stopServer() {
        this.packetProcessor.close();
        if (this.metricsRecorder.isRecording()) {
            this.cancelRecordingMetrics();
        }
        LOGGER.info("Stopping server");
        this.getConnection().stop();
        this.isSaving = true;
        if (this.playerList != null) {
            LOGGER.info("Saving players");
            this.playerList.saveAll();
            this.playerList.removeAll();
        }
        LOGGER.info("Saving worlds");
        for (ServerLevel $$02 : this.getAllLevels()) {
            if ($$02 == null) continue;
            $$02.noSave = false;
        }
        while (this.levels.values().stream().anyMatch($$0 -> $$0.getChunkSource().chunkMap.hasWork())) {
            this.nextTickTimeNanos = Util.getNanos() + TimeUtil.NANOSECONDS_PER_MILLISECOND;
            for (ServerLevel $$1 : this.getAllLevels()) {
                $$1.getChunkSource().deactivateTicketsOnClosing();
                $$1.getChunkSource().tick(() -> true, false);
            }
            this.waitUntilNextTick();
        }
        this.saveAllChunks(false, true, false);
        for (ServerLevel $$2 : this.getAllLevels()) {
            if ($$2 == null) continue;
            try {
                $$2.close();
            }
            catch (IOException $$3) {
                LOGGER.error("Exception closing the level", (Throwable)$$3);
            }
        }
        this.isSaving = false;
        this.resources.close();
        try {
            this.storageSource.close();
        }
        catch (IOException $$4) {
            LOGGER.error("Failed to unlock level {}", (Object)this.storageSource.getLevelId(), (Object)$$4);
        }
    }

    public String getLocalIp() {
        return this.localIp;
    }

    public void setLocalIp(String $$0) {
        this.localIp = $$0;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void halt(boolean $$0) {
        this.running = false;
        if ($$0) {
            try {
                this.serverThread.join();
            }
            catch (InterruptedException $$1) {
                LOGGER.error("Error while shutting down", (Throwable)$$1);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected void runServer() {
        try {
            if (!this.initServer()) throw new IllegalStateException("Failed to initialize server");
            this.nextTickTimeNanos = Util.getNanos();
            this.statusIcon = this.loadStatusIcon().orElse(null);
            this.status = this.buildServerStatus();
            while (this.running) {
                boolean $$4;
                long $$1;
                if (!this.isPaused() && this.tickRateManager.isSprinting() && this.tickRateManager.checkShouldSprintThisTick()) {
                    long $$0 = 0L;
                    this.lastOverloadWarningNanos = this.nextTickTimeNanos = Util.getNanos();
                } else {
                    $$1 = this.tickRateManager.nanosecondsPerTick();
                    long $$2 = Util.getNanos() - this.nextTickTimeNanos;
                    if ($$2 > OVERLOADED_THRESHOLD_NANOS + 20L * $$1 && this.nextTickTimeNanos - this.lastOverloadWarningNanos >= OVERLOADED_WARNING_INTERVAL_NANOS + 100L * $$1) {
                        long $$3 = $$2 / $$1;
                        LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", (Object)($$2 / TimeUtil.NANOSECONDS_PER_MILLISECOND), (Object)$$3);
                        this.nextTickTimeNanos += $$3 * $$1;
                        this.lastOverloadWarningNanos = this.nextTickTimeNanos;
                    }
                }
                boolean bl = $$4 = $$1 == 0L;
                if (this.debugCommandProfilerDelayStart) {
                    this.debugCommandProfilerDelayStart = false;
                    this.debugCommandProfiler = new TimeProfiler(Util.getNanos(), this.tickCount);
                }
                this.nextTickTimeNanos += $$1;
                try (Profiler.Scope $$5 = Profiler.use(this.createProfiler());){
                    this.processPacketsAndTick($$4);
                    ProfilerFiller $$6 = Profiler.get();
                    $$6.push("nextTickWait");
                    this.mayHaveDelayedTasks = true;
                    this.delayedTasksMaxNextTickTimeNanos = Math.max(Util.getNanos() + $$1, this.nextTickTimeNanos);
                    this.startMeasuringTaskExecutionTime();
                    this.waitUntilNextTick();
                    this.finishMeasuringTaskExecutionTime();
                    if ($$4) {
                        this.tickRateManager.endTickWork();
                    }
                    $$6.pop();
                    this.logFullTickTime();
                }
                finally {
                    this.endMetricsRecordingTick();
                }
                this.isReady = true;
                JvmProfiler.INSTANCE.onServerTick(this.smoothedTickTimeMillis);
            }
            return;
        }
        catch (Throwable $$8) {
            LOGGER.error("Encountered an unexpected exception", $$8);
            CrashReport $$9 = MinecraftServer.constructOrExtractCrashReport($$8);
            this.fillSystemReport($$9.getSystemReport());
            Path $$10 = this.getServerDirectory().resolve("crash-reports").resolve("crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
            if ($$9.saveToFile($$10, ReportType.CRASH)) {
                LOGGER.error("This crash report has been saved to: {}", (Object)$$10.toAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }
            this.onServerCrash($$9);
            return;
        }
        finally {
            try {
                this.stopped = true;
                this.stopServer();
            }
            catch (Throwable $$7) {
                LOGGER.error("Exception stopping the server", $$7);
            }
            finally {
                this.onServerExit();
            }
        }
    }

    private void logFullTickTime() {
        long $$0 = Util.getNanos();
        if (this.isTickTimeLoggingEnabled()) {
            this.getTickTimeLogger().logSample($$0 - this.lastTickNanos);
        }
        this.lastTickNanos = $$0;
    }

    private void startMeasuringTaskExecutionTime() {
        if (this.isTickTimeLoggingEnabled()) {
            this.taskExecutionStartNanos = Util.getNanos();
            this.idleTimeNanos = 0L;
        }
    }

    private void finishMeasuringTaskExecutionTime() {
        if (this.isTickTimeLoggingEnabled()) {
            SampleLogger $$0 = this.getTickTimeLogger();
            $$0.logPartialSample(Util.getNanos() - this.taskExecutionStartNanos - this.idleTimeNanos, TpsDebugDimensions.SCHEDULED_TASKS.ordinal());
            $$0.logPartialSample(this.idleTimeNanos, TpsDebugDimensions.IDLE.ordinal());
        }
    }

    private static CrashReport constructOrExtractCrashReport(Throwable $$0) {
        CrashReport $$5;
        ReportedException $$1 = null;
        for (Throwable $$2 = $$0; $$2 != null; $$2 = $$2.getCause()) {
            ReportedException $$3;
            if (!($$2 instanceof ReportedException)) continue;
            $$1 = $$3 = (ReportedException)$$2;
        }
        if ($$1 != null) {
            CrashReport $$4 = $$1.getReport();
            if ($$1 != $$0) {
                $$4.addCategory("Wrapped in").setDetailError("Wrapping exception", $$0);
            }
        } else {
            $$5 = new CrashReport("Exception in server tick loop", $$0);
        }
        return $$5;
    }

    private boolean haveTime() {
        return this.runningTask() || Util.getNanos() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTimeNanos : this.nextTickTimeNanos);
    }

    public static boolean throwIfFatalException() {
        RuntimeException $$0 = fatalException.get();
        if ($$0 != null) {
            throw $$0;
        }
        return true;
    }

    public static void setFatalException(RuntimeException $$0) {
        fatalException.compareAndSet(null, $$0);
    }

    @Override
    public void managedBlock(BooleanSupplier $$0) {
        super.managedBlock(() -> MinecraftServer.throwIfFatalException() && $$0.getAsBoolean());
    }

    public NotificationManager notificationManager() {
        return this.notificationManager;
    }

    protected void waitUntilNextTick() {
        this.runAllTasks();
        this.waitingForNextTick = true;
        try {
            this.managedBlock(() -> !this.haveTime());
        }
        finally {
            this.waitingForNextTick = false;
        }
    }

    @Override
    public void waitForTasks() {
        boolean $$0 = this.isTickTimeLoggingEnabled();
        long $$1 = $$0 ? Util.getNanos() : 0L;
        long $$2 = this.waitingForNextTick ? this.nextTickTimeNanos - Util.getNanos() : 100000L;
        LockSupport.parkNanos("waiting for tasks", $$2);
        if ($$0) {
            this.idleTimeNanos += Util.getNanos() - $$1;
        }
    }

    @Override
    public TickTask wrapRunnable(Runnable $$0) {
        return new TickTask(this.tickCount, $$0);
    }

    @Override
    protected boolean shouldRun(TickTask $$0) {
        return $$0.getTick() + 3 < this.tickCount || this.haveTime();
    }

    @Override
    public boolean pollTask() {
        boolean $$0;
        this.mayHaveDelayedTasks = $$0 = this.pollTaskInternal();
        return $$0;
    }

    private boolean pollTaskInternal() {
        if (super.pollTask()) {
            return true;
        }
        if (this.tickRateManager.isSprinting() || this.shouldRunAllTasks() || this.haveTime()) {
            for (ServerLevel $$0 : this.getAllLevels()) {
                if (!$$0.getChunkSource().pollTask()) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doRunTask(TickTask $$0) {
        Profiler.get().incrementCounter("runTask");
        super.doRunTask($$0);
    }

    private Optional<ServerStatus.Favicon> loadStatusIcon() {
        Optional<Path> $$02 = Optional.of(this.getFile("server-icon.png")).filter($$0 -> Files.isRegularFile($$0, new LinkOption[0])).or(() -> this.storageSource.getIconFile().filter($$0 -> Files.isRegularFile($$0, new LinkOption[0])));
        return $$02.flatMap($$0 -> {
            try {
                byte[] $$1 = Files.readAllBytes($$0);
                PngInfo $$2 = PngInfo.fromBytes($$1);
                if ($$2.width() != 64 || $$2.height() != 64) {
                    throw new IllegalArgumentException("Invalid world icon size [" + $$2.width() + ", " + $$2.height() + "], but expected [64, 64]");
                }
                return Optional.of(new ServerStatus.Favicon($$1));
            }
            catch (Exception $$3) {
                LOGGER.error("Couldn't load server icon", (Throwable)$$3);
                return Optional.empty();
            }
        });
    }

    public Optional<Path> getWorldScreenshotFile() {
        return this.storageSource.getIconFile();
    }

    public Path getServerDirectory() {
        return Path.of("", new String[0]);
    }

    public ServerActivityMonitor getServerActivityMonitor() {
        return this.serverActivityMonitor;
    }

    public void onServerCrash(CrashReport $$0) {
    }

    public void onServerExit() {
    }

    public boolean isPaused() {
        return false;
    }

    public void tickServer(BooleanSupplier $$0) {
        long $$1 = Util.getNanos();
        int $$2 = this.pauseWhenEmptySeconds() * 20;
        if ($$2 > 0) {
            this.emptyTicks = this.playerList.getPlayerCount() == 0 && !this.tickRateManager.isSprinting() ? ++this.emptyTicks : 0;
            if (this.emptyTicks >= $$2) {
                if (this.emptyTicks == $$2) {
                    LOGGER.info("Server empty for {} seconds, pausing", (Object)this.pauseWhenEmptySeconds());
                    this.autoSave();
                }
                this.tickConnection();
                return;
            }
        }
        ++this.tickCount;
        this.tickRateManager.tick();
        this.tickChildren($$0);
        if ($$1 - this.lastServerStatus >= STATUS_EXPIRE_TIME_NANOS) {
            this.lastServerStatus = $$1;
            this.status = this.buildServerStatus();
        }
        --this.ticksUntilAutosave;
        if (this.ticksUntilAutosave <= 0) {
            this.autoSave();
        }
        ProfilerFiller $$3 = Profiler.get();
        $$3.push("tallying");
        long $$4 = Util.getNanos() - $$1;
        int $$5 = this.tickCount % 100;
        this.aggregatedTickTimesNanos -= this.tickTimesNanos[$$5];
        this.aggregatedTickTimesNanos += $$4;
        this.tickTimesNanos[$$5] = $$4;
        this.smoothedTickTimeMillis = this.smoothedTickTimeMillis * 0.8f + (float)$$4 / (float)TimeUtil.NANOSECONDS_PER_MILLISECOND * 0.19999999f;
        this.logTickMethodTime($$1);
        $$3.pop();
    }

    protected void processPacketsAndTick(boolean $$0) {
        ProfilerFiller $$1 = Profiler.get();
        $$1.push("tick");
        this.tickFrame.start();
        $$1.push("scheduledPacketProcessing");
        this.packetProcessor.processQueuedPackets();
        $$1.pop();
        this.tickServer($$0 ? () -> false : this::haveTime);
        this.tickFrame.end();
        $$1.pop();
    }

    private void autoSave() {
        this.ticksUntilAutosave = this.computeNextAutosaveInterval();
        LOGGER.debug("Autosave started");
        ProfilerFiller $$0 = Profiler.get();
        $$0.push("save");
        this.saveEverything(true, false, false);
        $$0.pop();
        LOGGER.debug("Autosave finished");
    }

    private void logTickMethodTime(long $$0) {
        if (this.isTickTimeLoggingEnabled()) {
            this.getTickTimeLogger().logPartialSample(Util.getNanos() - $$0, TpsDebugDimensions.TICK_SERVER_METHOD.ordinal());
        }
    }

    private int computeNextAutosaveInterval() {
        float $$2;
        if (this.tickRateManager.isSprinting()) {
            long $$0 = this.getAverageTickTimeNanos() + 1L;
            float $$1 = (float)TimeUtil.NANOSECONDS_PER_SECOND / (float)$$0;
        } else {
            $$2 = this.tickRateManager.tickrate();
        }
        int $$3 = 300;
        return Math.max(100, (int)($$2 * 300.0f));
    }

    public void onTickRateChanged() {
        int $$0 = this.computeNextAutosaveInterval();
        if ($$0 < this.ticksUntilAutosave) {
            this.ticksUntilAutosave = $$0;
        }
    }

    protected abstract SampleLogger getTickTimeLogger();

    public abstract boolean isTickTimeLoggingEnabled();

    private ServerStatus buildServerStatus() {
        ServerStatus.Players $$0 = this.buildPlayerStatus();
        return new ServerStatus(Component.nullToEmpty(this.getMotd()), Optional.of($$0), Optional.of(ServerStatus.Version.current()), Optional.ofNullable(this.statusIcon), this.enforceSecureProfile());
    }

    private ServerStatus.Players buildPlayerStatus() {
        List<ServerPlayer> $$0 = this.playerList.getPlayers();
        int $$1 = this.getMaxPlayers();
        if (this.hidesOnlinePlayers()) {
            return new ServerStatus.Players($$1, $$0.size(), List.of());
        }
        int $$2 = Math.min($$0.size(), 12);
        ObjectArrayList $$3 = new ObjectArrayList($$2);
        int $$4 = Mth.nextInt(this.random, 0, $$0.size() - $$2);
        for (int $$5 = 0; $$5 < $$2; ++$$5) {
            ServerPlayer $$6 = $$0.get($$4 + $$5);
            $$3.add((Object)($$6.allowsListing() ? $$6.nameAndId() : ANONYMOUS_PLAYER_PROFILE));
        }
        Util.shuffle($$3, this.random);
        return new ServerStatus.Players($$1, $$0.size(), (List<NameAndId>)$$3);
    }

    protected void tickChildren(BooleanSupplier $$02) {
        ProfilerFiller $$1 = Profiler.get();
        this.getPlayerList().getPlayers().forEach($$0 -> $$0.connection.suspendFlushing());
        $$1.push("commandFunctions");
        this.getFunctions().tick();
        $$1.popPush("levels");
        this.updateEffectiveRespawnData();
        for (ServerLevel $$2 : this.getAllLevels()) {
            $$1.push(() -> String.valueOf($$2) + " " + String.valueOf($$2.dimension().identifier()));
            if (this.tickCount % 20 == 0) {
                $$1.push("timeSync");
                this.synchronizeTime($$2);
                $$1.pop();
            }
            $$1.push("tick");
            try {
                $$2.tick($$02);
            }
            catch (Throwable $$3) {
                CrashReport $$4 = CrashReport.forThrowable($$3, "Exception ticking world");
                $$2.fillReportDetails($$4);
                throw new ReportedException($$4);
            }
            $$1.pop();
            $$1.pop();
        }
        $$1.popPush("connection");
        this.tickConnection();
        $$1.popPush("players");
        this.playerList.tick();
        $$1.popPush("debugSubscribers");
        this.debugSubscribers.tick();
        if (this.tickRateManager.runsNormally()) {
            $$1.popPush("gameTests");
            GameTestTicker.SINGLETON.tick();
        }
        $$1.popPush("server gui refresh");
        for (Runnable $$5 : this.tickables) {
            $$5.run();
        }
        $$1.popPush("send chunks");
        for (ServerPlayer $$6 : this.playerList.getPlayers()) {
            $$6.connection.chunkSender.sendNextChunks($$6);
            $$6.connection.resumeFlushing();
        }
        $$1.pop();
        this.serverActivityMonitor.tick();
    }

    private void updateEffectiveRespawnData() {
        LevelData.RespawnData $$0 = this.worldData.overworldData().getRespawnData();
        ServerLevel $$1 = this.findRespawnDimension();
        this.effectiveRespawnData = $$1.getWorldBorderAdjustedRespawnData($$0);
    }

    public void tickConnection() {
        this.getConnection().tick();
    }

    private void synchronizeTime(ServerLevel $$0) {
        this.playerList.broadcastAll(new ClientboundSetTimePacket($$0.getGameTime(), $$0.getDayTime(), $$0.getGameRules().get(GameRules.ADVANCE_TIME)), $$0.dimension());
    }

    public void forceTimeSynchronization() {
        ProfilerFiller $$0 = Profiler.get();
        $$0.push("timeSync");
        for (ServerLevel $$1 : this.getAllLevels()) {
            this.synchronizeTime($$1);
        }
        $$0.pop();
    }

    public void addTickable(Runnable $$0) {
        this.tickables.add($$0);
    }

    protected void setId(String $$0) {
        this.serverId = $$0;
    }

    public boolean isShutdown() {
        return !this.serverThread.isAlive();
    }

    public Path getFile(String $$0) {
        return this.getServerDirectory().resolve($$0);
    }

    public final ServerLevel overworld() {
        return this.levels.get(Level.OVERWORLD);
    }

    public @Nullable ServerLevel getLevel(ResourceKey<Level> $$0) {
        return this.levels.get($$0);
    }

    public Set<ResourceKey<Level>> levelKeys() {
        return this.levels.keySet();
    }

    public Iterable<ServerLevel> getAllLevels() {
        return this.levels.values();
    }

    @Override
    public String getServerVersion() {
        return SharedConstants.getCurrentVersion().name();
    }

    @Override
    public int getPlayerCount() {
        return this.playerList.getPlayerCount();
    }

    public String[] getPlayerNames() {
        return this.playerList.getPlayerNamesArray();
    }

    @DontObfuscate
    public String getServerModName() {
        return VANILLA_BRAND;
    }

    public SystemReport fillSystemReport(SystemReport $$0) {
        $$0.setDetail("Server Running", () -> Boolean.toString(this.running));
        if (this.playerList != null) {
            $$0.setDetail("Player Count", () -> this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + String.valueOf(this.playerList.getPlayers()));
        }
        $$0.setDetail("Active Data Packs", () -> PackRepository.displayPackList(this.packRepository.getSelectedPacks()));
        $$0.setDetail("Available Data Packs", () -> PackRepository.displayPackList(this.packRepository.getAvailablePacks()));
        $$0.setDetail("Enabled Feature Flags", () -> FeatureFlags.REGISTRY.toNames(this.worldData.enabledFeatures()).stream().map(Identifier::toString).collect(Collectors.joining(", ")));
        $$0.setDetail("World Generation", () -> this.worldData.worldGenSettingsLifecycle().toString());
        $$0.setDetail("World Seed", () -> String.valueOf(this.worldData.worldGenOptions().seed()));
        $$0.setDetail("Suppressed Exceptions", this.suppressedExceptions::dump);
        if (this.serverId != null) {
            $$0.setDetail("Server Id", () -> this.serverId);
        }
        return this.fillServerSystemReport($$0);
    }

    public abstract SystemReport fillServerSystemReport(SystemReport var1);

    public ModCheck getModdedStatus() {
        return ModCheck.identify(VANILLA_BRAND, this::getServerModName, "Server", MinecraftServer.class);
    }

    @Override
    public void sendSystemMessage(Component $$0) {
        LOGGER.info($$0.getString());
    }

    public KeyPair getKeyPair() {
        return Objects.requireNonNull(this.keyPair);
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int $$0) {
        this.port = $$0;
    }

    public @Nullable GameProfile getSingleplayerProfile() {
        return this.singleplayerProfile;
    }

    public void setSingleplayerProfile(@Nullable GameProfile $$0) {
        this.singleplayerProfile = $$0;
    }

    public boolean isSingleplayer() {
        return this.singleplayerProfile != null;
    }

    protected void initializeKeyPair() {
        LOGGER.info("Generating keypair");
        try {
            this.keyPair = Crypt.generateKeyPair();
        }
        catch (CryptException $$0) {
            throw new IllegalStateException("Failed to generate key pair", $$0);
        }
    }

    public void setDifficulty(Difficulty $$0, boolean $$1) {
        if (!$$1 && this.worldData.isDifficultyLocked()) {
            return;
        }
        this.worldData.setDifficulty(this.worldData.isHardcore() ? Difficulty.HARD : $$0);
        this.updateMobSpawningFlags();
        this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
    }

    public int getScaledTrackingDistance(int $$0) {
        return $$0;
    }

    public void updateMobSpawningFlags() {
        for (ServerLevel $$0 : this.getAllLevels()) {
            $$0.setSpawnSettings($$0.isSpawningMonsters());
        }
    }

    public void setDifficultyLocked(boolean $$0) {
        this.worldData.setDifficultyLocked($$0);
        this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
    }

    private void sendDifficultyUpdate(ServerPlayer $$0) {
        LevelData $$1 = $$0.level().getLevelData();
        $$0.connection.send(new ClientboundChangeDifficultyPacket($$1.getDifficulty(), $$1.isDifficultyLocked()));
    }

    public boolean isDemo() {
        return this.isDemo;
    }

    public void setDemo(boolean $$0) {
        this.isDemo = $$0;
    }

    public Map<String, String> getCodeOfConducts() {
        return Map.of();
    }

    public Optional<ServerResourcePackInfo> getServerResourcePack() {
        return Optional.empty();
    }

    public boolean isResourcePackRequired() {
        return this.getServerResourcePack().filter(ServerResourcePackInfo::isRequired).isPresent();
    }

    public abstract boolean isDedicatedServer();

    public abstract int getRateLimitPacketsPerSecond();

    public boolean usesAuthentication() {
        return this.onlineMode;
    }

    public void setUsesAuthentication(boolean $$0) {
        this.onlineMode = $$0;
    }

    public boolean getPreventProxyConnections() {
        return this.preventProxyConnections;
    }

    public void setPreventProxyConnections(boolean $$0) {
        this.preventProxyConnections = $$0;
    }

    public abstract boolean useNativeTransport();

    public boolean allowFlight() {
        return true;
    }

    @Override
    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String $$0) {
        this.motd = $$0;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    public void setPlayerList(PlayerList $$0) {
        this.playerList = $$0;
    }

    public abstract boolean isPublished();

    public void setDefaultGameType(GameType $$0) {
        this.worldData.setGameType($$0);
    }

    public int enforceGameTypeForPlayers(@Nullable GameType $$0) {
        if ($$0 == null) {
            return 0;
        }
        int $$1 = 0;
        for (ServerPlayer $$2 : this.getPlayerList().getPlayers()) {
            if (!$$2.setGameMode($$0)) continue;
            ++$$1;
        }
        return $$1;
    }

    public ServerConnectionListener getConnection() {
        return this.connection;
    }

    public boolean isReady() {
        return this.isReady;
    }

    public boolean publishServer(@Nullable GameType $$0, boolean $$1, int $$2) {
        return false;
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public boolean isUnderSpawnProtection(ServerLevel $$0, BlockPos $$1, Player $$2) {
        return false;
    }

    public boolean repliesToStatus() {
        return true;
    }

    public boolean hidesOnlinePlayers() {
        return false;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public int playerIdleTimeout() {
        return this.playerIdleTimeout;
    }

    public void setPlayerIdleTimeout(int $$0) {
        this.playerIdleTimeout = $$0;
    }

    public Services services() {
        return this.services;
    }

    public @Nullable ServerStatus getStatus() {
        return this.status;
    }

    public void invalidateStatus() {
        this.lastServerStatus = 0L;
    }

    public int getAbsoluteMaxWorldSize() {
        return 29999984;
    }

    @Override
    public boolean scheduleExecutables() {
        return super.scheduleExecutables() && !this.isStopped();
    }

    @Override
    public void executeIfPossible(Runnable $$0) {
        if (this.isStopped()) {
            throw new RejectedExecutionException("Server already shutting down");
        }
        super.executeIfPossible($$0);
    }

    @Override
    public Thread getRunningThread() {
        return this.serverThread;
    }

    public int getCompressionThreshold() {
        return 256;
    }

    public boolean enforceSecureProfile() {
        return false;
    }

    public long getNextTickTime() {
        return this.nextTickTimeNanos;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.resources.managers.getAdvancements();
    }

    public ServerFunctionManager getFunctions() {
        return this.functionManager;
    }

    public CompletableFuture<Void> reloadResources(Collection<String> $$02) {
        CompletionStage $$12 = ((CompletableFuture)CompletableFuture.supplyAsync(() -> (ImmutableList)$$02.stream().map(this.packRepository::getPack).filter(Objects::nonNull).map(Pack::open).collect(ImmutableList.toImmutableList()), this).thenCompose($$0 -> {
            MultiPackResourceManager $$12 = new MultiPackResourceManager(PackType.SERVER_DATA, (List<PackResources>)$$0);
            List<Registry.PendingTags<?>> $$22 = TagLoader.loadTagsForExistingRegistries($$12, this.registries.compositeAccess());
            return ((CompletableFuture)ReloadableServerResources.loadResources($$12, this.registries, $$22, this.worldData.enabledFeatures(), this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED, this.getFunctionCompilationPermissions(), this.executor, this).whenComplete(($$1, $$2) -> {
                if ($$2 != null) {
                    $$12.close();
                }
            })).thenApply($$1 -> new ReloadableResources($$12, (ReloadableServerResources)$$1));
        })).thenAcceptAsync($$1 -> {
            this.resources.close();
            this.resources = $$1;
            this.packRepository.setSelected($$02);
            WorldDataConfiguration $$2 = new WorldDataConfiguration(MinecraftServer.getSelectedPacks(this.packRepository, true), this.worldData.enabledFeatures());
            this.worldData.setDataConfiguration($$2);
            this.resources.managers.updateStaticRegistryTags();
            this.resources.managers.getRecipeManager().finalizeRecipeLoading(this.worldData.enabledFeatures());
            this.getPlayerList().saveAll();
            this.getPlayerList().reloadResources();
            this.functionManager.replaceLibrary(this.resources.managers.getFunctionLibrary());
            this.structureTemplateManager.onResourceManagerReload(this.resources.resourceManager);
            this.fuelValues = FuelValues.vanillaBurnTimes(this.registries.compositeAccess(), this.worldData.enabledFeatures());
        }, (Executor)this);
        if (this.isSameThread()) {
            this.managedBlock(((CompletableFuture)$$12)::isDone);
        }
        return $$12;
    }

    public static WorldDataConfiguration configurePackRepository(PackRepository $$0, WorldDataConfiguration $$1, boolean $$2, boolean $$3) {
        DataPackConfig $$4 = $$1.dataPacks();
        FeatureFlagSet $$5 = $$2 ? FeatureFlagSet.of() : $$1.enabledFeatures();
        FeatureFlagSet $$6 = $$2 ? FeatureFlags.REGISTRY.allFlags() : $$1.enabledFeatures();
        $$0.reload();
        if ($$3) {
            return MinecraftServer.configureRepositoryWithSelection($$0, List.of(VANILLA_BRAND), $$5, false);
        }
        LinkedHashSet $$7 = Sets.newLinkedHashSet();
        for (String $$8 : $$4.getEnabled()) {
            if ($$0.isAvailable($$8)) {
                $$7.add($$8);
                continue;
            }
            LOGGER.warn("Missing data pack {}", (Object)$$8);
        }
        for (Pack $$9 : $$0.getAvailablePacks()) {
            String $$10 = $$9.getId();
            if ($$4.getDisabled().contains($$10)) continue;
            FeatureFlagSet $$11 = $$9.getRequestedFeatures();
            boolean $$12 = $$7.contains($$10);
            if (!$$12 && $$9.getPackSource().shouldAddAutomatically()) {
                if ($$11.isSubsetOf($$6)) {
                    LOGGER.info("Found new data pack {}, loading it automatically", (Object)$$10);
                    $$7.add($$10);
                } else {
                    LOGGER.info("Found new data pack {}, but can't load it due to missing features {}", (Object)$$10, (Object)FeatureFlags.printMissingFlags($$6, $$11));
                }
            }
            if (!$$12 || $$11.isSubsetOf($$6)) continue;
            LOGGER.warn("Pack {} requires features {} that are not enabled for this world, disabling pack.", (Object)$$10, (Object)FeatureFlags.printMissingFlags($$6, $$11));
            $$7.remove($$10);
        }
        if ($$7.isEmpty()) {
            LOGGER.info("No datapacks selected, forcing vanilla");
            $$7.add(VANILLA_BRAND);
        }
        return MinecraftServer.configureRepositoryWithSelection($$0, $$7, $$5, true);
    }

    private static WorldDataConfiguration configureRepositoryWithSelection(PackRepository $$0, Collection<String> $$1, FeatureFlagSet $$2, boolean $$3) {
        $$0.setSelected($$1);
        MinecraftServer.enableForcedFeaturePacks($$0, $$2);
        DataPackConfig $$4 = MinecraftServer.getSelectedPacks($$0, $$3);
        FeatureFlagSet $$5 = $$0.getRequestedFeatureFlags().join($$2);
        return new WorldDataConfiguration($$4, $$5);
    }

    private static void enableForcedFeaturePacks(PackRepository $$0, FeatureFlagSet $$1) {
        FeatureFlagSet $$2 = $$0.getRequestedFeatureFlags();
        FeatureFlagSet $$3 = $$1.subtract($$2);
        if ($$3.isEmpty()) {
            return;
        }
        ObjectArraySet $$4 = new ObjectArraySet($$0.getSelectedIds());
        for (Pack $$5 : $$0.getAvailablePacks()) {
            if ($$3.isEmpty()) break;
            if ($$5.getPackSource() != PackSource.FEATURE) continue;
            String $$6 = $$5.getId();
            FeatureFlagSet $$7 = $$5.getRequestedFeatures();
            if ($$7.isEmpty() || !$$7.intersects($$3) || !$$7.isSubsetOf($$1)) continue;
            if (!$$4.add($$6)) {
                throw new IllegalStateException("Tried to force '" + $$6 + "', but it was already enabled");
            }
            LOGGER.info("Found feature pack ('{}') for requested feature, forcing to enabled", (Object)$$6);
            $$3 = $$3.subtract($$7);
        }
        $$0.setSelected((Collection<String>)$$4);
    }

    private static DataPackConfig getSelectedPacks(PackRepository $$0, boolean $$12) {
        Collection<String> $$2 = $$0.getSelectedIds();
        ImmutableList $$3 = ImmutableList.copyOf($$2);
        List<String> $$4 = $$12 ? $$0.getAvailableIds().stream().filter($$1 -> !$$2.contains($$1)).toList() : List.of();
        return new DataPackConfig((List<String>)$$3, $$4);
    }

    public void kickUnlistedPlayers() {
        if (!this.isEnforceWhitelist() || !this.isUsingWhitelist()) {
            return;
        }
        PlayerList $$0 = this.getPlayerList();
        UserWhiteList $$1 = $$0.getWhiteList();
        ArrayList $$2 = Lists.newArrayList($$0.getPlayers());
        for (ServerPlayer $$3 : $$2) {
            if ($$1.isWhiteListed($$3.nameAndId())) continue;
            $$3.connection.disconnect(Component.translatable("multiplayer.disconnect.not_whitelisted"));
        }
    }

    public PackRepository getPackRepository() {
        return this.packRepository;
    }

    public Commands getCommands() {
        return this.resources.managers.getCommands();
    }

    public CommandSourceStack createCommandSourceStack() {
        ServerLevel $$0 = this.findRespawnDimension();
        return new CommandSourceStack(this, Vec3.atLowerCornerOf(this.getRespawnData().pos()), Vec2.ZERO, $$0, LevelBasedPermissionSet.OWNER, "Server", Component.literal("Server"), this, null);
    }

    public ServerLevel findRespawnDimension() {
        LevelData.RespawnData $$0 = this.getWorldData().overworldData().getRespawnData();
        ResourceKey<Level> $$1 = $$0.dimension();
        ServerLevel $$2 = this.getLevel($$1);
        return $$2 != null ? $$2 : this.overworld();
    }

    public void setRespawnData(LevelData.RespawnData $$0) {
        ServerLevelData $$1 = this.worldData.overworldData();
        LevelData.RespawnData $$2 = $$1.getRespawnData();
        if (!$$2.equals($$0)) {
            $$1.setSpawn($$0);
            this.getPlayerList().broadcastAll(new ClientboundSetDefaultSpawnPositionPacket($$0));
            this.updateEffectiveRespawnData();
        }
    }

    public LevelData.RespawnData getRespawnData() {
        return this.effectiveRespawnData;
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public abstract boolean shouldInformAdmins();

    public RecipeManager getRecipeManager() {
        return this.resources.managers.getRecipeManager();
    }

    public ServerScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public CommandStorage getCommandStorage() {
        if (this.commandStorage == null) {
            throw new NullPointerException("Called before server init");
        }
        return this.commandStorage;
    }

    public Stopwatches getStopwatches() {
        if (this.stopwatches == null) {
            throw new NullPointerException("Called before server init");
        }
        return this.stopwatches;
    }

    public CustomBossEvents getCustomBossEvents() {
        return this.customBossEvents;
    }

    public boolean isEnforceWhitelist() {
        return this.enforceWhitelist;
    }

    public void setEnforceWhitelist(boolean $$0) {
        this.enforceWhitelist = $$0;
    }

    public boolean isUsingWhitelist() {
        return this.usingWhitelist;
    }

    public void setUsingWhitelist(boolean $$0) {
        this.usingWhitelist = $$0;
    }

    public float getCurrentSmoothedTickTime() {
        return this.smoothedTickTimeMillis;
    }

    public ServerTickRateManager tickRateManager() {
        return this.tickRateManager;
    }

    public long getAverageTickTimeNanos() {
        return this.aggregatedTickTimesNanos / (long)Math.min(100, Math.max(this.tickCount, 1));
    }

    public long[] getTickTimesNanos() {
        return this.tickTimesNanos;
    }

    public LevelBasedPermissionSet getProfilePermissions(NameAndId $$0) {
        if (this.getPlayerList().isOp($$0)) {
            ServerOpListEntry $$1 = (ServerOpListEntry)this.getPlayerList().getOps().get($$0);
            if ($$1 != null) {
                return $$1.permissions();
            }
            if (this.isSingleplayerOwner($$0)) {
                return LevelBasedPermissionSet.OWNER;
            }
            if (this.isSingleplayer()) {
                return this.getPlayerList().isAllowCommandsForAllPlayers() ? LevelBasedPermissionSet.OWNER : LevelBasedPermissionSet.ALL;
            }
            return this.operatorUserPermissions();
        }
        return LevelBasedPermissionSet.ALL;
    }

    public abstract boolean isSingleplayerOwner(NameAndId var1);

    public void dumpServerProperties(Path $$0) throws IOException {
    }

    private void saveDebugReport(Path $$0) {
        Path $$1 = $$0.resolve("levels");
        try {
            for (Map.Entry<ResourceKey<Level>, ServerLevel> $$2 : this.levels.entrySet()) {
                Identifier $$3 = $$2.getKey().identifier();
                Path $$4 = $$1.resolve($$3.getNamespace()).resolve($$3.getPath());
                Files.createDirectories($$4, new FileAttribute[0]);
                $$2.getValue().saveDebugReport($$4);
            }
            this.dumpGameRules($$0.resolve("gamerules.txt"));
            this.dumpClasspath($$0.resolve("classpath.txt"));
            this.dumpMiscStats($$0.resolve("stats.txt"));
            this.dumpThreads($$0.resolve("threads.txt"));
            this.dumpServerProperties($$0.resolve("server.properties.txt"));
            this.dumpNativeModules($$0.resolve("modules.txt"));
        }
        catch (IOException $$5) {
            LOGGER.warn("Failed to save debug report", (Throwable)$$5);
        }
    }

    private void dumpMiscStats(Path $$0) throws IOException {
        try (BufferedWriter $$1 = Files.newBufferedWriter($$0, new OpenOption[0]);){
            $$1.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getPendingTasksCount()));
            $$1.write(String.format(Locale.ROOT, "average_tick_time: %f\n", Float.valueOf(this.getCurrentSmoothedTickTime())));
            $$1.write(String.format(Locale.ROOT, "tick_times: %s\n", Arrays.toString(this.tickTimesNanos)));
            $$1.write(String.format(Locale.ROOT, "queue: %s\n", Util.backgroundExecutor()));
        }
    }

    private void dumpGameRules(Path $$0) throws IOException {
        try (BufferedWriter $$1 = Files.newBufferedWriter($$0, new OpenOption[0]);){
            final ArrayList $$2 = Lists.newArrayList();
            final GameRules $$3 = this.worldData.getGameRules();
            $$3.visitGameRuleTypes(new GameRuleTypeVisitor(){

                @Override
                public <T> void visit(GameRule<T> $$0) {
                    $$2.add(String.format(Locale.ROOT, "%s=%s\n", $$0.getIdentifier(), $$3.getAsString($$0)));
                }
            });
            for (String $$4 : $$2) {
                $$1.write($$4);
            }
        }
    }

    private void dumpClasspath(Path $$0) throws IOException {
        try (BufferedWriter $$1 = Files.newBufferedWriter($$0, new OpenOption[0]);){
            String $$2 = System.getProperty("java.class.path");
            String $$3 = File.pathSeparator;
            for (String $$4 : Splitter.on((String)$$3).split((CharSequence)$$2)) {
                $$1.write($$4);
                $$1.write("\n");
            }
        }
    }

    private void dumpThreads(Path $$0) throws IOException {
        ThreadMXBean $$1 = ManagementFactory.getThreadMXBean();
        ThreadInfo[] $$2 = $$1.dumpAllThreads(true, true);
        Arrays.sort($$2, Comparator.comparing(ThreadInfo::getThreadName));
        try (BufferedWriter $$3 = Files.newBufferedWriter($$0, new OpenOption[0]);){
            for (ThreadInfo $$4 : $$2) {
                $$3.write($$4.toString());
                ((Writer)$$3).write(10);
            }
        }
    }

    /*
     * WARNING - void declaration
     */
    private void dumpNativeModules(Path $$02) throws IOException {
        BufferedWriter $$1 = Files.newBufferedWriter($$02, new OpenOption[0]);
        try {
            void $$4;
            try {
                ArrayList $$2 = Lists.newArrayList(NativeModuleLister.listModules());
            }
            catch (Throwable $$3) {
                LOGGER.warn("Failed to list native modules", $$3);
                if ($$1 != null) {
                    ((Writer)$$1).close();
                }
                return;
            }
            $$4.sort(Comparator.comparing($$0 -> $$0.name));
            for (NativeModuleLister.NativeModuleInfo $$5 : $$4) {
                $$1.write($$5.toString());
                ((Writer)$$1).write(10);
            }
        }
        finally {
            if ($$1 != null) {
                try {
                    ((Writer)$$1).close();
                }
                catch (Throwable throwable) {
                    Throwable throwable2;
                    throwable2.addSuppressed(throwable);
                }
            }
        }
    }

    private ProfilerFiller createProfiler() {
        if (this.willStartRecordingMetrics) {
            this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ServerMetricsSamplersProvider(Util.timeSource, this.isDedicatedServer()), Util.timeSource, Util.ioPool(), new MetricsPersister("server"), this.onMetricsRecordingStopped, $$0 -> {
                this.executeBlocking(() -> this.saveDebugReport($$0.resolve("server")));
                this.onMetricsRecordingFinished.accept((Path)$$0);
            });
            this.willStartRecordingMetrics = false;
        }
        this.metricsRecorder.startTick();
        return SingleTickProfiler.decorateFiller(this.metricsRecorder.getProfiler(), SingleTickProfiler.createTickProfiler("Server"));
    }

    public void endMetricsRecordingTick() {
        this.metricsRecorder.endTick();
    }

    public boolean isRecordingMetrics() {
        return this.metricsRecorder.isRecording();
    }

    public void startRecordingMetrics(Consumer<ProfileResults> $$0, Consumer<Path> $$12) {
        this.onMetricsRecordingStopped = $$1 -> {
            this.stopRecordingMetrics();
            $$0.accept((ProfileResults)$$1);
        };
        this.onMetricsRecordingFinished = $$12;
        this.willStartRecordingMetrics = true;
    }

    public void stopRecordingMetrics() {
        this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    }

    public void finishRecordingMetrics() {
        this.metricsRecorder.end();
    }

    public void cancelRecordingMetrics() {
        this.metricsRecorder.cancel();
    }

    public Path getWorldPath(LevelResource $$0) {
        return this.storageSource.getLevelPath($$0);
    }

    public boolean forceSynchronousWrites() {
        return true;
    }

    public StructureTemplateManager getStructureManager() {
        return this.structureTemplateManager;
    }

    public WorldData getWorldData() {
        return this.worldData;
    }

    public RegistryAccess.Frozen registryAccess() {
        return this.registries.compositeAccess();
    }

    public LayeredRegistryAccess<RegistryLayer> registries() {
        return this.registries;
    }

    public ReloadableServerRegistries.Holder reloadableRegistries() {
        return this.resources.managers.fullRegistries();
    }

    public TextFilter createTextFilterForPlayer(ServerPlayer $$0) {
        return TextFilter.DUMMY;
    }

    public ServerPlayerGameMode createGameModeForPlayer(ServerPlayer $$0) {
        return this.isDemo() ? new DemoMode($$0) : new ServerPlayerGameMode($$0);
    }

    public @Nullable GameType getForcedGameType() {
        return null;
    }

    public ResourceManager getResourceManager() {
        return this.resources.resourceManager;
    }

    public boolean isCurrentlySaving() {
        return this.isSaving;
    }

    public boolean isTimeProfilerRunning() {
        return this.debugCommandProfilerDelayStart || this.debugCommandProfiler != null;
    }

    public void startTimeProfiler() {
        this.debugCommandProfilerDelayStart = true;
    }

    public ProfileResults stopTimeProfiler() {
        if (this.debugCommandProfiler == null) {
            return EmptyProfileResults.EMPTY;
        }
        ProfileResults $$0 = this.debugCommandProfiler.stop(Util.getNanos(), this.tickCount);
        this.debugCommandProfiler = null;
        return $$0;
    }

    public int getMaxChainedNeighborUpdates() {
        return 1000000;
    }

    public void logChatMessage(Component $$0, ChatType.Bound $$1, @Nullable String $$2) {
        String $$3 = $$1.decorate($$0).getString();
        if ($$2 != null) {
            LOGGER.info("[{}] {}", (Object)$$2, (Object)$$3);
        } else {
            LOGGER.info("{}", (Object)$$3);
        }
    }

    public ChatDecorator getChatDecorator() {
        return ChatDecorator.PLAIN;
    }

    public boolean logIPs() {
        return true;
    }

    public void handleCustomClickAction(Identifier $$0, Optional<Tag> $$1) {
        LOGGER.debug("Received custom click action {} with payload {}", (Object)$$0, $$1.orElse(null));
    }

    public LevelLoadListener getLevelLoadListener() {
        return this.levelLoadListener;
    }

    public boolean setAutoSave(boolean $$0) {
        boolean $$1 = false;
        for (ServerLevel $$2 : this.getAllLevels()) {
            if ($$2 == null || $$2.noSave != $$0) continue;
            $$2.noSave = !$$0;
            $$1 = true;
        }
        return $$1;
    }

    public boolean isAutoSave() {
        for (ServerLevel $$0 : this.getAllLevels()) {
            if ($$0 == null || $$0.noSave) continue;
            return true;
        }
        return false;
    }

    public <T> void onGameRuleChanged(GameRule<T> $$0, T $$12) {
        this.notificationManager().onGameRuleChanged($$0, $$12);
        if ($$0 == GameRules.REDUCED_DEBUG_INFO) {
            byte $$2 = (Boolean)$$12 != false ? (byte)22 : 23;
            for (ServerPlayer $$3 : this.getPlayerList().getPlayers()) {
                $$3.connection.send(new ClientboundEntityEventPacket($$3, $$2));
            }
        } else if ($$0 == GameRules.LIMITED_CRAFTING || $$0 == GameRules.IMMEDIATE_RESPAWN) {
            ClientboundGameEventPacket.Type $$4 = $$0 == GameRules.LIMITED_CRAFTING ? ClientboundGameEventPacket.LIMITED_CRAFTING : ClientboundGameEventPacket.IMMEDIATE_RESPAWN;
            ClientboundGameEventPacket $$5 = new ClientboundGameEventPacket($$4, (Boolean)$$12 != false ? 1.0f : 0.0f);
            this.getPlayerList().getPlayers().forEach($$1 -> $$1.connection.send($$5));
        } else if ($$0 == GameRules.LOCATOR_BAR) {
            this.getAllLevels().forEach($$1 -> {
                ServerWaypointManager $$2 = $$1.getWaypointManager();
                if (((Boolean)$$12).booleanValue()) {
                    $$1.players().forEach($$2::updatePlayer);
                } else {
                    $$2.breakAllConnections();
                }
            });
        } else if ($$0 == GameRules.SPAWN_MONSTERS) {
            this.updateMobSpawningFlags();
        }
    }

    public boolean acceptsTransfers() {
        return false;
    }

    private void storeChunkIoError(CrashReport $$0, ChunkPos $$1, RegionStorageInfo $$2) {
        Util.ioPool().execute(() -> {
            try {
                Path $$3 = this.getFile("debug");
                FileUtil.createDirectoriesSafe($$3);
                String $$4 = FileUtil.sanitizeName($$2.level());
                Path $$5 = $$3.resolve("chunk-" + $$4 + "-" + Util.getFilenameFormattedDateTime() + "-server.txt");
                FileStore $$6 = Files.getFileStore($$3);
                long $$7 = $$6.getUsableSpace();
                if ($$7 < 8192L) {
                    LOGGER.warn("Not storing chunk IO report due to low space on drive {}", (Object)$$6.name());
                    return;
                }
                CrashReportCategory $$8 = $$0.addCategory("Chunk Info");
                $$8.setDetail("Level", $$2::level);
                $$8.setDetail("Dimension", () -> $$2.dimension().identifier().toString());
                $$8.setDetail("Storage", $$2::type);
                $$8.setDetail("Position", $$1::toString);
                $$0.saveToFile($$5, ReportType.CHUNK_IO_ERROR);
                LOGGER.info("Saved details to {}", (Object)$$0.getSaveFile());
            }
            catch (Exception $$9) {
                LOGGER.warn("Failed to store chunk IO exception", (Throwable)$$9);
            }
        });
    }

    @Override
    public void reportChunkLoadFailure(Throwable $$0, RegionStorageInfo $$1, ChunkPos $$2) {
        LOGGER.error("Failed to load chunk {},{}", new Object[]{$$2.x, $$2.z, $$0});
        this.suppressedExceptions.addEntry("chunk/load", $$0);
        this.storeChunkIoError(CrashReport.forThrowable($$0, "Chunk load failure"), $$2, $$1);
    }

    @Override
    public void reportChunkSaveFailure(Throwable $$0, RegionStorageInfo $$1, ChunkPos $$2) {
        LOGGER.error("Failed to save chunk {},{}", new Object[]{$$2.x, $$2.z, $$0});
        this.suppressedExceptions.addEntry("chunk/save", $$0);
        this.storeChunkIoError(CrashReport.forThrowable($$0, "Chunk save failure"), $$2, $$1);
    }

    public void reportPacketHandlingException(Throwable $$0, PacketType<?> $$1) {
        this.suppressedExceptions.addEntry("packet/" + String.valueOf($$1), $$0);
    }

    public PotionBrewing potionBrewing() {
        return this.potionBrewing;
    }

    public FuelValues fuelValues() {
        return this.fuelValues;
    }

    public ServerLinks serverLinks() {
        return ServerLinks.EMPTY;
    }

    protected int pauseWhenEmptySeconds() {
        return 0;
    }

    public PacketProcessor packetProcessor() {
        return this.packetProcessor;
    }

    public ServerDebugSubscribers debugSubscribers() {
        return this.debugSubscribers;
    }

    @Override
    public /* synthetic */ void doRunTask(Runnable runnable) {
        this.doRunTask((TickTask)runnable);
    }

    @Override
    public /* synthetic */ boolean shouldRun(Runnable runnable) {
        return this.shouldRun((TickTask)runnable);
    }

    @Override
    public /* synthetic */ Runnable wrapRunnable(Runnable runnable) {
        return this.wrapRunnable(runnable);
    }

    static final class ReloadableResources
    extends Record
    implements AutoCloseable {
        final CloseableResourceManager resourceManager;
        final ReloadableServerResources managers;

        ReloadableResources(CloseableResourceManager $$0, ReloadableServerResources $$1) {
            this.resourceManager = $$0;
            this.managers = $$1;
        }

        @Override
        public void close() {
            this.resourceManager.close();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ReloadableResources.class, "resourceManager;managers", "resourceManager", "managers"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ReloadableResources.class, "resourceManager;managers", "resourceManager", "managers"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ReloadableResources.class, "resourceManager;managers", "resourceManager", "managers"}, this, $$0);
        }

        public CloseableResourceManager resourceManager() {
            return this.resourceManager;
        }

        public ReloadableServerResources managers() {
            return this.managers;
        }
    }

    static class TimeProfiler {
        final long startNanos;
        final int startTick;

        TimeProfiler(long $$0, int $$1) {
            this.startNanos = $$0;
            this.startTick = $$1;
        }

        ProfileResults stop(final long $$0, final int $$1) {
            return new ProfileResults(){

                @Override
                public List<ResultField> getTimes(String $$02) {
                    return Collections.emptyList();
                }

                @Override
                public boolean saveResults(Path $$02) {
                    return false;
                }

                @Override
                public long getStartTimeNano() {
                    return startNanos;
                }

                @Override
                public int getStartTimeTicks() {
                    return startTick;
                }

                @Override
                public long getEndTimeNano() {
                    return $$0;
                }

                @Override
                public int getEndTimeTicks() {
                    return $$1;
                }

                @Override
                public String getProfilerResults() {
                    return "";
                }
            };
        }
    }

    public record ServerResourcePackInfo(UUID id, String url, String hash, boolean isRequired, @Nullable Component prompt) {
    }
}

