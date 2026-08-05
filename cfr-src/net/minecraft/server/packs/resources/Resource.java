/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jspecify.annotations.Nullable;

public class Resource {
    private final PackResources source;
    private final IoSupplier<InputStream> streamSupplier;
    private final IoSupplier<ResourceMetadata> metadataSupplier;
    private @Nullable ResourceMetadata cachedMetadata;

    public Resource(PackResources $$0, IoSupplier<InputStream> $$1, IoSupplier<ResourceMetadata> $$2) {
        this.source = $$0;
        this.streamSupplier = $$1;
        this.metadataSupplier = $$2;
    }

    public Resource(PackResources $$0, IoSupplier<InputStream> $$1) {
        this.source = $$0;
        this.streamSupplier = $$1;
        this.metadataSupplier = ResourceMetadata.EMPTY_SUPPLIER;
        this.cachedMetadata = ResourceMetadata.EMPTY;
    }

    public PackResources source() {
        return this.source;
    }

    public String sourcePackId() {
        return this.source.packId();
    }

    public Optional<KnownPack> knownPackInfo() {
        return this.source.knownPackInfo();
    }

    public InputStream open() throws IOException {
        return this.streamSupplier.get();
    }

    public BufferedReader openAsReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.open(), StandardCharsets.UTF_8));
    }

    public ResourceMetadata metadata() throws IOException {
        if (this.cachedMetadata == null) {
            this.cachedMetadata = this.metadataSupplier.get();
        }
        return this.cachedMetadata;
    }
}

