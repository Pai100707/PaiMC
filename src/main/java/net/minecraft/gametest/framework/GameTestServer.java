package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.brigadier.StringReader;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.minecraft.SystemReport;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.ResourceSelectorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.gizmos.GizmoCollector;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.WorldLoader.DataLoadOutput;
import net.minecraft.server.WorldLoader.InitConfig;
import net.minecraft.server.WorldLoader.PackConfig;
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
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.WorldDimensions.Complete;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.LevelData.RespawnData;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import org.slf4j.Logger;

public class GameTestServer extends MinecraftServer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int PROGRESS_REPORT_INTERVAL = 20;
   private static final int TEST_POSITION_RANGE = 14999992;
   private static final Services NO_SERVICES = new Services(
      null, ServicesKeySet.EMPTY, null, new GameTestServer.MockUserNameToIdResolver(), new GameTestServer.MockProfileResolver()
   );
   private static final FeatureFlagSet ENABLED_FEATURES = FeatureFlags.REGISTRY
      .allFlags()
      .subtract(FeatureFlagSet.of(FeatureFlags.REDSTONE_EXPERIMENTS, new FeatureFlag[]{FeatureFlags.MINECART_IMPROVEMENTS}));
   private final LocalSampleLogger sampleLogger = new LocalSampleLogger(4);
   private final Optional<String> testSelection;
   private final boolean verify;
   private List<GameTestBatch> testBatches = new ArrayList<>();
   private final Stopwatch stopwatch = Stopwatch.createUnstarted();
   private static final WorldOptions WORLD_OPTIONS = new WorldOptions(0L, false, false);
   
   private MultipleTestTracker testTracker;

   public static GameTestServer create(Thread $$0, LevelStorageAccess $$1, PackRepository $$2, Optional<String> $$3, boolean $$4) {
      $$2.reload();
      ArrayList<String> $$5 = new ArrayList<>($$2.getAvailableIds());
      $$5.remove("vanilla");
      $$5.addFirst("vanilla");
      WorldDataConfiguration $$6 = new WorldDataConfiguration(new DataPackConfig($$5, List.of()), ENABLED_FEATURES);
      LevelSettings $$7 = new LevelSettings("Test Level", GameType.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(ENABLED_FEATURES), $$6);
      PackConfig $$8 = new PackConfig($$2, $$6, false, true);
      InitConfig $$9 = new InitConfig($$8, CommandSelection.DEDICATED, LevelBasedPermissionSet.OWNER);

      try {
         LOGGER.debug("Starting resource loading");
         Stopwatch $$10 = Stopwatch.createStarted();
         WorldStem $$11 = (WorldStem)Util.blockUntilDone(
               $$2x -> WorldLoader.load(
                  $$9,
                  $$1xx -> {
                     Registry<LevelStem> $$2xx = new MappedRegistry(Registries.LEVEL_STEM, Lifecycle.stable()).freeze();
                     Complete $$3x = ((WorldPreset)$$1xx.datapackWorldgen().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value())
                        .createWorldDimensions()
                        .bake($$2xx);
                     return new DataLoadOutput(
                        new PrimaryLevelData($$7, WORLD_OPTIONS, $$3x.specialWorldProperty(), $$3x.lifecycle()), $$3x.dimensionsRegistryAccess()
                     );
                  },
                  WorldStem::new,
                  Util.backgroundExecutor(),
                  $$2x
               )
            )
            .get();
         $$10.stop();
         LOGGER.debug("Finished resource loading after {} ms", $$10.elapsed(TimeUnit.MILLISECONDS));
         return new GameTestServer($$0, $$1, $$2, $$11, $$3, $$4);
      } catch (Exception var12) {
         LOGGER.warn("Failed to load vanilla datapack, bit oops", var12);
         System.exit(-1);
         throw new IllegalStateException();
      }
   }

   private GameTestServer(Thread $$0, LevelStorageAccess $$1, PackRepository $$2, WorldStem $$3, Optional<String> $$4, boolean $$5) {
      super($$0, $$1, $$2, $$3, Proxy.NO_PROXY, DataFixers.getDataFixer(), NO_SERVICES, LoggingLevelLoadListener.forDedicatedServer());
      this.testSelection = $$4;
      this.verify = $$5;
   }

   public boolean initServer() {
      this.setPlayerList(new PlayerList(this, this.registries(), this.playerDataStorage, new EmptyNotificationService()) {});
      Gizmos.withCollector(GizmoCollector.NOOP);
      this.loadLevel();
      ServerLevel $$0 = this.overworld();
      this.testBatches = this.evaluateTestsToRun($$0);
      LOGGER.info("Started game test server");
      return true;
   }

   private List<GameTestBatch> evaluateTestsToRun(ServerLevel $$0) {
      Registry<GameTestInstance> $$1 = $$0.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE);
      Collection<Reference<GameTestInstance>> $$2;
      GameTestBatchFactory.TestDecorator $$3;
      if (this.testSelection.isPresent()) {
         $$2 = getTestsForSelection($$0.registryAccess(), this.testSelection.get()).filter($$0x -> !((GameTestInstance)$$0x.value()).manualOnly()).toList();
         if (this.verify) {
            $$3 = GameTestServer::rotateAndMultiply;
            LOGGER.info("Verify requested. Will run each test that matches {} {} times", this.testSelection.get(), 100 * Rotation.values().length);
         } else {
            $$3 = GameTestBatchFactory.DIRECT;
            LOGGER.info("Will run tests matching {} ({} tests)", this.testSelection.get(), $$2.size());
         }
      } else {
         $$2 = $$1.listElements().filter($$0x -> !((GameTestInstance)$$0x.value()).manualOnly()).toList();
         $$3 = GameTestBatchFactory.DIRECT;
      }

      return GameTestBatchFactory.divideIntoBatches($$2, $$3, $$0);
   }

   private static Stream<GameTestInfo> rotateAndMultiply(Reference<GameTestInstance> $$0, ServerLevel $$1) {
      Builder<GameTestInfo> $$2 = Stream.builder();

      for (Rotation $$3 : Rotation.values()) {
         for (int $$4 = 0; $$4 < 100; $$4++) {
            $$2.add(new GameTestInfo($$0, $$3, $$1, RetryOptions.noRetries()));
         }
      }

      return $$2.build();
   }

   public static Stream<Reference<GameTestInstance>> getTestsForSelection(RegistryAccess $$0, String $$1) {
      return ResourceSelectorArgument.parse(new StringReader($$1), $$0.lookupOrThrow(Registries.TEST_INSTANCE)).stream();
   }

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
         LOGGER.info("========= {} GAME TESTS COMPLETE IN {} ======================", this.testTracker.getTotalCount(), this.stopwatch.stop());
         if (this.testTracker.hasFailedRequired()) {
            LOGGER.info("{} required tests failed :(", this.testTracker.getFailedRequiredCount());
            this.testTracker.getFailedRequired().forEach(GameTestServer::logFailedTest);
         } else {
            LOGGER.info("All {} required tests passed :)", this.testTracker.getTotalCount());
         }

         if (this.testTracker.hasFailedOptional()) {
            LOGGER.info("{} optional tests failed", this.testTracker.getFailedOptionalCount());
            this.testTracker.getFailedOptional().forEach(GameTestServer::logFailedTest);
         }

         LOGGER.info("====================================================");
      }
   }

   private static void logFailedTest(GameTestInfo $$0) {
      if ($$0.getRotation() != Rotation.NONE) {
         LOGGER.info("   - {} with rotation {}: {}", new Object[]{$$0.id(), $$0.getRotation().getSerializedName(), $$0.getError().getDescription().getString()});
      } else {
         LOGGER.info("   - {}: {}", $$0.id(), $$0.getError().getDescription().getString());
      }
   }

   public SampleLogger getTickTimeLogger() {
      return this.sampleLogger;
   }

   public boolean isTickTimeLoggingEnabled() {
      return false;
   }

   public void waitUntilNextTick() {
      this.runAllTasks();
   }

   public SystemReport fillServerSystemReport(SystemReport $$0) {
      $$0.setDetail("Type", "Game test server");
      return $$0;
   }

   public void onServerExit() {
      super.onServerExit();
      LOGGER.info("Game test server shutting down");
      System.exit(this.testTracker != null ? this.testTracker.getFailedRequiredCount() : -1);
   }

   public void onServerCrash(CrashReport $$0) {
      super.onServerCrash($$0);
      LOGGER.error("Game test server crashed\n{}", $$0.getFriendlyReport(ReportType.CRASH));
      System.exit(1);
   }

   private void startTests(ServerLevel $$0) {
      BlockPos $$1 = new BlockPos($$0.random.nextIntBetweenInclusive(-14999992, 14999992), -59, $$0.random.nextIntBetweenInclusive(-14999992, 14999992));
      $$0.setRespawnData(RespawnData.of($$0.dimension(), $$1, 0.0F, 0.0F));
      GameTestRunner $$2 = GameTestRunner.Builder.fromBatches(this.testBatches, $$0).newStructureSpawner(new StructureGridSpawner($$1, 8, false)).build();
      Collection<GameTestInfo> $$3 = $$2.getTestInfos();
      this.testTracker = new MultipleTestTracker($$3);
      LOGGER.info("{} tests are now running at position {}!", this.testTracker.getTotalCount(), $$1.toShortString());
      this.stopwatch.reset();
      this.stopwatch.start();
      $$2.start();
   }

   private boolean haveTestsStarted() {
      return this.testTracker != null;
   }

   public boolean isHardcore() {
      return false;
   }

   public LevelBasedPermissionSet operatorUserPermissions() {
      return LevelBasedPermissionSet.ALL;
   }

   public PermissionSet getFunctionCompilationPermissions() {
      return LevelBasedPermissionSet.OWNER;
   }

   public boolean shouldRconBroadcast() {
      return false;
   }

   public boolean isDedicatedServer() {
      return false;
   }

   public int getRateLimitPacketsPerSecond() {
      return 0;
   }

   public boolean useNativeTransport() {
      return false;
   }

   public boolean isPublished() {
      return false;
   }

   public boolean shouldInformAdmins() {
      return false;
   }

   public boolean isSingleplayerOwner(NameAndId $$0) {
      return false;
   }

   public int getMaxPlayers() {
      return 1;
   }

   static class MockProfileResolver implements ProfileResolver {
      public Optional<GameProfile> fetchByName(String $$0) {
         return Optional.empty();
      }

      public Optional<GameProfile> fetchById(UUID $$0) {
         return Optional.empty();
      }
   }

   static class MockUserNameToIdResolver implements UserNameToIdResolver {
      private final Set<NameAndId> savedIds = new HashSet<>();

      public void add(NameAndId $$0) {
         this.savedIds.add($$0);
      }

      public Optional<NameAndId> get(String $$0) {
         return this.savedIds.stream().filter($$1 -> $$1.name().equals($$0)).findFirst().or(() -> Optional.of(NameAndId.createOffline($$0)));
      }

      public Optional<NameAndId> get(UUID $$0) {
         return this.savedIds.stream().filter($$1 -> $$1.id().equals($$0)).findFirst();
      }

      public void resolveOfflineUsers(boolean $$0) {
      }

      public void save() {
      }
   }
}
