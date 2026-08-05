/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.FileUtil;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class VanillaPackResources
implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;
    private final BuiltInMetadata metadata;
    private final Set<String> namespaces;
    private final List<Path> rootPaths;
    private final Map<PackType, List<Path>> pathsForType;

    VanillaPackResources(PackLocationInfo $$0, BuiltInMetadata $$1, Set<String> $$2, List<Path> $$3, Map<PackType, List<Path>> $$4) {
        this.location = $$0;
        this.metadata = $$1;
        this.namespaces = $$2;
        this.rootPaths = $$3;
        this.pathsForType = $$4;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String ... $$0) {
        FileUtil.validatePath($$0);
        List<String> $$1 = List.of($$0);
        for (Path $$2 : this.rootPaths) {
            Path $$3 = FileUtil.resolvePath($$2, $$1);
            if (!Files.exists($$3, new LinkOption[0]) || !PathPackResources.validatePath($$3)) continue;
            return IoSupplier.create($$3);
        }
        return null;
    }

    public void listRawPaths(PackType $$0, Identifier $$12, Consumer<Path> $$2) {
        FileUtil.decomposePath($$12.getPath()).ifSuccess($$3 -> {
            String $$4 = $$12.getNamespace();
            for (Path $$5 : this.pathsForType.get((Object)$$0)) {
                Path $$6 = $$5.resolve($$4);
                $$2.accept(FileUtil.resolvePath($$6, $$3));
            }
        }).ifError($$1 -> LOGGER.error("Invalid path {}: {}", (Object)$$12, (Object)$$1.message()));
    }

    @Override
    public void listResources(PackType $$0, String $$12, String $$2, PackResources.ResourceOutput $$32) {
        FileUtil.decomposePath($$2).ifSuccess($$3 -> {
            List<Path> $$4 = this.pathsForType.get((Object)$$0);
            int $$5 = $$4.size();
            if ($$5 == 1) {
                VanillaPackResources.getResources($$32, $$12, $$4.get(0), $$3);
            } else if ($$5 > 1) {
                HashMap<Identifier, IoSupplier<InputStream>> $$6 = new HashMap<Identifier, IoSupplier<InputStream>>();
                for (int $$7 = 0; $$7 < $$5 - 1; ++$$7) {
                    VanillaPackResources.getResources($$6::putIfAbsent, $$12, $$4.get($$7), $$3);
                }
                Path $$8 = $$4.get($$5 - 1);
                if ($$6.isEmpty()) {
                    VanillaPackResources.getResources($$32, $$12, $$8, $$3);
                } else {
                    VanillaPackResources.getResources($$6::putIfAbsent, $$12, $$8, $$3);
                    $$6.forEach($$32);
                }
            }
        }).ifError($$1 -> LOGGER.error("Invalid path {}: {}", (Object)$$2, (Object)$$1.message()));
    }

    private static void getResources(PackResources.ResourceOutput $$0, String $$1, Path $$2, List<String> $$3) {
        Path $$4 = $$2.resolve($$1);
        PathPackResources.listPath($$1, $$4, $$3, $$0);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType $$0, Identifier $$12) {
        return (IoSupplier)FileUtil.decomposePath($$12.getPath()).mapOrElse($$2 -> {
            String $$3 = $$12.getNamespace();
            for (Path $$4 : this.pathsForType.get((Object)$$0)) {
                Path $$5 = FileUtil.resolvePath($$4.resolve($$3), $$2);
                if (!Files.exists($$5, new LinkOption[0]) || !PathPackResources.validatePath($$5)) continue;
                return IoSupplier.create($$5);
            }
            return null;
        }, $$1 -> {
            LOGGER.error("Invalid path {}: {}", (Object)$$12, (Object)$$1.message());
            return null;
        });
    }

    @Override
    public Set<String> getNamespaces(PackType $$0) {
        return this.namespaces;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> $$0) {
        IoSupplier<InputStream> $$1 = this.getRootResource("pack.mcmeta");
        if ($$1 == null) return this.metadata.get($$0);
        try (InputStream $$2 = $$1.get();){
            T $$3 = AbstractPackResources.getMetadataFromStream($$0, $$2, this.location);
            if ($$3 == null) return this.metadata.get($$0);
            T t = $$3;
            return t;
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return this.metadata.get($$0);
    }

    @Override
    public PackLocationInfo location() {
        return this.location;
    }

    @Override
    public void close() {
    }

    public ResourceProvider asProvider() {
        return $$02 -> Optional.ofNullable(this.getResource(PackType.CLIENT_RESOURCES, $$02)).map($$0 -> new Resource(this, (IoSupplier<InputStream>)$$0));
    }
}

