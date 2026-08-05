/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackDetector;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.FileUtil;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FolderRepositorySource
implements RepositorySource {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final PackSelectionConfig DISCOVERED_PACK_SELECTION_CONFIG = new PackSelectionConfig(false, Pack.Position.TOP, false);
    private final Path folder;
    private final PackType packType;
    private final PackSource packSource;
    private final DirectoryValidator validator;

    public FolderRepositorySource(Path $$0, PackType $$1, PackSource $$2, DirectoryValidator $$3) {
        this.folder = $$0;
        this.packType = $$1;
        this.packSource = $$2;
        this.validator = $$3;
    }

    private static String nameFromPath(Path $$0) {
        return $$0.getFileName().toString();
    }

    @Override
    public void loadPacks(Consumer<Pack> $$0) {
        try {
            FileUtil.createDirectoriesSafe(this.folder);
            FolderRepositorySource.discoverPacks(this.folder, this.validator, ($$1, $$2) -> {
                PackLocationInfo $$3 = this.createDiscoveredFilePackInfo((Path)$$1);
                Pack $$4 = Pack.readMetaAndCreate($$3, $$2, this.packType, DISCOVERED_PACK_SELECTION_CONFIG);
                if ($$4 != null) {
                    $$0.accept($$4);
                }
            });
        }
        catch (IOException $$12) {
            LOGGER.warn("Failed to list packs in {}", (Object)this.folder, (Object)$$12);
        }
    }

    private PackLocationInfo createDiscoveredFilePackInfo(Path $$0) {
        String $$1 = FolderRepositorySource.nameFromPath($$0);
        return new PackLocationInfo("file/" + $$1, Component.literal($$1), this.packSource, Optional.empty());
    }

    public static void discoverPacks(Path $$0, DirectoryValidator $$1, BiConsumer<Path, Pack.ResourcesSupplier> $$2) throws IOException {
        FolderPackDetector $$3 = new FolderPackDetector($$1);
        try (DirectoryStream<Path> $$4 = Files.newDirectoryStream($$0);){
            for (Path $$5 : $$4) {
                try {
                    ArrayList<ForbiddenSymlinkInfo> $$6 = new ArrayList<ForbiddenSymlinkInfo>();
                    Pack.ResourcesSupplier $$7 = (Pack.ResourcesSupplier)$$3.detectPackResources($$5, $$6);
                    if (!$$6.isEmpty()) {
                        LOGGER.warn("Ignoring potential pack entry: {}", (Object)ContentValidationException.getMessage($$5, $$6));
                        continue;
                    }
                    if ($$7 != null) {
                        $$2.accept($$5, $$7);
                        continue;
                    }
                    LOGGER.info("Found non-pack entry '{}', ignoring", (Object)$$5);
                }
                catch (IOException $$8) {
                    LOGGER.warn("Failed to read properties of '{}', ignoring", (Object)$$5, (Object)$$8);
                }
            }
        }
    }

    static class FolderPackDetector
    extends PackDetector<Pack.ResourcesSupplier> {
        protected FolderPackDetector(DirectoryValidator $$0) {
            super($$0);
        }

        @Override
        protected @Nullable Pack.ResourcesSupplier createZipPack(Path $$0) {
            FileSystem $$1 = $$0.getFileSystem();
            if ($$1 == FileSystems.getDefault() || $$1 instanceof LinkFileSystem) {
                return new FilePackResources.FileResourcesSupplier($$0);
            }
            LOGGER.info("Can't open pack archive at {}", (Object)$$0);
            return null;
        }

        @Override
        protected Pack.ResourcesSupplier createDirectoryPack(Path $$0) {
            return new PathPackResources.PathResourcesSupplier($$0);
        }

        @Override
        protected /* synthetic */ Object createDirectoryPack(Path path) throws IOException {
            return this.createDirectoryPack(path);
        }

        @Override
        protected /* synthetic */ @Nullable Object createZipPack(Path path) throws IOException {
            return this.createZipPack(path);
        }
    }
}

