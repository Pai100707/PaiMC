package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;

public class DropChancesFormatFix extends DataFix {
   private static final List<String> ARMOR_SLOT_NAMES = List.of("feet", "legs", "chest", "head");
   private static final List<String> HAND_SLOT_NAMES = List.of("mainhand", "offhand");
   private static final float DEFAULT_CHANCE = 0.085F;

   public DropChancesFormatFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "DropChancesFormatFix", this.getInputSchema().getType(References.ENTITY), $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> {
            List<Float> $$1 = parseDropChances($$0x.get("ArmorDropChances"));
            List<Float> $$2 = parseDropChances($$0x.get("HandDropChances"));
            float $$3 = $$0x.get("body_armor_drop_chance").asNumber().result().map(Number::floatValue).orElse(0.085F);
            $$0x = $$0x.remove("ArmorDropChances").remove("HandDropChances").remove("body_armor_drop_chance");
            Dynamic<?> $$4 = $$0x.emptyMap();
            $$4 = addSlotChances($$4, $$1, ARMOR_SLOT_NAMES);
            $$4 = addSlotChances($$4, $$2, HAND_SLOT_NAMES);
            if ($$3 != 0.085F) {
               $$4 = $$4.set("body", $$0x.createFloat($$3));
            }

            return !$$4.equals($$0x.emptyMap()) ? $$0x.set("drop_chances", $$4) : $$0x;
         })
      );
   }

   private static Dynamic<?> addSlotChances(Dynamic<?> $$0, List<Float> $$1, List<String> $$2) {
      for (int $$3 = 0; $$3 < $$2.size() && $$3 < $$1.size(); $$3++) {
         String $$4 = $$2.get($$3);
         float $$5 = $$1.get($$3);
         if ($$5 != 0.085F) {
            $$0 = $$0.set($$4, $$0.createFloat($$5));
         }
      }

      return $$0;
   }

   private static List<Float> parseDropChances(OptionalDynamic<?> $$0) {
      return $$0.asStream().map($$0x -> $$0x.asFloat(0.085F)).toList();
   }
}
