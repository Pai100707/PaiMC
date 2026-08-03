package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class OptionsAmbientOcclusionFix extends DataFix {
   public OptionsAmbientOcclusionFix(Schema $$0) {
      super($$0, false);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "OptionsAmbientOcclusionFix",
         this.getInputSchema().getType(References.OPTIONS),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> (Dynamic)DataFixUtils.orElse($$0x.get("ao").asString().map($$1 -> $$0x.set("ao", $$0x.createString(updateValue($$1)))).result(), $$0x)
         )
      );
   }

   private static String updateValue(String $$0) {
      return switch ($$0) {
         case "0" -> "false";
         case "1", "2" -> "true";
         default -> $$0;
      };
   }
}
