package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtFormatException;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.SkipFields;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldLoader.PackConfig;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.FileUtil;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import net.minecraft.world.level.validation.PathAllowList;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class LevelStorageSource {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final String TAG_DATA = "Data";
   private static final PathMatcher NO_SYMLINKS_ALLOWED = $$0 -> false;
   public static final String ALLOWED_SYMLINKS_CONFIG_NAME = "allowed_symlinks.txt";
   private static final int DISK_SPACE_WARNING_THRESHOLD = 67108864;
   private final Path baseDir;
   private final Path backupDir;
   final DataFixer fixerUpper;
   private final DirectoryValidator worldDirValidator;

   public LevelStorageSource(Path $$0, Path $$1, DirectoryValidator $$2, DataFixer $$3) {
      this.fixerUpper = $$3;

      try {
         FileUtil.createDirectoriesSafe($$0);
      } catch (IOException var6) {
         throw new UncheckedIOException(var6);
      }

      this.baseDir = $$0;
      this.backupDir = $$1;
      this.worldDirValidator = $$2;
   }

   public static DirectoryValidator parseValidator(Path $$0) {
      if (Files.exists($$0)) {
         try {
            DirectoryValidator var2;
            try (BufferedReader $$1 = Files.newBufferedReader($$0)) {
               var2 = new DirectoryValidator(PathAllowList.readPlain($$1));
            }

            return var2;
         } catch (Exception var6) {
            LOGGER.error("Failed to parse {}, disallowing all symbolic links", "allowed_symlinks.txt", var6);
         }
      }

      return new DirectoryValidator(NO_SYMLINKS_ALLOWED);
   }

   public static LevelStorageSource createDefault(Path $$0) {
      DirectoryValidator $$1 = parseValidator($$0.resolve("allowed_symlinks.txt"));
      return new LevelStorageSource($$0, $$0.resolve("../backups"), $$1, DataFixers.getDataFixer());
   }

   public static net.minecraft.world.level.WorldDataConfiguration readDataConfig(Dynamic<?> $$0) {
      return net.minecraft.world.level.WorldDataConfiguration.CODEC
         .parse($$0)
         .resultOrPartial(LOGGER::error)
         .orElse(net.minecraft.world.level.WorldDataConfiguration.DEFAULT);
   }

   public static PackConfig getPackConfig(Dynamic<?> $$0, PackRepository $$1, boolean $$2) {
      return new PackConfig($$1, readDataConfig($$0), $$2, false);
   }

   public static LevelDataAndDimensions getLevelDataAndDimensions(
      Dynamic<?> $$0, net.minecraft.world.level.WorldDataConfiguration $$1, Registry<LevelStem> $$2, Provider $$3
   ) {
      Dynamic<?> $$4 = RegistryOps.injectRegistryContext($$0, $$3);
      Dynamic<?> $$5 = $$4.get("WorldGenSettings").orElseEmptyMap();
      WorldGenSettings $$6 = (WorldGenSettings)WorldGenSettings.CODEC.parse($$5).getOrThrow();
      net.minecraft.world.level.LevelSettings $$7 = net.minecraft.world.level.LevelSettings.parse($$4, $$1);
      WorldDimensions.Complete $$8 = $$6.dimensions().bake($$2);
      Lifecycle $$9 = $$8.lifecycle().add($$3.allRegistriesLifecycle());
      PrimaryLevelData $$10 = PrimaryLevelData.parse($$4, $$7, $$8.specialWorldProperty(), $$6.options(), $$9);
      return new LevelDataAndDimensions($$10, $$8);
   }

   public String getName() {
      return "Anvil";
   }

   public LevelStorageSource.LevelCandidates findLevelCandidates() throws LevelStorageException {
      if (!Files.isDirectory(this.baseDir)) {
         throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
      } else {
         try {
            LevelStorageSource.LevelCandidates var3;
            try (Stream<Path> $$0 = Files.list(this.baseDir)) {
               List<LevelStorageSource.LevelDirectory> $$1 = $$0.filter($$0x -> Files.isDirectory($$0x))
                  .map(LevelStorageSource.LevelDirectory::new)
                  .filter($$0x -> Files.isRegularFile($$0x.dataFile()) || Files.isRegularFile($$0x.oldDataFile()))
                  .toList();
               var3 = new LevelStorageSource.LevelCandidates($$1);
            }

            return var3;
         } catch (IOException var6) {
            throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
         }
      }
   }

   public CompletableFuture<List<LevelSummary>> loadLevelSummaries(LevelStorageSource.LevelCandidates $$0) {
      List<CompletableFuture<LevelSummary>> $$1 = new ArrayList<>($$0.levels.size());

      for (LevelStorageSource.LevelDirectory $$2 : $$0.levels) {
         $$1.add(CompletableFuture.supplyAsync(() -> {
            boolean $$1x;
            try {
               $$1x = DirectoryLock.isLocked($$2.path());
            } catch (Exception var13) {
               LOGGER.warn("Failed to read {} lock", $$2.path(), var13);
               return null;
            }

            try {
               return this.readLevelSummary($$2, $$1x);
            } catch (OutOfMemoryError var12) {
               MemoryReserve.release();
               String $$5 = "Ran out of memory trying to read summary of world folder \"" + $$2.directoryName() + "\"";
               LOGGER.error(LogUtils.FATAL_MARKER, $$5);
               OutOfMemoryError $$6 = new OutOfMemoryError("Ran out of memory reading level data");
               $$6.initCause(var12);
               CrashReport $$7 = CrashReport.forThrowable($$6, $$5);
               CrashReportCategory $$8 = $$7.addCategory("World details");
               $$8.setDetail("Folder Name", $$2.directoryName());

               try {
                  long $$9 = Files.size($$2.dataFile());
                  $$8.setDetail("level.dat size", $$9);
               } catch (IOException var11) {
                  $$8.setDetailError("level.dat size", var11);
               }

               throw new ReportedException($$7);
            }
         }, Util.backgroundExecutor().forName("loadLevelSummaries")));
      }

      return Util.sequenceFailFastAndCancel($$1).thenApply($$0x -> $$0x.stream().filter(Objects::nonNull).sorted().toList());
   }

   private int getStorageVersion() {
      return 19133;
   }

   static CompoundTag readLevelDataTagRaw(Path $$0) throws IOException {
      return NbtIo.readCompressed($$0, NbtAccounter.uncompressedQuota());
   }

   static Dynamic<?> readLevelDataTagFixed(Path $$0, DataFixer $$1) throws IOException {
      CompoundTag $$2 = readLevelDataTagRaw($$0);
      CompoundTag $$3 = $$2.getCompoundOrEmpty("Data");
      int $$4 = NbtUtils.getDataVersion($$3);
      Dynamic<?> $$5 = DataFixTypes.LEVEL.updateToCurrentVersion($$1, new Dynamic(NbtOps.INSTANCE, $$3), $$4);
      $$5 = $$5.update("Player", $$2x -> DataFixTypes.PLAYER.updateToCurrentVersion($$1, $$2x, $$4));
      return $$5.update("WorldGenSettings", $$2x -> DataFixTypes.WORLD_GEN_SETTINGS.updateToCurrentVersion($$1, $$2x, $$4));
   }

   private LevelSummary readLevelSummary(LevelStorageSource.LevelDirectory $$0, boolean $$1) {
      Path $$2 = $$0.dataFile();
      if (Files.exists($$2)) {
         try {
            if (Files.isSymbolicLink($$2)) {
               List<ForbiddenSymlinkInfo> $$3 = this.worldDirValidator.validateSymlink($$2);
               if (!$$3.isEmpty()) {
                  LOGGER.warn("{}", ContentValidationException.getMessage($$2, $$3));
                  return new LevelSummary.SymlinkLevelSummary($$0.directoryName(), $$0.iconFile());
               }
            }

            if (readLightweightData($$2) instanceof CompoundTag $$5) {
               CompoundTag $$6 = $$5.getCompoundOrEmpty("Data");
               int $$7 = NbtUtils.getDataVersion($$6);
               Dynamic<?> $$8 = DataFixTypes.LEVEL_SUMMARY.updateToCurrentVersion(this.fixerUpper, new Dynamic(NbtOps.INSTANCE, $$6), $$7);
               return this.makeLevelSummary($$8, $$0, $$1);
            }

            LOGGER.warn("Invalid root tag in {}", $$2);
         } catch (Exception var9) {
            LOGGER.error("Exception reading {}", $$2, var9);
         }
      }

      return new LevelSummary.CorruptedLevelSummary($$0.directoryName(), $$0.iconFile(), getFileModificationTime($$0));
   }

   private static long getFileModificationTime(LevelStorageSource.LevelDirectory $$0) {
      Instant $$1 = getFileModificationTime($$0.dataFile());
      if ($$1 == null) {
         $$1 = getFileModificationTime($$0.oldDataFile());
      }

      return $$1 == null ? -1L : $$1.toEpochMilli();
   }

   @Nullable
   static Instant getFileModificationTime(Path $$0) {
      try {
         return Files.getLastModifiedTime($$0).toInstant();
      } catch (IOException var2) {
         return null;
      }
   }

   LevelSummary makeLevelSummary(Dynamic<?> $$0, LevelStorageSource.LevelDirectory $$1, boolean $$2) {
      LevelVersion $$3 = LevelVersion.parse($$0);
      int $$4 = $$3.levelDataVersion();
      if ($$4 != 19132 && $$4 != 19133) {
         throw new NbtFormatException("Unknown data version: " + Integer.toHexString($$4));
      } else {
         boolean $$5 = $$4 != this.getStorageVersion();
         Path $$6 = $$1.iconFile();
         net.minecraft.world.level.WorldDataConfiguration $$7 = readDataConfig($$0);
         net.minecraft.world.level.LevelSettings $$8 = net.minecraft.world.level.LevelSettings.parse($$0, $$7);
         FeatureFlagSet $$9 = parseFeatureFlagsFromSummary($$0);
         boolean $$10 = FeatureFlags.isExperimental($$9);
         return new LevelSummary($$8, $$3, $$1.directoryName(), $$5, $$2, $$10, $$6);
      }
   }

   private static FeatureFlagSet parseFeatureFlagsFromSummary(Dynamic<?> $$0) {
      Set<Identifier> $$1 = $$0.get("enabled_features")
         .asStream()
         .flatMap($$0x -> $$0x.asString().result().map(Identifier::tryParse).stream())
         .collect(Collectors.toSet());
      return FeatureFlags.REGISTRY.fromNames($$1, $$0x -> {});
   }

   @Nullable
   private static Tag readLightweightData(Path $$0) throws IOException {
      SkipFields $$1 = new SkipFields(
         new FieldSelector[]{new FieldSelector("Data", CompoundTag.TYPE, "Player"), new FieldSelector("Data", CompoundTag.TYPE, "WorldGenSettings")}
      );
      NbtIo.parseCompressed($$0, $$1, NbtAccounter.uncompressedQuota());
      return $$1.getResult();
   }

   public boolean isNewLevelIdAcceptable(String $$0) {
      try {
         Path $$1 = this.getLevelPath($$0);
         Files.createDirectory($$1);
         Files.deleteIfExists($$1);
         return true;
      } catch (IOException var3) {
         return false;
      }
   }

   public boolean levelExists(String $$0) {
      try {
         return Files.isDirectory(this.getLevelPath($$0));
      } catch (InvalidPathException var3) {
         return false;
      }
   }

   public Path getLevelPath(String $$0) {
      return this.baseDir.resolve($$0);
   }

   public Path getBaseDir() {
      return this.baseDir;
   }

   public Path getBackupPath() {
      return this.backupDir;
   }

   public LevelStorageSource.LevelStorageAccess validateAndCreateAccess(String $$0) throws IOException, ContentValidationException {
      Path $$1 = this.getLevelPath($$0);
      List<ForbiddenSymlinkInfo> $$2 = this.worldDirValidator.validateDirectory($$1, true);
      if (!$$2.isEmpty()) {
         throw new ContentValidationException($$1, $$2);
      } else {
         return new LevelStorageSource.LevelStorageAccess($$0, $$1);
      }
   }

   public LevelStorageSource.LevelStorageAccess createAccess(String $$0) throws IOException {
      Path $$1 = this.getLevelPath($$0);
      return new LevelStorageSource.LevelStorageAccess($$0, $$1);
   }

   public DirectoryValidator getWorldDirValidator() {
      return this.worldDirValidator;
   }

   public record LevelCandidates(List<LevelStorageSource.LevelDirectory> levels) implements Iterable<LevelStorageSource.LevelDirectory> {

      public boolean isEmpty() {
         return this.levels.isEmpty();
      }

      @Override
      public Iterator<LevelStorageSource.LevelDirectory> iterator() {
         return this.levels.iterator();
      }
   }

   public record LevelDirectory(Path path) {

      public String directoryName() {
         return this.path.getFileName().toString();
      }

      public Path dataFile() {
         return this.resourcePath(LevelResource.LEVEL_DATA_FILE);
      }

      public Path oldDataFile() {
         return this.resourcePath(LevelResource.OLD_LEVEL_DATA_FILE);
      }

      public Path corruptedDataFile(ZonedDateTime $$0) {
         return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_corrupted_" + $$0.format(FileNameDateFormatter.FORMATTER));
      }

      public Path rawDataFile(ZonedDateTime $$0) {
         return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_raw_" + $$0.format(FileNameDateFormatter.FORMATTER));
      }

      public Path iconFile() {
         return this.resourcePath(LevelResource.ICON_FILE);
      }

      public Path lockFile() {
         return this.resourcePath(LevelResource.LOCK_FILE);
      }

      public Path resourcePath(LevelResource $$0) {
         return this.path.resolve($$0.getId());
      }
   }

   public class LevelStorageAccess implements AutoCloseable {
      final DirectoryLock lock;
      final LevelStorageSource.LevelDirectory levelDirectory;
      private final String levelId;
      private final Map<LevelResource, Path> resources = Maps.newHashMap();

      LevelStorageAccess(final String $$1, final Path $$2) throws IOException {
         this.levelId = $$1;
         this.levelDirectory = new LevelStorageSource.LevelDirectory($$2);
         this.lock = DirectoryLock.create($$2);
      }

      public long estimateDiskSpace() {
         try {
            return Files.getFileStore(this.levelDirectory.path).getUsableSpace();
         } catch (Exception var2) {
            return Long.MAX_VALUE;
         }
      }

      public boolean checkForLowDiskSpace() {
         return this.estimateDiskSpace() < 67108864L;
      }

      public void safeClose() {
         try {
            this.close();
         } catch (IOException var2) {
            LevelStorageSource.LOGGER.warn("Failed to unlock access to level {}", this.getLevelId(), var2);
         }
      }

      public LevelStorageSource parent() {
         return LevelStorageSource.this;
      }

      public LevelStorageSource.LevelDirectory getLevelDirectory() {
         return this.levelDirectory;
      }

      public String getLevelId() {
         return this.levelId;
      }

      public Path getLevelPath(LevelResource $$0) {
         return this.resources.computeIfAbsent($$0, this.levelDirectory::resourcePath);
      }

      public Path getDimensionPath(ResourceKey<net.minecraft.world.level.Level> $$0) {
         return DimensionType.getStorageFolder($$0, this.levelDirectory.path());
      }

      private void checkLock() {
         if (!this.lock.isValid()) {
            throw new IllegalStateException("Lock is no longer valid");
         }
      }

      public PlayerDataStorage createPlayerStorage() {
         this.checkLock();
         return new PlayerDataStorage(this, LevelStorageSource.this.fixerUpper);
      }

      public LevelSummary getSummary(Dynamic<?> $$0) {
         this.checkLock();
         return LevelStorageSource.this.makeLevelSummary($$0, this.levelDirectory, false);
      }

      public Dynamic<?> getDataTag() throws IOException {
         return this.getDataTag(false);
      }

      public Dynamic<?> getDataTagFallback() throws IOException {
         return this.getDataTag(true);
      }

      private Dynamic<?> getDataTag(boolean $$0) throws IOException {
         this.checkLock();
         return LevelStorageSource.readLevelDataTagFixed(
            $$0 ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile(), LevelStorageSource.this.fixerUpper
         );
      }

      public void saveDataTag(RegistryAccess $$0, WorldData $$1) {
         this.saveDataTag($$0, $$1, null);
      }

      public void saveDataTag(RegistryAccess $$0, WorldData $$1, @Nullable CompoundTag $$2) {
         CompoundTag $$3 = $$1.createTag($$0, $$2);
         CompoundTag $$4 = new CompoundTag();
         $$4.put("Data", $$3);
         this.saveLevelData($$4);
      }

      private void saveLevelData(CompoundTag $$0) {
         Path $$1 = this.levelDirectory.path();

         try {
            Path $$2 = Files.createTempFile($$1, "level", ".dat");
            NbtIo.writeCompressed($$0, $$2);
            Path $$3 = this.levelDirectory.oldDataFile();
            Path $$4 = this.levelDirectory.dataFile();
            Util.safeReplaceFile($$4, $$2, $$3);
         } catch (Exception var6) {
            LevelStorageSource.LOGGER.error("Failed to save level {}", $$1, var6);
         }
      }

      public Optional<Path> getIconFile() {
         return !this.lock.isValid() ? Optional.empty() : Optional.of(this.levelDirectory.iconFile());
      }

      public void deleteLevel() throws IOException {
         this.checkLock();
         final Path $$0 = this.levelDirectory.lockFile();
         LevelStorageSource.LOGGER.info("Deleting level {}", this.levelId);

         for (int $$1 = 1; $$1 <= 5; $$1++) {
            LevelStorageSource.LOGGER.info("Attempt {}...", $$1);

            try {
               Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
                  public FileVisitResult visitFile(Path $$0x, BasicFileAttributes $$1x) throws IOException {
                     if (!$$0.equals($$0)) {
                        LevelStorageSource.LOGGER.debug("Deleting {}", $$0);
                        Files.delete($$0);
                     }

                     return FileVisitResult.CONTINUE;
                  }

                  public FileVisitResult postVisitDirectory(Path $$0x, @Nullable IOException $$1x) throws IOException {
                     if ($$1x != null) {
                        throw $$1x;
                     } else {
                        if ($$0.equals(LevelStorageAccess.this.levelDirectory.path())) {
                           LevelStorageAccess.this.lock.close();
                           Files.deleteIfExists($$0);
                        }

                        Files.delete($$0);
                        return FileVisitResult.CONTINUE;
                     }
                  }
               });
               break;
            } catch (IOException var6) {
               if ($$1 >= 5) {
                  throw var6;
               }

               LevelStorageSource.LOGGER.warn("Failed to delete {}", this.levelDirectory.path(), var6);

               try {
                  Thread.sleep(500L);
               } catch (InterruptedException var5) {
               }
            }
         }
      }

      public void renameLevel(String $$0) throws IOException {
         this.modifyLevelDataWithoutDatafix($$1 -> $$1.putString("LevelName", $$0.trim()));
      }

      public void renameAndDropPlayer(String $$0) throws IOException {
         this.modifyLevelDataWithoutDatafix($$1 -> {
            $$1.putString("LevelName", $$0.trim());
            $$1.remove("Player");
         });
      }

      private void modifyLevelDataWithoutDatafix(Consumer<CompoundTag> $$0) throws IOException {
         this.checkLock();
         CompoundTag $$1 = LevelStorageSource.readLevelDataTagRaw(this.levelDirectory.dataFile());
         $$0.accept($$1.getCompoundOrEmpty("Data"));
         this.saveLevelData($$1);
      }

      public long makeWorldBackup() throws IOException {
         this.checkLock();
         String $$0 = FileNameDateFormatter.FORMATTER.format(ZonedDateTime.now()) + "_" + this.levelId;
         Path $$1 = LevelStorageSource.this.getBackupPath();

         try {
            FileUtil.createDirectoriesSafe($$1);
         } catch (IOException var9) {
            throw new RuntimeException(var9);
         }

         Path $$3 = $$1.resolve(FileUtil.findAvailableName($$1, $$0, ".zip"));

         try (final ZipOutputStream $$4 = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream($$3)))) {
            final Path $$5 = Paths.get(this.levelId);
            Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
               public FileVisitResult visitFile(Path $$0, BasicFileAttributes $$1x) throws IOException {
                  if ($$0.endsWith("session.lock")) {
                     return FileVisitResult.CONTINUE;
                  } else {
                     String $$2 = $$5.resolve(LevelStorageAccess.this.levelDirectory.path().relativize($$0)).toString().replace('\\', '/');
                     ZipEntry $$3x = new ZipEntry($$2);
                     $$4.putNextEntry($$3x);
                     com.google.common.io.Files.asByteSource($$0.toFile()).copyTo($$4);
                     $$4.closeEntry();
                     return FileVisitResult.CONTINUE;
                  }
               }
            });
         }

         return Files.size($$3);
      }

      public boolean hasWorldData() {
         return Files.exists(this.levelDirectory.dataFile()) || Files.exists(this.levelDirectory.oldDataFile());
      }

      @Override
      public void close() throws IOException {
         this.lock.close();
      }

      public boolean restoreLevelDataFromOld() {
         return Util.safeReplaceOrMoveFile(
            this.levelDirectory.dataFile(), this.levelDirectory.oldDataFile(), this.levelDirectory.corruptedDataFile(ZonedDateTime.now()), true
         );
      }

      @Nullable
      public Instant getFileModificationTime(boolean $$0) {
         return LevelStorageSource.getFileModificationTime($$0 ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile());
      }
   }
}
