package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V4301 extends NamespacedSchema {
   public V4301(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      $$0.registerType(
         true,
         References.ENTITY_EQUIPMENT,
         () -> DSL.optional(
            DSL.field(
               "equipment",
               DSL.optionalFields(
                  new Pair[]{
                     Pair.of("mainhand", References.ITEM_STACK.in($$0)),
                     Pair.of("offhand", References.ITEM_STACK.in($$0)),
                     Pair.of("feet", References.ITEM_STACK.in($$0)),
                     Pair.of("legs", References.ITEM_STACK.in($$0)),
                     Pair.of("chest", References.ITEM_STACK.in($$0)),
                     Pair.of("head", References.ITEM_STACK.in($$0)),
                     Pair.of("body", References.ITEM_STACK.in($$0)),
                     Pair.of("saddle", References.ITEM_STACK.in($$0))
                  }
               )
            )
         )
      );
   }
}
