package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;

public class EntityFallDistanceFloatToDoubleFix extends DataFix {
   private final TypeReference type;

   public EntityFallDistanceFloatToDoubleFix(Schema $$0, TypeReference $$1) {
      super($$0, false);
      this.type = $$1;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "EntityFallDistanceFloatToDoubleFixFor" + this.type.typeName(),
         this.getOutputSchema().getType(this.type),
         EntityFallDistanceFloatToDoubleFix::fixEntity
      );
   }

   private static Typed<?> fixEntity(Typed<?> $$0) {
      return $$0.update(
         DSL.remainderFinder(), $$0x -> $$0x.renameAndFixField("FallDistance", "fall_distance", $$0xx -> $$0xx.createDouble($$0xx.asFloat(0.0F)))
      );
   }
}
