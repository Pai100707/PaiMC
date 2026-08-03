package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.IdentifierException;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.FileUtil;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureTemplateManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String STRUCTURE_RESOURCE_DIRECTORY_NAME = "structure";
   private static final String STRUCTURE_GENERATED_DIRECTORY_NAME = "structures";
   private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
   private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
   private final Map<Identifier, Optional<StructureTemplate>> structureRepository = Maps.newConcurrentMap();
   private final DataFixer fixerUpper;
   private ResourceManager resourceManager;
   private final Path generatedDir;
   private final List<StructureTemplateManager.Source> sources;
   private final HolderGetter<Block> blockLookup;
   private static final FileToIdConverter RESOURCE_LISTER = new FileToIdConverter("structure", ".nbt");

   public StructureTemplateManager(ResourceManager $$0, LevelStorageSource.LevelStorageAccess $$1, DataFixer $$2, HolderGetter<Block> $$3) {
      this.resourceManager = $$0;
      this.fixerUpper = $$2;
      this.generatedDir = $$1.getLevelPath(LevelResource.GENERATED_DIR).normalize();
      this.blockLookup = $$3;
      Builder<StructureTemplateManager.Source> $$4 = ImmutableList.builder();
      $$4.add(new StructureTemplateManager.Source(this::loadFromGenerated, this::listGenerated));
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         $$4.add(new StructureTemplateManager.Source(this::loadFromTestStructures, this::listTestStructures));
      }

      $$4.add(new StructureTemplateManager.Source(this::loadFromResource, this::listResources));
      this.sources = $$4.build();
   }

   public StructureTemplate getOrCreate(Identifier $$0) {
      Optional<StructureTemplate> $$1 = this.get($$0);
      if ($$1.isPresent()) {
         return $$1.get();
      } else {
         StructureTemplate $$2 = new StructureTemplate();
         this.structureRepository.put($$0, Optional.of($$2));
         return $$2;
      }
   }

   public Optional<StructureTemplate> get(Identifier $$0) {
      return this.structureRepository.computeIfAbsent($$0, this::tryLoad);
   }

   public Stream<Identifier> listTemplates() {
      return this.sources.stream().flatMap($$0 -> $$0.lister().get()).distinct();
   }

   private Optional<StructureTemplate> tryLoad(Identifier $$0) {
      for (StructureTemplateManager.Source $$1 : this.sources) {
         try {
            Optional<StructureTemplate> $$2 = $$1.loader().apply($$0);
            if ($$2.isPresent()) {
               return $$2;
            }
         } catch (Exception var5) {
         }
      }

      return Optional.empty();
   }

   public void onResourceManagerReload(ResourceManager $$0) {
      this.resourceManager = $$0;
      this.structureRepository.clear();
   }

   private Optional<StructureTemplate> loadFromResource(Identifier $$0) {
      Identifier $$1 = RESOURCE_LISTER.idToFile($$0);
      return this.load(() -> this.resourceManager.open($$1), $$1x -> LOGGER.error("Couldn't load structure {}", $$0, $$1x));
   }

   private Stream<Identifier> listResources() {
      return RESOURCE_LISTER.listMatchingResources(this.resourceManager).keySet().stream().map(RESOURCE_LISTER::fileToId);
   }

   private Optional<StructureTemplate> loadFromTestStructures(Identifier $$0) {
      return this.loadFromSnbt($$0, StructureUtils.testStructuresDir);
   }

   private Stream<Identifier> listTestStructures() {
      if (!Files.isDirectory(StructureUtils.testStructuresDir)) {
         return Stream.empty();
      } else {
         List<Identifier> $$0 = new ArrayList<>();
         this.listFolderContents(StructureUtils.testStructuresDir, "minecraft", ".snbt", $$0::add);
         return $$0.stream();
      }
   }

   private Optional<StructureTemplate> loadFromGenerated(Identifier $$0) {
      if (!Files.isDirectory(this.generatedDir)) {
         return Optional.empty();
      } else {
         Path $$1 = this.createAndValidatePathToGeneratedStructure($$0, ".nbt");
         return this.load(() -> new FileInputStream($$1.toFile()), $$1x -> LOGGER.error("Couldn't load structure from {}", $$1, $$1x));
      }
   }

   private Stream<Identifier> listGenerated() {
      if (!Files.isDirectory(this.generatedDir)) {
         return Stream.empty();
      } else {
         try {
            List<Identifier> $$0 = new ArrayList<>();

            try (DirectoryStream<Path> $$1 = Files.newDirectoryStream(this.generatedDir, $$0x -> Files.isDirectory($$0x))) {
               for (Path $$2 : $$1) {
                  String $$3 = $$2.getFileName().toString();
                  Path $$4 = $$2.resolve("structures");
                  this.listFolderContents($$4, $$3, ".nbt", $$0::add);
               }
            }

            return $$0.stream();
         } catch (IOException var9) {
            return Stream.empty();
         }
      }
   }

   private void listFolderContents(Path $$0, String $$1, String $$2, Consumer<Identifier> $$3) {
      int $$4 = $$2.length();
      Function<String, String> $$5 = $$1x -> $$1x.substring(0, $$1x.length() - $$4);

      try (Stream<Path> $$6 = Files.find($$0, Integer.MAX_VALUE, ($$1x, $$2x) -> $$2x.isRegularFile() && $$1x.toString().endsWith($$2))) {
         $$6.forEach($$4x -> {
            try {
               $$3.accept(Identifier.fromNamespaceAndPath($$1, $$5.apply(this.relativize($$0, $$4x))));
            } catch (IdentifierException var7x) {
               LOGGER.error("Invalid location while listing folder {} contents", $$0, var7x);
            }
         });
      } catch (IOException var12) {
         LOGGER.error("Failed to list folder {} contents", $$0, var12);
      }
   }

   private String relativize(Path $$0, Path $$1) {
      return $$0.relativize($$1).toString().replace(File.separator, "/");
   }

   private Optional<StructureTemplate> loadFromSnbt(Identifier $$0, Path $$1) {
      if (!Files.isDirectory($$1)) {
         return Optional.empty();
      } else {
         Path $$2 = FileUtil.createPathToResource($$1, $$0.getPath(), ".snbt");

         try {
            Optional var6;
            try (BufferedReader $$3 = Files.newBufferedReader($$2)) {
               String $$4 = IOUtils.toString($$3);
               var6 = Optional.of(this.readStructure(NbtUtils.snbtToStructure($$4)));
            }

            return var6;
         } catch (NoSuchFileException var9) {
            return Optional.empty();
         } catch (CommandSyntaxException | IOException var10) {
            LOGGER.error("Couldn't load structure from {}", $$2, var10);
            return Optional.empty();
         }
      }
   }

   private Optional<StructureTemplate> load(StructureTemplateManager.InputStreamOpener $$0, Consumer<Throwable> $$1) {
      try {
         Optional var5;
         try (
            InputStream $$2 = $$0.open();
            InputStream $$3 = new FastBufferedInputStream($$2);
         ) {
            var5 = Optional.of(this.readStructure($$3));
         }

         return var5;
      } catch (FileNotFoundException var11) {
         return Optional.empty();
      } catch (Throwable var12) {
         $$1.accept(var12);
         return Optional.empty();
      }
   }

   private StructureTemplate readStructure(InputStream $$0) throws IOException {
      CompoundTag $$1 = NbtIo.readCompressed($$0, NbtAccounter.unlimitedHeap());
      return this.readStructure($$1);
   }

   public StructureTemplate readStructure(CompoundTag $$0) {
      StructureTemplate $$1 = new StructureTemplate();
      int $$2 = NbtUtils.getDataVersion($$0, 500);
      $$1.load(this.blockLookup, DataFixTypes.STRUCTURE.updateToCurrentVersion(this.fixerUpper, $$0, $$2));
      return $$1;
   }

   public boolean save(Identifier $$0) {
      Optional<StructureTemplate> $$1 = this.structureRepository.get($$0);
      if ($$1.isEmpty()) {
         return false;
      } else {
         StructureTemplate $$2 = $$1.get();
         Path $$3 = this.createAndValidatePathToGeneratedStructure($$0, SharedConstants.DEBUG_SAVE_STRUCTURES_AS_SNBT ? ".snbt" : ".nbt");
         Path $$4 = $$3.getParent();
         if ($$4 == null) {
            return false;
         } else {
            try {
               Files.createDirectories(Files.exists($$4) ? $$4.toRealPath() : $$4);
            } catch (IOException var14) {
               LOGGER.error("Failed to create parent directory: {}", $$4);
               return false;
            }

            CompoundTag $$6 = $$2.save(new CompoundTag());
            if (SharedConstants.DEBUG_SAVE_STRUCTURES_AS_SNBT) {
               try {
                  NbtToSnbt.writeSnbt(CachedOutput.NO_CACHE, $$3, NbtUtils.structureToSnbt($$6));
               } catch (Throwable var13) {
                  return false;
               }
            } else {
               try (OutputStream $$8 = new FileOutputStream($$3.toFile())) {
                  NbtIo.writeCompressed($$6, $$8);
               } catch (Throwable var12) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public Path createAndValidatePathToGeneratedStructure(Identifier $$0, String $$1) {
      if ($$0.getPath().contains("//")) {
         throw new IdentifierException("Invalid resource path: " + $$0);
      } else {
         try {
            Path $$2 = this.generatedDir.resolve($$0.getNamespace());
            Path $$3 = $$2.resolve("structures");
            Path $$4 = FileUtil.createPathToResource($$3, $$0.getPath(), $$1);
            if ($$4.startsWith(this.generatedDir) && FileUtil.isPathNormalized($$4) && FileUtil.isPathPortable($$4)) {
               return $$4;
            } else {
               throw new IdentifierException("Invalid resource path: " + $$4);
            }
         } catch (InvalidPathException var6) {
            throw new IdentifierException("Invalid resource path: " + $$0, var6);
         }
      }
   }

   public void remove(Identifier $$0) {
      this.structureRepository.remove($$0);
   }

   @FunctionalInterface
   interface InputStreamOpener {
      InputStream open() throws IOException;
   }

   record Source(Function<Identifier, Optional<StructureTemplate>> loader, Supplier<Stream<Identifier>> lister) {
   }
}
