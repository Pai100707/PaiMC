/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.locale.DeprecatedTranslationsInfo;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringDecomposer;
import org.slf4j.Logger;

public abstract class Language {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public static final String DEFAULT = "en_us";
    private static volatile Language instance = Language.loadDefault();

    private static Language loadDefault() {
        DeprecatedTranslationsInfo $$0 = DeprecatedTranslationsInfo.loadFromDefaultResource();
        HashMap<String, String> $$1 = new HashMap<String, String>();
        BiConsumer<String, String> $$2 = $$1::put;
        Language.parseTranslations($$2, "/assets/minecraft/lang/en_us.json");
        $$0.applyToMap($$1);
        final Map<String, String> $$3 = Map.copyOf($$1);
        return new Language(){

            @Override
            public String getOrDefault(String $$0, String $$1) {
                return $$3.getOrDefault($$0, $$1);
            }

            @Override
            public boolean has(String $$0) {
                return $$3.containsKey($$0);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }

            @Override
            public FormattedCharSequence getVisualOrder(FormattedText $$0) {
                return $$12 -> $$0.visit(($$1, $$2) -> StringDecomposer.iterateFormatted($$2, $$1, $$12) ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY).isPresent();
            }
        };
    }

    private static void parseTranslations(BiConsumer<String, String> $$0, String $$1) {
        try (InputStream $$2 = Language.class.getResourceAsStream($$1);){
            Language.loadFromJson($$2, $$0);
        }
        catch (JsonParseException | IOException $$3) {
            LOGGER.error("Couldn't read strings from {}", (Object)$$1, (Object)$$3);
        }
    }

    public static void loadFromJson(InputStream $$0, BiConsumer<String, String> $$1) {
        JsonObject $$2 = (JsonObject)GSON.fromJson((Reader)new InputStreamReader($$0, StandardCharsets.UTF_8), JsonObject.class);
        for (Map.Entry $$3 : $$2.entrySet()) {
            String $$4 = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString((JsonElement)$$3.getValue(), (String)$$3.getKey())).replaceAll("%$1s");
            $$1.accept((String)$$3.getKey(), $$4);
        }
    }

    public static Language getInstance() {
        return instance;
    }

    public static void inject(Language $$0) {
        instance = $$0;
    }

    public String getOrDefault(String $$0) {
        return this.getOrDefault($$0, $$0);
    }

    public abstract String getOrDefault(String var1, String var2);

    public abstract boolean has(String var1);

    public abstract boolean isDefaultRightToLeft();

    public abstract FormattedCharSequence getVisualOrder(FormattedText var1);

    public List<FormattedCharSequence> getVisualOrder(List<FormattedText> $$0) {
        return (List)$$0.stream().map(this::getVisualOrder).collect(ImmutableList.toImmutableList());
    }
}

