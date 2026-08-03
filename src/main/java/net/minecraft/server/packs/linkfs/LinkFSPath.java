package net.minecraft.server.packs.linkfs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

class LinkFSPath implements Path {
   private static final BasicFileAttributes DIRECTORY_ATTRIBUTES = new DummyFileAttributes() {
      @Override
      public boolean isRegularFile() {
         return false;
      }

      @Override
      public boolean isDirectory() {
         return true;
      }
   };
   private static final BasicFileAttributes FILE_ATTRIBUTES = new DummyFileAttributes() {
      @Override
      public boolean isRegularFile() {
         return true;
      }

      @Override
      public boolean isDirectory() {
         return false;
      }
   };
   private static final Comparator<LinkFSPath> PATH_COMPARATOR = Comparator.comparing(LinkFSPath::pathToString);
   private final String name;
   private final LinkFileSystem fileSystem;
   @Nullable
   private final LinkFSPath parent;
   @Nullable
   private List<String> pathToRoot;
   @Nullable
   private String pathString;
   private final PathContents pathContents;

   public LinkFSPath(LinkFileSystem $$0, String $$1, @Nullable LinkFSPath $$2, PathContents $$3) {
      this.fileSystem = $$0;
      this.name = $$1;
      this.parent = $$2;
      this.pathContents = $$3;
   }

   private LinkFSPath createRelativePath(@Nullable LinkFSPath $$0, String $$1) {
      return new LinkFSPath(this.fileSystem, $$1, $$0, PathContents.RELATIVE);
   }

   public LinkFileSystem getFileSystem() {
      return this.fileSystem;
   }

   @Override
   public boolean isAbsolute() {
      return this.pathContents != PathContents.RELATIVE;
   }

   @Override
   public File toFile() {
      if (this.pathContents instanceof PathContents.FileContents $$0) {
         return $$0.contents().toFile();
      } else {
         throw new UnsupportedOperationException("Path " + this.pathToString() + " does not represent file");
      }
   }

   @Nullable
   public LinkFSPath getRoot() {
      return this.isAbsolute() ? this.fileSystem.rootPath() : null;
   }

   public LinkFSPath getFileName() {
      return this.createRelativePath(null, this.name);
   }

   @Nullable
   public LinkFSPath getParent() {
      return this.parent;
   }

   @Override
   public int getNameCount() {
      return this.pathToRoot().size();
   }

   private List<String> pathToRoot() {
      if (this.name.isEmpty()) {
         return List.of();
      } else {
         if (this.pathToRoot == null) {
            Builder<String> $$0 = ImmutableList.builder();
            if (this.parent != null) {
               $$0.addAll(this.parent.pathToRoot());
            }

            $$0.add(this.name);
            this.pathToRoot = $$0.build();
         }

         return this.pathToRoot;
      }
   }

   public LinkFSPath getName(int $$0) {
      List<String> $$1 = this.pathToRoot();
      if ($$0 >= 0 && $$0 < $$1.size()) {
         return this.createRelativePath(null, $$1.get($$0));
      } else {
         throw new IllegalArgumentException("Invalid index: " + $$0);
      }
   }

