package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public record NameAndId(UUID id, String name) {
   public static final Codec<NameAndId> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(NameAndId::id), Codec.STRING.fieldOf("name").forGetter(NameAndId::name))
         .apply($$0, NameAndId::new)
   );

   public NameAndId(GameProfile $$0) {
      this($$0.id(), $$0.name());
   }

   public NameAndId(com.mojang.authlib.yggdrasil.response.NameAndId $$0) {
      this($$0.id(), $$0.name());
   }

   
   public static NameAndId fromJson(JsonObject $$0) {
      if ($$0.has("uuid") && $$0.has("name")) {
         String $$1 = $$0.get("uuid").getAsString();

         UUID $$2;
         try {
            $$2 = UUID.fromString($$1);
         } catch (Throwable var4) {
            return null;
         }

         return new NameAndId($$2, $$0.get("name").getAsString());
      } else {
         return null;
      }
   }

   public void appendTo(JsonObject $$0) {
      $$0.addProperty("uuid", this.id().toString());
      $$0.addProperty("name", this.name());
   }

   public static NameAndId createOffline(String $$0) {
      UUID $$1 = UUIDUtil.createOfflinePlayerUUID($$0);
      return new NameAndId($$1, $$0);
   }
}
