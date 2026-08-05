/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.hash.HashCode
 *  com.google.common.hash.Hashing
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.WorldVersion;
import net.minecraft.data.CachedOutput;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class HashCache {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String HEADER_MARKER = "// ";
    private final Path rootDir;
    private final Path cacheDir;
    private final String versionId;
    private final Map<String, ProviderCache> caches;
    private final Set<String> cachesToWrite = new HashSet<String>();
    final Set<Path> cachePaths = new HashSet<Path>();
    private final int initialCount;
    private int writes;

    private Path getProviderCachePath(String $$0) {
        return this.cacheDir.resolve(Hashing.sha1().hashString((CharSequence)$$0, StandardCharsets.UTF_8).toString());
    }

    public HashCache(Path $$0, Collection<String> $$1, WorldVersion $$2) throws IOException {
        this.versionId = $$2.id();
        this.rootDir = $$0;
        this.cacheDir = $$0.resolve(".cache");
        Files.createDirectories(this.cacheDir, new FileAttribute[0]);
        HashMap<String, ProviderCache> $$3 = new HashMap<String, ProviderCache>();
        int $$4 = 0;
        for (String $$5 : $$1) {
            Path $$6 = this.getProviderCachePath($$5);
            this.cachePaths.add($$6);
            ProviderCache $$7 = HashCache.readCache($$0, $$6);
            $$3.put($$5, $$7);
            $$4 += $$7.count();
        }
        this.caches = $$3;
        this.initialCount = $$4;
    }

    private static ProviderCache readCache(Path $$0, Path $$1) {
        if (Files.isReadable($$1)) {
            try {
                return ProviderCache.load($$0, $$1);
            }
            catch (Exception $$2) {
                LOGGER.warn("Failed to parse cache {}, discarding", (Object)$$1, (Object)$$2);
            }
        }
        return new ProviderCache("unknown", (ImmutableMap<Path, HashCode>)ImmutableMap.of());
    }

    public boolean shouldRunInThisVersion(String $$0) {
        ProviderCache $$1 = this.caches.get($$0);
        return $$1 == null || !$$1.version.equals(this.versionId);
    }

    public CompletableFuture<UpdateResult> generateUpdate(String $$0, UpdateFunction $$12) {
        ProviderCache $$2 = this.caches.get($$0);
        if ($$2 == null) {
            throw new IllegalStateException("Provider not registered: " + $$0);
        }
        CacheUpdater $$3 = new CacheUpdater($$0, this.versionId, $$2);
        return $$12.update($$3).thenApply($$1 -> $$3.close());
    }

    public void applyUpdate(UpdateResult $$0) {
        this.caches.put($$0.providerId(), $$0.cache());
        this.cachesToWrite.add($$0.providerId());
        this.writes += $$0.writes();
    }

    public void purgeStaleAndWrite() throws IOException {
        final HashSet<Path> $$0 = new HashSet<Path>();
        this.caches.forEach(($$1, $$2) -> {
            if (this.cachesToWrite.contains($$1)) {
                Path $$3 = this.getProviderCachePath((String)$$1);
                $$2.save(this.rootDir, $$3, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ZonedDateTime.now()) + "\t" + $$1);
            }
            $$0.addAll((Collection<Path>)$$2.data().keySet());
        });
        $$0.add(this.rootDir.resolve("version.json"));
        final MutableInt $$12 = new MutableInt();
        final MutableInt $$22 = new MutableInt();
        Files.walkFileTree(this.rootDir, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult visitFile(Path $$02, BasicFileAttributes $$1) {
                if (HashCache.this.cachePaths.contains($$02)) {
                    return FileVisitResult.CONTINUE;
                }
                $$12.increment();
                if ($$0.contains($$02)) {
                    return FileVisitResult.CONTINUE;
                }
                try {
                    Files.delete($$02);
                }
                catch (IOException $$2) {
                    LOGGER.warn("Failed to delete file {}", (Object)$$02, (Object)$$2);
                }
                $$22.increment();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public /* synthetic */ FileVisitResult visitFile(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                return this.visitFile((Path)object, basicFileAttributes);
            }
        });
        LOGGER.info("Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}", new Object[]{$$12, this.initialCount, $$0.size(), $$22, this.writes});
    }

    static final class ProviderCache
    extends Record {
        final String version;
        private final ImmutableMap<Path, HashCode> data;

        ProviderCache(String $$0, ImmutableMap<Path, HashCode> $$1) {
            this.version = $$0;
            this.data = $$1;
        }

        public @Nullable HashCode get(Path $$0) {
            return (HashCode)this.data.get((Object)$$0);
        }

        public int count() {
            return this.data.size();
        }

        public static ProviderCache load(Path $$0, Path $$1) throws IOException {
            try (BufferedReader $$22 = Files.newBufferedReader($$1, StandardCharsets.UTF_8);){
                String $$3 = $$22.readLine();
                if (!$$3.startsWith(HashCache.HEADER_MARKER)) {
                    throw new IllegalStateException("Missing cache file header");
                }
                String[] $$4 = $$3.substring(HashCache.HEADER_MARKER.length()).split("\t", 2);
                String $$5 = $$4[0];
                ImmutableMap.Builder $$6 = ImmutableMap.builder();
                $$22.lines().forEach($$2 -> {
                    int $$3 = $$2.indexOf(32);
                    $$6.put((Object)$$0.resolve($$2.substring($$3 + 1)), (Object)HashCode.fromString((String)$$2.substring(0, $$3)));
                });
                ProviderCache providerCache = new ProviderCache($$5, (ImmutableMap<Path, HashCode>)$$6.build());
                return providerCache;
            }
        }

        public void save(Path $$0, Path $$1, String $$2) {
            try (BufferedWriter $$3 = Files.newBufferedWriter($$1, StandardCharsets.UTF_8, new OpenOption[0]);){
                $$3.write(HashCache.HEADER_MARKER);
                $$3.write(this.version);
                $$3.write(9);
                $$3.write($$2);
                $$3.newLine();
                for (Map.Entry $$4 : this.data.entrySet()) {
                    $$3.write(((HashCode)$$4.getValue()).toString());
                    $$3.write(32);
                    $$3.write($$0.relativize((Path)$$4.getKey()).toString());
                    $$3.newLine();
                }
            }
            catch (IOException $$5) {
                LOGGER.warn("Unable write cachefile {}: {}", (Object)$$1, (Object)$$5);
            }
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ProviderCache.class, "version;data", "version", "data"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ProviderCache.class, "version;data", "version", "data"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ProviderCache.class, "version;data", "version", "data"}, this, $$0);
        }

        public String version() {
            return this.version;
        }

        public ImmutableMap<Path, HashCode> data() {
            return this.data;
        }
    }

    static class CacheUpdater
    implements CachedOutput {
        private final String provider;
        private final ProviderCache oldCache;
        private final ProviderCacheBuilder newCache;
        private final AtomicInteger writes = new AtomicInteger();
        private volatile boolean closed;

        CacheUpdater(String $$0, String $$1, ProviderCache $$2) {
            this.provider = $$0;
            this.oldCache = $$2;
            this.newCache = new ProviderCacheBuilder($$1);
        }

        private boolean shouldWrite(Path $$0, HashCode $$1) {
            return !Objects.equals(this.oldCache.get($$0), $$1) || !Files.exists($$0, new LinkOption[0]);
        }

        @Override
        public void writeIfNeeded(Path $$0, byte[] $$1, HashCode $$2) throws IOException {
            if (this.closed) {
                throw new IllegalStateException("Cannot write to cache as it has already been closed");
            }
            if (this.shouldWrite($$0, $$2)) {
                this.writes.incrementAndGet();
                Files.createDirectories($$0.getParent(), new FileAttribute[0]);
                Files.write($$0, $$1, new OpenOption[0]);
            }
            this.newCache.put($$0, $$2);
        }

        public UpdateResult close() {
            this.closed = true;
            return new UpdateResult(this.provider, this.newCache.build(), this.writes.get());
        }
    }

    @FunctionalInterface
    public static interface UpdateFunction {
        public CompletableFuture<?> update(CachedOutput var1);
    }

    public record UpdateResult(String providerId, ProviderCache cache, int writes) {
    }

    record ProviderCacheBuilder(String version, ConcurrentMap<Path, HashCode> data) {
        ProviderCacheBuilder(String $$0) {
            this($$0, new ConcurrentHashMap<Path, HashCode>());
        }

        public void put(Path $$0, HashCode $$1) {
            this.data.put($$0, $$1);
        }

        public ProviderCache build() {
            return new ProviderCache(this.version, (ImmutableMap<Path, HashCode>)ImmutableMap.copyOf(this.data));
        }
    }
}

