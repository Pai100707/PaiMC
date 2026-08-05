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
import net.minecraft.util.GsonHelper;

public class LegacyTextFilter extends ServerTextFilter {
   private static final String ENDPOINT = "v1/chat";
   final URL joinEndpoint;
   final LegacyTextFilter.JoinOrLeaveEncoder joinEncoder;
   final URL leaveEndpoint;
   final LegacyTextFilter.JoinOrLeaveEncoder leaveEncoder;
   private final String authKey;

   private LegacyTextFilter(
      URL $$0,
      ServerTextFilter.MessageEncoder $$1,
      URL $$2,
      LegacyTextFilter.JoinOrLeaveEncoder $$3,
      URL $$4,
      LegacyTextFilter.JoinOrLeaveEncoder $$5,
      String $$6,
      ServerTextFilter.IgnoreStrategy $$7,
      ExecutorService $$8
   ) {
      super($$0, $$1, $$7, $$8);
      this.joinEndpoint = $$2;
      this.joinEncoder = $$3;
      this.leaveEndpoint = $$4;
      this.leaveEncoder = $$5;
      this.authKey = $$6;
   }

   
   public static ServerTextFilter createTextFilterFromConfig(String $$0) {
      try {
         JsonObject $$1 = GsonHelper.parse($$0);
         URI $$2 = new URI(GsonHelper.getAsString($$1, "apiServer"));
         String $$3 = GsonHelper.getAsString($$1, "apiKey");
         if ($$3.isEmpty()) {
            throw new IllegalArgumentException("Missing API key");
         } else {
            int $$4 = GsonHelper.getAsInt($$1, "ruleId", 1);
            String $$5 = GsonHelper.getAsString($$1, "serverId", "");
            String $$6 = GsonHelper.getAsString($$1, "roomId", "Java:Chat");
            int $$7 = GsonHelper.getAsInt($$1, "hashesToDrop", -1);
            int $$8 = GsonHelper.getAsInt($$1, "maxConcurrentRequests", 7);
            JsonObject $$9 = GsonHelper.getAsJsonObject($$1, "endpoints", null);
            String $$10 = getEndpointFromConfig($$9, "chat", "v1/chat");
            boolean $$11 = $$10.equals("v1/chat");
            URL $$12 = $$2.resolve("/" + $$10).toURL();
            URL $$13 = getEndpoint($$2, $$9, "join", "v1/join");
            URL $$14 = getEndpoint($$2, $$9, "leave", "v1/leave");
            LegacyTextFilter.JoinOrLeaveEncoder $$15 = $$2x -> {
               JsonObject $$3x = new JsonObject();
               $$3x.addProperty("server", $$5);
               $$3x.addProperty("room", $$6);
               $$3x.addProperty("user_id", $$2x.id().toString());
               $$3x.addProperty("user_display_name", $$2x.name());
               return $$3x;
            };
            ServerTextFilter.MessageEncoder $$16;
            if ($$11) {
               $$16 = ($$3x, $$4x) -> {
                  JsonObject $$5x = new JsonObject();
                  $$5x.addProperty("rule", $$4);
                  $$5x.addProperty("server", $$5);
                  $$5x.addProperty("room", $$6);
                  $$5x.addProperty("player", $$3x.id().toString());
                  $$5x.addProperty("player_display_name", $$3x.name());
                  $$5x.addProperty("text", $$4x);
                  $$5x.addProperty("language", "*");
                  return $$5x;
               };
            } else {
               String $$17 = String.valueOf($$4);
               $$16 = ($$3x, $$4x) -> {
                  JsonObject $$5x = new JsonObject();
                  $$5x.addProperty("rule_id", $$17);
                  $$5x.addProperty("category", $$5);
                  $$5x.addProperty("subcategory", $$6);
                  $$5x.addProperty("user_id", $$3x.id().toString());
                  $$5x.addProperty("user_display_name", $$3x.name());
                  $$5x.addProperty("text", $$4x);
                  $$5x.addProperty("language", "*");
                  return $$5x;
               };
            }

            ServerTextFilter.IgnoreStrategy $$19 = ServerTextFilter.IgnoreStrategy.select($$7);
            ExecutorService $$20 = createWorkerPool($$8);
            String $$21 = Base64.getEncoder().encodeToString($$3.getBytes(StandardCharsets.US_ASCII));
            return new LegacyTextFilter($$12, $$16, $$13, $$15, $$14, $$15, $$21, $$19, $$20);
         }
      } catch (Exception var20) {
         LOGGER.warn("Failed to parse chat filter config {}", $$0, var20);
         return null;
      }
   }

   @Override
   public TextFilter createContext(GameProfile $$0) {
      return new ServerTextFilter.PlayerContext($$0) {
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

   void processJoinOrLeave(GameProfile $$0, URL $$1, LegacyTextFilter.JoinOrLeaveEncoder $$2, Executor $$3) {
      $$3.execute(() -> {
         JsonObject $$3x = $$2.encode($$0);

         try {
            this.processRequest($$3x, $$1);
         } catch (Exception var6) {
            LOGGER.warn("Failed to send join/leave packet to {} for player {}", new Object[]{$$1, $$0, var6});
         }
      });
   }

   private void processRequest(JsonObject $$0, URL $$1) throws IOException {
      HttpURLConnection $$2 = this.makeRequest($$0, $$1);

      try (InputStream $$3 = $$2.getInputStream()) {
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
      } else {
         String $$4 = GsonHelper.getAsString($$2, "hashed", null);
         if ($$4 == null) {
            return FilteredText.fullyFiltered($$0);
         } else {
            JsonArray $$5 = GsonHelper.getAsJsonArray($$2, "hashes");
            FilterMask $$6 = this.parseMask($$0, $$5, $$1);
            return new FilteredText($$0, $$6);
         }
      }
   }

   @FunctionalInterface
   interface JoinOrLeaveEncoder {
      JsonObject encode(GameProfile var1);
   }
}
