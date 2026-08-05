/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.packs.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.jspecify.annotations.Nullable;

public abstract class PackDetector<T> {
    private final DirectoryValidator validator;

    protected PackDetector(DirectoryValidator $$0) {
        this.validator = $$0;
    }

    public @Nullable T detectPackResources(Path $$0, List<ForbiddenSymlinkInfo> $$1) throws IOException {
        BasicFileAttributes $$5;
        Path $$2 = $$0;
        try {
            BasicFileAttributes $$3 = Files.readAttributes($$0, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }
        catch (NoSuchFileException $$4) {
            return null;
        }
        if ($$5.isSymbolicLink()) {
            this.validator.validateSymlink($$0, $$1);
            if (!$$1.isEmpty()) {
                return null;
            }
            $$2 = Files.readSymbolicLink($$0);
            $$5 = Files.readAttributes($$2, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }
        if ($$5.isDirectory()) {
            this.validator.validateKnownDirectory($$2, $$1);
            if (!$$1.isEmpty()) {
                return null;
            }
            if (!Files.isRegularFile($$2.resolve("pack.mcmeta"), new LinkOption[0])) {
                return null;
            }
            return this.createDirectoryPack($$2);
        }
        if ($$5.isRegularFile() && $$2.getFileName().toString().endsWith(".zip")) {
            return this.createZipPack($$2);
        }
        return null;
    }

    protected abstract @Nullable T createZipPack(Path var1) throws IOException;

    protected abstract @Nullable T createDirectoryPack(Path var1) throws IOException;
}

