/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.StrictJsonParser;

public class LegacyComponentDataFixUtils {
    private static final String EMPTY_CONTENTS = LegacyComponentDataFixUtils.createTextComponentJson("");

    public static <T> Dynamic<T> createPlainTextComponent(DynamicOps<T> $$0, String $$1) {
        String $$2 = LegacyComponentDataFixUtils.createTextComponentJson($$1);
        return new Dynamic($$0, $$0.createString($$2));
    }

    public static <T> Dynamic<T> createEmptyComponent(DynamicOps<T> $$0) {
        return new Dynamic($$0, $$0.createString(EMPTY_CONTENTS));
    }

    public static String createTextComponentJson(String $$0) {
        JsonObject $$1 = new JsonObject();
        $$1.addProperty("text", $$0);
        return GsonHelper.toStableString((JsonElement)$$1);
    }

    public static String createTranslatableComponentJson(String $$0) {
        JsonObject $$1 = new JsonObject();
        $$1.addProperty("translate", $$0);
        return GsonHelper.toStableString((JsonElement)$$1);
    }

    public static <T> Dynamic<T> createTranslatableComponent(DynamicOps<T> $$0, String $$1) {
        String $$2 = LegacyComponentDataFixUtils.createTranslatableComponentJson($$1);
        return new Dynamic($$0, $$0.createString($$2));
    }

    public static String rewriteFromLenient(String $$0) {
        if ($$0.isEmpty() || $$0.equals("null")) {
            return EMPTY_CONTENTS;
        }
        char $$1 = $$0.charAt(0);
        char $$2 = $$0.charAt($$0.length() - 1);
        if ($$1 == '\"' && $$2 == '\"' || $$1 == '{' && $$2 == '}' || $$1 == '[' && $$2 == ']') {
            try {
                JsonElement $$3 = LenientJsonParser.parse($$0);
                if ($$3.isJsonPrimitive()) {
                    return LegacyComponentDataFixUtils.createTextComponentJson($$3.getAsString());
                }
                return GsonHelper.toStableString($$3);
            }
            catch (JsonParseException jsonParseException) {
                // empty catch block
            }
        }
        return LegacyComponentDataFixUtils.createTextComponentJson($$0);
    }

    public static boolean isStrictlyValidJson(Dynamic<?> $$02) {
        return $$02.asString().result().filter($$0 -> {
            try {
                StrictJsonParser.parse($$0);
                return true;
            }
            catch (JsonParseException $$1) {
                return false;
            }
        }).isPresent();
    }

    public static Optional<String> extractTranslationString(String $$0) {
        try {
            JsonObject $$2;
            JsonElement $$3;
            JsonElement $$1 = LenientJsonParser.parse($$0);
            if ($$1.isJsonObject() && ($$3 = ($$2 = $$1.getAsJsonObject()).get("translate")) != null && $$3.isJsonPrimitive()) {
                return Optional.of($$3.getAsString());
            }
        }
        catch (JsonParseException jsonParseException) {
            // empty catch block
        }
        return Optional.empty();
    }
}

