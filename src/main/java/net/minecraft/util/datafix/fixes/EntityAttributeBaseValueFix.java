package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.DoubleUnaryOperator;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityAttributeBaseValueFix extends NamedEntityFix {
   private final String attributeId;
   private final DoubleUnaryOperator valueFixer;

   public EntityAttributeBaseValueFix(Schema $$0, String $$1, String $$2, String $$3, DoubleUnaryOperator $$4) {
      super($$0, false, $$1, References.ENTITY, $$2);
      this.attributeId = $$3;
      this.valueFixer = $$4;
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), this::fixValue);
   }

   private Dynamic<?> fixValue(Dynamic<?> $$0) {
      return $$0.update("attributes", $$1 -> $$0.createList($$1.asStream().map($$0xx -> {
         String $$1x = NamespacedSchema.ensureNamespaced($$0xx.get("id").asString(""));
         if (!$$1x.equals(this.attributeId)) {
            return $$0xx;
         } else {
            double $$2 = $$0xx.get("base").asDouble(0.0);
            return $$0xx.set("base", $$0xx.createDouble(this.valueFixer.applyAsDouble($$2)));
         }
      })));
   }
}
