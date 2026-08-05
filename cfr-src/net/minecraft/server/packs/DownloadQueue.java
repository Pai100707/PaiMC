/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.HashCode
 *  com.google.common.hash.HashFunction
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
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
import net.minecraft.server.packs.DownloadCacheCleaner;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FileUtil;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.util.eventlog.JsonEventLog;
import net.minecraft.util.thread.ConsecutiveExecutor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class DownloadQueue
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_KEPT_PACKS = 20;
    private final Path cacheDir;
    private final JsonEventLog<LogEntry> eventLog;
    private final ConsecutiveExecutor tasks = new ConsecutiveExecutor(Util.nonCriticalIoPool(), "download-queue");

    public DownloadQueue(Path $$0) throws IOException {
        this.cacheDir = $$0;
        FileUtil.createDirectoriesSafe($$0);
        this.eventLog = JsonEventLog.open(LogEntry.CODEC, $$0.resolve("log.json"));
        DownloadCacheCleaner.vacuumCacheDir($$0, 20);
    }

    private BatchResult runDownload(BatchConfig $$0, Map<UUID, DownloadRequest> $$1) {
        BatchResult $$22 = new BatchResult();
        $$1.forEach(($$2, $$3) -> {
            Path $$4 = this.cacheDir.resolve($$2.toString());
            Path $$5 = null;
            try {
                $$5 = HttpUtil.downloadFile($$4, $$3.url, $$0.headers, $$0.hashFunction, $$3.hash, $$0.maxSize, $$0.proxy, $$0.listener);
                $$1.downloaded.put((UUID)$$2, $$5);
            }
            catch (Exception $$6) {
                LOGGER.error("Failed to download {}", (Object)$$3.url, (Object)$$6);
                $$1.failed.add((UUID)$$2);
            }
            try {
                this.eventLog.write(new LogEntry((UUID)$$2, $$3.url.toString(), Instant.now(), Optional.ofNullable($$3.hash).map(HashCode::toString), $$5 != null ? this.getFileInfo($$5) : Either.left((Object)"download_failed")));
            }
            catch (Exception $$7) {
                LOGGER.error("Failed to log download of {}", (Object)$$3.url, (Object)$$7);
            }
        });
        return $$22;
    }

    private Either<String, FileInfoEntry> getFileInfo(Path $$0) {
        try {
            long $$1 = Files.size($$0);
            Path $$2 = this.cacheDir.relativize($$0);
            return Either.right((Object)new FileInfoEntry($$2.toString(), $$1));
        }
        catch (IOException $$3) {
            LOGGER.error("Failed to get file size of {}", (Object)$$0, (Object)$$3);
            return Either.left((Object)"no_access");
        }
    }

    public CompletableFuture<BatchResult> downloadBatch(BatchConfig $$0, Map<UUID, DownloadRequest> $$1) {
        return CompletableFuture.supplyAsync(() -> this.runDownload($$0, $$1), this.tasks::schedule);
    }

    @Override
    public void close() throws IOException {
        this.tasks.close();
        this.eventLog.close();
    }

    record LogEntry(UUID id, String url, Instant time, Optional<String> hash, Either<String, FileInfoEntry> errorOrFileInfo) {
        public static final Codec<LogEntry> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(LogEntry::id), (App)Codec.STRING.fieldOf("url").forGetter(LogEntry::url), (App)ExtraCodecs.INSTANT_ISO8601.fieldOf("time").forGetter(LogEntry::time), (App)Codec.STRING.optionalFieldOf("hash").forGetter(LogEntry::hash), (App)Codec.mapEither((MapCodec)Codec.STRING.fieldOf("error"), (MapCodec)FileInfoEntry.CODEC.fieldOf("file")).forGetter(LogEntry::errorOrFileInfo)).apply((Applicative)$$0, LogEntry::new));
    }

    public static final class BatchResult
    extends Record {
        final Map<UUID, Path> downloaded;
        final Set<UUID> failed;

        public BatchResult() {
            this(new HashMap<UUID, Path>(), new HashSet<UUID>());
        }

        public BatchResult(Map<UUID, Path> $$0, Set<UUID> $$1) {
            this.downloaded = $$0;
            this.failed = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BatchResult.class, "downloaded;failed", "downloaded", "failed"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BatchResult.class, "downloaded;failed", "downloaded", "failed"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BatchResult.class, "downloaded;failed", "downloaded", "failed"}, this, $$0);
        }

        public Map<UUID, Path> downloaded() {
            return this.downloaded;
        }

        public Set<UUID> failed() {
            return this.failed;
        }
    }

    public static final class BatchConfig
    extends Record {
        final HashFunction hashFunction;
        final int maxSize;
        final Map<String, String> headers;
        final Proxy proxy;
        final HttpUtil.DownloadProgressListener listener;

        public BatchConfig(HashFunction $$0, int $$1, Map<String, String> $$2, Proxy $$3, HttpUtil.DownloadProgressListener $$4) {
            this.hashFunction = $$0;
            this.maxSize = $$1;
            this.headers = $$2;
            this.proxy = $$3;
            this.listener = $$4;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BatchConfig.class, "hashFunction;maxSize;headers;proxy;listener", "hashFunction", "maxSize", "headers", "proxy", "listener"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BatchConfig.class, "hashFunction;maxSize;headers;proxy;listener", "hashFunction", "maxSize", "headers", "proxy", "listener"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BatchConfig.class, "hashFunction;maxSize;headers;proxy;listener", "hashFunction", "maxSize", "headers", "proxy", "listener"}, this, $$0);
        }

        public HashFunction hashFunction() {
            return this.hashFunction;
        }

        public int maxSize() {
            return this.maxSize;
        }

        public Map<String, String> headers() {
            return this.headers;
        }

        public Proxy proxy() {
            return this.proxy;
        }

        public HttpUtil.DownloadProgressListener listener() {
            return this.listener;
        }
    }

    record FileInfoEntry(String name, long size) {
        public static final Codec<FileInfoEntry> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.STRING.fieldOf("name").forGetter(FileInfoEntry::name), (App)Codec.LONG.fieldOf("size").forGetter(FileInfoEntry::size)).apply((Applicative)$$0, FileInfoEntry::new));
    }

    public static final class DownloadRequest
    extends Record {
        final URL url;
        final @Nullable HashCode hash;

        public DownloadRequest(URL $$0, @Nullable HashCode $$1) {
            this.url = $$0;
            this.hash = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DownloadRequest.class, "url;hash", "url", "hash"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DownloadRequest.class, "url;hash", "url", "hash"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DownloadRequest.class, "url;hash", "url", "hash"}, this, $$0);
        }

        public URL url() {
            return this.url;
        }

        public @Nullable HashCode hash() {
            return this.hash;
        }
    }
}

