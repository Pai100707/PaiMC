package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.util.FileUtil;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FolderRepositorySource implements RepositorySource {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final PackSelectionConfig DISCOVERED_PACK_SELECTION_CONFIG = new PackSelectionConfig(false, Pack.Position.TOP, false);
   private final Path folder;
   private final PackType packType;
   private final PackSource packSource;
   private final DirectoryValidator validator;

   public FolderRepositorySource(Path $$0, PackType $$1, PackSource $$2, DirectoryValidator $$3) {
      this.folder = $$0;
      this.packType = $$1;
      this.packSource = $$2;
      this.validator = $$3;
   }

   private static String nameFromPath(Path $$0) {
      return $$0.getFileName().toString();
   }

   @Override
   public void loadPacks(Consumer<Pack> $$0) {
      try {
         FileUtil.createDirectoriesSafe(this.folder);
         discoverPacks(this.folder, this.validator, ($$1, $$2) -> {
            PackLocationInfo $$3 = this.createDiscoveredFilePackInfo($$1);
            Pack $$4 = Pack.readMetaAndCreate($$3, $$2, this.packType, DISCOVERED_PACK_SELECTION_CONFIG);
            if ($$4 != null) {
               $$0.accept($$4);
            }
         });
      } catch (IOException var3) {
         LOGGER.warn("Failed to list packs in {}", this.folder, var3);
      }
   }

   private PackLocationInfo createDiscoveredFilePackInfo(Path $$0) {
      String $$1 = nameFromPath($$0);
      return new PackLocationInfo("file/" + $$1, Component.literal($$1), this.packSource, Optional.empty());
   }

   public static void discoverPacks(Path $$0, DirectoryValidator $$1, BiConsumer<Path, Pack.ResourcesSupplier> $$2) throws IOException {
      FolderRepositorySource.FolderPackDetector $$3 = new FolderRepositorySource.FolderPackDetector($$1);

      try (DirectoryStream<Path> $$4 = Files.newDirectoryStream($$0)) {
         for (Path $$5 : $$4) {
            try {
               List<ForbiddenSymlinkInfo> $$6 = new ArrayList<>();
               Pack.ResourcesSupplier $$7 = $$3.detectPackResources($$5, $$6);
               if (!$$6.isEmpty()) {
                  LOGGER.warn("Ignoring potential pack entry: {}", ContentValidationException.getMessage($$5, $$6));
               } else if ($$7 != null) {
                  $$2.accept($$5, $$7);
               } else {
                  LOGGER.info("Found non-pack entry '{}', ignoring", $$5);
               }
            } catch (IOException var10) {
               LOGGER.warn("Failed to read properties of '{}', ignoring", $$5, var10);
            }
         }
      }
   }

   static class FolderPackDetector extends PackDetector<Pack.ResourcesSupplier> {
      protected FolderPackDetector(DirectoryValidator $$0) {
         super($$0);
      }

      @Nullable
      protected Pack.ResourcesSupplier createZipPack(Path $$0) {
         FileSystem $$1 = $$0.getFileSystem();
         if ($$1 != FileSystems.getDefault() && !($$1 instanceof LinkFileSystem)) {
            FolderRepositorySource.LOGGER.info("Can't open pack archive at {}", $$0);
            return null;
         } else {
            return new FilePackResources.FileResourcesSupplier($$0);
         }
      }

      protected Pack.ResourcesSupplier createDirectoryPack(Path $$0) {
         return new PathPackResources.PathResourcesSupplier($$0);
      }
   }
}
