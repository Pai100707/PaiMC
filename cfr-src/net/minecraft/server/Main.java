/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.Lifecycle
 *  joptsimple.AbstractOptionSpec
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.NonOptionArgumentSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 *  joptsimple.ValueConverter
 *  joptsimple.util.PathConverter
 *  joptsimple.util.PathProperties
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import joptsimple.ValueConverter;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Eula;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
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
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Main {
    private static final Logger LOGGER = LogUtils.getLogger();

    /*
     * WARNING - void declaration
     */
    @SuppressForbidden(reason="System.out needed before bootstrap")
    @DontObfuscate
    public static void main(String[] $$0) {
        SharedConstants.tryDetectVersion();
        OptionParser $$1 = new OptionParser();
        OptionSpecBuilder $$2 = $$1.accepts("nogui");
        OptionSpecBuilder $$3 = $$1.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder $$4 = $$1.accepts("demo");
        OptionSpecBuilder $$5 = $$1.accepts("bonusChest");
        OptionSpecBuilder $$6 = $$1.accepts("forceUpgrade");
        OptionSpecBuilder $$7 = $$1.accepts("eraseCache");
        OptionSpecBuilder $$8 = $$1.accepts("recreateRegionFiles");
        OptionSpecBuilder $$9 = $$1.accepts("safeMode", "Loads level with vanilla datapack only");
        AbstractOptionSpec $$10 = $$1.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec $$11 = $$1.accepts("universe").withRequiredArg().defaultsTo((Object)".", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec $$12 = $$1.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec $$13 = $$1.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo((Object)-1, (Object[])new Integer[0]);
        ArgumentAcceptingOptionSpec $$14 = $$1.accepts("serverId").withRequiredArg();
        OptionSpecBuilder $$15 = $$1.accepts("jfrProfile");
        ArgumentAcceptingOptionSpec $$16 = $$1.accepts("pidFile").withRequiredArg().withValuesConvertedBy((ValueConverter)new PathConverter(new PathProperties[0]));
        NonOptionArgumentSpec $$17 = $$1.nonOptions();
        try {
            void $$45;
            Dynamic $$38;
            OptionSet $$18 = $$1.parse($$0);
            if ($$18.has((OptionSpec)$$10)) {
                $$1.printHelpOn((OutputStream)System.err);
                return;
            }
            Path $$19 = (Path)$$18.valueOf((OptionSpec)$$16);
            if ($$19 != null) {
                Main.writePidFile($$19);
            }
            CrashReport.preload();
            if ($$18.has((OptionSpec)$$15)) {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }
            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
            Path $$20 = Paths.get("server.properties", new String[0]);
            DedicatedServerSettings $$21 = new DedicatedServerSettings($$20);
            $$21.forceSave();
            RegionFileVersion.configure($$21.getProperties().regionFileComression);
            Path $$22 = Paths.get("eula.txt", new String[0]);
            Eula $$23 = new Eula($$22);
            if ($$18.has((OptionSpec)$$3)) {
                LOGGER.info("Initialized '{}' and '{}'", (Object)$$20.toAbsolutePath(), (Object)$$22.toAbsolutePath());
                return;
            }
            if (!$$23.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File $$24 = new File((String)$$18.valueOf((OptionSpec)$$11));
            Services $$25 = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), $$24);
            String $$26 = Optional.ofNullable((String)$$18.valueOf((OptionSpec)$$12)).orElse($$21.getProperties().levelName);
            LevelStorageSource $$27 = LevelStorageSource.createDefault($$24.toPath());
            LevelStorageSource.LevelStorageAccess $$28 = $$27.validateAndCreateAccess($$26);
            if ($$28.hasWorldData()) {
                void $$37;
                try {
                    Dynamic<?> $$29 = $$28.getDataTag();
                    LevelSummary $$30 = $$28.getSummary($$29);
                }
                catch (IOException | NbtException | ReportedNbtException $$31) {
                    LevelStorageSource.LevelDirectory $$32 = $$28.getLevelDirectory();
                    LOGGER.warn("Failed to load world data from {}", (Object)$$32.dataFile(), (Object)$$31);
                    LOGGER.info("Attempting to use fallback");
                    try {
                        Dynamic<?> $$33 = $$28.getDataTagFallback();
                        LevelSummary $$34 = $$28.getSummary($$33);
                    }
                    catch (IOException | NbtException | ReportedNbtException $$35) {
                        LOGGER.error("Failed to load world data from {}", (Object)$$32.oldDataFile(), (Object)$$35);
                        LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", (Object)$$32.dataFile(), (Object)$$32.oldDataFile());
                        return;
                    }
                    $$28.restoreLevelDataFromOld();
                }
                if ($$37.requiresManualConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }
                if (!$$37.isCompatible()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            } else {
                $$38 = null;
            }
            Dynamic $$39 = $$38;
            boolean $$40 = $$18.has((OptionSpec)$$9);
            if ($$40) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }
            PackRepository $$41 = ServerPacksSource.createPackRepository($$28);
            try {
                WorldLoader.InitConfig $$42 = Main.loadOrCreateConfig($$21.getProperties(), $$39, $$40, $$41);
                WorldStem $$43 = (WorldStem)Util.blockUntilDone(arg_0 -> Main.lambda$main$1($$42, $$39, $$21, $$18, (OptionSpec)$$4, (OptionSpec)$$5, arg_0)).get();
            }
            catch (Exception $$44) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", (Throwable)$$44);
                return;
            }
            RegistryAccess.Frozen $$46 = $$45.registries().compositeAccess();
            WorldData $$47 = $$45.worldData();
            boolean $$48 = $$18.has((OptionSpec)$$8);
            if ($$18.has((OptionSpec)$$6) || $$48) {
                Main.forceUpgrade($$28, $$47, DataFixers.getDataFixer(), $$18.has((OptionSpec)$$7), () -> true, $$46, $$48);
            }
            $$28.saveDataTag($$46, $$47);
            final DedicatedServer $$49 = MinecraftServer.spin(arg_0 -> Main.lambda$main$3($$28, $$41, (WorldStem)$$45, $$21, $$25, $$18, (OptionSpec)$$13, (OptionSpec)$$4, (OptionSpec)$$14, (OptionSpec)$$2, (OptionSpec)$$17, arg_0));
            Thread $$50 = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    $$49.halt(true);
                }
            };
            $$50.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook($$50);
        }
        catch (Throwable $$51) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", $$51);
        }
    }

    private static WorldLoader.DataLoadOutput<WorldData> createNewWorldData(DedicatedServerSettings $$0, WorldLoader.DataLoadContext $$1, Registry<LevelStem> $$2, boolean $$3, boolean $$4) {
        WorldDimensions $$11;
        WorldOptions $$10;
        LevelSettings $$9;
        if ($$3) {
            LevelSettings $$5 = MinecraftServer.DEMO_SETTINGS;
            WorldOptions $$6 = WorldOptions.DEMO_OPTIONS;
            WorldDimensions $$7 = WorldPresets.createNormalWorldDimensions($$1.datapackWorldgen());
        } else {
            DedicatedServerProperties $$8 = $$0.getProperties();
            $$9 = new LevelSettings($$8.levelName, $$8.gameMode.get(), $$8.hardcore, $$8.difficulty.get(), false, new GameRules($$1.dataConfiguration().enabledFeatures()), $$1.dataConfiguration());
            $$10 = $$4 ? $$8.worldOptions.withBonusChest(true) : $$8.worldOptions;
            $$11 = $$8.createDimensions($$1.datapackWorldgen());
        }
        WorldDimensions.Complete $$12 = $$11.bake($$2);
        Lifecycle $$13 = $$12.lifecycle().add($$1.datapackWorldgen().allRegistriesLifecycle());
        return new WorldLoader.DataLoadOutput<WorldData>(new PrimaryLevelData($$9, $$10, $$12.specialWorldProperty(), $$13), $$12.dimensionsRegistryAccess());
    }

    private static void writePidFile(Path $$0) {
        try {
            long $$1 = ProcessHandle.current().pid();
            Files.writeString($$0, (CharSequence)Long.toString($$1), new OpenOption[0]);
        }
        catch (IOException $$2) {
            throw new UncheckedIOException($$2);
        }
    }

    private static WorldLoader.InitConfig loadOrCreateConfig(DedicatedServerProperties $$0, @Nullable Dynamic<?> $$1, boolean $$2, PackRepository $$3) {
        WorldDataConfiguration $$8;
        boolean $$7;
        if ($$1 != null) {
            WorldDataConfiguration $$4 = LevelStorageSource.readDataConfig($$1);
            boolean $$5 = false;
            WorldDataConfiguration $$6 = $$4;
        } else {
            $$7 = true;
            $$8 = new WorldDataConfiguration($$0.initialDataPackConfiguration, FeatureFlags.DEFAULT_FLAGS);
        }
        WorldLoader.PackConfig $$9 = new WorldLoader.PackConfig($$3, $$8, $$2, $$7);
        return new WorldLoader.InitConfig($$9, Commands.CommandSelection.DEDICATED, $$0.functionPermissions);
    }

    private static void forceUpgrade(LevelStorageSource.LevelStorageAccess $$0, WorldData $$1, DataFixer $$2, boolean $$3, BooleanSupplier $$4, RegistryAccess $$5, boolean $$6) {
        LOGGER.info("Forcing world upgrade!");
        try (WorldUpgrader $$7 = new WorldUpgrader($$0, $$2, $$1, $$5, $$3, $$6);){
            Component $$8 = null;
            while (!$$7.isFinished()) {
                int $$10;
                Component $$9 = $$7.getStatus();
                if ($$8 != $$9) {
                    $$8 = $$9;
                    LOGGER.info($$7.getStatus().getString());
                }
                if (($$10 = $$7.getTotalChunks()) > 0) {
                    int $$11 = $$7.getConverted() + $$7.getSkipped();
                    LOGGER.info("{}% completed ({} / {} chunks)...", new Object[]{Mth.floor((float)$$11 / (float)$$10 * 100.0f), $$11, $$10});
                }
                if (!$$4.getAsBoolean()) {
                    $$7.cancel();
                    continue;
                }
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedException) {}
            }
        }
    }

    private static /* synthetic */ DedicatedServer lambda$main$3(LevelStorageSource.LevelStorageAccess $$0, PackRepository $$1, WorldStem $$2, DedicatedServerSettings $$3, Services $$4, OptionSet $$5, OptionSpec $$6, OptionSpec $$7, OptionSpec $$8, OptionSpec $$9, OptionSpec $$10, Thread $$11) {
        boolean $$13;
        DedicatedServer $$12 = new DedicatedServer($$11, $$0, $$1, $$2, $$3, DataFixers.getDataFixer(), $$4);
        $$12.setPort((Integer)$$5.valueOf($$6));
        $$12.setDemo($$5.has($$7));
        $$12.setId((String)$$5.valueOf($$8));
        boolean bl = $$13 = !$$5.has($$9) && !$$5.valuesOf($$10).contains("nogui");
        if ($$13 && !GraphicsEnvironment.isHeadless()) {
            $$12.showGui();
        }
        return $$12;
    }

    private static /* synthetic */ CompletableFuture lambda$main$1(WorldLoader.InitConfig $$0, Dynamic $$1, DedicatedServerSettings $$2, OptionSet $$3, OptionSpec $$4, OptionSpec $$52, Executor $$6) {
        return WorldLoader.load($$0, $$5 -> {
            HolderLookup.RegistryLookup $$6 = $$5.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM);
            if ($$1 != null) {
                LevelDataAndDimensions $$7 = LevelStorageSource.getLevelDataAndDimensions($$1, $$5.dataConfiguration(), (Registry<LevelStem>)$$6, $$5.datapackWorldgen());
                return new WorldLoader.DataLoadOutput<WorldData>($$7.worldData(), $$7.dimensions().dimensionsRegistryAccess());
            }
            LOGGER.info("No existing world data, creating new world");
            return Main.createNewWorldData($$2, $$5, (Registry<LevelStem>)$$6, $$3.has($$4), $$3.has($$52));
        }, WorldStem::new, Util.backgroundExecutor(), $$6);
    }
}

