package net.minecraft.server.dialog.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.functions.StringTemplate;

public class ParsedTemplate {
   public static final Codec<ParsedTemplate> CODEC = Codec.STRING.comapFlatMap(ParsedTemplate::parse, $$0 -> $$0.raw);
   public static final Codec<String> VARIABLE_CODEC = Codec.STRING
      .validate($$0 -> StringTemplate.isValidVariableName($$0) ? DataResult.success($$0) : DataResult.error(() -> $$0 + " is not a valid input name"));
   private final String raw;
   private final StringTemplate parsed;

   private ParsedTemplate(String $$0, StringTemplate $$1) {
      this.raw = $$0;
      this.parsed = $$1;
   }

   private static DataResult<ParsedTemplate> parse(String $$0) {
      StringTemplate $$1;
      try {
         $$1 = StringTemplate.fromString($$0);
      } catch (Exception var3) {
         return DataResult.error(() -> "Failed to parse template " + $$0 + ": " + var3.getMessage());
      }

      return DataResult.success(new ParsedTemplate($$0, $$1));
   }

   public String instantiate(Map<String, String> $$0) {
      List<String> $$1 = this.parsed.variables().stream().map($$1x -> $$0.getOrDefault($$1x, "")).toList();
      return this.parsed.substitute($$1);
   }
}
