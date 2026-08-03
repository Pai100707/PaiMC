package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class ChunkLightRemoveFix extends DataFix {
   public ChunkLightRemoveFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.CHUNK);
      Type<?> $$1 = $$0.findFieldType("Level");
      OpticFinder<?> $$2 = DSL.fieldFinder("Level", $$1);
      return this.fixTypeEverywhereTyped(
         "ChunkLightRemoveFix",
         $$0,
         this.getOutputSchema().getType(References.CHUNK),
         $$1x -> $$1x.updateTyped($$2, $$0xx -> $$0xx.update(DSL.remainderFinder(), $$0xxx -> $$0xxx.remove("isLightOn")))
      );
   }
}
