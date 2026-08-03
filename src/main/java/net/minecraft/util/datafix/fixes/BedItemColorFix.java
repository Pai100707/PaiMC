package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BedItemColorFix extends DataFix {
   public BedItemColorFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public TypeRewriteRule makeRule() {
      OpticFinder<Pair<String, String>> $$0 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      return this.fixTypeEverywhereTyped("BedItemColorFix", this.getInputSchema().getType(References.ITEM_STACK), $$1 -> {
         Optional<Pair<String, String>> $$2 = $$1.getOptional($$0);
         if ($$2.isPresent() && Objects.equals($$2.get().getSecond(), "minecraft:bed")) {
            Dynamic<?> $$3 = (Dynamic<?>)$$1.get(DSL.remainderFinder());
            if ($$3.get("Damage").asInt(0) == 0) {
               return $$1.set(DSL.remainderFinder(), $$3.set("Damage", $$3.createShort((short)14)));
            }
         }

         return $$1;
      });
   }
}
