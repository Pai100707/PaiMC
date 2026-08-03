package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Map;
import java.util.stream.Stream;

public class CustomModelDataExpandFix extends DataFix {
   public CustomModelDataExpandFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.DATA_COMPONENTS);
      return this.fixTypeEverywhereTyped(
         "Custom Model Data expansion", $$0, $$0x -> $$0x.update(DSL.remainderFinder(), $$0xx -> $$0xx.update("minecraft:custom_model_data", $$0xxx -> {
            float $$1 = $$0xxx.asNumber(0.0F).floatValue();
            return $$0xxx.createMap(Map.of($$0xxx.createString("floats"), $$0xxx.createList(Stream.of($$0xxx.createFloat($$1)))));
         }))
      );
   }
}
