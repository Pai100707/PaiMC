/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

public class JsonRPCUtils {
    public static final String JSON_RPC_VERSION = "2.0";
    public static final String OPEN_RPC_VERSION = "1.3.2";

    public static JsonObject createSuccessResult(JsonElement $$0, JsonElement $$1) {
        JsonObject $$2 = new JsonObject();
        $$2.addProperty("jsonrpc", JSON_RPC_VERSION);
        $$2.add("id", $$0);
        $$2.add("result", $$1);
        return $$2;
    }

    public static JsonObject createRequest(@Nullable Integer $$0, Identifier $$1, List<JsonElement> $$2) {
        JsonObject $$3 = new JsonObject();
        $$3.addProperty("jsonrpc", JSON_RPC_VERSION);
        if ($$0 != null) {
            $$3.addProperty("id", (Number)$$0);
        }
        $$3.addProperty("method", $$1.toString());
        if (!$$2.isEmpty()) {
            JsonArray $$4 = new JsonArray($$2.size());
            for (JsonElement $$5 : $$2) {
                $$4.add($$5);
            }
            $$3.add("params", (JsonElement)$$4);
        }
        return $$3;
    }

    public static JsonObject createError(JsonElement $$0, String $$1, int $$2, @Nullable String $$3) {
        JsonObject $$4 = new JsonObject();
        $$4.addProperty("jsonrpc", JSON_RPC_VERSION);
        $$4.add("id", $$0);
        JsonObject $$5 = new JsonObject();
        $$5.addProperty("code", (Number)$$2);
        $$5.addProperty("message", $$1);
        if ($$3 != null && !$$3.isBlank()) {
            $$5.addProperty("data", $$3);
        }
        $$4.add("error", (JsonElement)$$5);
        return $$4;
    }

    public static @Nullable JsonElement getRequestId(JsonObject $$0) {
        return $$0.get("id");
    }

    public static @Nullable String getMethodName(JsonObject $$0) {
        return GsonHelper.getAsString($$0, "method", null);
    }

    public static @Nullable JsonElement getParams(JsonObject $$0) {
        return $$0.get("params");
    }

    public static @Nullable JsonElement getResult(JsonObject $$0) {
        return $$0.get("result");
    }

    public static @Nullable JsonObject getError(JsonObject $$0) {
        return GsonHelper.getAsJsonObject($$0, "error", null);
    }
}

