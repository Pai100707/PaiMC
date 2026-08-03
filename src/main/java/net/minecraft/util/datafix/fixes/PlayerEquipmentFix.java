package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;

public class PlayerEquipmentFix extends DataFix {
   private static final Map<Integer, String> SLOT_TRANSLATIONS = Map.of(100, "feet", 101, "legs", 102, "chest", 103, "head", -106, "offhand");

   public PlayerEquipmentFix(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getTypeRaw(References.PLAYER);
      Type<?> $$1 = this.getOutputSchema().getTypeRaw(References.PLAYER);
      return this.writeFixAndRead("Player Equipment Fix", $$0, $$1, $$0x -> {
         Map<Dynamic<?>, Dynamic<?>> $$1x = new HashMap<>();
         $$0x = $$0x.update("Inventory", $$1xx -> $$1xx.createList($$1xx.asStream().filter($$2 -> {
            int $$3 = $$2.get("Slot").asInt(-1);
            String $$4 = SLOT_TRANSLATIONS.get($$3);
            if ($$4 != null) {
               $$1x.put($$1xx.createString($$4), $$2.remove("Slot"));
            }

            return $$4 == null;
         })));
         return $$0x.set("equipment", $$0x.createMap($$1x));
      });
   }
}
