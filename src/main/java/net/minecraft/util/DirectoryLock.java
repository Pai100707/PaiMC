package net.minecraft.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DirectoryLock implements AutoCloseable {
   public static final String LOCK_FILE = "session.lock";
   private final FileChannel lockFile;
   private final FileLock lock;
   private static final ByteBuffer DUMMY;

   public static net.minecraft.util.DirectoryLock create(Path $$0) throws IOException {
      Path $$1 = $$0.resolve("session.lock");
      net.minecraft.util.FileUtil.createDirectoriesSafe($$0);
      FileChannel $$2 = FileChannel.open($$1, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

      try {
         $$2.write(DUMMY.duplicate());
         $$2.force(true);
         FileLock $$3 = $$2.tryLock();
         if ($$3 == null) {
            throw net.minecraft.util.DirectoryLock.LockException.alreadyLocked($$1);
         } else {
            return new net.minecraft.util.DirectoryLock($$2, $$3);
         }
      } catch (IOException var6) {
         try {
            $$2.close();
         } catch (IOException var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }
   }

   private DirectoryLock(FileChannel $$0, FileLock $$1) {
      this.lockFile = $$0;
      this.lock = $$1;
   }

   @Override
   public void close() throws IOException {
      try {
         if (this.lock.isValid()) {
            this.lock.release();
         }
      } finally {
         if (this.lockFile.isOpen()) {
            this.lockFile.close();
         }
      }
   }

   public boolean isValid() {
      return this.lock.isValid();
   }

   public static boolean isLocked(Path $$0) throws IOException {
      Path $$1 = $$0.resolve("session.lock");

      try {
         boolean var4;
         try (
            FileChannel $$2 = FileChannel.open($$1, StandardOpenOption.WRITE);
            FileLock $$3 = $$2.tryLock();
         ) {
            var4 = $$3 == null;
         }

         return var4;
      } catch (AccessDeniedException var10) {
         return true;
      } catch (NoSuchFileException var11) {
         return false;
      }
   }

   static {
      byte[] $$0 = "☃".getBytes(StandardCharsets.UTF_8);
      DUMMY = ByteBuffer.allocateDirect($$0.length);
      DUMMY.put($$0);
      DUMMY.flip();
   }

   public static class LockException extends IOException {
      private LockException(Path $$0, String $$1) {
         super($$0.toAbsolutePath() + ": " + $$1);
      }

      public static net.minecraft.util.DirectoryLock.LockException alreadyLocked(Path $$0) {
         return new net.minecraft.util.DirectoryLock.LockException($$0, "already locked (possibly by other Minecraft instance?)");
      }
   }
}
