package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.SuppressForbidden;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class GameTestMainUtil {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String DEFAULT_UNIVERSE_DIR = "gametestserver";
   private static final String LEVEL_NAME = "gametestworld";
   private static final OptionParser parser = new OptionParser();
   private static final OptionSpec<String> universe = parser.accepts(
         "universe", "The path to where the test server world will be created. Any existing folder will be replaced."
      )
      .withRequiredArg()
      .defaultsTo("gametestserver", new String[0]);
   private static final OptionSpec<File> report = parser.accepts("report", "Exports results in a junit-like XML report at the given path.")
      .withRequiredArg()
      .ofType(File.class);
   private static final OptionSpec<String> tests = parser.accepts(
         "tests", "Which test(s) to run (namespaced ID selector using wildcards). Empty means run all."
      )
      .withRequiredArg();
   private static final OptionSpec<Boolean> verify = parser.accepts(
         "verify", "Runs the tests specified with `test` or `testNamespace` 100 times for each 90 degree rotation step"
      )
      .withRequiredArg()
      .ofType(Boolean.class)
      .defaultsTo(false, new Boolean[0]);
   private static final OptionSpec<String> packs = parser.accepts("packs", "A folder of datapacks to include in the world").withRequiredArg();
   private static final OptionSpec<Void> help = parser.accepts("help").forHelp();

   @SuppressForbidden(
      reason = "Using System.err due to no bootstrap"
   )
   public static void runGameTestServer(String[] $$0, Consumer<String> $$1) throws Exception {
      parser.allowsUnrecognizedOptions();
      OptionSet $$2 = parser.parse($$0);
      if ($$2.has(help)) {
         parser.printHelpOn(System.err);
      } else {
         if ((Boolean)$$2.valueOf(verify) && !$$2.has(tests)) {
            LOGGER.error("Please specify a test selection to run the verify option. For example: --verify --tests example:test_something_*");
            System.exit(-1);
         }

         LOGGER.info("Running GameTestMain with cwd '{}', universe path '{}'", System.getProperty("user.dir"), $$2.valueOf(universe));
         if ($$2.has(report)) {
            GlobalTestReporter.replaceWith(new JUnitLikeTestReporter((File)report.value($$2)));
         }

         Bootstrap.bootStrap();
         Util.startTimerHackThread();
         String $$3 = (String)$$2.valueOf(universe);
         createOrResetDir($$3);
         $$1.accept($$3);
         if ($$2.has(packs)) {
            String $$4 = (String)$$2.valueOf(packs);
            copyPacks($$3, $$4);
         }

         LevelStorageAccess $$5 = LevelStorageSource.createDefault(Paths.get($$3)).createAccess("gametestworld");
         PackRepository $$6 = ServerPacksSource.createPackRepository($$5);
         MinecraftServer.spin($$3x -> GameTestServer.create($$3x, $$5, $$6, optionalFromOption($$2, tests), $$2.has(verify)));
      }
   }

   private static Optional<String> optionalFromOption(OptionSet $$0, OptionSpec<String> $$1) {
      return $$0.has($$1) ? Optional.of((String)$$0.valueOf($$1)) : Optional.empty();
   }

   private static void createOrResetDir(String $$0) throws IOException {
      Path $$1 = Paths.get($$0);
      if (Files.exists($$1)) {
         FileUtils.deleteDirectory($$1.toFile());
      }

      Files.createDirectories($$1);
   }

   private static void copyPacks(String $$0, String $$1) throws IOException {
      Path $$2 = Paths.get($$0).resolve("gametestworld").resolve("datapacks");
      if (!Files.exists($$2)) {
         Files.createDirectories($$2);
      }

      Path $$3 = Paths.get($$1);
      if (Files.exists($$3)) {
         try (Stream<Path> $$4 = Files.list($$3)) {
            for (Path $$5 : $$4.toList()) {
               Path $$6 = $$2.resolve($$5.getFileName());
               if (Files.isDirectory($$5)) {
                  if (Files.isRegularFile($$5.resolve("pack.mcmeta"))) {
                     FileUtils.copyDirectory($$5.toFile(), $$6.toFile());
                     LOGGER.info("Included folder pack {}", $$5.getFileName());
                  }
               } else if ($$5.toString().endsWith(".zip")) {
                  Files.copy($$5, $$6);
                  LOGGER.info("Included zip pack {}", $$5.getFileName());
               }
            }
         }
      }
   }
}
