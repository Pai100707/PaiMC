/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class FileZipper
implements Closeable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path outputFile;
    private final Path tempFile;
    private final FileSystem fs;

    public FileZipper(Path $$0) {
        this.outputFile = $$0;
        this.tempFile = $$0.resolveSibling($$0.getFileName().toString() + "_tmp");
        try {
            this.fs = Util.ZIP_FILE_SYSTEM_PROVIDER.newFileSystem(this.tempFile, (Map<String, ?>)ImmutableMap.of((Object)"create", (Object)"true"));
        }
        catch (IOException $$1) {
            throw new UncheckedIOException($$1);
        }
    }

    public void add(Path $$0, String $$1) {
        try {
            Path $$2 = this.fs.getPath(File.separator, new String[0]);
            Path $$3 = $$2.resolve($$0.toString());
            Files.createDirectories($$3.getParent(), new FileAttribute[0]);
            Files.write($$3, $$1.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        }
        catch (IOException $$4) {
            throw new UncheckedIOException($$4);
        }
    }

    public void add(Path $$0, File $$1) {
        try {
            Path $$2 = this.fs.getPath(File.separator, new String[0]);
            Path $$3 = $$2.resolve($$0.toString());
            Files.createDirectories($$3.getParent(), new FileAttribute[0]);
            Files.copy($$1.toPath(), $$3, new CopyOption[0]);
        }
        catch (IOException $$4) {
            throw new UncheckedIOException($$4);
        }
    }

    public void add(Path $$02) {
        try {
            Path $$12 = this.fs.getPath(File.separator, new String[0]);
            if (Files.isRegularFile($$02, new LinkOption[0])) {
                Path $$2 = $$12.resolve($$02.getParent().relativize($$02).toString());
                Files.copy($$2, $$02, new CopyOption[0]);
                return;
            }
            try (Stream<Path> $$3 = Files.find($$02, Integer.MAX_VALUE, ($$0, $$1) -> $$1.isRegularFile(), new FileVisitOption[0]);){
                for (Path $$4 : $$3.collect(Collectors.toList())) {
                    Path $$5 = $$12.resolve($$02.relativize($$4).toString());
                    Files.createDirectories($$5.getParent(), new FileAttribute[0]);
                    Files.copy($$4, $$5, new CopyOption[0]);
                }
            }
        }
        catch (IOException $$6) {
            throw new UncheckedIOException($$6);
        }
    }

    @Override
    public void close() {
        try {
            this.fs.close();
            Files.move(this.tempFile, this.outputFile, new CopyOption[0]);
            LOGGER.info("Compressed to {}", (Object)this.outputFile);
        }
        catch (IOException $$0) {
            throw new UncheckedIOException($$0);
        }
    }
}

