package net.minecraft.server.packs.linkfs;

import com.google.common.base.Splitter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LinkFileSystem extends FileSystem {
   private static final Set<String> VIEWS = Set.of("basic");
   public static final String PATH_SEPARATOR = "/";
   private static final Splitter PATH_SPLITTER = Splitter.on('/');
   private final FileStore store;
   private final FileSystemProvider provider = new LinkFSProvider();
   private final LinkFSPath root;

   LinkFileSystem(String $$0, LinkFileSystem.DirectoryEntry $$1) {
      this.store = new LinkFSFileStore($$0);
      this.root = buildPath($$1, this, "", null);
   }

   private static LinkFSPath buildPath(LinkFileSystem.DirectoryEntry $$0, LinkFileSystem $$1, String $$2, LinkFSPath $$3) {
      Object2ObjectOpenHashMap<String, LinkFSPath> $$4 = new Object2ObjectOpenHashMap();
      LinkFSPath $$5 = new LinkFSPath($$1, $$2, $$3, new PathContents.DirectoryContents($$4));
      $$0.files.forEach(($$3x, $$4x) -> $$4.put($$3x, new LinkFSPath($$1, $$3x, $$5, new PathContents.FileContents($$4x))));
      $$0.children.forEach(($$3x, $$4x) -> $$4.put($$3x, buildPath($$4x, $$1, $$3x, $$5)));
      $$4.trim();
      return $$5;
   }

   @Override
   public FileSystemProvider provider() {
      return this.provider;
   }

   @Override
   public void close() {
   }

   @Override
   public boolean isOpen() {
      return true;
   }

   @Override
   public boolean isReadOnly() {
      return true;
   }

   @Override
   public String getSeparator() {
      return "/";
   }

   @Override
   public Iterable<Path> getRootDirectories() {
      return List.of(this.root);
   }

   @Override
   public Iterable<FileStore> getFileStores() {
      return List.of(this.store);
   }

   @Override
   public Set<String> supportedFileAttributeViews() {
      return VIEWS;
   }

   @Override
   public Path getPath(String $$0, String... $$1) {
      Stream<String> $$2 = Stream.of($$0);
      if ($$1.length > 0) {
         $$2 = Stream.concat($$2, Stream.of($$1));
      }

      String $$3 = $$2.collect(Collectors.joining("/"));
      if ($$3.equals("/")) {
         return this.root;
      } else if ($$3.startsWith("/")) {
         LinkFSPath $$4 = this.root;

         for (String $$5 : PATH_SPLITTER.split($$3.substring(1))) {
            if ($$5.isEmpty()) {
               throw new IllegalArgumentException("Empty paths not allowed");
            }

            $$4 = $$4.resolveName($$5);
         }

         return $$4;
      } else {
         LinkFSPath $$6 = null;

         for (String $$7 : PATH_SPLITTER.split($$3)) {
            if ($$7.isEmpty()) {
               throw new IllegalArgumentException("Empty paths not allowed");
            }

            $$6 = new LinkFSPath(this, $$7, $$6, PathContents.RELATIVE);
         }

         if ($$6 == null) {
            throw new IllegalArgumentException("Empty paths not allowed");
         } else {
            return $$6;
         }
      }
   }

   @Override
   public PathMatcher getPathMatcher(String $$0) {
      throw new UnsupportedOperationException();
   }

   @Override
   public UserPrincipalLookupService getUserPrincipalLookupService() {
      throw new UnsupportedOperationException();
   }

   @Override
   public WatchService newWatchService() {
      throw new UnsupportedOperationException();
   }

   public FileStore store() {
      return this.store;
   }

   public LinkFSPath rootPath() {
      return this.root;
   }

   public static LinkFileSystem.Builder builder() {
      return new LinkFileSystem.Builder();
   }

   public static class Builder {
      private final LinkFileSystem.DirectoryEntry root = new LinkFileSystem.DirectoryEntry();

      public LinkFileSystem.Builder put(List<String> $$0, String $$1, Path $$2) {
         LinkFileSystem.DirectoryEntry $$3 = this.root;

         for (String $$4 : $$0) {
            $$3 = $$3.children.computeIfAbsent($$4, $$0x -> new LinkFileSystem.DirectoryEntry());
         }

         $$3.files.put($$1, $$2);
         return this;
      }

      public LinkFileSystem.Builder put(List<String> $$0, Path $$1) {
         if ($$0.isEmpty()) {
            throw new IllegalArgumentException("Path can't be empty");
         } else {
            int $$2 = $$0.size() - 1;
            return this.put($$0.subList(0, $$2), $$0.get($$2), $$1);
         }
      }

      public FileSystem build(String $$0) {
         return new LinkFileSystem($$0, this.root);
      }
   }

   record DirectoryEntry(Map<String, LinkFileSystem.DirectoryEntry> children, Map<String, Path> files) {

      public DirectoryEntry() {
         this(new HashMap<>(), new HashMap<>());
      }
   }
}
