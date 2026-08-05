/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.locale;

import com.google.gson.JsonElement;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.minecraft.locale.Language;
import net.minecraft.util.StrictJsonParser;
import org.slf4j.Logger;

public record DeprecatedTranslationsInfo(List<String> removed, Map<String, String> renamed) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeprecatedTranslationsInfo EMPTY = new DeprecatedTranslationsInfo(List.of(), Map.of());
    public static final Codec<DeprecatedTranslationsInfo> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.STRING.listOf().fieldOf("removed").forGetter(DeprecatedTranslationsInfo::removed), (App)Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.STRING).fieldOf("renamed").forGetter(DeprecatedTranslationsInfo::renamed)).apply((Applicative)$$0, DeprecatedTranslationsInfo::new));

    public static DeprecatedTranslationsInfo loadFromJson(InputStream $$02) {
        JsonElement $$1 = StrictJsonParser.parse(new InputStreamReader($$02, StandardCharsets.UTF_8));
        return (DeprecatedTranslationsInfo)CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)$$1).getOrThrow($$0 -> new IllegalStateException("Failed to parse deprecated language data: " + $$0));
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static DeprecatedTranslationsInfo loadFromResource(String $$0) {
        try (InputStream $$1 = Language.class.getResourceAsStream($$0);){
            if ($$1 == null) return EMPTY;
            DeprecatedTranslationsInfo deprecatedTranslationsInfo = DeprecatedTranslationsInfo.loadFromJson($$1);
            return deprecatedTranslationsInfo;
        }
        catch (Exception $$2) {
            LOGGER.error("Failed to read {}", (Object)$$0, (Object)$$2);
        }
        return EMPTY;
    }

    public static DeprecatedTranslationsInfo loadFromDefaultResource() {
        return DeprecatedTranslationsInfo.loadFromResource("/assets/minecraft/lang/deprecated.json");
    }

    public void applyToMap(Map<String, String> $$0) {
        for (String $$12 : this.removed) {
            $$0.remove($$12);
        }
        this.renamed.forEach(($$1, $$2) -> {
            String $$3 = (String)$$0.remove($$1);
            if ($$3 == null) {
                LOGGER.warn("Missing translation key for rename: {}", $$1);
                $$0.remove($$2);
            } else {
                $$0.put((String)$$2, $$3);
            }
        });
    }
}

