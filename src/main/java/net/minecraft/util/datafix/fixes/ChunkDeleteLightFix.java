package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class ChunkDeleteLightFix extends DataFix {
   public ChunkDeleteLightFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> $$1 = $$0.findField("sections");
      return this.fixTypeEverywhereTyped("ChunkDeleteLightFix for " + this.getOutputSchema().getVersionKey(), $$0, $$1x -> {
         $$1x = $$1x.update(DSL.remainderFinder(), $$0xx -> $$0xx.remove("isLightOn"));
         return $$1x.updateTyped($$1, $$0xx -> $$0xx.update(DSL.remainderFinder(), $$0xxx -> $$0xxx.remove("BlockLight").remove("SkyLight")));
      });
   }
}
