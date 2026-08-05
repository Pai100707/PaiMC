/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.eventlog;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class EventLogDirectory {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int COMPRESS_BUFFER_SIZE = 4096;
    private static final String COMPRESSED_EXTENSION = ".gz";
    private final Path root;
    private final String extension;

    private EventLogDirectory(Path $$0, String $$1) {
        this.root = $$0;
        this.extension = $$1;
    }

    public static EventLogDirectory open(Path $$0, String $$1) throws IOException {
        Files.createDirectories($$0, new FileAttribute[0]);
        return new EventLogDirectory($$0, $$1);
    }

    public FileList listFiles() throws IOException {
        try (Stream<Path> $$02 = Files.list(this.root);){
            FileList fileList = new FileList($$02.filter($$0 -> Files.isRegularFile($$0, new LinkOption[0])).map(this::parseFile).filter(Objects::nonNull).toList());
            return fileList;
        }
    }

    private @Nullable File parseFile(Path $$0) {
        String $$1 = $$0.getFileName().toString();
        int $$2 = $$1.indexOf(46);
        if ($$2 == -1) {
            return null;
        }
        FileId $$3 = FileId.parse($$1.substring(0, $$2));
        if ($$3 != null) {
            String $$4 = $$1.substring($$2);
            if ($$4.equals(this.extension)) {
                return new RawFile($$0, $$3);
            }
            if ($$4.equals(this.extension + COMPRESSED_EXTENSION)) {
                return new CompressedFile($$0, $$3);
            }
        }
        return null;
    }

    static void tryCompress(Path $$0, Path $$1) throws IOException {
        if (Files.exists($$1, new LinkOption[0])) {
            throw new IOException("Compressed target file already exists: " + String.valueOf($$1));
        }
        try (FileChannel $$2 = FileChannel.open($$0, StandardOpenOption.WRITE, StandardOpenOption.READ);){
            FileLock $$3 = $$2.tryLock();
            if ($$3 == null) {
                throw new IOException("Raw log file is already locked, cannot compress: " + String.valueOf($$0));
            }
            EventLogDirectory.writeCompressed($$2, $$1);
            $$2.truncate(0L);
        }
        Files.delete($$0);
    }

    private static void writeCompressed(ReadableByteChannel $$0, Path $$1) throws IOException {
        try (GZIPOutputStream $$2 = new GZIPOutputStream(Files.newOutputStream($$1, new OpenOption[0]));){
            byte[] $$3 = new byte[4096];
            ByteBuffer $$4 = ByteBuffer.wrap($$3);
            while ($$0.read($$4) >= 0) {
                $$4.flip();
                ((OutputStream)$$2).write($$3, 0, $$4.limit());
                $$4.clear();
            }
        }
    }

    public RawFile createNewFile(LocalDate $$0) throws IOException {
        FileId $$3;
        int $$1 = 1;
        Set<FileId> $$2 = this.listFiles().ids();
        while ($$2.contains($$3 = new FileId($$0, $$1++))) {
        }
        RawFile $$4 = new RawFile(this.root.resolve($$3.toFileName(this.extension)), $$3);
        Files.createFile($$4.path(), new FileAttribute[0]);
        return $$4;
    }

    public static class FileList
    implements Iterable<File> {
        private final List<File> files;

        FileList(List<File> $$0) {
            this.files = new ArrayList<File>($$0);
        }

        public FileList prune(LocalDate $$0, int $$1) {
            this.files.removeIf($$2 -> {
                FileId $$3 = $$2.id();
                LocalDate $$4 = $$3.date().plusDays($$1);
                if (!$$0.isBefore($$4)) {
                    try {
                        Files.delete($$2.path());
                        return true;
                    }
                    catch (IOException $$5) {
                        LOGGER.warn("Failed to delete expired event log file: {}", (Object)$$2.path(), (Object)$$5);
                    }
                }
                return false;
            });
            return this;
        }

        public FileList compressAll() {
            ListIterator<File> $$0 = this.files.listIterator();
            while ($$0.hasNext()) {
                File $$1 = $$0.next();
                try {
                    $$0.set($$1.compress());
                }
                catch (IOException $$2) {
                    LOGGER.warn("Failed to compress event log file: {}", (Object)$$1.path(), (Object)$$2);
                }
            }
            return this;
        }

        @Override
        public Iterator<File> iterator() {
            return this.files.iterator();
        }

        public Stream<File> stream() {
            return this.files.stream();
        }

        public Set<FileId> ids() {
            return this.files.stream().map(File::id).collect(Collectors.toSet());
        }
    }

    public record FileId(LocalDate date, int index) {
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

        public static @Nullable FileId parse(String $$0) {
            int $$1 = $$0.indexOf("-");
            if ($$1 == -1) {
                return null;
            }
            String $$2 = $$0.substring(0, $$1);
            String $$3 = $$0.substring($$1 + 1);
            try {
                return new FileId(LocalDate.parse($$2, DATE_FORMATTER), Integer.parseInt($$3));
            }
            catch (NumberFormatException | DateTimeParseException $$4) {
                return null;
            }
        }

        @Override
        public String toString() {
            return DATE_FORMATTER.format(this.date) + "-" + this.index;
        }

        public String toFileName(String $$0) {
            return String.valueOf(this) + $$0;
        }
    }

    public record RawFile(Path path, FileId id) implements File
    {
        public FileChannel openChannel() throws IOException {
            return FileChannel.open(this.path, StandardOpenOption.WRITE, StandardOpenOption.READ);
        }

        @Override
        public @Nullable Reader openReader() throws IOException {
            return Files.exists(this.path, new LinkOption[0]) ? Files.newBufferedReader(this.path) : null;
        }

        @Override
        public CompressedFile compress() throws IOException {
            Path $$0 = this.path.resolveSibling(this.path.getFileName().toString() + EventLogDirectory.COMPRESSED_EXTENSION);
            EventLogDirectory.tryCompress(this.path, $$0);
            return new CompressedFile($$0, this.id);
        }
    }

    public record CompressedFile(Path path, FileId id) implements File
    {
        @Override
        public @Nullable Reader openReader() throws IOException {
            if (!Files.exists(this.path, new LinkOption[0])) {
                return null;
            }
            return new BufferedReader(new InputStreamReader((InputStream)new GZIPInputStream(Files.newInputStream(this.path, new OpenOption[0])), StandardCharsets.UTF_8));
        }

        @Override
        public CompressedFile compress() {
            return this;
        }
    }

    public static interface File {
        public Path path();

        public FileId id();

        public @Nullable Reader openReader() throws IOException;

        public CompressedFile compress() throws IOException;
    }
}

