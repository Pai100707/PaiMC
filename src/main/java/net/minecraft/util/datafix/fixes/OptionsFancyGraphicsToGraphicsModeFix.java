package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class OptionsFancyGraphicsToGraphicsModeFix extends DataFix {
   public OptionsFancyGraphicsToGraphicsModeFix(Schema $$0) {
      super($$0, true);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "fancyGraphics to graphicsMode",
         this.getInputSchema().getType(References.OPTIONS),
         $$0 -> $$0.update(
            DSL.remainderFinder(), $$0x -> $$0x.renameAndFixField("fancyGraphics", "graphicsMode", OptionsFancyGraphicsToGraphicsModeFix::fixGraphicsMode)
         )
      );
   }

   private static <T> Dynamic<T> fixGraphicsMode(Dynamic<T> $$0) {
      return "true".equals($$0.asString("true")) ? $$0.createString("1") : $$0.createString("0");
   }
}
