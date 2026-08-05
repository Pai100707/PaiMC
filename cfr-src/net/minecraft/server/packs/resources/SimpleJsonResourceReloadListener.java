/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener<T>
extends SimplePreparableReloadListener<Map<Identifier, T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DynamicOps<JsonElement> ops;
    private final Codec<T> codec;
    private final FileToIdConverter lister;

    protected SimpleJsonResourceReloadListener(HolderLookup.Provider $$0, Codec<T> $$1, ResourceKey<? extends Registry<T>> $$2) {
        this($$0.createSerializationContext(JsonOps.INSTANCE), $$1, FileToIdConverter.registry($$2));
    }

    protected SimpleJsonResourceReloadListener(Codec<T> $$0, FileToIdConverter $$1) {
        this((DynamicOps<JsonElement>)JsonOps.INSTANCE, $$0, $$1);
    }

    private SimpleJsonResourceReloadListener(DynamicOps<JsonElement> $$0, Codec<T> $$1, FileToIdConverter $$2) {
        this.ops = $$0;
        this.codec = $$1;
        this.lister = $$2;
    }

    @Override
    protected Map<Identifier, T> prepare(ResourceManager $$0, ProfilerFiller $$1) {
        HashMap $$2 = new HashMap();
        SimpleJsonResourceReloadListener.scanDirectory($$0, this.lister, this.ops, this.codec, $$2);
        return $$2;
    }

    public static <T> void scanDirectory(ResourceManager $$0, ResourceKey<? extends Registry<T>> $$1, DynamicOps<JsonElement> $$2, Codec<T> $$3, Map<Identifier, T> $$4) {
        SimpleJsonResourceReloadListener.scanDirectory($$0, FileToIdConverter.registry($$1), $$2, $$3, $$4);
    }

    public static <T> void scanDirectory(ResourceManager $$0, FileToIdConverter $$1, DynamicOps<JsonElement> $$22, Codec<T> $$3, Map<Identifier, T> $$4) {
        for (Map.Entry<Identifier, Resource> $$5 : $$1.listMatchingResources($$0).entrySet()) {
            Identifier $$6 = $$5.getKey();
            Identifier $$7 = $$1.fileToId($$6);
            try {
                BufferedReader $$8 = $$5.getValue().openAsReader();
                try {
                    $$3.parse($$22, (Object)StrictJsonParser.parse($$8)).ifSuccess($$2 -> {
                        if ($$4.putIfAbsent($$7, $$2) != null) {
                            throw new IllegalStateException("Duplicate data file ignored with ID " + String.valueOf($$7));
                        }
                    }).ifError($$2 -> LOGGER.error("Couldn't parse data file '{}' from '{}': {}", new Object[]{$$7, $$6, $$2}));
                }
                finally {
                    if ($$8 == null) continue;
                    ((Reader)$$8).close();
                }
            }
            catch (JsonParseException | IOException | IllegalArgumentException $$9) {
                LOGGER.error("Couldn't parse data file '{}' from '{}'", new Object[]{$$7, $$6, $$9});
            }
        }
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }
}

