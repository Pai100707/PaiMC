package net.minecraft.data;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.WorldVersion;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;

public class DataGenerator {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Path rootOutputFolder;
   private final net.minecraft.data.PackOutput vanillaPackOutput;
   final Set<String> allProviderIds = new HashSet<>();
   final Map<String, net.minecraft.data.DataProvider> providersToRun = new LinkedHashMap<>();
   private final WorldVersion version;
   private final boolean alwaysGenerate;

   public DataGenerator(Path $$0, WorldVersion $$1, boolean $$2) {
      this.rootOutputFolder = $$0;
      this.vanillaPackOutput = new net.minecraft.data.PackOutput(this.rootOutputFolder);
      this.version = $$1;
      this.alwaysGenerate = $$2;
   }

   public void run() throws IOException {
      net.minecraft.data.HashCache $$0 = new net.minecraft.data.HashCache(this.rootOutputFolder, this.allProviderIds, this.version);
      Stopwatch $$1 = Stopwatch.createStarted();
      Stopwatch $$2 = Stopwatch.createUnstarted();
      this.providersToRun.forEach(($$2x, $$3) -> {
         if (!this.alwaysGenerate && !$$0.shouldRunInThisVersion($$2x)) {
            LOGGER.debug("Generator {} already run for version {}", $$2x, this.version.name());
         } else {
            LOGGER.info("Starting provider: {}", $$2x);
            $$2.start();
            $$0.applyUpdate($$0.generateUpdate($$2x, $$3::run).join());
            $$2.stop();
            LOGGER.info("{} finished after {} ms", $$2x, $$2.elapsed(TimeUnit.MILLISECONDS));
            $$2.reset();
         }
      });
      LOGGER.info("All providers took: {} ms", $$1.elapsed(TimeUnit.MILLISECONDS));
      $$0.purgeStaleAndWrite();
   }

   public net.minecraft.data.DataGenerator.PackGenerator getVanillaPack(boolean $$0) {
      return new net.minecraft.data.DataGenerator.PackGenerator($$0, "vanilla", this.vanillaPackOutput);
   }

   public net.minecraft.data.DataGenerator.PackGenerator getBuiltinDatapack(boolean $$0, String $$1) {
      Path $$2 = this.vanillaPackOutput.getOutputFolder(net.minecraft.data.PackOutput.Target.DATA_PACK).resolve("minecraft").resolve("datapacks").resolve($$1);
      return new net.minecraft.data.DataGenerator.PackGenerator($$0, $$1, new net.minecraft.data.PackOutput($$2));
   }

   static {
      Bootstrap.bootStrap();
   }

   public class PackGenerator {
      private final boolean toRun;
      private final String providerPrefix;
      private final net.minecraft.data.PackOutput output;

      PackGenerator(final boolean $$1, final String $$2, final net.minecraft.data.PackOutput $$3) {
         this.toRun = $$1;
         this.providerPrefix = $$2;
         this.output = $$3;
      }

      public <T extends net.minecraft.data.DataProvider> T addProvider(net.minecraft.data.DataProvider.Factory<T> $$0) {
         T $$1 = $$0.create(this.output);
         String $$2 = this.providerPrefix + "/" + $$1.getName();
         if (!DataGenerator.this.allProviderIds.add($$2)) {
            throw new IllegalStateException("Duplicate provider: " + $$2);
         } else {
            if (this.toRun) {
               DataGenerator.this.providersToRun.put($$2, $$1);
            }

            return $$1;
         }
      }
   }
}
