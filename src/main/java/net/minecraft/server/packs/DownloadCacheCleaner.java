package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;

public class DownloadCacheCleaner {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void vacuumCacheDir(Path $$0, int $$1) {
      try {
         List<DownloadCacheCleaner.PathAndTime> $$2 = listFilesWithModificationTimes($$0);
         int $$3 = $$2.size() - $$1;
         if ($$3 <= 0) {
            return;
         }

         $$2.sort(DownloadCacheCleaner.PathAndTime.NEWEST_FIRST);
         List<DownloadCacheCleaner.PathAndPriority> $$4 = prioritizeFilesInDirs($$2);
         Collections.reverse($$4);
         $$4.sort(DownloadCacheCleaner.PathAndPriority.HIGHEST_PRIORITY_FIRST);
         Set<Path> $$5 = new HashSet<>();

         for (int $$6 = 0; $$6 < $$3; $$6++) {
            DownloadCacheCleaner.PathAndPriority $$7 = $$4.get($$6);
            Path $$8 = $$7.path;

            try {
               Files.delete($$8);
               if ($$7.removalPriority == 0) {
                  $$5.add($$8.getParent());
               }
            } catch (IOException var12) {
               LOGGER.warn("Failed to delete cache file {}", $$8, var12);
            }
         }

         $$5.remove($$0);

         for (Path $$10 : $$5) {
            try {
               Files.delete($$10);
            } catch (DirectoryNotEmptyException var10) {
            } catch (IOException var11) {
               LOGGER.warn("Failed to delete empty(?) cache directory {}", $$10, var11);
            }
         }
      } catch (UncheckedIOException | IOException var13) {
         LOGGER.error("Failed to vacuum cache dir {}", $$0, var13);
      }
   }

   private static List<DownloadCacheCleaner.PathAndTime> listFilesWithModificationTimes(final Path $$0) throws IOException {
      try {
         final List<DownloadCacheCleaner.PathAndTime> $$1 = new ArrayList<>();
         Files.walkFileTree($$0, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path $$0x, BasicFileAttributes $$1x) {
               if ($$1x.isRegularFile() && !$$0.getParent().equals($$0)) {
                  FileTime $$2 = $$1x.lastModifiedTime();
                  $$1.add(new DownloadCacheCleaner.PathAndTime($$0, $$2));
               }

               return FileVisitResult.CONTINUE;
            }
         });
         return $$1;
      } catch (NoSuchFileException var2) {
         return List.of();
      }
   }

   private static List<DownloadCacheCleaner.PathAndPriority> prioritizeFilesInDirs(List<DownloadCacheCleaner.PathAndTime> $$0) {
      List<DownloadCacheCleaner.PathAndPriority> $$1 = new ArrayList<>();
      Object2IntOpenHashMap<Path> $$2 = new Object2IntOpenHashMap();

      for (DownloadCacheCleaner.PathAndTime $$3 : $$0) {
         int $$4 = $$2.addTo($$3.path.getParent(), 1);
         $$1.add(new DownloadCacheCleaner.PathAndPriority($$3.path, $$4));
      }

      return $$1;
   }

   record PathAndPriority(Path path, int removalPriority) {
      public static final Comparator<DownloadCacheCleaner.PathAndPriority> HIGHEST_PRIORITY_FIRST = Comparator.comparing(
            DownloadCacheCleaner.PathAndPriority::removalPriority
         )
         .reversed();
   }

   record PathAndTime(Path path, FileTime modifiedTime) {
      public static final Comparator<DownloadCacheCleaner.PathAndTime> NEWEST_FIRST = Comparator.comparing(DownloadCacheCleaner.PathAndTime::modifiedTime)
         .reversed();
   }
}
