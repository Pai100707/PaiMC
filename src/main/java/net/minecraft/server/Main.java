package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.WorldDimensions.Complete;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.LevelStorageSource.LevelDirectory;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Main {
   private static final Logger LOGGER = LogUtils.getLogger();

   @SuppressForbidden(
      reason = "System.out needed before bootstrap"
   )
   @DontObfuscate
   public static void main(String[] $$0) {
      SharedConstants.tryDetectVersion();
      OptionParser $$1 = new OptionParser();
      OptionSpec<Void> $$2 = $$1.accepts("nogui");
      OptionSpec<Void> $$3 = $$1.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
      OptionSpec<Void> $$4 = $$1.accepts("demo");
      OptionSpec<Void> $$5 = $$1.accepts("bonusChest");
      OptionSpec<Void> $$6 = $$1.accepts("forceUpgrade");
      OptionSpec<Void> $$7 = $$1.accepts("eraseCache");
      OptionSpec<Void> $$8 = $$1.accepts("recreateRegionFiles");
      OptionSpec<Void> $$9 = $$1.accepts("safeMode", "Loads level with vanilla datapack only");
      OptionSpec<Void> $$10 = $$1.accepts("help").forHelp();
      OptionSpec<String> $$11 = $$1.accepts("universe").withRequiredArg().defaultsTo(".", new String[0]);
      OptionSpec<String> $$12 = $$1.accepts("world").withRequiredArg();
      OptionSpec<Integer> $$13 = $$1.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, new Integer[0]);
      OptionSpec<String> $$14 = $$1.accepts("serverId").withRequiredArg();
      OptionSpec<Void> $$15 = $$1.accepts("jfrProfile");
      OptionSpec<Path> $$16 = $$1.accepts("pidFile").withRequiredArg().withValuesConvertedBy(new PathConverter(new PathProperties[0]));
      OptionSpec<String> $$17 = $$1.nonOptions();

      try {
         OptionSet $$18 = $$1.parse($$0);
         if ($$18.has($$10)) {
            $$1.printHelpOn(System.err);
            return;
         }

         Path $$19 = (Path)$$18.valueOf($$16);
         if ($$19 != null) {
            writePidFile($$19);
         }

         CrashReport.preload();
         if ($$18.has($$15)) {
            JvmProfiler.INSTANCE.start(Environment.SERVER);
         }

         net.minecraft.server.Bootstrap.bootStrap();
         net.minecraft.server.Bootstrap.validate();
         Util.startTimerHackThread();
         Path $$20 = Paths.get("server.properties");
         DedicatedServerSettings $$21 = new DedicatedServerSettings($$20);
         $$21.forceSave();
         RegionFileVersion.configure($$21.getProperties().regionFileComression);
         Path $$22 = Paths.get("eula.txt");
         net.minecraft.server.Eula $$23 = new net.minecraft.server.Eula($$22);
         if ($$18.has($$3)) {
            LOGGER.info("Initialized '{}' and '{}'", $$20.toAbsolutePath(), $$22.toAbsolutePath());
            return;
         }

         if (!$$23.hasAgreedToEULA()) {
            LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
            return;
         }

         File $$24 = new File((String)$$18.valueOf($$11));
         net.minecraft.server.Services $$25 = net.minecraft.server.Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), $$24);
         String $$26 = (String)Optional.ofNullable((String)$$18.valueOf($$12)).orElse($$21.getProperties().levelName);
         LevelStorageSource $$27 = LevelStorageSource.createDefault($$24.toPath());
         LevelStorageAccess $$28 = $$27.validateAndCreateAccess($$26);
         Dynamic<?> $$29;
         if ($$28.hasWorldData()) {
            LevelSummary $$30;
            try {
               $$29 = $$28.getDataTag();
               $$30 = $$28.getSummary($$29);
            } catch (NbtException | ReportedNbtException | IOException var41) {
               LevelDirectory $$32 = $$28.getLevelDirectory();
               LOGGER.warn("Failed to load world data from {}", $$32.dataFile(), var41);
               LOGGER.info("Attempting to use fallback");

               try {
                  $$29 = $$28.getDataTagFallback();
                  $$30 = $$28.getSummary($$29);
               } catch (NbtException | ReportedNbtException | IOException var40) {
                  LOGGER.error("Failed to load world data from {}", $$32.oldDataFile(), var40);
                  LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", $$32.dataFile(), $$32.oldDataFile());
                  return;
               }

               $$28.restoreLevelDataFromOld();
            }

            if ($$30.requiresManualConversion()) {
               LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
               return;
            }

            if (!$$30.isCompatible()) {
               LOGGER.info("This world was created by an incompatible version.");
               return;
            }
         } else {
            $$29 = null;
         }

         Dynamic<?> $$39 = $$29;
         boolean $$40 = $$18.has($$9);
         if ($$40) {
            LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
         }

         PackRepository $$41 = ServerPacksSource.createPackRepository($$28);

         net.minecraft.server.WorldStem $$43;
         try {
            net.minecraft.server.WorldLoader.InitConfig $$42 = loadOrCreateConfig($$21.getProperties(), $$39, $$40, $$41);
            $$43 = (net.minecraft.server.WorldStem)Util.blockUntilDone($$6x -> net.minecraft.server.WorldLoader.load($$42, $$5xx -> {
               Registry<LevelStem> $$6xx = $$5xx.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM);
               if ($$39 != null) {
                  LevelDataAndDimensions $$7x = LevelStorageSource.getLevelDataAndDimensions($$39, $$5xx.dataConfiguration(), $$6xx, $$5xx.datapackWorldgen());
                  return new net.minecraft.server.WorldLoader.DataLoadOutput<>($$7x.worldData(), $$7x.dimensions().dimensionsRegistryAccess());
               } else {
                  LOGGER.info("No existing world data, creating new world");
                  return createNewWorldData($$21, $$5xx, $$6xx, $$18.has($$4), $$18.has($$5));
               }
            }, net.minecraft.server.WorldStem::new, Util.backgroundExecutor(), $$6x)).get();
         } catch (Exception var39) {
            LOGGER.warn(
               "Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", var39
            );
            return;
         }

         Frozen $$46 = $$43.registries().compositeAccess();
         WorldData $$47 = $$43.worldData();
         boolean $$48 = $$18.has($$8);
         if ($$18.has($$6) || $$48) {
            forceUpgrade($$28, $$47, DataFixers.getDataFixer(), $$18.has($$7), () -> true, $$46, $$48);
         }

         $$28.saveDataTag($$46, $$47);
         final DedicatedServer $$49 = net.minecraft.server.MinecraftServer.spin($$11x -> {
            DedicatedServer $$12x = new DedicatedServer($$11x, $$28, $$41, $$43, $$21, DataFixers.getDataFixer(), $$25);
            $$12x.setPort((Integer)$$18.valueOf($$13));
            $$12x.setDemo($$18.has($$4));
            $$12x.setId((String)$$18.valueOf($$14));
            boolean $$13x = !$$18.has($$2) && !$$18.valuesOf($$17).contains("nogui");
            if ($$13x && !GraphicsEnvironment.isHeadless()) {
               $$12x.showGui();
            }

            return $$12x;
         });
         Thread $$50 = new Thread("Server Shutdown Thread") {
            @Override
            public void run() {
               $$49.halt(true);
            }
         };
         $$50.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
         Runtime.getRuntime().addShutdownHook($$50);
      } catch (Throwable var42) {
         LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", var42);
      }
   }

   private static net.minecraft.server.WorldLoader.DataLoadOutput<WorldData> createNewWorldData(
      DedicatedServerSettings $$0, net.minecraft.server.WorldLoader.DataLoadContext $$1, Registry<LevelStem> $$2, boolean $$3, boolean $$4
   ) {
      LevelSettings $$5;
      WorldOptions $$6;
      WorldDimensions $$7;
      if ($$3) {
         $$5 = net.minecraft.server.MinecraftServer.DEMO_SETTINGS;
         $$6 = WorldOptions.DEMO_OPTIONS;
         $$7 = WorldPresets.createNormalWorldDimensions($$1.datapackWorldgen());
      } else {
         DedicatedServerProperties $$8 = $$0.getProperties();
         $$5 = new LevelSettings(
            $$8.levelName,
            $$8.gameMode.get(),
            $$8.hardcore,
            $$8.difficulty.get(),
            false,
            new GameRules($$1.dataConfiguration().enabledFeatures()),
            $$1.dataConfiguration()
         );
         $$6 = $$4 ? $$8.worldOptions.withBonusChest(true) : $$8.worldOptions;
         $$7 = $$8.createDimensions($$1.datapackWorldgen());
      }

      Complete $$12 = $$7.bake($$2);
      Lifecycle $$13 = $$12.lifecycle().add($$1.datapackWorldgen().allRegistriesLifecycle());
      return new net.minecraft.server.WorldLoader.DataLoadOutput<>(
         new PrimaryLevelData($$5, $$6, $$12.specialWorldProperty(), $$13), $$12.dimensionsRegistryAccess()
      );
   }

   private static void writePidFile(Path $$0) {
      try {
         long $$1 = ProcessHandle.current().pid();
         Files.writeString($$0, Long.toString($$1));
      } catch (IOException var3) {
         throw new UncheckedIOException(var3);
      }
   }

   private static net.minecraft.server.WorldLoader.InitConfig loadOrCreateConfig(
      DedicatedServerProperties $$0, @Nullable Dynamic<?> $$1, boolean $$2, PackRepository $$3
   ) {
      boolean $$5;
      WorldDataConfiguration $$6;
      if ($$1 != null) {
         WorldDataConfiguration $$4 = LevelStorageSource.readDataConfig($$1);
         $$5 = false;
         $$6 = $$4;
      } else {
         $$5 = true;
         $$6 = new WorldDataConfiguration($$0.initialDataPackConfiguration, FeatureFlags.DEFAULT_FLAGS);
      }

      net.minecraft.server.WorldLoader.PackConfig $$9 = new net.minecraft.server.WorldLoader.PackConfig($$3, $$6, $$2, $$5);
      return new net.minecraft.server.WorldLoader.InitConfig($$9, CommandSelection.DEDICATED, $$0.functionPermissions);
   }

   private static void forceUpgrade(LevelStorageAccess $$0, WorldData $$1, DataFixer $$2, boolean $$3, BooleanSupplier $$4, RegistryAccess $$5, boolean $$6) {
      LOGGER.info("Forcing world upgrade!");
      WorldUpgrader $$7 = new WorldUpgrader($$0, $$2, $$1, $$5, $$3, $$6);

      try {
         Component $$8 = null;

         while (!$$7.isFinished()) {
            Component $$9 = $$7.getStatus();
            if ($$8 != $$9) {
               $$8 = $$9;
               LOGGER.info($$7.getStatus().getString());
            }

            int $$10 = $$7.getTotalChunks();
            if ($$10 > 0) {
               int $$11 = $$7.getConverted() + $$7.getSkipped();
               LOGGER.info("{}% completed ({} / {} chunks)...", new Object[]{Mth.floor((float)$$11 / $$10 * 100.0F), $$11, $$10});
            }

            if (!$$4.getAsBoolean()) {
               $$7.cancel();
            } else {
               try {
                  Thread.sleep(1000L);
               } catch (InterruptedException var13) {
               }
            }
         }
      } catch (Throwable var14) {
         try {
            $$7.close();
         } catch (Throwable var12) {
            var14.addSuppressed(var12);
         }

         throw var14;
      }

      $$7.close();
   }
}
