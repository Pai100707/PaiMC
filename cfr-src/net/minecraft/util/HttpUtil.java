/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.Funnels
 *  com.google.common.hash.HashCode
 *  com.google.common.hash.HashFunction
 *  com.google.common.hash.Hasher
 *  com.google.common.hash.PrimitiveSink
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.io.IOUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.PrimitiveSink;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Map;
import java.util.OptionalLong;
import net.minecraft.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class HttpUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    private HttpUtil() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    public static Path downloadFile(Path $$0, URL $$1, Map<String, String> $$2, HashFunction $$3, @Nullable HashCode $$4, int $$5, Proxy $$6, DownloadProgressListener $$7) {
        InputStream $$9;
        HttpURLConnection $$8;
        block21: {
            $$8 = null;
            $$9 = null;
            $$7.requestStart();
            if ($$4 != null) {
                Path $$10 = HttpUtil.cachedFilePath($$0, $$4);
                try {
                    if (HttpUtil.checkExistingFile($$10, $$3, $$4)) {
                        LOGGER.info("Returning cached file since actual hash matches requested");
                        $$7.requestFinished(true);
                        HttpUtil.updateModificationTime($$10);
                        return $$10;
                    }
                }
                catch (IOException $$11) {
                    LOGGER.warn("Failed to check cached file {}", (Object)$$10, (Object)$$11);
                }
                try {
                    LOGGER.warn("Existing file {} not found or had mismatched hash", (Object)$$10);
                    Files.deleteIfExists($$10);
                }
                catch (IOException $$12) {
                    $$7.requestFinished(false);
                    throw new UncheckedIOException("Failed to remove existing file " + String.valueOf($$10), $$12);
                }
            }
            Path $$13 = null;
            $$8 = (HttpURLConnection)$$1.openConnection($$6);
            $$8.setInstanceFollowRedirects(true);
            $$2.forEach($$8::setRequestProperty);
            $$9 = $$8.getInputStream();
            long $$14 = $$8.getContentLengthLong();
            OptionalLong $$15 = $$14 != -1L ? OptionalLong.of($$14) : OptionalLong.empty();
            FileUtil.createDirectoriesSafe($$0);
            $$7.downloadStart($$15);
            if ($$15.isPresent() && $$15.getAsLong() > (long)$$5) {
                throw new IOException("Filesize is bigger than maximum allowed (file is " + String.valueOf($$15) + ", limit is " + $$5 + ")");
            }
            if ($$13 == null) break block21;
            HashCode $$16 = HttpUtil.downloadAndHash($$3, $$5, $$7, $$9, $$13);
            if (!$$16.equals((Object)$$4)) {
                throw new IOException("Hash of downloaded file (" + String.valueOf($$16) + ") did not match requested (" + String.valueOf($$4) + ")");
            }
            $$7.requestFinished(true);
            Path path = $$13;
            IOUtils.closeQuietly((InputStream)$$9);
            return path;
        }
        Path $$17 = Files.createTempFile($$0, "download", ".tmp", new FileAttribute[0]);
        HashCode $$18 = HttpUtil.downloadAndHash($$3, $$5, $$7, $$9, $$17);
        Path $$19 = HttpUtil.cachedFilePath($$0, $$18);
        if (!HttpUtil.checkExistingFile($$19, $$3, $$18)) {
            Files.move($$17, $$19, StandardCopyOption.REPLACE_EXISTING);
        } else {
            HttpUtil.updateModificationTime($$19);
        }
        $$7.requestFinished(true);
        Path path = $$19;
        Files.deleteIfExists($$17);
        IOUtils.closeQuietly((InputStream)$$9);
        return path;
        {
            catch (Throwable throwable) {
                try {
                    try {
                        Files.deleteIfExists($$17);
                        throw throwable;
                    }
                    catch (Throwable $$20) {
                        InputStream $$21;
                        if ($$8 != null && ($$21 = $$8.getErrorStream()) != null) {
                            try {
                                LOGGER.error("HTTP response error: {}", (Object)IOUtils.toString((InputStream)$$21, (Charset)StandardCharsets.UTF_8));
                            }
                            catch (Exception $$22) {
                                LOGGER.error("Failed to read response from server");
                            }
                        }
                        $$7.requestFinished(false);
                        throw new IllegalStateException("Failed to download file " + String.valueOf($$1), $$20);
                    }
                }
                catch (Throwable throwable2) {
                    IOUtils.closeQuietly($$9);
                    throw throwable2;
                }
            }
        }
    }

    private static void updateModificationTime(Path $$0) {
        try {
            Files.setLastModifiedTime($$0, FileTime.from(Instant.now()));
        }
        catch (IOException $$1) {
            LOGGER.warn("Failed to update modification time of {}", (Object)$$0, (Object)$$1);
        }
    }

    private static HashCode hashFile(Path $$0, HashFunction $$1) throws IOException {
        Hasher $$2 = $$1.newHasher();
        try (OutputStream $$3 = Funnels.asOutputStream((PrimitiveSink)$$2);
             InputStream $$4 = Files.newInputStream($$0, new OpenOption[0]);){
            $$4.transferTo($$3);
        }
        return $$2.hash();
    }

    private static boolean checkExistingFile(Path $$0, HashFunction $$1, HashCode $$2) throws IOException {
        if (Files.exists($$0, new LinkOption[0])) {
            HashCode $$3 = HttpUtil.hashFile($$0, $$1);
            if ($$3.equals((Object)$$2)) {
                return true;
            }
            LOGGER.warn("Mismatched hash of file {}, expected {} but found {}", new Object[]{$$0, $$2, $$3});
        }
        return false;
    }

    private static Path cachedFilePath(Path $$0, HashCode $$1) {
        return $$0.resolve($$1.toString());
    }

    private static HashCode downloadAndHash(HashFunction $$0, int $$1, DownloadProgressListener $$2, InputStream $$3, Path $$4) throws IOException {
        try (OutputStream $$5 = Files.newOutputStream($$4, StandardOpenOption.CREATE);){
            int $$9;
            Hasher $$6 = $$0.newHasher();
            byte[] $$7 = new byte[8196];
            long $$8 = 0L;
            while (($$9 = $$3.read($$7)) >= 0) {
                $$2.downloadedBytes($$8 += (long)$$9);
                if ($$8 > (long)$$1) {
                    throw new IOException("Filesize was bigger than maximum allowed (got >= " + $$8 + ", limit was " + $$1 + ")");
                }
                if (Thread.interrupted()) {
                    LOGGER.error("INTERRUPTED");
                    throw new IOException("Download interrupted");
                }
                $$5.write($$7, 0, $$9);
                $$6.putBytes($$7, 0, $$9);
            }
            HashCode hashCode = $$6.hash();
            return hashCode;
        }
    }

    public static int getAvailablePort() {
        int n;
        ServerSocket $$0 = new ServerSocket(0);
        try {
            n = $$0.getLocalPort();
        }
        catch (Throwable throwable) {
            try {
                try {
                    $$0.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException $$1) {
                return 25564;
            }
        }
        $$0.close();
        return n;
    }

    public static boolean isPortAvailable(int $$0) {
        boolean bl;
        if ($$0 < 0 || $$0 > 65535) {
            return false;
        }
        ServerSocket $$1 = new ServerSocket($$0);
        try {
            bl = $$1.getLocalPort() == $$0;
        }
        catch (Throwable throwable) {
            try {
                try {
                    $$1.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException $$2) {
                return false;
            }
        }
        $$1.close();
        return bl;
    }

    public static interface DownloadProgressListener {
        public void requestStart();

        public void downloadStart(OptionalLong var1);

        public void downloadedBytes(long var1);

        public void requestFinished(boolean var1);
    }
}

