/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.server.packs.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;

public interface ResourceMetadata {
    public static final ResourceMetadata EMPTY = new ResourceMetadata(){

        @Override
        public <T> Optional<T> getSection(MetadataSectionType<T> $$0) {
            return Optional.empty();
        }
    };
    public static final IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> EMPTY;

    public static ResourceMetadata fromJsonStream(InputStream $$0) throws IOException {
        try (BufferedReader $$1 = new BufferedReader(new InputStreamReader($$0, StandardCharsets.UTF_8));){
            final JsonObject $$2 = GsonHelper.parse($$1);
            ResourceMetadata resourceMetadata = new ResourceMetadata(){

                @Override
                public <T> Optional<T> getSection(MetadataSectionType<T> $$0) {
                    String $$1 = $$0.name();
                    if ($$2.has($$1)) {
                        Object $$22 = $$0.codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)$$2.get($$1)).getOrThrow(JsonParseException::new);
                        return Optional.of($$22);
                    }
                    return Optional.empty();
                }
            };
            return resourceMetadata;
        }
    }

    public <T> Optional<T> getSection(MetadataSectionType<T> var1);

    default public <T> Optional<MetadataSectionType.WithValue<T>> getTypedSection(MetadataSectionType<T> $$0) {
        return this.getSection($$0).map($$0::withValue);
    }

    default public List<MetadataSectionType.WithValue<?>> getTypedSections(Collection<MetadataSectionType<?>> $$0) {
        return $$0.stream().map(this::getTypedSection).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
    }
}

