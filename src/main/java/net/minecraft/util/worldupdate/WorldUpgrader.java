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
import java.nio.file.Files;
import java.nio.file.Path;
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
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class WorldUpgrader implements AutoCloseable {
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
   final LevelStorageAccess levelStorage;
   private final Thread thread;
   final DataFixer dataFixer;
   volatile boolean running = true;
   private volatile boolean finished;
   volatile float progress;
   volatile int totalChunks;
   volatile int totalFiles;
   volatile int converted;
   volatile int skipped;
   final Reference2FloatMap<ResourceKey<Level>> progressMap = Reference2FloatMaps.synchronize(new Reference2FloatOpenHashMap());
   volatile Component status = Component.translatable("optimizeWorld.stage.counting");
   static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
   final DimensionDataStorage overworldDataStorage;

   public WorldUpgrader(LevelStorageAccess $$0, DataFixer $$1, WorldData $$2, RegistryAccess $$3, boolean $$4, boolean $$5) {
      this.dimensions = $$3.lookupOrThrow(Registries.LEVEL_STEM);
      this.levels = this.dimensions.registryKeySet().stream().<ResourceKey<Level>>map(Registries::levelStemToLevel).collect(Collectors.toUnmodifiableSet());
      this.eraseCache = $$4;
      this.dataFixer = $$1;
      this.levelStorage = $$0;
      this.overworldDataStorage = new DimensionDataStorage(this.levelStorage.getDimensionPath(Level.OVERWORLD).resolve("data"), $$1, $$3);
      this.recreateRegionFiles = $$5;
      this.thread = THREAD_FACTORY.newThread(this::work);
      this.thread.setUncaughtExceptionHandler(($$0x, $$1x) -> {
         LOGGER.error("Error upgrading world", $$1x);
         this.status = Component.translatable("optimizeWorld.stage.failed");
         this.finished = true;
      });
      this.thread.start();
   }

   public void cancel() {
      this.running = false;

      try {
         this.thread.join();
      } catch (InterruptedException var2) {
      }
   }

   private void work() {
      long $$0 = net.minecraft.util.Util.getMillis();
      LOGGER.info("Upgrading entities");
      new WorldUpgrader.EntityUpgrader().upgrade();
      LOGGER.info("Upgrading POIs");
      new WorldUpgrader.PoiUpgrader().upgrade();
      LOGGER.info("Upgrading blocks");
      new WorldUpgrader.ChunkUpgrader().upgrade();
      this.overworldDataStorage.saveAndJoin();
      $$0 = net.minecraft.util.Util.getMillis() - $$0;
      LOGGER.info("World optimizaton finished after {} seconds", $$0 / 1000L);
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
      return $$0.resolveSibling("new_" + $$0.getFileName().toString());
   }

   abstract class AbstractUpgrader {
      private final Component upgradingStatus;
      private final Component finishedStatus;
      private final String type;
      private final String folderName;
      @Nullable
      protected CompletableFuture<Void> previousWriteFuture;
      protected final DataFixTypes dataFixType;

      AbstractUpgrader(final DataFixTypes $$0, final String $$1, final String $$2, final Component $$3, final Component $$4) {
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
         List<WorldUpgrader.DimensionToUpgrade> $$0 = this.getDimensionsToUpgrade();
         if (WorldUpgrader.this.totalChunks != 0) {
            float $$1 = WorldUpgrader.this.totalFiles;
            WorldUpgrader.this.status = this.upgradingStatus;

            while (WorldUpgrader.this.running) {
               boolean $$2 = false;
               float $$3 = 0.0F;

               for (WorldUpgrader.DimensionToUpgrade $$4 : $$0) {
                  ResourceKey<Level> $$5 = $$4.dimensionKey;
                  ListIterator<WorldUpgrader.FileToUpgrade> $$6 = $$4.files;
                  SimpleRegionStorage $$7 = $$4.storage;
                  if ($$6.hasNext()) {
                     WorldUpgrader.FileToUpgrade $$8 = $$6.next();
                     boolean $$9 = true;

                     for (ChunkPos $$10 : $$8.chunksToUpgrade) {
                        $$9 = $$9 && this.processOnePosition($$5, $$7, $$10);
                        $$2 = true;
                     }

                     if (WorldUpgrader.this.recreateRegionFiles) {
                        if ($$9) {
                           this.onFileFinished($$8.file);
                        } else {
                           WorldUpgrader.LOGGER.error("Failed to convert region file {}", $$8.file.getPath());
                        }
                     }
                  }

                  float $$11 = $$6.nextIndex() / $$1;
                  WorldUpgrader.this.progressMap.put($$5, $$11);
                  $$3 += $$11;
               }

               WorldUpgrader.this.progress = $$3;
               if (!$$2) {
                  break;
               }
            }

            WorldUpgrader.this.status = this.finishedStatus;

            for (WorldUpgrader.DimensionToUpgrade $$12 : $$0) {
               try {
                  $$12.storage.close();
               } catch (Exception var14) {
                  WorldUpgrader.LOGGER.error("Error upgrading chunk", var14);
               }
            }
         }
      }

      private List<WorldUpgrader.DimensionToUpgrade> getDimensionsToUpgrade() {
         List<WorldUpgrader.DimensionToUpgrade> $$0 = Lists.newArrayList();

         for (ResourceKey<Level> $$1 : WorldUpgrader.this.levels) {
            RegionStorageInfo $$2 = new RegionStorageInfo(WorldUpgrader.this.levelStorage.getLevelId(), $$1, this.type);
            Path $$3 = WorldUpgrader.this.levelStorage.getDimensionPath($$1).resolve(this.folderName);
            SimpleRegionStorage $$4 = this.createStorage($$2, $$3);
            ListIterator<WorldUpgrader.FileToUpgrade> $$5 = this.getFilesToProcess($$2, $$3);
            $$0.add(new WorldUpgrader.DimensionToUpgrade($$1, $$4, $$5));
         }

         return $$0;
      }

      protected abstract SimpleRegionStorage createStorage(RegionStorageInfo var1, Path var2);

      private ListIterator<WorldUpgrader.FileToUpgrade> getFilesToProcess(RegionStorageInfo $$0, Path $$1) {
         List<WorldUpgrader.FileToUpgrade> $$2 = getAllChunkPositions($$0, $$1);
         WorldUpgrader.this.totalFiles = WorldUpgrader.this.totalFiles + $$2.size();
         WorldUpgrader.this.totalChunks = WorldUpgrader.this.totalChunks + $$2.stream().mapToInt($$0x -> $$0x.chunksToUpgrade.size()).sum();
         return $$2.listIterator();
      }

      private static List<WorldUpgrader.FileToUpgrade> getAllChunkPositions(RegionStorageInfo $$0, Path $$1) {
         File[] $$2 = $$1.toFile().listFiles(($$0x, $$1x) -> $$1x.endsWith(".mca"));
         if ($$2 == null) {
            return List.of();
         } else {
            List<WorldUpgrader.FileToUpgrade> $$3 = Lists.newArrayList();

            for (File $$4 : $$2) {
               Matcher $$5 = WorldUpgrader.REGEX.matcher($$4.getName());
               if ($$5.matches()) {
                  int $$6 = Integer.parseInt($$5.group(1)) << 5;
                  int $$7 = Integer.parseInt($$5.group(2)) << 5;
                  List<ChunkPos> $$8 = Lists.newArrayList();

                  try {
                     RegionFile $$9 = new RegionFile($$0, $$4.toPath(), $$1, true);

                     try {
                        for (int $$10 = 0; $$10 < 32; $$10++) {
                           for (int $$11 = 0; $$11 < 32; $$11++) {
                              ChunkPos $$12 = new ChunkPos($$10 + $$6, $$11 + $$7);
                              if ($$9.doesChunkExist($$12)) {
                                 $$8.add($$12);
                              }
                           }
                        }

                        if (!$$8.isEmpty()) {
                           $$3.add(new WorldUpgrader.FileToUpgrade($$9, $$8));
                        }
                     } catch (Throwable var17) {
                        try {
                           $$9.close();
                        } catch (Throwable var16) {
                           var17.addSuppressed(var16);
                        }

                        throw var17;
                     }

                     $$9.close();
                  } catch (Throwable var18) {
                     WorldUpgrader.LOGGER.error("Failed to read chunks from region file {}", $$4.toPath(), var18);
                  }
               }
            }

            return $$3;
         }
      }

      private boolean processOnePosition(ResourceKey<Level> $$0, SimpleRegionStorage $$1, ChunkPos $$2) {
         boolean $$3 = false;

         try {
            $$3 = this.tryProcessOnePosition($$1, $$2, $$0);
         } catch (CompletionException | ReportedException var7) {
            Throwable $$5 = var7.getCause();
            if (!($$5 instanceof IOException)) {
               throw var7;
            }

            WorldUpgrader.LOGGER.error("Error upgrading chunk {}", $$2, $$5);
         }

         if ($$3) {
            WorldUpgrader.this.converted++;
         } else {
            WorldUpgrader.this.skipped++;
         }

         return $$3;
      }

      protected abstract boolean tryProcessOnePosition(SimpleRegionStorage var1, ChunkPos var2, ResourceKey<Level> var3);

      private void onFileFinished(RegionFile $$0) {
         if (WorldUpgrader.this.recreateRegionFiles) {
            if (this.previousWriteFuture != null) {
               this.previousWriteFuture.join();
            }

            Path $$1 = $$0.getPath();
            Path $$2 = $$1.getParent();
            Path $$3 = WorldUpgrader.resolveRecreateDirectory($$2).resolve($$1.getFileName().toString());

            try {
               if ($$3.toFile().exists()) {
                  Files.delete($$1);
                  Files.move($$3, $$1);
               } else {
                  WorldUpgrader.LOGGER.error("Failed to replace an old region file. New file {} does not exist.", $$3);
               }
            } catch (IOException var6) {
               WorldUpgrader.LOGGER.error("Failed to replace an old region file", var6);
            }
         }
      }
   }

   class ChunkUpgrader extends WorldUpgrader.AbstractUpgrader {
      ChunkUpgrader() {
         super(DataFixTypes.CHUNK, "chunk", "region", WorldUpgrader.STATUS_UPGRADING_CHUNKS, WorldUpgrader.STATUS_FINISHED_CHUNKS);
      }

      @Override
      protected boolean tryProcessOnePosition(SimpleRegionStorage $$0, ChunkPos $$1, ResourceKey<Level> $$2) {
         CompoundTag $$3 = (CompoundTag)((Optional)$$0.read($$1).join()).orElse(null);
         if ($$3 != null) {
            int $$4 = NbtUtils.getDataVersion($$3);
            ChunkGenerator $$5 = ((LevelStem)WorldUpgrader.this.dimensions.getValueOrThrow(Registries.levelToLevelStem($$2))).generator();
            CompoundTag $$6 = $$0.upgradeChunkTag($$3, -1, ChunkMap.getChunkDataFixContextTag($$2, $$5.getTypeNameForDataFixer()));
            ChunkPos $$7 = new ChunkPos($$6.getIntOr("xPos", 0), $$6.getIntOr("zPos", 0));
            if (!$$7.equals($$1)) {
               WorldUpgrader.LOGGER.warn("Chunk {} has invalid position {}", $$1, $$7);
            }

            boolean $$8 = $$4 < SharedConstants.getCurrentVersion().dataVersion().version();
            if (WorldUpgrader.this.eraseCache) {
               $$8 = $$8 || $$6.contains("Heightmaps");
               $$6.remove("Heightmaps");
               $$8 = $$8 || $$6.contains("isLightOn");
               $$6.remove("isLightOn");
               ListTag $$9 = $$6.getListOrEmpty("sections");

               for (int $$10 = 0; $$10 < $$9.size(); $$10++) {
                  Optional<CompoundTag> $$11 = $$9.getCompound($$10);
                  if (!$$11.isEmpty()) {
                     CompoundTag $$12 = $$11.get();
                     $$8 = $$8 || $$12.contains("BlockLight");
                     $$12.remove("BlockLight");
                     $$8 = $$8 || $$12.contains("SkyLight");
                     $$12.remove("SkyLight");
                  }
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
         Supplier<LegacyTagFixer> $$2 = LegacyStructureDataHandler.getLegacyTagFixer(
            $$0.dimension(), () -> WorldUpgrader.this.overworldDataStorage, WorldUpgrader.this.dataFixer
         );
         return (SimpleRegionStorage)(WorldUpgrader.this.recreateRegionFiles
            ? new RecreatingSimpleRegionStorage(
               $$0.withTypeSuffix("source"),
               $$1,
               $$0.withTypeSuffix("target"),
               WorldUpgrader.resolveRecreateDirectory($$1),
               WorldUpgrader.this.dataFixer,
               true,
               DataFixTypes.CHUNK,
               $$2
            )
            : new SimpleRegionStorage($$0, $$1, WorldUpgrader.this.dataFixer, true, DataFixTypes.CHUNK, $$2));
      }
   }

   record DimensionToUpgrade(ResourceKey<Level> dimensionKey, SimpleRegionStorage storage, ListIterator<WorldUpgrader.FileToUpgrade> files) {
   }

   class EntityUpgrader extends WorldUpgrader.SimpleRegionStorageUpgrader {
      EntityUpgrader() {
         super(DataFixTypes.ENTITY_CHUNK, "entities", WorldUpgrader.STATUS_UPGRADING_ENTITIES, WorldUpgrader.STATUS_FINISHED_ENTITIES);
      }

      @Override
      protected CompoundTag upgradeTag(SimpleRegionStorage $$0, CompoundTag $$1) {
         return $$0.upgradeChunkTag($$1, -1);
      }
   }

   record FileToUpgrade(RegionFile file, List<ChunkPos> chunksToUpgrade) {
   }

   class PoiUpgrader extends WorldUpgrader.SimpleRegionStorageUpgrader {
      PoiUpgrader() {
         super(DataFixTypes.POI_CHUNK, "poi", WorldUpgrader.STATUS_UPGRADING_POI, WorldUpgrader.STATUS_FINISHED_POI);
      }

      @Override
      protected CompoundTag upgradeTag(SimpleRegionStorage $$0, CompoundTag $$1) {
         return $$0.upgradeChunkTag($$1, 1945);
      }
   }

   abstract class SimpleRegionStorageUpgrader extends WorldUpgrader.AbstractUpgrader {
      SimpleRegionStorageUpgrader(final DataFixTypes $$0, final String $$1, final Component $$2, final Component $$3) {
         super($$0, $$1, $$1, $$2, $$3);
      }

      @Override
      protected SimpleRegionStorage createStorage(RegionStorageInfo $$0, Path $$1) {
         return (SimpleRegionStorage)(WorldUpgrader.this.recreateRegionFiles
            ? new RecreatingSimpleRegionStorage(
               $$0.withTypeSuffix("source"),
               $$1,
               $$0.withTypeSuffix("target"),
               WorldUpgrader.resolveRecreateDirectory($$1),
               WorldUpgrader.this.dataFixer,
               true,
               this.dataFixType,
               LegacyTagFixer.EMPTY
            )
            : new SimpleRegionStorage($$0, $$1, WorldUpgrader.this.dataFixer, true, this.dataFixType));
      }

      @Override
      protected boolean tryProcessOnePosition(SimpleRegionStorage $$0, ChunkPos $$1, ResourceKey<Level> $$2) {
         CompoundTag $$3 = (CompoundTag)((Optional)$$0.read($$1).join()).orElse(null);
         if ($$3 != null) {
            int $$4 = NbtUtils.getDataVersion($$3);
            CompoundTag $$5 = this.upgradeTag($$0, $$3);
            boolean $$6 = $$4 < SharedConstants.getCurrentVersion().dataVersion().version();
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
}
