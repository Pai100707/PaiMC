package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class ChunkStatusFix extends DataFix {
   public ChunkStatusFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.CHUNK);
      Type<?> $$1 = $$0.findFieldType("Level");
      OpticFinder<?> $$2 = DSL.fieldFinder("Level", $$1);
      return this.fixTypeEverywhereTyped("ChunkStatusFix", $$0, this.getOutputSchema().getType(References.CHUNK), $$1x -> $$1x.updateTyped($$2, $$0xx -> {
         Dynamic<?> $$1xx = (Dynamic<?>)$$0xx.get(DSL.remainderFinder());
         String $$2x = $$1xx.get("Status").asString("empty");
         if (Objects.equals($$2x, "postprocessed")) {
            $$1xx = $$1xx.set("Status", $$1xx.createString("fullchunk"));
         }

         return $$0xx.set(DSL.remainderFinder(), $$1xx);
      }));
   }
}
