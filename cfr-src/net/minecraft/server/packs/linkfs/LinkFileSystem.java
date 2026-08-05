/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.packs.linkfs;

import com.google.common.base.Splitter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
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
import net.minecraft.server.packs.linkfs.LinkFSFileStore;
import net.minecraft.server.packs.linkfs.LinkFSPath;
import net.minecraft.server.packs.linkfs.LinkFSProvider;
import net.minecraft.server.packs.linkfs.PathContents;
import org.jspecify.annotations.Nullable;

public class LinkFileSystem
extends FileSystem {
    private static final Set<String> VIEWS = Set.of("basic");
    public static final String PATH_SEPARATOR = "/";
    private static final Splitter PATH_SPLITTER = Splitter.on((char)'/');
    private final FileStore store;
    private final FileSystemProvider provider = new LinkFSProvider();
    private final LinkFSPath root;

    LinkFileSystem(String $$0, DirectoryEntry $$1) {
        this.store = new LinkFSFileStore($$0);
        this.root = LinkFileSystem.buildPath($$1, this, "", null);
    }

    private static LinkFSPath buildPath(DirectoryEntry $$0, LinkFileSystem $$1, String $$2, @Nullable LinkFSPath $$32) {
        Object2ObjectOpenHashMap $$42 = new Object2ObjectOpenHashMap();
        LinkFSPath $$5 = new LinkFSPath($$1, $$2, $$32, new PathContents.DirectoryContents((Map<String, LinkFSPath>)$$42));
        $$0.files.forEach(($$3, $$4) -> $$42.put($$3, (Object)new LinkFSPath($$1, (String)$$3, $$5, new PathContents.FileContents((Path)$$4))));
        $$0.children.forEach(($$3, $$4) -> $$42.put($$3, (Object)LinkFileSystem.buildPath($$4, $$1, $$3, $$5)));
        $$42.trim();
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
        return PATH_SEPARATOR;
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
    public Path getPath(String $$0, String ... $$1) {
        String $$3;
        Stream<String> $$2 = Stream.of($$0);
        if ($$1.length > 0) {
            $$2 = Stream.concat($$2, Stream.of($$1));
        }
        if (($$3 = $$2.collect(Collectors.joining(PATH_SEPARATOR))).equals(PATH_SEPARATOR)) {
            return this.root;
        }
        if ($$3.startsWith(PATH_SEPARATOR)) {
            LinkFSPath $$4 = this.root;
            for (String $$5 : PATH_SPLITTER.split((CharSequence)$$3.substring(1))) {
                if ($$5.isEmpty()) {
                    throw new IllegalArgumentException("Empty paths not allowed");
                }
                $$4 = $$4.resolveName($$5);
            }
            return $$4;
        }
        LinkFSPath $$6 = null;
        for (String $$7 : PATH_SPLITTER.split((CharSequence)$$3)) {
            if ($$7.isEmpty()) {
                throw new IllegalArgumentException("Empty paths not allowed");
            }
            $$6 = new LinkFSPath(this, $$7, $$6, PathContents.RELATIVE);
        }
        if ($$6 == null) {
            throw new IllegalArgumentException("Empty paths not allowed");
        }
        return $$6;
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

    public static Builder builder() {
        return new Builder();
    }

    static final class DirectoryEntry
    extends Record {
        final Map<String, DirectoryEntry> children;
        final Map<String, Path> files;

        public DirectoryEntry() {
            this(new HashMap<String, DirectoryEntry>(), new HashMap<String, Path>());
        }

        private DirectoryEntry(Map<String, DirectoryEntry> $$0, Map<String, Path> $$1) {
            this.children = $$0;
            this.files = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DirectoryEntry.class, "children;files", "children", "files"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DirectoryEntry.class, "children;files", "children", "files"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DirectoryEntry.class, "children;files", "children", "files"}, this, $$0);
        }

        public Map<String, DirectoryEntry> children() {
            return this.children;
        }

        public Map<String, Path> files() {
            return this.files;
        }
    }

    public static class Builder {
        private final DirectoryEntry root = new DirectoryEntry();

        public Builder put(List<String> $$02, String $$1, Path $$2) {
            DirectoryEntry $$3 = this.root;
            for (String $$4 : $$02) {
                $$3 = $$3.children.computeIfAbsent($$4, $$0 -> new DirectoryEntry());
            }
            $$3.files.put($$1, $$2);
            return this;
        }

        public Builder put(List<String> $$0, Path $$1) {
            if ($$0.isEmpty()) {
                throw new IllegalArgumentException("Path can't be empty");
            }
            int $$2 = $$0.size() - 1;
            return this.put($$0.subList(0, $$2), $$0.get($$2), $$1);
        }

        public FileSystem build(String $$0) {
            return new LinkFileSystem($$0, this.root);
        }
    }
}

