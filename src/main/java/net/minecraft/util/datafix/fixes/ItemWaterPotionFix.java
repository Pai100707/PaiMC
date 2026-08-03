package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemWaterPotionFix extends DataFix {
   public ItemWaterPotionFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> $$1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> $$2 = $$0.findField("tag");
      return this.fixTypeEverywhereTyped(
         "ItemWaterPotionFix",
         $$0,
         $$2x -> {
            Optional<Pair<String, String>> $$3 = $$2x.getOptional($$1);
            if ($$3.isPresent()) {
               String $$4 = (String)$$3.get().getSecond();
               if ("minecraft:potion".equals($$4)
                  || "minecraft:splash_potion".equals($$4)
                  || "minecraft:lingering_potion".equals($$4)
                  || "minecraft:tipped_arrow".equals($$4)) {
                  Typed<?> $$5 = $$2x.getOrCreateTyped($$2);
                  Dynamic<?> $$6 = (Dynamic<?>)$$5.get(DSL.remainderFinder());
                  if ($$6.get("Potion").asString().result().isEmpty()) {
                     $$6 = $$6.set("Potion", $$6.createString("minecraft:water"));
                  }

                  return $$2x.set($$2, $$5.set(DSL.remainderFinder(), $$6));
               }
            }

            return $$2x;
         }
      );
   }
}
