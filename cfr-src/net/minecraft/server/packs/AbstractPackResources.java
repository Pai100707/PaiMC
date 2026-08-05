/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractPackResources
implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;

    protected AbstractPackResources(PackLocationInfo $$0) {
        this.location = $$0;
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> $$0) throws IOException {
        IoSupplier<InputStream> $$1 = this.getRootResource("pack.mcmeta");
        if ($$1 == null) {
            return null;
        }
        try (InputStream $$2 = $$1.get();){
            T t = AbstractPackResources.getMetadataFromStream($$0, $$2, this.location);
            return t;
        }
    }

    /*
     * WARNING - void declaration
     */
    public static <T> @Nullable T getMetadataFromStream(MetadataSectionType<T> $$0, InputStream $$1, PackLocationInfo $$22) {
        void $$7;
        try (BufferedReader $$3 = new BufferedReader(new InputStreamReader($$1, StandardCharsets.UTF_8));){
            JsonObject $$4 = GsonHelper.parse($$3);
        }
        catch (Exception $$6) {
            LOGGER.error("Couldn't load {} {} metadata: {}", new Object[]{$$22.id(), $$0.name(), $$6.getMessage()});
            return null;
        }
        if (!$$7.has($$0.name())) {
            return null;
        }
        return $$0.codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)$$7.get($$0.name())).ifError($$2 -> LOGGER.error("Couldn't load {} {} metadata: {}", new Object[]{$$22.id(), $$0.name(), $$2.message()})).result().orElse(null);
    }

    @Override
    public PackLocationInfo location() {
        return this.location;
    }
}

