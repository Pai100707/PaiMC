package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class ForcePoiRebuild extends DataFix {
   public ForcePoiRebuild(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   protected TypeRewriteRule makeRule() {
      Type<Pair<String, Dynamic<?>>> $$0 = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
      if (!Objects.equals($$0, this.getInputSchema().getType(References.POI_CHUNK))) {
         throw new IllegalStateException("Poi type is not what was expected.");
      } else {
         return this.fixTypeEverywhere("POI rebuild", $$0, $$0x -> $$0xx -> $$0xx.mapSecond(ForcePoiRebuild::cap));
      }
   }

   private static <T> Dynamic<T> cap(Dynamic<T> $$0) {
      return $$0.update("Sections", $$0x -> $$0x.updateMapValues($$0xx -> $$0xx.mapSecond($$0xxx -> $$0xxx.remove("Valid"))));
   }
}
