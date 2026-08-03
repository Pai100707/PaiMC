package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.util.FileSystemUtil;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class VanillaPackResourcesBuilder {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static Consumer<VanillaPackResourcesBuilder> developmentConfig = $$0 -> {};
   private static final Map<PackType, Path> ROOT_DIR_BY_TYPE = (Map<PackType, Path>)Util.make(() -> {
      synchronized (VanillaPackResources.class) {
         Builder<PackType, Path> $$0 = ImmutableMap.builder();

         for (PackType $$1 : PackType.values()) {
            String $$2 = "/" + $$1.getDirectory() + "/.mcassetsroot";
            URL $$3 = VanillaPackResources.class.getResource($$2);
            if ($$3 == null) {
               LOGGER.error("File {} does not exist in classpath", $$2);
            } else {
               try {
                  URI $$4 = $$3.toURI();
                  String $$5 = $$4.getScheme();
                  if (!"jar".equals($$5) && !"file".equals($$5)) {
                     LOGGER.warn("Assets URL '{}' uses unexpected schema", $$4);
                  }

                  Path $$6 = FileSystemUtil.safeGetPath($$4);
                  $$0.put($$1, $$6.getParent());
               } catch (Exception var12) {
                  LOGGER.error("Couldn't resolve path to vanilla assets", var12);
               }
            }
         }

         return $$0.build();
      }
   });
   private final Set<Path> rootPaths = new LinkedHashSet<>();
   private final Map<PackType, Set<Path>> pathsForType = new EnumMap<>(PackType.class);
   private BuiltInMetadata metadata = BuiltInMetadata.of();
   private final Set<String> namespaces = new HashSet<>();

   private boolean validateDirPath(Path $$0) {
      if (!Files.exists($$0)) {
         return false;
      } else if (!Files.isDirectory($$0)) {
         throw new IllegalArgumentException("Path " + $$0.toAbsolutePath() + " is not directory");
      } else {
         return true;
      }
   }

   private void pushRootPath(Path $$0) {
      if (this.validateDirPath($$0)) {
         this.rootPaths.add($$0);
      }
   }

   private void pushPathForType(PackType $$0, Path $$1) {
      if (this.validateDirPath($$1)) {
         this.pathsForType.computeIfAbsent($$0, $$0x -> new LinkedHashSet<>()).add($$1);
      }
   }

   public VanillaPackResourcesBuilder pushJarResources() {
      ROOT_DIR_BY_TYPE.forEach(($$0, $$1) -> {
         this.pushRootPath($$1.getParent());
         this.pushPathForType($$0, $$1);
      });
      return this;
   }

   public VanillaPackResourcesBuilder pushClasspathResources(PackType $$0, Class<?> $$1) {
      Enumeration<URL> $$2 = null;

      try {
         $$2 = $$1.getClassLoader().getResources($$0.getDirectory() + "/");
      } catch (IOException var8) {
      }

      while ($$2 != null && $$2.hasMoreElements()) {
         URL $$3 = $$2.nextElement();

         try {
            URI $$4 = $$3.toURI();
            if ("file".equals($$4.getScheme())) {
               Path $$5 = Paths.get($$4);
               this.pushRootPath($$5.getParent());
               this.pushPathForType($$0, $$5);
            }
         } catch (Exception var7) {
            LOGGER.error("Failed to extract path from {}", $$3, var7);
         }
      }

      return this;
   }

   public VanillaPackResourcesBuilder applyDevelopmentConfig() {
      developmentConfig.accept(this);
      return this;
   }

   public VanillaPackResourcesBuilder pushUniversalPath(Path $$0) {
      this.pushRootPath($$0);

      for (PackType $$1 : PackType.values()) {
         this.pushPathForType($$1, $$0.resolve($$1.getDirectory()));
      }

      return this;
   }

   public VanillaPackResourcesBuilder pushAssetPath(PackType $$0, Path $$1) {
      this.pushRootPath($$1);
      this.pushPathForType($$0, $$1);
      return this;
   }

   public VanillaPackResourcesBuilder setMetadata(BuiltInMetadata $$0) {
      this.metadata = $$0;
      return this;
   }

   public VanillaPackResourcesBuilder exposeNamespace(String... $$0) {
      this.namespaces.addAll(Arrays.asList($$0));
      return this;
   }

   public VanillaPackResources build(PackLocationInfo $$0) {
      return new VanillaPackResources(
         $$0,
         this.metadata,
         Set.copyOf(this.namespaces),
         copyAndReverse(this.rootPaths),
         Util.makeEnumMap(PackType.class, $$0x -> copyAndReverse(this.pathsForType.getOrDefault($$0x, Set.of())))
      );
   }

   private static List<Path> copyAndReverse(Collection<Path> $$0) {
      List<Path> $$1 = new ArrayList<>($$0);
      Collections.reverse($$1);
      return List.copyOf($$1);
   }
}
