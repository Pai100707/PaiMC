package net.minecraft.server.packs.linkfs;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.StandardOpenOption;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

class LinkFSProvider extends FileSystemProvider {
   public static final String SCHEME = "x-mc-link";

   @Override
   public String getScheme() {
      return "x-mc-link";
   }

   @Override
   public FileSystem newFileSystem(URI $$0, Map<String, ?> $$1) {
      throw new UnsupportedOperationException();
   }

   @Override
   public FileSystem getFileSystem(URI $$0) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Path getPath(URI $$0) {
      throw new UnsupportedOperationException();
   }

   @Override
   public SeekableByteChannel newByteChannel(Path $$0, Set<? extends OpenOption> $$1, FileAttribute<?>... $$2) throws IOException {
      if (!$$1.contains(StandardOpenOption.CREATE_NEW)
         && !$$1.contains(StandardOpenOption.CREATE)
         && !$$1.contains(StandardOpenOption.APPEND)
         && !$$1.contains(StandardOpenOption.WRITE)) {
         Path $$3 = toLinkPath($$0).toAbsolutePath().getTargetPath();
         if ($$3 == null) {
            throw new NoSuchFileException($$0.toString());
         } else {
            return Files.newByteChannel($$3, $$1, $$2);
         }
      } else {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   public DirectoryStream<Path> newDirectoryStream(Path $$0, final Filter<? super Path> $$1) throws IOException {
      final PathContents.DirectoryContents $$2 = toLinkPath($$0).toAbsolutePath().getDirectoryContents();
      if ($$2 == null) {
         throw new NotDirectoryException($$0.toString());
      } else {
         return new DirectoryStream<Path>() {
            @Override
            public Iterator<Path> iterator() {
               return $$2.children().values().stream().filter($$1xx -> {
                  try {
                     return $$1.accept($$1xx);
                  } catch (IOException var3) {
                     throw new DirectoryIteratorException(var3);
                  }
               }).map($$0x -> (Path)$$0x).iterator();
            }

            @Override
            public void close() {
            }
         };
      }
   }

   @Override
   public void createDirectory(Path $$0, FileAttribute<?>... $$1) {
      throw new ReadOnlyFileSystemException();
   }

   @Override
   public void delete(Path $$0) {
      throw new ReadOnlyFileSystemException();
   }

   @Override
   public void copy(Path $$0, Path $$1, CopyOption... $$2) {
      throw new ReadOnlyFileSystemException();
   }

   @Override
   public void move(Path $$0, Path $$1, CopyOption... $$2) {
      throw new ReadOnlyFileSystemException();
   }

   @Override
   public boolean isSameFile(Path $$0, Path $$1) {
      return $$0 instanceof LinkFSPath && $$1 instanceof LinkFSPath && $$0.equals($$1);
   }

   @Override
   public boolean isHidden(Path $$0) {
      return false;
   }

   @Override
   public FileStore getFileStore(Path $$0) {
      return toLinkPath($$0).getFileSystem().store();
   }

   @Override
   public void checkAccess(Path $$0, AccessMode... $$1) throws IOException {
      if ($$1.length == 0 && !toLinkPath($$0).exists()) {
         throw new NoSuchFileException($$0.toString());
      } else {
         AccessMode[] var3 = $$1;
         int var4 = $$1.length;
         int var5 = 0;

         while (var5 < var4) {
            AccessMode $$2 = var3[var5];
            switch ($$2) {
               case READ:
                  if (!toLinkPath($$0).exists()) {
                     throw new NoSuchFileException($$0.toString());
                  }
               default:
                  var5++;
                  break;
               case EXECUTE:
               case WRITE:
                  throw new AccessDeniedException($$2.toString());
            }
         }
      }
   }

   @Nullable
   @Override
   public <V extends FileAttributeView> V getFileAttributeView(Path $$0, Class<V> $$1, LinkOption... $$2) {
      LinkFSPath $$3 = toLinkPath($$0);
      return (V)($$1 == BasicFileAttributeView.class ? $$3.getBasicAttributeView() : null);
   }

   @Override
   public <A extends BasicFileAttributes> A readAttributes(Path $$0, Class<A> $$1, LinkOption... $$2) throws IOException {
      LinkFSPath $$3 = toLinkPath($$0).toAbsolutePath();
      if ($$1 == BasicFileAttributes.class) {
         return (A)$$3.getBasicAttributes();
      } else {
         throw new UnsupportedOperationException("Attributes of type " + $$1.getName() + " not supported");
      }
   }

   @Override
   public Map<String, Object> readAttributes(Path $$0, String $$1, LinkOption... $$2) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setAttribute(Path $$0, String $$1, Object $$2, LinkOption... $$3) {
      throw new ReadOnlyFileSystemException();
   }

   private static LinkFSPath toLinkPath(@Nullable Path $$0) {
      if ($$0 == null) {
         throw new NullPointerException();
      } else if ($$0 instanceof LinkFSPath $$1) {
         return $$1;
      } else {
         throw new ProviderMismatchException();
      }
   }
}
