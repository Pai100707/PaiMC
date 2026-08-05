/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
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
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.PackOutput;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;

public class DataGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path rootOutputFolder;
    private final PackOutput vanillaPackOutput;
    final Set<String> allProviderIds = new HashSet<String>();
    final Map<String, DataProvider> providersToRun = new LinkedHashMap<String, DataProvider>();
    private final WorldVersion version;
    private final boolean alwaysGenerate;

    public DataGenerator(Path $$0, WorldVersion $$1, boolean $$2) {
        this.rootOutputFolder = $$0;
        this.vanillaPackOutput = new PackOutput(this.rootOutputFolder);
        this.version = $$1;
        this.alwaysGenerate = $$2;
    }

    public void run() throws IOException {
        HashCache $$0 = new HashCache(this.rootOutputFolder, this.allProviderIds, this.version);
        Stopwatch $$1 = Stopwatch.createStarted();
        Stopwatch $$22 = Stopwatch.createUnstarted();
        this.providersToRun.forEach(($$2, $$3) -> {
            if (!this.alwaysGenerate && !$$0.shouldRunInThisVersion((String)$$2)) {
                LOGGER.debug("Generator {} already run for version {}", $$2, (Object)this.version.name());
                return;
            }
            LOGGER.info("Starting provider: {}", $$2);
            $$22.start();
            $$0.applyUpdate($$0.generateUpdate((String)$$2, $$3::run).join());
            $$22.stop();
            LOGGER.info("{} finished after {} ms", $$2, (Object)$$22.elapsed(TimeUnit.MILLISECONDS));
            $$22.reset();
        });
        LOGGER.info("All providers took: {} ms", (Object)$$1.elapsed(TimeUnit.MILLISECONDS));
        $$0.purgeStaleAndWrite();
    }

    public PackGenerator getVanillaPack(boolean $$0) {
        return new PackGenerator($$0, "vanilla", this.vanillaPackOutput);
    }

    public PackGenerator getBuiltinDatapack(boolean $$0, String $$1) {
        Path $$2 = this.vanillaPackOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("minecraft").resolve("datapacks").resolve($$1);
        return new PackGenerator($$0, $$1, new PackOutput($$2));
    }

    static {
        Bootstrap.bootStrap();
    }

    public class PackGenerator {
        private final boolean toRun;
        private final String providerPrefix;
        private final PackOutput output;

        PackGenerator(boolean $$1, String $$2, PackOutput $$3) {
            this.toRun = $$1;
            this.providerPrefix = $$2;
            this.output = $$3;
        }

        public <T extends DataProvider> T addProvider(DataProvider.Factory<T> $$0) {
            T $$1 = $$0.create(this.output);
            String $$2 = this.providerPrefix + "/" + $$1.getName();
            if (!DataGenerator.this.allProviderIds.add($$2)) {
                throw new IllegalStateException("Duplicate provider: " + $$2);
            }
            if (this.toRun) {
                DataGenerator.this.providersToRun.put($$2, (DataProvider)$$1);
            }
            return $$1;
        }
    }
}

