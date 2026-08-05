/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.io.IOUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FilePackResources
extends AbstractPackResources {
    static final Logger LOGGER = LogUtils.getLogger();
    private final SharedZipFileAccess zipFileAccess;
    private final String prefix;

    FilePackResources(PackLocationInfo $$0, SharedZipFileAccess $$1, String $$2) {
        super($$0);
        this.zipFileAccess = $$1;
        this.prefix = $$2;
    }

    private static String getPathFromLocation(PackType $$0, Identifier $$1) {
        return String.format(Locale.ROOT, "%s/%s/%s", $$0.getDirectory(), $$1.getNamespace(), $$1.getPath());
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String ... $$0) {
        return this.getResource(String.join((CharSequence)"/", $$0));
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType $$0, Identifier $$1) {
        return this.getResource(FilePackResources.getPathFromLocation($$0, $$1));
    }

    private String addPrefix(String $$0) {
        if (this.prefix.isEmpty()) {
            return $$0;
        }
        return this.prefix + "/" + $$0;
    }

    private @Nullable IoSupplier<InputStream> getResource(String $$0) {
        ZipFile $$1 = this.zipFileAccess.getOrCreateZipFile();
        if ($$1 == null) {
            return null;
        }
        ZipEntry $$2 = $$1.getEntry(this.addPrefix($$0));
        if ($$2 == null) {
            return null;
        }
        return IoSupplier.create($$1, $$2);
    }

    @Override
    public Set<String> getNamespaces(PackType $$0) {
        ZipFile $$1 = this.zipFileAccess.getOrCreateZipFile();
        if ($$1 == null) {
            return Set.of();
        }
        Enumeration<? extends ZipEntry> $$2 = $$1.entries();
        HashSet $$3 = Sets.newHashSet();
        String $$4 = this.addPrefix($$0.getDirectory() + "/");
        while ($$2.hasMoreElements()) {
            ZipEntry $$5 = $$2.nextElement();
            String $$6 = $$5.getName();
            String $$7 = FilePackResources.extractNamespace($$4, $$6);
            if ($$7.isEmpty()) continue;
            if (Identifier.isValidNamespace($$7)) {
                $$3.add($$7);
                continue;
            }
            LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", (Object)$$7, (Object)this.zipFileAccess.file);
        }
        return $$3;
    }

    @VisibleForTesting
    public static String extractNamespace(String $$0, String $$1) {
        if (!$$1.startsWith($$0)) {
            return "";
        }
        int $$2 = $$0.length();
        int $$3 = $$1.indexOf(47, $$2);
        if ($$3 == -1) {
            return $$1.substring($$2);
        }
        return $$1.substring($$2, $$3);
    }

    @Override
    public void close() {
        this.zipFileAccess.close();
    }

    @Override
    public void listResources(PackType $$0, String $$1, String $$2, PackResources.ResourceOutput $$3) {
        ZipFile $$4 = this.zipFileAccess.getOrCreateZipFile();
        if ($$4 == null) {
            return;
        }
        Enumeration<? extends ZipEntry> $$5 = $$4.entries();
        String $$6 = this.addPrefix($$0.getDirectory() + "/" + $$1 + "/");
        String $$7 = $$6 + $$2 + "/";
        while ($$5.hasMoreElements()) {
            String $$9;
            ZipEntry $$8 = $$5.nextElement();
            if ($$8.isDirectory() || !($$9 = $$8.getName()).startsWith($$7)) continue;
            String $$10 = $$9.substring($$6.length());
            Identifier $$11 = Identifier.tryBuild($$1, $$10);
            if ($$11 != null) {
                $$3.accept($$11, IoSupplier.create($$4, $$8));
                continue;
            }
            LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", (Object)$$1, (Object)$$10);
        }
    }

    static class SharedZipFileAccess
    implements AutoCloseable {
        final File file;
        private @Nullable ZipFile zipFile;
        private boolean failedToLoad;

        SharedZipFileAccess(File $$0) {
            this.file = $$0;
        }

        @Nullable ZipFile getOrCreateZipFile() {
            if (this.failedToLoad) {
                return null;
            }
            if (this.zipFile == null) {
                try {
                    this.zipFile = new ZipFile(this.file);
                }
                catch (IOException $$0) {
                    LOGGER.error("Failed to open pack {}", (Object)this.file, (Object)$$0);
                    this.failedToLoad = true;
                    return null;
                }
            }
            return this.zipFile;
        }

        @Override
        public void close() {
            if (this.zipFile != null) {
                IOUtils.closeQuietly((Closeable)this.zipFile);
                this.zipFile = null;
            }
        }

        protected void finalize() throws Throwable {
            this.close();
            super.finalize();
        }
    }

    public static class FileResourcesSupplier
    implements Pack.ResourcesSupplier {
        private final File content;

        public FileResourcesSupplier(Path $$0) {
            this($$0.toFile());
        }

        public FileResourcesSupplier(File $$0) {
            this.content = $$0;
        }

        @Override
        public PackResources openPrimary(PackLocationInfo $$0) {
            SharedZipFileAccess $$1 = new SharedZipFileAccess(this.content);
            return new FilePackResources($$0, $$1, "");
        }

        @Override
        public PackResources openFull(PackLocationInfo $$0, Pack.Metadata $$1) {
            SharedZipFileAccess $$2 = new SharedZipFileAccess(this.content);
            FilePackResources $$3 = new FilePackResources($$0, $$2, "");
            List<String> $$4 = $$1.overlays();
            if ($$4.isEmpty()) {
                return $$3;
            }
            ArrayList<PackResources> $$5 = new ArrayList<PackResources>($$4.size());
            for (String $$6 : $$4) {
                $$5.add(new FilePackResources($$0, $$2, $$6));
            }
            return new CompositePackResources($$3, $$5);
        }
    }
}

