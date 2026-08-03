package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class DebugProfileOverlayReferenceFix extends DataFix {
   public DebugProfileOverlayReferenceFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "DebugProfileOverlayReferenceFix",
         this.getInputSchema().getType(References.DEBUG_PROFILE),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> $$0x.update(
               "custom",
               $$0xx -> $$0xx.updateMapValues(
                  $$0xxx -> $$0xxx.mapSecond($$0xxxx -> $$0xxxx.asString("").equals("inF3") ? $$0xxxx.createString("inOverlay") : $$0xxxx)
               )
            )
         )
      );
   }
}
