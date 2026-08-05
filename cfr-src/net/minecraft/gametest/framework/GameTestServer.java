/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.yggdrasil.ServicesKeySet
 *  com.mojang.brigadier.StringReader
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.brigadier.StringReader;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.minecraft.SystemReport;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceSelectorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchFactory;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.StructureGridSpawner;
import net.minecraft.gizmos.GizmoCollector;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LoggingLevelLoadListener;
import net.minecraft.server.notifications.EmptyNotificationService;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.debugchart.SampleLogger;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class GameTestServer
extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PROGRESS_REPORT_INTERVAL = 20;
    private static final int TEST_POSITION_RANGE = 14999992;
    private static final Services NO_SERVICES = new Services(null, ServicesKeySet.EMPTY, null, new MockUserNameToIdResolver(), new MockProfileResolver());
    private static final FeatureFlagSet ENABLED_FEATURES = FeatureFlags.REGISTRY.allFlags().subtract(FeatureFlagSet.of(FeatureFlags.REDSTONE_EXPERIMENTS, FeatureFlags.MINECART_IMPROVEMENTS));
    private final LocalSampleLogger sampleLogger = new LocalSampleLogger(4);
    private final Optional<String> testSelection;
    private final boolean verify;
    private List<GameTestBatch> testBatches = new ArrayList<GameTestBatch>();
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private static final WorldOptions WORLD_OPTIONS = new WorldOptions(0L, false, false);
    private @Nullable MultipleTestTracker testTracker;

    public static GameTestServer create(Thread $$0, LevelStorageSource.LevelStorageAccess $$1, PackRepository $$22, Optional<String> $$3, boolean $$4) {
        $$22.reload();
        ArrayList<String> $$5 = new ArrayList<String>($$22.getAvailableIds());
        $$5.remove("vanilla");
        $$5.addFirst("vanilla");
        WorldDataConfiguration $$6 = new WorldDataConfiguration(new DataPackConfig($$5, List.of()), ENABLED_FEATURES);
        LevelSettings $$7 = new LevelSettings("Test Level", GameType.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(ENABLED_FEATURES), $$6);
        WorldLoader.PackConfig $$8 = new WorldLoader.PackConfig($$22, $$6, false, true);
        WorldLoader.InitConfig $$9 = new WorldLoader.InitConfig($$8, Commands.CommandSelection.DEDICATED, LevelBasedPermissionSet.OWNER);
        try {
            LOGGER.debug("Starting resource loading");
            Stopwatch $$10 = Stopwatch.createStarted();
            WorldStem $$11 = (WorldStem)Util.blockUntilDone($$2 -> WorldLoader.load($$9, $$1 -> {
                Object $$2 = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, Lifecycle.stable()).freeze();
                WorldDimensions.Complete $$3 = $$1.datapackWorldgen().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions().bake((Registry<LevelStem>)$$2);
                return new WorldLoader.DataLoadOutput<PrimaryLevelData>(new PrimaryLevelData($$7, WORLD_OPTIONS, $$3.specialWorldProperty(), $$3.lifecycle()), $$3.dimensionsRegistryAccess());
            }, WorldStem::new, Util.backgroundExecutor(), $$2)).get();
            $$10.stop();
            LOGGER.debug("Finished resource loading after {} ms", (Object)$$10.elapsed(TimeUnit.MILLISECONDS));
            return new GameTestServer($$0, $$1, $$22, $$11, $$3, $$4);
        }
        catch (Exception $$12) {
            LOGGER.warn("Failed to load vanilla datapack, bit oops", (Throwable)$$12);
            System.exit(-1);
            throw new IllegalStateException();
        }
    }

    private GameTestServer(Thread $$0, LevelStorageSource.LevelStorageAccess $$1, PackRepository $$2, WorldStem $$3, Optional<String> $$4, boolean $$5) {
        super($$0, $$1, $$2, $$3, Proxy.NO_PROXY, DataFixers.getDataFixer(), NO_SERVICES, LoggingLevelLoadListener.forDedicatedServer());
        this.testSelection = $$4;
        this.verify = $$5;
    }

    @Override
    public boolean initServer() {
        this.setPlayerList(new PlayerList(this, this, this.registries(), this.playerDataStorage, new EmptyNotificationService()){});
        Gizmos.withCollector(GizmoCollector.NOOP);
        this.loadLevel();
        ServerLevel $$0 = this.overworld();
        this.testBatches = this.evaluateTestsToRun($$0);
        LOGGER.info("Started game test server");
        return true;
    }

    private List<GameTestBatch> evaluateTestsToRun(ServerLevel $$02) {
        GameTestBatchFactory.TestDecorator $$6;
        List<Holder.Reference<GameTestInstance>> $$5;
        HolderLookup.RegistryLookup $$1 = $$02.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE);
        if (this.testSelection.isPresent()) {
            List<Holder.Reference> $$2 = GameTestServer.getTestsForSelection($$02.registryAccess(), this.testSelection.get()).filter($$0 -> !((GameTestInstance)$$0.value()).manualOnly()).toList();
            if (this.verify) {
                GameTestBatchFactory.TestDecorator $$3 = GameTestServer::rotateAndMultiply;
                LOGGER.info("Verify requested. Will run each test that matches {} {} times", (Object)this.testSelection.get(), (Object)(100 * Rotation.values().length));
            } else {
                GameTestBatchFactory.TestDecorator $$4 = GameTestBatchFactory.DIRECT;
                LOGGER.info("Will run tests matching {} ({} tests)", (Object)this.testSelection.get(), (Object)$$2.size());
            }
        } else {
            $$5 = $$1.listElements().filter($$0 -> !((GameTestInstance)$$0.value()).manualOnly()).toList();
            $$6 = GameTestBatchFactory.DIRECT;
        }
        return GameTestBatchFactory.divideIntoBatches($$5, $$6, $$02);
    }

    private static Stream<GameTestInfo> rotateAndMultiply(Holder.Reference<GameTestInstance> $$0, ServerLevel $$1) {
        Stream.Builder<GameTestInfo> $$2 = Stream.builder();
        for (Rotation $$3 : Rotation.values()) {
            for (int $$4 = 0; $$4 < 100; ++$$4) {
                $$2.add(new GameTestInfo($$0, $$3, $$1, RetryOptions.noRetries()));
            }
        }
        return $$2.build();
    }

    public static Stream<Holder.Reference<GameTestInstance>> getTestsForSelection(RegistryAccess $$0, String $$1) {
        return ResourceSelectorArgument.parse(new StringReader($$1), $$0.lookupOrThrow(Registries.TEST_INSTANCE)).stream();
    }

    @Override
    public void tickServer(BooleanSupplier $$0) {
        super.tickServer($$0);
        ServerLevel $$1 = this.overworld();
        if (!this.haveTestsStarted()) {
            this.startTests($$1);
        }
        if ($$1.getGameTime() % 20L == 0L) {
            LOGGER.info(this.testTracker.getProgressBar());
        }
        if (this.testTracker.isDone()) {
            this.halt(false);
            LOGGER.info(this.testTracker.getProgressBar());
            GlobalTestReporter.finish();
            LOGGER.info("========= {} GAME TESTS COMPLETE IN {} ======================", (Object)this.testTracker.getTotalCount(), (Object)this.stopwatch.stop());
            if (this.testTracker.hasFailedRequired()) {
                LOGGER.info("{} required tests failed :(", (Object)this.testTracker.getFailedRequiredCount());
                this.testTracker.getFailedRequired().forEach(GameTestServer::logFailedTest);
            } else {
                LOGGER.info("All {} required tests passed :)", (Object)this.testTracker.getTotalCount());
            }
            if (this.testTracker.hasFailedOptional()) {
                LOGGER.info("{} optional tests failed", (Object)this.testTracker.getFailedOptionalCount());
                this.testTracker.getFailedOptional().forEach(GameTestServer::logFailedTest);
            }
            LOGGER.info("====================================================");
        }
    }

    private static void logFailedTest(GameTestInfo $$0) {
        if ($$0.getRotation() != Rotation.NONE) {
            LOGGER.info("   - {} with rotation {}: {}", new Object[]{$$0.id(), $$0.getRotation().getSerializedName(), $$0.getError().getDescription().getString()});
        } else {
            LOGGER.info("   - {}: {}", (Object)$$0.id(), (Object)$$0.getError().getDescription().getString());
        }
    }

    @Override
    public SampleLogger getTickTimeLogger() {
        return this.sampleLogger;
    }

    @Override
    public boolean isTickTimeLoggingEnabled() {
        return false;
    }

    @Override
    public void waitUntilNextTick() {
        this.runAllTasks();
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport $$0) {
        $$0.setDetail("Type", "Game test server");
        return $$0;
    }

    @Override
    public void onServerExit() {
        super.onServerExit();
        LOGGER.info("Game test server shutting down");
        System.exit(this.testTracker != null ? this.testTracker.getFailedRequiredCount() : -1);
    }

    @Override
    public void onServerCrash(CrashReport $$0) {
        super.onServerCrash($$0);
        LOGGER.error("Game test server crashed\n{}", (Object)$$0.getFriendlyReport(ReportType.CRASH));
        System.exit(1);
    }

    private void startTests(ServerLevel $$0) {
        BlockPos $$1 = new BlockPos($$0.random.nextIntBetweenInclusive(-14999992, 14999992), -59, $$0.random.nextIntBetweenInclusive(-14999992, 14999992));
        $$0.setRespawnData(LevelData.RespawnData.of($$0.dimension(), $$1, 0.0f, 0.0f));
        GameTestRunner $$2 = GameTestRunner.Builder.fromBatches(this.testBatches, $$0).newStructureSpawner(new StructureGridSpawner($$1, 8, false)).build();
        List<GameTestInfo> $$3 = $$2.getTestInfos();
        this.testTracker = new MultipleTestTracker($$3);
        LOGGER.info("{} tests are now running at position {}!", (Object)this.testTracker.getTotalCount(), (Object)$$1.toShortString());
        this.stopwatch.reset();
        this.stopwatch.start();
        $$2.start();
    }

    private boolean haveTestsStarted() {
        return this.testTracker != null;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public LevelBasedPermissionSet operatorUserPermissions() {
        return LevelBasedPermissionSet.ALL;
    }

    @Override
    public PermissionSet getFunctionCompilationPermissions() {
        return LevelBasedPermissionSet.OWNER;
    }

    @Override
    public boolean shouldRconBroadcast() {
        return false;
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return 0;
    }

    @Override
    public boolean useNativeTransport() {
        return false;
    }

    @Override
    public boolean isPublished() {
        return false;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    @Override
    public boolean isSingleplayerOwner(NameAndId $$0) {
        return false;
    }

    @Override
    public int getMaxPlayers() {
        return 1;
    }

    static class MockUserNameToIdResolver
    implements UserNameToIdResolver {
        private final Set<NameAndId> savedIds = new HashSet<NameAndId>();

        MockUserNameToIdResolver() {
        }

        @Override
        public void add(NameAndId $$0) {
            this.savedIds.add($$0);
        }

        @Override
        public Optional<NameAndId> get(String $$0) {
            return this.savedIds.stream().filter($$1 -> $$1.name().equals($$0)).findFirst().or(() -> Optional.of(NameAndId.createOffline($$0)));
        }

        @Override
        public Optional<NameAndId> get(UUID $$0) {
            return this.savedIds.stream().filter($$1 -> $$1.id().equals($$0)).findFirst();
        }

        @Override
        public void resolveOfflineUsers(boolean $$0) {
        }

        @Override
        public void save() {
        }
    }

    static class MockProfileResolver
    implements ProfileResolver {
        MockProfileResolver() {
        }

        @Override
        public Optional<GameProfile> fetchByName(String $$0) {
            return Optional.empty();
        }

        @Override
        public Optional<GameProfile> fetchById(UUID $$0) {
            return Optional.empty();
        }
    }
}