   public LinkFSPath subpath(int $$0, int $$1) {
      List<String> $$2 = this.pathToRoot();
      if ($$0 >= 0 && $$1 <= $$2.size() && $$0 < $$1) {
         LinkFSPath $$3 = null;

         for (int $$4 = $$0; $$4 < $$1; $$4++) {
            $$3 = this.createRelativePath($$3, $$2.get($$4));
         }

         return $$3;
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public boolean startsWith(Path $$0) {
      if ($$0.isAbsolute() != this.isAbsolute()) {
         return false;
      } else if ($$0 instanceof LinkFSPath $$1) {
         if ($$1.fileSystem != this.fileSystem) {
            return false;
         } else {
            List<String> $$2 = this.pathToRoot();
            List<String> $$3 = $$1.pathToRoot();
            int $$4 = $$3.size();
            if ($$4 > $$2.size()) {
               return false;
            } else {
               for (int $$5 = 0; $$5 < $$4; $$5++) {
                  if (!$$3.get($$5).equals($$2.get($$5))) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean endsWith(Path $$0) {
      if ($$0.isAbsolute() && !this.isAbsolute()) {
         return false;
      } else if ($$0 instanceof LinkFSPath $$1) {
         if ($$1.fileSystem != this.fileSystem) {
            return false;
         } else {
            List<String> $$2 = this.pathToRoot();
            List<String> $$3 = $$1.pathToRoot();
            int $$4 = $$3.size();
            int $$5 = $$2.size() - $$4;
            if ($$5 < 0) {
               return false;
            } else {
               for (int $$6 = $$4 - 1; $$6 >= 0; $$6--) {
                  if (!$$3.get($$6).equals($$2.get($$5 + $$6))) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public LinkFSPath normalize() {
      return this;
   }

   public LinkFSPath resolve(Path $$0) {
      LinkFSPath $$1 = this.toLinkPath($$0);
      return $$0.isAbsolute() ? $$1 : this.resolve($$1.pathToRoot());
   }

   private LinkFSPath resolve(List<String> $$0) {
      LinkFSPath $$1 = this;

      for (String $$2 : $$0) {
         $$1 = $$1.resolveName($$2);
      }

      return $$1;
   }

   LinkFSPath resolveName(String $$0) {
      if (isRelativeOrMissing(this.pathContents)) {
         return new LinkFSPath(this.fileSystem, $$0, this, this.pathContents);
      } else if (this.pathContents instanceof PathContents.DirectoryContents $$1) {
         LinkFSPath $$2 = $$1.children().get($$0);
         return $$2 != null ? $$2 : new LinkFSPath(this.fileSystem, $$0, this, PathContents.MISSING);
      } else if (this.pathContents instanceof PathContents.FileContents) {
         return new LinkFSPath(this.fileSystem, $$0, this, PathContents.MISSING);
      } else {
         throw new AssertionError("All content types should be already handled");
      }
   }

   private static boolean isRelativeOrMissing(PathContents $$0) {
      return $$0 == PathContents.MISSING || $$0 == PathContents.RELATIVE;
   }

   public LinkFSPath relativize(Path $$0) {
      LinkFSPath $$1 = this.toLinkPath($$0);
      if (this.isAbsolute() != $$1.isAbsolute()) {
         throw new IllegalArgumentException("absolute mismatch");
      } else {
         List<String> $$2 = this.pathToRoot();
         List<String> $$3 = $$1.pathToRoot();
         if ($$2.size() >= $$3.size()) {
            throw new IllegalArgumentException();
         } else {
            for (int $$4 = 0; $$4 < $$2.size(); $$4++) {
               if (!$$2.get($$4).equals($$3.get($$4))) {
                  throw new IllegalArgumentException();
               }
            }

            return $$1.subpath($$2.size(), $$3.size());
         }
      }
   }

   @Override
   public URI toUri() {
      try {
         return new URI("x-mc-link", this.fileSystem.store().name(), this.pathToString(), null);
      } catch (URISyntaxException var2) {
         throw new AssertionError("Failed to create URI", var2);
      }
   }

   public LinkFSPath toAbsolutePath() {
      return this.isAbsolute() ? this : this.fileSystem.rootPath().resolve(this);
   }

   public LinkFSPath toRealPath(LinkOption... $$0) {
      return this.toAbsolutePath();
   }

   @Override
   public WatchKey register(WatchService $$0, Kind<?>[] $$1, Modifier... $$2) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int compareTo(Path $$0) {
      LinkFSPath $$1 = this.toLinkPath($$0);
      return PATH_COMPARATOR.compare(this, $$1);
   }

   @Override
   public boolean equals(Object $$0) {
      if ($$0 == this) {
         return true;
      } else if ($$0 instanceof LinkFSPath $$1) {
         if (this.fileSystem != $$1.fileSystem) {
            return false;
         } else {
            boolean $$2 = this.hasRealContents();
            if ($$2 != $$1.hasRealContents()) {
               return false;
            } else {
               return $$2 ? this.pathContents == $$1.pathContents : Objects.equals(this.parent, $$1.parent) && Objects.equals(this.name, $$1.name);
            }
         }
      } else {
         return false;
      }
   }

   private boolean hasRealContents() {
      return !isRelativeOrMissing(this.pathContents);
   }

   @Override
   public int hashCode() {
      return this.hasRealContents() ? this.pathContents.hashCode() : this.name.hashCode();
   }

   @Override
   public String toString() {
      return this.pathToString();
   }

   private String pathToString() {
      if (this.pathString == null) {
         StringBuilder $$0 = new StringBuilder();
         if (this.isAbsolute()) {
            $$0.append("/");
         }

         Joiner.on("/").appendTo($$0, this.pathToRoot());
         this.pathString = $$0.toString();
      }

      return this.pathString;
   }

   private LinkFSPath toLinkPath(@Nullable Path $$0) {
      if ($$0 == null) {
         throw new NullPointerException();
      } else if ($$0 instanceof LinkFSPath $$1 && $$1.fileSystem == this.fileSystem) {
         return $$1;
      } else {
         throw new ProviderMismatchException();
      }
   }

   public boolean exists() {
      return this.hasRealContents();
   }

   @Nullable
   public Path getTargetPath() {
      return this.pathContents instanceof PathContents.FileContents $$0 ? $$0.contents() : null;
   }

   @Nullable
   public PathContents.DirectoryContents getDirectoryContents() {
      return this.pathContents instanceof PathContents.DirectoryContents $$0 ? $$0 : null;
   }

   public BasicFileAttributeView getBasicAttributeView() {
      return new BasicFileAttributeView() {
         @Override
         public String name() {
            return "basic";
         }

         @Override
         public BasicFileAttributes readAttributes() throws IOException {
            return LinkFSPath.this.getBasicAttributes();
         }

         @Override
         public void setTimes(FileTime $$0, FileTime $$1, FileTime $$2) {
            throw new ReadOnlyFileSystemException();
         }
      };
   }

   public BasicFileAttributes getBasicAttributes() throws IOException {
      if (this.pathContents instanceof PathContents.DirectoryContents) {
         return DIRECTORY_ATTRIBUTES;
      } else if (this.pathContents instanceof PathContents.FileContents) {
         return FILE_ATTRIBUTES;
      } else {
         throw new NoSuchFileException(this.pathToString());
      }
   }
}
