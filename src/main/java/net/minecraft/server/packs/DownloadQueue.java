package net.minecraft.server.packs;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FileUtil;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.util.HttpUtil.DownloadProgressListener;
import net.minecraft.util.eventlog.JsonEventLog;
import net.minecraft.util.thread.ConsecutiveExecutor;
import org.slf4j.Logger;

public class DownloadQueue implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_KEPT_PACKS = 20;
   private final Path cacheDir;
   private final JsonEventLog<DownloadQueue.LogEntry> eventLog;
   private final ConsecutiveExecutor tasks = new ConsecutiveExecutor(Util.nonCriticalIoPool(), "download-queue");

   public DownloadQueue(Path $$0) throws IOException {
      this.cacheDir = $$0;
      FileUtil.createDirectoriesSafe($$0);
      this.eventLog = JsonEventLog.open(DownloadQueue.LogEntry.CODEC, $$0.resolve("log.json"));
      DownloadCacheCleaner.vacuumCacheDir($$0, 20);
   }

   private DownloadQueue.BatchResult runDownload(DownloadQueue.BatchConfig $$0, Map<UUID, DownloadQueue.DownloadRequest> $$1) {
      DownloadQueue.BatchResult $$2 = new DownloadQueue.BatchResult();
      $$1.forEach(
         ($$2x, $$3) -> {
            Path $$4 = this.cacheDir.resolve($$2x.toString());
            Path $$5 = null;

            try {
               $$5 = HttpUtil.downloadFile($$4, $$3.url, $$0.headers, $$0.hashFunction, $$3.hash, $$0.maxSize, $$0.proxy, $$0.listener);
               $$2.downloaded.put($$2x, $$5);
            } catch (Exception var9) {
               LOGGER.error("Failed to download {}", $$3.url, var9);
               $$2.failed.add($$2x);
            }

            try {
               this.eventLog
                  .write(
                     new DownloadQueue.LogEntry(
                        $$2x,
                        $$3.url.toString(),
                        Instant.now(),
                        Optional.ofNullable($$3.hash).map(HashCode::toString),
                        $$5 != null ? this.getFileInfo($$5) : Either.left("download_failed")
                     )
                  );
            } catch (Exception var8) {
               LOGGER.error("Failed to log download of {}", $$3.url, var8);
            }
         }
      );
      return $$2;
   }

   private Either<String, DownloadQueue.FileInfoEntry> getFileInfo(Path $$0) {
      try {
         long $$1 = Files.size($$0);
         Path $$2 = this.cacheDir.relativize($$0);
         return Either.right(new DownloadQueue.FileInfoEntry($$2.toString(), $$1));
      } catch (IOException var5) {
         LOGGER.error("Failed to get file size of {}", $$0, var5);
         return Either.left("no_access");
      }
   }

   public CompletableFuture<DownloadQueue.BatchResult> downloadBatch(DownloadQueue.BatchConfig $$0, Map<UUID, DownloadQueue.DownloadRequest> $$1) {
      return CompletableFuture.supplyAsync(() -> this.runDownload($$0, $$1), this.tasks::schedule);
   }

   @Override
   public void close() throws IOException {
      this.tasks.close();
      this.eventLog.close();
   }

   public record BatchConfig(HashFunction hashFunction, int maxSize, Map<String, String> headers, Proxy proxy, DownloadProgressListener listener) {
   }

   public record BatchResult(Map<UUID, Path> downloaded, Set<UUID> failed) {

      public BatchResult() {
         this(new HashMap<>(), new HashSet<>());
      }
   }

   public record DownloadRequest(URL url, HashCode hash) {
   }

   record FileInfoEntry(String name, long size) {
      public static final Codec<DownloadQueue.FileInfoEntry> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.STRING.fieldOf("name").forGetter(DownloadQueue.FileInfoEntry::name),
               Codec.LONG.fieldOf("size").forGetter(DownloadQueue.FileInfoEntry::size)
            )
            .apply($$0, DownloadQueue.FileInfoEntry::new)
      );
   }

   record LogEntry(UUID id, String url, Instant time, Optional<String> hash, Either<String, DownloadQueue.FileInfoEntry> errorOrFileInfo) {
      public static final Codec<DownloadQueue.LogEntry> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(DownloadQueue.LogEntry::id),
               Codec.STRING.fieldOf("url").forGetter(DownloadQueue.LogEntry::url),
               ExtraCodecs.INSTANT_ISO8601.fieldOf("time").forGetter(DownloadQueue.LogEntry::time),
               Codec.STRING.optionalFieldOf("hash").forGetter(DownloadQueue.LogEntry::hash),
               Codec.mapEither(Codec.STRING.fieldOf("error"), DownloadQueue.FileInfoEntry.CODEC.fieldOf("file"))
                  .forGetter(DownloadQueue.LogEntry::errorOrFileInfo)
            )
            .apply($$0, DownloadQueue.LogEntry::new)
      );
   }
}
