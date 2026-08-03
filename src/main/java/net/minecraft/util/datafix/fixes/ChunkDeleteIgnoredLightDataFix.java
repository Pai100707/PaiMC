package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class ChunkDeleteIgnoredLightDataFix extends DataFix {
   public ChunkDeleteIgnoredLightDataFix(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> $$1 = $$0.findField("sections");
      return this.fixTypeEverywhereTyped("ChunkDeleteIgnoredLightDataFix", $$0, $$1x -> {
         boolean $$2 = ((Dynamic)$$1x.get(DSL.remainderFinder())).get("isLightOn").asBoolean(false);
         return !$$2 ? $$1x.updateTyped($$1, $$0xx -> $$0xx.update(DSL.remainderFinder(), $$0xxx -> $$0xxx.remove("BlockLight").remove("SkyLight"))) : $$1x;
      });
   }
}
