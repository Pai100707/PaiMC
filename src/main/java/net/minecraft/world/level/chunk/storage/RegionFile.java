package net.minecraft.world.level.chunk.storage;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class RegionFile implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SECTOR_BYTES = 4096;
   @VisibleForTesting
   protected static final int SECTOR_INTS = 1024;
   private static final int CHUNK_HEADER_SIZE = 5;
   private static final int HEADER_OFFSET = 0;
   private static final ByteBuffer PADDING_BUFFER = ByteBuffer.allocateDirect(1);
   private static final String EXTERNAL_FILE_EXTENSION = ".mcc";
   private static final int EXTERNAL_STREAM_FLAG = 128;
   private static final int EXTERNAL_CHUNK_THRESHOLD = 256;
   private static final int CHUNK_NOT_PRESENT = 0;
   final RegionStorageInfo info;
   private final Path path;
   private final FileChannel file;
   private final Path externalFileDir;
   final RegionFileVersion version;
   private final ByteBuffer header = ByteBuffer.allocateDirect(8192);
   private final IntBuffer offsets;
   private final IntBuffer timestamps;
   @VisibleForTesting
   protected final RegionBitmap usedSectors = new RegionBitmap();

   public RegionFile(RegionStorageInfo $$0, Path $$1, Path $$2, boolean $$3) throws IOException {
      this($$0, $$1, $$2, RegionFileVersion.getSelected(), $$3);
   }

   public RegionFile(RegionStorageInfo $$0, Path $$1, Path $$2, RegionFileVersion $$3, boolean $$4) throws IOException {
      this.info = $$0;
      this.path = $$1;
      this.version = $$3;
      if (!Files.isDirectory($$2)) {
         throw new IllegalArgumentException("Expected directory, got " + $$2.toAbsolutePath());
      } else {
         this.externalFileDir = $$2;
         this.offsets = this.header.asIntBuffer();
         this.offsets.limit(1024);
         this.header.position(4096);
         this.timestamps = this.header.asIntBuffer();
         if ($$4) {
            this.file = FileChannel.open($$1, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
         } else {
            this.file = FileChannel.open($$1, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
         }

         this.usedSectors.force(0, 2);
         this.header.position(0);
         int $$5 = this.file.read(this.header, 0L);
         if ($$5 != -1) {
            if ($$5 != 8192) {
               LOGGER.warn("Region file {} has truncated header: {}", $$1, $$5);
            }

            long $$6 = Files.size($$1);

            for (int $$7 = 0; $$7 < 1024; $$7++) {
               int $$8 = this.offsets.get($$7);
               if ($$8 != 0) {
                  int $$9 = getSectorNumber($$8);
                  int $$10 = getNumSectors($$8);
                  if ($$9 < 2) {
                     LOGGER.warn("Region file {} has invalid sector at index: {}; sector {} overlaps with header", new Object[]{$$1, $$7, $$9});
                     this.offsets.put($$7, 0);
                  } else if ($$10 == 0) {
                     LOGGER.warn("Region file {} has an invalid sector at index: {}; size has to be > 0", $$1, $$7);
                     this.offsets.put($$7, 0);
                  } else if ($$9 * 4096L > $$6) {
                     LOGGER.warn("Region file {} has an invalid sector at index: {}; sector {} is out of bounds", new Object[]{$$1, $$7, $$9});
                     this.offsets.put($$7, 0);
                  } else {
                     this.usedSectors.force($$9, $$10);
                  }
               }
            }
         }
      }
   }

   public Path getPath() {
      return this.path;
   }

   private Path getExternalChunkPath(net.minecraft.world.level.ChunkPos $$0) {
      String $$1 = "c." + $$0.x + "." + $$0.z + ".mcc";
      return this.externalFileDir.resolve($$1);
   }

   
   public synchronized DataInputStream getChunkDataInputStream(net.minecraft.world.level.ChunkPos $$0) throws IOException {
      int $$1 = this.getOffset($$0);
      if ($$1 == 0) {
         return null;
      } else {
         int $$2 = getSectorNumber($$1);
         int $$3 = getNumSectors($$1);
         int $$4 = $$3 * 4096;
         ByteBuffer $$5 = ByteBuffer.allocate($$4);
         this.file.read($$5, $$2 * 4096);
         $$5.flip();
         if ($$5.remaining() < 5) {
            LOGGER.error("Chunk {} header is truncated: expected {} but read {}", new Object[]{$$0, $$4, $$5.remaining()});
            return null;
         } else {
            int $$6 = $$5.getInt();
            byte $$7 = $$5.get();
            if ($$6 == 0) {
               LOGGER.warn("Chunk {} is allocated, but stream is missing", $$0);
               return null;
            } else {
               int $$8 = $$6 - 1;
               if (isExternalStreamChunk($$7)) {
                  if ($$8 != 0) {
                     LOGGER.warn("Chunk has both internal and external streams");
                  }

                  return this.createExternalChunkInputStream($$0, getExternalChunkVersion($$7));
               } else if ($$8 > $$5.remaining()) {
                  LOGGER.error("Chunk {} stream is truncated: expected {} but read {}", new Object[]{$$0, $$8, $$5.remaining()});
                  return null;
               } else if ($$8 < 0) {
                  LOGGER.error("Declared size {} of chunk {} is negative", $$6, $$0);
                  return null;
               } else {
                  JvmProfiler.INSTANCE.onRegionFileRead(this.info, $$0, this.version, $$8);
                  return this.createChunkInputStream($$0, $$7, createStream($$5, $$8));
               }
            }
         }
      }
   }

   private static int getTimestamp() {
      return (int)(Util.getEpochMillis() / 1000L);
   }

   private static boolean isExternalStreamChunk(byte $$0) {
      return ($$0 & 128) != 0;
   }

   private static byte getExternalChunkVersion(byte $$0) {
      return (byte)($$0 & -129);
   }

   
   private DataInputStream createChunkInputStream(net.minecraft.world.level.ChunkPos $$0, byte $$1, InputStream $$2) throws IOException {
      RegionFileVersion $$3 = RegionFileVersion.fromId($$1);
      if ($$3 == RegionFileVersion.VERSION_CUSTOM) {
         String $$4 = new DataInputStream($$2).readUTF();
         Identifier $$5 = Identifier.tryParse($$4);
         if ($$5 != null) {
            LOGGER.error("Unrecognized custom compression {}", $$5);
            return null;
         } else {
            LOGGER.error("Invalid custom compression id {}", $$4);
            return null;
         }
      } else if ($$3 == null) {
         LOGGER.error("Chunk {} has invalid chunk stream version {}", $$0, $$1);
         return null;
      } else {
         return new DataInputStream($$3.wrap($$2));
      }
   }

   
   private DataInputStream createExternalChunkInputStream(net.minecraft.world.level.ChunkPos $$0, byte $$1) throws IOException {
      Path $$2 = this.getExternalChunkPath($$0);
      if (!Files.isRegularFile($$2)) {
         LOGGER.error("External chunk path {} is not file", $$2);
         return null;
      } else {
         return this.createChunkInputStream($$0, $$1, Files.newInputStream($$2));
      }
   }

   private static ByteArrayInputStream createStream(ByteBuffer $$0, int $$1) {
      return new ByteArrayInputStream($$0.array(), $$0.position(), $$1);
   }

   private int packSectorOffset(int $$0, int $$1) {
      return $$0 << 8 | $$1;
   }

   private static int getNumSectors(int $$0) {
      return $$0 & 0xFF;
   }

   private static int getSectorNumber(int $$0) {
      return $$0 >> 8 & 16777215;
   }

   private static int sizeToSectors(int $$0) {
      return ($$0 + 4096 - 1) / 4096;
   }

   public boolean doesChunkExist(net.minecraft.world.level.ChunkPos $$0) {
      int $$1 = this.getOffset($$0);
      if ($$1 == 0) {
         return false;
      } else {
         int $$2 = getSectorNumber($$1);
         int $$3 = getNumSectors($$1);
         ByteBuffer $$4 = ByteBuffer.allocate(5);

         try {
            this.file.read($$4, $$2 * 4096);
            $$4.flip();
            if ($$4.remaining() != 5) {
               return false;
            } else {
               int $$5 = $$4.getInt();
               byte $$6 = $$4.get();
               if (isExternalStreamChunk($$6)) {
                  if (!RegionFileVersion.isValidVersion(getExternalChunkVersion($$6))) {
                     return false;
                  }

                  if (!Files.isRegularFile(this.getExternalChunkPath($$0))) {
                     return false;
                  }
               } else {
                  if (!RegionFileVersion.isValidVersion($$6)) {
                     return false;
                  }

                  if ($$5 == 0) {
                     return false;
                  }

                  int $$7 = $$5 - 1;
                  if ($$7 < 0 || $$7 > 4096 * $$3) {
                     return false;
                  }
               }

               return true;
            }
         } catch (IOException var9) {
            return false;
         }
      }
   }

   public DataOutputStream getChunkDataOutputStream(net.minecraft.world.level.ChunkPos $$0) throws IOException {
      return new DataOutputStream(this.version.wrap(new RegionFile.ChunkBuffer($$0)));
   }

   public void flush() throws IOException {
      this.file.force(true);
   }

   public void clear(net.minecraft.world.level.ChunkPos $$0) throws IOException {
      int $$1 = getOffsetIndex($$0);
      int $$2 = this.offsets.get($$1);
      if ($$2 != 0) {
         this.offsets.put($$1, 0);
         this.timestamps.put($$1, getTimestamp());
         this.writeHeader();
         Files.deleteIfExists(this.getExternalChunkPath($$0));
         this.usedSectors.free(getSectorNumber($$2), getNumSectors($$2));
      }
   }

   protected synchronized void write(net.minecraft.world.level.ChunkPos $$0, ByteBuffer $$1) throws IOException {
      int $$2 = getOffsetIndex($$0);
      int $$3 = this.offsets.get($$2);
      int $$4 = getSectorNumber($$3);
      int $$5 = getNumSectors($$3);
      int $$6 = $$1.remaining();
      int $$7 = sizeToSectors($$6);
      int $$9;
      RegionFile.CommitOp $$10;
      if ($$7 >= 256) {
         Path $$8 = this.getExternalChunkPath($$0);
         LOGGER.warn("Saving oversized chunk {} ({} bytes} to external file {}", new Object[]{$$0, $$6, $$8});
         $$7 = 1;
         $$9 = this.usedSectors.allocate($$7);
         $$10 = this.writeToExternalFile($$8, $$1);
         ByteBuffer $$11 = this.createExternalStub();
         this.file.write($$11, $$9 * 4096);
      } else {
         $$9 = this.usedSectors.allocate($$7);
         $$10 = () -> Files.deleteIfExists(this.getExternalChunkPath($$0));
         this.file.write($$1, $$9 * 4096);
      }

      this.offsets.put($$2, this.packSectorOffset($$9, $$7));
      this.timestamps.put($$2, getTimestamp());
      this.writeHeader();
      $$10.run();
      if ($$4 != 0) {
         this.usedSectors.free($$4, $$5);
      }
   }

   private ByteBuffer createExternalStub() {
      ByteBuffer $$0 = ByteBuffer.allocate(5);
      $$0.putInt(1);
      $$0.put((byte)(this.version.getId() | 128));
      $$0.flip();
      return $$0;
   }

   private RegionFile.CommitOp writeToExternalFile(Path $$0, ByteBuffer $$1) throws IOException {
      Path $$2 = Files.createTempFile(this.externalFileDir, "tmp", null);

      try (FileChannel $$3 = FileChannel.open($$2, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
         $$1.position(5);
         $$3.write($$1);
      }

      return () -> Files.move($$2, $$0, StandardCopyOption.REPLACE_EXISTING);
   }

   private void writeHeader() throws IOException {
      this.header.position(0);
      this.file.write(this.header, 0L);
   }

   private int getOffset(net.minecraft.world.level.ChunkPos $$0) {
      return this.offsets.get(getOffsetIndex($$0));
   }

   public boolean hasChunk(net.minecraft.world.level.ChunkPos $$0) {
      return this.getOffset($$0) != 0;
   }

   private static int getOffsetIndex(net.minecraft.world.level.ChunkPos $$0) {
      return $$0.getRegionLocalX() + $$0.getRegionLocalZ() * 32;
   }

   @Override
   public void close() throws IOException {
      try {
         this.padToFullSector();
      } finally {
         try {
            this.file.force(true);
         } finally {
            this.file.close();
         }
      }
   }

   private void padToFullSector() throws IOException {
      int $$0 = (int)this.file.size();
      int $$1 = sizeToSectors($$0) * 4096;
      if ($$0 != $$1) {
         ByteBuffer $$2 = PADDING_BUFFER.duplicate();
         $$2.position(0);
         this.file.write($$2, $$1 - 1);
      }
   }

   class ChunkBuffer extends ByteArrayOutputStream {
      private final net.minecraft.world.level.ChunkPos pos;

      public ChunkBuffer(final net.minecraft.world.level.ChunkPos $$0) {
         super(8096);
         super.write(0);
         super.write(0);
         super.write(0);
         super.write(0);
         super.write(RegionFile.this.version.getId());
         this.pos = $$0;
      }

      @Override
      public void close() throws IOException {
         ByteBuffer $$0 = ByteBuffer.wrap(this.buf, 0, this.count);
         int $$1 = this.count - 5 + 1;
         JvmProfiler.INSTANCE.onRegionFileWrite(RegionFile.this.info, this.pos, RegionFile.this.version, $$1);
         $$0.putInt(0, $$1);
         RegionFile.this.write(this.pos, $$0);
      }
   }

   interface CommitOp {
      void run() throws IOException;
   }
}
