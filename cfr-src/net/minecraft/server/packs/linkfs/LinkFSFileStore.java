/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.packs.linkfs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import org.jspecify.annotations.Nullable;

class LinkFSFileStore
extends FileStore {
    private final String name;

    public LinkFSFileStore(String $$0) {
        this.name = $$0;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String type() {
        return "index";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public long getTotalSpace() {
        return 0L;
    }

    @Override
    public long getUsableSpace() {
        return 0L;
    }

    @Override
    public long getUnallocatedSpace() {
        return 0L;
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> $$0) {
        return $$0 == BasicFileAttributeView.class;
    }

    @Override
    public boolean supportsFileAttributeView(String $$0) {
        return "basic".equals($$0);
    }

    @Override
    public <V extends FileStoreAttributeView> @Nullable V getFileStoreAttributeView(Class<V> $$0) {
        return null;
    }

    @Override
    public Object getAttribute(String $$0) throws IOException {
        throw new UnsupportedOperationException();
    }
}

