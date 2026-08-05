/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
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
import org.slf4j.Logger;

public class DownloadCacheCleaner {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void vacuumCacheDir(Path $$0, int $$1) {
        try {
            List<PathAndTime> $$2 = DownloadCacheCleaner.listFilesWithModificationTimes($$0);
            int $$3 = $$2.size() - $$1;
            if ($$3 <= 0) {
                return;
            }
            $$2.sort(PathAndTime.NEWEST_FIRST);
            List<PathAndPriority> $$4 = DownloadCacheCleaner.prioritizeFilesInDirs($$2);
            Collections.reverse($$4);
            $$4.sort(PathAndPriority.HIGHEST_PRIORITY_FIRST);
            HashSet<Path> $$5 = new HashSet<Path>();
            for (int $$6 = 0; $$6 < $$3; ++$$6) {
                PathAndPriority $$7 = $$4.get($$6);
                Path $$8 = $$7.path;
                try {
                    Files.delete($$8);
                    if ($$7.removalPriority != 0) continue;
                    $$5.add($$8.getParent());
                    continue;
                }
                catch (IOException $$9) {
                    LOGGER.warn("Failed to delete cache file {}", (Object)$$8, (Object)$$9);
                }
            }
            $$5.remove($$0);
            for (Path $$10 : $$5) {
                try {
                    Files.delete($$10);
                }
                catch (DirectoryNotEmptyException $$8) {
                }
                catch (IOException $$11) {
                    LOGGER.warn("Failed to delete empty(?) cache directory {}", (Object)$$10, (Object)$$11);
                }
            }
        }
        catch (IOException | UncheckedIOException $$12) {
            LOGGER.error("Failed to vacuum cache dir {}", (Object)$$0, (Object)$$12);
        }
    }

    private static List<PathAndTime> listFilesWithModificationTimes(final Path $$0) throws IOException {
        try {
            final ArrayList<PathAndTime> $$1 = new ArrayList<PathAndTime>();
            Files.walkFileTree($$0, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult visitFile(Path $$02, BasicFileAttributes $$12) {
                    if ($$12.isRegularFile() && !$$02.getParent().equals($$0)) {
                        FileTime $$2 = $$12.lastModifiedTime();
                        $$1.add(new PathAndTime($$02, $$2));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public /* synthetic */ FileVisitResult visitFile(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                    return this.visitFile((Path)object, basicFileAttributes);
                }
            });
            return $$1;
        }
        catch (NoSuchFileException $$2) {
            return List.of();
        }
    }

    private static List<PathAndPriority> prioritizeFilesInDirs(List<PathAndTime> $$0) {
        ArrayList<PathAndPriority> $$1 = new ArrayList<PathAndPriority>();
        Object2IntOpenHashMap $$2 = new Object2IntOpenHashMap();
        for (PathAndTime $$3 : $$0) {
            int $$4 = $$2.addTo((Object)$$3.path.getParent(), 1);
            $$1.add(new PathAndPriority($$3.path, $$4));
        }
        return $$1;
    }

    static final class PathAndTime
    extends Record {
        final Path path;
        private final FileTime modifiedTime;
        public static final Comparator<PathAndTime> NEWEST_FIRST = Comparator.comparing(PathAndTime::modifiedTime).reversed();

        PathAndTime(Path $$0, FileTime $$1) {
            this.path = $$0;
            this.modifiedTime = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PathAndTime.class, "path;modifiedTime", "path", "modifiedTime"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PathAndTime.class, "path;modifiedTime", "path", "modifiedTime"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PathAndTime.class, "path;modifiedTime", "path", "modifiedTime"}, this, $$0);
        }

        public Path path() {
            return this.path;
        }

        public FileTime modifiedTime() {
            return this.modifiedTime;
        }
    }

    static final class PathAndPriority
    extends Record {
        final Path path;
        final int removalPriority;
        public static final Comparator<PathAndPriority> HIGHEST_PRIORITY_FIRST = Comparator.comparing(PathAndPriority::removalPriority).reversed();

        PathAndPriority(Path $$0, int $$1) {
            this.path = $$0;
            this.removalPriority = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PathAndPriority.class, "path;removalPriority", "path", "removalPriority"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PathAndPriority.class, "path;removalPriority", "path", "removalPriority"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PathAndPriority.class, "path;removalPriority", "path", "removalPriority"}, this, $$0);
        }

        public Path path() {
            return this.path;
        }

        public int removalPriority() {
            return this.removalPriority;
        }
    }
}

