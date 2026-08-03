package net.minecraft.util.datafix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;

public class LegacyComponentDataFixUtils {
   private static final String EMPTY_CONTENTS = createTextComponentJson("");

   public static <T> Dynamic<T> createPlainTextComponent(DynamicOps<T> $$0, String $$1) {
      String $$2 = createTextComponentJson($$1);
      return new Dynamic($$0, $$0.createString($$2));
   }

   public static <T> Dynamic<T> createEmptyComponent(DynamicOps<T> $$0) {
      return new Dynamic($$0, $$0.createString(EMPTY_CONTENTS));
   }

   public static String createTextComponentJson(String $$0) {
      JsonObject $$1 = new JsonObject();
      $$1.addProperty("text", $$0);
      return net.minecraft.util.GsonHelper.toStableString($$1);
   }

   public static String createTranslatableComponentJson(String $$0) {
      JsonObject $$1 = new JsonObject();
      $$1.addProperty("translate", $$0);
      return net.minecraft.util.GsonHelper.toStableString($$1);
   }

   public static <T> Dynamic<T> createTranslatableComponent(DynamicOps<T> $$0, String $$1) {
      String $$2 = createTranslatableComponentJson($$1);
      return new Dynamic($$0, $$0.createString($$2));
   }

   public static String rewriteFromLenient(String $$0) {
      if (!$$0.isEmpty() && !$$0.equals("null")) {
         char $$1 = $$0.charAt(0);
         char $$2 = $$0.charAt($$0.length() - 1);
         if ($$1 == '"' && $$2 == '"' || $$1 == '{' && $$2 == '}' || $$1 == '[' && $$2 == ']') {
            try {
               JsonElement $$3 = net.minecraft.util.LenientJsonParser.parse($$0);
               if ($$3.isJsonPrimitive()) {
                  return createTextComponentJson($$3.getAsString());
               }

               return net.minecraft.util.GsonHelper.toStableString($$3);
            } catch (JsonParseException var4) {
            }
         }

         return createTextComponentJson($$0);
      } else {
         return EMPTY_CONTENTS;
      }
   }

   public static boolean isStrictlyValidJson(Dynamic<?> $$0) {
      return $$0.asString().result().filter($$0x -> {
         try {
            net.minecraft.util.StrictJsonParser.parse($$0x);
            return true;
         } catch (JsonParseException var2) {
            return false;
         }
      }).isPresent();
   }

   public static Optional<String> extractTranslationString(String $$0) {
      try {
         JsonElement $$1 = net.minecraft.util.LenientJsonParser.parse($$0);
         if ($$1.isJsonObject()) {
            JsonObject $$2 = $$1.getAsJsonObject();
            JsonElement $$3 = $$2.get("translate");
            if ($$3 != null && $$3.isJsonPrimitive()) {
               return Optional.of($$3.getAsString());
            }
         }
      } catch (JsonParseException var4) {
      }

      return Optional.empty();
   }
}
