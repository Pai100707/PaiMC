package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsMusicToastFix extends DataFix {
   public OptionsMusicToastFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "OptionsMusicToastFix",
         this.getInputSchema().getType(References.OPTIONS),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> $$0x.renameAndFixField(
               "showNowPlayingToast", "musicToast", $$1 -> $$0x.createString($$1.asString("false").equals("false") ? "never" : "pause_and_toast")
            )
         )
      );
   }
}
