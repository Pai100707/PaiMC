/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@FunctionalInterface
public interface IoSupplier<T> {
    public static IoSupplier<InputStream> create(Path $$0) {
        return () -> Files.newInputStream($$0, new OpenOption[0]);
    }

    public static IoSupplier<InputStream> create(ZipFile $$0, ZipEntry $$1) {
        return () -> $$0.getInputStream($$1);
    }

    public T get() throws IOException;
}

