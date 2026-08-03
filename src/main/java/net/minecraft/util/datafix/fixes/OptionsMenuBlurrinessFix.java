package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsMenuBlurrinessFix extends DataFix {
   public OptionsMenuBlurrinessFix(Schema $$0) {
      super($$0, false);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "OptionsMenuBlurrinessFix",
         this.getInputSchema().getType(References.OPTIONS),
         $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> $$0x.update("menuBackgroundBlurriness", $$0xx -> {
            int $$1 = this.convertToIntRange($$0xx.asString("0.5"));
            return $$0xx.createString(String.valueOf($$1));
         }))
      );
   }

   private int convertToIntRange(String $$0) {
      try {
         return Math.round(Float.parseFloat($$0) * 10.0F);
      } catch (NumberFormatException var3) {
         return 5;
      }
   }
}
