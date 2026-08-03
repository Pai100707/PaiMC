package net.minecraft.util.eventlog;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
      Files.createDirectories($$0);
      return new EventLogDirectory($$0, $$1);
   }

   public EventLogDirectory.FileList listFiles() throws IOException {
      EventLogDirectory.FileList var2;
      try (Stream<Path> $$0 = Files.list(this.root)) {
         var2 = new EventLogDirectory.FileList($$0.filter($$0x -> Files.isRegularFile($$0x)).map(this::parseFile).filter(Objects::nonNull).toList());
      }

      return var2;
   }

   @Nullable
   private EventLogDirectory.File parseFile(Path $$0) {
      String $$1 = $$0.getFileName().toString();
      int $$2 = $$1.indexOf(46);
      if ($$2 == -1) {
         return null;
      } else {
         EventLogDirectory.FileId $$3 = EventLogDirectory.FileId.parse($$1.substring(0, $$2));
         if ($$3 != null) {
            String $$4 = $$1.substring($$2);
            if ($$4.equals(this.extension)) {
               return new EventLogDirectory.RawFile($$0, $$3);
            }

            if ($$4.equals(this.extension + ".gz")) {
               return new EventLogDirectory.CompressedFile($$0, $$3);
            }
         }

         return null;
      }
   }

   static void tryCompress(Path $$0, Path $$1) throws IOException {
      if (Files.exists($$1)) {
         throw new IOException("Compressed target file already exists: " + $$1);
      } else {
         try (FileChannel $$2 = FileChannel.open($$0, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            FileLock $$3 = $$2.tryLock();
            if ($$3 == null) {
               throw new IOException("Raw log file is already locked, cannot compress: " + $$0);
            }

            writeCompressed($$2, $$1);
            $$2.truncate(0L);
         }

         Files.delete($$0);
      }
   }

   private static void writeCompressed(ReadableByteChannel $$0, Path $$1) throws IOException {
      try (OutputStream $$2 = new GZIPOutputStream(Files.newOutputStream($$1))) {
         byte[] $$3 = new byte[4096];
         ByteBuffer $$4 = ByteBuffer.wrap($$3);

         while ($$0.read($$4) >= 0) {
            $$4.flip();
            $$2.write($$3, 0, $$4.limit());
            $$4.clear();
         }
      }
   }

   public EventLogDirectory.RawFile createNewFile(LocalDate $$0) throws IOException {
      int $$1 = 1;
      Set<EventLogDirectory.FileId> $$2 = this.listFiles().ids();

      EventLogDirectory.FileId $$3;
      do {
         $$3 = new EventLogDirectory.FileId($$0, $$1++);
      } while ($$2.contains($$3));

      EventLogDirectory.RawFile $$4 = new EventLogDirectory.RawFile(this.root.resolve($$3.toFileName(this.extension)), $$3);
      Files.createFile($$4.path());
      return $$4;
   }

   public record CompressedFile(Path path, EventLogDirectory.FileId id) implements EventLogDirectory.File {
      @Nullable
      @Override
      public Reader openReader() throws IOException {
         return !Files.exists(this.path)
            ? null
            : new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(this.path)), StandardCharsets.UTF_8));
      }

      @Override
      public EventLogDirectory.CompressedFile compress() {
         return this;
      }
   }

   public interface File {
      Path path();

      EventLogDirectory.FileId id();

      @Nullable
      Reader openReader() throws IOException;

      EventLogDirectory.CompressedFile compress() throws IOException;
   }

   public record FileId(LocalDate date, int index) {
      private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

      @Nullable
      public static EventLogDirectory.FileId parse(String $$0) {
         int $$1 = $$0.indexOf("-");
         if ($$1 == -1) {
            return null;
         } else {
            String $$2 = $$0.substring(0, $$1);
            String $$3 = $$0.substring($$1 + 1);

            try {
               return new EventLogDirectory.FileId(LocalDate.parse($$2, DATE_FORMATTER), Integer.parseInt($$3));
            } catch (DateTimeParseException | NumberFormatException var5) {
               return null;
            }
         }
      }

      @Override
      public String toString() {
         return DATE_FORMATTER.format(this.date) + "-" + this.index;
      }

      public String toFileName(String $$0) {
         return this + $$0;
      }
   }

   public static class FileList implements Iterable<EventLogDirectory.File> {
      private final List<EventLogDirectory.File> files;

      FileList(List<EventLogDirectory.File> $$0) {
         this.files = new ArrayList<>($$0);
      }

      public EventLogDirectory.FileList prune(LocalDate $$0, int $$1) {
         this.files.removeIf($$2 -> {
            EventLogDirectory.FileId $$3 = $$2.id();
            LocalDate $$4 = $$3.date().plusDays($$1);
            if (!$$0.isBefore($$4)) {
               try {
                  Files.delete($$2.path());
                  return true;
               } catch (IOException var6) {
                  EventLogDirectory.LOGGER.warn("Failed to delete expired event log file: {}", $$2.path(), var6);
               }
            }

            return false;
         });
         return this;
      }

      public EventLogDirectory.FileList compressAll() {
         ListIterator<EventLogDirectory.File> $$0 = this.files.listIterator();

         while ($$0.hasNext()) {
            EventLogDirectory.File $$1 = $$0.next();

            try {
               $$0.set($$1.compress());
            } catch (IOException var4) {
               EventLogDirectory.LOGGER.warn("Failed to compress event log file: {}", $$1.path(), var4);
            }
         }

         return this;
      }

      @Override
      public Iterator<EventLogDirectory.File> iterator() {
         return this.files.iterator();
      }

      public Stream<EventLogDirectory.File> stream() {
         return this.files.stream();
      }

      public Set<EventLogDirectory.FileId> ids() {
         return this.files.stream().map(EventLogDirectory.File::id).collect(Collectors.toSet());
      }
   }

   public record RawFile(Path path, EventLogDirectory.FileId id) implements EventLogDirectory.File {
      public FileChannel openChannel() throws IOException {
         return FileChannel.open(this.path, StandardOpenOption.WRITE, StandardOpenOption.READ);
      }

      @Nullable
      @Override
      public Reader openReader() throws IOException {
         return Files.exists(this.path) ? Files.newBufferedReader(this.path) : null;
      }

      @Override
      public EventLogDirectory.CompressedFile compress() throws IOException {
         Path $$0 = this.path.resolveSibling(this.path.getFileName().toString() + ".gz");
         EventLogDirectory.tryCompress(this.path, $$0);
         return new EventLogDirectory.CompressedFile($$0, this.id);
      }
   }
}
