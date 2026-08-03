package net.minecraft.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.Reader;

public class LenientJsonParser {
   public static JsonElement parse(Reader $$0) throws JsonIOException, JsonSyntaxException {
      return JsonParser.parseReader($$0);
   }

   public static JsonElement parse(String $$0) throws JsonSyntaxException {
      return JsonParser.parseString($$0);
   }
}
