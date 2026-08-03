package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Optional;

public class ObjectiveRenderTypeFix extends DataFix {
   public ObjectiveRenderTypeFix(Schema $$0) {
      super($$0, false);
   }

   private static String getRenderType(String $$0) {
      return $$0.equals("health") ? "hearts" : "integer";
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.OBJECTIVE);
      return this.fixTypeEverywhereTyped("ObjectiveRenderTypeFix", $$0, $$0x -> $$0x.update(DSL.remainderFinder(), $$0xx -> {
         Optional<String> $$1 = $$0xx.get("RenderType").asString().result();
         if ($$1.isEmpty()) {
            String $$2 = $$0xx.get("CriteriaName").asString("");
            String $$3 = getRenderType($$2);
            return $$0xx.set("RenderType", $$0xx.createString($$3));
         } else {
            return $$0xx;
         }
      }));
   }
}
