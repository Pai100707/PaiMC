package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;

public class FoodToConsumableFix extends DataFix {
   public FoodToConsumableFix(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      return this.writeFixAndRead(
         "Food to consumable fix",
         this.getInputSchema().getType(References.DATA_COMPONENTS),
         this.getOutputSchema().getType(References.DATA_COMPONENTS),
         $$0 -> {
            Optional<? extends Dynamic<?>> $$1 = $$0.get("minecraft:food").result();
            if ($$1.isPresent()) {
               float $$2 = $$1.get().get("eat_seconds").asFloat(1.6F);
               Stream<? extends Dynamic<?>> $$3 = $$1.get().get("effects").asStream();
               Stream<? extends Dynamic<?>> $$4 = $$3.map(
                  $$0x -> $$0x.emptyMap()
                     .set("type", $$0x.createString("minecraft:apply_effects"))
                     .set("effects", $$0x.createList($$0x.get("effect").result().stream()))
                     .set("probability", $$0x.createFloat($$0x.get("probability").asFloat(1.0F)))
               );
               $$0 = Dynamic.copyField($$1.get(), "using_converts_to", $$0, "minecraft:use_remainder");
               $$0 = $$0.set("minecraft:food", $$1.get().remove("eat_seconds").remove("effects").remove("using_converts_to"));
               return $$0.set(
                  "minecraft:consumable", $$0.emptyMap().set("consume_seconds", $$0.createFloat($$2)).set("on_consume_effects", $$0.createList($$4))
               );
            } else {
               return $$0;
            }
         }
      );
   }
}
