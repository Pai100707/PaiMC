package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.FileUtil;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class VanillaPackResources implements PackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final PackLocationInfo location;
   private final BuiltInMetadata metadata;
   private final Set<String> namespaces;
   private final List<Path> rootPaths;
   private final Map<PackType, List<Path>> pathsForType;

   VanillaPackResources(PackLocationInfo $$0, BuiltInMetadata $$1, Set<String> $$2, List<Path> $$3, Map<PackType, List<Path>> $$4) {
      this.location = $$0;
      this.metadata = $$1;
      this.namespaces = $$2;
      this.rootPaths = $$3;
      this.pathsForType = $$4;
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getRootResource(String... $$0) {
      FileUtil.validatePath($$0);
      List<String> $$1 = List.of($$0);

      for (Path $$2 : this.rootPaths) {
         Path $$3 = FileUtil.resolvePath($$2, $$1);
         if (Files.exists($$3) && PathPackResources.validatePath($$3)) {
            return IoSupplier.create($$3);
         }
      }

      return null;
   }

   public void listRawPaths(PackType $$0, Identifier $$1, Consumer<Path> $$2) {
      FileUtil.decomposePath($$1.getPath()).ifSuccess($$3 -> {
         String $$4 = $$1.getNamespace();

         for (Path $$5 : this.pathsForType.get($$0)) {
            Path $$6 = $$5.resolve($$4);
            $$2.accept(FileUtil.resolvePath($$6, $$3));
         }
      }).ifError($$1x -> LOGGER.error("Invalid path {}: {}", $$1, $$1x.message()));
   }

   @Override
   public void listResources(PackType $$0, String $$1, String $$2, PackResources.ResourceOutput $$3) {
      FileUtil.decomposePath($$2).ifSuccess($$3x -> {
         List<Path> $$4 = this.pathsForType.get($$0);
         int $$5 = $$4.size();
         if ($$5 == 1) {
            getResources($$3, $$1, $$4.get(0), $$3x);
         } else if ($$5 > 1) {
            Map<Identifier, IoSupplier<InputStream>> $$6 = new HashMap<>();

            for (int $$7 = 0; $$7 < $$5 - 1; $$7++) {
               getResources($$6::putIfAbsent, $$1, $$4.get($$7), $$3x);
            }

            Path $$8 = $$4.get($$5 - 1);
            if ($$6.isEmpty()) {
               getResources($$3, $$1, $$8, $$3x);
            } else {
               getResources($$6::putIfAbsent, $$1, $$8, $$3x);
               $$6.forEach($$3);
            }
         }
      }).ifError($$1x -> LOGGER.error("Invalid path {}: {}", $$2, $$1x.message()));
   }

   private static void getResources(PackResources.ResourceOutput $$0, String $$1, Path $$2, List<String> $$3) {
      Path $$4 = $$2.resolve($$1);
      PathPackResources.listPath($$1, $$4, $$3, $$0);
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getResource(PackType $$0, Identifier $$1) {
      return (IoSupplier<InputStream>)FileUtil.decomposePath($$1.getPath()).mapOrElse($$2 -> {
         String $$3 = $$1.getNamespace();

         for (Path $$4 : this.pathsForType.get($$0)) {
            Path $$5 = FileUtil.resolvePath($$4.resolve($$3), $$2);
            if (Files.exists($$5) && PathPackResources.validatePath($$5)) {
               return IoSupplier.create($$5);
            }
         }

         return null;
      }, $$1x -> {
         LOGGER.error("Invalid path {}: {}", $$1, $$1x.message());
         return null;
      });
   }

   @Override
   public Set<String> getNamespaces(PackType $$0) {
      return this.namespaces;
   }

   @Nullable
   @Override
   public <T> T getMetadataSection(MetadataSectionType<T> $$0) {
      IoSupplier<InputStream> $$1 = this.getRootResource("pack.mcmeta");
      if ($$1 != null) {
         try (InputStream $$2 = $$1.get()) {
            T $$3 = AbstractPackResources.getMetadataFromStream($$0, $$2, this.location);
            if ($$3 != null) {
               return $$3;
            }

            return this.metadata.get($$0);
         } catch (IOException var8) {
         }
      }

      return this.metadata.get($$0);
   }

   @Override
   public PackLocationInfo location() {
      return this.location;
   }

   @Override
   public void close() {
   }

   public ResourceProvider asProvider() {
      return $$0 -> Optional.ofNullable(this.getResource(PackType.CLIENT_RESOURCES, $$0)).map($$0x -> new Resource(this, $$0x));
   }
}
