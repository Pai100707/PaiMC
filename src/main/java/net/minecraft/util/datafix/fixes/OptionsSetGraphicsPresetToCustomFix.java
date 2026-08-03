package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsSetGraphicsPresetToCustomFix extends DataFix {
   public OptionsSetGraphicsPresetToCustomFix(Schema $$0) {
      super($$0, true);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "graphicsPreset set to \"custom\"",
         this.getInputSchema().getType(References.OPTIONS),
         $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> $$0x.set("graphicsPreset", $$0x.createString("custom")))
      );
   }
}
