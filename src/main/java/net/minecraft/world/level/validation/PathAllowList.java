package net.minecraft.world.level.validation;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

public class PathAllowList implements PathMatcher {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String COMMENT_PREFIX = "#";
   private final List<PathAllowList.ConfigEntry> entries;
   private final Map<String, PathMatcher> compiledPaths = new ConcurrentHashMap<>();

   public PathAllowList(List<PathAllowList.ConfigEntry> $$0) {
      this.entries = $$0;
   }

   public PathMatcher getForFileSystem(FileSystem $$0) {
      return this.compiledPaths.computeIfAbsent($$0.provider().getScheme(), $$1 -> {
         List<PathMatcher> $$2;
         try {
            $$2 = this.entries.stream().map($$1x -> $$1x.compile($$0)).toList();
         } catch (Exception var5) {
            LOGGER.error("Failed to compile file pattern list", var5);
            return $$0xx -> false;
         }
         return switch ($$2.size()) {
            case 0 -> $$0xx -> false;
            case 1 -> (PathMatcher)$$2.get(0);
            default -> $$1x -> {
               for (PathMatcher $$2 : $$2) {
                  if ($$2.matches($$1x)) {
                     return true;
                  }
               }

               return false;
            };
         };
      });
   }

   @Override
   public boolean matches(Path $$0) {
      return this.getForFileSystem($$0.getFileSystem()).matches($$0);
   }

   public static PathAllowList readPlain(BufferedReader $$0) {
      return new PathAllowList($$0.lines().flatMap($$0x -> PathAllowList.ConfigEntry.parse($$0x).stream()).toList());
   }

   public record ConfigEntry(PathAllowList.EntryType type, String pattern) {
      public PathMatcher compile(FileSystem $$0) {
         return this.type().compile($$0, this.pattern);
      }

      static Optional<PathAllowList.ConfigEntry> parse(String $$0) {
         if ($$0.isBlank() || $$0.startsWith("#")) {
            return Optional.empty();
         } else if (!$$0.startsWith("[")) {
            return Optional.of(new PathAllowList.ConfigEntry(PathAllowList.EntryType.PREFIX, $$0));
         } else {
            int $$1 = $$0.indexOf(93, 1);
            if ($$1 == -1) {
               throw new IllegalArgumentException("Unterminated type in line '" + $$0 + "'");
            } else {
               String $$2 = $$0.substring(1, $$1);
               String $$3 = $$0.substring($$1 + 1);

               return switch ($$2) {
                  case "glob", "regex" -> Optional.of(new PathAllowList.ConfigEntry(PathAllowList.EntryType.FILESYSTEM, $$2 + ":" + $$3));
                  case "prefix" -> Optional.of(new PathAllowList.ConfigEntry(PathAllowList.EntryType.PREFIX, $$3));
                  default -> throw new IllegalArgumentException("Unsupported definition type in line '" + $$0 + "'");
               };
            }
         }
      }

      static PathAllowList.ConfigEntry glob(String $$0) {
         return new PathAllowList.ConfigEntry(PathAllowList.EntryType.FILESYSTEM, "glob:" + $$0);
      }

      static PathAllowList.ConfigEntry regex(String $$0) {
         return new PathAllowList.ConfigEntry(PathAllowList.EntryType.FILESYSTEM, "regex:" + $$0);
      }

      static PathAllowList.ConfigEntry prefix(String $$0) {
         return new PathAllowList.ConfigEntry(PathAllowList.EntryType.PREFIX, $$0);
      }
   }

   @FunctionalInterface
   public interface EntryType {
      PathAllowList.EntryType FILESYSTEM = FileSystem::getPathMatcher;
      PathAllowList.EntryType PREFIX = ($$0, $$1) -> $$1x -> $$1x.toString().startsWith($$1);

      PathMatcher compile(FileSystem var1, String var2);
   }
}
