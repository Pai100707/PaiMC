package net.minecraft.server.packs;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.FileUtil;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class PathPackResources extends AbstractPackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Joiner PATH_JOINER = Joiner.on("/");
   private final Path root;

   public PathPackResources(PackLocationInfo $$0, Path $$1) {
      super($$0);
      this.root = $$1;
   }

   
   @Override
   public IoSupplier<InputStream> getRootResource(String... $$0) {
      FileUtil.validatePath($$0);
      Path $$1 = FileUtil.resolvePath(this.root, List.of($$0));
      return Files.exists($$1) ? IoSupplier.create($$1) : null;
   }

   public static boolean validatePath(Path $$0) {
      if (!SharedConstants.DEBUG_VALIDATE_RESOURCE_PATH_CASE) {
         return true;
      } else if ($$0.getFileSystem() != FileSystems.getDefault()) {
         return true;
      } else {
         try {
            return $$0.toRealPath().endsWith($$0);
         } catch (IOException var2) {
            LOGGER.warn("Failed to resolve real path for {}", $$0, var2);
            return false;
         }
      }
   }

   
   @Override
   public IoSupplier<InputStream> getResource(PackType $$0, Identifier $$1) {
      Path $$2 = this.root.resolve($$0.getDirectory()).resolve($$1.getNamespace());
      return getResource($$1, $$2);
   }

   
   public static IoSupplier<InputStream> getResource(Identifier $$0, Path $$1) {
      return (IoSupplier<InputStream>)FileUtil.decomposePath($$0.getPath()).mapOrElse($$1x -> {
         Path $$2 = FileUtil.resolvePath($$1, $$1x);
         return returnFileIfExists($$2);
      }, $$1x -> {
         LOGGER.error("Invalid path {}: {}", $$0, $$1x.message());
         return null;
      });
   }

   
   private static IoSupplier<InputStream> returnFileIfExists(Path $$0) {
      return Files.exists($$0) && validatePath($$0) ? IoSupplier.create($$0) : null;
   }

   @Override
   public void listResources(PackType $$0, String $$1, String $$2, PackResources.ResourceOutput $$3) {
      FileUtil.decomposePath($$2).ifSuccess($$3x -> {
         Path $$4 = this.root.resolve($$0.getDirectory()).resolve($$1);
         listPath($$1, $$4, $$3x, $$3);
      }).ifError($$1x -> LOGGER.error("Invalid path {}: {}", $$2, $$1x.message()));
   }

   public static void listPath(String $$0, Path $$1, List<String> $$2, PackResources.ResourceOutput $$3) {
      Path $$4 = FileUtil.resolvePath($$1, $$2);

      try (Stream<Path> $$5 = Files.find($$4, Integer.MAX_VALUE, PathPackResources::isRegularFile)) {
         $$5.forEach($$3x -> {
            String $$4x = PATH_JOINER.join($$1.relativize($$3x));
            Identifier $$5x = Identifier.tryBuild($$0, $$4x);
            if ($$5x == null) {
               Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", $$0, $$4x));
            } else {
               $$3.accept($$5x, IoSupplier.create($$3x));
            }
         });
      } catch (NotDirectoryException | NoSuchFileException var10) {
      } catch (IOException var11) {
         LOGGER.error("Failed to list path {}", $$4, var11);
      }
   }

   private static boolean isRegularFile(Path $$0, BasicFileAttributes $$1) {
      return !SharedConstants.IS_RUNNING_IN_IDE
         ? $$1.isRegularFile()
         : $$1.isRegularFile() && !StringUtils.equalsIgnoreCase($$0.getFileName().toString(), ".ds_store");
   }

   @Override
   public Set<String> getNamespaces(PackType $$0) {
      Set<String> $$1 = Sets.newHashSet();
      Path $$2 = this.root.resolve($$0.getDirectory());

      try (DirectoryStream<Path> $$3 = Files.newDirectoryStream($$2)) {
         for (Path $$4 : $$3) {
            String $$5 = $$4.getFileName().toString();
            if (Identifier.isValidNamespace($$5)) {
               $$1.add($$5);
            } else {
               LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", $$5, this.root);
            }
         }
      } catch (NotDirectoryException | NoSuchFileException var10) {
      } catch (IOException var11) {
         LOGGER.error("Failed to list path {}", $$2, var11);
      }

      return $$1;
   }

   @Override
   public void close() {
   }

   public static class PathResourcesSupplier implements Pack.ResourcesSupplier {
      private final Path content;

      public PathResourcesSupplier(Path $$0) {
         this.content = $$0;
      }

      @Override
      public PackResources openPrimary(PackLocationInfo $$0) {
         return new PathPackResources($$0, this.content);
      }

      @Override
      public PackResources openFull(PackLocationInfo $$0, Pack.Metadata $$1) {
         PackResources $$2 = this.openPrimary($$0);
         List<String> $$3 = $$1.overlays();
         if ($$3.isEmpty()) {
            return $$2;
         } else {
            List<PackResources> $$4 = new ArrayList<>($$3.size());

            for (String $$5 : $$3) {
               Path $$6 = this.content.resolve($$5);
               $$4.add(new PathPackResources($$0, $$6));
            }

            return new CompositePackResources($$2, $$4);
         }
      }
   }
}
