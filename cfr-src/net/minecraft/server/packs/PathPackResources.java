/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.FileUtil;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PathPackResources
extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Joiner PATH_JOINER = Joiner.on((String)"/");
    private final Path root;

    public PathPackResources(PackLocationInfo $$0, Path $$1) {
        super($$0);
        this.root = $$1;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String ... $$0) {
        FileUtil.validatePath($$0);
        Path $$1 = FileUtil.resolvePath(this.root, List.of($$0));
        if (Files.exists($$1, new LinkOption[0])) {
            return IoSupplier.create($$1);
        }
        return null;
    }

    public static boolean validatePath(Path $$0) {
        if (!SharedConstants.DEBUG_VALIDATE_RESOURCE_PATH_CASE) {
            return true;
        }
        if ($$0.getFileSystem() != FileSystems.getDefault()) {
            return true;
        }
        try {
            return $$0.toRealPath(new LinkOption[0]).endsWith($$0);
        }
        catch (IOException $$1) {
            LOGGER.warn("Failed to resolve real path for {}", (Object)$$0, (Object)$$1);
            return false;
        }
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType $$0, Identifier $$1) {
        Path $$2 = this.root.resolve($$0.getDirectory()).resolve($$1.getNamespace());
        return PathPackResources.getResource($$1, $$2);
    }

    public static @Nullable IoSupplier<InputStream> getResource(Identifier $$0, Path $$12) {
        return (IoSupplier)FileUtil.decomposePath($$0.getPath()).mapOrElse($$1 -> {
            Path $$2 = FileUtil.resolvePath($$12, $$1);
            return PathPackResources.returnFileIfExists($$2);
        }, $$1 -> {
            LOGGER.error("Invalid path {}: {}", (Object)$$0, (Object)$$1.message());
            return null;
        });
    }

    private static @Nullable IoSupplier<InputStream> returnFileIfExists(Path $$0) {
        if (Files.exists($$0, new LinkOption[0]) && PathPackResources.validatePath($$0)) {
            return IoSupplier.create($$0);
        }
        return null;
    }

    @Override
    public void listResources(PackType $$0, String $$12, String $$2, PackResources.ResourceOutput $$32) {
        FileUtil.decomposePath($$2).ifSuccess($$3 -> {
            Path $$4 = this.root.resolve($$0.getDirectory()).resolve($$12);
            PathPackResources.listPath($$12, $$4, $$3, $$32);
        }).ifError($$1 -> LOGGER.error("Invalid path {}: {}", (Object)$$2, (Object)$$1.message()));
    }

    public static void listPath(String $$0, Path $$1, List<String> $$2, PackResources.ResourceOutput $$32) {
        Path $$4 = FileUtil.resolvePath($$1, $$2);
        try (Stream<Path> $$52 = Files.find($$4, Integer.MAX_VALUE, PathPackResources::isRegularFile, new FileVisitOption[0]);){
            $$52.forEach($$3 -> {
                String $$4 = PATH_JOINER.join((Iterable)$$1.relativize((Path)$$3));
                Identifier $$5 = Identifier.tryBuild($$0, $$4);
                if ($$5 == null) {
                    Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", $$0, $$4));
                } else {
                    $$32.accept($$5, IoSupplier.create($$3));
                }
            });
        }
        catch (NoSuchFileException | NotDirectoryException $$52) {
        }
        catch (IOException $$6) {
            LOGGER.error("Failed to list path {}", (Object)$$4, (Object)$$6);
        }
    }

    private static boolean isRegularFile(Path $$0, BasicFileAttributes $$1) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return $$1.isRegularFile() && !StringUtils.equalsIgnoreCase((CharSequence)$$0.getFileName().toString(), (CharSequence)".ds_store");
        }
        return $$1.isRegularFile();
    }

    @Override
    public Set<String> getNamespaces(PackType $$0) {
        HashSet $$1 = Sets.newHashSet();
        Path $$2 = this.root.resolve($$0.getDirectory());
        try (DirectoryStream<Path> $$32 = Files.newDirectoryStream($$2);){
            for (Path $$4 : $$32) {
                String $$5 = $$4.getFileName().toString();
                if (Identifier.isValidNamespace($$5)) {
                    $$1.add($$5);
                    continue;
                }
                LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", (Object)$$5, (Object)this.root);
            }
        }
        catch (NoSuchFileException | NotDirectoryException $$32) {
        }
        catch (IOException $$6) {
            LOGGER.error("Failed to list path {}", (Object)$$2, (Object)$$6);
        }
        return $$1;
    }

    @Override
    public void close() {
    }

    public static class PathResourcesSupplier
    implements Pack.ResourcesSupplier {
        private final Path content;

        public PathResourcesSupplier(Path $$0) {
            this.content = $$0;
        }

        @Override
        public PackResources openPrimary(PackLocationInfo $$0) {
            return new PathPackResources($$0, this.content);
        }

        @Override
        public PackResources openFull(PackLocationInfo $$0, Pack.Metadata $$1) {
            PackResources $$2 = this.openPrimary($$0);
            List<String> $$3 = $$1.overlays();
            if ($$3.isEmpty()) {
                return $$2;
            }
            ArrayList<PackResources> $$4 = new ArrayList<PackResources>($$3.size());
            for (String $$5 : $$3) {
                Path $$6 = this.content.resolve($$5);
                $$4.add(new PathPackResources($$0, $$6));
            }
            return new CompositePackResources($$2, $$4);
        }
    }
}

