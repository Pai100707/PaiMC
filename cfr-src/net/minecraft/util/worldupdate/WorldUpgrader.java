/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Reference2FloatMap
 *  it.unimi.dsi.fastutil.objects.Reference2FloatMaps
 *  it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.worldupdate;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMaps;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.LegacyTagFixer;
import net.minecraft.world.level.chunk.storage.RecreatingSimpleRegionStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class WorldUpgrader
implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private static final String NEW_DIRECTORY_PREFIX = "new_";
    static final Component STATUS_UPGRADING_POI = Component.translatable("optimizeWorld.stage.upgrading.poi");
    static final Component STATUS_FINISHED_POI = Component.translatable("optimizeWorld.stage.finished.poi");
    static final Component STATUS_UPGRADING_ENTITIES = Component.translatable("optimizeWorld.stage.upgrading.entities");
    static final Component STATUS_FINISHED_ENTITIES = Component.translatable("optimizeWorld.stage.finished.entities");
    static final Component STATUS_UPGRADING_CHUNKS = Component.translatable("optimizeWorld.stage.upgrading.chunks");
    static final Component STATUS_FINISHED_CHUNKS = Component.translatable("optimizeWorld.stage.finished.chunks");
    final Registry<LevelStem> dimensions;
    final Set<ResourceKey<Level>> levels;
    final boolean eraseCache;
    final boolean recreateRegionFiles;
    final LevelStorageSource.LevelStorageAccess levelStorage;
    private final Thread thread;
    final DataFixer dataFixer;
    volatile boolean running = true;
    private volatile boolean finished;
    volatile float progress;
    volatile int totalChunks;
    volatile int totalFiles;
    volatile int converted;
    volatile int skipped;
    final Reference2FloatMap<ResourceKey<Level>> progressMap = Reference2FloatMaps.synchronize((Reference2FloatMap)new Reference2FloatOpenHashMap());
    volatile Component status = Component.translatable("optimizeWorld.stage.counting");
    static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    final DimensionDataStorage overworldDataStorage;

    public WorldUpgrader(LevelStorageSource.LevelStorageAccess $$02, DataFixer $$12, WorldData $$2, RegistryAccess $$3, boolean $$4, boolean $$5) {
        this.dimensions = $$3.lookupOrThrow(Registries.LEVEL_STEM);
        this.levels = this.dimensions.registryKeySet().stream().map(Registries::levelStemToLevel).collect(Collectors.toUnmodifiableSet());
        this.eraseCache = $$4;
        this.dataFixer = $$12;
        this.levelStorage = $$02;
        this.overworldDataStorage = new DimensionDataStorage(this.levelStorage.getDimensionPath(Level.OVERWORLD).resolve("data"), $$12, $$3);
        this.recreateRegionFiles = $$5;
        this.thread = THREAD_FACTORY.newThread(this::work);
        this.thread.setUncaughtExceptionHandler(($$0, $$1) -> {
            LOGGER.error("Error upgrading world", $$1);
            this.status = Component.translatable("optimizeWorld.stage.failed");
            this.finished = true;
        });
        this.thread.start();
    }

    public void cancel() {
        this.running = false;
        try {
            this.thread.join();
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    private void work() {
        long $$0 = Util.getMillis();
        LOGGER.info("Upgrading entities");
        new EntityUpgrader(this).upgrade();
        LOGGER.info("Upgrading POIs");
        new PoiUpgrader(this).upgrade();
        LOGGER.info("Upgrading blocks");
        new ChunkUpgrader().upgrade();
        this.overworldDataStorage.saveAndJoin();
        $$0 = Util.getMillis() - $$0;
        LOGGER.info("World optimizaton finished after {} seconds", (Object)($$0 / 1000L));
        this.finished = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public Set<ResourceKey<Level>> levels() {
        return this.levels;
    }

    public float dimensionProgress(ResourceKey<Level> $$0) {
        return this.progressMap.getFloat($$0);
    }

    public float getProgress() {
        return this.progress;
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }

    public int getConverted() {
        return this.converted;
    }

    public int getSkipped() {
        return this.skipped;
    }

    public Component getStatus() {
        return this.status;
    }

    @Override
    public void close() {
        this.overworldDataStorage.close();
    }

    static Path resolveRecreateDirectory(Path $$0) {
        return $$0.resolveSibling(NEW_DIRECTORY_PREFIX + $$0.getFileName().toString());
    }

    class EntityUpgrader
    extends SimpleRegionStorageUpgrader {
        EntityUpgrader(WorldUpgrader worldUpgrader) {
            super(DataFixTypes.ENTITY_CHUNK, "entities", STATUS_UPGRADING_ENTITIES, STATUS_FINISHED_ENTITIES);
        }

        @Override
        protected CompoundTag upgradeTag(SimpleRegionStorage $$0, CompoundTag $$1) {
            return $$0.upgradeChunkTag($$1, -1);
        }
    }

    class PoiUpgrader
    extends SimpleRegionStorageUpgrader {
        PoiUpgrader(WorldUpgrader worldUpgrader) {
            super(DataFixTypes.POI_CHUNK, "poi", STATUS_UPGRADING_POI, STATUS_FINISHED_POI);
        }

        @Override
        protected CompoundTag upgradeTag(SimpleRegionStorage $$0, CompoundTag $$1) {
            return $$0.upgradeChunkTag($$1, 1945);
        }
    }

    class ChunkUpgrader
    extends AbstractUpgrader {
        ChunkUpgrader() {
            super(DataFixTypes.CHUNK, "chunk", "region", STATUS_UPGRADING_CHUNKS, STATUS_FINISHED_CHUNKS);
        }

        @Override
        protected boolean tryProcessOnePosition(SimpleRegionStorage $$0, ChunkPos $$1, ResourceKey<Level> $$2) {
            CompoundTag $$3 = $$0.read($$1).join().orElse(null);
            if ($$3 != null) {
                boolean $$8;
                int $$4 = NbtUtils.getDataVersion($$3);
                ChunkGenerator $$5 = WorldUpgrader.this.dimensions.getValueOrThrow(Registries.levelToLevelStem($$2)).generator();
                CompoundTag $$6 = $$0.upgradeChunkTag($$3, -1, ChunkMap.getChunkDataFixContextTag($$2, $$5.getTypeNameForDataFixer()));
                ChunkPos $$7 = new ChunkPos($$6.getIntOr("xPos", 0), $$6.getIntOr("zPos", 0));
                if (!$$7.equals($$1)) {
                    LOGGER.warn("Chunk {} has invalid position {}", (Object)$$1, (Object)$$7);
                }
                boolean bl = $$8 = $$4 < SharedConstants.getCurrentVersion().dataVersion().version();
                if (WorldUpgrader.this.eraseCache) {
                    $$8 = $$8 || $$6.contains("Heightmaps");
                    $$6.remove("Heightmaps");
                    $$8 = $$8 || $$6.contains("isLightOn");
                    $$6.remove("isLightOn");
                    ListTag $$9 = $$6.getListOrEmpty("sections");
                    for (int $$10 = 0; $$10 < $$9.size(); ++$$10) {
                        Optional<CompoundTag> $$11 = $$9.getCompound($$10);
                        if ($$11.isEmpty()) continue;
                        CompoundTag $$12 = $$11.get();
                        $$8 = $$8 || $$12.contains("BlockLight");
                        $$12.remove("BlockLight");
                        $$8 = $$8 || $$12.contains("SkyLight");
                        $$12.remove("SkyLight");
                    }
                }
                if ($$8 || WorldUpgrader.this.recreateRegionFiles) {
                    if (this.previousWriteFuture != null) {
                        this.previousWriteFuture.join();
                    }
                    this.previousWriteFuture = $$0.write($$1, $$6);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected SimpleRegionStorage createStorage(RegionStorageInfo $$0, Path $$1) {
            Supplier<LegacyTagFixer> $$2 = LegacyStructureDataHandler.getLegacyTagFixer($$0.dimension(), () -> WorldUpgrader.this.overworldDataStorage, WorldUpgrader.this.dataFixer);
            return WorldUpgrader.this.recreateRegionFiles ? new RecreatingSimpleRegionStorage($$0.withTypeSuffix("source"), $$1, $$0.withTypeSuffix("target"), WorldUpgrader.resolveRecreateDirectory($$1), WorldUpgrader.this.dataFixer, true, DataFixTypes.CHUNK, $$2) : new SimpleRegionStorage($$0, $$1, WorldUpgrader.this.dataFixer, true, DataFixTypes.CHUNK, $$2);
        }
    }

    abstract class SimpleRegionStorageUpgrader
    extends AbstractUpgrader {
        SimpleRegionStorageUpgrader(DataFixTypes $$0, String $$1, Component $$2, Component $$3) {
            super($$0, $$1, $$1, $$2, $$3);
        }

        @Override
        protected SimpleRegionStorage createStorage(RegionStorageInfo $$0, Path $$1) {
            return WorldUpgrader.this.recreateRegionFiles ? new RecreatingSimpleRegionStorage($$0.withTypeSuffix("source"), $$1, $$0.withTypeSuffix("target"), WorldUpgrader.resolveRecreateDirectory($$1), WorldUpgrader.this.dataFixer, true, this.dataFixType, LegacyTagFixer.EMPTY) : new SimpleRegionStorage($$0, $$1, WorldUpgrader.this.dataFixer, true, this.dataFixType);
        }

        @Override
        protected boolean tryProcessOnePosition(SimpleRegionStorage $$0, ChunkPos $$1, ResourceKey<Level> $$2) {
            CompoundTag $$3 = $$0.read($$1).join().orElse(null);
            if ($$3 != null) {
                boolean $$6;
                int $$4 = NbtUtils.getDataVersion($$3);
                CompoundTag $$5 = this.upgradeTag($$0, $$3);
                boolean bl = $$6 = $$4 < SharedConstants.getCurrentVersion().dataVersion().version();
                if ($$6 || WorldUpgrader.this.recreateRegionFiles) {
                    if (this.previousWriteFuture != null) {
                        this.previousWriteFuture.join();
                    }
                    this.previousWriteFuture = $$0.write($$1, $$5);
                    return true;
                }
            }
            return false;
        }

        protected abstract CompoundTag upgradeTag(SimpleRegionStorage var1, CompoundTag var2);
    }

    abstract class AbstractUpgrader {
        private final Component upgradingStatus;
        private final Component finishedStatus;
        private final String type;
        private final String folderName;
        protected @Nullable CompletableFuture<Void> previousWriteFuture;
        protected final DataFixTypes dataFixType;

        AbstractUpgrader(DataFixTypes $$0, String $$1, String $$2, Component $$3, Component $$4) {
            this.dataFixType = $$0;
            this.type = $$1;
            this.folderName = $$2;
            this.upgradingStatus = $$3;
            this.finishedStatus = $$4;
        }

        public void upgrade() {
            WorldUpgrader.this.totalFiles = 0;
            WorldUpgrader.this.totalChunks = 0;
            WorldUpgrader.this.converted = 0;
            WorldUpgrader.this.skipped = 0;
            List<DimensionToUpgrade> $$0 = this.getDimensionsToUpgrade();
            if (WorldUpgrader.this.totalChunks == 0) {
                return;
            }
            float $$1 = WorldUpgrader.this.totalFiles;
            WorldUpgrader.this.status = this.upgradingStatus;
            while (WorldUpgrader.this.running) {
                boolean $$2 = false;
                float $$3 = 0.0f;
                for (DimensionToUpgrade $$4 : $$0) {
                    ResourceKey<Level> $$5 = $$4.dimensionKey;
                    ListIterator<FileToUpgrade> $$6 = $$4.files;
                    SimpleRegionStorage $$7 = $$4.storage;
                    if ($$6.hasNext()) {
                        FileToUpgrade $$8 = $$6.next();
                        boolean $$9 = true;
                        for (ChunkPos $$10 : $$8.chunksToUpgrade) {
                            $$9 = $$9 && this.processOnePosition($$5, $$7, $$10);
                            $$2 = true;
                        }
                        if (WorldUpgrader.this.recreateRegionFiles) {
                            if ($$9) {
                                this.onFileFinished($$8.file);
                            } else {
                                LOGGER.error("Failed to convert region file {}", (Object)$$8.file.getPath());
                            }
                        }
                    }
                    float $$11 = (float)$$6.nextIndex() / $$1;
                    WorldUpgrader.this.progressMap.put($$5, $$11);
                    $$3 += $$11;
                }
                WorldUpgrader.this.progress = $$3;
                if ($$2) continue;
                break;
            }
            WorldUpgrader.this.status = this.finishedStatus;
            for (DimensionToUpgrade $$12 : $$0) {
                try {
                    $$12.storage.close();
                }
                catch (Exception $$13) {
                    LOGGER.error("Error upgrading chunk", (Throwable)$$13);
                }
            }
        }

        private List<DimensionToUpgrade> getDimensionsToUpgrade() {
            ArrayList $$0 = Lists.newArrayList();
            for (ResourceKey<Level> $$1 : WorldUpgrader.this.levels) {
                RegionStorageInfo $$2 = new RegionStorageInfo(WorldUpgrader.this.levelStorage.getLevelId(), $$1, this.type);
                Path $$3 = WorldUpgrader.this.levelStorage.getDimensionPath($$1).resolve(this.folderName);
                SimpleRegionStorage $$4 = this.createStorage($$2, $$3);
                ListIterator<FileToUpgrade> $$5 = this.getFilesToProcess($$2, $$3);
                $$0.add(new DimensionToUpgrade($$1, $$4, $$5));
            }
            return $$0;
        }

        protected abstract SimpleRegionStorage createStorage(RegionStorageInfo var1, Path var2);

        private ListIterator<FileToUpgrade> getFilesToProcess(RegionStorageInfo $$02, Path $$1) {
            List<FileToUpgrade> $$2 = AbstractUpgrader.getAllChunkPositions($$02, $$1);
            WorldUpgrader.this.totalFiles += $$2.size();
            WorldUpgrader.this.totalChunks += $$2.stream().mapToInt($$0 -> $$0.chunksToUpgrade.size()).sum();
            return $$2.listIterator();
        }

        private static List<FileToUpgrade> getAllChunkPositions(RegionStorageInfo $$02, Path $$12) {
            File[] $$2 = $$12.toFile().listFiles(($$0, $$1) -> $$1.endsWith(".mca"));
            if ($$2 == null) {
                return List.of();
            }
            ArrayList $$3 = Lists.newArrayList();
            for (File $$4 : $$2) {
                Matcher $$5 = REGEX.matcher($$4.getName());
                if (!$$5.matches()) continue;
                int $$6 = Integer.parseInt($$5.group(1)) << 5;
                int $$7 = Integer.parseInt($$5.group(2)) << 5;
                ArrayList $$8 = Lists.newArrayList();
                try (RegionFile $$9 = new RegionFile($$02, $$4.toPath(), $$12, true);){
                    for (int $$10 = 0; $$10 < 32; ++$$10) {
                        for (int $$11 = 0; $$11 < 32; ++$$11) {
                            ChunkPos $$122 = new ChunkPos($$10 + $$6, $$11 + $$7);
                            if (!$$9.doesChunkExist($$122)) continue;
                            $$8.add($$122);
                        }
                    }
                    if ($$8.isEmpty()) continue;
                    $$3.add(new FileToUpgrade($$9, $$8));
                }
                catch (Throwable $$13) {
                    LOGGER.error("Failed to read chunks from region file {}", (Object)$$4.toPath(), (Object)$$13);
                }
            }
            return $$3;
        }

        private boolean processOnePosition(ResourceKey<Level> $$0, SimpleRegionStorage $$1, ChunkPos $$2) {
            boolean $$3 = false;
            try {
                $$3 = this.tryProcessOnePosition($$1, $$2, $$0);
            }
            catch (CompletionException | ReportedException $$4) {
                Throwable $$5 = $$4.getCause();
                if ($$5 instanceof IOException) {
                    LOGGER.error("Error upgrading chunk {}", (Object)$$2, (Object)$$5);
                }
                throw $$4;
            }
            if ($$3) {
                ++WorldUpgrader.this.converted;
            } else {
                ++WorldUpgrader.this.skipped;
            }
            return $$3;
        }

        protected abstract boolean tryProcessOnePosition(SimpleRegionStorage var1, ChunkPos var2, ResourceKey<Level> var3);

        private void onFileFinished(RegionFile $$0) {
            if (!WorldUpgrader.this.recreateRegionFiles) {
                return;
            }
            if (this.previousWriteFuture != null) {
                this.previousWriteFuture.join();
            }
            Path $$1 = $$0.getPath();
            Path $$2 = $$1.getParent();
            Path $$3 = WorldUpgrader.resolveRecreateDirectory($$2).resolve($$1.getFileName().toString());
            try {
                if ($$3.toFile().exists()) {
                    Files.delete($$1);
                    Files.move($$3, $$1, new CopyOption[0]);
                } else {
                    LOGGER.error("Failed to replace an old region file. New file {} does not exist.", (Object)$$3);
                }
            }
            catch (IOException $$4) {
                LOGGER.error("Failed to replace an old region file", (Throwable)$$4);
            }
        }
    }

    static final class FileToUpgrade
    extends Record {
        final RegionFile file;
        final List<ChunkPos> chunksToUpgrade;

        FileToUpgrade(RegionFile $$0, List<ChunkPos> $$1) {
            this.file = $$0;
            this.chunksToUpgrade = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{FileToUpgrade.class, "file;chunksToUpgrade", "file", "chunksToUpgrade"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FileToUpgrade.class, "file;chunksToUpgrade", "file", "chunksToUpgrade"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FileToUpgrade.class, "file;chunksToUpgrade", "file", "chunksToUpgrade"}, this, $$0);
        }

        public RegionFile file() {
            return this.file;
        }

        public List<ChunkPos> chunksToUpgrade() {
            return this.chunksToUpgrade;
        }
    }

    static final class DimensionToUpgrade
    extends Record {
        final ResourceKey<Level> dimensionKey;
        final SimpleRegionStorage storage;
        final ListIterator<FileToUpgrade> files;

        DimensionToUpgrade(ResourceKey<Level> $$0, SimpleRegionStorage $$1, ListIterator<FileToUpgrade> $$2) {
            this.dimensionKey = $$0;
            this.storage = $$1;
            this.files = $$2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DimensionToUpgrade.class, "dimensionKey;storage;files", "dimensionKey", "storage", "files"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DimensionToUpgrade.class, "dimensionKey;storage;files", "dimensionKey", "storage", "files"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DimensionToUpgrade.class, "dimensionKey;storage;files", "dimensionKey", "storage", "files"}, this, $$0);
        }

        public ResourceKey<Level> dimensionKey() {
            return this.dimensionKey;
        }

        public SimpleRegionStorage storage() {
            return this.storage;
        }

        public ListIterator<FileToUpgrade> files() {
            return this.files;
        }
    }
}

