package net.minecraft.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.WorldVersion;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class HashCache {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final String HEADER_MARKER = "// ";
   private final Path rootDir;
   private final Path cacheDir;
   private final String versionId;
   private final Map<String, net.minecraft.data.HashCache.ProviderCache> caches;
   private final Set<String> cachesToWrite = new HashSet<>();
   final Set<Path> cachePaths = new HashSet<>();
   private final int initialCount;
   private int writes;

   private Path getProviderCachePath(String $$0) {
      return this.cacheDir.resolve(Hashing.sha1().hashString($$0, StandardCharsets.UTF_8).toString());
   }

   public HashCache(Path $$0, Collection<String> $$1, WorldVersion $$2) throws IOException {
      this.versionId = $$2.id();
      this.rootDir = $$0;
      this.cacheDir = $$0.resolve(".cache");
      Files.createDirectories(this.cacheDir);
      Map<String, net.minecraft.data.HashCache.ProviderCache> $$3 = new HashMap<>();
      int $$4 = 0;

      for (String $$5 : $$1) {
         Path $$6 = this.getProviderCachePath($$5);
         this.cachePaths.add($$6);
         net.minecraft.data.HashCache.ProviderCache $$7 = readCache($$0, $$6);
         $$3.put($$5, $$7);
         $$4 += $$7.count();
      }

      this.caches = $$3;
      this.initialCount = $$4;
   }

   private static net.minecraft.data.HashCache.ProviderCache readCache(Path $$0, Path $$1) {
      if (Files.isReadable($$1)) {
         try {
            return net.minecraft.data.HashCache.ProviderCache.load($$0, $$1);
         } catch (Exception var3) {
            LOGGER.warn("Failed to parse cache {}, discarding", $$1, var3);
         }
      }

      return new net.minecraft.data.HashCache.ProviderCache("unknown", ImmutableMap.of());
   }

   public boolean shouldRunInThisVersion(String $$0) {
      net.minecraft.data.HashCache.ProviderCache $$1 = this.caches.get($$0);
      return $$1 == null || !$$1.version.equals(this.versionId);
   }

   public CompletableFuture<net.minecraft.data.HashCache.UpdateResult> generateUpdate(String $$0, net.minecraft.data.HashCache.UpdateFunction $$1) {
      net.minecraft.data.HashCache.ProviderCache $$2 = this.caches.get($$0);
      if ($$2 == null) {
         throw new IllegalStateException("Provider not registered: " + $$0);
      } else {
         net.minecraft.data.HashCache.CacheUpdater $$3 = new net.minecraft.data.HashCache.CacheUpdater($$0, this.versionId, $$2);
         return $$1.update($$3).thenApply($$1x -> $$3.close());
      }
   }

   public void applyUpdate(net.minecraft.data.HashCache.UpdateResult $$0) {
      this.caches.put($$0.providerId(), $$0.cache());
      this.cachesToWrite.add($$0.providerId());
      this.writes = this.writes + $$0.writes();
   }

   public void purgeStaleAndWrite() throws IOException {
      final Set<Path> $$0 = new HashSet<>();
      this.caches.forEach(($$1x, $$2x) -> {
         if (this.cachesToWrite.contains($$1x)) {
            Path $$3 = this.getProviderCachePath($$1x);
            $$2x.save(this.rootDir, $$3, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ZonedDateTime.now()) + "\t" + $$1x);
         }

         $$0.addAll($$2x.data().keySet());
      });
      $$0.add(this.rootDir.resolve("version.json"));
      final MutableInt $$1 = new MutableInt();
      final MutableInt $$2 = new MutableInt();
      Files.walkFileTree(this.rootDir, new SimpleFileVisitor<Path>() {
         public FileVisitResult visitFile(Path $$0x, BasicFileAttributes $$1x) {
            if (HashCache.this.cachePaths.contains($$0)) {
               return FileVisitResult.CONTINUE;
            } else {
               $$1.increment();
               if ($$0.contains($$0)) {
                  return FileVisitResult.CONTINUE;
               } else {
                  try {
                     Files.delete($$0);
                  } catch (IOException var4) {
                     net.minecraft.data.HashCache.LOGGER.warn("Failed to delete file {}", $$0, var4);
                  }

                  $$2.increment();
                  return FileVisitResult.CONTINUE;
               }
            }
         }
      });
      LOGGER.info(
         "Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}",
         new Object[]{$$1, this.initialCount, $$0.size(), $$2, this.writes}
      );
   }

   static class CacheUpdater implements net.minecraft.data.CachedOutput {
      private final String provider;
      private final net.minecraft.data.HashCache.ProviderCache oldCache;
      private final net.minecraft.data.HashCache.ProviderCacheBuilder newCache;
      private final AtomicInteger writes = new AtomicInteger();
      private volatile boolean closed;

      CacheUpdater(String $$0, String $$1, net.minecraft.data.HashCache.ProviderCache $$2) {
         this.provider = $$0;
         this.oldCache = $$2;
         this.newCache = new net.minecraft.data.HashCache.ProviderCacheBuilder($$1);
      }

      private boolean shouldWrite(Path $$0, HashCode $$1) {
         return !Objects.equals(this.oldCache.get($$0), $$1) || !Files.exists($$0);
      }

      @Override
      public void writeIfNeeded(Path $$0, byte[] $$1, HashCode $$2) throws IOException {
         if (this.closed) {
            throw new IllegalStateException("Cannot write to cache as it has already been closed");
         } else {
            if (this.shouldWrite($$0, $$2)) {
               this.writes.incrementAndGet();
               Files.createDirectories($$0.getParent());
               Files.write($$0, $$1);
            }

            this.newCache.put($$0, $$2);
         }
      }

      public net.minecraft.data.HashCache.UpdateResult close() {
         this.closed = true;
         return new net.minecraft.data.HashCache.UpdateResult(this.provider, this.newCache.build(), this.writes.get());
      }
   }

   record ProviderCache(String version, ImmutableMap<Path, HashCode> data) {

      
      public HashCode get(Path $$0) {
         return (HashCode)this.data.get($$0);
      }

      public int count() {
         return this.data.size();
      }

      public static net.minecraft.data.HashCache.ProviderCache load(Path $$0, Path $$1) throws IOException {
         net.minecraft.data.HashCache.ProviderCache var7;
         try (BufferedReader $$2 = Files.newBufferedReader($$1, StandardCharsets.UTF_8)) {
            String $$3 = $$2.readLine();
            if (!$$3.startsWith("// ")) {
               throw new IllegalStateException("Missing cache file header");
            }

            String[] $$4 = $$3.substring("// ".length()).split("\t", 2);
            String $$5 = $$4[0];
            Builder<Path, HashCode> $$6 = ImmutableMap.builder();
            $$2.lines().forEach($$2x -> {
               int $$3x = $$2x.indexOf(32);
               $$6.put($$0.resolve($$2x.substring($$3x + 1)), HashCode.fromString($$2x.substring(0, $$3x)));
            });
            var7 = new net.minecraft.data.HashCache.ProviderCache($$5, $$6.build());
         }

         return var7;
      }

      public void save(Path $$0, Path $$1, String $$2) {
         try (BufferedWriter $$3 = Files.newBufferedWriter($$1, StandardCharsets.UTF_8)) {
            $$3.write("// ");
            $$3.write(this.version);
            $$3.write(9);
            $$3.write($$2);
            $$3.newLine();
            UnmodifiableIterator var5 = this.data.entrySet().iterator();

            while (var5.hasNext()) {
               Entry<Path, HashCode> $$4 = (Entry<Path, HashCode>)var5.next();
               $$3.write($$4.getValue().toString());
               $$3.write(32);
               $$3.write($$0.relativize($$4.getKey()).toString());
               $$3.newLine();
            }
         } catch (IOException var9) {
            net.minecraft.data.HashCache.LOGGER.warn("Unable write cachefile {}: {}", $$1, var9);
         }
      }
   }

   record ProviderCacheBuilder(String version, ConcurrentMap<Path, HashCode> data) {
      ProviderCacheBuilder(String $$0) {
         this($$0, new ConcurrentHashMap<>());
      }

      public void put(Path $$0, HashCode $$1) {
         this.data.put($$0, $$1);
      }

      public net.minecraft.data.HashCache.ProviderCache build() {
         return new net.minecraft.data.HashCache.ProviderCache(this.version, ImmutableMap.copyOf(this.data));
      }
   }

   @FunctionalInterface
   public interface UpdateFunction {
      CompletableFuture<?> update(net.minecraft.data.CachedOutput var1);
   }

   public record UpdateResult(String providerId, net.minecraft.data.HashCache.ProviderCache cache, int writes) {
   }
}
