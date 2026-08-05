/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.DataVersion;
import org.slf4j.Logger;

public class DetectedVersion {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final WorldVersion BUILT_IN = DetectedVersion.createBuiltIn(UUID.randomUUID().toString().replaceAll("-", ""), "Development Version");

    public static WorldVersion createBuiltIn(String $$0, String $$1) {
        return DetectedVersion.createBuiltIn($$0, $$1, true);
    }

    public static WorldVersion createBuiltIn(String $$0, String $$1, boolean $$2) {
        return new WorldVersion.Simple($$0, $$1, new DataVersion(4671, "main"), SharedConstants.getProtocolVersion(), PackFormat.of(75, 0), PackFormat.of(94, 1), new Date(), $$2);
    }

    private static WorldVersion createFromJson(JsonObject $$0) {
        JsonObject $$1 = GsonHelper.getAsJsonObject($$0, "pack_version");
        return new WorldVersion.Simple(GsonHelper.getAsString($$0, "id"), GsonHelper.getAsString($$0, "name"), new DataVersion(GsonHelper.getAsInt($$0, "world_version"), GsonHelper.getAsString($$0, "series_id", "main")), GsonHelper.getAsInt($$0, "protocol_version"), PackFormat.of(GsonHelper.getAsInt($$1, "resource_major"), GsonHelper.getAsInt($$1, "resource_minor")), PackFormat.of(GsonHelper.getAsInt($$1, "data_major"), GsonHelper.getAsInt($$1, "data_minor")), Date.from(ZonedDateTime.parse(GsonHelper.getAsString($$0, "build_time")).toInstant()), GsonHelper.getAsBoolean($$0, "stable"));
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static WorldVersion tryDetectVersion() {
        try (InputStream $$0 = DetectedVersion.class.getResourceAsStream("/version.json");){
            WorldVersion worldVersion;
            if ($$0 == null) {
                LOGGER.warn("Missing version information!");
                WorldVersion worldVersion2 = BUILT_IN;
                return worldVersion2;
            }
            try (InputStreamReader $$1 = new InputStreamReader($$0, StandardCharsets.UTF_8);){
                worldVersion = DetectedVersion.createFromJson(GsonHelper.parse($$1));
            }
            return worldVersion;
        }
        catch (JsonParseException | IOException $$2) {
            throw new IllegalStateException("Game version information is corrupt", $$2);
        }
    }
}

