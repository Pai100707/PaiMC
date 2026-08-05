/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.Hashing
 *  com.google.common.hash.HashingOutputStream
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.data.structures;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class NbtToSnbt
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Iterable<Path> inputFolders;
    private final PackOutput output;

    public NbtToSnbt(PackOutput $$0, Collection<Path> $$1) {
        this.inputFolders = $$1;
        this.output = $$0;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$02) {
        Path $$1 = this.output.getOutputFolder();
        ArrayList<CompletionStage> $$2 = new ArrayList<CompletionStage>();
        for (Path $$3 : this.inputFolders) {
            $$2.add(CompletableFuture.supplyAsync(() -> {
                CompletableFuture<Void> completableFuture;
                block8: {
                    Stream<Path> $$32 = Files.walk($$3, new FileVisitOption[0]);
                    try {
                        completableFuture = CompletableFuture.allOf((CompletableFuture[])$$32.filter($$0 -> $$0.toString().endsWith(".nbt")).map($$3 -> CompletableFuture.runAsync(() -> NbtToSnbt.convertStructure($$02, $$3, NbtToSnbt.getName($$3, $$3), $$1), Util.ioPool())).toArray(CompletableFuture[]::new));
                        if ($$32 == null) break block8;
                    }
                    catch (Throwable throwable) {
                        try {
                            if ($$32 != null) {
                                try {
                                    $$32.close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        catch (IOException $$4) {
                            LOGGER.error("Failed to read structure input directory", (Throwable)$$4);
                            return CompletableFuture.completedFuture(null);
                        }
                    }
                    $$32.close();
                }
                return completableFuture;
            }, Util.backgroundExecutor().forName("NbtToSnbt")).thenCompose($$0 -> $$0));
        }
        return CompletableFuture.allOf((CompletableFuture[])$$2.toArray(CompletableFuture[]::new));
    }

    @Override
    public final String getName() {
        return "NBT -> SNBT";
    }

    private static String getName(Path $$0, Path $$1) {
        String $$2 = $$0.relativize($$1).toString().replaceAll("\\\\", "/");
        return $$2.substring(0, $$2.length() - ".nbt".length());
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static @Nullable Path convertStructure(CachedOutput $$0, Path $$1, String $$2, Path $$3) {
        try (InputStream $$4 = Files.newInputStream($$1, new OpenOption[0]);){
            Path path;
            try (FastBufferedInputStream $$5 = new FastBufferedInputStream($$4);){
                Path $$6 = $$3.resolve($$2 + ".snbt");
                NbtToSnbt.writeSnbt($$0, $$6, NbtUtils.structureToSnbt(NbtIo.readCompressed($$5, NbtAccounter.unlimitedHeap())));
                LOGGER.info("Converted {} from NBT to SNBT", (Object)$$2);
                path = $$6;
            }
            return path;
        }
        catch (IOException $$7) {
            LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", new Object[]{$$2, $$1, $$7});
            return null;
        }
    }

    public static void writeSnbt(CachedOutput $$0, Path $$1, String $$2) throws IOException {
        ByteArrayOutputStream $$3 = new ByteArrayOutputStream();
        HashingOutputStream $$4 = new HashingOutputStream(Hashing.sha1(), (OutputStream)$$3);
        $$4.write($$2.getBytes(StandardCharsets.UTF_8));
        $$4.write(10);
        $$0.writeIfNeeded($$1, $$3.toByteArray(), $$4.hash());
    }
}

