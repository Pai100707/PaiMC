/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.mojang.authlib.GameProfile
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerTextFilter;
import net.minecraft.server.network.TextFilter;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

public class LegacyTextFilter
extends ServerTextFilter {
    private static final String ENDPOINT = "v1/chat";
    final URL joinEndpoint;
    final JoinOrLeaveEncoder joinEncoder;
    final URL leaveEndpoint;
    final JoinOrLeaveEncoder leaveEncoder;
    private final String authKey;

    private LegacyTextFilter(URL $$0, ServerTextFilter.MessageEncoder $$1, URL $$2, JoinOrLeaveEncoder $$3, URL $$4, JoinOrLeaveEncoder $$5, String $$6, ServerTextFilter.IgnoreStrategy $$7, ExecutorService $$8) {
        super($$0, $$1, $$7, $$8);
        this.joinEndpoint = $$2;
        this.joinEncoder = $$3;
        this.leaveEndpoint = $$4;
        this.leaveEncoder = $$5;
        this.authKey = $$6;
    }

    public static @Nullable ServerTextFilter createTextFilterFromConfig(String $$0) {
        try {
            ServerTextFilter.MessageEncoder $$18;
            JsonObject $$1 = GsonHelper.parse($$0);
            URI $$22 = new URI(GsonHelper.getAsString($$1, "apiServer"));
            String $$32 = GsonHelper.getAsString($$1, "apiKey");
            if ($$32.isEmpty()) {
                throw new IllegalArgumentException("Missing API key");
            }
            int $$42 = GsonHelper.getAsInt($$1, "ruleId", 1);
            String $$5 = GsonHelper.getAsString($$1, "serverId", "");
            String $$6 = GsonHelper.getAsString($$1, "roomId", "Java:Chat");
            int $$7 = GsonHelper.getAsInt($$1, "hashesToDrop", -1);
            int $$8 = GsonHelper.getAsInt($$1, "maxConcurrentRequests", 7);
            JsonObject $$9 = GsonHelper.getAsJsonObject($$1, "endpoints", null);
            String $$10 = LegacyTextFilter.getEndpointFromConfig($$9, "chat", ENDPOINT);
            boolean $$11 = $$10.equals(ENDPOINT);
            URL $$12 = $$22.resolve("/" + $$10).toURL();
            URL $$13 = LegacyTextFilter.getEndpoint($$22, $$9, "join", "v1/join");
            URL $$14 = LegacyTextFilter.getEndpoint($$22, $$9, "leave", "v1/leave");
            JoinOrLeaveEncoder $$15 = $$2 -> {
                JsonObject $$3 = new JsonObject();
                $$3.addProperty("server", $$5);
                $$3.addProperty("room", $$6);
                $$3.addProperty("user_id", $$2.id().toString());
                $$3.addProperty("user_display_name", $$2.name());
                return $$3;
            };
            if ($$11) {
                ServerTextFilter.MessageEncoder $$16 = ($$3, $$4) -> {
                    JsonObject $$5 = new JsonObject();
                    $$5.addProperty("rule", (Number)$$42);
                    $$5.addProperty("server", $$5);
                    $$5.addProperty("room", $$6);
                    $$5.addProperty("player", $$3.id().toString());
                    $$5.addProperty("player_display_name", $$3.name());
                    $$5.addProperty("text", $$4);
                    $$5.addProperty("language", "*");
                    return $$5;
                };
            } else {
                String $$17 = String.valueOf($$42);
                $$18 = ($$3, $$4) -> {
                    JsonObject $$5 = new JsonObject();
                    $$5.addProperty("rule_id", $$17);
                    $$5.addProperty("category", $$5);
                    $$5.addProperty("subcategory", $$6);
                    $$5.addProperty("user_id", $$3.id().toString());
                    $$5.addProperty("user_display_name", $$3.name());
                    $$5.addProperty("text", $$4);
                    $$5.addProperty("language", "*");
                    return $$5;
                };
            }
            ServerTextFilter.IgnoreStrategy $$19 = ServerTextFilter.IgnoreStrategy.select($$7);
            ExecutorService $$20 = LegacyTextFilter.createWorkerPool($$8);
            String $$21 = Base64.getEncoder().encodeToString($$32.getBytes(StandardCharsets.US_ASCII));
            return new LegacyTextFilter($$12, $$18, $$13, $$15, $$14, $$15, $$21, $$19, $$20);
        }
        catch (Exception $$22) {
            LOGGER.warn("Failed to parse chat filter config {}", (Object)$$0, (Object)$$22);
            return null;
        }
    }

    @Override
    public TextFilter createContext(GameProfile $$0) {
        return new ServerTextFilter.PlayerContext($$0){

            @Override
            public void join() {
                LegacyTextFilter.this.processJoinOrLeave(this.profile, LegacyTextFilter.this.joinEndpoint, LegacyTextFilter.this.joinEncoder, this.streamExecutor);
            }

            @Override
            public void leave() {
                LegacyTextFilter.this.processJoinOrLeave(this.profile, LegacyTextFilter.this.leaveEndpoint, LegacyTextFilter.this.leaveEncoder, this.streamExecutor);
            }
        };
    }

    void processJoinOrLeave(GameProfile $$0, URL $$1, JoinOrLeaveEncoder $$2, Executor $$3) {
        $$3.execute(() -> {
            JsonObject $$3 = $$2.encode($$0);
            try {
                this.processRequest($$3, $$1);
            }
            catch (Exception $$4) {
                LOGGER.warn("Failed to send join/leave packet to {} for player {}", new Object[]{$$1, $$0, $$4});
            }
        });
    }

    private void processRequest(JsonObject $$0, URL $$1) throws IOException {
        HttpURLConnection $$2 = this.makeRequest($$0, $$1);
        try (InputStream $$3 = $$2.getInputStream();){
            this.drainStream($$3);
        }
    }

    @Override
    protected void setAuthorizationProperty(HttpURLConnection $$0) {
        $$0.setRequestProperty("Authorization", "Basic " + this.authKey);
    }

    @Override
    protected FilteredText filterText(String $$0, ServerTextFilter.IgnoreStrategy $$1, JsonObject $$2) {
        boolean $$3 = GsonHelper.getAsBoolean($$2, "response", false);
        if ($$3) {
            return FilteredText.passThrough($$0);
        }
        String $$4 = GsonHelper.getAsString($$2, "hashed", null);
        if ($$4 == null) {
            return FilteredText.fullyFiltered($$0);
        }
        JsonArray $$5 = GsonHelper.getAsJsonArray($$2, "hashes");
        FilterMask $$6 = this.parseMask($$0, $$5, $$1);
        return new FilteredText($$0, $$6);
    }

    @FunctionalInterface
    static interface JoinOrLeaveEncoder {
        public JsonObject encode(GameProfile var1);
    }
}

